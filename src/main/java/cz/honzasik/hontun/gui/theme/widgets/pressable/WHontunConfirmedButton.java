package cz.honzasik.hontun.gui.theme.widgets.pressable;

import cz.honzasik.hontun.gui.theme.HontunWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.widgets.pressable.WConfirmedButton;

public class WHontunConfirmedButton extends WConfirmedButton implements HontunWidget {
    private final double[] size = new double[2];

    public WHontunConfirmedButton(String text, String confirmText, GuiTexture texture) {
        super(text, confirmText, texture);
    }

    @Override
    protected void onCalculateSize() {
        super.onCalculateSize();
        size[0] = width;
        size[1] = height;
        style().pressableSize(this, size);
        width = size[0];
        height = size[1];
    }

    @Override
    protected void onPressed(int button) {
        style().onPressed(this);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        style().paintConfirmedButton(this, renderer, mouseX, mouseY);
    }

    public boolean armed() {
        return pressedOnce;
    }

    public boolean isPressed() {
        return pressed;
    }

    public double labelWidth() {
        return textWidth;
    }

    public GuiTexture icon() {
        return texture;
    }
}
