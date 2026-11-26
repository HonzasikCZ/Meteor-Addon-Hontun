package cz.honzasik.hontun.copy.export;

import cz.honzasik.hontun.copy.CopyStore;
import cz.honzasik.hontun.copy.Names;
import cz.honzasik.hontun.copy.Notes;
import cz.honzasik.hontun.copy.Output;
import cz.honzasik.hontun.copy.Recorder;
import cz.honzasik.hontun.copy.StyledText;
import cz.honzasik.hontun.copy.Target;
import cz.honzasik.hontun.copy.Yaml;
import cz.honzasik.hontun.copy.capture.Hologram;
import cz.honzasik.hontun.copy.capture.ItemInfo;
import cz.honzasik.hontun.copy.capture.Motion;
import cz.honzasik.hontun.copy.capture.TabCapture;
import cz.honzasik.hontun.copy.format.Dialects;
import cz.honzasik.hontun.copy.format.LegacyWriter;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CmiExport {
    private static final double ROW = 0.25;
    private static final int MAX_FRAMES = 1200;

    private CmiExport() {}

    public static Output tab(CopyStore.State state) {
        Output out = new Output(Target.CMI);
        TabCapture.Snapshot tab = state.tab;
        if (tab == null) return out.fail("nothing captured");
        Notes notes = out.notes;
        TabGroups.Result groups = TabGroups.of(tab);

        Map<String, Object> list = Yaml.map();
        boolean grouped = groups.groups().size() > 1;
        list.put("GroupedEnabled", grouped);
        list.put("UpdateTabListNames", true);
        Motion.Tab motion = state.tabMotion;
        Map<String, Object> header = TabExport.single("1", lines(tab.header(), notes));
        Map<String, Object> footer = TabExport.single("1", lines(tab.footer(), notes));
        int auto = 60;
        if (motion != null) {
            motion.notes().forEach(notes::add);
            if (motion.header().animated() || motion.footer().animated()) {
                int[] intervals = Recorder.intervals(motion.header(), motion.footer());
                long step = 0;
                for (int iv : intervals) step = Recorder.gcd(step, iv);
                step = Math.max(50, step);
                long cycle = Recorder.cycle(motion.header(), motion.footer());
                int repeat = 1;
                if (step % 1000 == 0) {
                    auto = (int) (step / 1000);
                } else {
                    auto = 0;
                    repeat = (int) (step / 50);
                    notes.add("CMI switches tab frames only every whole second or every tick, so this animation uses AutoInterval 0 (the tab is updated every tick)");
                }
                long needed = Math.max(1, cycle / step);
                int count;
                if (needed * repeat > MAX_FRAMES) {
                    count = Math.max(1, MAX_FRAMES / repeat);
                    notes.add("The tab animation is very long, CMI got only its first " + count + " frames");
                } else {
                    count = (int) needed;
                }
                header = frames(Recorder.compose(tab.header().isEmpty() ? List.of() : tab.header().lines(), motion.header().lines(), motion.header().whole(), (int) step, count), repeat, notes);
                footer = frames(Recorder.compose(tab.footer().isEmpty() ? List.of() : tab.footer().lines(), motion.footer().lines(), motion.footer().whole(), (int) step, count), repeat, notes);
            }
        }
        list.put("Updates", TabExport.single("AutoInterval", auto));
        list.put("addTabListHeader", true);
        list.put("addTabListFooter", true);

        Map<String, Object> general = Yaml.map();
        TabGroups.Group fallback = groups.fallback();
        general.put("PlayerName", fallback == null ? "[playerName]" : name(fallback, notes));
        general.put("Header", header);
        general.put("Footer", footer);
        list.put("GeneralFormat", general);

        if (grouped) {
            Map<String, Object> formats = Yaml.map();
            List<TabGroups.Group> ranked = new ArrayList<>();
            for (TabGroups.Group g : groups.groups()) if (!g.fallback) ranked.add(g);
            int number = ranked.size();
            StringBuilder perms = new StringBuilder();
            for (TabGroups.Group g : ranked) {
                formats.put(String.valueOf(number), TabExport.single("PlayerName", name(g, notes)));
                if (!perms.isEmpty()) perms.append(", ");
                perms.append("cmi.tablist.").append(number).append(" = ").append(g.name);
                number--;
            }
            list.put("GroupFormat", formats);
            notes.add("CMI tab formats are picked by permission: " + perms);
        }
        list.put("useGeneralName", true);
        list.put("useGeneralHeader", true);
        list.put("useGeneralFooter", true);
        list.put("Sorting", TabExport.single("Enabled", false));

        out.files.put("CMI/Settings/TabList.yml", Yaml.write(TabExport.single("TabList", list)));
        if (tab.layout()) notes.add("CMI has no fixed tab layouts, only the header, footer and player names were copied");
        if (groups.skipped() > 0) notes.add(groups.skipped() + " tab entries show a nickname instead of the player name, their format was not copied");
        if (groups.mixedNames()) notes.add("Player names drawn with a gradient or several styles were copied with the first letter's style only");
        for (TabGroups.Group g : groups.groups()) {
            if (g.tagPrefix != null && (!g.tagPrefix.isBlank() || !g.tagSuffix.isBlank())) {
                notes.add("Name tag prefixes above heads are a separate CMI feature (NamePlates), they were not copied");
                break;
            }
        }
        return out;
    }

    private static String name(TabGroups.Group g, Notes notes) {
        return LegacyWriter.write(g.prefix, Dialects.CMI, notes, StyledText.Look.UNKNOWN, g.nameLook) + "[playerName]" + LegacyWriter.write(g.suffix, Dialects.CMI, notes, g.nameLook, null);
    }

    private static Map<String, Object> frames(List<List<StyledText>> frames, int repeat, Notes notes) {
        Map<String, Object> out = Yaml.map();
        Map<StyledText, String> cache = new IdentityHashMap<>();
        int n = 1;
        for (List<StyledText> frame : frames) {
            List<Object> lines = new ArrayList<>();
            for (StyledText line : frame) {
                lines.add(cache.computeIfAbsent(line, l -> {
                    String w = LegacyWriter.write(l, Dialects.CMI, notes);
                    return w.isEmpty() ? " " : w;
                }));
            }
            for (int r = 0; r < repeat; r++) out.put(String.valueOf(n++), lines);
        }
        return out;
    }

    private static List<Object> lines(StyledText text, Notes notes) {
        List<Object> out = new ArrayList<>();
        if (text == null || text.isEmpty()) return out;
        for (StyledText line : text.lines()) {
            String s = LegacyWriter.write(line, Dialects.CMI, notes);
            out.add(s.isEmpty() ? " " : s);
        }
        return out;
    }

    public static Output holograms(List<Hologram> holograms) {
        Output out = new Output(Target.CMI);
        Notes notes = out.notes;
        Names names = new Names();
        Map<String, Object> root = Yaml.map();
        for (Hologram h : holograms) {
            if (h.kind() == Hologram.Kind.BLOCK_DISPLAY) {
                notes.add("CMI can't show a standalone block display, it was skipped");
                continue;
            }
            root.put(names.of(h), hologram(h, notes));
        }
        out.files.put("CMI/Saves/Holograms.yml", root.isEmpty() ? "{}\n" : Yaml.write(root));
        notes.add("World names are guessed from the dimension (world, world_nether, world_the_end), fix Loc if your worlds are named differently");
        return out;
    }

    public static String unsupported(Hologram h) {
        return h.kind() == Hologram.Kind.BLOCK_DISPLAY ? "no block displays" : null;
    }

    private static Map<String, Object> hologram(Hologram h, Notes notes) {
        Map<String, Object> m = Yaml.map();
        String world = CopyStore.world(h.dimension());
        switch (h.kind()) {
            case ARMOR_STANDS -> {
                List<Hologram.Line> lines = new ArrayList<>();
                for (Hologram.Line l : h.lines()) if (l.type() != Hologram.Type.ENTITY) lines.add(l);
                if (lines.size() < h.lines().size()) notes.add("CMI can't show mob lines in a hologram, they were left out");
                if (lines.isEmpty()) lines.add(h.lines().getFirst());
                Hologram.Line first = lines.getFirst();
                double rows = (lines.size() - 1) * ROW;
                double base = switch (first.type()) {
                    case ICON -> first.at() - rows;
                    case HEAD, SMALLHEAD -> first.at() + 0.85 - rows;
                    default -> first.top() - 0.2 - rows;
                };
                m.put("Loc", loc(world, h.x(), base, h.z()));
                m.put("ArmorStand", true);
                m.put("Settings", TabExport.single("Bill", new Yaml.Raw("CENTER")));
                m.put("Lines", lines(lines, notes));
            }
            case TEXT_DISPLAY -> {
                Hologram.Props p = h.props();
                m.put("Loc", loc(world, h.x(), h.y(), h.z()));
                m.put("Settings", settings(h, p, notes));
                m.put("Text", text(p));
                m.put("Lines", lines(h.lines(), notes));
                if (p.rotated()) notes.add("CMI can't rotate a hologram with a transformation, the rotation was left out");
                if (p.glow() != -1) notes.add("CMI can't set a glow color on text holograms, it was left out");
            }
            case ITEM_DISPLAY -> {
                Hologram.Props p = h.props();
                double lift = ROW / 1.8;
                m.put("Loc", loc(world, h.x() + p.tx(), h.y() + p.ty() - lift, h.z() + p.tz()));
                Map<String, Object> settings = Yaml.map();
                settings.put("Bill", new Yaml.Raw(p.billboard().toUpperCase(Locale.ROOT)));
                if (h.yaw() != 0 || h.pitch() != 0) settings.put("Dir", vec(h.pitch(), h.yaw(), 0));
                if (p.brightness() != -1) settings.put("Light", light(p.brightness()));
                m.put("Settings", settings);
                m.put("Icon", TabExport.single("Scale", vec(p.sx() * 2, p.sy() * 2, p.sz() * 2)));
                List<Object> lines = new ArrayList<>();
                lines.add("SICON:" + item(h.item()));
                m.put("Lines", lines);
                if (p.rotated()) notes.add("CMI can't rotate an item hologram with a transformation, the rotation was left out");
            }
            default -> {}
        }
        return m;
    }

    private static Map<String, Object> settings(Hologram h, Hologram.Props p, Notes notes) {
        Map<String, Object> s = Yaml.map();
        s.put("Bill", new Yaml.Raw(p.billboard().toUpperCase(Locale.ROOT)));
        if (h.yaw() != 0 || h.pitch() != 0) s.put("Dir", vec(h.pitch(), h.yaw(), 0));
        float oz = Math.abs(p.tz() - 0.001f) < 0.0005f ? 0f : p.tz();
        if (Math.abs(p.tx()) > 0.0005 || Math.abs(p.ty()) > 0.0005 || Math.abs(oz) > 0.0005) s.put("Offset", vec(p.tx(), p.ty(), oz));
        float sx = Math.abs(p.sx() - 1f) < 0.0001f ? 1f : p.sx();
        float sy = Math.abs(p.sy() - 1f) < 0.0001f ? 1f : p.sy();
        if (sx != 1f || sy != 1f) s.put("Scale", num(sx) + ";" + num(sy));
        if (p.brightness() != -1) s.put("Light", light(p.brightness()));
        if (p.viewRange() >= 2 && Math.abs(p.viewRange() - Math.round(p.viewRange())) < 0.001) {
            s.put("Range", TabExport.single("Visual", Math.round(p.viewRange())));
        }
        return s;
    }

    private static Map<String, Object> text(Hologram.Props p) {
        Map<String, Object> t = Yaml.map();
        if (!"center".equals(p.align())) t.put("Align", new Yaml.Raw(p.align().toUpperCase(Locale.ROOT)));
        int bg = p.defaultBackground() ? 0x40000000 : p.background();
        t.put("BColor", "{#" + LegacyWriter.hex6(bg) + "}");
        t.put("BAlpha", (bg >>> 24) & 0xFF);
        if (p.opacity() != 255) t.put("TAlpha", p.opacity());
        if (!p.shadow()) t.put("Shadow", false);
        if (p.seeThrough()) t.put("See", true);
        return t;
    }

    private static List<Object> lines(List<Hologram.Line> lines, Notes notes) {
        List<Object> out = new ArrayList<>();
        for (Hologram.Line l : lines) {
            switch (l.type()) {
                case TEXT -> {
                    String s = l.text() == null ? "" : LegacyWriter.write(l.text(), Dialects.CMI, notes);
                    out.add(s.isEmpty() ? "_" : s);
                }
                case ICON -> out.add("ICON:" + item(l.item()));
                case HEAD, SMALLHEAD -> out.add("SICON:" + item(l.item()));
                case ENTITY -> {}
            }
        }
        return out;
    }

    static String item(ItemInfo item) {
        if (item == null) return "STONE";
        StringBuilder sb = new StringBuilder();
        if (item.isHead() && item.headTexture() != null) sb.append("player_head:").append(item.headTexture());
        else if (item.isHead() && item.headName() != null) sb.append("head:").append(item.headName());
        else sb.append(item.material());
        if (item.count() > 1 && !item.isHead()) sb.append(';').append(item.count());
        if (item.glint()) sb.append("%enchanted%");
        if (item.customModelData() != null) sb.append("%CustomModelData:").append(item.customModelData()).append('%');
        return sb.toString();
    }

    private static String loc(String world, double x, double y, double z) {
        return world + ";" + num(x) + ";" + num(y) + ";" + num(z);
    }

    private static String vec(double a, double b, double c) {
        return num(a) + ";" + num(b) + ";" + num(c);
    }

    private static String light(int packed) {
        int block = (packed >> 4) & 0xF;
        int sky = (packed >> 20) & 0xF;
        return block + ";" + sky + ";0";
    }

    static String num(double d) {
        String s = String.format(Locale.ROOT, "%.4f", d);
        while (s.contains(".") && (s.endsWith("0") || s.endsWith("."))) s = s.substring(0, s.length() - 1);
        return s.equals("-0") ? "0" : s;
    }
}
