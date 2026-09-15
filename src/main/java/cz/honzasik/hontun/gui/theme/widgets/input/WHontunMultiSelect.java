package cz.honzasik.hontun.gui.theme.widgets.input;

import cz.honzasik.hontun.gui.api.render.Corners;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.widget.input.WMultiSelect;
import cz.honzasik.hontun.gui.util.ColorUtils;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

import java.util.List;

public class WHontunMultiSelect<T> extends WMultiSelect<T> implements HontunWidget {
    public WHontunMultiSelect(String title, List<T> items) {
        super(title, items);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (expanded || animation.isRunning())
            roundedRect().pos(x, y + header.height)
                         .size(width, height - header.height)
                         .radius(radius(), Corners.BOTTOM)
                         .color(ColorUtils.withAlpha(theme().baseColor(), theme().backgroundOpacity()))
                         .render();
    }

    @Override
    protected WHeader createHeader() {
        return new WHontunHeader(title);
    }

    @Override
    protected WItem createItem(T item) {
        return new WHontunItem(item);
    }

    protected class WHontunHeader extends WHeader {
        public WHontunHeader(String title) {
            super(title);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            HontunGuiTheme theme = theme();
            Color bgColor = ColorUtils.withAlpha(
                    mouseOver ? theme.surface1Color() : theme.surface0Color(),
                    theme.backgroundOpacity()
            );

            roundedRect().bounds(this)
                         .radius(radius(), expanded || animation.isRunning() ? Corners.TOP : Corners.ALL)
                         .color(bgColor)
                         .render();
        }
    }

    protected class WHontunItem extends WItem {
        public WHontunItem(T item) {
            super(item);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            if (!mouseOver || checkbox.mouseOver) return;

            roundedRect().bounds(this)
                         .radius(smallRadius())
                         .color(theme().surface0Color())
                         .render();
        }
    }
}
