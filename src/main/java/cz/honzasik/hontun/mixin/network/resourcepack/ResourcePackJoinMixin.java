package cz.honzasik.hontun.mixin.network.resourcepack;

import cz.honzasik.hontun.utils.ResourcePackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ResourcePackJoinMixin extends ClientCommonPacketListenerImpl {
    private ResourcePackJoinMixin(Minecraft minecraft, Connection connection, CommonListenerCookie cookie) {
        super(minecraft, connection, cookie);
    }

    @Inject(method = "handleLogin", at = @At("HEAD"))
    private void hontun$packsJoined(ClientboundLoginPacket packet, CallbackInfo ci) {
        if (!Minecraft.getInstance().isSameThread()) return;
        try {
            ResourcePackInfo.joined(connection);
        } catch (Throwable ignored) {
        }
    }

    @Inject(method = "handleTabListCustomisation", at = @At("HEAD"))
    private void hontun$packsTabList(ClientboundTabListPacket packet, CallbackInfo ci) {
        try {
            if (Minecraft.getInstance().isSameThread()) ResourcePackInfo.tabList(connection, packet);
            else ResourcePackInfo.tabArrived(packet);
        } catch (Throwable ignored) {
        }
    }
}
