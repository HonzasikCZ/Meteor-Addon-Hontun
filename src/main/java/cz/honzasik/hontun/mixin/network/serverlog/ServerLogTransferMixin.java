package cz.honzasik.hontun.mixin.network.serverlog;

import cz.honzasik.hontun.serverlog.HopTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientCommonPacketListenerImpl.class, priority = 500)
public abstract class ServerLogTransferMixin {
    @Shadow @Final protected ServerData serverData;

    @Inject(method = "handleTransfer", at = @At("HEAD"))
    private void hontun$logTransfer(ClientboundTransferPacket packet, CallbackInfo ci) {
        if (!Minecraft.getInstance().isSameThread()) return;
        try {
            HopTracker.transfer(packet.host(), packet.port(), serverData);
        } catch (Throwable ignored) {
        }
    }
}
