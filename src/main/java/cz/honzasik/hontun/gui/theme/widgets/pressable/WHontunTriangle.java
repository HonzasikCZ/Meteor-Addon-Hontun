package cz.honzasik.hontun.gui.theme.widgets.pressable;

import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.icons.HontunBuiltinIcons;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WTriangle;

public class WHontunTriangle extends WTriangle implements HontunWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double s = theme.textHeight();

        renderer.rotatedQuad(
                x,
                y,
                s,
                s,
                rotation,
                HontunBuiltinIcons.ARROW.texture(),
                theme.textColor()
        );
    }
}
