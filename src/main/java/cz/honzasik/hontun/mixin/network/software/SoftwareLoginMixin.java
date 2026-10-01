package cz.honzasik.hontun.mixin.network.software;

import cz.honzasik.hontun.utils.ServerSoftware;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientHandshakePacketListenerImpl.class)
public abstract class SoftwareLoginMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void hontun$reset(CallbackInfo ci) {
        ServerSoftware.resetAll();
    }
}
