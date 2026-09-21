package com.mrmelon54.DraggableLists.duck;

import net.minecraft.server.packs.repository.Pack;

/** Arbitrary-distance reordering for {@code PackSelectionModel.EntryBase}. */
public interface PackEntryDuck {
    Pack dl$pack();

    /**
     * Moves this pack so it sits immediately before or after {@code anchor}.
     *
     * <p>Relative rather than absolute because the rows on screen are not always the whole
     * list - the pack screen has a search box, and a filtered view's third row can be the
     * model's tenth.
     */
    void dl$moveNextTo(Pack anchor, boolean after);
}
