package cz.honzasik.hontun.gui.render.pixel;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;

public final class TexBlitState implements GuiElementRenderState {
    private final Matrix3x2f pose;
    private final TextureSetup textureSetup;
    private final float[] xs;
    private final float[] ys;
    private final float u0, v0, u1, v1;
    private final int color;
    private final ScreenRectangle scissor;
    private final ScreenRectangle bounds;

    private TexBlitState(Matrix3x2f pose, TextureSetup textureSetup, float[] xs, float[] ys,
                         float u0, float v0, float u1, float v1, int color,
                         ScreenRectangle scissor, ScreenRectangle bounds) {
        this.pose = pose;
        this.textureSetup = textureSetup;
        this.xs = xs;
        this.ys = ys;
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
        this.color = color;
        this.scissor = scissor;
        this.bounds = bounds;
    }

    public static TexBlitState of(GuiGraphicsExtractor g, GpuTextureView view, GpuSampler sampler,
                                  float x, float y, float w, float h,
                                  float u0, float v0, float u1, float v1,
                                  double rotation, int color) {
        float[] xs = {x, x, x + w, x + w};
        float[] ys = {y, y + h, y + h, y};

        if (rotation != 0) {
            double rad = Math.toRadians(rotation);
            double cos = Math.cos(rad);
            double sin = Math.sin(rad);
            double ox = x + w / 2.0;
            double oy = y + h / 2.0;

            for (int i = 0; i < 4; i++) {
                double dx = xs[i] - ox;
                double dy = ys[i] - oy;
                xs[i] = (float) (dx * cos - dy * sin + ox);
                ys[i] = (float) (dy * cos + dx * sin + oy);
            }
        }

        float minX = Math.min(Math.min(xs[0], xs[1]), Math.min(xs[2], xs[3]));
        float minY = Math.min(Math.min(ys[0], ys[1]), Math.min(ys[2], ys[3]));
        float maxX = Math.max(Math.max(xs[0], xs[1]), Math.max(xs[2], xs[3]));
        float maxY = Math.max(Math.max(ys[0], ys[1]), Math.max(ys[2], ys[3]));

        int left = (int) Math.floor(minX);
        int top = (int) Math.floor(minY);
        int right = (int) Math.ceil(maxX);
        int bottom = (int) Math.ceil(maxY);

        Matrix3x2f pose = new Matrix3x2f(g.pose());
        ScreenRectangle scissor = g.scissorStack.peek();
        ScreenRectangle area = new ScreenRectangle(left, top, Math.max(1, right - left), Math.max(1, bottom - top))
                .transformMaxBounds(pose);
        ScreenRectangle bounds = scissor != null ? scissor.intersection(area) : area;

        return new TexBlitState(pose, TextureSetup.singleTexture(view, sampler), xs, ys,
                u0, v0, u1, v1, color, scissor, bounds);
    }

    public boolean visible() {
        return bounds != null;
    }

    @Override
    public void buildVertices(VertexConsumer vc) {
        vc.addVertexWith2DPose(pose, xs[0], ys[0]).setUv(u0, v0).setColor(color);
        vc.addVertexWith2DPose(pose, xs[1], ys[1]).setUv(u0, v1).setColor(color);
        vc.addVertexWith2DPose(pose, xs[2], ys[2]).setUv(u1, v1).setColor(color);
        vc.addVertexWith2DPose(pose, xs[3], ys[3]).setUv(u1, v0).setColor(color);
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.GUI_TEXTURED;
    }

    @Override
    public TextureSetup textureSetup() {
        return textureSetup;
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
