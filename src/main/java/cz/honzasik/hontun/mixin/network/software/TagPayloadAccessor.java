package cz.honzasik.hontun.mixin.network.software;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagNetworkSerialization;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(TagNetworkSerialization.NetworkPayload.class)
public interface TagPayloadAccessor {
    @Accessor("tags")
    Map<Identifier, IntList> hontun$tags();
}
