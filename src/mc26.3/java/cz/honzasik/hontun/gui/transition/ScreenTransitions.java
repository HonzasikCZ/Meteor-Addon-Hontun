package cz.honzasik.hontun.gui.transition;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.textures.GpuTexture;
import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.gui.mixin.meteorclient.WidgetScreenAccessor;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.style.terminal.WTermFooter;
import cz.honzasik.hontun.utils.HontunTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WTopBar;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.client.gui.screens.inventory.AbstractCommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.BookSignScreen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.JigsawBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.TestBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.TestInstanceBlockEditScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class ScreenTransitions {
    private static final Logger LOG = LoggerFactory.getLogger("Hontun/ScreenTransition");

    private static final float ZOOM = 0.03f;
    private static final int MAX_CHAIN = 6;
    private static final long CALM_NANOS = 400_000_000L;
    private static final long PENDING = Long.MIN_VALUE;
    private static final double BAR_PAD = 2.0;
    private static final double SLIDE = 8.0;

    private static boolean broken;
    private static boolean warmed;
    private static boolean forceNext;
    private static Screen shown;
    private static boolean shownBlocked = true;
    private static boolean titleSeen;
    private static GpuTexture presented;
    private static boolean running;
    private static boolean zoom;
    private static boolean slide;
    private static float slideUv;
    private static float footUv;
    private static float maskX0;
    private static float maskY0;
    private static float maskX1;
    private static float maskY1;
    private static long startNanos;
    private static long durationNanos;
    private static int chain;
    private static boolean calming;
    private static long calmUntil;

    private ScreenTransitions() {}

    public static void forceInstant() {
        running = false;
        forceNext = true;
    }

    public static void beforeRender(Minecraft mc) {
        if (broken) return;
        try {
            boolean forced = forceNext;
            forceNext = false;
            Screen current = mc.gui.screen();
            boolean blocked = mc.gui.overlay() != null || !mc.isGameLoadFinished();
            Screen previous = shown;
            boolean previousBlocked = shownBlocked;
            shown = current;
            shownBlocked = blocked;
            if (blocked) {
                running = false;
                return;
            }
            if (!warmed) {
                warmed = true;
                if (style() != TransitionStyle.Off) GhostPass.warm();
            }
            boolean firstTitle = false;
            if (!titleSeen && current instanceof TitleScreen) {
                titleSeen = true;
                firstTitle = true;
            }
            if (current == previous) return;
            TransitionStyle style = style();
            if (forced || previousBlocked || firstTitle || style == TransitionStyle.Off
                    || !GhostPass.available() || !applies(mc, previous, current)) {
                running = false;
                return;
            }
            start(mc, style, previous, current);
        } catch (Throwable t) {
            disable(t);
        }
    }

    public static void afterRender(Minecraft mc) {
        if (broken) return;
        try {
            RenderTarget target = mc.gameRenderer.mainRenderTarget();
            if (running) draw(target);
            presented = target.getColorTexture();
        } catch (Throwable t) {
            disable(t);
        }
    }

    public static void endFrame() {
        if (broken) return;
        GhostPass.endFrame(running);
    }

    private static void start(Minecraft mc, TransitionStyle style, Screen from, Screen to) {
        long now = System.nanoTime();
        if (running) {
            if (++chain > MAX_CHAIN) {
                calming = true;
                calmUntil = now + CALM_NANOS;
                return;
            }
        } else {
            if (calming && now - calmUntil < 0L) {
                calmUntil = now + CALM_NANOS;
                return;
            }
            calming = false;
            chain = 0;
        }
        RenderTarget target = mc.gameRenderer.mainRenderTarget();
        GpuTexture color = target.getColorTexture();
        if (color == null || color != presented || !GhostPass.capture(color, target.width, target.height)) {
            running = false;
            return;
        }
        boolean tabs = from instanceof TabScreen && to instanceof TabScreen;
        running = true;
        slide = HontunTheme.terminal() && style == TransitionStyle.FadeZoom && mc.level == null
                && from != null && to != null && (tabs || from.getClass() != to.getClass());
        slideUv = slide ? (float) (SLIDE * mc.getWindow().getGuiScale() / Math.max(1, target.height)) : 0f;
        zoom = !slide && style == TransitionStyle.FadeZoom && mc.level == null && !tabs
                && from != null && to != null && from.getClass() != to.getClass();
        boolean pair = slide && from instanceof WidgetScreen && to instanceof WidgetScreen;
        boolean bar = tabs ? topBar((WidgetScreen) from)
                : pair && holds((WidgetScreen) to, WTopBar.class) && topBar((WidgetScreen) from);
        if (!bar) {
            maskX0 = 2f;
            maskY0 = 2f;
            maskX1 = -1f;
            maskY1 = -1f;
        }
        footUv = pair && holds((WidgetScreen) to, WTermFooter.class) ? footer((WidgetScreen) from) : 0f;
        startNanos = PENDING;
        durationNanos = duration() * 1_000_000L;
    }

    private static boolean topBar(WidgetScreen screen) {
        WContainer root = ((WidgetScreenAccessor) screen).hontun$root();
        if (root == null || root.width <= 0 || root.height <= 0) return false;
        List<Cell<?>> cells = root.cells;
        for (int i = 0; i < cells.size(); i++) {
            WWidget w = cells.get(i).widget();
            if (!(w instanceof WTopBar) || !w.visible || w.width <= 0 || w.height <= 0) continue;
            maskX0 = (float) ((w.x - BAR_PAD) / root.width);
            maskX1 = (float) ((w.x + w.width + BAR_PAD) / root.width);
            maskY0 = (float) (1.0 - (w.y + w.height + BAR_PAD) / root.height);
            maskY1 = (float) (1.0 - (w.y - BAR_PAD) / root.height);
            return true;
        }
        return false;
    }

    private static boolean holds(WidgetScreen screen, Class<? extends WWidget> type) {
        WContainer root = ((WidgetScreenAccessor) screen).hontun$root();
        if (root == null) return false;
        List<Cell<?>> cells = root.cells;
        for (int i = 0; i < cells.size(); i++) {
            WWidget w = cells.get(i).widget();
            if (type.isInstance(w) && w.visible) return true;
        }
        return false;
    }

    private static float footer(WidgetScreen screen) {
        WContainer root = ((WidgetScreenAccessor) screen).hontun$root();
        if (root == null || root.width <= 0 || root.height <= 0) return 0f;
        List<Cell<?>> cells = root.cells;
        for (int i = 0; i < cells.size(); i++) {
            WWidget w = cells.get(i).widget();
            if (!(w instanceof WTermFooter) || !w.visible || w.width <= 0 || w.height <= 0) continue;
            return (float) Math.max(0.0, Math.min(1.0, 1.0 - (w.y - BAR_PAD) / root.height));
        }
        return 0f;
    }

    private static void draw(RenderTarget target) {
        long now = System.nanoTime();
        if (startNanos == PENDING) startNanos = now;
        long elapsed = now - startNanos;
        if (elapsed >= durationNanos || elapsed < 0L) {
            running = false;
            return;
        }
        float inv = 1f - (float) elapsed / durationNanos;
        float eased = 1f - inv * inv;
        float scale = zoom ? 1f + ZOOM * eased : 1f;
        float shift = slide ? slideUv * eased : 0f;
        if (!GhostPass.draw(target, 1f - eased, scale, shift, footUv, maskX0, maskY0, maskX1, maskY1)) running = false;
    }

    private static TransitionStyle style() {
        HontunGuiTheme theme = Hontun.THEME;
        if (theme == null || !HontunTheme.restyleEnabled()) return TransitionStyle.Off;
        TransitionStyle style = theme.screenTransition.get();
        return style == null ? TransitionStyle.Off : style;
    }

    private static long duration() {
        HontunGuiTheme theme = Hontun.THEME;
        Integer ms = theme == null ? null : theme.screenTransitionDuration.get();
        return ms == null ? 220L : Math.max(80, Math.min(600, ms));
    }

    private static boolean applies(Minecraft mc, Screen from, Screen to) {
        if ((from == null || to == null) && mc.level == null) return false;
        return !instant(from) && !instant(to);
    }

    private static boolean instant(Screen screen) {
        return screen instanceof ChatScreen
                || screen instanceof AbstractContainerScreen<?>
                || screen instanceof AbstractSignEditScreen
                || screen instanceof BookEditScreen
                || screen instanceof BookSignScreen
                || screen instanceof BookViewScreen
                || screen instanceof AbstractCommandBlockEditScreen
                || screen instanceof JigsawBlockEditScreen
                || screen instanceof StructureBlockEditScreen
                || screen instanceof TestBlockEditScreen
                || screen instanceof TestInstanceBlockEditScreen
                || screen instanceof DeathScreen
                || screen instanceof WinScreen
                || screen instanceof GameModeSwitcherScreen;
    }

    private static void disable(Throwable t) {
        if (broken) return;
        broken = true;
        running = false;
        presented = null;
        shown = null;
        try {
            GhostPass.shutdown();
        } catch (Throwable ignored) {}
        LOG.warn("[Hontun] Screen transition failed; screens switch instantly for this session", t);
    }
}
