package cz.honzasik.hontun.mixin.client.death;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import cz.honzasik.hontun.modules.DeathFreecam;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public abstract class GuiDeathFreecamMixin {
    @WrapOperation(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isDeadOrDying()Z"))
    private boolean hontun$keepDeathScreenClosed(LocalPlayer player, Operation<Boolean> original) {
        return original.call(player) && !DeathFreecam.flying();
    }

    @WrapOperation(method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isDeadOrDying()Z"))
    private boolean hontun$closeDeathScreen(LocalPlayer player, Operation<Boolean> original) {
        return original.call(player) && !DeathFreecam.flying();
    }
}
