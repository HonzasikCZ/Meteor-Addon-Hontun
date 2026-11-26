package cz.honzasik.hontun.mixin.network.serverlog;

import cz.honzasik.hontun.serverlog.HopTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, priority = 500)
public abstract class ServerLogSessionMixin {
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V", at = @At("HEAD"))
    private void hontun$logLeave(Screen screen, boolean keepResourcePacks, boolean stopSound, CallbackInfo ci) {
        try {
            HopTracker.disconnect();
        } catch (Throwable ignored) {
        }
    }
}
