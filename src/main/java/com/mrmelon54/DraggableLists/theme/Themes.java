package com.mrmelon54.DraggableLists.theme;

import com.mrmelon54.DraggableLists.DraggableLists;
import com.mrmelon54.DraggableLists.config.ThemeMode;

public final class Themes {
    private Themes() {
    }

    /** The palette to draw with right now. */
    public static Palette current() {
        ThemeMode mode = DraggableLists.config().theme;
        if (mode == ThemeMode.BUILTIN) return Palette.BUILTIN;

        if (mode == ThemeMode.AUTO) {
            // No Hontun installed at all: the built-in accent is the intended look.
            if (!HontunBridge.available()) return Palette.BUILTIN;
            // Hontun is installed but its restyle is off (its "Vanilla" UI mode). Its colours
            // are not on screen anywhere else, so the drag must not be the one thing still
            // painting an accent - it uses the plain Vanilla palette, distinct from the
            // styled modes, rather than the built-in blue.
            if (!HontunBridge.restyling()) return Palette.VANILLA;
        }

        Palette hontun = HontunBridge.palette();
        return hontun != null ? hontun : Palette.BUILTIN;
    }

    /** True when Hontun is the one drawing the rows underneath our overlay. */
    public static boolean hontunStyled() {
        return DraggableLists.config().theme != ThemeMode.BUILTIN && HontunBridge.restyling();
    }
}
