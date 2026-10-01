package cz.honzasik.hontun.gui.mixin.meteorclient;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import cz.honzasik.hontun.gui.render.HontunRenderer;
import meteordevelopment.meteorclient.renderer.text.VanillaTextRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = VanillaTextRenderer.class, remap = false)
public abstract class VanillaTextRendererMixin {
    @WrapOperation(
            method = "render(Ljava/lang/String;DDLmeteordevelopment/meteorclient/utils/render/color/Color;Z)D",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V")
    )
    private void hontun$text(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int color,
                             Operation<Void> original, @Local(argsOnly = true) boolean shadow) {
        boolean flat = HontunRenderer.flatText();
        boolean drawShadow = HontunRenderer.clickGuiTextPass() ? !flat : shadow && !flat;
        if (drawShadow) original.call(graphics, font, text, x, y, color);
        else graphics.text(font, text, x, y, color, false);
    }
}
