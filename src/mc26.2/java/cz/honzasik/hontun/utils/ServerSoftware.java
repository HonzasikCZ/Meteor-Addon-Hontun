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
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.network.protocol.game.ClientboundChunkBatchFinishedPacket;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
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
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTickingStatePacket;
import net.minecraft.network.protocol.game.ClientboundTickingStepPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ClientboundLoginFinishedPacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerLinks;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.players.NameAndId;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
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
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ServerSoftware {
    public enum Family {
        PAPER("Paper-based"), BUKKIT("Bukkit-based"), HYBRID("NeoForge + Paper hybrid"),
        MODDED_BUKKIT("Forge/NeoForge/Fabric + Bukkit hybrid"), CUSTOM("Custom (non-Mojang) server"), SPONGE("Sponge"),
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
    private static final int PROTO_26_2 = 776;
    private static final int PURPUR_MIN_PACKETS = 15;
    private static final double PURPUR_MIN_RATE = 10.0;
    private static final int FABRIC_API_PING = 0xFAB71C;
    private static final long SNAPSHOT_WINDOW_MS = 50;
    private static final long RESPAWN_WINDOW_MS = 250;
    private static final long RESYNC_WINDOW_MS = 50;
    private static final long RESYNC_EARLY_MS = 20000;
    private static final int TRACK_CAP = 512;
    private static final int CHANNEL_CAP = 1024;
    private static final int JOIN_LIST_CAP = 64;
    private static final long RAIN_FLIP_WINDOW_MS = 1500;
    private static final Pattern VIAPROXY_BRAND = Pattern.compile("^ViaProxy \\((.+?)\\) -> (.*) \\(([^()]+)\\)$");
    private static final String GATE_SUFFIX = " (Gate by Minekube)";
    private static final Map<String, String> LINEAGE = Map.of(
            "Plazma", "Purpur",
            "DivineMC", "Purpur",
            "Canvas", "Folia",
            "Pluto", "Pufferfish",
            "ShreddedPaper", "Purpur");
    private static final Map<String, Integer> LATE_REGISTRY_PROTOCOL = Map.of(
            "world_clock", PROTO_1_21_11,
            "zombie_nautilus_variant", 771,
            "dialog", 770,
            "frog_variant", 768,
            "instrument", PROTO_1_21,
            "painting_variant", 766);
    private static final Set<String> LIMBO_SOFTWARE = Set.of("PicoLimbo", "NanoLimbo", "LOOHP Limbo", "LimboAPI");
    private static final Set<String> PUMPKIN_TAB = Set.of("ADD_PLAYER", "UPDATE_GAME_MODE", "UPDATE_LISTED", "UPDATE_LATENCY", "UPDATE_HAT", "UPDATE_LIST_ORDER");
    private static final Set<String> LIMBOAPI_TAB = Set.of("ADD_PLAYER", "UPDATE_GAME_MODE", "UPDATE_DISPLAY_NAME");
    private static final long DAY_MS = 86_400_000L;
    private static final List<String> HYBRID_BRANDS = List.of("Youer", "Mohist", "Magma", "Ketting", "Banner");
    private static final Set<String> FML_LEGACY = Set.of("fml:loginwrapper", "fml:handshake", "fml:play", "forge:split", "forge:tier_sorting");
    private static final long OXYGEN_WINDOW_MS = 3000;
    private static final int STATUS_FORGE_DATA = 1;
    private static final int STATUS_MODDED = 2;

    private static final Set<String> chatTypes = ConcurrentHashMap.newKeySet();
    private static final Set<String> registries = ConcurrentHashMap.newKeySet();
    private static final Set<String> registryNamespaces = ConcurrentHashMap.newKeySet();
    private static final List<Long> keepAlives = new ArrayList<>();
    private static final List<long[]> keepAliveIds = new ArrayList<>();
    private static volatile long firstKeepAliveAt;
    private static final java.util.ArrayDeque<Object[]> trackedSpawns = new java.util.ArrayDeque<>();
    private static final java.util.ArrayDeque<Object[]> miscSpawns = new java.util.ArrayDeque<>();
    private static final Map<Integer, long[]> passengerLinks = new LinkedHashMap<>();
    private static final Set<String> TRACK_MISC = Set.of("item", "item_frame", "glow_item_frame", "painting", "experience_orb");
    private static final Set<String> TRACK_ANIMALS = Set.of("pig", "cow", "mooshroom", "sheep", "chicken", "rabbit", "horse", "donkey",
            "mule", "llama", "goat", "cat", "wolf", "fox", "villager", "iron_golem", "bee", "frog", "turtle", "camel", "armadillo",
            "sniffer", "panda", "polar_bear", "ocelot", "parrot");
    private static final Set<String> TRACK_MONSTERS = Set.of("zombie", "husk", "drowned", "skeleton", "stray", "creeper", "spider",
            "cave_spider", "enderman", "witch", "slime", "zombie_villager", "pillager", "vindicator");
    private static final double SPIGOT_TRACK_LIMIT = 58.0;
    private static final double PAPER_TRACK_LIMIT = 106.0;
    private static final double SPIGOT_MISC_LIMIT = 36.0;
    private static final double SPIGOT_OTHER_LIMIT = 68.0;
    private static final long PASSENGER_WINDOW_MS = 1000;
    private static final AtomicInteger chunkBatches = new AtomicInteger();
    private static final AtomicInteger chunks = new AtomicInteger();
    private static volatile long lastKeepAliveId = Long.MIN_VALUE;
    private static volatile List<String> rootCommands = List.of();
    private static volatile int pluginsArguments = -1;
    private static final Map<ServerData, Boolean> sampleV7 = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<ServerData, Integer> statusFlags = Collections.synchronizedMap(new WeakHashMap<>());
    private static final int STATUS_CAP = 1024;
    private static final ThreadLocal<Integer> statusJsonFlags = new ThreadLocal<>();
    private static int positionsSeen;
    private static int positionsApplied;
    private static int placeholderEpoch = -1;
    private static long positionSeenAt;
    private static int maxChunkRadius;

    private static final Object JOIN = new Object();
    private static final Set<String> loginQueries = new LinkedHashSet<>();
    private static Boolean zeroSession;
    private static UUID selfProfile;
    private static String profileRenamed;
    private static String firstBrand;
    private static String lastBrand;
    private static boolean brandInConfig;
    private static boolean brandFirstInPlay;
    private static int configRounds;
    private static int configKeepAlives;
    private static boolean cfgKeepAliveBeforeRegistries;
    private static String firstRegistry;
    private static int mcRegistryEntries;
    private static int mcRegistryEntriesWithData;
    private static boolean unsortedRegistryEntries;
    private static boolean biomeNbtChecked;
    private static boolean serverOnlyBiomeNbt;
    private static int biomeEntries = -1;
    private static boolean limboPadding;
    private static final Set<String> knownPacks = new LinkedHashSet<>();
    private static final List<Integer> configPings = new ArrayList<>();
    private static final Set<String> preLoginPayloads = new LinkedHashSet<>();
    private static final Set<String> registerChannels = new LinkedHashSet<>();
    private static final List<String> lateRegistries = new ArrayList<>();
    private static final Map<String, int[]> tagSummary = new LinkedHashMap<>();
    private static final Map<String, Integer> tagNamespaces = new HashMap<>();
    private static final Map<String, Integer> playTagNamespaces = new HashMap<>();
    private static boolean featuresAfterTags;
    private static int ownAttrPackets;
    private static boolean ownOxygen;
    private static boolean ownDiedEarly;
    private static int pairingWithOxygen;
    private static final List<String> levelNames = new ArrayList<>();
    private static int firstTeleportId = Integer.MIN_VALUE;
    private static boolean firstTeleportYRot;
    private static int loginView = -1;
    private static int loginSim = -1;
    private static boolean loginReducedDebug;
    private static boolean loginDeathScreen = true;
    private static long loginSeed;
    private static boolean loginPrevSurvival;
    private static boolean abilitiesSeen;
    private static float firstFlyingSpeed;
    private static int emptyChunkBatches;
    private static final Set<String> selfTabActions = new TreeSet<>();
    private static int selfTabChecks;
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
    private static final List<String> earlyLinks = new ArrayList<>();
    private static boolean emptyRegisterInPlay;
    private static boolean leavesChannels;
    private static String leavesVersion;
    private static String vvChannel;
    private static int vvVersion;
    private static String vvName;
    private static String backendVvChannel;
    private static int backendVvVersion;
    private static String backendVvName;
    private static int configLogins;
    private static boolean appDetailsSeen;
    private static Component firstMotd;
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
    private static double farthestMisc;
    private static String farthestMiscType;
    private static double farthestOther;
    private static String farthestOtherType;
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
    private static int joinSeq;
    private static int advancementsAt = -1;
    private static boolean advancementsSeen;
    private static int ownMetaAt = -1;
    private static int bordersAtCommands = -1;
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
    private static Map<net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<?>>, net.minecraft.tags.TagNetworkSerialization.NetworkPayload> configTagMap;
    private static Map<net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<?>>, net.minecraft.tags.TagNetworkSerialization.NetworkPayload> playTagMap;
    private static boolean tagsBeforeRest;
    private static final Map<String, List<Identifier>> realRegistries = new HashMap<>();
    private static final Set<String> REAL_REGISTRIES = Set.of("minecraft:worldgen/biome", "minecraft:chat_type");
    private static final int REAL_REGISTRY_CAP = 512;
    private static int overworldMinY = Integer.MIN_VALUE;
    private static int overworldHeight = Integer.MIN_VALUE;
    private static int loginSeaLevel = Integer.MIN_VALUE;
    private static int teleportCount;
    private static boolean teleportNonZero;
    private static boolean simDistanceSeen;
    private static volatile boolean syntheticCommands;
    private static volatile boolean commandsSeen;

    private ServerSoftware() {}

    public static void resetAll() {
        synchronized (JOIN) {
            loginQueries.clear();
            zeroSession = null;
            selfProfile = null;
            profileRenamed = null;
        }
        resetConfig();
        synchronized (JOIN) {
            configRounds = 0;
        }
    }

    public static void resetConfig() {
        chatTypes.clear();
        registries.clear();
        registryNamespaces.clear();
        synchronized (JOIN) {
            firstBrand = null;
            lastBrand = null;
            brandInConfig = false;
            brandFirstInPlay = false;
            configRounds++;
            configLogins = 0;
            configKeepAlives = 0;
            cfgKeepAliveBeforeRegistries = false;
            firstRegistry = null;
            mcRegistryEntries = 0;
            mcRegistryEntriesWithData = 0;
            unsortedRegistryEntries = false;
            biomeNbtChecked = false;
            serverOnlyBiomeNbt = false;
            biomeEntries = -1;
            limboPadding = false;
            knownPacks.clear();
            configPings.clear();
            preLoginPayloads.clear();
            registerChannels.clear();
            lateRegistries.clear();
            tagSummary.clear();
            tagNamespaces.clear();
            featuresAfterTags = false;
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
            earlyLinks.clear();
            configTagMap = null;
            tagsBeforeRest = false;
            realRegistries.clear();
            overworldMinY = Integer.MIN_VALUE;
            overworldHeight = Integer.MIN_VALUE;
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
        miscSpawns.clear();
        passengerLinks.clear();
        loginAt = 0;
        selfTabAdds = 0;
        firstTabAddEntries = -1;
        levelNames.clear();
        firstTabActions.clear();
        firstTeleportId = Integer.MIN_VALUE;
        firstTeleportYRot = false;
        loginView = -1;
        loginSim = -1;
        loginReducedDebug = false;
        loginDeathScreen = true;
        loginSeed = 0;
        loginPrevSurvival = false;
        abilitiesSeen = false;
        firstFlyingSpeed = 0f;
        emptyChunkBatches = 0;
        selfTabActions.clear();
        selfTabChecks = 0;
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
        pairingWithOxygen = 0;
        pairingTypes.clear();
        playTagNamespaces.clear();
        ownAttrPackets = 0;
        ownOxygen = false;
        ownDiedEarly = false;
        joinWeather = "";
        respawnPending = false;
        respawnRadiusFirst = 0;
        respawnPositionFirst = 0;
        ownDataBeforeBorder = false;
        farthestTracked = 0;
        farthestTrackedType = null;
        trackedMobs = 0;
        farthestMisc = 0;
        farthestMiscType = null;
        farthestOther = 0;
        farthestOtherType = null;
        maxChunkRadius = 0;
        chunkBatches.set(0);
        chunks.set(0);
        lastKeepAliveId = Long.MIN_VALUE;
        rootCommands = List.of();
        pluginsArguments = -1;
        playTagMap = null;
        loginSeaLevel = Integer.MIN_VALUE;
        teleportCount = 0;
        teleportNonZero = false;
        simDistanceSeen = false;
        syntheticCommands = false;
        commandsSeen = false;
        MobCadence.reset();
        FreshFreeze.reset();
        BlockEntityOrder.reset();
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
        backendVvChannel = null;
        backendVvVersion = 0;
        backendVvName = null;
        appDetailsSeen = false;
        firstMotd = null;
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
        joinSeq = 0;
        advancementsAt = -1;
        advancementsSeen = false;
        ownMetaAt = -1;
        bordersAtCommands = -1;
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

    private static void onRegistryEntries(ClientboundRegistryDataPacket packet) {
        String key = packet.registry().identifier().toString();
        if (firstRegistry == null) firstRegistry = key.length() > 128 ? key.substring(0, 128) : key;
        boolean biome = "minecraft:worldgen/biome".equals(key);
        if (biome) biomeEntries = packet.entries().size();
        List<Identifier> real = null;
        if (REAL_REGISTRIES.contains(key)) {
            real = new ArrayList<>();
            realRegistries.put(key, real);
        }
        boolean dimensions = "minecraft:dimension_type".equals(key);
        String prev = null;
        for (RegistrySynchronization.PackedRegistryEntry entry : packet.entries()) {
            Identifier id = entry.id();
            String path = id.getPath();
            if (real != null && real.size() < REAL_REGISTRY_CAP) real.add(id);
            if (dimensions && "overworld".equals(path) && "minecraft".equals(id.getNamespace()) && entry.data().isPresent() && entry.data().get() instanceof CompoundTag dim) {
                overworldMinY = dim.getIntOr("min_y", Integer.MIN_VALUE);
                overworldHeight = dim.getIntOr("height", Integer.MIN_VALUE);
            }
            if (!limboPadding && path.startsWith("limboapi_padding_")) limboPadding = true;
            if (biome && !biomeNbtChecked && entry.data().isPresent()) {
                biomeNbtChecked = true;
                serverOnlyBiomeNbt = entry.data().get() instanceof CompoundTag tag && (tag.contains("carvers") || tag.contains("features"));
            }
            if (!"minecraft".equals(id.getNamespace())) continue;
            mcRegistryEntries++;
            if (entry.data().isPresent()) mcRegistryEntriesWithData++;
            if (prev != null && path.compareTo(prev) < 0) unsortedRegistryEntries = true;
            prev = path;
        }
    }

    public static void onKeepAlive(long id) {
        synchronized (JOIN) {
            if (!loggedIn) {
                configKeepAlives++;
                if (!configFinished && !sawRegistry && configRounds == 1) cfgKeepAliveBeforeRegistries = true;
                return;
            }
        }
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
        boolean placeholder = false;
        for (CommandNode<?> node : dispatcher.getRoot().getChildren()) {
            names.add(node.getName());
            if (node.getName().equals("plugins") || node.getName().equals("bukkit:plugins")) plugins = Math.max(plugins, node.getChildren().size());
            if (node instanceof com.mojang.brigadier.tree.ArgumentCommandNode<?, ?> && "args".equals(node.getName())) placeholder = true;
        }
        rootCommands = List.copyOf(names);
        pluginsArguments = plugins;
        syntheticCommands = placeholder;
        commandsSeen = true;
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
                        if (registerChannels.size() < CHANNEL_CAP) registerChannels.add(c);
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
        if ("vv:app_details".equals(channel)) {
            synchronized (JOIN) {
                appDetailsSeen = true;
            }
        }
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
                if (!"vv:proxy_details".equals(channel)) {
                    backendVvChannel = channel;
                    backendVvVersion = version;
                    backendVvName = name;
                }
            }
        } catch (Throwable ignored) {
        }
    }

    public static void onStatusJson(JsonElement json) {
        int flags = 0;
        try {
            if (json != null && json.isJsonObject()) {
                JsonObject object = json.getAsJsonObject();
                JsonElement forgeData = object.get("forgeData");
                if (forgeData != null && forgeData.isJsonObject()) flags |= STATUS_FORGE_DATA;
                JsonElement modinfo = object.get("modinfo");
                if (modinfo != null && modinfo.isJsonObject()) {
                    JsonElement type = modinfo.getAsJsonObject().get("type");
                    if (type != null && type.isJsonPrimitive() && "FML".equalsIgnoreCase(type.getAsString())) flags |= STATUS_FORGE_DATA;
                }
                JsonElement modded = object.get("isModded");
                if (modded != null && modded.isJsonPrimitive() && modded.getAsJsonPrimitive().isBoolean() && modded.getAsBoolean()) flags |= STATUS_MODDED;
            }
        } catch (Throwable ignored) {
        }
        statusJsonFlags.set(flags);
    }

    public static void onStatus(ServerData data, ServerStatus status) {
        Integer jsonFlags = statusJsonFlags.get();
        statusJsonFlags.remove();
        try {
            if (data == null || status == null) return;
            if (statusFlags.size() >= STATUS_CAP) statusFlags.clear();
            statusFlags.put(data, jsonFlags == null ? 0 : jsonFlags);
            List<NameAndId> sample = status.players().map(ServerStatus.Players::sample).orElse(null);
            boolean v7 = false;
            if (sample != null) {
                int seen = 0;
                for (NameAndId entry : sample) {
                    if (++seen > 16) break;
                    if (entry != null && entry.id() != null && entry.id().version() == 7) {
                        v7 = true;
                        break;
                    }
                }
            }
            if (sampleV7.size() >= STATUS_CAP) sampleV7.clear();
            sampleV7.put(data, v7);
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
                if (loginQueries.size() < JOIN_LIST_CAP) loginQueries.add(query.payload().id().toString());
                return;
            }
            if (packet instanceof ClientboundLoginFinishedPacket finished) {
                selfProfile = finished.gameProfile() == null ? null : finished.gameProfile().id();
                String got = finished.gameProfile() == null ? null : finished.gameProfile().name();
                String own = MCUtil.MC.getUser() == null ? null : MCUtil.MC.getUser().getName();
                profileRenamed = got != null && own != null && !got.equalsIgnoreCase(own) ? (got.length() > 32 ? got.substring(0, 32) : got) : null;
                UUID session = finished.sessionId();
                zeroSession = session == null || (session.getMostSignificantBits() == 0 && session.getLeastSignificantBits() == 0);
                return;
            }
            if (packet instanceof ClientboundCustomPayloadPacket custom) {
                if (custom.payload() instanceof BrandPayload brand) {
                    if (firstBrand == null) {
                        firstBrand = brand.brand();
                        if (!loggedIn && sawRegistry) brandAfterRegistry = true;
                        if (loggedIn) brandFirstInPlay = true;
                    }
                    lastBrand = brand.brand();
                    if (!loggedIn && !configFinished) brandInConfig = true;
                }
                if (configFinished && !loggedIn && preLoginPayloads.size() < JOIN_LIST_CAP) preLoginPayloads.add(custom.payload().type().id().toString());
            }
            if (!loggedIn && !configFinished) {
                if (packet instanceof ClientboundRegistryDataPacket registry) {
                    sawRegistry = true;
                    if (tagsSeen && lateRegistries.size() < JOIN_LIST_CAP) lateRegistries.add(registry.registry().identifier().getPath());
                    onRegistryEntries(registry);
                }
                if (packet instanceof ClientboundUpdateTagsPacket tags) {
                    if (!tagsSeen && !sawRegistry && !featuresSeen && !knownPacksReceived) tagsBeforeRest = true;
                    configTagMap = tags.getTags();
                    tagsSeen = true;
                    captureTags(tags);
                    countTagNamespaces(tags, tagNamespaces);
                }
                if (packet instanceof ClientboundUpdateEnabledFeaturesPacket) {
                    if (tagsSeen && !featuresSeen) featuresAfterTags = true;
                    featuresSeen = true;
                }
                if (packet instanceof ClientboundSelectKnownPacks select) {
                    knownPacksReceived = true;
                    knownPacksEmpty = select.knownPacks().isEmpty();
                    for (KnownPack kp : select.knownPacks()) {
                        knownPacks.add(kp.namespace() + ":" + kp.id());
                        if ("minecraft".equals(kp.namespace()) && "core".equals(kp.id())) coreVersion = kp.version();
                    }
                }
                if (packet instanceof ClientboundPingPacket ping && configPings.size() < JOIN_LIST_CAP) configPings.add(ping.getId());
            }
            if (packet instanceof ClientboundFinishConfigurationPacket) {
                configFinished = true;
                return;
            }
            if (packet instanceof ClientboundLoginPacket login) {
                if (loggedIn) resetPlayLocked();
                loggedIn = true;
                configLogins++;
                loginAt = now;
                placeholderEpoch = ++positionsSeen;
                positionSeenAt = now;
                lastRainLevel = -1f;
                playPackets = 0;
                ownEntityId = login.playerId();
                maxPlayers = login.maxPlayers();
                loginView = login.chunkRadius();
                maxChunkRadius = Math.max(0, loginView);
                loginSim = login.simulationDistance();
                loginReducedDebug = login.reducedDebugInfo();
                loginDeathScreen = login.showDeathScreen();
                CommonPlayerSpawnInfo spawn = login.commonPlayerSpawnInfo();
                BlockEntityOrder.newWorld(levelKey(spawn));
                if (spawn != null) {
                    loginSeed = spawn.seed();
                    loginPrevSurvival = spawn.previousGameType() == GameType.SURVIVAL;
                    loginSeaLevel = spawn.seaLevel();
                }
                emptyLevels = login.levels().isEmpty();
                for (net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> level : login.levels()) {
                    if (levelNames.size() < 16) levelNames.add(level.identifier().toString());
                }
                previousJoinPacket = packet.getClass().getSimpleName();
                return;
            }
            if (!loggedIn) {
                if (configFinished && packet instanceof ClientboundUpdateTagsPacket tags) {
                    countTagNamespaces(tags, playTagNamespaces);
                    if (playTagMap == null) playTagMap = tags.getTags();
                }
                if (configFinished && packet instanceof ClientboundTabListPacket) tabListBeforeLogin = true;
                if (configFinished && packet instanceof ClientboundServerLinksPacket links) {
                    serverLinksBeforeLogin = true;
                    for (ServerLinks.UntrustedEntry entry : links.links()) {
                        if (earlyLinks.size() >= 8) break;
                        String link = entry.link();
                        if (link != null) earlyLinks.add(link.length() > 256 ? link.substring(0, 256) : link);
                    }
                }
                return;
            }
            playPackets++;
            if (packet instanceof ClientboundUpdateAdvancementsPacket) advancementsSeen = true;
            if (packet instanceof ClientboundUpdateTagsPacket tags) {
                countTagNamespaces(tags, playTagNamespaces);
                if (playTagMap == null && now - loginAt <= 10000) playTagMap = tags.getTags();
            }
            if (packet instanceof ClientboundSetSimulationDistancePacket) simDistanceSeen = true;
            if (packet instanceof ClientboundPlayerPositionPacket teleport) {
                teleportCount++;
                if (teleport.id() != 0) teleportNonZero = true;
            }
            if (packet instanceof ClientboundSetHealthPacket health && health.getHealth() <= 0f && now - loginAt <= OXYGEN_WINDOW_MS) ownDiedEarly = true;
            if (packet instanceof ClientboundPlayerCombatKillPacket kill && kill.playerId() == ownEntityId && now - loginAt <= OXYGEN_WINDOW_MS) ownDiedEarly = true;
            if (packet instanceof ClientboundPlayerPositionPacket) {
                positionsSeen++;
                positionSeenAt = now;
            }
            if (packet instanceof ClientboundPlayerPositionPacket position && firstTeleportId == Integer.MIN_VALUE) {
                firstTeleportId = position.id();
                firstTeleportYRot = position.relatives() != null && position.relatives().contains(Relative.Y_ROT);
            }
            if (packet instanceof ClientboundPlayerAbilitiesPacket abilities && !abilitiesSeen) {
                abilitiesSeen = true;
                firstFlyingSpeed = abilities.getFlyingSpeed();
            }
            if (packet instanceof ClientboundChunkBatchFinishedPacket batch && batch.batchSize() == 0 && now - loginAt <= 3000) emptyChunkBatches++;
            if (selfTabActions.isEmpty() && selfTabChecks < 16 && selfProfile != null && packet instanceof ClientboundPlayerInfoUpdatePacket info
                    && info.actions().contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER)) {
                selfTabChecks++;
                for (ClientboundPlayerInfoUpdatePacket.Entry entry : info.entries()) {
                    if (!selfProfile.equals(entry.profileId())) continue;
                    for (ClientboundPlayerInfoUpdatePacket.Action action : info.actions()) selfTabActions.add(action.name());
                    break;
                }
            }
            if (playPackets == 1) {
                firstAfterLogin = packet instanceof ClientboundCustomPayloadPacket custom
                        ? custom.payload().type().id().toString()
                        : packet.getClass().getSimpleName();
            }
            if (packet instanceof ClientboundClearTitlesPacket && playPackets <= 3) clearTitlesAfterLogin = true;
            if (packet instanceof ClientboundServerDataPacket motdPacket) {
                if (serverDataCount == 0) firstMotd = motdPacket.motd();
                if (serverDataCount == 0 && !joinDone) {
                    serverDataAfterPosition = ClientboundPlayerPositionPacket.class.getSimpleName().equals(previousJoinPacket);
                    serverDataBorders = borderCount;
                }
                serverDataCount++;
            }
            if (packet instanceof ClientboundSetTimePacket time) onTime(time);
            if (packet instanceof ClientboundLevelChunkWithLightPacket chunk) {
                onChunkOrder(chunk.getX(), chunk.getZ(), now);
                BlockEntityOrder.capture(chunk.getX(), chunk.getZ(), output -> chunk.getChunkData().getBlockEntitiesTagsConsumer(chunk.getX(), chunk.getZ()).accept(output));
            } else if (packet instanceof ClientboundSetChunkCacheCenterPacket center) {
                centerX = center.getX();
                centerZ = center.getZ();
                centerKnown = true;
                prevChunkDistance = -1;
            } else if (packet instanceof ClientboundSetChunkCacheRadiusPacket radius) {
                prevChunkDistance = -1;
                maxChunkRadius = Math.max(maxChunkRadius, radius.getRadius());
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
            } else if (packet instanceof ClientboundRespawnPacket respawn) {
                loadedChunks.clear();
                liveEntities.clear();
                prevChunkDistance = -1;
                FreshFreeze.newWorld();
                BlockEntityOrder.newWorld(levelKey(respawn.commonPlayerSpawnInfo()));
                miscSpawns.removeIf(spawn -> spawn[6] == null);
                placeholderEpoch = ++positionsSeen;
                positionSeenAt = now;
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
                linkPassengers(passengers, now);
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
            joinSeq++;
            if (packet instanceof ClientboundUpdateAdvancementsPacket && advancementsAt < 0) advancementsAt = joinSeq;
            if (packet instanceof ClientboundSetEntityDataPacket meta && meta.id() == ownEntityId && ownMetaAt < 0) ownMetaAt = joinSeq;
            if (packet instanceof ClientboundCommandsPacket && bordersAtCommands < 0) bordersAtCommands = borderCount;
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

    private static void linkPassengers(ClientboundSetPassengersPacket packet, long now) {
        linkPassenger(packet.getVehicle(), now);
        int linked = 0;
        for (int p : packet.getPassengers()) {
            if (++linked > 64) break;
            linkPassenger(p, now);
        }
    }

    private static void linkPassenger(int id, long now) {
        long[] seen = passengerLinks.remove(id);
        passengerLinks.put(id, new long[]{seen == null ? now : seen[0], now});
        Iterator<Map.Entry<Integer, long[]>> it = passengerLinks.entrySet().iterator();
        while (it.hasNext()) {
            long last = it.next().getValue()[1];
            if (passengerLinks.size() <= TRACK_CAP && now - last <= 5 * PASSENGER_WINDOW_MS) break;
            it.remove();
        }
    }

    private static boolean vehicleType(String typeKey) {
        return typeKey.equals("minecart") || typeKey.endsWith("_minecart") || typeKey.endsWith("_boat") || typeKey.endsWith("_raft");
    }

    private static long chunkKey(int x, int z) {
        return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
    }

    private static String levelKey(CommonPlayerSpawnInfo spawn) {
        return spawn == null || spawn.dimension() == null ? "" : spawn.dimension().identifier().toString();
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
            trackedSpawns.add(new Object[]{typeKey, add.getX(), add.getZ(), positionsSeen});
        }
        boolean misc = TRACK_MISC.contains(typeKey);
        if ((misc || vehicleType(typeKey)) && miscSpawns.size() < TRACK_CAP) {
            miscSpawns.add(new Object[]{typeKey, add.getX(), add.getZ(), id, now, misc, null, positionsSeen});
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

    private static void countTagNamespaces(ClientboundUpdateTagsPacket packet, Map<String, Integer> into) {
        try {
            Map<String, Integer> counts = new HashMap<>();
            for (net.minecraft.tags.TagNetworkSerialization.NetworkPayload payload : packet.getTags().values()) {
                for (Identifier tag : ((cz.honzasik.hontun.mixin.network.software.TagPayloadAccessor) (Object) payload).hontun$tags().keySet()) {
                    String ns = tag.getNamespace();
                    if (counts.size() >= 64 && !counts.containsKey(ns)) continue;
                    counts.merge(ns, 1, Integer::sum);
                }
            }
            for (Map.Entry<String, Integer> e : counts.entrySet()) {
                if (into.size() >= 64 && !into.containsKey(e.getKey())) continue;
                into.merge(e.getKey(), e.getValue(), Math::max);
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
        if (id == ownEntityId && loginAt > 0 && now - loginAt <= OXYGEN_WINDOW_MS) {
            ownAttrPackets++;
            for (ClientboundUpdateAttributesPacket.AttributeSnapshot s : packet.getValues()) {
                if ("oxygen_bonus".equals(s.attribute().unwrapKey().map(k -> k.identifier().getPath()).orElse(""))) ownOxygen = true;
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
        boolean maxHealth = false, scale = false, oxygen = false;
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
            if (path.equals("oxygen_bonus")) oxygen = true;
            ids.add(reg);
        }
        if (snapshot && !player && maxHealth && scale) purpurAttrLists++;
        if (snapshot && id != ownEntityId && !player) {
            if (maxHealth) pairingWithMaxHealth++;
            else pairingWithoutMaxHealth++;
            if (scale) pairingWithScale++;
            if (oxygen) pairingWithOxygen++;
            String typeKey = pendingTypes.remove(id);
            if (typeKey != null && pairingTypes.size() < 32) pairingTypes.add(typeKey);
        }
        if (id == ownEntityId && snapshot && !ids.isEmpty() && ids.get(ids.size() - 1) == maxHealthId) ids.remove(ids.size() - 1);

        int n = ids.size();
        if (n < 2 || new HashSet<>(ids).size() != n) return;
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

    public static void onPositionApplied() {
        positionsApplied++;
    }

    public static void tick() {
        net.minecraft.client.player.LocalPlayer me = MCUtil.MC.player;
        if (me == null || me.isPassenger()) return;
        long now = System.currentTimeMillis();
        synchronized (JOIN) {
            if (positionsApplied > positionsSeen || (positionsApplied < positionsSeen && now - positionSeenAt > PASSENGER_WINDOW_MS)) {
                positionsApplied = positionsSeen;
            }
            double reach = maxChunkRadius > 0 ? (maxChunkRadius + 1) * 16 * 1.42 : Double.MAX_VALUE;
            while (!trackedSpawns.isEmpty()) {
                int epoch = (int) trackedSpawns.peek()[3];
                if (epoch > positionsApplied) break;
                Object[] spawn = trackedSpawns.poll();
                if (epoch < positionsApplied || epoch == placeholderEpoch) continue;
                double dx = (double) spawn[1] - me.getX(), dz = (double) spawn[2] - me.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > reach) continue;
                trackedMobs++;
                if (distance > farthestTracked) {
                    farthestTracked = distance;
                    farthestTrackedType = (String) spawn[0];
                }
            }
            Iterator<Object[]> fresh = miscSpawns.descendingIterator();
            while (fresh.hasNext()) {
                Object[] spawn = fresh.next();
                if (spawn[6] != null) break;
                int epoch = (int) spawn[7];
                if (epoch > positionsApplied) continue;
                double dx = (double) spawn[1] - me.getX(), dz = (double) spawn[2] - me.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);
                boolean stale = epoch < positionsApplied || epoch == placeholderEpoch || distance > reach;
                spawn[6] = stale || now - (long) spawn[4] > PASSENGER_WINDOW_MS ? -1.0 : distance;
            }
            while (!miscSpawns.isEmpty() && now - (long) miscSpawns.peek()[4] > PASSENGER_WINDOW_MS) {
                Object[] spawn = miscSpawns.poll();
                long at = (long) spawn[4];
                double distance = spawn[6] instanceof Double d ? d : -1.0;
                long[] link = passengerLinks.get((int) spawn[3]);
                boolean linked = link != null && link[0] <= at + PASSENGER_WINDOW_MS && link[1] >= at - PASSENGER_WINDOW_MS;
                if (distance < 0 || linked || now - at > 4 * PASSENGER_WINDOW_MS) continue;
                if ((boolean) spawn[5]) {
                    if (distance > farthestMisc) {
                        farthestMisc = distance;
                        farthestMiscType = (String) spawn[0];
                    }
                } else if (distance > farthestOther) {
                    farthestOther = distance;
                    farthestOtherType = (String) spawn[0];
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

    private static long[] firstKeepAliveId() {
        synchronized (keepAlives) { return keepAliveIds.isEmpty() ? null : keepAliveIds.get(0).clone(); }
    }

    public static RealVersion.Snapshot realVersionSnapshot() {
        RealVersion.Snapshot s = new RealVersion.Snapshot();
        s.clientProtocol = nativeProtocol();
        s.clientVersion = SharedConstants.getCurrentVersion().name();
        long now = System.currentTimeMillis();
        Map<net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<?>>, net.minecraft.tags.TagNetworkSerialization.NetworkPayload> config, play;
        boolean firstBackend;
        synchronized (JOIN) {
            firstBackend = configRounds <= 1 && configLogins <= 1;
            s.vvChannel = backendVvChannel;
            s.vvProtocol = backendVvVersion;
            s.vvName = backendVvName;
            s.corePack = coreVersion;
            s.knownPacksReceived = knownPacksReceived;
            s.knownPacksEmpty = knownPacksEmpty;
            s.tagsFirst = tagsBeforeRest;
            s.featuresSeen = featuresSeen;
            s.configFinished = configFinished;
            s.brandInConfig = brandInConfig;
            s.brandInPlay = brandFirstInPlay;
            for (Map.Entry<String, List<Identifier>> e : realRegistries.entrySet()) {
                List<String> ids = new ArrayList<>(e.getValue().size());
                for (Identifier id : e.getValue()) ids.add(id.toString());
                s.registries.put(e.getKey(), ids);
            }
            s.overworldMinY = overworldMinY;
            s.overworldHeight = overworldHeight;
            s.loggedIn = loggedIn;
            s.sinceLoginMs = loginAt > 0 ? now - loginAt : -1;
            s.loginView = loginView;
            s.loginSeed = loginSeed;
            s.seaLevel = loginSeaLevel;
            s.teleports = teleportCount;
            s.teleportNonZero = teleportNonZero;
            s.firstTabActions.addAll(firstTabActions);
            s.tickingState = tickingStates > 0;
            s.serverData = serverDataCount > 0;
            s.simulationDistance = simDistanceSeen;
            s.advancementsOrRecipes = advancementsSeen || recipeBookAt >= 0;
            config = configTagMap;
            play = playTagMap;
        }
        s.chunkBatches = chunkBatches.get() > 0;
        s.syntheticCommands = syntheticCommands;
        s.commandsSeen = commandsSeen;
        synchronized (keepAlives) {
            long[] ids = new long[keepAliveIds.size()];
            for (int i = 0; i < ids.length; i++) ids[i] = keepAliveIds.get(i)[0];
            s.keepAliveIds = ids;
        }
        s.playTags = play != null;
        if (config != null || play != null) s.tags = new RealVersionTags(config, play);
        try {
            if (VfpBridge.available()) {
                Object target = VfpBridge.current();
                if (target != null) {
                    s.vfpProtocol = VfpBridge.idOf(target);
                    s.vfpName = VfpBridge.nameOf(target);
                }
            }
        } catch (Throwable ignored) {
        }
        ServerData data = MCUtil.MC.getCurrentServer();
        if (firstBackend && data != null && data.version != null && data.state() == ServerData.State.SUCCESSFUL) s.pingName = strip(data.version.getString());
        return s;
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
        if (v.equals(SharedConstants.getCurrentVersion().name()) || v.equals(SharedConstants.getCurrentVersion().id())) return nativeProtocol();
        if (v.startsWith("26.1")) return PROTO_26_1;
        return switch (v) {
            case "26.3" -> 777;
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
        List<String> late, links;
        boolean brandLate, linksEarly, tabEarly, titlesEarly, emptyRegister, done, leavesProto, packsReceived, packsEmpty,
                features, configDone, noLevels, appDetails;
        Component motdFirst;
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
        double farthest, farMisc, farOther;
        String farthestType, farMiscType, farOtherType;
        int trackedCount;
        int advAt, metaAt, cmdBorders;
        boolean advSeen;
        int statsNonZero;
        JoinMessage jm;
        long timeSpan, doneAt;
        String regFirst, renamed;
        int mcEntries, mcWithData, biomes, view, sim, emptyBatches;
        boolean unsortedEntries, biomeNbt, padding, registrySeen, brandPlay, cfgKaEarly, rdi, deathScreen, prevSurvival,
                abilities, teleportYRot;
        long seed;
        float flySpeed;
        Set<String> selfTab;
        Map<String, Integer> tagNs;
        boolean featuresLate, ownOxy, ownDead;
        int ownAttrs, mobOxygen;
        synchronized (JOIN) {
            sweepResync(System.currentTimeMillis());
            tagNs = new HashMap<>(tagNamespaces);
            for (Map.Entry<String, Integer> e : playTagNamespaces.entrySet()) tagNs.merge(e.getKey(), e.getValue(), Math::max);
            featuresLate = featuresAfterTags;
            ownAttrs = ownAttrPackets;
            ownOxy = ownOxygen;
            ownDead = ownDiedEarly;
            mobOxygen = pairingWithOxygen;
            regFirst = firstRegistry;
            renamed = profileRenamed;
            mcEntries = mcRegistryEntries;
            mcWithData = mcRegistryEntriesWithData;
            biomes = biomeEntries;
            unsortedEntries = unsortedRegistryEntries;
            biomeNbt = serverOnlyBiomeNbt;
            padding = limboPadding;
            registrySeen = sawRegistry;
            brandPlay = brandFirstInPlay;
            cfgKaEarly = cfgKeepAliveBeforeRegistries;
            view = loginView;
            sim = loginSim;
            rdi = loginReducedDebug;
            deathScreen = loginDeathScreen;
            seed = loginSeed;
            prevSurvival = loginPrevSurvival;
            abilities = abilitiesSeen;
            flySpeed = firstFlyingSpeed;
            emptyBatches = emptyChunkBatches;
            selfTab = new TreeSet<>(selfTabActions);
            teleportYRot = firstTeleportYRot;
            brandFirst = firstBrand;
            brandLast = lastBrand;
            brandConfig = brandInConfig;
            queries = new LinkedHashSet<>(loginQueries);
            packs = new LinkedHashSet<>(knownPacks);
            preLogin = new LinkedHashSet<>(preLoginPayloads);
            channels = new LinkedHashSet<>(registerChannels);
            pings = new ArrayList<>(configPings);
            late = new ArrayList<>(lateRegistries);
            links = new ArrayList<>(earlyLinks);
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
            appDetails = appDetailsSeen;
            motdFirst = firstMotd;
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
            farMisc = farthestMisc;
            farMiscType = farthestMiscType;
            farOther = farthestOther;
            farOtherType = farthestOtherType;
            advAt = advancementsAt;
            advSeen = advancementsSeen;
            metaAt = ownMetaAt;
            cmdBorders = bordersAtCommands;
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
                } else if (backendBrand.endsWith(" (VeloFlame)")) {
                    proxy = appendProxy(proxy, "VeloFlame");
                    backendBrand = backendBrand.substring(0, backendBrand.length() - " (VeloFlame)".length()).trim();
                    ev.add(new Evidence("brand", "\"" + brand + "\" is VeloFlame's rewrite of the backend brand", Family.UNKNOWN, 0));
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
        boolean arclightBrand = arclightLoader != null;
        String brandLower = backendBrand == null ? "" : backendBrand.toLowerCase(Locale.ROOT);
        boolean sectionSuffix = rawBrand != null && rawBrand.endsWith("§r")
                && rawBrand.indexOf('§') == rawBrand.length() - 2;

        ServerData data = MCUtil.MC.getCurrentServer();
        String pingName = data != null && data.state() == ServerData.State.SUCCESSFUL && data.version != null ? data.version.getString() : null;
        if (pingName != null) {
            String lower = pingName.toLowerCase(Locale.ROOT);
            if (lower.startsWith("veloflame")) proxy = proxy == null ? "VeloFlame" : proxy;
            else if (lower.startsWith("velocity-ctd")) proxy = proxy == null ? "Velocity-CTD" : proxy;
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
        boolean vfRoots = roots.contains("veloflame");
        if (vfRoots) {
            if (proxy == null || proxy.equals("Velocity") || proxy.equals("Velocity-CTD")) proxy = "VeloFlame";
            else if (!proxy.contains("VeloFlame")) proxy = appendProxy(proxy, "VeloFlame");
            ev.add(new Evidence("commands", "/veloflame (alias /velo) is VeloFlame's own proxy command, offered to every player", Family.UNKNOWN, 0));
        }
        if (roots.contains("velocity:callback")) {
            proxy = proxy == null ? "Velocity" : proxy;
            ev.add(new Evidence("commands", "velocity:callback is registered by the Velocity proxy", Family.UNKNOWN, 0));
        }
        boolean velocityMarkers = tabEarly || linksEarly || titlesEarly;
        boolean bungeeRoots = roots.contains("bungee") || (roots.contains("glist") && !roots.contains("send"));
        if (bungeeRoots && !vfRoots && !velocityMarkers) {
            proxy = proxy == null ? "BungeeCord" : proxy;
            ev.add(new Evidence("commands", "bungee/glist commands come from the BungeeCord proxy", Family.UNKNOWN, 0));
        }
        if (proxy == null && roots.contains("glist") && roots.contains("send") && !roots.contains("bungee") && !vfRoots && !tabEarly && !linksEarly) {
            proxy = "Gate";
            ev.add(new Evidence("commands", "Gate's builtin server/glist/send commands are offered to everyone", Family.UNKNOWN, 0));
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
        if (appDetails) {
            if (proxy == null || !proxy.contains("ViaProxy")) proxy = appendProxy(proxy, "ViaProxy");
            ev.add(new Evidence("join", "vv:app_details is only sent by a standalone Via application (ViaProxy)", Family.UNKNOWN, 0));
        }
        if (proto != clientSide) {
            String who = "vv:proxy_details".equals(vvCh) ? "ViaVersion on the proxy"
                    : "vv:app_details".equals(vvCh) ? "ViaProxy" : "ViaVersion on the server";
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
        long fabricMods = packs.stream().filter(p -> p.startsWith("vanilla:") || p.startsWith("fabric:")).count();
        if (fabricMods > 0) {
            notes.add("Server data packs from " + fabricMods + " Fabric mod(s): " + packs.stream().filter(p -> p.startsWith("vanilla:") || p.startsWith("fabric:")).limit(8).toList());
        }
        if (configPhase && brandLate && features && packsReceived) {
            ev.add(new Evidence("config", "brand sent after the registries (Forge order)", Family.FORGE, 5));
        } else if (featuresLate && configPhase && clientSide == nativeProtocol() && late.isEmpty() && !(packsReceived && packsEmpty)) {
            ev.add(new Evidence("config", "feature flags sent after the tags (Forge order)", Family.FORGE, 3));
        }
        if (channels.stream().anyMatch(c -> c.startsWith("sponge:"))) {
            ev.add(new Evidence("channels", "sponge channels registered", Family.SPONGE, 2));
        }
        String arclightPackId = packs.stream().filter(p -> p.contains(":mod/arclight")).findFirst().orElse(null);
        boolean arclightPack = arclightPackId != null;
        if (arclightPack) {
            int colon = arclightPackId.indexOf(':');
            if (arclightLoader == null) arclightLoader = colon > 0 ? arclightPackId.substring(0, Math.min(colon, 32)) : "neoforge";
            ev.add(new Evidence("config", "known pack " + arclightPackId + " (Arclight data pack, brand not needed)", Family.MODDED_BUKKIT, 4));
        }
        int fmlChannels = 0;
        for (String ch : FML_LEGACY) if (channels.contains(ch)) fmlChannels++;
        if (fmlChannels >= 2) {
            ev.add(new Evidence("channels", "FML network channels registered (Forge 1.20.x or older)", Family.FORGE, 4));
        }
        boolean fabricChannels = channels.stream().anyMatch(ch -> ch.startsWith("fabric:") || ch.startsWith("fabric-"));
        if (fabricChannels) {
            ev.add(new Evidence("channels", "Fabric API channels registered", Family.FABRIC, configPhase ? 1 : 2));
        }
        boolean oxygenWindow = proto >= PROTO_1_21 && ownAttrs > 0 && joinedAt > 0 && System.currentTimeMillis() - joinedAt > OXYGEN_WINDOW_MS && !ownDead;
        boolean notFml = oxygenWindow && !ownOxy;
        if (notFml) {
            ev.add(new Evidence("join", "your attributes carry no oxygen_bonus (not Forge/NeoForge-based)", Family.UNKNOWN, 0));
            if (neoPreamble || neoPack != null) {
                notes.add("Conflicting signals: NeoForge networking, but your attributes carry no oxygen_bonus (every Forge/NeoForge 1.21+ server syncs it).");
            }
        }
        boolean fabricSide = fabricChannels || fabricPing || brandLower.contains("fabric") || brandLower.contains("quilt");
        int forgeTags = tagNs.getOrDefault("forge", 0);
        int neoTags = tagNs.getOrDefault("neoforge", 0);
        boolean forgeSide = brandLower.equals("forge") || (roots.contains("forge") && roots.contains("config"))
                || queries.stream().anyMatch(q -> q.startsWith("fml:") || q.startsWith("forge:"));
        boolean neoSide = brandLower.equals("neoforge") || roots.contains("neoforge");
        for (Evidence e : ev) {
            if (e.weight() <= 0) continue;
            if (e.family() == Family.FORGE) forgeSide = true;
            else if (e.family() == Family.NEOFORGE) neoSide = true;
        }
        boolean fmlSync = proto >= PROTO_1_21 && ownOxy;
        if (forgeTags >= 5 && !fabricSide) {
            ev.add(notFml ? new Evidence("tags", forgeTags + " forge: tags, but no Forge-style attribute sync (a data pack, not Forge)", Family.UNKNOWN, 0)
                    : neoSide ? new Evidence("tags", forgeTags + " forge: tags next to NeoForge signals (mod data, not Forge)", Family.UNKNOWN, 0)
                    : !forgeSide && !fmlSync ? new Evidence("tags", forgeTags + " forge: tags, but nothing else points to Forge (a data pack can ship them)", Family.UNKNOWN, 0)
                    : new Evidence("tags", forgeTags + " forge: tags (Forge)", Family.FORGE, 3));
        }
        if (neoTags >= 5 && !fabricSide) {
            ev.add(notFml ? new Evidence("tags", neoTags + " neoforge: tags, but no NeoForge-style attribute sync (a data pack, not NeoForge)", Family.UNKNOWN, 0)
                    : forgeSide ? new Evidence("tags", neoTags + " neoforge: tags next to Forge signals (mod data, not NeoForge)", Family.UNKNOWN, 0)
                    : !neoSide && !fmlSync ? new Evidence("tags", neoTags + " neoforge: tags, but nothing else points to NeoForge (a data pack can ship them)", Family.UNKNOWN, 0)
                    : new Evidence("tags", neoTags + " neoforge: tags (NeoForge)", Family.NEOFORGE, 3));
        }

        if (tabEarly) {
            proxy = proxy == null ? "Velocity" : proxy;
            ev.add(new Evidence("join", "tab list reset before the login packet (Velocity family)", Family.UNKNOWN, 0));
        } else if (titlesEarly && clientSide >= PROTO_1_20_2 && !linksEarly && !vfRoots) {
            proxy = proxy == null ? "Gate" : proxy;
            ev.add(new Evidence("join", "titles cleared right after login without a tab-list reset (Gate)", Family.UNKNOWN, 0));
        } else if (titlesEarly) {
            proxy = proxy == null ? "Velocity or Gate" : proxy;
            ev.add(new Evidence("join", "titles cleared right after login (Velocity family or Gate; this client version cannot tell them apart)", Family.UNKNOWN, 0));
        }
        if (linksEarly) {
            if (proxy == null || "Velocity".equals(proxy) || "Velocity or Gate".equals(proxy)) proxy = "Velocity-CTD";
            ev.add(new Evidence("join", "server links sent before the login packet (Velocity-CTD or a CTD fork such as VeloFlame)", Family.UNKNOWN, 0));
        }
        if (sectionSuffix && tabEarly && !linksEarly && clientSide >= PROTO_1_20_2 && clientSide < PROTO_1_21 && "Velocity".equals(proxy)) {
            proxy = "Velocity-CTD";
            ev.add(new Evidence("brand", "brand rewritten with a trailing §r (Velocity-CTD family)", Family.UNKNOWN, 0));
        }
        boolean vfLinks = links.stream().anyMatch(l -> l.toLowerCase(Locale.ROOT).contains("veloflame.com"));
        boolean ctdLinks = links.stream().anyMatch(l -> l.toLowerCase(Locale.ROOT).contains("github.com/gemstonegg/velocity-ctd"));
        if (vfLinks) {
            if ("Velocity-CTD".equals(proxy)) proxy = "VeloFlame";
            ev.add(new Evidence("join", "default VeloFlame server links (veloflame.com)", Family.UNKNOWN, 0));
        }
        if (ctdLinks) {
            ev.add(new Evidence("join", "default Velocity-CTD server links (GemstoneGG/Velocity-CTD on GitHub)", Family.UNKNOWN, 0));
        }
        String motd = motdFirst == null ? null : strip(motdFirst.getString());
        if ("A VeloFlame Server".equals(motd)) {
            if ((linksEarly || tabEarly) && ("Velocity-CTD".equals(proxy) || "Velocity".equals(proxy))) proxy = "VeloFlame";
            ev.add(new Evidence("join", "default VeloFlame MOTD in the server-data packet", Family.UNKNOWN, 0));
        } else if ("A Velocity-CTD Server".equals(motd)) {
            ev.add(new Evidence("join", "default Velocity-CTD MOTD in the server-data packet", Family.UNKNOWN, 0));
        } else if ("A Velocity Server".equals(motd)) {
            ev.add(new Evidence("join", "default Velocity MOTD in the server-data packet", Family.UNKNOWN, 0));
        }
        boolean v7Sample = data != null && Boolean.TRUE.equals(sampleV7.get(data));
        if (v7Sample) {
            if (("Velocity".equals(proxy) || "Velocity or Gate".equals(proxy)) && !linksEarly && clientSide < PROTO_1_21) proxy = "Velocity-CTD";
            ev.add(new Evidence("ping", "server-list sample uses UUIDv7 ids (Velocity-CTD family)", Family.UNKNOWN, 0));
        }
        boolean limbo = configDone && !features && !(packsReceived && packsEmpty);
        if ("minecraft:register".equals(firstPlay) && channels.contains("bungeecord:main")) {
            proxy = proxy == null ? "BungeeCord" : proxy;
            ev.add(new Evidence("join", "bungeecord:main registered right after login (BungeeCord)", Family.UNKNOWN, 0));
        }
        boolean arclightPing = false;
        String pingHybrid = null;
        if (pingName != null && proxy == null) {
            int arclight = pingName.indexOf(" arclight/");
            String loader = arclight > 0 ? pingName.substring(0, arclight).trim() : "";
            if (arclightLoader == null && !loader.isEmpty()) {
                arclightLoader = loader.length() > 32 ? loader.substring(0, 32) : loader;
                arclightPing = true;
            }
            String lower = pingName.toLowerCase(Locale.ROOT);
            pingHybrid = lower.startsWith("youer ") ? "Youer" : lower.startsWith("magma ") ? "Magma" : lower.startsWith("mohist ") ? "Mohist" : null;
        }
        Integer pingFlags = data != null && data.state() == ServerData.State.SUCCESSFUL ? statusFlags.get(data) : null;
        if (pingFlags != null && proxy == null) {
            if ((pingFlags & STATUS_FORGE_DATA) != 0) ev.add(new Evidence("ping", "server list advertises forgeData (Forge)", Family.FORGE, 2));
            if ((pingFlags & STATUS_MODDED) != 0) ev.add(new Evidence("ping", "server list says isModded (NeoForge)", Family.NEOFORGE, 2));
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
        boolean paperOrder = configPhase && done && border >= 2 && viewDist;
        boolean shreddedTick = paperOrder && advAt >= 0 && metaAt >= 0 && metaAt > advAt;
        boolean shreddedCmd = paperOrder && cmdBorders == 1;
        if (shreddedTick) {
            ev.add(new Evidence("join", "your entity data came after the advancements (ShreddedPaper ticks players before the entity tracker)", Family.PAPER, 1));
        }
        if (shreddedCmd) {
            ev.add(new Evidence("join", "commands arrived between the two world-info blocks (ShreddedPaper places players off the main thread)", Family.PAPER, 0));
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
        boolean universeChunks = proto == PROTO_1_21_11 && !batched && (descents >= 2 || earlyEntities >= 2);
        if (universeChunks) {
            ev.add(new Evidence("chunks", (descents >= 2 ? descents + " chunks arrived out of distance order" : earlyEntities + " entities arrived before their chunk")
                    + " (UniverseSpigot sends chunks from parallel threads)", Family.PAPER, 0));
        } else if (proto == PROTO_1_21_11 && !batched && fresh >= 100 && descents == 0) {
            ev.add(new Evidence("chunks", fresh + " chunks arrived strictly by distance (no UniverseSpigot async chunk sending)", Family.UNKNOWN, 0));
        }
        BlockEntityOrder.Stats blockEntities = BlockEntityOrder.stats();
        boolean leafHash = proto >= PROTO_1_21_11 && blockEntities.leafHash();
        boolean vanillaHash = proto >= PROTO_1_21_11 && blockEntities.vanillaHash();
        boolean mojangHash = vanillaHash && proto >= PROTO_26_2;
        boolean canvasSorted = proto >= PROTO_26_1 && blockEntities.sortedYzx();
        if (leafHash) {
            ev.add(new Evidence("chunks", "block entities in chunk packets use Leaf's BlockPos hash (Leaf/UniverseSpigot family)", Family.PAPER, 2));
        } else if (vanillaHash) {
            ev.add(new Evidence("chunks", "block entities in chunk packets use Mojang's BlockPos hash (not a recent Leaf/UniverseSpigot build)", Family.UNKNOWN, 0));
        }
        if (canvasSorted) {
            ev.add(new Evidence("chunks", "block entities listed in section order (Canvas block-entity storage)", Family.PAPER, 0));
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
        boolean nativeTags = !(packsReceived && packsEmpty) && clientSide == nativeProtocol();
        boolean emptyBlockTags = nativeTags && blockTags != null && blockTags[0] > 0 && blockTags[1] == 0;
        boolean trimmedTags = nativeTags && !tagInfo.isEmpty() && tagInfo.size() <= 6 && blockTags != null && blockTags[0] < 150 && blockTags[1] > 0
                && (itemTags == null || itemTags[0] < 60);
        boolean tagsForEmptyRegistries = false;
        if (nativeTags) for (int[] t : tagInfo.values()) if (t[0] == 0) tagsForEmptyRegistries = true;
        boolean noVanillaLevel = !levels.isEmpty() && levels.stream().noneMatch(VANILLA_LEVELS::contains);
        boolean noBrand = brandFirst == null && done && System.currentTimeMillis() - doneAt > 1500;
        String tabShape = tabActions.isEmpty() ? null : String.join(",", tabActions);
        boolean picoTab = "ADD_PLAYER,UPDATE_LISTED".equals(tabShape);
        boolean nanoTab = "ADD_PLAYER,UPDATE_GAME_MODE,UPDATE_LISTED".equals(tabShape);
        boolean loohpTab = "ADD_PLAYER,UPDATE_DISPLAY_NAME,UPDATE_GAME_MODE,UPDATE_LATENCY,UPDATE_LISTED".equals(tabShape);
        boolean oldBackend = packsReceived && packsEmpty;
        boolean nativeClient = clientSide == nativeProtocol();
        boolean tpGate = !oldBackend && clientSide >= 107;
        boolean picoTeleport = tpGate && teleportId == 0;
        if (picoTeleport) {
            ev.add(new Evidence("join", "your first teleport id is 0 (Mojang's server code starts at 1, PicoLimbo never counts)", Family.CUSTOM, 3));
        }
        boolean regOrder = configDone && regFirst != null && !regFirst.equals("minecraft:worldgen/biome") && !oldBackend && clientSide >= 766;
        if (regOrder) {
            ev.add(new Evidence("config", "registries not in Mojang's order (first: " + regFirst + ")", Family.CUSTOM, 3));
        }
        boolean coreAccepted = nativeClient && core != null && core.equals(SharedConstants.getCurrentVersion().id());
        if (unsortedEntries && nativeClient && !oldBackend && (!packsReceived || coreAccepted)) {
            ev.add(new Evidence("registry", "minecraft registry entries out of alphabetical order (not loaded from Mojang's data pack)", Family.CUSTOM, 2));
        }
        boolean fullData = coreAccepted && mcEntries >= 100 && (long) mcWithData * 10 >= (long) mcEntries * 9;
        if (fullData) {
            ev.add(new Evidence("config", "registry data sent in full although the client knows the vanilla pack (" + mcWithData + "/" + mcEntries + " entries)", Family.CUSTOM, 3));
        }
        boolean noPacks = configDone && !packsReceived && registrySeen && nativeClient;
        if (noPacks) {
            ev.add(new Evidence("config", "no known-packs step before the registries", Family.CUSTOM, 3));
        }
        boolean biomeServerNbt = biomeNbt && !oldBackend;
        if (biomeServerNbt) {
            ev.add(new Evidence("registry", "biome data carries the server-only carvers/features fields (LOOHP Limbo, SteelMC or Pumpkin)", Family.CUSTOM, 3));
        }
        boolean rawChat = chatTypes.contains("minecraft:raw") && packsReceived && !packsEmpty && nativeClient
                && !namespaces.contains("bukkit") && !roots.contains("icanhasbukkit");
        if (rawChat) {
            ev.add(new Evidence("registry", "chat_type contains minecraft:raw (Pumpkin, Paper uses paper:raw)", Family.CUSTOM, 5));
        }
        boolean biomeSingle = biomes == 1;
        if (biomeSingle) {
            boolean dimFirst = "minecraft:dimension_type".equals(regFirst);
            ev.add(new Evidence("registry", "the biome registry holds a single entry" + (dimFirst ? " and dimension_type comes first" : "") + " (QuasarMC)", Family.CUSTOM, dimFirst ? 4 : 3));
        }
        boolean nanoLogin = !oldBackend && joinedAt > 0 && view == 0 && rdi && seed == 0L && sim > 0;
        boolean nanoAbilities = !oldBackend && abilities && flySpeed == 0f;
        if (nanoLogin || nanoAbilities) {
            String shape = nanoLogin && nanoAbilities ? "view distance 0, reduced debug info, hashed seed 0 and flying speed 0 at login"
                    : nanoLogin ? "view distance 0, reduced debug info and hashed seed 0 in the login packet" : "flying speed 0 in your first abilities packet";
            ev.add(new Evidence("login", shape + " (NanoLimbo)", Family.CUSTOM, nanoLogin && nanoAbilities ? 4 : 3));
        }
        boolean sonarLogin = !oldBackend && joinedAt > 0 && maxP == 1 && view == 0 && sim == 0 && rdi && !deathScreen;
        if (sonarLogin) {
            ev.add(new Evidence("login", "max players 1, view and simulation distance 0, reduced debug info, no death screen (Sonar verification)", Family.CUSTOM, 3));
            if (seed != 0L && (seed & ~0x539L) == 0) ev.add(new Evidence("login", "hashed seed " + seed + " fits Sonar's 0x539 mask", Family.UNKNOWN, 0));
        }
        if (!nanoLogin && !oldBackend && joinedAt > 0 && seed == 0L && clientSide >= 573) {
            ev.add(new Evidence("login", "hashed seed 0 in the login packet (Mojang sends a hash of the world seed)", Family.CUSTOM, 1));
        }
        if (renamed != null) {
            ev.add(new Evidence("login", "the login returned the profile \"" + renamed + "\" instead of your name (NanoLimbo, Sonar)", Family.CUSTOM, 2));
        }
        boolean sonarLimbo = sonarLogin || "Sonar".equals(renamed);
        if (cfgKaEarly && !oldBackend) {
            ev.add(new Evidence("keep-alive", "keep-alive sent in the configuration phase before the registries", Family.CUSTOM, 1));
        }
        long[] firstKaId = firstKeepAliveId();
        if (firstKaId != null && !oldBackend && clientSide >= 340) {
            long kaId = firstKaId[0];
            if (kaId > firstKaId[1] - DAY_MS && kaId < firstKaId[1] + DAY_MS) {
                ev.add(new Evidence("keep-alive", "first keep-alive id is the wall clock (LOOHP Limbo, QuasarMC, SteelMC)", Family.CUSTOM, 2));
            } else if (kaId < 0 || kaId >= 1_000_000_000_000L) {
                ev.add(new Evidence("keep-alive", "first keep-alive id " + kaId + " is not an uptime clock (not Mojang's server code)", Family.CUSTOM, 1));
            }
        }
        boolean loohpBatches = !oldBackend && emptyBatches >= 3;
        if (loohpBatches) {
            ev.add(new Evidence("chunks", emptyBatches + " empty chunk batches in the first 3 s (LOOHP Limbo)", Family.CUSTOM, 3));
        }
        if (brandPlay && !brandConfig && !oldBackend && nativeClient) {
            ev.add(new Evidence("config", "brand sent only after the login packet, never in the configuration (LOOHP Limbo, LimboAPI)", Family.CUSTOM, 2));
        }
        boolean pumpkinTab = selfTab.equals(PUMPKIN_TAB);
        boolean limboApiTab = selfTab.equals(LIMBOAPI_TAB);
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
        } else if (!selfTab.isEmpty() && !selfTab.contains("INITIALIZE_CHAT") && !oldBackend) {
            ev.add(new Evidence("join", "your own tab-list entry came without initialize_chat ("
                    + (pumpkinTab ? "Pumpkin's tab flags" : limboApiTab ? "LimboAPI's tab flags" : "Mojang's server code always sends it") + ")", Family.CUSTOM, 1));
        }
        if (limbo) {
            ev.add(new Evidence("config", "no feature flags in the configuration phase (limbo or from-scratch server)", Family.CUSTOM, 4));
        }
        boolean tp44 = tpGate && teleportId == 44;
        boolean limboApi = tp44 || padding;
        if (limboApi) {
            String why = padding && tp44 ? "registry entries named limboapi_padding_N and first teleport id 44 (LimboAPI)"
                    : padding ? "registry entries named limboapi_padding_N (LimboAPI fills registry gaps)" : "your first teleport id is 44 (LimboAPI hardcodes it)";
            ev.add(new Evidence(padding ? "registry" : "join", why, Family.CUSTOM, 5));
            if (maxP == 1 && prevSurvival) ev.add(new Evidence("login", "max players 1 and previous game mode survival in the login packet (LimboAPI)", Family.UNKNOWN, 0));
        }
        boolean nanoTeleport = tpGate && teleportYRot && teleportId != 0 && teleportId != 44;
        boolean minestomLogin = noLevels && maxP == 0;
        boolean steelLike = biomeServerNbt && packsReceived && !features && !rawChat;
        String limboKind = limboApi || limboApiTab ? "LimboAPI"
                : rawChat || pumpkinTab ? "Pumpkin"
                : emptyBlockTags || picoTeleport ? "PicoLimbo"
                : nanoLogin || nanoAbilities || nanoTeleport || nanoTab || "NanoLimbo".equals(renamed) ? "NanoLimbo"
                : loohpBatches || (biomeServerNbt && !packsReceived) || noVanillaLevel || loohpTab || done && maxP < 0 ? "LOOHP Limbo"
                : steelLike ? "SteelMC"
                : biomeSingle || trimmedTags && noBrand ? "QuasarMC"
                : minestomLogin || "nanos".equals(kaIds) || tagsForEmptyRegistries ? "Minestom"
                : null;
        String byBrand = brandLower.contains("nanolimbo") ? "NanoLimbo"
                : brandLower.contains("picolimbo") ? "PicoLimbo"
                : brandLower.contains("minestom") ? "Minestom"
                : brandLower.contains("quasar") ? "QuasarMC"
                : brandLower.equals("limbo") ? "LOOHP Limbo"
                : brandLower.equals("pumpkin") ? "Pumpkin"
                : brandLower.equals("steel") ? "SteelMC"
                : brandLower.startsWith("ferrumc") ? "FerrumC"
                : brandLower.startsWith("limboapi (") ? "LimboAPI"
                : null;
        boolean vfWorld = !packsReceived && maxP == 1 && levels.size() == 1 && "minecraft:overworld".equals(levels.get(0));
        boolean vfPing = pingName != null && pingName.toLowerCase(Locale.ROOT).startsWith("veloflame") && vfWorld && limboKind == null && byBrand == null;
        boolean veloflameLimbo = limbo && !limboApi && !sonarLimbo && ("veloflame".equals(brandLower) || vfPing);
        if (veloflameLimbo) {
            if (maxP == 1) ev.add(new Evidence("login", "max players 1 in the login packet (VeloFlame verification world)", Family.UNKNOWN, 0));
            if (levels.size() == 1 && "minecraft:overworld".equals(levels.get(0))) {
                ev.add(new Evidence("login", "only minecraft:overworld in the login packet (VeloFlame verification world)", Family.UNKNOWN, 0));
            }
            if (teleportId != Integer.MIN_VALUE && teleportId != 0 && teleportId != 1) {
                ev.add(new Evidence("join", "first teleport id " + teleportId + " is random, not a counter (VeloFlame verification world)", Family.UNKNOWN, 0));
            }
        }
        if (nanoTeleport) {
            ev.add(new Evidence("join", "your first teleport keeps the yaw relative (NanoLimbo)", Family.CUSTOM, 3));
        } else if (tpGate && !veloflameLimbo && teleportId != Integer.MIN_VALUE && teleportId != 0 && teleportId != 1 && teleportId != 44) {
            ev.add(new Evidence("join", "first teleport id " + teleportId + " (Mojang's server code starts at 1)", Family.CUSTOM, 2));
        }
        if ("random".equals(kaIds)) {
            if (!oldBackend && clientSide >= 340) ev.add(new Evidence("keep-alive", "keep-alive ids are random, not a clock (not Mojang's server code)", Family.CUSTOM, 2));
        } else if ("nanos".equals(kaIds)) {
            ev.add(new Evidence("keep-alive", "keep-alive ids count nanoseconds (Minestom)", Family.CUSTOM, 2));
        }
        if (Boolean.TRUE.equals(zeroSess)) {
            ev.add(new Evidence("login", "all-zero session id at login (Minestom-style)", Family.CUSTOM, 1));
        }
        if (minestomLogin) {
            ev.add(new Evidence("login", "empty dimension list and 0 max players in the login packet (Minestom hardcodes both)", Family.CUSTOM, 3));
        } else if (noLevels || maxP == 0) {
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
        boolean wrappedNeo = roots.contains("minecraft:neoforge");
        boolean loaderWrapped = bukkitCmds && !chatTypes.contains("paper:raw") && (wrappedNeo || roots.contains("minecraft:forge"));
        if (loaderWrapped) {
            ev.add(new Evidence("commands", "Bukkit wraps the mod loader's own command (" + (wrappedNeo ? "minecraft:neoforge" : "minecraft:forge") + ")", Family.MODDED_BUKKIT, 3));
            ev.add(wrappedNeo ? new Evidence("commands", "the wrapped loader command is NeoForge's /neoforge", Family.NEOFORGE, 2)
                    : new Evidence("commands", "the wrapped loader command is Forge's /forge", Family.FORGE, 2));
        }
        int rootForge = 0, rootNeo = 0;
        if (roots.contains("forge") && roots.contains("config") && twinFree(roots, "forge")) {
            if (notFml) {
                ev.add(new Evidence("commands", "forge and config commands, but no Forge-style attribute sync (not Forge)", Family.UNKNOWN, 0));
            } else {
                rootForge = 2;
                ev.add(new Evidence("commands", "forge and config commands without a plugin twin (Forge)", Family.FORGE, rootForge));
            }
        }
        if (roots.contains("neoforge") && twinFree(roots, "neoforge")) {
            if (notFml) {
                ev.add(new Evidence("commands", "neoforge command, but no NeoForge-style attribute sync (not NeoForge)", Family.UNKNOWN, 0));
            } else {
                rootNeo = 1;
                ev.add(new Evidence("commands", "neoforge command without a plugin twin (NeoForge)", Family.NEOFORGE, rootNeo));
            }
        }
        boolean paperApi = chatTypes.contains("paper:raw") || namespaces.contains("paper") || namespaces.contains("purpur")
                || namespaces.contains("folia") || bukkitCmds;
        boolean shredded = roots.contains("shreddedpaper:mpmap") || namespaces.contains("shreddedpaper")
                || (roots.contains("mpmap") && !namespaces.contains("multipaper") && !roots.contains("mpdebug"));
        if (shredded && paperApi) ev.add(new Evidence("commands", "mpmap command (ShreddedPaper)", Family.PAPER, 3));
        if (namespaces.contains("multipaper") || (roots.contains("mpdebug") && roots.contains("mpmap"))) {
            notes.add("MultiPaper commands are offered (multipaper: namespace or mpdebug + mpmap), so this looks like MultiPaper, not ShreddedPaper.");
        }
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

        if (arclightBrand) ev.add(new Evidence("brand", backendBrand + " (Arclight)", Family.MODDED_BUKKIT, 3));
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
        boolean noAdvancements = configPhase && done && System.currentTimeMillis() - doneAt > 3000 && !advSeen && !limbo && custom < 4;
        if (noAdvancements) {
            ev.add(new Evidence("join", "no advancements packet after joining (advancements or criterion triggers disabled)", Family.UNKNOWN, 0));
        }
        int mobLists = mhWith + mhWithout;
        if (proto >= PROTO_1_21 && mobLists >= 8) {
            if (mhTypes >= 2 && mobOxygen * 10 >= mobLists * 6 && ownOxy) {
                String share = mobOxygen + "/" + mobLists + " mobs arrived with oxygen_bonus (Forge/NeoForge sync it for every living entity)";
                if (neo > 0) {
                    ev.add(new Evidence("entities", share, Family.NEOFORGE, 2));
                    neo += 2;
                } else if (forge > 0) {
                    ev.add(new Evidence("entities", share, Family.FORGE, 2));
                    forge += 2;
                } else {
                    notes.add("Mobs carry FML-style oxygen_bonus (" + mobOxygen + "/" + mobLists + "), but nothing else points to Forge or NeoForge.");
                }
            } else if (mobOxygen * 10 <= mobLists) {
                ev.add(new Evidence("entities", (mobLists - mobOxygen) + "/" + mobLists + " mobs arrived without oxygen_bonus (not Forge/NeoForge-based)", Family.UNKNOWN, 0));
            }
        }
        boolean forgeHybrid = forge - rootForge >= 4 && bukkitCmds && !chatTypes.contains("paper:raw");
        boolean timedHybrid = (neo - rootNeo >= 4 || fabricPing) && bukkitCmds && paper < 3 && !chatTypes.contains("paper:raw")
                && (jm == JoinMessage.LITERAL || (ka >= 20000 && ka <= 30000));
        boolean moddedBukkit = arclightLoader != null || arclightPack || loaderWrapped || forgeHybrid || timedHybrid;

        Family family;
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
                    || (family == Family.MODDED_BUKKIT && (e.family() == Family.NEOFORGE || e.family() == Family.FORGE || e.family() == Family.FABRIC || e.family() == Family.BUKKIT))
                    || (MODDED_VANILLA.contains(family) && e.family() == Family.VANILLA)) {
                structural += e.weight();
            }
        }
        String confidence = structural >= 6 ? "high" : structural >= 3 ? "medium" : "low";

        String software = name(family, backendBrand, ev, notes, clientSide != nativeProtocol() || !configPhase);
        if (family == Family.BUKKIT) {
            String known = knownName(backendBrand);
            int radius = MCUtil.MC.options == null ? view : Math.min(view, MCUtil.MC.options.renderDistance().get());
            boolean miscBeyond = radius >= 3 && farMisc > SPIGOT_MISC_LIMIT;
            boolean otherBeyond = radius >= 5 && farOther > SPIGOT_OTHER_LIMIT;
            boolean beyondSpigotMisc = miscBeyond || otherBeyond;
            if (miscBeyond) {
                ev.add(new Evidence("entities", "a " + farMiscType + " was sent " + Math.round(farMisc) + " blocks away (beyond Spigot's misc 32 / other 64 default)", Family.UNKNOWN, 0));
            }
            if (otherBeyond) {
                ev.add(new Evidence("entities", "a " + farOtherType + " was sent " + Math.round(farOther) + " blocks away (beyond Spigot's misc 32 / other 64 default)", Family.UNKNOWN, 0));
            }
            boolean spigotProven = false;
            if (respawnRadius > 0 && !beyondSpigot) {
                software = "Spigot";
                spigotProven = true;
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
            } else if (spigotRange) {
                if ("CraftBukkit".equals(known)) {
                    notes.add("Brand says CraftBukkit, but mobs stop at Spigot's 48-block tracking range (Spigot with a changed brand?).");
                } else {
                    if (!"Spigot".equals(known)) software = "Spigot";
                    spigotProven = true;
                }
            } else if (beyondSpigotMisc) {
                if ("Spigot".equals(known)) {
                    notes.add("Brand says Spigot, but misc/vehicle entities are sent beyond Spigot's default 32/64 blocks (raised entity-tracking-range in spigot.yml, or CraftBukkit).");
                } else {
                    software = "CraftBukkit";
                    notes.add("CraftBukkit: misc/vehicle entities sent beyond Spigot's default 32/64 blocks (a " + (miscBeyond ? farMiscType : farOtherType)
                            + " " + Math.round(miscBeyond ? farMisc : farOther) + " blocks away).");
                }
            }
            if (spigotProven && beyondSpigotMisc) {
                notes.add("Misc/vehicle entities are sent beyond Spigot's default 32/64 blocks, so its entity-tracking-range in spigot.yml was raised.");
            }
        } else if (family == Family.PAPER && beyondPaper) {
            notes.add("Mobs are sent from beyond Paper's default 96 blocks, so its entity-tracking-range was raised.");
        }

        if (family == Family.PAPER) {
            String known = knownName(backendBrand);
            String fork = null;
            String why = null;
            boolean conflict = false;
            List<String> universeReasons = new ArrayList<>();
            if (universeSpeed) universeReasons.add("exact Speed effect modifier");
            if (universeRain) universeReasons.add("rain without the start/stop event");
            if (universeFreeze) universeReasons.add("fresh mobs past 64 blocks frozen, odd ids never move");
            if (universeChunks) universeReasons.add("chunks sent out of order by parallel threads");
            if (universeEquipment) universeReasons.add("mob equipment sent twice by the async tracker");
            if (universeTab) universeReasons.add("players unlisted from the tab list");
            String statsWhy = "time statistics in steps of 20";
            boolean universeAgainst = purpurFamily || hashMap || rainWithFlip > 0 || floatSpeed > 0 || (statsOffGrid && playTimeMoved);
            String leafWhy = "block entities hashed with Leaf's BlockPos hash";
            if (shredded) { fork = "ShreddedPaper"; why = "mpmap command"; }
            else if (shreddedTick && (shreddedCmd || purpurFamily || "ShreddedPaper".equals(known))) { fork = "ShreddedPaper"; why = "join tick order"; }
            else if (canvas || canvasSorted) {
                fork = "Canvas";
                why = canvas && canvasSorted ? "ticking state without ticking step, block entities in section order"
                        : canvas ? "ticking state without ticking step" : "block entities in section order";
            }
            else if (leaves) { fork = "Leaves"; why = "bladeren protocol"; }
            else if (divine) { fork = "DivineMC"; why = "DivineMC-only packets"; }
            else if (purpurTime) { fork = "Purpur"; why = "per-tick time packets"; }
            else if (leafHash && (proto >= PROTO_26_1 || purpurFamily)) {
                fork = "Leaf";
                why = purpurFamily ? leafWhy + ", Purpur mob attributes" : leafWhy;
            } else if (leafHash) {
                List<String> reasons = new ArrayList<>(universeReasons);
                if (statsStep20 && !"Gale".equals(known) && !"Leaf".equals(known)) reasons.add(statsWhy);
                if (mhWithout >= 2) reasons.add("mobs without Purpur's max_health attribute");
                if (!reasons.isEmpty() && universeAgainst) conflict = true;
                if (!reasons.isEmpty() && !universeAgainst) {
                    if (noAdvancements) reasons.add("criterion triggers disabled (UniverseSpigot recommended config)");
                    reasons.add(0, leafWhy);
                    fork = "UniverseSpigot";
                    why = String.join(", ", reasons);
                } else {
                    fork = "Leaf".equals(known) || "UniverseSpigot".equals(known) ? known : "Leaf or UniverseSpigot";
                    why = leafWhy + ", shared by Leaf and UniverseSpigot";
                }
            } else if (purpurFamily && !hashMap && !mojangHash && (proto >= PROTO_26_1 ? seconds >= 3 : arrayMap || (ownListArray && "Leaf".equals(known)))) {
                fork = "Leaf";
                why = proto >= PROTO_26_1 ? "Purpur mob attributes without Purpur's time packets" : "Purpur mob attributes in an array attribute map";
            } else if (purpurFamily && hashMap && proto < PROTO_26_1) {
                fork = "Purpur";
                why = "Purpur mob attributes in a hash attribute map";
            } else if (!universeReasons.isEmpty() || (statsStep20 && !"Gale".equals(known))) {
                if (universeAgainst) conflict = true;
                else {
                    fork = "UniverseSpigot";
                    List<String> reasons = new ArrayList<>(universeReasons);
                    if (statsStep20) reasons.add(statsWhy);
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

            if (shreddedTick && !"ShreddedPaper".equals(fork)) {
                notes.add("Join tick order matches ShreddedPaper, but nothing else confirms it, so the name is not changed.");
            }
            if (conflict) {
                notes.add("Conflicting UniverseSpigot signals: the Speed modifier or the weather says UniverseSpigot, other packets do not (a per-player weather plugin can cause this).");
            }
            boolean leafSpoof = "Leaf".equals(known) && mojangHash;
            if (leafSpoof) {
                notes.add("Brand says Leaf, but block entities use Mojang's BlockPos hash (every Leaf build for 26.2 and newer uses its own, brand likely spoofed).");
            } else if (vanillaHash && ("Leaf".equals(known) || "UniverseSpigot".equals(known))) {
                notes.add("Brand says " + known + ", but block entities use Mojang's BlockPos hash (an older build from before January 2026, or not " + known + ").");
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
                } else if (fork.contains(" or ")) {
                    notes.add("The packets point to " + fork + " (" + why + "), nothing seen so far tells the two apart.");
                } else {
                    notes.add(fork + " confirmed by the packets (" + why + ").");
                }
                software = fork;
            } else if (purpurFamily) {
                if (known != null && (known.equals("Purpur") || "Purpur".equals(LINEAGE.get(known)))) {
                    notes.add(mojangHash ? "Purpur-family mobs, and block entities use Mojang's BlockPos hash, so this is not Leaf."
                            : "Purpur-family mobs. Telling Purpur from Leaf needs more attribute kinds in view (horses, other players, or a death and respawn).");
                } else {
                    software = "Purpur-based";
                    notes.add("Purpur-family mob attributes" + (known != null ? ", although the brand says " + known : "") + ".");
                }
            } else if (known != null && !known.equals("Paper")) {
                if (known.equals("Leaf") && hashMap) {
                    notes.add("Brand says Leaf, but the mob attributes come from a hash map (not Leaf, brand likely spoofed).");
                    software = "Paper-based";
                } else if (leafSpoof) {
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
                    notes.add("No packet signal for " + known + " yet, the fork name comes from the brand" + (pingAgrees(pingName, known) && !tabEarly && !linksEarly ? " and the server list name." : "."));
                }
            }
        } else if (family == Family.HYBRID) {
            String named = arclightLoader != null ? "Arclight (" + arclightLoader + ")" : hybridName(brandLower);
            boolean namedByPing = named == null && pingHybrid != null;
            if (namedByPing) named = pingHybrid;
            if (named == null && purpurTime) {
                software = "Youer (NeoForge + Purpur)";
                notes.add("NeoForge negotiation with Purpur's per-tick time packets; Youer is the only known NeoForge + Purpur hybrid.");
            } else {
                software = named != null ? named : "NeoForge + Paper hybrid";
                notes.add("NeoForge networking together with Paper/Purpur server behaviour.");
                if (namedByPing) notes.add(named + " is named from the server list version \"" + pingName + "\".");
                if (named != null && !named.equals("Youer") && purpurTime) {
                    notes.add("This server names itself " + named + ", but Purpur's per-tick time packets point to Youer (the only known NeoForge + Purpur hybrid).");
                }
            }
        } else if (family == Family.CUSTOM) {
            String kind = null;
            if (veloflameLimbo) {
                software = "VeloFlame verification limbo";
                notes.add("This is VeloFlame's antibot check world, the real server comes after the reconnect/transfer.");
            } else if (sonarLimbo) {
                software = "Sonar verification limbo";
                notes.add("This is Sonar's antibot check world, the real server comes after you pass the verification.");
            } else if (limboKind != null) {
                kind = limboKind;
                software = limboKind;
                if (byBrand != null && !byBrand.equals(limboKind)) notes.add("Brand says " + byBrand + ", but the packets look like " + limboKind + ".");
                else notes.add(limboKind + " confirmed by the packets" + (byBrand != null ? " and the brand." : " (brand not needed)."));
            } else if (byBrand != null) {
                kind = byBrand;
                software = byBrand;
                notes.add(byBrand + " recognised from the brand, the packets only show a custom server.");
            } else if (limbo && !packsReceived && c < 8) {
                software = "Limbo server";
            } else {
                software = backendBrand != null && !backendBrand.isBlank() ? backendBrand + " (custom server)" : "Custom server";
            }
            if ("LimboAPI".equals(kind) && brandLower.startsWith("limboapi (")) {
                int arrow = backendBrand.lastIndexOf(" -> ");
                String limboName = arrow < 0 ? "" : backendBrand.substring(arrow + 4).trim();
                if (!limboName.isEmpty()) software = "LimboAPI (" + (limboName.length() > 32 ? limboName.substring(0, 32) : limboName) + ")";
            }
            boolean lightweight = veloflameLimbo || sonarLimbo || (kind != null && LIMBO_SOFTWARE.contains(kind)) || "Limbo server".equals(software);
            notes.add("Not built on Mojang's server code: " + (lightweight ? "a lightweight limbo/lobby server."
                    : "Pumpkin".equals(kind) ? "a from-scratch full server." : "a from-scratch server implementation."));
        } else if (family == Family.MODDED_BUKKIT) {
            String base = arclightLoader != null ? loaderLabel(arclightLoader)
                    : loaderWrapped ? (wrappedNeo ? "NeoForge" : "Forge")
                    : forgeHybrid && neo - rootNeo < 4 && !fabricPing ? "Forge"
                    : neo - rootNeo >= 4 ? "NeoForge" : "Fabric";
            String named = hybridName(brandLower);
            boolean namedByPing = named == null && pingHybrid != null;
            if (namedByPing) named = pingHybrid;
            if (arclightLoader != null) software = "Arclight (" + arclightLoader + ")";
            else if (named != null) software = named;
            else software = (loaderWrapped || forgeHybrid ? "" : "Arclight-style ") + base + " + Bukkit hybrid";
            if (arclightPing) notes.add("Arclight is named from the server list version \"" + pingName + "\".");
            else if (arclightLoader == null && namedByPing) notes.add(named + " is named from the server list version \"" + pingName + "\".");
            String how = loaderWrapped ? "Bukkit's command map wraps " + base + "'s own command (" + (wrappedNeo ? "minecraft:neoforge" : "minecraft:forge") + "), so " + base + " runs under a Bukkit layer."
                    : forgeHybrid && "Forge".equals(base) ? "Forge (FML) signals together with Bukkit commands."
                    : arclightPack && !arclightBrand ? "Arclight's own data pack is offered in the configuration (brand not needed)."
                    : timedHybrid || arclightBrand ? base + " networking with Bukkit commands and Spigot-style timings."
                    : null;
            if (how != null) notes.add(how);
        }

        if (family == Family.UNKNOWN && knownName(backendBrand) != null) {
            software = knownName(backendBrand) + " (brand only)";
            notes.add("Only the brand identifies this, the packet signals were inconclusive (rejoin for a cleaner read).");
        }
        if (limboApi || brandLower.startsWith("limboapi (") || software.startsWith("LimboAPI")) proxy = proxy == null ? "Velocity + LimboAPI" : appendProxy(proxy, "LimboAPI");
        if (sonarLimbo) proxy = proxy == null ? "Velocity + Sonar (verification)" : appendProxy(proxy, "Sonar (verification)");

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
        String beOrder = BlockEntityOrder.debug();
        ServerData current = MCUtil.MC.getCurrentServer();
        Integer status = current == null ? null : statusFlags.get(current);
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
                    + " tags=" + tagSummaryText() + " levels=" + levelNames + " tabActions=" + firstTabActions + " tp0=" + firstTeleportId + (firstTeleportYRot ? "/yrot" : "")
                    + " regFirst=" + firstRegistry + " regData=" + mcRegistryEntriesWithData + "/" + mcRegistryEntries + " regUnsorted=" + unsortedRegistryEntries
                    + " biomes=" + biomeEntries + " biomeNbt=" + serverOnlyBiomeNbt + " pad=" + limboPadding + " cfgKa=" + configKeepAlives + (cfgKeepAliveBeforeRegistries ? "/early" : "")
                    + " round=" + configRounds + " login=" + loginView + "/" + loginSim + "/" + loginReducedDebug + "/" + loginDeathScreen + "/" + loginSeed
                    + " fly=" + (abilitiesSeen ? firstFlyingSpeed : "none") + " emptyBatches=" + emptyChunkBatches + " selfTab=" + selfTabActions
                    + " renamed=" + profileRenamed + " brandPlay=" + brandFirstInPlay
                    + " features=" + featuresSeen + " zeroSession=" + zeroSession + " emptyLevels=" + emptyLevels + " kaIds=" + keepAliveIdPattern()
                    + " tabAdds=" + selfTabAdds + "/" + firstTabAddEntries + ":" + tabAddPattern + " mh=" + pairingWithMaxHealth + "/" + pairingWithoutMaxHealth + "/" + pairingWithScale
                    + " weather=" + joinWeather + " respawn=" + respawnRadiusFirst + "/" + respawnPositionFirst + " ownDataEarly=" + ownDataBeforeBorder
                    + " firstKa=" + (firstKeepAliveAt > 0 && loginAt > 0 ? firstKeepAliveAt - loginAt : 0) + " tracked=" + trackedMobs + "/" + Math.round(farthestTracked) + ":" + farthestTrackedType
                    + " border=" + borderCount + " speed=" + speedExact + "/" + speedFloat + " setTime=" + setTimePackets
                    + " shredOrder=" + advancementsAt + "/" + ownMetaAt + "/" + bordersAtCommands
                    + " misc=" + Math.round(farthestMisc) + ":" + farthestMiscType + " other=" + Math.round(farthestOther) + ":" + farthestOtherType
                    + " adv=" + advancementsSeen + " beOrder=" + beOrder
                    + " tagNs=" + tagNamespaces + "/" + playTagNamespaces + " featLate=" + featuresAfterTags
                    + " oxy=" + ownAttrPackets + ":" + ownOxygen + (ownDiedEarly ? "/dead" : "") + " mobOxy=" + pairingWithOxygen + "/" + (pairingWithMaxHealth + pairingWithoutMaxHealth)
                    + " status=" + status;
        }
    }

    private static boolean twinFree(List<String> roots, String name) {
        for (String root : roots) {
            int colon = root.indexOf(':');
            if (colon > 0 && root.substring(colon + 1).equals(name) && !root.startsWith("minecraft:")) return false;
        }
        return true;
    }

    private static String hybridName(String lower) {
        if (lower == null || lower.isEmpty()) return null;
        for (String hybrid : HYBRID_BRANDS) if (lower.startsWith(hybrid.toLowerCase(Locale.ROOT))) return hybrid;
        return null;
    }

    private static String loaderLabel(String loader) {
        return switch (loader.toLowerCase(Locale.ROOT)) {
            case "neoforge" -> "NeoForge";
            case "forge" -> "Forge";
            case "fabric" -> "Fabric";
            case "quilt" -> "Quilt";
            default -> loader;
        };
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
            case MODDED_BUKKIT -> { return "Forge/NeoForge/Fabric + Bukkit hybrid"; }
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
