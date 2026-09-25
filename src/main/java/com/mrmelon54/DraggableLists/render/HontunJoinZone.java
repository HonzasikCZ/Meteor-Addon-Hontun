package com.mrmelon54.DraggableLists.render;

import com.mrmelon54.DraggableLists.theme.HontunBridge;
import com.mrmelon54.DraggableLists.theme.Palette;
import cz.honzasik.hontun.gui.render.RoundedGui;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Redraws the join button that Hontun paints over a server row's icon.
 *
 * <p>Hontun draws the join, move-up and move-down buttons in one call, so hiding its arrows
 * means taking over that call - and then putting the join button back, because it is the
 * only way to start a server from the row without double-clicking it. The geometry matches
 * Hontun's: a 16x32 zone on the right half of the icon with a filled triangle in it.
 */
public final class HontunJoinZone {
    private HontunJoinZone() {
    }

    public static void draw(GuiGraphicsExtractor graphics, int contentX, int contentY,
                            int mouseX, int mouseY, Palette palette) {
        int x = contentX + 16;
        boolean hot = mouseX >= x && mouseX < x + 16 && mouseY >= contentY && mouseY < contentY + 32;

        if (HontunBridge.smog() && RoundedGui.available()) {
            RoundedGui.fill(graphics, x + 1f, contentY + 1f, 14f, 30f, 4f, 0f,
                Palette.argb(hot ? 0xC0 : 0x7A, 0x000000));
            float cx = x + 8f, cy = contentY + 16f;
            int col = Palette.argb(0xFF, hot ? palette.accentHi() : palette.accent());
            RoundedGui.capsule(graphics, cx - 1.9f, cy - 4.2f, cx + 2.1f, cy, 0.85f, col);
            RoundedGui.capsule(graphics, cx + 2.1f, cy, cx - 1.9f, cy + 4.2f, 0.85f, col);
            return;
        }

        Shapes.fill(graphics, x, contentY, 16, 32, palette.corners(), 2,
            Palette.argb(hot ? 0xE8 : 0xB8, hot ? palette.surfaceHi() : palette.crust()));
        if (hot) {
            Shapes.outline(graphics, x, contentY, 16, 32, palette.corners(), 2,
                Palette.argb(0xFF, palette.accentHi()));
        }

        arrowRight(graphics, contentX + 19, contentY + 16, 6,
            Palette.argb(0xFF, hot ? palette.accentHi() : palette.accent()));
    }

    private static void arrowRight(GuiGraphicsExtractor graphics, int x, int centreY, int size, int argb) {
        for (int i = 0; i < size; i++) {
            int half = size - 1 - i;
            graphics.fill(x + i, centreY - half, x + i + 1, centreY + half + 1, argb);
        }
    }
}
