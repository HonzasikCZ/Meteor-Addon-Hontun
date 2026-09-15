package cz.honzasik.hontun.gui.theme.flavors;

import cz.honzasik.hontun.gui.HontunGui;
import cz.honzasik.hontun.gui.theme.colors.HontunColor;
import cz.honzasik.hontun.gui.theme.flavors.flavor.Frappe;
import cz.honzasik.hontun.gui.theme.flavors.flavor.Latte;
import cz.honzasik.hontun.gui.theme.flavors.flavor.Macchiato;
import cz.honzasik.hontun.gui.theme.flavors.flavor.Mocha;
import cz.honzasik.hontun.gui.theme.flavors.flavor.*;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public enum HontunFlavors {
    Latte(Latte.class),
    Frappe(Frappe.class),
    Macchiato(Macchiato.class),
    Mocha(Mocha.class);

    private final Class<? extends FlavorColorProvider> colorProviderClass;

    HontunFlavors(Class<? extends FlavorColorProvider> providerClass) {
        this.colorProviderClass = providerClass;
    }

    public SettingColor getColor(HontunColor color) {
        try {
            for (FlavorColorProvider provider : colorProviderClass.getEnumConstants()) {
                if (provider.getType() == color) {
                    return provider.getColor();
                }
            }
            return new SettingColor(255, 255, 255);
        } catch (Exception e) {
            HontunGui.LOG.error(e.getMessage());
            return new SettingColor(255, 255, 255);
        }
    }
}
