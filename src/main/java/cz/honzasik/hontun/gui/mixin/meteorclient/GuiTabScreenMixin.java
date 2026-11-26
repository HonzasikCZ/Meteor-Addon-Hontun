package cz.honzasik.hontun.gui.mixin.meteorclient;

import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.style.terminal.TerminalClickStyle;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import meteordevelopment.meteorclient.settings.Settings;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(targets = "meteordevelopment.meteorclient.gui.tabs.builtin.GuiTab$GuiScreen", remap = false)
public abstract class GuiTabScreenMixin extends WindowTabScreen {
    protected GuiTabScreenMixin(GuiTheme theme, Tab tab) {
        super(theme, tab);
    }

    @ModifyArg(method = "initWidgets", at = @At(value = "INVOKE", target = "Lmeteordevelopment/meteorclient/gui/GuiTheme;settings(Lmeteordevelopment/meteorclient/settings/Settings;)Lmeteordevelopment/meteorclient/gui/widgets/WWidget;"), index = 0)
    private Settings hontun$guiOnly(Settings settings) {
        return theme instanceof HontunGuiTheme hontun && settings == hontun.settings ? hontun.guiSettings : settings;
    }

    @Inject(method = "initWidgets", at = @At("TAIL"))
    private void hontun$dropTopRow(CallbackInfo ci) {
        if (!(theme instanceof HontunGuiTheme hontun) || !(hontun.style() instanceof TerminalClickStyle)) return;
        if (window == null || window.view == null) return;

        List<Cell<?>> cells = window.view.cells;
        if (cells.isEmpty() || !(cells.getFirst().widget() instanceof WHorizontalList)) return;

        cells.removeFirst();
        window.view.invalidate();
    }
}
