package cz.honzasik.hontun.gui.theme.widgets.pressable;

import cz.honzasik.hontun.gui.theme.HontunWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WFavorite;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WHontunFavorite extends WFavorite implements HontunWidget {
    double size;

    public WHontunFavorite(boolean checked) {
        super(checked);
    }

    @Override
    public void init() {
        size = style().favoriteSize(this);
    }

    @Override
    protected void onCalculateSize() {
        width = size;
        height = size;
    }

    @Override
    protected void onPressed(int button) {
        super.onPressed(button);
        style().onPressed(this);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        style().paintFavorite(this, renderer, mouseX, mouseY);
    }

    public double size() {
        return size;
    }

    public Color tint() {
        return getColor();
    }

    public boolean isPressed() {
        return pressed;
    }

    @Override
    protected Color getColor() {
        return style().favoriteColor(this);
    }
}
