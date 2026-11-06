package cz.honzasik.hontun.mixin.client.multiplayer;

import cz.honzasik.hontun.gui.widget.TerminalWidgets;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSelectionList.class)
public abstract class SelectionOutlineMixin {
    @Inject(method = "extractSelection", at = @At("HEAD"), cancellable = true)
    private void hontun$noOutline(GuiGraphicsExtractor g, AbstractSelectionList.Entry<?> entry,
                                  int outlineColor, CallbackInfo ci) {
        if ((Object) this instanceof ServerSelectionList) {
            ci.cancel();
            return;
        }
        if (HontunTheme.terminal()) {
            TerminalWidgets.row(g, entry.getX(), entry.getY(), entry.getWidth(), entry.getHeight(), true, false);
            ci.cancel();
        }
    }

    @Inject(method = "extractListBackground", at = @At("HEAD"), cancellable = true)
    private void hontun$terminalBackground(GuiGraphicsExtractor g, CallbackInfo ci) {
        if (!HontunTheme.terminal()) return;
        AbstractSelectionList<?> self = (AbstractSelectionList<?>) (Object) this;
        AbstractScrollArea area = self;
        int rowLeft = self.getRowLeft(), rowRight = self.getRowRight();
        int pad = Math.max(TerminalWidgets.markerSpace() + 3, area.scrollBarX() + area.scrollbarWidth() + 2 - rowRight);
        int left = Math.max(self.getX(), rowLeft - pad);
        int right = Math.min(self.getRight(), rowRight + pad);
        TerminalWidgets.listColumn(g, left, self.getY(), right - left, self.getHeight());
        ci.cancel();
    }

    @Inject(method = "extractListSeparators", at = @At("HEAD"), cancellable = true)
    private void hontun$terminalSeparators(GuiGraphicsExtractor g, CallbackInfo ci) {
        if (!HontunTheme.terminal()) return;
        AbstractSelectionList<?> self = (AbstractSelectionList<?>) (Object) this;
        TerminalWidgets.listSeparators(g, self.getX(), self.getY(), self.getBottom(), self.getWidth());
        ci.cancel();
    }
}
