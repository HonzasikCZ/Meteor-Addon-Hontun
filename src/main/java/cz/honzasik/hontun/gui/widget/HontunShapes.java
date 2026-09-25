package cz.honzasik.hontun.gui.widget;

import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class HontunShapes {
    private HontunShapes() {}

    public static void fillClipped(GuiGraphicsExtractor g, int x, int y, int w, int h,
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
        for (int i = 1; i <= layers; i++) {
            outlineClipped(g, x - i, y - i, w + 2 * i, h + 2 * i, cut, cut,
                    HontunTheme.argb(baseAlpha / (i + 1), rgb));
        }
    }

    public static void brackets(GuiGraphicsExtractor g, int x, int y, int w, int h, int len, int argb) {
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
        int inset = 2;
        for (int i = inset; i < cut; i++) {
            int lx = x + (cut - i) + 2;
            g.fill(lx, y + i + 1, lx + 3, y + i + 2, topArgb);
        }
        g.fillGradient(x + 2, y + cut + 1, x + 5, y + h - 3, topArgb, botArgb);
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
}
