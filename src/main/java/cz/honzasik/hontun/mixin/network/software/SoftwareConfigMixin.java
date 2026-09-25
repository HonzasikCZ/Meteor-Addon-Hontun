package cz.honzasik.hontun.mixin.network.software;

import cz.honzasik.hontun.utils.ServerSoftware;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConfigurationPacketListenerImpl.class)
public abstract class SoftwareConfigMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void hontun$reset(CallbackInfo ci) {
        ServerSoftware.reset();
    }

    @Inject(method = "handleRegistryData", at = @At("HEAD"))
    private void hontun$registry(ClientboundRegistryDataPacket packet, CallbackInfo ci) {
        ServerSoftware.onRegistry(packet);
    }
}
