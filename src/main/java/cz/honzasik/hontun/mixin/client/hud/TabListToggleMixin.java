package cz.honzasik.hontun.mixin.client.hud;

import cz.honzasik.hontun.commands.ToggleTab;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Hud.class)
public abstract class TabListToggleMixin {
    @Redirect(method = "extractTabList",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"))
    private boolean hontun$forceTabList(KeyMapping key) {
        return key.isDown() || ToggleTab.shown;
    }
}
