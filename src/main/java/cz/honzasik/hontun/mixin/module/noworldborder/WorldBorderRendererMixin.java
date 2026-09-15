package cz.honzasik.hontun.mixin.module.noworldborder;

import cz.honzasik.hontun.modules.NoWorldBorder;
import cz.honzasik.hontun.utils.Util;
import net.minecraft.client.renderer.WorldBorderRenderer;
import net.minecraft.client.renderer.state.level.WorldBorderRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldBorderRenderer.class)
public abstract class WorldBorderRendererMixin {
    @Inject(
        method = "render(Lnet/minecraft/client/renderer/state/level/WorldBorderRenderState;Lnet/minecraft/world/phys/Vec3;DD)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hontun$hideWorldBorder(WorldBorderRenderState state, Vec3 cameraPos, double d, double e, CallbackInfo ci) {
        if (Util.isActive(NoWorldBorder.class)) ci.cancel();
    }
}
