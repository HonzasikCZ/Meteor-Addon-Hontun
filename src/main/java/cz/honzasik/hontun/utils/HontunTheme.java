package cz.honzasik.hontun.utils;

import meteordevelopment.meteorclient.utils.render.color.Color;

import java.util.concurrent.CopyOnWriteArrayList;

public final class HontunTheme {
    public enum UiMode { Vanilla, HVanilla, HModern1, HModern2, SmogClient }

    public record Ramp(int crust, int mantle, int base, int surface0, int surface1, int surface2,
                       int overlay0, int overlay1, int overlay2, int text, int subtext1, int textDim) {}

    private static final Ramp MINECRAFT_RAMP = new Ramp(0x0C0D0F, 0x101114, 0x141519, 0x1C1D22, 0x26272D, 0x32343A,
            0x44464E, 0x5A5C66, 0x74767F, 0xE6E8EC, 0xC2C5CC, 0xA0A3AB);
    private static final Ramp MODERN_RAMP = new Ramp(0x08090B, 0x0C0D10, 0x101114, 0x16181C, 0x1E2024, 0x282A30,
            0x3A3C42, 0x4E5058, 0x646670, 0xF0F2F5, 0xCACDD4, 0x9CA0A8);
    private static final Ramp SMOG_RAMP = new Ramp(0x000000, 0x050506, 0x0A0A0C, 0x0E0E10, 0x151517, 0x1E1E20,
            0x2A2A2C, 0x3C3C3E, 0x555558, 0xFFFFFF, 0xC8CDD2, 0x9096A0);

    private static volatile UiMode mode = UiMode.HModern2;

    public static final int SMOG_ACCENT    = 0xE6E9EC;
    public static final int SMOG_ACCENT_HI = 0xFFFFFF;
    public static final int SMOG_ACCENT_LO = 0x8A9196;
    private static final int SMOG_CHAT_NAME    = 0x64676C;
    private static final int SMOG_CHAT_VALUE   = 0x8C9095;
    private static final int SMOG_CHAT_BRACKET = 0x3F4145;
    private static final int SMOG_CHAT_PUNCT   = 0x5A5D62;
    private static final int SMOG_CHAT_TAG     = 0xA3A6AB;

    private static volatile int accent   = 0x3E8CFF;
    private static volatile int accentHi = 0x5AC8FF;
    private static volatile int accentLo = 0x2E63B8;

    private static volatile int bgOverlay = (235 << 24) | 0xA8ABB2;

    private static volatile int menuColor = 0x141519;

    private static volatile int version = 0;
    private static final CopyOnWriteArrayList<Runnable> listeners = new CopyOnWriteArrayList<>();

    private HontunTheme() {}

    public static UiMode mode()            { return mode; }
    public static boolean vanilla()        { return mode == UiMode.Vanilla; }

    public static boolean modern()         { return mode == UiMode.HModern1 || mode == UiMode.HModern2 || mode == UiMode.SmogClient; }
    public static boolean hvanilla()       { return mode == UiMode.HVanilla; }
    public static boolean modern1()        { return mode == UiMode.HModern1; }
    public static boolean modern2()        { return mode == UiMode.HModern2; }
    public static boolean smog()           { return mode == UiMode.SmogClient; }
    public static boolean restyleEnabled() { return mode != UiMode.Vanilla; }

    public static void setMode(UiMode m) {
        if (m != null && m != mode) { mode = m; bump(); }
    }

    public static int accent()   { return smog() ? SMOG_ACCENT   : accent; }
    public static int accentHi() { return smog() ? SMOG_ACCENT_HI : accentHi; }
    public static int accentLo() { return smog() ? SMOG_ACCENT_LO : accentLo; }

    public static int userAccent()   { return accent; }
    public static int userAccentHi() { return accentHi; }
    public static int userAccentLo() { return accentLo; }

    public static int chatName()    { return smog() ? SMOG_CHAT_NAME    : accent(); }
    public static int chatValue()   { return smog() ? SMOG_CHAT_VALUE   : accentHi(); }
    public static int chatBracket() { return smog() ? SMOG_CHAT_BRACKET : overlay0(); }
    public static int chatPunct()   { return smog() ? SMOG_CHAT_PUNCT   : textDim(); }
    public static int chatTag()     { return smog() ? SMOG_CHAT_TAG     : subtext1(); }

    public static void setAccent(int rgb) {
        rgb &= 0xFFFFFF;
        if (rgb == accent) return;
        accent = rgb;
        recomputeAccent();
        bump();
    }

    private static void recomputeAccent() {
        float[] hsb = java.awt.Color.RGBtoHSB((accent >> 16) & 0xFF, (accent >> 8) & 0xFF, accent & 0xFF, null);

        accentHi = java.awt.Color.HSBtoRGB(hsb[0], clamp(hsb[1] - 0.14f), clamp(hsb[2] + 0.26f)) & 0xFFFFFF;
        accentLo = java.awt.Color.HSBtoRGB(hsb[0], clamp(hsb[1] + 0.06f), clamp(hsb[2] * 0.60f)) & 0xFFFFFF;
    }

    private static float clamp(float v) { return v < 0f ? 0f : (v > 1f ? 1f : v); }

    public static int bgOverlay() { return bgOverlay; }

    public static void setBgOverlay(int argb) {
        if (argb == bgOverlay) return;
        bgOverlay = argb;
        bump();
    }

    public static int menuColor() { return menuColor; }

    public static void setMenuColor(int rgb) {
        rgb &= 0xFFFFFF;
        if (rgb == menuColor) return;
        menuColor = rgb;
        bump();
    }

    public static Ramp ramp(UiMode m) {
        if (m == null) return MODERN_RAMP;
        return switch (m) {
            case SmogClient -> SMOG_RAMP;
            case HModern1, HModern2 -> MODERN_RAMP;
            default -> MINECRAFT_RAMP;
        };
    }

    private static Ramp ramp() { return ramp(mode); }

    public static int crust()    { return ramp().crust(); }
    public static int mantle()   { return ramp().mantle(); }
    public static int base()     { return ramp().base(); }
    public static int surface0() { return ramp().surface0(); }
    public static int surface1() { return ramp().surface1(); }
    public static int surface2() { return ramp().surface2(); }
    public static int overlay0() { return ramp().overlay0(); }
    public static int overlay1() { return ramp().overlay1(); }
    public static int overlay2() { return ramp().overlay2(); }
    public static int textLight(){ return ramp().text(); }
    public static int subtext1() { return ramp().subtext1(); }
    public static int textDim()  { return ramp().textDim(); }

    public static int green()  { return 0x5EE0A0; }
    public static int yellow() { return 0xF5D28A; }
    public static int red()    { return 0xFF6B7A; }

    public static int argb(int a, int rgb)        { return (a << 24) | (rgb & 0xFFFFFF); }
    public static int withAlpha(int argb, int a)  { return (a << 24) | (argb & 0xFFFFFF); }
    public static int lighten(int rgb, float f)   { return scale(rgb, f); }
    public static int darken(int rgb, float f)    { return scale(rgb, f); }

    private static int scale(int rgb, float f) {
        int r = Math.min(255, (int) (((rgb >> 16) & 0xFF) * f));
        int gg = Math.min(255, (int) (((rgb >> 8) & 0xFF) * f));
        int b = Math.min(255, (int) ((rgb & 0xFF) * f));
        return (r << 16) | (gg << 8) | b;
    }

    public static int lerp(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = (int) (ar + (br - ar) * t);
        int gg = (int) (ag + (bg - ag) * t);
        int bl = (int) (ab + (bb - ab) * t);
        return (r << 16) | (gg << 8) | bl;
    }

    public static Color color(int rgb) {
        return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }

    public static Color color(int rgb, int a) {
        return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, a);
    }

    public static int version() { return version; }

    public static void addListener(Runnable r) { listeners.add(r); }

    private static void bump() {
        version++;
        for (Runnable r : listeners) {
            try { r.run(); } catch (Throwable ignored) {}
        }
    }
}
