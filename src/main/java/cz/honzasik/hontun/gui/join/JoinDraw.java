package cz.honzasik.hontun.gui.join;

import cz.honzasik.hontun.gui.render.RoundedGui;
import cz.honzasik.hontun.gui.widget.HontunRound;
import cz.honzasik.hontun.utils.HontunFont;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.progress.ChunkLoadStatusView;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class JoinDraw {
    public static final double TAU = Math.PI * 2.0;
    public static final int WHITE = 0xFFFFFF;
    public static final int SOFT = 0xE6E9EC;
    public static final int LIGHT = 0xC8CDD2;
    public static final int DIM = 0x9096A0;
    public static final int MUTED = 0x5A5D62;

    private static final Map<String, String> FIT = new HashMap<>();

    private JoinDraw() {}

    public static float width(Font font, String s, float scale) {
        return s == null ? 0f : font.width(HontunFont.text(s)) * scale;
    }

    public static void text(GuiGraphicsExtractor g, Font font, String s, float x, float y, float scale, int argb) {
        if ((argb >>> 24) < 5 || s == null || s.isEmpty()) return;
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        g.text(font, HontunFont.text(s), 0, 0, argb, false);
        g.pose().popMatrix();
    }

    public static void centered(GuiGraphicsExtractor g, Font font, String s, float cx, float y, float scale, int argb) {
        text(g, font, s, cx - width(font, s, scale) / 2f, y, scale, argb);
    }

    public static void right(GuiGraphicsExtractor g, Font font, String s, float rx, float y, float scale, int argb) {
        text(g, font, s, rx - width(font, s, scale), y, scale, argb);
    }

    public static String fit(Font font, String s, float maxW, float scale) {
        if (s == null) return "";
        float max = maxW / scale;
        String key = s + '\u0000' + Math.round(max);
        String cached = FIT.get(key);
        if (cached != null) return cached;
        String out = s;
        if (font.width(HontunFont.text(s)) > max) {
            int lo = 0, hi = s.length();
            while (lo < hi) {
                int mid = (lo + hi + 1) >>> 1;
                if (font.width(HontunFont.text(s.substring(0, mid) + "...")) <= max) lo = mid;
                else hi = mid - 1;
            }
            out = s.substring(0, lo).stripTrailing() + "...";
        }
        if (FIT.size() > 64) FIT.clear();
        FIT.put(key, out);
        return out;
    }

    public static void crossfade(GuiGraphicsExtractor g, JoinState s, String cur, String prev, long changedAt,
                                 float cx, float y, float scale, int rgb, float alpha, float maxW) {
        long d = s.now - changedAt;
        if (prev != null && changedAt != 0L && d < 160) {
            float u1 = cl(d / 160f);
            centered(g, s.font, fit(s.font, prev, maxW, scale), cx, y - 3f * u1, scale,
                    argb(Math.round(255 * alpha * (1f - u1 * u1)), rgb));
        }
        float u2 = prev == null || changedAt == 0L ? 1f : eoc(cl((d - 60) / 260f));
        centered(g, s.font, fit(s.font, cur, maxW, scale), cx, y + 4f * (1f - u2), scale,
                argb(Math.round(255 * alpha * u2), rgb));
    }

    public static void crossfadeLeft(GuiGraphicsExtractor g, JoinState s, String cur, String prev, long changedAt,
                                     float x, float y, float scale, int rgb, float alpha, float maxW) {
        long d = s.now - changedAt;
        if (prev != null && changedAt != 0L && d < 160) {
            float u1 = cl(d / 160f);
            text(g, s.font, fit(s.font, prev, maxW, scale), x, y - 3f * u1, scale,
                    argb(Math.round(255 * alpha * (1f - u1 * u1)), rgb));
        }
        float u2 = prev == null || changedAt == 0L ? 1f : eoc(cl((d - 60) / 260f));
        text(g, s.font, fit(s.font, cur, maxW, scale), x, y + 4f * (1f - u2), scale,
                argb(Math.round(255 * alpha * u2), rgb));
    }

    public static void statusLine(GuiGraphicsExtractor g, JoinState s, float cx, float y, float scale, float alpha, float maxW) {
        float sepW = 11f * scale;
        float boxW = width(s.font, "00.0 s", scale);
        String cur = fit(s.font, s.status, maxW - sepW - boxW, scale);
        float wCur = width(s.font, cur, scale);
        float x0 = cx - (wCur + sepW + boxW) / 2f;
        long d = s.now - s.statusAt;
        if (s.prevStatus != null && s.statusAt != 0L && d < 160) {
            float u1 = cl(d / 160f);
            String prev = fit(s.font, s.prevStatus, maxW - sepW - boxW, scale);
            float px = cx - (width(s.font, prev, scale) + sepW + boxW) / 2f;
            text(g, s.font, prev, px, y - 3f * u1, scale, argb(Math.round(255 * alpha * (1f - u1 * u1)), DIM));
        }
        float u2 = s.prevStatus == null || s.statusAt == 0L ? 1f : eoc(cl((d - 60) / 260f));
        text(g, s.font, cur, x0, y + 4f * (1f - u2), scale, argb(Math.round(255 * alpha * u2), DIM));
        float fade = eoc(cl((s.elapsedMs - 800) / 300f)) * alpha;
        if (fade > 0.02f) {
            dot(g, x0 + wCur + sepW / 2f, y + 4f * scale, 0.9f * scale, argb(Math.round(0x80 * fade), DIM));
            text(g, s.font, formatElapsed(s.elapsedMs), x0 + wCur + sepW, y, scale, argb(Math.round(0xA0 * fade), DIM));
        }
    }

    public static void line(GuiGraphicsExtractor g, float x0, float y0, float x1, float y1, float ht, int argb) {
        if (Math.abs(x1 - x0) + Math.abs(y1 - y0) < 0.2f || (argb >>> 24) == 0) return;
        HontunRound.stroke(g, x0, y0, x1, y1, ht, argb);
    }

    public static void dot(GuiGraphicsExtractor g, float x, float y, float r, int argb) {
        if ((argb >>> 24) == 0) return;
        HontunRound.dot(g, x, y, r, argb);
    }

    public static void glow(GuiGraphicsExtractor g, float x, float y, float r, int argb) {
        if ((argb >>> 24) == 0) return;
        RoundedGui.circle(g, x, y, r, r, argb);
    }

    public static void ring(GuiGraphicsExtractor g, float x, float y, float r, float th, int argb) {
        if ((argb >>> 24) == 0) return;
        if (RoundedGui.ring(g, x, y, r, th, argb)) return;
        HontunRound.ring(g, x, y, r, th * 0.5f, argb);
    }

    public static void pill(GuiGraphicsExtractor g, float x, float y, float w, float h, float r, int argb) {
        if ((argb >>> 24) == 0 || w <= 0f || h <= 0f) return;
        if (RoundedGui.fill(g, x, y, w, h, r, 0f, argb)) return;
        HontunRound.fill(g, Math.round(x), Math.round(y), Math.round(w), Math.round(h), Math.round(r), argb);
    }

    public static void outline(GuiGraphicsExtractor g, float x, float y, float w, float h, float r, float th, int argb) {
        if ((argb >>> 24) == 0 || w <= 0f || h <= 0f) return;
        if (RoundedGui.outline(g, x, y, w, h, r, th, argb)) return;
        int ix = Math.round(x), iy = Math.round(y), iw = Math.round(w), ih = Math.round(h);
        g.fill(ix, iy, ix + iw, iy + 1, argb);
        g.fill(ix, iy + ih - 1, ix + iw, iy + ih, argb);
        g.fill(ix, iy + 1, ix + 1, iy + ih - 1, argb);
        g.fill(ix + iw - 1, iy + 1, ix + iw, iy + ih - 1, argb);
    }

    public static void soft(GuiGraphicsExtractor g, float x, float y, float w, float h, float r, float feather, int argb) {
        if ((argb >>> 24) == 0 || w <= 0f || h <= 0f) return;
        RoundedGui.fill(g, x, y, w, h, r, feather, argb);
    }

    public static void arc(GuiGraphicsExtractor g, float cx, float cy, float r, double from, double span, float ht, int argb) {
        if (span <= 0.01 || (argb >>> 24) == 0) return;
        int n = Math.max(2, (int) Math.ceil(span / 0.14));
        float px = cx + (float) Math.cos(from) * r, py = cy + (float) Math.sin(from) * r;
        for (int i = 1; i <= n; i++) {
            double a = from + i * span / n;
            float qx = cx + (float) Math.cos(a) * r, qy = cy + (float) Math.sin(a) * r;
            line(g, px, py, qx, qy, ht, argb);
            px = qx;
            py = qy;
        }
    }

    public static void check(GuiGraphicsExtractor g, float cx, float cy, float size, float ht, int argb) {
        float x0 = cx - 0.42f * size, y0 = cy + 0.02f * size;
        float x1 = cx - 0.12f * size, y1 = cy + 0.30f * size;
        float x2 = cx + 0.42f * size, y2 = cy - 0.30f * size;
        line(g, x0, y0, x1, y1, ht, argb);
        line(g, x1, y1, x2, y2, ht, argb);
    }

    public static void spinner(GuiGraphicsExtractor g, JoinState s, float cx, float cy, float r, float ringTh, float ht, float alpha) {
        if (!s.sdf) {
            float head = frac(s.t / 1.05) * 12f;
            for (int i = 0; i < 12; i++) {
                float d = (head - i + 12f) % 12f;
                int a = Math.round((0x30 + 0xCF * (float) Math.pow(1f - d / 12f, 1.7f)) * alpha);
                double ang = -Math.PI / 2 + i * Math.PI / 6;
                dot(g, cx + (float) Math.cos(ang) * r, cy + (float) Math.sin(ang) * r, 1.2f, argb(a, WHITE));
            }
            return;
        }
        ring(g, cx, cy, r, ringTh, argb(Math.round(0x24 * alpha), WHITE));
        double theta = -Math.PI / 2 + TAU * frac(s.t / 1.4);
        double span = (0.95 + 0.55 * pulse(s.t, 2.8)) * s.ent(120);
        if (span <= 0.02) return;
        arc(g, cx, cy, r, theta - span, span, ht, argb(Math.round(255 * alpha), WHITE));
        for (int j = 1; j <= 7; j++) {
            double phi = theta - span - 0.16 * j;
            int a = Math.round(0xD0 * (float) Math.pow(1f - j / 8f, 1.5f) * alpha);
            RoundedGui.circle(g, cx + (float) Math.cos(phi) * r, cy + (float) Math.sin(phi) * r, ht * (1f - 0.1f * j), 0f, argb(a, WHITE));
        }
        glow(g, cx + (float) Math.cos(theta) * r, cy + (float) Math.sin(theta) * r, 0.3f * r, argb(Math.round(0x40 * alpha), WHITE));
    }

    public static void cloud(GuiGraphicsExtractor g, JoinState s, float cx, float cy, float size, int rgb, float alpha) {
        float wc = size;
        float ox = cx + 0.04f * wc * (float) Math.sin(TAU * frac(s.t / 6.4));
        float oy = cy + 0.05f * wc * (float) Math.sin(TAU * frac(s.t / 3.2)) + 0.02f * wc;
        int c = argb(Math.round(255 * alpha), rgb);
        pill(g, ox - 0.45f * wc, oy - 0.02f * wc, 0.90f * wc, 0.26f * wc, 0.13f * wc, c);
        if (!s.sdf) return;
        RoundedGui.circle(g, ox - 0.24f * wc, oy + 0.04f * wc, 0.19f * wc, 0f, c);
        RoundedGui.circle(g, ox + 0.03f * wc, oy - 0.08f * wc, 0.27f * wc, 0f, c);
        RoundedGui.circle(g, ox + 0.29f * wc, oy + 0.07f * wc, 0.17f * wc, 0f, c);
    }

    public static void ripple(GuiGraphicsExtractor g, JoinState s, float cx, float cy, float r) {
        if (s.advancedAt == 0L || !s.sdf) return;
        float u = s.since(s.advancedAt, 700f);
        if (u >= 1f) return;
        ring(g, cx, cy, r + 2f + 0.9f * r * eoc(u), 1.3f - 0.8f * u, argb(Math.round(0x48 * (1f - u) * (1f - u)), WHITE));
    }

    public static void bar(GuiGraphicsExtractor g, float x, float y, float w, float h, float frac, float alpha, boolean head) {
        float fw = Math.max(h, w * cl(frac));
        pill(g, x, y - h / 2f, w, h, h / 2f, argb(Math.round(0x24 * alpha), WHITE));
        pill(g, x, y - h / 2f, fw, h, h / 2f, argb(Math.round(0xE6 * alpha), WHITE));
        if (head) glow(g, x + fw, y, 2.6f * h, argb(Math.round(0x40 * alpha), WHITE));
    }

    public static float chunkWell(GuiGraphicsExtractor g, JoinState s, float cx, float top, float maxSize) {
        ChunkLoadStatusView view = s.view;
        if (view == null) return 0f;
        int d = view.radius() * 2 + 1;
        int pitch = Math.min(5, (int) Math.floor((maxSize - 10f) / d));
        if (pitch < 2) return 0f;
        int gap = pitch >= 3 ? 1 : 0;
        int cell = pitch - gap;
        int gw = d * pitch - gap;
        int gx = Math.round(cx - gw / 2f);
        int gy = Math.round(top + 5f);
        pill(g, gx - 5f, gy - 5f, gw + 10f, gw + 10f, 5f, 0xC8000000);
        outline(g, gx - 5f, gy - 5f, gw + 10f, gw + 10f, 5f, 1f, 0x34FFFFFF);
        int full = Math.max(1, ChunkStatus.FULL.getIndex());
        for (int x = 0; x < d; x++) {
            for (int z = 0; z < d; z++) {
                ChunkStatus st = view.get(x, z);
                int col;
                if (st == null || st == ChunkStatus.EMPTY) col = 0xFF151517;
                else if (st == ChunkStatus.FULL) col = 0xFFE6E9EC;
                else col = 0xFF000000 | lerpRgb(0x2A2A2C, DIM, st.getIndex() / (float) full);
                int x0 = gx + x * pitch, y0 = gy + z * pitch;
                g.fill(x0, y0, x0 + cell, y0 + cell, col);
            }
        }
        int c = d / 2;
        if (s.sdf) outline(g, gx + c * pitch - 1.5f, gy + c * pitch - 1.5f, cell + 3f, cell + 3f, 1.5f, 0.75f, 0xC8FFFFFF);
        return gw + 10f;
    }

    public static void ghost(GuiGraphicsExtractor g, JoinState s) {
        if (s.ghostAt == 0L || s.cancelRect == null || s.mode != JoinView.Mode.JOIN) return;
        float u = s.since(s.ghostAt, 220f);
        if (u >= 1f) return;
        float k = 1f - u * u;
        int[] r = s.cancelRect;
        pill(g, r[0], r[1], r[2], r[3], Math.min(8, Math.min(r[2], r[3]) / 2f), argb(Math.round(0x6E * k), 0x000000));
        centered(g, s.font, Component.translatable("gui.cancel").getString(), r[0] + r[2] / 2f, r[1] + (r[3] - 8) / 2f, 1f,
                argb(Math.round(255 * k), WHITE));
    }

    public static String formatElapsed(long ms) {
        if (ms < 10_000L) return String.format(Locale.ROOT, "%.1f s", ms / 1000.0);
        if (ms < 60_000L) return (ms / 1000L) + " s";
        long sec = ms / 1000L;
        return String.format(Locale.ROOT, "%d:%02d", sec / 60L, sec % 60L);
    }

    public static String formatStage(long ms, boolean compact) {
        if (ms < 1000L) return compact ? String.valueOf(ms) : ms + " ms";
        return String.format(Locale.ROOT, "%.1f s", ms / 1000.0);
    }

    public static float frac(double v) {
        return (float) (v - Math.floor(v));
    }

    public static float pulse(double t, double period) {
        return 0.5f - 0.5f * (float) Math.cos(TAU * frac(t / period));
    }

    public static float cl(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }

    public static float eoc(float x) {
        float u = 1f - x;
        return 1f - u * u * u;
    }

    public static float eob(float x) {
        float u = x - 1f;
        return 1f + 2.2f * u * u * u + 1.2f * u * u;
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static int lerpRgb(int a, int b, float t) {
        int r = Math.round(lerp((a >> 16) & 0xFF, (b >> 16) & 0xFF, t));
        int gg = Math.round(lerp((a >> 8) & 0xFF, (b >> 8) & 0xFF, t));
        int bl = Math.round(lerp(a & 0xFF, b & 0xFF, t));
        return (r << 16) | (gg << 8) | bl;
    }

    public static int argb(int a, int rgb) {
        return (Math.max(0, Math.min(255, a)) << 24) | (rgb & 0xFFFFFF);
    }
}
