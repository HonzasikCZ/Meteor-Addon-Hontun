package cz.honzasik.hontun.utils;

import com.google.common.collect.MapMaker;
import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.modules.AntiExploit;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.BundleContents;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ItemTemplateGuard {
    public static final int MAX_DEPTH = 64;
    public static final int MAX_NESTED = 65536;
    public static final int MAX_LIST = 4096;

    private static final ThreadLocal<State> STATE = ThreadLocal.withInitial(State::new);
    private static final ThreadLocal<Boolean> CLIENTBOUND = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static final Map<BundleContents, Integer> HIDDEN = new MapMaker().weakKeys().makeMap();

    private static final class State {
        int depth;
        int stacks;
        int nested;
        int flattened;
        int trimmed;
    }

    private ItemTemplateGuard() {}

    public static boolean clientbound(boolean value) {
        boolean previous = CLIENTBOUND.get();
        CLIENTBOUND.set(value);
        return previous;
    }

    private static boolean active() {
        return AntiExploit.itemGuard() && CLIENTBOUND.get();
    }

    public static StreamCodec<RegistryFriendlyByteBuf, ItemStackTemplate> template(StreamCodec<RegistryFriendlyByteBuf, ItemStackTemplate> codec) {
        return new StreamCodec<>() {
            @Override
            public ItemStackTemplate decode(RegistryFriendlyByteBuf buf) {
                if (!active()) return codec.decode(buf);
                State state = STATE.get();
                int level = ++state.depth;
                try {
                    ItemStackTemplate template = codec.decode(buf);
                    state.nested++;
                    return level > MAX_DEPTH || state.nested > MAX_NESTED ? strip(template, state) : template;
                } finally {
                    state.depth--;
                    finish(state);
                }
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, ItemStackTemplate template) {
                codec.encode(buf, template);
            }
        };
    }

    public static StreamCodec<RegistryFriendlyByteBuf, ItemStack> stack(StreamCodec<RegistryFriendlyByteBuf, ItemStack> codec) {
        return new StreamCodec<>() {
            @Override
            public ItemStack decode(RegistryFriendlyByteBuf buf) {
                if (!active()) return codec.decode(buf);
                State state = STATE.get();
                state.stacks++;
                try {
                    return codec.decode(buf);
                } finally {
                    state.stacks--;
                    finish(state);
                }
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, ItemStack stack) {
                codec.encode(buf, stack);
            }
        };
    }

    public static StreamCodec<RegistryFriendlyByteBuf, BundleContents> bundle(StreamCodec<RegistryFriendlyByteBuf, BundleContents> codec) {
        return new StreamCodec<>() {
            @Override
            public BundleContents decode(RegistryFriendlyByteBuf buf) {
                BundleContents contents = codec.decode(buf);
                if (contents.size() <= MAX_LIST || !active()) return contents;
                List<ItemStackTemplate> items = contents.items();
                long hidden = 0;
                for (int i = MAX_LIST; i < items.size(); i++) hidden += Math.max(0, items.get(i).count());
                BundleContents capped = new BundleContents(new ArrayList<>(items.subList(0, MAX_LIST)));
                HIDDEN.put(capped, (int) Math.min(Integer.MAX_VALUE, hidden));
                State state = STATE.get();
                state.trimmed++;
                finish(state);
                return capped;
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, BundleContents contents) {
                codec.encode(buf, contents);
            }
        };
    }

    public static int hiddenItems(BundleContents contents) {
        if (contents == null || HIDDEN.isEmpty()) return 0;
        Integer hidden = HIDDEN.get(contents);
        return hidden == null ? 0 : hidden;
    }

    private static ItemStackTemplate strip(ItemStackTemplate template, State state) {
        DataComponentPatch patch = template.components();
        if (patch.isEmpty()) return template;
        DataComponentPatch kept = patch.forget(ItemTemplateGuard::carrier);
        if (kept.size() == patch.size()) return template;
        state.flattened++;
        return new ItemStackTemplate(template.item(), template.count(), kept);
    }

    private static boolean carrier(DataComponentType<?> type) {
        return type == DataComponents.BUNDLE_CONTENTS ||
            type == DataComponents.CONTAINER ||
            type == DataComponents.CHARGED_PROJECTILES ||
            type == DataComponents.USE_REMAINDER ||
            type == DataComponents.SULFUR_CUBE_CONTENT;
    }

    private static void finish(State state) {
        if (state.depth > 0 || state.stacks > 0) return;
        state.nested = 0;
        int flattened = state.flattened;
        int trimmed = state.trimmed;
        if (flattened == 0 && trimmed == 0) return;
        state.flattened = 0;
        state.trimmed = 0;
        if (flattened > 0) {
            if (AntiExploit.shouldWarn("item-nesting")) {
                Hontun.LOG.warn("[AntiExploit] flattened {} nested item(s) past depth {} or {} nested items", flattened, MAX_DEPTH, MAX_NESTED);
            }
            AntiExploit.blocked("Illegal item", "nested items flattened");
        }
        if (trimmed > 0) {
            if (AntiExploit.shouldWarn("item-bundle-list")) {
                Hontun.LOG.warn("[AntiExploit] trimmed {} bundle(s) to their first {} entries", trimmed, MAX_LIST);
            }
            AntiExploit.blocked("Illegal item", "bundle over " + MAX_LIST + " entries trimmed");
        }
    }
}
