package cz.honzasik.hontun.gui.render.text;

import java.util.ArrayList;
import java.util.List;

public final class GlyphSupport {
    private GlyphSupport() {}

    public static boolean supported(char c) {
        if (c < 32) return true;
        if (c <= 126) return true;
        if (c >= 160 && c <= 383) return true;
        if (c >= 880 && c <= 1279) return true;
        return c == 8734;
    }

    public static boolean needsVanilla(String text) {
        if (text == null) return false;
        for (int i = 0, n = text.length(); i < n; i++) {
            if (!supported(text.charAt(i))) return true;
        }
        return false;
    }

    public static boolean needsVanilla(String text, int length) {
        if (text == null) return false;
        int n = Math.min(length, text.length());
        for (int i = 0; i < n; i++) {
            if (!supported(text.charAt(i))) return true;
        }
        return false;
    }

    public static int runEnd(String text, int start, int end) {
        boolean vanilla = !supported(text.charAt(start));
        int i = start + 1;
        while (i < end && supported(text.charAt(i)) != vanilla) i++;
        return i;
    }

    public static boolean vanillaRun(String text, int start) {
        return !supported(text.charAt(start));
    }

    private static final String NO_BREAK_BEFORE = "，。、！？：；）」』】〉》〕｝％…·ーゝゞヽヾ々";
    private static final String NO_BREAK_AFTER = "（「『【〈《〔｛";

    public static boolean cjk(int cp) {
        Character.UnicodeScript s = Character.UnicodeScript.of(cp);
        if (s == Character.UnicodeScript.HAN || s == Character.UnicodeScript.HIRAGANA
                || s == Character.UnicodeScript.KATAKANA || s == Character.UnicodeScript.BOPOMOFO) return true;
        Character.UnicodeBlock b = Character.UnicodeBlock.of(cp);
        return b == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || b == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || b == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS
                || b == Character.UnicodeBlock.VERTICAL_FORMS;
    }

    public static boolean hasCjk(String text) {
        if (text == null) return false;
        for (int i = 0, n = text.length(); i < n; ) {
            int cp = text.codePointAt(i);
            if (cjk(cp)) return true;
            i += Character.charCount(cp);
        }
        return false;
    }

    public static List<String> breakUnits(String word) {
        List<String> out = new ArrayList<>();
        int start = 0;
        int prev = -1;
        for (int i = 0, n = word.length(); i < n; ) {
            int cp = word.codePointAt(i);
            if (prev >= 0 && (cjk(prev) || cjk(cp)) && NO_BREAK_BEFORE.indexOf(cp) < 0 && NO_BREAK_AFTER.indexOf(prev) < 0) {
                out.add(word.substring(start, i));
                start = i;
            }
            prev = cp;
            i += Character.charCount(cp);
        }
        out.add(word.substring(start));
        return out;
    }
}
