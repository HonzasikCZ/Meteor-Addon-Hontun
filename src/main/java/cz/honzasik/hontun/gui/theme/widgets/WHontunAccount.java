package cz.honzasik.hontun.gui.theme.widgets;

import cz.honzasik.hontun.gui.theme.HontunWidget;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.widgets.WAccount;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WHontunAccount extends WAccount implements HontunWidget {
    public WHontunAccount(WidgetScreen screen, Account<?> account) {
        super(screen, account);
    }

    @Override
    protected Color loggedInColor() {
        return theme().greenColor();
    }

    @Override
    protected Color accountTypeColor() {
        return theme().textSecondaryColor();
    }
}
