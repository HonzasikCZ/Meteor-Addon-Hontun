package cz.honzasik.hontun.mixin.client.container;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import cz.honzasik.hontun.gui.widget.HontunContainers;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractContainerScreen.class)
public abstract class SlotHighlightMixin {
    @WrapOperation(method = "extractSlotHighlightBack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private void hontun$highlightBack(GuiGraphicsExtractor g, RenderPipeline pipeline, Identifier sprite,
                                      int x, int y, int w, int h, Operation<Void> op) {
        if (!HontunContainers.slotHighlight(g, (AbstractContainerScreen<?>) (Object) this, x + 4, y + 4, false)) {
            op.call(g, pipeline, sprite, x, y, w, h);
        }
    }

    @WrapOperation(method = "extractSlotHighlightFront", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private void hontun$highlightFront(GuiGraphicsExtractor g, RenderPipeline pipeline, Identifier sprite,
                                       int x, int y, int w, int h, Operation<Void> op) {
        if (!HontunContainers.slotHighlight(g, (AbstractContainerScreen<?>) (Object) this, x + 4, y + 4, true)) {
            op.call(g, pipeline, sprite, x, y, w, h);
        }
    }
}
