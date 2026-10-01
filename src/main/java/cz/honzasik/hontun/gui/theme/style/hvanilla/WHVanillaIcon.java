package cz.honzasik.hontun.gui.theme.style.hvanilla;

import cz.honzasik.hontun.gui.render.pixel.PixelCanvas;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.widgets.WWidget;

public class WHVanillaIcon extends WWidget implements HontunWidget {
    private final GuiTexture texture;

    public WHVanillaIcon(GuiTexture texture) {
        this.texture = texture;
    }

    public GuiTexture texture() {
        return texture;
    }

    @Override
    protected void onCalculateSize() {
        int p = PixelCanvas.unit(theme());
        width = 8 * p;
        height = 8 * p;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (style() instanceof HVanillaClickStyle style) style.paintHeaderIcon(this, renderer);
        else renderer.quad(x, y, width, height, texture, theme().textColor());
    }
}
