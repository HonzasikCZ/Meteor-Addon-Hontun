package cz.honzasik.hontun.gui.api.render;

import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.utils.render.color.Color;
import org.jetbrains.annotations.ApiStatus;

public class RoundedRect {
    private static final RoundedRect INSTANCE = new RoundedRect();

    private RoundedRectRenderer renderer;

    private double x, y, width, height;
    private float rTopLeft, rTopRight, rBottomLeft, rBottomRight;
    private Color fillColor;
    private Color outlineColor;
    private float outlineWidth;

    private boolean gradient;
    private final Color gradientTopLeft = new Color();
    private final Color gradientTopRight = new Color();
    private final Color gradientBottomRight = new Color();
    private final Color gradientBottomLeft = new Color();

    private float feather;
    private boolean dither;

    private boolean clip;
    private double clipX, clipY, clipWidth, clipHeight;

    private RoundedRect() {}

    public static RoundedRect get() {
        INSTANCE.reset();
        return INSTANCE;
    }

    @ApiStatus.Internal
    public void registerRenderer(RoundedRectRenderer renderer) {
        this.renderer = renderer;
    }

    public void reset() {
        this.fillColor = Color.BLACK;
        this.outlineColor = Color.BLACK;
        this.outlineWidth = 0;
        this.rTopLeft = this.rTopRight = this.rBottomLeft = this.rBottomRight = 0;
        this.gradient = false;
        this.feather = 0;
        this.dither = false;
        this.clip = false;
        this.clipX = this.clipY = this.clipWidth = this.clipHeight = 0;
    }

    public RoundedRect pos(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public RoundedRect size(double width, double height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public RoundedRect bounds(WWidget widget) {
        return pos(widget.x, widget.y).size(widget.width, widget.height);
    }

    public RoundedRect radius(double radius) {
        this.rTopLeft = this.rTopRight = this.rBottomLeft = this.rBottomRight = (float) radius;
        return this;
    }

    public RoundedRect radius(float radius, Corners corners) {
        return this.radii(
                corners.topLeft ? radius : 0,
                corners.topRight ? radius : 0,
                corners.bottomLeft ? radius : 0,
                corners.bottomRight ? radius : 0
        );
    }

    public RoundedRect radii(float topLeft, float topRight, float bottomLeft, float bottomRight) {
        this.rTopLeft = topLeft;
        this.rTopRight = topRight;
        this.rBottomLeft = bottomLeft;
        this.rBottomRight = bottomRight;
        return this;
    }

    public RoundedRect color(Color fill) {
        this.fillColor = fill.copy();
        return this;
    }

    public RoundedRect outline(Color color, float width) {
        this.outlineColor = color.copy();
        this.outlineWidth = width;
        return this;
    }

    public RoundedRect gradient(Color topLeft, Color topRight, Color bottomRight, Color bottomLeft) {
        this.fillColor = Color.WHITE;
        this.gradient = true;
        this.gradientTopLeft.set(topLeft);
        this.gradientTopRight.set(topRight);
        this.gradientBottomRight.set(bottomRight);
        this.gradientBottomLeft.set(bottomLeft);
        return this;
    }

    public RoundedRect vgradient(Color top, Color bottom) {
        return gradient(top, top, bottom, bottom);
    }

    public RoundedRect hgradient(Color left, Color right) {
        return gradient(left, right, right, left);
    }

    public RoundedRect feather(float px) {
        this.feather = px;
        return this;
    }

    public RoundedRect dither(boolean dither) {
        this.dither = dither;
        return this;
    }

    public RoundedRect clip(double x, double y, double width, double height) {
        this.clip = true;
        this.clipX = x;
        this.clipY = y;
        this.clipWidth = width;
        this.clipHeight = height;
        return this;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }

    public float getTopLeft() { return rTopLeft; }
    public float getTopRight() { return rTopRight; }
    public float getBottomLeft() { return rBottomLeft; }
    public float getBottomRight() { return rBottomRight; }

    public Color getFillColor() { return fillColor; }
    public Color getOutlineColor() { return outlineColor; }
    public float getOutlineWidth() { return outlineWidth; }

    public boolean hasGradient() { return gradient; }
    public Color getGradientTopLeft() { return gradientTopLeft; }
    public Color getGradientTopRight() { return gradientTopRight; }
    public Color getGradientBottomRight() { return gradientBottomRight; }
    public Color getGradientBottomLeft() { return gradientBottomLeft; }

    public float getFeather() { return feather; }
    public boolean getDither() { return dither; }

    public boolean hasClip() { return clip; }
    public double getClipX() { return clipX; }
    public double getClipY() { return clipY; }
    public double getClipWidth() { return clipWidth; }
    public double getClipHeight() { return clipHeight; }

    private int fillAlpha() {
        if (!gradient) return fillColor.a;
        int g = Math.max(Math.max(gradientTopLeft.a, gradientTopRight.a), Math.max(gradientBottomRight.a, gradientBottomLeft.a));
        return fillColor.a == 0 ? 0 : g;
    }

    public void render() {
        if (width <= 0 || height <= 0) return;
        if (fillAlpha() == 0 && (outlineColor.a == 0 || outlineWidth <= 0)) return;

        if (renderer != null) renderer.renderRoundedRect(this);
    }
}
