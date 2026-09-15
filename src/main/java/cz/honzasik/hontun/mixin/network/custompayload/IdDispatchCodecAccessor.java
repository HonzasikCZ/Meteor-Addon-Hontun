package cz.honzasik.hontun.mixin.network.custompayload;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.network.codec.IdDispatchCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(IdDispatchCodec.class)
public interface IdDispatchCodecAccessor {
    @Accessor("toId")
    Object2IntMap<Object> hontun$getToId();
}
