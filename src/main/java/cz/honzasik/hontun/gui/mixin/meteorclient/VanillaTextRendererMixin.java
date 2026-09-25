package cz.honzasik.hontun.gui.mixin.meteorclient;

import cz.honzasik.hontun.gui.render.HontunRenderer;
import meteordevelopment.meteorclient.renderer.text.VanillaTextRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = VanillaTextRenderer.class, remap = false)
public abstract class VanillaTextRendererMixin {
    @Redirect(
            method = "render(Ljava/lang/String;DDLmeteordevelopment/meteorclient/utils/render/color/Color;Z)D",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V")
    )
    private void hontun$text(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int color) {
        graphics.text(font, text, x, y, color, !HontunRenderer.flatText());
    }
}
