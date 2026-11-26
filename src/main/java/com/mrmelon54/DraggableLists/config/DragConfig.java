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

public class DragConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    public EnabledMode serverDragging = EnabledMode.ENABLED;
    public EnabledMode resourcePackDragging = EnabledMode.ENABLED;

    public boolean hideServerArrows = true;
    public boolean hideResourcePackArrows = true;

    public boolean grabHandle = true;
    public CursorStyle cursor = CursorStyle.HAND;
    public boolean animations = true;
    public float animationSpeed = 1.0f;
    public boolean shadow = true;
    public boolean lift = true;
    public boolean dropIndicator = true;
    public boolean settleFlash = true;
    public boolean autoScroll = true;

    public ThemeMode theme = ThemeMode.AUTO;

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

    private void sanitise() {
        if (serverDragging == null) serverDragging = EnabledMode.ENABLED;
        if (resourcePackDragging == null) resourcePackDragging = EnabledMode.ENABLED;
        if (cursor == null) cursor = CursorStyle.HAND;
        if (theme == null) theme = ThemeMode.AUTO;
        if (!Float.isFinite(animationSpeed) || animationSpeed <= 0f) animationSpeed = 1.0f;
        animationSpeed = Math.clamp(animationSpeed, 0.1f, 5.0f);
    }
}
