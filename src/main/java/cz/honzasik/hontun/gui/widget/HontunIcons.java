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
            default -> { }
        }
    }

    public static void pingBars(GuiGraphicsExtractor g, int x, int y, int bars, int onArgb, int offArgb) {
        for (int i = 0; i < 4; i++) {
            int h = 2 + i * 2;
            g.fill(x + i * 3, y + 8 - h, x + i * 3 + 2, y + 8, i < bars ? onArgb : offArgb);
        }
    }
}
