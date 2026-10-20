package cz.honzasik.hontun.utils;

import com.mojang.blaze3d.platform.Window;
import cz.honzasik.hontun.gui.render.RoundedGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public final class SmogBackground {
    private static final int COUNT = 120;
    private static final float LINK = 78f;
    private static final Dot[] DOTS = new Dot[COUNT];
    private static final Random RNG = new Random(0x534D4F47L);
    private static final double[] CURSOR_X = new double[1];
    private static final double[] CURSOR_Y = new double[1];

    private static int lastW = -1, lastH = -1;
    private static long lastNanos = -1L;
    private static float time;
    private static float parX, parY;

    static {
        for (int i = 0; i < COUNT; i++) DOTS[i] = new Dot();
    }

    private SmogBackground() {}

    public static void render(GuiGraphicsExtractor g, int width, int height, boolean opaque, boolean particles) {
        long now = System.nanoTime();
        float dt = lastNanos < 0 ? 0f : Math.min(0.1f, (now - lastNanos) / 1_000_000_000f);
        lastNanos = now;
        time += dt;

        if (width != lastW || height != lastH) {
            for (Dot d : DOTS) d.seed(width, height, true);
            lastW = width;
            lastH = height;
        }

        if (!RoundedGui.available()) {
            g.fillGradient(0, 0, width, height,
                    opaque ? 0xFF0F0F11 : 0xA80F0F11,
                    opaque ? 0xFF030304 : 0xC8030304);
            if (particles) legacy(g, width, height, dt);
            return;
        }

        if (opaque) {
            g.fill(0, 0, width, height, 0xFF030304);
            float f = height;
            RoundedGui.fill(g, -f, -f, width + 2f * f, 2f * f, 0f, f, 0xFF0F0F11);
        } else {
            g.fill(0, 0, width, height, 0xB4060607);
        }

        pointer(dt);

        float span = Math.max(width, height);
        orb(g, width * (0.26f + 0.10f * sin(time * 0.071f)), height * (0.28f + 0.08f * cos(time * 0.053f)),
                span * 0.42f, 0x15);
        orb(g, width * (0.76f + 0.08f * cos(time * 0.047f)), height * (0.64f + 0.10f * sin(time * 0.061f)),
                span * 0.36f, 0x11);
        orb(g, width * (0.52f + 0.14f * sin(time * 0.033f + 1.7f)), height * (0.95f + 0.05f * cos(time * 0.041f)),
                span * 0.30f, 0x0D);

        if (!particles) return;

        for (Dot d : DOTS) d.update(width, height, dt);

        for (int i = 0; i < COUNT; i++) {
            Dot a = DOTS[i];
            if (a.layer == 0) continue;
            for (int j = i + 1; j < COUNT; j++) {
                Dot b = DOTS[j];
                if (b.layer == 0) continue;
                float dx = b.sx - a.sx, dy = b.sy - a.sy;
                float d2 = dx * dx + dy * dy;
                if (d2 >= LINK * LINK) continue;
                float k = 1f - (float) Math.sqrt(d2) / LINK;
                int alpha = (int) (0x48 * k * k * Math.min(a.glow, b.glow));
                if (alpha < 3) continue;
                RoundedGui.capsule(g, a.sx, a.sy, b.sx, b.sy, 0.32f, (alpha << 24) | 0xC8CDD2);
            }
        }

        for (Dot d : DOTS) {
            if (d.layer != 2) continue;
            int alpha = (int) (0x24 * d.glow);
            if (alpha > 2) RoundedGui.circle(g, d.sx, d.sy, d.r * 3.4f, d.r * 3.4f, (alpha << 24) | 0xFFFFFF);
        }
        for (Dot d : DOTS) {
            int alpha = (int) (d.alpha * d.glow);
            if (alpha > 2) RoundedGui.circle(g, d.sx, d.sy, d.r, 0f, (alpha << 24) | d.rgb);
        }
    }

    private static void orb(GuiGraphicsExtractor g, float x, float y, float r, int alpha) {
        RoundedGui.circle(g, x, y, r, r, (alpha << 24) | 0xFFFFFF);
    }

    private static void pointer(float dt) {
        float tx = 0f, ty = 0f;
        try {
            Window w = Minecraft.getInstance().getWindow();
            if (w.getScreenWidth() > 0 && w.getScreenHeight() > 0) {
                GLFW.glfwGetCursorPos(w.handle(), CURSOR_X, CURSOR_Y);
                tx = (float) (CURSOR_X[0] / w.getScreenWidth()) * 2f - 1f;
                ty = (float) (CURSOR_Y[0] / w.getScreenHeight()) * 2f - 1f;
            }
        } catch (Throwable ignored) {}
        tx = Math.max(-1f, Math.min(1f, tx));
        ty = Math.max(-1f, Math.min(1f, ty));
        float k = Math.min(1f, dt * 2.5f);
        parX += (tx - parX) * k;
        parY += (ty - parY) * k;
    }

    private static void legacy(GuiGraphicsExtractor g, int width, int height, float dt) {
        for (Dot d : DOTS) {
            d.update(width, height, dt);
            int alpha = (int) (d.alpha * d.glow);
            int s = d.layer == 2 ? 2 : 1;
            g.fill((int) d.sx, (int) d.sy, (int) d.sx + s, (int) d.sy + s, (alpha << 24) | d.rgb);
        }
    }

    private static float sin(float v) { return (float) Math.sin(v); }
    private static float cos(float v) { return (float) Math.cos(v); }

    private static final class Dot {
        final int layer;
        float x, y, sx, sy, r, speed, sway, swayF, phase, twinkle, glow = 1f;
        int alpha, rgb;

        Dot() {
            float roll = RNG.nextFloat();
            layer = roll < 0.45f ? 0 : (roll < 0.82f ? 1 : 2);
        }

        void seed(int w, int h, boolean anywhere) {
            x = RNG.nextFloat() * Math.max(1, w);
            y = anywhere ? RNG.nextFloat() * Math.max(1, h) : h + 4f + RNG.nextFloat() * 24f;
            phase = RNG.nextFloat() * 6.2831855f;
            twinkle = 0.6f + RNG.nextFloat() * 1.4f;
            swayF = 0.15f + RNG.nextFloat() * 0.35f;
            switch (layer) {
                case 0 -> {
                    r = 0.55f + RNG.nextFloat() * 0.30f;
                    speed = 3f + RNG.nextFloat() * 3f;
                    sway = 3f;
                    alpha = 0x70 + RNG.nextInt(0x30);
                    rgb = 0x7C8188;
                }
                case 1 -> {
                    r = 0.85f + RNG.nextFloat() * 0.40f;
                    speed = 6f + RNG.nextFloat() * 5f;
                    sway = 5f;
                    alpha = 0x98 + RNG.nextInt(0x30);
                    rgb = 0xB4B9BF;
                }
                default -> {
                    r = 1.30f + RNG.nextFloat() * 0.60f;
                    speed = 10f + RNG.nextFloat() * 7f;
                    sway = 8f;
                    alpha = 0xC0 + RNG.nextInt(0x3F);
                    rgb = 0xF4F6F8;
                }
            }
        }

        void update(int w, int h, float dt) {
            y -= speed * dt;
            x += sin(time * swayF + phase) * sway * dt;
            if (y < -6f) seed(w, h, false);
            if (x < -6f) x += w + 12f;
            else if (x > w + 6f) x -= w + 12f;
            glow = 0.72f + 0.28f * sin(time * twinkle + phase);
            float depth = layer == 0 ? 3f : (layer == 1 ? 7f : 13f);
            sx = x - parX * depth;
            sy = y - parY * depth * 0.6f;
        }
    }
}
