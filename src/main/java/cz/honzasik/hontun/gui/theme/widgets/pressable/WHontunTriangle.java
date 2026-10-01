package cz.honzasik.hontun.gui.theme.widgets.pressable;

import cz.honzasik.hontun.gui.theme.HontunWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WTriangle;

public class WHontunTriangle extends WTriangle implements HontunWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        style().paintTriangle(this, renderer, mouseX, mouseY);
    }

    public boolean isPressed() {
        return pressed;
    }
}
