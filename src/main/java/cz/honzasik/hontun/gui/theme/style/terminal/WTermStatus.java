package cz.honzasik.hontun.gui.theme.style.terminal;

import cz.honzasik.hontun.gui.theme.HontunWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;

public class WTermStatus extends WWidget implements HontunWidget {
    private String status = "";
    private long builtAt;
    private int length = -1;

    private String status() {
        long now = System.currentTimeMillis();
        if (status.isEmpty() || now - builtAt > 500) {
            status = TerminalClickStyle.statusText();
            builtAt = now;
        }
        return status;
    }

    @Override
    protected void onCalculateSize() {
        TerminalGrid g = TerminalGrid.of(theme());
        String s = status();
        length = s.length();
        width = (length + 2) * g.cw;
        height = g.tb;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        String s = status();
        if (s.length() != length) invalidate();
        if (style() instanceof TerminalClickStyle style) style.paintStatus(this, s);
    }
}
