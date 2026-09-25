package cz.honzasik.hontun.mixin.network.software;

import cz.honzasik.hontun.utils.ServerSoftware;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundChunkBatchStartPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class SoftwarePlayMixin {
    @Inject(method = "handleChunkBatchStart", at = @At("HEAD"))
    private void hontun$batch(ClientboundChunkBatchStartPacket packet, CallbackInfo ci) {
        ServerSoftware.onChunkBatch();
    }

    @Inject(method = "handleLevelChunkWithLight", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V",
            shift = At.Shift.AFTER))
    private void hontun$chunk(ClientboundLevelChunkWithLightPacket packet, CallbackInfo ci) {
        ServerSoftware.onChunk();
    }

    @Inject(method = "handleCommands", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;commands:Lcom/mojang/brigadier/CommandDispatcher;",
            opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void hontun$commands(ClientboundCommandsPacket packet, CallbackInfo ci) {
        ServerSoftware.onCommands(((ClientPacketListener) (Object) this).getCommands());
    }
}
