package cz.honzasik.hontun.gui.theme.widgets.container;

import cz.honzasik.hontun.gui.render.route.Routers;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.style.ClickStyle;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.utils.Utils;

public class WHontunView extends WView implements HontunWidget {
    @Override
    public void init() {
        maxHeight = Utils.getWindowHeight() - theme.scale(metrics().viewMaxHeightInset);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        ClickStyle style = style();
        Routers.of(style.pipeline()).afterViewUpdate(this);
        style.paintViewUnderlay(this, renderer, mouseX, mouseY);
        style.paintScrollbar(this, renderer, mouseX, mouseY);
    }

    @Override
    protected double handleWidth() {
        return style().scrollbarWidth(theme());
    }

    public boolean scrollable() {
        return canScroll;
    }

    public boolean barHovered() {
        return handleMouseOver;
    }

    public double barX() {
        return handleX();
    }

    public double barY() {
        return handleY();
    }

    public double barWidth() {
        return handleWidth();
    }

    public double barHeight() {
        return handleHeight();
    }
}
