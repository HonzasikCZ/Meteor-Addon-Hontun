package cz.honzasik.hontun.mixin.client.death;

import cz.honzasik.hontun.mixin.client.multiplayer.ScreenInvoker;
import cz.honzasik.hontun.modules.DeathFreecam;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public abstract class DeathScreenFreecamButtonMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void hontun$deathFreecamButton(CallbackInfo ci) {
        DeathFreecam module = (DeathFreecam) Modules.get().get(DeathFreecam.class);
        if (module == null || !module.isActive()) return;
        Screen self = (Screen) (Object) this;
        module.setDeathScreen(self);
        Button button = Button.builder(Component.literal("Freecam"), b -> module.enterFreecam())
            .bounds(self.width / 2 - 100, self.height / 4 + 120, 200, 20).build();
        ((ScreenInvoker) self).hontun$addRenderableWidget(button);
        if (module.takeAuto()) Minecraft.getInstance().execute(module::enterFreecam);
    }
}
