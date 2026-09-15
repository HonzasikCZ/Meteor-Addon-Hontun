package cz.honzasik.hontun.gui.theme.flavors.flavor;

import cz.honzasik.hontun.gui.theme.colors.HontunColor;
import cz.honzasik.hontun.gui.theme.flavors.FlavorColorProvider;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public enum Mocha implements FlavorColorProvider {
    Rosewater(new SettingColor(245, 224, 220), HontunColor.Rosewater),
    Flamingo(new SettingColor(242, 205, 205), HontunColor.Flamingo),
    Pink(new SettingColor(245, 194, 231), HontunColor.Pink),
    Mauve(new SettingColor(203, 166, 247), HontunColor.Mauve),
    Red(new SettingColor(243, 139, 168), HontunColor.Red),
    Maroon(new SettingColor(235, 160, 172), HontunColor.Maroon),
    Peach(new SettingColor(250, 179, 135), HontunColor.Peach),
    Yellow(new SettingColor(249, 226, 175), HontunColor.Yellow),
    Green(new SettingColor(166, 227, 161), HontunColor.Green),
    Teal(new SettingColor(148, 226, 213), HontunColor.Teal),
    Sky(new SettingColor(137, 220, 235), HontunColor.Sky),
    Sapphire(new SettingColor(116, 199, 236), HontunColor.Sapphire),
    Blue(new SettingColor(137, 180, 250), HontunColor.Blue),
    Lavender(new SettingColor(180, 190, 254), HontunColor.Lavender),
    Text(new SettingColor(205, 214, 244), HontunColor.Text),
    Subtext1(new SettingColor(186, 194, 222), HontunColor.Subtext1),
    Subtext0(new SettingColor(166, 173, 200), HontunColor.Subtext0),
    Overlay2(new SettingColor(147, 153, 178), HontunColor.Overlay2),
    Overlay1(new SettingColor(127, 132, 156), HontunColor.Overlay1),
    Overlay0(new SettingColor(108, 112, 134), HontunColor.Overlay0),
    Surface2(new SettingColor(88, 91, 112), HontunColor.Surface2),
    Surface1(new SettingColor(69, 71, 90), HontunColor.Surface1),
    Surface0(new SettingColor(49, 50, 68), HontunColor.Surface0),
    Base(new SettingColor(30, 30, 46), HontunColor.Base),
    Mantle(new SettingColor(24, 24, 37), HontunColor.Mantle),
    Crust(new SettingColor(17, 17, 27), HontunColor.Crust);

    private final SettingColor color;
    private final HontunColor type;

    Mocha(SettingColor color, HontunColor type) {
        this.color = color;
        this.type = type;
    }

    public SettingColor getColor() {
        return color;
    }

    public HontunColor getType() {
        return type;
    }
}
