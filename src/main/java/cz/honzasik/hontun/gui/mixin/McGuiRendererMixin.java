package cz.honzasik.hontun.gui.mixin;

import cz.honzasik.hontun.gui.render.text.FallbackTextLayer;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiRenderer.class)
public abstract class McGuiRendererMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void hontun$capture(GuiRenderState renderState, FeatureRenderDispatcher features,
                                List<PictureInPictureRenderer<?>> pictureInPicture, CallbackInfo ci) {
        if (((Object) this).getClass() == GuiRenderer.class) FallbackTextLayer.capture(features, pictureInPicture);
    }
}
