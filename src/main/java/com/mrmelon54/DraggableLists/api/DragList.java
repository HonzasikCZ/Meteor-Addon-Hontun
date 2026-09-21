package com.mrmelon54.DraggableLists.api;

import com.mrmelon54.DraggableLists.drag.DragManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;

import java.util.List;

/**
 * The seam between {@link DragManager} and a concrete vanilla list.
 *
 * <p>26.2 gives every entry its own x/y/width/height and repositions them itself, so this is
 * much smaller than the equivalent in upstream: the manager can read geometry straight off
 * the entries and only needs the list for the few things entries do not know - what counts
 * as a row it may reorder, where the grip belongs, and how to actually commit the move.
 */
public interface DragList {
    DragManager dl$drag();

    List<? extends ObjectSelectionList.Entry<?>> dl$children();

    /** Config gate for this particular list, including the "hold shift" mode. */
    boolean dl$dragEnabled();

    boolean dl$overScrollbar(double x, double y);

    /** Calls the list's own {@code extractItem} so selection outlines keep working. */
    void dl$extractItem(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick,
                        ObjectSelectionList.Entry<?> entry);

    /**
     * Applies the reorder to the backing data. Expressed relative to a neighbour rather than
     * as an absolute index because the visible rows are not always the whole list - the
     * resource pack screen has a search filter and a header row, and the server screen has
     * the LAN section below the reorderable block.
     *
     * @param moved  the row that was carried
     * @param anchor the row it was dropped next to
     * @param after  true to place {@code moved} after {@code anchor}, false for before
     */
    void dl$move(ObjectSelectionList.Entry<?> moved, ObjectSelectionList.Entry<?> anchor, boolean after);

    int dl$top();

    int dl$bottom();

    double dl$scroll();

    void dl$setScroll(double value);

    void dl$setDraggingFlag(boolean dragging);

    /**
     * Whether a drag may start at this x on this row. Not a single threshold, because the
     * grip sits in a freed column inside the icon while the row's remaining button sits
     * beside it - so the grabbable area is the row body plus the grip, with the button
     * between them left alone.
     */
    boolean dl$canGrabAt(ObjectSelectionList.Entry<?> entry, double x);

    /**
     * Absolute x of the 16 pixel column the grip is drawn in - the space the up/down arrows
     * used to occupy - or -1 when this row has no free column.
     */
    int dl$handleX(ObjectSelectionList.Entry<?> entry);
}
