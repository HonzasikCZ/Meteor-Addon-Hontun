package cz.honzasik.hontun.utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ConnectTracker {
    public record Step(String name, long at) {}

    private static final List<Step> STEPS = new CopyOnWriteArrayList<>();
    private static volatile long started = 0L;
    private static volatile String target = "";

    private ConnectTracker() {}

    public static void reset(String serverAddress) {
        reset(serverAddress, null);
    }

    public static void reset(String serverAddress, String firstStep) {
        STEPS.clear();
        started = System.currentTimeMillis();
        target = serverAddress == null ? "" : serverAddress;
        if (firstStep != null && !firstStep.isEmpty()) {
            STEPS.add(new Step(firstStep, started));
        }
    }

    public static void step(String name) {
        if (name == null || name.isEmpty()) return;
        if (started == 0L) started = System.currentTimeMillis();
        List<Step> s = STEPS;
        if (!s.isEmpty() && s.get(s.size() - 1).name().equals(name)) return;
        s.add(new Step(name, System.currentTimeMillis()));
    }

    public static List<Step> steps() { return STEPS; }
    public static long started() { return started; }
    public static String target() { return target; }

    public static long durationOf(int i) {
        List<Step> s = STEPS;
        if (i < 0 || i >= s.size()) return 0L;
        long end = (i + 1 < s.size()) ? s.get(i + 1).at() : System.currentTimeMillis();
        return Math.max(0L, end - s.get(i).at());
    }

    public static long totalMs() {
        return started == 0L ? 0L : System.currentTimeMillis() - started;
    }
}
