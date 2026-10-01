package cz.honzasik.hontun.gui.theme.style.flat;

import cz.honzasik.hontun.gui.theme.style.AnimSpec;

import java.util.Map;
import java.util.WeakHashMap;

final class FlatAnim {
    static final int HOVER = 0;
    static final int FOCUS = 1;
    static final int DRAG = 2;
    static final int PRESS = 3;

    private static final int CHANNELS = 4;
    private static final Map<Object, Slot[]> SLOTS = new WeakHashMap<>();

    private FlatAnim() {}

    static double tween(Object owner, int channel, boolean on, AnimSpec spec) {
        Slot[] slots = SLOTS.computeIfAbsent(owner, k -> new Slot[CHANNELS]);
        Slot slot = slots[channel];
        long now = System.nanoTime();

        if (slot == null) {
            slot = new Slot();
            slot.progress = on ? 1 : 0;
            slot.time = now;
            slots[channel] = slot;
        } else {
            double ms = on ? spec.inMs() : spec.outMs();
            double dt = (now - slot.time) / 1_000_000.0;
            slot.time = now;

            if (ms <= 0) slot.progress = on ? 1 : 0;
            else slot.progress = Math.clamp(slot.progress + (on ? dt : -dt) / ms, 0.0, 1.0);
        }

        if (slot.progress <= 0) return 0;
        if (slot.progress >= 1) return 1;
        return Math.clamp(spec.easing().apply(slot.progress), 0.0, 1.0);
    }

    private static final class Slot {
        private double progress;
        private long time;
    }
}
