package cz.honzasik.hontun.mixin.client.titlescreen;

import cz.honzasik.hontun.utils.HontunTheme;
import cz.honzasik.hontun.utils.MenuBackground;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class MenuScreenBackgroundMixin {
    @Shadow protected abstract void extractPanorama(GuiGraphicsExtractor g, float delta);
    @Shadow protected abstract void extractMenuBackground(GuiGraphicsExtractor g);

    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void hontun$menuBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        boolean capture = MenuBackground.forceOnServer;

        // Normally this only takes over at the main menu (no world). While the local screenshot
        // tool is capturing it also takes over on a server, so the shots show the menu background
        // instead of the live game world sitting behind each screen.
        if (mc.level != null && !capture) return;
        Object self = this;
        if (self instanceof TitleScreen || self instanceof PauseScreen) return;

        if (HontunTheme.restyleEnabled()) {
            boolean particles = !(self instanceof net.minecraft.client.gui.screens.ConnectScreen);
            MenuBackground.render(graphics, mc.getWindow().getGuiScaledWidth(),
                    mc.getWindow().getGuiScaledHeight(), true, particles);
            ci.cancel();
        } else if (capture) {
            // Vanilla UI mode during a capture on a server: draw Minecraft's own no-world
            // background (panorama + dirt) so the shot matches the plain main-menu look.
            extractPanorama(graphics, delta);
            extractMenuBackground(graphics);
            ci.cancel();
        }
    }
}
