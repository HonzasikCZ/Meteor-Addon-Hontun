package cz.honzasik.hontun.serverlog;

import cz.honzasik.hontun.commands.PluginScanner;
import cz.honzasik.hontun.modules.AnticheatDetect;
import cz.honzasik.hontun.modules.ChannelFetcher;
import cz.honzasik.hontun.utils.CookieObserver;
import cz.honzasik.hontun.utils.RealVersion;
import cz.honzasik.hontun.utils.ResourcePackInfo;
import cz.honzasik.hontun.utils.ServerSoftware;
import cz.honzasik.hontun.utils.VersionKeeper;
import cz.honzasik.hontun.utils.WorldInfo;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class SnapshotCollector {
    private static final SnapshotCollector INSTANCE = new SnapshotCollector();
    private static final Pattern PLAYER = Pattern.compile("[A-Za-z0-9_.*]{3,17}");
    private static final long[] FIRST_MS = {2_000L, 5_000L, 10_000L};
    private static final long EVERY_MS = 10_000L;
    private static final long IDLE_MS = 60_000L;
    private static final int IDLE_AFTER = 6;

    private static long hopAt;
    private static long nextAt;
    private static int taken;
    private static int unchanged;
    private static boolean scanned;
    private static LogModel.Snapshot last;
    private static long lastAt;

    private SnapshotCollector() {}

    public static void init() {
        try {
            ServerLog.init(FabricLoader.getInstance().getGameDir().resolve("hontun").resolve("server-log"));
            PluginScanner.INSTANCE.onFinish(list -> Minecraft.getInstance().execute(SnapshotCollector::scanFinished));
            MeteorClient.EVENT_BUS.subscribe(INSTANCE);
        } catch (Throwable ignored) {
        }
    }

    static void hopStarted(long now) {
        hopAt = now;
        nextAt = now + FIRST_MS[0];
        taken = 0;
        unchanged = 0;
        scanned = false;
        last = null;
        lastAt = 0L;
    }

    private static void scanFinished() {
        if (!HopTracker.playing()) return;
        scanned = true;
        unchanged = 0;
        nextAt = 0L;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!HopTracker.playing() || System.currentTimeMillis() < nextAt) return;
        sample(false);
    }

    static void sample(boolean leaving) {
        Minecraft mc = Minecraft.getInstance();
        HopTracker.Hop hop = HopTracker.current();
        ServerLog log = HopTracker.log();
        if (!mc.isSameThread() || log == null || hop == null || !HopTracker.playing()) return;
        long now = System.currentTimeMillis();
        if (!leaving) {
            taken++;
            nextAt = taken < FIRST_MS.length ? hopAt + FIRST_MS[taken] : now + (unchanged >= IDLE_AFTER ? IDLE_MS : EVERY_MS);
        } else if (now - lastAt < 50L) {
            return;
        }
        LogModel.Snapshot s;
        try {
            s = read(mc, leaving);
        } catch (Throwable t) {
            return;
        }
        unchanged = s.equals(last) ? unchanged + 1 : 0;
        last = s;
        lastAt = now;
        log.snapshot(hop.number(), now, s);
    }

    private static LogModel.Snapshot read(Minecraft mc, boolean leaving) {
        ServerSoftware.Result r = null;
        try {
            r = ServerSoftware.detect();
        } catch (Throwable ignored) {
        }
        String software = r == null ? "" : str(r.software()), family = "", origin = "", proxy = r == null ? "" : str(r.proxy()).strip();
        boolean rewrites = false;
        if (r != null) {
            if (r.family() != null && r.family() != ServerSoftware.Family.UNKNOWN) family = r.family().label;
            if (r.nameOrigin() != null) origin = r.nameOrigin() == ServerSoftware.Origin.CONFIG ? "config" : "built-in";
            rewrites = !proxy.isEmpty() && rewrites(r, proxy.split(" \\+ ")[0]);
        }

        String version = "", why = "";
        boolean exact = false;
        try {
            RealVersion.Report rep = RealVersion.explain(ServerSoftware.realVersionSnapshot());
            RealVersion.Result v = rep == null ? null : rep.result();
            if (v != null) {
                version = str(v.range()).strip();
                why = !str(v.method()).isBlank() ? "From the " + v.method()
                        : str(v.text()).contains("(hidden by ") ? "Hidden by the translation" : "Not enough signals";
                exact = !version.isEmpty() && v.lo() == v.hi() && v.lo() >= 0;
                if (version.equalsIgnoreCase("unknown")) {
                    version = "";
                    exact = false;
                }
            }
        } catch (Throwable ignored) {
        }
        if (version.isEmpty() && VersionKeeper.version != null && !"unknown".equals(VersionKeeper.version)) {
            version = VersionKeeper.version;
            why = "From the core pack";
            exact = true;
        }

        String ac = "", acConfidence = "", acSource = "";
        boolean acNone = false;
        try {
            AnticheatDetect.Verdict v = AnticheatDetect.COLLECTOR.evaluate(AnticheatDetect.address());
            if (v != null && LogModel.named(v.name())) {
                ac = v.name();
                if (v.also() != null && LogModel.named(v.also().name())) ac += ", " + v.also().name();
                acConfidence = str(v.confidence());
                acSource = str(v.source());
            } else if (v != null && v.name() == null && !leaving) {
                acNone = System.currentTimeMillis() - hopAt > EVERY_MS && System.currentTimeMillis() >= v.deadline();
            }
        } catch (Throwable ignored) {
        }

        String world = "";
        List<String> levels = List.of();
        int maxPlayers = -1, view = -1, simulation = -1, secure = -1, hardcore = -1;
        try {
            if (WorldInfo.captured) {
                world = str(WorldInfo.dimension);
                synchronized (WorldInfo.levels) {
                    levels = WorldInfo.levels.stream().sorted().toList();
                }
                maxPlayers = WorldInfo.maxPlayers;
                view = WorldInfo.chunkRadius;
                simulation = WorldInfo.simulationDistance;
                secure = WorldInfo.secureChat ? 1 : 0;
                hardcore = WorldInfo.hardcore ? 1 : 0;
            }
            if (world.isEmpty() && mc.level != null) world = mc.level.dimension().identifier().toString();
        } catch (Throwable ignored) {
        }

        List<String> plugins = null;
        try {
            if (scanned && !PluginScanner.INSTANCE.running()) plugins = PluginScanner.INSTANCE.results();
        } catch (Throwable ignored) {
        }

        List<String> players = List.of();
        try {
            players = players(mc);
        } catch (Throwable ignored) {
        }

        List<LogModel.Pack> packs = null;
        try {
            packs = packs(proxy);
        } catch (Throwable ignored) {
        }

        int cookies = 0, channels = 0;
        try {
            cookies = CookieObserver.list().size();
            Set<String> all = new LinkedHashSet<>();
            synchronized (ChannelFetcher.REGISTERED) {
                all.addAll(ChannelFetcher.REGISTERED);
            }
            synchronized (ChannelFetcher.SEEN) {
                all.addAll(ChannelFetcher.SEEN);
            }
            channels = all.size();
        } catch (Throwable ignored) {
        }

        return new LogModel.Snapshot(software, family, r == null ? "" : str(r.confidence()), origin, r == null ? "" : str(r.brand()),
                proxy, rewrites, version, why, exact, ac, acConfidence, acSource, acNone, world, levels, maxPlayers, view, simulation,
                secure, hardcore, plugins, players, packs, cookies, channels);
    }

    private static boolean rewrites(ServerSoftware.Result r, String proxy) {
        if (r.evidence() == null) return false;
        for (ServerSoftware.Evidence e : r.evidence()) {
            String d = e.detail();
            if ("brand".equals(e.signal()) && d != null && d.contains("rewrite") && d.contains(proxy)) return true;
        }
        return false;
    }

    private static List<String> players(Minecraft mc) {
        ClientPacketListener c = mc.getConnection();
        if (c == null) return List.of();
        String own = c.getLocalGameProfile() == null ? "" : str(c.getLocalGameProfile().name());
        List<String> out = new ArrayList<>();
        for (PlayerInfo p : c.getListedOnlinePlayers()) {
            String n = p.getProfile().name();
            if (n == null || n.equalsIgnoreCase(own) || !PLAYER.matcher(n).matches()) continue;
            out.add(n);
            if (out.size() >= 4 * LogModel.MAX_PLAYERS) break;
        }
        out.sort(String.CASE_INSENSITIVE_ORDER);
        return out.size() > LogModel.MAX_PLAYERS ? List.copyOf(out.subList(0, LogModel.MAX_PLAYERS)) : List.copyOf(out);
    }

    private static List<LogModel.Pack> packs(String proxy) {
        List<LogModel.Pack> out = new ArrayList<>();
        for (ResourcePackInfo.Pack p : ResourcePackInfo.packs(proxy)) {
            if (p.popped()) continue;
            String loaded = switch (p.state()) {
                case WAITING -> "not loaded yet";
                case DECLINED -> "declined";
                case FAILED -> "failed";
                case LOADED -> p.loadedPhase() == ResourcePackInfo.Phase.CONFIGURATION ? "before joining (configuration)"
                        : p.loadedAfterJoin() < 0L ? "while joining, before the server's join packet"
                        : "after joining, " + String.format(Locale.ROOT, "%.1f s", p.loadedAfterJoin() / 1000.0) + " in";
            };
            out.add(new LogModel.Pack(p.origin().name(), p.originWhy(), p.required(), p.state().name(), loaded, p.url()));
        }
        return List.copyOf(out);
    }

    private static String str(String s) {
        return s == null ? "" : s;
    }
}
