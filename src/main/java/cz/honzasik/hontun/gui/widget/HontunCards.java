package cz.honzasik.hontun.gui.widget;

import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class HontunCards {
    private HontunCards() {}

    public static void card(GuiGraphicsExtractor g, int x, int y, int w, int h, int argb) {
        if (HontunTheme.modern()) {
            g.fill(x + 1, y, x + w - 1, y + 1, argb);
            if (h > 1) g.fill(x + 1, y + h - 1, x + w - 1, y + h, argb);
            g.fill(x, y + 1, x + w, y + h - 1, argb);
        } else {
            g.fill(x, y, x + w, y + h, argb);
        }
    }

    public static Component clip(net.minecraft.client.gui.Font f, String s, int maxW) {
        if (s == null) return Component.empty();
        if (maxW <= 0) return Component.empty();
        if (f.width(s) <= maxW) return Component.literal(s);
        int room = Math.max(0, maxW - f.width("…"));
        return Component.literal(f.plainSubstrByWidth(s, room) + "…");
    }

    public static void border(GuiGraphicsExtractor g, int x, int y, int w, int h, int argb) {
        g.fill(x + 1, y, x + w - 1, y + 1, argb);
        g.fill(x + 1, y + h - 1, x + w - 1, y + h, argb);
        g.fill(x, y + 1, x + 1, y + h - 1, argb);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, argb);
    }

    public static void bevel(GuiGraphicsExtractor g, int x, int y, int w, int h, int lightArgb, int darkArgb) {
        g.fill(x, y, x + w - 1, y + 1, lightArgb);
        g.fill(x, y, x + 1, y + h - 1, lightArgb);
        g.fill(x + 1, y + h - 1, x + w, y + h, darkArgb);
        g.fill(x + w - 1, y + 1, x + w, y + h, darkArgb);
    }

    public static void surface(GuiGraphicsExtractor g, int x, int y, int w, int h, int bgRgb, int alpha) {
        if (HontunTheme.smog()) {
            HontunRound.card(g, x, y, w, h, 5,
                    HontunTheme.argb(alpha, bgRgb),
                    HontunTheme.argb(0x60, HontunTheme.overlay2()));
            return;
        }
        card(g, x, y, w, h, HontunTheme.argb(alpha, bgRgb));
        if (HontunTheme.modern()) {
            border(g, x, y, w, h, HontunTheme.argb(0x40, HontunTheme.overlay0()));
        } else {
            bevel(g, x, y, w, h,
                    HontunTheme.argb(0x90, HontunTheme.overlay1()),
                    HontunTheme.argb(0xB0, HontunTheme.crust()));
        }
    }

    public static void leftBar(GuiGraphicsExtractor g, int x, int y, int h, int topArgb, int botArgb) {
        g.fillGradient(x + 1, y + 2, x + 4, y + h - 2, topArgb, botArgb);
    }

    public static int bg(boolean active, boolean hovered, int accentTint) {
        if (active) return HontunTheme.lerp(HontunTheme.surface1(), accentTint, 0.18f);
        if (hovered) return HontunTheme.surface2();
        return HontunTheme.surface1();
    }

    public static boolean deleteButton(GuiGraphicsExtractor g, net.minecraft.client.gui.Font f,
                                       int boxX, int boxY, int boxSize, int mouseX, int mouseY) {
        boolean over = mouseX >= boxX && mouseX < boxX + boxSize && mouseY >= boxY && mouseY < boxY + boxSize;
        if (HontunTheme.smog()) {
            if (over) HontunRound.fill(g, boxX, boxY, boxSize, boxSize, 5,
                    HontunTheme.argb(0x66, HontunTheme.red()));
            float cx = boxX + boxSize / 2f, cy = boxY + boxSize / 2f;
            float arm = Math.min(boxSize / 2f - 2f, 4.5f);
            int col = HontunTheme.argb(0xFF, over ? 0xFFFFFF : HontunTheme.textDim());
            HontunRound.stroke(g, cx - arm, cy - arm, cx + arm, cy + arm, 1.25f, col);
            HontunRound.stroke(g, cx - arm, cy + arm, cx + arm, cy - arm, 1.25f, col);
            return over;
        }
        if (over) {
            card(g, boxX, boxY, boxSize, boxSize, HontunTheme.argb(0x55, HontunTheme.red()));
        }
        int col = HontunTheme.argb(0xFF, over ? HontunTheme.red() : HontunTheme.textDim());

        float cx = boxX + boxSize / 2f;
        float cy = boxY + boxSize / 2f;
        g.pose().pushMatrix();
        g.pose().translate(cx, cy);
        g.pose().scale(1.4f, 1.4f);
        net.minecraft.network.chat.Component x = net.minecraft.network.chat.Component.literal("✕");
        g.text(f, x, -f.width(x) / 2, -f.lineHeight / 2, col, false);
        g.pose().popMatrix();
        return over;
    }

    private static final Identifier MENU_LIST_BACKGROUND =
            Identifier.withDefaultNamespace("textures/gui/menu_list_background.png");
    private static final Identifier INWORLD_MENU_LIST_BACKGROUND =
            Identifier.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");

    private static final Identifier INWORLD_MENU_BACKGROUND =
            Identifier.withDefaultNamespace("textures/gui/inworld_menu_background.png");

    private static boolean inWorld() { return Minecraft.getInstance().level != null; }

    public static void vanillaScreenBackground(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        Identifier tex = inWorld() ? INWORLD_MENU_BACKGROUND : Screen.MENU_BACKGROUND;
        Screen.extractMenuBackgroundTexture(g, tex, x, y, 0.0f, 0.0f, w, h);
    }

    public static void vanillaListBackground(GuiGraphicsExtractor g, int x, int y, int w, int h, double scroll) {
        Identifier tex = inWorld() ? INWORLD_MENU_LIST_BACKGROUND : MENU_LIST_BACKGROUND;
        g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, (float) (x + w), (float) (y + h + (int) scroll), w, h, 32, 32);
    }

    public static void vanillaSeparators(GuiGraphicsExtractor g, int x, int top, int bottom, int w) {
        Identifier header = inWorld() ? Screen.INWORLD_HEADER_SEPARATOR : Screen.HEADER_SEPARATOR;
        Identifier footer = inWorld() ? Screen.INWORLD_FOOTER_SEPARATOR : Screen.FOOTER_SEPARATOR;
        g.blit(RenderPipelines.GUI_TEXTURED, header, x, top - 2, 0.0f, 0.0f, w, 2, 32, 2);
        g.blit(RenderPipelines.GUI_TEXTURED, footer, x, bottom, 0.0f, 0.0f, w, 2, 32, 2);
    }

    public static void row(GuiGraphicsExtractor g, int x, int y, int w, int h,
                           boolean active, boolean hovered, int accentTint) {
        if (HontunTheme.smog()) {
            int fill, border;
            if (active) {
                fill = HontunTheme.argb(0xFF, HontunTheme.surface1());
                border = HontunTheme.argb(0xFF, HontunTheme.accent());
            } else if (hovered) {
                fill = HontunTheme.argb(0xFF, HontunTheme.surface2());
                border = HontunTheme.argb(0xFF, HontunTheme.accent());
            } else {
                fill = HontunTheme.argb(0xF0, HontunTheme.base());
                border = HontunTheme.argb(0x60, HontunTheme.overlay2());
            }
            HontunRound.card(g, x, y, w, h, 4, fill, border);
            if (active) {
                HontunRound.fill(g, x + 2, y + 4, 2, h - 8, 1, HontunTheme.argb(0xFF, HontunTheme.accent()));
            }
            return;
        }
        if (HontunTheme.modern2()) {
            HontunShapes.row(g, x, y, w, h, active, hovered);
            return;
        }
        if (HontunTheme.modern()) {
            surface(g, x, y, w, h, bg(active, hovered, accentTint), 0xFF);
            if (active) {
                leftBar(g, x, y, h,
                        HontunTheme.argb(0xFF, HontunTheme.accentHi()),
                        HontunTheme.argb(0xFF, HontunTheme.accentLo()));
            }
            return;
        }

        if (hovered) g.fill(x, y, x + w, y + h, HontunTheme.argb(0x1A, HontunTheme.textLight()));
    }

    private static final Identifier BTN = Identifier.withDefaultNamespace("widget/button");
    private static final Identifier BTN_HI = Identifier.withDefaultNamespace("widget/button_highlighted");

    public static void vanillaButtonCell(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean highlighted) {
        g.blitSprite(RenderPipelines.GUI_TEXTURED, highlighted ? BTN_HI : BTN, x, y, w, h);
    }

    public static void vanillaSelection(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean focused) {
        g.fill(x, y, x + w, y + h,
                HontunTheme.argb(0xFF, focused ? HontunTheme.textLight() : HontunTheme.overlay2()));
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, HontunTheme.argb(0xFF, HontunTheme.crust()));
    }

    public static int rowTextInset(boolean active) {
        if (HontunTheme.smog()) return 6;
        return HontunTheme.modern() && active ? 3 : 0;
    }
}
