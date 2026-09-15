package cz.honzasik.hontun.mixin.client.container;

import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ContainerScreenBackgroundMixin {
    @Inject(method = "extractTransparentBackground", at = @At("HEAD"), cancellable = true)
    private void hontun$containerBg(GuiGraphicsExtractor g, CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (!HontunTheme.restyleEnabled() || !(self instanceof AbstractContainerScreen)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        int c = HontunTheme.bgOverlay();
        int a = (c >>> 24) & 0xFF;
        int rgb = c & 0xFFFFFF;

        if (HontunTheme.modern()) {
            int topA = Math.max(0, a - 40);
            g.fillGradient(0, 0, w, h,
                    HontunTheme.argb(topA, HontunTheme.lighten(rgb, 1.10f)),
                    HontunTheme.argb(a, rgb));
        } else {
            g.fill(0, 0, w, h, HontunTheme.argb(a, rgb));
        }
        ci.cancel();
    }
}
