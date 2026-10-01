package cz.honzasik.hontun.mixin.client.connect;

import cz.honzasik.hontun.gui.join.JoinView;
import cz.honzasik.hontun.utils.ConnectTracker;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenInfoMixin {
    @Inject(method = "startConnecting(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/resolver/ServerAddress;Lnet/minecraft/client/multiplayer/ServerData;ZLnet/minecraft/client/multiplayer/TransferState;)V",
            at = @At("HEAD"))
    private static void hontun$seedTracker(Screen parent, Minecraft minecraft, ServerAddress hostAndPort,
                                           ServerData data, boolean isQuickPlay, TransferState transferState,
                                           CallbackInfo ci) {
        if (minecraft.gui.screen() instanceof ConnectScreen) return;

        String addr = transferState == null && data != null && data.ip != null && !data.ip.isEmpty()
                ? data.ip
                : hostAndPort.getHost() + (hostAndPort.getPort() == 25565 ? "" : ":" + hostAndPort.getPort());

        ConnectTracker.reset(addr, Component.translatable(
                transferState != null ? "connect.transferring" : "connect.connecting"));
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void hontun$bindTracker(CallbackInfo ci) {
        ConnectTracker.bind(this);
    }

    @Inject(method = "updateStatus", at = @At("HEAD"))
    private void hontun$recordStep(Component status, CallbackInfo ci) {
        ConnectTracker.step(this, status);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void hontun$placeCancel(CallbackInfo ci) {
        if (!HontunTheme.restyleEnabled()) return;
        Screen self = (Screen) (Object) this;
        Button cancel = null;
        int buttons = 0;
        for (GuiEventListener child : self.children()) {
            if (!(child instanceof Button b)) continue;
            buttons++;
            if (cancel == null || hontun$isCancel(b)) cancel = b;
        }
        if (cancel == null || (buttons > 1 && !hontun$isCancel(cancel))) return;
        int[] r = JoinView.cancelBounds(self.width, self.height);
        cancel.setRectangle(r[2], r[3], r[0], r[1]);
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
        JoinView.render(g, self, self.width, self.height, JoinView.Mode.CONNECT, 0f, false, null, null);
    }

    @Unique
    private static boolean hontun$isCancel(Button b) {
        return CommonComponents.GUI_CANCEL.getString().equals(b.getMessage().getString());
    }
}
