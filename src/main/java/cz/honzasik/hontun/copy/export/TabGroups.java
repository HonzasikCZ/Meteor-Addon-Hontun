package cz.honzasik.hontun.copy.export;

import cz.honzasik.hontun.copy.StyledText;
import cz.honzasik.hontun.copy.capture.TabCapture;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class TabGroups {
    public static final class Group {
        public String name;
        public final StyledText prefix;
        public final StyledText suffix;
        public final StyledText.Look nameLook;
        public final StyledText tagPrefix;
        public final StyledText tagSuffix;
        public final StyledText.Look tagNameLook;
        public int count;
        public boolean fallback;

        Group(StyledText prefix, StyledText suffix, StyledText.Look nameLook, StyledText tagPrefix, StyledText tagSuffix, StyledText.Look tagNameLook) {
            this.prefix = prefix;
            this.suffix = suffix;
            this.nameLook = nameLook;
            this.tagPrefix = tagPrefix;
            this.tagSuffix = tagSuffix;
            this.tagNameLook = tagNameLook;
        }
    }

    public record Result(List<Group> groups, int skipped, boolean mixedNames) {
        public Group fallback() {
            for (Group g : groups) if (g.fallback) return g;
            return null;
        }
    }

    private TabGroups() {}

    public static Result of(TabCapture.Snapshot snapshot) {
        Map<String, Group> byKey = new LinkedHashMap<>();
        int skipped = 0;
        boolean mixed = false;
        boolean layout = snapshot.layout();
        for (TabCapture.Entry e : snapshot.entries()) {
            if (e.fake() && !layout) continue;
            String player = snapshot.player(e);
            if (player == null) continue;
            Split shown = split(e.shown(), player);
            if (shown == null) {
                skipped++;
                continue;
            }
            if (shown.mixed) mixed = true;
            Split tag = e.fake() || e.tag() == null ? null : split(e.tag(), player);
            String key = shown.prefix.key() + '\u0001' + shown.suffix.key() + '\u0001' + shown.look + '\u0001'
                + (tag == null ? "-" : tag.prefix.key() + '\u0001' + tag.suffix.key() + '\u0001' + tag.look);
            Group g = byKey.get(key);
            if (g == null) {
                g = new Group(shown.prefix, shown.suffix, shown.look,
                    tag == null ? null : tag.prefix, tag == null ? null : tag.suffix, tag == null ? null : tag.look);
                byKey.put(key, g);
            }
            g.count++;
        }
        List<Group> groups = new ArrayList<>(byKey.values());
        Group most = null;
        for (Group g : groups) if (most == null || g.count >= most.count) most = g;
        if (most != null) most.fallback = true;
        List<String> used = new ArrayList<>();
        int n = 1;
        for (Group g : groups) {
            if (g.fallback) {
                g.name = "default";
                continue;
            }
            String base = clean(g.prefix.plain());
            if (base.isEmpty()) base = clean(g.suffix.plain());
            if (base.isEmpty() || base.equals("default") || base.equals("per_world") || base.equals("per_server") || base.equals("none")) base = "group" + n;
            String name = base;
            int k = 2;
            while (used.contains(name)) name = base + "_" + k++;
            used.add(name);
            g.name = name;
            n++;
        }
        return new Result(List.copyOf(groups), skipped, mixed);
    }

    private record Split(StyledText prefix, StyledText suffix, StyledText.Look look, boolean mixed) {}

    private static Split split(StyledText text, String name) {
        if (name == null || name.isEmpty()) return null;
        String plain = text.plain();
        int at = plain.indexOf(name);
        if (at < 0) at = plain.toLowerCase(Locale.ROOT).indexOf(name.toLowerCase(Locale.ROOT));
        if (at < 0) return null;
        return new Split(text.slice(0, at), text.slice(at + name.length(), text.length()), text.lookAt(at), !text.uniformBetween(at, at + name.length()));
    }

    private static String clean(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length() && sb.length() < 24; i++) {
            char c = Character.toLowerCase(s.charAt(i));
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) sb.append(c);
            else if (c == '_' || c == '-') sb.append('_');
        }
        while (!sb.isEmpty() && sb.charAt(0) == '_') sb.deleteCharAt(0);
        while (!sb.isEmpty() && sb.charAt(sb.length() - 1) == '_') sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}
