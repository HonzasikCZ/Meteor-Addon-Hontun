package cz.honzasik.hontun.copy;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;

import java.util.ArrayList;
import java.util.List;

public record StyledText(List<Run> runs) {
    public static final StyledText EMPTY = new StyledText(List.of());
    public static final String OBJECT = "@object";

    public record Look(int color, String colorName, boolean bold, boolean italic, boolean underlined,
                       boolean strikethrough, boolean obfuscated, String font, int shadow, boolean hasShadow) {
        public static final Look PLAIN = new Look(-1, null, false, false, false, false, false, null, 0, false);
        public static final Look UNKNOWN = new Look(-2, null, true, true, true, true, true, "@unknown", 0, false);

        public boolean hasColor() {
            return color >= 0;
        }

        public boolean decorated() {
            return bold || italic || underlined || strikethrough || obfuscated;
        }

        public Look withoutDecorations() {
            return new Look(color, colorName, false, false, false, false, false, font, shadow, hasShadow);
        }

        public boolean sameDecorations(Look o) {
            return bold == o.bold && italic == o.italic && underlined == o.underlined && strikethrough == o.strikethrough && obfuscated == o.obfuscated;
        }

        public boolean visibleOnSpace() {
            return underlined || strikethrough;
        }

        static Look of(Style style) {
            TextColor tc = style.getColor();
            int color = tc == null ? -1 : tc.getValue() & 0xFFFFFF;
            String name = null;
            if (tc != null) {
                String s = tc.serialize();
                if (!s.startsWith("#")) name = s;
            }
            String font = null;
            FontDescription fd = style.getFont();
            if (fd instanceof FontDescription.Resource res) {
                if (!"minecraft:default".equals(res.id().toString())) font = res.id().toString();
            } else if (fd != null) {
                font = OBJECT;
            }
            Integer shadow = style.getShadowColor();
            return new Look(color, name, style.isBold(), style.isItalic(), style.isUnderlined(),
                style.isStrikethrough(), style.isObfuscated(), font, shadow == null ? 0 : shadow, shadow != null);
        }
    }

    public record Run(String text, Look look) {}

    public static StyledText of(Component component) {
        if (component == null) return EMPTY;
        List<Run> runs = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        Look[] current = {null};
        StringDecomposer.iterateFormatted(component, Style.EMPTY, (index, style, codepoint) -> {
            Look look = Look.of(style);
            if (current[0] != null && !current[0].equals(look)) {
                runs.add(new Run(text.toString(), current[0]));
                text.setLength(0);
            }
            current[0] = look;
            text.appendCodePoint(codepoint);
            return true;
        });
        if (current[0] != null && !text.isEmpty()) runs.add(new Run(text.toString(), current[0]));
        return new StyledText(List.copyOf(runs));
    }

    public static StyledText of(FormattedText text) {
        if (text == null) return EMPTY;
        List<Run> runs = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        Look[] current = {null};
        StringDecomposer.iterateFormatted(text, Style.EMPTY, (index, style, codepoint) -> {
            Look look = Look.of(style);
            if (current[0] != null && !current[0].equals(look)) {
                runs.add(new Run(sb.toString(), current[0]));
                sb.setLength(0);
            }
            current[0] = look;
            sb.appendCodePoint(codepoint);
            return true;
        });
        if (current[0] != null && !sb.isEmpty()) runs.add(new Run(sb.toString(), current[0]));
        return new StyledText(List.copyOf(runs));
    }

    public boolean uniformBetween(int from, int to) {
        StyledText part = slice(from, to);
        return part.runs.size() <= 1;
    }

    public static StyledText of(FormattedCharSequence sequence) {
        if (sequence == null) return EMPTY;
        List<Run> runs = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        Look[] current = {null};
        sequence.accept((index, style, codepoint) -> {
            Look look = Look.of(style);
            if (current[0] != null && !current[0].equals(look)) {
                runs.add(new Run(text.toString(), current[0]));
                text.setLength(0);
            }
            current[0] = look;
            text.appendCodePoint(codepoint);
            return true;
        });
        if (current[0] != null && !text.isEmpty()) runs.add(new Run(text.toString(), current[0]));
        return new StyledText(List.copyOf(runs));
    }

    public static StyledText plain(String s) {
        return s == null || s.isEmpty() ? EMPTY : new StyledText(List.of(new Run(s, Look.PLAIN)));
    }

    public String plain() {
        StringBuilder sb = new StringBuilder();
        for (Run r : runs) sb.append(r.text);
        return sb.toString();
    }

    public boolean isEmpty() {
        for (Run r : runs) if (!r.text.isEmpty()) return false;
        return true;
    }

    public boolean isBlank() {
        return plain().isBlank();
    }

    public int length() {
        int n = 0;
        for (Run r : runs) n += r.text.length();
        return n;
    }

    public StyledText slice(int from, int to) {
        List<Run> out = new ArrayList<>();
        int pos = 0;
        for (Run r : runs) {
            int start = pos;
            int end = pos + r.text.length();
            pos = end;
            int a = Math.max(from, start);
            int b = Math.min(to, end);
            if (a < b) out.add(new Run(r.text.substring(a - start, b - start), r.look));
        }
        return new StyledText(List.copyOf(out));
    }

    public Look lookAt(int index) {
        int pos = 0;
        for (Run r : runs) {
            if (index < pos + r.text.length()) return r.look;
            pos += r.text.length();
        }
        return runs.isEmpty() ? Look.PLAIN : runs.getLast().look;
    }

    public List<StyledText> lines() {
        List<StyledText> out = new ArrayList<>();
        List<Run> line = new ArrayList<>();
        for (Run r : runs) {
            String t = r.text;
            int start = 0;
            for (int i = 0; i < t.length(); i++) {
                if (t.charAt(i) != '\n') continue;
                if (i > start) line.add(new Run(t.substring(start, i), r.look));
                out.add(new StyledText(List.copyOf(line)));
                line.clear();
                start = i + 1;
            }
            if (start < t.length()) line.add(new Run(t.substring(start), r.look));
        }
        out.add(new StyledText(List.copyOf(line)));
        return out;
    }

    public StyledText concat(StyledText other) {
        List<Run> out = new ArrayList<>(runs);
        out.addAll(other.runs);
        return new StyledText(List.copyOf(out));
    }

    public boolean hasObjects() {
        for (Run r : runs) if (OBJECT.equals(r.look.font)) return true;
        return false;
    }

    public StyledText withoutObjects() {
        if (!hasObjects()) return this;
        List<Run> out = new ArrayList<>();
        for (Run r : runs) if (!OBJECT.equals(r.look.font)) out.add(r);
        return new StyledText(List.copyOf(out));
    }

    public boolean usesFont() {
        for (Run r : runs) if (r.look.font != null) return true;
        return false;
    }

    public boolean usesShadowColor() {
        for (Run r : runs) if (r.look.hasShadow) return true;
        return false;
    }

    public String key() {
        StringBuilder sb = new StringBuilder();
        for (Run r : runs) sb.append(r.look).append(':').append(r.text).append('\u0000');
        return sb.toString();
    }
}
