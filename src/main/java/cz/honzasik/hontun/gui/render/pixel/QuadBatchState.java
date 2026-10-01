package cz.honzasik.hontun.gui.render.pixel;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;

import java.util.Arrays;

public final class QuadBatchState implements GuiElementRenderState {
    private final Matrix3x2f pose;
    private final float[] rects;
    private final int[] colors;
    private final int count;
    private final ScreenRectangle scissor;
    private final ScreenRectangle bounds;

    private QuadBatchState(Matrix3x2f pose, float[] rects, int[] colors, int count, ScreenRectangle scissor, ScreenRectangle bounds) {
        this.pose = pose;
        this.rects = rects;
        this.colors = colors;
        this.count = count;
        this.scissor = scissor;
        this.bounds = bounds;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private float[] rects = new float[256];
        private int[] colors = new int[64];
        private int count;
        private float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;

        public Builder add(float x0, float y0, float x1, float y1, int argb) {
            if (x1 <= x0 || y1 <= y0 || (argb >>> 24) == 0) return this;
            if (count == colors.length) {
                colors = Arrays.copyOf(colors, count * 2);
                rects = Arrays.copyOf(rects, count * 8);
            }
            int i = count * 4;
            rects[i] = x0;
            rects[i + 1] = y0;
            rects[i + 2] = x1;
            rects[i + 3] = y1;
            colors[count++] = argb;
            minX = Math.min(minX, x0);
            minY = Math.min(minY, y0);
            maxX = Math.max(maxX, x1);
            maxY = Math.max(maxY, y1);
            return this;
        }

        public void submit(GuiGraphicsExtractor g) {
            if (count == 0 || g == null) return;
            Matrix3x2f pose = new Matrix3x2f(g.pose());
            ScreenRectangle scissor = g.scissorStack.peek();
            int x = (int) Math.floor(minX), y = (int) Math.floor(minY);
            ScreenRectangle area = new ScreenRectangle(x, y,
                    Math.max(1, (int) Math.ceil(maxX) - x), Math.max(1, (int) Math.ceil(maxY) - y)).transformMaxBounds(pose);
            ScreenRectangle bounds = scissor != null ? scissor.intersection(area) : area;
            if (bounds == null) return;
            g.guiRenderState.addGuiElement(new QuadBatchState(pose, rects, colors, count, scissor, bounds));
        }
    }

    @Override
    public void buildVertices(VertexConsumer vc) {
        for (int q = 0; q < count; q++) {
            int i = q * 4;
            float x0 = rects[i], y0 = rects[i + 1], x1 = rects[i + 2], y1 = rects[i + 3];
            int c = colors[q];
            vc.addVertexWith2DPose(pose, x0, y0).setColor(c);
            vc.addVertexWith2DPose(pose, x0, y1).setColor(c);
            vc.addVertexWith2DPose(pose, x1, y1).setColor(c);
            vc.addVertexWith2DPose(pose, x1, y0).setColor(c);
        }
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
