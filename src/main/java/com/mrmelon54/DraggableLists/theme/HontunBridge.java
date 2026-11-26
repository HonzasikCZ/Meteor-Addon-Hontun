package com.mrmelon54.DraggableLists.theme;

import com.mrmelon54.DraggableLists.DraggableLists;
import cz.honzasik.hontun.gui.theme.style.terminal.TerminalPalette;
import cz.honzasik.hontun.gui.widget.TerminalWidgets;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class HontunBridge {
    private static final String MOD_ID = "hontun";
    private static final String THEME_CLASS = "cz.honzasik.hontun.utils.HontunTheme";

    private static boolean resolved;
    private static boolean available;

    private static MethodHandle accent, accentHi, accentLo;
    private static MethodHandle surface0, surface1, crust, overlay0;
    private static MethodHandle textLight, textDim;
    private static MethodHandle restyleEnabled, modern1, modern2, smog, terminal, version;

    private static int cachedVersion = Integer.MIN_VALUE;
    private static Palette cached;

    private HontunBridge() {
    }

    public static boolean available() {
        resolve();
        return available;
    }

    public static boolean restyling() {
        if (!available()) return false;
        try {
            return (boolean) restyleEnabled.invokeExact();
        } catch (Throwable t) {
            disable(t);
            return false;
        }
    }

    public static boolean smog() {
        if (!available()) return false;
        try {
            return (boolean) smog.invokeExact();
        } catch (Throwable t) {
            disable(t);
            return false;
        }
    }

    public static boolean terminal() {
        if (!available()) return false;
        try {
            return (boolean) terminal.invokeExact();
        } catch (Throwable t) {
            disable(t);
            return false;
        }
    }

    public static Palette palette() {
        if (!available()) return null;
        try {
            int v = (int) version.invokeExact();
            if (cached != null && v == cachedVersion) return cached;

            if ((boolean) terminal.invokeExact()) {
                cached = new Palette(
                    TerminalWidgets.acc() & 0xFFFFFF,
                    TerminalWidgets.acc() & 0xFFFFFF,
                    TerminalWidgets.fill() & 0xFFFFFF,
                    TerminalWidgets.bg() & 0xFFFFFF,
                    TerminalPalette.bar,
                    TerminalPalette.bar,
                    TerminalWidgets.line() & 0xFFFFFF,
                    TerminalWidgets.fg() & 0xFFFFFF,
                    TerminalWidgets.dim() & 0xFFFFFF,
                    CornerStyle.SQUARE,
                    0
                );
                cachedVersion = v;
                return cached;
            }
            CornerStyle corners;
            int cornerSize;
            if ((boolean) smog.invokeExact()) {
                corners = CornerStyle.ROUND;
                cornerSize = 4;
            } else if ((boolean) modern2.invokeExact()) {
                corners = CornerStyle.CHAMFER;
                cornerSize = 5;
            } else if ((boolean) modern1.invokeExact()) {
                corners = CornerStyle.ROUND;
                cornerSize = 3;
            } else {
                corners = CornerStyle.SQUARE;
                cornerSize = 0;
            }
            cached = new Palette(
                (int) accent.invokeExact(),
                (int) accentHi.invokeExact(),
                (int) accentLo.invokeExact(),
                (int) surface0.invokeExact(),
                (int) surface1.invokeExact(),
                (int) crust.invokeExact(),
                (int) overlay0.invokeExact(),
                (int) textLight.invokeExact(),
                (int) textDim.invokeExact(),
                corners,
                cornerSize
            );
            cachedVersion = v;
            return cached;
        } catch (Throwable t) {
            disable(t);
            return null;
        }
    }

    private static void resolve() {
        if (resolved) return;
        resolved = true;

        if (!FabricLoader.getInstance().isModLoaded(MOD_ID)) return;

        try {
            Class<?> theme = Class.forName(THEME_CLASS);
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            MethodType intGetter = MethodType.methodType(int.class);
            MethodType boolGetter = MethodType.methodType(boolean.class);

            accent = lookup.findStatic(theme, "accent", intGetter);
            accentHi = lookup.findStatic(theme, "accentHi", intGetter);
            accentLo = lookup.findStatic(theme, "accentLo", intGetter);
            surface0 = lookup.findStatic(theme, "surface0", intGetter);
            surface1 = lookup.findStatic(theme, "surface1", intGetter);
            crust = lookup.findStatic(theme, "crust", intGetter);
            overlay0 = lookup.findStatic(theme, "overlay0", intGetter);
            textLight = lookup.findStatic(theme, "textLight", intGetter);
            textDim = lookup.findStatic(theme, "textDim", intGetter);
            restyleEnabled = lookup.findStatic(theme, "restyleEnabled", boolGetter);
            modern1 = lookup.findStatic(theme, "modern1", boolGetter);
            modern2 = lookup.findStatic(theme, "modern2", boolGetter);
            smog = lookup.findStatic(theme, "smog", boolGetter);
            terminal = lookup.findStatic(theme, "terminal", boolGetter);
            version = lookup.findStatic(theme, "version", intGetter);

            available = true;
            DraggableLists.LOGGER.info("Hontun detected - drag visuals will follow its theme.");
        } catch (Throwable t) {
            DraggableLists.LOGGER.warn("Hontun is installed but its theme could not be read; using the built-in palette.", t);
        }
    }

    private static void disable(Throwable t) {
        if (!available) return;
        available = false;
        cached = null;
        DraggableLists.LOGGER.warn("Reading Hontun's theme failed; falling back to the built-in palette.", t);
    }
}
