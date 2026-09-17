package cz.honzasik.hontun.mixin.network.resourcepack;

import cz.honzasik.hontun.utils.ResourcePackInfo;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ResourcePackPushMixin {
    @Inject(method = "handleResourcePackPush", at = @At("HEAD"))
    private void hontun$captureResourcePack(ClientboundResourcePackPushPacket packet, CallbackInfo ci) {
        ResourcePackInfo.got(packet);
    }
}
