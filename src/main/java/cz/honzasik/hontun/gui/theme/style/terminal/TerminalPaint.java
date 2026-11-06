package cz.honzasik.hontun.gui.theme.style.terminal;

import cz.honzasik.hontun.gui.api.render.RoundedRect;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.render.HontunRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

public final class TerminalPaint {
    private static final Color CLEAR = new Color(0, 0, 0, 0);

    public static double fade = 1;
    public static double dx = 0;

    private TerminalPaint() {}

    public static Color color(int argb) {
        int a = (argb >>> 24) & 0xFF;
        if (fade < 1) a = (int) Math.round(a * Math.max(0, fade));
        return new Color((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, a);
    }

    public static void rect(double x, double y, double w, double h, int argb) {
        if (w <= 0 || h <= 0 || (argb >>> 24) == 0) return;
        double l = Math.round(x + dx), t = Math.round(y);
        double r = Math.round(x + dx + w), b = Math.round(y + h);
        RoundedRect.get().pos(l, t).size(Math.max(1, r - l), Math.max(1, b - t)).color(color(argb)).render();
    }

    public static void exact(double x, double y, double w, double h, int argb) {
        if (w <= 0 || h <= 0 || (argb >>> 24) == 0) return;
        RoundedRect.get().pos(x + dx, y).size(w, h).color(color(argb)).render();
    }

    public static void round(double x, double y, double w, double h, double r, int argb) {
        if (w <= 0 || h <= 0 || (argb >>> 24) == 0) return;
        RoundedRect.get().pos(Math.round(x + dx), Math.round(y)).size(Math.round(w), Math.round(h)).radius(r).color(color(argb)).render();
    }

    public static void frame(double x, double y, double w, double h, double r, int fill, int outline) {
        if (w <= 0 || h <= 0) return;
        RoundedRect rect = RoundedRect.get().pos(Math.round(x + dx), Math.round(y)).size(Math.round(w), Math.round(h)).radius(r);
        rect.color((fill >>> 24) == 0 ? CLEAR : color(fill));
        if ((outline >>> 24) != 0) rect.outline(color(outline), 1f);
        rect.render();
    }

    public static void hline(double x, double y, double w, int argb) {
        rect(x, y, w, 1, argb);
    }

    public static void vline(double x, double y, double h, int argb) {
        rect(x, y, 1, h, argb);
    }

    public static void box(double x, double y, double w, double h, int argb) {
        hline(x, y, w, argb);
        hline(x, y + h - 1, w, argb);
        vline(x, y + 1, h - 2, argb);
        vline(x + w - 1, y + 1, h - 2, argb);
    }

    public static void text(String s, double x, double y, int argb) {
        if (s == null || s.isEmpty() || (argb >>> 24) == 0) return;
        HontunRenderer.get().text(RichText.of(s), Math.round(x + dx), Math.round(y), color(argb));
    }

    public static void arrow(double x, double y, double w, double h, double u, boolean up, int argb) {
        int n = (int) Math.max(3, Math.round(3.5 * u));
        int s = (int) Math.max(1, Math.round(u));
        int total = (int) Math.max(n + 2, Math.round(9 * u));
        double left = Math.round(x + (w - (2 * n - 1)) / 2.0);
        double top = Math.round(y + (h - total) / 2.0);

        for (int r = 0; r < n; r++) {
            double ry = up ? top + r : top + total - 1 - r;
            rect(left + n - 1 - r, ry, 2 * r + 1, 1, argb);
        }

        double sx = left + n - 1 - Math.floor((s - 1) / 2.0);
        double sy = up ? top + n : top;
        rect(sx, sy, s, total - n, argb);
    }

    public static void ellipsis(double x, double y, double cw, int argb) {
        double step = cw / 3.0;
        text(".", x - step, y, argb);
        text(".", x, y, argb);
        text(".", x + step, y, argb);
    }

    public static void reset() {
        fade = 1;
        dx = 0;
    }
}
