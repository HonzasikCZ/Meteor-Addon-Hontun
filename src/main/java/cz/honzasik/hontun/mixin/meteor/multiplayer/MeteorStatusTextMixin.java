package cz.honzasik.hontun.mixin.meteor.multiplayer;

import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = JoinMultiplayerScreen.class, priority = 1500)
public abstract class MeteorStatusTextMixin {
    @Redirect(
            method = "extractRenderState",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"),
            require = 0)
    private void hontun$hideMeteorStatus(GuiGraphicsExtractor g, Font font, String text, int x, int y, int color) {
        if (HontunTheme.restyleEnabled()) return;
        g.text(font, text, x, y, color);
    }
}
