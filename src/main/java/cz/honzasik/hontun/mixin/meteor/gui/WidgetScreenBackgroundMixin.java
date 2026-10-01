package cz.honzasik.hontun.mixin.meteor.gui;

import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.style.ClickStyle;
import cz.honzasik.hontun.utils.MenuBackground;
import cz.honzasik.hontun.utils.SmogBackground;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WidgetScreen.class, remap = false)
public abstract class WidgetScreenBackgroundMixin {
    @Shadow @Final protected GuiTheme theme;

    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void hontun$menuBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        HontunGuiTheme hontun = theme instanceof HontunGuiTheme h ? h : Hontun.THEME;
        if (hontun == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) return;

        ClickStyle style = hontun.style();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        switch (style.backdrop()) {
            case VANILLA -> {
                return;
            }
            case SMOG -> SmogBackground.render(graphics, width, height, true, true);
            default -> MenuBackground.render(graphics, width, height, true, true, style.id());
        }

        ci.cancel();
    }
}
