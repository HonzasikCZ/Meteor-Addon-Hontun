package cz.honzasik.hontun.mixin.network.resourcepack;

import cz.honzasik.hontun.utils.ResourcePackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ResourcePackPushMixin {
    @Shadow @Final protected Connection connection;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void hontun$packsConfiguring(CallbackInfo ci) {
        try {
            if ((Object) this instanceof ClientConfigurationPacketListenerImpl) ResourcePackInfo.configuring(connection);
            else if ((Object) this instanceof ClientPacketListener) ResourcePackInfo.playing(connection);
        } catch (Throwable ignored) {
        }
    }

    @Inject(method = "handleResourcePackPush", at = @At("HEAD"))
    private void hontun$captureResourcePack(ClientboundResourcePackPushPacket packet, CallbackInfo ci) {
        ResourcePackInfo.got(packet);
        if (!Minecraft.getInstance().isSameThread()) {
            try {
                ResourcePackInfo.arrived(packet);
            } catch (Throwable ignored) {
            }
            return;
        }
        try {
            ResourcePackInfo.pushed(packet, (Object) this instanceof ClientConfigurationPacketListenerImpl
                    ? ResourcePackInfo.Phase.CONFIGURATION : ResourcePackInfo.Phase.PLAY, connection);
        } catch (Throwable ignored) {
        }
    }

    @Inject(method = "handleResourcePackPop", at = @At("HEAD"))
    private void hontun$captureResourcePackPop(ClientboundResourcePackPopPacket packet, CallbackInfo ci) {
        if (!Minecraft.getInstance().isSameThread()) return;
        try {
            ResourcePackInfo.popped(packet.id(), connection);
        } catch (Throwable ignored) {
        }
    }
}
