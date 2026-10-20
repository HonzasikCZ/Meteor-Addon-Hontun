package cz.honzasik.hontun.mixin.client.container;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import cz.honzasik.hontun.gui.widget.HontunContainers;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractFurnaceScreen.class)
public abstract class FurnaceProgressMixin {
    @WrapOperation(method = "extractBackground", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIIIIII)V"))
    private void hontun$progress(GuiGraphicsExtractor g, RenderPipeline pipeline, Identifier sprite,
                                 int spriteW, int spriteH, int u, int v, int x, int y, int w, int h,
                                 Operation<Void> op) {
        if (!HontunTheme.restyleEnabled()) {
            op.call(g, pipeline, sprite, spriteW, spriteH, u, v, x, y, w, h);
            return;
        }
        if (spriteW == 14) HontunContainers.flameLit(g, x, y - v, h);
        else if (spriteW == 24) HontunContainers.arrowLit(g, x - u, y, w);
        else op.call(g, pipeline, sprite, spriteW, spriteH, u, v, x, y, w, h);
    }
}
