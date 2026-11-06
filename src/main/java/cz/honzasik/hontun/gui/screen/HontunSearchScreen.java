package cz.honzasik.hontun.gui.screen;

import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.utils.Utils;

public class HontunSearchScreen extends WidgetScreen {
    private final HontunGuiTheme theme;
    private final String query;

    public HontunSearchScreen(GuiTheme theme) {
        this(theme, "");
    }

    public HontunSearchScreen(GuiTheme theme, String query) {
        super(theme, "Search");
        this.theme = (HontunGuiTheme) theme;
        this.query = query == null ? "" : query;
    }

    @Override
    public void initWidgets() {
        if (theme.style().searchView(theme, this, query)) return;

        double margin = Utils.getWindowHeight() / 8.0;
        add(theme.search()).marginTop(margin).top().centerX();
    }
}
