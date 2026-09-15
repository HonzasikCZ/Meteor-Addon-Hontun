package cz.honzasik.hontun.mixin.network.version;

import cz.honzasik.hontun.utils.VersionKeeper;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConfigurationPacketListenerImpl.class)
public abstract class ClientConfigMixin {
    @Inject(method = "handleSelectKnownPacks", at = @At("HEAD"))
    private void hontun$captureVersion(ClientboundSelectKnownPacks packet, CallbackInfo ci) {
        VersionKeeper.gotPacket(packet);
    }
}
