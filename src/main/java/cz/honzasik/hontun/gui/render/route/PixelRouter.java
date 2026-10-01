package cz.honzasik.hontun.gui.render.route;

import cz.honzasik.hontun.gui.api.render.RoundedRect;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.api.text.RichTextSegment;
import cz.honzasik.hontun.gui.mixin.meteorclient.WViewScrollAccessor;
import cz.honzasik.hontun.gui.render.HontunRenderer;
import cz.honzasik.hontun.gui.render.pixel.PixelCanvas;
import cz.honzasik.hontun.gui.render.pixel.PixelGlyph;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.renderer.packer.TextureRegion;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.renderer.Texture;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class PixelRouter implements PrimitiveRouter {
    @Override
    public boolean quad4(GuiRenderer r, double x, double y, double w, double h, Color tl, Color tr, Color br, Color bl) {
        if (tl.a == 0 && tr.a == 0 && br.a == 0 && bl.a == 0) return true;
        if (w == 0 || h == 0) return true;

        PixelCanvas.quad4(x, y, w, h, tl.getPacked(), tr.getPacked(), br.getPacked(), bl.getPacked());
        return true;
    }

    @Override
    public boolean texQuad(GuiRenderer r, double x, double y, double w, double h, GuiTexture t, Color c) {
        return icon(r, x, y, w, h, 0, t, c);
    }

    @Override
    public boolean rotatedTexQuad(GuiRenderer r, double x, double y, double w, double h, double rot, GuiTexture t, Color c) {
        return icon(r, x, y, w, h, rot, t, c);
    }

    private boolean icon(GuiRenderer r, double x, double y, double w, double h, double rot, GuiTexture t, Color c) {
        if (c.a == 0 || w == 0 || h == 0 || t == null) return true;

        HontunGuiTheme theme = theme(r);
        PixelGlyph glyph = theme != null ? theme.style().glyphFor(t) : null;

        if (glyph != null) {
            PixelCanvas.glyphFit(glyph, x, y, w, h, c.getPacked(), (int) Math.round(rot / 90.0));
            return true;
        }

        Texture atlas = HontunRenderer.get().atlas();
        if (atlas == null) return false;

        TextureRegion region = t.get(w, h);
        PixelCanvas.texBlit(atlas.getTextureView(), atlas.getSampler(), x, y, w, h,
                (float) region.x1, (float) region.y1, (float) region.x2, (float) region.y2,
                rot, c.getPacked());
        return true;
    }

    @Override
    public boolean triangle(GuiRenderer r, double x1, double y1, double x2, double y2, double x3, double y3, Color c) {
        if (c.a == 0) return true;

        PixelCanvas.triangle(x1, y1, x2, y2, x3, y3, c.getPacked());
        return true;
    }

    @Override
    public boolean texture(GuiRenderer r, double x, double y, double w, double h, double rot, Texture t) {
        if (t == null || w == 0 || h == 0) return true;

        PixelCanvas.texBlit(t.getTextureView(), PixelCanvas.nearestSampler(), x, y, w, h,
                0f, 0f, 1f, 1f, rot, 0xFFFFFFFF);
        return true;
    }

    @Override
    public boolean meteorText(GuiRenderer r, String s, double x, double y, Color c, boolean title) {
        if (s == null || s.isEmpty() || c.a == 0) return true;

        HontunGuiTheme theme = theme(r);
        PixelCanvas.text(s, x, y, c.getPacked(), theme == null || !theme.light());
        return true;
    }

    @Override
    public boolean richText(RichText t, double x, double y, Color c) {
        if (t == null || c.a == 0) return true;

        HontunGuiTheme theme = PixelCanvas.theme();
        boolean shadow = theme == null || !theme.light();
        if (!shadow) {
            for (RichTextSegment segment : t.getSegments()) {
                if (segment.hasShadow()) {
                    shadow = true;
                    break;
                }
            }
        }

        PixelCanvas.text(PixelCanvas.component(t), x, y, c.getPacked(), shadow);
        return true;
    }

    @Override
    public boolean roundedRect(RoundedRect rect) {
        double x = rect.getX();
        double y = rect.getY();
        double w = rect.getWidth();
        double h = rect.getHeight();
        if (w <= 0 || h <= 0) return true;

        boolean clip = rect.hasClip();
        if (clip) PixelCanvas.pushClip(rect.getClipX(), rect.getClipY(), rect.getClipWidth(), rect.getClipHeight());

        try {
            int p = PixelCanvas.unit();
            Color outline = rect.getOutlineColor();
            boolean framed = rect.getOutlineWidth() > 0 && outline != null && outline.a > 0;
            int units = framed ? Math.max(1, Math.round(rect.getOutlineWidth() / p)) : 0;
            double inset = (double) units * p;

            double fx = x + inset;
            double fy = y + inset;
            double fw = w - 2 * inset;
            double fh = h - 2 * inset;
            Color fill = rect.getFillColor();

            if (fw > 0 && fh > 0 && fill != null && fill.a > 0) {
                if (rect.hasGradient()) {
                    PixelCanvas.quad4(fx, fy, fw, fh,
                            tint(rect.getGradientTopLeft(), fill),
                            tint(rect.getGradientTopRight(), fill),
                            tint(rect.getGradientBottomRight(), fill),
                            tint(rect.getGradientBottomLeft(), fill));
                } else {
                    PixelCanvas.fill(fx, fy, fw, fh, fill.getPacked());
                }
            }

            if (framed) PixelCanvas.frame(x, y, w, h, outline.getPacked(), PixelCanvas.ALL, units);
        } finally {
            if (clip) PixelCanvas.popClip();
        }

        return true;
    }

    private static int tint(Color gradient, Color fill) {
        int packed = gradient.getPacked();
        if (fill.a >= 255) return packed;
        int a = Math.round(gradient.a * fill.a / 255f);
        return (a << 24) | (packed & 0xFFFFFF);
    }

    @Override
    public double alpha(double a) {
        HontunGuiTheme theme = PixelCanvas.theme();
        double v = theme != null ? theme.style().pixelAlpha(a) : a;
        PixelCanvas.setAlpha(v);
        return v;
    }

    @Override
    public void windowLayer(GuiRenderer r) {
        PixelCanvas.nextLayer();
    }

    @Override
    public boolean skipTextPass() {
        return true;
    }

    @Override
    public double measure(HontunGuiTheme t, RichText text) {
        if (text == null) return 0;
        return PixelCanvas.width(PixelCanvas.component(text), PixelCanvas.unit(t));
    }

    @Override
    public void afterViewUpdate(WView v) {
        if (!(v.theme instanceof HontunGuiTheme theme)) return;

        int p = PixelCanvas.unit(theme);
        if (p <= 1) return;

        WViewScrollAccessor view = (WViewScrollAccessor) v;
        double max = Math.max(0, view.hontun$getActualHeight() - v.height);
        double limit = Math.floor(max / p) * p;

        double target = view.hontun$getTargetScroll();
        double snappedTarget = Math.max(0, Math.min(limit, PixelCanvas.snap(target, p)));
        if (snappedTarget != target) view.hontun$setTargetScroll(snappedTarget);

        double scroll = view.hontun$getScroll();
        double snapped = Math.max(0, Math.min(limit, PixelCanvas.snap(scroll, p)));
        if (snapped != scroll) {
            view.hontun$setScroll(snapped);
            v.moveCells(0, scroll - snapped);
        }
    }

    private static HontunGuiTheme theme(GuiRenderer r) {
        if (r != null && r.theme instanceof HontunGuiTheme hontun) return hontun;
        return PixelCanvas.theme();
    }
}
