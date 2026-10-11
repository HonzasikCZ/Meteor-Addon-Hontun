package cz.honzasik.hontun.gui.cursor;

import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.gui.widget.HontunRound;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

public final class HontunCrosshair {
    private HontunCrosshair() {}

    public static boolean enabled() {
        return Hontun.THEME != null && Hontun.THEME.customCrosshair.get() && HontunTheme.restyleEnabled();
    }

    public static void draw(GuiGraphicsExtractor g) {
        Minecraft mc = Minecraft.getInstance();
        int s = Math.max(1, (int) Math.round(mc.getWindow().getGuiScale()));
        int fw = mc.getWindow().getWidth(), fh = mc.getWindow().getHeight();
        int cx = fw / 2, cy = fh / 2;

        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.scale(1f / s, 1f / s);
        if (HontunTheme.smog()) smog(g, cx, cy, s);
        else if (HontunTheme.modern2()) chamfer(g, cx, cy, s);
        else if (HontunTheme.modern1()) flat(g, cx, cy, s);
        else pixel(g, cx, cy, s);
        pose.popMatrix();

        float gx = cx / (float) s, gy = cy / (float) s;
        if (HontunTheme.smog()) {
            HontunRound.dot(g, gx, gy, 1.6f, HontunTheme.argb(0x90, 0x000000));
            HontunRound.dot(g, gx, gy, 1.1f, HontunTheme.argb(0xFF, 0xFFFFFF));
        } else if (HontunTheme.modern1()) {
            HontunRound.dot(g, gx, gy, 1.5f, HontunTheme.argb(0x80, 0x000000));
            HontunRound.dot(g, gx, gy, 1.0f, HontunTheme.argb(0xFF, HontunTheme.accentHi()));
        }
    }

    private static void box(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int argb) {
        g.fill(Math.min(x0, x1), Math.min(y0, y1), Math.max(x0, x1), Math.max(y0, y1), argb);
    }

    private static void outlined(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int o, int fill, int edge) {
        box(g, x0 - o, y0 - o, x1 + o, y1 + o, edge);
        box(g, x0, y0, x1, y1, fill);
    }

    private static void pixel(GuiGraphicsExtractor g, int cx, int cy, int s) {
        int x = cx - s / 2, y = cy - s / 2;
        int arm = 4 * s;
        int o = Math.max(1, s / 2);
        int edge = HontunTheme.argb(0xC0, 0x000000);
        int fill = HontunTheme.argb(0xFF, HontunTheme.accent());
        box(g, x - arm - o, y - o, x + s + arm + o, y + s + o, edge);
        box(g, x - o, y - arm - o, x + s + o, y + s + arm + o, edge);
        box(g, x - arm, y, x + s + arm, y + s, fill);
        box(g, x, y - arm, x + s, y + s + arm, fill);
        box(g, x, y, x + s, y + s, HontunTheme.argb(0xFF, HontunTheme.accentHi()));
    }

    private static void flat(GuiGraphicsExtractor g, int cx, int cy, int s) {
        int t = s, half = t / 2, gap = 2 * s + s / 2, len = 3 * s;
        int fill = HontunTheme.argb(0xF0, HontunTheme.accentHi());
        int edge = HontunTheme.argb(0x80, 0x000000);
        int o = Math.max(1, s / 2);
        outlined(g, cx - half, cy - gap - len, cx - half + t, cy - gap, o, fill, edge);
        outlined(g, cx - half, cy + gap, cx - half + t, cy + gap + len, o, fill, edge);
        outlined(g, cx - gap - len, cy - half, cx - gap, cy - half + t, o, fill, edge);
        outlined(g, cx + gap, cy - half, cx + gap + len, cy - half + t, o, fill, edge);
    }

    private static void chamfer(GuiGraphicsExtractor g, int cx, int cy, int s) {
        int t = s, d = 4 * s, len = 2 * s + s / 2;
        int fill = HontunTheme.argb(0xFF, HontunTheme.accent());
        int edge = HontunTheme.argb(0x90, 0x000000);
        int o = Math.max(1, s / 2);
        int[][] rects = new int[8][];
        int n = 0;
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sy = -1; sy <= 1; sy += 2) {
                int px = cx + sx * d, py = cy + sy * d;
                int hx0 = sx < 0 ? px : px - len, hy0 = sy < 0 ? py : py - t;
                int vx0 = sx < 0 ? px : px - t, vy0 = sy < 0 ? py : py - len;
                rects[n++] = new int[]{hx0, hy0, hx0 + len, hy0 + t};
                rects[n++] = new int[]{vx0, vy0, vx0 + t, vy0 + len};
            }
        }
        for (int[] r : rects) box(g, r[0] - o, r[1] - o, r[2] + o, r[3] + o, edge);
        for (int[] r : rects) box(g, r[0], r[1], r[2], r[3], fill);
        int half = t / 2;
        outlined(g, cx - half, cy - half, cx - half + t, cy - half + t, o,
                HontunTheme.argb(0xFF, HontunTheme.accentHi()), edge);
    }

    private static void smog(GuiGraphicsExtractor g, int cx, int cy, int s) {
        int t = Math.max(1, s / 2 + (s % 2)), half = t / 2, gap = 3 * s, len = 3 * s;
        int fill = HontunTheme.argb(0xFF, 0xFFFFFF);
        int edge = HontunTheme.argb(0x78, 0x000000);
        int o = 1;
        outlined(g, cx - half, cy - gap - len, cx - half + t, cy - gap, o, fill, edge);
        outlined(g, cx - half, cy + gap, cx - half + t, cy + gap + len, o, fill, edge);
        outlined(g, cx - gap - len, cy - half, cx - gap, cy - half + t, o, fill, edge);
        outlined(g, cx + gap, cy - half, cx + gap + len, cy - half + t, o, fill, edge);
    }
}
