package cz.honzasik.hontun.mixin.client.titlescreen;

import cz.honzasik.hontun.gui.join.JoinView;
import cz.honzasik.hontun.mixin.client.connect.LevelLoadingScreenAccessor;
import cz.honzasik.hontun.utils.HontunTheme;
import cz.honzasik.hontun.utils.MenuBackground;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import net.minecraft.server.level.progress.ChunkLoadStatusView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelLoadingScreen.class)
public abstract class LevelLoadingBackgroundMixin {
    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void hontun$loadingBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!HontunTheme.restyleEnabled()) return;
        Minecraft mc = Minecraft.getInstance();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        MenuBackground.render(graphics, w, h, true);
        LevelLoadingScreenAccessor acc = (LevelLoadingScreenAccessor) (Object) this;
        LevelLoadTracker tracker = acc.hontun$loadTracker();
        ChunkLoadStatusView view = tracker == null ? null : tracker.statusView();
        JoinView.backdrop(graphics, w, h, JoinView.loadingMode(acc.hontun$reason(), view), view);
        ci.cancel();
    }
}
