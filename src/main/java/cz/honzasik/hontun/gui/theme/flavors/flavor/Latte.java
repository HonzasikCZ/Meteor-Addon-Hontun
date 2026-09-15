package cz.honzasik.hontun.gui.theme.flavors.flavor;

import cz.honzasik.hontun.gui.theme.colors.HontunColor;
import cz.honzasik.hontun.gui.theme.flavors.FlavorColorProvider;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public enum Latte implements FlavorColorProvider {
    Rosewater(new SettingColor(220, 138, 120), HontunColor.Rosewater),
    Flamingo(new SettingColor(221, 120, 120), HontunColor.Flamingo),
    Pink(new SettingColor(234, 118, 203), HontunColor.Pink),
    Mauve(new SettingColor(136, 57, 239), HontunColor.Mauve),
    Red(new SettingColor(210, 15, 57), HontunColor.Red),
    Maroon(new SettingColor(230, 69, 83), HontunColor.Maroon),
    Peach(new SettingColor(254, 100, 11), HontunColor.Peach),
    Yellow(new SettingColor(223, 142, 29), HontunColor.Yellow),
    Green(new SettingColor(64, 160, 43), HontunColor.Green),
    Teal(new SettingColor(23, 146, 153), HontunColor.Teal),
    Sky(new SettingColor(4, 165, 229), HontunColor.Sky),
    Sapphire(new SettingColor(32, 159, 181), HontunColor.Sapphire),
    Blue(new SettingColor(30, 102, 245), HontunColor.Blue),
    Lavender(new SettingColor(114, 135, 253), HontunColor.Lavender),
    Text(new SettingColor(76, 79, 105), HontunColor.Text),
    Subtext1(new SettingColor(92, 95, 119), HontunColor.Subtext1),
    Subtext0(new SettingColor(108, 111, 133), HontunColor.Subtext0),
    Overlay2(new SettingColor(124, 127, 147), HontunColor.Overlay2),
    Overlay1(new SettingColor(140, 143, 161), HontunColor.Overlay1),
    Overlay0(new SettingColor(156, 160, 176), HontunColor.Overlay0),
    Surface2(new SettingColor(172, 176, 190), HontunColor.Surface2),
    Surface1(new SettingColor(188, 192, 204), HontunColor.Surface1),
    Surface0(new SettingColor(204, 208, 218), HontunColor.Surface0),
    Base(new SettingColor(239, 241, 245), HontunColor.Base),
    Mantle(new SettingColor(230, 233, 239), HontunColor.Mantle),
    Crust(new SettingColor(220, 224, 232), HontunColor.Crust);

    private final SettingColor color;
    private final HontunColor type;

    Latte(SettingColor color, HontunColor type) {
        this.color = color;
        this.type = type;
    }

    @Override
    public SettingColor getColor() {
        return color;
    }

    @Override
    public HontunColor getType() {
        return type;
    }
}
