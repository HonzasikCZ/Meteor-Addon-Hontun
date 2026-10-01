package cz.honzasik.hontun.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class HontunIcons {
    public static final int PERSON = 0;
    public static final int SEARCH = 1;
    public static final int PLUS = 2;
    public static final int KEY = 3;
    public static final int ARROW = 4;
    public static final int LINK = 5;
    public static final int PENCIL = 6;
    public static final int TRASH = 7;
    public static final int REFRESH = 8;
    public static final int CHEVRON = 9;
    public static final int GLOBE = 10;
    public static final int WARNING = 11;

    public static final int SIZE = 8;

    private HontunIcons() {}

    public static void draw(GuiGraphicsExtractor g, int id, int x, int y, int argb) {
        switch (id) {
            case PERSON -> {
                g.fill(x + 2, y, x + 5, y + 3, argb);
                g.fill(x + 1, y + 4, x + 6, y + 7, argb);
            }
            case SEARCH -> {
                g.fill(x + 1, y, x + 5, y + 1, argb);
                g.fill(x + 1, y + 5, x + 5, y + 6, argb);
                g.fill(x, y + 1, x + 1, y + 5, argb);
                g.fill(x + 5, y + 1, x + 6, y + 5, argb);
                g.fill(x + 5, y + 5, x + 7, y + 7, argb);
            }
            case PLUS -> {
                g.fill(x + 3, y, x + 4, y + 7, argb);
                g.fill(x, y + 3, x + 7, y + 4, argb);
            }
            case KEY -> {
                g.fill(x, y + 1, x + 4, y + 2, argb);
                g.fill(x, y + 4, x + 4, y + 5, argb);
                g.fill(x, y + 1, x + 1, y + 5, argb);
                g.fill(x + 3, y + 1, x + 4, y + 5, argb);
                g.fill(x + 4, y + 2, x + 8, y + 4, argb);
            }
            case ARROW -> {
                g.fill(x, y + 3, x + 6, y + 4, argb);
                for (int i = 0; i < 3; i++) g.fill(x + 4 + i, y + 1 + i, x + 5 + i, y + 6 - i, argb);
            }
            case LINK -> {
                g.fill(x, y + 2, x + 3, y + 5, argb);
                g.fill(x + 4, y + 2, x + 7, y + 5, argb);
                g.fill(x + 2, y + 3, x + 5, y + 4, argb);
            }
            case PENCIL -> {
                for (int i = 0; i < 4; i++) g.fill(x + 2 + i, y + 4 - i, x + 4 + i, y + 6 - i, argb);
                g.fill(x, y + 6, x + 3, y + 8, argb);
                g.fill(x + 5, y, x + 8, y + 3, argb);
            }
            case TRASH -> {
                g.fill(x, y, x + 7, y + 1, argb);
                g.fill(x + 1, y + 2, x + 6, y + 7, argb);
            }
            case REFRESH -> {
                g.fill(x + 2, y, x + 7, y + 1, argb);
                g.fill(x + 1, y + 1, x + 3, y + 2, argb);
                g.fill(x + 6, y + 1, x + 7, y + 2, argb);
                g.fill(x, y + 2, x + 3, y + 3, argb);
                g.fill(x + 5, y + 5, x + 8, y + 6, argb);
                g.fill(x + 1, y + 6, x + 2, y + 7, argb);
                g.fill(x + 5, y + 6, x + 7, y + 7, argb);
                g.fill(x + 1, y + 7, x + 6, y + 8, argb);
            }
            case CHEVRON -> {
                for (int i = 0; i < 4; i++) {
                    g.fill(x + i, y + 3 - i, x + i + 1, y + 4 - i, argb);
                    g.fill(x + i, y + 3 + i, x + i + 1, y + 4 + i, argb);
                }
            }
            case GLOBE -> {
                g.fill(x + 2, y, x + 5, y + 1, argb);
                g.fill(x + 2, y + 6, x + 5, y + 7, argb);
                g.fill(x, y + 2, x + 7, y + 3, argb);
                g.fill(x, y + 4, x + 7, y + 5, argb);
                g.fill(x + 1, y + 1, x + 2, y + 6, argb);
                g.fill(x + 5, y + 1, x + 6, y + 6, argb);
            }
            case WARNING -> {
                g.fill(x + 3, y, x + 5, y + 5, argb);
                g.fill(x + 3, y + 6, x + 5, y + 8, argb);
            }
            default -> { }
        }
    }

    public static void pingBars(GuiGraphicsExtractor g, int x, int y, int bars, int onArgb, int offArgb) {
        for (int i = 0; i < 4; i++) {
            int h = 2 + i * 2;
            g.fill(x + i * 3, y + 8 - h, x + i * 3 + 2, y + 8, i < bars ? onArgb : offArgb);
        }
    }

    public static void draw(HontunShapes.Fill g, int id, int x, int y, int u, int argb) {
        u = Math.max(1, u);
        switch (id) {
            case PERSON -> {
                cell(g, x, y, u, 2, 0, 5, 3, argb);
                cell(g, x, y, u, 1, 4, 6, 7, argb);
            }
            case SEARCH -> {
                cell(g, x, y, u, 1, 0, 5, 1, argb);
                cell(g, x, y, u, 1, 5, 5, 6, argb);
                cell(g, x, y, u, 0, 1, 1, 5, argb);
                cell(g, x, y, u, 5, 1, 6, 5, argb);
                cell(g, x, y, u, 5, 5, 7, 7, argb);
            }
            case PLUS -> {
                cell(g, x, y, u, 3, 0, 4, 7, argb);
                cell(g, x, y, u, 0, 3, 7, 4, argb);
            }
            case KEY -> {
                cell(g, x, y, u, 0, 1, 4, 2, argb);
                cell(g, x, y, u, 0, 4, 4, 5, argb);
                cell(g, x, y, u, 0, 1, 1, 5, argb);
                cell(g, x, y, u, 3, 1, 4, 5, argb);
                cell(g, x, y, u, 4, 2, 8, 4, argb);
            }
            case ARROW -> {
                cell(g, x, y, u, 0, 3, 6, 4, argb);
                for (int i = 0; i < 3; i++) cell(g, x, y, u, 4 + i, 1 + i, 5 + i, 6 - i, argb);
            }
            case LINK -> {
                cell(g, x, y, u, 0, 2, 3, 5, argb);
                cell(g, x, y, u, 4, 2, 7, 5, argb);
                cell(g, x, y, u, 2, 3, 5, 4, argb);
            }
            case PENCIL -> {
                for (int i = 0; i < 4; i++) cell(g, x, y, u, 2 + i, 4 - i, 4 + i, 6 - i, argb);
                cell(g, x, y, u, 0, 6, 3, 8, argb);
                cell(g, x, y, u, 5, 0, 8, 3, argb);
            }
            case TRASH -> {
                cell(g, x, y, u, 0, 0, 7, 1, argb);
                cell(g, x, y, u, 1, 2, 6, 7, argb);
            }
            case REFRESH -> {
                cell(g, x, y, u, 2, 0, 7, 1, argb);
                cell(g, x, y, u, 1, 1, 3, 2, argb);
                cell(g, x, y, u, 6, 1, 7, 2, argb);
                cell(g, x, y, u, 0, 2, 3, 3, argb);
                cell(g, x, y, u, 5, 5, 8, 6, argb);
                cell(g, x, y, u, 1, 6, 2, 7, argb);
                cell(g, x, y, u, 5, 6, 7, 7, argb);
                cell(g, x, y, u, 1, 7, 6, 8, argb);
            }
            case CHEVRON -> {
                for (int i = 0; i < 4; i++) {
                    cell(g, x, y, u, i, 3 - i, i + 1, 4 - i, argb);
                    cell(g, x, y, u, i, 3 + i, i + 1, 4 + i, argb);
                }
            }
            case GLOBE -> {
                cell(g, x, y, u, 2, 0, 5, 1, argb);
                cell(g, x, y, u, 2, 6, 5, 7, argb);
                cell(g, x, y, u, 0, 2, 7, 3, argb);
                cell(g, x, y, u, 0, 4, 7, 5, argb);
                cell(g, x, y, u, 1, 1, 2, 6, argb);
                cell(g, x, y, u, 5, 1, 6, 6, argb);
            }
            case WARNING -> {
                cell(g, x, y, u, 3, 0, 5, 5, argb);
                cell(g, x, y, u, 3, 6, 5, 8, argb);
            }
            default -> { }
        }
    }

    public static void pixelCheck(HontunShapes.Fill g, int x, int y, int cell, int strokes, int argb) {
        int u = Math.max(1, cell);
        int n = 7 * u;
        int bx = x + (n - 5 * u) / 2, by = y + (n - 4 * u) / 2;
        if (strokes > 0) g.fill(bx, by + 2 * u, bx + u, by + 3 * u, argb);
        if (strokes > 1) g.fill(bx + u, by + 3 * u, bx + 2 * u, by + 4 * u, argb);
        if (strokes > 2) g.fill(bx + 2 * u, by + 2 * u, bx + 3 * u, by + 3 * u, argb);
        if (strokes > 3) g.fill(bx + 3 * u, by + u, bx + 4 * u, by + 2 * u, argb);
        if (strokes > 4) g.fill(bx + 4 * u, by, bx + 5 * u, by + u, argb);
    }

    private static void cell(HontunShapes.Fill g, int x, int y, int u, int cx0, int cy0, int cx1, int cy1, int argb) {
        g.fill(x + cx0 * u, y + cy0 * u, x + cx1 * u, y + cy1 * u, argb);
    }
}
