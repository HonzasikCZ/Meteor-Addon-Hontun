package cz.honzasik.hontun.gui.join;

import cz.honzasik.hontun.gui.widget.HontunCards;
import cz.honzasik.hontun.gui.widget.HontunShapes;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.server.level.progress.ChunkLoadStatusView;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import static cz.honzasik.hontun.gui.join.JoinDraw.*;

public abstract class JoinSkin {
    private static final JoinSkin SMOG = new Smog();
    private static final JoinSkin FLAT = new Flat();
    private static final JoinSkin CHAMFER = new Chamfer();
    private static final JoinSkin PIXEL = new Pixel();

    public static JoinSkin current() {
        if (HontunTheme.smog()) return SMOG;
        if (HontunTheme.modern2()) return CHAMFER;
        if (HontunTheme.modern1()) return FLAT;
        return PIXEL;
    }

    public abstract boolean pixel();

    public int title() { return HontunTheme.textLight(); }
    public int light() { return HontunTheme.subtext1(); }
    public int dim() { return HontunTheme.textDim(); }
    public int muted() { return HontunTheme.overlay1(); }
    public int accent() { return HontunTheme.accent(); }

    public abstract void panel(GuiGraphicsExtractor g, float x, float y, float w, float h, int tier);

    public void buttonWell(GuiGraphicsExtractor g, float x, float y, float w, float h, float k) {}

    public abstract void rule(GuiGraphicsExtractor g, float x0, float x1, float y, float e);

    public abstract void highlight(GuiGraphicsExtractor g, float x, float y, float w, float h, float a);

    public abstract void link(GuiGraphicsExtractor g, float x, float y0, float y1, float fill, boolean bright, float e);

    public abstract void marker(GuiGraphicsExtractor g, JoinState s, int i, float cx, float cy, float r, float e);

    public abstract void bar(GuiGraphicsExtractor g, JoinState s, float x, float y, float w, float h, float e);

    public abstract float well(GuiGraphicsExtractor g, JoinState s, float cx, float top, float max);

    static int cellColor(ChunkStatus st, int empty, int lo, int hi, int full) {
        if (st == null || st == ChunkStatus.EMPTY) return empty;
        if (st == ChunkStatus.FULL) return full;
        return 0xFF000000 | lerpRgb(lo, hi, st.getIndex() / (float) Math.max(1, ChunkStatus.FULL.getIndex()));
    }

    static float pixelWell(GuiGraphicsExtractor g, JoinState s, float cx, float top, float max, int frame, int border) {
        ChunkLoadStatusView view = s.view;
        if (view == null) return 0f;
        int d = view.radius() * 2 + 1;
        int pitch = Math.min(4, (int) Math.floor((max - 8f) / d));
        if (pitch < 2) return 0f;
        int gap = pitch >= 3 ? 1 : 0;
        int cell = pitch - gap;
        int gw = d * pitch - gap;
        int gx = Math.round(cx - gw / 2f), gy = Math.round(top + 4f);
        g.fill(gx - 4, gy - 4, gx + gw + 4, gy + gw + 4, frame);
        HontunCards.border(g, gx - 4, gy - 4, gw + 8, gw + 8, border);
        int acc = HontunTheme.accent();
        for (int x = 0; x < d; x++) {
            for (int z = 0; z < d; z++) {
                int col = cellColor(view.get(x, z), 0xFF101114, HontunTheme.overlay0(), HontunTheme.lighten(acc, 0.75f), 0xFF000000 | acc);
                int x0 = gx + x * pitch, y0 = gy + z * pitch;
                g.fill(x0, y0, x0 + cell, y0 + cell, col);
            }
        }
        return gw + 8f;
    }

    static void pixelCheck(GuiGraphicsExtractor g, int x, int y, int n, int argb) {
        int u = Math.max(1, n / 7);
        int bx = x + (n - 5 * u) / 2, by = y + (n - 4 * u) / 2;
        g.fill(bx, by + 2 * u, bx + u, by + 3 * u, argb);
        g.fill(bx + u, by + 3 * u, bx + 2 * u, by + 4 * u, argb);
        g.fill(bx + 2 * u, by + 2 * u, bx + 3 * u, by + 3 * u, argb);
        g.fill(bx + 3 * u, by + u, bx + 4 * u, by + 2 * u, argb);
        g.fill(bx + 4 * u, by, bx + 5 * u, by + u, argb);
    }

    static void runner(GuiGraphicsExtractor g, JoinState s, int x, int y, int n, int argb, boolean clip) {
        int per = 4 * (n - 1);
        int head = (int) Math.floor(frac(s.t / 1.1) * per);
        for (int k = 0; k < 3; k++) {
            int p = ((head - k) % per + per) % per;
            if (clip && (p == 0 || p == 2 * (n - 1))) continue;
            int px, py;
            if (p < n - 1) { px = x + p; py = y; }
            else if (p < 2 * (n - 1)) { px = x + n - 1; py = y + p - (n - 1); }
            else if (p < 3 * (n - 1)) { px = x + n - 1 - (p - 2 * (n - 1)); py = y + n - 1; }
            else { px = x; py = y + n - 1 - (p - 3 * (n - 1)); }
            g.fill(px, py, px + 1, py + 1, argb(Math.round(((argb >>> 24) & 0xFF) * (1f - k * 0.3f)), argb));
        }
    }

    static void indeterminate(GuiGraphicsExtractor g, JoinState s, float x, float y, float w, float h, int track, int fill, boolean round) {
        float u = frac(s.t / 1.7);
        u = u * u * (3f - 2f * u);
        float len = w * 0.3f;
        float lead = x - len + (w + len) * u;
        float a = Math.max(x, lead), b = Math.min(x + w, lead + len);
        if (round) {
            pill(g, x, y - h / 2f, w, h, h / 2f, track);
            if (b - a > h) pill(g, a, y - h / 2f, b - a, h, h / 2f, fill);
        } else {
            int iy = Math.round(y - h / 2f), ih = Math.max(1, Math.round(h));
            g.fill(Math.round(x), iy, Math.round(x + w), iy + ih, track);
            if (b - a > 1f) g.fill(Math.round(a), iy, Math.round(b), iy + ih, fill);
        }
    }

    private static final class Smog extends JoinSkin {
        private static final int FILL = 0xD20C0C0E;
        private static final int INK = 0x0B0B0D;
        private static final float[] CORNER = {10f, 12f, 14f};

        @Override public boolean pixel() { return false; }
        @Override public int title() { return WHITE; }
        @Override public int light() { return LIGHT; }
        @Override public int dim() { return DIM; }
        @Override public int muted() { return MUTED; }
        @Override public int accent() { return WHITE; }

        @Override
        public void panel(GuiGraphicsExtractor g, float x, float y, float w, float h, int tier) {
            float r = CORNER[tier];
            soft(g, x - 40f, y - 30f, w + 80f, h + 74f, r + 40f, 44f, 0x46000000);
            soft(g, x - 8f, y - 5f, w + 16f, h + 18f, r + 8f, 12f, 0x78000000);
            pill(g, x, y, w, h, r, FILL);
            outline(g, x, y, w, h, r, 1f, 0x22FFFFFF);
        }

        @Override
        public void buttonWell(GuiGraphicsExtractor g, float x, float y, float w, float h, float k) {
            float r = Math.min(8f, Math.min(w, h) / 2f);
            pill(g, x, y, w, h, r, argb(Math.round(0x2C * k), WHITE));
            outline(g, x - 0.5f, y - 0.5f, w + 1f, h + 1f, r + 0.5f, 1f, argb(Math.round(0x1A * k), WHITE));
        }

        @Override
        public void rule(GuiGraphicsExtractor g, float x0, float x1, float y, float e) {
            if (e <= 0.01f) return;
            line(g, x0 + 0.5f, y, x0 + 0.5f + (x1 - x0 - 1f) * e, y, 0.5f, argb(Math.round(0x1A * e), WHITE));
        }

        @Override
        public void highlight(GuiGraphicsExtractor g, float x, float y, float w, float h, float a) {
            pill(g, x, y, w, h, h / 2f, argb(Math.round(0x12 * a), WHITE));
        }

        @Override
        public void link(GuiGraphicsExtractor g, float x, float y0, float y1, float fill, boolean bright, float e) {
            line(g, x, y0, x, y1, 0.5f, argb(Math.round(0x1E * e), WHITE));
            if (fill > 0.001f) line(g, x, y0, x, y0 + (y1 - y0) * cl(fill), 0.5f, argb(Math.round((bright ? 0xB0 : 0x80) * e), WHITE));
        }

        @Override
        public void marker(GuiGraphicsExtractor g, JoinState s, int i, float mx, float cy, float r, float e) {
            float st = 0.55f * r / 4f;
            if (s.skipped(i)) {
                ring(g, mx, cy, r - 0.4f, 0.8f, argb(Math.round(0x24 * e), WHITE));
                line(g, mx - 0.45f * r, cy, mx + 0.45f * r, cy, st, argb(Math.round(0x8C * e), WHITE));
            } else if (s.done(i)) {
                float u = s.doneAt[i] == 0L ? 1f : s.since(s.doneAt[i], 360f);
                float rr = r * (1f + 0.18f * (float) Math.sin(Math.PI * u));
                if (s.sdf && u < 1f) glow(g, mx, cy, 2.6f * r, argb(Math.round(0x34 * (1f - u) * e), WHITE));
                dot(g, mx, cy, rr, argb(Math.round(255 * e), SOFT));
                int ink = argb(Math.round(255 * e), INK);
                if (s.sdf) tick(g, mx, cy, 1.22f * rr, st, ink, cl(u * 1.3f));
                else dot(g, mx, cy, 0.38f * r, ink);
            } else if (s.active(i)) {
                if (s.sdf) {
                    glow(g, mx, cy, 2.4f * r, argb(Math.round(0x1C * (0.65f + 0.35f * pulse(s.t, 1.8)) * e), WHITE));
                    ring(g, mx, cy, r - 0.4f, 0.9f, argb(Math.round(0x38 * e), WHITE));
                    int c = argb(Math.round(255 * e), WHITE);
                    if (i == JoinState.STAGES - 1 && s.hasProgress) arc(g, mx, cy, r - 0.4f, -Math.PI / 2, TAU * cl(s.progress), st, c);
                    else arc(g, mx, cy, r - 0.4f, TAU * frac(s.t / 0.9), 1.3 + 0.9 * pulse(s.t, 1.8), st, c);
                    ripple(g, s, mx, cy, r);
                } else {
                    dot(g, mx, cy, r * (0.55f + 0.2f * pulse(s.t, 1.2)), argb(Math.round(255 * e), WHITE));
                }
            } else {
                ring(g, mx, cy, r - 0.4f, 0.9f, argb(Math.round(0x4C * e), WHITE));
            }
        }

        private static void tick(GuiGraphicsExtractor g, float cx, float cy, float size, float ht, int argb, float u) {
            if (u >= 1f) {
                check(g, cx, cy, size, ht, argb);
                return;
            }
            float x0 = cx - 0.42f * size, y0 = cy + 0.02f * size;
            float x1 = cx - 0.12f * size, y1 = cy + 0.30f * size;
            float x2 = cx + 0.42f * size, y2 = cy - 0.30f * size;
            float k1 = cl(u / 0.34f), k2 = eoc(cl((u - 0.34f) / 0.66f));
            if (k1 > 0f) line(g, x0, y0, lerp(x0, x1, k1), lerp(y0, y1, k1), ht, argb);
            if (k2 > 0f) line(g, x1, y1, lerp(x1, x2, k2), lerp(y1, y2, k2), ht, argb);
        }

        @Override
        public void bar(GuiGraphicsExtractor g, JoinState s, float x, float y, float w, float h, float e) {
            if (s.hasProgress) JoinDraw.bar(g, x, y, w, h, s.progress, e, s.sdf);
            else indeterminate(g, s, x, y, w, h, argb(Math.round(0x24 * e), WHITE), argb(Math.round(0xC8 * e), WHITE), true);
        }

        @Override
        public float well(GuiGraphicsExtractor g, JoinState s, float cx, float top, float max) {
            return chunkWell(g, s, cx, top, max);
        }
    }

    private static final class Flat extends JoinSkin {
        @Override public boolean pixel() { return true; }

        @Override
        public void panel(GuiGraphicsExtractor g, float x, float y, float w, float h, int tier) {
            int ix = Math.round(x), iy = Math.round(y), iw = Math.round(w), ih = Math.round(h);
            g.fill(ix - 3, iy - 3, ix + iw + 3, iy + ih + 3, 0x40000000);
            g.fillGradient(ix, iy, ix + iw, iy + ih, HontunTheme.argb(0xF4, HontunTheme.surface1()), HontunTheme.argb(0xF4, HontunTheme.surface0()));
            HontunCards.border(g, ix, iy, iw, ih, HontunTheme.argb(0x80, HontunTheme.overlay0()));
            g.fillGradient(ix + 1, iy + ih - 2, ix + iw - 1, iy + ih, HontunTheme.argb(0xFF, HontunTheme.accent()), HontunTheme.argb(0xFF, HontunTheme.accentHi()));
        }

        @Override
        public void rule(GuiGraphicsExtractor g, float x0, float x1, float y, float e) {
            if (e <= 0.01f) return;
            int iy = Math.round(y - 0.5f);
            g.fill(Math.round(x0), iy, Math.round(x0 + (x1 - x0) * e), iy + 1, HontunTheme.argb(Math.round(0x90 * e), HontunTheme.overlay0()));
        }

        @Override
        public void highlight(GuiGraphicsExtractor g, float x, float y, float w, float h, float a) {
            int ix = Math.round(x), iy = Math.round(y), iw = Math.round(w), ih = Math.round(h);
            g.fill(ix, iy, ix + iw, iy + ih, HontunTheme.argb(Math.round(0x22 * a), HontunTheme.accent()));
            g.fill(ix, iy, ix + 2, iy + ih, HontunTheme.argb(Math.round(0xE6 * a), HontunTheme.accent()));
        }

        @Override
        public void link(GuiGraphicsExtractor g, float x, float y0, float y1, float fill, boolean bright, float e) {
            int ix = Math.round(x - 0.5f), a = Math.round(y0), b = Math.round(y1);
            g.fill(ix, a, ix + 1, b, HontunTheme.argb(Math.round(0x70 * e), HontunTheme.overlay0()));
            if (fill > 0.001f) g.fill(ix, a, ix + 1, a + Math.round((b - a) * cl(fill)), HontunTheme.argb(Math.round((bright ? 0xE6 : 0x90) * e), HontunTheme.accent()));
        }

        @Override
        public void marker(GuiGraphicsExtractor g, JoinState s, int i, float mx, float cy, float r, float e) {
            if (!s.sdf) {
                PIXEL.marker(g, s, i, mx, cy, r, e);
                return;
            }
            int acc = HontunTheme.accent();
            float st = 0.55f * r / 4f;
            if (s.skipped(i)) {
                ring(g, mx, cy, r - 0.4f, 0.9f, argb(Math.round(0x80 * e), HontunTheme.overlay0()));
                line(g, mx - 0.45f * r, cy, mx + 0.45f * r, cy, st, argb(Math.round(0xC0 * e), HontunTheme.overlay1()));
            } else if (s.done(i)) {
                dot(g, mx, cy, r, argb(Math.round(255 * e), acc));
                check(g, mx, cy, 1.2f * r, st, argb(Math.round(255 * e), HontunTheme.crust()));
            } else if (s.active(i)) {
                ring(g, mx, cy, r - 0.4f, 0.9f, argb(Math.round(0x60 * e), acc));
                int c = argb(Math.round(255 * e), HontunTheme.accentHi());
                if (i == JoinState.STAGES - 1 && s.hasProgress) arc(g, mx, cy, r - 0.4f, -Math.PI / 2, TAU * cl(s.progress), st, c);
                else arc(g, mx, cy, r - 0.4f, TAU * frac(s.t / 0.9), 1.3 + 0.9 * pulse(s.t, 1.8), st, c);
            } else {
                ring(g, mx, cy, r - 0.4f, 0.9f, argb(Math.round(0xB0 * e), HontunTheme.overlay1()));
            }
        }

        @Override
        public void bar(GuiGraphicsExtractor g, JoinState s, float x, float y, float w, float h, float e) {
            int track = HontunTheme.argb(Math.round(0xC0 * e), HontunTheme.crust());
            int fill = HontunTheme.argb(Math.round(255 * e), HontunTheme.accent());
            if (!s.hasProgress) {
                indeterminate(g, s, x, y, w, h, track, fill, false);
                return;
            }
            int iy = Math.round(y - h / 2f), ih = Math.max(1, Math.round(h));
            g.fill(Math.round(x), iy, Math.round(x + w), iy + ih, track);
            g.fill(Math.round(x), iy, Math.round(x + w * cl(s.progress)), iy + ih, fill);
        }

        @Override
        public float well(GuiGraphicsExtractor g, JoinState s, float cx, float top, float max) {
            return pixelWell(g, s, cx, top, max, HontunTheme.argb(0xFF, HontunTheme.crust()), HontunTheme.argb(0x90, HontunTheme.overlay0()));
        }
    }

    private static final class Chamfer extends JoinSkin {
        @Override public boolean pixel() { return true; }

        @Override
        public void panel(GuiGraphicsExtractor g, float x, float y, float w, float h, int tier) {
            int ix = Math.round(x), iy = Math.round(y), iw = Math.round(w), ih = Math.round(h);
            HontunShapes.glow(g, ix, iy, iw, ih, 6, HontunTheme.accent(), 2, 0x50);
            HontunShapes.fillClipped(g, ix, iy, iw, ih, 6, 6, HontunTheme.argb(0xF4, HontunTheme.surface0()));
            HontunShapes.outlineClipped(g, ix, iy, iw, ih, 6, 6, HontunTheme.argb(0xFF, HontunTheme.accentLo()));
        }

        @Override
        public void rule(GuiGraphicsExtractor g, float x0, float x1, float y, float e) {
            if (e <= 0.01f) return;
            int iy = Math.round(y - 0.5f);
            g.fill(Math.round(x0), iy, Math.round(x0 + (x1 - x0) * e), iy + 1, HontunTheme.argb(Math.round(0x80 * e), HontunTheme.accentLo()));
        }

        @Override
        public void highlight(GuiGraphicsExtractor g, float x, float y, float w, float h, float a) {
            int ix = Math.round(x), iy = Math.round(y), iw = Math.round(w), ih = Math.round(h);
            HontunShapes.fillClipped(g, ix, iy, iw, ih, 3, 3, HontunTheme.argb(Math.round(0x26 * a), HontunTheme.accent()));
            HontunShapes.outlineClipped(g, ix, iy, iw, ih, 3, 3, HontunTheme.argb(Math.round(0x90 * a), HontunTheme.accent()));
        }

        @Override
        public void link(GuiGraphicsExtractor g, float x, float y0, float y1, float fill, boolean bright, float e) {
            int ix = Math.round(x - 0.5f), a = Math.round(y0), b = Math.round(y1);
            g.fill(ix, a, ix + 1, b, HontunTheme.argb(Math.round(0x80 * e), HontunTheme.overlay0()));
            if (fill > 0.001f) g.fill(ix, a, ix + 1, a + Math.round((b - a) * cl(fill)), HontunTheme.argb(Math.round((bright ? 0xFF : 0x9C) * e), HontunTheme.accent()));
        }

        @Override
        public void marker(GuiGraphicsExtractor g, JoinState s, int i, float mx, float cy, float r, float e) {
            int n = Math.max(7, Math.round(2f * r) | 1);
            int x = Math.round(mx - n / 2f), y = Math.round(cy - n / 2f);
            int acc = HontunTheme.accent();
            if (s.skipped(i)) {
                HontunShapes.outlineClipped(g, x, y, n, n, 2, 2, HontunTheme.argb(Math.round(0x90 * e), HontunTheme.overlay0()));
                g.fill(x + 2, y + n / 2, x + n - 2, y + n / 2 + 1, HontunTheme.argb(Math.round(0xC0 * e), HontunTheme.overlay1()));
            } else if (s.done(i)) {
                HontunShapes.fillClipped(g, x, y, n, n, 2, 2, HontunTheme.argb(Math.round(255 * e), acc));
                pixelCheck(g, x, y, n, HontunTheme.argb(Math.round(255 * e), HontunTheme.crust()));
            } else if (s.active(i)) {
                HontunShapes.glow(g, x, y, n, n, 2, acc, 1, Math.round(0x70 * (0.6f + 0.4f * pulse(s.t, 1.6)) * e));
                HontunShapes.outlineClipped(g, x, y, n, n, 2, 2, HontunTheme.argb(Math.round(0x70 * e), acc));
                runner(g, s, x, y, n, HontunTheme.argb(Math.round(255 * e), HontunTheme.accentHi()), true);
                int in = Math.max(1, n - 6);
                g.fill(x + 3, y + 3, x + 3 + in, y + 3 + in, HontunTheme.argb(Math.round(255 * (0.35f + 0.65f * pulse(s.t, 1.2)) * e), acc));
            } else {
                HontunShapes.outlineClipped(g, x, y, n, n, 2, 2, HontunTheme.argb(Math.round(0xC0 * e), HontunTheme.overlay1()));
            }
        }

        @Override
        public void bar(GuiGraphicsExtractor g, JoinState s, float x, float y, float w, float h, float e) {
            int track = HontunTheme.argb(Math.round(0xC0 * e), HontunTheme.crust());
            int fill = HontunTheme.argb(Math.round(255 * e), HontunTheme.accent());
            if (!s.hasProgress) {
                indeterminate(g, s, x, y, w, h, track, fill, false);
                return;
            }
            int iy = Math.round(y - h / 2f), ih = Math.max(1, Math.round(h));
            g.fill(Math.round(x), iy, Math.round(x + w), iy + ih, track);
            g.fill(Math.round(x), iy, Math.round(x + w * cl(s.progress)), iy + ih, fill);
        }

        @Override
        public float well(GuiGraphicsExtractor g, JoinState s, float cx, float top, float max) {
            return pixelWell(g, s, cx, top, max, HontunTheme.argb(0xFF, HontunTheme.crust()), HontunTheme.argb(0xB0, HontunTheme.accentLo()));
        }
    }

    private static final class Pixel extends JoinSkin {
        @Override public boolean pixel() { return true; }

        @Override
        public void panel(GuiGraphicsExtractor g, float x, float y, float w, float h, int tier) {
            int ix = Math.round(x), iy = Math.round(y), iw = Math.round(w), ih = Math.round(h);
            g.fill(ix + 2, iy + 2, ix + iw + 2, iy + ih + 2, 0x70000000);
            g.fill(ix, iy, ix + iw, iy + ih, HontunTheme.argb(0xF2, HontunTheme.base()));
            g.fill(ix, iy, ix + iw, iy + 1, HontunTheme.argb(0xCC, HontunTheme.accent()));
            g.fill(ix, iy + ih - 1, ix + iw, iy + ih, HontunTheme.argb(0xCC, HontunTheme.accent()));
            g.fill(ix, iy + 1, ix + 1, iy + ih - 1, HontunTheme.argb(0xCC, HontunTheme.accent()));
            g.fill(ix + iw - 1, iy + 1, ix + iw, iy + ih - 1, HontunTheme.argb(0xCC, HontunTheme.accent()));
        }

        @Override
        public void rule(GuiGraphicsExtractor g, float x0, float x1, float y, float e) {
            if (e <= 0.01f) return;
            int iy = Math.round(y - 0.5f);
            g.fill(Math.round(x0), iy, Math.round(x0 + (x1 - x0) * e), iy + 1, HontunTheme.argb(Math.round(0x70 * e), HontunTheme.accent()));
        }

        @Override
        public void highlight(GuiGraphicsExtractor g, float x, float y, float w, float h, float a) {
            int ix = Math.round(x), iy = Math.round(y), iw = Math.round(w), ih = Math.round(h);
            g.fill(ix, iy, ix + iw, iy + ih, HontunTheme.argb(Math.round(0x30 * a), HontunTheme.accent()));
        }

        @Override
        public void link(GuiGraphicsExtractor g, float x, float y0, float y1, float fill, boolean bright, float e) {
            int ix = Math.round(x - 0.5f), a = Math.round(y0), b = Math.round(y1);
            g.fill(ix, a, ix + 1, b, HontunTheme.argb(Math.round(0x80 * e), HontunTheme.overlay0()));
            if (fill > 0.001f) g.fill(ix, a, ix + 1, a + Math.round((b - a) * cl(fill)), HontunTheme.argb(Math.round((bright ? 0xFF : 0x9C) * e), HontunTheme.accent()));
        }

        @Override
        public void marker(GuiGraphicsExtractor g, JoinState s, int i, float mx, float cy, float r, float e) {
            int n = Math.max(7, Math.round(2f * r) | 1);
            int x = Math.round(mx - n / 2f), y = Math.round(cy - n / 2f);
            int acc = HontunTheme.accent();
            if (s.skipped(i)) {
                HontunCards.border(g, x, y, n, n, HontunTheme.argb(Math.round(0xA0 * e), HontunTheme.overlay0()));
                g.fill(x + 2, y + n / 2, x + n - 2, y + n / 2 + 1, HontunTheme.argb(Math.round(0xC0 * e), HontunTheme.overlay1()));
            } else if (s.done(i)) {
                g.fill(x, y, x + n, y + n, HontunTheme.argb(Math.round(255 * e), acc));
                pixelCheck(g, x, y, n, HontunTheme.argb(Math.round(255 * e), 0x000000));
            } else if (s.active(i)) {
                g.fill(x, y, x + n, y + 1, HontunTheme.argb(Math.round(0x70 * e), acc));
                g.fill(x, y + n - 1, x + n, y + n, HontunTheme.argb(Math.round(0x70 * e), acc));
                g.fill(x, y + 1, x + 1, y + n - 1, HontunTheme.argb(Math.round(0x70 * e), acc));
                g.fill(x + n - 1, y + 1, x + n, y + n - 1, HontunTheme.argb(Math.round(0x70 * e), acc));
                runner(g, s, x, y, n, HontunTheme.argb(Math.round(255 * e), 0xFFFFFF), false);
                int in = Math.max(1, n - 6);
                g.fill(x + 3, y + 3, x + 3 + in, y + 3 + in, HontunTheme.argb(Math.round(255 * (0.35f + 0.65f * pulse(s.t, 1.2)) * e), acc));
            } else {
                HontunCards.border(g, x, y, n, n, HontunTheme.argb(Math.round(0xC0 * e), HontunTheme.overlay1()));
                g.fill(x, y, x + 1, y + 1, HontunTheme.argb(Math.round(0xC0 * e), HontunTheme.overlay1()));
                g.fill(x + n - 1, y, x + n, y + 1, HontunTheme.argb(Math.round(0xC0 * e), HontunTheme.overlay1()));
                g.fill(x, y + n - 1, x + 1, y + n, HontunTheme.argb(Math.round(0xC0 * e), HontunTheme.overlay1()));
                g.fill(x + n - 1, y + n - 1, x + n, y + n, HontunTheme.argb(Math.round(0xC0 * e), HontunTheme.overlay1()));
            }
        }

        @Override
        public void bar(GuiGraphicsExtractor g, JoinState s, float x, float y, float w, float h, float e) {
            int track = HontunTheme.argb(Math.round(0xE0 * e), 0x000000);
            int fill = HontunTheme.argb(Math.round(255 * e), HontunTheme.accent());
            if (!s.hasProgress) {
                indeterminate(g, s, x, y, w, h, track, fill, false);
                return;
            }
            int iy = Math.round(y - h / 2f), ih = Math.max(1, Math.round(h));
            g.fill(Math.round(x), iy, Math.round(x + w), iy + ih, track);
            g.fill(Math.round(x), iy, Math.round(x + w * cl(s.progress)), iy + ih, fill);
        }

        @Override
        public float well(GuiGraphicsExtractor g, JoinState s, float cx, float top, float max) {
            return pixelWell(g, s, cx, top, max, 0xFF000000, HontunTheme.argb(0xCC, HontunTheme.accent()));
        }
    }
}
