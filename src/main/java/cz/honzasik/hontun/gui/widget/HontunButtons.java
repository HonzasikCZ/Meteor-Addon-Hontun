package cz.honzasik.hontun.gui.widget;

import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class HontunButtons {
    private HontunButtons() {}

    public static void background(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean active, boolean hovered) {
        if (w <= 2 || h <= 2) return;
        int x2 = x + w, y2 = y + h;

        if (HontunTheme.smog()) {
            int a = !active ? 0x40 : (hovered ? 0xB4 : 0x6E);
            HontunRound.fill(g, x, y, w, h, Math.min(8, Math.min(w, h) / 2), HontunTheme.argb(a, 0x000000));
            return;
        }

        if (HontunTheme.modern2()) {
            int cut = Math.min(5, Math.min(w, h) / 3);
            if (hovered) HontunShapes.glow(g, x, y, w, h, cut, HontunTheme.accent(), 2, 0x60);
            HontunShapes.fillClipped(g, x, y, w, h, cut, cut, HontunTheme.argb(
                    active ? (hovered ? 0xF0 : 0xD0) : 0x60,
                    hovered ? HontunTheme.surface2() : HontunTheme.surface0()));
            HontunShapes.outlineClipped(g, x, y, w, h, cut, cut, HontunTheme.argb(
                    active ? (hovered ? 0xFF : 0x90) : 0x40,
                    hovered ? HontunTheme.accentHi() : HontunTheme.accentLo()));
            if (w > cut + 4) {
                g.fill(x + 2, y + h - 2, x + w - cut - 1, y + h - 1,
                        HontunTheme.argb(active ? (hovered ? 0xFF : 0xA0) : 0x30, HontunTheme.accent()));
            }
            return;
        }

        if (HontunTheme.modern1()) {
            int topFill, botFill;
            if (!active) {
                topFill = HontunTheme.argb(0x30, HontunTheme.lighten(HontunTheme.surface1(), 1.06f));
                botFill = HontunTheme.argb(0x30, HontunTheme.surface0());
            } else if (hovered) {
                topFill = HontunTheme.argb(0x9A, HontunTheme.accentLo());
                botFill = HontunTheme.argb(0x66, HontunTheme.darken(HontunTheme.accentLo(), 0.6f));
            } else {
                topFill = HontunTheme.argb(0x55, HontunTheme.lighten(HontunTheme.surface2(), 1.04f));
                botFill = HontunTheme.argb(0x4A, HontunTheme.surface0());
            }
            g.fillGradient(x + 1, y, x2 - 1, y2, topFill, botFill);
            g.fill(x, y + 1, x + 1, y2 - 1, botFill);
            g.fill(x2 - 1, y + 1, x2, y2 - 1, botFill);
            g.fill(x + 1, y, x2 - 1, y + 1, HontunTheme.argb(hovered ? 0x70 : 0x2E, HontunTheme.overlay2()));
            g.fillGradient(x + 1, y2 - 2, x2 - 1, y2,
                    HontunTheme.argb(active ? (hovered ? 0xFF : 0xC0) : 0x40, HontunTheme.accent()),
                    HontunTheme.argb(active ? 0xFF : 0x40, HontunTheme.accentHi()));
            if (hovered) {
                int bd = HontunTheme.argb(0xE0, HontunTheme.accentHi());
                g.fill(x + 1, y, x2 - 1, y + 1, bd);
                g.fill(x, y + 1, x + 1, y2 - 1, bd);
                g.fill(x2 - 1, y + 1, x2, y2 - 1, bd);
            }
            return;
        }

        int bg = !active ? HontunTheme.argb(0x55, HontunTheme.surface0())
                : hovered ? HontunTheme.argb(0xCC, HontunTheme.accentLo())
                : HontunTheme.argb(0x99, HontunTheme.surface1());
        int border = !active ? HontunTheme.argb(0x44, HontunTheme.accentLo())
                : hovered ? HontunTheme.argb(0xFF, HontunTheme.accentHi())
                : HontunTheme.argb(0xAA, HontunTheme.accent());
        g.fill(x, y, x2, y2, bg);
        g.fill(x, y, x2, y + 1, border);
        g.fill(x, y2 - 1, x2, y2, border);
        g.fill(x, y, x + 1, y2, border);
        g.fill(x2 - 1, y, x2, y2, border);
    }

}
