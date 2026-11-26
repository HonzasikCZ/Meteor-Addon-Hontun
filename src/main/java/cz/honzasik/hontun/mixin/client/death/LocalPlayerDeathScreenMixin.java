package cz.honzasik.hontun.mixin.client.death;

import cz.honzasik.hontun.modules.DeathFreecam;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerDeathScreenMixin {
    @Inject(method = "shouldShowDeathScreen", at = @At("HEAD"), cancellable = true)
    private void hontun$forceDeathScreen(CallbackInfoReturnable<Boolean> cir) {
        if (DeathFreecam.bypassInstaRespawn()) cir.setReturnValue(true);
    }
}
