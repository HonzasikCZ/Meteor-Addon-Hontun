package com.mrmelon54.DraggableLists.render;

import com.mrmelon54.DraggableLists.theme.CornerStyle;
import com.mrmelon54.DraggableLists.theme.HontunBridge;
import cz.honzasik.hontun.gui.render.RoundedGui;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class Shapes {
    private Shapes() {
    }

    public static final class Profile {
        final int width;
        final int height;
        final int[] left;
        final int[] right;
        final int straightFrom;
        final int straightTo;

        private Profile(int width, int height, int[] left, int[] right) {
            this.width = width;
            this.height = height;
            this.left = left;
            this.right = right;

            int from = -1;
            int to = -1;
            for (int i = 0; i < height; i++) {
                if (left[i] == 0 && right[i] == 0) {
                    if (from < 0) from = i;
                    to = i;
                } else if (from >= 0) {
                    break;
                }
            }
            this.straightFrom = from;
            this.straightTo = to;
        }
    }

    public static Profile profile(int width, int height, CornerStyle style, int cornerSize) {
        int size = Math.max(0, Math.min(cornerSize, Math.min(width, height) / 2));
        int[] left = new int[height];
        int[] right = new int[height];

        switch (style) {
            case CHAMFER -> {
                for (int i = 0; i < size; i++) {
                    left[i] = size - i;
                    right[height - 1 - i] = size - i;
                }
            }
            case SQUARE -> {
            }
            case ROUND -> {
                for (int i = 0; i < size; i++) {
                    double dy = size - i - 0.5;
                    int inset = (int) Math.round(size - Math.sqrt((double) size * size - dy * dy));
                    left[i] = inset;
                    right[i] = inset;
                    left[height - 1 - i] = inset;
                    right[height - 1 - i] = inset;
                }
            }
        }
        return new Profile(width, height, left, right);
    }

    public static void fill(GuiGraphicsExtractor g, int x, int y, int width, int height,
                            CornerStyle style, int cornerSize, int argb) {
        if (width <= 0 || height <= 0) return;
        if (smooth() && RoundedGui.fill(g, (float) x, (float) y, (float) width, (float) height,
                (float) smoothRadius(style, cornerSize), 0f, argb)) return;
        fill(g, x, y, profile(width, height, style, cornerSize), argb);
    }

    private static boolean smooth() {
        return HontunBridge.smog() && RoundedGui.available();
    }

    private static int smoothRadius(CornerStyle style, int cornerSize) {
        return style == CornerStyle.SQUARE ? 0 : cornerSize;
    }

    public static void fill(GuiGraphicsExtractor g, int x, int y, Profile p, int argb) {
        if (p.width <= 0 || p.height <= 0) return;

        if (p.straightFrom >= 0) {
            g.fill(x, y + p.straightFrom, x + p.width, y + p.straightTo + 1, argb);
        }
        for (int i = 0; i < p.height; i++) {
            if (i >= p.straightFrom && i <= p.straightTo && p.straightFrom >= 0) continue;
            int x0 = x + p.left[i];
            int x1 = x + p.width - p.right[i];
            if (x1 > x0) g.fill(x0, y + i, x1, y + i + 1, argb);
        }
    }

    public static void outline(GuiGraphicsExtractor g, int x, int y, int width, int height,
                               CornerStyle style, int cornerSize, int argb) {
        if (width <= 0 || height <= 0) return;
        if (smooth() && RoundedGui.outline(g, (float) x, (float) y, (float) width, (float) height,
                (float) smoothRadius(style, cornerSize), 1f, argb)) return;
        outline(g, x, y, profile(width, height, style, cornerSize), argb);
    }

    public static void outline(GuiGraphicsExtractor g, int x, int y, Profile p, int argb) {
        if (p.width <= 0 || p.height <= 0) return;

        int half = p.width / 2;
        for (int i = 0; i < p.height; i++) {
            int l = p.left[i];
            int r = p.right[i];

            int lPrev = i == 0 ? half : p.left[i - 1];
            int lNext = i == p.height - 1 ? half : p.left[i + 1];
            int rPrev = i == 0 ? half : p.right[i - 1];
            int rNext = i == p.height - 1 ? half : p.right[i + 1];

            int leftRun = Math.max(1, Math.max(lPrev - l, lNext - l));
            int rightRun = Math.max(1, Math.max(rPrev - r, rNext - r));

            int lx0 = x + l;
            int lx1 = Math.min(x + p.width - r, lx0 + leftRun);
            if (lx1 > lx0) g.fill(lx0, y + i, lx1, y + i + 1, argb);

            int rx1 = x + p.width - r;
            int rx0 = Math.max(x + l, rx1 - rightRun);
            if (rx1 > rx0) g.fill(rx0, y + i, rx1, y + i + 1, argb);
        }
    }

    public static void shadow(GuiGraphicsExtractor g, int x, int y, int width, int height,
                              CornerStyle style, int cornerSize, int layers, int baseAlpha, int dropY) {
        if (smooth()) {
            float spread = layers * 2f;
            if (RoundedGui.fill(g, x - spread, y - spread + dropY, width + spread * 2f, height + spread * 2f,
                    smoothRadius(style, cornerSize) + spread, spread * 1.6f, (Math.min(255, baseAlpha) << 24))) return;
        }
        for (int i = layers; i >= 1; i--) {
            int alpha = Math.max(1, baseAlpha / (i * i));
            outline(g, x - i, y - i + dropY, width + i * 2, height + i * 2, style, cornerSize + i,
                alpha << 24);
        }
    }

    public static void glow(GuiGraphicsExtractor g, int x, int y, int width, int height,
                            CornerStyle style, int cornerSize, int layers, int baseAlpha, int rgb) {
        if (smooth()) return;
        for (int i = layers; i >= 1; i--) {
            int alpha = Math.max(1, baseAlpha / (i + 1));
            outline(g, x - i, y - i, width + i * 2, height + i * 2, style, cornerSize + i,
                (alpha << 24) | (rgb & 0xFFFFFF));
        }
    }
}
