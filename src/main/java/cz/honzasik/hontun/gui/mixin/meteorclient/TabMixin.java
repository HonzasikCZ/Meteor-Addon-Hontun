package cz.honzasik.hontun.gui.mixin.meteorclient;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.style.FooterKind;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.gui.tabs.builtin.ConfigTab;
import meteordevelopment.meteorclient.gui.tabs.builtin.GuiTab;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Tab.class, remap = false)
public abstract class TabMixin {
    @WrapOperation(
        method = "openScreen",
        at = @At(
            value = "INVOKE",
            target = "Lmeteordevelopment/meteorclient/gui/tabs/TabScreen;addDirect(Lmeteordevelopment/meteorclient/gui/widgets/WWidget;)Lmeteordevelopment/meteorclient/gui/utils/Cell;"
        )
    )
    private Cell<?> hontun$addTopBarMargin(TabScreen screen, WWidget widget, Operation<Cell<?>> original) {
        Cell<?> cell = original.call(screen, widget);

        if (GuiThemes.get() instanceof HontunGuiTheme hontun) {
            cell.marginTop(hontun.style().metrics().topBarMarginTop);

            if (screen instanceof WindowTabScreen) {
                Object self = this;
                FooterKind kind = self instanceof GuiTab || self instanceof ConfigTab ? FooterKind.MODULE : FooterKind.TAB;
                WWidget footer = hontun.style().footer(hontun, kind);
                if (footer != null) original.call(screen, footer).bottom().expandX();
            }
        }

        return cell;
    }
}
