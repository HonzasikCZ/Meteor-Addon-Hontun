package cz.honzasik.hontun.utils;

import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public final class SidebarCache {
    public static final int ROWS = 15;
    public static final int MAX_HOLDERS = 100_000;
    public static final long MAX_OWNER_CHARS = 32L * 1024 * 1024;

    private static final int THROTTLE = 8;
    private static final AtomicInteger VERSION = new AtomicInteger();

    private static WeakReference<Objective> objective = new WeakReference<>(null);
    private static int version;
    private static List<PlayerScoreEntry> rows = List.of();
    private static long refreshAfter;

    private record Ranked(PlayerScoreEntry entry, int index) {}

    private SidebarCache() {}

    public static void changed() {
        VERSION.incrementAndGet();
    }

    public static Collection<PlayerScoreEntry> top(Objective target, Comparator<? super PlayerScoreEntry> order, Supplier<Collection<PlayerScoreEntry>> all) {
        int current = VERSION.get();
        long now = System.nanoTime();
        if (objective.get() == target && (version == current || now - refreshAfter < 0)) return rows;

        List<PlayerScoreEntry> top = select(all.get(), order);
        long done = System.nanoTime();
        objective = new WeakReference<>(target);
        version = current;
        rows = top;
        refreshAfter = done + (done - now) * THROTTLE;
        return rows;
    }

    private static List<PlayerScoreEntry> select(Collection<PlayerScoreEntry> entries, Comparator<? super PlayerScoreEntry> order) {
        Comparator<Ranked> rank = (a, b) -> {
            int c = order.compare(a.entry(), b.entry());
            return c != 0 ? c : Integer.compare(a.index(), b.index());
        };
        PriorityQueue<Ranked> worst = new PriorityQueue<>(ROWS + 1, rank.reversed());
        int index = 0;
        for (PlayerScoreEntry entry : entries) {
            int at = index++;
            if (entry.isHidden()) continue;
            Ranked ranked = new Ranked(entry, at);
            if (worst.size() < ROWS) {
                worst.add(ranked);
            } else if (rank.compare(ranked, worst.peek()) < 0) {
                worst.poll();
                worst.add(ranked);
            }
        }
        List<Ranked> best = new ArrayList<>(worst);
        best.sort(rank);
        List<PlayerScoreEntry> result = new ArrayList<>(best.size());
        for (Ranked ranked : best) result.add(ranked.entry());
        return List.copyOf(result);
    }
}
