package cz.honzasik.hontun.commands;

import com.mojang.brigadier.suggestion.Suggestion;
import cz.honzasik.hontun.utils.HontunChat;
import cz.honzasik.hontun.utils.MCUtil;
import cz.honzasik.hontun.utils.ServerSoftware;
import cz.honzasik.hontun.utils.ServerSoftware.Evidence;
import cz.honzasik.hontun.utils.ServerSoftware.Family;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.world.entity.player.ChatVisiblity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoftwareProbe {
    public static final SoftwareProbe INSTANCE = new SoftwareProbe();

    private enum Phase { IDLE, SILENT, CHAT }

    private enum Shape { ONE, TWO, CUSTOM }

    private static final int SILENT_TICKS = 30;
    private static final int UNKNOWN_DELAY_TICKS = 30;
    private static final int CHAT_TIMEOUT_TICKS = 200;
    private static final int UNKNOWN_SETTLE_TICKS = 20;
    private static final int LATE_FILTER_TICKS = 300;

    private static final Pattern VERSION_LINE = Pattern.compile("This server is running (.+?) version (\\S+)");

    private static int nextId = 63000;

    private volatile Phase phase = Phase.IDLE;
    private boolean full;
    private long silentStartTick;

    private volatile int nonsenseId = -1;
    private volatile int helpId = -1;
    private volatile int rootId = -1;
    private volatile Boolean nonsenseEmpty;
    private volatile List<String> helpTopics;
    private volatile boolean rootReplied;

    private volatile String token = "";
    private volatile long versionSentTick = -1;
    private volatile long unknownSentTick = -1;
    private volatile long unknownFirstTick = -1;
    private volatile String versionText;
    private volatile boolean versionUnknown;
    private volatile boolean versionDenied;
    private volatile Shape shape;
    private volatile long lateFilterUntil = -1;

    private final List<Evidence> evidence = new ArrayList<>();
    private final List<String> notes = new ArrayList<>();
    private String exactName;

    private SoftwareProbe() {}

    public boolean running() { return phase != Phase.IDLE; }

    public void passive() {
        if (MCUtil.getPlayNetHandler() == null) { ChatUtils.error("Not connected to a server."); return; }
        report(ServerSoftware.detect());
    }

    public void run(boolean full) {
        ClientPacketListener conn = MCUtil.getPlayNetHandler();
        if (conn == null) { ChatUtils.error("Not connected to a server."); return; }
        if (running()) { ChatUtils.warning("A software probe is already running."); return; }

        this.full = full;
        evidence.clear();
        notes.clear();
        exactName = null;
        nonsenseEmpty = null;
        helpTopics = null;
        rootReplied = false;
        nonsenseId = -1;
        helpId = -1;
        rootId = -1;
        versionText = null;
        versionUnknown = false;
        versionDenied = false;
        shape = null;
        versionSentTick = -1;
        unknownSentTick = -1;
        unknownFirstTick = -1;
        token = randomToken();

        ChatUtils.info("Probing server software%s ...", full ? " [full]" : "");
        if (!ServerSoftware.translated()) {
            nonsenseId = nextId++;
            helpId = nextId++;
            rootId = nextId++;
            conn.send(new ServerboundCommandSuggestionPacket(nonsenseId, "/" + token + " "));
            conn.send(new ServerboundCommandSuggestionPacket(helpId, "/help "));
            conn.send(new ServerboundCommandSuggestionPacket(rootId, "/"));
            silentStartTick = MCUtil.MC.clientTickCount;
            phase = Phase.SILENT;
        } else {
            notes.add("Suggestion probes skipped while ViaFabricPlus translates the connection.");
            if (full) startChat(conn);
            else finish();
        }
    }

    private static String randomToken() {
        StringBuilder sb = new StringBuilder();
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int i = 0; i < 9; i++) sb.append((char) ('a' + r.nextInt(26)));
        return sb.toString();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        Phase p = phase;
        if (p == Phase.IDLE) return;
        ClientPacketListener conn = MCUtil.getPlayNetHandler();
        if (conn == null) { phase = Phase.IDLE; return; }
        long now = MCUtil.MC.clientTickCount;

        if (p == Phase.SILENT) {
            boolean allIn = rootReplied && nonsenseEmpty != null && helpTopics != null;
            if (allIn || now - silentStartTick >= SILENT_TICKS) {
                collectSilent();
                if (full) startChat(conn);
                else finish();
            }
            return;
        }

        if (p == Phase.CHAT) {
            if (unknownSentTick < 0 && now - versionSentTick >= UNKNOWN_DELAY_TICKS) {
                unknownSentTick = now;
                conn.sendCommand(token);
            }
            boolean versionDone = versionText != null || versionUnknown || versionDenied;
            boolean unknownDone = shape != null
                    || (unknownFirstTick >= 0 && now - unknownFirstTick >= UNKNOWN_SETTLE_TICKS);
            if ((versionDone && unknownDone) || now - versionSentTick >= CHAT_TIMEOUT_TICKS) {
                collectChat();
                finish();
            }
        }
    }

    private void startChat(ClientPacketListener conn) {
        if (MCUtil.MC.player == null || MCUtil.MC.player.isDeadOrDying()) {
            notes.add("Skipped /version because you are dead (Bukkit refuses commands then).");
            finish();
            return;
        }
        if (MCUtil.MC.options.chatVisibility().get() == ChatVisiblity.HIDDEN) {
            notes.add("Skipped /version because chat is hidden in your settings (the server refuses commands).");
            finish();
            return;
        }
        versionSentTick = MCUtil.MC.clientTickCount;
        phase = Phase.CHAT;
        conn.sendCommand("version");
    }

    private void collectSilent() {
        Boolean empty = nonsenseEmpty;
        if (empty == null) {
            if (rootReplied || helpTopics != null) {
                evidence.add(new Evidence("tab", "no reply to a suggestion for an unknown command (Bukkit behaviour)", Family.BUKKIT, 3));
            } else {
                notes.add("The server answered no suggestion requests, so tab-complete is probably blocked.");
            }
        } else if (empty) {
            evidence.add(new Evidence("tab", "empty reply to a suggestion for an unknown command (vanilla behaviour)", Family.VANILLA, 3, true));
        }

        List<String> topics = helpTopics;
        if (topics != null) {
            boolean paper = false, bukkit = false;
            for (String t : topics) {
                if (t.equalsIgnoreCase("Paper")) paper = true;
                if (t.equalsIgnoreCase("Bukkit") || t.equalsIgnoreCase("Aliases")) bukkit = true;
            }
            if (paper) evidence.add(new Evidence("help", "/help topics include Paper", Family.PAPER, 2));
            else if (bukkit) evidence.add(new Evidence("help", "/help topics have Bukkit but no Paper topic", Family.BUKKIT, 1, true));
        }
    }

    private static String[] parseVersion(String line) {
        if (line == null) return null;
        Matcher m = VERSION_LINE.matcher(line);
        if (!m.find()) return null;
        String name = m.group(1).trim();
        String ver = m.group(2);
        if (name.equalsIgnoreCase("CraftBukkit")) name = ver.contains("-Spigot-") ? "Spigot" : "CraftBukkit";
        return new String[]{name, ver};
    }

    private void collectChat() {
        String[] parsed = parseVersion(versionText);
        if (parsed != null) {
            evidence.add(new Evidence("version", parsed[0] + " " + parsed[1], ServerSoftware.familyOf(parsed[0]), 0));
            exactName = parsed[0];
        } else if (versionUnknown) {
            notes.add("/version is not available here (unknown command).");
        } else if (versionDenied) {
            notes.add("/version is blocked by permissions.");
        } else {
            notes.add("/version did not answer within 10 s.");
        }

        Shape s = shape;
        if (s == Shape.ONE) {
            evidence.add(new Evidence("unknown-cmd", "unknown-command reply is one message (Paper style)", Family.PAPER, 3));
        } else if (s == Shape.TWO) {
            boolean bukkit = false;
            for (Evidence e : evidence) if ("tab".equals(e.signal()) && e.family() == Family.BUKKIT) bukkit = true;
            evidence.add(new Evidence("unknown-cmd", "unknown-command reply is two messages (Spigot/vanilla style)",
                    bukkit ? Family.BUKKIT : Family.VANILLA, 1, true));
        } else if (s == Shape.CUSTOM) {
            notes.add("The unknown-command message is customized by a plugin.");
        }
    }

    private void finish() {
        ServerSoftware.setProbed(new ServerSoftware.Probe(exactName, ServerSoftware.familyOf(exactName),
                List.copyOf(evidence), List.copyOf(notes), full));
        lateFilterUntil = full ? MCUtil.MC.clientTickCount + LATE_FILTER_TICKS : -1;
        phase = Phase.IDLE;
        ServerSoftware.Result r = ServerSoftware.detect();
        MCUtil.forceMainThread(() -> report(r));
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof ClientboundCommandSuggestionsPacket sug) {
            int id = sug.id();
            if (id == rootId) {
                rootReplied = true;
                event.cancel();
            } else if (id == nonsenseId) {
                nonsenseEmpty = sug.toSuggestions().getList().isEmpty();
                event.cancel();
            } else if (id == helpId) {
                List<String> topics = new ArrayList<>();
                for (Suggestion s : sug.toSuggestions().getList()) topics.add(s.getText().trim());
                helpTopics = topics;
                event.cancel();
            }
            return;
        }
        if (event.packet instanceof ClientboundSystemChatPacket chat && !chat.overlay()) {
            if (phase == Phase.CHAT) {
                if (captureChat(chat.content())) event.cancel();
            } else if (lateFilterUntil >= 0 && MCUtil.MC.clientTickCount <= lateFilterUntil) {
                if (lateChat(chat.content())) event.cancel();
            }
        }
    }

    private static boolean versionCheckLine(String text) {
        String t = text.trim();
        return t.startsWith("Checking version, please wait")
                || t.contains("You are running the latest version")
                || t.contains("version(s) behind")
                || t.contains("versions behind")
                || t.contains("Error obtaining version information")
                || t.contains("Unknown version")
                || t.contains("You are running a development version");
    }

    private static String versionLineOf(String text) {
        String found = null;
        for (String l : text.split("\n")) if (l.contains("This server is running ")) found = l.trim();
        return found;
    }

    private boolean captureChat(Component content) {
        String text = content.getString();
        if (text.contains("This server is running ")) {
            versionText = versionLineOf(text);
            return true;
        }
        if (versionCheckLine(text)) return true;

        Set<String> keys = new HashSet<>();
        keys(content, keys, 0);
        boolean unknownKey = keys.contains("command.unknown.command");
        boolean contextKey = keys.contains("command.context.here");
        long now = MCUtil.MC.clientTickCount;

        if (unknownSentTick < 0) {
            if (unknownKey || (contextKey && text.contains("version"))) {
                versionUnknown = true;
                return true;
            }
            if (text.toLowerCase(Locale.ROOT).contains("permission") && now - versionSentTick <= 60) {
                versionDenied = true;
                return true;
            }
            return false;
        }

        boolean ours = text.contains(token);
        if (unknownKey && contextKey) {
            if (shape == null) shape = Shape.ONE;
            return true;
        }
        if (unknownKey) {
            if (unknownFirstTick < 0) unknownFirstTick = now;
            return true;
        }
        if (contextKey && ours) {
            if (shape == null) shape = unknownFirstTick >= 0 ? Shape.TWO : Shape.ONE;
            return true;
        }
        if (ours || text.toLowerCase(Locale.ROOT).contains("unknown command")) {
            if (shape == null) shape = Shape.CUSTOM;
            return true;
        }
        return false;
    }

    private boolean lateChat(Component content) {
        String text = content.getString();
        if (!text.contains("This server is running ")) return versionCheckLine(text);
        String[] parsed = parseVersion(versionLineOf(text));
        ServerSoftware.Probe old = ServerSoftware.probed();
        if (parsed != null && old != null && old.exact() == null) {
            List<Evidence> ev = new ArrayList<>(old.evidence());
            ev.add(new Evidence("version", parsed[0] + " " + parsed[1], ServerSoftware.familyOf(parsed[0]), 0));
            List<String> n = new ArrayList<>(old.notes());
            n.removeIf(s -> s.startsWith("/version did not answer"));
            ServerSoftware.setProbed(new ServerSoftware.Probe(parsed[0], ServerSoftware.familyOf(parsed[0]),
                    List.copyOf(ev), List.copyOf(n), old.full()));
            ServerSoftware.Result r = ServerSoftware.detect();
            MCUtil.forceMainThread(() -> ChatUtils.sendMsg(HontunChat.light("Late /version reply, updated: ")
                    .append(HontunChat.value(summary(r)))));
        }
        return true;
    }

    private static void keys(Component c, Set<String> out, int depth) {
        if (c == null || depth > 8) return;
        if (c.getContents() instanceof TranslatableContents t) {
            out.add(t.getKey());
            for (Object a : t.getArgs()) if (a instanceof Component ac) keys(ac, out, depth + 1);
        }
        for (Component s : c.getSiblings()) keys(s, out, depth + 1);
    }

    public static String summary(ServerSoftware.Result r) {
        StringBuilder sb = new StringBuilder(r.software());
        sb.append(" (");
        if (r.family() != Family.UNKNOWN && !r.software().equals(r.family().label)) sb.append(r.family().label).append(", ");
        sb.append(r.confidence()).append(" confidence)");
        if (r.proxy() != null) sb.append(" behind ").append(r.proxy());
        return sb.toString();
    }

    public static void report(ServerSoftware.Result r) {
        MutableComponent msg = HontunChat.light("Software: ").append(HontunChat.value(r.software()));
        msg.append(HontunChat.punct(" ("));
        if (r.family() != Family.UNKNOWN && !r.software().equals(r.family().label)) {
            msg.append(HontunChat.light(r.family().label)).append(HontunChat.punct(", "));
        }
        msg.append(HontunChat.light(r.confidence() + " confidence")).append(HontunChat.punct(")"));
        if (r.proxy() != null) {
            msg.append(HontunChat.light("\nProxy: ")).append(HontunChat.value(r.proxy()));
        }
        if (!r.evidence().isEmpty()) {
            msg.append(HontunChat.light("\nEvidence:"));
            for (Evidence e : r.evidence()) {
                boolean paperOnBukkit = r.family() == Family.PAPER && e.family() == Family.BUKKIT && !e.againstPaper();
                boolean supports = e.weight() > 0 && (e.family() == r.family() || paperOnBukkit);
                boolean against = (e.weight() > 0 && !supports)
                        || (e.againstPaper() && r.family() == Family.PAPER);
                String mark = supports ? " + " : against ? " - " : " · ";
                msg.append(HontunChat.dim("\n" + mark)).append(HontunChat.light(e.signal() + ": "))
                        .append(HontunChat.punct(e.detail()));
                if (e.weight() > 0) {
                    msg.append(HontunChat.dim(" [")).append(supports ? HontunChat.value(e.family().label + " +" + e.weight())
                            : HontunChat.light(e.family().label + " +" + e.weight())).append(HontunChat.dim("]"));
                }
            }
        }
        for (String n : r.notes()) {
            msg.append(HontunChat.dim("\n ! ")).append(HontunChat.punct(n));
        }
        ServerSoftware.Probe p = ServerSoftware.probed();
        if (p == null) {
            msg.append(HontunChat.dim("\n ")).append(HontunChat.punct("Passive only. .server software probes silently, .server software full also runs /version."));
        } else if (!p.full()) {
            msg.append(HontunChat.dim("\n ")).append(HontunChat.punct(".server software full also runs /version for the exact fork (shows in the server log)."));
        }
        ChatUtils.sendMsg(msg);
    }
}
