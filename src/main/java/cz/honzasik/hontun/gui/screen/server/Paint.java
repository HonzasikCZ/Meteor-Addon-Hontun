package cz.honzasik.hontun.gui.screen.server;

import cz.honzasik.hontun.gui.render.RoundedGui;
import cz.honzasik.hontun.gui.widget.HontunCards;
import cz.honzasik.hontun.gui.widget.HontunRound;
import cz.honzasik.hontun.gui.widget.HontunShapes;
import cz.honzasik.hontun.gui.theme.style.terminal.TerminalPalette;
import cz.honzasik.hontun.gui.widget.TerminalWidgets;
import cz.honzasik.hontun.utils.HontunFont;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

final class Paint {
    private Paint() {}

    static final int OVERVIEW = 0, SOFTWARE = 1, ANTICHEAT = 2, VERSION = 3, PLUGINS = 4, PLAYERS = 5, WORLD = 6,
            CONNECTION = 7, CHANNELS = 8, COOKIES = 9, COPY = 10, CLOSE = 11, INFO = 12, CHECK = 13, SHIELD = 14,
            BUILTIN = 15, CONFIG = 16, FILE = 17, PLUS = 18, MINUS = 19, DOT = 20, TILDE = 21, SEARCH = 22,
            REFRESH = 23, WARN = 24, EXPAND = 25, SAVE = 26, FOLDER = 27;

    private static final String[][] ICONS = {
        {"###.###", "#+#.#+#", "###.###", ".......", "###.###", "#+#.#+#", "###.###"},
        {"#######", "#+#+++#", "#######", ".......", "#######", "#+#+++#", "#######"},
        {"#######", "#+++++#", "#+++++#", "#+++++#", ".#+++#.", "..#+#..", "...#..."},
        {".......", "#####..", "#++++#.", "#+.+++#", "#++++#.", "#####..", "......."},
        {".#...#.", ".#...#.", "#######", "#+++++#", ".#+++#.", "..###..", "...#..."},
        {"+++++++", "+++++++", "+#####+", "#.###.#", "#######", "##+++##", "#######"},
        {"#######", "#######", "#+##+##", "++#++#+", "+++++++", "+++++++", "+++++++"},
        {"......#", "......#", "....#.#", "....#.#", "..#.#.#", "..#.#.#", "#.#.#.#"},
        {".......", "#######", "##+++##", "#+#+#+#", "#++#++#", "#######", "......."},
        {"..###..", ".###+#.", "#+#####", "###+###", "#####+#", ".#+###.", "..###.."},
        {"..#####", "..#...#", "#####.#", "#+++#.#", "#+++###", "#+++#..", "#####.."},
        {".......", ".#...#.", "..#.#..", "...#...", "..#.#..", ".#...#.", "......."},
        {"..###..", ".##.##.", "#######", "###.###", "###.###", ".##.##.", "..###.."},
        {".......", "......#", ".....##", "#...##.", "##.##..", ".###...", "..#...."},
        {"#######", "#+++++#", "#+++++#", "#+++++#", ".#+++#.", "..#+#..", "...#..."},
        {".......", "...#...", "..###..", ".#####.", "..###..", "...#...", "......."},
        {"..#.#..", ".#####.", "###.###", ".#...#.", "###.###", ".#####.", "..#.#.."},
        {"####..", "#++##.", "#++###", "#++++#", "#++++#", "#++++#", "######"},
        {".......", "...#...", "...#...", ".#####.", "...#...", "...#...", "......."},
        {".......", ".......", ".......", ".#####.", ".......", ".......", "......."},
        {".......", ".......", ".......", "...##..", "...##..", ".......", "......."},
        {".......", ".......", ".##....", "#..#..#", "....##.", ".......", "......."},
        {".###...", "#+++#..", "#+++#..", "#+++#..", ".####..", "....##.", ".....##"},
        {"..###.#", ".#...##", "#...###", "#......", "......#", "###...#", "##...#.", "#.###.."},
        {"...#...", "..###..", "..#.#..", ".##.##.", ".#####.", "###.###", "#######"},
        {"....###", ".....##", "....#.#", ".......", "#.#....", "##.....", "###...."},
        {"...#...", "...#...", ".#.#.#.", "..###..", "...#...", "#.....#", "#######"},
        {".......", "###....", "#######", "#+++++#", "#+++++#", "#######", "......."}
    };

    private static final String[] TE_ICONS = {
        null, null, null, null, null, null, null, null, null, null,
        ":w", "x", "?", "✓", "#", "◆", "◇", null, "+", "-",
        "·", "~", "$", null, "!", null, null, null
    };

    private static final Identifier SMOG_ICONS = Identifier.fromNamespaceAndPath("hontun", "textures/gui/server/smog_icons.png");

    static Font font() { return Minecraft.getInstance().font; }

    static boolean term() { return HontunTheme.terminal(); }

    static boolean vanilla() { return !HontunTheme.restyleEnabled(); }

    static int text() {
        if (term()) return TerminalWidgets.fg();
        return vanilla() ? 0xFFFFFFFF : HontunTheme.argb(0xFF, HontunTheme.textLight());
    }

    static int sub() {
        if (term()) return TerminalWidgets.fg();
        return vanilla() ? 0xFFE0E0E0 : HontunTheme.argb(0xFF, HontunTheme.subtext1());
    }

    static int dim() {
        if (term()) return TerminalWidgets.dim();
        return vanilla() ? 0xFFA0A0A0 : HontunTheme.argb(0xFF, HontunTheme.textDim());
    }

    static int acc() {
        if (term()) return TerminalWidgets.acc();
        return vanilla() ? 0xFFFFFF55 : HontunTheme.argb(0xFF, HontunTheme.accentHi());
    }

    static int accLo() {
        if (term()) return TerminalWidgets.lineF();
        return vanilla() ? 0xFFAAAA00 : HontunTheme.argb(0xFF, HontunTheme.accentLo());
    }

    static int green() {
        if (term()) {
            TerminalWidgets.ensure();
            return TerminalWidgets.o(TerminalPalette.green);
        }
        return vanilla() ? 0xFF55FF55 : HontunTheme.argb(0xFF, HontunTheme.green());
    }

    static int yellow() {
        if (term()) {
            TerminalWidgets.ensure();
            return TerminalWidgets.o(TerminalPalette.yellow);
        }
        return vanilla() ? 0xFFFFFF55 : HontunTheme.argb(0xFF, HontunTheme.yellow());
    }

    static int red() {
        if (term()) return TerminalWidgets.red();
        return vanilla() ? 0xFFFF5555 : HontunTheme.argb(0xFF, HontunTheme.red());
    }

    static int forDim() {
        if (term()) return mix(green(), TerminalWidgets.empty(), 0.5f);
        return vanilla() ? 0xFF00AA00 : mix(green(), HontunTheme.argb(0xFF, HontunTheme.surface1()), 0.5f);
    }

    static int agDim() {
        if (term()) return mix(red(), TerminalWidgets.empty(), 0.5f);
        return vanilla() ? 0xFFAA0000 : mix(red(), HontunTheme.argb(0xFF, HontunTheme.surface1()), 0.5f);
    }

    static int pad() {
        if (term()) return 12;
        if (HontunTheme.smog()) return 0;
        return vanilla() ? 16 : 14;
    }

    static int iwRef() {
        if (term()) return 782;
        if (HontunTheme.smog()) return 570;
        if (vanilla()) return 680;
        if (HontunTheme.hvanilla()) return 712;
        return HontunTheme.modern2() ? 590 : 570;
    }

    static int group() { return HontunTheme.modern2() ? acc() : dim(); }

    static int nav(boolean active) {
        if (active) return HontunTheme.modern2() ? acc() : term() ? TerminalWidgets.acc() : text();
        return vanilla() ? text() : sub();
    }

    static Component c(String s) { return HontunFont.text(s == null ? "" : s); }

    static Component c(Component comp) { return HontunFont.apply(comp); }

    static int w(Component c) { return font().width(c); }

    static void text(GuiGraphicsExtractor g, Component c, int x, int y, int color) {
        g.text(font(), c, x, y, color, !term());
    }

    static void text(GuiGraphicsExtractor g, FormattedCharSequence c, int x, int y, int color) {
        g.text(font(), c, x, y, color, !term());
    }

    private static final FontDescription SMOG_BIG = new FontDescription.Resource(Identifier.fromNamespaceAndPath("hontun", "smog_big"));
    private static final FontDescription SMOG_SM = new FontDescription.Resource(Identifier.fromNamespaceAndPath("hontun", "smog_sm"));
    private static final FontDescription SMOG_LG = new FontDescription.Resource(Identifier.fromNamespaceAndPath("hontun", "smog_lg"));

    private static Component smogFont(Component comp, FontDescription f) {
        if (comp == null || !HontunTheme.smog()) return comp;
        return comp.copy().withStyle(st -> st.withFont(f));
    }

    static Component sm(String s) { return smogFont(c(s), SMOG_SM); }

    static Component sm(Component comp) { return smogFont(c(comp), SMOG_SM); }

    static Component lg(String s) { return smogFont(c(s), SMOG_LG); }

    static Component lg(Component comp) { return smogFont(c(comp), SMOG_LG); }

    static Component bigText(Component c) {
        return HontunTheme.smog() ? c.copy().withStyle(st -> st.withFont(SMOG_BIG)) : c;
    }

    static int bigWidth(Component c) {
        if (HontunTheme.smog()) return w(bigText(c));
        return term() ? w(c) : w(c) * 2;
    }

    static void big(GuiGraphicsExtractor g, Component c, int x, int y, int color) {
        if (HontunTheme.smog()) {
            Component b = bigText(c);
            g.text(font(), b, x + 2, y + 2, (color & 0xFF000000) | ((color >> 2) & 0x3F3F3F), false);
            g.text(font(), b, x, y, color, false);
            return;
        }
        if (term()) {
            text(g, c, x, y + 4, color);
            return;
        }
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(2f, 2f);
        g.text(font(), c, 0, 0, color, !term());
        g.pose().popMatrix();
    }

    static void icon(GuiGraphicsExtractor g, int id, int x, int y, int color) {
        icon(g, id, x, y, color, 8);
    }

    static void icon(GuiGraphicsExtractor g, int id, int x, int y, int color, int size) {
        if (id < 0 || id > FOLDER) return;
        if (term()) {
            String gl = TE_ICONS[id];
            if (gl != null) text(g, c(gl), x, y, color);
            return;
        }
        if (HontunTheme.smog()) {
            int u = (id % 8) * 40, v = (id / 8) * 40, d = size == 6 ? 8 : 10;
            g.blit(RenderPipelines.GUI_TEXTURED, SMOG_ICONS, x - 1, y - 1, u, v, d, d, 40, 40, 320, 160, color);
            return;
        }
        if (vanilla()) {
            if (id == SEARCH || id == EXPAND) return;
            shadow(g, ICONS[id], x, y, (color & 0xFF000000) | ((color >> 2) & 0x3F3F3F));
            pix(g, ICONS[id], x, y, color);
            return;
        }
        if (HontunTheme.hvanilla()) shadow(g, ICONS[id], x, y, 0x60000000);
        pix(g, ICONS[id], x, y, color);
    }

    static int iconW(int id) {
        if (id < 0 || id > FOLDER) return 0;
        if (term()) return TE_ICONS[id] != null ? w(c(TE_ICONS[id])) : 0;
        if (vanilla() && (id == SEARCH || id == EXPAND)) return 0;
        return 8;
    }

    private static void pix(GuiGraphicsExtractor g, String[] rows, int x, int y, int color) {
        int half = ((color >>> 25) << 24) | (color & 0xFFFFFF);
        for (int r = 0; r < rows.length; r++) {
            String row = rows[r];
            int n = row.length(), c = 0;
            while (c < n) {
                char ch = row.charAt(c);
                if (ch == '.') {
                    c++;
                    continue;
                }
                int e = c;
                while (e < n && row.charAt(e) == ch) e++;
                g.fill(x + c, y + r, x + e, y + r + 1, ch == '#' ? color : half);
                c = e;
            }
        }
    }

    private static void shadow(GuiGraphicsExtractor g, String[] rows, int x, int y, int color) {
        for (int r = 0; r < rows.length; r++) {
            String row = rows[r];
            for (int c = 0; c < row.length(); c++) {
                if (row.charAt(c) == '.' || lit(rows, r + 1, c + 1)) continue;
                g.fill(x + c + 1, y + r + 1, x + c + 2, y + r + 2, color);
            }
        }
    }

    private static boolean lit(String[] rows, int r, int c) {
        return r < rows.length && c < rows[r].length() && rows[r].charAt(c) != '.';
    }

    private static void rects(GuiGraphicsExtractor g, int[][] rs, int x, int y, int color) {
        for (int[] r : rs) g.fill(x + r[0], y + r[1], x + r[0] + r[2], y + r[1] + r[3], color);
    }

    static void meter(GuiGraphicsExtractor g, int x, int ty, int level) {
        int on = meterOn(level);
        if (term()) {
            Font f = font();
            int d = dim(), off = TerminalWidgets.empty();
            TerminalWidgets.textAt(g, f, "[", x, ty, d);
            for (int i = 0; i < 3; i++) {
                int bx = x + 6 + i * 5;
                g.fill(bx, ty + 2, bx + 4, ty + 7, i < level ? on : off);
            }
            TerminalWidgets.textAt(g, f, "]", x + 21.5f, ty, d);
            return;
        }
        bars(g, x, ty + 3, 6, 3, 7, HontunTheme.smog() ? 1 : 0, level, on, meterOff());
    }

    static void meterL(GuiGraphicsExtractor g, int x, int ty, int level) {
        if (term()) {
            meter(g, x, ty, level);
            return;
        }
        boolean m1 = HontunTheme.modern1();
        bars(g, x, ty + (m1 ? 3 : 2), 12, m1 ? 3 : 4, 14, HontunTheme.smog() ? 2 : 0, level, meterOn(level), meterOff());
    }

    static void pingBars(GuiGraphicsExtractor g, int x, int y, int lit) {
        int on, off;
        boolean hv = false;
        if (term()) {
            on = TerminalWidgets.fg();
            off = TerminalWidgets.empty();
        } else if (HontunTheme.smog()) {
            on = sub();
            off = HontunTheme.argb(0xFF, HontunTheme.overlay1());
        } else if (HontunTheme.hvanilla()) {
            on = 0xFFC2C5CC;
            off = 0xFF44464E;
            hv = true;
        } else {
            on = sub();
            off = vanilla() ? 0xFF555555 : HontunTheme.argb(0xFF, HontunTheme.overlay0());
        }
        if (hv) {
            for (int k = 0; k < 5; k++) {
                int bx = x + 2 * k, bh = k + 2;
                g.fill(bx + 1, y + 7 - bh, bx + 2, y + 7, 0x60000000);
            }
        }
        for (int k = 0; k < 5; k++) {
            int bx = x + 2 * k, bh = k + 2;
            g.fill(bx, y + 6 - bh, bx + 1, y + 6, k < lit ? on : off);
        }
    }

    static int pingLit(int latency) {
        if (latency < 0) return 0;
        return latency < 150 ? 5 : latency < 300 ? 4 : latency < 600 ? 3 : latency < 1000 ? 2 : 1;
    }

    static int meterW() { return term() ? 27 : 20; }

    static int meterLW() { return term() ? 27 : 40; }

    static int meterGap() {
        if (term()) return 6;
        return HontunTheme.modern2() || HontunTheme.modern1() ? 3 : 4;
    }

    static int meterLGap() { return 6; }

    private static int meterOn(int level) {
        return level >= 3 ? green() : level == 2 ? yellow() : red();
    }

    private static int meterOff() {
        if (HontunTheme.smog()) return HontunTheme.argb(0xFF, HontunTheme.overlay1());
        return vanilla() ? 0xFF555555 : HontunTheme.argb(0xFF, HontunTheme.overlay0());
    }

    private static void bars(GuiGraphicsExtractor g, int x, int y, int bw, int bh, int pitch, int r, int level, int on, int off) {
        for (int i = 0; i < 3; i++) {
            int bx = x + i * pitch, col = i < level ? on : off;
            if (r > 0) HontunRound.fill(g, bx, y, bw, bh, r, col);
            else g.fill(bx, y, bx + bw, y + bh, col);
        }
    }

    static Component clip(Component c, int maxW) {
        if (maxW <= 0 || w(c) <= maxW) return c;
        Style st = c.getStyle();
        String s = c.getString();
        int lo = 0, hi = Math.min(s.length(), 4 * maxW);
        while (lo < hi) {
            int mid = (lo + hi + 1) >>> 1;
            if (w(Component.literal(s.substring(0, mid) + "…").withStyle(st)) <= maxW) lo = mid;
            else hi = mid - 1;
        }
        if (lo > 0 && Character.isHighSurrogate(s.charAt(lo - 1))) lo--;
        return Component.literal(s.substring(0, lo).stripTrailing() + "…").withStyle(st);
    }

    static int ruleH() {
        return HontunTheme.hvanilla() || vanilla() ? 2 : 1;
    }

    static void sep(GuiGraphicsExtractor g, int x, int y, int w) {
        if (w <= 0) return;
        if (term()) {
            new TerminalWidgets.Px().hline(x, x + w, y, TerminalWidgets.line()).submit(g);
        } else if (HontunTheme.smog()) {
            g.fill(x, y, x + w, y + 1, 0x12FFFFFF);
        } else if (HontunTheme.hvanilla()) {
            g.fill(x, y, x + w, y + 1, 0xFF050506);
            g.fill(x, y + 1, x + w, y + 2, 0xFF44464E);
        } else if (vanilla()) {
            g.fill(x, y, x + w, y + 1, 0x33FFFFFF);
            g.fill(x, y + 1, x + w, y + 2, 0xBF000000);
        } else {
            g.fill(x, y, x + w, y + 1, HontunTheme.argb(0x60, HontunTheme.overlay0()));
        }
    }

    static void sepF(GuiGraphicsExtractor g, int x, int y, int w) {
        if (!vanilla()) {
            sep(g, x, y, w);
            return;
        }
        if (w <= 0) return;
        g.fill(x, y, x + w, y + 1, 0xBF000000);
        g.fill(x, y + 1, x + w, y + 2, 0x33FFFFFF);
    }

    static void fadeSep(GuiGraphicsExtractor g, int x, int y, int w) {
        if (!HontunTheme.smog()) {
            sep(g, x, y, w);
            return;
        }
        int x0 = x + 6, x1 = x + w - 6;
        if (x1 <= x0) return;
        int ramp = Math.min(12, (x1 - x0) / 2);
        for (int i = 0; i < ramp; i++) {
            int a = Math.round(0x12 * (i + 0.5f) / 12f);
            if (a <= 0) continue;
            int col = (a << 24) | 0xFFFFFF;
            g.fill(x0 + i, y, x0 + i + 1, y + 1, col);
            g.fill(x1 - i - 1, y, x1 - i, y + 1, col);
        }
        if (x1 - ramp > x0 + ramp) g.fill(x0 + ramp, y, x1 - ramp, y + 1, 0x12FFFFFF);
    }

    static void divider(GuiGraphicsExtractor g, int x0, int x1, int y) {
        if (!HontunTheme.modern2()) {
            sep(g, x0, y, x1 - x0);
            return;
        }
        int head = Math.min(x1, x0 + 4);
        g.fill(x0, y, head, y + 1, acc());
        int start = head + 1;
        int from = HontunTheme.argb(0xA0, HontunTheme.accentLo()), to = HontunTheme.argb(0x60, HontunTheme.overlay0());
        for (int x = start; x < x1; x += 3) {
            float t = (x - start) / (float) Math.max(1, x1 - start);
            g.fill(x, y, Math.min(x1, x + 3), y + 1, mix(from, to, t));
        }
    }

    static int mix(int a, int b, float t) {
        int r = 0;
        for (int sh = 0; sh <= 24; sh += 8) {
            int ca = (a >>> sh) & 0xFF, cb = (b >>> sh) & 0xFF;
            r |= Math.round(ca + (cb - ca) * t) << sh;
        }
        return r;
    }

    static void vsep(GuiGraphicsExtractor g, int x, int y, int h) {
        if (h <= 0) return;
        if (term()) {
            new TerminalWidgets.Px().vline(x, y, y + h, TerminalWidgets.line()).submit(g);
        } else if (HontunTheme.smog()) {
            g.fill(x, y, x + 1, y + h, 0x10FFFFFF);
        } else if (HontunTheme.hvanilla()) {
            for (int yy = y; yy < y + h; yy += 2) g.fill(x, yy, x + 1, yy + 1, 0xFF44464E);
        } else if (vanilla()) {
            g.fill(x, y, x + 1, y + h, 0x33FFFFFF);
            g.fill(x + 1, y, x + 2, y + h, 0xBF000000);
        } else {
            g.fill(x, y, x + 1, y + h, HontunTheme.argb(0x50, HontunTheme.overlay0()));
        }
    }

    static void well(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        if (term()) {
            TerminalWidgets.well(g, x, y, w, h);
        } else if (HontunTheme.smog()) {
            HontunRound.card(g, x, y, w, h, 5, 0xB00A0A0C, 0xDE555558);
        } else if (HontunTheme.modern2()) {
            HontunShapes.fillClipped(g, x, y, w, h, 5, 5, HontunTheme.argb(0xA0, HontunTheme.crust()));
            HontunShapes.outlineClipped(g, x, y, w, h, 5, 5, HontunTheme.argb(0x70, HontunTheme.overlay0()));
        } else if (HontunTheme.modern1()) {
            g.fill(x, y, x + w, y + h, HontunTheme.argb(0xC0, HontunTheme.mantle()));
            HontunCards.border(g, x, y, w, h, HontunTheme.argb(0x90, HontunTheme.overlay0()));
        } else {
            g.fill(x, y, x + w, y + h, 0x70000000);
            HontunCards.border(g, x, y, w, h, HontunTheme.argb(0x80, HontunTheme.overlay0()));
        }
    }

    static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) return;
        if (term()) {
            TerminalWidgets.frame(g, x, y, w, h, true);
        } else if (HontunTheme.smog()) {
            HontunRound.card(g, x, y, w, h, 5, 0x66000000, 0x4A555558);
        } else if (HontunTheme.modern2()) {
            m2Card(g, x, y, w, h);
        } else if (HontunTheme.modern1()) {
            g.fill(x, y, x + w, y + h, HontunTheme.argb(0xC0, HontunTheme.mantle()));
            HontunCards.border(g, x, y, w, h, HontunTheme.argb(0x90, HontunTheme.overlay0()));
        } else if (HontunTheme.hvanilla()) {
            g.fill(x, y, x + w, y + h, 0x99101114);
            HontunCards.bevel(g, x, y, w, h, 0xFF050506, 0xFF5A5C66);
        } else {
            sep(g, x, y, w);
            sepF(g, x, y + h - 2, w);
        }
    }

    static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean top) {
        if (!top && vanilla()) {
            if (w > 0 && h > 0) sepF(g, x, y + h - 2, w);
            return;
        }
        panel(g, x, y, w, h);
    }

    static void strip(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0 || term() || !HontunTheme.hvanilla()) return;
        g.fill(x, y, x + w, y + h, 0xFF26272D);
        HontunCards.bevel(g, x, y, w, h, 0xFF5A5C66, 0xFF050506);
    }

    static void band(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0 || term()) return;
        if (HontunTheme.smog()) {
            HontunRound.card(g, x, y, w, h, 5, 0xB00A0A0C, 0xDE555558);
        } else if (HontunTheme.modern2()) {
            m2Card(g, x, y, w, h);
        } else if (HontunTheme.modern1()) {
            g.fillGradient(x, y, x + w, y + h, 0xF41E2024, 0xF416181C);
            int e = HontunTheme.argb(0x80, HontunTheme.overlay0());
            g.fill(x + 1, y, x + w - 1, y + 1, e);
            g.fill(x, y + 1, x + 1, y + h - 1, e);
            g.fill(x + w - 1, y + 1, x + w, y + h - 1, e);
            if (h >= 2) {
                g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, HontunTheme.argb(0xFF, HontunTheme.accent()));
                g.fill(x + 1, y + h - 1, x + w - 1, y + h, HontunTheme.argb(0xFF, HontunTheme.accentHi()));
            }
        }
    }

    private static void m2Card(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        HontunShapes.fillClipped(g, x, y, w, h, 6, 6, HontunTheme.argb(0xC0, HontunTheme.surface0()));
        HontunShapes.outlineClipped(g, x, y, w, h, 6, 6, HontunTheme.argb(0x70, HontunTheme.overlay0()));
    }

    private static void notched(GuiGraphicsExtractor g, int x, int y, int w, int h, int fill, int border) {
        if (w <= 0 || h <= 0) return;
        if ((fill >>> 24) != 0) {
            g.fill(x + 1, y, x + w - 1, y + h, fill);
            g.fill(x, y + 1, x + 1, y + h - 1, fill);
            g.fill(x + w - 1, y + 1, x + w, y + h - 1, fill);
        }
        HontunCards.border(g, x, y, w, h, border);
    }

    private static void frame(GuiGraphicsExtractor g, int x, int y, int w, int h, int argb) {
        if (w <= 0 || h <= 0) return;
        g.fill(x, y, x + w, y + 1, argb);
        g.fill(x, y + h - 1, x + w, y + h, argb);
        g.fill(x, y + 1, x + 1, y + h - 1, argb);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, argb);
    }

    private static void ring(GuiGraphicsExtractor g, int x, int y, int w, int h, int r, int argb) {
        if (w <= 0 || h <= 0) return;
        if (RoundedGui.outline(g, (float) x, (float) y, (float) w, (float) h, (float) r, 1f, argb)) return;
        HontunCards.border(g, x, y, w, h, argb);
    }

    static void navRow(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean active, boolean hovered) {
        if (!active && !hovered) return;
        if (term()) {
            TerminalWidgets.row(g, x, y, w, h, active, hovered);
        } else if (HontunTheme.smog()) {
            if (active) {
                HontunRound.card(g, x, y, w, h, 4, HontunTheme.argb(0xFF, HontunTheme.surface1()), 0xDEE6E9EC);
                HontunRound.fill(g, x + 2, y + 4, 2, h - 8, 1, HontunTheme.argb(0xFF, HontunTheme.accent()));
            } else {
                HontunRound.fill(g, x, y, w, h, 4, HontunTheme.argb(0xC0, HontunTheme.surface0()));
            }
        } else if (HontunTheme.modern2()) {
            HontunShapes.row(g, x, y, w, h, active, hovered);
        } else if (HontunTheme.modern1()) {
            if (active) {
                notched(g, x, y, w, h, 0xFF23334B, HontunTheme.argb(0x40, HontunTheme.overlay0()));
                if (h > 4) g.fillGradient(x + 1, y + 2, x + 4, y + h - 2, HontunTheme.argb(0xFF, HontunTheme.accentHi()), HontunTheme.argb(0xFF, HontunTheme.accentLo()));
            } else {
                g.fill(x, y, x + w, y + h, HontunTheme.argb(0x20, HontunTheme.accent()));
            }
        } else if (vanilla()) {
            if (active) {
                g.fill(x, y, x + w, y + h, 0xFF000000);
                HontunCards.border(g, x, y, w, h, 0xFFFFFFFF);
            } else {
                g.fill(x, y, x + w, y + h, 0x1AFFFFFF);
            }
        } else {
            if (active) {
                g.fill(x, y, x + w, y + h, HontunTheme.argb(0xCC, HontunTheme.accentLo()));
                HontunCards.border(g, x, y, w, h, HontunTheme.argb(0xFF, HontunTheme.accentHi()));
            } else {
                g.fill(x, y, x + w, y + h, HontunTheme.argb(0x1A, HontunTheme.textLight()));
            }
        }
    }

    static void row(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean active, boolean hovered) {
        if (!active && !hovered) return;
        if (term()) {
            g.fill(x, y, x + w, y + h, active ? TerminalWidgets.sel() : TerminalWidgets.hl());
        } else if (HontunTheme.smog()) {
            if (active) HontunRound.fill(g, x, y, w, h, 3, HontunTheme.argb(0xFF, HontunTheme.surface1()));
            else HontunRound.card(g, x, y, w, h, 4, HontunTheme.argb(0xFF, HontunTheme.surface2()), 0xDEE6E9EC);
        } else if (HontunTheme.modern2()) {
            if (active) {
                HontunShapes.fillClipped(g, x, y, w, h, 3, 3, HontunTheme.argb(0xE0, HontunTheme.surface1()));
                HontunShapes.outlineClipped(g, x, y, w, h, 3, 3, HontunTheme.argb(0xC0, HontunTheme.accentLo()));
            } else {
                HontunShapes.row(g, x, y, w, h, false, true);
            }
        } else if (active) {
            g.fill(x, y, x + w, y + h, HontunTheme.argb(0x30, HontunTheme.textLight()));
        } else if (HontunTheme.modern1()) {
            notched(g, x, y, w, h, HontunTheme.argb(0xFF, HontunTheme.surface2()), HontunTheme.argb(0x40, HontunTheme.overlay0()));
        } else {
            g.fill(x, y, x + w, y + h, 0x1AE6E8EC);
        }
    }

    static int[] rowInset() {
        if (term()) return new int[]{1, 2};
        if (HontunTheme.smog() || HontunTheme.modern2()) return new int[]{3, 6};
        if (HontunTheme.modern1()) return new int[]{0, 0};
        return HontunTheme.hvanilla() ? new int[]{1, 2} : new int[]{2, 4};
    }

    static void listCard(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean hovered) {
        if (term() || !HontunTheme.modern1()) return;
        notched(g, x, y, w, h, HontunTheme.argb(0xFF, hovered ? HontunTheme.surface2() : HontunTheme.surface1()),
                HontunTheme.argb(0x40, HontunTheme.overlay0()));
    }

    static final int NORMAL = 0, ACC = 1, DIM = 2;

    static int pill(GuiGraphicsExtractor g, Component label, int x, int y, int color) {
        return pill(g, label, x, y, tone(color), -1);
    }

    static int pillWidth(Component label) {
        return pillWidth(label, NORMAL, -1);
    }

    private static int tone(int color) {
        if (color == acc()) return ACC;
        return color == dim() ? DIM : NORMAL;
    }

    private static int pillIconW(int icon) {
        if (icon < 0 || icon > FOLDER || term()) return 0;
        if (HontunTheme.smog()) return 9;
        int iw = iconW(icon);
        return iw > 0 ? iw + 3 : 0;
    }

    static int pillWidth(Component label, int tone, int icon) {
        if (term()) return w(c("[" + label.getString() + "]"));
        if (HontunTheme.smog()) return 8 + pillIconW(icon) + w(sm(label));
        return (HontunTheme.modern2() ? 8 : 7) + pillIconW(icon) + w(label);
    }

    static int pill(GuiGraphicsExtractor g, Component label, int x, int y, int tone, int icon) {
        int bw = pillWidth(label, tone, icon);
        if (term()) {
            int d = dim();
            Component open = c("[");
            text(g, open, x, y, d);
            text(g, label, x + w(open), y, tone == ACC ? acc() : tone == DIM ? d : TerminalWidgets.fg());
            text(g, c("]"), x + w(c("[" + label.getString())), y, d);
            return bw;
        }
        boolean hasIcon = pillIconW(icon) > 0;
        int lx = x + 4 + pillIconW(icon);
        if (HontunTheme.smog()) {
            int top = y - 1;
            if (tone == ACC) HontunRound.card(g, x, top, bw, 10, 5, 0x16FFFFFF, 0x14FFFFFF);
            else if (tone == DIM) ring(g, x, top, bw, 10, 5, 0x14FFFFFF);
            else HontunRound.fill(g, x, top, bw, 10, 5, 0x0EFFFFFF);
            int col = tone == DIM ? dim() : text();
            if (hasIcon) icon(g, icon, x + 4, top + 2, col, 6);
            text(g, sm(label), lx, y, col);
            return bw;
        }
        int top = y - 2, col;
        if (HontunTheme.modern2()) {
            int base = tone == ACC ? HontunTheme.accent() : HontunTheme.textDim();
            if (tone != DIM) HontunShapes.fillClipped(g, x, top, bw, 12, 2, 2, HontunTheme.argb(0x26, base));
            HontunShapes.outlineClipped(g, x, top, bw, 12, 2, 2, HontunTheme.argb(tone == ACC ? 0x90 : tone == DIM ? 0x70 : 0xC0, base));
            col = tone == ACC ? acc() : tone == DIM ? dim() : sub();
        } else if (HontunTheme.modern1()) {
            int fill = tone == ACC ? 0xFF23334B : tone == DIM ? 0 : HontunTheme.argb(0xFF, HontunTheme.surface2());
            notched(g, x, top, bw, 11, fill, HontunTheme.argb(tone == DIM ? 0x60 : 0x40, HontunTheme.overlay0()));
            col = tone == ACC ? acc() : tone == DIM ? dim() : sub();
        } else if (HontunTheme.hvanilla()) {
            if (tone != DIM) g.fill(x, top, x + bw, top + 12, tone == ACC ? 0x303E8CFF : 0xFF26272D);
            frame(g, x, top, bw, 12, tone == ACC ? 0xFF3E8CFF : 0xFF44464E);
            col = tone == ACC ? 0xFF5AC8FF : tone == DIM ? dim() : sub();
        } else {
            if (tone == ACC) {
                g.fill(x, top, x + bw, top + 12, 0x29FFFF55);
                frame(g, x, top, bw, 12, 0xFFAAAA00);
            } else if (tone == DIM) {
                frame(g, x, top, bw, 12, 0xFF555555);
            } else {
                g.fill(x, top, x + bw, top + 12, 0xFF555555);
            }
            col = tone == ACC ? 0xFFFFFF55 : tone == DIM ? 0xFFA0A0A0 : 0xFFE0E0E0;
        }
        if (hasIcon) icon(g, icon, x + 4, y, col);
        text(g, label, lx, y, col);
        return bw;
    }

    private static int[] chipPad() {
        if (term()) return new int[]{6, 6, 6};
        if (HontunTheme.smog()) return new int[]{6, 4, 6};
        if (HontunTheme.modern2()) return new int[]{4, 4, 4};
        return new int[]{4, 4, 3};
    }

    static int chipWire() { return term() ? 12 : 10; }

    static int chipW(Component step, Component range, boolean joined) {
        int[] p = chipPad();
        int sw = step == null ? 0 : w(step);
        return (joined ? chipWire() : 0) + p[0] + (sw > 0 ? sw + p[1] : 0) + (range == null ? 0 : w(range)) + p[2];
    }

    private static Component chipRange(Doc.Span s) {
        return s.count != null && s.maxW > 0 ? clip(s.count, s.maxW) : s.count;
    }

    static int chipW(Doc.Span s) { return chipW(s.text, chipRange(s), s.joined); }

    static void chip(GuiGraphicsExtractor g, Doc.Span s, int x, int y) {
        int wl = s.joined ? chipWire() : 0, cx = x + wl, bw = chipW(s) - wl, top = y - 2;
        boolean last = s.last;
        int stepCol = dim(), rangeCol = last ? acc() : text();
        if (term()) {
            TerminalWidgets.Px px = new TerminalWidgets.Px();
            if (wl > 0) px.hline(x, cx, top + 6, TerminalWidgets.line());
            if (last) px.rect(cx, top, bw, 13, TerminalWidgets.sel());
            px.frame(cx, top, bw, 13, last ? TerminalWidgets.lineF() : TerminalWidgets.line()).submit(g);
            rangeCol = last ? acc() : TerminalWidgets.fg();
        } else if (HontunTheme.smog()) {
            if (wl > 0) g.fill(x, top + 5, cx, top + 6, HontunTheme.argb(0xFF, HontunTheme.overlay1()));
            if (last) HontunRound.card(g, cx, top, bw, 12, 6, 0x16FFFFFF, 0x14FFFFFF);
            else HontunRound.fill(g, cx, top, bw, 12, 6, 0x0EFFFFFF);
        } else if (HontunTheme.modern2()) {
            if (wl > 0) g.fill(x, top + 5, cx, top + 6, HontunTheme.argb(0xFF, HontunTheme.overlay1()));
            int base = last ? HontunTheme.accent() : HontunTheme.textDim();
            HontunShapes.fillClipped(g, cx, top, bw, 12, 2, 2, HontunTheme.argb(0x26, base));
            HontunShapes.outlineClipped(g, cx, top, bw, 12, 2, 2, HontunTheme.argb(last ? 0x90 : 0xC0, base));
        } else if (HontunTheme.modern1()) {
            if (wl > 0) g.fill(x, top + 5, cx, top + 6, HontunTheme.argb(0xFF, HontunTheme.overlay1()));
            notched(g, cx, top, bw, 11, last ? 0xFF23334B : HontunTheme.argb(0xFF, HontunTheme.surface2()), HontunTheme.argb(0x40, HontunTheme.overlay0()));
            if (last) stepCol = acc();
        } else if (HontunTheme.hvanilla()) {
            if (wl > 0) g.fill(x, top + 5, cx, top + 6, 0xFF5A5C66);
            g.fill(cx, top, cx + bw, top + 12, last ? 0x303E8CFF : 0xFF26272D);
            frame(g, cx, top, bw, 12, last ? 0xFF3E8CFF : 0xFF44464E);
            rangeCol = last ? 0xFF5AC8FF : text();
        } else {
            if (wl > 0) g.fill(x, top + 6, cx, top + 7, 0xFFA0A0A0);
            if (last) {
                g.fill(cx, top, cx + bw, top + 12, 0x29FFFF55);
                frame(g, cx, top, bw, 12, 0xFFAAAA00);
            } else {
                g.fill(cx, top, cx + bw, top + 12, 0xFF555555);
            }
            rangeCol = last ? 0xFFFFFF55 : 0xFFFFFFFF;
        }
        int[] p = chipPad();
        int tx = cx + p[0];
        if (s.text != null && w(s.text) > 0) {
            text(g, s.text, tx, y, stepCol);
            tx += w(s.text) + p[1];
        }
        Component range = chipRange(s);
        if (range != null) text(g, range, tx, y, rangeCol);
    }

    private static final String[] DIAMOND = {"..##..", ".####.", "######", "######", ".####.", "..##.."};
    private static final int[][] VA_DOT = {{2, 0, 3, 1}, {1, 1, 5, 1}, {0, 2, 7, 3}, {1, 5, 5, 1}, {2, 6, 3, 1}};
    private static final int[] M1_OCT = {3, 5, 7, 7, 7, 5, 3};

    private static float hair() {
        double gs = Minecraft.getInstance().getWindow().getGuiScale();
        return gs > 0 ? 1f / (float) gs : 1f;
    }

    static int chainPad() { return ServerPages.t6(10, 10, 10, 12, 12, 12); }

    static int[] chainGeo() {
        return new int[]{ServerPages.t6(6, 6, 6, 6, 6, 4), ServerPages.t6(4, 3, 3, 3, 3, 3),
                ServerPages.t6(12, 12, 12, 13, 12, 13), ServerPages.t6(6, 5, 5, 6, 5, 5)};
    }

    static int chainStepDy() { return ServerPages.t6(6, 5, 6, 5, 6, 6); }

    static int[] chainInsets() {
        return new int[]{ServerPages.t6(0, 2, 0, 0, 0, 0), ServerPages.t6(3, 3, 3, 3, 4, 4),
                ServerPages.t6(3, 3, 3, 3, 3, 4), ServerPages.t6(0, 2, 0, 0, 0, 0)};
    }

    static void chainCell(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean on) {
        if (w <= 0 || h <= 0) return;
        if (term()) {
            new TerminalWidgets.Px().rect(x, y, w, h, on ? TerminalWidgets.sel() : TerminalWidgets.bg())
                    .frame(x, y, w, h, on ? TerminalWidgets.lineF() : TerminalWidgets.line()).submit(g);
        } else if (HontunTheme.smog()) {
            if (on) HontunRound.card(g, x, y, w, h, 4, HontunTheme.argb(0xFF, HontunTheme.surface1()), 0xDEE6E9EC);
            else HontunRound.card(g, x, y, w, h, 4, 0xF00A0A0C, 0x54555558);
        } else if (HontunTheme.modern2()) {
            if (on) {
                HontunShapes.fillClipped(g, x, y, w, h, 2, 2, HontunTheme.argb(0x26, HontunTheme.accent()));
                HontunShapes.outlineClipped(g, x, y, w, h, 2, 2, HontunTheme.argb(0x90, HontunTheme.accent()));
            } else {
                HontunShapes.row(g, x, y, w, h, false, false);
            }
        } else if (HontunTheme.modern1()) {
            notched(g, x, y, w, h, on ? 0xFF23334B : HontunTheme.argb(0xFF, HontunTheme.surface1()),
                    on ? 0xB03E8CFF : HontunTheme.argb(0x40, HontunTheme.overlay0()));
        } else if (HontunTheme.hvanilla()) {
            g.fill(x, y, x + w, y + h, on ? 0x303E8CFF : 0xFF26272D);
            frame(g, x, y, w, h, on ? 0xFF3E8CFF : 0xFF44464E);
        } else {
            g.fill(x, y, x + w, y + h, on ? 0x29FFFF55 : 0xFF555555);
            frame(g, x, y, w, h, on ? 0xFFAAAA00 : 0xFF6F6F6F);
        }
    }

    static int chainText(boolean on) {
        if (term()) return on ? acc() : TerminalWidgets.fg();
        if (HontunTheme.smog()) return on ? text() : sub();
        if (HontunTheme.modern2() || HontunTheme.modern1()) return on ? acc() : sub();
        if (HontunTheme.hvanilla()) return on ? 0xFF5AC8FF : sub();
        return on ? 0xFFFFFF55 : 0xFFE0E0E0;
    }

    static int chainStep(boolean on) {
        if (!on) return dim();
        return HontunTheme.smog() ? text() : acc();
    }

    static int chainIndex(boolean on) {
        return on && HontunTheme.hvanilla() ? acc() : dim();
    }

    private static int chainLine(boolean on) {
        if (HontunTheme.modern1()) return on ? 0xB03E8CFF : HontunTheme.argb(0xFF, HontunTheme.overlay1());
        if (vanilla()) return on ? 0xFFAAAA00 : 0xFFA0A0A0;
        return HontunTheme.argb(0xFF, on ? HontunTheme.accent() : HontunTheme.overlay1());
    }

    static void chainWire(GuiGraphicsExtractor g, int x0, int x1, int y, boolean on) {
        if (x1 <= x0) return;
        if (term()) {
            new TerminalWidgets.Px().hline(x0, x1, y, on ? TerminalWidgets.lineF() : TerminalWidgets.line()).submit(g);
            return;
        }
        g.fill(x0, y, x1, y + 1, chainLine(on));
    }

    static void chainBracket(GuiGraphicsExtractor g, int x, int y, int w, boolean on) {
        if (w <= 1) return;
        if (term()) {
            float u = hair();
            int c = on ? TerminalWidgets.lineF() : TerminalWidgets.line();
            new TerminalWidgets.Px().vline(x, y, y + 4, c).vline(x + w - u, y, y + 4, c).hline(x, x + w, y + 4 - u, c).submit(g);
            return;
        }
        int c = chainLine(on), in = HontunTheme.smog() ? 1 : 0;
        g.fill(x, y, x + 1, y + 3, c);
        g.fill(x + w - 1, y, x + w, y + 3, c);
        g.fill(x + in, y + 3, x + w - in, y + 4, c);
    }

    static int routeDot() { return HontunTheme.hvanilla() && !term() ? 8 : 7; }

    static void routeMark(GuiGraphicsExtractor g, int x, int y, boolean first) {
        if (term()) {
            HontunRound.fill(g, x, y, 7, 7, 4, first ? acc() : dim());
            return;
        }
        int col = first ? acc() : sub();
        if (HontunTheme.smog()) {
            HontunRound.fill(g, x, y, 7, 7, 4, col);
        } else if (HontunTheme.modern2()) {
            g.fill(x, y, x + 7, y + 7, col);
        } else if (HontunTheme.modern1()) {
            for (int r = 0; r < M1_OCT.length; r++) {
                int o = (7 - M1_OCT[r]) / 2;
                g.fill(x + o, y + r, x + o + M1_OCT[r], y + r + 1, col);
            }
        } else if (HontunTheme.hvanilla()) {
            pix(g, DIAMOND, x + 2, y + 2, 0x60000000);
            pix(g, DIAMOND, x + 1, y + 1, col);
        } else {
            rects(g, VA_DOT, x + 1, y + 1, first ? 0xFF3F3F15 : 0xFF383838);
            rects(g, VA_DOT, x, y, first ? 0xFFFFFF55 : 0xFFE0E0E0);
        }
    }

    static int routeGap() { return term() ? 6 : 5; }

    static void routeWire(GuiGraphicsExtractor g, int x0, int x1, int y) {
        if (x1 <= x0) return;
        if (term()) {
            new TerminalWidgets.Px().hline(x0, x1, y, TerminalWidgets.line()).submit(g);
        } else if (HontunTheme.hvanilla()) {
            g.fill(x0 + 1, y + 1, x1 + 1, y + 2, 0x60000000);
            g.fill(x0, y, x1, y + 1, 0xFF5A5C66);
        } else if (vanilla()) {
            g.fill(x0, y, x1, y + 1, 0xFFA0A0A0);
        } else {
            g.fill(x0, y, x1, y + 1, HontunTheme.argb(0xFF, HontunTheme.overlay1()));
        }
    }

    private static final Identifier TAB_SPR = Identifier.withDefaultNamespace("widget/tab");
    private static final Identifier TAB_SEL = Identifier.withDefaultNamespace("widget/tab_selected");
    private static final Identifier TAB_HI = Identifier.withDefaultNamespace("widget/tab_highlighted");
    private static final Identifier TAB_SEL_HI = Identifier.withDefaultNamespace("widget/tab_selected_highlighted");
    private static final Identifier BOX = Identifier.withDefaultNamespace("widget/checkbox");
    private static final Identifier BOX_SEL = Identifier.withDefaultNamespace("widget/checkbox_selected");
    private static final Identifier BOX_HI = Identifier.withDefaultNamespace("widget/checkbox_highlighted");
    private static final Identifier BOX_SEL_HI = Identifier.withDefaultNamespace("widget/checkbox_selected_highlighted");

    private static int tabGapIn() { return term() ? 6 : 4; }

    static int tabPad() { return ServerPages.t6(7, 5, 2, 5, 8, 6); }

    static int tabContentW(Component label, int icon, Component count) {
        int ig = tabGapIn(), iw = iconW(icon);
        return (iw > 0 ? iw + ig : 0) + w(label) + (count != null ? ig + w(count) : 0);
    }

    static int tabW(Doc.Span s) {
        if (s.sw > 0) return s.sw;
        return tabContentW(s.text, s.icon, s.count) + 2 * tabPad();
    }

    static int toggleW(Component label, int icon, Component count) {
        int c = tabContentW(label, icon, count);
        if (term()) return w(c("[x]")) + 6 + c;
        if (HontunTheme.smog()) return c + 16;
        if (HontunTheme.modern2()) return c + 13;
        if (HontunTheme.modern1()) return c + 16;
        if (HontunTheme.hvanilla()) return c + 10;
        return 24 + c;
    }

    static int toggleW(Doc.Span s) { return toggleW(s.text, s.icon, s.count); }

    private static void tabContent(GuiGraphicsExtractor g, Doc.Span s, int x, int y, int iconCol, int col, int countCol) {
        int ig = tabGapIn(), iw = iconW(s.icon);
        if (iw > 0) {
            icon(g, s.icon, x, y, iconCol);
            x += iw + ig;
        }
        text(g, s.text, x, y, col);
        if (s.count != null) text(g, s.count, x + w(s.text) + ig, y, countCol);
    }

    private static void hgrad(GuiGraphicsExtractor g, int x, int y, int w, int from, int to) {
        for (int i = 0; i < w; i++) g.fill(x + i, y, x + i + 1, y + 1, mix(from, to, w > 1 ? i / (float) (w - 1) : 0f));
    }

    private static void m2Button(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean sel) {
        int cut = Math.min(5, Math.min(w, h) / 3);
        if (sel) HontunShapes.glow(g, x, y, w, h, cut, HontunTheme.accent(), 2, 0x30);
        HontunShapes.fillClipped(g, x, y, w, h, cut, cut, HontunTheme.argb(sel ? 0xE8 : 0x60, sel ? HontunTheme.surface1() : HontunTheme.surface0()));
        HontunShapes.outlineClipped(g, x, y, w, h, cut, cut, HontunTheme.argb(sel ? 0xFF : 0x40, sel ? HontunTheme.accent() : HontunTheme.accentLo()));
        if (w > cut + 4) g.fill(x + 2, y + h - 2, x + w - cut - 1, y + h - 1, HontunTheme.argb(sel ? 0xFF : 0x30, HontunTheme.accent()));
    }

    private static void m1Button(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean sel) {
        if (w <= 2 || h <= 4) return;
        int side = sel ? 0x4A1B3B6E : 0x3016181C;
        g.fill(x, y + 1, x + 1, y + h - 1, side);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, side);
        if (sel) g.fillGradient(x + 1, y, x + w - 1, y + h, HontunTheme.argb(0x6A, HontunTheme.accentLo()), side);
        else g.fillGradient(x + 1, y, x + w - 1, y + h, 0x301F2126, side);
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, HontunTheme.argb(sel ? 0xC0 : 0x40, HontunTheme.accent()));
        g.fill(x + 1, y + h - 1, x + w - 1, y + h, HontunTheme.argb(sel ? 0xFF : 0x40, HontunTheme.accentHi()));
        if (!sel) {
            g.fill(x + 1, y, x + w - 1, y + 1, 0x2E646670);
            return;
        }
        g.fillGradient(x + 2, y + 3, x + 4, y + h - 4, HontunTheme.argb(0xFF, HontunTheme.accentHi()), HontunTheme.argb(0xFF, HontunTheme.accentLo()));
        int b = HontunTheme.argb(0xB0, HontunTheme.accent());
        g.fill(x + 1, y, x + w - 1, y + 1, b);
        g.fill(x, y + 1, x + 1, y + h - 1, b);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, b);
    }

    static void tabGroup(GuiGraphicsExtractor g, int x, int y, int w, int right) {
        if (w <= 0 || term()) return;
        if (HontunTheme.smog()) {
            HontunRound.fill(g, x, y, w, 16, 8, 0xE00A0A0C);
        } else if (HontunTheme.modern2()) {
            HontunShapes.fillClipped(g, x, y, w, 18, 4, 4, HontunTheme.argb(0xF0, HontunTheme.crust()));
            HontunShapes.outlineClipped(g, x, y, w, 18, 4, 4, HontunTheme.argb(0x70, HontunTheme.overlay0()));
        } else if (vanilla()) {
            sep(g, x + w, y + 22, right - x - w);
        }
    }

    static void tab(GuiGraphicsExtractor g, Doc.Span s, int x, int y, boolean hover) {
        int w = tabW(s), cx = x + tabPad(), ty, col, cc = dim();
        boolean on = s.sel;
        if (term()) {
            if (on) g.fill(x, y, x + w, y + 13, TerminalWidgets.fill());
            col = on ? TerminalWidgets.ink() : hover ? acc() : TerminalWidgets.fg();
            if (on) cc = TerminalWidgets.ink();
            ty = y + 2;
        } else if (HontunTheme.smog()) {
            if (on) HontunRound.card(g, x, y + 2, w, 12, 6, 0x16FFFFFF, 0x14FFFFFF);
            col = on ? text() : hover ? sub() : dim();
            ty = y + 4;
        } else if (HontunTheme.modern2()) {
            if (on) m2Button(g, x, y + 3, w, 12, true);
            col = on ? acc() : hover ? text() : sub();
            ty = y + 5;
        } else if (HontunTheme.modern1()) {
            if (on) hgrad(g, x + 1, y + 14, w - 2, accLo(), acc());
            col = on || hover ? text() : sub();
            ty = y + 4;
        } else if (HontunTheme.hvanilla()) {
            g.fill(x, y, x + w, y + 16, on ? 0xCC2E63B8 : 0x9926272D);
            frame(g, x, y, w, 16, on ? acc() : 0xAA3E8CFF);
            col = on || hover ? text() : sub();
            ty = y + 4;
        } else {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, on ? hover ? TAB_SEL_HI : TAB_SEL : hover ? TAB_HI : TAB_SPR, x, y, w, 24);
            int cw = tabContentW(s.text, s.icon, s.count);
            if (on) {
                g.fill(x + 2, y + 2, x + w - 2, y + 24, 0x40000000);
                int uw = Math.min(cw, w - 4), ux = x + (w - uw) / 2;
                g.fill(ux, y + 22, ux + uw, y + 23, 0xFFFFFFFF);
            }
            cx = x + (w - cw) / 2;
            col = text();
            ty = y + (on ? 8 : 10);
        }
        tabContent(g, s, cx, ty, s.color, col, cc);
    }

    static void toggle(GuiGraphicsExtractor g, Doc.Span s, int x, int y, boolean hover) {
        int w = toggleW(s), cx, ty, ic, col, cc = dim();
        boolean on = s.sel;
        if (term()) {
            int d = dim();
            Component open = c("[");
            text(g, open, x, y + 2, d);
            if (on) text(g, c("x"), x + w(open), y + 2, acc());
            text(g, c("]"), x + w(c("[x")), y + 2, d);
            cx = x + w(c("[x]")) + 6;
            ty = y + 2;
            ic = acc();
            col = hover ? acc() : TerminalWidgets.fg();
        } else if (HontunTheme.smog()) {
            if (on) HontunRound.card(g, x, y, w, 16, 8, 0x16FFFFFF, 0x14FFFFFF);
            else ring(g, x, y, w, 16, 8, 0x14FFFFFF);
            cx = x + 8;
            ty = y + 4;
            col = on ? text() : hover ? sub() : dim();
            ic = col;
        } else if (HontunTheme.modern2()) {
            m2Button(g, x, y + 1, w, 16, on);
            cx = x + 6;
            ty = y + 5;
            col = on ? acc() : hover ? text() : sub();
            ic = on ? acc() : dim();
        } else if (HontunTheme.modern1()) {
            m1Button(g, x, y, w, 16, on);
            cx = x + 8;
            ty = y + 4;
            col = on ? acc() : hover ? text() : 0xFFA0A0A0;
            ic = on ? acc() : dim();
            if (on) cc = acc();
        } else if (HontunTheme.hvanilla()) {
            g.fill(x, y, x + w, y + 16, on ? 0xA82E63B8 : 0x9926272D);
            if (on) frame(g, x + 1, y + 1, w - 2, 14, 0x903E8CFF);
            frame(g, x, y, w, 16, on ? acc() : 0xAA3E8CFF);
            cx = x + 5;
            ty = y + 4;
            col = on || hover ? text() : sub();
            ic = on ? acc() : dim();
            if (on) cc = sub();
        } else {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, on ? hover ? BOX_SEL_HI : BOX_SEL : hover ? BOX_HI : BOX, x, y + 1, 20, 20);
            cx = x + 24;
            ty = y + 7;
            col = sub();
            ic = sub();
        }
        tabContent(g, s, cx, ty, ic, col, cc);
    }

    static void swatch(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        if (w <= 0 || h <= 0) return;
        if (HontunTheme.smog()) {
            HontunRound.fill(g, x, y, w, h, Math.min(w, h) / 2, color);
            return;
        }
        if (HontunTheme.hvanilla()) g.fill(x + 1, y + 1, x + w + 1, y + h + 1, 0x60000000);
        g.fill(x, y, x + w, y + h, color);
    }

    static void weightSeg(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        if (w <= 0 || h <= 0) return;
        if (term()) {
            for (int cx = x; cx < x + w; cx += 6) g.fill(cx, y, Math.min(cx + 5, x + w), y + h, color);
        } else if (HontunTheme.smog()) {
            HontunRound.fill(g, x, y, w, h, 2, color);
        } else {
            g.fill(x, y, x + w, y + h, color);
        }
    }

    private static final Identifier SCROLLER_BG = Identifier.withDefaultNamespace("widget/scroller_background");
    private static final Identifier SCROLLER = Identifier.withDefaultNamespace("widget/scroller");

    static int scrollbarW() {
        if (term()) return 2;
        return HontunTheme.smog() ? 3 : 6;
    }

    static void scrollbar(GuiGraphicsExtractor g, int x, int top, int bot, int thumbTop, int thumbH) {
        if (bot <= top) return;
        if (term()) {
            TerminalWidgets.scrollbar(g, x, top, bot, 2, thumbTop, thumbH);
        } else if (vanilla()) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_BG, x, top, 6, bot - top);
            if (thumbH > 0) g.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER, x, thumbTop, 6, thumbH);
        } else if (HontunTheme.hvanilla()) {
            g.fill(x, top, x + 6, bot, 0xFF000000);
            if (thumbH > 0) {
                g.fill(x, thumbTop, x + 6, thumbTop + thumbH, 0xFF808080);
                g.fill(x, thumbTop, x + 5, thumbTop + thumbH - 1, 0xFFC0C0C0);
            }
        } else {
            HontunShapes.scrollbar(g, x, top, bot, scrollbarW(), thumbTop, thumbH);
        }
    }

    private static int[] tipSpec() {
        if (term()) return new int[]{6, 6, 6, 6, 2, 9, 11, 0, 330};
        if (HontunTheme.smog()) return new int[]{5, 7, 7, 6, 3, 8, 11, 0, 280};
        if (HontunTheme.modern2()) return new int[]{2, 4, 4, 2, 2, 10, 10, 0, 270};
        return new int[]{3, 4, 4, 3, 0, 10, 10, 2, 280};
    }

    static int tipRowH() { return tipSpec()[5]; }

    static int tipMax() {
        int[] s = tipSpec();
        return s[8] - s[1] - s[2];
    }

    static int fade(int color, float a) {
        int al = Math.round(((color >>> 24) & 0xFF) * Math.max(0f, Math.min(1f, a)));
        return (al << 24) | (color & 0xFFFFFF);
    }

    static void toast(GuiGraphicsExtractor g, Component label, int cx, int y, float a) {
        if (a <= 0.04f) return;
        int tw = w(label), w = 8 + 4 + tw + 12, h = 14, x = cx - w / 2;
        int ok = green();
        if (term()) {
            TerminalWidgets.ensure();
            g.fill(x, y, x + w, y + h, fade(TerminalWidgets.o(TerminalPalette.bar), a));
            g.fill(x, y, x + 1, y + h, fade(ok, a));
        } else if (HontunTheme.smog()) {
            HontunRound.fill(g, x, y, w, h, 4, fade(0xFA0A0A0C, a));
        } else if (HontunTheme.modern2()) {
            HontunShapes.fillClipped(g, x, y, w, h, 2, 2, fade(HontunTheme.argb(0xF4, HontunTheme.crust()), a));
            HontunShapes.outlineClipped(g, x, y, w, h, 2, 2, fade(ok, a * 0.7f));
        } else {
            int bg = fade(HontunTheme.argb(0xEC, HontunTheme.crust()), a), line = fade(ok, a * 0.6f);
            g.fill(x, y, x + w, y + h, bg);
            g.fill(x, y, x + w, y + 1, line);
            g.fill(x, y + h - 1, x + w, y + h, line);
            g.fill(x, y + 1, x + 1, y + h - 1, line);
            g.fill(x + w - 1, y + 1, x + w, y + h - 1, line);
        }
        int col = fade(ok, a);
        icon(g, CHECK, x + 6, y + 3, col);
        text(g, label, x + 18, y + 3, col);
    }

    static void tooltip(GuiGraphicsExtractor g, Doc.Tip tip, int mx, int my, int left, int top, int right, int bottom) {
        if (tip == null || tip.rows.isEmpty()) return;
        int[] s = tipSpec();
        int padT = s[0], padL = s[1], padR = s[2], padB = s[3], gap = s[4], paraH = s[6], extra = s[7];
        int pads = padL + padR;
        int natural = 0, lineMax = 0;
        for (Doc.Item it : tip.rows) {
            if (it instanceof Doc.Line l) {
                int e = l.extent();
                lineMax = Math.max(lineMax, e);
                natural = Math.max(natural, e);
            } else if (it instanceof Doc.Para p) {
                natural = Math.max(natural, p.natural());
            }
        }
        int cw = tip.width > 0 ? Math.max(tip.width - pads, lineMax) : Math.max(Math.min(natural, s[8] - pads), lineMax);
        int cy = 0;
        for (int i = 0; i < tip.rows.size(); i++) {
            Doc.Item it = tip.rows.get(i);
            if (i > 0) cy += gap + (i == 1 ? extra : 0);
            if (it instanceof Doc.Para p) {
                p.lh = paraH;
                p.pad = 0;
                p.top = (paraH - 8) / 2;
            }
            it.x = 0;
            it.w = cw;
            it.measure(cw);
            it.y = cy;
            cy += it.h;
        }
        int w = cw + pads, h = cy + padT + padB;
        int x = mx + (HontunTheme.smog() ? 6 : 12), y = my + 12;
        if (x + w > right) x = right - w;
        if (x < left) x = left;
        if (y + h > bottom) y = my - (HontunTheme.modern2() ? 16 : 4) - h;
        if (y < top) y = top;
        tipFrame(g, x, y, w, h);
        for (Doc.Item it : tip.rows) it.draw(g, x + padL, y + padT, -1, -1);
    }

    private static void tipFrame(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        if (term()) {
            TerminalWidgets.ensure();
            new TerminalWidgets.Px().rect(x, y, w, h, TerminalWidgets.o(TerminalPalette.bar)).frame(x, y, w, h, TerminalWidgets.line()).submit(g);
        } else if (HontunTheme.smog()) {
            HontunRound.fill(g, x, y, w, h, 5, 0xFA0A0A0C);
            int[] ys = {y, y + Math.round(h / 3f), y + Math.round(h * 2 / 3f), y + h};
            int[] cols = {0x20FFFFFF, 0x19FFFFFF, 0x12FFFFFF};
            for (int i = 0; i < 3; i++) {
                if (ys[i + 1] <= ys[i]) continue;
                g.enableScissor(x, ys[i], x + w, ys[i + 1]);
                ring(g, x, y, w, h, 5, cols[i]);
                g.disableScissor();
            }
        } else if (HontunTheme.modern2()) {
            HontunShapes.fillClipped(g, x, y, w, h, 2, 2, HontunTheme.argb(0xF4, HontunTheme.crust()));
            HontunShapes.outlineClipped(g, x, y, w, h, 2, 2, HontunTheme.argb(0xC0, HontunTheme.accentLo()));
            g.fill(x + 1, y + 2, x + 2, y + h - 2, HontunTheme.argb(0xFF, HontunTheme.accent()));
            HontunShapes.brackets(g, x, y, w, h, 2, HontunTheme.argb(0xFF, HontunTheme.accentHi()));
        } else if (HontunTheme.hvanilla()) {
            int bg = 0xF0100010, hi = 0x505000FF, lo = 0x5028007F;
            g.fill(x + 1, y, x + w - 1, y + h, bg);
            g.fill(x, y + 1, x + 1, y + h - 1, bg);
            g.fill(x + w - 1, y + 1, x + w, y + h - 1, bg);
            g.fill(x + 2, y + 1, x + w - 2, y + 2, hi);
            g.fill(x + 2, y + h - 2, x + w - 2, y + h - 1, lo);
            g.fillGradient(x + 1, y + 2, x + 2, y + h - 2, hi, lo);
            g.fillGradient(x + w - 2, y + 2, x + w - 1, y + h - 2, hi, lo);
        } else {
            TooltipRenderUtil.extractTooltipBackground(g, x + 4, y + 4, w - 8, h - 8, null);
        }
    }
}
