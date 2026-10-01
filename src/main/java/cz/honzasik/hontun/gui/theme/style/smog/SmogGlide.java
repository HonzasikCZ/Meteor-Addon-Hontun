package cz.honzasik.hontun.gui.theme.style.smog;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import static meteordevelopment.meteorclient.MeteorClient.mc;

final class SmogGlide {
    private static final Map<Object, SmogGlide> ALL = new WeakHashMap<>();

    private static final double TAU = 0.035;
    private static final double FADE_IN = 0.090;
    private static final double FADE_OUT = 0.160;
    private static final double FLOOR = 0.35;
    private static final long JUMP_AFTER = 250_000_000L;

    private double x, y, w, h;
    private double alpha;
    private double fadeIn;
    private boolean shown;
    private boolean placed;
    private long last = -1;
    private long lostAt = -1;
    private WeakReference<Object> target;
    private double targetY;
    private WeakReference<Object> screen;

    private SmogGlide() {}

    static SmogGlide of(Object owner) {
        return ALL.computeIfAbsent(owner, k -> new SmogGlide());
    }

    void track(Object t, double tx, double ty, double tw, double th, double jumpDistance) {
        long now = System.nanoTime();
        double dt = last < 0 ? 0 : Math.min(0.1, (now - last) / 1_000_000_000.0);
        last = now;

        Object current = mc != null && mc.gui != null ? mc.gui.screen() : null;
        if (current != deref(screen)) {
            screen = current != null ? new WeakReference<>(current) : null;
            reset();
            dt = 0;
        }

        if (t == null) {
            if (shown) {
                shown = false;
                lostAt = now;
            }

            alpha = Math.max(0, alpha - dt / FADE_OUT);
            target = null;
            return;
        }

        double dx = (tx + tw / 2) - (x + w / 2);
        double dy = (ty + th / 2) - (y + h / 2);

        boolean jump = !placed
                || (!shown && lostAt >= 0 && now - lostAt > JUMP_AFTER)
                || Math.sqrt(dx * dx + dy * dy) > jumpDistance;

        if (jump) {
            x = tx;
            y = ty;
            w = tw;
            h = th;
            placed = true;
        }
        else {
            if (t == deref(target)) y += ty - targetY;

            double k = 1 - Math.exp(-dt / TAU);
            x += (tx - x) * k;
            y += (ty - y) * k;
            w += (tw - w) * k;
            h += (th - h) * k;
        }

        if (!shown) {
            shown = true;
            lostAt = -1;

            if (alpha <= FLOOR) fadeIn = 0;
            else fadeIn = 1 - Math.cbrt(1 - Math.clamp((alpha - FLOOR) / (1 - FLOOR), 0.0, 1.0));
        }

        fadeIn = Math.min(1, fadeIn + dt / FADE_IN);
        alpha = FLOOR + (1 - FLOOR) * (1 - Math.pow(1 - fadeIn, 3));

        if (t != deref(target)) target = new WeakReference<>(t);
        targetY = ty;
    }

    void reset() {
        alpha = 0;
        fadeIn = 0;
        shown = false;
        placed = false;
        lostAt = -1;
        target = null;
    }

    boolean visible() {
        return placed && alpha > 0.002;
    }

    private static Object deref(WeakReference<Object> ref) {
        return ref != null ? ref.get() : null;
    }

    double alpha() {
        return alpha;
    }

    double x() {
        return x;
    }

    double y() {
        return y;
    }

    double w() {
        return w;
    }

    double h() {
        return h;
    }
}
