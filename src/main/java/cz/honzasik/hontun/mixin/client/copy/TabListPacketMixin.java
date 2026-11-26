package cz.honzasik.hontun.mixin.client.copy;

import cz.honzasik.hontun.copy.Recorder;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class TabListPacketMixin {
    @Inject(method = "handleTabListCustomisation", at = @At("TAIL"))
    private void hontun$recordTab(ClientboundTabListPacket packet, CallbackInfo ci) {
        Recorder.INSTANCE.onTabPacket();
    }
}
