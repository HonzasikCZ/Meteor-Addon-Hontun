package cz.honzasik.hontun.gui.theme;

import cz.honzasik.hontun.utils.HontunTheme;

public final class HontunLightPalette {
    private static final double TEXT_CONTRAST = 4.5;
    private static final float ROW_TINT = 49 / 255f;

    private static final HontunTheme.Ramp MINECRAFT_RAMP = new HontunTheme.Ramp(0xDCDCDF, 0xE8E8EA, 0xF3F3F4, 0xE2E2E5, 0xD5D5D9, 0xC7C7CC,
            0xB0B0B6, 0x96969D, 0x7C7C84, 0x1C1C20, 0x3A3A40, 0x606067);
    private static final HontunTheme.Ramp MODERN_RAMP = new HontunTheme.Ramp(0xE1E4E9, 0xECEEF2, 0xF6F7F9, 0xE6E8EC, 0xDADDE2, 0xCDD1D7,
            0xB6BAC2, 0x9BA0A9, 0x80858F, 0x15171B, 0x33363D, 0x5B5F68);
    private static final HontunTheme.Ramp SMOG_RAMP = new HontunTheme.Ramp(0xEBEBED, 0xF5F5F6, 0xFFFFFF, 0xEEEEF0, 0xE3E3E6, 0xD7D7DB,
            0xC2C2C7, 0xA6A6AD, 0x8A8A92, 0x0A0A0C, 0x2C2C30, 0x5A5A60);

    public static final int SMOG_ACCENT = 0x26272B;
    public static final int SMOG_ACCENT_HI = 0x0E0E10;

    private HontunLightPalette() {}

    public static HontunTheme.Ramp ramp(HontunTheme.UiMode m) {
        if (m == null) return MODERN_RAMP;
        return switch (m) {
            case SmogClient -> SMOG_RAMP;
            case HModern1, HModern2 -> MODERN_RAMP;
            default -> MINECRAFT_RAMP;
        };
    }

    public static int green()  { return 0x1E8E3E; }
    public static int yellow() { return 0x9A6700; }
    public static int red()    { return 0xD20F39; }

    public static int body(HontunTheme.Ramp dark, HontunTheme.Ramp light, double windowOpacity) {
        return HontunTheme.lerp(dark.base(), light.mantle(), (float) Math.clamp(windowOpacity, 0.0, 1.0));
    }

    public static int accentFor(int rawAccent, int bodyRgb) {
        return readableOnTint(rawAccent, bodyRgb);
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
