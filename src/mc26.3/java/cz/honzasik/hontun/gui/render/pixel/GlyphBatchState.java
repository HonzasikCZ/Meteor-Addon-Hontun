package cz.honzasik.hontun.gui.render.pixel;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.joml.Matrix3x2f;

import java.util.Arrays;

public final class GlyphBatchState implements GuiElementRenderState {
    private static final int STRIDE = 8;

    private final Matrix3x2f pose;
    private final float[] quads;
    private final int[] colors;
    private final int count;
    private final TextureSetup textureSetup;
    private final ScreenRectangle scissor;
    private final ScreenRectangle bounds;

    private GlyphBatchState(Matrix3x2f pose, float[] quads, int[] colors, int count, TextureSetup textureSetup,
                            ScreenRectangle scissor, ScreenRectangle bounds) {
        this.pose = pose;
        this.quads = quads;
        this.colors = colors;
        this.count = count;
        this.textureSetup = textureSetup;
        this.scissor = scissor;
        this.bounds = bounds;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private float[] quads = new float[STRIDE * 64];
        private int[] colors = new int[64];
        private int count;
        private float minX, minY, maxX, maxY;
        private AbstractTexture texture;
        private TextureSetup setup;

        private Builder() {
            reset();
        }

        public Builder reset() {
            count = 0;
            minX = Float.MAX_VALUE;
            minY = Float.MAX_VALUE;
            maxX = -Float.MAX_VALUE;
            maxY = -Float.MAX_VALUE;
            return this;
        }

        public Builder reserve(int quadCount) {
            if (quadCount > colors.length) {
                colors = Arrays.copyOf(colors, quadCount);
                quads = Arrays.copyOf(quads, quadCount * STRIDE);
            }
            return this;
        }

        public int size() {
            return count;
        }

        public Builder add(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, int argb) {
            if (x1 <= x0 || y1 <= y0 || (argb >>> 24) == 0) return this;
            if (count == colors.length) reserve(count * 2);
            int i = count * STRIDE;
            float[] q = quads;
            q[i] = x0;
            q[i + 1] = y0;
            q[i + 2] = x1;
            q[i + 3] = y1;
            q[i + 4] = u0;
            q[i + 5] = v0;
            q[i + 6] = u1;
            q[i + 7] = v1;
            colors[count++] = argb;
            if (x0 < minX) minX = x0;
            if (y0 < minY) minY = y0;
            if (x1 > maxX) maxX = x1;
            if (y1 > maxY) maxY = y1;
            return this;
        }

        public boolean submit(GuiGraphicsExtractor g, AbstractTexture texture) {
            if (count == 0 || g == null || texture == null) return false;
            if (texture != this.texture || setup == null) {
                if (texture.getTextureView() == null || texture.getSampler() == null) return false;
                this.texture = texture;
                this.setup = TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler());
            }
            Matrix3x2f pose = new Matrix3x2f(g.pose());
            ScreenRectangle scissor = g.scissorStack.peek();
            int x = (int) Math.floor(minX), y = (int) Math.floor(minY);
            ScreenRectangle area = new ScreenRectangle(x, y,
                    Math.max(1, (int) Math.ceil(maxX) - x), Math.max(1, (int) Math.ceil(maxY) - y)).transformMaxBounds(pose);
            ScreenRectangle bounds = scissor != null ? scissor.intersection(area) : area;
            if (bounds == null) return false;
            g.guiRenderState.addGuiElement(new GlyphBatchState(pose, quads, colors, count, setup, scissor, bounds));
            return true;
        }
    }

    @Override
    public void buildVertices(VertexConsumer vc) {
        float[] q = quads;
        for (int n = 0; n < count; n++) {
            int i = n * STRIDE;
            float x0 = q[i], y0 = q[i + 1], x1 = q[i + 2], y1 = q[i + 3];
            float u0 = q[i + 4], v0 = q[i + 5], u1 = q[i + 6], v1 = q[i + 7];
            int c = colors[n];
            vc.addVertexWith2DPose(pose, x0, y0).setUv(u0, v0).setColor(c);
            vc.addVertexWith2DPose(pose, x0, y1).setUv(u0, v1).setColor(c);
            vc.addVertexWith2DPose(pose, x1, y1).setUv(u1, v1).setColor(c);
            vc.addVertexWith2DPose(pose, x1, y0).setUv(u1, v0).setColor(c);
        }
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
