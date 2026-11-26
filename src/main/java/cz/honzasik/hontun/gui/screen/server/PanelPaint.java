package cz.honzasik.hontun.gui.screen.server;

import cz.honzasik.hontun.gui.widget.HontunCards;
import cz.honzasik.hontun.gui.widget.HontunRound;
import cz.honzasik.hontun.gui.widget.HontunShapes;
import cz.honzasik.hontun.gui.widget.TerminalWidgets;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

final class PanelPaint {
    private static final String TITLE = "Server info";
    private static final Identifier TAB = Identifier.withDefaultNamespace("widget/tab");
    private static final Identifier TAB_ON = Identifier.withDefaultNamespace("widget/tab_selected");
    private static final Identifier TAB_HI = Identifier.withDefaultNamespace("widget/tab_highlighted");
    private static final Identifier TAB_ON_HI = Identifier.withDefaultNamespace("widget/tab_selected_highlighted");

    private PanelPaint() {}

    static int headTop() { return ServerPages.t6(26, 14, 26, 28, 28, 14); }

    static void frame(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        int cx = x + w / 2;
        if (Paint.term()) {
            Component t = Paint.c("server-info");
            int tw = Paint.w(t);
            TerminalWidgets.legendFrame(g, x, y, w, h, cx, tw + 10);
            Paint.text(g, t, cx - tw / 2, y - 4, Paint.acc());
            return;
        }
        if (HontunTheme.modern2()) {
            HontunShapes.panel(g, x, y, w, h, 10);
            HontunShapes.titleChip(g, Paint.font(), cx, y, Component.literal(TITLE));
            return;
        }
        Component t = Paint.c(TITLE);
        int tw = Paint.w(t), tx = cx - tw / 2;
        if (HontunTheme.smog()) {
            HontunRound.card(g, x, y, w, h, 5, HontunTheme.argb(0xF2, HontunTheme.surface0()), HontunTheme.argb(0xFF, HontunTheme.overlay2()));
            Paint.text(g, t, tx, y + 7, Paint.text());
            g.fill(tx, y + 18, tx + tw, y + 19, HontunTheme.argb(0xC0, HontunTheme.accent()));
        } else if (HontunTheme.modern1()) {
            g.fill(x, y, x + w, y + h, HontunTheme.argb(0xF2, HontunTheme.surface0()));
            g.fillGradient(x, y, x + w, y + 2, HontunTheme.argb(0xFF, HontunTheme.accentLo()), HontunTheme.argb(0xFF, HontunTheme.accentHi()));
            int b = HontunTheme.argb(0x70, HontunTheme.overlay0());
            g.fill(x, y + h - 1, x + w, y + h, b);
            g.fill(x, y + 2, x + 1, y + h - 1, b);
            g.fill(x + w - 1, y + 2, x + w, y + h - 1, b);
            Paint.text(g, t, tx, y + 7, Paint.text());
            g.fill(tx, y + 18, tx + tw, y + 19, HontunTheme.argb(0xC0, HontunTheme.accentHi()));
        } else if (Paint.vanilla()) {
            g.fill(x, y, x + w, y + h, 0xA0000000);
            Paint.sep(g, x, y, w);
            Paint.sepF(g, x, y + h - 2, w);
            Paint.text(g, t, tx, y + 8, Paint.text());
            Paint.sep(g, x, y + 20, w);
        } else {
            g.fill(x, y, x + w, y + h, 0xCC101114);
            HontunCards.bevel(g, x, y, w, h, 0xFF050506, 0xFF5A5C66);
            Paint.text(g, t, tx, y + 8, Paint.text());
            Paint.sep(g, x + 1, y + 20, w - 2);
        }
    }

    static void veil(GuiGraphicsExtractor g, int x, int y, int w, int h, float a) {
        int col = Paint.fade(0xB0000000 | (Paint.term() ? TerminalWidgets.bg() & 0xFFFFFF : HontunTheme.crust()), a);
        if ((col >>> 24) < 4) return;
        if (HontunTheme.modern2()) HontunShapes.fillClipped(g, x, y, w, h, 10, 10, col);
        else if (HontunTheme.smog()) HontunRound.fill(g, x, y, w, h, 5, col);
        else g.fill(x, y, x + w, y + h, col);
    }

    private static void ring(GuiGraphicsExtractor g, int x, int y, int w, int h, int argb) {
        g.fill(x, y, x + w, y + 1, argb);
        g.fill(x, y + h - 1, x + w, y + h, argb);
        g.fill(x, y + 1, x + 1, y + h - 1, argb);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, argb);
    }

    static void status(GuiGraphicsExtractor g, Component label, int x, int ty, int color, int icon) {
        if (Paint.term()) {
            Component open = Paint.c("[");
            Paint.text(g, open, x, ty, Paint.dim());
            Paint.text(g, label, x + Paint.w(open), ty, color);
            Paint.text(g, Paint.c("]"), x + Paint.w(open) + Paint.w(label), ty, Paint.dim());
            return;
        }
        int w = Paint.pillWidth(label, Paint.NORMAL, icon), rgb = color & 0xFFFFFF;
        if (HontunTheme.smog()) {
            HontunRound.fill(g, x, ty - 1, w, 10, 5, HontunTheme.argb(0x22, rgb));
            int tx = x + 4;
            if (Paint.iconW(icon) > 0) {
                Paint.icon(g, icon, tx, ty + 1, color, 6);
                tx += 9;
            }
            Paint.text(g, Paint.sm(label), tx, ty, color);
            return;
        }
        int top = ty - 2, h = HontunTheme.modern1() ? 11 : 12;
        if (HontunTheme.modern2()) {
            HontunShapes.fillClipped(g, x, top, w, h, 2, 2, HontunTheme.argb(0x26, rgb));
            HontunShapes.outlineClipped(g, x, top, w, h, 2, 2, HontunTheme.argb(0x90, rgb));
        } else {
            g.fill(x, top, x + w, top + h, HontunTheme.argb(0x26, rgb));
            if (HontunTheme.modern1()) HontunCards.border(g, x, top, w, h, HontunTheme.argb(0x70, rgb));
            else ring(g, x, top, w, h, HontunTheme.argb(0xA0, rgb));
        }
        int tx = x + 4, iw = Paint.iconW(icon);
        if (iw > 0) {
            Paint.icon(g, icon, tx, ty, color);
            tx += iw + 3;
        }
        Paint.text(g, label, tx, ty, color);
    }

    static void tab(GuiGraphicsExtractor g, int x, int y, int w, int h, Component label, Component count, int gap, boolean on, boolean hover, boolean enabled) {
        int col = on || hover ? Paint.text() : Paint.sub(), cc = Paint.dim(), ty = y + (h - 8) / 2;
        if (Paint.term()) {
            if (on) g.fill(x, y + 4, x + w, y + h, TerminalWidgets.fill());
            else if (hover) g.fill(x, y + 4, x + w, y + h, TerminalWidgets.hl());
            col = on ? TerminalWidgets.ink() : hover ? Paint.acc() : TerminalWidgets.fg();
            if (on) cc = TerminalWidgets.ink();
            ty = y + 8;
        } else if (HontunTheme.smog()) {
            if (on || hover) {
                g.enableScissor(x - 1, y, x + w + 1, y + h);
                if (on) HontunRound.card(g, x, y, w, h + 8, 5, HontunTheme.argb(0xFF, HontunTheme.surface2()), 0x4A555558);
                else HontunRound.fill(g, x, y + 1, w, h + 8, 5, HontunTheme.argb(0x90, HontunTheme.surface1()));
                g.disableScissor();
            }
            col = on ? Paint.text() : hover ? Paint.sub() : Paint.dim();
        } else if (HontunTheme.modern2()) {
            if (on) {
                HontunShapes.fillClipped(g, x, y, w, h + 1, 5, 0, HontunTheme.argb(0xFF, HontunTheme.surface1()));
                g.fill(x + 5, y, x + w - 5, y + 1, HontunTheme.argb(0xFF, HontunTheme.accent()));
            } else {
                HontunShapes.fillClipped(g, x, y + 2, w, h - 2, 4, 0, HontunTheme.argb(hover ? 0xC0 : 0x80, hover ? HontunTheme.surface1() : HontunTheme.surface0()));
            }
        } else if (HontunTheme.modern1()) {
            if (on) {
                g.fillGradient(x, y + 1, x + w, y + h, 0xFF23334B, HontunTheme.argb(0xFF, HontunTheme.surface1()));
                g.fill(x + 1, y, x + w - 1, y + 1, HontunTheme.argb(0xFF, HontunTheme.accentHi()));
            } else {
                g.fill(x, y + 2, x + w, y + h, HontunTheme.argb(hover ? 0xA0 : 0x50, HontunTheme.surface2()));
            }
        } else if (Paint.vanilla()) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, on ? hover ? TAB_ON_HI : TAB_ON : hover ? TAB_HI : TAB, x, y, w, h);
            if (on) g.fill(x + 2, y + 2, x + w - 2, y + h, 0x40000000);
            col = Paint.text();
            ty = on ? y + 6 : y + 7;
        } else {
            int top = on ? y : y + 2;
            g.fill(x, top, x + w, y + h, on ? HontunTheme.argb(0xCC, HontunTheme.accentLo()) : 0x9926272D);
            int b = on ? HontunTheme.argb(0xFF, HontunTheme.accentHi()) : HontunTheme.argb(hover ? 0xFF : 0xAA, HontunTheme.accent());
            g.fill(x, top, x + w, top + 1, b);
            g.fill(x, top + 1, x + 1, y + h, b);
            g.fill(x + w - 1, top + 1, x + w, y + h, b);
            if (on) cc = Paint.sub();
        }
        if (!enabled) {
            col = Paint.fade(col, 0.45f);
            cc = Paint.fade(cc, 0.45f);
        }
        int lw = Paint.w(label), tw = lw + (count != null ? gap + Paint.w(count) : 0), tx = x + (w - tw) / 2;
        Paint.text(g, label, tx, ty, col);
        if (count != null) Paint.text(g, count, tx + lw + gap, ty, cc);
        if (on && Paint.vanilla()) g.fill(tx, y + h - 2, tx + tw, y + h - 1, 0xFFFFFFFF);
    }

    static void row(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean active, boolean hover) {
        if (w <= 0 || h <= 0) return;
        if (Paint.term()) {
            TerminalWidgets.Px p = new TerminalWidgets.Px();
            if (active || hover) p.rect(x, y, w, h, active ? TerminalWidgets.sel() : TerminalWidgets.hl());
            p.frame(x, y, w, h, active ? TerminalWidgets.lineF() : TerminalWidgets.line()).submit(g);
        } else if (HontunTheme.smog()) {
            if (active || hover) Paint.navRow(g, x, y, w, h, active, hover);
            else HontunRound.card(g, x, y, w, h, 4, 0x66000000, 0x4A555558);
        } else if (HontunTheme.modern2()) {
            HontunShapes.row(g, x, y, w, h, active, hover);
        } else if (HontunTheme.modern1()) {
            if (active) Paint.navRow(g, x, y, w, h, true, hover);
            else Paint.listCard(g, x, y, w, h, hover);
        } else if (Paint.vanilla()) {
            if (!active) ring(g, x, y, w, h, hover ? 0x80FFFFFF : 0x33FFFFFF);
            Paint.navRow(g, x, y, w, h, active, hover);
        } else {
            if (!active) {
                g.fill(x, y, x + w, y + h, 0x9926272D);
                ring(g, x, y, w, h, hover ? 0xFF5A5C66 : 0xFF44464E);
            }
            Paint.navRow(g, x, y, w, h, active, hover);
        }
    }

    static int wire() {
        if (Paint.term()) return TerminalWidgets.line();
        return Paint.vanilla() ? 0x60FFFFFF : HontunTheme.argb(0xFF, HontunTheme.overlay1());
    }

    static void hollow(GuiGraphicsExtractor g, int x, int y) {
        int c = Paint.dim();
        if (Paint.term() || HontunTheme.smog()) {
            HontunRound.fill(g, x, y, 7, 7, 4, c);
            HontunRound.fill(g, x + 1, y + 1, 5, 5, 3, Paint.term() ? TerminalWidgets.bg() | 0xFF000000 : HontunTheme.argb(0xFF, HontunTheme.surface0()));
            return;
        }
        ring(g, x, y, 7, 7, c);
    }

    static int bar(boolean last, boolean high) {
        if (last) return Paint.acc();
        if (high) return Paint.accLo();
        if (Paint.term()) return TerminalWidgets.dim();
        if (Paint.vanilla()) return 0xFF6F6F6F;
        return HontunTheme.argb(0xFF, HontunTheme.smog() ? HontunTheme.overlay2() : HontunTheme.overlay1());
    }

    static int barGrid() {
        if (Paint.term()) return TerminalWidgets.empty();
        return Paint.vanilla() ? 0x26FFFFFF : HontunTheme.argb(0x50, HontunTheme.overlay0());
    }

    static int meterBack() {
        if (Paint.term()) return TerminalWidgets.empty();
        return Paint.vanilla() ? 0xFF404040 : HontunTheme.argb(0xFF, HontunTheme.surface2());
    }

    static int meterFill() {
        if (Paint.term()) return TerminalWidgets.fill();
        return Paint.vanilla() ? 0xFF55FF55 : HontunTheme.argb(0xFF, HontunTheme.accent());
    }
}
