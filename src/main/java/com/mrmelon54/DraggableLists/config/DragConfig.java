package com.mrmelon54.DraggableLists.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mrmelon54.DraggableLists.DraggableLists;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Plain JSON config at {@code config/draggable_lists.json}.
 *
 * <p>Upstream used AutoConfig + Cloth Config + Mod Menu for this. None of those are part of
 * this fork: Cloth has no 26.2 build to link against, and three libraries to hold nine
 * booleans is a heavy trade for a mod whose whole job is to make one gesture feel right.
 * The file is re-read every time one of the affected screens opens, so editing it while the
 * game is running is enough - no restart, no GUI.
 */
public class DragConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    // --- what can be dragged -------------------------------------------------------

    public EnabledMode serverDragging = EnabledMode.ENABLED;
    public EnabledMode resourcePackDragging = EnabledMode.ENABLED;

    /** Hide vanilla's (and Hontun's) up/down arrows on server rows. */
    public boolean hideServerArrows = true;
    /** Hide vanilla's up/down arrows on resource pack rows. */
    public boolean hideResourcePackArrows = true;

    // --- how the gesture looks -----------------------------------------------------

    /** Draw the six-dot grip where the up/down arrows used to be. */
    public boolean grabHandle = true;
    /** Cursor shown while carrying a row. */
    public CursorStyle cursor = CursorStyle.HAND;
    /** Ease rows into their new slots instead of snapping them. */
    public boolean animations = true;
    /** Multiplier on every animation in the drag; 2.0 is twice as fast. */
    public float animationSpeed = 1.0f;
    /** Soft shadow under the carried row. */
    public boolean shadow = true;
    /** Lift the carried row: a small scale-up and tilt around the grab point. */
    public boolean lift = true;
    /** The accent line showing where the row will land. */
    public boolean dropIndicator = true;
    /** Briefly outline a row after it settles into its new position. */
    public boolean settleFlash = true;
    /** Scroll the list when the carried row is held near its top or bottom edge. */
    public boolean autoScroll = true;

    // --- colours -------------------------------------------------------------------

    public ThemeMode theme = ThemeMode.AUTO;

    // --- persistence ---------------------------------------------------------------

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve(DraggableLists.MOD_ID + ".json");
    }

    public static DragConfig load() {
        Path path = path();
        if (!Files.isRegularFile(path)) {
            DragConfig fresh = new DragConfig();
            fresh.save();
            return fresh;
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            DragConfig loaded = GSON.fromJson(reader, DragConfig.class);
            if (loaded == null) return new DragConfig();
            loaded.sanitise();
            return loaded;
        } catch (Exception e) {
            DraggableLists.LOGGER.warn("Could not read {}, using defaults.", path, e);
            return new DragConfig();
        }
    }

    public void save() {
        Path path = path();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            DraggableLists.LOGGER.warn("Could not write {}.", path, e);
        }
    }

    /** Replaces anything the file left out or set to nonsense. */
    private void sanitise() {
        if (serverDragging == null) serverDragging = EnabledMode.ENABLED;
        if (resourcePackDragging == null) resourcePackDragging = EnabledMode.ENABLED;
        if (cursor == null) cursor = CursorStyle.HAND;
        if (theme == null) theme = ThemeMode.AUTO;
        if (!Float.isFinite(animationSpeed) || animationSpeed <= 0f) animationSpeed = 1.0f;
        animationSpeed = Math.clamp(animationSpeed, 0.1f, 5.0f);
    }
}
