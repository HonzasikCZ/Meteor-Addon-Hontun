package com.mrmelon54.DraggableLists.theme;

import com.mrmelon54.DraggableLists.DraggableLists;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Reads the live palette out of Hontun's {@code HontunTheme} without compiling against it.
 *
 * <p>Hontun is a Meteor Client addon, so linking against it would drag Meteor (and a
 * matching Meteor snapshot) into this mod's build for what is, in the end, a handful of
 * ints. {@code HontunTheme} is a static, plain-int holder that already exists purely so
 * other code can ask it for colours, so a small bundle of {@link MethodHandle}s over it is
 * both cheap and stable. Everything is resolved once; if any single accessor is missing the
 * whole bridge switches itself off and the built-in palette is used instead.
 *
 * <p>Hontun bumps {@code version()} whenever the user changes the flavour, accent or UI
 * mode, so the palette is only rebuilt when that number actually moves.
 */
public final class HontunBridge {
    private static final String MOD_ID = "hontun";
    private static final String THEME_CLASS = "cz.honzasik.hontun.utils.HontunTheme";

    private static boolean resolved;
    private static boolean available;

    private static MethodHandle accent, accentHi, accentLo;
    private static MethodHandle surface0, surface1, crust, overlay0;
    private static MethodHandle textLight, textDim;
    private static MethodHandle restyleEnabled, modern1, modern2, version;

    private static int cachedVersion = Integer.MIN_VALUE;
    private static Palette cached;

    private HontunBridge() {
    }

    /** True when Hontun is installed and every colour accessor was found. */
    public static boolean available() {
        resolve();
        return available;
    }

    /** True when Hontun is installed and the user has not put it back into Vanilla mode. */
    public static boolean restyling() {
        if (!available()) return false;
        try {
            return (boolean) restyleEnabled.invokeExact();
        } catch (Throwable t) {
            disable(t);
            return false;
        }
    }

    /** The current Hontun palette, or {@code null} if Hontun is unavailable. */
    public static Palette palette() {
        if (!available()) return null;
        try {
            int v = (int) version.invokeExact();
            if (cached != null && v == cachedVersion) return cached;

            // Each Hontun UI mode gets the corner treatment that matches how it draws its own
            // panels and server cards: HModern2 cuts its corners (chamfer), HModern1 is softly
            // rounded, and HVanilla keeps Minecraft's sharp squares. Vanilla never reaches here
            // (restyling() is false, so Themes falls back to the built-in palette).
            CornerStyle corners;
            int cornerSize;
            if ((boolean) modern2.invokeExact()) {
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
