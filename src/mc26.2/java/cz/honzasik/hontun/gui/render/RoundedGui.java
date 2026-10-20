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

    private static final float MARGIN = 1.0f;
    private static final float MAX_UNITS = 32000f;
    private static final int MAX_SCALE = 8;

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

    public static boolean available() {
        return ready();
    }

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
        return shape(g, x, y, w, h, radius, 0f, 0f, argb);
    }

    public static boolean fill(GuiGraphicsExtractor g, float x, float y, float w, float h,
                               float radius, float feather, int argb) {
        return shape(g, x, y, w, h, radius, feather, 0f, argb);
    }

    public static boolean outline(GuiGraphicsExtractor g, float x, float y, float w, float h,
                                  float radius, float thickness, int argb) {
        return shape(g, x, y, w, h, radius, 0f, Math.max(0.25f, thickness), argb);
    }

    public static boolean circle(GuiGraphicsExtractor g, float cx, float cy, float r, float feather, int argb) {
        return shape(g, cx - r, cy - r, r * 2f, r * 2f, r, feather, 0f, argb);
    }

    public static boolean ring(GuiGraphicsExtractor g, float cx, float cy, float r, float thickness, int argb) {
        float outer = r + thickness * 0.5f;
        return shape(g, cx - outer, cy - outer, outer * 2f, outer * 2f, outer, 0f, Math.max(0.25f, thickness), argb);
    }

    public static boolean capsule(GuiGraphicsExtractor g, float x0, float y0, float x1, float y1,
                                  float halfThick, int argb) {
        if ((argb >>> 24) == 0) return true;
        if (!ready()) return false;
        float ht = Math.max(0.25f, halfThick);
        float dx = x1 - x0, dy = y1 - y0;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        float w = len + 2f * ht, h = 2f * ht;
        float angle = (len < 0.001f) ? 0f : (float) Math.atan2(dy, dx);
        float mx = (x0 + x1) * 0.5f, my = (y0 + y1) * 0.5f;

        Matrix3x2f base = new Matrix3x2f(g.pose());
        Matrix3x2f pose = new Matrix3x2f(base).translate(mx, my).rotate(angle);
        ScreenRectangle bounds = rect(Math.min(x0, x1) - ht - MARGIN, Math.min(y0, y1) - ht - MARGIN,
                Math.abs(dx) + 2f * (ht + MARGIN), Math.abs(dy) + 2f * (ht + MARGIN)).transformAxisAligned(base);
        submit(g, pose, 0f, 0f, w, h, ht, 0f, 0f, argb, bounds);
        return true;
    }

    private static boolean shape(GuiGraphicsExtractor g, float x, float y, float w, float h,
                                 float radius, float feather, float ring, int argb) {
        if (w <= 0f || h <= 0f) return true;
        if ((argb >>> 24) == 0) return true;
        if (!ready()) return false;
        float r = Math.max(0f, Math.min(radius, Math.min(w, h) * 0.5f));
        Matrix3x2f pose = new Matrix3x2f(g.pose());
        ScreenRectangle bounds = rect(x - MARGIN, y - MARGIN, w + 2f * MARGIN, h + 2f * MARGIN)
                .transformAxisAligned(pose);
        submit(g, pose, x + w * 0.5f, y + h * 0.5f, w, h, r, feather, ring, argb, bounds);
        return true;
    }

    private static ScreenRectangle rect(float x, float y, float w, float h) {
        int x0 = (int) Math.floor(x), y0 = (int) Math.floor(y);
        int x1 = (int) Math.ceil(x + w), y1 = (int) Math.ceil(y + h);
        return new ScreenRectangle(x0, y0, Math.max(1, x1 - x0), Math.max(1, y1 - y0));
    }

    private static void submit(GuiGraphicsExtractor g, Matrix3x2f pose, float cx, float cy, float w, float h,
                               float radius, float feather, float ring, int argb, ScreenRectangle bounds) {
        float extent = Math.max(w, h) + 2f * MARGIN;
        int scale = Math.max(1, Math.min(MAX_SCALE, (int) (MAX_UNITS / Math.max(1f, extent))));
        int param = ring > 0f
                ? -Math.max(1, Math.round(ring * scale))
                : Math.min(32000, Math.round(Math.max(0f, feather) * scale));

        ScreenRectangle scissor = null;
        try {
            var ss = g.scissorStack;
            if (ss != null) scissor = ss.peek();
        } catch (Throwable ignored) {}

        g.guiRenderState.addGuiElement(new Elem(pose, cx, cy, w * 0.5f + MARGIN, h * 0.5f + MARGIN, scale,
                Math.round(w * scale), Math.round(h * scale), Math.round(radius * scale), param, argb,
                scissor, bounds));
    }

    private static final class Elem implements GuiElementRenderState {
        private final Matrix3x2f pose;
        private final float cx, cy, qw, qh;
        private final int scale, sizeW, sizeH, radius, param, argb;
        private final ScreenRectangle scissor, bounds;

        Elem(Matrix3x2f pose, float cx, float cy, float qw, float qh, int scale,
             int sizeW, int sizeH, int radius, int param, int argb,
             ScreenRectangle scissor, ScreenRectangle bounds) {
            this.pose = pose;
            this.cx = cx; this.cy = cy; this.qw = qw; this.qh = qh;
            this.scale = scale;
            this.sizeW = sizeW; this.sizeH = sizeH; this.radius = radius; this.param = param;
            this.argb = argb;
            this.scissor = scissor; this.bounds = bounds;
        }

        @Override
        public void buildVertices(VertexConsumer vc) {
            v(vc, cx - qw, cy - qh, -qw, -qh);
            v(vc, cx - qw, cy + qh, -qw,  qh);
            v(vc, cx + qw, cy + qh,  qw,  qh);
            v(vc, cx + qw, cy - qh,  qw, -qh);
        }

        private void v(VertexConsumer vc, float px, float py, float lx, float ly) {
            vc.addVertexWith2DPose(pose, px, py)
              .setColor(argb)
              .setUv(lx * scale, ly * scale)
              .setUv1(sizeW, sizeH)
              .setUv2(radius, param);
        }

        @Override public RenderPipeline pipeline() { return PIPELINE; }
        @Override public TextureSetup textureSetup() { return TextureSetup.noTexture(); }
        @Override public ScreenRectangle scissorArea() { return scissor; }
        @Override public ScreenRectangle bounds() { return bounds; }
    }
}
