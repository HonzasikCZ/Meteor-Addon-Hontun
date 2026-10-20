package cz.honzasik.hontun.gui.transition;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.minecraft.client.renderer.DynamicGpuDataStorage;
import net.minecraft.client.renderer.DynamicGpuDataStorageMapped;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;

final class GhostPass {
    private static final Logger LOG = LoggerFactory.getLogger("Hontun/ScreenTransition");

    private static final long IDLE_NANOS = 10_000_000_000L;
    private static final int UBO_SIZE = new Std140SizeCalculator()
            .putVec4()
            .putVec4()
            .get();

    private static final RenderPipeline PIPELINE;

    private static final Data DATA = new Data();

    private static int state = 0;
    private static DynamicGpuDataStorage<Data> storage;
    private static GpuBuffer triangle;
    private static GpuBufferSlice triangleSlice;
    private static GpuTexture ghost;
    private static GpuTextureView ghostView;
    private static boolean wrote;
    private static long lastUse;

    static {
        RenderPipeline pipe = null;
        try {
            VertexFormat format = VertexFormat.builder(0)
                    .addAttribute("Position", GpuFormat.RG32_FLOAT)
                    .build();
            pipe = RenderPipeline.builder()
                    .withLocation(Identifier.fromNamespaceAndPath("hontun", "pipeline/screen_transition"))
                    .withVertexShader(Identifier.fromNamespaceAndPath("hontun", "core/screen_transition"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath("hontun", "core/screen_transition"))
                    .withVertexBinding(0, format)
                    .withBindGroupLayout(BindGroupLayout.builder()
                            .withUniform("TransitionData", UniformType.UNIFORM_BUFFER)
                            .build())
                    .withBindGroupLayout(BindGroupLayout.builder()
                            .withUniform("Ghost", UniformType.COMBINED_IMAGE_SAMPLER)
                            .build())
                    .withColorTargetState(new ColorTargetState(Optional.of(BlendFunction.TRANSLUCENT),
                            GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_COLOR))
                    .withDepthStencilState(Optional.empty())
                    .withCull(false)
                    .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                    .build();
        } catch (Throwable t) {
            fail("pipeline could not be built", t);
        }
        PIPELINE = pipe;
    }

    private GhostPass() {}

    static boolean available() {
        return state != 2;
    }

    static void warm() {
        if (state == 0) ready();
    }

    static void shutdown() {
        state = 2;
        try {
            release();
        } catch (Throwable ignored) {}
    }

    static boolean capture(GpuTexture source, int width, int height) {
        if (state == 2 || source == null || source.isClosed() || width <= 0 || height <= 0) return false;
        if (source.getWidth(0) < width || source.getHeight(0) < height) return false;
        try {
            if (!ready()) return false;
            GpuDevice device = RenderSystem.getDevice();
            if (compiled() == null) return false;
            ensureGhost(device, width, height, source.getFormat());
            device.createCommandEncoder().copyTextureToTexture(source, ghost, 0, 0, 0, 0, 0, width, height);
            lastUse = System.nanoTime();
            return true;
        } catch (Throwable t) {
            fail("could not capture the previous screen", t);
            return false;
        }
    }

    static boolean draw(RenderTarget target, float alpha, float scale, float x0, float y0, float x1, float y1) {
        if (state != 1 || ghost == null || ghost.isClosed() || ghostView == null) return false;
        try {
            GpuTextureView output = target.getColorTextureView();
            if (output == null || output.isClosed()
                    || target.width != ghost.getWidth(0) || target.height != ghost.getHeight(0)) {
                release();
                return false;
            }
            GpuDevice device = RenderSystem.getDevice();
            CompiledRenderPipeline compiled = compiled();
            if (compiled == null) return false;
            DATA.alpha = alpha;
            DATA.scale = scale;
            DATA.x0 = x0;
            DATA.y0 = y0;
            DATA.x1 = x1;
            DATA.y1 = y1;
            RenderSystem.AutoStorageIndexBuffer sequential = RenderSystem.getSequentialBuffer(PrimitiveTopology.TRIANGLES);
            GpuBuffer indices = sequential.getBuffer(3);
            GpuBufferSlice uniforms = storage.writeData(DATA);
            wrote = true;
            GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
            try (RenderPass pass = device.createCommandEncoder()
                    .createRenderPass(() -> "Hontun - Screen transition", output, Optional.empty())) {
                pass.setPipeline(compiled);
                pass.setUniform("TransitionData", uniforms);
                pass.setUniform("Ghost", ghostView, sampler);
                pass.setVertexBuffer(0, triangleSlice);
                pass.setIndexBuffer(indices, sequential.type());
                pass.drawIndexed(3, 1, 0, 0, 0);
            }
            lastUse = System.nanoTime();
            return true;
        } catch (Throwable t) {
            fail("failed while drawing", t);
            return false;
        }
    }

    static void endFrame(boolean active) {
        if (state != 1) return;
        try {
            if (wrote) {
                wrote = false;
                storage.endFrame();
            }
            if (!active && ghost != null && System.nanoTime() - lastUse > IDLE_NANOS) release();
        } catch (Throwable t) {
            fail("failed at the end of a frame", t);
        }
    }

    private static boolean ready() {
        if (state != 0) return state == 1;
        if (PIPELINE == null) {
            fail("pipeline is missing", null);
            return false;
        }
        try {
            GpuDevice device = RenderSystem.getDevice();
            CompiledRenderPipeline compiled = RenderSystem.getCompiledPipelineNullable(PIPELINE);
            if (compiled == null || compiled.isClosed()) {
                fail("shader did not compile", null);
                return false;
            }
            storage = new DynamicGpuDataStorageMapped<>("Hontun - Screen transition UBO", UBO_SIZE, GpuBuffer.USAGE_UNIFORM, 2);
            ByteBuffer vertices = ByteBuffer.allocateDirect(6 * Float.BYTES).order(ByteOrder.nativeOrder());
            vertices.putFloat(-1f).putFloat(-1f)
                    .putFloat(3f).putFloat(-1f)
                    .putFloat(-1f).putFloat(3f)
                    .flip();
            triangle = device.createBuffer(() -> "Hontun - Screen transition triangle", GpuBuffer.USAGE_VERTEX, vertices);
            triangleSlice = triangle.slice();
            state = 1;
            return true;
        } catch (Throwable t) {
            fail("could not be initialised", t);
            return false;
        }
    }

    private static CompiledRenderPipeline compiled() {
        CompiledRenderPipeline compiled = RenderSystem.getCompiledPipelineNullable(PIPELINE);
        if (compiled != null && !compiled.isClosed()) return compiled;
        fail("shader is no longer valid", null);
        return null;
    }

    private static void ensureGhost(GpuDevice device, int w, int h, GpuFormat format) {
        if (ghost != null && !ghost.isClosed() && ghost.getWidth(0) == w && ghost.getHeight(0) == h
                && ghost.getFormat() == format) return;
        release();
        ghost = device.createTexture(() -> "Hontun - Screen transition ghost",
                GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING,
                format, w, h, 1, 1);
        ghostView = device.createTextureView(ghost);
    }

    private static void release() {
        GpuTextureView v = ghostView;
        GpuTexture t = ghost;
        ghostView = null;
        ghost = null;
        if (v != null) v.close();
        if (t != null) t.close();
    }

    private static void fail(String what, Throwable t) {
        if (state == 2) return;
        state = 2;
        if (t != null) LOG.warn("[Hontun] Screen transition {}; screens switch instantly for this session", what, t);
        else LOG.warn("[Hontun] Screen transition {}; screens switch instantly for this session", what);
        try {
            release();
        } catch (Throwable ignored) {}
    }

    private static final class Data implements DynamicGpuDataStorage.DynamicGpuData {
        private float alpha;
        private float scale;
        private float x0;
        private float y0;
        private float x1;
        private float y1;

        @Override
        public void write(@NonNull ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer)
                    .putVec4(alpha, scale, 0f, 0f)
                    .putVec4(x0, y0, x1, y1);
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }
    }
}
