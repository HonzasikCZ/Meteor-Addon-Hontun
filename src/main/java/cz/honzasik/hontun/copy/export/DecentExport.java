package cz.honzasik.hontun.copy.export;

import cz.honzasik.hontun.copy.CopyStore;
import cz.honzasik.hontun.copy.Names;
import cz.honzasik.hontun.copy.Notes;
import cz.honzasik.hontun.copy.Output;
import cz.honzasik.hontun.copy.Target;
import cz.honzasik.hontun.copy.Yaml;
import cz.honzasik.hontun.copy.capture.Hologram;
import cz.honzasik.hontun.copy.capture.ItemInfo;
import cz.honzasik.hontun.copy.format.Dialects;
import cz.honzasik.hontun.copy.format.LegacyWriter;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DecentExport {
    private DecentExport() {}

    public static String unsupported(Hologram h) {
        return h.displays() ? "only armor stand holograms" : null;
    }

    public static Output export(List<Hologram> holograms) {
        Output out = new Output(Target.DECENT_HOLOGRAMS);
        Notes notes = out.notes;
        Names names = new Names();
        for (Hologram h : holograms) {
            if (unsupported(h) != null) continue;
            String name = names.of(h);
            out.files.put("DecentHolograms/holograms/" + name + ".yml", Yaml.write(hologram(h, notes)));
        }
        notes.add("World names are guessed from the dimension (world, world_nether, world_the_end), fix location if your worlds are named differently");
        return out;
    }

    private static Map<String, Object> hologram(Hologram h, Notes notes) {
        Map<String, Object> m = Yaml.map();
        String world = CopyStore.world(h.dimension());
        List<Hologram.Line> src = h.lines();
        m.put("location", world + ":" + fmt(h.x()) + ":" + fmt(slotTop(src.getFirst())) + ":" + fmt(h.z()));
        m.put("enabled", true);
        m.put("display-range", 48);
        m.put("update-range", 48);
        m.put("update-interval", 20);
        m.put("facing", (double) Mth.wrapDegrees(h.yaw()));
        m.put("down-origin", false);

        List<Object> lines = new ArrayList<>();
        for (int i = 0; i < src.size(); i++) {
            Hologram.Line l = src.get(i);
            Map<String, Object> line = Yaml.map();
            line.put("content", content(l, notes));
            double height = i + 1 < src.size() ? Math.max(0.0, slotTop(l) - slotTop(src.get(i + 1))) : defaultHeight(l);
            line.put("height", round(height));
            if (Math.abs(l.dx()) > 0.001) line.put("offsetX", round(l.dx()));
            if (Math.abs(l.dz()) > 0.001) line.put("offsetZ", round(l.dz()));
            lines.add(line);
        }
        Map<String, Object> page = Yaml.map();
        page.put("lines", lines);
        page.put("actions", Yaml.map());
        List<Object> pages = new ArrayList<>();
        pages.add(page);
        m.put("pages", pages);
        return m;
    }

    private static String content(Hologram.Line l, Notes notes) {
        return switch (l.type()) {
            case TEXT -> l.text() == null ? "" : LegacyWriter.write(l.text(), Dialects.DECENT, notes);
            case ICON -> "#ICON: " + item(l.item(), notes);
            case HEAD -> "#HEAD: " + item(l.item(), notes);
            case SMALLHEAD -> "#SMALLHEAD: " + item(l.item(), notes);
            case ENTITY -> "#ENTITY: " + entity(l.entity());
        };
    }

    private static String item(ItemInfo item, Notes notes) {
        if (item == null) return "STONE";
        StringBuilder sb = new StringBuilder(item.material());
        if (item.isHead()) {
            if (item.headTexture() != null) sb.append(" (").append(item.headTexture()).append(')');
            else if (item.headName() != null) sb.append(" (").append(item.headName()).append(')');
        }
        if (item.glint()) sb.append(" !ENCHANTED");
        if (item.customModelData() != null) sb.append(" {CustomModelData:").append(item.customModelData()).append('}');
        if (item.components().containsKey("minecraft:item_model")) notes.add("DecentHolograms can't set an item_model on icons, it was left out");
        return sb.toString();
    }

    private static double slotTop(Hologram.Line l) {
        return switch (l.type()) {
            case TEXT, ENTITY -> l.top();
            case ICON -> l.rider() ? l.top() : l.at() + 0.55;
            case HEAD -> l.at() + 2.0;
            case SMALLHEAD -> l.at() + 1.1875;
        };
    }

    private static double defaultHeight(Hologram.Line l) {
        return switch (l.type()) {
            case TEXT -> 0.3;
            case ICON -> 0.6;
            case HEAD -> 0.75;
            case SMALLHEAD -> 0.6;
            case ENTITY -> Math.max(0.0, l.top() - l.at() - 0.25);
        };
    }

    private static String entity(String id) {
        if (id == null) return "PIG";
        int colon = id.indexOf(':');
        return (colon >= 0 ? id.substring(colon + 1) : id).toUpperCase(Locale.ROOT);
    }

    private static double round(double d) {
        return Math.round(d * 10000.0) / 10000.0;
    }

    private static String fmt(double d) {
        return String.format(Locale.ROOT, "%.3f", d);
    }
}
