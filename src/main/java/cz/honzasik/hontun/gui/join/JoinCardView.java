package cz.honzasik.hontun.gui.join;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.server.level.progress.ChunkLoadStatusView;

import static cz.honzasik.hontun.gui.join.JoinDraw.*;

public final class JoinCardView {
    private static final int ROWS = JoinState.STAGES;
    private static final int[] INNER_SMOG = {208, 236, 296};
    private static final int[] INNER_PIXEL = {220, 244, 276};
    private static final float[] PAD = {10f, 12f, 15f};
    private static final float[] TITLE_S = {1.1f, 1.25f, 1.5f};
    private static final float[] TIME_S = {0.8f, 0.85f, 1.0f};
    private static final float[] GAP_A = {5f, 6f, 8f};
    private static final float[] GAP_B = {3f, 4f, 4f};
    private static final float[] GAP_F = {7f, 8f, 10f};
    private static final float[] ROW_SMOG = {16f, 18f, 24f};
    private static final float[] ROW_PIXEL = {14f, 16f, 18f};
    private static final float[] MARK_SMOG = {3.5f, 4f, 5f};
    private static final float[] MARK_PIXEL = {3.5f, 3.5f, 4.5f};
    private static final float[] LABEL_GAP = {6f, 7f, 9f};
    private static final float[] LABEL_S = {0.8f, 0.85f, 1.05f};
    private static final float[] MS_S = {0.72f, 0.78f, 0.92f};
    private static final float[] STATUS_S = {0.8f, 0.85f, 1.0f};
    private static final float[] BAR_GAP = {5f, 6f, 7f};
    private static final float[] BAR_H = {3f, 3f, 4f};
    private static final float[] WELL_GAP = {8f, 10f, 12f};
    private static final int[] BTN_W = {72, 80, 96};
    private static final int[] BTN_H_SMOG = {18, 20, 22};
    private static final int[] BTN_H_PIXEL = {18, 20, 20};
    private static final int ROW_MS = 140;
    private static final int STEP_MS = 50;
    private static final String[] PCT = new String[101];

    static {
        for (int i = 0; i < PCT.length; i++) PCT[i] = i + "%";
    }

    private record L(int tier, boolean pixel, float cardX, float cardY, float cardW, float cardH,
                     float x0, float x1, float cx, float top, float titleS, float timeS,
                     float rule1, float rowsTop, float rowH, float r, float bleed,
                     float labelX, float labelS, float msS, float rule2, float statusY, float statusS,
                     int btnX, int btnY, int btnW, int btnH, float barY, float barH, float wellTop, float wellArg) {}

    private static L cache;
    private static int cacheW = -1, cacheH = -1, cacheRadius = -1;
    private static JoinView.Mode cacheMode;
    private static JoinSkin cacheSkin;
    private static long session = -1L;
    private static int hiStage = -1, hiFrom = -1;

    private JoinCardView() {}

    private static L at(int w, int h, JoinView.Mode mode, ChunkLoadStatusView view, JoinSkin skin) {
        int radius = mode == JoinView.Mode.LOCAL && view != null ? view.radius() : -1;
        if (cache == null || w != cacheW || h != cacheH || mode != cacheMode || radius != cacheRadius || skin != cacheSkin) {
            cache = layout(w, h, mode, view, skin.pixel());
            cacheW = w;
            cacheH = h;
            cacheMode = mode;
            cacheRadius = radius;
            cacheSkin = skin;
        }
        return cache;
    }

    private static L layout(int w, int h, JoinView.Mode mode, ChunkLoadStatusView view, boolean px) {
        int t = h < 250 ? 0 : (h < 400 ? 1 : 2);
        float p = PAD[t];
        float inner = Math.max(120f, Math.min(px ? INNER_PIXEL[t] : INNER_SMOG[t], w - 24f - 2f * p));
        float ts = px ? 1f : TITLE_S[t], ss = px ? 1f : STATUS_S[t], ls = px ? 1f : LABEL_S[t];
        float ms = px ? 1f : MS_S[t], es = px ? 1f : TIME_S[t];
        float rh = px ? ROW_PIXEL[t] : ROW_SMOG[t], r = px ? MARK_PIXEL[t] : MARK_SMOG[t];
        int btnW = BTN_W[t], btnH = px ? BTN_H_PIXEL[t] : BTN_H_SMOG[t];
        float barH = BAR_H[t];
        boolean track = mode == JoinView.Mode.CONNECT || mode == JoinView.Mode.JOIN;

        float head = 9f * ts + GAP_A[t] + GAP_B[t];
        float body;
        float wellArg = 0f;
        if (track) {
            body = ROWS * rh + GAP_B[t] + GAP_F[t] + btnH;
        } else {
            body = 9f * ss + BAR_GAP[t] + barH;
            if (mode == JoinView.Mode.LOCAL && view != null) {
                int d = view.radius() * 2 + 1;
                float avail = Math.min(inner, h - 16f - 2f * p - head - body - WELL_GAP[t]);
                int pitch = Math.min(px ? 4 : 5, (int) Math.floor((avail - 10f) / d));
                if (pitch >= 2) {
                    wellArg = d * pitch + 10f;
                    body += WELL_GAP[t] + d * pitch - (pitch >= 3 ? 1 : 0) + 10f;
                }
            }
        }
        float stack = head + body;
        float cardW = inner + 2f * p, cardH = (float) Math.ceil(stack + 2f * p);
        float cardX = (float) Math.floor((w - cardW) / 2f);
        float lift = Math.min(10f, (float) Math.floor(h * 0.03f));
        float cardY = Math.max(8f, Math.min((float) Math.floor((h - cardH) / 2f - lift), (float) Math.floor(h - 8f - cardH)));
        float x0 = cardX + p, x1 = x0 + inner, top = cardY + p;

        float rule1 = (float) Math.floor(top + 9f * ts + GAP_A[t]) + 0.5f;
        float rowsTop = (float) Math.floor(rule1) + GAP_B[t];
        float rule2 = rowsTop + ROWS * rh + GAP_B[t] + 0.5f;
        int btnY = Math.round(rule2 - 0.5f + GAP_F[t]);
        int btnX = Math.round(x1) - btnW;

        float statusY, barY = 0f, wellTop = 0f;
        if (track) {
            statusY = btnY + btnH / 2f - 4f * ss;
        } else {
            statusY = rowsTop;
            barY = statusY + 9f * ss + BAR_GAP[t] + barH / 2f;
            wellTop = Math.round(barY + barH / 2f + WELL_GAP[t]);
        }
        float bleed = px ? 3f : (rh - 2f) / 2f - r;
        return new L(t, px, cardX, cardY, cardW, cardH, x0, x1, (x0 + x1) / 2f, top, ts, es,
                rule1, rowsTop, rh, r, bleed, x0 + 2f * r + LABEL_GAP[t], ls, ms, rule2, statusY, ss,
                btnX, btnY, btnW, btnH, barY, barH, wellTop, wellArg);
    }

    public static int[] cancel(int w, int h) {
        L l = at(w, h, JoinView.Mode.CONNECT, null, JoinSkin.current());
        return new int[]{l.btnX(), l.btnY(), l.btnW(), l.btnH()};
    }

    public static void background(GuiGraphicsExtractor g, int w, int h, JoinView.Mode mode, ChunkLoadStatusView view) {
        JoinSkin skin = JoinSkin.current();
        L l = at(w, h, mode, view, skin);
        skin.panel(g, l.cardX(), l.cardY(), l.cardW(), l.cardH(), l.tier());
        if (mode == JoinView.Mode.CONNECT) skin.buttonWell(g, l.btnX(), l.btnY(), l.btnW(), l.btnH(), 1f);
    }

    public static void render(GuiGraphicsExtractor g, JoinState s) {
        JoinSkin skin = JoinSkin.current();
        L l = at(s.w, s.h, s.mode, s.view, skin);
        header(g, s, l, skin);
        if (s.track()) {
            highlight(g, s, l, skin);
            links(g, s, l, skin);
            rows(g, s, l, skin);
            footer(g, s, l, skin);
        } else {
            body(g, s, l, skin);
        }
        if (s.mode == JoinView.Mode.JOIN && s.ghostAt != 0L && s.cancelRect != null && !l.pixel()) {
            float u = s.since(s.ghostAt, 220f);
            int[] r = s.cancelRect;
            if (u < 1f) skin.buttonWell(g, r[0], r[1], r[2], r[3], 1f - u * u);
            ghost(g, s);
        }
    }

    private static void txt(GuiGraphicsExtractor g, JoinState s, L l, String str, float x, float y, float scale, int argb) {
        if (l.pixel()) text(g, s.font, str, Math.round(x), Math.round(y), 1f, argb);
        else text(g, s.font, str, x, y, scale, argb);
    }

    private static void txtRight(GuiGraphicsExtractor g, JoinState s, L l, String str, float rx, float y, float scale, int argb) {
        txt(g, s, l, str, rx - width(s.font, str, l.pixel() ? 1f : scale), y, scale, argb);
    }

    private static void fadeLeft(GuiGraphicsExtractor g, JoinState s, L l, String cur, String prev, long at,
                                 float x, float y, float scale, int rgb, float alpha, float maxW) {
        float sc = l.pixel() ? 1f : scale;
        long d = s.now - at;
        if (prev != null && at != 0L && d < 160) {
            float u1 = cl(d / 160f);
            txt(g, s, l, fit(s.font, prev, maxW, sc), x, y - 3f * u1, sc, argb(Math.round(255 * alpha * (1f - u1 * u1)), rgb));
        }
        float u2 = prev == null || at == 0L ? 1f : eoc(cl((d - 60) / 260f));
        txt(g, s, l, fit(s.font, cur, maxW, sc), x, y + 4f * (1f - u2), sc, argb(Math.round(255 * alpha * u2), rgb));
    }

    private static void header(GuiGraphicsExtractor g, JoinState s, L l, JoinSkin skin) {
        float et = s.ent(0);
        float ts = l.titleS(), es = l.timeS();
        float ty = l.top() + (1f - et) * 4f;
        float box = width(s.font, "00.0 s", es) + 10f;
        fadeLeft(g, s, l, s.title, s.prevTitle, s.titleAt, l.x0(), ty, ts, skin.title(), et, l.x1() - l.x0() - box);
        float fade = eoc(cl((s.elapsedMs - 400L) / 300f)) * s.ent(60);
        if (fade > 0.02f) {
            txtRight(g, s, l, formatElapsed(s.elapsedMs), l.x1(), ty + 7f * (ts - es), es, argb(Math.round(0xB0 * fade), skin.dim()));
        }
        skin.rule(g, l.x0(), l.x1(), l.rule1(), s.ent(90));
    }

    private static float rowY(L l, int i, float e) {
        return l.rowsTop() + (i + 0.5f) * l.rowH() + (1f - e) * 3f;
    }

    private static void highlight(GuiGraphicsExtractor g, JoinState s, L l, JoinSkin skin) {
        if (s.sessionStart != session) {
            session = s.sessionStart;
            hiStage = s.stage;
            hiFrom = -1;
        } else if (s.stage != hiStage) {
            hiFrom = hiStage;
            hiStage = s.stage;
        }
        int st = s.stage;
        if (st < 0 || st >= ROWS) return;
        float e = s.ent(ROW_MS + STEP_MS * st);
        if (e <= 0.01f) return;
        float a = 1f;
        float row = st;
        if (s.advancedAt != 0L) {
            float u = s.since(s.advancedAt, 380f);
            if (hiFrom >= 0 && hiFrom < ROWS) row = lerp(hiFrom, st, eoc(u));
            else a = u;
        }
        float hh = l.rowH() - 2f;
        float y = l.rowsTop() + row * l.rowH() + 1f + (1f - e) * 3f;
        skin.highlight(g, l.x0() - l.bleed(), y, l.x1() - l.x0() + 2f * l.bleed(), hh, a * e);
    }

    private static void links(GuiGraphicsExtractor g, JoinState s, L l, JoinSkin skin) {
        float mx = l.x0() + l.r(), gap = l.r() + 2f;
        for (int i = 0; i < ROWS - 1; i++) {
            float f = s.fill(i);
            float ea = s.ent(ROW_MS + STEP_MS * i), eb = s.ent(ROW_MS + STEP_MS * (i + 1));
            if (eb <= 0.01f) continue;
            float a = rowY(l, i, ea) + gap, b = rowY(l, i + 1, eb) - gap;
            if (b - a < 1f) continue;
            skin.link(g, mx, a, b, f, s.done(i) || s.skipped(i), eb);
        }
    }

    private static void rows(GuiGraphicsExtractor g, JoinState s, L l, JoinSkin skin) {
        float mx = l.x0() + l.r();
        float ls = l.labelS(), ms = l.msS();
        for (int i = 0; i < ROWS; i++) {
            float e = s.ent(ROW_MS + STEP_MS * i);
            if (e <= 0.01f) continue;
            float cy = rowY(l, i, e);
            skin.marker(g, s, i, mx, cy, l.r(), e);

            int col, a, tcol, ta;
            if (s.skipped(i)) {
                col = skin.muted();
                a = 255;
                tcol = skin.muted();
                ta = 255;
            } else if (s.done(i)) {
                float k = s.doneAt[i] == 0L ? 1f : s.since(s.doneAt[i], 320f);
                col = lerpRgb(skin.title(), skin.light(), k);
                a = 255;
                tcol = skin.dim();
                ta = 255;
            } else if (s.active(i)) {
                float k = s.advancedAt == 0L ? 1f : s.since(s.advancedAt, 320f);
                col = lerpRgb(skin.dim(), skin.title(), k);
                a = Math.round(lerp(0x8C, 255, k));
                tcol = skin.title();
                ta = Math.round((i == ROWS - 1 && s.hasProgress ? 255 : 0xB3) * k);
            } else {
                col = skin.dim();
                a = 0x8C;
                tcol = skin.dim();
                ta = 0;
            }

            String label = s.label(i);
            String time = s.stageText(i, false);
            float room = l.x1() - l.labelX();
            float lsc = l.pixel() ? 1f : ls, msc = l.pixel() ? 1f : ms;
            if (time != null) {
                room -= width(s.font, time, msc) + 8f;
                txtRight(g, s, l, time, l.x1(), cy - 4f * msc, msc, argb(Math.round(ta * e), tcol));
            }
            if (width(s.font, label, lsc) > room) label = fit(s.font, label, room, lsc);
            txt(g, s, l, label, l.labelX(), cy - 4f * lsc, lsc, argb(Math.round(a * e), col));
        }
    }

    private static void footer(GuiGraphicsExtractor g, JoinState s, L l, JoinSkin skin) {
        skin.rule(g, l.x0(), l.x1(), l.rule2(), s.ent(ROW_MS + STEP_MS * ROWS));
        float e = s.ent(ROW_MS + STEP_MS * ROWS + 40);
        if (e <= 0.01f) return;
        boolean btn = s.mode == JoinView.Mode.CONNECT || (s.ghostAt != 0L && s.since(s.ghostAt, 220f) < 1f);
        float maxW = (btn ? l.btnX() - 10f : l.x1()) - l.x0();
        fadeLeft(g, s, l, s.status, s.prevStatus, s.statusAt, l.x0(), l.statusY() + (1f - e) * 3f, l.statusS(), skin.dim(), e, maxW);
    }

    private static void body(GuiGraphicsExtractor g, JoinState s, L l, JoinSkin skin) {
        float es = s.ent(ROW_MS);
        if (es > 0.01f) {
            float ss = l.statusS();
            float sy = l.statusY() + (1f - es) * 3f;
            float room = l.x1() - l.x0();
            if (s.hasProgress) {
                int pct = Math.max(0, Math.min(100, (int) Math.floor(s.progress * 100f)));
                room -= width(s.font, "100%", l.pixel() ? 1f : ss) + 10f;
                txtRight(g, s, l, PCT[pct], l.x1(), sy, ss, argb(Math.round(255 * es), skin.light()));
            }
            fadeLeft(g, s, l, s.status, s.prevStatus, s.statusAt, l.x0(), sy, ss, skin.dim(), es, room);
        }

        float eb = s.ent(ROW_MS + STEP_MS);
        if (eb > 0.01f) skin.bar(g, s, l.x0(), l.barY() + (1f - eb) * 3f, l.x1() - l.x0(), l.barH(), eb);

        if (s.mode == JoinView.Mode.LOCAL && s.view != null && l.wellArg() > 0f) {
            float ew = s.ent(ROW_MS + 2 * STEP_MS);
            if (ew > 0.01f) {
                g.pose().pushMatrix();
                g.pose().translate(0f, l.pixel() ? Math.round((1f - ew) * 4f) : (1f - ew) * 4f);
                skin.well(g, s, l.cx(), l.wellTop(), l.wellArg());
                g.pose().popMatrix();
            }
        }
    }
}
