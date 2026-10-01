package cz.honzasik.hontun.gui.theme.style.smog;

import cz.honzasik.hontun.gui.api.animation.Easing;
import cz.honzasik.hontun.gui.theme.style.AnimSpec;

import java.util.Map;
import java.util.WeakHashMap;

final class SmogAnim {
    static final int HOVER = 0;
    static final int PRESS = 1;
    static final int FOCUS = 2;
    static final int DRAG = 3;
    static final int ON = 4;
    static final int AUX = 5;

    private static final int CHANNELS = 6;
    private static final Map<Object, Slot[]> SLOTS = new WeakHashMap<>();

    private SmogAnim() {}

    static double tween(Object owner, int channel, boolean on, AnimSpec spec) {
        return tween(owner, channel, on, spec.easing(), spec.inMs(), spec.outMs());
    }

    static double tween(Object owner, int channel, boolean on, Easing easing, int inMs, int outMs) {
        Slot slot = slot(owner, channel, on);
        long now = System.nanoTime();

        double ms = on ? inMs : outMs;
        double dt = (now - slot.time) / 1_000_000.0;
        slot.time = now;

        if (ms <= 0) slot.progress = on ? 1 : 0;
        else slot.progress = Math.clamp(slot.progress + (on ? dt : -dt) / ms, 0.0, 1.0);

        if (slot.progress <= 0) return 0;
        if (slot.progress >= 1) return 1;
        return Math.clamp((easing == null ? Easing.LINEAR : easing).apply(slot.progress), 0.0, 1.0);
    }

    static double sinceChange(Object owner, int channel, boolean state) {
        Slot slot = slot(owner, channel, state);
        long now = System.nanoTime();

        if (slot.state != state) {
            slot.state = state;
            slot.changed = now;
        }

        if (slot.changed < 0) return Double.MAX_VALUE;
        return (now - slot.changed) / 1_000_000.0;
    }

    private static Slot slot(Object owner, int channel, boolean on) {
        Slot[] slots = SLOTS.computeIfAbsent(owner, k -> new Slot[CHANNELS]);
        Slot slot = slots[channel];

        if (slot == null) {
            slot = new Slot();
            slot.progress = on ? 1 : 0;
            slot.time = System.nanoTime();
            slot.state = on;
            slot.changed = -1;
            slots[channel] = slot;
        }

        return slot;
    }

    private static final class Slot {
        private double progress;
        private long time;
        private boolean state;
        private long changed;
    }
}
