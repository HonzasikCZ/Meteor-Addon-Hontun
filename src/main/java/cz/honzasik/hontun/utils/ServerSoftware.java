package cz.honzasik.hontun.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import cz.honzasik.hontun.mixin.network.software.MoveEntityPacketAccessor;
import cz.honzasik.hontun.mixin.network.software.RotateHeadPacketAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ClientboundServerLinksPacket;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;
import net.minecraft.network.protocol.configuration.ClientboundUpdateEnabledFeaturesPacket;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundRecipeBookSettingsPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundServerDataPacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTickingStatePacket;
import net.minecraft.network.protocol.game.ClientboundTickingStepPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ClientboundLoginFinishedPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ServerSoftware {
    public enum Family {
        PAPER("Paper-based"), BUKKIT("Bukkit-based"), HYBRID("NeoForge + Paper hybrid"),
        MODDED_BUKKIT("NeoForge/Fabric + Bukkit hybrid"), CUSTOM("Custom (non-Mojang) server"), SPONGE("Sponge"),
        NEOFORGE("NeoForge"), FORGE("Forge"), QUILT("Quilt"), FABRIC("Fabric"), VANILLA("Vanilla"), UNKNOWN("Unknown");

        public final String label;
        Family(String label) { this.label = label; }
    }

    public record Evidence(String signal, String detail, Family family, int weight, boolean againstPaper) {
        public Evidence(String signal, String detail, Family family, int weight) {
            this(signal, detail, family, weight, false);
        }
    }

    public record Result(String software, Family family, String brand, String proxy, String confidence,
                         List<Evidence> evidence, List<String> notes) {}

    private enum JoinMessage { NONE, LITERAL, TRANSLATED, CUSTOM }

    public static final List<String> PAPER_FORKS = List.of("Paper", "Purpur", "Folia", "Canvas", "Leaf", "DivineMC",
            "Pufferfish", "UniverseSpigot", "Leaves", "Pluto", "AdvancedSlimePaper", "Gale", "SparklyPaper",
            "ShreddedPaper", "Plazma");

    private static final Set<Family> MODDED_VANILLA = Set.of(Family.FABRIC, Family.QUILT, Family.FORGE, Family.NEOFORGE);
    private static final int PROTO_1_20_1 = 763;
    private static final int PROTO_1_20_2 = 764;
    private static final int PROTO_1_20_3 = 765;
    private static final int PROTO_1_21 = 767;
    private static final int PROTO_1_21_9 = 773;
    private static final int PROTO_1_21_11 = 774;
    private static final int PROTO_26_1 = 775;
    private static final int PURPUR_MIN_PACKETS = 15;
    private static final double PURPUR_MIN_RATE = 10.0;
    private static final int FABRIC_API_PING = 0xFAB71C;
    private static final long SNAPSHOT_WINDOW_MS = 50;
    private static final long RESPAWN_WINDOW_MS = 250;
    private static final long RESYNC_WINDOW_MS = 50;
    private static final long RESYNC_EARLY_MS = 20000;
    private static final int TRACK_CAP = 512;
    private static final long RAIN_FLIP_WINDOW_MS = 1500;
    private static final Pattern VIAPROXY_BRAND = Pattern.compile("^ViaProxy \\((.+?)\\) -> (.*) \\(([^()]+)\\)$");
    private static final String GATE_SUFFIX = " (Gate by Minekube)";
    private static final Map<String, String> LINEAGE = Map.of(
            "Plazma", "Purpur",
            "DivineMC", "Purpur",
            "Canvas", "Folia",
            "Pluto", "Pufferfish");
    private static final Map<String, Integer> LATE_REGISTRY_PROTOCOL = Map.of(
            "world_clock", PROTO_1_21_11,
            "zombie_nautilus_variant", 771,
            "dialog", 770,
            "frog_variant", 768,
            "instrument", PROTO_1_21,
            "painting_variant", 766);

    private static final Set<String> chatTypes = ConcurrentHashMap.newKeySet();
    private static final Set<String> registries = ConcurrentHashMap.newKeySet();
    private static final Set<String> registryNamespaces = ConcurrentHashMap.newKeySet();
    private static final List<Long> keepAlives = new ArrayList<>();
    private static final List<long[]> keepAliveIds = new ArrayList<>();
    private static volatile long firstKeepAliveAt;
    private static final java.util.ArrayDeque<Object[]> trackedSpawns = new java.util.ArrayDeque<>();
    private static final Set<String> TRACK_ANIMALS = Set.of("pig", "cow", "mooshroom", "sheep", "chicken", "rabbit", "horse", "donkey",
            "mule", "llama", "goat", "cat", "wolf", "fox", "villager", "iron_golem", "bee", "frog", "turtle", "camel", "armadillo",
            "sniffer", "panda", "polar_bear", "ocelot", "parrot");
    private static final Set<String> TRACK_MONSTERS = Set.of("zombie", "husk", "drowned", "skeleton", "stray", "creeper", "spider",
            "cave_spider", "enderman", "witch", "slime", "zombie_villager", "pillager", "vindicator");
    private static final double SPIGOT_TRACK_LIMIT = 58.0;
    private static final double PAPER_TRACK_LIMIT = 106.0;
    private static final AtomicInteger chunkBatches = new AtomicInteger();
    private static final AtomicInteger chunks = new AtomicInteger();
    private static volatile long lastKeepAliveId = Long.MIN_VALUE;
    private static volatile List<String> rootCommands = List.of();
    private static volatile int pluginsArguments = -1;

    private static final Object JOIN = new Object();
    private static final Set<String> loginQueries = new LinkedHashSet<>();
    private static Boolean zeroSession;
    private static UUID selfProfile;
    private static String firstBrand;
    private static String lastBrand;
    private static boolean brandInConfig;
    private static final Set<String> knownPacks = new LinkedHashSet<>();
    private static final List<Integer> configPings = new ArrayList<>();
    private static final Set<String> preLoginPayloads = new LinkedHashSet<>();
    private static final Set<String> registerChannels = new LinkedHashSet<>();
    private static final List<String> lateRegistries = new ArrayList<>();
    private static final Map<String, int[]> tagSummary = new LinkedHashMap<>();
    private static final List<String> levelNames = new ArrayList<>();
    private static int firstTeleportId = Integer.MIN_VALUE;
    private static final Set<Long> loadedChunks = new HashSet<>();
    private static int centerX, centerZ;
    private static boolean centerKnown;
    private static int prevChunkDistance = -1;
    private static int orderedChunks;
    private static int chunkDescents;
    private static int entitiesBeforeChunk;
    private static final Set<Integer> liveEntities = new HashSet<>();
    private static final Map<Integer, Long> equipmentOrphans = new HashMap<>();
    private static final Map<Integer, Long> equipmentSpawns = new HashMap<>();
    private static final Map<Integer, String> equipmentFirst = new HashMap<>();
    private static final Set<Integer> equipmentOrphanIds = new HashSet<>();
    private static final Set<Integer> equipmentDuplicateIds = new HashSet<>();
    private static int equipmentDuplicates;
    private static final Set<String> firstTabActions = new TreeSet<>();
    private static final Set<String> VANILLA_LEVELS = Set.of("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end");
    private static final Map<Integer, Long> pendingSnapshot = new LinkedHashMap<>();
    private static final Set<Integer> pendingPlayers = new HashSet<>();
    private static final Map<Integer, String> pendingTypes = new HashMap<>();
    private static final Map<Integer, long[]> pendingResync = new LinkedHashMap<>();
    private static final Set<String> ascendingKeySets = new HashSet<>();
    private static final Set<Integer> ascendingAttributes = new HashSet<>();
    private static final Set<Integer> zombieHorses = new HashSet<>();
    private static final List<Long> rainCrossings = new ArrayList<>();
    private static final List<Long> rainFlips = new ArrayList<>();
    private static int rainResolvedWith;
    private static int rainResolvedWithout;
    private static final List<Integer> playTimeReadings = new ArrayList<>();
    private static final Map<Identifier, Integer> lastTimeStats = new HashMap<>();
    private static boolean sawRegistry;
    private static boolean brandAfterRegistry;
    private static boolean configFinished;
    private static boolean knownPacksReceived;
    private static boolean knownPacksEmpty;
    private static String coreVersion;
    private static boolean tagsSeen;
    private static boolean featuresSeen;
    private static boolean loggedIn;
    private static boolean joinDone;
    private static long joinDoneAt;
    private static int playPackets;
    private static String firstAfterLogin;
    private static String previousJoinPacket;
    private static boolean tabListBeforeLogin;
    private static boolean clearTitlesAfterLogin;
    private static boolean serverLinksBeforeLogin;
    private static boolean emptyRegisterInPlay;
    private static boolean leavesChannels;
    private static String leavesVersion;
    private static String vvChannel;
    private static int vvVersion;
    private static String vvName;
    private static int ownEntityId = Integer.MIN_VALUE;
    private static long loginAt;
    private static int selfTabAdds;
    private static int firstTabAddEntries = -1;
    private static final StringBuilder tabAddPattern = new StringBuilder();
    private static int pairingWithMaxHealth;
    private static int pairingWithoutMaxHealth;
    private static int pairingWithScale;
    private static final Set<String> pairingTypes = new HashSet<>();
    private static String joinWeather = "";
    private static boolean respawnPending;
    private static int respawnRadiusFirst;
    private static int respawnPositionFirst;
    private static boolean ownDataBeforeBorder;
    private static double farthestTracked;
    private static String farthestTrackedType;
    private static int trackedMobs;
    private static boolean emptyLevels;
    private static int maxPlayers = -1;
    private static int serverDataCount;
    private static boolean serverDataAfterPosition;
    private static int serverDataBorders = -1;
    private static int joinIndex;
    private static int borderCount;
    private static int tickingStates;
    private static int tickingSteps;
    private static boolean viewDistanceSent;
    private static int commandsAt = -1;
    private static int recipeBookAt = -1;
    private static JoinMessage joinMessage = JoinMessage.NONE;
    private static int setTimePackets;
    private static int setTimeSingleClock;
    private static long setTimeFirst;
    private static long setTimeLast;
    private static long respawnAt;
    private static boolean ownJoinListSeen;
    private static int ownJoinLength;
    private static boolean ownJoinAscending;
    private static int pairingLists;
    private static int purpurAttrLists;
    private static int longestAscending;
    private static int unsortedSnapshots;
    private static int unsortedSnapshotsLong;
    private static int resyncHits;
    private static int resyncMisses;
    private static int resyncEarlyHits;
    private static int resyncEarlyMisses;
    private static int speedExact;
    private static int speedFloat;
    private static int zombieHorse15;
    private static int zombieHorse25;
    private static float lastRainLevel = -1f;
    private static int timeStatsReadings;
    private static boolean timeStatsOffGrid;
    private static boolean crouchSeen;
    private static boolean timeStatsAbsoluteGrid = true;
    private static int timeStatsNonZero;

    private ServerSoftware() {}

    public static void resetAll() {
        synchronized (JOIN) {
            loginQueries.clear();
            zeroSession = null;
            selfProfile = null;
        }
        resetConfig();
    }

    public static void resetConfig() {
        chatTypes.clear();
        registries.clear();
        registryNamespaces.clear();
        synchronized (JOIN) {
            firstBrand = null;
            lastBrand = null;
            brandInConfig = false;
            knownPacks.clear();
            configPings.clear();
            preLoginPayloads.clear();
            registerChannels.clear();
            lateRegistries.clear();
            tagSummary.clear();
            sawRegistry = false;
            brandAfterRegistry = false;
            configFinished = false;
            knownPacksReceived = false;
            knownPacksEmpty = false;
            coreVersion = null;
            tagsSeen = false;
            featuresSeen = false;
            tabListBeforeLogin = false;
            serverLinksBeforeLogin = false;
            resetPlayLocked();
        }
    }

    private static void resetPlayLocked() {
        synchronized (keepAlives) {
            keepAlives.clear();
            keepAliveIds.clear();
        }
        firstKeepAliveAt = 0;
        trackedSpawns.clear();
        loginAt = 0;
        selfTabAdds = 0;
        firstTabAddEntries = -1;
        levelNames.clear();
        firstTabActions.clear();
        firstTeleportId = Integer.MIN_VALUE;
        loadedChunks.clear();
        centerKnown = false;
        prevChunkDistance = -1;
        orderedChunks = 0;
        chunkDescents = 0;
        entitiesBeforeChunk = 0;
        liveEntities.clear();
        equipmentOrphans.clear();
        equipmentSpawns.clear();
        equipmentFirst.clear();
        equipmentOrphanIds.clear();
        equipmentDuplicateIds.clear();
        equipmentDuplicates = 0;
        tabAddPattern.setLength(0);
        pairingWithMaxHealth = 0;
        pairingWithoutMaxHealth = 0;
        pairingWithScale = 0;
        pairingTypes.clear();
        joinWeather = "";
        respawnPending = false;
        respawnRadiusFirst = 0;
        respawnPositionFirst = 0;
        ownDataBeforeBorder = false;
        farthestTracked = 0;
        farthestTrackedType = null;
        trackedMobs = 0;
        chunkBatches.set(0);
        chunks.set(0);
        lastKeepAliveId = Long.MIN_VALUE;
        rootCommands = List.of();
        pluginsArguments = -1;
        MobCadence.reset();
        FreshFreeze.reset();
        pendingSnapshot.clear();
        pendingPlayers.clear();
        pendingTypes.clear();
        pendingResync.clear();
        ascendingKeySets.clear();
        ascendingAttributes.clear();
        zombieHorses.clear();
        rainCrossings.clear();
        rainFlips.clear();
        rainResolvedWith = 0;
        rainResolvedWithout = 0;
        lastRainLevel = -1f;
        playTimeReadings.clear();
        lastTimeStats.clear();
        timeStatsReadings = 0;
        timeStatsOffGrid = false;
        crouchSeen = false;
        timeStatsAbsoluteGrid = true;
        timeStatsNonZero = 0;
        loggedIn = false;
        joinDone = false;
        joinDoneAt = 0;
        playPackets = 0;
        firstAfterLogin = null;
        previousJoinPacket = null;
        clearTitlesAfterLogin = false;
        emptyRegisterInPlay = false;
        leavesChannels = false;
        leavesVersion = null;
        vvChannel = null;
        vvVersion = 0;
        vvName = null;
        ownEntityId = Integer.MIN_VALUE;
        emptyLevels = false;
        maxPlayers = -1;
        serverDataCount = 0;
        serverDataAfterPosition = false;
        serverDataBorders = -1;
        joinIndex = 0;
        borderCount = 0;
        tickingStates = 0;
        tickingSteps = 0;
        viewDistanceSent = false;
        commandsAt = -1;
        recipeBookAt = -1;
        joinMessage = JoinMessage.NONE;
        setTimePackets = 0;
        setTimeSingleClock = 0;
        setTimeFirst = 0;
        setTimeLast = 0;
        respawnAt = 0;
        ownJoinListSeen = false;
        ownJoinLength = 0;
        ownJoinAscending = false;
        pairingLists = 0;
        purpurAttrLists = 0;
        longestAscending = 0;
        unsortedSnapshots = 0;
        unsortedSnapshotsLong = 0;
        resyncHits = 0;
        resyncMisses = 0;
        resyncEarlyHits = 0;
        resyncEarlyMisses = 0;
        speedExact = 0;
        speedFloat = 0;
        zombieHorse15 = 0;
        zombieHorse25 = 0;
    }

    public static void onRegistry(ClientboundRegistryDataPacket packet) {
        String key = packet.registry().identifier().toString();
        registries.add(key);
        String registry = packet.registry().identifier().getPath();
        for (RegistrySynchronization.PackedRegistryEntry entry : packet.entries()) {
            String ns = entry.id().getNamespace();
            if (!"minecraft".equals(ns)) registryNamespaces.add(registry + ":" + ns);
            if ("minecraft:chat_type".equals(key)) chatTypes.add(entry.id().toString());
        }
    }

    public static void onKeepAlive(long id) {
        if (id == lastKeepAliveId) return;
        lastKeepAliveId = id;
        synchronized (keepAlives) {
            long now = System.currentTimeMillis();
            if (firstKeepAliveAt == 0) firstKeepAliveAt = now;
            keepAlives.add(now);
            while (keepAlives.size() > 10) keepAlives.remove(0);
            if (keepAliveIds.size() < 10) keepAliveIds.add(new long[]{id, now});
        }
    }

    public static void onChunkBatch() { chunkBatches.incrementAndGet(); }

    public static void onChunk() { chunks.incrementAndGet(); }

    public static void onCommands(CommandDispatcher<?> dispatcher) {
        List<String> names = new ArrayList<>();
        int plugins = -1;
        for (CommandNode<?> node : dispatcher.getRoot().getChildren()) {
            names.add(node.getName());
            if (node.getName().equals("plugins") || node.getName().equals("bukkit:plugins")) plugins = Math.max(plugins, node.getChildren().size());
        }
        rootCommands = List.copyOf(names);
        pluginsArguments = plugins;
    }

    public static void onRawPayload(Identifier id, byte[] data) {
        try {
            handleRawPayload(id, data);
        } catch (Throwable ignored) {
        }
    }

    private static void handleRawPayload(Identifier id, byte[] data) {
        String channel = id.toString();
        switch (channel) {
            case "minecraft:register" -> {
                synchronized (JOIN) {
                    if (data.length == 0 && loggedIn) emptyRegisterInPlay = true;
                    for (String c : new String(data, StandardCharsets.UTF_8).split("\0")) {
                        if (c.isEmpty()) continue;
                        registerChannels.add(c);
                        if (c.startsWith("bladeren:")) leavesChannels = true;
                    }
                }
            }
            case "bladeren:hello" -> {
                String hello = readUtf(data);
                synchronized (JOIN) {
                    leavesChannels = true;
                    if (hello != null && hello.startsWith("bladeren-leaves-")) leavesVersion = hello.substring("bladeren-leaves-".length());
                }
            }
            case "vv:server_details", "vv:proxy_details", "vv:app_details" -> onViaDetails(channel, data);
            default -> {}
        }
    }

    private static void onViaDetails(String channel, byte[] data) {
        try {
            String text = new String(data, StandardCharsets.UTF_8);
            int start = text.indexOf('{');
            int end = text.lastIndexOf('}');
            if (start < 0 || end <= start) return;
            JsonElement parsed = JsonParser.parseString(text.substring(start, end + 1));
            if (!parsed.isJsonObject()) return;
            JsonObject json = parsed.getAsJsonObject();
            int version = json.has("version") ? json.get("version").getAsInt() : 0;
            String name = json.has("versionName") ? json.get("versionName").getAsString() : null;
            synchronized (JOIN) {
                vvChannel = channel;
                vvVersion = version;
                vvName = name;
            }
        } catch (Throwable ignored) {
        }
    }

    private static String readUtf(byte[] data) {
        int length = 0, shift = 0, i = 0;
        while (i < data.length && i < 5) {
            int b = data[i++];
            length |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                if (length < 0 || length > data.length - i) return null;
                return new String(data, i, length, StandardCharsets.UTF_8);
            }
            shift += 7;
        }
        return null;
    }

    public static void observe(Packet<?> packet) {
        long now = System.currentTimeMillis();
        synchronized (JOIN) {
            if (packet instanceof ClientboundCustomQueryPacket query) {
                loginQueries.add(query.payload().id().toString());
                return;
            }
            if (packet instanceof ClientboundLoginFinishedPacket finished) {
                selfProfile = finished.gameProfile() == null ? null : finished.gameProfile().id();
                UUID session = finished.sessionId();
                zeroSession = session == null || (session.getMostSignificantBits() == 0 && session.getLeastSignificantBits() == 0);
                return;
            }
            if (packet instanceof ClientboundCustomPayloadPacket custom) {
                if (custom.payload() instanceof BrandPayload brand) {
                    if (firstBrand == null) {
                        firstBrand = brand.brand();
                        if (!loggedIn && sawRegistry) brandAfterRegistry = true;
                    }
                    lastBrand = brand.brand();
                    if (!loggedIn && !configFinished) brandInConfig = true;
                }
                if (configFinished && !loggedIn) preLoginPayloads.add(custom.payload().type().id().toString());
            }
            if (!loggedIn && !configFinished) {
                if (packet instanceof ClientboundRegistryDataPacket registry) {
                    sawRegistry = true;
                    if (tagsSeen) lateRegistries.add(registry.registry().identifier().getPath());
                }
                if (packet instanceof ClientboundUpdateTagsPacket tags) {
                    tagsSeen = true;
                    captureTags(tags);
                }
                if (packet instanceof ClientboundUpdateEnabledFeaturesPacket) featuresSeen = true;
                if (packet instanceof ClientboundSelectKnownPacks select) {
                    knownPacksReceived = true;
                    knownPacksEmpty = select.knownPacks().isEmpty();
                    for (KnownPack kp : select.knownPacks()) {
                        knownPacks.add(kp.namespace() + ":" + kp.id());
                        if ("minecraft".equals(kp.namespace()) && "core".equals(kp.id())) coreVersion = kp.version();
                    }
                }
                if (packet instanceof ClientboundPingPacket ping) configPings.add(ping.getId());
            }
            if (packet instanceof ClientboundFinishConfigurationPacket) {
                configFinished = true;
                return;
            }
            if (packet instanceof ClientboundLoginPacket login) {
                if (loggedIn) resetPlayLocked();
                loggedIn = true;
                loginAt = now;
                lastRainLevel = -1f;
                playPackets = 0;
                ownEntityId = login.playerId();
                maxPlayers = login.maxPlayers();
                emptyLevels = login.levels().isEmpty();
                for (net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> level : login.levels()) {
                    if (levelNames.size() < 16) levelNames.add(level.identifier().toString());
                }
                previousJoinPacket = packet.getClass().getSimpleName();
                return;
            }
            if (!loggedIn) {
                if (configFinished && packet instanceof ClientboundTabListPacket) tabListBeforeLogin = true;
                if (configFinished && packet instanceof ClientboundServerLinksPacket) serverLinksBeforeLogin = true;
                return;
            }
            playPackets++;
            if (packet instanceof ClientboundPlayerPositionPacket position && firstTeleportId == Integer.MIN_VALUE) firstTeleportId = position.id();
            if (playPackets == 1) {
                firstAfterLogin = packet instanceof ClientboundCustomPayloadPacket custom
                        ? custom.payload().type().id().toString()
                        : packet.getClass().getSimpleName();
            }
            if (packet instanceof ClientboundClearTitlesPacket && playPackets <= 3) clearTitlesAfterLogin = true;
            if (packet instanceof ClientboundServerDataPacket) {
                if (serverDataCount == 0 && !joinDone) {
                    serverDataAfterPosition = ClientboundPlayerPositionPacket.class.getSimpleName().equals(previousJoinPacket);
                    serverDataBorders = borderCount;
                }
                serverDataCount++;
            }
            if (packet instanceof ClientboundSetTimePacket time) onTime(time);
            if (packet instanceof ClientboundLevelChunkWithLightPacket chunk) {
                onChunkOrder(chunk.getX(), chunk.getZ(), now);
            } else if (packet instanceof ClientboundSetChunkCacheCenterPacket center) {
                centerX = center.getX();
                centerZ = center.getZ();
                centerKnown = true;
                prevChunkDistance = -1;
            } else if (packet instanceof ClientboundSetChunkCacheRadiusPacket) {
                prevChunkDistance = -1;
            } else if (packet instanceof ClientboundForgetLevelChunkPacket forget) {
                loadedChunks.remove(chunkKey(forget.pos().x(), forget.pos().z()));
            } else if (packet instanceof ClientboundRemoveEntitiesPacket remove) {
                for (int gone : remove.getEntityIds()) {
                    liveEntities.remove(gone);
                    equipmentSpawns.remove(gone);
                    equipmentFirst.remove(gone);
                    FreshFreeze.onRemove(gone);
                }
            } else if (packet instanceof ClientboundSetEquipmentPacket equipment) {
                onEquipment(equipment, now);
            } else if (packet instanceof ClientboundRespawnPacket) {
                loadedChunks.clear();
                liveEntities.clear();
                prevChunkDistance = -1;
                FreshFreeze.newWorld();
            }
            if (packet instanceof ClientboundRespawnPacket) {
                respawnAt = now;
                lastRainLevel = -1f;
                respawnPending = true;
            } else if (respawnPending) {
                respawnPending = false;
                if (packet instanceof ClientboundSetChunkCacheRadiusPacket) respawnRadiusFirst++;
                else if (packet instanceof ClientboundPlayerPositionPacket) respawnPositionFirst++;
            }
            if (packet instanceof ClientboundGameEventPacket game) onGameEvent(game, now);
            if (packet instanceof ClientboundAwardStatsPacket award) onStats(award);
            if (packet instanceof ClientboundAddEntityPacket add) onAdd(add, now);
            else if (packet instanceof ClientboundEntityPositionSyncPacket sync) {
                onPositionSync(sync.id(), now);
                MobCadence.onMove(sync.id(), now);
            }
            else if (packet instanceof ClientboundSetPassengersPacket passengers) {
                for (int p : passengers.getPassengers()) pendingResync.remove(p);
            } else if (packet instanceof ClientboundMoveEntityPacket move) {
                int moved = ((MoveEntityPacketAccessor) move).hontun$entityId();
                pendingSnapshot.remove(moved);
                if (move.hasPosition()) FreshFreeze.onMove(moved, move.getXa(), move.getZa(), move.isOnGround(), now);
                if (move.hasPosition() && (move.getXa() != 0 || move.getZa() != 0)) MobCadence.onMove(moved, now);
            } else if (packet instanceof ClientboundRotateHeadPacket head) {
                int turned = ((RotateHeadPacketAccessor) head).hontun$entityId();
                pendingSnapshot.remove(turned);
                MobCadence.onHead(turned, ((RotateHeadPacketAccessor) head).hontun$yHeadRot(), now);
                FreshFreeze.onHead(turned, ((RotateHeadPacketAccessor) head).hontun$yHeadRot(), now);
            } else if (packet instanceof ClientboundSetEntityMotionPacket motion) {
                pendingSnapshot.remove(motion.id());
                if (motion.movement().horizontalDistanceSqr() > 1.0E-7) MobCadence.onMove(motion.id(), now);
            } else if (packet instanceof ClientboundTeleportEntityPacket teleport) {
                pendingSnapshot.remove(teleport.id());
                MobCadence.onMove(teleport.id(), now);
            } else if (packet instanceof ClientboundUpdateAttributesPacket attrs) {
                onSpeed(attrs);
                onAttributes(attrs, now);
            }
            if (joinDone) return;
            previousJoinPacket = packet.getClass().getSimpleName();
            if (packet instanceof ClientboundLevelChunkWithLightPacket) {
                joinDone = true;
                joinDoneAt = now;
                return;
            }
            if (packet instanceof ClientboundPlayerInfoUpdatePacket info && info.actions().contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER)) {
                selfTabAdds++;
                if (selfTabAdds == 1) {
                    firstTabAddEntries = info.entries().size();
                    for (ClientboundPlayerInfoUpdatePacket.Action action : info.actions()) firstTabActions.add(action.name());
                }
                boolean self = false;
                for (ClientboundPlayerInfoUpdatePacket.Entry entry : info.entries()) {
                    if (selfProfile != null && selfProfile.equals(entry.profileId())) self = true;
                }
                if (tabAddPattern.length() < 8) tabAddPattern.append(self ? 'S' : 'o');
            } else if (packet instanceof ClientboundGameEventPacket game && borderCount <= 1 && joinWeather.length() < 8) {
                if (game.getEvent() == ClientboundGameEventPacket.START_RAINING) joinWeather += "+";
                else if (game.getEvent() == ClientboundGameEventPacket.STOP_RAINING) joinWeather += "-";
                else if (game.getEvent() == ClientboundGameEventPacket.RAIN_LEVEL_CHANGE) joinWeather += game.getParam() > 0.2f ? "R" : "r";
                else if (game.getEvent() == ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE) joinWeather += "T";
            } else if (packet instanceof ClientboundSetEntityDataPacket data && data.id() == ownEntityId && borderCount == 0) {
                ownDataBeforeBorder = true;
            }
            if (packet instanceof ClientboundInitializeBorderPacket) {
                borderCount++;
                joinIndex++;
            } else if (packet instanceof ClientboundTickingStatePacket) {
                tickingStates++;
            } else if (packet instanceof ClientboundTickingStepPacket) {
                tickingSteps++;
            } else if (packet instanceof ClientboundSetChunkCacheRadiusPacket || packet instanceof ClientboundSetSimulationDistancePacket) {
                viewDistanceSent = true;
                joinIndex++;
            } else if (packet instanceof ClientboundCommandsPacket) {
                if (commandsAt < 0) commandsAt = joinIndex;
                joinIndex++;
            } else if (packet instanceof ClientboundRecipeBookSettingsPacket) {
                if (recipeBookAt < 0) recipeBookAt = joinIndex;
                joinIndex++;
            } else if (packet instanceof ClientboundSystemChatPacket chat && !chat.overlay() && joinMessage == JoinMessage.NONE) {
                joinMessage = classify(chat.content());
            }
        }
    }

    private static long chunkKey(int x, int z) {
        return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
    }

    private static void onChunkOrder(int x, int z, long now) {
        FreshFreeze.onChunk(x, z, now);
        if (loadedChunks.size() > 20000) loadedChunks.clear();
        if (!loadedChunks.add(chunkKey(x, z))) return;
        if (!centerKnown || orderedChunks >= 200) return;
        orderedChunks++;
        int distance = Math.abs(x - centerX) + Math.abs(z - centerZ);
        if (prevChunkDistance >= 0 && distance < prevChunkDistance) chunkDescents++;
        prevChunkDistance = distance;
    }

    private static String equipmentSignature(ClientboundSetEquipmentPacket packet) {
        StringBuilder sb = new StringBuilder();
        for (Pair<EquipmentSlot, ItemStack> slot : packet.getSlots()) {
            ItemStack stack = slot.getSecond();
            sb.append(slot.getFirst().getName()).append('=').append(BuiltInRegistries.ITEM.getKey(stack.getItem()))
                    .append('x').append(stack.getCount()).append('#').append(ItemStack.hashItemAndComponents(stack)).append(';');
        }
        return sb.toString();
    }

    private static void onEquipment(ClientboundSetEquipmentPacket packet, long now) {
        int id = packet.getEntity();
        if (id == ownEntityId) return;
        if (!liveEntities.contains(id)) {
            if (equipmentOrphans.size() > TRACK_CAP) equipmentOrphans.clear();
            equipmentOrphans.put(id, now);
            return;
        }
        Long spawned = equipmentSpawns.get(id);
        if (spawned == null) return;
        if (now - spawned > 300) {
            equipmentSpawns.remove(id);
            equipmentFirst.remove(id);
            return;
        }
        String signature = equipmentSignature(packet);
        String first = equipmentFirst.get(id);
        if (first == null) {
            equipmentFirst.put(id, signature);
            return;
        }
        equipmentSpawns.remove(id);
        equipmentFirst.remove(id);
        if (first.equals(signature) && !equipmentOrphanIds.contains(id)) {
            equipmentDuplicates++;
            if (equipmentDuplicateIds.size() < 64) equipmentDuplicateIds.add(id);
        }
    }

    private static void onAdd(ClientboundAddEntityPacket add, long now) {
        int id = add.getId();
        if (liveEntities.size() > 8192) liveEntities.clear();
        liveEntities.add(id);
        FreshFreeze.onAdd(id, add.getType(), add.getX(), add.getZ(), add.getYHeadRot(), now);
        if (centerKnown && !loadedChunks.isEmpty()) {
            int cx = (int) Math.floor(add.getX()) >> 4, cz = (int) Math.floor(add.getZ()) >> 4;
            int radius = WorldInfo.chunkRadius;
            if (radius > 0 && Math.max(Math.abs(cx - centerX), Math.abs(cz - centerZ)) < radius && !loadedChunks.contains(chunkKey(cx, cz))) {
                entitiesBeforeChunk++;
            }
        }
        if (add.getType() != EntityTypes.PLAYER && add.getType() != EntityTypes.ARMOR_STAND && livingType(add.getType())) {
            Long orphan = equipmentOrphans.remove(id);
            if (orphan != null && now - orphan <= 100) {
                if (equipmentOrphanIds.size() < 256) equipmentOrphanIds.add(id);
            } else {
                if (equipmentSpawns.size() > TRACK_CAP) equipmentSpawns.clear();
                equipmentSpawns.put(id, now);
            }
        }
        String typeKey = BuiltInRegistries.ENTITY_TYPE.getKey(add.getType()).getPath();
        if (pendingTypes.size() > TRACK_CAP) pendingTypes.clear();
        pendingTypes.put(id, typeKey);
        if ((TRACK_ANIMALS.contains(typeKey) || TRACK_MONSTERS.contains(typeKey)) && trackedSpawns.size() < 512) {
            trackedSpawns.add(new Object[]{typeKey, add.getX(), add.getZ()});
        }
        pendingSnapshot.put(id, now);
        if (add.getType() == EntityTypes.PLAYER) pendingPlayers.add(id);
        if (add.getType() == EntityTypes.ZOMBIE_HORSE && zombieHorses.size() < 64) zombieHorses.add(id);
        if (pendingSnapshot.size() > TRACK_CAP) {
            Integer oldest = pendingSnapshot.keySet().iterator().next();
            pendingSnapshot.remove(oldest);
            pendingPlayers.remove(oldest);
        }
        if (!livingType(add.getType())) return;
        sweepResync(now);
        pendingResync.put(id, new long[]{now, joinDone && now - joinDoneAt <= RESYNC_EARLY_MS ? 1 : 0});
        if (pendingResync.size() > TRACK_CAP) pendingResync.remove(pendingResync.keySet().iterator().next());
    }

    private static boolean livingType(EntityType<?> type) {
        try {
            return DefaultAttributes.hasSupplier(type);
        } catch (Throwable t) {
            return false;
        }
    }

    private static void onPositionSync(int id, long now) {
        pendingSnapshot.remove(id);
        long[] spawn = pendingResync.remove(id);
        if (spawn == null) return;
        boolean hit = now - spawn[0] <= RESYNC_WINDOW_MS;
        countResync(hit, spawn[1] == 1);
    }

    private static void countResync(boolean hit, boolean early) {
        if (hit) resyncHits++;
        else resyncMisses++;
        if (early) {
            if (hit) resyncEarlyHits++;
            else resyncEarlyMisses++;
        }
    }

    private static void sweepResync(long now) {
        Iterator<Map.Entry<Integer, long[]>> it = pendingResync.entrySet().iterator();
        while (it.hasNext()) {
            long[] spawn = it.next().getValue();
            if (now - spawn[0] <= RESYNC_WINDOW_MS) break;
            countResync(false, spawn[1] == 1);
            it.remove();
        }
    }

    private static void captureTags(ClientboundUpdateTagsPacket packet) {
        try {
            for (Map.Entry<net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<?>>, net.minecraft.tags.TagNetworkSerialization.NetworkPayload> entry : packet.getTags().entrySet()) {
                Map<Identifier, it.unimi.dsi.fastutil.ints.IntList> tags = ((cz.honzasik.hontun.mixin.network.software.TagPayloadAccessor) (Object) entry.getValue()).hontun$tags();
                int entries = 0;
                for (it.unimi.dsi.fastutil.ints.IntList list : tags.values()) entries += list.size();
                if (tagSummary.size() < 64) tagSummary.put(entry.getKey().identifier().getPath(), new int[]{tags.size(), entries});
            }
        } catch (Throwable ignored) {
        }
    }

    private static void onGameEvent(ClientboundGameEventPacket game, long now) {
        ClientboundGameEventPacket.Type type = game.getEvent();
        resolveRain(now);
        if (type == ClientboundGameEventPacket.START_RAINING || type == ClientboundGameEventPacket.STOP_RAINING) {
            while (rainFlips.size() >= 64) rainFlips.remove(0);
            rainFlips.add(now);
        } else if (type == ClientboundGameEventPacket.RAIN_LEVEL_CHANGE) {
            float level = game.getParam();
            if (lastRainLevel >= 0f && joinDone && (lastRainLevel <= 0.2f) != (level <= 0.2f) && rainCrossings.size() < 64) {
                rainCrossings.add(now);
            }
            lastRainLevel = level;
        }
    }

    private static void onStats(ClientboundAwardStatsPacket award) {
        Integer playTime = null;
        boolean anyTime = false;
        int nonZero = 0;
        for (Object2IntMap.Entry<Stat<?>> entry : award.stats().object2IntEntrySet()) {
            Stat<?> stat = entry.getKey();
            if (stat.getType() != Stats.CUSTOM || !(stat.getValue() instanceof Identifier id)) continue;
            boolean timeStat = id.equals(Stats.PLAY_TIME) || id.equals(Stats.TOTAL_WORLD_TIME) || id.equals(Stats.TIME_SINCE_DEATH)
                    || id.equals(Stats.TIME_SINCE_REST) || id.equals(Stats.CROUCH_TIME);
            if (!timeStat) continue;
            anyTime = true;
            int value = entry.getIntValue();
            if (id.equals(Stats.PLAY_TIME)) playTime = value;
            if (id.equals(Stats.CROUCH_TIME) && value > 0) crouchSeen = true;
            if (value % 20 != 0) timeStatsAbsoluteGrid = false;
            if (value != 0) nonZero++;
            Integer previous = lastTimeStats.put(id, value);
            if (previous != null && (value - previous) % 20 != 0) timeStatsOffGrid = true;
        }
        if (!anyTime) return;
        timeStatsReadings++;
        timeStatsNonZero = Math.max(timeStatsNonZero, nonZero);
        if (playTime != null && playTimeReadings.size() < 16) playTimeReadings.add(playTime);
    }

    private static void onTime(ClientboundSetTimePacket time) {
        long now = System.currentTimeMillis();
        if (setTimePackets == 0) setTimeFirst = now;
        setTimeLast = now;
        setTimePackets++;
        if (time.clockUpdates().size() == 1) setTimeSingleClock++;
    }

    private static void onSpeed(ClientboundUpdateAttributesPacket packet) {
        for (ClientboundUpdateAttributesPacket.AttributeSnapshot snapshot : packet.getValues()) {
            for (AttributeModifier modifier : snapshot.modifiers()) {
                if (!"effect.speed".equals(modifier.id().getPath())) continue;
                double amount = modifier.amount();
                long level = Math.round(amount / 0.2);
                if (level < 1 || level > 256) continue;
                if (amount == 0.2 * level) speedExact++;
                else if (amount == (double) 0.2f * level) speedFloat++;
            }
        }
    }

    private static void onAttributes(ClientboundUpdateAttributesPacket packet, long now) {
        int id = packet.getEntityId();
        if (zombieHorses.contains(id)) {
            for (ClientboundUpdateAttributesPacket.AttributeSnapshot s : packet.getValues()) {
                if (!"max_health".equals(s.attribute().unwrapKey().map(k -> k.identifier().getPath()).orElse(""))) continue;
                if (s.base() == 15.0) zombieHorse15++;
                else if (s.base() == 25.0) zombieHorse25++;
                zombieHorses.remove(id);
            }
        }
        boolean snapshot;
        boolean player;
        boolean ownJoin = false;
        if (id == ownEntityId) {
            player = true;
            if (respawnAt > 0 && now - respawnAt <= RESPAWN_WINDOW_MS) {
                snapshot = true;
                respawnAt = 0;
            } else if (!ownJoinListSeen) {
                snapshot = false;
                ownJoin = !joinDone;
                ownJoinListSeen = true;
                if (!ownJoin) return;
            } else {
                return;
            }
        } else {
            Long added = pendingSnapshot.remove(id);
            player = pendingPlayers.remove(id);
            if (added == null || now - added > SNAPSHOT_WINDOW_MS) return;
            snapshot = true;
            pairingLists++;
        }

        List<Integer> ids = new ArrayList<>();
        boolean maxHealth = false, scale = false;
        int maxHealthId = -1;
        for (ClientboundUpdateAttributesPacket.AttributeSnapshot s : packet.getValues()) {
            Holder<Attribute> holder = s.attribute();
            String path = holder.unwrapKey().map(k -> k.identifier().getPath()).orElse("");
            int reg = BuiltInRegistries.ATTRIBUTE.getId(holder.value());
            if (path.equals("max_health")) {
                maxHealth = true;
                maxHealthId = reg;
            }
            if (path.equals("scale")) scale = true;
            ids.add(reg);
        }
        if (snapshot && !player && maxHealth && scale) purpurAttrLists++;
        if (snapshot && id != ownEntityId && !player) {
            if (maxHealth) pairingWithMaxHealth++;
            else pairingWithoutMaxHealth++;
            if (scale) pairingWithScale++;
            String typeKey = pendingTypes.remove(id);
            if (typeKey != null && pairingTypes.size() < 32) pairingTypes.add(typeKey);
        }
        if (id == ownEntityId && snapshot && !ids.isEmpty() && ids.get(ids.size() - 1) == maxHealthId) ids.remove(ids.size() - 1);

        int n = ids.size();
        if (n < 2) return;
        boolean ascending = true;
        for (int i = 1; i < n; i++) {
            if (ids.get(i) <= ids.get(i - 1)) {
                ascending = false;
                break;
            }
        }
        if (ownJoin) {
            ownJoinLength = n;
            ownJoinAscending = ascending;
        }
        if (ascending) {
            longestAscending = Math.max(longestAscending, n);
            ascendingKeySets.add(new TreeSet<>(ids).toString());
            ascendingAttributes.addAll(ids);
        } else if (snapshot || (ownJoin && n >= 3)) {
            unsortedSnapshots++;
            if (n >= 3) unsortedSnapshotsLong++;
        }
    }

    private static JoinMessage classify(Component content) {
        Set<String> keys = new TreeSet<>();
        keys(content, keys, 0);
        if (keys.contains("multiplayer.player.joined")) return JoinMessage.TRANSLATED;
        if (keys.isEmpty() && plainOnly(content, 0) && content.getString().endsWith(" joined the game")) return JoinMessage.LITERAL;
        return JoinMessage.CUSTOM;
    }

    private static void keys(Component c, Set<String> out, int depth) {
        if (c == null || depth > 8) return;
        if (c.getContents() instanceof TranslatableContents t) {
            out.add(t.getKey());
            for (Object a : t.getArgs()) if (a instanceof Component ac) keys(ac, out, depth + 1);
        }
        for (Component s : c.getSiblings()) keys(s, out, depth + 1);
    }

    private static boolean plainOnly(Component c, int depth) {
        if (depth > 8) return false;
        if (!(c.getContents() instanceof PlainTextContents)) return false;
        for (Component s : c.getSiblings()) if (!plainOnly(s, depth + 1)) return false;
        return true;
    }

    public static void tick() {
        net.minecraft.client.player.LocalPlayer me = MCUtil.MC.player;
        if (me == null || me.isPassenger()) return;
        synchronized (JOIN) {
            while (!trackedSpawns.isEmpty()) {
                Object[] spawn = trackedSpawns.poll();
                double dx = (double) spawn[1] - me.getX(), dz = (double) spawn[2] - me.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);
                trackedMobs++;
                if (distance > farthestTracked) {
                    farthestTracked = distance;
                    farthestTrackedType = (String) spawn[0];
                }
            }
        }
    }

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

    private static String keepAliveIdPattern() {
        List<long[]> t;
        synchronized (keepAlives) { t = new ArrayList<>(keepAliveIds); }
        if (t.size() < 3) return null;
        long[] first = t.get(0), last = t.get(t.size() - 1);
        long span = last[1] - first[1];
        if (span < 2000) return null;
        boolean increasing = true;
        for (int i = 1; i < t.size(); i++) if (t.get(i)[0] <= t.get(i - 1)[0]) increasing = false;
        double rate = (double) (last[0] - first[0]) / span;
        if (increasing && rate >= 0.5 && rate <= 2.0) return "clock";
        if (increasing && rate >= 0.5e6 && rate <= 2.0e6) return "nanos";
        return "random";
    }

    public static int keepAliveCount() {
        synchronized (keepAlives) { return keepAlives.size(); }
    }

    public static int nativeProtocol() {
        return SharedConstants.getCurrentVersion().protocolVersion();
    }

    public static int serverProtocol() {
        if (!VfpBridge.available()) return nativeProtocol();
        int target = VfpBridge.idOf(VfpBridge.current());
        if (target > 0) return target;
        ServerData data = MCUtil.MC.getCurrentServer();
        if (data != null && data.state() == ServerData.State.SUCCESSFUL && data.protocol > 0) return data.protocol;
        return nativeProtocol();
    }

    public static int backendProtocol() {
        int vv;
        String core;
        boolean packsReceived, packsEmpty, features, brandConfigured;
        String firstLate;
        synchronized (JOIN) {
            vv = vvVersion;
            core = coreVersion;
            packsReceived = knownPacksReceived;
            packsEmpty = knownPacksEmpty;
            features = featuresSeen;
            brandConfigured = brandInConfig;
            firstLate = lateRegistries.isEmpty() ? null : lateRegistries.get(0);
        }
        if (vv > 0) return vv;
        if (core != null) {
            int p = protocolOf(core);
            if (p > 0) return p;
        }
        if (packsReceived && packsEmpty) {
            int client = serverProtocol();
            if (client <= PROTO_1_20_3) return client;
            return brandConfigured ? PROTO_1_20_3 : PROTO_1_20_1;
        }
        if (firstLate != null) {
            Integer p = LATE_REGISTRY_PROTOCOL.get(firstLate);
            if (p != null) return p;
        }
        return serverProtocol();
    }

    private static int protocolOf(String version) {
        String v = version.endsWith("_unobfuscated") ? version.substring(0, version.length() - "_unobfuscated".length()) : version;
        if (v.equals(SharedConstants.getCurrentVersion().name())) return nativeProtocol();
        if (v.startsWith("26.1")) return PROTO_26_1;
        return switch (v) {
            case "26.2" -> 776;
            case "1.21.11" -> PROTO_1_21_11;
            case "1.21.9", "1.21.10" -> PROTO_1_21_9;
            case "1.21.7", "1.21.8" -> 772;
            case "1.21.6" -> 771;
            case "1.21.5" -> 770;
            case "1.21.4" -> 769;
            case "1.21.2", "1.21.3" -> 768;
            case "1.21", "1.21.1" -> PROTO_1_21;
            case "1.20.5", "1.20.6" -> 766;
            case "1.20.3", "1.20.4" -> PROTO_1_20_3;
            case "1.20.2" -> PROTO_1_20_2;
            default -> 0;
        };
    }

    public static boolean translated() {
        return backendProtocol() != nativeProtocol();
    }

    private static String strip(String s) {
        return s == null ? null : s.replaceAll("§.", "").trim();
    }

    private static String appendProxy(String proxy, String add) {
        if (proxy == null) return add;
        if (proxy.equals(add) || proxy.endsWith(" + " + add)) return proxy;
        return proxy + " + " + add;
    }

    public static Result detect() {
        ClientPacketListener conn = MCUtil.getPlayNetHandler();
        List<Evidence> ev = new ArrayList<>();
        List<String> notes = new ArrayList<>();
        int clientSide = serverProtocol();
        int proto = backendProtocol();
        boolean translated = proto != nativeProtocol();
        boolean configPhase = proto >= PROTO_1_20_2;

        String brandFirst, brandLast;
        boolean brandConfig;
        Set<String> queries, packs, preLogin, channels;
        List<Integer> pings;
        List<String> late;
        boolean brandLate, linksEarly, tabEarly, titlesEarly, emptyRegister, done, leavesProto, packsReceived, packsEmpty,
                features, configDone, noLevels;
        Boolean zeroSess;
        String firstPlay, core, vvCh, vvNm, leavesVer;
        int serverData, maxP, border, states, steps, cmdAt, rbAt, timePackets, timeSingle, pairing, purpurAttrs,
                exactSpeed, floatSpeed, vvVer, longestAsc, ascSets, ascAttrs, unsorted, unsortedLong, sdBorders, zh15, zh25,
                rainWithFlip, rainWithoutFlip, statReadings,
                hits, misses, earlyHits, earlyMisses;
        boolean viewDist, sdAfterPos, statsOffGrid, statsCrouch, playTimeMoved, statsAbsoluteGrid, ownListAscending;
        int ownListLength;
        long joinedAt;
        int tabAdds, firstTabEntries, mhWith, mhWithout, mhScale, mhTypes, respawnRadius, respawnPosition;
        String tabPattern, weatherAtJoin;
        Map<String, int[]> tagInfo;
        List<String> levels;
        Set<String> tabActions;
        int teleportId, descents, fresh, earlyEntities, equipOrphans, equipDups, equipDupIds;
        boolean ownDataEarly;
        double farthest;
        String farthestType;
        int trackedCount;
        int statsNonZero;
        JoinMessage jm;
        long timeSpan, doneAt;
        synchronized (JOIN) {
            sweepResync(System.currentTimeMillis());
            brandFirst = firstBrand;
            brandLast = lastBrand;
            brandConfig = brandInConfig;
            queries = new LinkedHashSet<>(loginQueries);
            packs = new LinkedHashSet<>(knownPacks);
            preLogin = new LinkedHashSet<>(preLoginPayloads);
            channels = new LinkedHashSet<>(registerChannels);
            pings = new ArrayList<>(configPings);
            late = new ArrayList<>(lateRegistries);
            brandLate = brandAfterRegistry;
            linksEarly = serverLinksBeforeLogin;
            tabEarly = tabListBeforeLogin;
            titlesEarly = clearTitlesAfterLogin;
            emptyRegister = emptyRegisterInPlay;
            leavesProto = leavesChannels;
            leavesVer = leavesVersion;
            packsReceived = knownPacksReceived;
            packsEmpty = knownPacksEmpty;
            features = featuresSeen;
            configDone = configFinished;
            noLevels = emptyLevels;
            zeroSess = zeroSession;
            core = coreVersion;
            vvCh = vvChannel;
            vvNm = vvName;
            vvVer = vvVersion;
            done = joinDone;
            doneAt = joinDoneAt;
            firstPlay = firstAfterLogin;
            serverData = serverDataCount;
            sdAfterPos = serverDataAfterPosition;
            sdBorders = serverDataBorders;
            maxP = maxPlayers;
            border = borderCount;
            states = tickingStates;
            steps = tickingSteps;
            cmdAt = commandsAt;
            rbAt = recipeBookAt;
            viewDist = viewDistanceSent;
            jm = joinMessage;
            timePackets = setTimePackets;
            timeSingle = setTimeSingleClock;
            timeSpan = setTimeLast - setTimeFirst;
            pairing = pairingLists;
            purpurAttrs = purpurAttrLists;
            longestAsc = longestAscending;
            ascSets = ascendingKeySets.size();
            ascAttrs = ascendingAttributes.size();
            ownListLength = ownJoinLength;
            ownListAscending = ownJoinAscending;
            zh15 = zombieHorse15;
            zh25 = zombieHorse25;
            int[] rain = rainOutcome(System.currentTimeMillis());
            rainWithFlip = rain[0];
            rainWithoutFlip = rain[1];
            statReadings = timeStatsReadings;
            statsOffGrid = timeStatsOffGrid;
            statsCrouch = crouchSeen;
            statsAbsoluteGrid = timeStatsAbsoluteGrid;
            statsNonZero = timeStatsNonZero;
            joinedAt = loginAt;
            tabAdds = selfTabAdds;
            firstTabEntries = firstTabAddEntries;
            tabPattern = tabAddPattern.toString();
            mhWith = pairingWithMaxHealth;
            mhWithout = pairingWithoutMaxHealth;
            mhScale = pairingWithScale;
            mhTypes = pairingTypes.size();
            weatherAtJoin = joinWeather;
            respawnRadius = respawnRadiusFirst;
            respawnPosition = respawnPositionFirst;
            tagInfo = new LinkedHashMap<>(tagSummary);
            levels = new ArrayList<>(levelNames);
            tabActions = new TreeSet<>(firstTabActions);
            teleportId = firstTeleportId;
            descents = chunkDescents;
            fresh = orderedChunks;
            earlyEntities = entitiesBeforeChunk;
            equipOrphans = equipmentOrphanIds.size();
            equipDups = equipmentDuplicates;
            equipDupIds = equipmentDuplicateIds.size();
            ownDataEarly = ownDataBeforeBorder;
            farthest = farthestTracked;
            farthestType = farthestTrackedType;
            trackedCount = trackedMobs;
            playTimeMoved = playTimeReadings.size() >= 2 && !playTimeReadings.get(0).equals(playTimeReadings.get(playTimeReadings.size() - 1));
            unsorted = unsortedSnapshots;
            unsortedLong = unsortedSnapshotsLong;
            hits = resyncHits;
            misses = resyncMisses;
            earlyHits = resyncEarlyHits;
            earlyMisses = resyncEarlyMisses;
            exactSpeed = speedExact;
            floatSpeed = speedFloat;
        }

        String rawBrand = brandFirst == null ? (conn == null ? null : conn.serverBrand()) : brandFirst;
        String brand = strip(rawBrand);
        String backendBrand = brand;
        String proxy = null;
        String arclightLoader = null;
        if (brand != null) {
            for (int pass = 0; pass < 6; pass++) {
                Matcher via = VIAPROXY_BRAND.matcher(backendBrand);
                if (via.matches()) {
                    proxy = appendProxy(proxy, "ViaProxy");
                    backendBrand = via.group(2).trim();
                    ev.add(new Evidence("brand", "\"" + brand + "\" is ViaProxy's rewrite (backend " + via.group(3) + ")", Family.UNKNOWN, 0));
                } else if (backendBrand.endsWith(GATE_SUFFIX)) {
                    proxy = appendProxy(proxy, "Gate");
                    backendBrand = backendBrand.substring(0, backendBrand.length() - GATE_SUFFIX.length()).trim();
                    ev.add(new Evidence("brand", "\"" + brand + "\" is Gate's rewrite of the backend brand", Family.UNKNOWN, 0));
                } else if (backendBrand.endsWith(" (Velocity-CTD)")) {
                    proxy = appendProxy(proxy, "Velocity-CTD");
                    backendBrand = backendBrand.substring(0, backendBrand.length() - " (Velocity-CTD)".length()).trim();
                    ev.add(new Evidence("brand", "\"" + brand + "\" is Velocity-CTD's rewrite of the backend brand", Family.UNKNOWN, 0));
                } else if (backendBrand.endsWith(" (Velocity)")) {
                    proxy = appendProxy(proxy, "Velocity");
                    backendBrand = backendBrand.substring(0, backendBrand.length() - " (Velocity)".length()).trim();
                    ev.add(new Evidence("brand", "\"" + brand + "\" is Velocity's rewrite of the backend brand", Family.UNKNOWN, 0));
                } else if (backendBrand.contains(" <- ")) {
                    String front = backendBrand.substring(0, backendBrand.indexOf(" <- ")).trim();
                    proxy = appendProxy(proxy, front.split("\\s+")[0]);
                    backendBrand = backendBrand.substring(backendBrand.indexOf(" <- ") + 4).trim();
                    ev.add(new Evidence("brand", "\"" + brand + "\" is a BungeeCord-style proxy chain", Family.UNKNOWN, 0));
                } else {
                    break;
                }
            }
            int arclight = backendBrand.indexOf(" arclight/");
            if (arclight >= 0) arclightLoader = backendBrand.substring(0, arclight).trim();
        }
        boolean sectionSuffix = rawBrand != null && rawBrand.endsWith("§r")
                && rawBrand.indexOf('§') == rawBrand.length() - 2;

        ServerData data = MCUtil.MC.getCurrentServer();
        String pingName = data != null && data.state() == ServerData.State.SUCCESSFUL && data.version != null ? data.version.getString() : null;
        if (pingName != null) {
            String lower = pingName.toLowerCase(Locale.ROOT);
            if (lower.startsWith("velocity-ctd")) proxy = proxy == null ? "Velocity-CTD" : proxy;
            else if (lower.startsWith("velocity")) proxy = proxy == null ? "Velocity" : proxy;
            else if (lower.startsWith("bungeecord")) proxy = proxy == null ? "BungeeCord" : proxy;
            else if (lower.startsWith("waterfall")) proxy = proxy == null ? "Waterfall" : proxy;
            else if (lower.startsWith("gate ")) proxy = proxy == null ? "Gate" : proxy;
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
        if (roots.contains("bungee") || roots.contains("glist")) {
            proxy = proxy == null ? "BungeeCord" : proxy;
            ev.add(new Evidence("commands", "bungee/glist commands come from the BungeeCord proxy", Family.UNKNOWN, 0));
        }

        boolean purpurTime = false, canvas = false, divine = false, leaves = false;
        boolean universeSpeed = false;
        boolean hashMap = unsortedLong >= 1 || unsorted >= 2;
        boolean arrayMap = unsorted == 0 && ascAttrs >= 5 && ascSets >= 2;
        boolean ownListArray = unsorted == 0 && ownListAscending && ownListLength >= 4;

        if (!queries.isEmpty()) {
            boolean sponge = queries.stream().anyMatch(q -> q.startsWith("sponge:"));
            ev.add(new Evidence("login", "login plugin requests " + queries + (sponge ? " (Sponge)" : ""),
                    sponge ? Family.SPONGE : Family.UNKNOWN, sponge ? 5 : 0));
        }

        if (clientSide != nativeProtocol()) {
            notes.add("Read through ViaFabricPlus (client speaks protocol " + clientSide + "), version-specific signals are matched to the server's version.");
        }
        String backendName = vvNm != null ? vvNm : core != null ? core.replace("_unobfuscated", "") : null;
        if (proto != clientSide) {
            String who = "vv:proxy_details".equals(vvCh) ? "ViaVersion on the proxy"
                    : "vv:app_details".equals(vvCh) ? "ViaProxy" : "ViaVersion on the server";
            if ("vv:app_details".equals(vvCh)) proxy = appendProxy(proxy, "ViaProxy");
            String version = backendName != null ? backendName + " (protocol " + proto + ")" : "protocol " + proto;
            ev.add(new Evidence("translation", who + " translates, the real backend is " + version, Family.UNKNOWN, 0));
            notes.add("The server translates versions itself (" + who + "), so version-specific signals use its real version " + version + ".");
        } else if (vvCh != null) {
            ev.add(new Evidence("translation", "ViaVersion is installed (" + vvCh + "), backend protocol " + vvVer, Family.UNKNOWN, 0));
        }
        if (packsReceived && packsEmpty) {
            ev.add(new Evidence("config", "no known packs offered (backend 1.20.4 or older, translated)", Family.UNKNOWN, 0));
        }
        if (!late.isEmpty() && clientSide == nativeProtocol()) {
            ev.add(new Evidence("config", "registries sent after the tags " + late.subList(0, Math.min(3, late.size())) + " (ViaVersion rebuilds the configuration)", Family.UNKNOWN, 0));
        }
        if (proto == PROTO_1_21_11 && core != null && core.endsWith("_unobfuscated")) {
            ev.add(new Evidence("config", "core pack is " + core + " (Spigot/CraftBukkit BuildTools jar, not Paper)", Family.BUKKIT, 1, true));
        }

        if (!registries.isEmpty()) {
            if (chatTypes.contains("paper:raw")) {
                ev.add(new Evidence("registry", "chat_type contains paper:raw", Family.PAPER, 5));
            } else {
                ev.add(new Evidence("registry", "chat_type has no paper:raw", Family.UNKNOWN, 0));
            }
            if (registryNamespaces.stream().anyMatch(n -> n.endsWith(":sponge"))) {
                ev.add(new Evidence("registry", "registry entries in the sponge namespace", Family.SPONGE, 3));
            }
            if (registryNamespaces.stream().anyMatch(n -> n.endsWith(":neoforge"))) {
                ev.add(new Evidence("registry", "registry entries in the neoforge namespace", Family.NEOFORGE, 2));
            }
        }

        boolean neoPreamble = configPhase && pings.contains(0) && (preLogin.contains("neoforge:network") || channels.contains("neoforge:register"));
        if (neoPreamble) {
            ev.add(new Evidence("config", "NeoForge network negotiation (ping 0 + neoforge channels)", Family.NEOFORGE, 5));
        }
        String neoPack = packs.stream().filter(p -> p.startsWith("neoforge:")).findFirst().orElse(null);
        if (neoPack != null) {
            ev.add(new Evidence("config", "known pack " + neoPack, Family.NEOFORGE, 4));
        }
        boolean fabricPing = configPhase && pings.contains(FABRIC_API_PING);
        if (fabricPing) {
            ev.add(new Evidence("config", "Fabric API configuration ping", Family.FABRIC, 3));
        }
        long fabricMods = packs.stream().filter(p -> p.startsWith("vanilla:")).count();
        if (fabricMods > 0) {
            notes.add("Server data packs from " + fabricMods + " Fabric mod(s): " + packs.stream().filter(p -> p.startsWith("vanilla:")).limit(8).toList());
        }
        if (configPhase && brandLate) {
            ev.add(new Evidence("config", "brand sent after the registries (Forge order)", Family.FORGE, 5));
        }
        if (channels.stream().anyMatch(c -> c.startsWith("sponge:"))) {
            ev.add(new Evidence("channels", "sponge channels registered", Family.SPONGE, 2));
        }

        if (tabEarly || titlesEarly) {
            proxy = proxy == null ? "Velocity" : proxy;
            ev.add(new Evidence("join", (tabEarly ? "tab list sent before the login packet" : "titles cleared right after login")
                    + " (Velocity)", Family.UNKNOWN, 0));
        }
        if (linksEarly) {
            proxy = "Velocity".equals(proxy) || proxy == null ? "Velocity-CTD" : proxy;
            ev.add(new Evidence("join", "server links sent before the login packet (Velocity-CTD)", Family.UNKNOWN, 0));
        }
        boolean limbo = configDone && !features && !(packsReceived && packsEmpty);
        if ("minecraft:register".equals(firstPlay) && channels.contains("bungeecord:main")) {
            proxy = proxy == null ? "BungeeCord" : proxy;
            ev.add(new Evidence("join", "bungeecord:main registered right after login (BungeeCord)", Family.UNKNOWN, 0));
        }
        boolean bungeeHint = proxy != null && (proxy.contains("BungeeCord") || proxy.contains("Waterfall"));
        if (bungeeHint && configPhase && done && !limbo && !ConnectTracker.transfer() && serverData == 0 && System.currentTimeMillis() - doneAt > 2000) {
            ev.add(new Evidence("join", "no server data packet at all (BungeeCord swallows it)", Family.UNKNOWN, 0));
        }
        if (brandFirst != null && brandLast != null && !brandFirst.equals(brandLast)) {
            ev.add(new Evidence("brand", "brand changed after login to \"" + strip(brandLast) + "\" (proxy)", Family.UNKNOWN, 0));
        }

        if (!configPhase) {
            notes.add("Join order is not compared, the server is older than 1.20.2.");
        } else if (border >= 2 && viewDist) {
            ev.add(new Evidence("join", "world info sent twice with view distance packets before the first chunk (Paper join order)", Family.PAPER, 4));
        } else if (done && border == 1 && !viewDist && cmdAt >= 0 && rbAt >= 0 && cmdAt < rbAt) {
            ev.add(new Evidence("join", "commands sent right after recipes, world info once (vanilla/Spigot join order)", Family.VANILLA, 2, true));
        } else if (!done) {
            notes.add("The join sequence was not captured, rejoin to get it.");
        }
        int selfIn = tabPattern.replace("o", "").length();
        if (done && ownDataEarly) {
            ev.add(new Evidence("join", "your entity data came before the world info (CraftBukkit/Spigot)", Family.BUKKIT, 3, true));
        }
        if (done && !tabPattern.isEmpty()) {
            if (selfIn >= 2) {
                ev.add(new Evidence("join", "you were added to the tab list twice at join (CraftBukkit/Spigot)", Family.BUKKIT, ownDataEarly ? 0 : 2, true));
            } else if (selfIn == 1 && tabPattern.charAt(0) == 'o') {
                ev.add(new Evidence("join", "the tab list was sent before your own entry (vanilla/modded order)", Family.VANILLA, 2, true));
            } else if (selfIn == 1 && tabPattern.charAt(0) == 'S') {
                ev.add(new Evidence("join", "your tab-list entry came in the first tab update (Paper order)", Family.PAPER, 3));
            }
        }
        if (mhWithout >= 2) {
            ev.add(new Evidence("entities", mhWithout + " mobs arrived without a max_health attribute (not CraftBukkit/Spigot, not Purpur)", Family.UNKNOWN, 0));
        } else if (mhWith >= 5 && mhTypes >= 3 && mhWithout == 0 && mhScale == 0) {
            ev.add(new Evidence("entities", "every mob arrived with max_health and none with scale (" + mhWith + " mobs, CraftBukkit/Spigot)", Family.BUKKIT, 2, true));
        }
        if (weatherAtJoin.contains("-R")) {
            ev.add(new Evidence("weather", "raining at join, announced with the inverted STOP_RAINING event (CraftBukkit lineage)", Family.UNKNOWN, 0));
        } else if (weatherAtJoin.contains("+R")) {
            ev.add(new Evidence("weather", "raining at join, announced with START_RAINING (vanilla/modded)", Family.VANILLA, 1, true));
        }
        if (respawnRadius > 0) {
            ev.add(new Evidence("respawn", "view distance re-sent right after respawning (Spigot)", Family.BUKKIT, 1, true));
        }
        if (proto >= PROTO_1_20_3 && done && states >= 1 && steps == 0) {
            canvas = true;
            ev.add(new Evidence("join", "ticking state sent without ticking step (Canvas)", Family.PAPER, 0));
        }
        boolean proxyTells = tabEarly || titlesEarly || linksEarly || proxy != null;
        if (proto >= PROTO_1_21 && done && !proxyTells && serverData > 0 && !sdAfterPos && sdBorders == 1 && border >= 2) {
            divine = true;
            ev.add(new Evidence("join", "server data sent after the first world-info block (DivineMC sends it async)", Family.PAPER, 0));
        }

        if (done) {
            switch (jm) {
                case LITERAL -> ev.add(new Evidence("join-msg", "join message is plain text (Spigot/CraftBukkit)", Family.BUKKIT, 2, true));
                case TRANSLATED -> ev.add(new Evidence("join-msg", "join message is translatable (Paper/Sponge)", Family.PAPER, 1));
                case NONE -> ev.add(new Evidence("join-msg", "no join message sent to you (vanilla)", Family.VANILLA, 1));
                default -> ev.add(new Evidence("join-msg", "join message is customized by a plugin", Family.UNKNOWN, 0));
            }
        }

        double seconds = timeSpan / 1000.0;
        double rate = seconds >= 1 ? timePackets / seconds : 0;
        if (timeSingle >= PURPUR_MIN_PACKETS && rate >= PURPUR_MIN_RATE) {
            purpurTime = true;
            ev.add(new Evidence("time", String.format(Locale.ROOT, "time sent per clock every tick, %.0f/s (Purpur)", rate), Family.PAPER, 3));
        }

        String brandLower = backendBrand == null ? "" : backendBrand.toLowerCase(Locale.ROOT);
        if (leavesProto) {
            leaves = true;
            ev.add(new Evidence("channels", "bladeren protocol" + (leavesVer != null ? " (" + leavesVer + ")" : "") + " (Leaves)", Family.PAPER, 3));
        } else if (emptyRegister && brandLower.equals("leaves")) {
            leaves = true;
            ev.add(new Evidence("channels", "empty minecraft:register with a Leaves brand (Leaves with bladeren off)", Family.PAPER, 0));
        } else if (emptyRegister) {
            divine = true;
            ev.add(new Evidence("channels", "empty minecraft:register sent after login (DivineMC)", Family.PAPER, 0));
        }

        boolean zombiePurpur = proto >= PROTO_1_21_11 && zh15 > 0 && zh25 == 0;
        boolean zombieVanilla = proto >= PROTO_1_21_11 && zh25 > 0 && zh15 == 0;
        if (zombiePurpur) {
            ev.add(new Evidence("entities", "zombie horse spawned with 15 HP (Purpur default, vanilla has 25 since 1.21.11)", Family.PAPER, 1));
        } else if (zombieVanilla) {
            ev.add(new Evidence("entities", "zombie horse spawned with vanilla's 25 HP (not Purpur-family)", Family.UNKNOWN, 0));
        }
        boolean statsStep20 = !statsOffGrid && statsAbsoluteGrid && statsNonZero >= 3 && ((statReadings >= 2 && playTimeMoved) || statsCrouch);
        if (statsStep20) {
            ev.add(new Evidence("stats", "time statistics only move in steps of 20 ticks (UniverseSpigot, or Gale)", Family.PAPER, 0));
        } else if (statsOffGrid && statReadings >= 2 && playTimeMoved) {
            ev.add(new Evidence("stats", "time statistics count every tick (not UniverseSpigot)", Family.UNKNOWN, 0));
        }
        boolean universeRain = rainWithoutFlip > 0 && rainWithFlip == 0;
        if (universeRain) {
            ev.add(new Evidence("weather", "rain started or stopped without the start/stop event (UniverseSpigot caches the rain state)", Family.PAPER, 0));
        } else if (rainWithFlip > 0) {
            ev.add(new Evidence("weather", "rain changes come with the start/stop event (not UniverseSpigot)", Family.UNKNOWN, 0));
        }
        boolean purpurFamily = purpurAttrs >= 2 || purpurTime || zombiePurpur;
        if (purpurAttrs >= 2) {
            ev.add(new Evidence("entities", "mobs spawn with max_health and scale attributes (Purpur patches)", Family.PAPER, 1));
        }
        if (arrayMap) {
            ev.add(new Evidence("entities", ascSets + " attribute lists over " + ascAttrs + " attributes all in registry order - array attribute map (Leaf/UniverseSpigot)", Family.PAPER, 0));
        }
        if (hashMap) {
            ev.add(new Evidence("entities", unsorted + " attribute list(s) out of registry order - hash attribute map (not Leaf/UniverseSpigot)", Family.UNKNOWN, 0));
        } else if (ownListArray && !arrayMap) {
            ev.add(new Evidence("join", "your own " + ownListLength + " attributes arrived in registry order (array attribute map, UniverseSpigot/Leaf)", Family.PAPER, 0));
        }
        if (!arrayMap && !hashMap && pairing == 0 && done) {
            notes.add("No mobs spawned in view yet, so the entity-based fork signals are still empty.");
        }
        if (exactSpeed > 0 && floatSpeed == 0) {
            universeSpeed = true;
            ev.add(new Evidence("entities", "Speed effect modifier is an exact 0.2 per level (UniverseSpigot)", Family.PAPER, 0));
        } else if (floatSpeed > 0) {
            ev.add(new Evidence("entities", "Speed effect modifier uses vanilla's float 0.2 (not UniverseSpigot)", Family.UNKNOWN, 0));
        }

        boolean beyondSpigot = farthest > SPIGOT_TRACK_LIMIT;
        boolean beyondPaper = farthest > PAPER_TRACK_LIMIT;
        boolean spigotRange = !beyondSpigot && trackedCount >= 25 && farthest > 0 && farthest <= 52;
        if (beyondSpigot) {
            ev.add(new Evidence("entities", "a " + farthestType + " was sent " + Math.round(farthest) + " blocks away (beyond Spigot's default 48-block entity tracking"
                    + (beyondPaper ? " and Paper's 96" : "") + ")", Family.UNKNOWN, 0));
        } else if (spigotRange) {
            ev.add(new Evidence("entities", "no mob sent beyond " + Math.round(farthest) + " blocks among " + trackedCount + " (Spigot's default 48-block entity tracking)", Family.UNKNOWN, 0));
        }

        boolean batched = chunkBatches.get() > 0;
        boolean universeChunks = !batched && (descents >= 2 || earlyEntities >= 2);
        if (universeChunks) {
            ev.add(new Evidence("chunks", (descents >= 2 ? descents + " chunks arrived out of distance order" : earlyEntities + " entities arrived before their chunk")
                    + " (UniverseSpigot sends chunks from parallel threads)", Family.PAPER, 0));
        } else if (!batched && fresh >= 100 && descents == 0) {
            ev.add(new Evidence("chunks", fresh + " chunks arrived strictly by distance (no UniverseSpigot async chunk sending)", Family.UNKNOWN, 0));
        }
        boolean universeEquipment = !batched && equipDups >= 3 && equipDupIds >= 2 && equipOrphans == 0;
        if (universeEquipment) {
            ev.add(new Evidence("entities", equipDups + " spawning mobs got their equipment sent twice and never before they appeared (UniverseSpigot async entity tracker)", Family.PAPER, 0));
        } else if (equipOrphans >= 2) {
            ev.add(new Evidence("entities", "mob equipment arrived just before the mob itself " + equipOrphans + " times (Bukkit's synchronous tracker, not UniverseSpigot's async one)", Family.UNKNOWN, 0));
        }
        boolean universeTab = tabActions.contains("ADD_PLAYER") && tabActions.contains("INITIALIZE_CHAT") && !tabActions.contains("UPDATE_LISTED");
        if (universeTab) {
            ev.add(new Evidence("join", "you were added to the tab list without the listed flag (UniverseSpigot removePlayersFromTabList)", Family.PAPER, 1));
        }
        FreshFreeze.Stats frozen = FreshFreeze.stats();
        boolean universeFreeze = frozen.universe();
        if (universeFreeze) {
            ev.add(new Evidence("entities", frozen.band() + " fresh mobs past " + Math.round(Math.min(frozen.reach(), 64.0)) + " blocks stood still (" + frozen.bandMoved() + " moved, none with an odd id) while "
                    + frozen.controlMoved() + "/" + frozen.control() + " closer ones moved (UniverseSpigot AI gate)", Family.PAPER, 1));
        } else if (frozen.normal()) {
            ev.add(new Evidence("entities", "fresh mobs past 64 blocks move normally, odd ids too (not UniverseSpigot)", Family.UNKNOWN, 0));
        }

        MobCadence.Stats cadence = MobCadence.stats();
        boolean dabOn = cadence.slowRuns() >= 2 && cadence.slowRuns() >= cadence.fastRuns();
        boolean dabOff = cadence.fastRuns() >= 3 && cadence.fastMobs() >= 2 && cadence.slowRuns() == 0 && cadence.observedMs() >= 60000;
        if (dabOn) {
            ev.add(new Evidence("entities", "distant mobs turn their heads 10° per update instead of 30° (" + cadence.slowRuns()
                    + " slow turns, Dynamic Activation of Brain, on by default in Pufferfish)", Family.PAPER, 1));
        } else if (dabOff) {
            ev.add(new Evidence("entities", "distant mobs turn their heads at full speed (no Dynamic Activation of Brain)", Family.UNKNOWN, 0));
        }

        if (proto >= PROTO_1_21_9) {
            int n = hits + misses;
            int earlyN = earlyHits + earlyMisses;
            if (n >= 5 && hits * 5 >= n * 3) {
                ev.add(new Evidence("entities", hits + "/" + n + " mobs got a position resync right after spawning (Paper entity tracker)", Family.PAPER, 3));
            } else if (earlyN >= 8 && earlyHits * 10 <= earlyN) {
                ev.add(new Evidence("entities", "no position resync after " + earlyN + " mob spawns (not Paper's entity tracker)", Family.UNKNOWN, 0, true));
            }
        }

        int c = chunks.get(), b = chunkBatches.get();
        if (c >= 8 && configPhase && !limbo) {
            if (b == 0) ev.add(new Evidence("chunks", c + " chunks, no chunk batches", Family.PAPER, 3));
            else ev.add(new Evidence("chunks", c + " chunks in " + b + " batches (vanilla-style)", Family.UNKNOWN, 0, true));
        }

        long ka = keepAliveIntervalMs();
        long firstKa = firstKeepAliveAt;
        long sinceJoin = joinedAt > 0 ? System.currentTimeMillis() - joinedAt : -1;
        if ((ka > 0 || firstKa > 0) && proto < 340) {
            notes.add("Keep-alive timing is not compared, the server is older than 1.12.2.");
        } else if (ka <= 0 && firstKa > 0 && joinedAt > 0) {
            long first = firstKa - joinedAt;
            if (first <= 4000) ev.add(new Evidence("keep-alive", "first keep-alive " + Math.max(first, 0) + " ms after joining (Paper sends one every second)", Family.PAPER, 3));
            else if (first >= 13000 && first <= 18000) ev.add(new Evidence("keep-alive", "first keep-alive " + first / 1000 + " s after joining (vanilla's 15 s)", Family.VANILLA, 2, true));
            else if (first >= 22000 && first <= 28000) ev.add(new Evidence("keep-alive", "first keep-alive " + first / 1000 + " s after joining (CraftBukkit/Spigot wait 25 s)", Family.BUKKIT, 3, true));
            else ev.add(new Evidence("keep-alive", "first keep-alive " + first / 1000 + " s after joining (no known pattern)", Family.UNKNOWN, 0));
        } else if (ka <= 0 && firstKa == 0 && sinceJoin > 18000 && proto >= 340) {
            ev.add(new Evidence("keep-alive", "no keep-alive in the first " + sinceJoin / 1000 + " s (not Paper's 1 s or vanilla's 15 s; CraftBukkit/Spigot wait 25 s)", Family.BUKKIT, 2, true));
        } else if (ka > 0) {
            if (ka < 4000) ev.add(new Evidence("keep-alive", "every ~" + ka + " ms (Paper sends one per second)", Family.PAPER, 3));
            else if (ka >= 20000 && ka <= 30000) ev.add(new Evidence("keep-alive", "every ~" + ka + " ms (Spigot uses 25 s)", Family.BUKKIT, 3, true));
            else if (ka >= 12000 && ka < 20000) ev.add(new Evidence("keep-alive", "every ~" + ka + " ms (vanilla timing)", Family.VANILLA, 2, true));
            else ev.add(new Evidence("keep-alive", "every ~" + ka + " ms (no known pattern)", Family.UNKNOWN, 0));
        } else {
            notes.add("Keep-alive timing needs more time on the server (" + keepAliveCount() + " seen so far).");
        }

        String kaIds = keepAliveIdPattern();
        int[] blockTags = tagInfo.get("block");
        int[] itemTags = tagInfo.get("item");
        boolean emptyBlockTags = blockTags != null && blockTags[0] > 0 && blockTags[1] == 0;
        boolean trimmedTags = !tagInfo.isEmpty() && tagInfo.size() <= 6 && blockTags != null && blockTags[0] < 150 && blockTags[1] > 0
                && (itemTags == null || itemTags[0] < 60);
        boolean tagsForEmptyRegistries = false;
        for (int[] t : tagInfo.values()) if (t[0] == 0) tagsForEmptyRegistries = true;
        boolean noVanillaLevel = !levels.isEmpty() && levels.stream().noneMatch(VANILLA_LEVELS::contains);
        boolean noBrand = brandFirst == null && done && System.currentTimeMillis() - doneAt > 1500;
        String tabShape = tabActions.isEmpty() ? null : String.join(",", tabActions);
        boolean picoTab = "ADD_PLAYER,UPDATE_LISTED".equals(tabShape);
        boolean nanoTab = "ADD_PLAYER,UPDATE_GAME_MODE,UPDATE_LISTED".equals(tabShape);
        boolean loohpTab = "ADD_PLAYER,UPDATE_DISPLAY_NAME,UPDATE_GAME_MODE,UPDATE_LATENCY,UPDATE_LISTED".equals(tabShape);
        boolean picoTeleport = teleportId == 0;
        if (picoTeleport) {
            ev.add(new Evidence("join", "your first teleport id is 0 (Mojang's server code starts at 1, PicoLimbo never counts)", Family.CUSTOM, 3));
        }
        if (emptyBlockTags) {
            ev.add(new Evidence("config", blockTags[0] + " block tags sent without a single block in them (PicoLimbo)", Family.CUSTOM, 3));
        } else if (trimmedTags) {
            ev.add(new Evidence("config", "hand-trimmed tag set: " + tagInfo.size() + " registries, " + blockTags[0] + " block tags (QuasarMC-style from-scratch server)", Family.CUSTOM, 3));
        }
        if (tagsForEmptyRegistries) {
            ev.add(new Evidence("config", "tag lists sent for registries that have no tags (Minestom)", Family.CUSTOM, 2));
        }
        if (noVanillaLevel) {
            ev.add(new Evidence("login", "no vanilla dimension in the login packet " + levels + " (LOOHP Limbo world)", Family.CUSTOM, 2));
        }
        if (done && maxP < 0) {
            ev.add(new Evidence("login", "max players " + maxP + " in the login packet (LOOHP Limbo)", Family.CUSTOM, 1));
        }
        if (noBrand) {
            ev.add(new Evidence("config", "no brand sent at all (vanilla, Paper and every proxy send one; QuasarMC does not)", Family.CUSTOM, 2));
        }
        if (tabShape != null && (picoTab || nanoTab || loohpTab)) {
            ev.add(new Evidence("join", "your tab-list entry only carries " + tabShape.toLowerCase(Locale.ROOT).replace(',', '/')
                    + (picoTab ? " (PicoLimbo)" : nanoTab ? " (NanoLimbo)" : " (LOOHP Limbo)"), Family.CUSTOM, 1));
        }
        if (limbo) {
            ev.add(new Evidence("config", "no feature flags in the configuration phase (limbo or from-scratch server)", Family.CUSTOM, 4));
        }
        if ("random".equals(kaIds)) {
            ev.add(new Evidence("keep-alive", "keep-alive ids are random, not a clock (not Mojang's server code)", Family.CUSTOM, 2));
        } else if ("nanos".equals(kaIds)) {
            ev.add(new Evidence("keep-alive", "keep-alive ids count nanoseconds (Minestom)", Family.CUSTOM, 2));
        }
        if (Boolean.TRUE.equals(zeroSess)) {
            ev.add(new Evidence("login", "all-zero session id at login (Minestom-style)", Family.CUSTOM, 1));
        }
        if (noLevels || maxP == 0) {
            ev.add(new Evidence("login", (noLevels ? "empty dimension list" : "0 max players") + " in the login packet (custom server)", Family.CUSTOM, 1));
        }

        if ("BungeeCord".equals(proxy) && maxP > 0) {
            ev.add(new Evidence("login", "max players " + maxP + " (BungeeCord sends its tab size)", Family.UNKNOWN, 0));
        }

        if (namespaces.contains("paper")) ev.add(new Evidence("commands", "paper: namespace present", Family.PAPER, 3));
        if (namespaces.contains("purpur")) ev.add(new Evidence("commands", "purpur: namespace present", Family.PAPER, 3));
        if (namespaces.contains("folia")) ev.add(new Evidence("commands", "folia: namespace present", Family.PAPER, 3));
        if (namespaces.contains("spigot")) ev.add(new Evidence("commands", "spigot: namespace present", Family.BUKKIT, 1));
        if (roots.contains("divinemc") || namespaces.contains("divinemc")) {
            divine = true;
            ev.add(new Evidence("commands", "divinemc command present (DivineMC)", Family.PAPER, 3));
        }
        boolean bukkitCmds = namespaces.contains("bukkit") || roots.contains("icanhasbukkit");
        if (bukkitCmds) ev.add(new Evidence("commands", "Bukkit commands present (bukkit:, icanhasbukkit)", Family.BUKKIT, 2));
        int pluginsArgs = pluginsArguments;
        if (bukkitCmds && pluginsArgs == 0) {
            ev.add(new Evidence("commands", "/plugins takes no argument (Paper's version of the command)", Family.PAPER, 2));
        } else if (bukkitCmds && pluginsArgs > 0) {
            ev.add(new Evidence("commands", "/plugins takes an argument (CraftBukkit/Spigot version of the command)", Family.BUKKIT, 2, true));
        }
        if (bukkitCmds && (roots.contains("list") || roots.contains("random") || roots.contains("minecraft:list") || roots.contains("minecraft:random"))) {
            ev.add(new Evidence("commands", "list/random are offered without op (Paper hides them)", Family.BUKKIT, 1, true));
        }
        boolean foliaTree = bukkitCmds && roots.contains("msg") && !roots.contains("teammsg") && !roots.contains("trigger");
        if (foliaTree) ev.add(new Evidence("commands", "no teammsg/tm/trigger (Folia removes them)", Family.PAPER, 0));

        if (sectionSuffix && proxy == null && !leaves) {
            divine = true;
            ev.add(new Evidence("brand", "brand ends with a hidden §r (DivineMC)", Family.PAPER, 0));
        }

        if (pingName != null && !pingName.isBlank()) {
            ev.add(new Evidence("ping", "server list says \"" + pingName + "\"", Family.UNKNOWN, 0));
        }
        int sample = 0;
        if (data != null && data.playerList != null) {
            for (Component line : data.playerList) {
                if (!(line.getContents() instanceof TranslatableContents t && t.getKey().startsWith("multiplayer.status"))) sample++;
            }
        }
        boolean uncappedSample = sample > 12 && proxy == null;
        if (uncappedSample) {
            ev.add(new Evidence("ping", "server list shows " + sample + " player names (Spigot and Paper cap it at 12)", Family.BUKKIT, 1, true));
        }

        if (arclightLoader != null) ev.add(new Evidence("brand", backendBrand + " (Arclight)", Family.MODDED_BUKKIT, 3));
        else if (brandLower.equals("fabric") || brandLower.endsWith(",fabric")) ev.add(new Evidence("brand", backendBrand, Family.FABRIC, 1));
        else if (brandLower.equals("quilt") || brandLower.endsWith(",quilt")) ev.add(new Evidence("brand", backendBrand, Family.QUILT, 1));
        else if (brandLower.equals("forge")) ev.add(new Evidence("brand", "forge", Family.FORGE, 1));
        else if (brandLower.equals("neoforge")) ev.add(new Evidence("brand", "neoforge", Family.NEOFORGE, 1));
        else if (brandLower.equals("sponge")) ev.add(new Evidence("brand", "sponge", Family.SPONGE, 1));
        else if (brandLower.equals("vanilla")) ev.add(new Evidence("brand", "vanilla", Family.VANILLA, 1));

        int paper = score(ev, Family.PAPER);
        int bukkit = score(ev, Family.BUKKIT);
        int vanilla = score(ev, Family.VANILLA);
        int neo = score(ev, Family.NEOFORGE), forge = score(ev, Family.FORGE), sponge = score(ev, Family.SPONGE);
        int fabric = score(ev, Family.FABRIC), quilt = score(ev, Family.QUILT);
        int custom = score(ev, Family.CUSTOM);
        boolean moddedBukkit = arclightLoader != null
                || ((neo >= 4 || fabricPing) && bukkitCmds && paper < 3 && !chatTypes.contains("paper:raw")
                && (jm == JoinMessage.LITERAL || (ka >= 20000 && ka <= 30000)));

        Family family;
        boolean paperApi = chatTypes.contains("paper:raw") || namespaces.contains("paper") || namespaces.contains("purpur")
                || namespaces.contains("folia") || bukkitCmds;
        boolean moddedEvidence = neo > 0 || forge > 0 || fabric > 0 || quilt > 0;
        int againstPaper = 0;
        for (Evidence e : ev) if (e.againstPaper()) againstPaper++;
        boolean paperContradicted = !chatTypes.contains("paper:raw") && againstPaper >= 2;
        if (neo >= 4 && paper >= 3 && paperApi) family = Family.HYBRID;
        else if (moddedBukkit) family = Family.MODDED_BUKKIT;
        else if (custom >= 4 && !chatTypes.contains("paper:raw")) family = Family.CUSTOM;
        else if (sponge >= 3) family = Family.SPONGE;
        else if (paper >= 3 && (paperApi || !moddedEvidence) && !paperContradicted) family = Family.PAPER;
        else if (bukkit >= 2) family = Family.BUKKIT;
        else if (neo > 0) family = Family.NEOFORGE;
        else if (forge > 0) family = Family.FORGE;
        else if (quilt > 0) family = Family.QUILT;
        else if (fabric > 0) family = Family.FABRIC;
        else if (vanilla > 0) family = Family.VANILLA;
        else family = Family.UNKNOWN;

        int structural = 0;
        for (Evidence e : ev) {
            if ("brand".equals(e.signal())) continue;
            if (e.family() == family || (family == Family.HYBRID && (e.family() == Family.NEOFORGE || e.family() == Family.PAPER))
                    || (family == Family.MODDED_BUKKIT && (e.family() == Family.NEOFORGE || e.family() == Family.FABRIC || e.family() == Family.BUKKIT))
                    || (MODDED_VANILLA.contains(family) && e.family() == Family.VANILLA)) {
                structural += e.weight();
            }
        }
        String confidence = structural >= 6 ? "high" : structural >= 3 ? "medium" : "low";

        String software = name(family, backendBrand, ev, notes, clientSide != nativeProtocol() || !configPhase);
        if (family == Family.BUKKIT) {
            String known = knownName(backendBrand);
            if (respawnRadius > 0 && !beyondSpigot) {
                software = "Spigot";
                notes.add("Spigot confirmed by the packets (view distance re-sent after respawning).");
            } else if (uncappedSample && !"Spigot".equals(known)) {
                software = "CraftBukkit";
                notes.add("CraftBukkit: the server list sample is not capped at 12 names like Spigot's.");
            } else if (beyondSpigot) {
                if ("Spigot".equals(known)) {
                    notes.add("Brand says Spigot, but mobs are sent from beyond Spigot's default 48 blocks (raised entity-tracking-range in spigot.yml, or CraftBukkit).");
                } else {
                    software = "CraftBukkit";
                    notes.add("CraftBukkit: Bukkit server without Spigot's entity tracking limits (mobs sent " + Math.round(farthest) + " blocks away).");
                }
            } else if (spigotRange && !"Spigot".equals(known)) {
                if ("CraftBukkit".equals(known)) notes.add("Brand says CraftBukkit, but mobs stop at Spigot's 48-block tracking range (Spigot with a changed brand?).");
                else software = "Spigot";
            }
        } else if (family == Family.PAPER && beyondPaper) {
            notes.add("Mobs are sent from beyond Paper's default 96 blocks, so its entity-tracking-range was raised.");
        }

        if (family == Family.PAPER) {
            String known = knownName(backendBrand);
            String fork = null;
            String why = null;
            boolean conflict = false;
            if (canvas) { fork = "Canvas"; why = "ticking state without ticking step"; }
            else if (leaves) { fork = "Leaves"; why = "bladeren protocol"; }
            else if (divine) { fork = "DivineMC"; why = "DivineMC-only packets"; }
            else if (purpurTime) { fork = "Purpur"; why = "per-tick time packets"; }
            else if (purpurFamily && !hashMap && (proto >= PROTO_26_1 ? seconds >= 3 : arrayMap || (ownListArray && "Leaf".equals(known)))) {
                fork = "Leaf";
                why = proto >= PROTO_26_1 ? "Purpur mob attributes without Purpur's time packets" : "Purpur mob attributes in an array attribute map";
            } else if (purpurFamily && hashMap && proto < PROTO_26_1) {
                fork = "Purpur";
                why = "Purpur mob attributes in a hash attribute map";
            } else if (universeSpeed || universeRain || universeFreeze || universeChunks || universeEquipment || universeTab || (statsStep20 && !"Gale".equals(known))) {
                if (purpurFamily || hashMap || rainWithFlip > 0 || floatSpeed > 0 || (statsOffGrid && playTimeMoved)) conflict = true;
                else {
                    fork = "UniverseSpigot";
                    List<String> reasons = new ArrayList<>();
                    if (universeSpeed) reasons.add("exact Speed effect modifier");
                    if (universeRain) reasons.add("rain without the start/stop event");
                    if (universeFreeze) reasons.add("fresh mobs past 64 blocks frozen, odd ids never move");
                    if (universeChunks) reasons.add("chunks sent out of order by parallel threads");
                    if (universeEquipment) reasons.add("mob equipment sent twice by the async tracker");
                    if (universeTab) reasons.add("players unlisted from the tab list");
                    if (statsStep20) reasons.add("time statistics in steps of 20");
                    why = String.join(", ", reasons);
                }
            } else if ((arrayMap || ownListArray) && !purpurFamily && "UniverseSpigot".equals(known) && floatSpeed == 0 && rainWithFlip == 0) {
                fork = "UniverseSpigot";
                why = "array attribute map without Purpur's attributes";
            } else if (dabOn && !purpurFamily && !arrayMap) {
                fork = "Pluto".equals(known) || proto >= PROTO_26_1 ? "Pluto" : "Pufferfish";
                why = "Dynamic Activation of Brain slows distant mob AI";
            } else if (foliaTree) {
                fork = "Folia";
                why = "teammsg/tm/trigger missing";
            }

            if (conflict) {
                notes.add("Conflicting UniverseSpigot signals: the Speed modifier or the weather says UniverseSpigot, other packets do not (a per-player weather plugin can cause this).");
            }
            if (fork != null && known != null && fork.equals(LINEAGE.get(known))) {
                notes.add("Packets match " + fork + ", which " + known + " is built on.");
                fork = known;
                why = null;
                software = known;
            } else if (fork != null) {
                if (known != null && !known.equals(fork) && !known.equals("Paper")) {
                    notes.add("Brand says " + known + ", but the packets point to " + fork + " (" + why + ").");
                } else if ("Paper".equals(known)) {
                    notes.add("Brand says Paper, but the packets point to " + fork + " (" + why + ").");
                } else {
                    notes.add(fork + " confirmed by the packets (" + why + ").");
                }
                software = fork;
            } else if (purpurFamily) {
                if (known != null && (known.equals("Purpur") || "Purpur".equals(LINEAGE.get(known)))) {
                    notes.add("Purpur-family mobs. Telling Purpur from Leaf needs more attribute kinds in view (horses, other players, or a death and respawn).");
                } else {
                    software = "Purpur-based";
                    notes.add("Purpur-family mob attributes" + (known != null ? ", although the brand says " + known : "") + ".");
                }
            } else if (known != null && !known.equals("Paper")) {
                if (known.equals("Leaf") && hashMap) {
                    notes.add("Brand says Leaf, but the mob attributes come from a hash map (not Leaf, brand likely spoofed).");
                    software = "Paper-based";
                } else if ((known.equals("Purpur") || known.equals("Leaf")) && zombieVanilla) {
                    notes.add("Brand says " + known + ", but zombie horses spawn with vanilla's 25 HP (Purpur config changed, or not Purpur).");
                } else if (known.equals("UniverseSpigot") && (floatSpeed > 0 || hashMap || rainWithFlip > 0)) {
                    notes.add("Brand says UniverseSpigot, but " + (floatSpeed > 0 ? "the Speed modifier is vanilla's" : rainWithFlip > 0 ? "rain changes come with the start/stop event" : "the mob attributes come from a hash map") + " (brand likely spoofed).");
                    software = "Paper-based";
                } else if ((known.equals("Pufferfish") || known.equals("Pluto")) && dabOff) {
                    notes.add("Brand says " + known + ", but distant mobs run full-speed AI (Dynamic Activation of Brain turned off, or the brand was changed).");
                } else if (known.equals("Pufferfish") || known.equals("Pluto")) {
                    notes.add(known + " sends Paper's join packets. It is confirmed by mob AI timing: stand still for about 30 s with animals 29-32 blocks away and no other player near them.");
                } else {
                    notes.add("No packet signal for " + known + " yet, the fork name comes from the brand" + (pingAgrees(pingName, known) ? " and the server list name." : "."));
                }
            }
        } else if (family == Family.HYBRID) {
            software = backendBrand != null && backendBrand.equalsIgnoreCase("Youer") ? "Youer" : "NeoForge + Paper hybrid";
            notes.add("NeoForge networking together with Paper/Purpur server behaviour.");
        } else if (family == Family.CUSTOM) {
            String limboKind = emptyBlockTags || picoTab || picoTeleport ? "PicoLimbo"
                    : nanoTab ? "NanoLimbo"
                    : noVanillaLevel || loohpTab || done && maxP < 0 ? "LOOHP Limbo"
                    : trimmedTags && noBrand ? "QuasarMC"
                    : "nanos".equals(kaIds) || tagsForEmptyRegistries ? "Minestom"
                    : null;
            String byBrand = brandLower.contains("nanolimbo") ? "NanoLimbo"
                    : brandLower.contains("picolimbo") ? "PicoLimbo"
                    : brandLower.contains("minestom") ? "Minestom"
                    : brandLower.contains("quasar") ? "QuasarMC"
                    : brandLower.equals("limbo") ? "LOOHP Limbo"
                    : null;
            if (limboKind != null) {
                software = limboKind;
                if (byBrand != null && !byBrand.equals(limboKind)) notes.add("Brand says " + byBrand + ", but the packets look like " + limboKind + ".");
                else notes.add(limboKind + " confirmed by the packets" + (byBrand != null ? " and the brand." : " (brand not needed)."));
            } else if (byBrand != null) {
                software = byBrand;
                notes.add(byBrand + " recognised from the brand, the packets only show a custom server.");
            } else if (limbo) {
                software = "Limbo server";
            } else {
                software = backendBrand != null && !backendBrand.isBlank() ? backendBrand + " (custom server)" : "Custom server";
            }
            notes.add("Not built on Mojang's server code: " + ("QuasarMC".equals(software) || "Minestom".equals(software)
                    ? "a from-scratch server implementation." : limbo ? "a lightweight limbo/lobby server." : "a from-scratch server implementation."));
        } else if (family == Family.MODDED_BUKKIT) {
            software = arclightLoader != null ? "Arclight (" + arclightLoader + ")" : "Arclight-style " + (neo >= 4 ? "NeoForge" : "Fabric") + " + Bukkit hybrid";
            notes.add((neo >= 4 ? "NeoForge" : "Fabric") + " networking with Bukkit commands and Spigot-style timings.");
        }

        if (family == Family.UNKNOWN && knownName(backendBrand) != null) {
            software = knownName(backendBrand) + " (brand only)";
            notes.add("Only the brand identifies this, the packet signals were inconclusive (rejoin for a cleaner read).");
        }

        return new Result(software, family, rawBrand == null ? null : strip(rawBrand), proxy, confidence, ev, notes);
    }

    private static void resolveRain(long now) {
        Iterator<Long> it = rainCrossings.iterator();
        while (it.hasNext()) {
            long crossing = it.next();
            if (now - crossing < RAIN_FLIP_WINDOW_MS) continue;
            boolean flipped = false;
            for (long flip : rainFlips) {
                if (Math.abs(flip - crossing) <= RAIN_FLIP_WINDOW_MS) {
                    flipped = true;
                    break;
                }
            }
            if (flipped) rainResolvedWith++;
            else rainResolvedWithout++;
            it.remove();
        }
    }

    private static String tagSummaryText() {
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<String, int[]> e : tagSummary.entrySet()) sb.append(e.getKey()).append(':').append(e.getValue()[0]).append('/').append(e.getValue()[1]).append(' ');
        return sb.append('}').toString();
    }

    private static int[] rainOutcome(long now) {
        resolveRain(now);
        return new int[]{rainResolvedWith, rainResolvedWithout};
    }

    public static String debugState() {
        synchronized (JOIN) {
            return "backend=" + backendProtocol() + " core=" + coreVersion + " packsEmpty=" + (knownPacksReceived ? knownPacksEmpty : "none")
                    + " late=" + lateRegistries + " features=" + featuresSeen + " vv=" + vvChannel + ":" + vvVersion
                    + " leaves=" + leavesChannels + ":" + leavesVersion
                    + " resync=" + resyncHits + "/" + (resyncHits + resyncMisses) + " early=" + resyncEarlyHits + "/" + (resyncEarlyHits + resyncEarlyMisses)
                    + " pairing=" + pairingLists + " purpurAttr=" + purpurAttrLists + " longestAsc=" + longestAscending
                    + " rain=" + java.util.Arrays.toString(rainOutcome(System.currentTimeMillis()))
                    + " stats=" + timeStatsReadings + (timeStatsOffGrid ? "/offgrid" : "/grid20") + playTimeReadings
                    + " ascAttrs=" + ascendingAttributes.size() + " zh=" + zombieHorse15 + "/" + zombieHorse25 + " cadence=" + MobCadence.debug() + " freeze=[" + FreshFreeze.debug() + "] chunkOrder=" + chunkDescents + "/" + orderedChunks + " early=" + entitiesBeforeChunk
                    + " equip=" + equipmentOrphanIds.size() + "/" + equipmentDuplicates + "/" + equipmentDuplicateIds.size()
                    + " ascSets=" + ascendingKeySets.size() + " unsortedSnap=" + unsortedSnapshots + "/" + unsortedSnapshotsLong
                    + " sd=" + serverDataCount + " sdAfterPos=" + serverDataAfterPosition + " sdBorders=" + serverDataBorders
                    + " tags=" + tagSummaryText() + " levels=" + levelNames + " tabActions=" + firstTabActions + " tp0=" + firstTeleportId
                    + " features=" + featuresSeen + " zeroSession=" + zeroSession + " emptyLevels=" + emptyLevels + " kaIds=" + keepAliveIdPattern()
                    + " tabAdds=" + selfTabAdds + "/" + firstTabAddEntries + ":" + tabAddPattern + " mh=" + pairingWithMaxHealth + "/" + pairingWithoutMaxHealth + "/" + pairingWithScale
                    + " weather=" + joinWeather + " respawn=" + respawnRadiusFirst + "/" + respawnPositionFirst + " ownDataEarly=" + ownDataBeforeBorder
                    + " firstKa=" + (firstKeepAliveAt > 0 && loginAt > 0 ? firstKeepAliveAt - loginAt : 0) + " tracked=" + trackedMobs + "/" + Math.round(farthestTracked) + ":" + farthestTrackedType
                    + " border=" + borderCount + " speed=" + speedExact + "/" + speedFloat + " setTime=" + setTimePackets;
        }
    }

    private static boolean pingAgrees(String pingName, String fork) {
        return pingName != null && pingName.toLowerCase(Locale.ROOT).startsWith(fork.toLowerCase(Locale.ROOT) + " ");
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

    private static String knownName(String brand) {
        if (brand == null) return null;
        for (String fork : PAPER_FORKS) if (fork.equalsIgnoreCase(brand)) return fork;
        if (brand.equalsIgnoreCase("Spigot")) return "Spigot";
        if (brand.equalsIgnoreCase("CraftBukkit")) return "CraftBukkit";
        return null;
    }

    private static String name(Family family, String brand, List<Evidence> ev, List<String> notes, boolean translated) {
        String known = knownName(brand);
        switch (family) {
            case PAPER -> {
                if (known != null && PAPER_FORKS.contains(known)) return known;
                if (known != null) notes.add("Brand claims \"" + known + "\", but the structure is Paper-based (brand likely spoofed).");
                else if (brand != null && !brand.isBlank()) notes.add("Brand \"" + brand + "\" is not a fork Hontun can confirm, the packets say Paper-based.");
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
            case HYBRID -> { return "NeoForge + Paper hybrid"; }
            case MODDED_BUKKIT -> { return "NeoForge/Fabric + Bukkit hybrid"; }
            case CUSTOM -> { return "Custom server"; }
            case SPONGE -> { return "Sponge"; }
            case NEOFORGE -> { return "NeoForge"; }
            case FORGE -> { return "Forge"; }
            case QUILT -> { return "Quilt"; }
            case FABRIC -> { return "Fabric"; }
            case VANILLA -> { return "Vanilla"; }
            default -> { return brand != null && !brand.isBlank() ? brand + " (unverified)" : "Unknown"; }
        }
    }
}
