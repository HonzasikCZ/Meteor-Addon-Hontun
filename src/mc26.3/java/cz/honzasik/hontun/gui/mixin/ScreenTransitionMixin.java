package cz.honzasik.hontun.gui.mixin;

import cz.honzasik.hontun.gui.transition.ScreenTransitions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class ScreenTransitionMixin {
    @Inject(method = "renderFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render()V"))
    private void hontun$transitionCapture(boolean advanceGameTime, CallbackInfo ci) {
        ScreenTransitions.beforeRender((Minecraft) (Object) this);
    }

    @Inject(method = "renderFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render()V", shift = At.Shift.AFTER))
    private void hontun$transitionDraw(boolean advanceGameTime, CallbackInfo ci) {
        ScreenTransitions.afterRender((Minecraft) (Object) this);
    }

    @Inject(method = "renderFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;endFrame()V", shift = At.Shift.AFTER))
    private void hontun$transitionEndFrame(boolean advanceGameTime, CallbackInfo ci) {
        ScreenTransitions.endFrame();
    }

    @Inject(method = "setScreenAndShow", at = @At("HEAD"))
    private void hontun$transitionForced(Screen screen, CallbackInfo ci) {
        ScreenTransitions.forceInstant();
    }
}
