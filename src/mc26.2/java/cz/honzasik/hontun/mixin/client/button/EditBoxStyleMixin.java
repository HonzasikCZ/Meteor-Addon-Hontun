package cz.honzasik.hontun.mixin.client.button;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import cz.honzasik.hontun.gui.widget.HontunRound;
import cz.honzasik.hontun.gui.widget.HontunTextBox;
import cz.honzasik.hontun.gui.widget.TerminalWidgets;
import cz.honzasik.hontun.utils.HontunFont;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2fStack;
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
    @Shadow private int textY;
    @Shadow private int cursorPos;
    @Shadow @Final private List<EditBox.TextFormatter> formatters;

    @Shadow public abstract boolean isBordered();

    @Shadow public abstract int getInnerWidth();

    @Shadow
    private FormattedCharSequence applyFormat(String text, int offset) {
        throw new AssertionError();
    }

    @Unique
    private boolean hontun$sf() {
        return HontunFont.active() && isBordered() && formatters.isEmpty();
    }

    @Unique
    private boolean hontun$term() {
        if (!HontunTheme.terminal()) return false;
        return (isBordered() && formatters.isEmpty()) || (Object) this instanceof HontunTextBox;
    }

    @Unique
    private static Style hontun$mono() {
        return Style.EMPTY.withFont(HontunFont.MONO);
    }

    @Unique
    private String hontun$shown() {
        String rest = value.substring(Math.min(displayPos, value.length()));
        return font.getSplitter().plainHeadByWidth(rest, getInnerWidth(), hontun$mono());
    }

    @Unique
    private float hontun$at(String shown, int rel) {
        if (rel <= 0) return textX;
        if (rel > shown.length()) return textX + ((EditBox) (Object) this).getWidth();
        return textX + TerminalWidgets.advance(font, applyFormat(shown.substring(0, rel), displayPos));
    }

    @WrapOperation(method = {"extractWidgetRenderState", "updateTextPosition", "scrollTo", "findClickedPositionInText"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;plainSubstrByWidth(Ljava/lang/String;I)Ljava/lang/String;"))
    private String hontun$fitHead(Font f, String s, int w, Operation<String> op) {
        if (hontun$term()) return f.getSplitter().plainHeadByWidth(s, w, hontun$mono());
        return op.call(f, s, w);
    }

    @WrapOperation(method = "scrollTo", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Font;plainSubstrByWidth(Ljava/lang/String;IZ)Ljava/lang/String;"))
    private String hontun$fitTail(Font f, String s, int w, boolean tail, Operation<String> op) {
        if (!hontun$term()) return op.call(f, s, w, tail);
        return tail
                ? f.getSplitter().plainTailByWidth(s, w, hontun$mono())
                : f.getSplitter().plainHeadByWidth(s, w, hontun$mono());
    }

    @WrapOperation(method = {"extractWidgetRenderState", "updateTextPosition", "getScreenX"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;width(Ljava/lang/String;)I"))
    private int hontun$measure(Font f, String s, Operation<Integer> op) {
        if (hontun$term()) return f.width(HontunFont.text(s));
        return op.call(f, s);
    }

    @WrapOperation(method = "extractWidgetRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V",
            ordinal = 1))
    private void hontun$tail(GuiGraphicsExtractor g, Font f, FormattedCharSequence s, int x, int y, int color,
                             boolean shadow, Operation<Void> op) {
        if (!hontun$term()) {
            op.call(g, f, s, x, y, color, shadow);
            return;
        }
        float ex = hontun$at(hontun$shown(), cursorPos - displayPos);
        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.translate(ex - x, 0f);
        op.call(g, f, s, x, y, color, shadow);
        pose.popMatrix();
    }

    @WrapOperation(method = "extractWidgetRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/TextCursorUtils;isCursorVisible(J)Z"))
    private boolean hontun$blink(long time, Operation<Boolean> op) {
        if (hontun$term()) return TerminalWidgets.blinkOn(time);
        return op.call(time);
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
        if (hontun$term()) TerminalWidgets.appendCaret(g, font, hontun$appendX(x), y);
        else if (hontun$sf()) hontun$caret(g, x, y, color);
        else op.call(g, f, x, y, color, shadow);
    }

    @WrapOperation(method = "extractWidgetRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/TextCursorUtils;extractInsertCursor(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIII)V"))
    private void hontun$insertCursor(GuiGraphicsExtractor g, int x, int y, int color, int lineHeight, Operation<Void> op) {
        if (hontun$term()) hontun$block(g, x, y);
        else if (hontun$sf()) hontun$caret(g, x, y, color);
        else op.call(g, x, y, color, lineHeight);
    }

    @WrapOperation(method = "extractWidgetRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;textHighlight(IIIIZ)V"))
    private void hontun$highlight(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, boolean invert, Operation<Void> op) {
        if (hontun$term()) {
            EditBox self = (EditBox) (Object) this;
            String shown = hontun$shown();
            float right = self.getX() + self.getWidth();
            float from = Math.min(hontun$at(shown, cursorPos - displayPos), right);
            float to = Math.min(hontun$at(shown, Mth.clamp(highlightPos - displayPos, 0, shown.length())), right);
            TerminalWidgets.selection(g, from, to, textY);
            return;
        }
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
    private float hontun$appendX(int x) {
        String shown = hontun$shown();
        int rel = cursorPos - displayPos;
        if (shown.isEmpty() || rel <= 0 || rel > shown.length()) return x;
        return hontun$at(shown, rel) + 1f;
    }

    @Unique
    private void hontun$block(GuiGraphicsExtractor g, int x, int y) {
        String v = value;
        int pos = Mth.clamp(cursorPos, 0, v.length());
        String shown = hontun$shown();
        int rel = pos - displayPos;
        float bx = rel < 0 || rel > shown.length() ? x : hontun$at(shown, rel);
        if (pos >= v.length()) {
            TerminalWidgets.appendCaret(g, font, bx, y);
            return;
        }
        int end = Math.min(v.length(), pos + Character.charCount(v.codePointAt(pos)));
        FormattedCharSequence glyph = applyFormat(v.substring(pos, end), pos);
        float w = Math.max(1f, TerminalWidgets.advance(font, glyph));
        TerminalWidgets.cursor(g, bx, y, w, TerminalWidgets.fill());
        TerminalWidgets.textAt(g, font, glyph, bx, y, TerminalWidgets.ink());
    }

    @Unique
    private static void hontun$caret(GuiGraphicsExtractor g, int x, int y, int color) {
        HontunRound.fill(g, x, y - 1, 1, 10, 1, color);
    }
}
