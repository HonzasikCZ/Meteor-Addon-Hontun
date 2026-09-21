package com.mrmelon54.DraggableLists.mixin.server;

import com.mrmelon54.DraggableLists.duck.ServerListDuck;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(ServerList.class)
public class ServerListMixin implements ServerListDuck {
    @Shadow
    @Final
    private List<ServerData> serverList;

    @Override
    public void dl$move(ServerData moved, ServerData anchor, boolean after) {
        if (moved == anchor) return;
        if (!serverList.remove(moved)) return;

        // Look the anchor up after the removal, so the index is already in post-removal terms.
        int index = serverList.indexOf(anchor);
        if (index < 0) {
            serverList.add(moved);
            return;
        }
        serverList.add(after ? index + 1 : index, moved);
    }
}
