package cz.honzasik.hontun.gui.join;

import cz.honzasik.hontun.gui.render.RoundedGui;
import cz.honzasik.hontun.utils.ConnectTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.progress.ChunkLoadStatusView;
import net.minecraft.world.level.Level;

public final class JoinView {
    public enum Mode { CONNECT, JOIN, LOCAL, TRAVEL }

    private static final long EPOCH = System.nanoTime();
    private static final long HANDOFF_GAP = 5_000_000_000L;
    private static final JoinState STATE = new JoinState();

    private static long lastNanos;
    private static Mode lastMode;
    private static int lastOwner;
    private static int lastStage = -2;

    private JoinView() {}

    public static Mode loadingMode(LevelLoadingScreen.Reason reason, ChunkLoadStatusView view) {
        if (reason != LevelLoadingScreen.Reason.OTHER) return Mode.TRAVEL;
        Minecraft mc = Minecraft.getInstance();
        if (view != null || mc.getSingleplayerServer() != null) return Mode.LOCAL;
        if (ConnectTracker.started() == 0L || ConnectTracker.finished()) return Mode.TRAVEL;
        boolean continuing = (lastMode == Mode.CONNECT || lastMode == Mode.JOIN)
                && System.nanoTime() - lastNanos < HANDOFF_GAP;
        boolean fresh = System.currentTimeMillis() - ConnectTracker.lastStepAt() < 30_000L;
        return continuing || fresh ? Mode.JOIN : Mode.TRAVEL;
    }

    public static int[] cancelBounds(int w, int h) {
        return JoinCardView.cancel(w, h);
    }

    public static void backdrop(GuiGraphicsExtractor g, int w, int h, Mode mode, ChunkLoadStatusView view) {
        JoinCardView.background(g, w, h, mode, view);
    }

    public static void render(GuiGraphicsExtractor g, Object owner, int w, int h, Mode mode, float progress, boolean hasProgress,
                              LevelLoadingScreen.Reason reason, ChunkLoadStatusView view) {
        long nanos = System.nanoTime();
        long now = System.currentTimeMillis();
        int ownerId = System.identityHashCode(owner);
        boolean handoff = lastMode == Mode.CONNECT && mode == Mode.JOIN && nanos - lastNanos < HANDOFF_GAP;
        boolean same = lastNanos != 0L && (ownerId == lastOwner || handoff);
        float dt = lastNanos == 0L ? 0f : Math.max(0f, Math.min(0.1f, (nanos - lastNanos) / 1e9f));
        Mode previous = lastMode;
        lastNanos = nanos;
        lastMode = mode;
        lastOwner = ownerId;

        if (mode == Mode.JOIN) ConnectTracker.enterWorld();
        int stage = mode == Mode.CONNECT || mode == Mode.JOIN ? ConnectTracker.stage() : -1;

        JoinState s = STATE;
        s.mode = mode;
        s.reason = reason;
        s.view = view;
        s.w = w;
        s.h = h;
        s.tier = h < 250 ? 0 : (h < 400 ? 1 : 2);
        s.cx = w / 2f;
        s.cy = h / 2f;
        s.t = (nanos - EPOCH) * 1e-9;
        s.now = now;
        s.sdf = RoundedGui.available();
        s.font = Minecraft.getInstance().font;

        if (!same) {
            s.sessionStart = now;
            lastStage = stage;
            s.advancedAt = 0L;
            for (int i = 0; i < JoinState.STAGES; i++) {
                s.doneAt[i] = 0L;
                s.fromFill[i] = 0f;
                s.drawnFill[i] = 0f;
            }
            s.title = null;
            s.prevTitle = null;
            s.titleAt = 0L;
            s.status = null;
            s.prevStatus = null;
            s.statusAt = 0L;
            s.progress = 0f;
            s.ghostAt = 0L;
        } else if (stage > lastStage) {
            s.advancedAt = now;
            for (int i = Math.max(0, lastStage); i < stage && i < JoinState.STAGES; i++) {
                if (s.doneAt[i] == 0L) {
                    s.doneAt[i] = now;
                    s.fromFill[i] = s.drawnFill[i];
                }
            }
            lastStage = stage;
        }
        if (same && previous == Mode.CONNECT && mode == Mode.JOIN) s.ghostAt = now;

        s.stage = stage;
        s.loginSkipped = ConnectTracker.loginSkipped();
        s.transfer = ConnectTracker.transfer();
        s.hasProgress = hasProgress;
        if (hasProgress) {
            float target = Math.max(s.progress, Math.min(1f, Math.max(0f, progress)));
            s.progress += (target - s.progress) * (1f - (float) Math.exp(-dt / 0.15f));
        }
        s.elapsedMs = mode == Mode.CONNECT || mode == Mode.JOIN ? ConnectTracker.totalMs() : now - s.sessionStart;

        String title = titleText(mode, reason);
        if (!title.equals(s.title)) {
            if (s.title != null) {
                s.prevTitle = s.title;
                s.titleAt = now;
            }
            s.title = title;
        }
        String status = statusText(mode);
        if (!status.equals(s.status)) {
            if (s.status != null) {
                s.prevStatus = s.status;
                s.statusAt = now;
            }
            s.status = status;
        }
        if (mode == Mode.CONNECT) s.cancelRect = JoinCardView.cancel(w, h);

        JoinCardView.render(g, s);
    }

    private static String titleText(Mode mode, LevelLoadingScreen.Reason reason) {
        if (mode == Mode.TRAVEL) return dimensionName(reason);
        if (mode == Mode.LOCAL) {
            String name = levelName();
            return name == null || name.isBlank() ? "Singleplayer" : name;
        }
        String target = ConnectTracker.target();
        if (target.endsWith(":25565")) target = target.substring(0, target.length() - 6);
        return target.isEmpty() ? "Connecting" : target;
    }

    private static String dimensionName(LevelLoadingScreen.Reason reason) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            ResourceKey<Level> dim = level.dimension();
            if (dim == Level.OVERWORLD) return "Overworld";
            if (dim == Level.NETHER) return "Nether";
            if (dim == Level.END) return "The End";
            String path = dim.identifier().getPath().replace('_', ' ');
            if (!path.isEmpty()) return Character.toUpperCase(path.charAt(0)) + path.substring(1);
        }
        if (reason == LevelLoadingScreen.Reason.NETHER_PORTAL) return "Nether";
        if (reason == LevelLoadingScreen.Reason.END_PORTAL) return "The End";
        return "Loading";
    }

    private static String levelName() {
        Object server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) return null;
        try {
            Object data = server.getClass().getMethod("getWorldData").invoke(server);
            Object name = data.getClass().getMethod("getLevelName").invoke(data);
            return name instanceof String str ? str : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String statusText(Mode mode) {
        String raw;
        if (mode == Mode.CONNECT) {
            ConnectTracker.Step step = ConnectTracker.current();
            raw = step == null ? Component.translatable("connect.connecting").getString() : step.name();
        } else {
            raw = Component.translatable("multiplayer.downloadingTerrain").getString();
        }
        String out = raw.strip();
        while (!out.isEmpty()) {
            char c = out.charAt(out.length() - 1);
            if (c == '.' || c == '…' || Character.isWhitespace(c)) out = out.substring(0, out.length() - 1);
            else break;
        }
        return out;
    }
}
