package cz.honzasik.hontun.gui.render;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public final class RoundedGui {
    private static final Logger LOG = LoggerFactory.getLogger("Hontun/RoundedGui");

    public static final VertexFormat FORMAT;
    public static final RenderPipeline PIPELINE;

    private static int state = 0;

    static {
        VertexFormat fmt = null;
        RenderPipeline pipe = null;
        try {
            fmt = VertexFormat.builder(0)
                    .addAttribute("Position", GpuFormat.RGB32_FLOAT)
                    .addAttribute("Color", GpuFormat.RGBA8_UNORM)
                    .addAttribute("UV0", GpuFormat.RG32_FLOAT)
                    .addAttribute("UV1", GpuFormat.RG16_SINT)
                    .addAttribute("UV2", GpuFormat.RG16_SINT)
                    .build();
            pipe = RenderPipeline.builder()
                    .withLocation(Identifier.fromNamespaceAndPath("hontun", "pipeline/rounded_gui"))
                    .withVertexShader(Identifier.fromNamespaceAndPath("hontun", "core/rounded_gui"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath("hontun", "core/rounded_gui"))
                    .withVertexBinding(0, fmt)
                    .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withDepthStencilState(Optional.empty())
                    .withCull(false)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .build();
        } catch (Throwable t) {
            state = 2;
            LOG.warn("[Hontun] rounded_gui pipeline could not be built; using analytic fallback", t);
        }
        FORMAT = fmt;
        PIPELINE = pipe;
    }

    private RoundedGui() {}

    private static boolean ready() {
        if (state != 0) return state == 1;
        if (PIPELINE == null) { state = 2; return false; }
        try {
            CompiledRenderPipeline compiled = RenderSystem.getDevice().precompilePipeline(PIPELINE);
            if (compiled != null && compiled.isValid()) {
                state = 1;
                return true;
            }
            state = 2;
            LOG.warn("[Hontun] rounded_gui pipeline is invalid; using analytic fallback");
        } catch (Throwable t) {
            state = 2;
            LOG.warn("[Hontun] rounded_gui pipeline failed to compile; using analytic fallback", t);
        }
        return false;
    }

    public static boolean fill(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius, int argb) {
        if (w <= 0 || h <= 0) return true;
        if ((argb >>> 24) == 0) return true;
        if (!ready()) return false;
        int r = Math.max(0, Math.min(radius, Math.min(w, h) / 2));
        Matrix3x2f pose = new Matrix3x2f(g.pose());
        ScreenRectangle bounds = new ScreenRectangle(x, y, w, h).transformAxisAligned(pose);
        submit(g, pose, x + w * 0.5f, y + h * 0.5f, w * 0.5f, h * 0.5f, w, h, r, argb, bounds);
        return true;
    }

    public static boolean capsule(GuiGraphicsExtractor g, float x0, float y0, float x1, float y1,
                                  float halfThick, int argb) {
        if ((argb >>> 24) == 0) return true;
        if (!ready()) return false;
        int ht = Math.max(1, Math.round(halfThick));
        float dx = x1 - x0, dy = y1 - y0;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        int fullW = Math.max(2 * ht, Math.round(len) + 2 * ht);
        int fullH = 2 * ht;
        float angle = (len < 0.001f) ? 0f : (float) Math.atan2(dy, dx);
        float mx = (x0 + x1) * 0.5f, my = (y0 + y1) * 0.5f;

        Matrix3x2f pose = new Matrix3x2f(g.pose()).translate(mx, my).rotate(angle);

        float bx0 = Math.min(x0, x1) - ht, by0 = Math.min(y0, y1) - ht;
        int bw = (int) Math.ceil(Math.abs(dx) + 2 * ht + 1);
        int bh = (int) Math.ceil(Math.abs(dy) + 2 * ht + 1);
        ScreenRectangle bounds = new ScreenRectangle((int) Math.floor(bx0), (int) Math.floor(by0), bw, bh)
                .transformAxisAligned(new Matrix3x2f(g.pose()));

        submit(g, pose, 0f, 0f, fullW * 0.5f, fullH * 0.5f, fullW, fullH, ht, argb, bounds);
        return true;
    }

    private static void submit(GuiGraphicsExtractor g, Matrix3x2f pose, float cx, float cy,
                               float halfW, float halfH, int fullW, int fullH, int radius, int argb,
                               ScreenRectangle bounds) {
        ScreenRectangle scissor = null;
        try {
            var ss = g.scissorStack;
            if (ss != null) scissor = ss.peek();
        } catch (Throwable ignored) {}
        g.guiRenderState.addGuiElement(new Elem(pose, cx, cy, halfW, halfH, fullW, fullH, radius, argb,
                scissor, bounds));
    }

    private static final class Elem implements GuiElementRenderState {
        private final Matrix3x2f pose;
        private final float cx, cy, halfW, halfH;
        private final int fullW, fullH, radius, argb;
        private final ScreenRectangle scissor, bounds;

        Elem(Matrix3x2f pose, float cx, float cy, float halfW, float halfH,
             int fullW, int fullH, int radius, int argb, ScreenRectangle scissor, ScreenRectangle bounds) {
            this.pose = pose;
            this.cx = cx; this.cy = cy; this.halfW = halfW; this.halfH = halfH;
            this.fullW = fullW; this.fullH = fullH; this.radius = radius; this.argb = argb;
            this.scissor = scissor; this.bounds = bounds;
        }

        @Override
        public void buildVertices(VertexConsumer vc) {
            v(vc, cx - halfW, cy - halfH, -halfW, -halfH);
            v(vc, cx - halfW, cy + halfH, -halfW,  halfH);
            v(vc, cx + halfW, cy + halfH,  halfW,  halfH);
            v(vc, cx + halfW, cy - halfH,  halfW, -halfH);
        }

        private void v(VertexConsumer vc, float px, float py, float lx, float ly) {
            vc.addVertexWith2DPose(pose, px, py)
              .setColor(argb)
              .setUv(lx, ly)
              .setUv1(fullW, fullH)
              .setUv2(radius, 0);
        }

        @Override public RenderPipeline pipeline() { return PIPELINE; }
        @Override public TextureSetup textureSetup() { return TextureSetup.noTexture(); }
        @Override public ScreenRectangle scissorArea() { return scissor; }
        @Override public ScreenRectangle bounds() { return bounds; }
    }
}
