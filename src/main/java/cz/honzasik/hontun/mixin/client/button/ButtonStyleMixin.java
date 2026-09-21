package cz.honzasik.hontun.mixin.client.button;

import cz.honzasik.hontun.gui.widget.HontunIcons;
import cz.honzasik.hontun.gui.widget.HontunShapes;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(AbstractButton.class)
public abstract class ButtonStyleMixin {
    @Inject(method = "extractDefaultSprite", at = @At("HEAD"), cancellable = true)
    private void hontun$modernButton(GuiGraphicsExtractor g, CallbackInfo ci) {
        if (!HontunTheme.restyleEnabled()) return;

        AbstractWidget w = (AbstractWidget) (Object) this;

        if (!HontunTheme.modern()) {
            hontun$vanillaStyled(g, w);
            ci.cancel();
            return;
        }

        if (HontunTheme.modern2()) {
            hontun$modern2Button(g, w);
            ci.cancel();
            return;
        }

        if (HontunTheme.smog()) {
            hontun$smogButton(g, w);
            ci.cancel();
            return;
        }

        int x = w.getX();
        int y = w.getY();
        int x2 = x + w.getWidth();
        int y2 = y + w.getHeight();

        boolean active = w.active;
        boolean hovered = active && w.isHoveredOrFocused();

        int surface0 = HontunTheme.surface0();
        int surface1 = HontunTheme.surface1();
        int surface2 = HontunTheme.surface2();
        int accent   = HontunTheme.accent();
        int accentHi = HontunTheme.accentHi();
        int accentLo = HontunTheme.accentLo();

            int topFill, botFill;
            if (!active) {
                topFill = HontunTheme.argb(0x30, HontunTheme.lighten(surface1, 1.06f));
                botFill = HontunTheme.argb(0x30, surface0);
            } else if (hovered) {
                topFill = HontunTheme.argb(0x9A, accentLo);
                botFill = HontunTheme.argb(0x66, HontunTheme.darken(accentLo, 0.6f));
            } else {
                topFill = HontunTheme.argb(0x55, HontunTheme.lighten(surface2, 1.04f));
                botFill = HontunTheme.argb(0x4A, surface0);
            }

            g.fillGradient(x + 1, y, x2 - 1, y2, topFill, botFill);

            g.fill(x, y + 1, x + 1, y2 - 1, botFill);
            g.fill(x2 - 1, y + 1, x2, y2 - 1, botFill);

            g.fill(x + 1, y, x2 - 1, y + 1, HontunTheme.argb(hovered ? 0x70 : 0x2E, HontunTheme.overlay2()));

            g.fillGradient(x + 1, y2 - 2, x2 - 1, y2,
                    HontunTheme.argb(active ? (hovered ? 0xFF : 0xC0) : 0x40, accent),
                    HontunTheme.argb(active ? 0xFF : 0x40, accentHi));

            if (hovered) {
                int bd = HontunTheme.argb(0xE0, accentHi);
                g.fill(x + 1, y, x2 - 1, y + 1, bd);
                g.fill(x, y + 1, x + 1, y2 - 1, bd);
                g.fill(x2 - 1, y + 1, x2, y2 - 1, bd);
            }

        ci.cancel();
    }

    @Unique
    private void hontun$vanillaStyled(GuiGraphicsExtractor g, AbstractWidget w) {
        int x = w.getX(), y = w.getY(), bw = w.getWidth(), bh = w.getHeight();
        if (bw <= 2 || bh <= 2) return;
        int x2 = x + bw, y2 = y + bh;

        boolean active = w.active;
        boolean hovered = active && w.isHoveredOrFocused();

        int bg = !active ? HontunTheme.argb(0x55, HontunTheme.surface0())
                : hovered ? HontunTheme.argb(0xCC, HontunTheme.accentLo())
                : HontunTheme.argb(0x99, HontunTheme.surface1());
        int border = !active ? HontunTheme.argb(0x44, HontunTheme.accentLo())
                : hovered ? HontunTheme.argb(0xFF, HontunTheme.accentHi())
                : HontunTheme.argb(0xAA, HontunTheme.accent());

        g.fill(x, y, x2, y2, bg);
        g.fill(x, y, x2, y + 1, border);
        g.fill(x, y2 - 1, x2, y2, border);
        g.fill(x, y, x + 1, y2, border);
        g.fill(x2 - 1, y, x2, y2, border);
    }

    @Unique private Component hontun$lastMsg;
    @Unique private int hontun$icon = -1;

    @Unique
    private void hontun$modern2Button(GuiGraphicsExtractor g, AbstractWidget w) {
        int x = w.getX(), y = w.getY(), bw = w.getWidth(), bh = w.getHeight();
        if (bw <= 0 || bh <= 0) return;

        boolean active = w.active;
        boolean hovered = active && w.isHoveredOrFocused();
        int cut = Math.min(5, Math.min(bw, bh) / 3);

        if (hovered) HontunShapes.glow(g, x, y, bw, bh, cut, HontunTheme.accent(), 2, 0x60);

        HontunShapes.fillClipped(g, x, y, bw, bh, cut, cut, HontunTheme.argb(
                active ? (hovered ? 0xF0 : 0xD0) : 0x60,
                hovered ? HontunTheme.surface2() : HontunTheme.surface0()));
        HontunShapes.outlineClipped(g, x, y, bw, bh, cut, cut, HontunTheme.argb(
                active ? (hovered ? 0xFF : 0x90) : 0x40,
                hovered ? HontunTheme.accentHi() : HontunTheme.accentLo()));

        if (bw > cut + 4) {
            g.fill(x + 2, y + bh - 2, x + bw - cut - 1, y + bh - 1,
                    HontunTheme.argb(active ? (hovered ? 0xFF : 0xA0) : 0x30, HontunTheme.accent()));
        }

        int icon = hontun$iconFor(w.getMessage());
        if (icon >= 0) {
            int tw = Minecraft.getInstance().font.width(w.getMessage());
            int ix = x + bw / 2 - tw / 2 - HontunIcons.SIZE - 3;
            if (ix >= x + 3) {
                HontunIcons.draw(g, icon, ix, y + bh / 2 - 4, HontunTheme.argb(
                        active ? 0xFF : 0x60, hovered ? HontunTheme.accentHi() : HontunTheme.subtext1()));
            }
        }
    }

    @Unique
    private void hontun$smogButton(GuiGraphicsExtractor g, AbstractWidget w) {
        int x = w.getX(), y = w.getY(), bw = w.getWidth(), bh = w.getHeight();
        if (bw <= 2 || bh <= 2) return;

        boolean active = w.active;
        boolean hovered = active && w.isHoveredOrFocused();
        int r = Math.min(8, Math.min(bw, bh) / 2);

        int a = !active ? 0x40 : (hovered ? 0xB4 : 0x6E);
        cz.honzasik.hontun.gui.widget.HontunRound.fill(g, x, y, bw, bh, r,
                cz.honzasik.hontun.utils.HontunTheme.argb(a, 0x000000));
    }

    @Unique
    private int hontun$iconFor(Component msg) {
        if (msg == hontun$lastMsg) return hontun$icon;
        hontun$lastMsg = msg;
        hontun$icon = -1;
        if (msg == null) return -1;
        String s = msg.getString().toLowerCase(Locale.ROOT);
        if (s.contains("join") || s.contains("play")) hontun$icon = HontunIcons.ARROW;
        else if (s.contains("direct")) hontun$icon = HontunIcons.LINK;
        else if (s.contains("add") || s.contains("new") || s.contains("create")) hontun$icon = HontunIcons.PLUS;
        else if (s.contains("edit") || s.contains("rename")) hontun$icon = HontunIcons.PENCIL;
        else if (s.contains("delete") || s.contains("remove")) hontun$icon = HontunIcons.TRASH;
        else if (s.contains("refresh") || s.contains("reload") || s.contains("check")) hontun$icon = HontunIcons.REFRESH;
        else if (s.contains("back") || s.contains("done") || s.contains("cancel")) hontun$icon = HontunIcons.CHEVRON;
        else if (s.contains("microsoft") || s.contains("session") || s.contains("altening") || s.contains("token"))
            hontun$icon = HontunIcons.KEY;
        else if (s.contains("offline") || s.contains("account")) hontun$icon = HontunIcons.PERSON;
        else if (s.contains("proxy") || s.contains("proxies") || s.contains("server") || s.contains("multiplayer"))
            hontun$icon = HontunIcons.GLOBE;
        else if (s.contains("search")) hontun$icon = HontunIcons.SEARCH;
        return hontun$icon;
    }
}
