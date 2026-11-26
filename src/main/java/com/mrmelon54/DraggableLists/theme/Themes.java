package com.mrmelon54.DraggableLists.theme;

import com.mrmelon54.DraggableLists.DraggableLists;
import com.mrmelon54.DraggableLists.config.ThemeMode;

public final class Themes {
    private Themes() {
    }

    public static Palette current() {
        ThemeMode mode = DraggableLists.config().theme;
        if (mode == ThemeMode.BUILTIN) return Palette.BUILTIN;

        if (mode == ThemeMode.AUTO) {
            if (!HontunBridge.available()) return Palette.BUILTIN;
            if (!HontunBridge.restyling()) return Palette.VANILLA;
        }

        Palette hontun = HontunBridge.palette();
        return hontun != null ? hontun : Palette.BUILTIN;
    }

    public static boolean hontunStyled() {
        return DraggableLists.config().theme != ThemeMode.BUILTIN && HontunBridge.restyling();
    }
}
