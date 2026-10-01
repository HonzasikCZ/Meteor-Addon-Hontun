package cz.honzasik.hontun.mixin.client.connect;

import cz.honzasik.hontun.utils.ConnectTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiInGameMixin {
    @Inject(method = "setScreen", at = @At("HEAD"))
    private void hontun$inGame(Screen screen, CallbackInfo ci) {
        if (screen == null && Minecraft.getInstance().level != null) ConnectTracker.finish();
    }
}
