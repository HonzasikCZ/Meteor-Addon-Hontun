package cz.honzasik.hontun.mixin.client.button;

import cz.honzasik.hontun.utils.HontunFont;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetLabelMixin {
    @Inject(method = "getMessage", at = @At("RETURN"), cancellable = true)
    private void hontun$smogFont(CallbackInfoReturnable<Component> cir) {
        if (!HontunTheme.smog()) return;
        Component msg = cir.getReturnValue();
        if (msg != null) cir.setReturnValue(HontunFont.apply(msg));
    }
}
