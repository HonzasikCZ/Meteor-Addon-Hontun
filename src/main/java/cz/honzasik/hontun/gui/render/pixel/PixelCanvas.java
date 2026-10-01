package cz.honzasik.hontun.gui.render.pixel;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import cz.honzasik.hontun.gui.HontunGui;
import cz.honzasik.hontun.gui.api.text.FontStyle;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.api.text.RichTextSegment;
import cz.honzasik.hontun.gui.render.HontunRenderer;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

import java.util.IdentityHashMap;
import java.util.Map;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public final class PixelCanvas {
    public static final int TOP = 1;
    public static final int RIGHT = 2;
    public static final int BOTTOM = 4;
    public static final int LEFT = 8;
    public static final int ALL = TOP | RIGHT | BOTTOM | LEFT;

    private static final int GLYPH_SHADOW_ALPHA = 0x60;
    private static final Identifier INWORLD_MENU_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/inworld_menu_background.png");
    private static final Map<PixelGlyph, PixelGlyph[]> TURNED = new IdentityHashMap<>();

    private static double alpha = 1;
    private static boolean missLogged;

    static {
        HontunRenderer.addStyleListener(PixelCanvas::reset);
    }

    private PixelCanvas() {}

    public static void reset() {
        alpha = 1;
        TURNED.clear();
    }

    public static GuiGraphicsExtractor graphics() {
        return HontunRenderer.get().graphics();
    }

    public static HontunGuiTheme theme() {
        HontunGuiTheme theme = HontunRenderer.get().theme();
        if (theme != null) return theme;
        return GuiThemes.get() instanceof HontunGuiTheme hontun ? hontun : null;
    }

    public static int unit() {
        HontunGuiTheme theme = theme();
        return theme == null ? 1 : unit(theme);
    }

    public static int unit(HontunGuiTheme theme) {
        return Math.max(1, theme.style().pixelUnit(theme));
    }

    public static int snap(double v) {
        return snap(v, unit());
    }

    public static int snap(double v, int p) {
        return (int) Math.floor(v / p + 0.5) * p;
    }

    public static double alpha() {
        return alpha;
    }

    public static void setAlpha(double a) {
        alpha = Math.max(0, Math.min(1, a));
    }

    public static int applyAlpha(int argb) {
        if (alpha >= 1) return argb;
        int a = (int) Math.round((argb >>> 24) * alpha);
        return (a << 24) | (argb & 0xFFFFFF);
    }

    public static void nextLayer() {
        GuiGraphicsExtractor g = graphics();
        if (g != null) g.nextStratum();
    }

    public static void pushClip(double x, double y, double w, double h) {
        GuiGraphicsExtractor g = graphics();
        if (g == null) return;
        int p = unit();
        g.enableScissor(snap(x, p), snap(y, p), snap(x + w, p), snap(y + h, p));
    }

    public static void popClip() {
        GuiGraphicsExtractor g = graphics();
        if (g != null) g.disableScissor();
    }

    public static void rawFill(int x0, int y0, int x1, int y1, int argb) {
        GuiGraphicsExtractor g = graphics();
        if (g == null) return;
        put(g, Math.min(x0, x1), Math.min(y0, y1), Math.max(x0, x1), Math.max(y0, y1), applyAlpha(argb));
    }

    public static void fill(double x, double y, double w, double h, int argb) {
        GuiGraphicsExtractor g = graphics();
        if (g == null) return;
        int p = unit();
        double l = Math.min(x, x + w);
        double t = Math.min(y, y + h);
        put(g, snap(l, p), snap(t, p), snap(l + Math.abs(w), p), snap(t + Math.abs(h), p), applyAlpha(argb));
    }

    public static void frame(double x, double y, double w, double h, int argb) {
        frame(x, y, w, h, argb, ALL, 1);
    }

    public static void frame(double x, double y, double w, double h, int argb, int sides) {
        frame(x, y, w, h, argb, sides, 1);
    }

    public static void frame(double x, double y, double w, double h, int argb, int sides, int units) {
        GuiGraphicsExtractor g = graphics();
        if (g == null) return;
        int c = applyAlpha(argb);
        if ((c >>> 24) == 0) return;

        int p = unit();
        int x0 = snap(x, p), y0 = snap(y, p), x1 = snap(x + w, p), y1 = snap(y + h, p);
        if (x1 <= x0 || y1 <= y0) return;

        int t = Math.max(1, units) * p;
        int top = (sides & TOP) != 0 ? Math.min(t, y1 - y0) : 0;
        int bottom = (sides & BOTTOM) != 0 ? Math.min(t, y1 - y0 - top) : 0;
        int left = (sides & LEFT) != 0 ? Math.min(t, x1 - x0) : 0;
        int right = (sides & RIGHT) != 0 ? Math.min(t, x1 - x0 - left) : 0;

        put(g, x0, y0, x1, y0 + top, c);
        put(g, x0, y1 - bottom, x1, y1, c);
        put(g, x0, y0 + top, x0 + left, y1 - bottom, c);
        put(g, x1 - right, y0 + top, x1, y1 - bottom, c);
    }

    public static void raised(double x, double y, double w, double h, int hi, int lo) {
        GuiGraphicsExtractor g = graphics();
        if (g == null) return;

        int p = unit();
        int x0 = snap(x, p), y0 = snap(y, p), x1 = snap(x + w, p), y1 = snap(y + h, p);
        if (x1 - x0 < p || y1 - y0 < p) return;

        int ch = applyAlpha(hi);
        int cl = applyAlpha(lo);

        put(g, x0, y0, x1 - p, y0 + p, ch);
        put(g, x0, y0 + p, x0 + p, y1 - p, ch);
        put(g, x0 + p, y1 - p, x1, y1, cl);
        put(g, x1 - p, y0 + p, x1, y1 - p, cl);
    }

    public static void sunk(double x, double y, double w, double h, int dark, int light) {
        raised(x, y, w, h, dark, light);
    }

    public static void hline(double x, double y, double w, int argb) {
        fill(x, y, w, unit(), argb);
    }

    public static void vline(double x, double y, double h, int argb) {
        fill(x, y, unit(), h, argb);
    }

    public static void dotted(double x, double y, double length, boolean vertical, int argb) {
        dotted(x, y, length, vertical, argb, Integer.MAX_VALUE, 0);
    }

    public static void dotted(double x, double y, double length, boolean vertical, int argb, int maxDots, int restArgb) {
        GuiGraphicsExtractor g = graphics();
        if (g == null) return;

        int p = unit();
        int x0 = snap(x, p), y0 = snap(y, p);
        int start = vertical ? y0 : x0;
        int end = snap((vertical ? y : x) + length, p);
        int cells = (end - start) / p;
        if (cells <= 0) return;

        int dot = applyAlpha(argb);
        int rest = applyAlpha(restArgb);
        int dots = 0;
        int i = 0;

        for (; i < cells; i += 2) {
            if (dots >= maxDots) break;
            int a = start + i * p;
            if (vertical) put(g, x0, a, x0 + p, a + p, dot);
            else put(g, a, y0, a + p, y0 + p, dot);
            dots++;
        }

        if (i < cells && (rest >>> 24) != 0) {
            int a = start + i * p;
            if (vertical) put(g, x0, a, x0 + p, end, rest);
            else put(g, a, y0, end, y0 + p, rest);
        }
    }

    public static void checker(double x, double y, double w, double h, double cell, int a, int b) {
        GuiGraphicsExtractor g = graphics();
        if (g == null) return;

        int p = unit();
        int x0 = snap(x, p), y0 = snap(y, p), x1 = snap(x + w, p), y1 = snap(y + h, p);
        if (x1 <= x0 || y1 <= y0) return;

        int c = Math.max(p, snap(cell, p));
        int ca = applyAlpha(a);
        int cb = applyAlpha(b);
        boolean backed = (ca >>> 24) == 0xFF;

        if (backed) put(g, x0, y0, x1, y1, ca);

        int row = 0;
        for (int cy = y0; cy < y1; cy += c, row++) {
            int ey = Math.min(cy + c, y1);
            int col = 0;
            for (int cx = x0; cx < x1; cx += c, col++) {
                boolean odd = ((row + col) & 1) == 1;
                if (backed && !odd) continue;
                put(g, cx, cy, Math.min(cx + c, x1), ey, odd ? cb : ca);
            }
        }
    }

    public static void vgrad(double x, double y, double w, double h, int top, int bottom) {
        GuiGraphicsExtractor g = graphics();
        if (g == null) return;

        int p = unit();
        int x0 = snap(x, p), y0 = snap(y, p), x1 = snap(x + w, p), y1 = snap(y + h, p);
        if (x1 <= x0 || y1 <= y0) return;

        int ct = applyAlpha(top);
        int cb = applyAlpha(bottom);
        if ((ct >>> 24) == 0 && (cb >>> 24) == 0) return;

        g.fillGradient(x0, y0, x1, y1, ct, cb);
    }

    public static int glyphWidth(PixelGlyph glyph) {
        if (glyph == null) return 0;
        if (glyph.width() > 0) return glyph.width();
        int w = 0;
        if (glyph.rows() != null) for (String row : glyph.rows()) if (row != null) w = Math.max(w, row.length());
        return w;
    }

    public static int glyphHeight(PixelGlyph glyph) {
        if (glyph == null) return 0;
        if (glyph.height() > 0) return glyph.height();
        return glyph.rows() == null ? 0 : glyph.rows().length;
    }

    public static void glyph(PixelGlyph glyph, double x, double y, int unit, int argb, boolean shadow) {
        GuiGraphicsExtractor g = graphics();
        if (g == null || glyph == null) return;

        int c = applyAlpha(argb);
        int a = c >>> 24;
        if (a == 0) return;

        int p = unit();
        int u = Math.max(1, unit);
        int gx = snap(x, p);
        int gy = snap(y, p);

        if (shadow) {
            int sa = Math.round(GLYPH_SHADOW_ALPHA * a / 255f);
            if (sa > 0) runs(g, glyph, gx + u, gy + u, u, Integer.MAX_VALUE, sa << 24);
        }

        runs(g, glyph, gx, gy, u, Integer.MAX_VALUE, c);
    }

    public static void glyphColumns(PixelGlyph glyph, double x, double y, int unit, int cols, int argb) {
        GuiGraphicsExtractor g = graphics();
        if (g == null || glyph == null || cols <= 0) return;

        int c = applyAlpha(argb);
        if ((c >>> 24) == 0) return;

        int p = unit();
        runs(g, glyph, snap(x, p), snap(y, p), Math.max(1, unit), cols, c);
    }

    public static void glyphFit(PixelGlyph glyph, double x, double y, double w, double h, int argb, int quarterTurns) {
        if (glyph == null) return;

        PixelGlyph turned = turn(glyph, quarterTurns);
        int p = unit();
        int base = (int) Math.max(1, Math.floor(Math.min(Math.abs(w), Math.abs(h)) / 8));
        int u = Math.max(p, snap(base, p));

        double gx = Math.min(x, x + w) + (Math.abs(w) - glyphWidth(turned) * u) / 2.0;
        double gy = Math.min(y, y + h) + (Math.abs(h) - glyphHeight(turned) * u) / 2.0;

        glyph(turned, gx, gy, u, argb, false);
    }

    public static PixelGlyph turn(PixelGlyph glyph, int quarterTurns) {
        int q = Math.floorMod(quarterTurns, 4);
        if (glyph == null || q == 0) return glyph;

        PixelGlyph[] cache = TURNED.computeIfAbsent(glyph, k -> new PixelGlyph[4]);
        if (cache[q] == null) {
            PixelGlyph out = glyph;
            for (int i = 0; i < q; i++) out = turnClockwise(out);
            cache[q] = out;
        }
        return cache[q];
    }

    private static PixelGlyph turnClockwise(PixelGlyph glyph) {
        int w = glyphWidth(glyph);
        int h = glyphHeight(glyph);
        String[] rows = new String[w];

        for (int nr = 0; nr < w; nr++) {
            char[] line = new char[h];
            for (int nc = 0; nc < h; nc++) line[nc] = cell(glyph, h - 1 - nc, nr) ? '#' : '.';
            rows[nr] = new String(line);
        }

        return new PixelGlyph(h, w, rows);
    }

    private static boolean cell(PixelGlyph glyph, int row, int col) {
        String[] rows = glyph.rows();
        if (rows == null || row < 0 || row >= rows.length || col < 0) return false;
        String line = rows[row];
        return line != null && col < line.length() && on(line.charAt(col));
    }

    private static boolean on(char ch) {
        return ch != '.' && ch != ' ';
    }

    private static void runs(GuiGraphicsExtractor g, PixelGlyph glyph, int x, int y, int u, int cols, int argb) {
        String[] rows = glyph.rows();
        if (rows == null) return;

        int limit = Math.min(cols, glyphWidth(glyph));
        int height = Math.min(rows.length, glyphHeight(glyph));

        for (int r = 0; r < height; r++) {
            String row = rows[r];
            if (row == null) continue;

            int n = Math.min(limit, row.length());
            int start = -1;
            for (int i = 0; i <= n; i++) {
                boolean lit = i < n && on(row.charAt(i));
                if (lit) {
                    if (start < 0) start = i;
                } else if (start >= 0) {
                    g.fill(x + start * u, y + r * u, x + i * u, y + (r + 1) * u, argb);
                    start = -1;
                }
            }
        }
    }

    public static void runner(double x, double y, double w, double h, int unit, long timeMs, int speed, int[] argbs) {
        GuiGraphicsExtractor g = graphics();
        if (g == null || argbs == null || argbs.length == 0) return;

        int p = unit();
        int u = Math.max(1, unit);
        int x0 = snap(x, p), y0 = snap(y, p), x1 = snap(x + w, p), y1 = snap(y + h, p);
        int cw = (x1 - x0) / u;
        int ch = (y1 - y0) / u;
        if (cw < 1 || ch < 1) return;

        int a = cw - 1;
        int b = ch - 1;
        int per = cw == 1 || ch == 1 ? Math.max(cw, ch) : 2 * (a + b);
        long head = Math.floorMod(Math.floorDiv(timeMs * speed, 1000L), (long) per);

        for (int k = 0; k < argbs.length && k < per; k++) {
            int pos = (int) Math.floorMod(head - k, (long) per);
            int cx, cy;

            if (ch == 1) {
                cx = pos;
                cy = 0;
            } else if (cw == 1) {
                cx = 0;
                cy = pos;
            } else if (pos < a) {
                cx = pos;
                cy = 0;
            } else if (pos < a + b) {
                cx = a;
                cy = pos - a;
            } else if (pos < 2 * a + b) {
                cx = a - (pos - a - b);
                cy = b;
            } else {
                cx = 0;
                cy = b - (pos - 2 * a - b);
            }

            int px = x0 + cx * u;
            int py = y0 + cy * u;
            put(g, px, py, px + u, py + u, applyAlpha(argbs[k]));
        }
    }

    public static void text(Component text, double x, double y, int argb, boolean shadow) {
        GuiGraphicsExtractor g = graphics();
        if (g == null || text == null) return;

        int c = applyAlpha(argb);
        if ((c >>> 24) == 0) return;

        int p = unit();
        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.translate(snap(x, p), snap(y, p));
        pose.scale(p, p);
        g.text(mc.font, text, 0, 0, c, shadow);
        pose.popMatrix();
    }

    public static void text(String text, double x, double y, int argb, boolean shadow) {
        GuiGraphicsExtractor g = graphics();
        if (g == null || text == null || text.isEmpty()) return;

        int c = applyAlpha(argb);
        if ((c >>> 24) == 0) return;

        int p = unit();
        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.translate(snap(x, p), snap(y, p));
        pose.scale(p, p);
        g.text(mc.font, text, 0, 0, c, shadow);
        pose.popMatrix();
    }

    public static void text(RichText text, double x, double y, int argb, boolean shadow) {
        if (text == null) return;
        text(component(text), x, y, argb, shadow);
    }

    public static int width(Component text) {
        return width(text, unit());
    }

    public static int width(Component text, int p) {
        return text == null ? 0 : mc.font.width(text) * p;
    }

    public static int width(String text) {
        return text == null || text.isEmpty() ? 0 : mc.font.width(text) * unit();
    }

    public static int width(RichText text) {
        return text == null ? 0 : width(component(text));
    }

    public static int textHeight() {
        return mc.font.lineHeight * unit();
    }

    public static Component component(RichText text) {
        return component(text, false);
    }

    public static Component component(RichText text, boolean underline) {
        MutableComponent out = Component.empty();
        if (text == null) return out;

        for (RichTextSegment segment : text.getSegments()) {
            String s = segment.getText();
            if (s == null || s.isEmpty()) continue;

            Style style = Style.EMPTY;
            if (segment.getStyle() == FontStyle.BOLD) style = style.withBold(true);
            else if (segment.getStyle() == FontStyle.ITALIC) style = style.withItalic(true);
            if (underline) style = style.withUnderlined(true);

            out.append(Component.literal(s).setStyle(style));
        }

        return out;
    }

    public static void sprite(Identifier id, double x, double y, int wT, int hT) {
        sprite(id, x, y, wT, hT, 0xFFFFFFFF);
    }

    public static void sprite(Identifier id, double x, double y, int wT, int hT, int argb) {
        GuiGraphicsExtractor g = graphics();
        if (g == null || id == null || wT <= 0 || hT <= 0) return;

        int c = applyAlpha(argb);
        if ((c >>> 24) == 0) return;

        int p = unit();
        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.translate(snap(x, p), snap(y, p));
        pose.scale(p, p);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, id, 0, 0, wT, hT, c);
        pose.popMatrix();
    }

    public static void tiled(Identifier texture, double x, double y, int wT, int hT, int texW, int texH) {
        tiled(texture, x, y, wT, hT, 0, 0, texW, texH, 0xFFFFFFFF);
    }

    public static void tiled(Identifier texture, double x, double y, int wT, int hT, float u, float v, int texW, int texH, int argb) {
        GuiGraphicsExtractor g = graphics();
        if (g == null || texture == null || wT <= 0 || hT <= 0 || texW <= 0 || texH <= 0) return;

        int c = applyAlpha(argb);
        if ((c >>> 24) == 0) return;

        int p = unit();
        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.translate(snap(x, p), snap(y, p));
        pose.scale(p, p);
        g.blit(RenderPipelines.GUI_TEXTURED, texture, 0, 0, u, v, wT, hT, texW, texH, c);
        pose.popMatrix();
    }

    public static void menuBackground(double x, double y, int wT, int hT) {
        menuBackground(mc.level == null ? Screen.MENU_BACKGROUND : INWORLD_MENU_BACKGROUND, x, y, wT, hT);
    }

    public static void menuBackground(Identifier texture, double x, double y, int wT, int hT) {
        GuiGraphicsExtractor g = graphics();
        if (g == null || texture == null || wT <= 0 || hT <= 0) return;

        int p = unit();
        int sx = snap(x, p);
        int sy = snap(y, p);
        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.translate(sx, sy);
        pose.scale(p, p);
        Screen.extractMenuBackgroundTexture(g, texture, 0, 0, sx / (float) p, sy / (float) p, wT, hT);
        pose.popMatrix();
    }

    public static void item(ItemStack stack, double x, double y) {
        GuiGraphicsExtractor g = graphics();
        if (g == null || stack == null || stack.isEmpty()) return;

        int p = unit();
        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.translate(snap(x, p), snap(y, p));
        pose.scale(p, p);
        g.item(stack, 0, 0);
        pose.popMatrix();
    }

    public static void tooltipBackground(double x, double y, int wT, int hT) {
        GuiGraphicsExtractor g = graphics();
        if (g == null || wT < 0 || hT < 0) return;

        int p = unit();
        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.translate(snap(x, p), snap(y, p));
        pose.scale(p, p);
        TooltipRenderUtil.extractTooltipBackground(g, 0, 0, wT, hT, null);
        pose.popMatrix();
    }

    public static void highlight(double x0, double y0, double x1, double y1) {
        highlight(x0, y0, x1, y1, true);
    }

    public static void highlight(double x0, double y0, double x1, double y1, boolean invert) {
        GuiGraphicsExtractor g = graphics();
        if (g == null) return;

        int p = unit();
        int l = snap(Math.min(x0, x1), p), r = snap(Math.max(x0, x1), p);
        int t = snap(Math.min(y0, y1), p), b = snap(Math.max(y0, y1), p);
        if (r <= l || b <= t) return;

        g.textHighlight(l, t, r, b, invert);
    }

    public static void quad4(double x, double y, double w, double h, int tl, int tr, int br, int bl) {
        GuiGraphicsExtractor g = graphics();
        if (g == null) return;

        if (w < 0) {
            x += w;
            w = -w;
            int t = tl; tl = tr; tr = t;
            t = bl; bl = br; br = t;
        }

        if (h < 0) {
            y += h;
            h = -h;
            int t = tl; tl = bl; bl = t;
            t = tr; tr = br; br = t;
        }

        int p = unit();
        int x0 = snap(x, p), y0 = snap(y, p), x1 = snap(x + w, p), y1 = snap(y + h, p);
        if (x1 <= x0 || y1 <= y0) return;

        int ctl = applyAlpha(tl), ctr = applyAlpha(tr), cbr = applyAlpha(br), cbl = applyAlpha(bl);
        if (((ctl | ctr | cbr | cbl) >>> 24) == 0) return;

        if (ctl == ctr && ctr == cbr && cbr == cbl) {
            put(g, x0, y0, x1, y1, ctl);
            return;
        }

        if (ctl == ctr && cbl == cbr) {
            g.fillGradient(x0, y0, x1, y1, ctl, cbl);
            return;
        }

        submit(g, Quad4State.rect(g, x0, y0, x1, y1, ctl, ctr, cbr, cbl));
    }

    public static void triangle(double x1, double y1, double x2, double y2, double x3, double y3, int argb) {
        GuiGraphicsExtractor g = graphics();
        if (g == null) return;

        int c = applyAlpha(argb);
        if ((c >>> 24) == 0) return;

        int p = unit();
        submit(g, Quad4State.triangle(g, snap(x1, p), snap(y1, p), snap(x2, p), snap(y2, p), snap(x3, p), snap(y3, p), c));
    }

    public static void texBlit(GpuTextureView view, GpuSampler sampler, double x, double y, double w, double h,
                               float u0, float v0, float u1, float v1, double rotation, int argb) {
        GuiGraphicsExtractor g = graphics();
        if (g == null || view == null || sampler == null) return;

        int c = applyAlpha(argb);
        if ((c >>> 24) == 0) return;

        if (w < 0) {
            x += w;
            w = -w;
            float t = u0; u0 = u1; u1 = t;
        }

        if (h < 0) {
            y += h;
            h = -h;
            float t = v0; v0 = v1; v1 = t;
        }

        int p = unit();
        int x0 = snap(x, p), y0 = snap(y, p), x1 = snap(x + w, p), y1 = snap(y + h, p);
        if (x1 <= x0 || y1 <= y0) return;

        submit(g, TexBlitState.of(g, view, sampler, x0, y0, x1 - x0, y1 - y0, u0, v0, u1, v1, rotation, c));
    }

    public static GpuSampler nearestSampler() {
        return RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
    }

    public static void missedRoute(String site) {
        if (missLogged) return;
        missLogged = true;
        HontunGui.LOG.warn("[Hontun] PIXEL ClickGUI pipeline reached {} directly; a primitive bypassed PixelRouter", site);
    }

    private static void submit(GuiGraphicsExtractor g, Quad4State state) {
        if (state.visible()) g.guiRenderState.addGuiElement(state);
    }

    private static void submit(GuiGraphicsExtractor g, TexBlitState state) {
        if (state.visible()) g.guiRenderState.addGuiElement(state);
    }

    private static void put(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int argb) {
        if (x1 <= x0 || y1 <= y0 || (argb >>> 24) == 0) return;
        g.fill(x0, y0, x1, y1, argb);
    }
}
