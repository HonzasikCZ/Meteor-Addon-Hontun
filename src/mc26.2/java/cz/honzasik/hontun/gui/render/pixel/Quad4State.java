package cz.honzasik.hontun.gui.render.pixel;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;

public final class Quad4State implements GuiElementRenderState {
    private final Matrix3x2f pose;
    private final float x0, y0, x1, y1, x2, y2, x3, y3;
    private final int c0, c1, c2, c3;
    private final ScreenRectangle scissor;
    private final ScreenRectangle bounds;

    private Quad4State(Matrix3x2f pose,
                       float x0, float y0, int c0,
                       float x1, float y1, int c1,
                       float x2, float y2, int c2,
                       float x3, float y3, int c3,
                       ScreenRectangle scissor, ScreenRectangle bounds) {
        this.pose = pose;
        this.x0 = x0; this.y0 = y0; this.c0 = c0;
        this.x1 = x1; this.y1 = y1; this.c1 = c1;
        this.x2 = x2; this.y2 = y2; this.c2 = c2;
        this.x3 = x3; this.y3 = y3; this.c3 = c3;
        this.scissor = scissor;
        this.bounds = bounds;
    }

    public static Quad4State rect(GuiGraphicsExtractor g, float left, float top, float right, float bottom,
                                  int tl, int tr, int br, int bl) {
        return create(g,
                left, top, tl,
                left, bottom, bl,
                right, bottom, br,
                right, top, tr);
    }

    public static Quad4State triangle(GuiGraphicsExtractor g, float ax, float ay, float bx, float by,
                                      float cx, float cy, int argb) {
        float cross = (bx - ax) * (cy - ay) - (by - ay) * (cx - ax);
        if (cross > 0) {
            float tx = bx, ty = by;
            bx = cx; by = cy;
            cx = tx; cy = ty;
        }
        return create(g,
                ax, ay, argb,
                bx, by, argb,
                cx, cy, argb,
                cx, cy, argb);
    }

    private static Quad4State create(GuiGraphicsExtractor g,
                                     float x0, float y0, int c0,
                                     float x1, float y1, int c1,
                                     float x2, float y2, int c2,
                                     float x3, float y3, int c3) {
        Matrix3x2f pose = new Matrix3x2f(g.pose());
        ScreenRectangle scissor = g.scissorStack.peek();

        int minX = (int) Math.floor(Math.min(Math.min(x0, x1), Math.min(x2, x3)));
        int minY = (int) Math.floor(Math.min(Math.min(y0, y1), Math.min(y2, y3)));
        int maxX = (int) Math.ceil(Math.max(Math.max(x0, x1), Math.max(x2, x3)));
        int maxY = (int) Math.ceil(Math.max(Math.max(y0, y1), Math.max(y2, y3)));

        ScreenRectangle area = new ScreenRectangle(minX, minY, Math.max(1, maxX - minX), Math.max(1, maxY - minY))
                .transformMaxBounds(pose);
        ScreenRectangle bounds = scissor != null ? scissor.intersection(area) : area;

        return new Quad4State(pose, x0, y0, c0, x1, y1, c1, x2, y2, c2, x3, y3, c3, scissor, bounds);
    }

    public boolean visible() {
        return bounds != null;
    }

    @Override
    public void buildVertices(VertexConsumer vc) {
        vc.addVertexWith2DPose(pose, x0, y0).setColor(c0);
        vc.addVertexWith2DPose(pose, x1, y1).setColor(c1);
        vc.addVertexWith2DPose(pose, x2, y2).setColor(c2);
        vc.addVertexWith2DPose(pose, x3, y3).setColor(c3);
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.GUI;
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    @Override
    public ScreenRectangle scissorArea() {
        return scissor;
    }

    @Override
    public ScreenRectangle bounds() {
        return bounds;
    }
}
