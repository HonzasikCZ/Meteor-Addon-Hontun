package cz.honzasik.hontun.gui.theme.style.hvanilla;

import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.render.pixel.PixelCanvas;
import cz.honzasik.hontun.gui.theme.widgets.WHontunLabel;

public class WHVanillaTitle extends WHontunLabel {
    private final int rows;

    public WHVanillaTitle(RichText text, int rows) {
        super(text);
        this.rows = rows;
        hidden = true;
    }

    @Override
    protected void onCalculateSize() {
        width = theme().textWidth(richText);
        height = rows * PixelCanvas.unit(theme());
    }
}
