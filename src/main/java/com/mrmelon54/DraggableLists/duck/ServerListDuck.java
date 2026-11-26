package com.mrmelon54.DraggableLists.duck;

import net.minecraft.client.multiplayer.ServerData;

public interface ServerListDuck {
    void dl$move(ServerData moved, ServerData anchor, boolean after);
}
