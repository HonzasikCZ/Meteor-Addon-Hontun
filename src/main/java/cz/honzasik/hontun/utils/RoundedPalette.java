package cz.honzasik.hontun.utils;

public record RoundedPalette(int glow, int edge, int veil, int orb, int link, int halo, int far, int mid, int near) {
    public static final RoundedPalette SMOG = new RoundedPalette(0x0F0F11, 0x030304, 0x060607,
            0xFFFFFF, 0xC8CDD2, 0xFFFFFF, 0x7C8188, 0xB4B9BF, 0xF4F6F8);

    public static RoundedPalette of(int menu, int accent, int accentHi, int accentLo) {
        return new RoundedPalette(menu, HontunTheme.darken(menu, 0.2f), HontunTheme.darken(menu, 0.3f),
                accent, accentHi, accentHi, accentLo, accent, accentHi);
    }

    public int dot(int layer) {
        return layer == 0 ? far : (layer == 1 ? mid : near);
    }
}
