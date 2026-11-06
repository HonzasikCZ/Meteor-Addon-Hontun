package cz.honzasik.hontun.gui.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import cz.honzasik.hontun.gui.render.pixel.GlyphBatchState;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.joml.Matrix3x2fStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.stb.STBTruetype.stbtt_GetCodepointBitmapBoxSubpixel;
import static org.lwjgl.stb.STBTruetype.stbtt_GetCodepointHMetrics;
import static org.lwjgl.stb.STBTruetype.stbtt_GetFontVMetrics;
import static org.lwjgl.stb.STBTruetype.stbtt_InitFont;
import static org.lwjgl.stb.STBTruetype.stbtt_MakeCodepointBitmapSubpixel;
import static org.lwjgl.stb.STBTruetype.stbtt_ScaleForMappingEmToPixels;

public final class MatrixRain {
    public enum Charset {
        Hex("0123456789ABCDEF+-*/=<>_|"),
        Ascii("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ$#%&*+=<>/\\|{}[]():;~^?!"),
        Binary("01");

        private final String glyphs;

        Charset(String glyphs) {
            this.glyphs = glyphs;
        }

        public String glyphs() {
            return glyphs;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger("Hontun/MatrixRain");
    private static final String FONT = "/assets/hontun/font/mono.ttf";

    private static final int STREAMS = 2;
    private static final int MAX_LEN = 28;
    private static final double MIN_ALPHA = 0.015;
    private static final int SCANLINE_ARGB = 0x42000000;

    private static final double[] WHITE = {255, 255, 255};

    private static volatile boolean enabled = true;
    private static volatile Charset charset = Charset.Hex;
    private static volatile boolean mirror = true;
    private static volatile double speed = 1.0;
    private static volatile double density = 0.7;
    private static volatile double brightness = 0.7;
    private static volatile boolean scanlines = false;

    private static final GlyphBatchState.Builder[] BATCHES = {GlyphBatchState.builder(), GlyphBatchState.builder()};
    private static final GlyphBatchState.Builder SCAN = GlyphBatchState.builder();
    private static int flip;

    private static ByteBuffer fontData;
    private static STBTTFontinfo fontInfo;
    private static boolean fontFailed;
    private static boolean atlasFailed;

    private static DynamicTexture atlas;
    private static DynamicTexture retired;
    private static DynamicTexture scanTexture;
    private static Charset atlasCharset;
    private static int atlasCw, atlasCh, glyphCount;
    private static float cellU, solidU;

    private static int gridW = -1, gridH = -1, gridCw, gridCh, cols, rows;
    private static double gridDensity = -1;
    private static float[] colDep = new float[0];
    private static byte[] colStreams = new byte[0];
    private static double[] streamSpeed = new double[0];
    private static int[] streamLen = new int[0];
    private static int[] streamSpan = new int[0];
    private static double[] streamPhase = new double[0];
    private static float[] cellRate = new float[0];
    private static float[] cellOffset = new float[0];
    private static float[] cellShade = new float[0];
    private static boolean[] cellMirror = new boolean[0];

    private static int colorVersion = Integer.MIN_VALUE;
    private static boolean colorSmog;
    private static int rainBg, trail, head;

    private static final long EPOCH = System.nanoTime();
    private static long lastNanos;
    private static double clock;

    private MatrixRain() {}

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static boolean enabled() {
        return enabled;
    }

    public static void setCharset(Charset value) {
        if (value != null) charset = value;
    }

    public static void setMirror(boolean value) {
        mirror = value;
    }

    public static void setSpeed(double value) {
        speed = clamp(value, 0.2, 3.0);
    }

    public static void setDensity(double value) {
        density = clamp(value, 0.1, 1.0);
    }

    public static void setBrightness(double value) {
        brightness = clamp(value, 0.1, 1.0);
    }

    public static void setScanlines(boolean value) {
        scanlines = value;
    }

    public static boolean scanlines() {
        return scanlines;
    }

    public static boolean render(GuiGraphicsExtractor g, int width, int height, HontunTheme.UiMode look) {
        if (g == null || width <= 0 || height <= 0) return false;

        Window window = Minecraft.getInstance().getWindow();
        int scale = Math.max(1, window.getGuiScale());
        int pw = width * scale;
        int ph = height * scale;

        double k = cellScale(window.getHeight());
        int cw = (int) Math.round(14 * k);
        int ch = (int) Math.round(18 * k);

        if (!ensureAtlas(charset, cw, ch, k)) return false;
        ensureGrid(pw, ph, cw, ch, density);
        ensureColors(look == HontunTheme.UiMode.SmogClient);

        long now = System.nanoTime();
        double dt = lastNanos == 0L ? 0.0 : clamp((now - lastNanos) / 1e9, 0.0, 0.1);
        lastNanos = now;
        clock += dt * speed;

        double t = (now - EPOCH) / 1e9;
        double time = clock;
        double br = brightness;
        boolean mir = mirror;
        int n = glyphCount;
        int rowCount = rows;
        int colCount = cols;
        float cu = cellU;
        int trailRgb = trail & 0xFFFFFF;
        int headRgb = head & 0xFFFFFF;

        GlyphBatchState.Builder b = BATCHES[flip ^= 1].reset();
        b.add(0, 0, pw, ph, solidU, 0.5f, solidU, 0.5f, 0xFF000000 | rainBg);

        for (int c = 0; c < colCount; c++) {
            int ns = colStreams[c];
            if (ns == 0) continue;
            double dep = colDep[c];
            float x0 = c * cw, x1 = x0 + cw;
            double headAlpha = Math.min(1.0, (0.5 + 0.5 * dep) * (0.45 + br));
            double trailScale = dep * br;

            for (int s = 0; s < ns; s++) {
                int si = c * STREAMS + s;
                int len = streamLen[si];
                int span = streamSpan[si];
                double pos = (time * streamSpeed[si] + streamPhase[si]) % span;
                int top = (int) pos;
                double f = pos - top;

                int kFrom = Math.max(0, top - (rowCount - 1));
                int kTo = Math.min(len - 1, top);
                for (int kk = kFrom; kk <= kTo; kk++) {
                    int row = top - kk;
                    double a;
                    if (kk == 0) {
                        a = headAlpha;
                    } else {
                        double q = 1.0 - (kk - 1 + f) / len;
                        a = q * q * trailScale;
                    }
                    if (a < MIN_ALPHA) {
                        if (kk == 0) continue;
                        break;
                    }

                    int cell = row * colCount + c;
                    int alpha = (int) (a * cellShade[cell] * 255.0 + 0.5);
                    if (alpha <= 0) continue;
                    if (alpha > 255) alpha = 255;

                    double rate = kk == 0 ? 9.0 : cellRate[cell];
                    int gi = (int) (hash(c, row, (int) (t * rate + cellOffset[cell])) * n);
                    if (gi >= n) gi = n - 1;

                    float u0 = gi * cu, u1 = u0 + cu;
                    if (mir && cellMirror[cell]) {
                        float sw = u0;
                        u0 = u1;
                        u1 = sw;
                    }

                    float y0 = row * ch;
                    b.add(x0, y0, x1, y0 + ch, u0, 0f, u1, 1f, (alpha << 24) | (kk == 0 ? headRgb : trailRgb));
                }
            }
        }

        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.scale(1f / scale, 1f / scale);
        b.submit(g, atlas);
        pose.popMatrix();
        return true;
    }

    public static void renderScanlines(GuiGraphicsExtractor g) {
        if (!scanlines || g == null) return;

        Window window = Minecraft.getInstance().getWindow();
        int scale = Math.max(1, window.getGuiScale());
        int pw = window.getGuiScaledWidth() * scale;
        int ph = window.getGuiScaledHeight() * scale;
        if (pw <= 0 || ph <= 0 || !ensureScanTexture()) return;

        SCAN.reset();
        for (int y = 0; y < ph; y += 3) SCAN.add(0, y, pw, y + 1, 0.5f, 0.5f, 0.5f, 0.5f, SCANLINE_ARGB);

        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.scale(1f / scale, 1f / scale);
        SCAN.submit(g, scanTexture);
        pose.popMatrix();
    }

    public static void reset() {
        lastNanos = 0L;
        clock = 0.0;
    }

    private static double cellScale(int windowHeight) {
        return clamp(Math.round(windowHeight / 1080.0 * 4.0) / 4.0, 1.0, 2.0);
    }

    private static void ensureGrid(int pw, int ph, int cw, int ch, double dens) {
        if (pw == gridW && ph == gridH && cw == gridCw && ch == gridCh && dens == gridDensity) return;
        gridW = pw;
        gridH = ph;
        gridCw = cw;
        gridCh = ch;
        gridDensity = dens;

        cols = (pw + cw - 1) / cw;
        rows = (ph + ch - 1) / ch + 1;

        if (colDep.length < cols) {
            colDep = new float[cols];
            colStreams = new byte[cols];
            streamSpeed = new double[cols * STREAMS];
            streamLen = new int[cols * STREAMS];
            streamSpan = new int[cols * STREAMS];
            streamPhase = new double[cols * STREAMS];
        }

        int quads = 1;
        for (int c = 0; c < cols; c++) {
            double dep = 0.3 + 0.7 * hash(c, 1, 7);
            int ns = hash(c, 2, 7) < dens ? (hash(c, 3, 7) < dens * 0.5 ? 2 : 1) : 0;
            colDep[c] = (float) dep;
            colStreams[c] = (byte) ns;
            for (int s = 0; s < STREAMS; s++) {
                int si = c * STREAMS + s;
                int len = 6 + (int) Math.floor(22 * hash(c, 5, s));
                int span = rows + len + (int) Math.floor(rows * hash(c, 6, s));
                streamSpeed[si] = (6 + 14 * hash(c, 4, s)) * (0.55 + 0.45 * dep);
                streamLen[si] = len;
                streamSpan[si] = span;
                streamPhase[si] = hash(c, 8, s) * span;
                if (s < ns) quads += Math.min(len, rows);
            }
        }

        int cells = cols * rows;
        if (cellRate.length < cells) {
            cellRate = new float[cells];
            cellOffset = new float[cells];
            cellShade = new float[cells];
            cellMirror = new boolean[cells];
        }

        for (int r = 0; r < rows; r++) {
            double dy = (((r + 0.5) * ch) / ph - 0.45) / 0.75;
            for (int c = 0; c < cols; c++) {
                int cell = r * cols + c;
                double dx = (((c + 0.5) * cw) / pw - 0.5) / 0.8;
                double d = Math.min(1.0, Math.sqrt(dx * dx + dy * dy));
                cellRate[cell] = (float) (0.3 + 2 * hash(r, c, 9));
                cellOffset[cell] = (float) (hash(c, r, 3) * 9);
                cellMirror[cell] = hash(r, c, 11) < 0.5;
                cellShade[cell] = (float) (1.0 - (0.18 + 0.52 * d));
            }
        }

        for (GlyphBatchState.Builder b : BATCHES) b.reserve(quads);
    }

    private static void ensureColors(boolean smog) {
        int v = HontunTheme.version();
        if (v == colorVersion && smog == colorSmog) return;
        colorVersion = v;
        colorSmog = smog;

        if (smog) {
            rainBg = 0x000000;
            trail = HontunTheme.SMOG_ACCENT;
            head = HontunTheme.SMOG_ACCENT_HI;
            return;
        }

        double[] a = rgb(HontunTheme.userAccent());
        double[] bg = mix(rgb(0x0B0C0D), a, 0.035);
        double[] acc = lift(a, bg, 5.5);
        trail = pack(acc);
        head = pack(mix(acc, WHITE, 0.72));
        rainBg = pack(mix(rgb(0x030404), a, 0.02));
    }

    private static boolean ensureAtlas(Charset cs, int cw, int ch, double k) {
        if (atlas != null && cs == atlasCharset && cw == atlasCw && ch == atlasCh) return true;
        if (atlasFailed || !loadFont()) return false;

        String glyphs = cs.glyphs();
        int n = glyphs.length();
        int width = (n + 1) * cw;
        NativeImage image = new NativeImage(width, ch, true);
        DynamicTexture texture;

        try {
            float scale = stbtt_ScaleForMappingEmToPixels(fontInfo, (float) (15 * k));
            int[] ascent = new int[1], descent = new int[1], gap = new int[1];
            stbtt_GetFontVMetrics(fontInfo, ascent, descent, gap);
            float baseline = ch / 2f + (float) k + (ascent[0] + descent[0]) / 2f * scale;

            int[] advance = new int[1], bearing = new int[1];
            int[] bx0 = new int[1], by0 = new int[1], bx1 = new int[1], by1 = new int[1];

            for (int i = 0; i < n; i++) {
                int cp = glyphs.charAt(i);
                stbtt_GetCodepointHMetrics(fontInfo, cp, advance, bearing);
                float penX = i * cw + cw / 2f - advance[0] * scale / 2f;
                int ox = (int) Math.floor(penX), oy = (int) Math.floor(baseline);
                float sx = penX - ox, sy = baseline - oy;

                stbtt_GetCodepointBitmapBoxSubpixel(fontInfo, cp, scale, scale, sx, sy, bx0, by0, bx1, by1);
                int gw = bx1[0] - bx0[0], gh = by1[0] - by0[0];
                if (gw <= 0 || gh <= 0) continue;

                ByteBuffer bitmap = MemoryUtil.memCalloc(gw * gh);
                try {
                    stbtt_MakeCodepointBitmapSubpixel(fontInfo, bitmap, gw, gh, gw, scale, scale, sx, sy, cp);
                    int left = i * cw, right = left + cw;
                    for (int py = 0; py < gh; py++) {
                        int dy = oy + by0[0] + py;
                        if (dy < 0 || dy >= ch) continue;
                        for (int px = 0; px < gw; px++) {
                            int dx = ox + bx0[0] + px;
                            if (dx < left || dx >= right) continue;
                            int cov = bitmap.get(py * gw + px) & 0xFF;
                            if (cov != 0) image.setPixel(dx, dy, (cov << 24) | 0xFFFFFF);
                        }
                    }
                } finally {
                    MemoryUtil.memFree(bitmap);
                }
            }

            image.fillRect(n * cw, 0, cw, ch, 0xFFFFFFFF);
            texture = new DynamicTexture(() -> "hontun_matrix_rain", image);
        } catch (Throwable e) {
            image.close();
            atlasFailed = true;
            LOG.warn("[Hontun] Matrix rain glyph atlas could not be built, using the particle backdrop", e);
            return false;
        }

        if (retired != null) retired.close();
        retired = atlas;
        atlas = texture;
        atlasCharset = cs;
        atlasCw = cw;
        atlasCh = ch;
        glyphCount = n;
        cellU = 1f / (n + 1);
        solidU = (n + 0.5f) / (n + 1);
        return true;
    }

    private static boolean ensureScanTexture() {
        if (scanTexture != null) return true;
        NativeImage image = new NativeImage(1, 1, true);
        try {
            image.setPixel(0, 0, 0xFFFFFFFF);
            scanTexture = new DynamicTexture(() -> "hontun_scanlines", image);
            return true;
        } catch (Throwable e) {
            image.close();
            scanlines = false;
            LOG.warn("[Hontun] Scanline texture could not be created", e);
            return false;
        }
    }

    private static boolean loadFont() {
        if (fontInfo != null) return true;
        if (fontFailed) return false;

        try (InputStream in = MatrixRain.class.getResourceAsStream(FONT)) {
            if (in == null) throw new IllegalStateException("Bundled font " + FONT + " not found on classpath");
            byte[] bytes = in.readAllBytes();
            ByteBuffer data = BufferUtils.createByteBuffer(bytes.length);
            data.put(bytes).flip();
            STBTTFontinfo info = STBTTFontinfo.create();
            if (!stbtt_InitFont(info, data)) throw new IllegalStateException("stb_truetype rejected " + FONT);
            fontData = data;
            fontInfo = info;
            return true;
        } catch (Throwable e) {
            fontFailed = true;
            LOG.warn("[Hontun] Matrix rain font could not be loaded, using the particle backdrop", e);
            return false;
        }
    }

    private static double hash(int a, int b, int c) {
        int x = (a * 0x27d4eb2d) ^ ((b + 0x3c6ef372) * 0x165667b1) ^ ((c + 0x1b873593) * 0x9e3779b1);
        x ^= x >>> 15;
        x *= 0x85ebca6b;
        x ^= x >>> 13;
        x *= 0xc2b2ae35;
        x ^= x >>> 16;
        return (x & 0xFFFFFFFFL) / 4294967296.0;
    }

    private static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    private static double[] rgb(int c) {
        return new double[] {(c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF};
    }

    private static int pack(double[] c) {
        int r = (int) Math.round(clamp(c[0], 0, 255));
        int g = (int) Math.round(clamp(c[1], 0, 255));
        int b = (int) Math.round(clamp(c[2], 0, 255));
        return (r << 16) | (g << 8) | b;
    }

    private static double[] mix(double[] a, double[] b, double t) {
        return new double[] {a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t};
    }

    private static double linear(double v) {
        v /= 255.0;
        return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    }

    private static double luminance(double[] c) {
        return 0.2126 * linear(c[0]) + 0.7152 * linear(c[1]) + 0.0722 * linear(c[2]);
    }

    private static double contrast(double[] a, double[] b) {
        double x = luminance(a), y = luminance(b);
        return (Math.max(x, y) + 0.05) / (Math.min(x, y) + 0.05);
    }

    private static double[] lift(double[] c, double[] bg, double target) {
        if (contrast(c, bg) >= target) return c;
        double[] hsl = toHsl(c);
        for (double l = hsl[2]; l <= 1.001; l += 0.01) {
            double[] x = fromHsl(hsl[0], hsl[1], Math.min(1.0, l));
            if (contrast(x, bg) >= target) return x;
        }
        return WHITE.clone();
    }

    private static double[] toHsl(double[] c) {
        double r = c[0] / 255.0, g = c[1] / 255.0, b = c[2] / 255.0;
        double mx = Math.max(r, Math.max(g, b)), mn = Math.min(r, Math.min(g, b));
        double h = 0, s = 0, l = (mx + mn) / 2;
        if (mx != mn) {
            double d = mx - mn;
            s = l > 0.5 ? d / (2 - mx - mn) : d / (mx + mn);
            if (mx == r) h = (g - b) / d + (g < b ? 6 : 0);
            else if (mx == g) h = (b - r) / d + 2;
            else h = (r - g) / d + 4;
            h /= 6;
        }
        return new double[] {h, s, l};
    }

    private static double[] fromHsl(double h, double s, double l) {
        if (s == 0) return new double[] {l * 255, l * 255, l * 255};
        double q = l < 0.5 ? l * (1 + s) : l + s - l * s;
        double p = 2 * l - q;
        return new double[] {hue(p, q, h + 1.0 / 3) * 255, hue(p, q, h) * 255, hue(p, q, h - 1.0 / 3) * 255};
    }

    private static double hue(double p, double q, double t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1.0 / 6) return p + (q - p) * 6 * t;
        if (t < 1.0 / 2) return q;
        if (t < 2.0 / 3) return p + (q - p) * (2.0 / 3 - t) * 6;
        return p;
    }
}
