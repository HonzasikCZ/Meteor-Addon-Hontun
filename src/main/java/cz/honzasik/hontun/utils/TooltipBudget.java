package cz.honzasik.hontun.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public final class TooltipBudget extends RuntimeException implements Consumer<Component> {
    public static final int MAX_LINES = 256;
    public static final int MAX_LINE_CHARS = 512;
    public static final int MAX_LINE_NODES = 2048;
    public static final int MAX_PROJECTILE_DEPTH = 2;

    private static final ThreadLocal<int[]> PROJECTILE_DEPTH = ThreadLocal.withInitial(() -> new int[1]);

    private final transient Consumer<Component> target;
    private int lines;

    public TooltipBudget(Consumer<Component> target) {
        super(null, null, false, false);
        this.target = target;
    }

    @Override
    public void accept(Component line) {
        if (++lines > MAX_LINES) throw this;
        target.accept(TextBudget.bound(line, MAX_LINE_CHARS, MAX_LINE_NODES));
    }

    public static Component more() {
        return Component.literal("… and more").withStyle(ChatFormatting.DARK_GRAY);
    }

    public static boolean enterProjectile() {
        int[] depth = PROJECTILE_DEPTH.get();
        if (depth[0] >= MAX_PROJECTILE_DEPTH) return false;
        depth[0]++;
        return true;
    }

    public static void exitProjectile() {
        int[] depth = PROJECTILE_DEPTH.get();
        if (depth[0] > 0) depth[0]--;
    }

    public static String itemId(ItemStack stack) {
        return stack.typeHolder().unwrapKey().map(k -> k.identifier().toString()).orElse("?");
    }
}
