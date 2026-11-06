package cz.honzasik.hontun.gui.theme.style.terminal;

import cz.honzasik.hontun.gui.api.animation.Easing;
import cz.honzasik.hontun.gui.screen.HontunSearchScreen;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.settings.Settings;
import net.minecraft.client.gui.screens.Screen;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public final class TerminalState {
    private static Tab seenTab;
    private static Tab lastTab;

    private static Screen ageScreen;
    private static long openedAt;

    private static final Map<Settings, int[]> CURSORS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<Object, Boolean> DIRTY = Collections.synchronizedMap(new WeakHashMap<>());

    private TerminalState() {}

    public static boolean blinkOn(HontunGuiTheme t) {
        if (!t.terminalCursorBlink.get()) return true;
        return System.currentTimeMillis() % 1060 < 530;
    }

    public static Tab currentTab() {
        Screen screen = mc.gui.screen();
        if (screen instanceof TabScreen ts) return ts.tab;
        if (screen instanceof TerminalModuleScreen || screen instanceof HontunSearchScreen) {
            return Tabs.get().isEmpty() ? null : Tabs.get().getFirst();
        }
        return null;
    }

    public static void observeTab() {
        Tab current = currentTab();
        if (current == null || current == seenTab) return;
        if (seenTab != null) lastTab = seenTab;
        seenTab = current;
    }

    public static Tab lastTab() {
        return lastTab;
    }

    public static double screenAge() {
        Screen screen = mc.gui.screen();
        long now = System.currentTimeMillis();
        if (screen != ageScreen) {
            ageScreen = screen;
            openedAt = now;
        }
        return now - openedAt;
    }

    public static void restartPrint() {
        openedAt = System.currentTimeMillis();
    }

    public static boolean printIn(HontunGuiTheme t, int index) {
        if (!t.terminalPrintAnimation.get()) {
            TerminalPaint.reset();
            return true;
        }

        double age = screenAge() - Math.min(index, 40) * 6;
        if (age >= 220) {
            TerminalPaint.reset();
            return true;
        }

        if (age <= 0) {
            TerminalPaint.fade = 0;
            TerminalPaint.dx = 0;
            return false;
        }

        double e = Easing.QUART_OUT.apply(age / 220.0);
        TerminalPaint.fade = e;
        TerminalPaint.dx = -Math.round(6 * TerminalGrid.of(t).u * (1 - e));
        return true;
    }

    public static int[] cursor(Settings settings) {
        return CURSORS.computeIfAbsent(settings, s -> new int[] { -1 });
    }

    public static boolean dirty(Object key) {
        return key != null && DIRTY.getOrDefault(key, Boolean.FALSE);
    }

    public static void dirty(Object key, boolean value) {
        if (key != null) DIRTY.put(key, value);
    }
}
