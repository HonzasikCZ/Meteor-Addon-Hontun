package cz.honzasik.hontun.copy;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class Yaml {
    private static final Pattern PLAIN_KEY = Pattern.compile("[A-Za-z_][A-Za-z0-9_\\-.]*");

    private Yaml() {}

    public static Map<String, Object> map() {
        return new LinkedHashMap<>();
    }

    public static String write(Map<String, ?> root) {
        StringBuilder sb = new StringBuilder();
        writeMap(sb, root, 0);
        return sb.toString();
    }

    private static void writeMap(StringBuilder sb, Map<String, ?> map, int indent) {
        for (Map.Entry<String, ?> e : map.entrySet()) {
            pad(sb, indent);
            sb.append(key(e.getKey())).append(':');
            writeValue(sb, e.getValue(), indent);
        }
    }

    private static void writeValue(StringBuilder sb, Object value, int indent) {
        if (value instanceof Map<?, ?> m) {
            if (m.isEmpty()) {
                sb.append(" {}\n");
                return;
            }
            sb.append('\n');
            writeMap(sb, cast(m), indent + 2);
        } else if (value instanceof List<?> list) {
            if (list.isEmpty()) {
                sb.append(" []\n");
                return;
            }
            sb.append('\n');
            writeList(sb, list, indent + 2);
        } else {
            sb.append(' ').append(scalar(value)).append('\n');
        }
    }

    private static void writeList(StringBuilder sb, List<?> list, int indent) {
        for (Object item : list) {
            pad(sb, indent);
            if (item instanceof Map<?, ?> m && !m.isEmpty()) {
                sb.append("- ");
                boolean first = true;
                for (Map.Entry<String, ?> e : cast(m).entrySet()) {
                    if (!first) pad(sb, indent + 2);
                    first = false;
                    sb.append(key(e.getKey())).append(':');
                    writeValue(sb, e.getValue(), indent + 2);
                }
            } else if (item instanceof List<?> inner && !inner.isEmpty()) {
                sb.append("-\n");
                writeList(sb, inner, indent + 2);
            } else if (item instanceof Map<?, ?>) {
                sb.append("- {}\n");
            } else if (item instanceof List<?>) {
                sb.append("- []\n");
            } else {
                sb.append("- ").append(scalar(item)).append('\n');
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, ?> cast(Map<?, ?> m) {
        return (Map<String, ?>) m;
    }

    private static void pad(StringBuilder sb, int n) {
        sb.append(" ".repeat(n));
    }

    private static String key(String k) {
        return PLAIN_KEY.matcher(k).matches() && !reserved(k) ? k : quote(k);
    }

    private static boolean reserved(String s) {
        return switch (s.toLowerCase()) {
            case "true", "false", "yes", "no", "on", "off", "null", "y", "n", "~" -> true;
            default -> false;
        };
    }

    public static String scalar(Object v) {
        if (v == null) return "''";
        if (v instanceof Boolean b) return b.toString();
        if (v instanceof Integer || v instanceof Long || v instanceof Short || v instanceof Byte) return v.toString();
        if (v instanceof Float f) return number(f.doubleValue());
        if (v instanceof Double d) return number(d);
        if (v instanceof Raw r) return r.text();
        return quote(v.toString());
    }

    public static String number(double d) {
        if (Double.isNaN(d) || Double.isInfinite(d)) return "0.0";
        String s = BigDecimal.valueOf(d).stripTrailingZeros().toPlainString();
        if (s.equals("-0")) s = "0";
        if (!s.contains(".")) s += ".0";
        return s;
    }

    public static String quote(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 2);
        boolean control = false;
        for (int i = 0; i < s.length(); i++) {
            if (special(s, i)) {
                control = true;
                break;
            }
        }
        if (control) {
            sb.append('"');
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                    case '"' -> sb.append("\\\"");
                    case '\\' -> sb.append("\\\\");
                    case '\n' -> sb.append("\\n");
                    case '\t' -> sb.append("\\t");
                    case '\r' -> sb.append("\\r");
                    default -> {
                        if (special(s, i)) sb.append(String.format("\\u%04X", (int) c));
                        else sb.append(c);
                    }
                }
            }
            return sb.append('"').toString();
        }
        sb.append('\'');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'') sb.append("''");
            else sb.append(c);
        }
        return sb.append('\'').toString();
    }

    private static boolean special(String s, int i) {
        char c = s.charAt(i);
        if (Character.isHighSurrogate(c)) return i + 1 >= s.length() || !Character.isLowSurrogate(s.charAt(i + 1));
        if (Character.isLowSurrogate(c)) return i == 0 || !Character.isHighSurrogate(s.charAt(i - 1));
        if (c == 0x9 || c == 0xA || c == 0xD) return true;
        if (c >= 0x20 && c <= 0x7E) return false;
        if (c >= 0xA0 && c <= 0xD7FF) return c == ' ' || c == ' ';
        if (c >= 0xE000 && c <= 0xFFFD) return c == '﻿';
        return true;
    }

    public record Raw(String text) {}
}
