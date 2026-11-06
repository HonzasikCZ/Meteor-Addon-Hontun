package cz.honzasik.hontun.gui.theme.style.terminal;

import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.style.FooterKind;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.utils.Utils;

public class WTermFooter extends WWidget implements HontunWidget {
    static final String ARROWS = "↑↓";

    private final FooterKind kind;

    public WTermFooter(FooterKind kind) {
        this.kind = kind;
    }

    public FooterKind kind() {
        return kind;
    }

    public String[][] keys() {
        HontunGuiTheme t = theme();
        boolean find = t.hontunSearchScreen.get();

        return switch (kind) {
            case MODULES -> find
                    ? new String[][] { { "^F", "Search" }, { "LMB", "Toggle" }, { "RMB", "Settings" }, { "DRAG", "Move" }, { "TYPE", "Find" } }
                    : new String[][] { { "LMB", "Toggle" }, { "RMB", "Settings" }, { "DRAG", "Move" } };
            case MODULE -> new String[][] { { "ESC", "Back" }, { "LMB", "Edit" }, { "j k", "Move" }, { "SPC", "Change" }, { "h l", "Adjust" }, { "RMB", "Fold" } };
            case SEARCH -> new String[][] { { "ESC", "Close" }, { ARROWS, "Select" }, { "RET", "Toggle" }, { "TAB", "Settings" }, { "RMB", "Settings" } };
            case TAB -> find
                    ? new String[][] { { "ESC", "Modules" }, { "LMB", "Run" }, { "^F", "Search" } }
                    : new String[][] { { "ESC", "Modules" }, { "LMB", "Run" } };
        };
    }

    @Override
    protected void onCalculateSize() {
        TerminalGrid g = TerminalGrid.of(theme());
        width = Utils.getWindowWidth();
        height = g.tb;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (style() instanceof TerminalClickStyle style) style.paintFooter(this);
    }
}
