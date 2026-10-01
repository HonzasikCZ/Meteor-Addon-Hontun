package cz.honzasik.hontun.gui.theme.widgets.pressable;

import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.style.AnimRole;
import cz.honzasik.hontun.gui.theme.style.StyleAnimation;
import cz.honzasik.hontun.gui.widget.IConditionalWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;

import java.util.function.BooleanSupplier;

public class WHontunButton extends WButton implements IConditionalWidget, HontunWidget {
    private BooleanSupplier visibilityCondition;
    private RichText richText;

    private StyleAnimation hoverAnimation;

    private final double[] size = new double[2];

    public WHontunButton(RichText text, GuiTexture texture) {
        super(text.getPlainText(), texture);
        this.richText = text;
    }

    public WHontunButton(GuiTexture texture) {
        super(null, texture);
    }

    @Override
    public void init() {
        hoverAnimation = StyleAnimation.of(style().anim(AnimRole.BUTTON_HOVER), theme());
    }

    public RichText displayText() {
        if (richText != null) return richText;
        if (texture == null) return null;

        String iconText = style().iconButtonText(texture);
        return iconText != null ? RichText.of(iconText) : null;
    }

    @Override
    protected void onCalculateSize() {
        RichText text = displayText();
        if (text != null) textWidth = theme().textWidth(text);

        style().buttonSize(this, size);
        width = size[0];
        height = size[1];
    }

    @Override
    protected void onPressed(int button) {
        style().onPressed(this);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!shouldRender(mouseOver)) return;

        double hoverProgress = hoverAnimation.getProgress();

        if (mouseOver && hoverProgress == 0)
            hoverAnimation.start();

        if (!mouseOver && hoverProgress > 0)
            hoverAnimation.reset();

        style().paintButton(this, renderer, mouseX, mouseY);
    }

    public double hoverProgress() {
        return hoverAnimation.getProgress();
    }

    public boolean isPressed() {
        return pressed;
    }

    public double labelWidth() {
        return textWidth;
    }

    public GuiTexture icon() {
        return texture;
    }

    public void set(RichText text) {
        if (richText == null || Math.round(theme().textWidth(richText)) != textWidth) invalidate();

        richText = text;
    }

    @Override
    public void set(String text) {
        set(RichText.of(text));
    }

    @Override
    public BooleanSupplier getVisibilityCondition() {
        return visibilityCondition;
    }

    @Override
    public void setVisibilityCondition(BooleanSupplier condition) {
        visibilityCondition = condition;
    }
}
