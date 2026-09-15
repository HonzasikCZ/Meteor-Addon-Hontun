package cz.honzasik.hontun.mixin.client.titlescreen;

import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.SplashRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashRenderer.class)
public abstract class SplashTextMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void hontun$hideSplash(GuiGraphicsExtractor g, int width, Font font, float alpha, CallbackInfo ci) {
        if (HontunTheme.restyleEnabled()) ci.cancel();
    }
}
