package cz.honzasik.hontun.gui.theme.style.chamfer;

import cz.honzasik.hontun.gui.render.HontunRenderer;
import cz.honzasik.hontun.gui.widget.HontunShapes;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

public final class GuiFill implements HontunShapes.Fill {
    private static final GuiFill INSTANCE = new GuiFill();

    private static long quads;

    private final Color first = new Color();
    private final Color second = new Color();
    private final Color third = new Color();

    private GuiRenderer renderer;

    private GuiFill() {}

    public static GuiFill of(GuiRenderer renderer) {
        INSTANCE.renderer = renderer != null ? renderer : HontunRenderer.guiRenderer;
        return INSTANCE;
    }

    public static GuiFill get() {
        return of(null);
    }

    public static long quadCount() {
        return quads;
    }

    @Override
    public void fill(int x1, int y1, int x2, int y2, int argb) {
        if (renderer == null || x2 <= x1 || y2 <= y1 || (argb >>> 24) == 0) return;
        Color c = set(first, argb);
        renderer.quad(x1, y1, x2 - x1, y2 - y1, c, c, c, c);
        quads++;
    }

    @Override
    public void gradient(int x1, int y1, int x2, int y2, int topArgb, int bottomArgb) {
        if (renderer == null || x2 <= x1 || y2 <= y1) return;
        if ((topArgb >>> 24) == 0 && (bottomArgb >>> 24) == 0) return;
        Color top = set(second, topArgb);
        Color bottom = set(third, bottomArgb);
        renderer.quad(x1, y1, x2 - x1, y2 - y1, top, top, bottom, bottom);
        quads++;
    }

    @Override
    public void hgradient(int x1, int y1, int x2, int y2, int leftArgb, int rightArgb) {
        if (renderer == null || x2 <= x1 || y2 <= y1) return;
        if ((leftArgb >>> 24) == 0 && (rightArgb >>> 24) == 0) return;
        renderer.quad(x1, y1, x2 - x1, y2 - y1, set(second, leftArgb), set(third, rightArgb));
        quads++;
    }

    private static Color set(Color c, int argb) {
        return c.set((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >>> 24) & 0xFF);
    }
}
