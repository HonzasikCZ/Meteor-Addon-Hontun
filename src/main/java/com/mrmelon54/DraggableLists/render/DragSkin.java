package com.mrmelon54.DraggableLists.render;

import com.mrmelon54.DraggableLists.theme.HontunBridge;
import com.mrmelon54.DraggableLists.theme.Palette;
import cz.honzasik.hontun.gui.render.RoundedGui;
import cz.honzasik.hontun.gui.widget.TerminalWidgets;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class DragSkin {
    public static final int HANDLE_WIDTH = 16;

    private static final int DOT = 2;
    private static final int DOT_GAP = 2;
    private static final int DOT_COLUMNS = 2;
    private static final int DOT_ROWS = 3;

    private DragSkin() {
    }

    public static void handle(GuiGraphicsExtractor g, int x, int contentY, int contentHeight,
                              boolean hot, float fade, Palette p) {
        if (fade <= 0.01f) return;

        if (HontunBridge.terminal()) {
            int gw = DOT_COLUMNS * DOT + (DOT_COLUMNS - 1) * DOT_GAP;
            int gh = DOT_ROWS * DOT + (DOT_ROWS - 1) * DOT_GAP;
            int tx = x + (HANDLE_WIDTH - gw) / 2;
            int ty = contentY + (contentHeight - gh) / 2;
            TerminalWidgets.Px px = new TerminalWidgets.Px();
            if (hot) {
                int bx = tx - 4, by = ty - 5, bw = gw + 8, bh = gh + 10;
                int hl = TerminalWidgets.hl();
                px.rect(bx, by, bw, bh, Palette.argb(scale((hl >>> 24) & 0xFF, fade), hl));
                px.frame(bx, by, bw, bh, Palette.argb(scale(0xFF, fade), TerminalWidgets.lineF()));
            }
            int dot = Palette.argb(scale(0xFF, fade), hot ? TerminalWidgets.acc() : TerminalWidgets.dim());
            for (int col = 0; col < DOT_COLUMNS; col++) {
                for (int row = 0; row < DOT_ROWS; row++) {
                    px.rect(tx + col * (DOT + DOT_GAP), ty + row * (DOT + DOT_GAP), DOT, DOT, dot);
                }
            }
            px.submit(g);
            return;
        }

        if (HontunBridge.smog() && RoundedGui.available()) {
            float plateH = Math.min(30f, contentHeight - 2f);
            float plateY = contentY + (contentHeight - plateH) / 2f;
            RoundedGui.fill(g, x + 1f, plateY, 14f, plateH, 4f, 0f,
                Palette.argb(scale(hot ? 0xC0 : 0x7A, fade), 0x000000));
            float cx = x + HANDLE_WIDTH / 2f;
            float cy = contentY + contentHeight / 2f;
            int dot = Palette.argb(scale(0xFF, fade), hot ? p.accentHi() : p.accent());
            for (int col = 0; col < DOT_COLUMNS; col++) {
                for (int row = 0; row < DOT_ROWS; row++) {
                    RoundedGui.circle(g, cx + (col - 0.5f) * 4.5f, cy + (row - 1) * 4.5f, 1.3f, 0f, dot);
                }
            }
            return;
        }

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

    public static void gap(GuiGraphicsExtractor g, int x, int y, int width, int height, Palette p) {
        if (width <= 0 || height <= 4) return;

        int inset = 1;
        int gx = x + inset;
        int gy = y + inset;
        int gw = width - inset * 2;
        int gh = height - inset * 2;
        if (gw <= 0 || gh <= 0) return;

        if (HontunBridge.terminal()) {
            new TerminalWidgets.Px().rect(gx, gy, gw, gh, TerminalWidgets.bg())
                .frame(gx, gy, gw, gh, TerminalWidgets.lineF()).submit(g);
            return;
        }

        Shapes.fill(g, gx, gy, gw, gh, p.corners(), p.cornerSize(), Palette.argb(0xFF, p.surface()));
        Shapes.outline(g, gx, gy, gw, gh, p.corners(), p.cornerSize(), Palette.argb(0xFF, p.accent()));
    }

    public static void cardShadow(GuiGraphicsExtractor g, int x, int y, int width, int height, Palette p) {
        Shapes.shadow(g, x, y, width, height, p.corners(), p.cornerSize(), 4, 0x78, 2);
    }

    public static void cardFill(GuiGraphicsExtractor g, int x, int y, int width, int height, Palette p) {
        Shapes.fill(g, x, y, width, height, p.corners(), p.cornerSize(), Palette.argb(0xFF, p.surfaceHi()));
    }

    public static void cardEdge(GuiGraphicsExtractor g, int x, int y, int width, int height, Palette p) {
        if (HontunBridge.terminal()) {
            new TerminalWidgets.Px().frame(x, y, width, height, TerminalWidgets.lineF()).submit(g);
            return;
        }
        Shapes.glow(g, x, y, width, height, p.corners(), p.cornerSize(), 3, 0x60, p.accent());
        Shapes.outline(g, x, y, width, height, p.corners(), p.cornerSize(), Palette.argb(0xFF, p.accentHi()));
    }

    public static void flash(GuiGraphicsExtractor g, int x, int y, int width, int height,
                             float progress, Palette p) {
        float strength = 1f - progress;
        if (strength <= 0f) return;

        int alpha = (int) (0xE0 * strength * strength);
        if (alpha <= 2) return;

        if (HontunBridge.terminal()) {
            new TerminalWidgets.Px().frame(x, y, width, height, Palette.argb(alpha, TerminalWidgets.acc())).submit(g);
            return;
        }

        Shapes.outline(g, x, y, width, height, p.corners(), p.cornerSize(),
            Palette.argb(alpha, p.accentHi()));
        Shapes.glow(g, x, y, width, height, p.corners(), p.cornerSize(), 2,
            (int) (0x50 * strength), p.accent());
    }

    private static int scale(int alpha, float fade) {
        return Math.clamp((int) (alpha * fade), 0, 255);
    }
}
