package cz.honzasik.hontun.modules;

import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.utils.HontunChat;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;

import java.util.List;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ChannelFetcher extends Module {
    public static final Set<String> REGISTERED = Collections.synchronizedSet(new LinkedHashSet<>());

    public static final Set<String> SEEN = Collections.synchronizedSet(new LinkedHashSet<>());

    private record Pending(Component msg, long dueMs) {}
    private static final Queue<Pending> PENDING = new ConcurrentLinkedQueue<>();
    private static final int MAX_PENDING = 512;
    private static final int MAX_PRINTS_PER_TICK = 20;
    private static final int MAX_CHANNELS = 1024;
    private static final AtomicInteger PENDING_COUNT = new AtomicInteger();
    private static final AtomicInteger UNPRINTED = new AtomicInteger();
    private static final int CHAT_TEXT = 160;
    private static final int CHAT_BYTES = 24;
    private static final long LOG_BUDGET = 4L << 20;
    private static final AtomicLong LOGGED = new AtomicLong();
    private static final AtomicInteger UNDUMPED = new AtomicInteger();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> printToChat = sgGeneral.add(new BoolSetting.Builder()
        .name("print-to-chat")
        .description("Print each channel to chat as it's captured. Off = silently collect; view them via .server.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> printDelay = sgGeneral.add(new IntSetting.Builder()
        .name("print-delay")
        .description("Delay (ticks) before a captured channel is printed to chat, so it doesn't get buried in the server's join messages. 0 = instant.")
        .defaultValue(0)
        .min(0)
        .sliderRange(0, 200)
        .visible(printToChat::get)
        .build()
    );

    private final Setting<List<String>> ignoreChannels = sgGeneral.add(new StringListSetting.Builder()
        .name("ignore-channels")
        .description("Channels to stop printing AFTER their first sighting. Each channel still prints once (so you see it exists) - only the repeat spam is muted. Use 'namespace:path' for one channel or just 'namespace' for all of it. Recording for .server is unaffected.")
        .defaultValue(List.of("voicechat:state", "voicechat:remove_group", "voicechat:add_group", "voicechat:remove_state"))
        .build()
    );

    public ChannelFetcher() {
        super(Hontun.CATEGORY, "Channel Fetcher", "Records plugin channels the server sends (register + payloads).");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (PENDING.isEmpty()) {
            PENDING_COUNT.set(0);
            int unprinted = UNPRINTED.getAndSet(0);
            if (unprinted > 0) info(HontunChat.dim(unprinted + " more channel messages were not printed, see .server"));
            return;
        }
        long now = System.currentTimeMillis();
        int printed = 0;
        Pending p;
        while (printed < MAX_PRINTS_PER_TICK && (p = PENDING.peek()) != null && p.dueMs() <= now) {
            PENDING.poll();
            PENDING_COUNT.decrementAndGet();
            info(p.msg());
            printed++;
        }
    }

    @Override
    public void onDeactivate() {
        PENDING.clear();
        PENDING_COUNT.set(0);
        UNPRINTED.set(0);
    }

    public static void onRawPayload(Identifier id, byte[] data) {
        cz.honzasik.hontun.utils.ServerSoftware.onRawPayload(id, data);
        AnticheatDetect.onRawPayload(id, data);
        String channel = id.toString();

        if (channel.equals("minecraft:brand")) return;

        ChannelFetcher self = Modules.get().get(ChannelFetcher.class);
        if (self == null || !self.isActive()) return;

        if (channel.equals("minecraft:register")) {
            for (String ch : new String(data, StandardCharsets.UTF_8).split("\0")) {
                if (ch.isEmpty()) continue;
                if (REGISTERED.size() < MAX_CHANNELS) REGISTERED.add(ch);
                enqueue(HontunChat.light("Register").append(HontunChat.punct(":")).append(Component.literal(" "))
                    .append(HontunChat.value(ch)));
            }
            return;
        }
        if (channel.equals("minecraft:unregister")) {
            for (String ch : new String(data, StandardCharsets.UTF_8).split("\0")) {
                if (!ch.isEmpty()) REGISTERED.remove(ch);
            }
            return;
        }

        boolean firstSight = SEEN.size() < MAX_CHANNELS ? SEEN.add(channel) : !SEEN.contains(channel);
        if (!firstSight && isIgnored(channel)) return;

        int length = data == null ? 0 : data.length;
        if (LOGGED.get() < LOG_BUDGET) {
            String full = fullData(data);
            if (LOGGED.addAndGet(full.length()) >= LOG_BUDGET) {
                Hontun.LOG.info("[Channel Fetcher] client log budget of 4 MB reached for this server, later payloads are counted but not dumped until you leave");
                enqueue(HontunChat.light("Channel Fetcher dumped 4 MB of payloads to the client log and stopped dumping for this server. The last payload of every channel is still in .server channels."));
            }
            Hontun.LOG.info("[Channel Fetcher] {} ({} B):\n{}", channel, length, full);
        } else {
            UNDUMPED.incrementAndGet();
        }
        if (!self.printToChat.get()) return;

        MutableComponent msg = HontunChat.light("Channel").append(HontunChat.punct(":")).append(Component.literal(" "))
            .append(HontunChat.value(channel)).append(Component.literal(" "))
            .append(HontunChat.dim("|")).append(Component.literal(" "))
            .append(HontunChat.light("Data")).append(HontunChat.punct(":")).append(Component.literal(" "))
            .append(HontunChat.light(shortData(data)));
        if (truncated(data)) msg.append(HontunChat.dim(" (" + length + " B, full data in the client log)"));
        enqueue(msg);
    }

    private static boolean isIgnored(String channel) {
        ChannelFetcher self = Modules.get().get(ChannelFetcher.class);
        if (self == null) return false;
        for (String e : self.ignoreChannels.get()) {
            if (e == null || e.isBlank()) continue;
            e = e.trim();
            if (e.equals(channel)) return true;
            if (!e.contains(":") && channel.startsWith(e + ":")) return true;
        }
        return false;
    }

    private static void enqueue(MutableComponent msg) {
        ChannelFetcher self = Modules.get().get(ChannelFetcher.class);
        if (self == null || !self.isActive() || !self.printToChat.get()) return;
        if (PENDING_COUNT.get() >= MAX_PENDING) {
            UNPRINTED.incrementAndGet();
            return;
        }
        int delayTicks = self.printDelay.get();
        PENDING.add(new Pending(msg, System.currentTimeMillis() + delayTicks * 50L));
        PENDING_COUNT.incrementAndGet();
    }

    private static String text(byte[] data) {
        String s;
        try {
            s = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(data)).toString();
        } catch (CharacterCodingException e) {
            return null;
        }
        int printable = 0;
        int total = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            total++;
            if (c == '\t' || c == '\n' || c == '\r' || (c >= 0x20 && c != 0x7F && !(c >= 0x80 && c < 0xA0))) printable++;
        }
        return total > 0 && printable >= total * 0.9 ? s : null;
    }

    private static boolean truncated(byte[] data) {
        if (data == null || data.length == 0) return false;
        String s = text(data);
        return s != null ? s.length() > CHAT_TEXT : data.length > CHAT_BYTES;
    }

    private static String shortData(byte[] data) {
        if (data == null || data.length == 0) return "(empty)";
        String s = text(data);
        if (s != null) {
            StringBuilder sb = new StringBuilder(Math.min(s.length(), CHAT_TEXT) + 1);
            int end = Math.min(s.length(), CHAT_TEXT);
            for (int i = 0; i < end; i++) {
                char c = s.charAt(i);
                if (c == '\t' || c == '\n' || c == '\r') sb.append(' ');
                else if (c < 0x20 || c == 0x7F || (c >= 0x80 && c < 0xA0)) sb.append('·');
                else sb.append(c);
            }
            if (s.length() > CHAT_TEXT) sb.append('…');
            return sb.toString();
        }
        int max = Math.min(data.length, CHAT_BYTES);
        StringBuilder hex = new StringBuilder("(" + data.length + " B) ");
        for (int i = 0; i < max; i++) {
            if (i > 0) hex.append(' ');
            hex.append(String.format("%02X", data[i] & 0xFF));
        }
        if (data.length > max) hex.append(" …");
        return hex.toString();
    }

    private static String fullData(byte[] data) {
        if (data == null || data.length == 0) return "(empty)";
        String s = text(data);
        if (s != null) {
            StringBuilder sb = new StringBuilder(s.length() + 16);
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '\n' || c == '\t') sb.append(c);
                else if (c == '\r') continue;
                else if (c < 0x20 || c == 0x7F || (c >= 0x80 && c < 0xA0)) sb.append(String.format("\\x%02X", (int) c));
                else sb.append(c);
            }
            return sb.toString();
        }
        StringBuilder sb = new StringBuilder(data.length * 4 + 64);
        for (int off = 0; off < data.length; off += 16) {
            if (off > 0) sb.append('\n');
            sb.append(String.format("%08X  ", off));
            int end = Math.min(off + 16, data.length);
            for (int i = off; i < off + 16; i++) {
                if (i < end) sb.append(String.format("%02X ", data[i] & 0xFF));
                else sb.append("   ");
                if (i == off + 7) sb.append(' ');
            }
            sb.append(" |");
            for (int i = off; i < end; i++) {
                int u = data[i] & 0xFF;
                sb.append(u >= 0x20 && u <= 0x7E ? (char) u : '.');
            }
            sb.append('|');
        }
        return sb.toString();
    }

    public static final class Cleaner {
        @EventHandler
        private void onGameLeft(GameLeftEvent event) {
            REGISTERED.clear();
            SEEN.clear();
            PENDING.clear();
            PENDING_COUNT.set(0);
            UNPRINTED.set(0);
            LOGGED.set(0);
            int undumped = UNDUMPED.getAndSet(0);
            if (undumped > 0) Hontun.LOG.info("[Channel Fetcher] {} payloads were not dumped after the client log budget was used up", undumped);
            cz.honzasik.hontun.utils.VersionKeeper.clear();
            cz.honzasik.hontun.utils.WorldInfo.clear();
            cz.honzasik.hontun.utils.ResourcePackInfo.left();
        }
    }
}
