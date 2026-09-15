package cz.honzasik.hontun.gui.theme.widgets.pressable;

import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.icons.HontunBuiltinIcons;
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
        size = theme.textHeight();
    }

    @Override
    protected void onCalculateSize() {
        width = size;
        height = size;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        renderer.quad(
                x,
                y,
                size,
                size,
                checked ? HontunBuiltinIcons.BOOKMARK_YES.texture() : HontunBuiltinIcons.BOOKMARK_NO.texture(),
                getColor()
        );
    }

    @Override
    protected Color getColor() {
        return checked
                ? theme().accentColor()
                : mouseOver
                    ? theme().textSecondaryColor()
                    : theme().textColor();
    }
}
