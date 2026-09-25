package cz.honzasik.hontun.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class HontunRound {
    private static final Identifier SHADOW = Identifier.fromNamespaceAndPath("hontun", "textures/gui/smog/shadow.png");
    private static final int STEX = 192, SCORNER = 68;

    private HontunRound() {}

    public static void fill(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius, int argb) {
        if (w <= 0 || h <= 0) return;
        if (cz.honzasik.hontun.gui.render.RoundedGui.fill(g, x, y, w, h, radius, argb)) return;
        int r = Math.max(0, Math.min(radius, Math.min(w, h) / 2));
        if (r == 0) { g.fill(x, y, x + w, y + h, argb); return; }

        g.fill(x + r, y, x + w - r, y + h, argb);
        g.fill(x, y + r, x + r, y + h - r, argb);
        g.fill(x + w - r, y + r, x + w, y + h - r, argb);

        aaCorner(g, x + r,     y + r,     r, argb, -1, -1);
        aaCorner(g, x + w - r, y + r,     r, argb, +1, -1);
        aaCorner(g, x + r,     y + h - r, r, argb, -1, +1);
        aaCorner(g, x + w - r, y + h - r, r, argb, +1, +1);
    }

    public static boolean smoothDots() {
        return cz.honzasik.hontun.utils.HontunTheme.smog() && cz.honzasik.hontun.gui.render.RoundedGui.available();
    }

    public static void dot(GuiGraphicsExtractor g, float cx, float cy, float r, int argb) {
        if (cz.honzasik.hontun.gui.render.RoundedGui.circle(g, cx, cy, r, 0f, argb)) return;
        int d = Math.max(1, Math.round(r * 2f));
        int x = Math.round(cx - r), y = Math.round(cy - r);
        g.fill(x, y, x + d, y + d, argb);
    }

    public static void card(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius, int fillArgb, int borderArgb) {
        cardThick(g, x, y, w, h, radius, fillArgb, borderArgb, 1);
    }

    public static void cardThick(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius,
                                 int fillArgb, int borderArgb, int t) {
        if (w <= 0 || h <= 0) return;
        if (cz.honzasik.hontun.gui.render.RoundedGui.available()) {
            cz.honzasik.hontun.gui.render.RoundedGui.fill(g, (float) x, (float) y, (float) w, (float) h, (float) radius, 0f, fillArgb);
            cz.honzasik.hontun.gui.render.RoundedGui.outline(g, (float) x, (float) y, (float) w, (float) h, (float) radius, (float) t, borderArgb);
            return;
        }
        fill(g, x, y, w, h, radius, borderArgb);
        fill(g, x + t, y + t, w - 2 * t, h - 2 * t, Math.max(0, radius - t), fillArgb);
    }

    public static void stroke(GuiGraphicsExtractor g, float x0, float y0, float x1, float y1,
                              float th, int argb) {
        int baseA = (argb >>> 24) & 0xFF, rgb = argb & 0xFFFFFF;
        if (baseA == 0) return;
        if (cz.honzasik.hontun.gui.render.RoundedGui.capsule(g, x0, y0, x1, y1, th, argb)) return;
        int minX = (int) Math.floor(Math.min(x0, x1) - th - 1);
        int maxX = (int) Math.ceil(Math.max(x0, x1) + th + 1);
        int minY = (int) Math.floor(Math.min(y0, y1) - th - 1);
        int maxY = (int) Math.ceil(Math.max(y0, y1) + th + 1);
        float dx = x1 - x0, dy = y1 - y0;
        float len2 = dx * dx + dy * dy;
        for (int py = minY; py < maxY; py++) {
            for (int px = minX; px < maxX; px++) {
                float pcx = px + 0.5f, pcy = py + 0.5f;
                float t = len2 <= 0 ? 0 : ((pcx - x0) * dx + (pcy - y0) * dy) / len2;
                t = Math.max(0f, Math.min(1f, t));
                float qx = x0 + t * dx, qy = y0 + t * dy;
                float d = (float) Math.sqrt((pcx - qx) * (pcx - qx) + (pcy - qy) * (pcy - qy));
                float cov = Math.max(0f, Math.min(1f, th + 0.5f - d));
                if (cov <= 0.02f) continue;
                int a = Math.round(baseA * cov);
                if (a > 0) g.fill(px, py, px + 1, py + 1, (a << 24) | rgb);
            }
        }
    }

    public static void ring(GuiGraphicsExtractor g, float cx, float cy, float radius, float th, int argb) {
        int baseA = (argb >>> 24) & 0xFF, rgb = argb & 0xFFFFFF;
        if (baseA == 0) return;
        if (cz.honzasik.hontun.gui.render.RoundedGui.ring(g, cx, cy, radius, th * 2f, argb)) return;
        int minX = (int) Math.floor(cx - radius - th - 1);
        int maxX = (int) Math.ceil(cx + radius + th + 1);
        int minY = (int) Math.floor(cy - radius - th - 1);
        int maxY = (int) Math.ceil(cy + radius + th + 1);
        for (int py = minY; py < maxY; py++) {
            for (int px = minX; px < maxX; px++) {
                float pcx = px + 0.5f, pcy = py + 0.5f;
                float d = (float) Math.sqrt((pcx - cx) * (pcx - cx) + (pcy - cy) * (pcy - cy));
                float cov = Math.max(0f, Math.min(1f, th + 0.5f - Math.abs(d - radius)));
                if (cov <= 0.02f) continue;
                int a = Math.round(baseA * cov);
                if (a > 0) g.fill(px, py, px + 1, py + 1, (a << 24) | rgb);
            }
        }
    }

    private static void aaCorner(GuiGraphicsExtractor g, int cx, int cy, int r, int argb, int sx, int sy) {
        int baseA = (argb >>> 24) & 0xFF;
        int rgb = argb & 0xFFFFFF;
        if (baseA == 0 || r <= 0) return;
        double rr = (double) r * r;

        for (int j = 0; j < r; j++) {
            int py = sy < 0 ? cy - 1 - j : cy + j;
            double dyv = (py + 0.5) - cy;
            double val = rr - dyv * dyv;
            if (val <= 0) continue;
            double hx = Math.sqrt(val);

            if (sx < 0) {
                double xEdge = cx - hx;
                int solidStart = (int) Math.ceil(xEdge);
                if (solidStart < cx) g.fill(solidStart, py, cx, py + 1, argb);
                double cov = solidStart - xEdge;
                if (cov > 0.03) {
                    int a = (int) Math.round(baseA * Math.min(1.0, cov));
                    if (a > 0) g.fill(solidStart - 1, py, solidStart, py + 1, (a << 24) | rgb);
                }
            } else {
                double xEdge = cx + hx;
                int solidEnd = (int) Math.floor(xEdge);
                if (solidEnd > cx) g.fill(cx, py, solidEnd, py + 1, argb);
                double cov = xEdge - solidEnd;
                if (cov > 0.03) {
                    int a = (int) Math.round(baseA * Math.min(1.0, cov));
                    if (a > 0) g.fill(solidEnd, py, solidEnd + 1, py + 1, (a << 24) | rgb);
                }
            }
        }
    }

    public static void shadow(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius,
                              int spread, int offsetY, int argb) {
        int sx = x - spread, sy = y - spread + offsetY;
        int sw = w + spread * 2, sh = h + spread * 2;
        nine(g, SHADOW, STEX, SCORNER, sx, sy, sw, sh, radius + spread, argb);
    }

    public static void glow(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius,
                            int spread, int argb) {
        shadow(g, x, y, w, h, radius, spread, 0, argb);
    }

    private static void nine(GuiGraphicsExtractor g, Identifier tex, int texSize, int corner,
                             int x, int y, int w, int h, int radius, int argb) {
        if (w <= 0 || h <= 0) return;
        int r = Math.max(0, Math.min(radius, Math.min(w, h) / 2));
        int c = corner;
        if (r == 0) { blit(g, tex, x, y, c, c, w, h, 1, 1, texSize, argb); return; }
        int rx = x + w - r, ry = y + h - r;
        int mw = w - 2 * r, mh = h - 2 * r;
        int fe = texSize - 2 * c;
        blit(g, tex, x,  y,  0,           0,           r, r, c, c, texSize, argb);
        blit(g, tex, rx, y,  texSize - c, 0,           r, r, c, c, texSize, argb);
        blit(g, tex, x,  ry, 0,           texSize - c, r, r, c, c, texSize, argb);
        blit(g, tex, rx, ry, texSize - c, texSize - c, r, r, c, c, texSize, argb);
        if (mw > 0) {
            blit(g, tex, x + r, y,  c, 0,           mw, r, fe, c, texSize, argb);
            blit(g, tex, x + r, ry, c, texSize - c, mw, r, fe, c, texSize, argb);
        }
        if (mh > 0) {
            blit(g, tex, x,  y + r, 0,           c, r, mh, c, fe, texSize, argb);
            blit(g, tex, rx, y + r, texSize - c, c, r, mh, c, fe, texSize, argb);
        }
        if (mw > 0 && mh > 0) blit(g, tex, x + r, y + r, c, c, mw, mh, fe, fe, texSize, argb);
    }

    private static void blit(GuiGraphicsExtractor g, Identifier tex, int x, int y, int u, int v,
                             int dstW, int dstH, int srcW, int srcH, int texSize, int argb) {
        g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, (float) u, (float) v,
                dstW, dstH, srcW, srcH, texSize, texSize, argb);
    }
}
