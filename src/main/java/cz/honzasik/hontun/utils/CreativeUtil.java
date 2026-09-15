package cz.honzasik.hontun.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class CreativeUtil {
    private static ItemStack savedStack = null;

    public static void saveHeldStack() {
        savedStack = MCUtil.getStackInSelectedSlot();
    }

    public static void restoreHeldStack() {
        setSelectedSlot(savedStack);
    }

    public static void setSelectedSlot(ItemStack stack) {
        setSlot(MCUtil.getSelectedSlot(), stack);
    }

    public static void setSlot(int slot, ItemStack stack) {
        MCUtil.MC.player.getInventory().setItem(slot, stack);

        MCUtil.MC.gameMode.handleCreativeModeItemAdd(stack, slot + 36);
    }

    public static void interactWBlockAtEyes() {
        Vec3 eyePos = MCUtil.MC.player.getEyePosition();

        BlockPos blockPos = new BlockPos(
            Mth.floor(eyePos.x),
            Mth.floor(eyePos.y),
            Mth.floor(eyePos.z)
        );

        BlockHitResult bhr = new BlockHitResult(
            eyePos,
            Direction.UP,
            blockPos,
            false
        );

        MCUtil.MC.gameMode.useItemOn(MCUtil.MC.player, InteractionHand.MAIN_HAND, bhr);
    }
}
