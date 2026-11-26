package com.mrmelon54.DraggableLists.theme;

public record Palette(
    int accent,
    int accentHi,
    int accentLo,
    int surface,
    int surfaceHi,
    int crust,
    int border,
    int text,
    int textDim,
    CornerStyle corners,
    int cornerSize
) {
    public static final Palette BUILTIN = new Palette(
        0x58A6FF,
        0x8CC8FF,
        0x2F6FD0,
        0x1E2026,
        0x2A2D35,
        0x0E1013,
        0x3A3F4A,
        0xECEFF4,
        0x9AA1AC,
        CornerStyle.ROUND,
        4
    );

    public static final Palette VANILLA = new Palette(
        0xFFFFFF,
        0xFFFFFF,
        0x9A9A9A,
        0x2B2B2B,
        0x3C3C3C,
        0x0A0A0A,
        0x7F7F7F,
        0xFFFFFF,
        0xA0A0A0,
        CornerStyle.SQUARE,
        0
    );

    public static int argb(int alpha, int rgb) {
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }

    public static int lerpRgb(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = (int) (ar + (br - ar) * t);
        int g = (int) (ag + (bg - ag) * t);
        int bl = (int) (ab + (bb - ab) * t);
        return (r << 16) | (g << 8) | bl;
    }
}
