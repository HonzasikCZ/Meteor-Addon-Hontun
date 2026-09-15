package cz.honzasik.hontun.gui.mixin.meteorclient;

import cz.honzasik.hontun.gui.HontunGui;
import cz.honzasik.hontun.gui.render.text.RichTextRenderer;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Fonts.class, remap = false)
public abstract class FontsMixin {
    @Inject(method = "load", at = @At("HEAD"))
    private static void hontun$load(FontFace fontFace, CallbackInfo ci) {
        if (!(GuiThemes.get() instanceof HontunGuiTheme theme)) return;

        if (theme.textRenderer() instanceof RichTextRenderer currentRenderer)
             if (currentRenderer.fontFace.equals(fontFace)) return;

        try {
            theme.setTextRenderer(new RichTextRenderer(fontFace));
        } catch (Exception e) {
            HontunGui.LOG.error("Failed to load font: {}", fontFace, e);
        }
    }
}
