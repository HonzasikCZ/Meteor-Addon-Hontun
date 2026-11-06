package cz.honzasik.hontun.mixin.client.button;

import cz.honzasik.hontun.utils.HontunFont;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StringWidget.class)
public abstract class StringWidgetMonoMixin extends AbstractStringWidget {
    protected StringWidgetMonoMixin(int x, int y, int width, int height, Component message, Font font) {
        super(x, y, width, height, message, font);
    }

    @Inject(method = "<init>(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/Font;)V", at = @At("RETURN"))
    private void hontun$monoInit(Component message, Font font, CallbackInfo ci) {
        hontun$fitMono();
    }

    @Inject(method = "setMessage", at = @At("TAIL"))
    private void hontun$monoMessage(Component message, CallbackInfo ci) {
        hontun$fitMono();
    }

    @Unique
    private void hontun$fitMono() {
        if (!HontunTheme.terminal() || !HontunFont.active()) return;
        setWidth(getFont().width(getMessage().getVisualOrderText()));
    }
}
