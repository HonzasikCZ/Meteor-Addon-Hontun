package cz.honzasik.hontun.gui.theme.style.terminal;

import static cz.honzasik.hontun.gui.theme.style.terminal.TerminalPalette.opaque;

public final class TerminalBar {
    public static final int SEGMENTS = 12;

    private TerminalBar() {}

    public static double width(TerminalGrid g, int n) {
        return (n + 2) * g.cw;
    }

    public static void paint(TerminalGrid g, double x, double y, double rowH, int n, double fr) {
        double ty = g.ty(y, rowH);
        TerminalPaint.text("[", x, ty, opaque(TerminalPalette.dim));
        TerminalPaint.text("]", x + (n + 1) * g.cw, ty, opaque(TerminalPalette.dim));

        double sh = Math.max(2, Math.round(g.px(8)));
        double sy = Math.round(y + (rowH - sh) / 2);
        double inset = Math.max(1, g.px(1));
        double sw = Math.max(1, Math.round(g.cw - 2 * inset));
        double r = Math.max(0, g.px(1));
        double filled = Math.max(0, Math.min(1, fr)) * n;

        for (int i = 0; i < n; i++) {
            double sx = Math.round(x + (i + 1) * g.cw + inset);
            TerminalPaint.round(sx, sy, sw, sh, r, opaque(TerminalPalette.empty));

            double f = Math.max(0, Math.min(1, filled - i));
            if (f <= 0) continue;

            double fw = Math.max(1, Math.round(sw * f));
            TerminalPaint.round(sx, sy, fw, sh, f < 1 ? 0 : r, opaque(TerminalPalette.fill));
        }
    }

    public static double fraction(TerminalGrid g, double x, int n, double mouseX) {
        double span = n * g.cw;
        if (span <= 0) return 0;
        return Math.max(0, Math.min(1, (mouseX - x - g.cw) / span));
    }
}
