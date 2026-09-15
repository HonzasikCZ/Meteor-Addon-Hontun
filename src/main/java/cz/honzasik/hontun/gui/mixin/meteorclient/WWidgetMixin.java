package cz.honzasik.hontun.gui.mixin.meteorclient;

import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.widget.IWidgetBackport;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = WWidget.class, remap = false)
public abstract class WWidgetMixin implements IWidgetBackport {
    @Redirect(method = "calculateSize", at = @At(value = "INVOKE", target = "Lmeteordevelopment/meteorclient/gui/GuiTheme;scale(D)D"))
    private double hontun$scale(GuiTheme theme, double value) {
        if (GuiThemes.get() instanceof HontunGuiTheme) return value;
        return theme.scale(value);
    }
}
