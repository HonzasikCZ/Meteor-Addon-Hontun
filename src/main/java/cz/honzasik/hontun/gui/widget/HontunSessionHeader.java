package cz.honzasik.hontun.gui.widget;

import cz.honzasik.hontun.utils.HontunTheme;
import meteordevelopment.meteorclient.systems.proxies.Proxies;
import meteordevelopment.meteorclient.systems.proxies.Proxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.Objects;
import java.util.UUID;

public class HontunSessionHeader extends AbstractWidget {
    private static final int BOX = 20;
    private static final int HEAD = 16;
    private static final int TEXT_X = 26;

    private ResolvableProfile profile;
    private String cachedName;
    private UUID cachedId;

    public HontunSessionHeader() {
        super(8, 6, 160, BOX, Component.empty());
        this.active = false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        if (!visible) return;

        Minecraft mc = Minecraft.getInstance();
        Font f = mc.font;
        User u = mc.getUser();
        String name = u != null ? u.getName() : "-";
        UUID id = u != null ? u.getProfileId() : null;

        if (profile == null || !Objects.equals(name, cachedName) || !Objects.equals(id, cachedId)) {
            cachedName = name;
            cachedId = id;
            profile = ResolvableProfile.createUnresolved(name);
        }

        int x = getX(), y = getY();
        hontun$frame(g, x, y);
        PlayerFaceExtractor.extractRenderState(g, profile, x + 2, y + 2, HEAD);

        int tx = x + TEXT_X;
        int maxW = Math.max(0, getWidth() - TEXT_X);

        String l1 = "Logged in as " + name;
        g.text(f, Component.literal(clip(f, l1, maxW)), tx, y,
                HontunTheme.argb(0xFF, HontunTheme.textLight()), true);

        String l2 = "Not using a proxy";
        int l2rgb = HontunTheme.textDim();
        try {
            Proxy p = Proxies.get() != null ? Proxies.get().getEnabled() : null;
            if (p != null) {
                String pn = String.valueOf(p.name.get());
                String addr = p.address.get() + ":" + p.port.get();
                boolean noName = pn.isBlank() || "null".equals(pn);
                l2 = (noName ? addr : pn) + "  [" + p.type.get() + "]";
                l2rgb = HontunTheme.green();
            }
        } catch (Throwable ignored) {
        }
        g.text(f, Component.literal(clip(f, l2, maxW)), tx, y + f.lineHeight + 2,
                HontunTheme.argb(0xFF, l2rgb), true);
    }

    private void hontun$frame(GuiGraphicsExtractor g, int x, int y) {
        if (HontunTheme.modern2()) {
            HontunShapes.fillClipped(g, x, y, BOX, BOX, 4, 4, HontunTheme.argb(0xF0, HontunTheme.crust()));
            HontunShapes.glow(g, x, y, BOX, BOX, 4, HontunTheme.accent(), 1, 0x50);
            HontunShapes.outlineClipped(g, x, y, BOX, BOX, 4, 4,
                    HontunTheme.argb(0xE0, HontunTheme.accent()));
            HontunShapes.brackets(g, x, y, BOX, BOX, 5, HontunTheme.argb(0xFF, HontunTheme.accentHi()));
            return;
        }
        if (HontunTheme.modern()) {
            int fill = HontunTheme.argb(0xF0, HontunTheme.surface0());
            g.fill(x + 1, y, x + BOX - 1, y + BOX, fill);
            g.fill(x, y + 1, x + BOX, y + BOX - 1, fill);
            int b = HontunTheme.argb(0x90, HontunTheme.overlay0());
            g.fill(x + 1, y, x + BOX - 1, y + 1, b);
            g.fill(x, y + 1, x + 1, y + BOX - 1, b);
            g.fill(x + BOX - 1, y + 1, x + BOX, y + BOX - 1, b);
            g.fillGradient(x + 1, y + BOX - 2, x + BOX - 1, y + BOX,
                    HontunTheme.argb(0xFF, HontunTheme.accentLo()),
                    HontunTheme.argb(0xFF, HontunTheme.accentHi()));
            return;
        }

        g.fill(x, y, x + BOX, y + BOX, HontunTheme.argb(0xC0, HontunTheme.crust()));
        int light = HontunTheme.argb(0x90, HontunTheme.overlay2());
        int dark = HontunTheme.argb(0xB0, HontunTheme.crust());
        g.fill(x, y, x + BOX - 1, y + 1, light);
        g.fill(x, y, x + 1, y + BOX - 1, light);
        g.fill(x + 1, y + BOX - 1, x + BOX, y + BOX, dark);
        g.fill(x + BOX - 1, y + 1, x + BOX, y + BOX, dark);
    }

    private static String clip(Font f, String s, int maxW) {
        if (maxW <= 0 || f.width(s) <= maxW) return s;
        int room = Math.max(0, maxW - f.width("…"));
        return f.plainSubstrByWidth(s, room) + "…";
    }
}
