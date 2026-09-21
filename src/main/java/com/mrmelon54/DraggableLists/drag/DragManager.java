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

/**
 * One drag gesture, start to finish, for a single list.
 *
 * <p>The rows the user is not carrying are never actually moved while the drag is in
 * progress - they are drawn at an animated offset and put straight back, so hit testing,
 * scrolling and the list's own idea of where things are all stay honest. Only the drop
 * commits anything.
 */
public class DragManager {
    /** How far from the top/bottom edge the carried row starts pulling the list along. */
    private static final int AUTOSCROLL_EDGE = 32;
    /** Peak auto-scroll speed, in GUI pixels per second. */
    private static final double AUTOSCROLL_SPEED = 1000.0;
    /** Exponential smoothing rate for row movement; higher is snappier. */
    private static final float SETTLE_RATE = 20f;
    /** How long the "this is where it went" outline lasts. */
    private static final long FLASH_MILLIS = 450L;

    /** How far the pointer must travel before a press turns into a drag. */
    private static final double DRAG_THRESHOLD = 2.0;

    private final DragList list;

    /** Where the button went down, which is not where the first drag event arrives. */
    private double pressX;
    private double pressY;
    private boolean pressed;

    private ObjectSelectionList.@Nullable Entry<?> dragged;
    /** Distance from the pointer to the top of the carried row, fixed at pick-up. */
    private int grabDy;
    /** Where the carried row is drawn: kept inside the list. */
    private int ghostY;
    /** Where the carried row would be if the list did not stop it: decides the drop slot. */
    private int dropY;
    private int pointerX;
    private float lift;

    /** Animated vertical offset from each row's real position, keyed by entry identity. */
    private final Map<Object, Float> offsets = new IdentityHashMap<>();

    private long lastFrame;

    private @Nullable Object hoveredHandleEntry;
    private float handleFade;

    private @Nullable String settleId;
    private long settleAt;

    public DragManager(DragList list) {
        this.list = list;
        // One manager is built per list, and a list is built when its screen opens, which is
        // exactly when a hand-edited config should be picked up.
        DraggableLists.reloadConfigIfStale();
    }

    public boolean active() {
        return dragged != null;
    }

    // ------------------------------------------------------------------ input

    /**
     * Records where the button went down.
     *
     * <p>The drag itself cannot start here - a press is usually just a click - but it cannot
     * use the first drag event's position either: by the time that arrives the pointer has
     * already moved, and anchoring the row to the moved position makes it jump out from under
     * the cursor by however far that first movement was, which is enough to land the drop a
     * whole row away from where it was aimed.
     */
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

        // A click wobbles by a pixel; wait for real travel before lifting anything.
        if (Math.abs(event.x() - pressX) < DRAG_THRESHOLD && Math.abs(event.y() - pressY) < DRAG_THRESHOLD) {
            return false;
        }

        if (pressY < list.dl$top() || pressY > list.dl$bottom()) return false;
        if (list.dl$overScrollbar(pressX, pressY)) return false;

        ObjectSelectionList.Entry<?> hit = entryAt(pressX, pressY);
        if (hit == null || !canDrag(hit)) return false;
        // The rest of the row's width belongs to its own buttons.
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
        pointerX = (int) event.x();
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

    /** Drops whatever is being carried without reordering anything. */
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

    // ------------------------------------------------------------------ layout

    /**
     * Where every row goes this frame.
     *
     * @param rest the reorderable rows without the carried one, in list order
     * @param from the carried row's current position within {@code rest}'s coordinate space
     * @param to   the position it would be dropped into
     */
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

        // Each neighbour is passed when the carried row's centre crosses that neighbour's own
        // centre, measured where it actually still is. Closing the gap up first and comparing
        // against the compacted positions instead looks equivalent but is not: it puts the
        // carried row's resting centre exactly on its lower neighbour's compacted centre, so
        // the row sits on a knife edge and a one pixel nudge downwards reorders the list.
        //
        // dropY, not ghostY: the drawn row is held inside the list, and if the last row also
        // ends flush with the bottom there is then no position left that is past its centre -
        // making the last slot unreachable in a full list.
        double carriedCentre = dropY + dragged.getHeight() / 2.0;
        int to = 0;
        for (ObjectSelectionList.Entry<?> entry : rest) {
            if (entry.getY() + entry.getHeight() / 2.0 < carriedCentre) to++;
        }

        int dropTop = blockTop;
        for (int i = 0; i < to; i++) dropTop += rest.get(i).getHeight();

        return new Layout(rest, from, to, blockTop, dropTop);
    }

    // ------------------------------------------------------------------ rendering

    /**
     * Replaces the list's own row loop while a drag is in progress.
     *
     * @return false if the caller should draw the list normally instead
     */
    public boolean extractListItems(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (dragged == null) return false;

        Layout layout = layout();
        if (layout == null) {
            // The list rebuilt itself underneath us - a LAN server appearing is enough to do
            // it - so the row being carried no longer exists. Let go rather than draw nothing.
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

        // Rows outside the reorderable block - the LAN section, the pack list header - keep
        // their own positions and are drawn exactly as the list would have drawn them.
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

    /** Everything that sits on top of the list: the grip, the carried row and the drop flash. */
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

        // Hontun insets its own card by a pixel top and bottom; line up with it so the accent
        // edge lands on the card's real border instead of a pixel outside it.
        boolean hontun = Themes.hontunStyled();
        int cardY = hontun ? ghostY + 1 : ghostY;
        int cardHeight = hontun ? height - 2 : height;

        boolean transformed = config.lift && lift > 0.005f;
        if (transformed) {
            float pivotX = pointerX;
            float pivotY = ghostY + height / 2f;
            graphics.pose().pushMatrix();
            graphics.pose().translate(pivotX, pivotY);
            graphics.pose().rotate((float) Math.toRadians(0.9) * lift);
            graphics.pose().scale(1f + 0.02f * lift, 1f + 0.02f * lift);
            graphics.pose().translate(-pivotX, -pivotY);
        }

        if (config.shadow) DragSkin.cardShadow(graphics, x, cardY, width, cardHeight, palette);
        // Always lay down an opaque backing, even under Hontun's own card. Hontun paints its
        // card at partial alpha (it normally sits on the solid list, not in mid-air) and its
        // non-Modern2 modes draw no card at all, so without this the lifted row is translucent
        // and the list shows through at its edges - the "gap top and bottom" while dragging.
        DragSkin.cardFill(graphics, x, cardY, width, cardHeight, palette);

        int realY = entry.getY();
        entry.setY(ghostY);
        try {
            // -1/-1 for the pointer: the row is under the cursor the whole time, and a row
            // that thinks it is hovered starts asking for tooltips and drawing its own
            // buttons on a card that is in mid-air.
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

    // ------------------------------------------------------------------ helpers

    /**
     * Advances the frame clock once per frame and returns the elapsed seconds. Also drives
     * the pick-up animation and the auto-scroll, both of which need the same delta.
     *
     * <p>Exactly one of {@link #extractListItems} and {@link #drawHandle} runs on any given
     * frame - the first only while carrying a row, the second only while not - so this is
     * called once per frame no matter which path the frame takes.
     */
    private float tick(DragConfig config) {
        long now = Util.getMillis();
        // A first frame, or one after the screen sat idle, must not be allowed to fast
        // forward every animation at once.
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
        // dropY, not ghostY: the drawn ghost is clamped to stay inside the list minus its own
        // height, so its centre can never reach the edge zone's full depth and the ramp below
        // would top out at a fraction of full speed near the ends. dropY tracks the real
        // pointer, so pressing against - or past - the edge ramps all the way to full speed.
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
