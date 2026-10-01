package cz.honzasik.hontun.utils;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MobCadence {
    public record Stats(int slowRuns, int fastRuns, int slowMobs, int fastMobs, long observedMs) {}

    private record Sample(int id, byte yaw, long ms) {}

    private static final class Track {
        int lastYaw;
        long lastMs;
        char kind;
        int sign;
        int length;
        long lastSlowEnd;
        int samples;
    }

    private static final Set<EntityType<?>> GOAL_MOBS = Set.of(
            EntityTypes.PIG, EntityTypes.COW, EntityTypes.MOOSHROOM, EntityTypes.SHEEP, EntityTypes.CHICKEN,
            EntityTypes.HORSE, EntityTypes.DONKEY, EntityTypes.MULE, EntityTypes.LLAMA, EntityTypes.WOLF,
            EntityTypes.POLAR_BEAR, EntityTypes.IRON_GOLEM, EntityTypes.SNOW_GOLEM, EntityTypes.ZOMBIE, EntityTypes.HUSK,
            EntityTypes.SKELETON, EntityTypes.STRAY, EntityTypes.CREEPER, EntityTypes.SPIDER, EntityTypes.ENDERMAN);
    private static final double MIN_DISTANCE = 29.0;
    private static final double OTHER_PLAYER_RADIUS = 28.0;
    private static final long MOVE_QUIET_MS = 400;
    private static final long FAST_AFTER_SLOW_MS = 8000;
    private static final int QUEUE_CAP = 4096;
    private static final int ZONE_CHECK_TICKS = 10;

    private static final Object LOCK = new Object();
    private static final ArrayDeque<Sample> queue = new ArrayDeque<>();
    private static final Map<Integer, Long> lastMove = new HashMap<>();
    private static final Map<Integer, Track> tracks = new HashMap<>();
    private static final Set<Integer> slowMobs = new HashSet<>();
    private static final Set<Integer> fastMobs = new HashSet<>();
    private static int slowRuns;
    private static int fastRuns;
    private static long observedMs;
    private static int tickCounter;

    private MobCadence() {}

    public static void reset() {
        synchronized (LOCK) {
            queue.clear();
            lastMove.clear();
            tracks.clear();
            slowMobs.clear();
            fastMobs.clear();
            slowRuns = 0;
            fastRuns = 0;
            observedMs = 0;
            tickCounter = 0;
        }
    }

    public static void onHead(int id, byte yaw, long ms) {
        synchronized (LOCK) {
            if (queue.size() < QUEUE_CAP) queue.add(new Sample(id, yaw, ms));
        }
    }

    public static void onMove(int id, long ms) {
        synchronized (LOCK) {
            if (lastMove.size() > 4096) lastMove.clear();
            lastMove.put(id, ms);
        }
    }

    public static void tick() {
        LocalPlayer me = MCUtil.MC.player;
        ClientLevel level = MCUtil.MC.level;
        if (me == null || level == null) return;
        int simDist = WorldInfo.simulationDistance > 0 ? WorldInfo.simulationDistance : 10;
        double maxChebyshev = Math.min(60.0, 16.0 * simDist - 10.0);
        synchronized (LOCK) {
            while (!queue.isEmpty()) {
                Sample s = queue.poll();
                Entity e = level.getEntity(s.id());
                if (e == null || !GOAL_MOBS.contains(e.getType())) continue;
                boolean quiet = s.ms() - lastMove.getOrDefault(s.id(), Long.MIN_VALUE / 2) > MOVE_QUIET_MS;
                process(s, quiet && inZone(e, me, level, maxChebyshev));
            }
            if (++tickCounter % ZONE_CHECK_TICKS == 0 && anyInZone(me, level, maxChebyshev)) {
                observedMs += ZONE_CHECK_TICKS * 50L;
            }
        }
    }

    private static void process(Sample s, boolean zone) {
        Track t = tracks.get(s.id());
        if (t == null) {
            t = new Track();
            t.lastYaw = s.yaw();
            t.lastMs = s.ms();
            t.samples = 1;
            if (tracks.size() > 2048) tracks.clear();
            tracks.put(s.id(), t);
            return;
        }
        int delta = (byte) (s.yaw() - t.lastYaw);
        long dt = s.ms() - t.lastMs;
        t.lastYaw = s.yaw();
        t.lastMs = s.ms();
        t.samples++;
        if (!zone) {
            t.kind = 0;
            t.length = 0;
            return;
        }
        char kind = 0;
        if (dt >= 100 && dt <= 260) {
            int size = Math.abs(delta);
            if (size >= 6 && size <= 8) kind = 'S';
            else if (size >= 19 && size <= 23) kind = 'F';
        }
        int sign = delta > 0 ? 1 : -1;
        if (kind != 0 && kind == t.kind && sign == t.sign) {
            t.length++;
        } else {
            t.kind = kind;
            t.sign = sign;
            t.length = kind == 0 ? 0 : 1;
        }
        if (t.kind == 'S') {
            if (t.length == 3) {
                slowRuns++;
                slowMobs.add(s.id());
            }
            if (t.length >= 3) t.lastSlowEnd = s.ms();
        } else if (t.kind == 'F' && t.length == 2 && t.samples > 3 && s.ms() - t.lastSlowEnd > FAST_AFTER_SLOW_MS) {
            fastRuns++;
            fastMobs.add(s.id());
        }
    }

    private static boolean inZone(Entity e, LocalPlayer me, ClientLevel level, double maxChebyshev) {
        if (me.isSpectator() || e.isPassenger()) return false;
        double dx = e.getX() - me.getX(), dy = e.getY() - me.getY(), dz = e.getZ() - me.getZ();
        if (Math.sqrt(dx * dx + dy * dy + dz * dz) < MIN_DISTANCE) return false;
        if (Math.max(Math.abs(dx), Math.abs(dz)) > maxChebyshev) return false;
        for (Player other : level.players()) {
            if (other == me) continue;
            if (other.distanceTo(e) < OTHER_PLAYER_RADIUS) return false;
        }
        return true;
    }

    private static boolean anyInZone(LocalPlayer me, ClientLevel level, double maxChebyshev) {
        for (Entity e : level.entitiesForRendering()) {
            if (GOAL_MOBS.contains(e.getType()) && inZone(e, me, level, maxChebyshev)) return true;
        }
        return false;
    }

    public static Stats stats() {
        synchronized (LOCK) {
            return new Stats(slowRuns, fastRuns, slowMobs.size(), fastMobs.size(), observedMs);
        }
    }

    public static List<String> debug() {
        synchronized (LOCK) {
            List<String> out = new ArrayList<>();
            out.add("slowRuns=" + slowRuns + " fastRuns=" + fastRuns + " slowMobs=" + slowMobs + " fastMobs=" + fastMobs + " observedMs=" + observedMs);
            return out;
        }
    }
}
