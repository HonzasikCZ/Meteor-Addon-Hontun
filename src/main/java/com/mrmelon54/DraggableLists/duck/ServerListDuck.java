package com.mrmelon54.DraggableLists.duck;

import net.minecraft.client.multiplayer.ServerData;

/** Lets the reorder reach {@code ServerList}'s private backing list. */
public interface ServerListDuck {
    void dl$move(ServerData moved, ServerData anchor, boolean after);
}
