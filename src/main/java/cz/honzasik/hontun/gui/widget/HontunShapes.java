package cz.honzasik.hontun.gui.widget;

import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class HontunShapes {
    private HontunShapes() {}

    public interface Fill {
        void fill(int x1, int y1, int x2, int y2, int argb);

        void gradient(int x1, int y1, int x2, int y2, int topArgb, int bottomArgb);

        default void hgradient(int x1, int y1, int x2, int y2, int leftArgb, int rightArgb) {
            fill(x1, y1, x2, y2, leftArgb);
        }

        static Fill of(GuiGraphicsExtractor g) {
            return new Fill() {
                @Override
                public void fill(int x1, int y1, int x2, int y2, int argb) {
                    g.fill(x1, y1, x2, y2, argb);
                }

                @Override
                public void gradient(int x1, int y1, int x2, int y2, int topArgb, int bottomArgb) {
                    g.fillGradient(x1, y1, x2, y2, topArgb, bottomArgb);
                }
            };
        }
    }

    public static void fillClipped(GuiGraphicsExtractor g, int x, int y, int w, int h,
                                   int cutTL, int cutBR, int argb) {
        fillClipped(Fill.of(g), x, y, w, h, cutTL, cutBR, argb);
    }

    public static void fillClipped(Fill g, int x, int y, int w, int h,
                                   int cutTL, int cutBR, int argb) {
        if (w <= 0 || h <= 0) return;
        cutTL = Math.max(0, Math.min(cutTL, Math.min(w, h) / 2));
        cutBR = Math.max(0, Math.min(cutBR, Math.min(w, h) / 2));

        int bodyTop = y + cutTL;
        int bodyBottom = y + h - cutBR;
        if (bodyBottom > bodyTop) g.fill(x, bodyTop, x + w, bodyBottom, argb);

        for (int i = 0; i < cutTL; i++) {
            g.fill(x + cutTL - i, y + i, x + w, y + i + 1, argb);
        }

        for (int i = 0; i < cutBR; i++) {
            g.fill(x, y + h - 1 - i, x + w - cutBR + i, y + h - i, argb);
        }
    }

    public static void outlineClipped(GuiGraphicsExtractor g, int x, int y, int w, int h,
                                      int cutTL, int cutBR, int argb) {
        outlineClipped(Fill.of(g), x, y, w, h, cutTL, cutBR, argb);
    }

    public static void outlineClipped(Fill g, int x, int y, int w, int h,
                                      int cutTL, int cutBR, int argb) {
        if (w <= 0 || h <= 0) return;
        cutTL = Math.max(0, Math.min(cutTL, Math.min(w, h) / 2));
        cutBR = Math.max(0, Math.min(cutBR, Math.min(w, h) / 2));

        g.fill(x + cutTL, y, x + w, y + 1, argb);
        g.fill(x, y + h - 1, x + w - cutBR, y + h, argb);
        g.fill(x, y + cutTL, x + 1, y + h, argb);
        g.fill(x + w - 1, y, x + w, y + h - cutBR, argb);
        for (int i = 0; i < cutTL; i++) {
            g.fill(x + cutTL - i - 1, y + i, x + cutTL - i, y + i + 1, argb);
        }
        for (int i = 0; i < cutBR; i++) {
            g.fill(x + w - cutBR + i, y + h - 1 - i, x + w - cutBR + i + 1, y + h - i, argb);
        }
    }

    public static void glow(GuiGraphicsExtractor g, int x, int y, int w, int h,
                            int cut, int rgb, int layers, int baseAlpha) {
        glow(Fill.of(g), x, y, w, h, cut, rgb, layers, baseAlpha);
    }

    public static void glow(Fill g, int x, int y, int w, int h,
                            int cut, int rgb, int layers, int baseAlpha) {
        for (int i = 1; i <= layers; i++) {
            outlineClipped(g, x - i, y - i, w + 2 * i, h + 2 * i, cut, cut,
                    HontunTheme.argb(baseAlpha / (i + 1), rgb));
        }
    }

    public static void brackets(GuiGraphicsExtractor g, int x, int y, int w, int h, int len, int argb) {
        brackets(Fill.of(g), x, y, w, h, len, argb);
    }

    public static void brackets(Fill g, int x, int y, int w, int h, int len, int argb) {
        g.fill(x + w - len, y, x + w, y + 1, argb);
        g.fill(x + w - 1, y, x + w, y + len, argb);
        g.fill(x, y + h - 1, x + len, y + h, argb);
        g.fill(x, y + h - len, x + 1, y + h, argb);
    }

    public static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h, int cut) {
        glow(g, x, y, w, h, cut, HontunTheme.accent(), 3, 0x50);
        fillClipped(g, x, y, w, h, cut, cut, HontunTheme.argb(0xF2, HontunTheme.base()));
        outlineClipped(g, x, y, w, h, cut, cut, HontunTheme.argb(0xC0, HontunTheme.accent()));
        brackets(g, x, y, w, h, 8, HontunTheme.argb(0xFF, HontunTheme.accentHi()));
    }

    public static void iconFrame(GuiGraphicsExtractor g, int x, int y, int size) {
        fillClipped(g, x, y, size, size, 3, 3, HontunTheme.argb(0xFF, HontunTheme.crust()));
        outlineClipped(g, x, y, size, size, 3, 3, HontunTheme.argb(0x80, HontunTheme.overlay0()));
    }

    public static void row(GuiGraphicsExtractor g, int x, int y, int w, int h,
                           boolean active, boolean hovered) {
        int cut = 6;
        int fillRgb = active ? HontunTheme.surface1() : HontunTheme.surface0();
        int fillA = active ? 0xF0 : (hovered ? 0xE0 : 0xB0);
        fillClipped(g, x, y, w, h, cut, cut, HontunTheme.argb(fillA, fillRgb));

        int borderRgb = active ? HontunTheme.accent() : (hovered ? HontunTheme.accentLo() : HontunTheme.overlay0());
        int borderA = active ? 0xFF : (hovered ? 0xC0 : 0x50);
        outlineClipped(g, x, y, w, h, cut, cut, HontunTheme.argb(borderA, borderRgb));

        if (active) {
            leftBarClipped(g, x, y, h, cut,
                    HontunTheme.argb(0xFF, HontunTheme.accentHi()),
                    HontunTheme.argb(0xFF, HontunTheme.accentLo()));
        }
    }

    public static void leftBarClipped(GuiGraphicsExtractor g, int x, int y, int h, int cut,
                                      int topArgb, int botArgb) {
        leftBarClipped(Fill.of(g), x, y, h, cut, topArgb, botArgb);
    }

    public static void leftBarClipped(Fill g, int x, int y, int h, int cut,
                                      int topArgb, int botArgb) {
        int inset = 2;
        for (int i = inset; i < cut; i++) {
            int lx = x + (cut - i) + 2;
            g.fill(lx, y + i + 1, lx + 3, y + i + 2, topArgb);
        }
        g.gradient(x + 2, y + cut + 1, x + 5, y + h - 3, topArgb, botArgb);
    }

    public static void groupBox(GuiGraphicsExtractor g, int x, int y, int w, int h, int labelW) {
        int cut = 5;
        int b = HontunTheme.argb(0x70, HontunTheme.overlay0());
        int gap = labelW + 6;
        g.fill(x + cut, y, x + 8, y + 1, b);
        g.fill(x + 8 + gap, y, x + w, y + 1, b);
        g.fill(x, y + h - 1, x + w - cut, y + h, b);
        g.fill(x, y + cut, x + 1, y + h, b);
        g.fill(x + w - 1, y, x + w, y + h - cut, b);
        for (int i = 0; i < cut; i++) g.fill(x + cut - i - 1, y + i, x + cut - i, y + i + 1, b);
        for (int i = 0; i < cut; i++) g.fill(x + w - cut + i, y + h - 1 - i, x + w - cut + i + 1, y + h - i, b);
    }

    public static void titleChip(GuiGraphicsExtractor g, Font f, int cxMid, int panelTop, Component title) {
        int tw = f.width(title);
        int w = tw + 34, h = 16;
        int x = cxMid - w / 2, y = panelTop - h / 2;

        fillClipped(g, x, y, w, h, 7, 7, HontunTheme.argb(0xFF, HontunTheme.crust()));
        outlineClipped(g, x, y, w, h, 7, 7, HontunTheme.argb(0xFF, HontunTheme.accent()));
        glow(g, x, y, w, h, 7, HontunTheme.accent(), 2, 0x50);

        int mid = y + h / 2;
        g.fill(x + 8, mid, x + 13, mid + 1, HontunTheme.argb(0xFF, HontunTheme.accentHi()));
        g.fill(x + w - 13, mid, x + w - 8, mid + 1, HontunTheme.argb(0xFF, HontunTheme.accentHi()));

        g.text(f, title, cxMid - tw / 2, y + 4, HontunTheme.argb(0xFF, HontunTheme.textLight()), true);
    }

    public static void scrollbar(GuiGraphicsExtractor g, int x, int top, int bot, int w,
                                 int thumbTop, int thumbH) {
        if (w <= 0 || bot <= top) return;

        if (HontunTheme.smog()) {
            float lane = Math.min(3f, w);
            float lx = x + (w - lane) / 2f;
            if (cz.honzasik.hontun.gui.render.RoundedGui.available()) {
                cz.honzasik.hontun.gui.render.RoundedGui.fill(g, lx, top + 2f, lane, bot - top - 4f, lane / 2f, 0f,
                        HontunTheme.argb(0x16, 0xFFFFFF));
                cz.honzasik.hontun.gui.render.RoundedGui.fill(g, lx, thumbTop + 2f, lane, Math.max(lane * 2f, thumbH - 4f),
                        lane / 2f, 0f, HontunTheme.argb(0xA8, HontunTheme.subtext1()));
                return;
            }
            HontunRound.fill(g, x + 1, thumbTop, Math.max(1, w - 2), thumbH, Math.max(1, (w - 2) / 2),
                    HontunTheme.argb(0xA8, HontunTheme.subtext1()));
            return;
        }

        g.fill(x, top, x + w, bot, HontunTheme.argb(0x60, HontunTheme.crust()));

        if (HontunTheme.modern2()) {
            int cut = Math.min(3, w / 2);
            glow(g, x, thumbTop, w, thumbH, cut, HontunTheme.accent(), 1, 0x60);
            fillClipped(g, x, thumbTop, w, thumbH, cut, cut, HontunTheme.argb(0xE0, HontunTheme.accentLo()));
            outlineClipped(g, x, thumbTop, w, thumbH, cut, cut,
                    HontunTheme.argb(0xFF, HontunTheme.accentHi()));

            int mid = x + w / 2;
            g.fill(mid, thumbTop + 4, mid + 1, thumbTop + thumbH - 4,
                    HontunTheme.argb(0x80, HontunTheme.accentHi()));
        } else {
            g.fillGradient(x + 1, thumbTop, x + w - 1, thumbTop + thumbH,
                    HontunTheme.argb(0xFF, HontunTheme.accentHi()),
                    HontunTheme.argb(0xFF, HontunTheme.accentLo()));
            g.fillGradient(x, thumbTop + 1, x + w, thumbTop + thumbH - 1,
                    HontunTheme.argb(0xFF, HontunTheme.accentHi()),
                    HontunTheme.argb(0xFF, HontunTheme.accentLo()));
        }
    }

    public static void fillClipped(Fill g, double x, double y, double w, double h,
                                   int cutTL, int cutBR, int u, int argb) {
        int x0 = snap(x), y0 = snap(y), x1 = snap(x + w), y1 = snap(y + h);
        if (x1 <= x0 || y1 <= y0 || (argb >>> 24) == 0) return;
        u = Math.max(1, u);
        int lim = Math.min(x1 - x0, y1 - y0) / 2;
        int a = unitCut(cutTL, u, lim), b = unitCut(cutBR, u, lim);

        rect(g, x0, y0 + a, x1, y1 - b, argb);
        for (int k = 0; k * u < a; k++) rect(g, x0 + a - k * u, y0 + k * u, x1, y0 + (k + 1) * u, argb);
        for (int k = 0; k * u < b; k++) rect(g, x0, y1 - (k + 1) * u, x1 - b + k * u, y1 - k * u, argb);
    }

    public static void outlineClipped(Fill g, double x, double y, double w, double h,
                                      int cutTL, int cutBR, int u, int argb) {
        outlineRun(g, x, y, w, h, cutTL, cutBR, u, true, true, argb);
    }

    public static void outlineRun(Fill g, double x, double y, double w, double h,
                                  int cutTL, int cutBR, int u, boolean top, boolean bottom, int argb) {
        int x0 = snap(x), y0 = snap(y), x1 = snap(x + w), y1 = snap(y + h);
        if (x1 <= x0 || y1 <= y0 || (argb >>> 24) == 0) return;
        u = Math.max(1, u);
        int lim = Math.min(x1 - x0, y1 - y0) / 2;
        int a = top ? unitCut(cutTL, u, lim) : 0;
        int b = bottom ? unitCut(cutBR, u, lim) : 0;
        unitEdges(g, x0, y0, x1, y1, a, b, u, top, bottom, null, argb);
    }

    public static void glow(Fill g, double x, double y, double w, double h,
                            int cutTL, int cutBR, int u, int n, int argb) {
        int x0 = snap(x), y0 = snap(y), x1 = snap(x + w), y1 = snap(y + h);
        if (x1 <= x0 || y1 <= y0) return;
        u = Math.max(1, u);
        int base = argb >>> 24;
        int rgb = argb & 0xFFFFFF;
        for (int i = 1; i <= n; i++) {
            int a = base / (i + 1);
            if (a <= 0) continue;
            int d = i * u;
            outlineRun(g, x0 - d, y0 - d, (x1 - x0) + 2 * d, (y1 - y0) + 2 * d,
                    cutTL, cutBR, u, true, true, (a << 24) | rgb);
        }
    }

    public static void brackets(Fill g, double x, double y, double w, double h, int len, int u, int argb) {
        int x0 = snap(x), y0 = snap(y), x1 = snap(x + w), y1 = snap(y + h);
        if (x1 <= x0 || y1 <= y0 || (argb >>> 24) == 0) return;
        u = Math.max(1, u);
        len = Math.max(u, len);
        rect(g, x1 - len, y0, x1, y0 + u, argb);
        rect(g, x1 - u, y0 + u, x1, y0 + len, argb);
        rect(g, x0, y1 - u, x0 + len, y1, argb);
        rect(g, x0, y1 - len, x0 + u, y1 - u, argb);
    }

    public static void leftBarClipped(Fill g, double x, double y, double h, int cut, int u,
                                      int topArgb, int botArgb) {
        int x0 = snap(x), y0 = snap(y), y1 = snap(y + h);
        u = Math.max(1, u);
        for (int i = 2 * u; i < cut; i += u) {
            int lx = x0 + (cut - i) + 2 * u;
            rect(g, lx, y0 + i + u, lx + 3 * u, y0 + i + 2 * u, topArgb);
        }
        if (y1 - 3 * u > y0 + cut + u) g.gradient(x0 + 2 * u, y0 + cut + u, x0 + 5 * u, y1 - 3 * u, topArgb, botArgb);
    }

    public static void groupBox(Fill g, double x, double y, double w, double h, int cut, int u,
                                int[] gaps, int argb) {
        int x0 = snap(x), y0 = snap(y), x1 = snap(x + w), y1 = snap(y + h);
        if (x1 <= x0 || y1 <= y0 || (argb >>> 24) == 0) return;
        u = Math.max(1, u);
        int lim = Math.min(x1 - x0, y1 - y0) / 2;
        int c = unitCut(cut, u, lim);
        unitEdges(g, x0, y0, x1, y1, c, c, u, true, true, gaps, argb);
    }

    public static void dashH(Fill g, double x1, double x2, double y, int u, int on, int off, int argb) {
        int a = snap(x1), b = snap(x2), yy = snap(y);
        if (b <= a || (argb >>> 24) == 0) return;
        u = Math.max(1, u);
        on = Math.max(1, on);
        off = Math.max(0, off);
        for (int xx = a; xx < b; xx += on + off) rect(g, xx, yy, Math.min(xx + on, b), yy + u, argb);
    }

    public static void checker(Fill g, double x, double y, double w, double h,
                               int cutTL, int cutBR, int u, int cell, int a, int b) {
        int x0 = snap(x), y0 = snap(y), x1 = snap(x + w), y1 = snap(y + h);
        if (x1 <= x0 || y1 <= y0) return;
        u = Math.max(1, u);
        cell = Math.max(1, cell);
        int lim = Math.min(x1 - x0, y1 - y0) / 2;
        int ca = unitCut(cutTL, u, lim), cb = unitCut(cutBR, u, lim);

        for (int k = 0; k * u < ca; k++) {
            checkerBand(g, x0, y0, cell, x0 + ca - k * u, x1, y0 + k * u, y0 + (k + 1) * u, a, b);
        }
        checkerBand(g, x0, y0, cell, x0, x1, y0 + ca, y1 - cb, a, b);
        for (int k = 0; k * u < cb; k++) {
            checkerBand(g, x0, y0, cell, x0, x1 - cb + k * u, y1 - (k + 1) * u, y1 - k * u, a, b);
        }
    }

    private static void checkerBand(Fill g, int ox, int oy, int cell, int sx0, int sx1, int sy0, int sy1, int a, int b) {
        if (sx1 <= sx0 || sy1 <= sy0) return;
        int j0 = Math.floorDiv(sy0 - oy, cell);
        int j1 = Math.floorDiv(sy1 - 1 - oy, cell);
        int i0 = Math.floorDiv(sx0 - ox, cell);
        int i1 = Math.floorDiv(sx1 - 1 - ox, cell);
        for (int j = j0; j <= j1; j++) {
            int cy0 = Math.max(sy0, oy + j * cell);
            int cy1 = Math.min(sy1, oy + (j + 1) * cell);
            for (int i = i0; i <= i1; i++) {
                int cx0 = Math.max(sx0, ox + i * cell);
                int cx1 = Math.min(sx1, ox + (i + 1) * cell);
                rect(g, cx0, cy0, cx1, cy1, ((i + j) & 1) == 0 ? a : b);
            }
        }
    }

    private static void unitEdges(Fill g, int x0, int y0, int x1, int y1, int a, int b, int u,
                                  boolean top, boolean bottom, int[] gaps, int argb) {
        if (top) {
            gappedLine(g, x0 + a, x1, y0, u, gaps, argb);
            for (int k = 0; k * u < a; k++) {
                rect(g, x0 + a - (k + 1) * u, y0 + k * u, x0 + a - k * u, y0 + (k + 1) * u, argb);
            }
        }
        if (bottom) {
            rect(g, x0, y1 - u, x1 - b, y1, argb);
            for (int k = 0; k * u < b; k++) {
                rect(g, x1 - b + k * u, y1 - (k + 1) * u, x1 - b + (k + 1) * u, y1 - k * u, argb);
            }
        }
        rect(g, x0, top ? y0 + Math.max(a, u) : y0, x0 + u, bottom ? y1 - u : y1, argb);
        rect(g, x1 - u, top ? y0 + u : y0, x1, bottom ? y1 - Math.max(b, u) : y1, argb);
    }

    private static void gappedLine(Fill g, int xa, int xb, int y, int u, int[] gaps, int argb) {
        if (gaps == null || gaps.length < 2) {
            rect(g, xa, y, xb, y + u, argb);
            return;
        }
        int cur = xa;
        for (int i = 0; i + 1 < gaps.length; i += 2) {
            int g0 = Math.min(gaps[i], gaps[i + 1]);
            int g1 = Math.max(gaps[i], gaps[i + 1]);
            if (g1 <= cur) continue;
            if (g0 >= xb) break;
            rect(g, cur, y, Math.min(g0, xb), y + u, argb);
            cur = Math.max(cur, g1);
        }
        rect(g, cur, y, xb, y + u, argb);
    }

    private static int unitCut(int cut, int u, int lim) {
        int c = Math.max(0, Math.min(cut, lim));
        return c / u * u;
    }

    private static void rect(Fill g, int x1, int y1, int x2, int y2, int argb) {
        if (x2 <= x1 || y2 <= y1) return;
        g.fill(x1, y1, x2, y2, argb);
    }

    private static int snap(double v) {
        return (int) Math.round(v);
    }
}
