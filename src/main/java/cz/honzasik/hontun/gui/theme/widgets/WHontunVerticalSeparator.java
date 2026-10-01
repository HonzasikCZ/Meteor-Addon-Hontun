package cz.honzasik.hontun.gui.theme.widgets;

import cz.honzasik.hontun.gui.theme.HontunWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WVerticalSeparator;

public class WHontunVerticalSeparator extends WVerticalSeparator implements HontunWidget {
    public double size = 2;

    @Override
    protected void onCalculateSize() {
        width = theme.scale(size);
        height = 1;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        style().paintSeparatorV(this, renderer, mouseX, mouseY);
    }
}
