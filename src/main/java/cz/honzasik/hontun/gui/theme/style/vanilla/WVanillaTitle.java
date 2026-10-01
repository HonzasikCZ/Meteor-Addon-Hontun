package cz.honzasik.hontun.gui.theme.style.vanilla;

import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.render.pixel.PixelCanvas;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.widgets.WHontunLabel;
import cz.honzasik.hontun.gui.theme.widgets.container.WHontunWindow;

public class WVanillaTitle extends WHontunLabel {
    private static final int DIALOG_WIDTH = 250;

    private final int rows;
    private final WHontunWindow window;

    public WVanillaTitle(RichText text, int rows, WHontunWindow window) {
        super(text);
        this.rows = rows;
        this.window = window;
        hidden = true;
    }

    @Override
    protected void onCalculateSize() {
        HontunGuiTheme t = theme();
        int p = PixelCanvas.unit(t);

        width = t.textWidth(richText);
        height = rows * p;

        if (window == null || !window.isDialog()) return;

        double chrome = 16 * p + t.textHeight();
        if (window.icon != null) chrome += window.icon.width + 4 * p;
        width = Math.max(width, DIALOG_WIDTH * p - chrome);
    }
}
