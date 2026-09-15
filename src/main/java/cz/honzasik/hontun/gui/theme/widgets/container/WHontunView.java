package cz.honzasik.hontun.gui.theme.widgets.container;

import cz.honzasik.hontun.gui.theme.HontunWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.utils.Utils;

public class WHontunView extends WView implements HontunWidget {
    @Override
    public void init() {
        maxHeight = Utils.getWindowHeight() - theme.scale(200);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (canScroll && hasScrollBar) {
            roundedRect().pos(handleX(), handleY())
                         .size(handleWidth(), handleHeight())
                         .radius(smallRadius())
                         .color(theme().scrollbarColor.get(

                                 focused,

                                 handleMouseOver
                         ))
                         .render();
        }
    }
}
