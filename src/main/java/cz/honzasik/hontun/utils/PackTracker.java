package cz.honzasik.hontun.utils;

import net.minecraft.client.resources.server.PackLoadFeedback;
import net.minecraft.util.HttpUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;

public final class PackTracker {
    public enum Phase { NONE, WAITING, DOWNLOADING, APPLYING, DONE, DECLINED, FAILED }

    public record View(Phase phase, int anchor, int count, float progress, boolean determinate,
                       long bytes, long startedAt, long endedAt) {}

    private static final class Pack {
        int anchor;
        boolean accepted, downloaded, finished;
        PackLoadFeedback.FinalResult result;
    }

    private static final Map<UUID, Pack> PACKS = new LinkedHashMap<>();
    private static long startedAt;
    private static long endedAt;
    private static int downloadTotal;
    private static int downloadIndex;
    private static volatile long bytes;
    private static volatile long size = -1L;
    private static volatile boolean downloading;
    private static final View NONE = new View(Phase.NONE, -1, 0, 0f, false, 0L, 0L, 0L);

    private PackTracker() {}

    public static synchronized void reset() {
        PACKS.clear();
        startedAt = 0L;
        endedAt = 0L;
        downloadTotal = 0;
        downloadIndex = 0;
        bytes = 0L;
        size = -1L;
        downloading = false;
    }

    public static synchronized void pushed(UUID id) {
        int stage = ConnectTracker.stage();
        if (ConnectTracker.started() == 0L || ConnectTracker.finished() || stage < 2) return;
        Pack p = PACKS.computeIfAbsent(id, k -> new Pack());
        p.anchor = Math.min(stage, ConnectTracker.STAGES - 1);
        if (startedAt == 0L) startedAt = System.currentTimeMillis();
        endedAt = 0L;
    }

    public static synchronized void popped(UUID id) {
        PACKS.remove(id);
    }

    private static synchronized void update(UUID id, PackLoadFeedback.Update update) {
        Pack p = PACKS.get(id);
        if (p == null) return;
        if (update == PackLoadFeedback.Update.ACCEPTED) p.accepted = true;
        else if (update == PackLoadFeedback.Update.DOWNLOADED) p.downloaded = true;
    }

    private static synchronized void result(UUID id, PackLoadFeedback.FinalResult result) {
        Pack p = PACKS.get(id);
        if (p == null) return;
        p.finished = true;
        p.result = result;
        boolean all = true;
        for (Pack q : PACKS.values()) all &= q.finished;
        if (all) endedAt = System.currentTimeMillis();
    }

    private static synchronized void downloadsStarted(int total) {
        downloadTotal = total;
        downloadIndex = 0;
    }

    public static HttpUtil.DownloadProgressListener wrap(HttpUtil.DownloadProgressListener inner, int total) {
        downloadsStarted(total);
        return new HttpUtil.DownloadProgressListener() {
            @Override
            public void requestStart() {
                synchronized (PackTracker.class) {
                    downloadIndex++;
                    bytes = 0L;
                    size = -1L;
                    downloading = true;
                            }
                inner.requestStart();
            }

            @Override
            public void downloadStart(OptionalLong sizeBytes) {
                size = sizeBytes.isPresent() ? sizeBytes.getAsLong() : -1L;
                bytes = 0L;
                inner.downloadStart(sizeBytes);
            }

            @Override
            public void downloadedBytes(long bytesSoFar) {
                bytes = bytesSoFar;
                inner.downloadedBytes(bytesSoFar);
            }

            @Override
            public void requestFinished(boolean success) {
                synchronized (PackTracker.class) {
                    downloading = false;
                            }
                inner.requestFinished(success);
            }
        };
    }

    public static PackLoadFeedback wrap(PackLoadFeedback inner) {
        return new PackLoadFeedback() {
            @Override
            public void reportUpdate(UUID id, PackLoadFeedback.Update result) {
                update(id, result);
                inner.reportUpdate(id, result);
            }

            @Override
            public void reportFinalResult(UUID id, PackLoadFeedback.FinalResult result) {
                PackTracker.result(id, result);
                ResourcePackInfo.result(id, result);
                inner.reportFinalResult(id, result);
            }
        };
    }

    public static synchronized View view() {
        if (PACKS.isEmpty()) return NONE;
        int anchor = -1, finished = 0, applied = 0, declined = 0, failed = 0, accepted = 0, downloaded = 0;
        for (Pack p : PACKS.values()) {
            anchor = anchor < 0 ? p.anchor : Math.min(anchor, p.anchor);
            if (p.accepted) accepted++;
            if (p.downloaded) downloaded++;
            if (p.finished) {
                finished++;
                if (p.result == PackLoadFeedback.FinalResult.APPLIED) applied++;
                else if (p.result == PackLoadFeedback.FinalResult.DECLINED) declined++;
                else if (p.result != PackLoadFeedback.FinalResult.DISCARDED) failed++;
            }
        }
        int count = PACKS.size();
        Phase phase;
        float progress = 0f;
        boolean determinate = false;
        long b = bytes, total = size;
        if (finished == count) {
            if (failed > 0) phase = Phase.FAILED;
            else if (applied == 0 && declined > 0) phase = Phase.DECLINED;
            else phase = Phase.DONE;
            progress = 1f;
            determinate = true;
        } else if (accepted == 0) {
            phase = Phase.WAITING;
        } else if (downloaded >= accepted && !downloading) {
            phase = Phase.APPLYING;
        } else {
            phase = Phase.DOWNLOADING;
            int n = Math.max(1, Math.max(downloadTotal, accepted));
            float current = total > 0L ? Math.min(1f, b / (float) total) : 0f;
            int doneBefore = Math.max(0, Math.min(n, downloadIndex - (downloading ? 1 : 0)));
            progress = Math.min(1f, (doneBefore + (downloading ? current : 0f)) / n);
            determinate = total > 0L || n > 1;
        }
        return new View(phase, anchor, count, progress, determinate, b, startedAt, endedAt);
    }
}
