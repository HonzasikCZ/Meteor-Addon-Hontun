package cz.honzasik.hontun.gui.theme.style.terminal;

import cz.honzasik.hontun.gui.theme.HontunWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;

public class WTermSession extends WWidget implements HontunWidget {
    @Override
    protected void onCalculateSize() {
        TerminalGrid g = TerminalGrid.of(theme());
        width = ("[hontun]".length() + 2) * g.cw;
        height = g.tb;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (style() instanceof TerminalClickStyle style) style.paintSession(this);
    }
}
