package cz.honzasik.hontun.mixin.network.serverlog;

import cz.honzasik.hontun.serverlog.LogModel;
import cz.honzasik.hontun.serverlog.ServerLog;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.multiplayer.ServerStatusPinger$1")
public abstract class ServerLogPingMixin {
    @Shadow
    @Final
    ServerData val$data;

    @Unique
    private ServerStatus hontun$status;

    @Inject(method = "handleStatusResponse", at = @At("HEAD"))
    private void hontun$keepStatus(ClientboundStatusResponsePacket packet, CallbackInfo ci) {
        if (hontun$status == null) hontun$status = packet.status();
    }

    @Inject(method = "handlePongResponse", at = @At("RETURN"))
    private void hontun$logPing(ClientboundPongResponsePacket packet, CallbackInfo ci) {
        try {
            ServerStatus status = hontun$status;
            hontun$status = null;
            if (status == null || val$data.ip == null || val$data.ip.isBlank() || val$data.isLan() || val$data.isRealm()) return;
            int online = status.players().map(ServerStatus.Players::online).orElse(-1);
            int max = status.players().map(ServerStatus.Players::max).orElse(-1);
            int protocol = status.version().map(ServerStatus.Version::protocol).orElse(0);
            String version = status.version().map(ServerStatus.Version::name).orElse("");
            ServerLog.forAddress(val$data.ip).ping(new LogModel.Ping(System.currentTimeMillis(), (int) Math.min(val$data.ping, 999_999L),
                    online, max, protocol, version, status.enforcesSecureChat()));
        } catch (Throwable ignored) {
        }
    }
}
