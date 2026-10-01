package cz.honzasik.hontun.gui.theme.widgets.input;

import cz.honzasik.hontun.gui.theme.HontunWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.input.WSlider;
import net.minecraft.client.input.MouseButtonEvent;

public class WHontunSlider extends WSlider implements HontunWidget {
    public WHontunSlider(double value, double min, double max) {
        super(value, min, max);
    }

    @Override
    public double handleSize() {
        return theme.textHeight() * 1.3f;
    }

    @Override
    protected void onCalculateSize() {
        width = handleSize();
        height = style().sliderHeight(this);
    }

    @Override
    public boolean onMouseReleased(MouseButtonEvent click) {
        boolean wasDragging = dragging;
        boolean result = super.onMouseReleased(click);
        if (wasDragging) style().onPressed(this);
        return result;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        style().paintSlider(this, renderer, mouseX, mouseY);
    }

    public double valueOffset() {
        return valueWidth();
    }

    public boolean isDragging() {
        return dragging;
    }

    public boolean handleHovered() {
        return handleMouseOver;
    }

    public double value() {
        return value;
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }
}
