package cz.honzasik.hontun.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class ChannelPayloads {
    public record Payload(String channel, byte[] data, int length, int count, long last) {}

    private static final Map<String, Payload> LAST = new ConcurrentHashMap<>();
    private static final int MAX_CHANNELS = 256;
    public static final String OVERSIZE = "payload not kept (over " + MAX_CHANNELS + " channels)";
    private static final AtomicInteger SKIPPED = new AtomicInteger();

    private ChannelPayloads() {}

    public static void record(String channel, byte[] data, int length) {
        if (LAST.size() >= MAX_CHANNELS && !LAST.containsKey(channel)) {
            SKIPPED.incrementAndGet();
            Oversize.item(OVERSIZE, channel + " (" + length + " B)");
            return;
        }
        long now = System.currentTimeMillis();
        LAST.merge(channel, new Payload(channel, data, length, 1, now),
                (old, cur) -> new Payload(channel, cur.data, cur.length, old.count + 1, now));
    }

    public static Payload get(String channel) {
        return LAST.get(channel);
    }

    public static boolean has(String channel) {
        return LAST.containsKey(channel);
    }

    public static int skipped() {
        return SKIPPED.get();
    }

    public static int limit() {
        return MAX_CHANNELS;
    }

    public static void clear() {
        SKIPPED.set(0);
        LAST.clear();
    }
}
