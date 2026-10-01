package cz.honzasik.hontun.utils;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FreshFreeze {
    public record Stats(int band, int bandMoved, int bandOdd, int bandOddMoved, int bandEven, int bandEvenMoved,
                        int control, int controlMoved, double reach) {
        public double bandRate() {
            return band == 0 ? 0 : (double) bandMoved / band;
        }

        public double controlRate() {
            return control == 0 ? 0 : (double) controlMoved / control;
        }

        public boolean universe() {
            return control >= 6 && controlRate() >= 0.6
                    && band >= 8 && bandRate() <= 0.25 && bandRate() <= 0.35 * controlRate()
                    && bandOdd >= 5 && bandOddMoved == 0 && bandEvenMoved >= 1;
        }

        public boolean normal() {
            return band >= 6 && bandOddMoved >= 2 && bandRate() >= 0.5;
        }
    }

    private static final class Mob {
        int id;
        double x, z;
        long addedAt;
        int headYaw;
        boolean moved;
        boolean fresh;
        boolean checked;
        boolean otherPlayerNear;
        boolean outsideTicking;
        double minChebyshev = Double.MAX_VALUE;
        double minDistance = Double.MAX_VALUE;
    }

    private record Spot(long ms, double x, double z) {}

    private static final Set<EntityType<?>> GOAL_MOBS = Set.of(
            EntityTypes.COW, EntityTypes.MOOSHROOM, EntityTypes.PIG, EntityTypes.SHEEP, EntityTypes.CHICKEN, EntityTypes.RABBIT,
            EntityTypes.HORSE, EntityTypes.DONKEY, EntityTypes.MULE, EntityTypes.LLAMA, EntityTypes.WOLF, EntityTypes.CAT,
            EntityTypes.OCELOT, EntityTypes.FOX, EntityTypes.PANDA, EntityTypes.POLAR_BEAR, EntityTypes.ZOMBIE, EntityTypes.HUSK,
            EntityTypes.DROWNED, EntityTypes.SKELETON, EntityTypes.STRAY, EntityTypes.BOGGED, EntityTypes.CREEPER, EntityTypes.SPIDER,
            EntityTypes.CAVE_SPIDER, EntityTypes.ENDERMAN, EntityTypes.SLIME, EntityTypes.WITCH);
    private static final long WINDOW_MS = 7000;
    private static final long CHUNK_FRESH_MS = 3000;
    private static final long STILL_MS = 3000;
    private static final int HEAD_STEP = 3;
    private static final int MOVE_STEP = 128;
    private static final double CONTROL_MIN = 33.0;
    private static final int CAP = 2048;

    private static final Object LOCK = new Object();
    private static final Map<Long, Long> chunkSeen = new HashMap<>();
    private static final Map<Integer, Mob> mobs = new HashMap<>();
    private static final ArrayDeque<Spot> path = new ArrayDeque<>();
    private static int band, bandMoved, bandOdd, bandOddMoved, bandEven, bandEvenMoved, control, controlMoved;
    private static double reach;

    private FreshFreeze() {}

    public static void reset() {
        synchronized (LOCK) {
            chunkSeen.clear();
            mobs.clear();
            path.clear();
            band = bandMoved = bandOdd = bandOddMoved = bandEven = bandEvenMoved = control = controlMoved = 0;
            reach = 0;
        }
    }

    public static void newWorld() {
        synchronized (LOCK) {
            chunkSeen.clear();
            mobs.values().removeIf(m -> !m.checked);
        }
    }

    public static void onChunk(int x, int z, long ms) {
        synchronized (LOCK) {
            if (chunkSeen.size() > 8192) chunkSeen.clear();
            chunkSeen.putIfAbsent(((long) x << 32) ^ (z & 0xFFFFFFFFL), ms);
        }
    }

    public static void onAdd(int id, EntityType<?> type, double x, double z, float headYaw, long ms) {
        if (!GOAL_MOBS.contains(type)) return;
        synchronized (LOCK) {
            if (mobs.size() > CAP) return;
            Mob m = new Mob();
            m.id = id;
            m.x = x;
            m.z = z;
            m.addedAt = ms;
            m.headYaw = (byte) Math.floor(headYaw * 256.0F / 360.0F);
            Long seen = chunkSeen.get(((long) ((int) Math.floor(x) >> 4) << 32) ^ (((int) Math.floor(z) >> 4) & 0xFFFFFFFFL));
            m.fresh = seen != null && ms - seen <= CHUNK_FRESH_MS;
            mobs.put(id, m);
        }
    }

    public static void onHead(int id, byte yaw, long ms) {
        synchronized (LOCK) {
            Mob m = mobs.get(id);
            if (m == null || m.checked || ms - m.addedAt > WINDOW_MS) return;
            if (Math.abs((byte) (yaw - m.headYaw)) >= HEAD_STEP) m.moved = true;
            m.headYaw = yaw;
        }
    }

    public static void onMove(int id, short xa, short za, boolean onGround, long ms) {
        synchronized (LOCK) {
            Mob m = mobs.get(id);
            if (m == null || m.checked || ms - m.addedAt > WINDOW_MS) return;
            if (onGround && Math.abs(xa) + Math.abs(za) >= MOVE_STEP) m.moved = true;
        }
    }

    public static void onRemove(int id) {
        synchronized (LOCK) {
            Mob m = mobs.get(id);
            if (m != null && !m.checked) mobs.remove(id);
        }
    }

    public static void tick() {
        LocalPlayer me = MCUtil.MC.player;
        ClientLevel level = MCUtil.MC.level;
        if (me == null || level == null) return;
        long now = System.currentTimeMillis();
        int sim = WorldInfo.simulationDistance;
        int view = WorldInfo.chunkRadius;
        if (sim <= 0 || view <= 0) return;
        double gate = Math.min(16.0 * sim - 8.0, 64.0);
        double bandMin = gate + 1.5;
        double trackLimit = Math.min(96.0, 16.0 * view) - 1.0;
        double controlMax = Math.min(gate, 48.0) - 2.0;
        int tickingChunks = sim - 2;
        double px = me.getX(), pz = me.getZ();
        int pcx = (int) Math.floor(px) >> 4, pcz = (int) Math.floor(pz) >> 4;
        List<Player> others = new ArrayList<>();
        for (Player p : level.players()) if (p != me) others.add(p);
        synchronized (LOCK) {
            path.add(new Spot(now, px, pz));
            while (!path.isEmpty() && now - path.peekFirst().ms() > STILL_MS * 2) path.pollFirst();
            Iterator<Mob> it = mobs.values().iterator();
            while (it.hasNext()) {
                Mob m = it.next();
                if (m.checked) {
                    if (now - m.addedAt > 60000) it.remove();
                    continue;
                }
                double dx = Math.abs(m.x - px), dz = Math.abs(m.z - pz);
                m.minChebyshev = Math.min(m.minChebyshev, Math.max(dx, dz));
                m.minDistance = Math.min(m.minDistance, Math.sqrt(dx * dx + dz * dz));
                int ccx = (int) Math.floor(m.x) >> 4, ccz = (int) Math.floor(m.z) >> 4;
                if (Math.max(Math.abs(ccx - pcx), Math.abs(ccz - pcz)) > tickingChunks) m.outsideTicking = true;
                for (Player o : others) {
                    if (Math.max(Math.abs(o.getX() - m.x), Math.abs(o.getZ() - m.z)) < gate + 32.0) m.otherPlayerNear = true;
                }
                if (!m.fresh && now - m.addedAt < 1000 && stillSince(now - STILL_MS) && Math.sqrt(dx * dx + dz * dz) <= trackLimit - 12.0) {
                    m.fresh = true;
                }
                if (now - m.addedAt < WINDOW_MS) continue;
                m.checked = true;
                if (!m.fresh || m.otherPlayerNear || m.outsideTicking) continue;
                if (m.minChebyshev >= bandMin && m.minDistance <= trackLimit) {
                    band++;
                    if (m.moved) bandMoved++;
                    if ((m.id & 1) == 0) {
                        bandEven++;
                        if (m.moved) bandEvenMoved++;
                    } else {
                        bandOdd++;
                        if (m.moved) bandOddMoved++;
                    }
                    reach = Math.max(reach, m.minChebyshev);
                } else if (m.minChebyshev >= CONTROL_MIN && m.minChebyshev <= controlMax) {
                    control++;
                    if (m.moved) controlMoved++;
                }
            }
        }
    }

    private static boolean stillSince(long since) {
        Spot first = null;
        for (Spot s : path) {
            if (s.ms() < since) continue;
            if (first == null) {
                first = s;
                continue;
            }
            if (Math.abs(s.x() - first.x()) > 3.0 || Math.abs(s.z() - first.z()) > 3.0) return false;
        }
        return first != null && path.peekFirst() != null && path.peekFirst().ms() <= since;
    }

    public static Stats stats() {
        synchronized (LOCK) {
            return new Stats(band, bandMoved, bandOdd, bandOddMoved, bandEven, bandEvenMoved, control, controlMoved, reach);
        }
    }

    public static String debug() {
        synchronized (LOCK) {
            return "band=" + bandMoved + "/" + band + " odd=" + bandOddMoved + "/" + bandOdd + " even=" + bandEvenMoved + "/" + bandEven
                    + " control=" + controlMoved + "/" + control + " tracked=" + mobs.size();
        }
    }
}
