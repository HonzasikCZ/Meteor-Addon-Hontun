package cz.honzasik.hontun.gui.theme.widgets.input;

import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.util.ColorUtils;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.input.WSlider;

public class WHontunSlider extends WSlider implements HontunWidget {
    public WHontunSlider(double value, double min, double max) {
        super(value, min, max);
    }

    @Override
    protected double handleSize() {
        return theme.textHeight() * 1.3f;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        HontunGuiTheme theme = theme();

        renderBar(theme);
        renderHandle(theme);
    }

    private void renderBar(HontunGuiTheme theme) {
        double halfHandleSize = handleSize() / 2;
        double backgroundY = y + height / 2 - halfHandleSize / 2;
        float smallerRadius = smallRadius() / 2;

        roundedRect().pos(x, backgroundY)
                     .size(valueWidth() + halfHandleSize / 2, halfHandleSize)
                     .radii(smallRadius(),
                             smallerRadius,
                             smallRadius(),
                             smallerRadius)
                     .color(theme.accentColor())
                     .render();

        roundedRect().pos(x + valueWidth() + handleSize() - halfHandleSize / 2, backgroundY)
                     .size(width - valueWidth() - handleSize() + halfHandleSize / 2, halfHandleSize)
                     .radii(smallerRadius,
                             smallRadius(),
                             smallerRadius,
                             smallRadius())
                     .color(ColorUtils.withAlpha(theme.accentColor(), 0.5))
                     .render();
    }

    private void renderHandle(HontunGuiTheme theme) {
        double size = handleSize();
        double handleX = x + valueWidth() + size / 2;
        double handleY = y + height / 2 - size / 2;
        double handleWidth = theme.scale(dragging ? 2 : 4);

        roundedRect().pos(handleX - handleWidth / 2, handleY)
                     .size(handleWidth, size)
                     .radius(smallRadius())
                     .color(theme.accentColor())
                     .render();
    }
}
