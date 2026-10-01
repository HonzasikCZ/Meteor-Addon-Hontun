package cz.honzasik.hontun.gui.mixin;

import cz.honzasik.hontun.gui.render.HontunRenderer;
import cz.honzasik.hontun.gui.render.pixel.PixelCanvas;
import cz.honzasik.hontun.gui.render.rounded.modern.RoundedRendererModern;
import cz.honzasik.hontun.gui.render.text.HontunTextRenderer;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.style.Pipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {HontunTextRenderer.class, RoundedRendererModern.class}, remap = false)
public abstract class PixelRouteGuardMixin {
    @Inject(
            method = {
                    "text(Lcz/honzasik/hontun/gui/api/text/RichText;DDLmeteordevelopment/meteorclient/utils/render/color/Color;Lcz/honzasik/hontun/gui/theme/HontunGuiTheme;)V",
                    "render(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lcz/honzasik/hontun/gui/theme/HontunGuiTheme;)V",
                    "render(Lcz/honzasik/hontun/gui/api/render/RoundedRect;)V"
            },
            at = @At("HEAD"),
            require = 0
    )
    private void hontun$pixelRouteGuard(CallbackInfo ci) {
        HontunGuiTheme theme = HontunRenderer.get().theme();
        if (theme != null && theme.style().pipeline() == Pipeline.PIXEL)
            PixelCanvas.missedRoute(getClass().getSimpleName());
    }
}
