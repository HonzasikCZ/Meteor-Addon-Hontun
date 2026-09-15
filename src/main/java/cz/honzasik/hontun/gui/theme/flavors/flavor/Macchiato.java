package cz.honzasik.hontun.gui.theme.flavors.flavor;

import cz.honzasik.hontun.gui.theme.colors.HontunColor;
import cz.honzasik.hontun.gui.theme.flavors.FlavorColorProvider;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public enum Macchiato implements FlavorColorProvider {
    Rosewater(new SettingColor(244, 219, 214), HontunColor.Rosewater),
    Flamingo(new SettingColor(240, 198, 198), HontunColor.Flamingo),
    Pink(new SettingColor(245, 189, 230), HontunColor.Pink),
    Mauve(new SettingColor(198, 160, 246), HontunColor.Mauve),
    Red(new SettingColor(237, 135, 150), HontunColor.Red),
    Maroon(new SettingColor(238, 153, 160), HontunColor.Maroon),
    Peach(new SettingColor(245, 169, 127), HontunColor.Peach),
    Yellow(new SettingColor(238, 212, 159), HontunColor.Yellow),
    Green(new SettingColor(166, 218, 149), HontunColor.Green),
    Teal(new SettingColor(139, 213, 202), HontunColor.Teal),
    Sky(new SettingColor(145, 215, 227), HontunColor.Sky),
    Sapphire(new SettingColor(125, 196, 228), HontunColor.Sapphire),
    Blue(new SettingColor(138, 173, 244), HontunColor.Blue),
    Lavender(new SettingColor(183, 189, 248), HontunColor.Lavender),
    Text(new SettingColor(202, 211, 245), HontunColor.Text),
    Subtext1(new SettingColor(184, 192, 224), HontunColor.Subtext1),
    Subtext0(new SettingColor(165, 173, 203), HontunColor.Subtext0),
    Overlay2(new SettingColor(147, 154, 183), HontunColor.Overlay2),
    Overlay1(new SettingColor(128, 135, 162), HontunColor.Overlay1),
    Overlay0(new SettingColor(110, 115, 141), HontunColor.Overlay0),
    Surface2(new SettingColor(91, 96, 120), HontunColor.Surface2),
    Surface1(new SettingColor(73, 77, 100), HontunColor.Surface1),
    Surface0(new SettingColor(54, 58, 79), HontunColor.Surface0),
    Base(new SettingColor(36, 39, 58), HontunColor.Base),
    Mantle(new SettingColor(30, 32, 48), HontunColor.Mantle),
    Crust(new SettingColor(24, 25, 38), HontunColor.Crust);

    private final SettingColor color;
    private final HontunColor type;

    Macchiato(SettingColor color, HontunColor type) {
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
