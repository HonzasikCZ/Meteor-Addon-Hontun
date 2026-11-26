package com.mrmelon54.DraggableLists.drag;

import com.mrmelon54.DraggableLists.DraggableLists;
import com.mrmelon54.DraggableLists.api.DragItem;
import com.mrmelon54.DraggableLists.api.DragList;

import com.mrmelon54.DraggableLists.config.DragConfig;
import com.mrmelon54.DraggableLists.render.DragSkin;
import com.mrmelon54.DraggableLists.theme.Palette;
import com.mrmelon54.DraggableLists.theme.Themes;
import com.mojang.blaze3d.platform.cursor.CursorType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class DragManager {
    private static final int AUTOSCROLL_EDGE = 32;
    private static final double AUTOSCROLL_SPEED = 1000.0;
    private static final float SETTLE_RATE = 20f;
    private static final long FLASH_MILLIS = 450L;

    private static final double DRAG_THRESHOLD = 2.0;

    private final DragList list;

    private double pressX;
    private double pressY;
    private boolean pressed;

    private ObjectSelectionList.@Nullable Entry<?> dragged;
    private int grabDy;
    private int ghostY;
    private int dropY;
    private float lift;

    private final Map<Object, Float> offsets = new IdentityHashMap<>();

    private long lastFrame;

    private @Nullable Object hoveredHandleEntry;
    private float handleFade;

    private @Nullable String settleId;
    private long settleAt;

    public DragManager(DragList list) {
        this.list = list;
        DraggableLists.reloadConfigIfStale();
    }

    public boolean active() {
        return dragged != null;
    }

    public void mousePressed(MouseButtonEvent event) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;
        pressX = event.x();
        pressY = event.y();
        pressed = true;
    }

    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragged != null) {
            follow(event);
            return true;
        }

        if (!pressed || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        if (!list.dl$dragEnabled()) return false;

        if (Math.abs(event.x() - pressX) < DRAG_THRESHOLD && Math.abs(event.y() - pressY) < DRAG_THRESHOLD) {
            return false;
        }

        if (pressY < list.dl$top() || pressY > list.dl$bottom()) return false;
        if (list.dl$overScrollbar(pressX, pressY)) return false;

        ObjectSelectionList.Entry<?> hit = entryAt(pressX, pressY);
        if (hit == null || !canDrag(hit)) return false;
        if (!list.dl$canGrabAt(hit, pressX)) return false;

        dragged = hit;
        grabDy = hit.getY() - (int) pressY;
        lift = 0f;
        offsets.clear();
        list.dl$setDraggingFlag(true);
        follow(event);
        return true;
    }

    private void follow(MouseButtonEvent event) {
        dropY = (int) event.y() + grabDy;
        ghostY = clampGhost(dropY);
    }

    public boolean mouseReleased(MouseButtonEvent event) {
        if (dragged == null) return false;

        follow(event);

        ObjectSelectionList.Entry<?> moved = dragged;
        Layout layout = layout();
        stop();

        if (layout == null || layout.to == layout.from) return true;

        if (moved instanceof DragItem item) {
            settleId = item.dl$stableId();
            settleAt = Util.getMillis();
        }

        if (layout.to < layout.rest.size()) {
            list.dl$move(moved, layout.rest.get(layout.to), false);
        } else if (!layout.rest.isEmpty()) {
            list.dl$move(moved, layout.rest.getLast(), true);
        }
        return true;
    }

    public void cancel() {
        if (dragged == null) return;
        stop();
    }

    private void stop() {
        dragged = null;
        pressed = false;
        lift = 0f;
        offsets.clear();
        list.dl$setDraggingFlag(false);
    }

    private record Layout(List<ObjectSelectionList.Entry<?>> rest, int from, int to, int blockTop, int dropTop) {
    }

    private @Nullable Layout layout() {
        if (dragged == null) return null;

        List<ObjectSelectionList.Entry<?>> block = new ArrayList<>();
        for (ObjectSelectionList.Entry<?> entry : list.dl$children()) {
            if (canDrag(entry)) block.add(entry);
        }

        int from = block.indexOf(dragged);
        if (from < 0) return null;

        int blockTop = block.getFirst().getY();

        List<ObjectSelectionList.Entry<?>> rest = new ArrayList<>(block);
        rest.remove(from);

        double carriedCentre = dropY + dragged.getHeight() / 2.0;
        int to = 0;
        for (ObjectSelectionList.Entry<?> entry : rest) {
            if (entry.getY() + entry.getHeight() / 2.0 < carriedCentre) to++;
        }

        int dropTop = blockTop;
        for (int i = 0; i < to; i++) dropTop += rest.get(i).getHeight();

        return new Layout(rest, from, to, blockTop, dropTop);
    }

    public boolean extractListItems(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (dragged == null) return false;

        Layout layout = layout();
        if (layout == null) {
            stop();
            return false;
        }

        DragConfig config = DraggableLists.config();
        Palette palette = Themes.current();
        float blend = blend(tick(config), config);

        if (config.dropIndicator) {
            DragSkin.gap(graphics, dragged.getX(), layout.dropTop, dragged.getWidth(), dragged.getHeight(), palette);
        }

        int slotY = layout.blockTop;
        for (int i = 0; i <= layout.rest.size(); i++) {
            if (i == layout.to) slotY += dragged.getHeight();
            if (i == layout.rest.size()) break;

            ObjectSelectionList.Entry<?> entry = layout.rest.get(i);
            float target = slotY - entry.getY();
            float current = config.animations
                ? offsets.merge(entry, target, (had, want) -> had + (want - had) * blend)
                : target;
            slotY += entry.getHeight();

            drawShifted(graphics, mouseX, mouseY, partialTick, entry, Math.round(current));
        }

        for (ObjectSelectionList.Entry<?> entry : list.dl$children()) {
            if (canDrag(entry)) continue;
            if (visible(entry)) list.dl$extractItem(graphics, mouseX, mouseY, partialTick, entry);
        }
        return true;
    }

    private void drawShifted(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick,
                             ObjectSelectionList.Entry<?> entry, int offset) {
        int realY = entry.getY();
        entry.setY(realY + offset);
        try {
            if (visible(entry)) list.dl$extractItem(graphics, mouseX, mouseY, partialTick, entry);
        } finally {
            entry.setY(realY);
        }
    }

    public void extractOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        DragConfig config = DraggableLists.config();
        Palette palette = Themes.current();

        int top = list.dl$top();
        int bottom = list.dl$bottom();
        if (bottom <= top) return;

        graphics.enableScissor(0, top, graphics.guiWidth(), bottom);
        try {
            if (config.settleFlash) drawFlash(graphics, palette);

            if (dragged != null) {
                drawCarried(graphics, partialTick, config, palette);
            } else if (config.grabHandle) {
                drawHandle(graphics, mouseX, mouseY, config, palette);
            }
        } finally {
            graphics.disableScissor();
        }

        if (dragged != null) {
            CursorType cursor = config.cursor.cursor();
            if (cursor != null) graphics.requestCursor(cursor);
        }
    }

    private void drawCarried(GuiGraphicsExtractor graphics, float partialTick,
                             DragConfig config, Palette palette) {
        ObjectSelectionList.Entry<?> entry = dragged;
        if (entry == null) return;

        int x = entry.getX();
        int width = entry.getWidth();
        int height = entry.getHeight();

        boolean hontun = Themes.hontunStyled();
        int cardY = hontun ? ghostY + 1 : ghostY;
        int cardHeight = hontun ? height - 2 : height;

        boolean transformed = config.lift && lift > 0.005f;
        if (transformed) {
            float pivotX = x + width / 2f;
            float pivotY = ghostY + height / 2f;
            graphics.pose().pushMatrix();
            graphics.pose().translate(pivotX, pivotY);
            graphics.pose().rotate((float) Math.toRadians(0.9) * lift);
            graphics.pose().scale(1f + 0.02f * lift, 1f + 0.02f * lift);
            graphics.pose().translate(-pivotX, -pivotY);
        }

        if (config.shadow) DragSkin.cardShadow(graphics, x, cardY, width, cardHeight, palette);
        DragSkin.cardFill(graphics, x, cardY, width, cardHeight, palette);

        int realY = entry.getY();
        entry.setY(ghostY);
        try {
            entry.extractContent(graphics, -1, -1, false, partialTick);
        } finally {
            entry.setY(realY);
        }

        DragSkin.cardEdge(graphics, x, cardY, width, cardHeight, palette);

        if (transformed) graphics.pose().popMatrix();
    }

    private void drawHandle(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                            DragConfig config, Palette palette) {
        if (!list.dl$dragEnabled()) {
            hoveredHandleEntry = null;
            handleFade = 0f;
            return;
        }

        float delta = tick(config);

        ObjectSelectionList.Entry<?> hovered = entryAt(mouseX, mouseY);
        if (hovered != null && (!canDrag(hovered)
            || mouseY < list.dl$top() || mouseY > list.dl$bottom()
            || list.dl$overScrollbar(mouseX, mouseY))) {
            hovered = null;
        }

        int handleX = hovered == null ? -1 : list.dl$handleX(hovered);
        if (hovered == null || handleX < 0) {
            hoveredHandleEntry = null;
            handleFade = 0f;
            return;
        }

        if (hoveredHandleEntry != hovered) {
            hoveredHandleEntry = hovered;
            handleFade = 0f;
        }
        handleFade = config.animations
            ? Math.min(1f, handleFade + delta * 7f * config.animationSpeed)
            : 1f;

        boolean hot = mouseX >= handleX && mouseX < handleX + DragSkin.HANDLE_WIDTH
            && mouseY >= hovered.getContentY() && mouseY < hovered.getContentBottom();

        DragSkin.handle(graphics, handleX, hovered.getContentY(), hovered.getContentHeight(),
            hot, handleFade, palette);

        if (hot && config.cursor.cursor() != null) {
            graphics.requestCursor(config.cursor.cursor());
        }
    }

    private void drawFlash(GuiGraphicsExtractor graphics, Palette palette) {
        if (settleId == null) return;

        long elapsed = Util.getMillis() - settleAt;
        if (elapsed >= FLASH_MILLIS) {
            settleId = null;
            return;
        }

        for (ObjectSelectionList.Entry<?> entry : list.dl$children()) {
            if (!(entry instanceof DragItem item) || !settleId.equals(item.dl$stableId())) continue;
            if (!visible(entry)) return;
            DragSkin.flash(graphics, entry.getX(), entry.getY(), entry.getWidth(), entry.getHeight(),
                elapsed / (float) FLASH_MILLIS, palette);
            return;
        }
    }

    private float tick(DragConfig config) {
        long now = Util.getMillis();
        float delta = lastFrame == 0L ? 0f : Math.min(0.1f, (now - lastFrame) / 1000f);
        lastFrame = now;

        lift = config.animations ? Math.min(1f, lift + delta * 7f * config.animationSpeed) : 1f;
        autoScroll(config, delta);
        return delta;
    }

    private static float blend(float delta, DragConfig config) {
        if (!config.animations) return 1f;
        return 1f - (float) Math.exp(-delta * SETTLE_RATE * config.animationSpeed);
    }

    private void autoScroll(DragConfig config, float delta) {
        if (!config.autoScroll || dragged == null || delta <= 0f) return;

        int top = list.dl$top();
        int bottom = list.dl$bottom();
        double centre = dropY + dragged.getHeight() / 2.0;

        double push = 0;
        if (centre < top + AUTOSCROLL_EDGE) {
            push = -(top + AUTOSCROLL_EDGE - centre) / AUTOSCROLL_EDGE;
        } else if (centre > bottom - AUTOSCROLL_EDGE) {
            push = (centre - (bottom - AUTOSCROLL_EDGE)) / AUTOSCROLL_EDGE;
        }
        if (push == 0) return;

        push = Mth.clamp(push, -1.0, 1.0);
        list.dl$setScroll(list.dl$scroll() + push * AUTOSCROLL_SPEED * delta);
        ghostY = clampGhost(ghostY);
    }

    private int clampGhost(int y) {
        if (dragged == null) return y;
        int min = list.dl$top() + 2;
        int max = list.dl$bottom() - 2 - dragged.getHeight();
        if (max < min) return min;
        return Mth.clamp(y, min, max);
    }

    private boolean visible(ObjectSelectionList.Entry<?> entry) {
        return entry.getY() + entry.getHeight() >= list.dl$top() && entry.getY() <= list.dl$bottom();
    }

    private static boolean canDrag(ObjectSelectionList.Entry<?> entry) {
        return entry instanceof DragItem item && item.dl$canDrag();
    }

    private ObjectSelectionList.@Nullable Entry<?> entryAt(double mouseX, double mouseY) {
        for (ObjectSelectionList.Entry<?> entry : list.dl$children()) {
            if (entry.isMouseOver(mouseX, mouseY)) return entry;
        }
        return null;
    }
}
