package cz.honzasik.hontun.mixin.network.software;

import cz.honzasik.hontun.utils.ServerSoftware;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class SoftwareCommonMixin {
    @Inject(method = "handleKeepAlive", at = @At("HEAD"))
    private void hontun$keepAlive(ClientboundKeepAlivePacket packet, CallbackInfo ci) {
        ServerSoftware.onKeepAlive(packet.getId());
    }
}
