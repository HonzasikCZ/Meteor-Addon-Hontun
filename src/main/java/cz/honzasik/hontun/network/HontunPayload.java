package cz.honzasik.hontun.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record HontunPayload(Identifier channel, byte[] data) implements CustomPacketPayload {
    @Override
    public Type<HontunPayload> type() {
        return new Type<>(channel);
    }
}
