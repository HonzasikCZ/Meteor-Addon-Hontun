package cz.honzasik.hontun.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ConnectTracker {
    public record Step(String name, String key, long at) {}

    public static final int STAGES = 4;

    private static final List<Step> STEPS = new CopyOnWriteArrayList<>();
    private static final long[] STAGE_START = new long[STAGES];
    private static volatile long started = 0L;
    private static volatile String target = "";
    private static volatile int stage = -1;
    private static volatile boolean loginSeen;
    private static volatile boolean loginSkipped;
    private static volatile boolean transfer;
    private static volatile long finishedAt;
    private static volatile int owner;

    private ConnectTracker() {}

    public static void reset(String serverAddress) {
        reset(serverAddress, null);
    }

    public static synchronized void reset(String serverAddress, Component firstStep) {
        STEPS.clear();
        started = System.currentTimeMillis();
        target = serverAddress == null ? "" : serverAddress;
        stage = -1;
        loginSeen = false;
        loginSkipped = false;
        transfer = false;
        finishedAt = 0L;
        for (int i = 0; i < STAGES; i++) STAGE_START[i] = 0L;
        if (firstStep != null) step(firstStep);
    }

    public static void bind(Object screen) {
        owner = System.identityHashCode(screen);
    }

    public static void step(Object screen, Component status) {
        if (System.identityHashCode(screen) == owner) step(status);
    }

    public static synchronized void step(Component status) {
        if (status == null) return;
        String name = status.getString();
        if (name.isEmpty()) return;
        long now = System.currentTimeMillis();
        if (started == 0L) started = now;
        String key = keyOf(status);
        List<Step> s = STEPS;
        if (s.isEmpty() || !s.get(s.size() - 1).name().equals(name)) s.add(new Step(name, key, now));
        if (key.equals("connect.transferring") && stage < 0) transfer = true;
        int st = stageOf(key);
        if (st == 1) loginSeen = true;
        if (st == 2 && !loginSeen && stage < 2) loginSkipped = true;
        advance(st, now);
    }

    public static synchronized void enterWorld() {
        if (finishedAt != 0L) return;
        advance(3, System.currentTimeMillis());
    }

    public static synchronized void finish() {
        if (started != 0L && finishedAt == 0L) finishedAt = System.currentTimeMillis();
    }

    private static void advance(int st, long now) {
        if (st <= stage) return;
        for (int k = Math.max(0, stage + 1); k <= st; k++) {
            if (STAGE_START[k] == 0L) STAGE_START[k] = now;
        }
        stage = st;
    }

    public static int stageOf(String key) {
        return switch (key) {
            case "connect.connecting", "connect.transferring" -> 0;
            case "connect.authorizing", "connect.encrypting", "connect.negotiating" -> 1;
            case "connect.joining", "connect.reconfiguring", "connect.reconfiging" -> 2;
            default -> -1;
        };
    }

    private static String keyOf(Component c) {
        return c.getContents() instanceof TranslatableContents t ? t.getKey() : "";
    }

    public static List<Step> steps() { return STEPS; }
    public static long started() { return started; }
    public static String target() { return target; }
    public static int stage() { return stage; }
    public static boolean loginSkipped() { return loginSkipped; }
    public static boolean transfer() { return transfer; }
    public static boolean finished() { return finishedAt != 0L; }

    public static long stageStart(int i) {
        return i < 0 || i >= STAGES ? 0L : STAGE_START[i];
    }

    public static long stageMs(int i) {
        if (i < 0 || i >= STAGES || STAGE_START[i] == 0L) return 0L;
        long end;
        if (i < stage && i + 1 < STAGES && STAGE_START[i + 1] != 0L) end = STAGE_START[i + 1];
        else end = finishedAt != 0L ? finishedAt : System.currentTimeMillis();
        return Math.max(0L, end - STAGE_START[i]);
    }

    public static long lastStepAt() {
        List<Step> s = STEPS;
        long last = s.isEmpty() ? started : s.get(s.size() - 1).at();
        for (int i = STAGES - 1; i >= 0; i--) {
            if (STAGE_START[i] != 0L) return Math.max(last, STAGE_START[i]);
        }
        return last;
    }

    public static Step current() {
        List<Step> s = STEPS;
        return s.isEmpty() ? null : s.get(s.size() - 1);
    }

    public static long durationOf(int i) {
        List<Step> s = STEPS;
        if (i < 0 || i >= s.size()) return 0L;
        long end = (i + 1 < s.size()) ? s.get(i + 1).at() : System.currentTimeMillis();
        return Math.max(0L, end - s.get(i).at());
    }

    public static long totalMs() {
        if (started == 0L) return 0L;
        return (finishedAt != 0L ? finishedAt : System.currentTimeMillis()) - started;
    }
}
