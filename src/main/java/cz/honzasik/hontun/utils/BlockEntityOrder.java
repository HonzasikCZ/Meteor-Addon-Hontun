package cz.honzasik.hontun.utils;

import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class BlockEntityOrder {
    public record Stats(int chunks, int both, int vanillaOnly, int leafOnly, int neither, int vanillaK, int leafK,
                        int vanillaMax, int leafMax, int sorted, int unsortedNeither) {
        public boolean leafHash() {
            return neither == 0 && vanillaOnly == 0 && leafOnly >= 1 && (leafMax >= 6 || (leafOnly >= 2 && leafK >= 8));
        }

        public boolean vanillaHash() {
            return neither == 0 && leafOnly == 0 && vanillaOnly >= 1 && (vanillaMax >= 6 || (vanillaOnly >= 2 && vanillaK >= 8));
        }

        public boolean sortedYzx() {
            return sorted >= 2 && vanillaOnly == 0 && leafOnly == 0 && unsortedNeither == 0;
        }
    }

    private static final int MIN_K = 2;
    private static final int MAX_K = 96;
    private static final int CHUNK_CAP = 64;
    private static final int SEEN_CAP = 4096;
    private static final int PHI = 0x9E3779B9;
    private static final int VANILLA = 1;
    private static final int LEAF = 2;
    private static final int SORTED = 4;

    private static final Object LOCK = new Object();
    private static final Set<Long> seen = new HashSet<>();
    private static final List<int[]> chunks = new ArrayList<>();
    private static final byte[] kinds = new byte[CHUNK_CAP];
    private static int classified;
    private static int generation;
    private static String level;
    private static final int[] buffer = new int[MAX_K * 3];
    private static int count;
    private static final ClientboundLevelChunkPacketData.BlockEntityTagOutput OUTPUT = (pos, type, tag) -> {
        if (count < MAX_K) {
            buffer[count * 3] = pos.getX();
            buffer[count * 3 + 1] = pos.getY();
            buffer[count * 3 + 2] = pos.getZ();
        }
        if (count <= MAX_K) count++;
    };

    private BlockEntityOrder() {}

    public static void reset() {
        synchronized (LOCK) {
            seen.clear();
            chunks.clear();
            classified = 0;
            generation++;
            level = null;
        }
    }

    public static void newWorld(String dimension) {
        synchronized (LOCK) {
            if (!dimension.equals(level)) seen.clear();
            level = dimension;
        }
    }

    public static void capture(int chunkX, int chunkZ, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> source) {
        synchronized (LOCK) {
            if (chunks.size() >= CHUNK_CAP || seen.size() >= SEEN_CAP) return;
            if (!seen.add(((long) chunkX << 32) ^ (chunkZ & 0xFFFFFFFFL))) return;
            count = 0;
            try {
                source.accept(OUTPUT);
            } catch (Throwable ignored) {
                return;
            }
            if (count >= MIN_K && count <= MAX_K) chunks.add(Arrays.copyOf(buffer, count * 3));
        }
    }

    public static Stats stats() {
        int[][] snapshot;
        byte[] known;
        int from, gen;
        synchronized (LOCK) {
            snapshot = chunks.toArray(new int[0][]);
            known = Arrays.copyOf(kinds, snapshot.length);
            from = classified;
            gen = generation;
        }
        for (int i = from; i < snapshot.length; i++) known[i] = (byte) classify(snapshot[i]);
        synchronized (LOCK) {
            if (gen == generation && snapshot.length > classified) {
                System.arraycopy(known, classified, kinds, classified, snapshot.length - classified);
                classified = snapshot.length;
            }
        }
        int both = 0, vanillaOnly = 0, leafOnly = 0, neither = 0, vanillaK = 0, leafK = 0, vanillaMax = 0, leafMax = 0;
        int sorted = 0, unsortedNeither = 0;
        for (int i = 0; i < snapshot.length; i++) {
            int k = snapshot[i].length / 3;
            boolean vanilla = (known[i] & VANILLA) != 0, leaf = (known[i] & LEAF) != 0;
            if (vanilla && leaf) {
                both++;
            } else if (vanilla) {
                vanillaOnly++;
                vanillaK += k;
                vanillaMax = Math.max(vanillaMax, k);
            } else if (leaf) {
                leafOnly++;
                leafK += k;
                leafMax = Math.max(leafMax, k);
            } else {
                neither++;
                if (k >= 4 && (known[i] & SORTED) != 0) sorted++;
                else if (k >= 4) unsortedNeither++;
            }
        }
        return new Stats(snapshot.length, both, vanillaOnly, leafOnly, neither, vanillaK, leafK, vanillaMax, leafMax, sorted, unsortedNeither);
    }

    public static String debug() {
        Stats s = stats();
        int examined;
        synchronized (LOCK) {
            examined = seen.size();
        }
        return s.chunks() + "/" + examined + " both=" + s.both() + " vanilla=" + s.vanillaOnly() + ":" + s.vanillaK()
                + " leaf=" + s.leafOnly() + ":" + s.leafK() + " neither=" + s.neither() + " sorted=" + s.sorted() + "/" + s.unsortedNeither();
    }

    private static int classify(int[] p) {
        int k = p.length / 3;
        int kind = 0;
        if (fits(p, k, false)) kind |= VANILLA;
        if (fits(p, k, true)) kind |= LEAF;
        if (sortedYzx(p, k)) kind |= SORTED;
        return kind;
    }

    private static int hash(int x, int y, int z, boolean leaf) {
        if (!leaf) return (y + z * 31) * 31 + x;
        int h = (x * 31337 + y * 961 + z) * PHI;
        return h ^ (h >>> 16);
    }

    private static int mix(int h) {
        int m = h * PHI;
        return m ^ (m >>> 16);
    }

    private static boolean fits(int[] p, int k, boolean leaf) {
        int[] mixed = new int[k];
        for (int i = 0; i < k; i++) {
            int j = (k - 1 - i) * 3;
            mixed[i] = mix(hash(p[j], p[j + 1], p[j + 2], leaf));
        }
        for (int n = 32; n == 32 || n * 3 <= k * 16; n <<= 1) {
            if (k * 4 <= n * 3 && fitsTable(mixed, k, n)) return true;
        }
        return false;
    }

    private static boolean fitsTable(int[] mixed, int k, int n) {
        int[] home = new int[k];
        for (int i = 0; i < k; i++) home[i] = mixed[i] & (n - 1);
        int[] slot = new int[k];
        int[] filled = new int[n + 1];
        for (int wrapped = 0; wrapped <= k; wrapped++) {
            if (assign(home, slot, k, n, wrapped) && walksFilled(home, slot, filled, k, n)) return true;
        }
        return false;
    }

    private static boolean assign(int[] home, int[] slot, int k, int n, int wrapped) {
        int prev = -1;
        for (int i = 0; i < k; i++) {
            int s = Math.max(i < wrapped ? home[i] - n : home[i], prev + 1);
            if (s >= n) return false;
            slot[i] = s;
            prev = s;
        }
        return true;
    }

    private static boolean walksFilled(int[] home, int[] slot, int[] filled, int k, int n) {
        Arrays.fill(filled, 0);
        for (int i = 0; i < k; i++) filled[slot[i] + 1] = 1;
        for (int i = 0; i < n; i++) filled[i + 1] += filled[i];
        for (int i = 0; i < k; i++) {
            int h = home[i], s = slot[i];
            int length = s >= h ? s - h + 1 : n - h + s + 1;
            int occupied = s >= h ? filled[s + 1] - filled[h] : filled[n] - filled[h] + filled[s + 1];
            if (occupied != length) return false;
        }
        return true;
    }

    private static boolean sortedYzx(int[] p, int k) {
        long prev = Long.MIN_VALUE;
        for (int i = 0; i < k; i++) {
            long key = ((long) p[i * 3 + 1] << 8) | ((p[i * 3 + 2] & 15) << 4) | (p[i * 3] & 15);
            if (key < prev) return false;
            prev = key;
        }
        return true;
    }
}
