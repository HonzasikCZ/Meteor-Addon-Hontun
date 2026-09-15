package cz.honzasik.hontun.gui.theme.flavors;

import cz.honzasik.hontun.gui.theme.colors.HontunColor;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public interface FlavorColorProvider {
    HontunColor getType();
    SettingColor getColor();
}
