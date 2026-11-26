package com.mrmelon54.DraggableLists.api;

import com.mrmelon54.DraggableLists.drag.DragManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;

import java.util.List;

public interface DragList {
    DragManager dl$drag();

    List<? extends ObjectSelectionList.Entry<?>> dl$children();

    boolean dl$dragEnabled();

    boolean dl$overScrollbar(double x, double y);

    void dl$extractItem(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick,
                        ObjectSelectionList.Entry<?> entry);

    void dl$move(ObjectSelectionList.Entry<?> moved, ObjectSelectionList.Entry<?> anchor, boolean after);

    int dl$top();

    int dl$bottom();

    double dl$scroll();

    void dl$setScroll(double value);

    void dl$setDraggingFlag(boolean dragging);

    boolean dl$canGrabAt(ObjectSelectionList.Entry<?> entry, double x);

    int dl$handleX(ObjectSelectionList.Entry<?> entry);
}
