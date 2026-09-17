package cz.honzasik.hontun.mixin.client.multiplayer;

import cz.honzasik.hontun.gui.screen.HontunAccountsScreen;
import cz.honzasik.hontun.gui.widget.HontunSessionHeader;
import cz.honzasik.hontun.utils.HontunTheme;
import cz.honzasik.hontun.gui.screen.HontunProxiesScreen;
import cz.honzasik.hontun.gui.screen.HontunVersionScreen;
import cz.honzasik.hontun.utils.VfpBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(JoinMultiplayerScreen.class)
public abstract class JoinMultiplayerVersionButtonMixin {
    private static final String[] hontun$FOREIGN_BUTTON_FIELDS = {
        "viaFabricPlus$button", "accounts", "proxies"
    };

    @Unique private Button hontun$accounts;
    @Unique private Button hontun$proxies;
    @Unique private Button hontun$version;
    @Unique private String hontun$lastVersion = "";
    @Unique private HontunSessionHeader hontun$header;

    @Inject(method = "init", at = @At("TAIL"))
    private void hontun$init(CallbackInfo ci) {
        Screen self = (Screen) (Object) this;

        hontun$accounts = Button.builder(Component.literal("Accounts"),
                b -> Minecraft.getInstance().gui.setScreen(new HontunAccountsScreen(self))).bounds(0, 0, 110, 20).build();
        hontun$proxies = Button.builder(Component.literal("Proxies"),
                b -> Minecraft.getInstance().gui.setScreen(new HontunProxiesScreen(self))).bounds(0, 0, 110, 20).build();
        ((ScreenInvoker) self).hontun$addRenderableWidget(hontun$accounts);
        ((ScreenInvoker) self).hontun$addRenderableWidget(hontun$proxies);

        if (VfpBridge.available()) {
            hontun$version = Button.builder(Component.literal("Version: " + VfpBridge.currentName()),
                    b -> Minecraft.getInstance().gui.setScreen(new HontunVersionScreen(self))).bounds(0, 0, 110, 20).build();
            ((ScreenInvoker) self).hontun$addRenderableWidget(hontun$version);
        }

        hontun$header = new HontunSessionHeader();
        ((ScreenInvoker) self).hontun$addRenderableWidget(hontun$header);

        hontun$layout();
        hontun$removeForeignButtons();
        Minecraft.getInstance().execute(this::hontun$removeForeignButtons);
    }

    @Inject(method = "repositionElements", at = @At("TAIL"))
    private void hontun$reposition(CallbackInfo ci) {
        hontun$layout();
        hontun$removeForeignButtons();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void hontun$tick(CallbackInfo ci) {
        if (hontun$version == null || !VfpBridge.available()) return;
        String cur = VfpBridge.currentName();
        if (!cur.equals(hontun$lastVersion)) {
            hontun$lastVersion = cur;
            hontun$version.setMessage(Component.literal("Version: " + cur));
        }
    }

    @Unique
    private void hontun$layout() {
        Screen self = (Screen) (Object) this;
        Font f = Minecraft.getInstance().font;

        int titleW = f.width(self.getTitle());
        int titleLeft = (self.width - titleW) / 2;
        int titleRight = titleLeft + titleW;

        int bandRight = self.width - 6;
        int band = bandRight - (titleRight + 6);

        int footerMargin = (self.width - 308) / 2;
        int vw = Math.min(110, footerMargin - 10);
        boolean versionInFooter = hontun$version != null && vw >= 60;
        int topCount = (hontun$version != null && !versionInFooter) ? 3 : 2;

        int bw = Math.min(110, (band - 6 * (topCount - 1)) / topCount);
        boolean topFits = bw >= 44;

        if (hontun$accounts != null) {
            hontun$accounts.visible = topFits;
            hontun$accounts.setWidth(Math.max(1, bw));
            hontun$accounts.setX(bandRight - bw);
            hontun$accounts.setY(6);
        }
        if (hontun$proxies != null) {
            hontun$proxies.visible = topFits;
            hontun$proxies.setWidth(Math.max(1, bw));
            hontun$proxies.setX(bandRight - 2 * bw - 6);
            hontun$proxies.setY(6);
        }

        if (hontun$version != null) {
            if (versionInFooter) {
                hontun$version.visible = true;
                hontun$version.setWidth(vw);
                hontun$version.setX(self.width - 6 - vw);

                hontun$version.setY(self.height - 60 + (60 - 20) / 2);
            } else {
                hontun$version.visible = topFits;
                hontun$version.setWidth(Math.max(1, bw));
                hontun$version.setX(bandRight - 3 * bw - 12);
                hontun$version.setY(6);
            }
        }

        if (hontun$header != null) {
            int limit = titleLeft - 6;
            if (hontun$proxies != null && hontun$proxies.visible) {
                limit = Math.min(limit, hontun$proxies.getX() - 6);
            }
            int avail = limit - 8;
            hontun$header.setX(8);
            hontun$header.setY(6);
            hontun$header.setWidth(Math.max(26, avail));
            hontun$header.visible = HontunTheme.restyleEnabled() && avail >= 60;
        }
    }

    @Unique
    private void hontun$removeForeignButtons() {
        for (String name : hontun$FOREIGN_BUTTON_FIELDS) {
            try {
                Field f = JoinMultiplayerScreen.class.getDeclaredField(name);
                f.setAccessible(true);
                Object btn = f.get(this);
                if (btn instanceof GuiEventListener g) {
                    ((ScreenInvoker) this).hontun$removeWidget(g);
                }
            } catch (Throwable ignored) {
            }
        }
    }
}
