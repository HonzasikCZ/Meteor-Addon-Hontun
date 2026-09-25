package cz.honzasik.hontun.utils;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.SharedConstants;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class ServerSoftware {
    public enum Family {
        PAPER("Paper-based"), BUKKIT("Bukkit-based"), VANILLA("Vanilla"),
        FABRIC("Fabric"), FORGE("Forge"), NEOFORGE("NeoForge"), UNKNOWN("Unknown");

        public final String label;
        Family(String label) { this.label = label; }
    }

    public record Evidence(String signal, String detail, Family family, int weight, boolean againstPaper) {
        public Evidence(String signal, String detail, Family family, int weight) {
            this(signal, detail, family, weight, false);
        }
    }

    public record Probe(String exact, Family exactFamily, List<Evidence> evidence, List<String> notes, boolean full) {}

    public record Result(String software, Family family, String proxy, String confidence,
                         List<Evidence> evidence, List<String> notes) {}

    public static final List<String> PAPER_FORKS = Arrays.asList(
            "Paper", "Purpur", "Pufferfish", "Leaf", "Folia", "Gale", "Leaves", "Kaiiju", "Canvas", "DivineMC",
            "Plazma", "Luminol", "Mirai", "Petal", "Sakura", "Scissors", "Tuinity", "Airplane", "ShreddedPaper",
            "SparklyPaper", "Parchment", "Akarin", "Nabulus", "Slice");

    private static final Set<String> chatTypes = ConcurrentHashMap.newKeySet();
    private static final Set<String> registries = ConcurrentHashMap.newKeySet();
    private static final List<Long> keepAlives = new ArrayList<>();
    private static final AtomicInteger chunkBatches = new AtomicInteger();
    private static final AtomicInteger chunks = new AtomicInteger();
    private static volatile long lastKeepAliveId = Long.MIN_VALUE;
    private static volatile List<String> rootCommands = List.of();
    private static volatile Probe probed;

    private ServerSoftware() {}

    public static void reset() {
        chatTypes.clear();
        registries.clear();
        synchronized (keepAlives) { keepAlives.clear(); }
        chunkBatches.set(0);
        chunks.set(0);
        lastKeepAliveId = Long.MIN_VALUE;
        rootCommands = List.of();
        probed = null;
    }

    public static void onRegistry(ClientboundRegistryDataPacket packet) {
        String key = packet.registry().identifier().toString();
        registries.add(key);
        if (!"minecraft:chat_type".equals(key)) return;
        for (RegistrySynchronization.PackedRegistryEntry entry : packet.entries()) chatTypes.add(entry.id().toString());
    }

    public static void onKeepAlive(long id) {
        if (id == lastKeepAliveId) return;
        lastKeepAliveId = id;
        synchronized (keepAlives) {
            keepAlives.add(System.currentTimeMillis());
            while (keepAlives.size() > 10) keepAlives.remove(0);
        }
    }

    public static void onChunkBatch() { chunkBatches.incrementAndGet(); }

    public static void onChunk() { chunks.incrementAndGet(); }

    public static void onCommands(CommandDispatcher<?> dispatcher) {
        List<String> names = new ArrayList<>();
        for (CommandNode<?> node : dispatcher.getRoot().getChildren()) names.add(node.getName());
        rootCommands = List.copyOf(names);
    }

    public static void setProbed(Probe probe) { probed = probe; }

    public static Probe probed() { return probed; }

    public static List<String> rootCommands() { return rootCommands; }

    public static long keepAliveIntervalMs() {
        List<Long> t;
        synchronized (keepAlives) { t = new ArrayList<>(keepAlives); }
        if (t.size() < 2) return -1;
        List<Long> gaps = new ArrayList<>();
        for (int i = 1; i < t.size(); i++) gaps.add(t.get(i) - t.get(i - 1));
        gaps.sort(Long::compare);
        return gaps.get(gaps.size() / 2);
    }

    public static int keepAliveCount() {
        synchronized (keepAlives) { return keepAlives.size(); }
    }

    public static boolean translated() {
        if (!VfpBridge.available()) return false;
        int target = VfpBridge.idOf(VfpBridge.current());
        return target > 0 && target != SharedConstants.getCurrentVersion().protocolVersion();
    }

    public static Family familyOf(String name) {
        if (name == null) return Family.UNKNOWN;
        for (String fork : PAPER_FORKS) if (fork.equalsIgnoreCase(name)) return Family.PAPER;
        if (name.equalsIgnoreCase("Spigot") || name.equalsIgnoreCase("CraftBukkit")) return Family.BUKKIT;
        return Family.UNKNOWN;
    }

    public static Result detect() {
        ClientPacketListener conn = MCUtil.getPlayNetHandler();
        String brand = conn == null ? null : conn.serverBrand();
        List<Evidence> ev = new ArrayList<>();
        List<String> notes = new ArrayList<>();
        boolean translated = translated();

        String backendBrand = brand;
        String proxy = null;
        if (brand != null) {
            if (brand.endsWith(" (Velocity)")) {
                proxy = "Velocity";
                backendBrand = brand.substring(0, brand.length() - " (Velocity)".length()).trim();
                ev.add(new Evidence("brand", "\"" + brand + "\" is Velocity's rewrite of the backend brand", Family.UNKNOWN, 0));
            } else if (brand.contains(" <- ")) {
                String front = brand.substring(0, brand.indexOf(" <- ")).trim();
                proxy = front.split("\\s+")[0];
                backendBrand = brand.substring(brand.indexOf(" <- ") + 4).trim();
                ev.add(new Evidence("brand", "\"" + brand + "\" is a BungeeCord-style proxy chain", Family.UNKNOWN, 0));
            }
        }

        ServerData data = MCUtil.MC.getCurrentServer();
        String pingName = data != null && data.version != null ? data.version.getString() : null;
        if (pingName != null) {
            String lower = pingName.toLowerCase(Locale.ROOT);
            if (lower.startsWith("velocity")) proxy = proxy == null ? "Velocity" : proxy;
            else if (lower.startsWith("bungeecord")) proxy = proxy == null ? "BungeeCord" : proxy;
            else if (lower.startsWith("waterfall")) proxy = proxy == null ? "Waterfall" : proxy;
        }

        List<String> roots = rootCommands;
        Set<String> namespaces = new TreeSet<>();
        for (String r : roots) {
            int i = r.indexOf(':');
            if (i > 0) namespaces.add(r.substring(0, i));
        }
        if (roots.contains("velocity:callback")) {
            proxy = proxy == null ? "Velocity" : proxy;
            ev.add(new Evidence("commands", "velocity:callback is registered by the Velocity proxy", Family.UNKNOWN, 0));
        }

        if (translated) {
            notes.add("ViaFabricPlus is translating to " + VfpBridge.currentName()
                    + ", so registry, chunk, keep-alive and tab-complete signals are not trusted.");
        } else {
            if (!registries.isEmpty()) {
                if (chatTypes.contains("paper:raw")) {
                    ev.add(new Evidence("registry", "chat_type contains paper:raw", Family.PAPER, 5));
                } else if (!chatTypes.isEmpty()) {
                    ev.add(new Evidence("registry", "chat_type has only vanilla entries (" + chatTypes.size() + ")", Family.UNKNOWN, 0));
                }
            }
            int c = chunks.get(), b = chunkBatches.get();
            if (c >= 8) {
                if (b == 0) ev.add(new Evidence("chunks", c + " chunks, no chunk batches", Family.PAPER, 3));
                else ev.add(new Evidence("chunks", c + " chunks in " + b + " batches (vanilla-style)", Family.UNKNOWN, 0, true));
            }

            long ka = keepAliveIntervalMs();
            if (ka > 0) {
                if (ka < 4000) ev.add(new Evidence("keep-alive", "every ~" + ka + " ms (Paper sends one per second)", Family.PAPER, 3));
                else if (ka >= 20000 && ka <= 30000) ev.add(new Evidence("keep-alive", "every ~" + ka + " ms (Spigot uses 25 s)", Family.BUKKIT, 3, true));
                else if (ka >= 12000 && ka < 20000) ev.add(new Evidence("keep-alive", "every ~" + ka + " ms (vanilla timing)", Family.VANILLA, 2, true));
                else ev.add(new Evidence("keep-alive", "every ~" + ka + " ms (no known pattern)", Family.UNKNOWN, 0));
            } else {
                notes.add("Keep-alive timing needs more time on the server (" + keepAliveCount() + " seen so far).");
            }
        }

        if (namespaces.contains("paper")) ev.add(new Evidence("commands", "paper: namespace present", Family.PAPER, 3));
        if (namespaces.contains("purpur")) ev.add(new Evidence("commands", "purpur: namespace present", Family.PAPER, 3));
        if (namespaces.contains("folia")) ev.add(new Evidence("commands", "folia: namespace present", Family.PAPER, 3));
        if (namespaces.contains("spigot")) ev.add(new Evidence("commands", "spigot: namespace present", Family.BUKKIT, 1));
        boolean bukkitCmds = namespaces.contains("bukkit") || roots.contains("icanhasbukkit");
        if (bukkitCmds) ev.add(new Evidence("commands", "Bukkit commands present (bukkit:, icanhasbukkit)", Family.BUKKIT, 2));
        boolean foliaTree = bukkitCmds && roots.contains("msg") && !roots.contains("teammsg") && !roots.contains("trigger");
        if (foliaTree) ev.add(new Evidence("commands", "no teammsg/tm/trigger (Folia removes them)", Family.PAPER, 0));

        String brandLower = backendBrand == null ? "" : backendBrand.toLowerCase(Locale.ROOT);
        if (brandLower.equals("fabric")) ev.add(new Evidence("brand", "fabric", Family.FABRIC, 1));
        else if (brandLower.equals("forge")) ev.add(new Evidence("brand", "forge", Family.FORGE, 1));
        else if (brandLower.equals("neoforge")) ev.add(new Evidence("brand", "neoforge", Family.NEOFORGE, 1));
        else if (brandLower.equals("vanilla")) ev.add(new Evidence("brand", "vanilla", Family.VANILLA, 1));

        Probe probe = probed;
        if (probe != null) {
            ev.addAll(probe.evidence());
            notes.addAll(probe.notes());
        }

        int paper = score(ev, Family.PAPER);
        int bukkit = score(ev, Family.BUKKIT);
        int vanilla = score(ev, Family.VANILLA);
        int fabric = score(ev, Family.FABRIC), forge = score(ev, Family.FORGE), neo = score(ev, Family.NEOFORGE);

        Family family;
        if (paper >= 3) family = Family.PAPER;
        else if (bukkit >= 2) family = Family.BUKKIT;
        else if (neo > 0) family = Family.NEOFORGE;
        else if (forge > 0) family = Family.FORGE;
        else if (fabric > 0) family = Family.FABRIC;
        else if (vanilla > 0) family = Family.VANILLA;
        else family = Family.UNKNOWN;

        int structural = 0;
        for (Evidence e : ev) if (e.family() == family && !"brand".equals(e.signal())) structural += e.weight();
        String confidence = structural >= 6 ? "high" : structural >= 3 ? "medium" : "low";

        String software = name(family, backendBrand, ev, notes, translated);

        if (family == Family.PAPER && foliaTree && !"Folia".equals(software)) {
            notes.add("teammsg/tm/trigger are missing while msg/tell are present, which is how Folia ships"
                    + (backendBrand != null && !backendBrand.isBlank() ? " (brand says \"" + backendBrand + "\")" : "") + ".");
            software = "Folia (likely)";
        }

        if (probe != null && probe.exact() != null) {
            String exact = probe.exact();
            Family ef = probe.exactFamily();
            if (ef == Family.UNKNOWN || family == Family.UNKNOWN || ef == family) {
                software = exact;
                if (family == Family.UNKNOWN && ef != Family.UNKNOWN) family = ef;
            } else if ("low".equals(confidence)) {
                notes.add("/version says " + exact + "; structural signals are too weak to disagree.");
                software = exact;
                family = ef;
            } else {
                notes.add("/version claims \"" + exact + "\", but the structure is " + family.label + " (version output customized).");
            }
        }

        return new Result(software, family, proxy, confidence, ev, notes);
    }

    private static int score(List<Evidence> ev, Family f) {
        int s = 0;
        for (Evidence e : ev) if (e.family() == f) s += e.weight();
        return s;
    }

    private static boolean contradictsPaper(List<Evidence> ev) {
        for (Evidence e : ev) if (e.againstPaper()) return true;
        return false;
    }

    private static String name(Family family, String brand, List<Evidence> ev, List<String> notes, boolean translated) {
        String known = null;
        if (brand != null) {
            for (String fork : PAPER_FORKS) if (fork.equalsIgnoreCase(brand)) known = fork;
            if (brand.equalsIgnoreCase("Spigot")) known = "Spigot";
            if (brand.equalsIgnoreCase("CraftBukkit")) known = "CraftBukkit";
        }
        switch (family) {
            case PAPER -> {
                if (known != null && PAPER_FORKS.contains(known)) return known;
                if (known != null) notes.add("Brand claims \"" + known + "\", but the structure is Paper-based (brand likely spoofed).");
                else if (brand != null && !brand.isBlank()) notes.add("Custom brand \"" + brand + "\" on a Paper-based server.");
                return "Paper-based";
            }
            case BUKKIT -> {
                if ("Spigot".equals(known) || "CraftBukkit".equals(known)) return known;
                if (known != null && PAPER_FORKS.contains(known)) {
                    if (translated || !contradictsPaper(ev)) {
                        notes.add("Paper-only markers are not visible here, so the " + known + " brand is taken as-is.");
                        return known;
                    }
                    notes.add("Brand claims \"" + known + "\", but the structure is plain Bukkit/Spigot (brand likely spoofed).");
                } else if (known == null && brand != null && !brand.isBlank()) {
                    notes.add("Custom brand \"" + brand + "\" on a Bukkit-based server.");
                }
                return "Spigot/CraftBukkit";
            }
            case VANILLA -> { return "Vanilla"; }
            case FABRIC -> { return "Fabric"; }
            case FORGE -> { return "Forge"; }
            case NEOFORGE -> { return "NeoForge"; }
            default -> { return brand != null && !brand.isBlank() ? brand + " (unverified)" : "Unknown"; }
        }
    }
}
