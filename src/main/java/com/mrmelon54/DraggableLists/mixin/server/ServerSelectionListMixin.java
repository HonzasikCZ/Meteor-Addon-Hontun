package com.mrmelon54.DraggableLists.mixin.server;

import com.mrmelon54.DraggableLists.DraggableLists;
import com.mrmelon54.DraggableLists.api.DragList;
import com.mrmelon54.DraggableLists.drag.DragManager;
import com.mrmelon54.DraggableLists.duck.ServerListDuck;
import com.mrmelon54.DraggableLists.duck.ServerRowDuck;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ServerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(ServerSelectionList.class)
public abstract class ServerSelectionListMixin extends ObjectSelectionList<ServerSelectionList.Entry> implements DragList {
    @Shadow
    @Final
    private JoinMultiplayerScreen screen;

    @Shadow
    public abstract void updateOnlineServers(ServerList servers);

    @Unique
    private final DragManager dl$drag = new DragManager(this);

    protected ServerSelectionListMixin(Minecraft minecraft, int width, int height, int y, int entryHeight) {
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
        // Vanilla still gets the event either way: it is what clears the scrollbar's own
        // drag state, and it costs nothing when the release was ours.
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
        return DraggableLists.config().serverDragging.isEnabled();
    }

    @Override
    public boolean dl$overScrollbar(double x, double y) {
        return scrollable() && isOverScrollbar(x, y);
    }

    @Override
    public void dl$extractItem(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick,
                               ObjectSelectionList.Entry<?> entry) {
        extractItem(graphics, mouseX, mouseY, partialTick, (ServerSelectionList.Entry) entry);
    }

    @Override
    public void dl$move(ObjectSelectionList.Entry<?> moved, ObjectSelectionList.Entry<?> anchor, boolean after) {
        if (!(moved instanceof ServerRowDuck movedRow) || !(anchor instanceof ServerRowDuck anchorRow)) return;

        ServerList servers = screen.getServers();
        if (!(servers instanceof ServerListDuck duck)) return;

        duck.dl$move(movedRow.dl$serverData(), anchorRow.dl$serverData(), after);
        servers.save();
        updateOnlineServers(servers);
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
        // The row body, plus the left half of the icon once the arrows there are hidden -
        // the grip is drawn in that column, so it has to be grabbable. The right half stays
        // the join button.
        if (x >= contentX + 32) return true;
        return DraggableLists.config().hideServerArrows && x >= contentX && x < contentX + 16;
    }

    @Override
    public int dl$handleX(ObjectSelectionList.Entry<?> entry) {
        // The left half of the icon is where the move up / move down buttons used to be, so
        // it is free for the grip exactly when those are hidden.
        return DraggableLists.config().hideServerArrows ? entry.getContentX() : -1;
    }
}
