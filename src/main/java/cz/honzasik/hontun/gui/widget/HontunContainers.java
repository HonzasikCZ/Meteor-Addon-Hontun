package cz.honzasik.hontun.gui.widget;

import cz.honzasik.hontun.gui.render.RoundedGui;
import cz.honzasik.hontun.mixin.client.container.ContainerScreenAccessor;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.DispenserScreen;
import net.minecraft.client.gui.screens.inventory.HopperScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public final class HontunContainers {
    private HontunContainers() {}

    public static boolean themed(AbstractContainerScreen<?> s) {
        return s instanceof InventoryScreen || s instanceof CraftingScreen || s instanceof ContainerScreen
                || s instanceof ShulkerBoxScreen || s instanceof HopperScreen || s instanceof DispenserScreen
                || s instanceof AbstractFurnaceScreen<?>;
    }

    public static void draw(GuiGraphicsExtractor g, AbstractContainerScreen<?> s, int x, int y) {
        ContainerScreenAccessor a = (ContainerScreenAccessor) s;
        int w = a.hontun$imageWidth();
        int h = a.hontun$imageHeight();
        if (s instanceof ContainerScreen) h -= 1;

        panel(g, x, y, w, h);

        List<Slot> slots = s.getMenu().slots;
        int big = s instanceof CraftingScreen ? 0 : (s instanceof AbstractFurnaceScreen<?> ? 2 : -1);
        for (int i = 0; i < slots.size(); i++) {
            Slot slot = slots.get(i);
            if (!slot.isActive()) continue;
            if (i == big) slot(g, x + slot.x - 5, y + slot.y - 5, 26);
            else slot(g, x + slot.x - 1, y + slot.y - 1, 18);
        }

        if (s instanceof InventoryScreen) {
            well(g, x + 25, y + 7, 51, 72);
            arrow(g, x + 135, y + 29, 16, 13, decorColor());
        } else if (s instanceof CraftingScreen) {
            arrow(g, x + 90, y + 35, 22, 15, decorColor());
        } else if (s instanceof AbstractFurnaceScreen<?>) {
            flameEmpty(g, x + 56, y + 36);
            arrowEmpty(g, x + 79, y + 34);
        }
    }

    private static final String[] FLAME = {
        ".#.........#..",
        ".##...#...###.",
        "..#...#...#.#.",
        ".###..##..##..",
        ".###...#..##..",
        ".###..###.###.",
        "####..###.###.",
        "####.####.####",
        "###..####..###",
        "###..####.####",
        "####.###..####",
        ".###.###..###.",
        "####.####.###.",
        ".###..###..###"
    };

    private static final String[] BURN = {
        "...............#........",
        "...............##.......",
        "...............###......",
        "...............####.....",
        "...............#####....",
        "...............######...",
        ".#####################..",
        ".######################.",
        ".######################.",
        ".#####################..",
        "...............######...",
        "...............#####....",
        "...............####.....",
        "...............###......",
        "...............##.......",
        "...............#........"
    };

    private static int decorColor() {
        if (HontunTheme.smog()) return HontunTheme.argb(0xFF, 0xB8BDC3);
        return HontunTheme.argb(0xFF, HontunTheme.modern() ? HontunTheme.overlay2() : HontunTheme.overlay1());
    }

    private static int emptyColor() {
        return HontunTheme.argb(0xFF, HontunTheme.smog() ? HontunTheme.overlay1() : HontunTheme.overlay0());
    }

    private static boolean smoothFurnace() {
        return HontunTheme.smog() && RoundedGui.available();
    }

    private static void flameEmpty(GuiGraphicsExtractor g, int bx, int by) {
        if (smoothFurnace()) {
            flameShape(g, bx, by, emptyColor());
            return;
        }
        mask(g, FLAME, bx, by, 0, 14, emptyColor(), emptyColor());
    }

    public static void flameLit(GuiGraphicsExtractor g, int bx, int by, int litH) {
        if (litH <= 0) return;
        int from = Math.max(0, 14 - litH);
        if (smoothFurnace()) {
            g.enableScissor(bx, by + from, bx + 14, by + 14);
            flameShape(g, bx, by, HontunTheme.argb(0xFF, HontunTheme.accentHi()));
            g.disableScissor();
            return;
        }
        mask(g, FLAME, bx, by, from, 14, 0xFFFFC43D, 0xFFFF8A2A);
    }

    private static void flameShape(GuiGraphicsExtractor g, int bx, int by, int argb) {
        float cx = bx + 7f;
        RoundedGui.circle(g, cx, by + 9.9f, 3.4f, 0f, argb);
        RoundedGui.capsule(g, cx, by + 9.4f, cx, by + 6.2f, 3.0f, argb);
        RoundedGui.capsule(g, cx, by + 6.8f, cx, by + 3.6f, 1.95f, argb);
        RoundedGui.capsule(g, cx, by + 4.2f, cx + 0.4f, by + 1.2f, 0.95f, argb);
    }

    private static void arrowEmpty(GuiGraphicsExtractor g, int bx, int by) {
        if (smoothFurnace()) {
            arrow(g, bx + 1, by + 1, 22, 15, emptyColor());
            return;
        }
        mask(g, BURN, bx, by, 0, 24, emptyColor(), emptyColor());
    }

    public static void arrowLit(GuiGraphicsExtractor g, int bx, int by, int progressW) {
        if (progressW <= 0) return;
        if (smoothFurnace()) {
            g.enableScissor(bx, by, bx + progressW, by + 16);
            arrow(g, bx + 1, by + 1, 22, 15, HontunTheme.argb(0xFF, HontunTheme.accentHi()));
            g.disableScissor();
            return;
        }
        int lit = HontunTheme.argb(0xFF, HontunTheme.textLight());
        mask(g, BURN, bx, by, 0, progressW, lit, lit);
    }

    private static void mask(GuiGraphicsExtractor g, String[] m, int x, int y, int rowFrom, int colTo,
                             int topArgb, int bottomArgb) {
        int half = m.length / 2;
        for (int r = rowFrom; r < m.length; r++) {
            String row = m[r];
            int limit = Math.min(colTo, row.length());
            int argb = r < half ? topArgb : bottomArgb;
            int c = 0;
            while (c < limit) {
                if (row.charAt(c) != '#') { c++; continue; }
                int start = c;
                while (c < limit && row.charAt(c) == '#') c++;
                g.fill(x + start, y + r, x + c, y + r + 1, argb);
            }
        }
    }

    private static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        if (HontunTheme.smog()) {
            RoundedGui.fill(g, x - 10f, y - 8f, w + 20f, h + 22f, 16f, 10f, HontunTheme.argb(0x90, 0x000000));
            HontunRound.card(g, x, y, w, h, 7, HontunTheme.argb(0xEE, HontunTheme.base()),
                    HontunTheme.argb(0x70, HontunTheme.overlay2()));
            return;
        }
        if (HontunTheme.modern2()) {
            HontunShapes.glow(g, x, y, w, h, 6, HontunTheme.accent(), 2, 0x50);
            HontunShapes.fillClipped(g, x, y, w, h, 6, 6, HontunTheme.argb(0xF4, HontunTheme.surface0()));
            HontunShapes.outlineClipped(g, x, y, w, h, 6, 6, HontunTheme.argb(0xFF, HontunTheme.accentLo()));
            return;
        }
        if (HontunTheme.modern1()) {
            g.fillGradient(x, y, x + w, y + h, HontunTheme.argb(0xF4, HontunTheme.surface1()),
                    HontunTheme.argb(0xF4, HontunTheme.surface0()));
            HontunCards.border(g, x, y, w, h, HontunTheme.argb(0x80, HontunTheme.overlay0()));
            g.fillGradient(x + 1, y + h - 2, x + w - 1, y + h, HontunTheme.argb(0xFF, HontunTheme.accent()),
                    HontunTheme.argb(0xFF, HontunTheme.accentHi()));
            return;
        }
        g.fill(x, y, x + w, y + h, HontunTheme.argb(0xF2, HontunTheme.base()));
        rect(g, x, y, w, h, HontunTheme.argb(0xCC, HontunTheme.accent()));
    }

    private static void slot(GuiGraphicsExtractor g, int x, int y, int size) {
        if (HontunTheme.smog()) {
            if (RoundedGui.available()) {
                RoundedGui.fill(g, (float) x + 0.5f, (float) y + 0.5f, size - 1f, size - 1f, 3.5f, 0f,
                        HontunTheme.argb(0xA0, 0x000000));
                RoundedGui.outline(g, (float) x + 0.5f, (float) y + 0.5f, size - 1f, size - 1f, 3.5f, 1f,
                        HontunTheme.argb(0x34, 0xFFFFFF));
            } else {
                HontunRound.fill(g, x, y, size, size, 3, HontunTheme.argb(0xA0, 0x000000));
            }
            return;
        }
        if (HontunTheme.modern2()) {
            HontunShapes.fillClipped(g, x, y, size, size, 3, 3, HontunTheme.argb(0xFF, HontunTheme.crust()));
            HontunShapes.outlineClipped(g, x, y, size, size, 3, 3, HontunTheme.argb(0xB0, HontunTheme.overlay0()));
            return;
        }
        if (HontunTheme.modern1()) {
            g.fill(x, y, x + size, y + size, HontunTheme.argb(0xFF, HontunTheme.mantle()));
            HontunCards.border(g, x, y, size, size, HontunTheme.argb(0x90, HontunTheme.overlay0()));
            return;
        }
        g.fill(x, y, x + size, y + size, HontunTheme.argb(0xFF, HontunTheme.crust()));
        int dark = HontunTheme.argb(0xFF, 0x050506);
        int light = HontunTheme.argb(0xFF, HontunTheme.overlay1());
        g.fill(x, y, x + size - 1, y + 1, dark);
        g.fill(x, y, x + 1, y + size - 1, dark);
        g.fill(x + 1, y + size - 1, x + size, y + size, light);
        g.fill(x + size - 1, y + 1, x + size, y + size, light);
    }

    private static void well(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        if (HontunTheme.smog()) {
            if (RoundedGui.available()) {
                RoundedGui.fill(g, (float) x, (float) y, w, h, 5f, 0f, HontunTheme.argb(0xC8, 0x000000));
                RoundedGui.outline(g, (float) x, (float) y, w, h, 5f, 1f, HontunTheme.argb(0x34, 0xFFFFFF));
            } else {
                HontunRound.fill(g, x, y, w, h, 5, HontunTheme.argb(0xC8, 0x000000));
            }
            return;
        }
        if (HontunTheme.modern2()) {
            HontunShapes.fillClipped(g, x, y, w, h, 5, 5, HontunTheme.argb(0xFF, HontunTheme.crust()));
            HontunShapes.outlineClipped(g, x, y, w, h, 5, 5, HontunTheme.argb(0xB0, HontunTheme.overlay0()));
            return;
        }
        g.fill(x, y, x + w, y + h, HontunTheme.argb(0xFF, HontunTheme.crust()));
        if (HontunTheme.modern1()) {
            HontunCards.border(g, x, y, w, h, HontunTheme.argb(0x90, HontunTheme.overlay0()));
            return;
        }
        int dark = HontunTheme.argb(0xFF, 0x050506);
        int light = HontunTheme.argb(0xFF, HontunTheme.overlay1());
        g.fill(x, y, x + w - 1, y + 1, dark);
        g.fill(x, y, x + 1, y + h - 1, dark);
        g.fill(x + 1, y + h - 1, x + w, y + h, light);
        g.fill(x + w - 1, y + 1, x + w, y + h, light);
    }

    private static void arrow(GuiGraphicsExtractor g, int x, int y, int w, int h, int col) {
        if (HontunTheme.smog() && RoundedGui.available()) {
            float cy = y + h / 2f;
            float tip = x + w - 1.5f;
            float hh = Math.min(h / 2f - 1.5f, 5f);
            RoundedGui.capsule(g, x + 1.5f, cy, tip - 0.8f, cy, 1.05f, col);
            RoundedGui.capsule(g, tip, cy, tip - hh, cy - hh, 1.05f, col);
            RoundedGui.capsule(g, tip, cy, tip - hh, cy + hh, 1.05f, col);
            return;
        }
        int cy = y + h / 2;
        int head = h / 2 + 1;
        int headX = x + w - head;
        g.fill(x, cy - 1, headX, cy + 2, col);
        for (int i = 0; i < head; i++) {
            int half = head - 1 - i;
            g.fill(headX + i, cy - half, headX + i + 1, cy + half + 1, col);
        }
    }

    private static void rect(GuiGraphicsExtractor g, int x, int y, int w, int h, int argb) {
        g.fill(x, y, x + w, y + 1, argb);
        g.fill(x, y + h - 1, x + w, y + h, argb);
        g.fill(x, y + 1, x + 1, y + h - 1, argb);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, argb);
    }
}
