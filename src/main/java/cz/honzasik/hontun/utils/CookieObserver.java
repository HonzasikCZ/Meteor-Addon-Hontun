package cz.honzasik.hontun.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.ClientboundStoreCookiePacket;
import net.minecraft.network.protocol.cookie.ClientboundCookieRequestPacket;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class CookieObserver {
    private static final int MAX_COOKIES = 64;
    private static final int MAX_HEX_BYTES = 2048;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final Map<String, Entry> COOKIES = new ConcurrentHashMap<>();
    private static final AtomicBoolean DIRTY = new AtomicBoolean(false);
    private static volatile String server = null;
    private static volatile Path file = null;

    private CookieObserver() {}

    private static final class Entry {
        volatile boolean stored;
        volatile boolean requested;
        volatile int size;
        volatile String hex;
        final long first = System.currentTimeMillis();
        volatile long last = System.currentTimeMillis();
    }

    public static void observe(Object packet) {
        if (packet instanceof ClientboundStoreCookiePacket store) {
            record(store.key().toString(), store.payload(), true);
        } else if (packet instanceof ClientboundCookieRequestPacket request) {
            record(request.key().toString(), null, false);
        }
    }

    private static void record(String key, byte[] payload, boolean store) {
        Entry e = COOKIES.get(key);
        if (e == null) {
            if (COOKIES.size() >= MAX_COOKIES) return;
            e = COOKIES.computeIfAbsent(key, k -> new Entry());
        }
        e.last = System.currentTimeMillis();
        if (store) {
            e.stored = true;
            e.size = payload == null ? 0 : payload.length;
            e.hex = payload == null ? "" : hex(payload);
        } else {
            e.requested = true;
        }
        DIRTY.set(true);
    }

    private static String hex(byte[] bytes) {
        int n = Math.min(bytes.length, MAX_HEX_BYTES);
        StringBuilder sb = new StringBuilder(n * 3);
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(i % 32 == 0 ? '\n' : ' ');
            sb.append(Character.forDigit((bytes[i] >> 4) & 0xF, 16)).append(Character.forDigit(bytes[i] & 0xF, 16));
        }
        if (bytes.length > n) sb.append("\n... (").append(bytes.length - n).append(" more bytes)");
        return sb.toString();
    }

    public static void tick() {
        String now = currentServer();
        if (now == null) {
            if (server != null) { COOKIES.clear(); server = null; file = null; DIRTY.set(false); }
            return;
        }
        if (!now.equals(server)) {
            COOKIES.clear();
            server = now;
            file = dir().resolve(sanitize(now) + ".txt");
            DIRTY.set(false);
            return;
        }
        if (DIRTY.getAndSet(false) && !COOKIES.isEmpty()) write();
    }

    private static String currentServer() {
        try {
            var data = MCUtil.MC.getCurrentServer();
            if (data != null && data.ip != null && !data.ip.isBlank()) return data.ip;
            if (MCUtil.getPlayNetHandler() != null) return "direct";
        } catch (Throwable ignored) {}
        return null;
    }

    private static Path dir() {
        return FabricLoader.getInstance().getGameDir().resolve("hontun").resolve("cookies");
    }

    private static String sanitize(String addr) {
        StringBuilder sb = new StringBuilder(addr.length());
        for (int i = 0; i < addr.length(); i++) {
            char c = addr.charAt(i);
            sb.append((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '.' || c == '-' ? c : '_');
        }
        return sb.length() > 80 ? sb.substring(0, 80) : sb.toString();
    }

    private static void write() {
        Path f = file;
        if (f == null) return;
        boolean blocking = hspooferBlocking();
        StringBuilder sb = new StringBuilder();
        sb.append("Server cookies seen by Hontun\n");
        sb.append("server: ").append(server).append('\n');
        sb.append("captured: ").append(LocalTime.now().format(TIME)).append('\n');
        sb.append("note: the server tried to store/read these cookies on your client");
        if (blocking) sb.append("; HSpoofer is BLOCKING the stores, so the server gets nothing back");
        sb.append(".\n\n");
        for (Map.Entry<String, Entry> me : COOKIES.entrySet()) {
            Entry e = me.getValue();
            String kind = e.stored && e.requested ? "STORE+REQUEST" : e.stored ? "STORE" : "REQUEST";
            sb.append('[').append(kind).append("]  ").append(me.getKey());
            if (e.stored) sb.append("   ").append(e.size).append(" B");
            sb.append("   first=").append(LocalTime.ofSecondOfDay((e.first / 1000) % 86400).format(TIME));
            sb.append(" last=").append(LocalTime.ofSecondOfDay((e.last / 1000) % 86400).format(TIME)).append('\n');
            if (e.stored && e.hex != null && !e.hex.isEmpty()) sb.append("  hex: ").append(e.hex.replace("\n", "\n       ")).append('\n');
            sb.append('\n');
        }
        try {
            Files.createDirectories(f.getParent());
            Files.writeString(f, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {}
    }

    private static boolean hspooferBlocking() {
        return FabricLoader.getInstance().isModLoaded("hspoofer");
    }

    public static MutableComponent line() {
        if (COOKIES.isEmpty()) return chat("\nCookies: ").append(dim("none observed"));
        int stored = 0, requested = 0;
        for (Entry e : COOKIES.values()) { if (e.stored) stored++; if (e.requested) requested++; }
        MutableComponent msg = chat("\nCookies: ").append(value(stored + " stored, " + requested + " requested"));
        int shown = 0;
        for (Map.Entry<String, Entry> me : COOKIES.entrySet()) {
            if (shown++ >= 8) { msg.append(dim(" ...")); break; }
            Entry e = me.getValue();
            String tag = e.stored ? e.size + " B" : "request";
            msg.append(dim("\n  · ")).append(value(me.getKey())).append(dim(" (" + tag + ")"));
        }
        if (hspooferBlocking()) msg.append(dim("\n  stores are being blocked by HSpoofer"));
        Path f = file;
        if (f != null) {
            MutableComponent link = HontunChat.punct("\n  file: hontun/cookies/" + f.getFileName().toString())
                    .withStyle(s -> s.withClickEvent(new ClickEvent.OpenFile(f.toFile()))
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal(f.toAbsolutePath().toString()))));
            msg.append(link);
        }
        return msg;
    }

    public static void reset() {
        COOKIES.clear();
        server = null;
        file = null;
        DIRTY.set(false);
    }

    private static MutableComponent chat(String s) { return HontunChat.light(s); }
    private static MutableComponent value(String s) { return HontunChat.value(s); }
    private static MutableComponent dim(String s) { return HontunChat.dim(s); }
}
