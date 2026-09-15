package cz.honzasik.hontun.gui.theme.widgets;

import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.widget.WGuiTexture;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;

public class WHontunGuiTexture extends WGuiTexture implements HontunWidget {
    public WHontunGuiTexture(GuiTexture texture, double size) {
        super(texture, size);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        renderer.quad(x, y, size, size, texture, color);
    }
}
