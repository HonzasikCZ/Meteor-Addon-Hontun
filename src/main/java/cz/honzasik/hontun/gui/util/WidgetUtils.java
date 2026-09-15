package cz.honzasik.hontun.gui.util;

import cz.honzasik.hontun.gui.theme.icons.HontunBuiltinIcons;
import cz.honzasik.hontun.gui.theme.widgets.pressable.WHontunButton;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.settings.Setting;

import java.util.function.BooleanSupplier;

public class WidgetUtils {
    public static Cell<WHontunButton> reset(WContainer c, Setting<?> setting, Runnable action) {
        return reset(c, setting, action, null);
    }

    public static Cell<WHontunButton> reset(WContainer c, Setting<?> setting, Runnable action, BooleanSupplier visibilityCondition) {
        WHontunButton button = (WHontunButton) c.getTheme().button(HontunBuiltinIcons.RESET.texture());

        button.setVisibilityCondition(visibilityCondition);
        button.tooltip = "Reset";
        button.action = () -> {
            if (setting != null) setting.reset();
            if (action != null) action.run();
        };

        return c.add(button);
    }
}
