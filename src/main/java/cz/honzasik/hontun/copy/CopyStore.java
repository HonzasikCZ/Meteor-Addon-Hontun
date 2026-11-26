package cz.honzasik.hontun.copy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.copy.capture.Hologram;
import cz.honzasik.hontun.copy.capture.Motion;
import cz.honzasik.hontun.copy.capture.SidebarCapture;
import cz.honzasik.hontun.copy.capture.TabCapture;
import meteordevelopment.meteorclient.utils.Utils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CopyStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final class State {
        public TabCapture.Snapshot tab;
        public SidebarCapture.Snapshot sidebar;
        public Motion.Tab tabMotion;
        public Motion.Sidebar sidebarMotion;
        public Map<String, List<Hologram>> holograms = new LinkedHashMap<>();

        public List<Hologram> holograms(Target target) {
            if (holograms == null) holograms = new LinkedHashMap<>();
            return holograms.computeIfAbsent(target.name(), k -> new ArrayList<>());
        }
    }

    private CopyStore() {}

    public static Path root() {
        return FabricLoader.getInstance().getGameDir().resolve("hontun").resolve("copy");
    }

    public static String serverKey() {
        Minecraft mc = Minecraft.getInstance();
        ServerData data = mc.getCurrentServer();
        if (data != null && data.ip != null && !data.ip.isBlank()) return sanitize(data.ip);
        if (mc.isLocalServer()) {
            String world = Utils.getWorldName();
            return "singleplayer_" + sanitize(world == null || world.isBlank() ? "world" : world);
        }
        return "unknown_server";
    }

    private static final DateTimeFormatter SESSION = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
    private static WeakReference<Object> sessionOwner = new WeakReference<>(null);
    private static String sessionServer;
    private static String sessionName;

    public static String saveKey() {
        Minecraft mc = Minecraft.getInstance();
        Object owner = mc.getConnection();
        String server = serverKey();
        if (owner == null || owner != sessionOwner.get() || !server.equals(sessionServer) || sessionName == null) {
            sessionOwner = new WeakReference<>(owner);
            sessionServer = server;
            String base = LocalDateTime.now().format(SESSION);
            String name = base;
            int n = 2;
            while (Files.exists(root().resolve(server).resolve(name))) name = base + " (" + n++ + ")";
            sessionName = name;
        }
        return server + "/" + sessionName;
    }

    public static Path serverDir(String key) {
        return root().resolve(key);
    }

    public static State load(String key) {
        Path file = root().resolve(".state").resolve(key + ".json");
        if (!Files.isRegularFile(file)) return new State();
        try {
            State s = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), State.class);
            return s == null ? new State() : s;
        } catch (Throwable t) {
            Hontun.LOG.warn("[Hontun] Could not read the copy state {}: {}", file, t.toString());
            return new State();
        }
    }

    public static void save(String key, State state) {
        Path file = root().resolve(".state").resolve(key + ".json");
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(state), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Hontun.LOG.warn("[Hontun] Could not save the copy state {}: {}", file, e.toString());
        }
    }

    public static List<Path> write(String key, Map<String, String> files) throws IOException {
        Path dir = serverDir(key);
        List<Path> out = new ArrayList<>();
        for (Map.Entry<String, String> e : files.entrySet()) {
            Path target = dir.resolve(e.getKey()).normalize();
            if (!target.startsWith(dir)) continue;
            Files.createDirectories(target.getParent());
            Files.writeString(target, e.getValue(), StandardCharsets.UTF_8);
            out.add(target);
        }
        return out;
    }

    public static void prune(String key, String sub, Set<String> keep) {
        Path dir = serverDir(key).resolve(sub);
        if (!Files.isDirectory(dir)) return;
        try (var files = Files.list(dir)) {
            for (Path f : files.toList()) {
                String rel = sub + "/" + f.getFileName();
                if (rel.endsWith(".yml") && !keep.contains(rel)) Files.deleteIfExists(f);
            }
        } catch (IOException e) {
            Hontun.LOG.warn("[Hontun] Could not clean {}: {}", dir, e.toString());
        }
    }

    public static String world(String dimension) {
        return switch (dimension) {
            case "minecraft:overworld" -> "world";
            case "minecraft:the_nether" -> "world_nether";
            case "minecraft:the_end" -> "world_the_end";
            default -> {
                int colon = dimension.indexOf(':');
                yield colon >= 0 ? dimension.substring(colon + 1) : dimension;
            }
        };
    }

    public static String sanitize(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            sb.append((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '.' || c == '-' ? c : '_');
        }
        String out = sb.length() > 80 ? sb.substring(0, 80) : sb.toString();
        return out.isEmpty() ? "server" : out;
    }
}
