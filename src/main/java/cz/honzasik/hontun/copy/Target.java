package cz.honzasik.hontun.copy;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public enum Target {
    TAB("TAB", true, true, false),
    DECENT_HOLOGRAMS("DecentHolograms", false, false, true),
    FANCY_HOLOGRAMS("FancyHolograms", false, false, true),
    CMI("CMI", true, false, true);

    public final String label;
    public final boolean tab;
    public final boolean scoreboard;
    public final boolean holograms;

    Target(String label, boolean tab, boolean scoreboard, boolean holograms) {
        this.label = label;
        this.tab = tab;
        this.scoreboard = scoreboard;
        this.holograms = holograms;
    }

    public static Target parse(String s) {
        if (s == null) return null;
        String k = s.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "");
        return switch (k) {
            case "tab" -> TAB;
            case "decentholograms", "decent", "dh" -> DECENT_HOLOGRAMS;
            case "fancyholograms", "fancy", "fh" -> FANCY_HOLOGRAMS;
            case "cmi" -> CMI;
            default -> null;
        };
    }

    public static List<String> labels(Kind kind) {
        List<String> out = new ArrayList<>();
        for (Target t : values()) if (t.supports(kind)) out.add(t.label);
        return out;
    }

    public static List<Target> all(Kind kind) {
        List<Target> out = new ArrayList<>();
        for (Target t : values()) if (t.supports(kind)) out.add(t);
        return out;
    }

    public boolean supports(Kind kind) {
        return switch (kind) {
            case TAB -> tab;
            case SCOREBOARD -> scoreboard;
            case HOLOGRAMS -> holograms;
        };
    }

    public enum Kind { TAB, SCOREBOARD, HOLOGRAMS }
}
