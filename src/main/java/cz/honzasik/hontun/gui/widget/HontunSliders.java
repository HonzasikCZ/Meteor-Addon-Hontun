package cz.honzasik.hontun.gui.widget;

import cz.honzasik.hontun.gui.render.RoundedGui;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class HontunSliders {
    private HontunSliders() {}

    private static int a(int alpha, int widgetAlpha) {
        return Math.max(0, Math.min(255, alpha * widgetAlpha / 255));
    }

    private static int c(int alpha, int widgetAlpha, int rgb) {
        return HontunTheme.argb(a(alpha, widgetAlpha), rgb);
    }

    public static void track(GuiGraphicsExtractor g, int x, int y, int w, int h, int cx,
                             boolean active, boolean hot, boolean focused, int wa) {
        int x2 = x + w, y2 = y + h;

        if (HontunTheme.smog()) {
            int r = Math.min(8, Math.min(w, h) / 2);
            HontunRound.fill(g, x, y, w, h, r, c(!active ? 0x40 : (hot ? 0xB4 : 0x6E), wa, 0x000000));
            if (active && cx > x + 2) {
                int fill = c(hot ? 0x2C : 0x1E, wa, 0xFFFFFF);
                if (!RoundedGui.fill(g, x + 2f, y + 2f, cx - (x + 2f), h - 4f, Math.max(1f, r - 2f), 0f, fill)) {
                    g.fill(x + 2, y + 2, cx, y2 - 2, fill);
                }
            }
            if (focused) RoundedGui.outline(g, x, y, w, h, r, 1f, c(0x90, wa, 0xFFFFFF));
            return;
        }

        if (HontunTheme.modern2()) {
            int cut = Math.min(5, Math.min(w, h) / 3);
            if (hot || focused) HontunShapes.glow(g, x, y, w, h, cut, HontunTheme.accent(), 2, a(0x60, wa));
            HontunShapes.fillClipped(g, x, y, w, h, cut, cut, c(active ? (hot ? 0xF0 : 0xD0) : 0x60, wa,
                    hot ? HontunTheme.surface2() : HontunTheme.surface0()));
            HontunShapes.outlineClipped(g, x, y, w, h, cut, cut, c(active ? (hot ? 0xFF : 0x90) : 0x40, wa,
                    hot ? HontunTheme.accentHi() : HontunTheme.accentLo()));
            int pw = cx - (x + 2);
            if (pw >= cut) HontunShapes.fillClipped(g, x + 2, y + 2, pw, h - 4, Math.max(0, cut - 2), 0,
                    c(active ? 0x55 : 0x20, wa, HontunTheme.accentLo()));
            int lineEnd = Math.min(cx, x + w - cut - 1);
            if (lineEnd > x + 2) g.fill(x + 2, y + h - 2, lineEnd, y + h - 1,
                    c(active ? (hot ? 0xFF : 0xA0) : 0x30, wa, HontunTheme.accent()));
            return;
        }

        if (HontunTheme.modern1()) {
            int top = c(active ? 0x55 : 0x30, wa, HontunTheme.lighten(HontunTheme.surface2(), 1.04f));
            int bot = c(active ? 0x4A : 0x30, wa, HontunTheme.surface0());
            g.fillGradient(x + 1, y, x2 - 1, y2, top, bot);
            g.fill(x, y + 1, x + 1, y2 - 1, bot);
            g.fill(x2 - 1, y + 1, x2, y2 - 1, bot);
            g.fill(x + 1, y, x2 - 1, y + 1, c(hot ? 0x70 : 0x2E, wa, HontunTheme.overlay2()));
            if (cx > x + 1) {
                g.fillGradient(x + 1, y, cx, y2 - 2, c(0x66, wa, HontunTheme.accentLo()),
                        c(0x40, wa, HontunTheme.darken(HontunTheme.accentLo(), 0.6f)));
                g.fillGradient(x + 1, y2 - 2, cx, y2, c(active ? (hot ? 0xFF : 0xC0) : 0x40, wa, HontunTheme.accent()),
                        c(active ? 0xFF : 0x40, wa, HontunTheme.accentHi()));
            }
            if (cx < x2 - 1) g.fill(cx, y2 - 2, x2 - 1, y2, c(0x40, wa, HontunTheme.accent()));
            if (hot || focused) {
                int bd = c(0xE0, wa, HontunTheme.accentHi());
                g.fill(x + 1, y, x2 - 1, y + 1, bd);
                g.fill(x, y + 1, x + 1, y2 - 1, bd);
                g.fill(x2 - 1, y + 1, x2, y2 - 1, bd);
            }
            return;
        }

        g.fill(x, y, x2, y2, !active ? c(0x55, wa, HontunTheme.surface0())
                : c(hot ? 0xAA : 0x99, wa, hot ? HontunTheme.lighten(HontunTheme.surface1(), 1.15f) : HontunTheme.surface1()));
        if (cx > x + 1) g.fill(x + 1, y + 1, cx, y2 - 1, c(active ? (hot ? 0xAA : 0x78) : 0x30, wa, HontunTheme.accentLo()));
        int border = !active ? c(0x44, wa, HontunTheme.accentLo())
                : (hot || focused) ? c(0xFF, wa, HontunTheme.accentHi()) : c(0xAA, wa, HontunTheme.accent());
        g.fill(x, y, x2, y + 1, border);
        g.fill(x, y2 - 1, x2, y2, border);
        g.fill(x, y, x + 1, y2, border);
        g.fill(x2 - 1, y, x2, y2, border);
    }

    public static void knob(GuiGraphicsExtractor g, int hx, int y, int hw, int h, boolean active, boolean hot, int wa) {
        int y2 = y + h;

        if (HontunTheme.smog()) {
            float kx = hx + hw / 2f;
            int col = c(active ? 0xFF : 0x60, wa, HontunTheme.accentHi());
            if (!RoundedGui.capsule(g, kx, y + 5f, kx, y2 - 5f, hot ? 1.3f : 1.0f, col)) {
                g.fill(hx + hw / 2 - 1, y + 4, hx + hw / 2 + 1, y2 - 4, col);
            }
            return;
        }

        if (HontunTheme.modern2()) {
            if (hot) HontunShapes.glow(g, hx, y, hw, h, 3, HontunTheme.accent(), 1, a(0x60, wa));
            HontunShapes.fillClipped(g, hx, y, hw, h, 3, 3, c(active ? 0xE0 : 0x50, wa, HontunTheme.accentLo()));
            HontunShapes.outlineClipped(g, hx, y, hw, h, 3, 3, c(active ? (hot ? 0xFF : 0xC0) : 0x40, wa, HontunTheme.accentHi()));
            g.fill(hx + hw / 2, y + 4, hx + hw / 2 + 1, y2 - 4, c(0x80, wa, HontunTheme.accentHi()));
            return;
        }

        if (HontunTheme.modern1()) {
            int al = active ? (hot ? 0xFF : 0xD0) : 0x40;
            g.fillGradient(hx + 1, y, hx + hw - 1, y2, c(al, wa, HontunTheme.accentHi()), c(al, wa, HontunTheme.accentLo()));
            g.fillGradient(hx, y + 1, hx + hw, y2 - 1, c(al, wa, HontunTheme.accentHi()), c(al, wa, HontunTheme.accentLo()));
            return;
        }

        g.fill(hx, y, hx + hw, y2, !active ? c(0x66, wa, HontunTheme.accentLo())
                : c(0xFF, wa, hot ? HontunTheme.accentHi() : HontunTheme.accent()));
        int edge = c(0xFF, wa, HontunTheme.darken(HontunTheme.accentLo(), 0.6f));
        g.fill(hx, y, hx + hw, y + 1, edge);
        g.fill(hx, y2 - 1, hx + hw, y2, edge);
        g.fill(hx, y, hx + 1, y2, edge);
        g.fill(hx + hw - 1, y, hx + hw, y2, edge);
    }
}
