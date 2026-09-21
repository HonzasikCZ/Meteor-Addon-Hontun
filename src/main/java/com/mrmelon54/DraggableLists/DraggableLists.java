package com.mrmelon54.DraggableLists;

import com.mrmelon54.DraggableLists.config.DragConfig;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DraggableLists implements ClientModInitializer {
    public static final String MOD_ID = "draggable_lists";
    public static final Logger LOGGER = LoggerFactory.getLogger("DraggableLists");

    /** Shortest gap between two re-reads of the config file. */
    private static final long RELOAD_COOLDOWN_MILLIS = 500L;

    private static volatile DragConfig config = new DragConfig();
    private static long lastLoad;

    @Override
    public void onInitializeClient() {
        reloadConfig();
    }

    public static DragConfig config() {
        return config;
    }

    public static void reloadConfig() {
        config = DragConfig.load();
        lastLoad = System.currentTimeMillis();
    }

    /**
     * Re-reads the config when one of the draggable lists is built, so hand-edits to the JSON
     * take effect without a restart. Throttled because a list is also rebuilt on every window
     * resize, and the pack screen builds two of them.
     */
    public static void reloadConfigIfStale() {
        long now = System.currentTimeMillis();
        if (now - lastLoad < RELOAD_COOLDOWN_MILLIS) return;
        reloadConfig();
    }
}
