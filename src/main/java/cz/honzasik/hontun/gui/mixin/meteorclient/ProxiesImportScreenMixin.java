package cz.honzasik.hontun.gui.mixin.meteorclient;

import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.screens.ProxiesImportScreen;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.utils.render.color.Color;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ProxiesImportScreen.class, remap = false)
public abstract class ProxiesImportScreenMixin {
    @Redirect(
            method = "initWidgets",
            at = @At(value = "INVOKE", target = "Lmeteordevelopment/meteorclient/gui/widgets/WLabel;color(Lmeteordevelopment/meteorclient/utils/render/color/Color;)Lmeteordevelopment/meteorclient/gui/widgets/WLabel;")
    )
    private WLabel hontun$color(WLabel label, Color color) {
        if (!(GuiThemes.get() instanceof HontunGuiTheme theme) || !theme.light()) return label.color(color);
        if (color == Color.GREEN) return label.color(theme.greenColor());
        if (color == Color.RED) return label.color(theme.redColor());
        if (color == Color.ORANGE) return label.color(theme.yellowColor());
        return label.color(color);
    }
}
