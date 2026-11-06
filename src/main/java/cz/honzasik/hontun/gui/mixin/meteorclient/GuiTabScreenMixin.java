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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(targets = "meteordevelopment.meteorclient.gui.tabs.builtin.GuiTab$GuiScreen", remap = false)
public abstract class GuiTabScreenMixin extends WindowTabScreen {
    protected GuiTabScreenMixin(GuiTheme theme, Tab tab) {
        super(theme, tab);
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
