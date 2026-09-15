package cz.honzasik.hontun.mixin.client.connect;

import cz.honzasik.hontun.gui.widget.HontunCards;
import cz.honzasik.hontun.utils.ConnectTracker;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenInfoMixin {
    @Inject(method = "startConnecting(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/resolver/ServerAddress;Lnet/minecraft/client/multiplayer/ServerData;ZLnet/minecraft/client/multiplayer/TransferState;)V",
            at = @At("HEAD"))
    private static void hontun$seedTracker(Screen parent, Minecraft minecraft, ServerAddress hostAndPort,
                                           ServerData data, boolean isQuickPlay, TransferState transferState,
                                           CallbackInfo ci) {
        if (minecraft.gui.screen() instanceof ConnectScreen) return;

        String addr = (data != null && data.ip != null && !data.ip.isEmpty())
                ? data.ip
                : hostAndPort.getHost() + (hostAndPort.getPort() == 25565 ? "" : ":" + hostAndPort.getPort());

        String first = Component.translatable(
                transferState != null ? "connect.transferring" : "connect.connecting").getString();

        ConnectTracker.reset(addr, first);
    }

    @Inject(method = "updateStatus", at = @At("HEAD"))
    private void hontun$recordStep(Component status, CallbackInfo ci) {
        if (status != null) ConnectTracker.step(status.getString());
    }

    @Redirect(method = "extractRenderState",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;centeredText(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"))
    private void hontun$dropVanillaStatus(GuiGraphicsExtractor g, Font f, Component text, int x, int y, int color) {
        if (!HontunTheme.restyleEnabled()) g.centeredText(f, text, x, y, color);
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void hontun$drawSteps(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!HontunTheme.restyleEnabled()) return;

        Screen self = (Screen) (Object) this;
        Font f = Minecraft.getInstance().font;
        List<ConnectTracker.Step> steps = ConnectTracker.steps();
        if (steps.isEmpty()) return;

        int lh = f.lineHeight + 3;
        int pw = 260;
        int ph = 10 + lh * (steps.size() + 1) + 8;
        int px = self.width / 2 - pw / 2;

        int cancelY = self.height / 4 + 132;
        int py = Math.max(8, cancelY - ph - 8);

        HontunCards.surface(g, px, py, pw, ph, HontunTheme.surface0(), 0xE6);

        int y = py + 7;

        String head = ConnectTracker.target().isEmpty() ? "Connecting" : ConnectTracker.target();
        g.text(f, Component.literal(head), px + 8, y, HontunTheme.argb(0xFF, HontunTheme.textLight()), false);
        String total = hontun$fmt(ConnectTracker.totalMs());
        g.text(f, Component.literal(total), px + pw - 8 - f.width(total), y,
                HontunTheme.argb(0xFF, HontunTheme.textDim()), false);
        y += lh + 2;

        for (int i = 0; i < steps.size(); i++) {
            boolean current = i == steps.size() - 1;
            String mark = current ? "▶ " : "✓ ";
            int col = current ? HontunTheme.accentHi() : HontunTheme.green();
            g.text(f, Component.literal(mark + steps.get(i).name()), px + 8, y,
                    HontunTheme.argb(0xFF, col), false);
            String ms = hontun$fmt(ConnectTracker.durationOf(i));
            g.text(f, Component.literal(ms), px + pw - 8 - f.width(ms), y,
                    HontunTheme.argb(0xFF, HontunTheme.textDim()), false);
            y += lh;
        }
    }

    @Unique
    private static String hontun$fmt(long ms) {
        if (ms < 1000) return ms + " ms";
        return String.format(java.util.Locale.ROOT, "%.1f s", ms / 1000.0);
    }
}
