package com.mrmelon54.DraggableLists.config;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public enum EnabledMode {
    DISABLED,
    REQUIRES_MODIFIER,
    ENABLED;

    public boolean isEnabled() {
        return switch (this) {
            case DISABLED -> false;
            case REQUIRES_MODIFIER -> shiftDown();
            case ENABLED -> true;
        };
    }

    /**
     * 26.2 carries modifiers on the input event rather than on {@code Screen}, and the grip is
     * drawn on frames where there is no event to ask, so the keyboard is polled directly.
     */
    private static boolean shiftDown() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) return false;

        Window window = minecraft.getWindow();
        if (window == null) return false;

        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT)
            || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }
}
