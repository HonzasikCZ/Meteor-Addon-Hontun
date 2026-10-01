package cz.honzasik.hontun.gui.render.route;

import cz.honzasik.hontun.gui.api.render.RoundedRect;
import cz.honzasik.hontun.gui.render.HontunRenderer;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class QuadRouter implements PrimitiveRouter {
    private final Color topLeft = new Color();
    private final Color topRight = new Color();
    private final Color bottomRight = new Color();
    private final Color bottomLeft = new Color();

    @Override
    public boolean roundedRect(RoundedRect rect) {
        GuiRenderer r = HontunRenderer.guiRenderer;
        if (r == null) return false;

        double x0 = Math.round(rect.getX());
        double y0 = Math.round(rect.getY());
        double x1 = Math.round(rect.getX() + rect.getWidth());
        double y1 = Math.round(rect.getY() + rect.getHeight());
        if (x1 <= x0 || y1 <= y0) return true;

        double cx0 = x0, cy0 = y0, cx1 = x1, cy1 = y1;
        if (rect.hasClip()) {
            cx0 = Math.max(cx0, Math.round(rect.getClipX()));
            cy0 = Math.max(cy0, Math.round(rect.getClipY()));
            cx1 = Math.min(cx1, Math.round(rect.getClipX() + rect.getClipWidth()));
            cy1 = Math.min(cy1, Math.round(rect.getClipY() + rect.getClipHeight()));
            if (cx1 <= cx0 || cy1 <= cy0) return true;
        }

        if (rect.hasGradient()) {
            if (rect.getFillColor().a > 0) {
                sample(rect, x0, y0, x1, y1, cx0, cy0, topLeft);
                sample(rect, x0, y0, x1, y1, cx1, cy0, topRight);
                sample(rect, x0, y0, x1, y1, cx1, cy1, bottomRight);
                sample(rect, x0, y0, x1, y1, cx0, cy1, bottomLeft);
                r.quad(cx0, cy0, cx1 - cx0, cy1 - cy0, topLeft, topRight, bottomRight, bottomLeft);
            }
        } else if (rect.getFillColor().a > 0) {
            Color c = rect.getFillColor();
            r.quad(cx0, cy0, cx1 - cx0, cy1 - cy0, c, c, c, c);
        }

        Color outline = rect.getOutlineColor();
        if (rect.getOutlineWidth() > 0 && outline.a > 0) {
            double s = Math.max(1, Math.round(rect.getOutlineWidth()));
            s = Math.min(s, Math.floor(Math.min(x1 - x0, y1 - y0) / 2));
            if (s < 1) s = 1;

            edge(r, x0, y0, x1, y0 + s, cx0, cy0, cx1, cy1, outline);
            edge(r, x0, y1 - s, x1, y1, cx0, cy0, cx1, cy1, outline);
            edge(r, x0, y0 + s, x0 + s, y1 - s, cx0, cy0, cx1, cy1, outline);
            edge(r, x1 - s, y0 + s, x1, y1 - s, cx0, cy0, cx1, cy1, outline);
        }

        return true;
    }

    private static void edge(GuiRenderer r, double x0, double y0, double x1, double y1,
                             double cx0, double cy0, double cx1, double cy1, Color c) {
        double ax = Math.max(x0, cx0), ay = Math.max(y0, cy0);
        double bx = Math.min(x1, cx1), by = Math.min(y1, cy1);
        if (bx <= ax || by <= ay) return;
        r.quad(ax, ay, bx - ax, by - ay, c, c, c, c);
    }

    private static void sample(RoundedRect rect, double x0, double y0, double x1, double y1,
                               double px, double py, Color out) {
        double fx = x1 > x0 ? (px - x0) / (x1 - x0) : 0;
        double fy = y1 > y0 ? (py - y0) / (y1 - y0) : 0;
        Color tl = rect.getGradientTopLeft();
        Color tr = rect.getGradientTopRight();
        Color br = rect.getGradientBottomRight();
        Color bl = rect.getGradientBottomLeft();
        out.set(
                mix(tl.r, tr.r, br.r, bl.r, fx, fy),
                mix(tl.g, tr.g, br.g, bl.g, fx, fy),
                mix(tl.b, tr.b, br.b, bl.b, fx, fy),
                mix(tl.a, tr.a, br.a, bl.a, fx, fy)
        );
    }

    private static int mix(int tl, int tr, int br, int bl, double fx, double fy) {
        double top = tl + (tr - tl) * fx;
        double bottom = bl + (br - bl) * fx;
        return (int) Math.round(Math.clamp(top + (bottom - top) * fy, 0, 255));
    }
}
