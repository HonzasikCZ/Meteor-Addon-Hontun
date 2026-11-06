package cz.honzasik.hontun.gui.widget;

import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.gui.render.pixel.QuadBatchState;
import cz.honzasik.hontun.gui.theme.style.terminal.TerminalPalette;
import cz.honzasik.hontun.utils.HontunFont;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2fStack;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Locale;

public final class TerminalWidgets {
    private static final int INACTIVE_GREY = 0xA0A0A0;

    private static final float MONO_SIZE = 9.0f;
    private static final float MONO_SHIFT = 0.5f;
    private static final float BASELINE = 7.0f + MONO_SHIFT;
    private static final float CELL_TOP = BASELINE - 0.83f * MONO_SIZE;
    private static final float CELL_BOTTOM = BASELINE + 0.11f * MONO_SIZE;
    private static final float CAP_MID = BASELINE - 0.365f * MONO_SIZE;
    private static final String ELLIPSIS = "…";

    public static final int LINE_STEP = 11;

    private TerminalWidgets() {}

    public static final class Px {
        private final QuadBatchState.Builder b = QuadBatchState.builder();
        private final float s;

        public Px() {
            double gs = Minecraft.getInstance().getWindow().getGuiScale();
            s = gs > 0 ? (float) gs : 1f;
        }

        public Px rect(float x, float y, float w, float h, int argb) {
            if (w <= 0 || h <= 0) return this;
            b.add(Math.round(x * s), Math.round(y * s), Math.round((x + w) * s), Math.round((y + h) * s), argb);
            return this;
        }

        public Px frame(float x, float y, float w, float h, int argb) {
            if (w <= 0 || h <= 0) return this;
            float x0 = Math.round(x * s), y0 = Math.round(y * s);
            float x1 = Math.round((x + w) * s), y1 = Math.round((y + h) * s);
            b.add(x0, y0, x1, y0 + 1, argb);
            b.add(x0, y1 - 1, x1, y1, argb);
            b.add(x0, y0 + 1, x0 + 1, y1 - 1, argb);
            b.add(x1 - 1, y0 + 1, x1, y1 - 1, argb);
            return this;
        }

        public Px hline(float x0, float x1, float y, int argb) {
            float py = Math.round(y * s);
            b.add(Math.round(x0 * s), py, Math.round(x1 * s), py + 1, argb);
            return this;
        }

        public Px vline(float x, float y0, float y1, int argb) {
            float px = Math.round(x * s);
            b.add(px, Math.round(y0 * s), px + 1, Math.round(y1 * s), argb);
            return this;
        }

        public void submit(GuiGraphicsExtractor g) {
            Matrix3x2fStack pose = g.pose();
            pose.pushMatrix();
            pose.scale(1f / s, 1f / s);
            b.submit(g);
            pose.popMatrix();
        }
    }

    public static void ensure() {
        TerminalPalette.ensure();
    }

    public static int o(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    public static int a(int alpha, int rgb) {
        return TerminalPalette.argb(alpha, rgb);
    }

    public static int fg()    { ensure(); return o(TerminalPalette.fg); }
    public static int acc()   { ensure(); return o(TerminalPalette.acc); }
    public static int dim()   { ensure(); return o(TerminalPalette.dim); }
    public static int faint() { ensure(); return o(TerminalPalette.faint); }
    public static int ink()   { ensure(); return o(TerminalPalette.ink); }
    public static int fill()  { ensure(); return o(TerminalPalette.fill); }
    public static int line()  { ensure(); return o(TerminalPalette.line); }
    public static int lineF() { ensure(); return o(TerminalPalette.lineF); }
    public static int empty() { ensure(); return o(TerminalPalette.empty); }
    public static int head()  { ensure(); return o(TerminalPalette.head); }
    public static int red()   { ensure(); return o(TerminalPalette.red); }
    public static int bg()    { ensure(); return o(TerminalPalette.bg); }
    public static int win()   { ensure(); return TerminalPalette.win(); }
    public static int barA()  { ensure(); return TerminalPalette.barA(); }
    public static int hl()    { ensure(); return TerminalPalette.hl(); }
    public static int sel()   { ensure(); return TerminalPalette.sel(); }

    public static Font font() {
        return Minecraft.getInstance().font;
    }

    public static int cw(Font f) {
        return Math.max(1, f.width(HontunFont.text("0")));
    }

    public static int cw() {
        return cw(font());
    }

    public static boolean blinkOn(long ms) {
        if (Hontun.THEME != null && !Hontun.THEME.terminalCursorBlink.get()) return true;
        return Math.floorMod(ms, 1060L) < 530L;
    }

    public static boolean blinkOn() {
        return blinkOn(System.currentTimeMillis());
    }

    public static void text(GuiGraphicsExtractor g, Font f, String s, int x, int y, int argb) {
        if (s == null || s.isEmpty()) return;
        g.text(f, HontunFont.text(s), x, y, argb, false);
    }

    public static int width(Font f, String s) {
        return s == null || s.isEmpty() ? 0 : f.width(HontunFont.text(s));
    }

    public static int lineY(int top, int h) {
        return Math.round(top + h / 2f - CAP_MID);
    }

    public static int stackY(int top, int h, int lines, int step) {
        return Math.round(top + h / 2f - CAP_MID - Math.max(0, lines - 1) * step / 2f);
    }

    public static void cursor(GuiGraphicsExtractor g, float x, int ty, float w, int argb) {
        if (w <= 0) return;
        new Px().rect(x, ty + CELL_TOP, w, CELL_BOTTOM - CELL_TOP, argb).submit(g);
    }

    public static float advance(Font f, Component c) {
        return c == null ? 0f : f.getSplitter().stringWidth(c);
    }

    public static float advance(Font f, FormattedCharSequence s) {
        return s == null ? 0f : f.getSplitter().stringWidth(s);
    }

    public static void textAt(GuiGraphicsExtractor g, Font f, FormattedCharSequence s, float x, int y, int argb) {
        if (s == null) return;
        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        g.text(f, s, 0, 0, argb, false);
        pose.popMatrix();
    }

    public static void textAt(GuiGraphicsExtractor g, Font f, String s, float x, int y, int argb) {
        if (s == null || s.isEmpty()) return;
        textAt(g, f, HontunFont.text(s).getVisualOrderText(), snap(x, scale()), y, argb);
    }

    public static void glyph(GuiGraphicsExtractor g, Font f, String s, float cx, float cy, int argb) {
        if (s == null || s.isEmpty()) return;
        Component c = HontunFont.text(s);
        float sc = scale();
        float ox = snap(cx - f.getSplitter().stringWidth(c) / 2f, sc);
        float oy = snap(cy - (BASELINE - inkMid(s) * MONO_SIZE), sc);
        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.translate(ox, oy);
        g.text(f, c, 0, 0, argb, false);
        pose.popMatrix();
    }

    private static float inkMid(String s) {
        return switch (s) {
            case "x", "v" -> 0.275f;
            case ">", "<" -> 0.33f;
            case "^" -> 0.535f;
            default -> 0.365f;
        };
    }

    private static float snap(float v, float sc) {
        return Math.round(v * sc) / sc;
    }

    public static String clipString(Font f, String s, int maxW) {
        if (s == null || maxW <= 0) return "";
        if (width(f, s) <= maxW) return s;
        int room = Math.max(0, maxW - width(f, ELLIPSIS));
        return f.getSplitter().plainHeadByWidth(s, room, Style.EMPTY.withFont(HontunFont.MONO)) + ELLIPSIS;
    }

    public static Component label(AbstractWidget w, Component msg) {
        Integer rgb = labelColor(w);
        TextColor cur = msg.getStyle().getColor();
        boolean recolor = rgb != null && (cur == null || (cur.getValue() & 0xFFFFFF) == INACTIVE_GREY);
        MutableComponent out = msg.copy();
        if (recolor) {
            TextColor tc = TextColor.fromRgb(rgb);
            return out.withStyle(st -> st.withFont(HontunFont.MONO).withColor(tc));
        }
        return out.withStyle(st -> st.withFont(HontunFont.MONO));
    }

    private static Integer labelColor(AbstractWidget w) {
        ensure();
        boolean active = w.active;
        boolean hot = active && w.isHoveredOrFocused();
        if (w instanceof PlainTextButton) {
            return !active ? TerminalPalette.faint : hot ? TerminalPalette.acc : TerminalPalette.dim;
        }
        if (w instanceof AbstractButton) {
            if (!active) return TerminalPalette.faint;
            if (w instanceof HontunDotButton d && d.selected()) return TerminalPalette.ink;
            return hot ? TerminalPalette.acc : TerminalPalette.fg;
        }
        if (w instanceof AbstractSliderButton) {
            return !active ? TerminalPalette.faint : hot ? TerminalPalette.acc : TerminalPalette.fg;
        }
        return null;
    }

    public static void button(GuiGraphicsExtractor g, AbstractWidget w, boolean selected) {
        int x = w.getX(), y = w.getY(), bw = w.getWidth(), bh = w.getHeight();
        if (bw <= 2 || bh <= 2) return;
        ensure();

        boolean active = w.active;
        boolean hot = active && w.isHoveredOrFocused();
        boolean sel = active && selected;

        Px p = new Px();
        if (sel) {
            p.rect(x, y, bw, bh, o(TerminalPalette.fill));
            if (hot) p.frame(x, y, bw, bh, o(TerminalPalette.head));
        } else {
            p.rect(x, y, bw, bh, TerminalPalette.win());
            if (hot) p.rect(x, y, bw, bh, TerminalPalette.hl());
            p.frame(x, y, bw, bh, !active ? a(0x80, TerminalPalette.line)
                    : hot ? o(TerminalPalette.lineF) : o(TerminalPalette.line));
        }
        p.submit(g);

        if (w instanceof SpriteIconButton) return;
        Component msg = w.getMessage();
        if (msg == null || msg.getString().isBlank()) return;

        Font f = font();
        int tw = f.width(msg);
        int left = x + 2, right = x + bw - 2;
        if (tw > right - left) return;

        int ty = lineY(y, bh);
        int cw = cw(f);
        int bc = !active ? TerminalPalette.faint : sel ? TerminalPalette.ink : hot ? TerminalPalette.acc : TerminalPalette.dim;

        if (bw >= tw + 6 * cw) {
            text(g, f, "[", x + cw, ty, o(bc));
            if (!badgeCovers(w)) {
                textAt(g, f, "]", x + bw - cw - advance(f, HontunFont.text("]")), ty, o(bc));
            }
        }

        if (hot && !sel && blinkOn()) {
            int tx = (left + right) / 2 - tw / 2;
            float cx = tx + advance(f, msg) + 1;
            if (cx + cw <= right) cursor(g, cx, ty, cw, o(TerminalPalette.fill));
        }
    }

    private static final ClassValue<Boolean> MOD_MENU = new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> type) {
            return type.getName().startsWith("com.terraformersmc.modmenu.");
        }
    };
    private static volatile boolean modMenuLooked;
    private static MethodHandle modMenuUpdates;
    private static MethodHandle modMenuBadgeOption;

    private static boolean badgeCovers(AbstractWidget w) {
        if (!MOD_MENU.get(w.getClass())) return false;
        if (!modMenuLooked) {
            synchronized (TerminalWidgets.class) {
                if (!modMenuLooked) {
                    try {
                        Class<?> mod = Class.forName("com.terraformersmc.modmenu.ModMenu");
                        modMenuUpdates = MethodHandles.publicLookup()
                                .findStatic(mod, "areModUpdatesAvailable", MethodType.methodType(boolean.class));
                    } catch (Throwable ignored) {
                        modMenuUpdates = null;
                    }
                    try {
                        Class<?> cfg = Class.forName("com.terraformersmc.modmenu.config.ModMenuConfig");
                        Object option = cfg.getField("BUTTON_UPDATE_BADGE").get(null);
                        modMenuBadgeOption = MethodHandles.publicLookup()
                                .findVirtual(option.getClass(), "getValue", MethodType.methodType(boolean.class))
                                .bindTo(option);
                    } catch (Throwable ignored) {
                        modMenuBadgeOption = null;
                    }
                    modMenuLooked = true;
                }
            }
        }
        try {
            if (modMenuBadgeOption != null && !(boolean) modMenuBadgeOption.invoke()) return false;
            return modMenuUpdates == null || (boolean) modMenuUpdates.invoke();
        } catch (Throwable t) {
            return true;
        }
    }

    public static void field(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean focused, boolean hovered, boolean active) {
        if (w <= 2 || h <= 2) return;
        ensure();
        Px p = new Px();
        p.rect(x, y, w, h, TerminalPalette.win());
        int border = !active ? a(0x80, TerminalPalette.line)
                : focused ? o(TerminalPalette.lineF)
                : hovered ? TerminalPalette.argb(0xFF, TerminalPalette.mix(TerminalPalette.line, TerminalPalette.lineF, 0.5))
                : o(TerminalPalette.line);
        p.frame(x, y, w, h, border);
        p.submit(g);
    }

    public static void appendCaret(GuiGraphicsExtractor g, Font f, float x, int y) {
        cursor(g, x, y, cw(f), fill());
    }

    public static void selection(GuiGraphicsExtractor g, float x0, float x1, int ty) {
        float l = Math.min(x0, x1), r = Math.max(x0, x1);
        if (r <= l) return;
        new Px().rect(l, ty + CELL_TOP, r - l, CELL_BOTTOM - CELL_TOP, sel()).submit(g);
    }

    public static void slider(GuiGraphicsExtractor g, int x, int y, int w, int h, double frac,
                              boolean active, boolean hot, boolean focused) {
        if (w <= 2 || h <= 2) return;
        ensure();
        Px p = new Px();
        p.rect(x, y, w, h, TerminalPalette.win());
        if (hot) p.rect(x, y, w, h, TerminalPalette.hl());
        p.frame(x, y, w, h, !active ? a(0x80, TerminalPalette.line)
                : hot || focused ? o(TerminalPalette.lineF) : o(TerminalPalette.line));

        int cw = cw();
        int pad = 3;
        int span = w - 2 * pad;
        int n = Math.max(1, (span + 1) / cw);
        int used = n * cw - 1;
        int sx = x + pad + (span - used) / 2;
        int sy = y + h - 4;
        double fr = Math.max(0, Math.min(1, frac));
        int filled = (int) Math.floor(fr * n);
        if (filled >= n) filled = n - 1;
        for (int i = 0; i < n; i++) {
            int col;
            if (!active) col = a(0x80, TerminalPalette.empty);
            else if (i < filled) col = o(TerminalPalette.fill);
            else if (i == filled) col = o(TerminalPalette.head);
            else col = o(TerminalPalette.empty);
            p.rect(sx + i * cw, sy, cw - 1, 2, col);
        }
        p.submit(g);
    }

    public static void segments(GuiGraphicsExtractor g, float x, float y, float w, float h, double from, double to, float alpha) {
        ensure();
        int cw = cw();
        int n = Math.max(1, (int) Math.floor((w + 1) / cw));
        float used = n * cw - 1;
        float sx = x + (w - used) / 2f;
        double lo = Math.max(0, Math.min(1, from)) * n;
        double hi = Math.max(0, Math.min(1, to)) * n;
        int ea = Math.round(255 * Math.max(0f, Math.min(1f, alpha)));
        float sh = Math.max(1f, h);
        Px p = new Px();
        for (int i = 0; i < n; i++) {
            float px = sx + i * cw;
            p.rect(px, y, cw - 1, sh, a(ea, TerminalPalette.empty));
            double a0 = Math.max(lo, i), a1 = Math.min(hi, i + 1);
            if (a1 > a0) {
                float fx = (float) ((a0 - i) * (cw - 1));
                float fw = (float) ((a1 - a0) * (cw - 1));
                p.rect(px + fx, y, fw, sh, a(ea, TerminalPalette.fill));
            }
        }
        p.submit(g);
    }

    public static void scrollbar(GuiGraphicsExtractor g, int x, int top, int bot, int w, int thumbTop, int thumbH) {
        if (w <= 0 || bot <= top || thumbH <= 0) return;
        ensure();
        int bx = x + Math.max(0, (w - 2) / 2);
        g.fill(bx, thumbTop, bx + 2, thumbTop + thumbH, a(0x8C, TerminalPalette.acc));
    }

    public static void listColumn(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) return;
        ensure();
        new Px().rect(x, y, w, h, TerminalPalette.win()).frame(x, y, w, h, o(TerminalPalette.line)).submit(g);
    }

    public static void listSeparators(GuiGraphicsExtractor g, int x, int top, int bottom, int w) {
        ensure();
        new Px().hline(x, x + w, top - 1, o(TerminalPalette.line)).hline(x, x + w, bottom, o(TerminalPalette.line)).submit(g);
    }

    public static int markerSpace() {
        return cw() + 4;
    }

    public static void row(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean active, boolean hovered) {
        ensure();
        Px p = new Px();
        if (active) p.rect(x, y, w, h, TerminalPalette.sel());
        else if (hovered) p.rect(x, y, w, h, TerminalPalette.hl());
        if (active) {
            int cw = cw();
            p.rect(x - cw - 3, y + 2, cw, Math.max(1, h - 4), o(TerminalPalette.fill));
        }
        p.submit(g);
    }

    public static void cell(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean current, boolean hovered) {
        ensure();
        Px p = new Px();
        if (current) {
            p.rect(x, y, w, h, o(TerminalPalette.fill));
            if (hovered) p.frame(x, y, w, h, o(TerminalPalette.head));
        } else {
            if (hovered) p.rect(x, y, w, h, TerminalPalette.hl());
            p.frame(x, y, w, h, hovered ? o(TerminalPalette.lineF) : o(TerminalPalette.line));
        }
        p.submit(g);
    }

    public static boolean deleteButton(GuiGraphicsExtractor g, Font f, int x, int y, int size, int mouseX, int mouseY) {
        ensure();
        boolean over = mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;
        if (over) {
            new Px().rect(x, y, size, size, a(0x30, TerminalPalette.red)).frame(x, y, size, size, o(TerminalPalette.red)).submit(g);
        }
        glyph(g, f, "x", x + size / 2f, y + size / 2f, o(over ? TerminalPalette.red : TerminalPalette.dim));
        return over;
    }

    public static void frame(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean filled) {
        ensure();
        Px p = new Px();
        if (filled) p.rect(x, y, w, h, TerminalPalette.win());
        p.frame(x, y, w, h, o(TerminalPalette.line));
        p.submit(g);
    }

    public static void legendFrame(GuiGraphicsExtractor g, int x, int y, int w, int h, int gapCenter, int gapWidth) {
        ensure();
        int gl = gapCenter - gapWidth / 2, gr = gl + gapWidth;
        int c = o(TerminalPalette.line);
        Px p = new Px();
        p.rect(x, y, w, h, TerminalPalette.win());
        if (gl > x && gr < x + w) {
            p.hline(x, gl, y, c);
            p.hline(gr, x + w, y, c);
        } else {
            p.hline(x, x + w, y, c);
        }
        p.hline(x, x + w, y + h - 1f / scale(), c);
        p.vline(x, y, y + h, c);
        p.vline(x + w - 1f / scale(), y, y + h, c);
        p.submit(g);
    }

    private static float scale() {
        double gs = Minecraft.getInstance().getWindow().getGuiScale();
        return gs > 0 ? (float) gs : 1f;
    }

    public static String path(Component title) {
        String s = title == null ? "" : title.getString().trim().toLowerCase(Locale.ROOT).replace(' ', '-');
        return s.isEmpty() ? "~" : s;
    }

    public static void screenChrome(GuiGraphicsExtractor g, Font f, int width, int height, int topH, int bottomY, Component title) {
        ensure();
        Px p = new Px();
        p.rect(0, 0, width, topH, TerminalPalette.barA());
        p.hline(0, width, topH, o(TerminalPalette.line));
        if (bottomY < height) {
            p.rect(0, bottomY, width, height - bottomY, TerminalPalette.barA());
            p.hline(0, width, bottomY, o(TerminalPalette.line));
        }
        p.submit(g);

        String pre = "~/";
        String name = clipString(f, path(title), width - 8 - width(f, pre));
        int tw = width(f, pre) + width(f, name);
        int tx = width / 2 - tw / 2;
        int ty = lineY(0, topH);
        text(g, f, pre, tx, ty, o(TerminalPalette.dim));
        text(g, f, name, tx + width(f, pre), ty, o(TerminalPalette.acc));
    }

    public static void avatarFrame(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        ensure();
        new Px().frame(x, y, w, h, o(TerminalPalette.line)).submit(g);
    }

    public static void hotbarFrame(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        ensure();
        Px p = new Px();
        p.rect(x, y, w, h, a(0xD8, TerminalPalette.bg));
        p.frame(x, y, w, h, o(TerminalPalette.line));
        for (int i = 0; i < 9; i++) {
            p.frame(x + 2 + i * 20, y + 2, 18, 18, o(TerminalPalette.empty));
        }
        p.submit(g);
    }

    public static void hotbarSelection(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        ensure();
        Px p = new Px();
        p.rect(x + 3, y + 3, 18, 18, TerminalPalette.hl());
        p.frame(x + 2, y + 2, 20, 20, o(TerminalPalette.fill));
        p.rect(x + 3, y + h - 2, 18, 2, o(TerminalPalette.fill));
        p.submit(g);
    }

    public static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        ensure();
        new Px().rect(x, y, w, h, a(0xF2, TerminalPalette.bg)).frame(x, y, w, h, o(TerminalPalette.line)).submit(g);
    }

    public static void slot(GuiGraphicsExtractor g, int x, int y, int size) {
        ensure();
        new Px().rect(x, y, size, size, o(TerminalPalette.mix(TerminalPalette.bg, 0x000000, 0.35)))
                .frame(x, y, size, size, o(TerminalPalette.empty)).submit(g);
    }

    public static void well(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        ensure();
        new Px().rect(x, y, w, h, o(TerminalPalette.mix(TerminalPalette.bg, 0x000000, 0.35)))
                .frame(x, y, w, h, o(TerminalPalette.line)).submit(g);
    }

    public static void slotHighlight(GuiGraphicsExtractor g, int x, int y, int size, boolean front) {
        ensure();
        if (front) {
            g.fill(x + 1, y + 1, x + size - 1, y + size - 1, a(0x18, TerminalPalette.acc));
            return;
        }
        new Px().rect(x, y, size, size, TerminalPalette.hl()).frame(x, y, size, size, o(TerminalPalette.lineF)).submit(g);
    }
}
