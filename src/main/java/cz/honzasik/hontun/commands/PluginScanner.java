package cz.honzasik.hontun.commands;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.CommandNode;
import cz.honzasik.hontun.utils.HontunChat;
import cz.honzasik.hontun.utils.MCUtil;
import net.fabricmc.loader.api.FabricLoader;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class PluginScanner {
    public static final PluginScanner INSTANCE = new PluginScanner();

    public static final Set<String> EXCLUDE_NS = Set.of(
        "minecraft", "bukkit", "spigot", "paper", "velocity", "bungeecord", "waterfall",
        "fabric", "forge", "neoforge", "mojang", "brigadier");

    private static final String[] VERSION_PROBES = {
        "/version ", "/ver ", "/about ", "/icanhasbukkit ",
        "/bukkit:version ", "/bukkit:ver ", "/bukkit:about ", "/paper:version ",
        "/plugins ", "/pl ", "/bukkit:plugins ", "/bukkit:pl "
    };

    private static final Set<String> ANTICHEAT = Set.of(
        "nocheatplus", "negativity", "warden", "horizon", "illegalstack", "coreprotect", "exploitsx",
        "vulcan", "abc", "spartan", "kauri", "anticheatreloaded", "witherac", "godseye", "matrix", "wraith",
        "antixrayheuristics", "grimac", "grim", "themis", "foxaddition", "guardianac", "ggintegrity",
        "lightanticheat", "anarchyexploitfixes", "polar", "intave",

        "antispoof", "lpx", "exploitfixer", "coffeeprotect", "pl-hide-pro", "plhidepro",
        "antihealthindicator", "sonar", "foxgate", "foxgateplus",
        "polarloader", "fairplay", "cultac", "cult", "angleguard");

    private static final int SENDS_PER_TICK = 3;
    private static final int TIMEOUT_TICKS = 120;
    private static final int IDLE_TIMEOUT_TICKS = 40;

    private static int nextId = 41000;

    private final Set<String> found = Collections.synchronizedSet(new TreeSet<>(String.CASE_INSENSITIVE_ORDER));
    private final Set<Integer> versionPending = ConcurrentHashMap.newKeySet();
    private final Set<Integer> enumPending = ConcurrentHashMap.newKeySet();
    private final Deque<String> enumQueue = new ArrayDeque<>();

    private final Set<String> onlinePlayers = ConcurrentHashMap.newKeySet();

    private volatile boolean active = false;
    private volatile boolean fallbackBypass = false;
    private volatile long deadlineTick = 0;
    private volatile long lastActivityTick = 0;

    private PluginScanner() {}

    public void run(boolean namespace, boolean version, boolean bypass, boolean fallbackBypass) {
        ClientPacketListener conn = MCUtil.getPlayNetHandler();
        if (conn == null) { ChatUtils.error("Not connected to a server."); return; }
        if (active) { ChatUtils.warning("A plugin scan is already running."); return; }

        found.clear();
        versionPending.clear();
        enumPending.clear();
        enumQueue.clear();
        this.fallbackBypass = fallbackBypass;

        onlinePlayers.clear();
        for (PlayerInfo pi : conn.getOnlinePlayers()) {
            String n = pi.getProfile().name();
            if (n != null && !n.isBlank()) onlinePlayers.add(n.toLowerCase());
        }

        if (namespace) {
            for (CommandNode<?> node : conn.getCommands().getRoot().getChildren()) addNamespace(node.getName());
        }
        if (version) {
            for (String probe : VERSION_PROBES) {
                int id = nextId++;
                versionPending.add(id);
                conn.send(new ServerboundCommandSuggestionPacket(id, probe));
            }
        }
        if (bypass) queueBypassProbes();

        boolean async = version || bypass;
        if (!async) {
            finishOrFallback();
            return;
        }

        active = true;
        long now = MCUtil.MC.clientTickCount;
        deadlineTick = now + TIMEOUT_TICKS;
        lastActivityTick = now;
        ChatUtils.info("Scanning plugins%s%s%s ...",
            namespace ? " [tree]" : "", version ? " [version]" : "", bypass ? " [bypass]" : "");
    }

    private void queueBypassProbes() {
        enumQueue.add("/help ");
        for (char c = 'a'; c <= 'z'; c++) enumQueue.add("/help " + c);
        for (char c = '0'; c <= '9'; c++) enumQueue.add("/help " + c);
        enumQueue.add("/help _");
        enumQueue.add("/execute run ");
        enumQueue.add("/execute run bukkit:");
        for (char c = 'a'; c <= 'z'; c++) enumQueue.add("/execute run " + c);
        enumQueue.add("/execute as @s run ");
    }

    private static boolean excludedNs(String ns) {
        if (EXCLUDE_NS.contains(ns)) return true;
        try {
            return FabricLoader.getInstance().isModLoaded(ns);
        } catch (Throwable t) {
            return false;
        }
    }

    private void addNamespace(String name) {
        int i = name.indexOf(':');
        if (i > 0) {
            String ns = name.substring(0, i).toLowerCase();
            if (!excludedNs(ns)) found.add(ns);
        }
    }

    private void addPluginCandidate(String t) {
        if (t == null) return;
        if (t.startsWith("/")) t = t.substring(1);
        t = t.trim();
        if (t.isEmpty()) return;
        if (onlinePlayers.contains(t.toLowerCase())) return;
        int c = t.indexOf(':');
        if (c > 0) {
            String ns = t.substring(0, c).toLowerCase();
            if (!excludedNs(ns)) found.add(ns);
            return;
        }
        if (!t.contains(" ") && Character.isLetter(t.charAt(0)) && Character.isUpperCase(t.charAt(0))
            && !excludedNs(t.toLowerCase())) {
            found.add(t);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!active) return;
        ClientPacketListener conn = MCUtil.getPlayNetHandler();
        if (conn == null) { active = false; return; }

        long now = MCUtil.MC.clientTickCount;

        int sent = 0;
        while (!enumQueue.isEmpty() && sent < SENDS_PER_TICK) {
            String buf = enumQueue.poll();
            int id = nextId++;
            enumPending.add(id);
            conn.send(new ServerboundCommandSuggestionPacket(id, buf));
            sent++;
            lastActivityTick = now;
        }

        boolean drained = enumQueue.isEmpty();
        boolean noPending = versionPending.isEmpty() && enumPending.isEmpty();
        boolean idle = now - lastActivityTick > IDLE_TIMEOUT_TICKS;

        if ((drained && noPending) || now >= deadlineTick || (drained && idle)) {
            finishOrFallback();
        }
    }

    private void finishOrFallback() {
        if (found.isEmpty() && fallbackBypass) {
            fallbackBypass = false;
            ClientPacketListener conn = MCUtil.getPlayNetHandler();
            if (conn == null) { active = false; print(); return; }
            versionPending.clear();
            enumPending.clear();
            enumQueue.clear();
            queueBypassProbes();
            active = true;
            long now = MCUtil.MC.clientTickCount;
            deadlineTick = now + TIMEOUT_TICKS;
            lastActivityTick = now;
            ChatUtils.warning("Default scan found nothing — trying CommandWhitelist bypass...");
            return;
        }
        active = false;
        print();
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (!(event.packet instanceof ClientboundCommandSuggestionsPacket packet)) return;
        int id = packet.id();

        if (versionPending.remove(id)) {
            List<String> sugg = new ArrayList<>();
            boolean playerCompletion = false;
            for (Suggestion s : packet.toSuggestions().getList()) {
                String name = s.getText().trim();
                if (name.isBlank()) continue;
                if (onlinePlayers.contains(name.toLowerCase())) { playerCompletion = true; break; }
                sugg.add(name);
            }
            if (playerCompletion) {
                versionPending.clear();
            } else {
                found.addAll(sugg);
            }
            lastActivityTick = MCUtil.MC.clientTickCount;
            return;
        }

        if (enumPending.remove(id)) {
            for (Suggestion s : packet.toSuggestions().getList()) {
                addPluginCandidate(s.getText());
            }
            lastActivityTick = MCUtil.MC.clientTickCount;
        }
    }

    private void print() {
        List<String> list = new ArrayList<>(found);
        list.sort(String.CASE_INSENSITIVE_ORDER);
        MCUtil.forceMainThread(() -> {
            if (list.isEmpty()) {
                ChatUtils.warning("No plugins found.");
                return;
            }

            MutableComponent msg = HontunChat.light("Plugins ")
                .append(HontunChat.punct("("))
                .append(HontunChat.value(String.valueOf(list.size())))
                .append(HontunChat.punct("): "));
            boolean first = true;
            for (String p : list) {
                if (!first) msg.append(HontunChat.punct(", "));
                first = false;
                String low = p.toLowerCase();
                boolean ac = ANTICHEAT.contains(low) || low.contains("anticheat") || low.contains("exploit");
                msg.append(ac ? HontunChat.value(p) : HontunChat.light(p));
            }
            ChatUtils.sendMsg(msg);
        });
    }
}
