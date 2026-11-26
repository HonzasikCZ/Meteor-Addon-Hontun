package cz.honzasik.hontun.mixin.client.death;

import com.mojang.blaze3d.vertex.PoseStack;
import cz.honzasik.hontun.modules.DeathFreecam;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererDeathTiltMixin {
    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void hontun$noDeathTilt(CameraRenderState camera, PoseStack pose, CallbackInfo ci) {
        if (DeathFreecam.deadFreecam()) ci.cancel();
    }
}
