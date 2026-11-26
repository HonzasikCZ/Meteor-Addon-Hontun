package cz.honzasik.hontun.copy.format;

import cz.honzasik.hontun.copy.Notes;
import cz.honzasik.hontun.copy.StyledText;
import net.minecraft.ChatFormatting;

import java.util.Objects;

public final class MiniMessageWriter {
    private MiniMessageWriter() {}

    public static String write(StyledText text, Notes notes) {
        if (text.hasObjects() && notes != null) notes.add("sprite and head text objects could not be copied, they were left out");
        text = text.withoutObjects();
        StringBuilder sb = new StringBuilder();
        StyledText.Look cur = StyledText.Look.PLAIN;
        for (GradientFit.Piece piece : GradientFit.split(text, Curves.MINI_MESSAGE)) {
            if (piece.gradient()) {
                StyledText.Look look = piece.look();
                if (!cur.equals(StyledText.Look.PLAIN)) sb.append("<reset>");
                decorations(sb, StyledText.Look.PLAIN, look);
                if (look.font() != null) sb.append("<font:").append(look.font()).append('>');
                if (look.hasShadow()) sb.append("<shadow:#").append(rgba(look.shadow())).append('>');
                sb.append("<gradient:#").append(LegacyWriter.hex6(piece.from())).append(":#").append(LegacyWriter.hex6(piece.to())).append('>');
                sb.append(escape(piece.text())).append("</gradient>");
                cur = uncolored(look);
                continue;
            }
            for (StyledText.Run run : piece.runs()) {
                if (run.text().isEmpty()) continue;
                StyledText.Look want = run.look();
                if (run.text().isBlank() && !want.visibleOnSpace() && sameFrame(cur, want)) {
                    sb.append(escape(run.text()));
                    continue;
                }
                if (needsReset(cur, want)) {
                    sb.append("<reset>");
                    cur = StyledText.Look.PLAIN;
                }
                if (want.hasColor() && want.color() != cur.color()) sb.append(color(want.color()));
                decorations(sb, cur, want);
                if (want.font() != null && !want.font().equals(cur.font())) sb.append("<font:").append(want.font()).append('>');
                if (want.hasShadow() && (!cur.hasShadow() || cur.shadow() != want.shadow())) sb.append("<shadow:#").append(rgba(want.shadow())).append('>');
                cur = want;
                sb.append(escape(run.text()));
            }
        }
        if (!cur.equals(StyledText.Look.PLAIN)) sb.append("<reset>");
        return sb.toString();
    }

    private static StyledText.Look uncolored(StyledText.Look look) {
        return new StyledText.Look(-1, null, look.bold(), look.italic(), look.underlined(), look.strikethrough(), look.obfuscated(), look.font(), look.shadow(), look.hasShadow());
    }

    private static boolean sameFrame(StyledText.Look cur, StyledText.Look want) {
        return cur.sameDecorations(want) && Objects.equals(cur.font(), want.font()) && cur.hasShadow() == want.hasShadow() && cur.shadow() == want.shadow();
    }

    private static boolean needsReset(StyledText.Look cur, StyledText.Look want) {
        if (cur.hasColor() && !want.hasColor()) return true;
        if (cur.bold() && !want.bold()) return true;
        if (cur.italic() && !want.italic()) return true;
        if (cur.underlined() && !want.underlined()) return true;
        if (cur.strikethrough() && !want.strikethrough()) return true;
        if (cur.obfuscated() && !want.obfuscated()) return true;
        if (cur.font() != null && !cur.font().equals(want.font())) return true;
        return cur.hasShadow() && !want.hasShadow();
    }

    private static void decorations(StringBuilder sb, StyledText.Look have, StyledText.Look want) {
        if (want.bold() && !have.bold()) sb.append("<b>");
        if (want.italic() && !have.italic()) sb.append("<i>");
        if (want.underlined() && !have.underlined()) sb.append("<u>");
        if (want.strikethrough() && !have.strikethrough()) sb.append("<st>");
        if (want.obfuscated() && !have.obfuscated()) sb.append("<obf>");
    }

    private static String color(int rgb) {
        ChatFormatting named = LegacyWriter.named(rgb);
        String name = named == null ? null : LegacyWriter.colorName(named);
        if (name != null) return "<" + name + ">";
        return "<#" + LegacyWriter.hex6(rgb) + ">";
    }

    private static String rgba(int argb) {
        return LegacyWriter.hex6(argb) + String.format("%02X", (argb >>> 24) & 0xFF);
    }

    public static String escape(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 4);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            char next = i + 1 < s.length() ? s.charAt(i + 1) : 0;
            if (c == '<' || c == '\\') {
                sb.append('\\').append(c);
                continue;
            }
            sb.append(c);
            if (c == '&' && next != 0 && ("0123456789abcdefklmnor#".indexOf(next) >= 0 || (next == 'x'))) sb.append("<b></b>");
        }
        return sb.toString();
    }
}
