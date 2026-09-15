package cz.honzasik.hontun.gui.screen;

import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.utils.Utils;

public class HontunSearchScreen extends WidgetScreen {
    private final HontunGuiTheme theme;

    public HontunSearchScreen(GuiTheme theme) {
        super(theme, "Search");
        this.theme = (HontunGuiTheme) theme;
    }

    @Override
    public void initWidgets() {
        double margin = Utils.getWindowHeight() / 8.0;
        add(theme.search()).marginTop(margin).top().centerX();
    }
}
