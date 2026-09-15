package cz.honzasik.hontun.gui.api.text;

import java.util.ArrayList;
import java.util.List;

public class RichText {
    private final List<RichTextSegment> segments = new ArrayList<>();
    private final StringBuilder plainText = new StringBuilder();

    public RichText(String text) {
        append(text);
    }

    public static RichText of(String text) {
        return new RichText(text);
    }

    public static RichText bold(String text) {
        return new RichText(text).bold();
    }

    public static RichText italic(String text) {
        return new RichText(text).italic();
    }

    public RichText append(String text) {
        if (text != null) {
            segments.add(new RichTextSegment(text));
            plainText.append(text);
        }
        return this;
    }

    public RichText bold() {
        if (!segments.isEmpty()) segments.getLast().setStyle(FontStyle.BOLD);
        return this;
    }

    public RichText italic() {
        if (!segments.isEmpty()) segments.getLast().setStyle(FontStyle.ITALIC);
        return this;
    }

    public RichText shadow() {
        if (!segments.isEmpty()) segments.getLast().setShadow(true);
        return this;
    }

    public RichText setStyle(FontStyle style) {
        if (!segments.isEmpty()) segments.getLast().setStyle(style);
        return this;
    }

    public RichText boldIf(boolean condition) {
        return condition ? bold() : setStyle(FontStyle.REGULAR);
    }

    public RichText italicIf(boolean condition) {
        return condition ? italic() : setStyle(FontStyle.REGULAR);
    }

    public RichText shadowIf(boolean condition) {
        return condition ? shadow() : setStyle(FontStyle.REGULAR);
    }

    public RichText scale(double scale) {
        if (!segments.isEmpty()) segments.getLast().setScale(scale);
        return this;
    }

    public String getPlainText() {
        return plainText.toString();
    }

    public int length() {
        return plainText.length();
    }

    public List<RichTextSegment> getSegments() {
        return segments;
    }
}
