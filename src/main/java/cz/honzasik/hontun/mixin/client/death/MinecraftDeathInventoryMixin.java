package cz.honzasik.hontun.mixin.client.death;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import cz.honzasik.hontun.modules.DeathFreecam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public abstract class MinecraftDeathInventoryMixin {
    @WrapWithCondition(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    private boolean hontun$noInventoryWhileDead(Gui gui, Screen screen) {
        return !(screen instanceof InventoryScreen) || !DeathFreecam.deadFreecam();
    }

    @WrapWithCondition(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;sendOpenInventory()V"))
    private boolean hontun$noServerInventoryWhileDead(LocalPlayer player) {
        return !DeathFreecam.deadFreecam();
    }
}
