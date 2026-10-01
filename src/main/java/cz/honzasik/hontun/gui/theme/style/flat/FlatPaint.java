package cz.honzasik.hontun.gui.theme.style.flat;

import cz.honzasik.hontun.gui.api.render.RoundedRect;
import cz.honzasik.hontun.gui.util.ColorUtils;
import cz.honzasik.hontun.utils.HontunTheme;
import meteordevelopment.meteorclient.utils.render.color.Color;

final class FlatPaint {
    static final Color CLEAR = new Color(0, 0, 0, 0);

    private FlatPaint() {}

    static double rd(double v) {
        return Math.round(v);
    }

    static RoundedRect edges(double x0, double y0, double x1, double y1) {
        double l = Math.round(x0);
        double t = Math.round(y0);
        double r = Math.round(x1);
        double b = Math.round(y1);
        return RoundedRect.get().pos(l, t).size(Math.max(0, r - l), Math.max(0, b - t));
    }

    static RoundedRect box(double x, double y, double w, double h) {
        return edges(x, y, x + w, y + h);
    }

    static int alpha(double a) {
        return (int) Math.max(0, Math.min(255, Math.round(a)));
    }

    static Color col(int rgb, double a) {
        return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, alpha(a));
    }

    static double lerp(double from, double to, double t) {
        return from + (to - from) * t;
    }

    static int mix(int from, int to, double t) {
        return HontunTheme.lerp(from, to, (float) Math.clamp(t, 0.0, 1.0));
    }

    static Color mix(Color from, Color to, double t) {
        return ColorUtils.interpolateColor(from, to, t);
    }

    static double clamp01(double v) {
        return Math.clamp(v, 0.0, 1.0);
    }

    static void set(Color target, int rgb) {
        target.set((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, 255);
    }
}
