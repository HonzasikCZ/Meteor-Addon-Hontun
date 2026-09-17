package cz.honzasik.hontun.commands;

import com.mojang.brigadier.suggestion.Suggestion;
import cz.honzasik.hontun.utils.HontunChat;
import cz.honzasik.hontun.utils.MCUtil;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class PlayerRoster {
    public static final PlayerRoster INSTANCE = new PlayerRoster();

    private static final String[] PROBES = {
        "/msg ", "/tell ", "/w ", "/whisper ", "/pm ", "/message ", "/dm ",
        "/tpa ", "/tpahere ", "/e ", "/reply "
    };

    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");

    private static final Set<String> NOT_A_PLAYER = Set.of(
        "accept", "deny", "cancel", "toggle", "list", "help", "on", "off", "all",
        "here", "reload", "confirm", "true", "false", "yes", "no", "me");

    private static final int SENDS_PER_TICK = 3;
    private static final int TIMEOUT_TICKS = 100;
    private static final int IDLE_TIMEOUT_TICKS = 30;

    private static int nextId = 52000;

    private final Set<Integer> pending = ConcurrentHashMap.newKeySet();
    private final Deque<String> queue = new ArrayDeque<>();
    private final Set<String> roster = Collections.synchronizedSet(new TreeSet<>(String.CASE_INSENSITIVE_ORDER));

    private final Set<String> tabLower = ConcurrentHashMap.newKeySet();
    private final List<String> tabNames = Collections.synchronizedList(new ArrayList<>());
    private volatile String selfName = "";

    private volatile boolean active = false;
    private volatile long deadlineTick = 0;
    private volatile long lastActivityTick = 0;

    private PlayerRoster() {}

    public void run() {
        ClientPacketListener conn = MCUtil.getPlayNetHandler();
        if (conn == null) { ChatUtils.error("Not connected to a server."); return; }
        if (active) { ChatUtils.warning("A player scan is already running."); return; }

        roster.clear();
        pending.clear();
        queue.clear();
        tabLower.clear();
        tabNames.clear();

        selfName = MCUtil.MC.player != null ? MCUtil.MC.player.getName().getString() : "";

        for (PlayerInfo pi : conn.getOnlinePlayers()) {
            String n = pi.getProfile().name();
            if (n != null && !n.isBlank()) {
                tabNames.add(n);
                tabLower.add(n.toLowerCase(Locale.ROOT));
            }
        }

        for (String probe : PROBES) queue.add(probe);

        active = true;
        long now = MCUtil.MC.clientTickCount;
        deadlineTick = now + TIMEOUT_TICKS;
        lastActivityTick = now;
        ChatUtils.info("Probing real player list via message-command suggestions ...");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!active) return;
        ClientPacketListener conn = MCUtil.getPlayNetHandler();
        if (conn == null) { active = false; return; }

        long now = MCUtil.MC.clientTickCount;

        int sent = 0;
        while (!queue.isEmpty() && sent < SENDS_PER_TICK) {
            String buf = queue.poll();
            int id = nextId++;
            pending.add(id);
            conn.send(new ServerboundCommandSuggestionPacket(id, buf));
            sent++;
            lastActivityTick = now;
        }

        boolean drained = queue.isEmpty();
        boolean idle = now - lastActivityTick > IDLE_TIMEOUT_TICKS;

        if ((drained && pending.isEmpty()) || now >= deadlineTick || (drained && idle)) {
            active = false;
            print();
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (!(event.packet instanceof ClientboundCommandSuggestionsPacket packet)) return;
        if (!pending.remove(packet.id())) return;

        for (Suggestion s : packet.toSuggestions().getList()) {
            String t = s.getText();
            if (t == null) continue;
            if (t.startsWith("/")) t = t.substring(1);
            t = t.trim();
            if (!NAME.matcher(t).matches()) continue;
            if (NOT_A_PLAYER.contains(t.toLowerCase(Locale.ROOT))) continue;
            roster.add(t);
        }
        lastActivityTick = MCUtil.MC.clientTickCount;
    }

    private void print() {
        List<String> found = new ArrayList<>(roster);
        found.sort(String.CASE_INSENSITIVE_ORDER);

        Set<String> foundLower = new LinkedHashSet<>();
        for (String n : found) foundLower.add(n.toLowerCase(Locale.ROOT));

        List<String> hidden = new ArrayList<>();
        for (String n : found) {
            String low = n.toLowerCase(Locale.ROOT);
            if (tabLower.contains(low)) continue;
            if (!selfName.isEmpty() && low.equals(selfName.toLowerCase(Locale.ROOT))) continue;
            hidden.add(n);
        }

        List<String> tab = new ArrayList<>(tabNames);
        tab.sort(String.CASE_INSENSITIVE_ORDER);

        MCUtil.forceMainThread(() -> {
            ChatUtils.sendMsg(HontunChat.light("Tab list ")
                .append(HontunChat.punct("(")).append(HontunChat.value(String.valueOf(tab.size()))).append(HontunChat.punct("), "))
                .append(HontunChat.light("suggestion roster "))
                .append(HontunChat.punct("(")).append(HontunChat.value(String.valueOf(found.size()))).append(HontunChat.punct(")")));

            if (found.isEmpty()) {
                ChatUtils.warning("Server exposed no player-name suggestions (message commands may be disabled).");
                return;
            }

            if (hidden.isEmpty()) {
                ChatUtils.info("No hidden players: every suggested name is already in the tab list.");
            } else {
                MutableComponent msg = HontunChat.light("Hidden players ")
                    .append(HontunChat.punct("(")).append(HontunChat.value(String.valueOf(hidden.size())))
                    .append(HontunChat.punct("), not in tab list: "));
                boolean first = true;
                for (String n : hidden) {
                    if (!first) msg.append(HontunChat.punct(", "));
                    first = false;
                    msg.append(HontunChat.value(n));
                }
                ChatUtils.sendMsg(msg);
            }
        });
    }
}
