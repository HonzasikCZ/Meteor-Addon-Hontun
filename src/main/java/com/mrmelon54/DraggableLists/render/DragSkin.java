package com.mrmelon54.DraggableLists.render;

import com.mrmelon54.DraggableLists.theme.Palette;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * The individual pieces of the drag: the grip, the hole a row leaves behind, the lifted
 * card's shadow and edge, and the flash that confirms where it landed.
 *
 * <p>Nothing here decides when to draw - that is {@link com.mrmelon54.DraggableLists.drag.DragManager}'s
 * job - and nothing here touches list state, so each piece can be toggled off on its own from
 * the config without the rest noticing.
 */
public final class DragSkin {
    /** Width of the column the grip lives in; the same 16 pixels vanilla gave each arrow. */
    public static final int HANDLE_WIDTH = 16;

    private static final int DOT = 2;
    private static final int DOT_GAP = 2;
    private static final int DOT_COLUMNS = 2;
    private static final int DOT_ROWS = 3;

    private DragSkin() {
    }

    /**
     * The six-dot grip. This is the affordance that replaces the two arrow buttons: arrows
     * say "click me once per position", a grip says "pick this up", which is what the row now
     * actually does.
     *
     * @param hot true when the pointer is over the grip itself
     */
    public static void handle(GuiGraphicsExtractor g, int x, int contentY, int contentHeight,
                              boolean hot, float fade, Palette p) {
        if (fade <= 0.01f) return;

        int alpha = (int) (255 * fade);
        int gridWidth = DOT_COLUMNS * DOT + (DOT_COLUMNS - 1) * DOT_GAP;
        int gridHeight = DOT_ROWS * DOT + (DOT_ROWS - 1) * DOT_GAP;
        int gx = x + (HANDLE_WIDTH - gridWidth) / 2;
        int gy = contentY + (contentHeight - gridHeight) / 2;

        if (hot) {
            int padX = 4;
            int padY = 5;
            Shapes.fill(g, gx - padX, gy - padY, gridWidth + padX * 2, gridHeight + padY * 2,
                p.corners(), 3, Palette.argb(scale(0xB0, fade), p.surfaceHi()));
            Shapes.outline(g, gx - padX, gy - padY, gridWidth + padX * 2, gridHeight + padY * 2,
                p.corners(), 3, Palette.argb(scale(0xD0, fade), p.accent()));
        }

        int dotColor = Palette.argb(hot ? alpha : scale(0xC0, fade), hot ? p.accentHi() : p.textDim());
        for (int col = 0; col < DOT_COLUMNS; col++) {
            for (int row = 0; row < DOT_ROWS; row++) {
                int dx = gx + col * (DOT + DOT_GAP);
                int dy = gy + row * (DOT + DOT_GAP);
                g.fill(dx, dy, dx + DOT, dy + DOT, dotColor);
            }
        }
    }

    /**
     * The slot the carried row will drop into. The rows around it have already moved apart to
     * make the space, so this fills that space with a solid placeholder in the deepest
     * background colour. It used to be a translucent hole, but on the busy menu/server-list
     * background that see-through middle read as a gap in the drop target, so it is opaque now.
     */
    public static void gap(GuiGraphicsExtractor g, int x, int y, int width, int height, Palette p) {
        if (width <= 0 || height <= 4) return;

        int inset = 1;
        int gx = x + inset;
        int gy = y + inset;
        int gw = width - inset * 2;
        int gh = height - inset * 2;
        if (gw <= 0 || gh <= 0) return;

        // Fully opaque, and in the row's own surface colour rather than the near-black crust:
        // crust blended into the dark menu background so only the outline showed, which read as a
        // hollow hole / pane of glass. Surface is a visible mid-tone, so the slot looks like a
        // solid "the row lands here" placeholder.
        Shapes.fill(g, gx, gy, gw, gh, p.corners(), p.cornerSize(), Palette.argb(0xFF, p.surface()));
        Shapes.outline(g, gx, gy, gw, gh, p.corners(), p.cornerSize(), Palette.argb(0xFF, p.accent()));
    }

    /** Soft shadow under the lifted row. Drawn before the row's own content. */
    public static void cardShadow(GuiGraphicsExtractor g, int x, int y, int width, int height, Palette p) {
        Shapes.shadow(g, x, y, width, height, p.corners(), p.cornerSize(), 4, 0x78, 2);
    }

    /** Backing fill for the lifted row, so the content is not sitting on the rows below it. */
    public static void cardFill(GuiGraphicsExtractor g, int x, int y, int width, int height, Palette p) {
        Shapes.fill(g, x, y, width, height, p.corners(), p.cornerSize(), Palette.argb(0xFF, p.surfaceHi()));
    }

    /** Accent edge and outward glow for the lifted row. Drawn after the row's own content. */
    public static void cardEdge(GuiGraphicsExtractor g, int x, int y, int width, int height, Palette p) {
        Shapes.glow(g, x, y, width, height, p.corners(), p.cornerSize(), 3, 0x60, p.accent());
        Shapes.outline(g, x, y, width, height, p.corners(), p.cornerSize(), Palette.argb(0xFF, p.accentHi()));
    }

    /**
     * Fading outline on the row that just moved. A reorder replaces every entry object in the
     * list, so without this the only feedback that the drop worked is the rows silently being
     * in a different order.
     *
     * @param progress 0 at the moment of the drop, 1 when the flash is over
     */
    public static void flash(GuiGraphicsExtractor g, int x, int y, int width, int height,
                             float progress, Palette p) {
        float strength = 1f - progress;
        if (strength <= 0f) return;

        int alpha = (int) (0xE0 * strength * strength);
        if (alpha <= 2) return;

        Shapes.outline(g, x, y, width, height, p.corners(), p.cornerSize(),
            Palette.argb(alpha, p.accentHi()));
        Shapes.glow(g, x, y, width, height, p.corners(), p.cornerSize(), 2,
            (int) (0x50 * strength), p.accent());
    }

    private static int scale(int alpha, float fade) {
        return Math.clamp((int) (alpha * fade), 0, 255);
    }
}
