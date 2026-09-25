package cz.honzasik.hontun.gui.theme.widgets;

import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.theme.HontunLightPalette;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WHontunSwatchLabel extends WHontunLabel {
    private static final double MIN_CONTRAST = 2.2;

    public WHontunSwatchLabel(RichText text) {
        super(text);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (color != null && !text.isEmpty()) {
            int rgb = packed(color);
            if (HontunLightPalette.contrast(rgb, packed(theme().mantleColor())) < MIN_CONTRAST) {
                Color chip = HontunLightPalette.luminance(rgb) > 0.4 ? new Color(22, 24, 28) : new Color(244, 245, 247);
                double pad = theme().scale(2);
                roundedRect().pos(x - pad, y - pad / 2)
                             .size(width + pad * 2, height + pad)
                             .radius(smallRadius())
                             .color(chip)
                             .render();
            }
        }
        super.onRender(renderer, mouseX, mouseY, delta);
    }

    private static int packed(Color c) {
        return (c.r << 16) | (c.g << 8) | c.b;
    }
}
