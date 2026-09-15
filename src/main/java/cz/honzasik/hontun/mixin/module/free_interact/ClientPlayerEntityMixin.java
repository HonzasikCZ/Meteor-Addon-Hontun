package cz.honzasik.hontun.mixin.module.free_interact;

import cz.honzasik.hontun.modules.FreeInteract;
import cz.honzasik.hontun.utils.Util;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin {
    @Inject(method = "isPassenger", at = @At("HEAD"), cancellable = true, require = 0)
    private void modifyIsRiding(CallbackInfoReturnable<Boolean> cir) {
        if (Util.isActiveAnd(FreeInteract.class, m -> m.interactInBoat.get())) {
            cir.setReturnValue(false);
        }
    }
}
