package com.mrmelon54.DraggableLists.mixin.packs;

import com.mrmelon54.DraggableLists.DraggableLists;
import com.mrmelon54.DraggableLists.api.DragList;
import com.mrmelon54.DraggableLists.drag.DragManager;
import com.mrmelon54.DraggableLists.duck.PackEntryDuck;
import com.mrmelon54.DraggableLists.duck.PackRowDuck;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(TransferableSelectionList.class)
public abstract class TransferableSelectionListMixin extends ObjectSelectionList<TransferableSelectionList.Entry> implements DragList {
    @Unique
    private final DragManager dl$drag = new DragManager(this);

    protected TransferableSelectionListMixin(Minecraft minecraft, int width, int height, int y, int entryHeight) {
        super(minecraft, width, height, y, entryHeight);
    }

    @Override
    protected void extractListItems(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (!dl$drag.extractListItems(graphics, mouseX, mouseY, partialTick)) {
            super.extractListItems(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, partialTick);
        dl$drag.extractOverlay(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        // Only remembers where the press landed; whether it becomes a drag is decided later,
        // once the pointer has actually moved.
        dl$drag.mousePressed(event);
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dl$drag.mouseDragged(event, dragX, dragY)) return true;
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean dropped = dl$drag.mouseReleased(event);
        return super.mouseReleased(event) || dropped;
    }

    // ------------------------------------------------------------------ DragList

    @Override
    public DragManager dl$drag() {
        return dl$drag;
    }

    @Override
    public List<? extends ObjectSelectionList.Entry<?>> dl$children() {
        return children();
    }

    @Override
    public boolean dl$dragEnabled() {
        return DraggableLists.config().resourcePackDragging.isEnabled();
    }

    @Override
    public boolean dl$overScrollbar(double x, double y) {
        return scrollable() && isOverScrollbar(x, y);
    }

    @Override
    public void dl$extractItem(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick,
                               ObjectSelectionList.Entry<?> entry) {
        extractItem(graphics, mouseX, mouseY, partialTick, (TransferableSelectionList.Entry) entry);
    }

    @Override
    public void dl$move(ObjectSelectionList.Entry<?> moved, ObjectSelectionList.Entry<?> anchor, boolean after) {
        if (!(moved instanceof PackRowDuck movedRow) || !(anchor instanceof PackRowDuck anchorRow)) return;
        if (!(movedRow.dl$model() instanceof PackEntryDuck movedPack)) return;
        if (!(anchorRow.dl$model() instanceof PackEntryDuck anchorPack)) return;

        // The model rebuilds both lists and the screen re-renders from the new order; nothing
        // else needs poking.
        movedPack.dl$moveNextTo(anchorPack.dl$pack(), after);
    }

    @Override
    public int dl$top() {
        return getY();
    }

    @Override
    public int dl$bottom() {
        return getBottom();
    }

    @Override
    public double dl$scroll() {
        return scrollAmount();
    }

    @Override
    public void dl$setScroll(double value) {
        setScrollAmount(value);
    }

    @Override
    public void dl$setDraggingFlag(boolean dragging) {
        setDragging(dragging);
    }

    @Override
    public boolean dl$canGrabAt(ObjectSelectionList.Entry<?> entry, double x) {
        int contentX = entry.getContentX();
        if (x >= contentX + 32) return true;
        // The right half of a selected pack's icon held the arrows and now holds the grip;
        // the left half is still unselect, and an available pack's whole icon is still select.
        return x >= contentX + 16 && x < contentX + 32 && dl$handleX(entry) >= 0;
    }

    @Override
    public int dl$handleX(ObjectSelectionList.Entry<?> entry) {
        if (!DraggableLists.config().hideResourcePackArrows) return -1;
        if (!(entry instanceof PackRowDuck row)) return -1;

        // On an available pack the whole icon is the select button, so there is no free
        // column to put a grip in; on a selected one the right half held the two arrows.
        return row.dl$model().canSelect() ? -1 : entry.getContentX() + 16;
    }
}
