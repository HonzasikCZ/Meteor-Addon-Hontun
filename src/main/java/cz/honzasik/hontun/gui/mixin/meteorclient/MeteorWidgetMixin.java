package cz.honzasik.hontun.gui.mixin.meteorclient;

import cz.honzasik.hontun.gui.api.render.RoundedRect;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import meteordevelopment.meteorclient.utils.render.color.Color;

@Mixin(value = MeteorWidget.class, remap = false)
public interface MeteorWidgetMixin {
    @Inject(
        method = "renderBackground(" +
                 "Lmeteordevelopment/meteorclient/gui/renderer/GuiRenderer;" +
                 "Lmeteordevelopment/meteorclient/gui/widgets/WWidget;" +
                 "Lmeteordevelopment/meteorclient/utils/render/color/Color;" +
                 "Lmeteordevelopment/meteorclient/utils/render/color/Color;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hontun$renderBackground(GuiRenderer renderer, WWidget widget, Color outlineColor, Color backgroundColor, CallbackInfo ci) {
        if (!(GuiThemes.get() instanceof HontunGuiTheme theme)) return;

        RoundedRect.get()
                   .bounds(widget)
                   .radius(theme.effSmallCornerRadius())
                   .color(backgroundColor)
                   .outline(outlineColor, 2f)
                   .render();

        ci.cancel();
    }

    @Inject(
        method = "renderBackground(" +
                 "Lmeteordevelopment/meteorclient/gui/renderer/GuiRenderer;" +
                 "Lmeteordevelopment/meteorclient/gui/widgets/WWidget;ZZ)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hontun$renderBackground(GuiRenderer renderer, WWidget widget, boolean pressed, boolean mouseOver, CallbackInfo ci) {
        if (!(GuiThemes.get() instanceof HontunGuiTheme theme)) return;

        RoundedRect.get()
                   .bounds(widget)
                   .radius(theme.effSmallCornerRadius())
                   .color(theme.backgroundColor.get(pressed, mouseOver))
                   .outline(theme.outlineColor.get(pressed, mouseOver), 2f)
                   .render();

        ci.cancel();
    }
}
