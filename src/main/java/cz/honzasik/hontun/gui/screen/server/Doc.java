package cz.honzasik.hontun.gui.screen.server;

import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.ArrayList;
import java.util.List;

final class Doc {
    static final int TEXT = 0, PILL = 1, LINK = 2, BIG = 3, FACE = 4, METER = 5, ICON = 6, RAW = 7, SPRITE = 8, METER_L = 9,
            TAB = 10, TOGGLE = 11, SWATCH = 12, CHIP = 13, BARS = 14, BOX = 15;
    static final int NONE = 0, WELL = 1, BAND = 2, PANEL = 3;
    static final int RULE = 0, FADE = 1, RULE_F = 2;

    final List<Item> full = new ArrayList<>();
    final List<Item> left = new ArrayList<>();
    final List<Item> right = new ArrayList<>();
    final List<Item> extra = new ArrayList<>();
    final List<Btn> buttons = new ArrayList<>();
    int height;
    int lw = -1, rx = -1, colRule = -1;
    Item searchAnchor;
    int searchX, searchW, searchH;

    record Btn(String label, Item anchor, int rightInset, int width, Runnable act, int icon) {
        Btn(String label, Item anchor, int rightInset, int width, Runnable act) {
            this(label, anchor, rightInset, width, act, -1);
        }
    }

    private static int tone(int color) {
        if (color == Paint.acc()) return Paint.ACC;
        return color == Paint.dim() ? Paint.DIM : Paint.NORMAL;
    }

    private static int toneColor(int tone) {
        if (tone == Paint.ACC) return Paint.acc();
        return tone == Paint.DIM ? Paint.dim() : Paint.sub();
    }

    static String flashKey;
    static long flashAt;
    static final int FLASH_MS = 1100;

    static float flash(String key) {
        if (key == null || !key.equals(flashKey)) return -1;
        long t = Util.getMillis() - flashAt;
        return t >= 0 && t < FLASH_MS ? t / (float) FLASH_MS : -1;
    }

    static int flashColor(float f, int base) {
        int ok = Paint.green();
        return f < 0.65f ? ok : Paint.mix(ok, base, (f - 0.65f) / 0.35f);
    }

    static Note note(String s) {
        return new Note(Paint.INFO, Paint.dim(), Paint.sm(s), Paint.dim(), 0);
    }

    static final class Span {
        final int x;
        final Component text;
        final int color;
        final int kind;
        int maxW;
        int level;
        int tone;
        int icon = -1;
        Identifier sprite;
        int sw, sh;
        int dy = Integer.MIN_VALUE;
        Runnable act;
        String key;
        Tip tip;
        PlayerSkin skin;
        Component count;
        boolean sel;
        boolean joined, last;

        Span(int x, Component text, int color, int kind) {
            this.x = x;
            this.text = text;
            this.color = color;
            this.kind = kind;
            if (kind == PILL) this.tone = tone(color);
        }

        Span max(int w) { maxW = w; return this; }

        Span on(Runnable a) { act = a; return this; }

        Span key(String k) { key = k; return this; }

        Span tip(List<Component> t) { tip = Tip.of(t); return this; }

        Span tip(Tip t) { tip = t; return this; }

        Span dy(int d) { dy = d; return this; }

        boolean clips() { return kind == TEXT || kind == LINK || kind == RAW; }

        Component shown() {
            return clips() ? Paint.clip(text, maxW) : text;
        }

        int width() {
            return switch (kind) {
                case FACE -> 8;
                case BARS -> 9;
                case BOX -> sw;
                case METER -> Paint.meterW();
                case METER_L -> Paint.meterLW();
                case PILL -> Paint.pillWidth(text, tone, icon);
                case BIG -> Paint.bigWidth(text);
                case ICON -> Paint.iconW(icon);
                case SPRITE, SWATCH -> sw;
                case TAB -> Paint.tabW(this);
                case TOGGLE -> Paint.toggleW(this);
                case CHIP -> Paint.chipW(this);
                default -> Paint.w(shown());
            };
        }

        Tip tooltip() {
            if (tip != null) return tip;
            if (clips() && maxW > 0 && Paint.w(text) > maxW) return Tip.of(List.of(text));
            return null;
        }
    }

    static final class Tip {
        final List<Item> rows = new ArrayList<>();
        int width;

        Tip() {}

        Tip(int width) { this.width = width; }

        static Tip of(List<Component> lines) {
            if (lines == null || lines.isEmpty()) return null;
            Tip t = new Tip();
            int max = Paint.tipMax();
            int first = HontunTheme.hvanilla() || Paint.vanilla() ? 0xFFFFFFFF : Paint.text();
            for (int i = 0; i < lines.size(); i++) {
                Component c = lines.get(i);
                if (c == null) continue;
                int color = t.rows.isEmpty() ? first : Paint.sub();
                if (Paint.w(c) <= max && c.getString().indexOf('\n') < 0) t.line().raw(0, c, color);
                else t.para(c, color);
            }
            return t.rows.isEmpty() ? null : t;
        }

        Line line() {
            Line l = new Line(Paint.tipRowH());
            rows.add(l);
            return l;
        }

        Tip para(Component c, int color) {
            rows.add(new Para(c == null ? Component.empty() : c, color, 0));
            return this;
        }

        Line keyRow(String key, int kw) {
            return line().text(0, key, Paint.dim()).at(kw);
        }
    }

    abstract static class Item {
        int x, y, w, h;

        void measure(int width) {}

        void draw(GuiGraphicsExtractor g, int ox, int oy, int mx, int my) {}

        Span spanAt(int ox, int oy, int mx, int my) { return null; }

        Item innerAt(int ox, int oy, int mx, int my) { return null; }

        String copy() { return ""; }

        boolean hit(int ox, int oy, int mx, int my) {
            return mx >= ox + x && mx < ox + x + w && my >= oy + y && my < oy + y + h;
        }
    }

    private static Span spanIn(List<Item> items, int ox, int oy, int mx, int my) {
        for (Item it : items) {
            if (!it.hit(ox, oy, mx, my)) continue;
            Span s = it.spanAt(ox, oy, mx, my);
            if (s != null) return s;
        }
        return null;
    }

    private static Item deepest(List<Item> items, int ox, int oy, int mx, int my) {
        Item first = null;
        for (Item it : items) {
            if (!it.hit(ox, oy, mx, my)) continue;
            Item d = it.innerAt(ox, oy, mx, my);
            Item r = d != null ? d : it;
            if (r instanceof Line) return r;
            if (first == null) first = r;
        }
        return first;
    }

    private static String join(List<Item> items) {
        StringBuilder b = new StringBuilder();
        for (Item it : items) {
            String c = it.copy();
            if (c.isEmpty()) continue;
            if (!b.isEmpty()) b.append('\n');
            b.append(c);
        }
        return b.toString();
    }

    static final class Line extends Item {
        final List<Span> spans = new ArrayList<>();
        final List<int[]> segs = new ArrayList<>();
        boolean rowBg;
        boolean active;
        boolean card;
        boolean tick;
        boolean ruleTop, ruleBottom, hideRule;
        boolean strip;
        int ruleX, ruleW;
        int hx = -4, hw = 4;
        int groupX, groupW;
        int cur;
        Runnable act;
        Tip tip;

        Line(int h) { this.h = h; }

        Line add(Span s) { spans.add(s); return this; }

        Line tip(Tip t) { tip = t; return this; }

        Line tip(List<Component> t) { tip = Tip.of(t); return this; }

        int extent() {
            int e = 0;
            for (Span s : spans) e = Math.max(e, s.x + s.width());
            return e;
        }

        Span last() { return spans.getLast(); }

        Line text(int x, String s, int color) { return add(new Span(x, Paint.c(s), color, TEXT)); }

        Line text(int x, String s, int color, int max) { return add(new Span(x, Paint.c(s), color, TEXT).max(max)); }

        Line comp(int x, Component c, int color) { return add(new Span(x, Paint.c(c), color, TEXT)); }

        Line small(int x, String s, int color) { return add(new Span(x, Paint.sm(s), color, TEXT)); }

        Line large(int x, String s, int color) { return add(new Span(x, Paint.lg(s), color, TEXT)); }

        Line raw(int x, Component c, int color) { return add(new Span(x, c == null ? Component.empty() : c, color, RAW)); }

        Line pill(int x, String s, int color) { return add(new Span(x, Paint.c(s), color, PILL)); }

        Line pill(int x, String s, int tone, int icon) { return add(pillSpan(x, s, tone, icon)); }

        Line link(int x, String s, Runnable a) { return add(new Span(x, Paint.c(s), Paint.acc(), LINK).on(a)); }

        Line big(int x, String s, int color) { return add(new Span(x, Paint.c(s), color, BIG)); }

        Line icon(int x, int id, int color) { return add(iconSpan(x, id, color)); }

        Line sprite(int x, Identifier id, int sw, int sh) {
            Span s = new Span(x, Component.empty(), 0, SPRITE);
            s.sprite = id;
            s.sw = sw;
            s.sh = sh;
            return add(s);
        }

        Line face(int x, PlayerSkin skin) {
            Span s = new Span(x, Component.empty(), 0, FACE);
            s.skin = skin;
            return add(s);
        }

        Line box(int x, int sw, int sh, int color) {
            Span s = new Span(x, Component.empty(), color, BOX);
            s.sw = sw;
            s.sh = sh;
            return add(s);
        }

        Line bars(int lit) {
            Span s = new Span(cur, Component.empty(), 0, BARS);
            s.level = lit;
            return append(s);
        }

        Line row(Runnable a) { rowBg = true; act = a; return this; }

        Line meter(int x, int level) { return add(meterSpan(x, level, METER)); }

        Line meterL(int x, int level) { return add(meterSpan(x, level, METER_L)); }

        private static Span pillSpan(int x, String s, int tone, int icon) {
            Span sp = new Span(x, Paint.c(s), toneColor(tone), PILL);
            sp.tone = tone;
            sp.icon = icon;
            return sp;
        }

        private static Span iconSpan(int x, int id, int color) {
            Span s = new Span(x, Component.empty(), color, ICON);
            s.icon = id;
            return s;
        }

        private static Span meterSpan(int x, int level, int kind) {
            Span s = new Span(x, Component.empty(), 0, kind);
            s.level = level;
            return s;
        }

        Line at(int x) { cur = x; return this; }

        Line sp(int n) { cur += n; return this; }

        private Line append(Span s) {
            spans.add(s);
            cur += s.width();
            return this;
        }

        Line t(String s, int color) { return append(new Span(cur, Paint.c(s), color, TEXT)); }

        Line t(String s, int color, int max) { return append(new Span(cur, Paint.c(s), color, TEXT).max(max)); }

        Line ts(String s, int color) { return append(new Span(cur, Paint.sm(s), color, TEXT)); }

        Line r(Component c, int color) { return append(new Span(cur, c == null ? Component.empty() : c, color, RAW)); }

        Line r(Component c, int color, int max) { return append(new Span(cur, c == null ? Component.empty() : c, color, RAW).max(max)); }

        Line p(String s, int tone, int icon) { return append(pillSpan(cur, s, tone, icon)); }

        Line i(int id, int color) { return append(iconSpan(cur, id, color)); }

        Line m(int level) { return append(meterSpan(cur, level, METER)); }

        Line ml(int level) { return append(meterSpan(cur, level, METER_L)); }

        Line chip(String step, String range, boolean joined, boolean last) {
            Span s = new Span(cur, Paint.sm(step), Paint.dim(), CHIP);
            s.count = Paint.c(range);
            s.joined = joined;
            s.last = last;
            return append(s);
        }

        @Override
        void draw(GuiGraphicsExtractor g, int ox, int oy, int mx, int my) {
            int bx = ox + x, by = oy + y;
            boolean hover = hit(ox, oy, mx, my);
            if (strip) Paint.strip(g, bx, by, w, h);
            if (card) Paint.listCard(g, bx, by + 1, w, h - 2, hover);
            if (tick) g.fillGradient(bx, by + 2, bx + 1, by + 8, Paint.acc(), Paint.accLo());
            int rw = ruleW > 0 ? ruleW : w - 2 * ruleX;
            if (!hideRule) {
                if (!segs.isEmpty()) {
                    for (int[] sg : segs) Paint.sep(g, bx + sg[0], by, sg[1]);
                } else if (ruleTop) {
                    Paint.sep(g, bx + ruleX, by, rw);
                }
            }
            if (ruleBottom) Paint.sep(g, bx + ruleX, by + h - Paint.ruleH(), rw);
            if (rowBg) Paint.row(g, bx + hx, by, w + hw, h, active, hover);
            if (groupW > 0) Paint.tabGroup(g, bx + groupX, by, groupW, bx + w);
            String hk = null;
            if (hover) {
                for (Span s : spans) {
                    if (s.key != null && over(s, bx + s.x, by, mx, my)) {
                        hk = s.key;
                        break;
                    }
                }
            }
            for (Span s : spans) {
                int sx = bx + s.x;
                boolean fixed = s.dy != Integer.MIN_VALUE;
                int ty = fixed ? by + s.dy : by + (h - 8) / 2;
                if (s.key != null && (s.kind == TEXT || s.kind == LINK || s.kind == ICON)) {
                    float f = flash(s.key);
                    boolean hot = s.key.equals(hk);
                    int col = f >= 0 ? flashColor(f, hot ? Paint.acc() : s.color) : hot ? Paint.acc() : s.color;
                    if (s.kind == ICON) {
                        Paint.icon(g, f >= 0 && f < 0.75f && s.icon == Paint.COPY ? Paint.CHECK : s.icon, sx, ty, col);
                    } else {
                        Component t = s.shown();
                        Paint.text(g, t, sx, ty, col);
                        if (hot) g.fill(sx, ty + 9, sx + Paint.w(t), ty + 10, col);
                    }
                    continue;
                }
                switch (s.kind) {
                    case FACE -> { if (s.skin != null) PlayerFaceExtractor.extractRenderState(g, s.skin, sx, ty, 8); }
                    case BARS -> Paint.pingBars(g, sx, ty + 1, s.level);
                    case BOX -> {
                        if (s.sw > 0 && s.sh > 0) {
                            int yy = fixed ? by + s.dy : by + (h - s.sh) / 2;
                            g.fill(sx, yy, sx + s.sw, yy + s.sh, s.color);
                        }
                    }
                    case PILL -> Paint.pill(g, s.text, sx, ty, s.tone, s.icon);
                    case METER -> Paint.meter(g, sx, ty, s.level);
                    case METER_L -> Paint.meterL(g, sx, ty, s.level);
                    case ICON -> Paint.icon(g, s.icon, sx, ty, s.color);
                    case SPRITE -> {
                        if (s.sprite != null && s.sw > 0 && s.sh > 0) {
                            g.blitSprite(RenderPipelines.GUI_TEXTURED, s.sprite, sx, fixed ? by + s.dy : by + (h - s.sh) / 2, s.sw, s.sh);
                        }
                    }
                    case BIG -> Paint.big(g, s.text, sx, fixed ? by + s.dy : by + (h - 16) / 2, s.color);
                    case TAB -> Paint.tab(g, s, sx, by, over(s, sx, by, mx, my));
                    case TOGGLE -> Paint.toggle(g, s, sx, by, over(s, sx, by, mx, my));
                    case SWATCH -> Paint.swatch(g, sx, fixed ? by + s.dy : by + (h - s.sh) / 2, s.sw, s.sh, s.color);
                    case CHIP -> Paint.chip(g, s, sx, ty);
                    case LINK -> {
                        Component t = s.shown();
                        Paint.text(g, t, sx, ty, s.color);
                        int tw = Paint.w(t);
                        if (mx >= sx && mx < sx + tw && my >= by && my < by + h) g.fill(sx, ty + 9, sx + tw, ty + 10, s.color);
                    }
                    default -> Paint.text(g, s.shown(), sx, ty, s.color);
                }
            }
        }

        private boolean over(Span s, int sx, int by, int mx, int my) {
            return mx >= sx && mx < sx + s.width() && my >= by && my < by + h;
        }

        @Override
        Span spanAt(int ox, int oy, int mx, int my) {
            if (!hit(ox, oy, mx, my)) return null;
            for (Span s : spans) {
                int sx = ox + x + s.x;
                if (mx >= sx && mx < sx + s.width()) return s;
            }
            return null;
        }

        @Override
        String copy() {
            StringBuilder b = new StringBuilder();
            for (Span s : spans) {
                if (s.kind == FACE) continue;
                String t = s.text.getString();
                if (s.count != null && !s.count.getString().isEmpty()) t = t + " " + s.count.getString();
                if (t.isEmpty()) continue;
                if (!b.isEmpty()) b.append("  ");
                b.append(t);
            }
            return b.toString();
        }
    }

    static final class Para extends Item {
        final Component text;
        final int color;
        final int indent;
        int lh = 10, pad = 2, top = 1;
        List<FormattedCharSequence> lines = List.of();

        Para(Component text, int color, int indent) {
            this.text = text;
            this.color = color;
            this.indent = indent;
        }

        @Override
        void measure(int width) {
            lines = Paint.font().split(text, Math.max(40, width - indent));
            h = lines.size() * lh + pad;
        }

        int natural() {
            int nw = 0;
            for (FormattedCharSequence l : Paint.font().split(text, 1 << 20)) nw = Math.max(nw, Paint.font().width(l));
            return indent + nw;
        }

        @Override
        void draw(GuiGraphicsExtractor g, int ox, int oy, int mx, int my) {
            int ty = oy + y + top;
            for (FormattedCharSequence l : lines) {
                Paint.text(g, l, ox + x + indent, ty, color);
                ty += lh;
            }
        }

        @Override
        String copy() { return text.getString(); }
    }

    static final class Note extends Item {
        final int icon;
        final int iconColor;
        final Component text;
        final int color;
        final int maxW;
        int indent;
        List<FormattedCharSequence> lines = List.of();

        Note(int icon, int iconColor, Component text, int color, int maxW) {
            this.icon = icon;
            this.iconColor = iconColor;
            this.text = text == null ? Component.empty() : text;
            this.color = color;
            this.maxW = maxW;
        }

        private int indentFor() {
            if (icon < 0) return 0;
            if (Paint.term()) {
                int gw = icon == Paint.INFO ? Paint.w(Paint.c("#")) : Paint.iconW(icon);
                return gw > 0 ? gw + 6 : 0;
            }
            if (Paint.vanilla()) {
                int gw = Paint.iconW(icon);
                return gw > 0 ? gw + 5 : 0;
            }
            return 13;
        }

        @Override
        void measure(int width) {
            indent = indentFor();
            int avail = Math.min(width, maxW > 0 ? maxW : width) - indent;
            boolean smog = HontunTheme.smog();
            lines = Paint.font().split(smog ? Paint.sm(text) : text, Math.max(20, avail));
            int n = Math.max(1, lines.size());
            h = smog ? (n - 1) * 10 + 8 : n * 10;
        }

        @Override
        void draw(GuiGraphicsExtractor g, int ox, int oy, int mx, int my) {
            int bx = ox + x, by = oy + y;
            if (icon >= 0) {
                if (Paint.term()) {
                    if (icon == Paint.INFO) Paint.text(g, Paint.c("#"), bx, by, Paint.dim());
                    else Paint.icon(g, icon, bx, by, iconColor);
                } else if (HontunTheme.modern2() || HontunTheme.modern1()) {
                    Paint.icon(g, icon, bx, by + 1, iconColor);
                } else {
                    Paint.icon(g, icon, bx, by, iconColor);
                }
            }
            int ty = by;
            for (FormattedCharSequence l : lines) {
                Paint.text(g, l, bx + indent, ty, color);
                ty += 10;
            }
        }

        @Override
        String copy() { return text.getString(); }
    }

    static final class Gap extends Item {
        Gap(int h) { this.h = h; }
    }

    static final class Sep extends Item {
        final int at;
        final int kind;
        int inset, len;
        boolean bleed;

        Sep() { this(9, 4, RULE); }

        Sep(int h, int at, int kind) {
            this.h = h;
            this.at = at;
            this.kind = kind;
        }

        @Override
        void draw(GuiGraphicsExtractor g, int ox, int oy, int mx, int my) {
            int sx = ox + x + inset, sw = len > 0 ? len : w - 2 * inset;
            if (bleed) {
                sx -= Paint.pad();
                sw += 2 * Paint.pad();
            }
            int sy = oy + y + at;
            switch (kind) {
                case FADE -> Paint.fadeSep(g, sx, sy, sw);
                case RULE_F -> Paint.sepF(g, sx, sy, sw);
                default -> Paint.sep(g, sx, sy, sw);
            }
        }
    }

    static final class Bar extends Item {
        final List<int[]> segs = new ArrayList<>();

        Bar(int h) { this.h = h; }

        @Override
        void draw(GuiGraphicsExtractor g, int ox, int oy, int mx, int my) {
            for (int[] s : segs) Paint.weightSeg(g, ox + x + s[0], oy + y, s[1], h, s[2]);
        }
    }

    static final class PingChain extends Item {
        final List<Integer> ids;
        final int sure, cw, cy, ny, by;
        final Component[] cells, nums, steps;

        PingChain(List<Integer> ids, int sure) {
            this.ids = List.copyOf(ids);
            int n = this.ids.size();
            this.sure = Math.max(0, Math.min(n, sure));
            cells = new Component[n];
            nums = new Component[n];
            steps = new Component[Math.max(0, n - 1)];
            int tw = 0;
            for (int i = 0; i < n; i++) {
                cells[i] = Paint.c(String.valueOf(this.ids.get(i)));
                tw = Math.max(tw, Paint.w(cells[i]));
                nums[i] = Paint.sm(String.valueOf(i + 1));
                if (i > 0) {
                    long st = (long) this.ids.get(i) - this.ids.get(i - 1);
                    steps[i - 1] = Paint.sm((st > 0 ? "+" : "") + st);
                }
            }
            cw = Math.max(tw + Paint.chainPad(), 20 + Math.floorMod(20 - tw, 2));
            int[] o = Paint.chainGeo();
            cy = o[0];
            ny = cy + 20 + o[1];
            by = ny + o[2];
            h = by + o[3];
        }

        int cx(int i, int width) {
            int n = cells.length;
            return n > 1 ? Math.round(i * (width - cw) / (float) (n - 1)) : 0;
        }

        @Override
        void draw(GuiGraphicsExtractor g, int ox, int oy, int mx, int my) {
            int bx = ox + x, top = oy + y, n = cells.length;
            int[] in = Paint.chainInsets();
            int wy = top + cy + 10, sy = top + cy + Paint.chainStepDy();
            for (int i = 0; i < n - 1; i++) {
                boolean on = i + 1 >= sure;
                int x0 = cx(i, w) + cw, jw = cx(i + 1, w) - x0, lw = Paint.w(steps[i]);
                if (jw <= 0) continue;
                if (lw + in[0] + in[1] + in[2] + in[3] + 2 > jw) {
                    Paint.chainWire(g, bx + x0 + in[0], bx + x0 + jw - in[3], wy, on);
                    continue;
                }
                int lx = x0 + Math.floorDiv(jw - lw, 2);
                Paint.text(g, steps[i], bx + lx, sy, Paint.chainStep(on));
                Paint.chainWire(g, bx + x0 + in[0], bx + lx - in[1], wy, on);
                Paint.chainWire(g, bx + lx + lw + in[2], bx + x0 + jw - in[3], wy, on);
            }
            for (int i = 0; i < n; i++) {
                boolean on = i >= sure;
                int cx = bx + cx(i, w);
                Paint.chainCell(g, cx, top + cy, cw, 20, on);
                Paint.text(g, cells[i], cx + Math.floorDiv(cw - Paint.w(cells[i]), 2), top + cy + 6, Paint.chainText(on));
                Paint.text(g, nums[i], cx + Math.floorDiv(cw - Paint.w(nums[i]), 2), top + ny, Paint.chainIndex(on));
            }
            if (sure > 0) Paint.chainBracket(g, bx, top + by, cx(sure - 1, w) + cw, false);
            if (sure < n) Paint.chainBracket(g, bx + cx(sure, w), top + by, cx(n - 1, w) + cw - cx(sure, w), true);
        }

        @Override
        String copy() {
            StringBuilder b = new StringBuilder();
            for (Integer id : ids) {
                if (!b.isEmpty()) b.append(' ');
                b.append(id);
            }
            return b.toString();
        }
    }

    static final class Route extends Item {
        final int[] xs;
        final int dot, top;

        Route(int[] xs, int dot, int top) {
            this.xs = xs;
            this.dot = dot;
            this.top = top;
            this.h = top + dot;
        }

        @Override
        void draw(GuiGraphicsExtractor g, int ox, int oy, int mx, int my) {
            int bx = ox + x, my0 = oy + y + top, gap = Paint.routeGap();
            for (int i = 0; i < xs.length; i++) {
                if (i < xs.length - 1) Paint.routeWire(g, bx + xs[i] + dot + gap, bx + xs[i + 1] - gap, my0 + 3);
                Paint.routeMark(g, bx + xs[i], my0, i == 0);
            }
        }
    }

    static final class VRule extends Item {
        VRule(int x, int y, int h) {
            this.x = x;
            this.y = y;
            this.h = h;
        }

        @Override
        void draw(GuiGraphicsExtractor g, int ox, int oy, int mx, int my) {
            Paint.vsep(g, ox + x, oy + y, h);
        }
    }

    static final class Well extends Item {
        final List<Item> inner = new ArrayList<>();
        int cols;
        int style = WELL;
        boolean openTop;

        @Override
        void measure(int width) {
            if (style == PANEL) {
                int[] ins = Paint.rowInset();
                int iy = 1;
                for (Item it : inner) {
                    it.x = 0;
                    it.w = width;
                    if (it instanceof Line l) {
                        l.hx = ins[0];
                        l.hw = -ins[1];
                    }
                    it.measure(width);
                    it.y = iy;
                    iy += it.h;
                }
                h = iy + 1;
                return;
            }
            int iy = 4;
            for (Item it : inner) {
                it.x = 6;
                it.w = width - 12;
                it.measure(it.w);
                it.y = iy;
                iy += it.h;
            }
            h = iy + 4;
        }

        @Override
        void draw(GuiGraphicsExtractor g, int ox, int oy, int mx, int my) {
            if (style == PANEL) {
                Paint.panel(g, ox + x, oy + y, w, h, !openTop);
                int hov = -1;
                for (int i = 0; i < inner.size(); i++) {
                    if (inner.get(i) instanceof Line l && (l.rowBg || l.card) && l.hit(ox + x, oy + y, mx, my)) {
                        hov = i;
                        break;
                    }
                }
                for (int i = 0; i < inner.size(); i++) {
                    if (inner.get(i) instanceof Line l) l.hideRule = hov >= 0 && (i == hov || i == hov + 1);
                }
            } else {
                Paint.well(g, ox + x, oy + y, w, h);
                for (int i = 1; i < cols; i++) Paint.vsep(g, ox + x + 6 + i * ((w - 12) / cols), oy + y + 6, h - 12);
            }
            for (Item it : inner) it.draw(g, ox + x, oy + y, mx, my);
        }

        @Override
        Item innerAt(int ox, int oy, int mx, int my) {
            return deepest(inner, ox + x, oy + y, mx, my);
        }

        @Override
        Span spanAt(int ox, int oy, int mx, int my) {
            return spanIn(inner, ox + x, oy + y, mx, my);
        }

        @Override
        String copy() { return join(inner); }
    }

    static final class Box extends Item {
        final List<Item> inner = new ArrayList<>();
        int style = NONE;
        boolean bleed;
        boolean ruleBottom;
        int[] vr;
        int vrTop, vrH;

        Box(int h) { this.h = h; }

        Box put(Item it, int x, int y, int w) {
            it.x = x;
            it.y = y;
            it.w = w;
            inner.add(it);
            return this;
        }

        @Override
        void measure(int width) {
            for (Item it : inner) it.measure(it.w);
        }

        @Override
        void draw(GuiGraphicsExtractor g, int ox, int oy, int mx, int my) {
            int bx = ox + x, by = oy + y;
            int fx = bleed ? bx - Paint.pad() : bx, fw = bleed ? w + 2 * Paint.pad() : w;
            if (style == BAND) Paint.band(g, fx, by, fw, h);
            else if (style == PANEL) Paint.panel(g, fx, by, fw, h);
            if (ruleBottom) Paint.sep(g, fx, by + h - Paint.ruleH(), fw);
            if (vr != null) {
                int vh = vrH > 0 ? vrH : h - 2 * vrTop;
                for (int v : vr) Paint.vsep(g, bx + v, by + vrTop, vh);
            }
            for (Item it : inner) it.draw(g, bx, by, mx, my);
        }

        @Override
        Item innerAt(int ox, int oy, int mx, int my) {
            return deepest(inner, ox + x, oy + y, mx, my);
        }

        @Override
        Span spanAt(int ox, int oy, int mx, int my) {
            return spanIn(inner, ox + x, oy + y, mx, my);
        }

        @Override
        String copy() { return join(inner); }
    }

    Item add(Item it) { full.add(it); return it; }

    void layout(int width, int gap) {
        extra.clear();
        int y = 0;
        for (Item it : full) {
            it.x = 0;
            it.w = width;
            it.measure(width);
            it.y = y;
            y += it.h;
        }
        if (!left.isEmpty() || !right.isEmpty()) {
            int lcw, rcx;
            if (lw >= 0 || rx >= 0) {
                lcw = lw >= 0 ? lw : Math.max(0, rx - gap);
                rcx = rx >= 0 ? rx : lcw + gap;
            } else {
                lcw = (width - gap) * 3 / 5;
                rcx = lcw + gap;
            }
            int rcw = width - rcx;
            int top = y, ly = y, ry = y;
            for (Item it : left) {
                it.x = 0;
                it.w = lcw;
                it.measure(lcw);
                it.y = ly;
                ly += it.h;
            }
            for (Item it : right) {
                it.x = rcx;
                it.w = rcw;
                it.measure(rcw);
                it.y = ry;
                ry += it.h;
            }
            y = Math.max(ly, ry);
            if (colRule >= 0 && y > top) extra.add(new VRule(colRule, top, y - top));
        }
        height = y;
    }

    List<Item> all() {
        List<Item> out = new ArrayList<>(full);
        out.addAll(left);
        out.addAll(right);
        out.addAll(extra);
        return out;
    }

    Span spanAt(int ox, int oy, int mx, int my) {
        return spanIn(all(), ox, oy, mx, my);
    }

    Line lineAt(int ox, int oy, int mx, int my) {
        for (Item it : all()) {
            if (!it.hit(ox, oy, mx, my)) continue;
            if (it instanceof Line l) return l;
            if (it.innerAt(ox, oy, mx, my) instanceof Line l) return l;
        }
        return null;
    }

    String copy() {
        StringBuilder b = new StringBuilder();
        for (Item it : all()) {
            String c = it.copy();
            if (c.isEmpty()) continue;
            b.append(c).append('\n');
        }
        return b.toString().stripTrailing();
    }
}
