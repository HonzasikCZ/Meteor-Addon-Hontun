package cz.honzasik.hontun.mixin.network.worldinfo;

import cz.honzasik.hontun.utils.WorldInfo;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientLoginMixin {
    @Inject(method = "handleLogin", at = @At("HEAD"))
    private void hontun$captureWorldInfo(ClientboundLoginPacket packet, CallbackInfo ci) {
        WorldInfo.gotLogin(packet);
    }
}
