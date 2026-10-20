package cz.honzasik.hontun.mixin.network.software;

import com.google.gson.JsonElement;
import cz.honzasik.hontun.utils.ServerSoftware;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundStatusResponsePacket.class)
public abstract class SoftwareStatusCodecMixin {
    @Shadow
    @Final
    @Mutable
    public static StreamCodec<ByteBuf, ClientboundStatusResponsePacket> STREAM_CODEC;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void hontun$peekStatusJson(CallbackInfo ci) {
        StreamCodec<ByteBuf, ClientboundStatusResponsePacket> original = STREAM_CODEC;
        StreamCodec<ByteBuf, JsonElement> json = ByteBufCodecs.lenientJson(32767);
        STREAM_CODEC = StreamCodec.of(original::encode, buf -> {
            int at = buf.readerIndex();
            JsonElement parsed = null;
            try {
                parsed = json.decode(buf);
            } catch (Throwable ignored) {
            } finally {
                buf.readerIndex(at);
            }
            ServerSoftware.onStatusJson(parsed);
            return original.decode(buf);
        });
    }
}
