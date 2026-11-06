package cz.honzasik.hontun.gui.theme.style.terminal;

import cz.honzasik.hontun.gui.theme.HontunWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;

public class WTermPromptLine extends WWidget implements HontunWidget {
    private final String command;
    private final boolean cursor;

    public WTermPromptLine(String command, boolean cursor) {
        this.command = command == null ? "" : command;
        this.cursor = cursor;
    }

    public String command() {
        return command;
    }

    public boolean cursor() {
        return cursor;
    }

    @Override
    protected void onCalculateSize() {
        TerminalGrid g = TerminalGrid.of(theme());
        int cols = theme().terminalPromptText().length() + 1 + command.length() + (cursor ? 2 : 0) + 2;
        width = cols * g.cw;
        height = g.lh;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (style() instanceof TerminalClickStyle style) style.paintPrompt(this);
    }
}
