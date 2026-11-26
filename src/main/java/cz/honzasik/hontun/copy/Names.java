package cz.honzasik.hontun.copy;

import cz.honzasik.hontun.copy.capture.Hologram;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class Names {
    private final Set<String> used = new HashSet<>();

    public String next(String raw, String fallback) {
        String base = clean(raw);
        if (base.isEmpty()) base = clean(fallback);
        if (base.isEmpty()) base = "hologram";
        if (base.startsWith("hologram_")) base = "holo_" + base.substring("hologram_".length());
        String name = base;
        int n = 2;
        while (!used.add(name.toLowerCase(Locale.ROOT))) name = base + "_" + n++;
        return name;
    }

    public String of(Hologram h) {
        String text = h.firstText();
        String fallback = h.item() != null ? h.item().path() : h.block() != null ? path(h.block()) : "hologram";
        if (text == null && h.kind() == Hologram.Kind.ARMOR_STANDS) {
            for (Hologram.Line l : h.lines()) {
                if (l.item() != null) {
                    fallback = l.item().path();
                    break;
                }
            }
        }
        return next(text, fallback);
    }

    private static String path(String id) {
        int colon = id.indexOf(':');
        return colon >= 0 ? id.substring(colon + 1) : id;
    }

    static String clean(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        boolean underscore = false;
        for (int i = 0; i < s.length() && sb.length() < 32; i++) {
            char c = Character.toLowerCase(s.charAt(i));
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                sb.append(c);
                underscore = false;
            } else if (!underscore && !sb.isEmpty()) {
                sb.append('_');
                underscore = true;
            }
        }
        while (!sb.isEmpty() && sb.charAt(sb.length() - 1) == '_') sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}
