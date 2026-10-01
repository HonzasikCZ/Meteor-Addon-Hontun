package cz.honzasik.hontun.mixin.network.software;

import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundRotateHeadPacket.class)
public interface RotateHeadPacketAccessor {
    @Accessor("entityId")
    int hontun$entityId();

    @Accessor("yHeadRot")
    byte hontun$yHeadRot();
}
