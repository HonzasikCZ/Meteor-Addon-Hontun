package cz.honzasik.hontun.copy.export;

import cz.honzasik.hontun.copy.CopyStore;
import cz.honzasik.hontun.copy.Names;
import cz.honzasik.hontun.copy.Notes;
import cz.honzasik.hontun.copy.Output;
import cz.honzasik.hontun.copy.StyledText;
import cz.honzasik.hontun.copy.Target;
import cz.honzasik.hontun.copy.Yaml;
import cz.honzasik.hontun.copy.capture.Hologram;
import cz.honzasik.hontun.copy.capture.ItemInfo;
import cz.honzasik.hontun.copy.format.MiniMessageWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class FancyExport {
    private static final double NAME_TAG_GAP = 0.025;
    private static final double TEXT_ROW = 0.25;

    private FancyExport() {}

    public static Output export(List<Hologram> holograms) {
        Output out = new Output(Target.FANCY_HOLOGRAMS);
        Notes notes = out.notes;
        Names names = new Names();
        Map<String, Object> all = Yaml.map();
        for (Hologram h : holograms) {
            String name = names.of(h);
            String world = CopyStore.world(h.dimension());
            switch (h.kind()) {
                case ARMOR_STANDS -> armorStands(h, name, world, names, all, notes);
                case TEXT_DISPLAY -> {
                    all.put(name, text(h, world, notes));
                    icons(h, name, world, names, all, notes);
                }
                case ITEM_DISPLAY -> all.put(name, item(h, world, notes));
                case BLOCK_DISPLAY -> all.put(name, block(h, world, notes));
            }
        }
        Map<String, Object> root = Yaml.map();
        root.put("version", 2);
        root.put("holograms", all);
        out.files.put("FancyHolograms/holograms.yml", Yaml.write(root));
        notes.add("World names are guessed from the dimension (world, world_nether, world_the_end), fix location.world if your worlds are named differently");
        notes.add("Stop the server before replacing FancyHolograms/holograms.yml, FancyHolograms overwrites it while running");
        return out;
    }

    private static void armorStands(Hologram h, String name, String world, Names names, Map<String, Object> all, Notes notes) {
        List<StyledText> texts = new ArrayList<>();
        double top = Double.NaN;
        for (Hologram.Line l : h.lines()) {
            if (l.type() != Hologram.Type.TEXT) continue;
            texts.add(l.text() == null ? StyledText.EMPTY : l.text());
            if (Double.isNaN(top)) top = l.top();
        }
        if (!texts.isEmpty()) {
            Map<String, Object> m = Yaml.map();
            m.put("type", new Yaml.Raw("TEXT"));
            m.put("location", location(world, h.x(), top + NAME_TAG_GAP - texts.size() * TEXT_ROW, h.z(), 0f, 0f));
            common(m, null);
            List<Object> lines = new ArrayList<>();
            for (StyledText t : texts) lines.add(MiniMessageWriter.write(t, notes));
            m.put("text", lines);
            m.put("text_shadow", false);
            m.put("see_through", false);
            m.put("text_alignment", new Yaml.Raw("center"));
            m.put("update_text_interval", -1);
            all.put(name, m);
        }
        int n = 1;
        for (Hologram.Line l : h.lines()) {
            if (l.type() == Hologram.Type.TEXT || l.item() == null) continue;
            double y;
            float scale;
            String billboard;
            switch (l.type()) {
                case ICON -> {
                    y = l.at() + 0.25;
                    scale = 0.5f;
                    billboard = null;
                }
                case HEAD -> {
                    y = l.at() + 1.75;
                    scale = 0.625f;
                    billboard = "fixed";
                }
                default -> {
                    y = l.at() + 0.8975;
                    scale = 0.3125f;
                    billboard = "fixed";
                }
            }
            Map<String, Object> m = Yaml.map();
            m.put("type", new Yaml.Raw("ITEM"));
            m.put("location", location(world, h.x() + l.dx(), y, h.z() + l.dz(), l.yaw(), 0f));
            m.put("visibility_distance", -1);
            m.put("visibility", new Yaml.Raw("ALL"));
            m.put("persistent", true);
            m.put("scale_x", (double) scale);
            m.put("scale_y", (double) scale);
            m.put("scale_z", (double) scale);
            m.put("translation_x", 0.0);
            m.put("translation_y", 0.0);
            m.put("translation_z", 0.0);
            m.put("shadow_radius", 0.0);
            m.put("shadow_strength", 1.0);
            if (billboard != null) m.put("billboard", new Yaml.Raw(billboard));
            m.put("item", itemStack(l.item()));
            all.put(names.next(name + "_item_" + n++, name + "_item"), m);
        }
        if (h.lines().stream().anyMatch(l -> l.item() != null)) {
            notes.add("FancyHolograms can't mix items into a text hologram, item and head lines became separate ITEM holograms");
        }
        if (h.lines().stream().anyMatch(l -> l.type() == Hologram.Type.ENTITY)) {
            notes.add("FancyHolograms can't show mob lines, they were left out");
        }
    }

    private static void icons(Hologram h, String name, String world, Names names, Map<String, Object> all, Notes notes) {
        int n = 1;
        for (Hologram.Line l : h.lines()) {
            if (l.type() == Hologram.Type.TEXT || l.item() == null) continue;
            Map<String, Object> m = Yaml.map();
            m.put("type", new Yaml.Raw("ITEM"));
            double y = l.type() == Hologram.Type.ICON ? l.at() + 0.25 : l.at();
            m.put("location", location(world, h.x() + l.dx(), y, h.z() + l.dz(), l.yaw(), 0f));
            m.put("visibility_distance", -1);
            m.put("visibility", new Yaml.Raw("ALL"));
            m.put("persistent", true);
            m.put("scale_x", 0.5);
            m.put("scale_y", 0.5);
            m.put("scale_z", 0.5);
            m.put("translation_x", 0.0);
            m.put("translation_y", 0.0);
            m.put("translation_z", 0.0);
            m.put("shadow_radius", 0.0);
            m.put("shadow_strength", 1.0);
            m.put("item", itemStack(l.item()));
            all.put(names.next(name + "_item_" + n++, name + "_item"), m);
            notes.add("FancyHolograms can't mix items into a text hologram, icon lines became separate ITEM holograms");
        }
    }

    private static Map<String, Object> text(Hologram h, String world, Notes notes) {
        Hologram.Props p = h.props();
        Map<String, Object> m = Yaml.map();
        m.put("type", new Yaml.Raw("TEXT"));
        m.put("location", location(world, h.x(), h.y(), h.z(), h.yaw(), h.pitch()));
        common(m, p);
        List<Object> lines = new ArrayList<>();
        for (Hologram.Line l : h.lines()) {
            if (l.type() == Hologram.Type.TEXT) lines.add(l.text() == null ? "" : MiniMessageWriter.write(l.text(), notes));
            else lines.add("");
        }
        m.put("text", lines);
        m.put("text_shadow", p.shadow());
        m.put("see_through", p.seeThrough());
        m.put("text_alignment", new Yaml.Raw(p.align()));
        m.put("update_text_interval", -1);
        if (!p.defaultBackground()) {
            int bg = p.background();
            if (((bg >>> 24) & 0xFF) == 0) m.put("background", new Yaml.Raw("transparent"));
            else if (bg != 0x40000000) m.put("background", String.format(Locale.ROOT, "#%08x", bg));
        }
        if (p.opacity() != 255) notes.add("FancyHolograms can't set text opacity, it was left out");
        notes(p, notes);
        return m;
    }

    private static Map<String, Object> item(Hologram h, String world, Notes notes) {
        Hologram.Props p = h.props();
        Map<String, Object> m = Yaml.map();
        m.put("type", new Yaml.Raw("ITEM"));
        m.put("location", location(world, h.x(), h.y(), h.z(), h.yaw(), h.pitch()));
        common(m, p);
        m.put("item", itemStack(h.item()));
        if (p.context() != null && !"none".equals(p.context())) notes.add("FancyHolograms can't set the item display mode (" + p.context() + "), it shows the default one");
        notes(p, notes);
        return m;
    }

    private static Map<String, Object> block(Hologram h, String world, Notes notes) {
        Hologram.Props p = h.props();
        Map<String, Object> m = Yaml.map();
        m.put("type", new Yaml.Raw("BLOCK"));
        m.put("location", location(world, h.x(), h.y(), h.z(), h.yaw(), h.pitch()));
        common(m, p);
        String path = h.block();
        int colon = path.indexOf(':');
        if (colon >= 0) path = path.substring(colon + 1);
        m.put("block", new Yaml.Raw(path.toUpperCase(Locale.ROOT)));
        if (!h.blockDefault()) notes.add("FancyHolograms only shows the default block state, block properties were left out");
        notes(p, notes);
        return m;
    }

    private static void notes(Hologram.Props p, Notes notes) {
        if (p.rotated()) notes.add("FancyHolograms can't rotate a hologram with a transformation, the rotation was left out");
        if (p.glow() != -1) notes.add("FancyHolograms can't set a glow color, it was left out");
    }

    private static void common(Map<String, Object> m, Hologram.Props p) {
        int distance = -1;
        if (p != null && p.viewRange() >= 2 && Math.abs(p.viewRange() - Math.round(p.viewRange())) < 0.001) distance = Math.round(p.viewRange());
        m.put("visibility_distance", distance);
        m.put("visibility", new Yaml.Raw("ALL"));
        m.put("persistent", true);
        m.put("scale_x", p == null ? 1.0 : (double) p.sx());
        m.put("scale_y", p == null ? 1.0 : (double) p.sy());
        m.put("scale_z", p == null ? 1.0 : (double) p.sz());
        m.put("translation_x", p == null ? 0.0 : (double) p.tx());
        m.put("translation_y", p == null ? 0.0 : (double) p.ty());
        m.put("translation_z", p == null ? 0.0 : (double) p.tz());
        m.put("shadow_radius", p == null ? 0.0 : (double) p.shadowRadius());
        m.put("shadow_strength", p == null ? 1.0 : (double) p.shadowStrength());
        if (p != null && !"center".equals(p.billboard())) m.put("billboard", new Yaml.Raw(p.billboard()));
        if (p != null && p.brightness() != -1) {
            m.put("block_brightness", (p.brightness() >> 4) & 0xF);
            m.put("sky_brightness", (p.brightness() >> 20) & 0xF);
        }
    }

    private static Map<String, Object> location(String world, double x, double y, double z, float yaw, float pitch) {
        Map<String, Object> l = Yaml.map();
        l.put("world", world);
        l.put("x", round(x));
        l.put("y", round(y));
        l.put("z", round(z));
        l.put("yaw", round(yaw));
        l.put("pitch", round(pitch));
        return l;
    }

    private static Map<String, Object> itemStack(ItemInfo item) {
        Map<String, Object> m = Yaml.map();
        m.put("==", new Yaml.Raw("org.bukkit.inventory.ItemStack"));
        m.put("DataVersion", item.dataVersion());
        m.put("id", item.id());
        m.put("count", item.count());
        if (!item.components().isEmpty()) {
            Map<String, Object> components = Yaml.map();
            item.components().forEach(components::put);
            m.put("components", components);
        }
        m.put("schema_version", 1);
        return m;
    }

    private static double round(double d) {
        return Math.round(d * 10000.0) / 10000.0;
    }
}
