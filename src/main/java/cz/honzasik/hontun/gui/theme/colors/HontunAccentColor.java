package cz.honzasik.hontun.gui.theme.colors;

public enum HontunAccentColor {
    Rosewater,
    Flamingo,
    Pink,
    Mauve,
    Maroon,
    Peach,
    Teal,
    Sky,
    Sapphire,
    Lavender,
    Red,
    Blue,
    Yellow,
    Green;

    public HontunColor toColor() {
        return HontunColor.valueOf(name());
    }
}
