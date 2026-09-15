package cz.honzasik.hontun.gui.theme.flavors.flavor;

import cz.honzasik.hontun.gui.theme.colors.HontunColor;
import cz.honzasik.hontun.gui.theme.flavors.FlavorColorProvider;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public enum Frappe implements FlavorColorProvider {
    Rosewater(new SettingColor(242, 213, 207), HontunColor.Rosewater),
    Flamingo(new SettingColor(238, 190, 190), HontunColor.Flamingo),
    Pink(new SettingColor(244, 184, 228), HontunColor.Pink),
    Mauve(new SettingColor(202, 158, 230), HontunColor.Mauve),
    Red(new SettingColor(231, 130, 132), HontunColor.Red),
    Maroon(new SettingColor(234, 153, 156), HontunColor.Maroon),
    Peach(new SettingColor(239, 159, 118), HontunColor.Peach),
    Yellow(new SettingColor(229, 200, 144), HontunColor.Yellow),
    Green(new SettingColor(166, 209, 137), HontunColor.Green),
    Teal(new SettingColor(129, 200, 190), HontunColor.Teal),
    Sky(new SettingColor(153, 209, 219), HontunColor.Sky),
    Sapphire(new SettingColor(133, 193, 220), HontunColor.Sapphire),
    Blue(new SettingColor(140, 170, 238), HontunColor.Blue),
    Lavender(new SettingColor(186, 187, 241), HontunColor.Lavender),
    Text(new SettingColor(198, 208, 245), HontunColor.Text),
    Subtext1(new SettingColor(181, 191, 226), HontunColor.Subtext1),
    Subtext0(new SettingColor(165, 173, 206), HontunColor.Subtext0),
    Overlay2(new SettingColor(148, 156, 187), HontunColor.Overlay2),
    Overlay1(new SettingColor(131, 139, 167), HontunColor.Overlay1),
    Overlay0(new SettingColor(115, 121, 148), HontunColor.Overlay0),
    Surface2(new SettingColor(98, 104, 128), HontunColor.Surface2),
    Surface1(new SettingColor(81, 87, 109), HontunColor.Surface1),
    Surface0(new SettingColor(65, 69, 89), HontunColor.Surface0),
    Base(new SettingColor(48, 52, 70), HontunColor.Base),
    Mantle(new SettingColor(41, 44, 60), HontunColor.Mantle),
    Crust(new SettingColor(35, 38, 52), HontunColor.Crust);

    private final SettingColor color;
    private final HontunColor type;

    Frappe(SettingColor color, HontunColor type) {
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
