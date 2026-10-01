package cz.honzasik.hontun.mixin.client.connect;

import cz.honzasik.hontun.utils.ConnectTracker;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ConnectLifecycleMixin {
    @Inject(method = "added", at = @At("HEAD"))
    private void hontun$attemptOver(CallbackInfo ci) {
        Object self = this;
        if (self instanceof DisconnectedScreen || self instanceof JoinMultiplayerScreen || self instanceof TitleScreen) {
            ConnectTracker.finish();
        }
    }
}
