package cz.honzasik.hontun.modules;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.commands.AnticheatProbe;
import cz.honzasik.hontun.utils.Oversize;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundStartConfigurationPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.login.ClientboundLoginFinishedPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.phys.Vec3;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class AnticheatDetect extends Module {
    public static final Collector COLLECTOR = new Collector();

    private static final int GUESS_PINGS = 5;
    private static final int MAX_PINGS = 8;
    private static final int MAX_SETBACKS = 64;
    private static final int MAX_CHANNEL_HITS = 256;
    public static final String OVERSIZE_CHANNELS = "anticheat channel not counted (over " + MAX_CHANNEL_HITS + ")";
    private static final int INTAVE_MASK = 0xF5550000;
    private static final int VULCAN_COUNTER = -32347;
    private static final int KARHU_TRANSACTION = -32767;
    private static final int PLEDGE_START = -3000;
    private static final int SPARTAN_FIRST = -1939;
    private static final int SPARTAN_LAST = -1945;
    private static final long JOIN_WINDOW_MS = 15000L;
    private static final long CONFIRM_WINDOW_MS = 5000L;
    private static final long COMPANION_NS = 50_000_000L;
    private static final Set<Relative> ROTATION_ONLY = Set.of(Relative.Y_ROT, Relative.X_ROT);
    private static final Set<String> OWN_PINGS = Set.of("CultAC", "Kauri", "TotemGuard", "RealAC");

    private static final Map<String, String> NAMES = Map.ofEntries(
        Map.entry("grimac", "Grim"),
        Map.entry("grim", "Grim"),
        Map.entry("vulcan", "Vulcan"),
        Map.entry("polar", "Polar"),
        Map.entry("polarloader", "Polar"),
        Map.entry("intave", "Intave"),
        Map.entry("matrix", "Matrix"),
        Map.entry("karhu", "Karhu"),
        Map.entry("karhuac", "Karhu"),
        Map.entry("grizzly", "Grizzly"),
        Map.entry("verus", "Verus"),
        Map.entry("nocheatplus", "NoCheatPlus"),
        Map.entry("negativity", "Negativity"),
        Map.entry("spartan", "Spartan"),
        Map.entry("kauri", "Kauri"),
        Map.entry("themis", "Themis"),
        Map.entry("horizon", "Horizon"),
        Map.entry("witherac", "WitherAC"),
        Map.entry("godseye", "GodsEye"),
        Map.entry("anticheatreloaded", "AntiCheatReloaded"),
        Map.entry("abc", "ABC"),
        Map.entry("lightanticheat", "LightAntiCheat"),
        Map.entry("guardianac", "GuardianAC"),
        Map.entry("foxaddition", "FoxAddition"),
        Map.entry("angleguard", "AngleGuard"),
        Map.entry("totemguard", "TotemGuard"),
        Map.entry("fairplay", "FairPlay"),
        Map.entry("cultac", "CultAC"),
        Map.entry("realac", "RealAC"));

    private static final Map<String, Set<String>> FINGERPRINTS = Map.of(
        "Grim", Set.of("Grim"),
        "Vulcan", Set.of("Vulcan", "Old Vulcan"),
        "Polar", Set.of("Polar"),
        "Intave", Set.of("Intave"),
        "Matrix", Set.of("Matrix"),
        "Karhu", Set.of("Karhu"),
        "Verus", Set.of("Verus"),
        "Grizzly", Set.of("Grizzly"),
        "FairPlay", Set.of("Grim", "FairPlay"),
        "Spartan", Set.of("Spartan"));

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> debug = sgGeneral.add(new BoolSetting.Builder()
        .name("debug")
        .description("Also prints what the result is based on: plugins, commands, channels, the ping IDs and their steps, setbacks and any disagreement between them.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> autoEnable = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-enable")
        .description("Turns the module on by itself when you join a server, reports the anticheat and turns it off again. Off = it only runs when you enable it.")
        .defaultValue(true)
        .build()
    );

    private boolean waiting;

    public AnticheatDetect() {
        super(Hontun.CATEGORY, "Anticheat Detect", "Passively detects the server's anticheat from plugins, commands, plugin channels, the first ping IDs after joining and setbacks.");
    }

    @Override
    public void onActivate() {
        if (mc.hasSingleplayerServer()) {
            error("Only works on multiplayer servers.");
            disable();
            return;
        }
        waiting = true;
        check();
    }

    @Override
    public void onDeactivate() {
        waiting = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (waiting) check();
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (isActive()) disable();
    }

    private void check() {
        if (COLLECTOR.switching()) return;
        int seen = COLLECTOR.changes();
        Verdict v = COLLECTOR.evaluate(address());
        if (!v.done() && System.currentTimeMillis() < v.deadline()) return;
        waiting = false;
        report(v);
        COLLECTOR.reported(v, seen);
        disable();
    }

    private void report(Verdict v) {
        info(AnticheatProbe.format(v, debug.get()));
    }

    public static String address() {
        ServerData server = Minecraft.getInstance().getCurrentServer();
        if (server == null || server.ip == null) return null;
        try {
            return ServerAddress.parseString(server.ip).getHost();
        } catch (Throwable t) {
            return server.ip;
        }
    }

    public static void onRawPayload(Identifier id, byte[] data) {
        COLLECTOR.onChannel(id.toString(), data);
    }

    private static String anticheatOf(String name) {
        if (name == null || name.isEmpty()) return null;
        int c = name.indexOf(':');
        String ns = c >= 0 ? name.substring(0, c) : name;
        return NAMES.get(ns.toLowerCase(Locale.ROOT));
    }

    private static Set<String> anticheatsOf(Set<String> names) {
        Set<String> out = new TreeSet<>();
        for (String n : names) {
            String ac = anticheatOf(n);
            if (ac != null) out.add(ac);
        }
        return out;
    }

    private static String key(Verdict v) {
        if (v.name() == null) return "none";
        if (v.name().equals("Unknown")) return "Unknown|" + v.reason();
        return v.name() + "|" + v.confidence() + (v.also() == null ? "" : "|" + v.also().name() + "|" + v.also().confidence());
    }

    private static String joinAnd(List<String> parts) {
        if (parts.size() < 2) return String.join("", parts);
        return String.join(", ", parts.subList(0, parts.size() - 1)) + " and " + parts.get(parts.size() - 1);
    }

    static String guess(List<Integer> ids, String address) {
        if (ids.size() < GUESS_PINGS) return null;
        int[] t = new int[GUESS_PINGS];
        for (int i = 0; i < GUESS_PINGS; i++) t[i] = ids.get(i);
        int[] d = new int[GUESS_PINGS - 1];
        for (int i = 0; i < d.length; i++) d[i] = t[i + 1] - t[i];
        int f = t[0];

        if (address != null && address.toLowerCase(Locale.ROOT).endsWith("hypixel.net")) return "Watchdog";

        boolean same = true;
        for (int x : d) {
            if (x != d[0]) {
                same = false;
                break;
            }
        }
        if (same) {
            if (d[0] == 1) {
                if (f >= -23772 && f <= -23762) return "Vulcan";
                if ((f >= 95 && f <= 105) || (f >= -20005 && f <= -19995)) return "Matrix";
                if (f >= -32773 && f <= -32762) return "Grizzly";
                return "Verus";
            }
            if (d[0] == -1) {
                if (f >= -8287 && f <= -8280) return "Errata";
                if (f < -3000) return "Intave";
                if (f >= -5 && f <= 0) return "Grim";
                if (f >= -3000 && f <= -2995) return "Karhu";
                return "Polar";
            }
            return null;
        }
        if (t[0] == t[1] && t[3] - t[2] == 1 && t[4] - t[3] == 1) return "Verus";
        if (d[0] >= 100 && d[1] == -1 && d[2] == -1 && d[3] == -1) return "Polar";
        if (f < -3000 && (t[0] == 0 || t[1] == 0 || t[2] == 0 || t[3] == 0 || t[4] == 0)) return "Intave";
        if (t[0] == -30767 && t[1] == -30766 && t[2] == -25767 && t[4] - t[3] == 1) return "Old Vulcan";
        return "Unknown";
    }

    static String refine(List<Integer> ids, String guess) {
        if (guess == null || ids.size() < GUESS_PINGS) return guess;
        int[] t = new int[GUESS_PINGS];
        for (int i = 0; i < GUESS_PINGS; i++) t[i] = ids.get(i);
        switch (guess) {
            case "Grim" -> {
                if (t[0] == -1) return "FairPlay";
            }
            case "Verus" -> {
                if (t[0] == VULCAN_COUNTER) return "Vulcan";
            }
            case "Polar" -> {
                if (pledge(t)) return "Karhu";
                if (spartanCycle(ids, ids.size())) return "Spartan";
            }
            case "Unknown" -> {
                if (spartanCycle(ids, ids.size())) return "Spartan";
                if (intaveRange(t)) return "Intave";
                if (t[0] >= -11500 && t[0] <= -11001 && t[1] == VULCAN_COUNTER && t[2] - t[1] == 1 && t[3] - t[2] == 1 && t[4] - t[3] == 1) return "Vulcan";
                if (pledge(t)) return "Karhu";
            }
            default -> {
            }
        }
        return guess;
    }

    private static boolean intaveRange(int[] ids) {
        for (int id : ids) {
            if ((id & 0xFFFF0000) != INTAVE_MASK) return false;
        }
        return true;
    }

    private static boolean spartanCycle(List<Integer> ids, int count) {
        for (int i = 0; i < count; i++) {
            int id = ids.get(i);
            if (id > SPARTAN_FIRST || id < SPARTAN_LAST) return false;
            if (i > 0 && !spartanNext(ids.get(i - 1), id)) return false;
        }
        return true;
    }

    private static boolean spartanNext(int previous, int id) {
        return id == (previous == SPARTAN_LAST ? SPARTAN_FIRST : previous - 1);
    }

    private static boolean pledge(int[] ids) {
        List<Integer> rest = new ArrayList<>();
        int transactions = 0;
        for (int id : ids) {
            if (id == KARHU_TRANSACTION) transactions++;
            else rest.add(id);
        }
        if (transactions != 1 || rest.get(0) > PLEDGE_START) return false;
        for (int i = 1; i < rest.size(); i++) {
            if (rest.get(i) - rest.get(i - 1) != -1) return false;
        }
        return true;
    }

    private static String step(long x) {
        return x > 0 ? "+" + x : String.valueOf(x);
    }

    public record Verdict(String name, String confidence, String source, String reason, boolean done, long deadline,
                          List<Integer> pings, String guess, String check, String conflict,
                          List<String> plugins, List<String> commands, List<String> channels,
                          String setbacks, int teleports, Verdict also) {}

    private static final class Setback {
        private final long at;
        private final Vec3 pos;
        private final boolean rotationOnly;
        private final boolean delta;
        private final boolean intavePing;
        private boolean velocity;
        private boolean entity;

        private Setback(long at, Vec3 pos, boolean rotationOnly, boolean delta, boolean intavePing) {
            this.at = at;
            this.pos = pos;
            this.rotationOnly = rotationOnly;
            this.delta = delta;
            this.intavePing = intavePing;
        }

        private String label() {
            if (!rotationOnly) return null;
            if (entity && !delta) return "CultAC";
            if (velocity && !delta) return intavePing ? "Intave" : "Grim";
            if (delta && !velocity) return "Intave";
            return null;
        }
    }

    public static final class Collector {
        private final Object lock = new Object();
        private final List<Integer> pings = new ArrayList<>();
        private final Set<String> channelHits = new TreeSet<>();
        private int channelSkipped;
        private final Set<String> pluginHits = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        private final Set<String> commandHits = new TreeSet<>();
        private final List<Setback> setbacks = new ArrayList<>();
        private Setback pending;
        private boolean inPlay;
        private boolean configured;
        private boolean relogin;
        private boolean scanned;
        private boolean treeReady;
        private boolean reported;
        private boolean pinged;
        private int lastPing;
        private int selfId = Integer.MIN_VALUE;
        private int teleports;
        private int changes;
        private int seenChanges;
        private long loginAt;
        private long lastPingAt;
        private String shown;
        private CommandDispatcher<?> tree;

        private Collector() {}

        @EventHandler(priority = EventPriority.HIGHEST)
        private void onPacketReceive(PacketEvent.Receive event) {
            Packet<?> packet = event.packet;
            if (packet instanceof ClientboundPingPacket ping) {
                onPing(ping.getId());
            } else if (packet instanceof ClientboundPlayerPositionPacket position) {
                onTeleport(position);
            } else if (packet instanceof ClientboundSetEntityMotionPacket motion) {
                onMotion(motion);
            } else if (packet instanceof ClientboundTeleportEntityPacket teleport) {
                onEntityTeleport(teleport);
            } else if (packet instanceof ClientboundFinishConfigurationPacket) {
                synchronized (lock) {
                    start();
                    configured = true;
                }
            } else if (packet instanceof ClientboundLoginPacket login) {
                synchronized (lock) {
                    selfId = login.playerId();
                    if (configured) {
                        configured = false;
                        return;
                    }
                    boolean switched = inPlay;
                    CommandDispatcher<?> seen = tree;
                    clear();
                    tree = seen;
                    relogin = switched;
                    start();
                }
            } else if (packet instanceof ClientboundStartConfigurationPacket || packet instanceof ClientboundLoginFinishedPacket) {
                synchronized (lock) {
                    clear();
                    inPlay = false;
                }
            }
        }

        @EventHandler
        private void onGameLeft(GameLeftEvent event) {
            ClientPacketListener conn = Minecraft.getInstance().getConnection();
            synchronized (lock) {
                if (relogin) {
                    relogin = false;
                    if (conn != null) tree = conn.getCommands();
                    return;
                }
                clear();
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        private void onGameJoined(GameJoinedEvent event) {
            AnticheatDetect module = Modules.get().get(AnticheatDetect.class);
            if (module == null || module.isActive() || !module.autoEnable.get()) return;
            if (Minecraft.getInstance().hasSingleplayerServer()) return;
            module.enable();
        }

        @EventHandler
        private void onTick(TickEvent.Post event) {
            scanCommands();
            boolean changed;
            synchronized (lock) {
                settle(System.nanoTime(), false);
                changed = reported && changes != seenChanges;
                seenChanges = changes;
            }
            if (!changed) return;
            AnticheatDetect module = Modules.get().get(AnticheatDetect.class);
            if (module == null || module.isActive()) return;
            Verdict v = evaluate(address());
            if (!strong(v) || (v.also() != null && !strong(v.also()))) return;
            String k = key(v);
            synchronized (lock) {
                if (!reported || k.equals(shown)) return;
                shown = k;
            }
            module.report(v);
        }

        public void onPluginScan(List<String> anticheats) {
            synchronized (lock) {
                scanned = true;
                for (String p : anticheats) {
                    if (NAMES.containsKey(p.toLowerCase(Locale.ROOT)) && pluginHits.add(p)) changes++;
                }
            }
        }

        private boolean strong(Verdict v) {
            return "high confidence".equals(v.confidence()) || "medium confidence".equals(v.confidence());
        }

        private boolean switching() {
            synchronized (lock) {
                return relogin;
            }
        }

        private int changes() {
            synchronized (lock) {
                return changes;
            }
        }

        private void reported(Verdict v, int seen) {
            synchronized (lock) {
                reported = true;
                shown = key(v);
                seenChanges = seen;
            }
        }

        private void start() {
            reported = false;
            pings.clear();
            clearSetbacks();
            lastPingAt = 0;
            pinged = false;
            loginAt = System.currentTimeMillis();
            inPlay = true;
            changes++;
        }

        private void clear() {
            reported = false;
            pings.clear();
            channelHits.clear();
            channelSkipped = 0;
            pluginHits.clear();
            commandHits.clear();
            clearSetbacks();
            scanned = false;
            treeReady = false;
            configured = false;
            relogin = false;
            tree = null;
            pinged = false;
            lastPingAt = 0;
            changes++;
        }

        private void clearSetbacks() {
            setbacks.clear();
            pending = null;
            teleports = 0;
            shown = null;
        }

        private void onChannel(String channel, byte[] data) {
            if (channel.equals("minecraft:register")) {
                for (String ch : new String(data, StandardCharsets.UTF_8).split("\0")) hit(ch);
                return;
            }
            hit(channel);
        }

        private void hit(String channel) {
            if (anticheatOf(channel) == null) return;
            boolean over;
            synchronized (lock) {
                if (channelHits.contains(channel)) return;
                over = channelHits.size() >= MAX_CHANNEL_HITS;
                if (over) {
                    channelSkipped++;
                } else {
                    channelHits.add(channel);
                    changes++;
                }
            }
            if (over) Oversize.item(OVERSIZE_CHANNELS, channel);
        }

        public int skippedChannels() {
            synchronized (lock) {
                return channelSkipped;
            }
        }

        private void scanCommands() {
            ClientPacketListener conn = Minecraft.getInstance().getConnection();
            if (conn == null) return;
            CommandDispatcher<?> dispatcher = conn.getCommands();
            synchronized (lock) {
                if (!inPlay || relogin || dispatcher == tree) return;
            }
            Set<String> found = new TreeSet<>();
            boolean any = false;
            for (CommandNode<?> node : dispatcher.getRoot().getChildren()) {
                any = true;
                String name = node.getName();
                int c = name.indexOf(':');
                if (c <= 0) continue;
                String ns = name.substring(0, c).toLowerCase(Locale.ROOT);
                if (NAMES.containsKey(ns) && !FabricLoader.getInstance().isModLoaded(ns)) found.add(ns);
            }
            synchronized (lock) {
                tree = dispatcher;
                if (any) treeReady = true;
                if (commandHits.addAll(found)) changes++;
            }
        }

        private void onPing(int raw) {
            int id = raw;
            boolean via = (raw >>> 24) == 0x40;
            if (via) {
                if (((raw >>> 16) & 0xFF) != 0) return;
                id = (short) raw;
            }
            synchronized (lock) {
                if (!inPlay || (via && id == 0 && !pings.isEmpty())) return;
                pinged = true;
                lastPing = id;
                if (pings.size() >= MAX_PINGS) return;
                pings.add(id);
                lastPingAt = System.currentTimeMillis();
                changes++;
            }
        }

        private void onTeleport(ClientboundPlayerPositionPacket p) {
            long now = System.nanoTime();
            synchronized (lock) {
                if (!inPlay) return;
                settle(now, true);
                Set<Relative> rel = p.relatives();
                if (p.id() >= 0 || rel.contains(Relative.X) || rel.contains(Relative.Y) || rel.contains(Relative.Z)) {
                    teleports++;
                    return;
                }
                pending = new Setback(now, p.change().position(), p.relatives().equals(ROTATION_ONLY),
                    p.change().deltaMovement().lengthSqr() > 1.0E-8, pinged && (lastPing & 0xFFFF0000) == INTAVE_MASK);
            }
        }

        private void onMotion(ClientboundSetEntityMotionPacket m) {
            synchronized (lock) {
                if (pending != null && m.id() == selfId && System.nanoTime() - pending.at <= COMPANION_NS) pending.velocity = true;
            }
        }

        private void onEntityTeleport(ClientboundTeleportEntityPacket t) {
            synchronized (lock) {
                if (pending != null && t.id() == selfId && System.nanoTime() - pending.at <= COMPANION_NS
                    && t.change().position().distanceToSqr(pending.pos) < 1.0E-6) pending.entity = true;
            }
        }

        private void settle(long now, boolean force) {
            if (pending == null || (!force && now - pending.at <= COMPANION_NS)) return;
            if (setbacks.size() < MAX_SETBACKS) {
                setbacks.add(pending);
                changes++;
            }
            pending = null;
        }

        public Verdict evaluate(String address) {
            if (Minecraft.getInstance().isSameThread()) scanCommands();
            List<Integer> ids;
            Set<String> plugins;
            Set<String> commands;
            Set<String> channels;
            List<Setback> sbs;
            int plain;
            boolean scan;
            boolean ready;
            long deadline;
            synchronized (lock) {
                settle(System.nanoTime(), false);
                ids = List.copyOf(pings);
                plugins = new TreeSet<>(pluginHits);
                commands = new TreeSet<>(commandHits);
                channels = new TreeSet<>(channelHits);
                sbs = List.copyOf(setbacks);
                plain = teleports;
                scan = scanned;
                ready = treeReady;
                deadline = Math.max(loginAt + JOIN_WINDOW_MS, lastPingAt == 0 ? 0 : lastPingAt + CONFIRM_WINDOW_MS);
            }
            List<String> pluginList = scan ? List.copyOf(plugins) : null;
            List<String> commandList = List.copyOf(commands);
            List<String> channelList = List.copyOf(channels);

            Map<String, Integer> tally = new TreeMap<>();
            int strange = 0;
            for (Setback s : sbs) {
                String label = s.label();
                if (label == null) strange++;
                else tally.merge(label, 1, Integer::sum);
            }
            String sbName = null;
            int sbTop = 0;
            int sbAll = 0;
            for (Map.Entry<String, Integer> e : tally.entrySet()) {
                sbAll += e.getValue();
                if (e.getValue() > sbTop) {
                    sbTop = e.getValue();
                    sbName = e.getKey();
                }
            }
            String sbConfidence = sbTop < sbAll ? "uncertain" : sbTop >= 2 ? "medium confidence" : "guess";
            List<String> parts = new ArrayList<>();
            for (Map.Entry<String, Integer> e : tally.entrySet()) parts.add(e.getKey() + " " + e.getValue());
            if (strange > 0) parts.add("unknown " + strange);
            String sbText = parts.isEmpty() ? null : String.join(", ", parts);

            String guess = refine(ids, guess(ids, address));
            boolean masked = "Intave".equals(guess) && ids.size() >= GUESS_PINGS && (ids.get(0) & 0xFFFF0000) == INTAVE_MASK;
            boolean cycle = "Spartan".equals(guess);
            boolean broke = false;
            int brokeAt = -1;
            if (ids.size() > GUESS_PINGS) {
                int last = ids.get(GUESS_PINGS - 1) - ids.get(GUESS_PINGS - 2);
                for (int i = GUESS_PINGS; i < ids.size(); i++) {
                    boolean off = masked ? (ids.get(i) & 0xFFFF0000) != INTAVE_MASK
                        : cycle ? !spartanCycle(ids.subList(i - 1, i + 1), 2)
                        : ids.get(i) - ids.get(i - 1) != last;
                    if (off) {
                        broke = true;
                        brokeAt = i + 1;
                        break;
                    }
                }
            }
            boolean confirmed = !broke && ids.size() >= MAX_PINGS;

            String check;
            if (ids.size() < GUESS_PINGS) {
                check = "needs " + GUESS_PINGS + " pings";
            } else if (guess == null) {
                check = "the step " + step((long) ids.get(1) - ids.get(0)) + " matches no fingerprint";
            } else if (guess.equals("Unknown")) {
                check = "the IDs follow no known pattern";
            } else if (cycle) {
                if (broke) check = "ping " + brokeAt + " leaves Spartan's -1939 to -1945 cycle";
                else if (confirmed) check = "pings 6-8 follow Spartan's -1939 to -1945 cycle";
                else check = "waiting for pings 6-8 to follow Spartan's -1939 to -1945 cycle";
            } else if (masked) {
                if (broke) check = "ping " + brokeAt + " is outside Intave's 0xF555 ID range";
                else if (confirmed) check = "pings 6-8 stay in Intave's 0xF555 ID range";
                else check = "waiting for pings 6-8 to stay in Intave's 0xF555 ID range";
            } else {
                String lastStep = step((long) ids.get(GUESS_PINGS - 1) - ids.get(GUESS_PINGS - 2));
                if (broke) check = "ping " + brokeAt + " breaks the " + lastStep + " step";
                else if (confirmed) check = "pings 6-8 continue the " + lastStep + " step";
                else check = "waiting for pings 6-8 to continue the " + lastStep + " step";
            }

            String name = null;
            String confidence = null;
            String source = null;
            String reason = null;
            String conflict = null;
            boolean done = false;
            String pingConfidence = broke ? "uncertain" : confirmed ? "medium confidence" : "guess";
            if (guess != null && !guess.equals("Unknown")) {
                if (sbName == null) {
                    name = guess;
                    confidence = pingConfidence;
                    source = "pings";
                    done = ready && (broke || confirmed);
                } else if (FINGERPRINTS.getOrDefault(sbName, Set.of(sbName)).contains(guess)) {
                    name = sbName;
                    confidence = broke ? "uncertain" : "medium confidence";
                    source = "pings, setbacks";
                    done = ready;
                } else {
                    name = guess + " or " + sbName;
                    confidence = "uncertain";
                    source = "pings, setbacks";
                    conflict = "pings say " + guess + "; setbacks say " + sbName;
                    done = ready;
                }
            } else if (sbName != null) {
                name = sbName;
                confidence = sbConfidence;
                source = "setbacks";
                done = ready;
            } else if (strange > 0) {
                boolean both = "Unknown".equals(guess);
                name = "Unknown";
                source = both ? "pings, setbacks" : "setbacks";
                reason = both ? "the pings and setbacks match no known anticheat" : "the setbacks match no known anticheat";
                done = ready;
            } else if (guess != null) {
                name = guess;
                confidence = pingConfidence;
                source = "pings";
                done = ready && (broke || confirmed);
            } else if (ids.size() >= GUESS_PINGS) {
                reason = "the ping pattern matches no known anticheat";
                done = ready;
            } else {
                reason = ids.isEmpty()
                    ? "no anticheat plugins, commands, channels or pings found"
                    : "only " + ids.size() + " of the " + GUESS_PINGS + " needed pings arrived";
            }
            Verdict passive = new Verdict(name, confidence, source, reason, done, deadline, ids, guess, check, conflict,
                pluginList, commandList, channelList, sbText, plain, null);

            Set<String> fromPlugins = anticheatsOf(plugins);
            Set<String> fromCommands = anticheatsOf(commands);
            Set<String> fromChannels = anticheatsOf(channels);
            Set<String> certain = new TreeSet<>(fromPlugins);
            certain.addAll(fromCommands);
            certain.addAll(fromChannels);
            if (certain.isEmpty()) return passive;

            List<String> sources = new ArrayList<>();
            if (!fromPlugins.isEmpty()) sources.add("plugins");
            if (!fromCommands.isEmpty()) sources.add("commands");
            if (!fromChannels.isEmpty()) sources.add("channels");
            boolean packetLevel = false;
            for (String ac : certain) {
                if (FINGERPRINTS.containsKey(ac) || OWN_PINGS.contains(ac)) packetLevel = true;
            }
            Verdict also = null;
            String certainConflict = null;
            if (!packetLevel) {
                if (name != null && !name.equals("Unknown")) {
                    also = passive;
                    certainConflict = conflict;
                }
            } else {
                List<String> disagree = new ArrayList<>();
                if (guess != null && !guess.equals("Unknown")) {
                    Set<String> accepted = new LinkedHashSet<>();
                    for (String ac : certain) accepted.addAll(FINGERPRINTS.getOrDefault(ac, Set.of()));
                    if (!accepted.contains(guess)) disagree.add("pings say " + guess);
                }
                if (sbName != null && !certain.contains(sbName)) disagree.add("setbacks say " + sbName);
                if (!disagree.isEmpty()) {
                    certainConflict = joinAnd(sources) + " say " + String.join(", ", certain) + "; " + String.join("; ", disagree)
                        + " (trusting " + joinAnd(sources) + ")";
                }
            }
            boolean certainDone = packetLevel || ids.isEmpty() || done;
            return new Verdict(String.join(", ", certain), "high confidence", String.join(", ", sources), null, certainDone, deadline,
                ids, guess, check, certainConflict, pluginList, commandList, channelList, sbText, plain, also);
        }
    }
}
