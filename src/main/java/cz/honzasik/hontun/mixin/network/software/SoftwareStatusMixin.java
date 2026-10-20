package cz.honzasik.hontun.mixin.network.software;

import cz.honzasik.hontun.utils.ServerSoftware;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.multiplayer.ServerStatusPinger$1")
public abstract class SoftwareStatusMixin {
    @Shadow
    @Final
    ServerData val$data;

    @Inject(method = "handleStatusResponse", at = @At("HEAD"))
    private void hontun$status(ClientboundStatusResponsePacket packet, CallbackInfo ci) {
        ServerSoftware.onStatus(val$data, packet.status());
    }
}
