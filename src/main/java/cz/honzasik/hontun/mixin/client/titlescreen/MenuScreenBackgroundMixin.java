package cz.honzasik.hontun.mixin.client.titlescreen;

import cz.honzasik.hontun.utils.HontunTheme;
import cz.honzasik.hontun.utils.MenuBackground;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class MenuScreenBackgroundMixin {
    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void hontun$menuBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!HontunTheme.restyleEnabled()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) return;
        Object self = this;
        if (self instanceof TitleScreen || self instanceof PauseScreen) return;

        boolean particles = !(self instanceof net.minecraft.client.gui.screens.ConnectScreen);
        MenuBackground.render(graphics, mc.getWindow().getGuiScaledWidth(),
                mc.getWindow().getGuiScaledHeight(), true, particles);
        ci.cancel();
    }
}
