package cz.honzasik.hontun.mixin.client.button;

import cz.honzasik.hontun.utils.HontunFont;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractWidget.class)
public abstract class WidgetLabelLineMixin {
    @Inject(method = "extractScrollingStringOverContents", at = @At("HEAD"), cancellable = true)
    private void hontun$monoLine(ActiveTextCollector output, Component message, int margin, CallbackInfo ci) {
        AbstractWidget self = (AbstractWidget) (Object) this;
        int h = self.getHeight();
        if ((h & 1) == 0 || !HontunTheme.terminal() || !HontunFont.active()) return;
        int x = self.getX(), y = self.getY();
        output.acceptScrollingWithDefaultCenter(message, x + margin, x + self.getWidth() - margin, y - 1, y + h);
        ci.cancel();
    }
}
