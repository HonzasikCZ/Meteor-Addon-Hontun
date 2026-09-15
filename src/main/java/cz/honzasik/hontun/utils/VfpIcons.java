package cz.honzasik.hontun.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class VfpIcons {

    private static final String TABLE = "assets/viafabricplus/data/version-metadata.json";

    private static final Identifier FALLBACK = Identifier.withDefaultNamespace("textures/item/barrier.png");
    private static final Identifier AUTO_DETECT = Identifier.withDefaultNamespace("textures/item/spyglass.png");

    private static final Map<String, Identifier> ICONS = new HashMap<>();
    private static boolean loaded;
    private static boolean usable;

    private VfpIcons() {}

    private static synchronized void load() {
        if (loaded) return;
        loaded = true;
        try {
            Optional<Path> path = FabricLoader.getInstance().getModContainer("viafabricplus")
                    .flatMap(c -> c.findPath(TABLE));
            if (path.isEmpty()) return;

            try (Reader reader = Files.newBufferedReader(path.get(), StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                for (String versionName : root.keySet()) {
                    JsonElement entry = root.get(versionName);
                    if (!entry.isJsonObject()) continue;
                    JsonElement icon = entry.getAsJsonObject().get("icon");
                    if (icon == null || icon.isJsonNull()) continue;
                    ICONS.put(versionName, Identifier.withDefaultNamespace("textures/" + icon.getAsString() + ".png"));
                }
            }
            usable = !ICONS.isEmpty();
        } catch (Throwable ignored) {
            usable = false;
        }
    }

    public static boolean available() {
        load();
        return usable;
    }

    public static Identifier forVersion(String versionName, boolean autoDetect) {
        load();
        if (!usable) return null;
        if (autoDetect) return AUTO_DETECT;
        Identifier icon = ICONS.get(versionName);
        return icon != null ? icon : FALLBACK;
    }
}
