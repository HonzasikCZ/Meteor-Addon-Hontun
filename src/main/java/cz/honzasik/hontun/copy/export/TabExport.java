package cz.honzasik.hontun.copy.export;

import cz.honzasik.hontun.copy.CopyStore;
import cz.honzasik.hontun.copy.Notes;
import cz.honzasik.hontun.copy.Output;
import cz.honzasik.hontun.copy.StyledText;
import cz.honzasik.hontun.copy.Target;
import cz.honzasik.hontun.copy.Yaml;
import cz.honzasik.hontun.copy.capture.Motion;
import cz.honzasik.hontun.copy.capture.SidebarCapture;
import cz.honzasik.hontun.copy.capture.TabCapture;
import cz.honzasik.hontun.copy.format.Dialects;
import cz.honzasik.hontun.copy.format.LegacyWriter;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class TabExport {
    private TabExport() {}

    public static Output export(CopyStore.State state) {
        Output out = new Output(Target.TAB);
        Notes notes = out.notes;
        TabCapture.Snapshot tab = state.tab;
        SidebarCapture.Snapshot sidebar = state.sidebar;
        boolean layout = tab != null && tab.layout();
        TabGroups.Result groups = tab == null ? null : TabGroups.of(tab);
        Map<String, Object> animations = Yaml.map();
        Motion.Tab tabMotion = tab == null ? null : state.tabMotion;
        Motion.Sidebar sideMotion = sidebar == null ? null : state.sidebarMotion;
        if (tabMotion != null) tabMotion.notes().forEach(notes::add);
        if (sideMotion != null) sideMotion.notes().forEach(notes::add);

        Map<String, Object> root = Yaml.map();

        Map<String, Object> headerFooter = Yaml.map();
        headerFooter.put("enabled", tab != null);
        Map<String, Object> designs = Yaml.map();
        if (tab != null) {
            Map<String, Object> design = Yaml.map();
            design.put("header", block(tab.header(), tabMotion == null ? null : tabMotion.header(), "hontun_header", animations, notes));
            design.put("footer", block(tab.footer(), tabMotion == null ? null : tabMotion.footer(), "hontun_footer", animations, notes));
            designs.put("copied", design);
        }
        headerFooter.put("designs", designs);
        root.put("header-footer", headerFooter);

        Map<String, Object> names = Yaml.map();
        names.put("enabled", groups != null && !groups.groups().isEmpty());
        names.put("disable-condition", "");
        root.put("tablist-name-formatting", names);

        Map<String, Object> teams = Yaml.map();
        teams.put("enabled", true);
        teams.put("enable-collision", true);
        teams.put("invisible-nametags", false);
        List<Object> sorting = new ArrayList<>();
        if (groups != null && groups.groups().size() > 1) {
            StringBuilder sb = new StringBuilder("GROUPS:");
            boolean first = true;
            for (TabGroups.Group g : groups.groups()) {
                if (!first) sb.append(',');
                sb.append(g.name);
                first = false;
            }
            sorting.add(sb.toString());
        }
        sorting.add("PLACEHOLDER_A_TO_Z:%player%");
        teams.put("sorting-types", sorting);
        teams.put("case-sensitive-sorting", true);
        teams.put("can-see-friendly-invisibles", false);
        teams.put("disable-condition", "");
        root.put("scoreboard-teams", teams);

        Map<String, Object> list = Yaml.map();
        list.put("enabled", false);
        list.put("value", "%ping%");
        list.put("fancy-value", "&7Ping: %ping%");
        list.put("title", "TAB");
        list.put("render-type", new Yaml.Raw("INTEGER"));
        list.put("disable-condition", "");
        root.put("playerlist-objective", list);

        Map<String, Object> below = Yaml.map();
        below.put("enabled", false);
        below.put("value", "%health%");
        below.put("fancy-value", "&c%health%");
        below.put("fancy-value-default", "NPC");
        below.put("title", "&cHealth");
        below.put("disable-condition", "");
        below.put("view-distance", 10);
        root.put("belowname-objective", below);

        root.put("prevent-spectator-effect", single("enabled", false));
        root.put("bossbar", single("enabled", false));

        Map<String, Object> scoreboard = Yaml.map();
        scoreboard.put("enabled", sidebar != null);
        scoreboard.put("toggle-command", "/sb");
        scoreboard.put("remember-toggle-choice", false);
        scoreboard.put("hidden-by-default", false);
        scoreboard.put("delay-on-join-milliseconds", 0);
        Map<String, Object> boards = Yaml.map();
        if (sidebar != null) {
            Map<String, Object> board = Yaml.map();
            Motion.Anim titleAnim = sideMotion == null ? null : sideMotion.title();
            board.put("title", titleAnim != null
                ? animation("hontun_title", titleAnim, Dialects.TAB_SCOREBOARD, animations, notes)
                : LegacyWriter.write(sidebar.title(), Dialects.TAB_SCOREBOARD, notes));
            List<Object> rows = new ArrayList<>();
            for (int i = 0; i < sidebar.lines().size(); i++) {
                SidebarCapture.Line line = sidebar.lines().get(i);
                Motion.Anim leftAnim = sideMotion == null ? null : sideMotion.lines().get(i);
                Motion.Anim rightAnim = sideMotion == null ? null : sideMotion.rights().get(i);
                String left = leftAnim != null
                    ? animation("hontun_line_" + (i + 1), leftAnim, Dialects.TAB_SCOREBOARD, animations, notes)
                    : LegacyWriter.write(line.text(), Dialects.TAB_SCOREBOARD, notes);
                if (rightAnim != null) {
                    left += "||" + animation("hontun_line_" + (i + 1) + "_right", rightAnim, Dialects.TAB_SCOREBOARD, animations, notes);
                } else if (line.right() != null) {
                    left += "||" + LegacyWriter.write(line.right(), Dialects.TAB_SCOREBOARD, notes);
                }
                rows.add(left);
            }
            board.put("lines", rows);
            boards.put("copied", board);
        }
        scoreboard.put("scoreboards", boards);
        root.put("scoreboard", scoreboard);

        root.put("layout", layout(tab, layout, notes));

        Map<String, Object> ping = Yaml.map();
        ping.put("enabled", false);
        ping.put("value", 0);
        root.put("ping-spoof", ping);

        Map<String, Object> global = Yaml.map();
        global.put("enabled", false);
        global.put("update-latency", false);
        root.put("global-playerlist", global);

        Map<String, Object> placeholders = Yaml.map();
        placeholders.put("date-format", "dd.MM.yyyy");
        placeholders.put("time-format", "[HH:mm:ss / h:mm a]");
        placeholders.put("time-offset", 0);
        placeholders.put("register-tab-expansion", false);
        placeholders.put("locale", "en-US");
        root.put("placeholders", placeholders);
        root.put("placeholder-output-replacements", Yaml.map());
        root.put("conditions", Yaml.map());
        root.put("placeholder-refresh-intervals", single("default-refresh-interval", 500));
        root.put("assign-groups-by-permissions", false);
        List<Object> finding = new ArrayList<>();
        if (groups != null) for (TabGroups.Group g : groups.groups()) finding.add(g.name);
        if (finding.isEmpty()) finding.add("default");
        root.put("primary-group-finding-list", finding);
        root.put("permission-refresh-interval", 1000);
        root.put("debug", false);
        root.put("mysql", single("enabled", false));

        Map<String, Object> proxy = Yaml.map();
        proxy.put("enabled", true);
        proxy.put("channel-name-suffix", "custom");
        proxy.put("type", new Yaml.Raw("PLUGIN"));
        proxy.put("plugin", single("name", new Yaml.Raw("RedisBungee")));
        proxy.put("redis", single("url", "redis://:password@localhost:6379/0"));
        Map<String, Object> rabbit = Yaml.map();
        rabbit.put("exchange", "plugin");
        rabbit.put("url", "amqp://guest:guest@localhost:5672/%2F");
        proxy.put("rabbitmq", rabbit);
        root.put("proxy-support", proxy);

        Map<String, Object> components = Yaml.map();
        components.put("minimessage-support", true);
        components.put("disable-shadow-for-heads", true);
        root.put("components", components);
        root.put("config-version", 7);
        root.put("per-world-playerlist", single("enabled", false));
        root.put("compensate-for-packetevents-bug", false);
        root.put("use-online-uuid-in-tablist", true);

        out.files.put("TAB/config.yml", Yaml.write(root));
        if (!animations.isEmpty()) {
            out.files.put("TAB/animations.yml", Yaml.write(animations));
            notes.add(animations.size() + " animation(s) were copied into TAB/animations.yml (" + String.join(", ", animations.keySet()) + ")");
        }

        Map<String, Object> groupsFile = Yaml.map();
        if (groups != null && !groups.groups().isEmpty()) {
            TabGroups.Group fallback = groups.fallback();
            if (fallback != null) groupsFile.put("_DEFAULT_", props(fallback, null, notes));
            for (TabGroups.Group g : groups.groups()) {
                if (g.fallback) continue;
                groupsFile.put(g.name, props(g, fallback, notes));
            }
            if (groups.groups().size() > 1) {
                notes.add("TAB group names in groups.yml are guessed from the prefixes, rename them to your permission groups (the most common format is _DEFAULT_)");
            }
            if (groups.mixedNames()) notes.add("Player names drawn with a gradient or several styles were copied with the first letter's style only");
        } else {
            Map<String, Object> blank = Yaml.map();
            blank.put("tabprefix", "");
            blank.put("tabsuffix", "");
            groupsFile.put("_DEFAULT_", blank);
        }
        out.files.put("TAB/groups.yml", Yaml.write(groupsFile));
        if (groups != null && groups.skipped() > 0) notes.add(groups.skipped() + " tab entries show a nickname instead of the player name, their format was not copied");
        if (tab != null && !layout && tab.entries().stream().anyMatch(TabCapture.Entry::fake)) notes.add("Fake tab entries (NPCs, decorations) were left out");
        return out;
    }

    private static Map<String, Object> props(TabGroups.Group g, TabGroups.Group fallback, Notes notes) {
        Map<String, Object> p = Yaml.map();
        p.put("tabprefix", LegacyWriter.write(g.prefix, Dialects.TAB, notes, StyledText.Look.UNKNOWN, g.nameLook));
        p.put("tabsuffix", LegacyWriter.write(g.suffix, Dialects.TAB, notes, g.nameLook, null));
        if (g.tagPrefix != null) {
            p.put("tagprefix", LegacyWriter.write(g.tagPrefix, Dialects.TAB, notes, StyledText.Look.UNKNOWN, g.tagNameLook));
            p.put("tagsuffix", LegacyWriter.write(g.tagSuffix, Dialects.TAB, notes, g.tagNameLook, null));
        } else if (fallback != null && fallback.tagPrefix != null) {
            p.put("tagprefix", "&f");
            p.put("tagsuffix", "");
        }
        return p;
    }

    private static Map<String, Object> layout(TabCapture.Snapshot tab, boolean enabled, Notes notes) {
        Map<String, Object> layout = Yaml.map();
        layout.put("enabled", enabled);
        layout.put("direction", new Yaml.Raw("COLUMNS"));
        layout.put("default-skin", "mineskin:37e93c8e12cd426cb28fce31969e0674");
        layout.put("enable-remaining-players-text", true);
        layout.put("remaining-players-text", "... and %s more");
        layout.put("empty-slot-ping-value", 1000);
        Map<String, Object> layouts = Yaml.map();
        if (enabled) {
            Map<String, Object> copied = Yaml.map();
            List<TabCapture.Entry> entries = tab.entries();
            copied.put("slot-count", entries.size());
            List<Object> fixed = new ArrayList<>();
            List<Integer> playerSlots = new ArrayList<>();
            for (int i = 0; i < entries.size(); i++) {
                TabCapture.Entry e = entries.get(i);
                int slot = i + 1;
                if (tab.player(e) != null) {
                    playerSlots.add(slot);
                    continue;
                }
                String text = LegacyWriter.write(e.shown(), Dialects.TAB_LAYOUT, notes);
                String skin = e.skinValue() != null && e.skinSignature() != null ? "signed_texture:" + e.skinValue() + ";" + e.skinSignature() : "";
                if (text.isEmpty() && skin.isEmpty()) continue;
                if (text.isEmpty()) text = " ";
                fixed.add(slot + "|" + text + "|" + skin + "|" + Math.max(0, e.latency()));
            }
            copied.put("fixed-slots", fixed);
            if (!playerSlots.isEmpty()) {
                Map<String, Object> players = Yaml.map();
                players.put("slots", ranges(playerSlots));
                copied.put("groups", single("players", players));
            } else {
                notes.add("No player names were found in the TAB layout, so the copied layout has no slots for real players");
            }
            layouts.put("copied", copied);
            if (tab.entries().stream().anyMatch(e -> e.shown().plain().contains("|"))) notes.add("TAB layout text can't contain |, it was replaced with │");
        }
        layout.put("layouts", layouts);
        return layout;
    }

    private static List<Object> ranges(List<Integer> slots) {
        List<Object> out = new ArrayList<>();
        int start = slots.getFirst();
        int prev = start;
        for (int i = 1; i <= slots.size(); i++) {
            int cur = i < slots.size() ? slots.get(i) : Integer.MIN_VALUE;
            if (cur == prev + 1) {
                prev = cur;
                continue;
            }
            out.add(start == prev ? String.valueOf(start) : start + "-" + prev);
            start = cur;
            prev = cur;
        }
        return out;
    }

    private static List<Object> block(StyledText text, Motion.Block motion, String name, Map<String, Object> animations, Notes notes) {
        if (motion != null && motion.whole() != null) {
            List<Object> out = new ArrayList<>();
            out.add(animation(name, motion.whole(), Dialects.TAB, animations, notes));
            return out;
        }
        List<Object> out = lines(text, notes);
        if (motion == null || motion.lines() == null) return out;
        for (Map.Entry<Integer, Motion.Anim> e : motion.lines().entrySet()) {
            int i = e.getKey();
            if (i >= out.size()) continue;
            out.set(i, animation(name + "_" + (i + 1), e.getValue(), Dialects.TAB, animations, notes));
        }
        return out;
    }

    private static String animation(String name, Motion.Anim anim, LegacyWriter.Dialect dialect, Map<String, Object> animations, Notes notes) {
        Map<String, Object> a = Yaml.map();
        a.put("change-interval", Math.max(50, Math.round(anim.interval() / 50f) * 50));
        List<Object> texts = new ArrayList<>();
        Map<StyledText, String> cache = new IdentityHashMap<>();
        for (StyledText frame : anim.frames()) texts.add(cache.computeIfAbsent(frame, f -> LegacyWriter.write(f, dialect, notes)));
        a.put("texts", texts);
        animations.put(name, a);
        return "%animation:" + name + "%";
    }

    static List<Object> lines(StyledText text, Notes notes) {
        List<Object> out = new ArrayList<>();
        if (text == null || text.isEmpty()) return out;
        for (StyledText line : text.lines()) out.add(LegacyWriter.write(line, Dialects.TAB, notes));
        return out;
    }

    static Map<String, Object> single(String key, Object value) {
        Map<String, Object> m = Yaml.map();
        m.put(key, value);
        return m;
    }

    static String lower(String s) {
        return s.toLowerCase(Locale.ROOT);
    }
}
