package com.mrmelon54.DraggableLists.config;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import org.jspecify.annotations.Nullable;

/**
 * Which OS cursor is shown while a row is being dragged.
 *
 * <p>Upstream always switched to the vertical resize cursor (the white double headed
 * arrow), which is the cursor for "resize this thing", not for "carry this thing", and it
 * looks out of place on top of the lifted row. {@link #NONE} and {@link #HAND} are the two
 * that read as dragging; {@link #RESIZE_NS} is kept for parity with upstream.
 */
public enum CursorStyle {
    NONE,
    HAND,
    MOVE,
    RESIZE_NS;

    /**
     * The cursor to request, or {@code null} for "leave it alone".
     *
     * <p>Resolved here and not held in a field, and that is load-bearing. Every constant in
     * {@link CursorTypes} is built by {@code glfwCreateStandardCursor}, so merely touching
     * that class runs eight GLFW calls. A field would pull it in when this enum is first
     * class-loaded, which is while the config is being read - inside {@code Minecraft.<init>},
     * on the render thread, before {@code GLX._initGlfw} has run. GLFW answers those calls
     * with {@code GLFW_NOT_INITIALIZED} and queues the error on the thread; {@code _initGlfw}
     * then opens with {@code glfwGetError} and turns whatever it finds into
     * "GLFW error before init", crashing the game on startup with a stack trace that points
     * at Minecraft and names no mod. Keep this lazy: it is only ever called while drawing,
     * by which time the window exists.
     */
    public @Nullable CursorType cursor() {
        return switch (this) {
            case NONE -> null;
            case HAND -> CursorTypes.POINTING_HAND;
            case MOVE -> CursorTypes.RESIZE_ALL;
            case RESIZE_NS -> CursorTypes.RESIZE_NS;
        };
    }
}
