package cz.honzasik.hontun.utils;

import cz.honzasik.hontun.gui.widget.HontunShapes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.Locale;

public final class HontunServerCard {
    private static final Identifier JOIN = Identifier.withDefaultNamespace("server_list/join");
    private static final Identifier JOIN_HI = Identifier.withDefaultNamespace("server_list/join_highlighted");
    private static final Identifier MOVE_UP = Identifier.withDefaultNamespace("server_list/move_up");
    private static final Identifier MOVE_UP_HI = Identifier.withDefaultNamespace("server_list/move_up_highlighted");
    private static final Identifier MOVE_DOWN = Identifier.withDefaultNamespace("server_list/move_down");
    private static final Identifier MOVE_DOWN_HI = Identifier.withDefaultNamespace("server_list/move_down_highlighted");

    private static final int CUT = 5;
    private static final int FLAG_W = HontunFlags.W;
    private static final int ROW_BOTTOM = 8;

    private HontunServerCard() {}

    public static void background(GuiGraphicsExtractor g, ServerSelectionList.OnlineServerEntry e, boolean hovered) {
        int x = e.getX(), y = e.getY() + 1, w = e.getWidth(), h = e.getHeight() - 2;

        if (hovered) HontunShapes.glow(g, x, y, w, h, CUT, HontunTheme.accent(), 2, 0x50);
        HontunShapes.fillClipped(g, x, y, w, h, CUT, CUT,
                HontunTheme.argb(hovered ? 0xE8 : 0xC0, hovered ? HontunTheme.surface1() : HontunTheme.surface0()));
        HontunShapes.outlineClipped(g, x, y, w, h, CUT, CUT,
                HontunTheme.argb(hovered ? 0xE0 : 0x60, hovered ? HontunTheme.accentLo() : HontunTheme.overlay0()));
    }

    public static void foreground(GuiGraphicsExtractor g, ServerSelectionList.OnlineServerEntry e,
                                  ServerData data, FaviconTexture icon, Identifier statusIcon,
                                  int index, int serverCount, boolean hovered, int mouseX, int mouseY) {
        Font f = Minecraft.getInstance().font;
        int contentX = e.getContentX(), contentY = e.getContentY(), contentRight = e.getContentRight();

        g.fill(contentX + 33, contentY, contentX + 34, contentY + 32,
                HontunTheme.argb(0x90, HontunTheme.overlay0()));

        g.blit(RenderPipelines.GUI_TEXTURED, icon.textureLocation(), contentX, contentY, 0.0f, 0.0f,
                32, 32, 64, 64, 64, 64);

        int statusIconX = contentRight - 15;
        if (statusIcon != null) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, statusIcon, statusIconX, contentY, 10, 8);
        }

        Component status = data.state() == ServerData.State.INCOMPATIBLE
                ? data.version.copy().withStyle(ChatFormatting.RED)
                : data.status;
        int statusX = statusIconX;
        if (status != null) {
            statusX = statusIconX - f.width(status) - 5;
            g.text(f, status, statusX, contentY + 1, HontunTheme.argb(0xFF, HontunTheme.subtext1()), true);
        }

        int tx = contentX + 35;
        boolean bad = data.state() == ServerData.State.UNREACHABLE;
        g.text(f, data.name, tx, contentY + 1,
                HontunTheme.argb(0xFF, bad ? HontunTheme.red() : HontunTheme.textLight()), true);

        if (data.motd != null) {
            int motdW = Math.max(40, contentRight - 12 - tx);
            List<FormattedCharSequence> lines = f.split(data.motd, motdW);
            int motdColor = HontunTheme.argb(0xFF, HontunTheme.textDim());
            for (int i = 0; i < Math.min(lines.size(), 2); i++) {
                g.text(f, lines.get(i), tx, contentY + 12 + f.lineHeight * i, motdColor, false);
            }
        }

        drawFlag(g, f, data, contentX, contentY, statusX);

        if (hovered) {
            g.fill(contentX, contentY, contentX + 32, contentY + 32, HontunTheme.argb(0xA0, HontunTheme.crust()));
            int relX = mouseX - contentX, relY = mouseY - contentY;
            boolean right = relX >= 16 && relX < 32 && relY >= 0 && relY < 32;
            g.blitSprite(RenderPipelines.GUI_TEXTURED, right ? JOIN_HI : JOIN, contentX, contentY, 32, 32);
            if (index > 0) {
                boolean topLeft = relX >= 0 && relX < 16 && relY >= 0 && relY < 16;
                g.blitSprite(RenderPipelines.GUI_TEXTURED, topLeft ? MOVE_UP_HI : MOVE_UP, contentX, contentY, 32, 32);
            }
            if (index >= 0 && index < serverCount - 1) {
                boolean botLeft = relX >= 0 && relX < 16 && relY >= 16 && relY < 32;
                g.blitSprite(RenderPipelines.GUI_TEXTURED, botLeft ? MOVE_DOWN_HI : MOVE_DOWN, contentX, contentY, 32, 32);
            }
        }
    }

    private static void drawFlag(GuiGraphicsExtractor g, Font f, ServerData data,
                                 int contentX, int contentY, int statusX) {
        if (!HontunGeo.enabled() || data.type() != ServerData.Type.OTHER) return;

        ServerData.State st = data.state();
        if (st == ServerData.State.INITIAL || st == ServerData.State.PINGING) return;

        String cc = HontunGeo.country(data.ip);

        int right = statusX - 6;
        int y = contentY + ROW_BOTTOM - HontunFlags.H;
        int nameEnd = contentX + 35 + f.width(data.name);
        if (right - FLAG_W < nameEnd + 4) return;

        if (cc == null) {
            HontunFlags.placeholder(g, right, y);
        } else if (!HontunFlags.drawRight(g, cc, right, y)) {
            String up = cc.toUpperCase(Locale.ROOT);
            g.text(f, Component.literal(up), right - f.width(up), y,
                    HontunTheme.argb(0xFF, HontunTheme.textDim()), false);
        }
    }
}
