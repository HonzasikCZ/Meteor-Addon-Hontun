package cz.honzasik.hontun.mixin.meteor.hud;

import cz.honzasik.hontun.gui.widget.HontunIcons;
import cz.honzasik.hontun.gui.widget.HontunShapes;
import cz.honzasik.hontun.utils.HontunTheme;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.elements.LagNotifierHud;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.world.TickRate;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LagNotifierHud.class, remap = false)
public abstract class LagNotifierStyleMixin {

    private final Color hontun$labelCol = new Color();
    private final Color hontun$valueCol = new Color();

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void hontun$themedRender(HudRenderer renderer, CallbackInfo ci) {
        if (!HontunTheme.restyleEnabled()) return;

        boolean editor = !Utils.canUpdate() || HudEditorScreen.isOpen();
        float since = editor ? 6.40f : TickRate.INSTANCE.getTimeSinceLastTick();

        if (since < 1.1f && !editor) {
            ci.cancel();
            return;
        }

        HudElement el = (HudElement) (Object) this;
        double scale = Hud.get().getTextScale();
        int ms = Math.round(since * 1000f);

        int state = since >= 4f ? HontunTheme.red() : HontunTheme.yellow();
        int accent = HontunTheme.accent();

        String label = "Not responding ";
        String value = String.format("%.2fs / %dms", since, ms);

        GuiGraphicsExtractor g = renderer.graphics;

        double th = renderer.textHeight(true, scale);
        double labelW = renderer.textWidth(label, true, scale);
        double valueW = renderer.textWidth(value, true, scale);

        if (HontunTheme.modern2()) {
            hontun$modern2(renderer, g, el, scale, th, labelW, valueW, label, value, state, accent);
        } else if (HontunTheme.modern1()) {
            hontun$modern1(renderer, g, el, scale, th, labelW, valueW, label, value, state, accent);
        } else {
            hontun$vanilla(renderer, g, el, scale, th, labelW, valueW, label, value, state, accent);
        }

        ci.cancel();
    }

    private void hontun$modern2(HudRenderer r, GuiGraphicsExtractor g, HudElement el, double scale, double th,
                                double labelW, double valueW, String label, String value, int state, int accent) {
        int icon = (int) Math.round(HontunIcons.SIZE * scale);
        int pad = (int) Math.round(6 * scale);
        int lead = (int) Math.round(7 * scale);
        int gap = (int) Math.round(5 * scale);

        int w = lead + icon + gap + (int) Math.ceil(labelW + valueW) + pad;
        int h = (int) Math.ceil(Math.max(th, icon)) + pad;
        el.setSize(w, h);
        int x = el.getX(), y = el.getY();
        int cut = Math.min(5, Math.min(w, h) / 3);

        HontunShapes.glow(g, x, y, w, h, cut, state, 2, 0x55);
        HontunShapes.fillClipped(g, x, y, w, h, cut, cut, HontunTheme.argb(0xE4, HontunTheme.surface0()));
        HontunShapes.outlineClipped(g, x, y, w, h, cut, cut, HontunTheme.argb(0xC0, accent));
        HontunShapes.leftBarClipped(g, x, y, h, cut, HontunTheme.argb(0xFF, state), HontunTheme.argb(0xB0, state));
        HontunShapes.brackets(g, x, y, w, h, (int) Math.round(7 * scale), HontunTheme.argb(0xFF, HontunTheme.accentHi()));
        g.fill(x + cut, y + h - 2, x + w - cut, y + h - 1, HontunTheme.argb(0xFF, accent));

        int iconX = x + lead;
        int iconY = y + (h - icon) / 2;
        hontun$icon(g, iconX, iconY, scale, state);

        double tx = iconX + icon + gap;
        double ty = y + (h - th) / 2.0;
        double after = r.text(label, tx, ty, hontun$col(hontun$labelCol, HontunTheme.textLight()), true, scale);
        r.text(value, after, ty, hontun$col(hontun$valueCol, state), true, scale);
    }

    private void hontun$modern1(HudRenderer r, GuiGraphicsExtractor g, HudElement el, double scale, double th,
                                double labelW, double valueW, String label, String value, int state, int accent) {
        int icon = (int) Math.round(HontunIcons.SIZE * scale);
        int pad = (int) Math.round(6 * scale);
        int lead = (int) Math.round(6 * scale);
        int gap = (int) Math.round(5 * scale);

        int w = lead + icon + gap + (int) Math.ceil(labelW + valueW) + pad;
        int h = (int) Math.ceil(Math.max(th, icon)) + pad;
        el.setSize(w, h);
        int x = el.getX(), y = el.getY();

        g.fillGradient(x, y, x + w, y + h,
                HontunTheme.argb(0xE0, HontunTheme.lighten(HontunTheme.surface1(), 1.05f)),
                HontunTheme.argb(0xE0, HontunTheme.surface0()));
        g.fill(x, y, x + 2, y + h, HontunTheme.argb(0xFF, state));
        g.fillGradient(x + 2, y + h - 2, x + w, y + h,
                HontunTheme.argb(0xFF, accent), HontunTheme.argb(0xFF, HontunTheme.accentHi()));
        g.fill(x, y, x + w, y + 1, HontunTheme.argb(0x30, HontunTheme.overlay2()));

        int iconX = x + lead;
        int iconY = y + (h - icon) / 2;
        hontun$icon(g, iconX, iconY, scale, state);

        double tx = iconX + icon + gap;
        double ty = y + (h - th) / 2.0;
        double after = r.text(label, tx, ty, hontun$col(hontun$labelCol, HontunTheme.textLight()), true, scale);
        r.text(value, after, ty, hontun$col(hontun$valueCol, state), true, scale);
    }

    private void hontun$vanilla(HudRenderer r, GuiGraphicsExtractor g, HudElement el, double scale, double th,
                                double labelW, double valueW, String label, String value, int state, int accent) {
        int icon = (int) Math.round(HontunIcons.SIZE * scale);
        int pad = (int) Math.round(6 * scale);
        int lead = (int) Math.round(6 * scale);
        int gap = (int) Math.round(5 * scale);

        int w = lead + icon + gap + (int) Math.ceil(labelW + valueW) + pad;
        int h = (int) Math.ceil(Math.max(th, icon)) + pad;
        el.setSize(w, h);
        int x = el.getX(), y = el.getY();
        int x2 = x + w, y2 = y + h;

        g.fill(x, y, x2, y2, HontunTheme.argb(0xA8, HontunTheme.surface1()));
        int b = HontunTheme.argb(0xC0, accent);
        g.fill(x, y, x2, y + 1, b);
        g.fill(x, y2 - 1, x2, y2, b);
        g.fill(x, y, x + 1, y2, b);
        g.fill(x2 - 1, y, x2, y2, b);
        int inner = HontunTheme.argb(0x50, HontunTheme.crust());
        g.fill(x + 1, y + 1, x2 - 1, y + 2, inner);
        g.fill(x + 1, y2 - 2, x2 - 1, y2 - 1, inner);
        g.fill(x, y, x + 2, y2, HontunTheme.argb(0xFF, state));

        int iconX = x + lead;
        int iconY = y + (h - icon) / 2;
        hontun$icon(g, iconX, iconY, scale, state);

        double tx = iconX + icon + gap;
        double ty = y + (h - th) / 2.0;
        double after = r.text(label, tx, ty, hontun$col(hontun$labelCol, HontunTheme.textLight()), true, scale);
        r.text(value, after, ty, hontun$col(hontun$valueCol, state), true, scale);
    }

    private void hontun$icon(GuiGraphicsExtractor g, int x, int y, double scale, int state) {
        int argb = HontunTheme.argb(0xFF, state);
        if (scale <= 1.01) {
            HontunIcons.draw(g, HontunIcons.WARNING, x, y, argb);
            return;
        }
        int s = (int) Math.round(scale);
        for (int dy = 0; dy < HontunIcons.SIZE; dy++) {
            for (int dx = 0; dx < HontunIcons.SIZE; dx++) {
                if (hontun$warnPixel(dx, dy)) {
                    g.fill(x + dx * s, y + dy * s, x + dx * s + s, y + dy * s + s, argb);
                }
            }
        }
    }

    private boolean hontun$warnPixel(int dx, int dy) {
        boolean stem = dx >= 3 && dx < 5 && dy < 5;
        boolean dot = dx >= 3 && dx < 5 && dy >= 6 && dy < 8;
        return stem || dot;
    }

    private Color hontun$col(Color c, int rgb) {
        return c.set((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, 255);
    }
}
