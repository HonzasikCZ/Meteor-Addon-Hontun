package cz.honzasik.hontun.copy.format;

import cz.honzasik.hontun.copy.Notes;
import cz.honzasik.hontun.copy.StyledText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;

import java.util.Objects;

public final class LegacyWriter {
    public interface Dialect {
        String name();

        String hex(int rgb);

        String escape(String text, String codes, boolean inFont);

        default boolean fonts() {
            return false;
        }

        default boolean softHex() {
            return false;
        }

        default GradientFit.Curve curve() {
            return null;
        }

        default String gradient(int from, int to, String decorations, String text) {
            return null;
        }

        default boolean gradientSafe(String text) {
            for (int i = 0; i < text.length(); i++) {
                if ("&#<>{}\\|%_[]§".indexOf(text.charAt(i)) >= 0) return false;
            }
            return true;
        }
    }

    private LegacyWriter() {}

    public static String write(StyledText text, Dialect dialect, Notes notes) {
        return write(text, dialect, notes, StyledText.Look.UNKNOWN, null);
    }

    public static String write(StyledText text, Dialect d, Notes notes, StyledText.Look start, StyledText.Look trailing) {
        if (text.hasObjects()) {
            if (notes != null) {
                notes.add(d.fonts()
                    ? "sprite and head text objects could not be copied, they were left out"
                    : d.name() + " can't show sprite or head text objects, they were left out");
            }
            text = text.withoutObjects();
        }
        StringBuilder sb = new StringBuilder();
        StyledText.Look state = start == null ? StyledText.Look.UNKNOWN : start;
        for (GradientFit.Piece piece : GradientFit.split(text, d.curve())) {
            boolean fontGradient = piece.gradient() && piece.look().font() != null && d.fonts();
            if (piece.gradient() && !fontGradient && d.gradientSafe(piece.text())) {
                StyledText.Look look = piece.look();
                note(look, d, notes);
                String gradient = d.gradient(piece.from(), piece.to(), decorations(look), piece.text());
                if (gradient != null) {
                    if (d.softHex() && state.decorated()) sb.append("&f");
                    sb.append(gradient);
                    state = look;
                    continue;
                }
            }
            for (StyledText.Run run : piece.runs()) {
                if (run.text().isEmpty()) continue;
                StyledText.Look want = run.look();
                note(want, d, notes);
                boolean font = want.font() != null && d.fonts();
                if (!font && run.text().isBlank() && sameSpacing(state, want)) {
                    sb.append(d.escape(run.text(), "", false));
                    continue;
                }
                String codes = codes(want, d);
                if (d.softHex() && state.decorated() && (font || isHex(want))) sb.append("&f");
                if (font) {
                    sb.append("<font:").append(want.font()).append('>').append(codes).append(d.escape(run.text(), codes, true)).append("</font>");
                } else {
                    sb.append(codes).append(d.escape(run.text(), codes, false));
                }
                state = font ? StyledText.Look.UNKNOWN : want;
            }
        }
        if (trailing != null) {
            if (trailing.font() != null && notes != null) notes.add(d.name() + ": the custom font " + trailing.font() + " of player names was not copied");
            if (d.softHex() && state.decorated() && isHex(trailing)) sb.append("&f");
            sb.append(codes(trailing, d));
        }
        return sb.toString();
    }

    private static boolean sameSpacing(StyledText.Look state, StyledText.Look want) {
        if (want.visibleOnSpace() || state.visibleOnSpace()) return false;
        return state.bold() == want.bold() && Objects.equals(state.font(), want.font());
    }

    private static boolean isHex(StyledText.Look look) {
        return look.hasColor() && named(look.color()) == null;
    }

    public static String codes(StyledText.Look look, Dialect d) {
        StringBuilder sb = new StringBuilder();
        if (look.hasColor()) {
            ChatFormatting named = named(look.color());
            sb.append(named != null ? "&" + code(named) : d.hex(look.color()));
        } else {
            sb.append("&f");
        }
        sb.append(decorations(look));
        return sb.toString();
    }

    public static String decorations(StyledText.Look look) {
        StringBuilder sb = new StringBuilder();
        if (look.obfuscated()) sb.append("&k");
        if (look.bold()) sb.append("&l");
        if (look.strikethrough()) sb.append("&m");
        if (look.underlined()) sb.append("&n");
        if (look.italic()) sb.append("&o");
        return sb.toString();
    }

    public static ChatFormatting named(int rgb) {
        for (ChatFormatting f : ChatFormatting.values()) {
            TextColor tc = TextColor.fromLegacyFormat(f);
            if (tc != null && (tc.getValue() & 0xFFFFFF) == rgb) return f;
        }
        return null;
    }

    public static char code(ChatFormatting f) {
        return f.toString().charAt(1);
    }

    public static String colorName(ChatFormatting f) {
        TextColor tc = TextColor.fromLegacyFormat(f);
        return tc == null ? null : tc.serialize();
    }

    private static void note(StyledText.Look look, Dialect d, Notes notes) {
        if (notes == null) return;
        if (look.font() != null && !d.fonts()) notes.add(d.name() + " can't use the custom font " + look.font() + ", it was left out");
        if (look.hasShadow()) notes.add(d.name() + " can't set text shadow colors, they were left out");
    }

    public static String hex6(int rgb) {
        return String.format("%06X", rgb & 0xFFFFFF);
    }

    static boolean hexAt(String s, int i) {
        if (i + 6 > s.length()) return false;
        for (int k = i; k < i + 6; k++) {
            char c = s.charAt(k);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) return false;
        }
        return true;
    }

    static boolean codeChar(char c, String extra) {
        char l = Character.toLowerCase(c);
        return (l >= '0' && l <= '9') || (l >= 'a' && l <= 'f') || (l >= 'k' && l <= 'o') || l == 'r' || l == 'x' || extra.indexOf(l) >= 0;
    }
}
