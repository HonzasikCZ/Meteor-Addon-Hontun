package cz.honzasik.hontun.gui.theme.widgets.pressable;

import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.widget.pressable.WColorPicker;
import cz.honzasik.hontun.gui.util.ColorUtils;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WHontunColorPicker extends WColorPicker implements HontunWidget {
    public WHontunColorPicker(Color color, GuiTexture overlayTexture) {
        super(color, overlayTexture);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        background(mouseOver ? ColorUtils.darker(color) : color, theme().surface2Color()).render();

        if (mouseOver) {
            double s = theme.textHeight();

            renderer.quad(
                    x + width / 2 - s / 2,
                    y + height / 2 - s / 2,
                    s,
                    s,
                    overlayTexture,
                    theme().textColor()
            );
        }
    }
}
