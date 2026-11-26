package cz.honzasik.hontun.gui.mixin.meteorclient;

import cz.honzasik.hontun.gui.tabs.HontunTab;
import meteordevelopment.meteorclient.gui.GuiThemes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiThemes.class, remap = false)
public abstract class GuiThemesMixin {
    @Inject(method = "saveTheme", at = @At("HEAD"))
    private static void hontun$saveDetached(CallbackInfo ci) {
        HontunTab.saveDetached();
    }

    @Inject(method = "postInit", at = @At("TAIL"))
    private static void hontun$loadDetached(CallbackInfo ci) {
        HontunTab.loadDetached();
    }
}
