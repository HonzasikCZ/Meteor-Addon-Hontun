package cz.honzasik.hontun.mixin.client.titlescreen;

import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.gui.render.MatrixRain;
import cz.honzasik.hontun.utils.HontunTheme;
import cz.honzasik.hontun.utils.MenuBackground;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WidgetScreen;
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
    @Shadow public abstract boolean isInGameUi();

    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void hontun$menuBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        boolean capture = MenuBackground.forceOnServer;

        if (mc.level != null && !capture) return;
        Object self = this;
        if (self instanceof TitleScreen || self instanceof PauseScreen) return;

        if (HontunTheme.restyleEnabled()) {
            boolean connect = self instanceof net.minecraft.client.gui.screens.ConnectScreen;
            int w = mc.getWindow().getGuiScaledWidth();
            int h = mc.getWindow().getGuiScaledHeight();
            MenuBackground.render(graphics, w, h, true, true);
            if (connect) {
                cz.honzasik.hontun.gui.join.JoinView.backdrop(graphics, w, h,
                        cz.honzasik.hontun.gui.join.JoinView.Mode.CONNECT, null);
            }
            ci.cancel();
        } else if (capture) {
            extractPanorama(graphics, delta);
            extractMenuBackground(graphics);
            ci.cancel();
        }
    }

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void hontun$worldMenuBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || isInGameUi()) return;
        MenuBackground.render(graphics, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), false);
    }

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("TAIL"))
    private void hontun$scanlines(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!MatrixRain.scanlines()) return;

        Object self = this;
        boolean terminal = Minecraft.getInstance().level == null && (self instanceof WidgetScreen
                ? Hontun.THEME != null && GuiThemes.get() == Hontun.THEME && Hontun.THEME.style().id() == HontunTheme.UiMode.Terminal
                : HontunTheme.terminal());

        if (terminal) MatrixRain.renderScanlines(graphics);
    }
}
