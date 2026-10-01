package cz.honzasik.hontun.mixin.client.connect;

import cz.honzasik.hontun.gui.join.JoinView;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.progress.ChunkLoadStatusView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelLoadingScreen.class)
public abstract class LevelLoadingScreenViewMixin {
    @Redirect(method = "extractRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;centeredText(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"))
    private void hontun$dropText(GuiGraphicsExtractor g, Font f, Component text, int x, int y, int color) {
        if (!HontunTheme.restyleEnabled()) g.centeredText(f, text, x, y, color);
    }

    @Redirect(method = "extractRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/LevelLoadingScreen;extractChunksForRendering(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIILnet/minecraft/server/level/progress/ChunkLoadStatusView;)V"))
    private void hontun$dropChunks(GuiGraphicsExtractor g, int xCenter, int yCenter, int size, int margin, ChunkLoadStatusView view) {
        if (!HontunTheme.restyleEnabled()) LevelLoadingScreen.extractChunksForRendering(g, xCenter, yCenter, size, margin, view);
    }

    @Inject(method = "drawProgressBar", at = @At("HEAD"), cancellable = true)
    private void hontun$dropBar(GuiGraphicsExtractor g, int left, int top, int width, int height, float progress, CallbackInfo ci) {
        if (HontunTheme.restyleEnabled()) ci.cancel();
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void hontun$drawView(GuiGraphicsExtractor g, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (!HontunTheme.restyleEnabled()) return;
        Screen self = (Screen) (Object) this;
        LevelLoadingScreenAccessor acc = (LevelLoadingScreenAccessor) (Object) this;
        LevelLoadTracker tracker = acc.hontun$loadTracker();
        LevelLoadingScreen.Reason reason = acc.hontun$reason();
        ChunkLoadStatusView view = tracker == null ? null : tracker.statusView();
        JoinView.Mode mode = JoinView.loadingMode(reason, view);
        boolean has = tracker != null && tracker.hasProgress();
        JoinView.render(g, self, self.width, self.height, mode, has ? tracker.serverProgress() : 0f, has, reason, view);
    }
}
