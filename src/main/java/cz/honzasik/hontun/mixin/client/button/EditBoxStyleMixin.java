package cz.honzasik.hontun.mixin.client.button;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import cz.honzasik.hontun.gui.widget.HontunRound;
import cz.honzasik.hontun.gui.widget.HontunTextBox;
import cz.honzasik.hontun.utils.HontunFont;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EditBox.class)
public abstract class EditBoxStyleMixin {
    @Shadow @Final private Font font;
    @Shadow private String value;
    @Shadow private int displayPos;
    @Shadow private int highlightPos;
    @Shadow private int textX;
    @Shadow @Final private List<EditBox.TextFormatter> formatters;

    @Shadow public abstract boolean isBordered();

    @Shadow public abstract int getInnerWidth();

    @Unique
    private boolean hontun$sf() {
        return HontunTheme.smog() && isBordered() && formatters.isEmpty();
    }

    @Inject(method = "applyFormat", at = @At("HEAD"), cancellable = true)
    private void hontun$sfFormat(String text, int offset, CallbackInfoReturnable<FormattedCharSequence> cir) {
        if (hontun$sf()) cir.setReturnValue(HontunFont.apply(Component.literal(text)).getVisualOrderText());
    }

    @WrapOperation(method = "extractWidgetRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private void hontun$box(GuiGraphicsExtractor g, RenderPipeline pipeline, Identifier sprite, int x, int y, int w, int h,
                            Operation<Void> op) {
        EditBox self = (EditBox) (Object) this;
        if (!HontunTextBox.themedBox(g, x, y, w, h, self.isFocused(), self.isHovered(), self.isActive())) {
            op.call(g, pipeline, sprite, x, y, w, h);
        }
    }

    @WrapOperation(method = "extractWidgetRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"))
    private void hontun$hint(GuiGraphicsExtractor g, Font f, Component text, int x, int y, int color, Operation<Void> op) {
        op.call(g, f, hontun$sf() ? HontunFont.apply(text) : text, x, y, color);
    }

    @WrapOperation(method = "extractWidgetRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/TextCursorUtils;extractAppendCursor(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIIZ)V"))
    private void hontun$appendCursor(GuiGraphicsExtractor g, Font f, int x, int y, int color, boolean shadow, Operation<Void> op) {
        if (hontun$sf()) hontun$caret(g, x, y, color);
        else op.call(g, f, x, y, color, shadow);
    }

    @WrapOperation(method = "extractWidgetRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/TextCursorUtils;extractInsertCursor(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIII)V"))
    private void hontun$insertCursor(GuiGraphicsExtractor g, int x, int y, int color, int lineHeight, Operation<Void> op) {
        if (hontun$sf()) hontun$caret(g, x, y, color);
        else op.call(g, x, y, color, lineHeight);
    }

    @WrapOperation(method = "extractWidgetRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;textHighlight(IIIIZ)V"))
    private void hontun$highlight(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, boolean invert, Operation<Void> op) {
        if (hontun$sf()) {
            EditBox self = (EditBox) (Object) this;
            String displayed = font.plainSubstrByWidth(value.substring(Math.min(displayPos, value.length())), getInnerWidth());
            int rel = Mth.clamp(highlightPos - displayPos, 0, displayed.length());
            int hx = textX + font.width(HontunFont.apply(Component.literal(displayed.substring(0, rel))));
            x1 = Math.min(hx - 1, self.getX() + self.getWidth());
        }
        op.call(g, x0, y0, x1, y1, invert);
    }

    @Unique
    private static void hontun$caret(GuiGraphicsExtractor g, int x, int y, int color) {
        HontunRound.fill(g, x, y - 1, 1, 10, 1, color);
    }
}
