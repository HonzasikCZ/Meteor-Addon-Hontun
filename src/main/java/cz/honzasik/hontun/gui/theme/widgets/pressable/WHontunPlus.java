package cz.honzasik.hontun.gui.theme.widgets.pressable;

import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.icons.HontunBuiltinIcons;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;

public class WHontunPlus extends WPlus implements HontunWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        HontunGuiTheme theme = theme();
        double pad = pad();
        double s = theme.textHeight();

        background(pressed, mouseOver).render();

        renderer.quad(
                x + pad,
                y + pad,
                s,
                s,
                HontunBuiltinIcons.PLUS.texture(),
                theme.greenColor()
        );
    }
}
