package cz.honzasik.hontun.gui.theme;

import cz.honzasik.hontun.gui.theme.style.StylePalette;
import cz.honzasik.hontun.utils.HontunTheme;

import java.util.function.IntPredicate;
import java.util.function.ToIntFunction;

public final class HontunLightPalette {
    public static final double TEXT_CONTRAST = 4.5;
    public static final double LINE_CONTRAST = 3.0;
    public static final double FAINT_SEPARATION = 0.1;
    public static final float ROW_TINT = 49 / 255f;
    public static final double PRESENCE = 0.12;
    public static final double PRESENCE_CHROMA = 0.35;
    public static final double SHIFT_STEP = 0.005;
    public static final int LIGHT_SHIFT_STEPS = 24;
    public static final int DARK_SHIFT_STEPS = 28;
    public static final int SHIFT_FADE_STEPS = 12;
    public static final double TONE_RATIO = 0.3;
    public static final double TONE_MAX = 0.03;

    private static final double TOE_K1 = 0.206;
    private static final double TOE_K2 = 0.03;
    private static final double TOE_K3 = (1 + TOE_K1) / (1 + TOE_K2);

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
            case Modern1, Modern2 -> MODERN_RAMP;
            default -> MINECRAFT_RAMP;
        };
    }

    public static int green()  { return 0x1E8E3E; }
    public static int yellow() { return 0x9A6700; }
    public static int red()    { return 0xD20F39; }

    public static int readableOn(int rgb, int bg, double min, boolean up) {
        int c = rgb & 0xFFFFFF, b = bg & 0xFFFFFF;
        if (contrast(c, b) >= min) return c;
        return shade(c, m -> contrast(m, b) >= min, up);
    }

    public static int readableOn(int rgb, int bg, double min) {
        return readableOn(rgb, bg, min, false);
    }

    public static int text(int rgb, int bg, boolean light) {
        return readableOn(rgb, bg, TEXT_CONTRAST, !light);
    }

    public static int line(int rgb, int bg, boolean light) {
        return readableOn(rgb, bg, LINE_CONTRAST, !light);
    }

    public static double separation(int a, int b) {
        double[] p = oklab(a & 0xFFFFFF), q = oklab(b & 0xFFFFFF);
        double dl = p[0] - q[0], da = p[1] - q[1], db = p[2] - q[2];
        return Math.sqrt(dl * dl + da * da + db * db);
    }

    public static boolean faint(int rgb, int... surfaces) {
        for (int s : surfaces) {
            if (separation(rgb, s) < FAINT_SEPARATION) return true;
        }
        return false;
    }

    public static double toe(double l) {
        double k = TOE_K3 * l - TOE_K1;
        return 0.5 * (k + Math.sqrt(k * k + 4 * TOE_K2 * TOE_K3 * l));
    }

    public static double presence(int a, int b) {
        double[] p = oklab(a & 0xFFFFFF), q = oklab(b & 0xFFFFFF);
        double dl = toe(p[0]) - toe(q[0]);
        double da = PRESENCE_CHROMA * (p[1] - q[1]), db = PRESENCE_CHROMA * (p[2] - q[2]);
        return Math.sqrt(dl * dl + da * da + db * db);
    }

    public static boolean visible(int accent, int body, StylePalette p) {
        return presence(accent, body) >= PRESENCE
                && !faint(accent, body, p.crust, p.mantle, p.base, p.surface0, p.surface1, p.surface2);
    }

    public static int shiftSurface(int rgb, double dl, double da, double db) {
        double[] lab = oklab(rgb & 0xFFFFFF);
        double l = Math.max(0, Math.min(1, lab[0] + dl)), a = lab[1] + da, b = lab[2] + db;
        return fromLch(l, Math.sqrt(a * a + b * b), Math.atan2(b, a));
    }

    public static double toneHue(int accent, int crust) {
        double[] lab = oklab(accent & 0xFFFFFF);
        if (Math.sqrt(lab[1] * lab[1] + lab[2] * lab[2]) >= 0.03) return Math.atan2(-lab[2], -lab[1]);
        double[] c = oklab(crust & 0xFFFFFF);
        return Math.atan2(c[2], c[1]);
    }

    public static void shiftSurfaces(StylePalette p, boolean light, ToIntFunction<StylePalette> body) {
        p.shift = 0;
        p.tone = 0;
        int body0 = body.applyAsInt(p);
        if (visible(p.accent, body0, p)) return;
        double before = presence(p.accent, body0);
        int[] orig = {p.crust, p.mantle, p.base, p.surface0, p.surface1, p.surface2, p.overlay0, p.overlay1, p.overlay2};
        int cap = light ? LIGHT_SHIFT_STEPS : DARK_SHIFT_STEPS;
        double dir = light ? -1 : 1;
        double hue = toneHue(p.accent, p.crust);
        int need = 0;
        double first = before;
        for (int i = 1; i <= cap + SHIFT_FADE_STEPS; i++) {
            applyShift(p, orig, light, dir, i, hue);
            int b = body.applyAsInt(p);
            if (i == 1) first = presence(p.accent, b);
            if (visible(p.accent, b, p)) {
                need = i;
                break;
            }
        }
        int steps = need == 0 || (need > cap && first <= before) ? 0
                : need <= cap ? need : cap * (SHIFT_FADE_STEPS - (need - cap)) / SHIFT_FADE_STEPS;
        applyShift(p, orig, light, dir, steps, hue);
        if (steps != 0 && need > cap && presence(p.accent, body.applyAsInt(p)) <= before) applyShift(p, orig, light, dir, 0, hue);
    }

    public static int holdContrast(int rgb, int body0, int bg0, int body, int bg, boolean light) {
        int c = readableOn(rgb, body, Math.min(contrast(rgb, body0), TEXT_CONTRAST), !light);
        return readableOn(c, bg, Math.min(contrast(rgb, bg0), TEXT_CONTRAST), !light);
    }

    private static void applyShift(StylePalette p, int[] orig, boolean light, double dir, int steps, double hue) {
        int[] s = orig;
        double shift = 0, tone = 0;
        if (steps != 0) {
            double m = steps * SHIFT_STEP;
            tone = light ? Math.min(TONE_MAX, TONE_RATIO * m) : 0;
            double da = tone * Math.cos(hue), db = tone * Math.sin(hue);
            s = new int[orig.length];
            for (int k = 0; k < orig.length; k++) s[k] = shiftSurface(orig[k], dir * m, da, db);
            shift = dir * m;
        }
        p.setSurfaces(s);
        p.shift = shift;
        p.tone = tone;
    }

    public static int ink(int fill, int dark, int light) {
        return contrast(dark, fill) >= contrast(light, fill) ? dark & 0xFFFFFF : light & 0xFFFFFF;
    }

    private static double worstContrast(int c, int a, int b) {
        return Math.min(contrast(c, a), contrast(c, b));
    }

    public static int readableInk(int a, int b, int dark, int light) {
        int c = worstContrast(dark, a, b) >= worstContrast(light, a, b) ? dark & 0xFFFFFF : light & 0xFFFFFF;
        if (worstContrast(c, a, b) >= TEXT_CONTRAST) return c;
        int k = worstContrast(0x000000, a, b) >= worstContrast(0xFFFFFF, a, b) ? 0x000000 : 0xFFFFFF;
        return worstContrast(k, a, b) > worstContrast(c, a, b) ? k : c;
    }

    public static int darkest(int a, int b) {
        return luminance(a) <= luminance(b) ? a & 0xFFFFFF : b & 0xFFFFFF;
    }

    public static int luminanceBand(int rgb, double min, double max) {
        int c = rgb & 0xFFFFFF;
        double y = luminance(c);
        if (y > max) return shade(c, m -> luminance(m) <= max, false);
        if (y < min) return shade(c, m -> luminance(m) >= min, true);
        return c;
    }

    private static int shade(int c, IntPredicate ok, boolean up) {
        double[] lab = oklab(c);
        double chroma = Math.hypot(lab[1], lab[2]), hue = Math.atan2(lab[2], lab[1]);
        double lo = up ? lab[0] : 0, hi = up ? 1 : lab[0];
        int best = fromLch(up ? 1 : 0, chroma, hue);
        for (int i = 0; i < 24; i++) {
            double mid = (lo + hi) / 2;
            int m = fromLch(mid, chroma, hue);
            if (ok.test(m)) {
                best = m;
                if (up) hi = mid;
                else lo = mid;
            } else if (up) {
                lo = mid;
            } else {
                hi = mid;
            }
        }
        return best;
    }

    private static double[] oklab(int rgb) {
        double r = linear((rgb >> 16) & 0xFF), g = linear((rgb >> 8) & 0xFF), b = linear(rgb & 0xFF);
        double l = Math.cbrt(0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b);
        double m = Math.cbrt(0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b);
        double s = Math.cbrt(0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b);
        return new double[] {
                0.2104542553 * l + 0.7936177850 * m - 0.0040720468 * s,
                1.9779984951 * l - 2.4285922050 * m + 0.4505937099 * s,
                0.0259040371 * l + 0.7827717662 * m - 0.8086757660 * s};
    }

    private static double[] linearFromLab(double lightness, double a, double b) {
        double l = cube(lightness + 0.3963377774 * a + 0.2158037573 * b);
        double m = cube(lightness - 0.1055613458 * a - 0.0638541728 * b);
        double s = cube(lightness - 0.0894841775 * a - 1.2914855480 * b);
        return new double[] {
                4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s,
                -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s,
                -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s};
    }

    private static int fromLch(double lightness, double chroma, double hue) {
        double cos = Math.cos(hue), sin = Math.sin(hue);
        double[] v = linearFromLab(lightness, chroma * cos, chroma * sin);
        if (!inGamut(v)) {
            double lo = 0, hi = chroma;
            for (int i = 0; i < 20; i++) {
                double mid = (lo + hi) / 2;
                if (inGamut(linearFromLab(lightness, mid * cos, mid * sin))) lo = mid;
                else hi = mid;
            }
            v = linearFromLab(lightness, lo * cos, lo * sin);
        }
        return (encode(v[0]) << 16) | (encode(v[1]) << 8) | encode(v[2]);
    }

    private static boolean inGamut(double[] v) {
        for (double x : v) {
            if (x < -1e-4 || x > 1 + 1e-4) return false;
        }
        return true;
    }

    private static double cube(double x) {
        return x * x * x;
    }

    private static double linear(int v) {
        double c = v / 255.0;
        return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    private static int encode(double x) {
        double c = Math.max(0, Math.min(1, x));
        double s = c <= 0.0031308 ? 12.92 * c : 1.055 * Math.pow(c, 1 / 2.4) - 0.055;
        return (int) Math.max(0, Math.min(255, Math.round(s * 255)));
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
