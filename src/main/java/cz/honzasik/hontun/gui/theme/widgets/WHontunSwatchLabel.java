package cz.honzasik.hontun.gui.theme.widgets;

import cz.honzasik.hontun.gui.api.text.RichText;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;

public class WHontunSwatchLabel extends WHontunLabel {
    public static final double MIN_CONTRAST = 2.2;

    public WHontunSwatchLabel(RichText text) {
        super(text);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        style().paintSwatchChip(this, renderer, mouseX, mouseY);
        super.onRender(renderer, mouseX, mouseY, delta);
    }
}
