package cz.honzasik.hontun.gui.theme.style.smog;

import cz.honzasik.hontun.gui.api.render.RoundedRect;
import meteordevelopment.meteorclient.utils.render.color.Color;

final class SmogPaint {
    static final Color CLEAR = new Color(0, 0, 0, 0);

    enum Glass {
        WINDOW(18, 6, 28, 0x48, 0x1A, 3, 4, 8, 0x66, 0x14),
        HERO(28, 14, 44, 0x50, 0x20, 6, 8, 14, 0x70, 0x18),
        POPUP(10, 6, 18, 0x60, 0x1C, 2, 2, 5, 0x60, 0x14),
        TOOLTIP(8, 4, 14, 0x58, 0x18, 0, 0, 0, 0, 0),
        DOCK(12, 4, 20, 0x40, 0x16, 2, 2, 6, 0x50, 0x12);

        final double sa, dya, fa;
        final int aa, aaLight;
        final double sk, dyk, fk;
        final int ak, akLight;

        Glass(double sa, double dya, double fa, int aa, int aaLight, double sk, double dyk, double fk, int ak, int akLight) {
            this.sa = sa;
            this.dya = dya;
            this.fa = fa;
            this.aa = aa;
            this.aaLight = aaLight;
            this.sk = sk;
            this.dyk = dyk;
            this.fk = fk;
            this.ak = ak;
            this.akLight = akLight;
        }
    }

    private SmogPaint() {}

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

    static RoundedRect exact(double x, double y, double w, double h) {
        return RoundedRect.get().pos(x, y).size(Math.max(0, w), Math.max(0, h));
    }

    static void glass(Glass level, double x0, double y0, double x1, double y1, double r,
                      double s, boolean light, boolean shadows, double ambient, double lift, double fade,
                      Color fill, Color rimTop, Color rimBottom, double hair) {
        if (x1 - x0 <= 0 || y1 - y0 <= 0) return;

        if (shadows) shadows(level, x0, y0, x1, y1, r, s, light, ambient, lift, fade);
        body(x0, y0, x1, y1, r, fill, rimTop, rimBottom, hair);
    }

    static void shadows(Glass level, double x0, double y0, double x1, double y1, double r,
                        double s, boolean light, double ambient, double lift, double fade) {
        if (x1 - x0 <= 0 || y1 - y0 <= 0 || fade <= 0) return;

        if (ambient > 0) {
            double sa = Math.min(u(s, level.sa), u(s, 28));
            double dya = u(s, level.dya);
            int a = alpha((light ? level.aaLight : level.aa) * clamp01(ambient) * fade);

            if (a > 0 && sa > 0) {
                edges(x0 - sa, y0 - sa + dya, x1 + sa, y1 + sa + dya)
                        .radius(r + sa)
                        .color(col(0x000000, a))
                        .feather((float) u(s, level.fa))
                        .dither(true)
                        .render();
            }
        }

        if (level.sk > 0) {
            double k = clamp01(lift);
            double sk = u(s, level.sk);
            double dyk = u(s, level.dyk) + Math.round(u(s, 4) * k);
            int a = alpha((light ? level.akLight + 0x0C * k : level.ak + 0x20 * k) * fade);

            if (a > 0) {
                edges(x0 - sk, y0 - sk + dyk, x1 + sk, y1 + sk + dyk)
                        .radius(r + sk)
                        .color(col(0x000000, a))
                        .feather((float) u(s, level.fk))
                        .dither(true)
                        .render();
            }
        }
    }

    static void body(double x0, double y0, double x1, double y1, double r, Color fill, Color rimTop, Color rimBottom, double hair) {
        if (x1 - x0 <= 0 || y1 - y0 <= 0) return;

        if (fill.a > 0) edges(x0, y0, x1, y1).radius(r).color(fill).render();
        rim(x0, y0, x1, y1, r, rimTop, rimBottom, hair);
    }

    static void rim(double x0, double y0, double x1, double y1, double r, Color top, Color bottom, double hair) {
        if (top.a == bottom.a && top.r == bottom.r && top.g == bottom.g && top.b == bottom.b) {
            if (top.a > 0) edges(x0, y0, x1, y1).radius(r).color(CLEAR).outline(top, (float) hair).render();
            return;
        }

        int bands = 3;
        double h = y1 - y0;

        for (int i = 0; i < bands; i++) {
            double by0 = y0 + Math.round(h * i / bands);
            double by1 = y0 + Math.round(h * (i + 1) / bands);
            if (by1 <= by0) continue;

            Color c = mix(top, bottom, (i + 0.5) / bands);
            if (c.a <= 0) continue;

            edges(x0, y0, x1, y1)
                    .radius(r)
                    .color(CLEAR)
                    .outline(c, (float) hair)
                    .clip(x0, by0, x1 - x0, by1 - by0)
                    .render();
        }
    }

    static void control(double x0, double y0, double x1, double y1, double r, Color fill, Color hair, double hairW) {
        RoundedRect rect = edges(x0, y0, x1, y1).radius(r).color(fill);
        if (hair != null && hair.a > 0) rect.outline(hair, (float) hairW);
        rect.render();
    }

    static void well(double x0, double y0, double x1, double y1, double r, Color fill, Color hair, double hairW) {
        control(x0, y0, x1, y1, r, fill, hair, hairW);
    }

    static void pill(double x0, double y0, double x1, double y1, Color fill) {
        if (fill.a <= 0) return;
        double h = Math.round(y1) - Math.round(y0);
        edges(x0, y0, x1, y1).radius(h / 2).color(fill).render();
    }

    static void lens(SmogGlide glide, Color ink) {
        if (glide == null || !glide.visible()) return;

        Color c = scaleAlpha(ink, glide.alpha());
        if (c.a <= 0) return;

        pill(glide.x(), glide.y(), glide.x() + glide.w(), glide.y() + glide.h(), c);
    }

    static void ring(double x0, double y0, double x1, double y1, double r, double expand, double width, Color color) {
        if (color.a <= 0 || width <= 0) return;

        edges(x0 - expand, y0 - expand, x1 + expand, y1 + expand)
                .radius(r + expand)
                .color(CLEAR)
                .outline(color, (float) width)
                .render();
    }

    static void halo(double x0, double y0, double x1, double y1, double r, double expand, double feather, Color color) {
        if (color.a <= 0) return;

        edges(x0 - expand, y0 - expand, x1 + expand, y1 + expand)
                .radius(r + expand)
                .color(color)
                .feather((float) feather)
                .render();
    }

    static void rule(double x0, double x1, double y, double hair, Color color, double fade) {
        if (color.a <= 0 || x1 <= x0) return;

        double l = Math.round(x0);
        double r = Math.round(x1);
        double t = Math.round(y);
        double f = Math.min(Math.round(fade), Math.floor((r - l) / 2));

        if (f <= 0) {
            edges(l, t, r, t + hair).radius(hair / 2).color(color).render();
            return;
        }

        Color none = col(color, 0);

        edges(l, t, l + f, t + hair).hgradient(none, color).render();
        if (r - f > l + f) edges(l + f, t, r - f, t + hair).color(color).render();
        edges(r - f, t, r, t + hair).hgradient(color, none).render();
    }

    static void vrule(double x, double y0, double y1, double hair, Color color) {
        if (color.a <= 0 || y1 <= y0) return;
        edges(x, y0, x + hair, y1).radius(hair / 2).color(color).render();
    }

    static double u(double s, double n) {
        return Math.round(n * s);
    }

    static int alpha(double a) {
        return (int) Math.max(0, Math.min(255, Math.round(a)));
    }

    static Color col(int rgb, double a) {
        return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, alpha(a));
    }

    static Color col(Color c, double a) {
        return new Color(c.r, c.g, c.b, alpha(a));
    }

    static Color scaleAlpha(Color c, double k) {
        return new Color(c.r, c.g, c.b, alpha(c.a * k));
    }

    static Color blend(int rgbA, double aA, int rgbB, double aB, double t) {
        double k = clamp01(t);
        return col(mixRgb(rgbA, rgbB, k), aA + (aB - aA) * k);
    }

    static Color mix(Color from, Color to, double t) {
        double k = clamp01(t);
        if (k <= 0) return from;
        if (k >= 1) return to;
        return new Color(
                (int) Math.round(from.r + (to.r - from.r) * k),
                (int) Math.round(from.g + (to.g - from.g) * k),
                (int) Math.round(from.b + (to.b - from.b) * k),
                (int) Math.round(from.a + (to.a - from.a) * k)
        );
    }

    static int mixRgb(int from, int to, double t) {
        double k = clamp01(t);
        int r = (int) Math.round(((from >> 16) & 0xFF) + ((((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * k));
        int g = (int) Math.round(((from >> 8) & 0xFF) + ((((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * k));
        int b = (int) Math.round((from & 0xFF) + (((to & 0xFF) - (from & 0xFF)) * k));
        return (r << 16) | (g << 8) | b;
    }

    static double lerp(double from, double to, double t) {
        return from + (to - from) * t;
    }

    static double clamp01(double v) {
        return Math.clamp(v, 0.0, 1.0);
    }

    static void set(Color target, int rgb, int a) {
        target.set((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, a);
    }
}
