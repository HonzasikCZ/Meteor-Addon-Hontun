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

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChannelFetcher extends Module {
    public static final Set<String> REGISTERED = Collections.synchronizedSet(new LinkedHashSet<>());

    public static final Set<String> SEEN = Collections.synchronizedSet(new LinkedHashSet<>());

    private record Pending(Component msg, long dueMs) {}
    private static final Queue<Pending> PENDING = new ConcurrentLinkedQueue<>();

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
        .defaultValue(List.of("voicechat:state"))
        .visible(printToChat::get)
        .build()
    );

    public ChannelFetcher() {
        super(Hontun.CATEGORY, "Channel Fetcher", "Records plugin channels the server sends (register + payloads).");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (PENDING.isEmpty()) return;
        long now = System.currentTimeMillis();
        Pending p;
        while ((p = PENDING.peek()) != null && p.dueMs() <= now) {
            PENDING.poll();
            info(p.msg());
        }
    }

    @Override
    public void onDeactivate() {
        PENDING.clear();
    }

    public static void onRawPayload(Identifier id, byte[] data) {
        String channel = id.toString();

        if (channel.equals("minecraft:brand")) return;

        ChannelFetcher self = Modules.get().get(ChannelFetcher.class);
        if (self == null || !self.isActive()) return;

        if (channel.equals("minecraft:register")) {
            for (String ch : new String(data, StandardCharsets.UTF_8).split("\0")) {
                if (ch.isEmpty()) continue;
                REGISTERED.add(ch);
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

        boolean firstSight = SEEN.add(channel);
        if (!firstSight && isIgnored(channel)) return;

        enqueue(HontunChat.light("Channel").append(HontunChat.punct(":")).append(Component.literal(" "))
            .append(HontunChat.value(channel)).append(Component.literal(" "))
            .append(HontunChat.dim("|")).append(Component.literal(" "))
            .append(HontunChat.light("Data")).append(HontunChat.punct(":")).append(Component.literal(" "))
            .append(HontunChat.light(readableData(data))));
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
        int delayTicks = self.printDelay.get();
        PENDING.add(new Pending(msg, System.currentTimeMillis() + delayTicks * 50L));
    }

    private static String readableData(byte[] data) {
        if (data == null || data.length == 0) return "(empty)";

        int printable = 0;
        for (byte b : data) {
            int u = b & 0xFF;
            if (u == 0x09 || u == 0x0A || u == 0x0D || (u >= 0x20 && u <= 0x7E)) printable++;
        }
        if (printable >= data.length * 0.9) {
            StringBuilder sb = new StringBuilder(data.length);
            for (byte b : data) {
                int u = b & 0xFF;
                if (u == 0x09 || u == 0x0A || u == 0x0D) sb.append(' ');
                else if (u < 0x20 || u > 0x7E) sb.append('·');
                else sb.append((char) u);
            }
            return sb.toString();
        }

        int max = Math.min(data.length, 64);
        StringBuilder hex = new StringBuilder("(" + data.length + " B) ");
        for (int i = 0; i < max; i++) {
            if (i > 0) hex.append(' ');
            hex.append(String.format("%02X", data[i] & 0xFF));
        }
        if (data.length > max) hex.append(" …");
        return hex.toString();
    }

    public static final class Cleaner {
        @EventHandler
        private void onGameLeft(GameLeftEvent event) {
            REGISTERED.clear();
            SEEN.clear();
            PENDING.clear();
            cz.honzasik.hontun.utils.VersionKeeper.clear();
            cz.honzasik.hontun.utils.WorldInfo.clear();
            cz.honzasik.hontun.utils.ResourcePackInfo.clear();
        }
    }
}
