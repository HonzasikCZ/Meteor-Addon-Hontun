package cz.honzasik.hontun.gui.join;

import cz.honzasik.hontun.utils.ConnectTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.server.level.progress.ChunkLoadStatusView;

public final class JoinState {
    public static final int STAGES = ConnectTracker.STAGES;
    private static final String[] LABELS = {"Connect", "Login", "Join", "World"};

    public JoinView.Mode mode;
    public LevelLoadingScreen.Reason reason;
    public ChunkLoadStatusView view;
    public int w, h, tier;
    public float cx, cy;
    public double t;
    public long now, sessionStart;
    public boolean sdf;
    public Font font;

    public String title, prevTitle;
    public long titleAt;
    public String status, prevStatus;
    public long statusAt;
    public long elapsedMs;

    public int stage = -1;
    public boolean loginSkipped, transfer;
    public boolean hasProgress;
    public float progress;

    public final long[] doneAt = new long[STAGES];
    public final float[] fromFill = new float[STAGES];
    public final float[] drawnFill = new float[STAGES];
    public long advancedAt;
    public long ghostAt;
    public int[] cancelRect;

    public boolean track() {
        return mode == JoinView.Mode.CONNECT || mode == JoinView.Mode.JOIN;
    }

    public boolean loading() {
        return mode != JoinView.Mode.CONNECT;
    }

    public float ent(int delayMs) {
        return JoinDraw.eoc(JoinDraw.cl((now - sessionStart - delayMs) / 420f));
    }

    public float since(long at, float ms) {
        return at == 0L ? 1f : JoinDraw.cl((now - at) / ms);
    }

    public boolean skipped(int i) {
        return i == 1 && loginSkipped && stage >= 2;
    }

    public boolean done(int i) {
        return !skipped(i) && stage > i;
    }

    public boolean active(int i) {
        return stage == i;
    }

    public boolean pending(int i) {
        return stage < i;
    }

    public String label(int i) {
        return i == 0 && transfer ? "Transfer" : LABELS[i];
    }

    public long stageMs(int i) {
        return ConnectTracker.stageMs(i);
    }

    public String stageText(int i, boolean compact) {
        if (skipped(i)) return "offline";
        if (i == 3 && active(i) && hasProgress) return (int) Math.floor(progress * 100f) + "%";
        if (done(i) || active(i)) return JoinDraw.formatStage(stageMs(i), compact);
        return null;
    }

    public float fill(int i) {
        float f;
        if (stage > i) {
            f = doneAt[i] == 0L ? 1f : JoinDraw.lerp(fromFill[i], 1f, JoinDraw.eoc(since(doneAt[i], 260f)));
        } else if (stage == i) {
            if (i == 3 && hasProgress) {
                f = progress;
            } else {
                long start = ConnectTracker.stageStart(i);
                float tau = start == 0L ? 0f : (now - start) / 1000f;
                f = 0.85f * (1f - (float) Math.exp(-tau / 1.6f));
            }
        } else {
            f = 0f;
        }
        drawnFill[i] = f;
        return f;
    }

    public float overall() {
        if (!track()) return hasProgress ? progress : 0f;
        float sum = 0f;
        for (int i = 0; i < STAGES; i++) sum += stage > i ? 1f : (stage == i ? fill(i) : 0f);
        return JoinDraw.cl(sum / STAGES);
    }

    public int stageNumber() {
        return Math.max(1, Math.min(STAGES, stage + 1));
    }
}
