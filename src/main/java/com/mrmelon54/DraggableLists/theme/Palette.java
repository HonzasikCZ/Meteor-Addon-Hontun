package com.mrmelon54.DraggableLists.theme;

/**
 * Every colour the drag visuals need, as 0xRRGGBB (no alpha - alpha is chosen per element
 * by {@link com.mrmelon54.DraggableLists.render.DragSkin}).
 *
 * @param accent      primary accent, used for the drop indicator and the lifted card's border
 * @param accentHi    brighter accent, used for highlights and the indicator core
 * @param accentLo    darker accent, used for glow falloff and gradients
 * @param surface     resting row / card fill
 * @param surfaceHi   raised card fill (the row while it is being carried)
 * @param crust       deepest background, used for the gap left behind and for shadows
 * @param border      neutral border
 * @param text        primary text
 * @param textDim     secondary text
 * @param corners     corner treatment
 * @param cornerSize  corner radius (ROUND) or cut length (CHAMFER), in GUI pixels
 */
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
    /**
     * The look used when Hontun is not driving the theme: a neutral dark card with a cool
     * blue accent, tuned to sit on top of vanilla's list background in both the menu and
     * the in-world variants.
     */
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

    /**
     * The look used when Hontun is installed but the user has put it back into its plain
     * "Vanilla" UI mode: neutral greys, a white accent and sharp square corners, so the drag
     * matches Minecraft's own list instead of carrying the built-in accent that would make
     * Vanilla mode look like one of the styled Hontun modes.
     */
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
