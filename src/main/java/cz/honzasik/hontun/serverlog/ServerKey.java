package cz.honzasik.hontun.serverlog;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public record ServerKey(long seed, String dimension, List<String> levels) {
    public ServerKey {
        dimension = dimension == null ? "" : dimension;
        levels = levels == null ? List.of() : levels.stream().filter(s -> s != null && !s.isBlank()).distinct().sorted().toList();
    }

    public boolean known() {
        return seed != 0L;
    }

    public String text() {
        return known() ? seedPart(seed) + "@" + dimension : "";
    }

    public boolean sameServer(LogModel.ServerEntry entry) {
        if (entry == null || !known() || entry.key == null) return false;
        if (entry.key.equals(text())) return true;
        return entry.key.startsWith(seedPart(seed) + "@") && levels.size() > 1 && Set.copyOf(levels).equals(Set.copyOf(entry.levels));
    }

    public boolean same(ServerKey other) {
        if (other == null || !known() || seed != other.seed) return false;
        return dimension.equals(other.dimension) || levels.size() > 1 && levels.equals(other.levels);
    }

    public static String fallback(String software, String version) {
        return "soft:" + part(software) + "|" + part(version);
    }

    private static String part(String s) {
        return s == null || s.isBlank() ? "unknown" : s.strip().toLowerCase(Locale.ROOT);
    }

    private static String seedPart(long seed) {
        return "seed:" + Long.toHexString(seed);
    }
}
