package cz.honzasik.hontun.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.server.PackLoadFeedback;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ResourcePackInfo {
    public enum Phase { CONFIGURATION, PLAY }

    public enum State { WAITING, LOADED, DECLINED, FAILED }

    public enum Origin { BACKEND, PROXY, UNKNOWN }

    public record Pack(UUID id, String url, String hash, boolean required, String prompt, int order,
                       long pushedAt, Phase phase, long pushedAfterJoin, int jumpsBefore,
                       State state, long loadedAt, Phase loadedPhase, long loadedAfterJoin,
                       int survivedJumps, boolean popped, Origin origin, String originWhy, boolean preLogin) {}

    private static final class Entry {
        UUID id;
        String url, hash, prompt;
        boolean required;
        int order, jumpsBefore, survived;
        long pushedAt, pushedAfterJoin, loadedAt, loadedAfterJoin, poppedAt, changedAt;
        Phase phase, loadedPhase;
        State state = State.WAITING;
        boolean popped, pending, changed, maybe, resent, preLogin, proxyJoin, afterDone;
    }

    private static final int CAP = 32;
    private static final int PROMPT_CAP = 1024;
    private static final long WINDOW_MS = 10_000L;
    private static final long BEFORE_JUMP_MS = 1_000L;
    private static final long PROXY_TTL_MS = 2_000L;
    private static final long TAB_GAP_NS = 100_000_000L;
    private static final int ARRIVED_CAP = 64;

    public static volatile boolean present = false;
    public static volatile String url = "";
    public static volatile String hash = "";
    public static volatile boolean required = false;
    public static volatile String prompt = "";

    private static final Map<UUID, Entry> ENTRIES = new LinkedHashMap<>();
    private static final Map<ClientboundResourcePackPushPacket, Long> ARRIVED = new IdentityHashMap<>();
    private static WeakReference<Connection> current = new WeakReference<>(null);
    private static long tabArrivedAt;
    private static boolean nettyTab;
    private static boolean clientTab;
    private static boolean windowDone;
    private static boolean configuring;
    private static boolean switching;
    private static boolean beforeLogin;
    private static boolean tabBeforeLogin;
    private static long joinedAt;
    private static long jumpAt;
    private static int jumps;
    private static int pushes;
    private static boolean proxySeen;
    private static boolean proxyChecked;
    private static long proxyCheckedAt;

    private ResourcePackInfo() {}

    public static void got(ClientboundResourcePackPushPacket packet) {
        url = packet.url() == null ? "" : packet.url();
        hash = packet.hash() == null ? "" : packet.hash();
        required = packet.required();
        prompt = packet.prompt().map(c -> c.getString()).orElse("");
        present = true;
    }

    public static synchronized void arrived(ClientboundResourcePackPushPacket packet) {
        if (ARRIVED.size() >= ARRIVED_CAP) ARRIVED.clear();
        ARRIVED.put(packet, tabArrivedAt == 0L ? Long.MAX_VALUE : System.nanoTime() - tabArrivedAt);
    }

    public static synchronized void tabArrived(ClientboundTabListPacket packet) {
        if (nettyTab || !blank(packet)) return;
        nettyTab = true;
        tabArrivedAt = System.nanoTime();
    }

    public static synchronized void pushed(ClientboundResourcePackPushPacket packet, Phase phase, Connection connection) {
        begin(connection);
        long now = System.currentTimeMillis();
        settle(now);
        Long gap = ARRIVED.remove(packet);
        Entry old = ENTRIES.remove(packet.id());
        Entry e = new Entry();
        e.id = packet.id();
        e.url = packet.url() == null ? "" : packet.url();
        e.hash = packet.hash() == null ? "" : packet.hash();
        if (old != null) {
            boolean same = e.url.equals(old.url) && e.hash.equals(old.hash);
            boolean recent = (old.changed || old.maybe) && now - old.changedAt < WINDOW_MS;
            e.survived = same || !old.pending ? old.survived : 0;
            e.resent = same && (old.pending || recent || old.resent);
            e.changed = old.changed && !(same && recent);
            e.maybe = old.maybe && !same;
            e.changedAt = old.changedAt;
            if (old.pending && !same) change(e, now);
        }
        e.required = packet.required();
        e.prompt = packet.prompt().map(c -> c.getString()).orElse("");
        if (e.prompt.length() > PROMPT_CAP) e.prompt = Oversize.cut("resource pack prompt", e.prompt) + e.prompt.substring(0, PROMPT_CAP);
        e.order = ++pushes;
        e.pushedAt = now;
        e.phase = phase;
        e.preLogin = phase == Phase.PLAY && beforeLogin;
        e.afterDone = e.preLogin && windowDone;
        e.proxyJoin = e.preLogin && tabBeforeLogin && !windowDone && gap != null && gap <= TAB_GAP_NS;
        if (e.preLogin && !usable(e.url)) windowDone = true;
        e.pushedAfterJoin = phase == Phase.PLAY && !e.preLogin && joinedAt > 0L ? now - joinedAt : -1L;
        e.jumpsBefore = jumps;
        e.loadedAfterJoin = -1L;
        trim();
        ENTRIES.put(e.id, e);
    }

    public static synchronized void popped(Optional<UUID> id, Connection connection) {
        begin(connection);
        long now = System.currentTimeMillis();
        settle(now);
        if (id.isPresent()) {
            pop(ENTRIES.get(id.get()), now);
        } else {
            for (Entry e : ENTRIES.values()) pop(e, now);
        }
        Entry top = null;
        for (Entry e : ENTRIES.values()) if (!e.popped) top = e;
        if (top == null) {
            present = false;
            url = "";
            hash = "";
            required = false;
            prompt = "";
        } else {
            present = true;
            url = top.url;
            hash = top.hash;
            required = top.required;
            prompt = top.prompt;
        }
    }

    private static void pop(Entry e, long now) {
        if (e == null || e.popped) return;
        e.popped = true;
        e.poppedAt = now;
        if (e.pending) change(e, now);
        e.pending = false;
    }

    private static void change(Entry e, long now) {
        if (switching) e.maybe = true;
        else e.changed = true;
        e.changedAt = now;
    }

    static synchronized void result(UUID id, PackLoadFeedback.FinalResult result) {
        if (result == null) return;
        if (beforeLogin) windowDone = true;
        Entry e = ENTRIES.get(id);
        if (e == null || result == PackLoadFeedback.FinalResult.DISCARDED) return;
        long now = System.currentTimeMillis();
        if (result == PackLoadFeedback.FinalResult.APPLIED) {
            e.state = State.LOADED;
            if (e.loadedAt != 0L) return;
            e.loadedAt = now;
            e.loadedPhase = beforeLogin || !configuring && joinedAt != 0L ? Phase.PLAY : Phase.CONFIGURATION;
            e.loadedAfterJoin = e.loadedPhase == Phase.PLAY && !beforeLogin ? now - joinedAt : -1L;
        } else {
            e.state = result == PackLoadFeedback.FinalResult.DECLINED ? State.DECLINED : State.FAILED;
        }
    }

    public static synchronized void configuring(Connection connection) {
        begin(connection);
        settle(System.currentTimeMillis());
        configuring = true;
        beforeLogin = false;
        tabBeforeLogin = false;
        windowDone = false;
    }

    public static synchronized void playing(Connection connection) {
        begin(connection);
        beforeLogin = true;
        tabBeforeLogin = false;
        windowDone = false;
        clientTab = false;
        nettyTab = false;
        tabArrivedAt = 0L;
    }

    public static synchronized void tabList(Connection connection, ClientboundTabListPacket packet) {
        begin(connection);
        if (!beforeLogin) return;
        proxySeen = true;
        if (clientTab || !blank(packet)) return;
        clientTab = true;
        tabBeforeLogin = true;
    }

    private static boolean blank(ClientboundTabListPacket packet) {
        return packet != null && blank(packet.header(), 0) && blank(packet.footer(), 0);
    }

    private static boolean blank(Component c, int depth) {
        if (c == null || depth > 8 || !(c.getContents() instanceof PlainTextContents p) || !p.text().isEmpty()) return false;
        for (Component s : c.getSiblings()) if (!blank(s, depth + 1)) return false;
        return true;
    }

    private static boolean usable(String url) {
        try {
            String protocol = new URI(url).toURL().getProtocol();
            return "http".equals(protocol) || "https".equals(protocol);
        } catch (Throwable t) {
            return false;
        }
    }

    public static synchronized void joined(Connection connection) {
        begin(connection);
        long now = System.currentTimeMillis();
        settle(now);
        configuring = false;
        beforeLogin = false;
        tabBeforeLogin = false;
        windowDone = false;
        joinedAt = now;
    }

    public static synchronized void onServerSwitch() {
        if (joinedAt == 0L) return;
        long now = System.currentTimeMillis();
        settle(now);
        jumps++;
        jumpAt = now;
        switching = true;
        for (Entry e : ENTRIES.values()) {
            if (!e.popped) e.pending = true;
            else if (now - e.poppedAt <= BEFORE_JUMP_MS) change(e, now);
        }
    }

    public static synchronized void switched(boolean sameServer, boolean known) {
        if (!switching) return;
        switching = false;
        if (sameServer) {
            jumps--;
            for (Entry e : ENTRIES.values()) {
                e.pending = false;
                e.maybe = false;
                e.jumpsBefore = Math.min(e.jumpsBefore, jumps);
            }
            return;
        }
        if (known) proxySeen = true;
        for (Entry e : ENTRIES.values()) {
            if (e.maybe) e.changed = true;
            e.maybe = false;
        }
    }

    private static void settle(long now) {
        if (configuring || switching || jumpAt == 0L || now - Math.max(jumpAt, joinedAt) < WINDOW_MS) return;
        for (Entry e : ENTRIES.values()) {
            if (!e.pending) continue;
            e.pending = false;
            if (e.state == State.LOADED) e.survived++;
        }
    }

    private static void trim() {
        if (ENTRIES.size() < CAP) return;
        for (Iterator<Entry> it = ENTRIES.values().iterator(); it.hasNext(); ) {
            if (it.next().popped) {
                it.remove();
                return;
            }
        }
        Iterator<Entry> it = ENTRIES.values().iterator();
        it.next();
        it.remove();
    }

    public static List<Pack> packs() {
        return packs(null);
    }

    public static List<Pack> packs(String proxy) {
        synchronized (ResourcePackInfo.class) {
            if (ENTRIES.isEmpty()) return List.of();
        }
        boolean seen = proxy != null && !proxy.isBlank() || detectProxy();
        synchronized (ResourcePackInfo.class) {
            if (seen) proxySeen = true;
            settle(System.currentTimeMillis());
            List<Pack> out = new ArrayList<>(ENTRIES.size());
            for (Entry e : ENTRIES.values()) out.add(view(e));
            return List.copyOf(out);
        }
    }

    private static boolean detectProxy() {
        long now = System.currentTimeMillis();
        synchronized (ResourcePackInfo.class) {
            if (proxySeen) return true;
            if (now - proxyCheckedAt < PROXY_TTL_MS || !Minecraft.getInstance().isSameThread()) return false;
            proxyCheckedAt = now;
        }
        try {
            ServerSoftware.Result r = ServerSoftware.detect();
            if (r == null) return false;
            synchronized (ResourcePackInfo.class) {
                proxyChecked = true;
            }
            return r.proxy() != null && !r.proxy().isBlank();
        } catch (Throwable t) {
            return false;
        }
    }

    private static Pack view(Entry e) {
        Origin origin = Origin.UNKNOWN;
        String why;
        if (e.proxyJoin) {
            origin = Origin.PROXY;
            why = "sent while the proxy connected you to a server: after its tab list reset and before the server's join packet. A server pack only gets there if the proxy holds it back, for example in a plugin";
        } else if (!proxySeen && !proxyChecked) {
            why = "not checked for a proxy yet";
        } else if (!proxySeen) {
            origin = Origin.BACKEND;
            why = "no proxy detected";
        } else if (e.afterDone) {
            why = "sent while joining, right after another pack finished: the proxy may have held a server pack back until then";
        } else if (e.preLogin) {
            why = "sent between the configuration and the server's join packet: the proxy, or a server pack the proxy passed on late";
        } else if (e.survived > 0) {
            why = "stayed loaded across a server jump, a proxy pack and a backend pack nobody removed both do that";
        } else if (e.changed) {
            why = "removed or replaced on a server jump: the next server or a proxy plugin can do that";
        } else if (e.resent) {
            why = "sent again after a server jump: a proxy pack, or the same pack on each server";
        } else if (e.pending) {
            why = "behind a proxy, the server jump is too recent to tell";
        } else if (jumps == 0) {
            why = "behind a proxy, no jump seen yet";
        } else if (e.jumpsBefore >= jumps) {
            why = "behind a proxy, no jump seen since it was sent";
        } else {
            why = "behind a proxy, the server jumps did not tell";
        }
        return new Pack(e.id, e.url, e.hash, e.required, e.prompt, e.order, e.pushedAt, e.phase, e.pushedAfterJoin, e.jumpsBefore,
                e.state, e.loadedAt, e.loadedPhase, e.loadedAfterJoin, e.survived, e.popped, origin, why, e.preLogin);
    }

    private static void begin(Connection connection) {
        if (connection == null || current.get() == connection) return;
        clear();
        current = new WeakReference<>(connection);
    }

    public static synchronized void left() {
        Connection c = current.get();
        if (c == null || !c.isConnected()) clear();
    }

    public static synchronized void clear() {
        present = false;
        url = "";
        hash = "";
        required = false;
        prompt = "";
        ENTRIES.clear();
        ARRIVED.clear();
        tabArrivedAt = 0L;
        nettyTab = false;
        clientTab = false;
        windowDone = false;
        current = new WeakReference<>(null);
        configuring = false;
        switching = false;
        beforeLogin = false;
        tabBeforeLogin = false;
        joinedAt = 0L;
        jumpAt = 0L;
        jumps = 0;
        pushes = 0;
        proxySeen = false;
        proxyChecked = false;
        proxyCheckedAt = 0L;
    }
}
