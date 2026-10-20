package cz.honzasik.hontun.gui.render;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
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
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.DynamicGpuDataStorage;
import net.minecraft.client.renderer.DynamicGpuDataStorageMapped;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.lwjgl.sdl.SDLMouse;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Optional;

public final class BlockyHorizon {
    private static final Logger LOG = LoggerFactory.getLogger("Hontun/BlockyHorizon");

    private static final float RES_SCALE = 1.0f;
    private static final double TIME_WRAP = 3600.0;
    private static final float POINTER_RATE = 2.5f;
    private static final int IDLE_FRAMES = 600;
    private static final int SMOG_BASE = 0x0F0F11;
    private static final int UBO_SIZE = new Std140SizeCalculator()
            .putVec4()
            .putVec4()
            .putVec4()
            .putVec4()
            .putVec4()
            .putVec4()
            .get();

    private static final RenderPipeline PIPELINE;

    private static final Data DATA = new Data();
    private static final double[] CURSOR_X = new double[1];
    private static final double[] CURSOR_Y = new double[1];
    private static final long START = System.nanoTime();

    private static int state = 0;
    private static DynamicGpuDataStorage<Data> storage;
    private static GpuBuffer triangle;
    private static GpuTexture texture;
    private static GpuTextureView view;

    private static long frame;
    private static long drawnFrame = -1L;
    private static long lastNanos = -1L;
    private static boolean pointerSet;
    private static float pointerX = 0.5f;
    private static float pointerY = 0.5f;

    static {
        RenderPipeline pipe = null;
        try {
            VertexFormat format = VertexFormat.builder(0)
                    .addAttribute("Position", GpuFormat.RG32_FLOAT)
                    .build();
            pipe = RenderPipeline.builder()
                    .withLocation(Identifier.fromNamespaceAndPath("hontun", "pipeline/blocky_horizon"))
                    .withVertexShader(Identifier.fromNamespaceAndPath("hontun", "core/blocky_horizon"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath("hontun", "core/blocky_horizon"))
                    .withVertexBinding(0, format)
                    .withBindGroupLayout(BindGroupLayout.builder()
                            .withUniform("BlockyData", UniformType.UNIFORM_BUFFER)
                            .build())
                    .withColorTargetState(ColorTargetState.DEFAULT)
                    .withDepthStencilState(Optional.empty())
                    .withCull(false)
                    .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                    .build();
        } catch (Throwable t) {
            fail("pipeline could not be built", t);
        }
        PIPELINE = pipe;
    }

    private BlockyHorizon() {}

    public static boolean render(GuiGraphicsExtractor g, int width, int height, HontunTheme.UiMode look) {
        if (state == 2 || width <= 0 || height <= 0) return false;
        try {
            if (!ready()) return false;
            Window window = Minecraft.getInstance().getWindow();
            int fbW = window.getWidth();
            int fbH = window.getHeight();
            if (fbW <= 0 || fbH <= 0) return false;
            int texW = Math.max(1, Math.round(fbW * RES_SCALE));
            int texH = Math.max(1, Math.round(fbH * RES_SCALE));
            boolean sized = texture != null && !texture.isClosed()
                    && texture.getWidth(0) == texW && texture.getHeight(0) == texH;
            if (drawnFrame != frame || !sized) {
                GpuDevice device = RenderSystem.getDevice();
                if (!valid(device)) return false;
                ensureTarget(device, texW, texH);
                fill(texW, texH, look);
                encode(device, view);
                drawnFrame = frame;
            }
            int scale = Math.max(1, window.getGuiScale());
            float u1 = (float) (width * scale) / fbW;
            float v1 = 1f - (float) (height * scale) / fbH;
            GpuSampler sampler = RenderSystem.getSamplerCache()
                    .getClampToEdge(RES_SCALE < 1f ? FilterMode.LINEAR : FilterMode.NEAREST);
            g.blit(view, sampler, 0, 0, width, height, 0f, u1, 1f, v1);
            return true;
        } catch (Throwable t) {
            fail("failed while rendering", t);
            return false;
        }
    }

    public static boolean renderInto(GpuTextureView target, int width, int height, HontunTheme.UiMode look) {
        if (state == 2 || target == null || width <= 0 || height <= 0) return false;
        try {
            if (!ready()) return false;
            GpuDevice device = RenderSystem.getDevice();
            if (!valid(device)) return false;
            fill(width, height, look);
            encode(device, target);
            return true;
        } catch (Throwable t) {
            fail("failed while rendering", t);
            return false;
        }
    }

    public static boolean available() {
        return state != 2;
    }

    public static void endFrame() {
        if (state != 1) return;
        try {
            frame++;
            storage.endFrame();
            if (texture != null && frame - drawnFrame > IDLE_FRAMES) release();
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
            storage = new DynamicGpuDataStorageMapped<>("Hontun - Blocky Horizon UBO", UBO_SIZE, GpuBuffer.USAGE_UNIFORM, 4);
            ByteBuffer vertices = ByteBuffer.allocateDirect(6 * Float.BYTES).order(ByteOrder.nativeOrder());
            vertices.putFloat(-1f).putFloat(-1f)
                    .putFloat(3f).putFloat(-1f)
                    .putFloat(-1f).putFloat(3f)
                    .flip();
            triangle = device.createBuffer(() -> "Hontun - Blocky Horizon triangle", GpuBuffer.USAGE_VERTEX, vertices);
            state = 1;
            return true;
        } catch (Throwable t) {
            fail("could not be initialised", t);
            return false;
        }
    }

    private static boolean valid(GpuDevice device) {
        CompiledRenderPipeline compiled = RenderSystem.getCompiledPipelineNullable(PIPELINE);
        if (compiled != null && !compiled.isClosed()) return true;
        fail("shader is no longer valid", null);
        return false;
    }

    private static void ensureTarget(GpuDevice device, int w, int h) {
        if (texture != null && !texture.isClosed() && texture.getWidth(0) == w && texture.getHeight(0) == h) return;
        release();
        texture = device.createTexture(() -> "Hontun - Blocky Horizon",
                GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_RENDER_ATTACHMENT,
                GpuFormat.RGBA8_UNORM, w, h, 1, 1);
        view = device.createTextureView(texture);
    }

    private static void encode(GpuDevice device, GpuTextureView target) {
        RenderSystem.AutoStorageIndexBuffer sequential = RenderSystem.getSequentialBuffer(PrimitiveTopology.TRIANGLES);
        GpuBuffer indices = sequential.getBuffer(3);
        GpuBufferSlice uniforms = storage.writeData(DATA);
        CompiledRenderPipeline compiled = RenderSystem.getCompiledPipeline(PIPELINE);
        try (RenderPass pass = device.createCommandEncoder()
                .createRenderPass(() -> "Hontun - Blocky Horizon", target, Optional.empty())) {
            pass.setPipeline(compiled);
            pass.setUniform("BlockyData", uniforms);
            pass.setVertexBuffer(0, triangle.slice());
            pass.setIndexBuffer(indices, sequential.type());
            pass.drawIndexed(3, 1, 0, 0, 0);
        }
    }

    private static void fill(int w, int h, HontunTheme.UiMode look) {
        long now = System.nanoTime();
        float dt = lastNanos < 0L ? 0f : Math.min(0.1f, (now - lastNanos) / 1_000_000_000f);
        lastNanos = now;
        pointer(dt);

        boolean smog = look == HontunTheme.UiMode.SmogClient;
        DATA.width = w;
        DATA.height = h;
        DATA.time = (float) (((now - START) / 1_000_000_000.0) % TIME_WRAP);
        DATA.pointerX = pointerX;
        DATA.pointerY = pointerY;
        DATA.accent = smog ? HontunTheme.SMOG_ACCENT : HontunTheme.userAccent();
        DATA.accentHi = smog ? HontunTheme.SMOG_ACCENT_HI : HontunTheme.userAccentHi();
        DATA.accentLo = smog ? HontunTheme.SMOG_ACCENT_LO : HontunTheme.userAccentLo();
        DATA.base = smog ? SMOG_BASE : HontunTheme.menuColor();
    }

    private static void pointer(float dt) {
        float tx = pointerX;
        float ty = pointerY;
        try {
            Window w = Minecraft.getInstance().getWindow();
            if (w.getScreenWidth() > 0 && w.getScreenHeight() > 0) {
                cursor(w.handle());
                tx = clamp01((float) (CURSOR_X[0] / w.getScreenWidth()));
                ty = clamp01(1f - (float) (CURSOR_Y[0] / w.getScreenHeight()));
            }
        } catch (Throwable ignored) {}
        if (!pointerSet) {
            pointerX = tx;
            pointerY = ty;
            pointerSet = true;
            return;
        }
        float k = 1f - (float) Math.exp(-dt * POINTER_RATE);
        pointerX += (tx - pointerX) * k;
        pointerY += (ty - pointerY) * k;
    }

    private static void cursor(long handle) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer mx = stack.mallocFloat(1);
            FloatBuffer my = stack.mallocFloat(1);
            IntBuffer wx = stack.mallocInt(1);
            IntBuffer wy = stack.mallocInt(1);
            SDLMouse.SDL_GetGlobalMouseState(mx, my);
            if (SDLVideo.SDL_GetWindowPosition(handle, wx, wy)) {
                CURSOR_X[0] = mx.get(0) - wx.get(0);
                CURSOR_Y[0] = my.get(0) - wy.get(0);
            } else {
                SDLMouse.SDL_GetMouseState(mx, my);
                CURSOR_X[0] = mx.get(0);
                CURSOR_Y[0] = my.get(0);
            }
        }
    }

    private static float clamp01(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }

    private static void release() {
        GpuTextureView v = view;
        GpuTexture t = texture;
        view = null;
        texture = null;
        if (v != null) v.close();
        if (t != null) t.close();
    }

    private static void fail(String what, Throwable t) {
        if (state == 2) return;
        state = 2;
        if (t != null) LOG.warn("[Hontun] Blocky Horizon menu scene {}; using the particle background for this session", what, t);
        else LOG.warn("[Hontun] Blocky Horizon menu scene {}; using the particle background for this session", what);
        try {
            release();
        } catch (Throwable ignored) {}
    }

    private static final class Data implements DynamicGpuDataStorage.DynamicGpuData {
        private float width;
        private float height;
        private float time;
        private float pointerX;
        private float pointerY;
        private int accent;
        private int accentHi;
        private int accentLo;
        private int base;

        @Override
        public void write(@NonNull ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer)
                    .putVec4(width, height, time, 0f)
                    .putVec4(pointerX, pointerY, 0f, 0f)
                    .putVec4(r(accent), g(accent), b(accent), 1f)
                    .putVec4(r(accentHi), g(accentHi), b(accentHi), 1f)
                    .putVec4(r(accentLo), g(accentLo), b(accentLo), 1f)
                    .putVec4(r(base), g(base), b(base), 1f);
        }

        private static float r(int rgb) {
            return ((rgb >> 16) & 0xFF) / 255f;
        }

        private static float g(int rgb) {
            return ((rgb >> 8) & 0xFF) / 255f;
        }

        private static float b(int rgb) {
            return (rgb & 0xFF) / 255f;
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
