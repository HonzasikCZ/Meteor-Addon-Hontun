package cz.honzasik.hontun.serverlog;

import cz.honzasik.hontun.utils.ResourcePackInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public final class HopTracker {
    public record Hop(int number, long at, String how, ServerKey key) {}

    private static final long TRANSFER_MS = 60_000L;

    private static WeakReference<Connection> connection = new WeakReference<>(null);
    private static ServerLog log;
    private static boolean between;
    private static String transferAddress;
    private static String transferTarget;
    private static long transferAt;
    private static volatile Hop last;
    private static int count;

    private HopTracker() {}

    public static Hop current() {
        return last;
    }

    static ServerLog log() {
        return log;
    }

    static boolean playing() {
        return log != null && !between;
    }

    public static void login(ClientboundLoginPacket packet, Connection from, ServerData data) {
        long now = System.currentTimeMillis();
        boolean same = log != null && from != null && connection.get() == from;
        if (!same) {
            if (log != null) end(now);
            if (from == null || from.isMemoryConnection() || data == null || data.ip == null || data.ip.isBlank() || data.isLan() || data.isRealm()) return;
        }
        String how;
        if (same) {
            how = between ? LogModel.CONFIGURATION : LogModel.LOGIN;
            if (!between) packsSwitch();
        } else {
            boolean transferred = transferAddress != null && transferAddress.equals(data.ip) && now - transferAt <= TRANSFER_MS;
            how = transferred ? LogModel.TRANSFER : LogModel.JOIN;
            connection = new WeakReference<>(from);
            last = null;
            count = 0;
            log = ServerLog.forAddress(data.ip);
            log.startVisit(now, transferred ? "Arrived by transfer at " + transferTarget : "");
            transferAddress = null;
        }
        between = false;
        ServerKey key = key(packet);
        Hop before = last;
        if (before != null) packsSwitched(before.key(), key);
        Hop hop = new Hop(++count, now, how, key);
        last = hop;
        log.hop(now, how, hop.key());
        SnapshotCollector.hopStarted(now);
    }

    public static void configurationStart(Connection from) {
        if (log == null || connection.get() != from || between) return;
        SnapshotCollector.sample(true);
        between = true;
        packsSwitch();
    }

    public static void transfer(String host, int port, ServerData data) {
        if (data == null || data.ip == null) return;
        long now = System.currentTimeMillis();
        transferAddress = data.ip;
        transferTarget = host + ":" + port;
        transferAt = now;
        if (log == null) return;
        SnapshotCollector.sample(true);
        log.note("Transferred to " + host + ":" + port);
        between = true;
    }

    public static void connecting() {
        transferAddress = null;
        transferTarget = null;
    }

    public static void disconnect() {
        if (log == null) return;
        SnapshotCollector.sample(true);
        end(System.currentTimeMillis());
    }

    private static void end(long now) {
        log.endVisit(now);
        log = null;
        between = false;
        last = null;
        count = 0;
        connection = new WeakReference<>(null);
    }

    private static void packsSwitch() {
        try {
            ResourcePackInfo.onServerSwitch();
        } catch (Throwable ignored) {
        }
    }

    private static void packsSwitched(ServerKey from, ServerKey to) {
        try {
            ResourcePackInfo.switched(to.same(from), from.known() && to.known());
        } catch (Throwable ignored) {
        }
    }

    private static ServerKey key(ClientboundLoginPacket packet) {
        try {
            List<String> levels = new ArrayList<>();
            packet.levels().forEach(l -> levels.add(l.identifier().toString()));
            return new ServerKey(packet.commonPlayerSpawnInfo().seed(), packet.commonPlayerSpawnInfo().dimension().identifier().toString(), levels);
        } catch (Throwable t) {
            return new ServerKey(0L, "", List.of());
        }
    }
}
