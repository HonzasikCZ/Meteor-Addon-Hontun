package cz.honzasik.hontun.gui.theme.widgets;

import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.style.Metrics;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WTooltip;

public class WHontunTooltip extends WTooltip implements HontunWidget {
    public WHontunTooltip(String text) {
        super(text);
    }

    @Override
    public void init() {
        Metrics m = metrics();
        add(theme.label(text)).padVertical(m.tooltipPadV).padHorizontal(m.tooltipPadH);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        style().paintTooltip(this, renderer, mouseX, mouseY);
    }

    public String textValue() {
        return text;
    }
}
