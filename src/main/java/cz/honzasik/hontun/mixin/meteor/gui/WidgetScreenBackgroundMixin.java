package cz.honzasik.hontun.mixin.meteor.gui;

import cz.honzasik.hontun.utils.HontunTheme;
import cz.honzasik.hontun.utils.MenuBackground;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WidgetScreen.class, remap = false)
public abstract class WidgetScreenBackgroundMixin {
    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void hontun$menuBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (!HontunTheme.restyleEnabled()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) return;
        MenuBackground.render(graphics, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), true);
        ci.cancel();
    }
}
