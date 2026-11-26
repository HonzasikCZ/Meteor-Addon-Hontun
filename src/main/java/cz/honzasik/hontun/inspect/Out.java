package cz.honzasik.hontun.inspect;

import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.List;

public final class Out {
    private Out() {}

    public static final class Entry {
        final MutableComponent content;
        final List<MutableComponent> children = new ArrayList<>();

        Entry(MutableComponent content) {
            this.content = content;
        }

        public Entry child(MutableComponent c) {
            children.add(c);
            return this;
        }
    }

    public static final class Block {
        private final String title;
        private final List<Entry> entries = new ArrayList<>();
        private final List<String> notes = new ArrayList<>();

        public Block(String title) {
            this.title = title;
        }

        public Entry add(MutableComponent content) {
            Entry e = new Entry(content);
            entries.add(e);
            return e;
        }

        public void note(String s) {
            notes.add(s);
        }

        public MutableComponent render() {
            MutableComponent out = header(title);
            for (Entry e : entries) {
                out.append(Component.literal("\n  ")).append(e.content);
                for (MutableComponent c : e.children) out.append(Component.literal("\n    ")).append(punct("· ")).append(c);
            }
            for (String n : notes) out.append(Component.literal("\n  ")).append(muted(n));
            return out;
        }
    }

    public static MutableComponent header(String s) {
        return color(s, HontunTheme.chatName());
    }

    public static MutableComponent kv(String label, MutableComponent value) {
        return label(label).append(value);
    }

    public static MutableComponent label(String s) {
        return color(s, HontunTheme.textLight()).append(punct(": "));
    }

    public static MutableComponent value(String s) {
        return color(s, HontunTheme.chatValue());
    }

    public static MutableComponent muted(String s) {
        return color(s, HontunTheme.chatTag());
    }

    public static MutableComponent punct(String s) {
        return color(s, HontunTheme.chatPunct());
    }

    public static MutableComponent sep() {
        return punct(" · ");
    }

    public static MutableComponent text(String s) {
        return color(s, HontunTheme.textLight());
    }

    private static MutableComponent color(String s, int rgb) {
        return Component.literal(s).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb & 0xFFFFFF)));
    }
}
