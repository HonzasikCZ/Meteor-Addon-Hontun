package cz.honzasik.hontun.gui.theme.style.terminal;

import cz.honzasik.hontun.gui.theme.HontunWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;

import static cz.honzasik.hontun.gui.theme.style.terminal.TerminalPalette.opaque;

public class WTermMessageLine extends WWidget implements HontunWidget {
    private final WTermConfig config;

    public WTermMessageLine(WTermConfig config) {
        this.config = config;
    }

    public WTermConfig config() {
        return config;
    }

    @Override
    protected void onCalculateSize() {
        TerminalGrid g = TerminalGrid.of(theme());
        width = 0;
        height = g.ml;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        TerminalGrid g = TerminalGrid.of(theme());

        int cols = (int) Math.floor((width - 2 * g.cw) / g.cw);
        if (cols < 2) return;

        String text = config.description();
        TerminalPaint.text(WTermConfig.trunc(text, cols), x + g.cw, g.ty(y, height), opaque(TerminalPalette.dim));
    }
}
