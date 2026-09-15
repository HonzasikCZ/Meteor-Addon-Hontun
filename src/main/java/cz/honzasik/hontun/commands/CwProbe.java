package cz.honzasik.hontun.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.CommandNode;
import cz.honzasik.hontun.utils.MCUtil;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
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

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class CwProbe extends Command {
    private static final Set<String> CORE_NS = Set.of("minecraft", "bukkit", "spigot", "paper");
    private static final int SENDS_PER_TICK = 3;
    private static final int IDLE_TIMEOUT_TICKS = 40;

    private static int nextId = 30000;

    private final Set<Integer> pending = ConcurrentHashMap.newKeySet();

    private final Deque<String> enumQueue = new ArrayDeque<>();
    private final Set<Integer> enumPending = ConcurrentHashMap.newKeySet();
    private final Set<String> enumResults = Collections.synchronizedSet(new TreeSet<>());
    private volatile boolean enumActive = false;
    private volatile long lastActivityTick = 0;

    public CwProbe() {
        super("cmdprobe", "Probes/enumerates CommandWhitelist tab-completion (issue #113).");
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(literal("args")
            .then(argument("command", greedyString())
                .suggests((ctx, sb) -> {
                    ClientPacketListener conn = MCUtil.getPlayNetHandler();
                    if (conn != null) {
                        for (CommandNode<?> node : conn.getCommands().getRoot().getChildren()) sb.suggest(node.getName());
                    }
                    return sb.buildFuture();
                })
                .executes(ctx -> {
                    String cmd = StringArgumentType.getString(ctx, "command");
                    probe(cmd.contains(" ") ? "/" + cmd : "/" + cmd + " ");
                    return SINGLE_SUCCESS;
                })));

        builder.then(literal("raw")
            .then(argument("buffer", greedyString())
                .executes(ctx -> {
                    probe(StringArgumentType.getString(ctx, "buffer"));
                    return SINGLE_SUCCESS;
                })));

        builder.then(literal("list")
            .executes(ctx -> {
                dumpTree();
                return SINGLE_SUCCESS;
            }));

        builder.then(literal("enum")
            .executes(ctx -> {
                startEnum();
                return SINGLE_SUCCESS;
            }));
    }

    private void probe(String buffer) {
        ClientPacketListener conn = MCUtil.getPlayNetHandler();
        if (conn == null) { error("Not connected to a server."); return; }
        int id = nextId++;
        pending.add(id);
        conn.send(new ServerboundCommandSuggestionPacket(id, buffer));
        info("Sent id %d, buffer \"%s\"", id, buffer);
    }

    private void dumpTree() {
        ClientPacketListener conn = MCUtil.getPlayNetHandler();
        if (conn == null) { error("Not connected to a server."); return; }
        List<String> names = new ArrayList<>();
        for (CommandNode<?> node : conn.getCommands().getRoot().getChildren()) names.add(node.getName());
        Collections.sort(names);
        info("Command tree: %d commands", names.size());
        infoChunked(names);
    }

    private void startEnum() {
        if (MCUtil.getPlayNetHandler() == null) { error("Not connected to a server."); return; }
        if (enumActive) { warning("Enumeration already running."); return; }

        enumQueue.clear();
        enumPending.clear();
        enumResults.clear();

        enumQueue.add("/help ");
        for (char c = 'a'; c <= 'z'; c++) enumQueue.add("/help " + c);
        for (char c = '0'; c <= '9'; c++) enumQueue.add("/help " + c);
        enumQueue.add("/help _");

        enumQueue.add("/execute run ");
        enumQueue.add("/execute run minecraft:");
        enumQueue.add("/execute run bukkit:");
        for (char c = 'a'; c <= 'z'; c++) enumQueue.add("/execute run " + c);
        enumQueue.add("/execute as @s run ");

        enumActive = true;
        lastActivityTick = MCUtil.MC.clientTickCount;
        info("Enumerating via %d suggestion probes (help paging + execute)... results in a moment.", enumQueue.size());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!enumActive) return;
        ClientPacketListener conn = MCUtil.getPlayNetHandler();
        if (conn == null) { finishEnum(true); return; }

        long now = MCUtil.MC.clientTickCount;

        int sent = 0;
        while (!enumQueue.isEmpty() && sent < SENDS_PER_TICK) {
            String buffer = enumQueue.poll();
            int id = nextId++;
            enumPending.add(id);
            conn.send(new ServerboundCommandSuggestionPacket(id, buffer));
            sent++;
            lastActivityTick = now;
        }

        if (enumQueue.isEmpty()) {
            if (enumPending.isEmpty() || now - lastActivityTick > IDLE_TIMEOUT_TICKS) {
                finishEnum(false);
            }
        }
    }

    private void finishEnum(boolean aborted) {
        enumActive = false;
        if (aborted) return;

        List<String> cmds = new ArrayList<>(enumResults);
        Collections.sort(cmds);

        info("=== enum: %d distinct command names leaked ===", cmds.size());
        infoChunked(cmds);

        Set<String> plugins = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (String c : cmds) {
            int i = c.indexOf(':');
            if (i > 0) {
                String ns = c.substring(0, i).toLowerCase();
                if (!CORE_NS.contains(ns)) plugins.add(ns);
            } else if (!c.isEmpty() && !c.contains(" ") && Character.isLetter(c.charAt(0)) && Character.isUpperCase(c.charAt(0))) {
                plugins.add(c);
            }
        }
        if (!plugins.isEmpty()) {
            info("=== plugins: %d ===", plugins.size());
            infoChunked(new ArrayList<>(plugins));
        } else {
            warning("No plugins leaked. Command names above may still reveal plugins by their labels.");
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (!(event.packet instanceof ClientboundCommandSuggestionsPacket packet)) return;
        int id = packet.id();

        if (enumPending.remove(id)) {
            for (Suggestion s : packet.toSuggestions().getList()) {
                String t = s.getText();
                if (t.startsWith("/")) t = t.substring(1);
                if (!t.isBlank()) enumResults.add(t);
            }
            lastActivityTick = MCUtil.MC.clientTickCount;
            return;
        }

        if (!pending.remove(id)) return;

        List<Suggestion> list = packet.toSuggestions().getList();
        MCUtil.forceMainThread(() -> {
            if (list.isEmpty()) {
                info("id %d -> 0 suggestions (blocked / empty = PATCHED)", id);
            } else {
                warning("id %d -> %d suggestions (if these are hidden-command args = LEAK):", id, list.size());
                for (Suggestion s : list) info(" - %s", s.getText());
            }
        });
    }

    private void infoChunked(List<String> items) {
        System.out.println("[cmdprobe] " + items);
        StringBuilder line = new StringBuilder();
        for (String n : items) {
            if (line.length() + n.length() + 2 > 200) {
                info("%s", line.toString());
                line.setLength(0);
            }
            if (line.length() > 0) line.append(", ");
            line.append(n);
        }
        if (line.length() > 0) info("%s", line.toString());
    }
}
