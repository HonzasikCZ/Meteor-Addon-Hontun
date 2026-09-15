package cz.honzasik.hontun.mixin.client.multiplayer;

import cz.honzasik.hontun.gui.widget.HontunShapes;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractScrollArea.class)
public abstract class ScrollbarStyleMixin {
    @Inject(method = "extractScrollbar", at = @At("HEAD"), cancellable = true)
    private void hontun$themedScrollbar(GuiGraphicsExtractor g, int mouseX, int mouseY, CallbackInfo ci) {
        if (!HontunTheme.modern()) return;
        Object self = this;
        if (!(self instanceof ServerSelectionList list)) return;
        if (!((AbstractScrollArea) self).scrollable()) return;

        AbstractScrollArea area = (AbstractScrollArea) self;
        HontunShapes.scrollbar(g, area.scrollBarX(), list.getY(), list.getBottom(),
                area.scrollbarWidth(), area.scrollBarY(), area.scrollerHeight());
        ci.cancel();
    }
}
