package cz.honzasik.hontun.gui.cursor;

import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryUtil;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class HontunCursors {
    private static final Map<CursorArt.Kind, Long> HANDLES = new EnumMap<>(CursorArt.Kind.class);
    private static final List<Long> RETIRED = new ArrayList<>();
    private static long builtKey = Long.MIN_VALUE;

    private HontunCursors() {}

    public static boolean enabled() {
        return Hontun.THEME != null && Hontun.THEME.customCursor.get() && HontunTheme.restyleEnabled();
    }

    private static long key() {
        int scale = Math.max(1, (int) Math.round(Minecraft.getInstance().getWindow().getGuiScale()));
        return ((long) HontunTheme.version() << 16) ^ ((long) HontunTheme.mode().ordinal() << 8) ^ scale;
    }

    public static boolean stale() {
        return enabled() ? key() != builtKey : !HANDLES.isEmpty();
    }

    public static long handle(String name) {
        long k = key();
        if (k != builtKey) {
            RETIRED.addAll(HANDLES.values());
            HANDLES.clear();
            builtKey = k;
        }
        CursorArt.Kind kind = CursorArt.kindOf(name);
        Long h = HANDLES.get(kind);
        if (h == null) {
            h = create(kind, (int) (k & 0xFF));
            HANDLES.put(kind, h);
        }
        return h;
    }

    public static void release() {
        RETIRED.addAll(HANDLES.values());
        HANDLES.clear();
        builtKey = Long.MIN_VALUE;
    }

    public static void destroyRetired() {
        for (long h : RETIRED) {
            if (h != 0L) GLFW.glfwDestroyCursor(h);
        }
        RETIRED.clear();
    }

    private static long create(CursorArt.Kind kind, int scale) {
        try {
            CursorArt.Art art = CursorArt.draw(kind, scale);
            BufferedImage img = art.image();
            int w = img.getWidth(), h = img.getHeight();
            ByteBuffer buf = MemoryUtil.memAlloc(w * h * 4);
            try {
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        int argb = img.getRGB(x, y);
                        buf.put((byte) (argb >> 16)).put((byte) (argb >> 8)).put((byte) argb).put((byte) (argb >>> 24));
                    }
                }
                buf.flip();
                try (GLFWImage glfw = GLFWImage.malloc()) {
                    glfw.set(w, h, buf);
                    return GLFW.glfwCreateCursor(glfw, art.hotX(), art.hotY());
                }
            } finally {
                MemoryUtil.memFree(buf);
            }
        } catch (Throwable t) {
            Hontun.LOG.warn("[Hontun] custom cursor {} failed: {}", kind, t.toString());
            return 0L;
        }
    }
}
