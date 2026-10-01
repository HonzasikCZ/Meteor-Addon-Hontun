package cz.honzasik.hontun.gui.theme.widgets.pressable;

import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.widget.pressable.WColorPicker;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WHontunColorPicker extends WColorPicker implements HontunWidget {
    private final double[] size = new double[2];

    public WHontunColorPicker(Color color, GuiTexture overlayTexture) {
        super(color, overlayTexture);
    }

    @Override
    protected void onCalculateSize() {
        style().colorPickerSize(this, size);
        width = size[0];
        height = size[1];
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        style().paintColorPicker(this, renderer, mouseX, mouseY);
    }

    public Color color() {
        return color;
    }

    public GuiTexture overlay() {
        return overlayTexture;
    }

    public boolean isPressed() {
        return pressed;
    }
}
