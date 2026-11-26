package cz.honzasik.hontun.mixin.network.serverlog;

import cz.honzasik.hontun.serverlog.HopTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundStartConfigurationPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPacketListener.class, priority = 500)
public abstract class ServerLogPlayMixin extends ClientCommonPacketListenerImpl {
    private ServerLogPlayMixin(Minecraft minecraft, Connection connection, CommonListenerCookie cookie) {
        super(minecraft, connection, cookie);
    }

    @Inject(method = "handleLogin", at = @At("HEAD"))
    private void hontun$logLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
        if (!Minecraft.getInstance().isSameThread()) return;
        try {
            HopTracker.login(packet, connection, serverData);
        } catch (Throwable ignored) {
        }
    }

    @Inject(method = "handleConfigurationStart", at = @At("HEAD"))
    private void hontun$logConfiguration(ClientboundStartConfigurationPacket packet, CallbackInfo ci) {
        if (!Minecraft.getInstance().isSameThread()) return;
        try {
            HopTracker.configurationStart(connection);
        } catch (Throwable ignored) {
        }
    }
}
