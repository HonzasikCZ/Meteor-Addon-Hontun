package cz.honzasik.hontun.gui.theme.style.terminal;

import cz.honzasik.hontun.gui.theme.HontunGuiTheme;

public final class TerminalGrid {
    private static TerminalGrid cached;
    private static double cachedScale = -1;
    private static Object cachedFont;

    public final double scale;
    public final double cw;
    public final double u;
    public final double lh;
    public final double tb;
    public final double st;
    public final double ml;
    public final double qh;
    public final double th;

    private TerminalGrid(HontunGuiTheme t, double scale) {
        this.scale = scale;

        double w = t.textWidth("0");
        if (!(w > 0)) w = 7.8 * scale;

        cw = w;
        u = cw / 7.8;
        lh = Math.round(18 * u);
        tb = Math.round(24 * u);
        st = Math.round(22 * u);
        ml = Math.round(20 * u);
        qh = Math.round(28 * u);

        double h = t.textHeight();
        th = h > 0 ? h : 13 * u;
    }

    public static TerminalGrid of(HontunGuiTheme t) {
        double s = t.scale(1);
        Object font = t.textRenderer();

        TerminalGrid g = cached;
        if (g != null && s == cachedScale && font == cachedFont) return g;

        g = new TerminalGrid(t, s);
        cached = g;
        cachedScale = s;
        cachedFont = font;
        return g;
    }

    public double px(double units) {
        return Math.round(units * u);
    }

    public double cols(int n) {
        return n * cw;
    }

    public double unscaled(double px) {
        return scale > 0 ? px / scale : px;
    }

    public double ty(double rowY, double rowH) {
        return Math.round(rowY + (rowH - th) / 2);
    }

    public double windowWidth(int columns) {
        return Math.round(columns * cw) + 2;
    }
}
