package cz.honzasik.hontun.mixin.module.free_interact;

import cz.honzasik.hontun.modules.FreeInteract;
import cz.honzasik.hontun.utils.MCUtil;
import cz.honzasik.hontun.utils.Util;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z", ordinal = 0))
    private void removeLimit(CallbackInfo ci, @Local LocalBooleanRef bl3) {
        if (Util.isActiveAnd(FreeInteract.class, m -> m.useAndAttack.get())) {
            while (MCUtil.MC.options.keyAttack.consumeClick()) {
                bl3.set(bl3.get() | MCUtil.MC.startAttack());
            }
        }
    }

    @Redirect(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    private boolean redirectIsUsingItem(LocalPlayer instance) {
        if (Util.isActiveAnd(FreeInteract.class, m -> m.useAndAttack.get())) {
            return false;
        }

        return instance.isUsingItem();
    }
}
