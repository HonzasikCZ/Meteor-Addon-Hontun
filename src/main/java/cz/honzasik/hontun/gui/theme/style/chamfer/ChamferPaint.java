package cz.honzasik.hontun.gui.theme.style.chamfer;

import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.render.HontunRenderer;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunLightPalette;
import cz.honzasik.hontun.gui.widget.HontunShapes;
import cz.honzasik.hontun.utils.HontunTheme;
import meteordevelopment.meteorclient.utils.render.color.Color;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

final class ChamferPaint {
    static final Color SUBTEXT1_DARK = HontunTheme.color(HontunTheme.ramp(HontunTheme.UiMode.HModern2).subtext1());
    static final Color SUBTEXT1_LIGHT = HontunTheme.color(HontunLightPalette.ramp(HontunTheme.UiMode.HModern2).subtext1());
    static final Color CLEAR = new Color(0, 0, 0, 0);
    static final Color RIM_LIGHT = HontunTheme.color(HontunTheme.lerp(
            HontunLightPalette.ramp(HontunTheme.UiMode.HModern2).overlay2(),
            HontunLightPalette.ramp(HontunTheme.UiMode.HModern2).textDim(), 0.4f));

    private static final Map<String, RichText> UPPER = new HashMap<>();

    private ChamferPaint() {}

    static int u(HontunGuiTheme t) {
        return (int) Math.max(1, Math.round(t.scale(2)));
    }

    static int s(HontunGuiTheme t, double v) {
        return (int) Math.round(t.scale(v));
    }

    static int px(double v) {
        return (int) Math.round(v);
    }

    static int rgb(Color c) {
        return (c.r << 16) | (c.g << 8) | c.b;
    }

    static int argb(int a, Color c) {
        return (Math.clamp(a, 0, 255) << 24) | rgb(c);
    }

    static int argb(int a, int rgb) {
        return (Math.clamp(a, 0, 255) << 24) | (rgb & 0xFFFFFF);
    }

    static int alpha(double f) {
        return (int) Math.clamp(Math.round(f * 255), 0, 255);
    }

    static int mul(int a, double f) {
        return (int) Math.clamp(Math.round(a * f), 0, 255);
    }

    static int mix(int a, int b, double f) {
        return (int) Math.round(a + (b - a) * Math.clamp(f, 0, 1));
    }

    static int lerp(Color a, Color b, double f) {
        return HontunTheme.lerp(rgb(a), rgb(b), (float) Math.clamp(f, 0, 1));
    }

    static int lerpArgb(int aA, Color a, int aB, Color b, double f) {
        return argb(mix(aA, aB, f), lerp(a, b, f));
    }

    static Color color(int rgb) {
        return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, 255);
    }

    static Color lerpColor(Color a, Color b, double f) {
        if (f <= 0) return a;
        if (f >= 1) return b;
        return color(lerp(a, b, f));
    }

    static double q4(double p) {
        return Math.floor(Math.clamp(p, 0, 1) * 4) / 4;
    }

    static Color subtext1(HontunGuiTheme t) {
        return t.light() ? SUBTEXT1_LIGHT : SUBTEXT1_DARK;
    }

    static int ink(HontunGuiTheme t, int a) {
        return t.light() ? 0xFF : a;
    }

    static Color rim(HontunGuiTheme t) {
        return t.light() ? RIM_LIGHT : t.overlay0Color();
    }

    static Color edgeText(HontunGuiTheme t) {
        return t.light() ? t.accentColor() : t.accentHiColor();
    }

    static int backdropBracket(HontunGuiTheme t) {
        return t.light() ? argb(0xFF, HontunTheme.userAccentHi()) : argb(0xFF, t.accentHiColor());
    }

    static int pressedPlate(HontunGuiTheme t) {
        return t.light() ? argb(0x90, t.overlay1Color()) : argb(0xF0, t.crustColor());
    }

    static Color onAccent(HontunGuiTheme t) {
        if (t.light()) return t.baseColor();
        int accent = rgb(t.accentColor());
        double crust = HontunLightPalette.contrast(rgb(t.crustColor()), accent);
        double base = HontunLightPalette.contrast(rgb(t.baseColor()), accent);
        return crust >= base ? t.crustColor() : t.baseColor();
    }

    static int cutFor(int h, int u) {
        int c = Math.min(3 * u, h / 3);
        return Math.max(0, c / u * u);
    }

    static void rect(HontunShapes.Fill g, int x0, int y0, int x1, int y1, int argb) {
        if (x1 <= x0 || y1 <= y0 || (argb >>> 24) == 0) return;
        g.fill(x0, y0, x1, y1, argb);
    }

    static void plate(HontunShapes.Fill g, int x0, int y0, int x1, int y1, int cutTL, int cutBR, int u, int argb) {
        HontunShapes.fillClipped(g, x0, y0, x1 - x0, y1 - y0, cutTL, cutBR, u, argb);
    }

    static void frame(HontunShapes.Fill g, int x0, int y0, int x1, int y1, int cutTL, int cutBR, int u, int argb) {
        HontunShapes.outlineClipped(g, x0, y0, x1 - x0, y1 - y0, cutTL, cutBR, u, argb);
    }

    static void brackets(HontunShapes.Fill g, int x0, int y0, int x1, int y1, int len, int u, int argb) {
        HontunShapes.brackets(g, x0, y0, x1 - x0, y1 - y0, len, u, argb);
    }

    static void glow(HontunShapes.Fill g, HontunGuiTheme t, int x0, int y0, int x1, int y1,
                     int cutTL, int cutBR, int u, int rings, int base) {
        if (rings <= 0 || base <= 0) return;
        if (t.light()) {
            HontunShapes.outlineClipped(g, x0 - u, y0 - u, (x1 - x0) + 2 * u, (y1 - y0) + 2 * u,
                    cutTL, cutBR, u, argb(2 * base, t.accentHiColor()));
            return;
        }
        HontunShapes.glow(g, x0, y0, x1 - x0, y1 - y0, cutTL, cutBR, u, rings, argb(base, t.accentColor()));
    }

    static void backdropGlow(HontunShapes.Fill g, HontunGuiTheme t, int x0, int y0, int x1, int y1,
                             int cutTL, int cutBR, int u, int rings, int base) {
        if (!t.light()) {
            glow(g, t, x0, y0, x1, y1, cutTL, cutBR, u, rings, base);
            return;
        }
        if (rings <= 0 || base <= 0) return;
        HontunShapes.glow(g, x0, y0, x1 - x0, y1 - y0, cutTL, cutBR, u, rings, argb(base, HontunTheme.userAccent()));
    }

    static void glow(HontunShapes.Fill g, HontunGuiTheme t, int x0, int y0, int x1, int y1,
                     int cutTL, int cutBR, int u, int rings, int base, Color tint) {
        if (rings <= 0 || base <= 0) return;
        if (t.light()) {
            HontunShapes.outlineClipped(g, x0 - u, y0 - u, (x1 - x0) + 2 * u, (y1 - y0) + 2 * u,
                    cutTL, cutBR, u, argb(2 * base, HontunTheme.darken(rgb(tint), 0.78f)));
            return;
        }
        HontunShapes.glow(g, x0, y0, x1 - x0, y1 - y0, cutTL, cutBR, u, rings, argb(base, tint));
    }

    static void shadowRings(HontunShapes.Fill g, HontunGuiTheme t, int x0, int y0, int x1, int y1,
                            int cutTL, int cutBR, int u, int rings) {
        int d = (t.light() ? 2 : rings + 1) * u;
        int argb = t.light() ? 0x18000000 : argb(0x90, t.crustColor());
        HontunShapes.outlineClipped(g, x0 - d, y0 - d, (x1 - x0) + 2 * d, (y1 - y0) + 2 * d, cutTL, cutBR, u, argb);
    }

    static void divider(HontunShapes.Fill g, HontunGuiTheme t, int x0, int x1, int y, int u) {
        if (x1 <= x0) return;
        int head = Math.min(x1, x0 + 4 * u);
        rect(g, x0, y, head, y + u, argb(0xFF, t.accentHiColor()));
        int start = head + u;
        if (x1 <= start) return;
        if (t.light()) g.hgradient(start, y, x1, y + u, argb(0xFF, t.accentLoColor()), argb(0xFF, t.overlay1Color()));
        else g.hgradient(start, y, x1, y + u, argb(0xA0, t.accentLoColor()), argb(0x60, t.overlay0Color()));
    }

    static void scan(HontunShapes.Fill g, HontunGuiTheme t, int x0, int x1, int y, int u, double p) {
        double f = Math.sin(Math.PI * Math.clamp(p, 0, 1));
        int a = mul(0xD0, f);
        if (a <= 0) return;
        rect(g, x0, y, x1, y + u, argb(a, t.accentHiColor()));
    }

    static void bracketGlyph(HontunShapes.Fill g, int x, int y, int h, int u, boolean open, int argb) {
        int serif = 2 * u;
        if (open) {
            rect(g, x, y, x + serif, y + u, argb);
            rect(g, x, y + u, x + u, y + h - u, argb);
            rect(g, x, y + h - u, x + serif, y + h, argb);
        } else {
            rect(g, x - serif, y, x, y + u, argb);
            rect(g, x - u, y + u, x, y + h - u, argb);
            rect(g, x - serif, y + h - u, x, y + h, argb);
        }
    }

    static RichText upper(String text, boolean bold) {
        String key = (bold ? "B" : "R") + text;
        RichText cached = UPPER.get(key);
        if (cached == null) {
            String up = text == null ? "" : text.toUpperCase(Locale.ROOT);
            cached = bold ? RichText.bold(up) : RichText.of(up);
            if (UPPER.size() > 512) UPPER.clear();
            UPPER.put(key, cached);
        }
        return cached;
    }

    static void drawText(RichText text, double x, double y, Color color) {
        HontunRenderer.get().text(text, Math.round(x), Math.round(y), color);
    }
}
