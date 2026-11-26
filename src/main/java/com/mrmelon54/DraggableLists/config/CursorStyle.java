package com.mrmelon54.DraggableLists.config;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import org.jspecify.annotations.Nullable;

public enum CursorStyle {
    NONE,
    HAND,
    MOVE,
    RESIZE_NS;

    public @Nullable CursorType cursor() {
        return switch (this) {
            case NONE -> null;
            case HAND -> CursorTypes.POINTING_HAND;
            case MOVE -> CursorTypes.RESIZE_ALL;
            case RESIZE_NS -> CursorTypes.RESIZE_NS;
        };
    }
}
