package cz.honzasik.hontun.mixin.network.channels;

import cz.honzasik.hontun.modules.ChannelFetcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.network.protocol.common.custom.CustomPacketPayload$1")
public abstract class CustomPayloadCodecMixin {
    private static final int CAPTURE_CAP = 65536;

    @Inject(
        method = "decode(Lnet/minecraft/network/FriendlyByteBuf;)Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;",
        at = @At("HEAD"),
        require = 1
    )
    private void hontun$capturePayload(FriendlyByteBuf input, CallbackInfoReturnable<CustomPacketPayload> cir) {
        int start = input.readerIndex();
        Identifier id;
        try {
            id = input.readIdentifier();
        } catch (Throwable t) {
            input.readerIndex(start);
            return;
        }

        int len = input.readableBytes();
        int copy = Math.max(0, Math.min(len, CAPTURE_CAP));
        byte[] data = new byte[copy];
        if (copy > 0) input.getBytes(input.readerIndex(), data);

        input.readerIndex(start);

        ChannelFetcher.onRawPayload(id, data);
    }
}
