package com.mrmelon54.DraggableLists.api;

/**
 * Implemented by the list entries that can be picked up.
 */
public interface DragItem {
    /**
     * An id that survives the list being rebuilt. Both lists throw their entry objects away
     * and build new ones after a reorder, so this is what lets the row that just moved be
     * found again in order to flash it.
     */
    String dl$stableId();

    /** False for rows that are pinned in place, such as required or built-in resource packs. */
    boolean dl$canDrag();
}
