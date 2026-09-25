package cz.honzasik.hontun.gui.theme;

import cz.honzasik.hontun.utils.HontunTheme;

public final class HontunLightPalette {
    private static final double TEXT_CONTRAST = 4.5;
    private static final float ROW_TINT = 49 / 255f;

    private HontunLightPalette() {}

    private static int p(int minecraft, int modern, int smog) {
        if (HontunTheme.smog()) return smog;
        return HontunTheme.modern() ? modern : minecraft;
    }

    public static int crust()    { return p(0xDCDCDF, 0xE1E4E9, 0xEBEBED); }
    public static int mantle()   { return p(0xE8E8EA, 0xECEEF2, 0xF5F5F6); }
    public static int base()     { return p(0xF3F3F4, 0xF6F7F9, 0xFFFFFF); }
    public static int surface0() { return p(0xE2E2E5, 0xE6E8EC, 0xEEEEF0); }
    public static int surface1() { return p(0xD5D5D9, 0xDADDE2, 0xE3E3E6); }
    public static int surface2() { return p(0xC7C7CC, 0xCDD1D7, 0xD7D7DB); }
    public static int overlay0() { return p(0xB0B0B6, 0xB6BAC2, 0xC2C2C7); }
    public static int overlay1() { return p(0x96969D, 0x9BA0A9, 0xA6A6AD); }
    public static int overlay2() { return p(0x7C7C84, 0x80858F, 0x8A8A92); }
    public static int text()     { return p(0x1C1C20, 0x15171B, 0x0A0A0C); }
    public static int subtext1() { return p(0x3A3A40, 0x33363D, 0x2C2C30); }
    public static int textDim()  { return p(0x606067, 0x5B5F68, 0x5A5A60); }

    public static int green()  { return 0x1E8E3E; }
    public static int yellow() { return 0x9A6700; }
    public static int red()    { return 0xD20F39; }

    public static int accent(double windowOpacity) {
        if (HontunTheme.smog()) return 0x26272B;
        return readableOnTint(HontunTheme.accent(), body(windowOpacity));
    }

    public static int accentHi(double windowOpacity) {
        if (HontunTheme.smog()) return 0x0E0E10;
        return readableOnTint(HontunTheme.accentHi(), body(windowOpacity));
    }

    private static int body(double windowOpacity) {
        return HontunTheme.lerp(HontunTheme.base(), mantle(), (float) Math.clamp(windowOpacity, 0.0, 1.0));
    }

    private static int readableOnTint(int rgb, int body) {
        int c = rgb & 0xFFFFFF;
        for (int i = 0; i < 48; i++) {
            if (contrast(c, HontunTheme.lerp(body, c, ROW_TINT)) >= TEXT_CONTRAST) break;
            c = HontunTheme.darken(c, 0.95f);
        }
        return c;
    }

    public static double contrast(int a, int b) {
        double la = luminance(a), lb = luminance(b);
        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }

    public static double luminance(int rgb) {
        return 0.2126 * channel((rgb >> 16) & 0xFF) + 0.7152 * channel((rgb >> 8) & 0xFF) + 0.0722 * channel(rgb & 0xFF);
    }

    private static double channel(int v) {
        double c = v / 255.0;
        return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }
}
