package cz.honzasik.hontun.mixin.network.custompayload;

import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientCommonPacketListenerImpl.class)
public interface ClientListenerConnectionAccessor {
    @Accessor("connection")
    Connection hontun$getConnection();
}
