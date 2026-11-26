package cz.honzasik.hontun.mixin.client.multiplayer;

import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerSelectionList.OnlineServerEntry.class)
public interface OnlineServerEntryAccessor {
    @Accessor("icon")
    FaviconTexture hontun$icon();
}
