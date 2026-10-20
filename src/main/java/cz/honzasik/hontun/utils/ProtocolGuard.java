package cz.honzasik.hontun.utils;

import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.mixin.network.custompayload.ClientListenerConnectionAccessor;
import cz.honzasik.hontun.modules.AntiExploit;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.PacketListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class ProtocolGuard {
    public static final int MAX_PREALLOC = 65536;
    public static final int MAX_SLOT_DEPTH = 32;
    public static final int MAX_POTION_BASES = 64;
    public static final int MAX_POTION_STACKS = 1024;
    public static final int MAX_DEMO_STACKS = 64;
    public static final int MAX_COMMAND_DEPTH = 512;
    public static final int MAX_REGISTRY_ELEMENTS = 262_144;
    public static final long FRAME_BUDGET_NANOS = 40_000_000L;
    public static final int MAX_BACKLOG = 1_000_000;

    private static final int TRIM_DEPTH = 1;
    private static final int TRIM_POTION = 2;
    private static final ThreadLocal<int[]> SLOT_STATE = ThreadLocal.withInitial(() -> new int[3]);
    private static final Map<StreamCodec<?, ?>, StreamCodec<RegistryFriendlyByteBuf, SlotDisplay>> SLOT_CODECS = new ConcurrentHashMap<>();

    public interface CommandEntry {
        boolean hontun$stubbed();

        int hontun$flags();

        int hontun$redirect();

        int[] hontun$children();
    }

    public interface CommandTree {
        boolean hontun$tooDeep();
    }

    public interface PacketBudget {
        void hontun$beginBudget(long nanos);

        void hontun$endBudget();
    }

    public interface TagRebuild {
        void hontun$flushTags();
    }

    private ProtocolGuard() {}

    public static int prealloc(int count) {
        if (count <= MAX_PREALLOC || !AntiExploit.packetGuard()) return count;
        if (AntiExploit.shouldWarn("prealloc")) {
            Hontun.LOG.warn("[AntiExploit] capped a declared collection size of {} to {} before reading it", count, MAX_PREALLOC);
        }
        return MAX_PREALLOC;
    }

    public static StreamCodec<RegistryFriendlyByteBuf, ?> slotCodec(StreamCodec<RegistryFriendlyByteBuf, ?> codec) {
        if (codec == null || codec instanceof SlotCodec) return codec;
        return SLOT_CODECS.computeIfAbsent(codec, c -> new SlotCodec(codec, (Object) codec == SlotDisplay.WithAnyPotion.STREAM_CODEC));
    }

    private static final class SlotCodec implements StreamCodec<RegistryFriendlyByteBuf, SlotDisplay> {
        private final StreamCodec<RegistryFriendlyByteBuf, ?> inner;
        private final boolean potion;

        private SlotCodec(StreamCodec<RegistryFriendlyByteBuf, ?> inner, boolean potion) {
            this.inner = inner;
            this.potion = potion;
        }

        @Override
        public SlotDisplay decode(RegistryFriendlyByteBuf buf) {
            int[] state = SLOT_STATE.get();
            int depth = ++state[0];
            if (potion) state[1]++;
            try {
                SlotDisplay value = (SlotDisplay) inner.decode(buf);
                if (!AntiExploit.packetGuard()) return value;
                if (depth > MAX_SLOT_DEPTH) {
                    state[2] |= TRIM_DEPTH;
                    return SlotDisplay.Empty.INSTANCE;
                }
                if (potion && state[1] > 1 && value instanceof SlotDisplay.WithAnyPotion nested) {
                    state[2] |= TRIM_POTION;
                    return nested.display();
                }
                return value;
            } finally {
                state[0]--;
                if (potion) state[1]--;
                if (state[0] == 0 && state[2] != 0) {
                    int trims = state[2];
                    state[2] = 0;
                    slotTrimmed(trims);
                }
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void encode(RegistryFriendlyByteBuf buf, SlotDisplay value) {
            ((StreamCodec<RegistryFriendlyByteBuf, Object>) inner).encode(buf, value);
        }
    }

    private static void slotTrimmed(int trims) {
        if ((trims & TRIM_DEPTH) != 0) {
            AntiExploit.blocked("Recipe display", "nesting over " + MAX_SLOT_DEPTH + " flattened");
            if (AntiExploit.shouldWarn("slot|depth")) {
                Hontun.LOG.warn("[AntiExploit] flattened a recipe display nested deeper than {}", MAX_SLOT_DEPTH);
            }
        }
        if ((trims & TRIM_POTION) != 0) {
            AntiExploit.blocked("Recipe display", "nested potion display merged");
            if (AntiExploit.shouldWarn("slot|potion")) {
                Hontun.LOG.warn("[AntiExploit] merged a potion recipe display nested inside another potion display");
            }
        }
    }

    public static List<ItemStack> potionBases(List<ItemStack> stacks) {
        if (stacks == null || stacks.size() <= MAX_POTION_BASES || !AntiExploit.packetGuard()) return stacks;
        if (AntiExploit.shouldWarn("slot|potion-bases")) {
            Hontun.LOG.warn("[AntiExploit] a potion recipe display resolved to {} items, showing the first {}", stacks.size(), MAX_POTION_BASES);
            AntiExploit.blocked("Recipe display", "potion variants capped");
        }
        return List.copyOf(stacks.subList(0, MAX_POTION_BASES));
    }

    public static <T> Stream<T> potionStacks(Stream<T> stacks) {
        return stacks == null || !AntiExploit.packetGuard() ? stacks : stacks.limit(MAX_POTION_STACKS);
    }

    public static SlotDisplay demoSide(SlotDisplay display) {
        return display == null || !AntiExploit.packetGuard() ? display : new LimitedDisplay(display, MAX_DEMO_STACKS);
    }

    private record LimitedDisplay(SlotDisplay inner, int limit) implements SlotDisplay {
        @Override
        public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
            return inner.resolve(context, factory).limit(limit);
        }

        @Override
        public Type<? extends SlotDisplay> type() {
            return inner.type();
        }

        @Override
        public boolean isEnabled(FeatureFlagSet enabledFeatures) {
            return inner.isEnabled(enabledFeatures);
        }
    }

    public static void validateCommandTree(List<?> entries) {
        int n = entries.size();
        int[] redirects = new int[n];
        int[][] children = new int[n][];
        for (int i = 0; i < n; i++) {
            CommandEntry entry = (CommandEntry) entries.get(i);
            redirects[i] = (entry.hontun$flags() & 8) != 0 ? entry.hontun$redirect() : -1;
            children[i] = entry.hontun$children();
        }
        if (!acyclicRedirects(redirects) || !acyclicChildren(children)) {
            throw new IllegalStateException("Server sent an impossible command tree");
        }
    }

    private static boolean acyclicRedirects(int[] redirects) {
        int n = redirects.length;
        byte[] state = new byte[n];
        for (int start = 0; start < n; start++) {
            int i = start;
            while (i >= 0 && i < n && state[i] == 0) {
                state[i] = 1;
                i = redirects[i];
            }
            if (i >= 0 && i < n && state[i] == 1) return false;
            i = start;
            while (i >= 0 && i < n && state[i] == 1) {
                state[i] = 2;
                i = redirects[i];
            }
        }
        return true;
    }

    private static boolean acyclicChildren(int[][] children) {
        int n = children.length;
        byte[] state = new byte[n];
        int[] stack = new int[n];
        int[] next = new int[n];
        for (int start = 0; start < n; start++) {
            if (state[start] != 0) continue;
            int sp = 0;
            stack[sp] = start;
            next[sp] = 0;
            sp++;
            state[start] = 1;
            while (sp > 0) {
                int node = stack[sp - 1];
                int[] kids = children[node];
                int at = next[sp - 1];
                if (at < kids.length) {
                    next[sp - 1] = at + 1;
                    int kid = kids[at];
                    if (kid < 0 || kid >= n) continue;
                    if (state[kid] == 1) return false;
                    if (state[kid] == 0) {
                        state[kid] = 1;
                        stack[sp] = kid;
                        next[sp] = 0;
                        sp++;
                    }
                } else {
                    state[node] = 2;
                    sp--;
                }
            }
        }
        return true;
    }

    public static boolean commandTreeTooDeep(List<?> entries, int root) {
        int n = entries.size();
        CommandEntry[] nodes = new CommandEntry[n];
        long budget = 8L * n + 4096;
        for (int i = 0; i < n; i++) {
            nodes[i] = (CommandEntry) entries.get(i);
            budget += 8L * nodes[i].hontun$children().length;
        }
        boolean[] resolved = new boolean[n];
        int[] index = new int[MAX_COMMAND_DEPTH];
        int[] phase = new int[MAX_COMMAND_DEPTH];
        int[] next = new int[MAX_COMMAND_DEPTH];
        int depth = 1;
        index[0] = root;
        long steps = 0;
        while (depth > 0) {
            if (++steps > budget) return true;
            int top = depth - 1;
            int i = index[top];
            if (phase[top] == 0) {
                if (i < 0 || i >= n) return false;
                if (resolved[i]) {
                    depth--;
                    continue;
                }
                phase[top] = 1;
                CommandEntry entry = nodes[i];
                if (entry.hontun$stubbed() && (entry.hontun$flags() & 8) != 0) {
                    if (depth >= MAX_COMMAND_DEPTH) return true;
                    index[depth] = entry.hontun$redirect();
                    phase[depth] = 0;
                    depth++;
                }
            } else if (phase[top] == 1) {
                resolved[i] = true;
                phase[top] = 2;
                next[top] = 0;
            } else {
                int[] kids = nodes[i].hontun$children();
                if (next[top] < kids.length) {
                    if (depth >= MAX_COMMAND_DEPTH) return true;
                    index[depth] = kids[next[top]++];
                    phase[depth] = 0;
                    depth++;
                } else {
                    depth--;
                }
            }
        }
        return false;
    }

    public static void commandTreeKept() {
        AntiExploit.blocked("Commands", "tree deeper than " + MAX_COMMAND_DEPTH + ", kept the previous one");
        if (AntiExploit.shouldWarn("commands|depth")) {
            Hontun.LOG.warn("[AntiExploit] the server sent a command tree deeper than {}, keeping the previous commands", MAX_COMMAND_DEPTH);
        }
    }

    public static Component registryFloodReason() {
        return Component.translatableWithFallback("hontun.antiexploit.disconnect.registry_flood",
            "Hontun Anti-Exploit: the server sent more than " + MAX_REGISTRY_ELEMENTS + " registry entries without finishing the configuration, disconnected to keep memory safe.");
    }

    public static void registryFlood(long elements) {
        AntiExploit.blocked("Registry data", "over " + MAX_REGISTRY_ELEMENTS + " entries, disconnected");
        if (AntiExploit.shouldWarn("registry|flood")) {
            Hontun.LOG.warn("[AntiExploit] the server sent {} registry entries without finishing the configuration, disconnecting", elements);
        }
    }

    public static void backlogOverflow(PacketListener listener) {
        AntiExploit.blocked("Packet backlog", "over " + MAX_BACKLOG + " queued, disconnected");
        if (AntiExploit.shouldWarn("backlog")) {
            Hontun.LOG.warn("[AntiExploit] more than {} packets are waiting to be handled, disconnecting", MAX_BACKLOG);
        }
        if (listener instanceof ClientCommonPacketListenerImpl) {
            ((ClientListenerConnectionAccessor) listener).hontun$getConnection().disconnect(Component.translatableWithFallback("hontun.antiexploit.disconnect.backlog",
                "Hontun Anti-Exploit: the server sent packets faster than they could be handled (over " + MAX_BACKLOG + " waiting), disconnected to keep the game responsive."));
        }
    }

    public static void flushTags() {
        if ((Object) MCUtil.MC.getConnection() instanceof TagRebuild rebuild) rebuild.hontun$flushTags();
    }
}
