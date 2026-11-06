package cz.honzasik.hontun.gui.theme.style.terminal;

import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;

public class TerminalSettingScreen extends WindowScreen {
    private final WWidget content;

    public TerminalSettingScreen(HontunGuiTheme theme, String title, WWidget content) {
        super(theme, title);
        this.content = content;
    }

    @Override
    public void initWidgets() {
        if (content != null) add(content).expandX();
    }
}
