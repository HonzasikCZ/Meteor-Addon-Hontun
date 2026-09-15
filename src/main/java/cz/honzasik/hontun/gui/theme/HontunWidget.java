package cz.honzasik.hontun.gui.theme;

import cz.honzasik.hontun.gui.api.render.RoundedRect;
import cz.honzasik.hontun.gui.render.HontunRenderer;
import cz.honzasik.hontun.gui.api.render.Corners;
import cz.honzasik.hontun.gui.util.ColorUtils;
import meteordevelopment.meteorclient.gui.utils.BaseWidget;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.utils.render.color.Color;

public interface HontunWidget extends BaseWidget {
    default HontunGuiTheme theme() {
        return (HontunGuiTheme) getTheme();
    }

    default HontunRenderer renderer() {
        return HontunRenderer.get();
    }

    default RoundedRect roundedRect() {
        return RoundedRect.get();
    }

    default float radius() {
        return (float) (theme().scale(theme().effCornerRadius()));
    }

    default float smallRadius() {
        return (float) (theme().scale(theme().effSmallCornerRadius()));
    }

    default Corners corners() {
        return Corners.ALL;
    }

    default float outlineWidth() {
        return 2f;
    }

    default RoundedRect background(Color backgroundColor, Color outlineColor) {
        return roundedRect().bounds((WWidget) this)
                            .radius(smallRadius(), corners())
                            .color(backgroundColor)
                            .outline(outlineColor, outlineWidth());
    }

    default RoundedRect background(boolean pressed, boolean mouseOver) {
        return background(getBackgroundColor(pressed, mouseOver), getOutlineColor(pressed, mouseOver));
    }

    default Color getBackgroundColor(boolean pressed, boolean mouseOver) {
        HontunGuiTheme theme = theme();

        return ColorUtils.withAlpha(
                theme.backgroundColor.get(pressed, mouseOver),
                theme.backgroundOpacity()
        );
    }

    default Color getOutlineColor(boolean pressed, boolean mouseOver) {
        HontunGuiTheme theme = theme();

        return ColorUtils.withAlpha(
                theme.outlineColor.get(pressed, mouseOver),
                theme.backgroundOpacity() * 0.5
        );
    }
}
