package cz.honzasik.hontun.mixin.network.custompayload;

import cz.honzasik.hontun.network.HontunPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.IdDispatchCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PacketEncoder.class)
public abstract class CustomPayloadEncoderMixin {
    @Shadow @Final private ProtocolInfo<?> protocolInfo;

    @Inject(
        method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hontun$writeRawCustomPayload(ChannelHandlerContext ctx, Packet<?> packet, ByteBuf out, CallbackInfo ci) {
        if (!(packet instanceof ServerboundCustomPayloadPacket scpp) || !(scpp.payload() instanceof HontunPayload payload)) {
            return;
        }
        ci.cancel();

        StreamCodec<?, ?> codec = protocolInfo.codec();
        if (!(codec instanceof IdDispatchCodec<?, ?, ?>)) {
            throw new EncoderException("Hontun: protocol codec is not IdDispatchCodec (" + codec.getClass().getName() + ")");
        }
        int id = ((IdDispatchCodecAccessor) (Object) codec).hontun$getToId().getOrDefault(packet.type(), -1);
        if (id < 0) {
            throw new EncoderException("Hontun: no packet id for SERVERBOUND_CUSTOM_PAYLOAD in " + protocolInfo.id());
        }

        VarInt.write(out, id);
        FriendlyByteBuf buf = new FriendlyByteBuf(out);
        buf.writeIdentifier(payload.channel());
        buf.writeBytes(payload.data());
    }
}
