package cz.honzasik.hontun.gui.theme.style.flat;

import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;

public class FlatTick extends WWidget {
    @Override
    protected void onCalculateSize() {
        HontunGuiTheme t = (HontunGuiTheme) theme;
        width = Math.max(2, Math.round(2 * t.scale(1)));
        height = Math.round(0.7 * t.textHeight());
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (((HontunGuiTheme) theme).style() instanceof FlatClickStyle style) style.paintTick(this);
    }
}
