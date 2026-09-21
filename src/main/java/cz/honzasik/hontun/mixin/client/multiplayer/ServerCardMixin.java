package cz.honzasik.hontun.mixin.client.multiplayer;

import cz.honzasik.hontun.utils.HontunFlags;
import cz.honzasik.hontun.utils.HontunGeo;
import cz.honzasik.hontun.utils.HontunServerCard;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(ServerSelectionList.OnlineServerEntry.class)
public abstract class ServerCardMixin {
    @Shadow @Final private ServerData serverData;
    @Shadow @Final private JoinMultiplayerScreen screen;
    @Shadow @Final private FaviconTexture icon;
    @Shadow private Identifier statusIcon;

    @Unique private static final int FLAG_W = HontunFlags.W;
    @Unique private static final int ROW_BOTTOM = 8;

    @Unique private boolean hontun$clipped;

    @Inject(method = "extractContent", at = @At("HEAD"))
    private void hontun$cardBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, boolean hovered,
                                       float pt, CallbackInfo ci) {
        if (!HontunTheme.modern2() && !HontunTheme.smog()) return;

        if (hontun$clipped) {
            hontun$clipped = false;
            try { g.disableScissor(); } catch (Throwable ignored) {}
        }
        ServerSelectionList.OnlineServerEntry self = (ServerSelectionList.OnlineServerEntry) (Object) this;
        HontunServerCard.background(g, self, hovered, hontun$selected(self));
        g.enableScissor(0, 0, 0, 0);
        hontun$clipped = true;
    }

    @Inject(method = "extractContent", at = @At("TAIL"))
    private void hontun$cardForeground(GuiGraphicsExtractor g, int mouseX, int mouseY, boolean hovered,
                                       float pt, CallbackInfo ci) {
        ServerSelectionList.OnlineServerEntry self = (ServerSelectionList.OnlineServerEntry) (Object) this;
        boolean selected = hontun$selected(self);

        if (hontun$clipped) {
            hontun$clipped = false;
            g.disableScissor();
            HontunServerCard.foreground(g, self, serverData, icon, statusIcon,
                    hontun$index(), hontun$serverCount(), hovered, mouseX, mouseY);
            HontunServerCard.selection(g, self, selected);
            return;
        }

        HontunServerCard.selection(g, self, selected);

        if (!HontunTheme.restyleEnabled()) return;

        if (hovered) {
            HontunServerCard.arrows(g, self.getContentX(), self.getContentY(),
                    hontun$index(), hontun$serverCount(), mouseX, mouseY);
        }

        if (!HontunGeo.enabled()) return;
        if (serverData == null || serverData.type() != ServerData.Type.OTHER) return;

        if (!hontun$pinged()) return;

        String cc = HontunGeo.country(serverData.ip);

        Font f = Minecraft.getInstance().font;
        Component status = serverData.state() == ServerData.State.INCOMPATIBLE
                ? serverData.version : serverData.status;
        int statusIconX = self.getContentRight() - 10 - 5;
        int statusX = statusIconX - (status == null ? 0 : f.width(status)) - 5;

        int right = statusX - 6;
        int y = self.getContentY() + ROW_BOTTOM - HontunFlags.H;
        int nameEnd = self.getContentX() + 32 + 3 + f.width(serverData.name);
        if (right - FLAG_W < nameEnd + 4) return;

        if (cc == null) {
            HontunFlags.placeholder(g, right, y);
        } else if (!HontunFlags.drawRight(g, cc, right, y)) {
            String up = cc.toUpperCase(Locale.ROOT);
            g.text(f, Component.literal(up), right - f.width(up), y,
                    HontunTheme.argb(0xFF, HontunTheme.textDim()), false);
        }
    }

    @Unique
    private boolean hontun$selected(ServerSelectionList.OnlineServerEntry self) {
        try {
            ServerSelectionList list = ((JoinMultiplayerScreenAccessor) (Object) screen).hontun$serverList();
            return list != null && list.getSelected() == self;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Unique
    private boolean hontun$pinged() {
        ServerData.State st = serverData.state();
        return st != ServerData.State.INITIAL && st != ServerData.State.PINGING;
    }

    @Unique
    private int hontun$index() {
        try {
            ServerList sl = screen.getServers();
            for (int i = 0; i < sl.size(); i++) if (sl.get(i) == serverData) return i;
        } catch (Throwable ignored) {
        }
        return -1;
    }

    @Unique
    private int hontun$serverCount() {
        try {
            return screen.getServers().size();
        } catch (Throwable ignored) {
            return 0;
        }
    }
}
