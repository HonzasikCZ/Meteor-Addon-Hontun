package cz.honzasik.hontun.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import cz.honzasik.hontun.mixin.network.antiexploit.TranslatableContentsAccessor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import net.minecraft.locale.Language;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtAccounterException;
import net.minecraft.nbt.TagTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.codec.StreamCodec;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public final class TextBudget {
    public static final int CAP = 1_000_000;
    public static final int BUDGET = 32_768;
    public static final int LAYOUT_CHARS = 32_768;
    public static final int LAYOUT_NODES = 4_096;
    public static final int RETAIN_CHARS = 65_536;
    public static final int RETAIN_NODES = 4_096;
    public static final int LOG_CHARS = 4_096;
    public static final long COMPONENT_QUOTA = 32L * 1024 * 1024;

    public static volatile boolean enabled;

    private static final String ELLIPSIS = "…";
    private static final int REMEMBER_NODES = 64;
    private static final Optional<Boolean> STOP = Optional.of(Boolean.TRUE);

    private static final Cache<Component, Weight> WEIGHTS = CacheBuilder.newBuilder().weakKeys().maximumSize(4096).build();
    private static final Cache<Component, Flat> FLATTENED = CacheBuilder.newBuilder().weakKeys().maximumSize(1024).build();

    private record Weight(Language language, int siblings, int units, int nodes) {}

    private record Flat(Language language, int siblings, int maxChars, int maxNodes, Component result) {}

    private record Frame(Iterator<? extends FormattedText> parts, Style style) {}

    public record Measure(Language language, int units) {}

    public interface Measured {
        int hontun$units(Language language);
    }

    private TextBudget() {}

    public static Component bound(Component text, int maxChars, int maxNodes) {
        if (text == null) return null;
        try {
            List<Component> siblings = text.getSiblings();
            if (siblings.isEmpty() && text.getContents() instanceof PlainTextContents plain && plain.text().length() <= maxChars) return text;

            Language language = Language.getInstance();
            int size = siblings.size();
            Flat flat = FLATTENED.getIfPresent(text);
            if (flat != null && flat.language == language && flat.siblings == size && flat.maxChars == maxChars && flat.maxNodes == maxNodes) {
                return flat.result;
            }

            Weight weight = WEIGHTS.getIfPresent(text);
            if (weight != null && weight.language == language && weight.siblings == size) {
                if (weight.units <= maxChars && weight.nodes <= maxNodes) return text;
            } else {
                Walk walk = Walk.measure(maxChars, maxNodes);
                walk.run(List.of(text), null);
                if (!walk.stopped) {
                    if (walk.nodes >= REMEMBER_NODES) WEIGHTS.put(text, new Weight(language, size, walk.units, walk.nodes));
                    return text;
                }
            }

            Component result = flatten(List.of(text), Style.EMPTY, maxChars, maxNodes);
            FLATTENED.put(text, new Flat(language, size, maxChars, maxNodes, result));
            return result;
        } catch (RuntimeException | StackOverflowError e) {
            return text;
        }
    }

    public static boolean exceeds(List<? extends FormattedText> parts, int maxUnits, int maxNodes) {
        Walk walk = Walk.measure(maxUnits, maxNodes);
        walk.run(parts, null);
        return walk.stopped;
    }

    public static MutableComponent flatten(List<? extends FormattedText> parts, Style style, int maxChars, int maxNodes) {
        Walk walk = Walk.styled(Math.max(1, maxChars - 2), Math.max(1, maxNodes - 2));
        walk.run(parts, style == null ? Style.EMPTY : style);
        return walk.result();
    }

    public static int weigh(List<? extends FormattedText> parts) {
        Language language = Language.getInstance();
        IdentityHashMap<FormattedText, Integer> seen = null;
        int total = 0;
        for (FormattedText part : parts) {
            if (total > CAP) break;
            if (!(part instanceof Component component)) {
                total = add(total, literalUnits(part));
                continue;
            }
            Integer known = seen == null ? null : seen.get(component);
            if (known == null) {
                Walk walk = Walk.weigh(language);
                walk.run(List.of(component), null);
                known = walk.units;
                if (seen == null) seen = new IdentityHashMap<>();
                seen.put(component, known);
            }
            total = add(total, known);
        }
        return total;
    }

    public static List<FormattedText> compact(List<? extends FormattedText> parts) {
        return List.of(flatten(parts, Style.EMPTY, BUDGET, BUDGET));
    }

    public static FormattedText layout(FormattedText text) {
        if (!enabled || text == null) return text;
        if (text instanceof Component component) return bound(component, LAYOUT_CHARS, LAYOUT_NODES);
        try {
            List<FormattedText> parts = List.of(text);
            return exceeds(parts, LAYOUT_CHARS, LAYOUT_NODES) ? flatten(parts, Style.EMPTY, LAYOUT_CHARS, LAYOUT_NODES) : text;
        } catch (RuntimeException | StackOverflowError e) {
            return text;
        }
    }

    public static Component retained(Component text) {
        return enabled ? bound(text, RETAIN_CHARS, RETAIN_NODES) : text;
    }

    public static String logLine(String line) {
        if (!enabled || line == null || line.length() <= LOG_CHARS) return line;
        int cut = LOG_CHARS;
        if (Character.isHighSurrogate(line.charAt(cut - 1))) cut--;
        return line.substring(0, cut) + "...(+" + (line.length() - cut) + ")";
    }

    public static NbtAccounter componentQuota() {
        return enabled ? NbtAccounter.create(COMPONENT_QUOTA) : NbtAccounter.unlimitedHeap();
    }

    public static <B extends ByteBuf> StreamCodec<B, Component> trusted(StreamCodec<B, Component> inner) {
        return new StreamCodec<>() {
            @Override
            public Component decode(B buf) {
                if (!enabled) return inner.decode(buf);
                int mark = buf.readerIndex();
                try {
                    return inner.decode(buf);
                } catch (RuntimeException error) {
                    if (!overflow(error)) throw error;
                    try {
                        buf.readerIndex(mark);
                        skipTag(buf);
                        return Component.literal(ELLIPSIS);
                    } catch (RuntimeException | IOException retry) {
                        buf.readerIndex(mark);
                        throw error;
                    }
                }
            }

            @Override
            public void encode(B buf, Component value) {
                inner.encode(buf, value);
            }
        };
    }

    private static void skipTag(ByteBuf buf) throws IOException {
        DataInput in = new ByteBufInputStream(buf);
        byte type = in.readByte();
        if (type != 0) TagTypes.getType(type).skip(in, NbtAccounter.unlimitedHeap());
    }

    private static boolean overflow(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause() == cause ? null : cause.getCause()) {
            if (cause instanceof NbtAccounterException) return true;
        }
        return false;
    }

    private static int literalUnits(FormattedText part) {
        int[] length = new int[1];
        part.visit(text -> {
            length[0] = add(length[0], Math.max(1, text.length()));
            return length[0] > CAP ? STOP : Optional.empty();
        });
        return Math.max(1, length[0]);
    }

    private static int measured(TranslatableContents contents, Language language) {
        return (Object) contents instanceof Measured measured ? measured.hontun$units(language) : -1;
    }

    private static List<FormattedText> parts(TranslatableContents contents) {
        TranslatableContentsAccessor accessor = (TranslatableContentsAccessor) (Object) contents;
        try {
            accessor.hontun$decompose();
        } catch (OutOfMemoryError e) {
            throw e;
        } catch (Throwable ignored) {
        }
        List<FormattedText> parts = accessor.hontun$decomposedParts();
        return parts == null ? List.of() : parts;
    }

    private static int add(int a, int b) {
        return (int) Math.min(Integer.MAX_VALUE, (long) a + b);
    }

    private static final class Walk {
        private final int maxUnits;
        private final int maxNodes;
        private final int textLimit;
        private final boolean styled;
        private final Language memo;
        private final StringBuilder run;
        private final MutableComponent out;
        private Style runStyle;
        private Style lastStyle;
        private int emitted;
        private int units;
        private int nodes;
        private boolean stopped;

        private Walk(int maxUnits, int maxNodes, int textLimit, Language memo) {
            this.maxUnits = maxUnits;
            this.maxNodes = maxNodes;
            this.textLimit = textLimit;
            this.styled = textLimit >= 0;
            this.memo = memo;
            this.run = styled ? new StringBuilder() : null;
            this.out = styled ? Component.empty() : null;
        }

        static Walk measure(int maxUnits, int maxNodes) {
            return new Walk(maxUnits, maxNodes, -1, null);
        }

        static Walk weigh(Language language) {
            return new Walk(CAP, Integer.MAX_VALUE, -1, language);
        }

        static Walk styled(int textLimit, int maxNodes) {
            return new Walk(Integer.MAX_VALUE, maxNodes, textLimit, null);
        }

        void run(List<? extends FormattedText> root, Style base) {
            ArrayDeque<Frame> stack = new ArrayDeque<>();
            stack.push(new Frame(root.iterator(), base));
            while (!stopped && !stack.isEmpty()) {
                Frame frame = stack.peek();
                if (!frame.parts().hasNext()) {
                    stack.pop();
                    continue;
                }
                FormattedText part = frame.parts().next();
                if (part instanceof Component component) {
                    component(component, frame.style(), stack);
                } else if (part != null) {
                    literal(part, frame.style());
                }
            }
        }

        private void component(Component component, Style parent, ArrayDeque<Frame> stack) {
            Style style = styled ? component.getStyle().applyTo(parent) : null;
            List<Component> siblings = component.getSiblings();
            if (!siblings.isEmpty()) stack.push(new Frame(siblings.iterator(), style));

            ComponentContents contents = component.getContents();
            if (contents instanceof PlainTextContents plain) {
                String text = plain.text();
                emit(text, style);
                count(text.length());
            } else if (contents instanceof TranslatableContents translatable) {
                if (memo == null && count(0)) return;
                List<FormattedText> parts = parts(translatable);
                if (memo != null) {
                    int known = measured(translatable, memo);
                    if (known >= 0) {
                        count(add(known, 1));
                        return;
                    }
                    if (count(0)) return;
                }
                if (!parts.isEmpty()) stack.push(new Frame(parts.iterator(), style));
            } else {
                other(contents, style);
            }
        }

        private void literal(FormattedText part, Style style) {
            int[] length = new int[1];
            if (styled) {
                part.visit((s, text) -> {
                    emit(text, s);
                    length[0] = add(length[0], Math.max(1, text.length()));
                    return full(length[0]) ? STOP : Optional.empty();
                }, style);
            } else {
                part.visit(text -> {
                    length[0] = add(length[0], Math.max(1, text.length()));
                    return full(length[0]) ? STOP : Optional.empty();
                });
            }
            count(length[0]);
        }

        private void other(ComponentContents contents, Style style) {
            int[] length = new int[1];
            if (styled) {
                contents.visit((s, text) -> {
                    emit(text, s);
                    length[0] = add(length[0], Math.max(1, text.length()));
                    return full(length[0]) ? STOP : Optional.empty();
                }, style);
            } else {
                contents.visit(text -> {
                    length[0] = add(length[0], Math.max(1, text.length()));
                    return full(length[0]) ? STOP : Optional.empty();
                });
            }
            count(length[0]);
        }

        private boolean full(int pending) {
            return styled ? emitted >= textLimit || pending > textLimit : add(units, pending) > maxUnits;
        }

        private boolean count(int length) {
            nodes++;
            units = add(units, Math.max(1, length));
            if (styled ? nodes >= maxNodes || emitted >= textLimit : units > maxUnits || nodes > maxNodes) stopped = true;
            return stopped;
        }

        private void emit(String text, Style style) {
            if (!styled || text.isEmpty() || emitted >= textLimit) return;
            int room = textLimit - emitted;
            if (text.length() > room) {
                int cut = room;
                if (Character.isHighSurrogate(text.charAt(cut - 1))) cut--;
                if (cut <= 0) {
                    emitted = textLimit;
                    return;
                }
                text = text.substring(0, cut);
            }
            if (style != runStyle) flush();
            runStyle = style;
            run.append(text);
            emitted += text.length();
        }

        private void flush() {
            if (run.isEmpty()) return;
            out.append(Component.literal(run.toString()).setStyle(runStyle));
            lastStyle = runStyle;
            run.setLength(0);
        }

        MutableComponent result() {
            flush();
            if (stopped) {
                Style ellipsis = lastStyle == null ? Style.EMPTY : Style.EMPTY.withColor(lastStyle.getColor());
                out.append(Component.literal(ELLIPSIS).setStyle(ellipsis));
            }
            return out;
        }
    }
}
