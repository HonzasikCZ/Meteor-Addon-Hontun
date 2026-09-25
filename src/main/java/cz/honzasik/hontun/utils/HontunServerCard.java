package cz.honzasik.hontun.utils;

import cz.honzasik.hontun.gui.widget.HontunRound;
import cz.honzasik.hontun.gui.widget.HontunShapes;
import cz.honzasik.hontun.mixin.client.multiplayer.JoinMultiplayerScreenAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.Locale;

public final class HontunServerCard {
    private static final int CUT = 5;
    private static final int FLAG_W = HontunFlags.W;
    private static final int ROW_BOTTOM = 8;

    private HontunServerCard() {}

    public static void background(GuiGraphicsExtractor g, ServerSelectionList.OnlineServerEntry e, boolean hovered) {
        background(g, e, hovered, false);
    }

    public static void background(GuiGraphicsExtractor g, ServerSelectionList.OnlineServerEntry e,
                                  boolean hovered, boolean selected) {
        int x = e.getX(), y = e.getY() + 1, w = e.getWidth(), h = e.getHeight() - 2;

        if (HontunTheme.smog()) {
            int fill, border;
            if (selected) {
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
            return;
        }

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

        boolean smog = HontunTheme.smog();

        Component status = data.state() == ServerData.State.INCOMPATIBLE
                ? data.version.copy().withStyle(ChatFormatting.RED)
                : data.status;
        int statusX = statusIconX;
        if (status != null) {
            Component statusText = smog ? HontunFont.apply(status) : status;
            statusX = statusIconX - f.width(statusText) - 5;
            g.text(f, statusText, statusX, contentY + 1, HontunTheme.argb(0xFF, HontunTheme.subtext1()), true);
        }

        int tx = contentX + 35;
        boolean bad = data.state() == ServerData.State.UNREACHABLE;
        int nameColor = HontunTheme.argb(0xFF, bad ? HontunTheme.red() : HontunTheme.textLight());
        if (smog) {
            g.text(f, HontunFont.text(data.name), tx, contentY + 1, nameColor, true);
        } else {
            g.text(f, data.name, tx, contentY + 1, nameColor, true);
        }

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
            arrows(g, contentX, contentY, index, serverCount, mouseX, mouseY);
        }
    }

    public static void selection(GuiGraphicsExtractor g, ServerSelectionList.OnlineServerEntry e, boolean selected) {
        if (!selected) return;
        int x = e.getX(), y = e.getY() + 1, w = e.getWidth(), h = e.getHeight() - 2;

        if (HontunTheme.smog()) return;

        if (HontunTheme.modern2()) {
            HontunShapes.glow(g, x, y, w, h, CUT, HontunTheme.accentHi(), 3, 0x90);
            HontunShapes.outlineClipped(g, x, y, w, h, CUT, CUT, HontunTheme.argb(0xFF, HontunTheme.accentHi()));
        } else if (HontunTheme.modern1()) {
            int c = HontunTheme.argb(0xFF, HontunTheme.accent());
            g.fill(x, y, x + w, y + 1, c);
            g.fill(x, y + h - 1, x + w, y + h, c);
            g.fill(x, y, x + 3, y + h, HontunTheme.argb(0xFF, HontunTheme.accentHi()));
        } else {
            int rgb = HontunTheme.vanilla() ? 0xFFFFFF : HontunTheme.accent();
            int c = HontunTheme.argb(0xFF, rgb);
            g.fill(x, y, x + w, y + 1, c);
            g.fill(x, y + h - 1, x + w, y + h, c);
            g.fill(x, y, x + 1, y + h, c);
            g.fill(x + w - 1, y, x + w, y + h, c);
        }
    }

    public static void arrows(GuiGraphicsExtractor g, int contentX, int contentY, int index, int count,
                              int mouseX, int mouseY) {
        int relX = mouseX - contentX, relY = mouseY - contentY;
        boolean upZone = index > 0;
        boolean downZone = index >= 0 && index < count - 1;
        boolean joinH = relX >= 16 && relX < 32 && relY >= 0 && relY < 32;
        boolean upH = upZone && relX >= 0 && relX < 16 && relY >= 0 && relY < 16;
        boolean downH = downZone && relX >= 0 && relX < 16 && relY >= 16 && relY < 32;

        zone(g, contentX + 16, contentY, 16, 32, joinH);
        if (upZone) zone(g, contentX, contentY, 16, 16, upH);
        if (downZone) zone(g, contentX, contentY + 16, 16, 16, downH);

        if (HontunTheme.smog()) {
            smogChevron(g, contentX + 16, contentY, 16, 32, '>', joinH);
            if (upZone) smogChevron(g, contentX, contentY, 16, 16, '^', upH);
            if (downZone) smogChevron(g, contentX, contentY + 16, 16, 16, 'v', downH);
            return;
        }
        arrowRight(g, contentX + 19, contentY + 16, 6, HontunTheme.argb(0xFF, joinH ? HontunTheme.accentHi() : HontunTheme.accent()));
        if (upZone) arrowUp(g, contentX + 8, contentY + 5, 4, HontunTheme.argb(0xFF, upH ? HontunTheme.accentHi() : HontunTheme.accent()));
        if (downZone) arrowDown(g, contentX + 8, contentY + 24, 4, HontunTheme.argb(0xFF, downH ? HontunTheme.accentHi() : HontunTheme.accent()));
    }

    private static void smogChevron(GuiGraphicsExtractor g, int zx, int zy, int zw, int zh,
                                    char dir, boolean hovered) {
        float cx = zx + zw / 2f, cy = zy + zh / 2f;
        float s = zh >= 24 ? 4.2f : 3.0f;
        float th = 0.85f;
        float ax, ay, mx, my, bx, by;
        switch (dir) {
            case '>' -> { mx = cx + s * 0.72f; my = cy;              ax = cx - s * 0.5f; ay = cy - s;        bx = cx - s * 0.5f; by = cy + s; }
            case '^' -> { mx = cx;              my = cy - s * 0.72f;  ax = cx - s;        ay = cy + s * 0.5f; bx = cx + s;        by = cy + s * 0.5f; }
            default  -> { mx = cx;              my = cy + s * 0.72f;  ax = cx - s;        ay = cy - s * 0.5f; bx = cx + s;        by = cy - s * 0.5f; }
        }
        int col = HontunTheme.argb(0xFF, hovered ? HontunTheme.textLight() : HontunTheme.subtext1());
        HontunRound.stroke(g, ax, ay, mx, my, th, col);
        HontunRound.stroke(g, mx, my, bx, by, th, col);
    }

    private static void zone(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean hovered) {
        if (HontunTheme.smog()) {
            HontunRound.fill(g, x + 1, y + 1, w - 2, h - 2, 4, HontunTheme.argb(hovered ? 0xC0 : 0x7A, 0x000000));
            return;
        }
        if (HontunTheme.modern2()) {
            int cut = 2;
            HontunShapes.fillClipped(g, x, y, w, h, cut, cut,
                    HontunTheme.argb(hovered ? 0xE8 : 0xB8, hovered ? HontunTheme.surface1() : HontunTheme.crust()));
            if (hovered) HontunShapes.outlineClipped(g, x, y, w, h, cut, cut, HontunTheme.argb(0xFF, HontunTheme.accentHi()));
        } else if (HontunTheme.modern1()) {
            g.fill(x, y, x + w, y + h, HontunTheme.argb(hovered ? 0xE8 : 0xB8, hovered ? HontunTheme.surface1() : HontunTheme.crust()));
            if (hovered) g.fill(x, y + h - 1, x + w, y + h, HontunTheme.argb(0xFF, HontunTheme.accent()));
        } else {
            g.fill(x, y, x + w, y + h, HontunTheme.argb(hovered ? 0xCC : 0x99, hovered ? HontunTheme.surface1() : HontunTheme.crust()));
            if (hovered) {
                int b = HontunTheme.argb(0xFF, HontunTheme.accent());
                g.fill(x, y, x + w, y + 1, b);
                g.fill(x, y + h - 1, x + w, y + h, b);
                g.fill(x, y, x + 1, y + h, b);
                g.fill(x + w - 1, y, x + w, y + h, b);
            }
        }
    }

    private static void arrowRight(GuiGraphicsExtractor g, int cx, int cy, int size, int argb) {
        for (int i = 0; i < size; i++) {
            int half = size - 1 - i;
            g.fill(cx + i, cy - half, cx + i + 1, cy + half + 1, argb);
        }
    }

    private static void arrowUp(GuiGraphicsExtractor g, int cx, int cy, int size, int argb) {
        for (int i = 0; i < size; i++) {
            g.fill(cx - i, cy + i, cx + i + 1, cy + i + 1, argb);
        }
    }

    private static void arrowDown(GuiGraphicsExtractor g, int cx, int cy, int size, int argb) {
        for (int i = 0; i < size; i++) {
            int half = size - 1 - i;
            g.fill(cx - half, cy + i, cx + half + 1, cy + i + 1, argb);
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
