package cz.honzasik.hontun.gui.theme.widgets;

import cz.honzasik.hontun.gui.theme.HontunWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WTooltip;

public class WHontunTooltip extends WTooltip implements HontunWidget {
    public WHontunTooltip(String text) {
        super(text);
    }

    @Override
    public void init() {
        add(theme.label(text)).padVertical(4).padHorizontal(6);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        background(theme().baseColor(), theme().surface0Color()).render();
    }
}
