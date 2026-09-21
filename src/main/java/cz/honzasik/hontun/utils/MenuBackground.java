package cz.honzasik.hontun.utils;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Random;

public class MenuBackground {
    public static volatile boolean forceOnServer = false;

    private static final Random RANDOM = new Random();
    private static final Particle[] PARTICLES = new Particle[120];

    private static int TOP_A, TOP_B;
    private static int BOT_A, BOT_B;
    private static int[] PARTICLE_COLORS = new int[3];
    private static int cachedVersion = -1;

    private static final long PERIOD_MS = 14000L;

    private static final float SPEED_MIN = 2.5f;
    private static final float SPEED_MAX = 9.0f;

    private static int lastWidth = -1;
    private static int lastHeight = -1;
    private static long lastMillis = -1L;

    static {
        for (int i = 0; i < PARTICLES.length; i++) PARTICLES[i] = new Particle();
    }

    public static void render(GuiGraphicsExtractor g, int width, int height, boolean opaque) {
        render(g, width, height, opaque, true);
    }

    public static void render(GuiGraphicsExtractor g, int width, int height, boolean opaque, boolean particles) {
        if (!HontunTheme.restyleEnabled()) return;

        refreshColors();
        boolean modern = HontunTheme.modern();

        long now = System.currentTimeMillis();

        float dt = lastMillis < 0 ? 0f : Math.min(0.1f, (now - lastMillis) / 1000f);
        lastMillis = now;

        if (width != lastWidth || height != lastHeight) {
            for (Particle p : PARTICLES) p.reset(width, height);
            lastWidth = width;
            lastHeight = height;
        }

        double raw = 0.5 + 0.5 * Math.sin((now % PERIOD_MS) / (double) PERIOD_MS * Math.PI * 2.0);
        float s = modern ? (float) (0.35 + 0.15 * (raw * 2 - 1)) : (float) raw;
        int topA = opaque ? 0xFF : (modern ? 0x80 : 0x8C);
        int botA = opaque ? 0xFF : (modern ? 0x92 : 0x9C);
        g.fillGradient(0, 0, width, height, argb(topA, lerp(TOP_A, TOP_B, s)), argb(botA, lerp(BOT_A, BOT_B, s)));

        int count = !particles ? 0 : (modern ? PARTICLES.length / 2 : PARTICLES.length);
        for (int i = 0; i < count; i++) {
            Particle p = PARTICLES[i];
            p.update(width, height, dt);
            int x = (int) p.x, y = (int) p.y;
            g.fill(x, y, x + p.size, y + p.size, p.color);
        }
    }

    private static void refreshColors() {
        int v = HontunTheme.version();
        if (v == cachedVersion) return;
        cachedVersion = v;

        int m = HontunTheme.menuColor();
        TOP_A = m;
        TOP_B = HontunTheme.lighten(m, 1.7f);
        BOT_A = HontunTheme.darken(m, 0.45f);
        BOT_B = m;

        int accHi = HontunTheme.accentHi();
        int acc   = HontunTheme.accent();
        int accLo = HontunTheme.accentLo();
        PARTICLE_COLORS = new int[] { argb(0xCC, accHi), argb(0xCC, acc), argb(0xB0, accLo) };

        for (Particle p : PARTICLES) p.recolor();
    }

    private static int lerp(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = (int) (ar + (br - ar) * t);
        int gg = (int) (ag + (bg - ag) * t);
        int bl = (int) (ab + (bb - ab) * t);
        return (r << 16) | (gg << 8) | bl;
    }

    private static int argb(int a, int rgb) {
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    private static final class Particle {
        float x, y, vx, vy;
        int color, size;

        Particle() {
            reset(0, 0);
        }

        void update(int width, int height, float dt) {
            x += vx * dt;
            y += vy * dt;
            if (x < -4 || x > width + 4 || y < -4 || y > height + 4) reset(width, height);
        }

        void reset(int width, int height) {
            x = RANDOM.nextInt(Math.max(width, 1));
            y = RANDOM.nextInt(Math.max(height, 1));

            double angle = RANDOM.nextDouble() * Math.PI * 2.0;
            float speed = SPEED_MIN + RANDOM.nextFloat() * (SPEED_MAX - SPEED_MIN);
            vx = (float) Math.cos(angle) * speed;
            vy = (float) Math.sin(angle) * speed;
            color = PARTICLE_COLORS[RANDOM.nextInt(PARTICLE_COLORS.length)];
            size = 2 + RANDOM.nextInt(2);
        }

        void recolor() {
            color = PARTICLE_COLORS[RANDOM.nextInt(PARTICLE_COLORS.length)];
        }
    }
}
