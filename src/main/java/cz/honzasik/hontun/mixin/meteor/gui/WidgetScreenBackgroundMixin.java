package cz.honzasik.hontun.mixin.meteor.gui;

import com.mojang.blaze3d.platform.InputConstants;
import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.gui.screen.HontunSearchScreen;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.style.Backdrop;
import cz.honzasik.hontun.gui.theme.style.ClickStyle;
import cz.honzasik.hontun.gui.theme.style.terminal.TerminalClickStyle;
import cz.honzasik.hontun.utils.MenuBackground;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

        if (style.backdrop() == Backdrop.VANILLA) return;

        MenuBackground.render(graphics, width, height, true, true, style.id());
        ci.cancel();
    }

    @Inject(
        method = "keyPressed",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z"),
        cancellable = true
    )
    private void hontun$terminalTabKeys(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
        if (!(theme instanceof HontunGuiTheme hontun) || !(hontun.style() instanceof TerminalClickStyle)) return;
        if (!((Object) this instanceof WindowTabScreen)) return;

        int key = input.key();

        if (key == InputConstants.KEY_ESCAPE) {
            if (Tabs.get().isEmpty()) return;
            Tabs.get().getFirst().openScreen(theme);
            cir.setReturnValue(true);
            return;
        }

        if (key != InputConstants.KEY_F || !hontun.hontunSearchScreen.get()) return;

        int mods = input.modifiers() & (InputConstants.MOD_SHIFT | InputConstants.MOD_CONTROL | InputConstants.MOD_ALT | InputConstants.MOD_SUPER);
        int wanted = Util.getPlatform() == Util.OS.OSX ? InputConstants.MOD_SUPER : InputConstants.MOD_CONTROL;
        if (mods == 0 || (mods & ~wanted) != 0) return;

        Minecraft.getInstance().gui.setScreen(new HontunSearchScreen(hontun));
        cir.setReturnValue(true);
    }
}
