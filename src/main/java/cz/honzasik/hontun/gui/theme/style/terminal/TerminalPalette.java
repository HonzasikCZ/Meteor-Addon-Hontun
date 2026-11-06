package cz.honzasik.hontun.gui.theme.style.terminal;

import cz.honzasik.hontun.gui.theme.HontunLightPalette;
import cz.honzasik.hontun.utils.HontunTheme;

public final class TerminalPalette {
    public static final int INK_D = 0x0A0B0C;
    public static final int INK_L = 0xF6F7F8;

    public static int bg;
    public static int bar;
    public static int acc;
    public static int fill;
    public static int ink;
    public static int fg;
    public static int dim;
    public static int faint;
    public static int line;
    public static int lineF;
    public static int empty;
    public static int head;
    public static int rainBg;
    public static int red;
    public static int green;
    public static int yellow;
    public static double opacity = 1;

    private static int keyAccent = -1;
    private static double keyOpacity = -1;
    private static double rawOpacity = 1;

    static {
        compute(HontunTheme.userAccent() & 0xFFFFFF, 1);
    }

    private TerminalPalette() {}

    public static void ensure() {
        update(HontunTheme.userAccent(), rawOpacity);
    }

    public static void update(int accent, double windowOpacity) {
        accent &= 0xFFFFFF;
        rawOpacity = windowOpacity;
        if (accent == keyAccent && windowOpacity == keyOpacity) return;
        compute(accent, windowOpacity);
    }

    private static synchronized void compute(int a, double o) {
        keyAccent = a;
        keyOpacity = o;
        opacity = Math.max(0.5, Math.min(1, o));

        bg = mix(0x0B0C0D, a, 0.035);
        bar = mix(0x121416, a, 0.07);
        acc = lift(a, bg, 5.5);

        int[] fi = fillInk(a, bg);
        fill = fi[0];
        ink = fi[1];

        fg = mix(0xD6DADE, acc, 0.05);
        dim = mix(0x848A90, acc, 0.06);
        faint = mix(0x5E646A, acc, 0.08);
        line = mix(0x26292C, acc, 0.20);
        lineF = mix(line, lift(a, bg, 3.0), 0.55);
        empty = mix(0x26292C, acc, 0.10);
        head = mix(acc, 0xFFFFFF, 0.72);
        rainBg = mix(0x030404, a, 0.02);

        red = lift(HontunTheme.red(), bg, 4.5);
        green = lift(HontunTheme.green(), bg, 4.5);
        yellow = lift(HontunTheme.yellow(), bg, 4.5);
    }

    public static int hl() {
        return argb(0x1F, acc);
    }

    public static int sel() {
        return argb(0x38, acc);
    }

    public static int fold() {
        return argb(0x12, acc);
    }

    public static int win() {
        return argb(alpha(opacity), bg);
    }

    public static int barA() {
        return argb(alpha(Math.min(1, opacity + 0.06)), bar);
    }

    public static int sbar() {
        return argb(0xFF, bar);
    }

    public static int opaque(int rgb) {
        return argb(0xFF, rgb);
    }

    public static int argb(int a, int rgb) {
        return (Math.max(0, Math.min(255, a)) << 24) | (rgb & 0xFFFFFF);
    }

    public static int alpha(double a) {
        return (int) Math.round(Math.max(0, Math.min(1, a)) * 255);
    }

    public static int mix(int a, int b, double t) {
        int r = (int) Math.round(((a >> 16) & 0xFF) + (((b >> 16) & 0xFF) - ((a >> 16) & 0xFF)) * t);
        int g = (int) Math.round(((a >> 8) & 0xFF) + (((b >> 8) & 0xFF) - ((a >> 8) & 0xFF)) * t);
        int bl = (int) Math.round((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t);
        return (clamp255(r) << 16) | (clamp255(g) << 8) | clamp255(bl);
    }

    public static double contrast(int a, int b) {
        return HontunLightPalette.contrast(a & 0xFFFFFF, b & 0xFFFFFF);
    }

    public static int lift(int c, int bg, double target) {
        if (contrast(c, bg) >= target) return c;

        double[] hsl = toHsl(c);
        for (double l = hsl[2]; l <= 1.001; l += 0.01) {
            int x = fromHsl(hsl[0], hsl[1], Math.min(1, l));
            if (contrast(x, bg) >= target) return x;
        }

        return 0xFFFFFF;
    }

    public static int[] fillInk(int c, int bg) {
        int f = lift(c, bg, 3.0);
        double cd = contrast(f, INK_D);
        double cl = contrast(f, INK_L);
        if (Math.max(cd, cl) >= 4.5) return new int[] { f, cd >= cl ? INK_D : INK_L };

        double[] hsl = toHsl(f);
        for (double d = 0.01; d < 0.6; d += 0.01) {
            int up = fromHsl(hsl[0], hsl[1], Math.min(1, hsl[2] + d));
            if (contrast(up, INK_D) >= 4.5) return new int[] { up, INK_D };

            int dn = fromHsl(hsl[0], hsl[1], Math.max(0, hsl[2] - d));
            if (contrast(dn, INK_L) >= 4.5 && contrast(dn, bg) >= 3) return new int[] { dn, INK_L };
        }

        return new int[] { f, cd >= cl ? INK_D : INK_L };
    }

    public static double[] toHsl(int c) {
        double r = ((c >> 16) & 0xFF) / 255.0;
        double g = ((c >> 8) & 0xFF) / 255.0;
        double b = (c & 0xFF) / 255.0;

        double mx = Math.max(r, Math.max(g, b));
        double mn = Math.min(r, Math.min(g, b));
        double h = 0, s = 0;
        double l = (mx + mn) / 2;

        if (mx != mn) {
            double d = mx - mn;
            s = l > 0.5 ? d / (2 - mx - mn) : d / (mx + mn);
            if (mx == r) h = (g - b) / d + (g < b ? 6 : 0);
            else if (mx == g) h = (b - r) / d + 2;
            else h = (r - g) / d + 4;
            h /= 6;
        }

        return new double[] { h, s, l };
    }

    public static int fromHsl(double h, double s, double l) {
        if (s == 0) {
            int v = clamp255((int) Math.round(l * 255));
            return (v << 16) | (v << 8) | v;
        }

        double q = l < 0.5 ? l * (1 + s) : l + s - l * s;
        double p = 2 * l - q;

        int r = clamp255((int) Math.round(hue(p, q, h + 1.0 / 3) * 255));
        int g = clamp255((int) Math.round(hue(p, q, h) * 255));
        int b = clamp255((int) Math.round(hue(p, q, h - 1.0 / 3) * 255));
        return (r << 16) | (g << 8) | b;
    }

    private static double hue(double p, double q, double t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1.0 / 6) return p + (q - p) * 6 * t;
        if (t < 1.0 / 2) return q;
        if (t < 2.0 / 3) return p + (q - p) * (2.0 / 3 - t) * 6;
        return p;
    }

    private static int clamp255(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
