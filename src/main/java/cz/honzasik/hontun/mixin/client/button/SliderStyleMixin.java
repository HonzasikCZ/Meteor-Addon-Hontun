package cz.honzasik.hontun.mixin.client.button;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import cz.honzasik.hontun.gui.widget.HontunSliders;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractSliderButton.class)
public abstract class SliderStyleMixin {
    @Shadow protected double value;
    @Shadow protected boolean canChangeValue;
    @Shadow private boolean dragging;

    @WrapOperation(method = "extractWidgetRenderState", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V"))
    private void hontun$track(GuiGraphicsExtractor g, RenderPipeline pipeline, Identifier sprite,
                              int x, int y, int w, int h, int color, Operation<Void> op) {
        if (!HontunTheme.restyleEnabled()) {
            op.call(g, pipeline, sprite, x, y, w, h, color);
            return;
        }
        AbstractWidget self = (AbstractWidget) (Object) this;
        boolean active = self.active;
        boolean hot = active && (self.isHovered() || dragging);
        boolean focused = active && self.isFocused() && !canChangeValue;
        int cx = x + 4 + (int) (value * (double) (w - 8));
        HontunSliders.track(g, x, y, w, h, cx, active, hot, focused, color >>> 24);
    }

    @WrapOperation(method = "extractWidgetRenderState", at = @At(value = "INVOKE", ordinal = 1,
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V"))
    private void hontun$knob(GuiGraphicsExtractor g, RenderPipeline pipeline, Identifier sprite,
                             int hx, int y, int hw, int h, int color, Operation<Void> op) {
        if (!HontunTheme.restyleEnabled()) {
            op.call(g, pipeline, sprite, hx, y, hw, h, color);
            return;
        }
        AbstractWidget self = (AbstractWidget) (Object) this;
        boolean active = self.active;
        boolean hot = active && (self.isHovered() || canChangeValue || dragging);
        HontunSliders.knob(g, hx, y, hw, h, active, hot, color >>> 24);
    }
}
