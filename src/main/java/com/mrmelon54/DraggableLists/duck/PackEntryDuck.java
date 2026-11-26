package com.mrmelon54.DraggableLists.duck;

import net.minecraft.server.packs.repository.Pack;

public interface PackEntryDuck {
    Pack dl$pack();

    void dl$moveNextTo(Pack anchor, boolean after);
}
