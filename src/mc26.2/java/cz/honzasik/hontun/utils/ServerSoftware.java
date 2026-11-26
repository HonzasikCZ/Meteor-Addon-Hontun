package cz.honzasik.hontun.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import cz.honzasik.hontun.mixin.network.software.EntityEventPacketAccessor;
import cz.honzasik.hontun.mixin.network.software.MoveEntityPacketAccessor;
import cz.honzasik.hontun.mixin.network.software.RotateHeadPacketAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
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
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveMinecartPacket;
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
import net.minecraft.util.Mth;
import net.minecraft.world.clock.ClockNetworkState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.shapes.VoxelShape;
import it.unimi.dsi.fastutil.ints.IntList;
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

    public enum Origin { BUILT_IN, CONFIG }

    public record Evidence(String signal, String detail, Family family, int weight, boolean againstPaper, String config) {
        public Evidence(String signal, String detail, Family family, int weight, boolean againstPaper) {
            this(signal, detail, family, weight, againstPaper, null);
        }

        public Evidence(String signal, String detail, Family family, int weight) {
            this(signal, detail, family, weight, false, null);
        }

        public Origin origin() {
            return config == null ? Origin.BUILT_IN : Origin.CONFIG;
        }

        public Evidence cfg(String key) {
            return new Evidence(signal, detail, family, weight, againstPaper, key);
        }
    }

    public record Note(String text, Origin origin, String config) {}

    public record Result(String software, Family family, String brand, String proxy, String confidence,
                         List<Evidence> evidence, List<Note> notes, Origin nameOrigin, String nameConfig) {}

    private static final class Notes {
        final List<Note> list = new ArrayList<>();
        Origin nameOrigin;
        String nameConfig;
        boolean nameFromBrand;

        void add(String text) {
            list.add(new Note(text, null, null));
        }

        void tag(String text, String config) {
            list.add(new Note(text, config == null ? Origin.BUILT_IN : Origin.CONFIG, config));
        }

        void name(String text, String config, boolean brandBacked) {
            tag(text, config);
            named(config, brandBacked);
        }

        void named(String config, boolean brandBacked) {
            nameOrigin = config == null ? Origin.BUILT_IN : Origin.CONFIG;
            nameConfig = config;
            nameFromBrand = brandBacked;
        }

        void structural() {
            nameOrigin = Origin.BUILT_IN;
            nameConfig = null;
            nameFromBrand = false;
        }
    }

    private static final class Reasons {
        final List<String> texts = new ArrayList<>();
        final Set<String> configs = new LinkedHashSet<>();
        boolean fixed;

        Reasons add(String text, String config) {
            texts.add(text);
            if (config == null) fixed = true;
            else configs.add(config);
            return this;
        }

        Reasons addAll(Reasons other) {
            texts.addAll(other.texts);
            configs.addAll(other.configs);
            fixed |= other.fixed;
            return this;
        }

        boolean isEmpty() {
            return texts.isEmpty();
        }

        String text() {
            return String.join(", ", texts);
        }

        String config() {
            return fixed || configs.isEmpty() ? null : String.join("; ", configs);
        }
    }

    private static final String CFG_TAB_COMPLETE = "spigot.yml commands.tab-complete (default 0, shows it; below 0 the command tree is empty or not sent at all)";
    private static final String CFG_BUKKIT_TREE = CFG_TAB_COMPLETE + "; spigot.yml commands.send-namespaced (default true) and the commands.yml icanhasbukkit alias";
    private static final String CFG_OP_FORK_COMMAND = "op only: the fork's own command defaults to op, so normal players never see it; spigot.yml commands.send-namespaced (default true) and commands.tab-complete (default 0)";
    private static final String CFG_ADVANCE_TIME = "gamerule advance_time (default true, shows it) on 26.x; on 1.21.x only when purpur.yml world-settings.default.gameplay-mechanics.daylight-cycle-ticks daytime/nighttime differ from 12000 (default 12000, hides it)";
    private static final String CFG_ZOMBIE_HORSE = "purpur.yml world-settings.default.mobs.zombie_horse.attributes.max_health min/max (default 15.0, shows it)";
    private static final String CFG_NO_ADVANCEMENTS = "spigot.yml advancements.disabled (default [minecraft:story/disabled], hides it; \"*\" removes them), universespigot.yml performance.disableCriterionTrigger and canvas-worlds.yml disable-criterion-trigger (default false)";
    private static final String CFG_UNIVERSE_SPEED = "universespigot.yml behavior.effects.speed.speedBaseModifier (default 0.2, shows it; any other value removes it)";
    private static final String CFG_UNIVERSE_CHUNKS = "universespigot.yml asynchronous.chunks-sending.enabled (default false, hides it)";
    private static final String CFG_UNIVERSE_TRACKER = "universespigot.yml asynchronous.tracker.enabled (default false, hides it)";
    private static final String CFG_UNIVERSE_TAB = "universespigot.yml behavior.removePlayersFromTabList (default false, hides it)";
    private static final String CFG_UNIVERSE_AI = "universespigot.yml performance.dynamic-brain.maximum-activation-priority (default 20; an odd value lets odd ids move), behavior.disableActivationRange and behavior.disableEntityAI (default false), spigot.yml entity-activation-range and server.properties simulation-distance; the defaults show it";
    private static final String CFG_TRACKING = "spigot.yml world-settings.default.entity-tracking-range animals/monsters (Spigot 48, Paper 96), server.properties view-distance (default 10) and entity-broadcast-range-percentage (default 100), and your render distance";
    private static final String CFG_MISC_TRACKING = "spigot.yml world-settings.default.entity-tracking-range misc (default 32) and other (default 64), server.properties view-distance and your render distance";
    private static final String CFG_DAB = "pufferfish.yml dab.enabled (default true on Pufferfish and Pluto, shows it), dab.start-distance and spigot.yml entity-activation-range; Leaf leaf-global.yml and DivineMC divinemc.yml performance.dab.enabled (default false)";
    private static final String CFG_SAMPLE = "server.properties hide-online-players (default false, shows it) and 13 or more players online; spigot.yml settings.sample-count (default 12) can be raised on Spigot and Paper to look the same";
    private static final String CFG_PING_VELOCITY = "velocity.toml [ping-passthrough] version (default false, shows it); Velocity-CTD and VeloFlame build it from [advanced] fallback-version-ping and custom-brand-proxy";
    private static final String CFG_PING_BUNGEE = "BungeeCord config.yml listeners[].ping_passthrough (default false, shows it)";
    private static final String CFG_ALT_KEEPALIVE = "purpur.yml settings.use-alternate-keepalive (Purpur default false; Plazma and DivineMC default true)";
    private static final String CFG_SONAR_NAME = "Sonar config verification.cached-username (default \"Sonar\", shows it)";
    private static final String CFG_NANO_TAB = "NanoLimbo settings.yml playerList.enable (default true, shows it)";
    private static final String CFG_NANO_NAME = "NanoLimbo settings.yml playerList.username (default \"NanoLimbo\", shows it)";
    private static final String CFG_LOOHP_LEVEL = "LOOHP Limbo server.properties level-name (default world;spawn.schem, shows it)";
    private static final String CFG_LOOHP_MAXP = "LOOHP Limbo server.properties max-players (default -1, shows it)";
    private static final String CFG_BLADEREN = "leaves.yml settings.protocol.bladeren.protocol (default true, shows it)";
    private static final String CFG_LEAVES_EMPTY = "leaves.yml settings.protocol.bladeren.protocol and every other protocol turned off (default on, hides it), plus the Leaves brand from leaves.yml settings.misc.server-mod-name";
    private static final String CFG_DIVINE_REGISTER = "divinemc.yml network.protocols appleskin/jade/xaeromap/syncmatica (all default false, shows it; any enabled one fills the list)";
    private static final String CFG_DIVINE_COMMAND = CFG_TAB_COMPLETE + "; otherwise both commands.yml aliases divinemc and divinemc:divinemc must be emptied";
    private static final String CFG_DIVINE_BRAND = "DivineMC purpur.yml settings.rebrand.f3-name (default \"DivineMC\", shows it; colour codes in it hide it)";
    private static final String CFG_GOLEM_COMMAND = "op only: golem.command.golem and membar/tpsbar default to op; spigot.yml commands.send-namespaced (default true)";
    private static final String CFG_GOLEM_HEARTBEAT = "golempaper.yml wire.drop-redundant.movement-packets (default false, shows it)";
    private static final String CFG_GOLEM_MODIFIER = "golempaper.yml mechanics.combat.disable-attack-cooldown (default false, hides it)";
    private static final String CFG_GOLEM_CULLING = "golempaper.yml wire.section-culling.enabled (default false, hides it)";
    private static final String CFG_VELOCITY_BRAND = "velocity.toml [advanced] server-brand (default \"{backend-brand} ({proxy-brand})\", shows it)";
    private static final String CFG_PROXY_COMMANDS = "velocity.toml [advanced] announce-proxy-commands (default true, shows it)";
    private static final String CFG_VIA_DETAILS = "ViaVersion config.yml (viaversion.yml on ViaProxy) send-server-details (default true, shows it)";
    private static final String CFG_VF_LIMBO = "veloflame.toml [limbo] enabled (default true) and always-active (default false: only while under attack); its brand comes from [limbo] server-brand";

    private enum JoinMessage { NONE, LITERAL, TRANSLATED, CUSTOM }

    public static final List<String> PAPER_FORKS = List.of("Paper", "Purpur", "Folia", "Canvas", "FlameMC", "Leaf", "DivineMC",
            "Pufferfish", "UniverseSpigot", "Leaves", "Pluto", "AdvancedSlimePaper", "Gale", "SparklyPaper",
            "ShreddedPaper", "Plazma", "GolemPaper");

    private static final Set<Family> MODDED_VANILLA = Set.of(Family.FABRIC, Family.QUILT, Family.FORGE, Family.NEOFORGE);
    private static final int PROTO_1_20_1 = 763;
    private static final int PROTO_1_20_2 = 764;
    private static final int PROTO_1_20_3 = 765;
    private static final int PROTO_1_21 = 767;
    private static final int PROTO_1_21_5 = 770;
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
            "FlameMC", "Canvas",
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
    private static final Set<String> REMOVAL_SKIP = Set.of("player", "armor_stand", "item_display", "block_display", "text_display",
            "interaction", "marker", "mannequin", "area_effect_cloud", "falling_block");
    private static final Set<String> PURPUR_COVERED = Set.of("cow", "pig", "sheep", "chicken", "zombie", "creeper", "spider");
    private static final Set<String> PURPUR_BARE = Set.of("goat", "frog", "tadpole", "warden", "armor_stand", "mannequin");
    private static final double ENDER_FLOAT_GAP = 1.5;
    private static final long ENDER_LANDING_MS = 500;
    private static final int CHORUS_SCAN_CAP = 600;
    private static final long CHORUS_WAIT_MS = 2000;
    private static final long REMOVAL_BURST_MS = 10;
    private static final long RESPAWN_BATCH_MS = 3000;
    private static final long HEALTH_LOOKBACK_MS = 250;
    private static final long RESPAWN_HEALTH_MS = 1000;
    private static final long COLLIDER_QUIET_MS = 5000;
    private static final long COLLIDER_REPEAT_MS = 600;
    private static final int COLLIDER_STREAM_REPEATS = 8;
    private static final int COLLIDER_CAP = 256;
    private static final int TYPE_CAP = 8192;

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
    private static final int TEXT_CAP = 512;
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
    private static int proxyVvVersion;
    private static String proxyVvName;
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
    private static final Map<Integer, String> entityTypes = new HashMap<>();
    private static final Map<Integer, String> priorTypes = new HashMap<>();
    private static int batchedRemovals;
    private static int batchedRemovalMax;
    private static int singleRemovalBursts;
    private static int singleRun;
    private static long singleRunAt;
    private static boolean respawnBatched;
    private static long respawnWatchAt;
    private static boolean deathPending;
    private static final Map<Integer, Collider> colliders = new HashMap<>();
    private static int colliderStreams;
    private static int colliderMoves;
    private static int colliderQuiet;
    private static int playKeepAlives;
    private static float lastOwnHealth = Float.NaN;
    private static long ownMaxHealthAt;
    private static boolean respawnHealthWatch;
    private static long respawnHealthAt;
    private static int healthBare;
    private static int healthSynced;
    private static final Map<Integer, Long> coveredPending = new LinkedHashMap<>();
    private static int coveredVanilla;
    private static int coveredPurpur;
    private static int golemModifiers;
    private static final Set<Long> culledChunks = new HashSet<>();
    private static int nearFloors;
    private static int nearHollow;
    private static int farCulled;
    private static int farFloors;
    private static boolean cullContrast;
    private static int culledRefills;
    private static int pairingBareNonPurpur;
    private static boolean clockPaused;
    private static boolean tickFrozen;
    private static final Map<Integer, Long> enderTeleports = new HashMap<>();
    private static final java.util.ArrayDeque<double[]> enderLandings = new java.util.ArrayDeque<>();
    private static int enderFloats;
    private static int enderGrounded;
    private static double enderFloatMax;
    private static final Set<Integer> enderFloatIds = new HashSet<>();
    private static boolean inEnd;
    private static final java.util.ArrayDeque<long[]> endChunks = new java.util.ArrayDeque<>();
    private static int chorusScanned;
    private static int chorusPlants;
    private static int chorusOdd;
    private static int chorusOddChunks;

    private static final class Collider {
        final long addedAt;
        final int keepAlivesAtAdd;
        boolean excluded, streamed, judged, synced, onGround;
        int moves, repeats, earlySyncs;
        double x, y, z;
        float yRot, xRot;
        long lastSyncAt;

        Collider(long addedAt, int keepAlivesAtAdd) {
            this.addedAt = addedAt;
            this.keepAlivesAtAdd = keepAlivesAtAdd;
        }
    }

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
        proxyVvVersion = 0;
        proxyVvName = null;
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
        entityTypes.clear();
        priorTypes.clear();
        batchedRemovals = 0;
        batchedRemovalMax = 0;
        singleRemovalBursts = 0;
        singleRun = 0;
        singleRunAt = 0;
        respawnBatched = false;
        respawnWatchAt = 0;
        deathPending = false;
        colliders.clear();
        colliderStreams = 0;
        colliderMoves = 0;
        colliderQuiet = 0;
        playKeepAlives = 0;
        lastOwnHealth = Float.NaN;
        ownMaxHealthAt = 0;
        respawnHealthWatch = false;
        respawnHealthAt = 0;
        healthBare = 0;
        healthSynced = 0;
        coveredPending.clear();
        coveredVanilla = 0;
        coveredPurpur = 0;
        golemModifiers = 0;
        culledChunks.clear();
        nearFloors = 0;
        nearHollow = 0;
        farCulled = 0;
        farFloors = 0;
        cullContrast = false;
        culledRefills = 0;
        pairingBareNonPurpur = 0;
        clockPaused = false;
        tickFrozen = false;
        enderTeleports.clear();
        enderLandings.clear();
        enderFloats = 0;
        enderGrounded = 0;
        enderFloatMax = 0;
        enderFloatIds.clear();
        inEnd = false;
        endChunks.clear();
        chorusScanned = 0;
        chorusPlants = 0;
        chorusOdd = 0;
        chorusOddChunks = 0;
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
                } else {
                    proxyVvVersion = version;
                    proxyVvName = name;
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
                inEnd = "minecraft:the_end".equals(levelKey(spawn));
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
            if (packet instanceof ClientboundKeepAlivePacket) {
                playKeepAlives++;
                sweepColliders(now);
            }
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
            if (packet instanceof ClientboundSetHealthPacket health) onOwnHealth(health.getHealth(), now);
            if (packet instanceof ClientboundPlayerCombatKillPacket kill && kill.playerId() == ownEntityId) deathPending = true;
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
                boolean resent = loadedChunks.contains(chunkKey(chunk.getX(), chunk.getZ()));
                onChunkOrder(chunk.getX(), chunk.getZ(), now);
                onSectionFloor(chunk.getX(), chunk.getZ(), chunk.getChunkData(), resent);
                onEndChunk(chunk.getX(), chunk.getZ(), now);
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
                culledChunks.remove(chunkKey(forget.pos().x(), forget.pos().z()));
            } else if (packet instanceof ClientboundRemoveEntitiesPacket remove) {
                onRemovals(remove.getEntityIds(), now);
                for (int gone : remove.getEntityIds()) {
                    liveEntities.remove(gone);
                    equipmentSpawns.remove(gone);
                    equipmentFirst.remove(gone);
                    FreshFreeze.onRemove(gone);
                }
            } else if (packet instanceof ClientboundSetEquipmentPacket equipment) {
                onEquipment(equipment, now);
            } else if (packet instanceof ClientboundRespawnPacket respawn) {
                onGolemRespawn(now);
                loadedChunks.clear();
                liveEntities.clear();
                prevChunkDistance = -1;
                FreshFreeze.newWorld();
                BlockEntityOrder.newWorld(levelKey(respawn.commonPlayerSpawnInfo()));
                inEnd = "minecraft:the_end".equals(levelKey(respawn.commonPlayerSpawnInfo()));
                endChunks.clear();
                enderTeleports.clear();
                enderLandings.clear();
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
            if (packet instanceof ClientboundTickingStatePacket ticking) tickFrozen = ticking.isFrozen();
            if (packet instanceof ClientboundEntityEventPacket event && event.getEventId() == 46) onEnderTeleport(((EntityEventPacketAccessor) event).hontun$entityId(), now);
            if (packet instanceof ClientboundAddEntityPacket add) onAdd(add, now);
            else if (packet instanceof ClientboundEntityPositionSyncPacket sync) {
                onPositionSync(sync.id(), now);
                MobCadence.onMove(sync.id(), now);
                var position = sync.values().position();
                onColliderSync(sync.id(), position.x, position.y, position.z, sync.values().yRot(), sync.values().xRot(), sync.onGround(), now);
                if (position != null) onEnderLanding(sync.id(), position.x, position.y, position.z, now);
            }
            else if (packet instanceof ClientboundSetPassengersPacket passengers) {
                for (int p : passengers.getPassengers()) pendingResync.remove(p);
                excludeColliders(passengers);
                linkPassengers(passengers, now);
            } else if (packet instanceof ClientboundMoveEntityPacket move) {
                int moved = ((MoveEntityPacketAccessor) move).hontun$entityId();
                pendingSnapshot.remove(moved);
                onColliderMove(moved);
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
                Set<Relative> relative = teleport.relatives();
                if (relative == null || (!relative.contains(Relative.X) && !relative.contains(Relative.Y) && !relative.contains(Relative.Z))) {
                    var target = teleport.change().position();
                    onEnderLanding(teleport.id(), target.x, target.y, target.z, now);
                }
            } else if (packet instanceof ClientboundUpdateAttributesPacket attrs) {
                onSpeed(attrs);
                onAttributes(attrs, now);
            } else if (packet instanceof ClientboundMoveMinecartPacket minecart) {
                Collider c = colliders.get(minecart.entityId());
                if (c != null) c.excluded = true;
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
        onGolemAdd(id, typeKey, now);
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

    private static boolean hardCollider(String typeKey) {
        return vehicleType(typeKey) || typeKey.equals("shulker") || typeKey.equals("happy_ghast");
    }

    private static void onGolemAdd(int id, String typeKey, long now) {
        singleRun = 0;
        respawnWatchAt = 0;
        if (entityTypes.size() >= TYPE_CAP) entityTypes.clear();
        entityTypes.put(id, typeKey);
        if (hardCollider(typeKey) && (colliders.size() < COLLIDER_CAP || colliders.containsKey(id))) colliders.put(id, new Collider(now, playKeepAlives));
        sweepCovered(now);
        coveredPending.remove(id);
        if (PURPUR_COVERED.contains(typeKey) && coveredPending.size() < TRACK_CAP) coveredPending.put(id, now);
    }

    private static void sweepCovered(long now) {
        Iterator<Map.Entry<Integer, Long>> it = coveredPending.entrySet().iterator();
        while (it.hasNext()) {
            if (now - it.next().getValue() <= SNAPSHOT_WINDOW_MS) break;
            coveredVanilla++;
            it.remove();
        }
    }

    private static void onRemovals(IntList ids, long now) {
        int size = ids.size();
        int real = 0;
        for (int i = 0; i < size; i++) {
            int id = ids.getInt(i);
            String type = entityTypes.remove(id);
            String prior = priorTypes.remove(id);
            if (type == null) type = prior;
            if (type != null && !REMOVAL_SKIP.contains(type)) real++;
            Collider gone = colliders.remove(id);
            if (gone != null && now - gone.addedAt >= COLLIDER_QUIET_MS) judgeCollider(gone);
        }
        if (size >= 2) {
            singleRun = 0;
            if (real >= 2) {
                batchedRemovals++;
                batchedRemovalMax = Math.max(batchedRemovalMax, real);
                if (respawnWatchAt > 0 && now - respawnWatchAt <= RESPAWN_BATCH_MS) respawnBatched = true;
            }
        } else if (size == 1 && real == 1) {
            if (singleRun > 0 && now - singleRunAt <= REMOVAL_BURST_MS) {
                singleRun++;
            } else {
                singleRun = 1;
                singleRunAt = now;
            }
            if (singleRun == 3) singleRemovalBursts++;
        }
    }

    private static void onGolemRespawn(long now) {
        priorTypes.clear();
        priorTypes.putAll(entityTypes);
        entityTypes.clear();
        for (Collider c : colliders.values()) {
            if (now - c.addedAt >= COLLIDER_QUIET_MS) judgeCollider(c);
        }
        colliders.clear();
        culledChunks.clear();
        nearFloors = 0;
        nearHollow = 0;
        farCulled = 0;
        farFloors = 0;
        lastOwnHealth = Float.NaN;
        respawnWatchAt = 0;
        respawnHealthWatch = false;
        if (deathPending) {
            deathPending = false;
            respawnWatchAt = now;
            respawnHealthWatch = true;
            respawnHealthAt = now;
        }
    }

    private static void sweepColliders(long now) {
        for (Collider c : colliders.values()) {
            if (!c.judged && now - c.addedAt >= COLLIDER_QUIET_MS) judgeCollider(c);
        }
    }

    private static void judgeCollider(Collider c) {
        if (c.judged) return;
        c.judged = true;
        if (!c.excluded && !c.streamed && c.earlySyncs <= 1 && playKeepAlives - c.keepAlivesAtAdd >= 3) colliderQuiet++;
    }

    private static void onColliderSync(int id, double x, double y, double z, float yRot, float xRot, boolean onGround, long now) {
        Collider c = colliders.get(id);
        if (c == null) return;
        if (now - c.addedAt <= COLLIDER_QUIET_MS) c.earlySyncs++;
        boolean same = c.synced && x == c.x && y == c.y && z == c.z && yRot == c.yRot && xRot == c.xRot && onGround == c.onGround
                && now - c.lastSyncAt <= COLLIDER_REPEAT_MS;
        c.repeats = same ? c.repeats + 1 : 0;
        if (c.repeats >= COLLIDER_STREAM_REPEATS && !c.excluded && !c.streamed) {
            c.streamed = true;
            colliderStreams++;
        }
        c.synced = true;
        c.x = x;
        c.y = y;
        c.z = z;
        c.yRot = yRot;
        c.xRot = xRot;
        c.onGround = onGround;
        c.lastSyncAt = now;
    }

    private static void onEnderTeleport(int id, long now) {
        if (!"enderman".equals(entityTypes.get(id))) return;
        if (enderTeleports.size() >= 64 && !enderTeleports.containsKey(id)) enderTeleports.clear();
        enderTeleports.put(id, now);
    }

    private static void onEnderLanding(int id, double x, double y, double z, long now) {
        if (enderTeleports.isEmpty()) return;
        Long at = enderTeleports.remove(id);
        if (at == null || now - at > ENDER_LANDING_MS || enderLandings.size() >= 64) return;
        enderLandings.add(new double[]{x, y, z, id});
    }

    private static void onEndChunk(int x, int z, long now) {
        if (!inEnd || chorusScanned >= CHORUS_SCAN_CAP || endChunks.size() >= 512) return;
        endChunks.add(new long[]{chunkKey(x, z), now});
    }

    private static void onColliderMove(int id) {
        Collider c = colliders.get(id);
        if (c == null || c.excluded) return;
        if (++c.moves == 2) colliderMoves++;
    }

    private static void excludeColliders(ClientboundSetPassengersPacket packet) {
        boolean self = false;
        for (int passenger : packet.getPassengers()) {
            Collider c = colliders.get(passenger);
            if (c != null) c.excluded = true;
            if (passenger == ownEntityId) self = true;
        }
        if (!self) return;
        Collider vehicle = colliders.get(packet.getVehicle());
        if (vehicle != null) vehicle.excluded = true;
    }

    private static void onOwnHealth(float health, long now) {
        if (health <= 0f) {
            deathPending = true;
            lastOwnHealth = Float.NaN;
            return;
        }
        float previous = lastOwnHealth;
        lastOwnHealth = health;
        if (Float.isNaN(previous) || previous == health || !joinDone) return;
        if (ownMaxHealthAt > 0 && now - ownMaxHealthAt <= HEALTH_LOOKBACK_MS) healthSynced++;
        else healthBare++;
    }

    private static boolean onOwnAttributes(ClientboundUpdateAttributesPacket packet, long now) {
        List<ClientboundUpdateAttributesPacket.AttributeSnapshot> values = packet.getValues();
        boolean maxHealth = false;
        for (ClientboundUpdateAttributesPacket.AttributeSnapshot s : values) {
            if ("max_health".equals(attributePath(s))) maxHealth = true;
        }
        boolean lone = maxHealth && values.size() == 1;
        if (maxHealth && values.size() <= 2) ownMaxHealthAt = now;
        if (respawnHealthWatch && now - respawnHealthAt <= RESPAWN_HEALTH_MS) {
            if (lone) {
                healthSynced++;
                respawnHealthWatch = false;
            } else if (maxHealth && values.size() >= 3) {
                healthBare++;
                respawnHealthWatch = false;
            }
        } else {
            respawnHealthWatch = false;
        }
        return lone && respawnAt > 0 && now - respawnAt <= RESPAWN_WINDOW_MS;
    }

    private static void onCoveredAttributes(int id, ClientboundUpdateAttributesPacket packet, long now) {
        Long added = coveredPending.remove(id);
        if (added == null) return;
        boolean maxHealth = false, scale = false;
        for (ClientboundUpdateAttributesPacket.AttributeSnapshot s : packet.getValues()) {
            String path = attributePath(s);
            if (path.equals("max_health")) maxHealth = true;
            else if (path.equals("scale")) scale = true;
        }
        if (now - added <= SNAPSHOT_WINDOW_MS && maxHealth && scale) coveredPurpur++;
        else coveredVanilla++;
    }

    private static String attributePath(ClientboundUpdateAttributesPacket.AttributeSnapshot snapshot) {
        return snapshot.attribute().unwrapKey().map(k -> k.identifier().getPath()).orElse("");
    }

    private static void onSectionFloor(int x, int z, ClientboundLevelChunkPacketData data, boolean resent) {
        if (!centerKnown || data == null) return;
        int blocks;
        boolean terrain = false;
        try {
            var buffer = data.getReadBuffer();
            if (buffer.readableBytes() < 2) return;
            blocks = buffer.getShort(buffer.readerIndex());
            for (long[] heights : data.getHeightmaps().values()) {
                for (long packed : heights) {
                    if (packed != 0) {
                        terrain = true;
                        break;
                    }
                }
                if (terrain) break;
            }
        } catch (Throwable t) {
            return;
        }
        long key = chunkKey(x, z);
        if (resent) {
            if (blocks > 0 && culledChunks.remove(key)) culledRefills++;
            return;
        }
        int distance = Math.max(Math.abs(x - centerX), Math.abs(z - centerZ));
        if (distance <= 2) {
            if (blocks > 0) nearFloors++;
            else nearHollow++;
        } else if (terrain) {
            if (blocks == 0) {
                farCulled++;
                if (culledChunks.size() < 4096) culledChunks.add(key);
            } else {
                farFloors++;
            }
        }
        if (nearFloors >= 4 && nearHollow == 0 && farCulled >= 8 && farFloors == 0) cullContrast = true;
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
        if (time.clockUpdates().isEmpty()) return;
        boolean paused = true;
        for (ClockNetworkState state : time.clockUpdates().values()) {
            if (state != null && state.rate() > 0f) paused = false;
        }
        clockPaused = paused;
    }

    private static void onSpeed(ClientboundUpdateAttributesPacket packet) {
        for (ClientboundUpdateAttributesPacket.AttributeSnapshot snapshot : packet.getValues()) {
            for (AttributeModifier modifier : snapshot.modifiers()) {
                if ("golem".equals(modifier.id().getNamespace())) golemModifiers++;
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
        onCoveredAttributes(id, packet, now);
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
        if (id == ownEntityId && onOwnAttributes(packet, now)) return;
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
            String typeKey = pendingTypes.remove(id);
            if (maxHealth) pairingWithMaxHealth++;
            else pairingWithoutMaxHealth++;
            if (!maxHealth && (typeKey == null || !PURPUR_BARE.contains(typeKey))) pairingBareNonPurpur++;
            if (scale) pairingWithScale++;
            if (oxygen) pairingWithOxygen++;
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

    private static void checkWorld(ClientLevel level, long now) {
        List<double[]> landings;
        List<Long> scan;
        synchronized (JOIN) {
            if (enderLandings.isEmpty() && endChunks.isEmpty()) return;
            scan = new ArrayList<>();
            landings = new ArrayList<>(enderLandings);
            enderLandings.clear();
            if (!Level.END.equals(level.dimension())) {
                endChunks.clear();
            } else {
                while (scan.size() < 4 && !endChunks.isEmpty() && chorusScanned + scan.size() < CHORUS_SCAN_CAP) {
                    long[] entry = endChunks.peek();
                    if (level.getChunkSource().getChunk((int) (entry[0] >> 32), (int) entry[0], ChunkStatus.FULL, false) == null) {
                        if (now - entry[1] < CHORUS_WAIT_MS) break;
                        endChunks.poll();
                        continue;
                    }
                    endChunks.poll();
                    scan.add(entry[0]);
                }
            }
        }
        if (landings.isEmpty() && scan.isEmpty()) return;
        List<double[]> gaps = new ArrayList<>();
        for (double[] landing : landings) {
            double gap = groundGap(level, landing[0], landing[1], landing[2]);
            if (!Double.isNaN(gap)) gaps.add(new double[]{gap, landing[3]});
        }
        int plants = 0, odd = 0, oddChunks = 0;
        for (long key : scan) {
            LevelChunk chunk = level.getChunkSource().getChunk((int) (key >> 32), (int) key, ChunkStatus.FULL, false);
            if (chunk == null) continue;
            int[] counts = chorusCounts(chunk);
            plants += counts[0];
            odd += counts[1];
            if (counts[1] > 0) oddChunks++;
        }
        synchronized (JOIN) {
            for (double[] gap : gaps) {
                if (gap[0] > ENDER_FLOAT_GAP) {
                    enderFloats++;
                    enderFloatMax = Math.max(enderFloatMax, gap[0]);
                    if (enderFloatIds.size() < 64) enderFloatIds.add((int) gap[1]);
                } else {
                    enderGrounded++;
                }
            }
            chorusScanned += scan.size();
            chorusPlants += plants;
            chorusOdd += odd;
            chorusOddChunks += oddChunks;
        }
    }

    private static double groundGap(ClientLevel level, double x, double y, double z) {
        int bx = Mth.floor(x), bz = Mth.floor(z);
        if (level.getChunkSource().getChunk(bx >> 4, bz >> 4, ChunkStatus.FULL, false) == null) return Double.NaN;
        int start = Mth.floor(y);
        int bottom = Math.max(level.getMinY(), start - 64);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int by = start; by >= bottom; by--) {
            pos.set(bx, by, bz);
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;
            VoxelShape shape = state.getCollisionShape(level, pos);
            if (shape.isEmpty() && !state.isSolid()) continue;
            return y - (by + (shape.isEmpty() ? 1.0 : shape.max(Direction.Axis.Y)));
        }
        return Double.NaN;
    }

    private static int[] chorusCounts(LevelChunk chunk) {
        int plants = 0, odd = 0;
        for (LevelChunkSection section : chunk.getSections()) {
            if (section == null || section.hasOnlyAir() || !section.maybeHas(s -> s.is(Blocks.CHORUS_PLANT))) continue;
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        BlockState state = section.getBlockState(x, y, z);
                        if (!state.is(Blocks.CHORUS_PLANT)) continue;
                        plants++;
                        if (state.getValue(PipeBlock.UP) && state.getValue(PipeBlock.DOWN) && (state.getValue(PipeBlock.NORTH)
                                || state.getValue(PipeBlock.EAST) || state.getValue(PipeBlock.SOUTH) || state.getValue(PipeBlock.WEST))) odd++;
                    }
                }
            }
        }
        return new int[]{plants, odd};
    }

    public static void tick() {
        net.minecraft.client.player.LocalPlayer me = MCUtil.MC.player;
        if (me == null) return;
        long now = System.currentTimeMillis();
        ClientLevel level = MCUtil.MC.level;
        if (level != null) checkWorld(level, now);
        if (me.isPassenger()) return;
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
            s.proxyVvProtocol = proxyVvVersion;
            s.proxyVvName = proxyVvName;
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
        if (data != null && data.version != null && data.state() == ServerData.State.SUCCESSFUL) {
            s.pingSeen = strip(data.version.getString(), "ping version name");
            s.pingProtocol = data.protocol;
            if (firstBackend) s.pingName = s.pingSeen;
        }
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

    private static String strip(String s, String what) {
        if (s == null) return null;
        String out = (s.length() > TEXT_CAP * 4 ? s.substring(0, TEXT_CAP * 4) : s).replaceAll("§.", "").trim();
        if (out.length() <= TEXT_CAP && s.length() <= TEXT_CAP * 4) return out;
        return Oversize.cut(what, s) + out.substring(0, Math.min(out.length(), TEXT_CAP));
    }

    private static String appendProxy(String proxy, String add) {
        if (proxy == null) return add;
        if (proxy.equals(add) || proxy.endsWith(" + " + add)) return proxy;
        return proxy + " + " + add;
    }

    public static Result detect() {
        ClientPacketListener conn = MCUtil.getPlayNetHandler();
        List<Evidence> ev = new ArrayList<>();
        Notes notes = new Notes();
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
        int rmBatched, rmBatchMax, rmBursts, colStreams, colMoves, colQuiet, hpBare, hpSynced, covVanilla, covPurpur, golemMods, cullRefills;
        boolean rmRespawn, cullFar;
        int mhBare, enderFloat, enderFloatMobs, enderGround, chorusAll, chorusBranch, chorusBranchChunks;
        double enderFloatTop;
        boolean timeFrozen;
        synchronized (JOIN) {
            mhBare = pairingBareNonPurpur;
            timeFrozen = clockPaused || tickFrozen;
            enderFloat = enderFloats;
            enderFloatMobs = enderFloatIds.size();
            enderGround = enderGrounded;
            enderFloatTop = enderFloatMax;
            chorusAll = chorusPlants;
            chorusBranch = chorusOdd;
            chorusBranchChunks = chorusOddChunks;
            sweepResync(System.currentTimeMillis());
            sweepCovered(System.currentTimeMillis());
            sweepColliders(System.currentTimeMillis());
            rmBatched = batchedRemovals;
            rmBatchMax = batchedRemovalMax;
            rmBursts = singleRemovalBursts;
            rmRespawn = respawnBatched;
            colStreams = colliderStreams;
            colMoves = colliderMoves;
            colQuiet = colliderQuiet;
            hpBare = healthBare;
            hpSynced = healthSynced;
            covVanilla = coveredVanilla;
            covPurpur = coveredPurpur;
            golemMods = golemModifiers;
            cullFar = cullContrast;
            cullRefills = culledRefills;
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
        String brand = strip(rawBrand, "server brand");
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
                    ev.add(new Evidence("brand", "\"" + brand + "\" is Velocity-CTD's rewrite of the backend brand", Family.UNKNOWN, 0).cfg(CFG_VELOCITY_BRAND));
                } else if (backendBrand.endsWith(" (VeloFlame)")) {
                    proxy = appendProxy(proxy, "VeloFlame");
                    backendBrand = backendBrand.substring(0, backendBrand.length() - " (VeloFlame)".length()).trim();
                    ev.add(new Evidence("brand", "\"" + brand + "\" is VeloFlame's rewrite of the backend brand", Family.UNKNOWN, 0)
                            .cfg(CFG_VELOCITY_BRAND + " on VeloFlame Pro; VeloFlame Lite forces it"));
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
            ev.add(new Evidence("commands", "/veloflame (alias /velo) is VeloFlame's own proxy command, offered to every player", Family.UNKNOWN, 0).cfg(CFG_PROXY_COMMANDS));
        }
        if (roots.contains("velocity:callback")) {
            proxy = proxy == null ? "Velocity" : proxy;
            ev.add(new Evidence("commands", "velocity:callback is registered by the Velocity proxy", Family.UNKNOWN, 0)
                    .cfg(CFG_PROXY_COMMANDS + "; only sent to 1.21.6+ clients"));
        }
        boolean velocityMarkers = tabEarly || linksEarly || titlesEarly;
        boolean bungeeRoots = roots.contains("bungee") || (roots.contains("glist") && !roots.contains("send"));
        if (bungeeRoots && !vfRoots && !velocityMarkers) {
            proxy = proxy == null ? "BungeeCord" : proxy;
            ev.add(new Evidence("commands", "bungee/glist commands come from the BungeeCord proxy", Family.UNKNOWN, 0)
                    .cfg("BungeeCord config.yml disabled_commands and permissions (the defaults let everyone see /bungee, shows it)"));
        }
        if (proxy == null && roots.contains("glist") && roots.contains("send") && !roots.contains("bungee") && !vfRoots && !tabEarly && !linksEarly) {
            proxy = "Gate";
            ev.add(new Evidence("commands", "Gate's builtin server/glist/send commands are offered to everyone", Family.UNKNOWN, 0)
                    .cfg("Gate config.yml builtinCommands (default true), requireBuiltinCommandPermissions (default false) and announceProxyCommands (default true); the defaults show it"));
        }
        if (proxy == null && roots.contains("server")) {
            proxy = "a proxy";
            ev.add(new Evidence("commands", "/server is registered by a re-encoding proxy (Velocity, Gate, BungeeCord and their forks all add it); no vanilla or Paper backend does, though a backend hub plugin could", Family.UNKNOWN, 0)
                    .cfg("the proxy's builtin server-switch command; announce-proxy-commands / builtinCommands defaults show it"));
        }

        boolean purpurTime = false, canvas = false, divine = false, leaves = false;
        Reasons divineWhy = new Reasons();
        String leavesCfg = null;
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
            ev.add(new Evidence("join", "vv:app_details is only sent by a standalone Via application (ViaProxy)", Family.UNKNOWN, 0).cfg(CFG_VIA_DETAILS));
        }
        if (proto != clientSide) {
            String who = "vv:proxy_details".equals(vvCh) ? "ViaVersion on the proxy"
                    : "vv:app_details".equals(vvCh) ? "ViaProxy" : "ViaVersion on the server";
            String version = backendName != null ? backendName + " (protocol " + proto + ")" : "protocol " + proto;
            ev.add(new Evidence("translation", who + " translates, the real backend is " + version, Family.UNKNOWN, 0)
                    .cfg(CFG_VIA_DETAILS + "; it decides which vv: channel names the translator"));
            notes.add("The server translates versions itself (" + who + "), so version-specific signals use its real version " + version + ".");
        } else if (vvCh != null) {
            ev.add(new Evidence("translation", "ViaVersion is installed (" + vvCh + "), backend protocol " + vvVer, Family.UNKNOWN, 0).cfg(CFG_VIA_DETAILS));
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
            ev.add(new Evidence("brand", "brand rewritten with a trailing colour-reset code (Velocity-CTD family)", Family.UNKNOWN, 0)
                    .cfg(CFG_VELOCITY_BRAND + "; colour codes in it break this check, VeloFlame Lite forces it"));
        }
        boolean vfLinks = links.stream().anyMatch(l -> l.toLowerCase(Locale.ROOT).contains("veloflame.com"));
        boolean ctdLinks = links.stream().anyMatch(l -> l.toLowerCase(Locale.ROOT).contains("github.com/gemstonegg/velocity-ctd"));
        if (vfLinks) {
            if ("Velocity-CTD".equals(proxy)) proxy = "VeloFlame";
            ev.add(new Evidence("join", "default VeloFlame server links (veloflame.com)", Family.UNKNOWN, 0)
                    .cfg("velocity.toml [server-links] (VeloFlame's default links to veloflame.com, shows it)"));
        }
        if (ctdLinks) {
            ev.add(new Evidence("join", "default Velocity-CTD server links (GemstoneGG/Velocity-CTD on GitHub)", Family.UNKNOWN, 0)
                    .cfg("velocity.toml [server-links] (Velocity-CTD's default links to its GitHub, shows it)"));
        }
        String motd = motdFirst == null ? null : strip(motdFirst.getString(), "in-game MOTD");
        if ("A VeloFlame Server".equals(motd)) {
            if ((linksEarly || tabEarly) && ("Velocity-CTD".equals(proxy) || "Velocity".equals(proxy))) proxy = "VeloFlame";
            ev.add(new Evidence("join", "default VeloFlame MOTD in the server-data packet", Family.UNKNOWN, 0)
                    .cfg("velocity.toml motd (default \"A VeloFlame Server\", shows it) and [ping-passthrough] description (default false)"));
        } else if ("A Velocity-CTD Server".equals(motd)) {
            ev.add(new Evidence("join", "default Velocity-CTD MOTD in the server-data packet", Family.UNKNOWN, 0)
                    .cfg("velocity.toml motd (default \"A Velocity-CTD Server\", shows it) and [ping-passthrough] description (default false)"));
        } else if ("A Velocity Server".equals(motd)) {
            ev.add(new Evidence("join", "default Velocity MOTD in the server-data packet", Family.UNKNOWN, 0)
                    .cfg("velocity.toml motd (default \"A Velocity Server\", shows it) and [ping-passthrough] description (default false)"));
        }
        boolean v7Sample = data != null && Boolean.TRUE.equals(sampleV7.get(data));
        if (v7Sample) {
            if (("Velocity".equals(proxy) || "Velocity or Gate".equals(proxy)) && !linksEarly && clientSide < PROTO_1_21) proxy = "Velocity-CTD";
            ev.add(new Evidence("ping", "server-list sample uses UUIDv7 ids (Velocity-CTD family)", Family.UNKNOWN, 0)
                    .cfg("velocity.toml motd-hover (two default lines, shows it; empty means no sample) and [ping-passthrough] players (default false)"));
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
            ev.add(new Evidence("brand", "brand changed after login to \"" + strip(brandLast, "server brand after login") + "\" (proxy)", Family.UNKNOWN, 0));
        }

        if (!configPhase) {
            notes.add("Join order is not compared, the server is older than 1.20.2.");
        } else if (border >= 2 && viewDist) {
            ev.add(new Evidence("join", "world info sent twice with view distance packets before the first chunk (Paper join order)", Family.PAPER, 4));
        } else if (done && border == 1 && !viewDist && cmdAt >= 0 && rbAt >= 0 && cmdAt < rbAt) {
            ev.add(new Evidence("join", "commands sent right after recipes, world info once (vanilla/Spigot join order)", Family.VANILLA, 2, true)
                    .cfg(CFG_TAB_COMPLETE + "; fixed on vanilla and CraftBukkit"));
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
            ev.add(new Evidence("entities", mhWithout + " mobs arrived without a max_health attribute (not CraftBukkit/Spigot"
                    + (mhBare >= 2 ? ", not Purpur)" : "; Purpur leaves it off goats, frogs, tadpoles and wardens too)"), Family.UNKNOWN, 0));
        } else if (mhWith >= 5 && mhTypes >= 3 && mhWithout == 0 && mhScale == 0) {
            ev.add(new Evidence("entities", "every mob arrived with max_health and none with scale (" + mhWith + " mobs, CraftBukkit/Spigot)", Family.BUKKIT, 2, true));
        }
        if (weatherAtJoin.contains("-R")) {
            ev.add(new Evidence("weather", "raining at join, announced with the inverted STOP_RAINING event (CraftBukkit lineage)", Family.UNKNOWN, 0));
        } else if (weatherAtJoin.contains("+R")) {
            ev.add(new Evidence("weather", "raining at join, announced with START_RAINING (vanilla/modded)", Family.VANILLA, 1, true));
        } else if (weatherAtJoin.startsWith("+r") && proto >= PROTO_26_2) {
            ev.add(new Evidence("weather", "clear weather at join announced as START_RAINING with rain level 0 (Paper's world info; vanilla and Spigot send nothing when it is clear)", Family.PAPER, 1));
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
            divineWhy.add("server data sent async", null);
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
            ev.add(new Evidence("time", String.format(Locale.ROOT, "time sent per clock every tick, %.0f/s (Purpur)", rate), Family.PAPER, 3).cfg(CFG_ADVANCE_TIME));
        }

        if (leavesProto) {
            leaves = true;
            leavesCfg = CFG_BLADEREN;
            ev.add(new Evidence("channels", "bladeren protocol" + (leavesVer != null ? " (" + leavesVer + ")" : "") + " (Leaves)", Family.PAPER, 3).cfg(CFG_BLADEREN));
        } else if (emptyRegister && brandLower.equals("leaves")) {
            leaves = true;
            leavesCfg = CFG_LEAVES_EMPTY;
            ev.add(new Evidence("channels", "empty minecraft:register with a Leaves brand (Leaves with bladeren off)", Family.PAPER, 0).cfg(CFG_LEAVES_EMPTY));
        } else if (emptyRegister) {
            divine = true;
            divineWhy.add("empty channel list", CFG_DIVINE_REGISTER);
            ev.add(new Evidence("channels", "empty minecraft:register sent after login (DivineMC)", Family.PAPER, 0).cfg(CFG_DIVINE_REGISTER));
        }

        boolean zombiePurpur = proto >= PROTO_1_21_11 && zh15 > 0 && zh25 == 0;
        boolean zombieVanilla = proto >= PROTO_1_21_11 && zh25 > 0 && zh15 == 0;
        if (zombiePurpur) {
            ev.add(new Evidence("entities", "zombie horse spawned with 15 HP (Purpur default, vanilla has 25 since 1.21.11)", Family.PAPER, 1).cfg(CFG_ZOMBIE_HORSE));
        } else if (zombieVanilla) {
            ev.add(new Evidence("entities", "zombie horse spawned with vanilla's 25 HP (not Purpur-family with default settings)", Family.UNKNOWN, 0).cfg(CFG_ZOMBIE_HORSE));
        }
        boolean statsStep20 = !statsOffGrid && statsAbsoluteGrid && statsNonZero >= 3 && ((statReadings >= 2 && playTimeMoved) || statsCrouch);
        if (statsStep20) {
            ev.add(new Evidence("stats", "time statistics only move in steps of 20 ticks (UniverseSpigot; Leaf, GolemPaper or old Gale when their statistics interval is set to 20)", Family.PAPER, 0));
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
            ev.add(new Evidence("entities", "Speed effect modifier is an exact 0.2 per level (UniverseSpigot)", Family.PAPER, 0).cfg(CFG_UNIVERSE_SPEED));
        } else if (floatSpeed > 0) {
            ev.add(new Evidence("entities", "Speed effect modifier uses vanilla's float 0.2 (not UniverseSpigot)", Family.UNKNOWN, 0));
        }

        boolean beyondSpigot = farthest > SPIGOT_TRACK_LIMIT;
        boolean beyondPaper = farthest > PAPER_TRACK_LIMIT;
        int trackRadius = MCUtil.MC.options == null ? view : Math.min(view, MCUtil.MC.options.renderDistance().get());
        boolean spigotRange = !beyondSpigot && trackRadius >= 4 && trackedCount >= 25 && farthest > 0 && farthest <= 52;
        if (beyondSpigot) {
            ev.add(new Evidence("entities", "a " + farthestType + " was sent " + Math.round(farthest) + " blocks away (beyond Spigot's default 48-block entity tracking"
                    + (beyondPaper ? " and Paper's 96" : "") + ")", Family.UNKNOWN, 0).cfg(CFG_TRACKING));
        } else if (spigotRange) {
            ev.add(new Evidence("entities", "no mob sent beyond " + Math.round(farthest) + " blocks among " + trackedCount + " (Spigot's default 48-block entity tracking)", Family.UNKNOWN, 0).cfg(CFG_TRACKING));
        }

        boolean batched = chunkBatches.get() > 0;
        boolean universeChunks = proto == PROTO_1_21_11 && !batched && (descents >= 2 || earlyEntities >= 2);
        if (universeChunks) {
            ev.add(new Evidence("chunks", (descents >= 2 ? descents + " chunks arrived out of distance order" : earlyEntities + " entities arrived before their chunk")
                    + " (UniverseSpigot sends chunks from parallel threads)", Family.PAPER, 0).cfg(CFG_UNIVERSE_CHUNKS));
        } else if (proto == PROTO_1_21_11 && !batched && fresh >= 100 && descents == 0) {
            ev.add(new Evidence("chunks", fresh + " chunks arrived strictly by distance (no UniverseSpigot async chunk sending)", Family.UNKNOWN, 0).cfg(CFG_UNIVERSE_CHUNKS));
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
            ev.add(new Evidence("entities", equipDups + " spawning mobs got their equipment sent twice and never before they appeared (UniverseSpigot async entity tracker)", Family.PAPER, 0).cfg(CFG_UNIVERSE_TRACKER));
        } else if (equipOrphans >= 2) {
            ev.add(new Evidence("entities", "mob equipment arrived just before the mob itself " + equipOrphans + " times (Bukkit's synchronous tracker, not UniverseSpigot's async one)", Family.UNKNOWN, 0).cfg(CFG_UNIVERSE_TRACKER));
        }
        boolean universeTab = tabActions.contains("ADD_PLAYER") && tabActions.contains("INITIALIZE_CHAT") && !tabActions.contains("UPDATE_LISTED");
        if (universeTab) {
            ev.add(new Evidence("join", "you were added to the tab list without the listed flag (UniverseSpigot removePlayersFromTabList)", Family.PAPER, 1).cfg(CFG_UNIVERSE_TAB));
        }
        FreshFreeze.Stats frozen = FreshFreeze.stats();
        boolean universeFreeze = frozen.universe();
        if (universeFreeze) {
            ev.add(new Evidence("entities", frozen.band() + " fresh mobs past " + Math.round(Math.min(frozen.reach(), 64.0)) + " blocks stood still (" + frozen.bandMoved() + " moved, none with an odd id) while "
                    + frozen.controlMoved() + "/" + frozen.control() + " closer ones moved (UniverseSpigot AI gate)", Family.PAPER, 1).cfg(CFG_UNIVERSE_AI));
        } else if (frozen.normal()) {
            ev.add(new Evidence("entities", "fresh mobs past 64 blocks move normally, odd ids too (not UniverseSpigot)", Family.UNKNOWN, 0).cfg(CFG_UNIVERSE_AI));
        }

        MobCadence.Stats cadence = MobCadence.stats();
        boolean dabOn = cadence.slowRuns() >= 2 && cadence.slowRuns() >= cadence.fastRuns();
        boolean dabOff = cadence.fastRuns() >= 3 && cadence.fastMobs() >= 2 && cadence.slowRuns() == 0 && cadence.observedMs() >= 60000;
        if (dabOn) {
            ev.add(new Evidence("entities", "distant mobs turn their heads 10° per update instead of 30° (" + cadence.slowRuns()
                    + " slow turns, Dynamic Activation of Brain, on by default in Pufferfish)", Family.PAPER, 1).cfg(CFG_DAB));
        } else if (dabOff) {
            ev.add(new Evidence("entities", "distant mobs turn their heads at full speed (no Dynamic Activation of Brain)", Family.UNKNOWN, 0).cfg(CFG_DAB));
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

        boolean golemGate = proto >= PROTO_1_21_11;
        boolean golemRemovals = golemGate && rmBursts == 0 && (rmBatched >= 2 || rmRespawn);
        boolean unbatchedRemovals = golemGate && rmBursts >= 1;
        if (golemGate && rmBatched >= 1 && rmBursts >= 1) {
            ev.add(new Evidence("entities", "entity removals came both batched (" + rmBatched + "x) and one per packet (" + rmBursts
                    + " burst(s)), a plugin probably sends its own", Family.UNKNOWN, 0));
        } else if (golemGate && rmBatched >= 1) {
            ev.add(new Evidence("entities", rmBatched + " entity-removal packet(s) carried several mobs/items at once (up to " + rmBatchMax + ")"
                    + (rmRespawn ? ", one right after your respawn" : "") + " - GolemPaper queues removals, every other server sends one id per packet",
                    Family.PAPER, golemRemovals ? 1 : 0));
        } else if (unbatchedRemovals) {
            ev.add(new Evidence("entities", rmBursts + " burst(s) of one-entity removal packets (not GolemPaper, which batches them)", Family.UNKNOWN, 0));
        }
        boolean colliderStream = golemGate && colStreams >= 1;
        boolean colliderQuietSig = golemGate && !colliderStream && (colMoves >= 1 || colQuiet >= 1);
        boolean colliderHeartbeat = colliderQuietSig && colMoves >= 1;
        if (colliderStream) {
            ev.add(new Evidence("entities", colStreams + " parked boat/minecart/shulker(s) got an unchanged position sync every update (Paper's tracker, not GolemPaper)", Family.PAPER, 1));
        } else if (colliderQuietSig) {
            ev.add(new Evidence("entities", "parked boats/minecarts/shulkers got no position-sync stream (" + (colMoves >= 1 ? colMoves + " with a plain move heartbeat" : colQuiet + " silent for 5 s")
                    + "; GolemPaper, vanilla and Spigot do this, other Paper forks do not)", Family.UNKNOWN, 0));
        }
        boolean healthQuiet = golemGate && hpBare >= 2 && hpSynced == 0;
        boolean healthSync = golemGate && hpSynced >= 2 && hpSynced >= 3 * hpBare;
        if (healthQuiet) {
            ev.add(new Evidence("entities", "your health changed " + hpBare + " times without a max_health attribute packet (GolemPaper, Leaf, DivineMC or vanilla; Paper and Purpur send one)", Family.UNKNOWN, 0));
        } else if (healthSync) {
            ev.add(new Evidence("entities", hpSynced + " health changes came with a max_health attribute packet (CraftBukkit, Paper or Purpur; not GolemPaper, Leaf or DivineMC)", Family.UNKNOWN, 0));
        }
        boolean noPurpurMobs = covVanilla >= 3 && covPurpur == 0 && purpurAttrs == 0 && !purpurFamily;
        if (noPurpurMobs) {
            ev.add(new Evidence("entities", covVanilla + " cows, pigs, sheep, zombies or similar paired without Purpur's max_health+scale attributes (not Purpur, Leaf or DivineMC)", Family.UNKNOWN, 0));
        }
        if (golemMods > 0) {
            ev.add(new Evidence("entities", "an attribute carries a golem: modifier (GolemPaper's 1.8 combat option)", Family.PAPER, 1).cfg(CFG_GOLEM_MODIFIER));
        }
        boolean golemCulling = cullFar && cullRefills >= 1;
        boolean plutoTeleport = enderFloat >= 2 && enderFloatMobs >= 2;
        boolean plutoChorus = chorusBranch >= 4 && chorusBranchChunks >= 2;
        if (plutoTeleport) {
            ev.add(new Evidence("entities", enderFloat + " enderman teleport(s) ended in mid-air, up to " + Math.round(enderFloatTop) + " blocks above the ground (" + enderGround
                    + " landed normally; Pluto checks the ground in the wrong column)", Family.PAPER, 2));
        }
        if (plutoChorus) {
            ev.add(new Evidence("chunks", chorusBranch + " of " + chorusAll + " chorus plants branch from the middle of a stalk in " + chorusBranchChunks
                    + " chunks (Pluto's chorus generation; it stays in the world after a jar change)", Family.PAPER, 1));
        }
        if (cullFar) {
            ev.add(new Evidence("chunks", "far chunks arrived without their bottom section" + (golemCulling ? " and were re-sent whole when you came closer" : "")
                    + " (GolemPaper section culling)", Family.PAPER, golemCulling ? 1 : 0).cfg(CFG_GOLEM_CULLING));
        }

        int c = chunks.get(), b = chunkBatches.get();
        if (c >= 8 && configPhase && !limbo) {
            if (b == 0) ev.add(new Evidence("chunks", c + " chunks, no chunk batches", Family.PAPER, 3));
            else ev.add(new Evidence("chunks", c + " chunks in " + b + " batches (vanilla-style)", Family.UNKNOWN, 0, true));
        }

        long ka = keepAliveIntervalMs();
        long firstKa = firstKeepAliveAt;
        long sinceJoin = joinedAt > 0 ? System.currentTimeMillis() - joinedAt : -1;
        boolean oldKeepAlive = proto <= PROTO_1_21_5;
        if ((ka > 0 || firstKa > 0) && proto < 340) {
            notes.add("Keep-alive timing is not compared, the server is older than 1.12.2.");
        } else if (ka <= 0 && firstKa > 0 && joinedAt > 0) {
            long first = firstKa - joinedAt;
            if (first <= 4000 && oldKeepAlive) ev.add(new Evidence("keep-alive", "first keep-alive " + Math.max(first, 0) + " ms after joining (Purpur's alternate keep-alive; Paper up to 1.21.5 waits 15 s)", Family.PAPER, 3).cfg(CFG_ALT_KEEPALIVE));
            else if (first <= 4000) ev.add(new Evidence("keep-alive", "first keep-alive " + Math.max(first, 0) + " ms after joining (Paper sends one every second)", Family.PAPER, 3));
            else if (first >= 13000 && first <= 18000 && oldKeepAlive) ev.add(new Evidence("keep-alive", "first keep-alive " + first / 1000 + " s after joining (vanilla's 15 s, which Paper kept up to 1.21.5)", Family.VANILLA, 1));
            else if (first >= 13000 && first <= 18000) ev.add(new Evidence("keep-alive", "first keep-alive " + first / 1000 + " s after joining (vanilla's 15 s)", Family.VANILLA, 2, true));
            else if (first >= 22000 && first <= 28000) ev.add(new Evidence("keep-alive", "first keep-alive " + first / 1000 + " s after joining (CraftBukkit/Spigot wait 25 s)", Family.BUKKIT, 3, true));
            else ev.add(new Evidence("keep-alive", "first keep-alive " + first / 1000 + " s after joining (no known pattern)", Family.UNKNOWN, 0));
        } else if (ka <= 0 && firstKa == 0 && sinceJoin > 18000 && proto >= 340) {
            ev.add(new Evidence("keep-alive", "no keep-alive in the first " + sinceJoin / 1000 + " s (not Paper's 1 s or vanilla's 15 s; CraftBukkit/Spigot wait 25 s)", Family.BUKKIT, 2, true));
        } else if (ka > 0) {
            if (ka < 4000 && oldKeepAlive) ev.add(new Evidence("keep-alive", "every ~" + ka + " ms (Purpur's alternate keep-alive; Paper up to 1.21.5 uses 15 s)", Family.PAPER, 3).cfg(CFG_ALT_KEEPALIVE));
            else if (ka < 4000) ev.add(new Evidence("keep-alive", "every ~" + ka + " ms (Paper sends one per second)", Family.PAPER, 3));
            else if (ka >= 20000 && ka <= 30000) ev.add(new Evidence("keep-alive", "every ~" + ka + " ms (Spigot uses 25 s)", Family.BUKKIT, 3, true));
            else if (ka >= 12000 && ka < 20000 && oldKeepAlive) ev.add(new Evidence("keep-alive", "every ~" + ka + " ms (vanilla timing, which Paper kept up to 1.21.5)", Family.VANILLA, 1));
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
            ev.add(new Evidence("login", "no vanilla dimension in the login packet " + levels + " (LOOHP Limbo world)", Family.CUSTOM, 2).cfg(CFG_LOOHP_LEVEL));
        }
        if (done && maxP < 0) {
            ev.add(new Evidence("login", "max players " + maxP + " in the login packet (LOOHP Limbo)", Family.CUSTOM, 1).cfg(CFG_LOOHP_MAXP));
        }
        if (noBrand) {
            ev.add(new Evidence("config", "no brand sent at all (vanilla, Paper and every proxy send one; QuasarMC does not)", Family.CUSTOM, 2));
        }
        if (tabShape != null && (picoTab || nanoTab || loohpTab)) {
            Evidence tabEntry = new Evidence("join", "your tab-list entry only carries " + tabShape.toLowerCase(Locale.ROOT).replace(',', '/')
                    + (picoTab ? " (PicoLimbo)" : nanoTab ? " (NanoLimbo)" : " (LOOHP Limbo)"), Family.CUSTOM, 1);
            ev.add(nanoTab ? tabEntry.cfg(CFG_NANO_TAB) : tabEntry);
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
            ev.add(new Evidence("login", "max players " + maxP + " (BungeeCord sends its tab size)", Family.UNKNOWN, 0)
                    .cfg("BungeeCord config.yml listeners[].tab_size (default 60, shows it)"));
        }

        String opNamespace = "op only: these commands default to op, so normal players never see them; spigot.yml commands.send-namespaced (default true) and commands.tab-complete (default 0)";
        if (namespaces.contains("paper")) ev.add(new Evidence("commands", "paper: namespace present", Family.PAPER, 3).cfg(opNamespace));
        if (namespaces.contains("purpur")) ev.add(new Evidence("commands", "purpur: namespace present", Family.PAPER, 3).cfg(opNamespace));
        if (namespaces.contains("folia")) ev.add(new Evidence("commands", "folia: namespace present", Family.PAPER, 3).cfg(opNamespace));
        if (namespaces.contains("spigot")) ev.add(new Evidence("commands", "spigot: namespace present", Family.BUKKIT, 1).cfg(opNamespace));
        if (roots.contains("divinemc") || namespaces.contains("divinemc")) {
            divine = true;
            divineWhy.add("divinemc command", CFG_DIVINE_COMMAND);
            ev.add(new Evidence("commands", "divinemc command present (DivineMC)", Family.PAPER, 3).cfg(CFG_DIVINE_COMMAND));
        }
        boolean plutoCmd = namespaces.contains("pluto") || (roots.contains("pluto") && twinFree(roots, "pluto"));
        boolean pufferfishCmd = namespaces.contains("pufferfish") || (roots.contains("pufferfish") && twinFree(roots, "pufferfish"));
        boolean plazmaCmd = namespaces.contains("plazma") || (roots.contains("plazma") && twinFree(roots, "plazma"));
        if (plutoCmd) ev.add(new Evidence("commands", "pluto command (Pluto's own command, shown to ops)", Family.PAPER, 3).cfg(CFG_OP_FORK_COMMAND));
        if (pufferfishCmd) {
            ev.add(new Evidence("commands", "pufferfish command (Pufferfish or Pluto, shown to ops)", Family.PAPER, plutoCmd ? 0 : 3).cfg(CFG_OP_FORK_COMMAND));
        }
        if (plazmaCmd) ev.add(new Evidence("commands", "plazma command (Plazma's own command, shown to ops)", Family.PAPER, 3).cfg(CFG_OP_FORK_COMMAND));
        boolean bukkitCmds = namespaces.contains("bukkit") || roots.contains("icanhasbukkit");
        if (bukkitCmds) ev.add(new Evidence("commands", "Bukkit commands present (bukkit:, icanhasbukkit)", Family.BUKKIT, 2).cfg(CFG_BUKKIT_TREE));
        boolean wrappedNeo = roots.contains("minecraft:neoforge");
        boolean loaderWrapped = bukkitCmds && !chatTypes.contains("paper:raw") && (wrappedNeo || roots.contains("minecraft:forge"));
        if (loaderWrapped) {
            String wrapCfg = "commands.yml aliases (an empty minecraft:neoforge or minecraft:forge alias removes it) and " + CFG_TAB_COMPLETE;
            ev.add(new Evidence("commands", "Bukkit wraps the mod loader's own command (" + (wrappedNeo ? "minecraft:neoforge" : "minecraft:forge") + ")", Family.MODDED_BUKKIT, 3).cfg(wrapCfg));
            ev.add((wrappedNeo ? new Evidence("commands", "the wrapped loader command is NeoForge's /neoforge", Family.NEOFORGE, 2)
                    : new Evidence("commands", "the wrapped loader command is Forge's /forge", Family.FORGE, 2)).cfg(wrapCfg));
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
        boolean golemNamespace = namespaces.contains("golempaper");
        boolean golemMembar = (roots.contains("membar") || roots.contains("minecraft:membar")) && twinFree(roots, "membar")
                && (roots.contains("tpsbar") || roots.contains("minecraft:tpsbar")) && !roots.contains("rambar") && !roots.contains("minecraft:rambar");
        boolean golem = golemNamespace || golemMembar;
        if (golemNamespace) ev.add(new Evidence("commands", "golempaper:golem command (GolemPaper)", Family.PAPER, 3).cfg(CFG_GOLEM_COMMAND));
        if (golemMembar) {
            ev.add(new Evidence("commands", "membar next to tpsbar, without Purpur's rambar (GolemPaper's own command, shown to ops)", Family.PAPER, golemNamespace ? 0 : 3).cfg(CFG_GOLEM_COMMAND));
        }
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
            ev.add(new Evidence("commands", "list/random are offered without op (Paper hides them)", Family.BUKKIT, 1, true)
                    .cfg("commands.yml ignore-vanilla-permissions (default false, shows it), permissions.yml minecraft.command.list/random; " + CFG_TAB_COMPLETE));
        }
        boolean foliaTree = bukkitCmds && roots.contains("msg") && !roots.contains("teammsg") && !roots.contains("trigger");
        if (foliaTree) ev.add(new Evidence("commands", "no teammsg/tm/trigger (Folia removes them)", Family.PAPER, 0));

        if (sectionSuffix && proxy == null && !leaves) {
            divine = true;
            divineWhy.add("hidden colour-reset code after the brand", CFG_DIVINE_BRAND);
            ev.add(new Evidence("brand", "brand ends with a hidden colour-reset code (DivineMC)", Family.PAPER, 0).cfg(CFG_DIVINE_BRAND));
        }

        if (pingName != null && !pingName.isBlank()) {
            Evidence pingEvidence = new Evidence("ping", "server list says \"" + pingName + "\"", Family.UNKNOWN, 0);
            String pingCfg = pingConfig(pingName);
            ev.add(pingCfg == null ? pingEvidence : pingEvidence.cfg(pingCfg));
        }
        int sample = 0;
        if (data != null && data.playerList != null) {
            for (Component line : data.playerList) {
                if (!(line.getContents() instanceof TranslatableContents t && t.getKey().startsWith("multiplayer.status"))) sample++;
            }
        }
        boolean uncappedSample = sample > 12 && proxy == null;
        if (uncappedSample) {
            ev.add(new Evidence("ping", "server list shows " + sample + " player names (Spigot and Paper cap it at 12)", Family.BUKKIT, 1, true).cfg(CFG_SAMPLE));
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
            ev.add(new Evidence("join", "no advancements packet after joining (advancements or criterion triggers disabled)", Family.UNKNOWN, 0).cfg(CFG_NO_ADVANCEMENTS));
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
            int radius = trackRadius;
            boolean miscBeyond = radius >= 3 && farMisc > SPIGOT_MISC_LIMIT;
            boolean otherBeyond = radius >= 5 && farOther > SPIGOT_OTHER_LIMIT;
            boolean beyondSpigotMisc = miscBeyond || otherBeyond;
            if (miscBeyond) {
                ev.add(new Evidence("entities", "a " + farMiscType + " was sent " + Math.round(farMisc) + " blocks away (beyond Spigot's misc 32 / other 64 default)", Family.UNKNOWN, 0).cfg(CFG_MISC_TRACKING));
            }
            if (otherBeyond) {
                ev.add(new Evidence("entities", "a " + farOtherType + " was sent " + Math.round(farOther) + " blocks away (beyond Spigot's misc 32 / other 64 default)", Family.UNKNOWN, 0).cfg(CFG_MISC_TRACKING));
            }
            boolean spigotProven = false;
            if (respawnRadius > 0) {
                software = "Spigot";
                spigotProven = true;
                notes.name("Spigot confirmed by the packets (view distance re-sent after respawning).", null, "Spigot".equals(known));
                if (beyondSpigot) {
                    notes.tag("Mobs are sent from beyond Spigot's default 48 blocks, so its entity-tracking-range in spigot.yml was raised.", CFG_TRACKING);
                }
            } else if (uncappedSample && !"Spigot".equals(known)) {
                software = "CraftBukkit";
                notes.name("CraftBukkit: the server list sample is not capped at 12 names like Spigot's.", CFG_SAMPLE, "CraftBukkit".equals(known));
            } else if (beyondSpigot) {
                if ("Spigot".equals(known)) {
                    notes.tag("Brand says Spigot, but mobs are sent from beyond Spigot's default 48 blocks (raised entity-tracking-range in spigot.yml, or CraftBukkit).", CFG_TRACKING);
                } else {
                    software = "CraftBukkit";
                    notes.name("CraftBukkit: Bukkit server without Spigot's entity tracking limits (mobs sent " + Math.round(farthest)
                            + " blocks away; a Spigot with a raised entity-tracking-range looks the same).", CFG_TRACKING, "CraftBukkit".equals(known));
                }
            } else if (spigotRange) {
                if ("CraftBukkit".equals(known)) {
                    notes.tag("Brand says CraftBukkit, but mobs stop at Spigot's 48-block tracking range (Spigot with a changed brand, or a low entity-broadcast-range-percentage).", CFG_TRACKING);
                } else {
                    if (!"Spigot".equals(known)) software = "Spigot";
                    spigotProven = true;
                    notes.name("Spigot: mobs stop at Spigot's default 48-block tracking range (a CraftBukkit with a low entity-broadcast-range-percentage looks the same).",
                            CFG_TRACKING, "Spigot".equals(known));
                }
            } else if (beyondSpigotMisc) {
                if ("Spigot".equals(known)) {
                    notes.tag("Brand says Spigot, but misc/vehicle entities are sent beyond Spigot's default 32/64 blocks (raised entity-tracking-range in spigot.yml, or CraftBukkit).", CFG_MISC_TRACKING);
                } else {
                    software = "CraftBukkit";
                    notes.name("CraftBukkit: misc/vehicle entities sent beyond Spigot's default 32/64 blocks (a " + (miscBeyond ? farMiscType : farOtherType)
                            + " " + Math.round(miscBeyond ? farMisc : farOther) + " blocks away).", CFG_MISC_TRACKING, "CraftBukkit".equals(known));
                }
            }
            if (spigotProven && beyondSpigotMisc) {
                notes.tag("Misc/vehicle entities are sent beyond Spigot's default 32/64 blocks, so its entity-tracking-range in spigot.yml was raised.", CFG_MISC_TRACKING);
            }
        } else if (family == Family.PAPER && beyondPaper) {
            notes.tag("Mobs are sent from beyond Paper's default 96 blocks, so its entity-tracking-range was raised.", CFG_TRACKING);
        }

        if (family == Family.PAPER) {
            String known = knownName(backendBrand);
            String fork = null;
            String why = null;
            String whyCfg = null;
            boolean conflict = false;
            Reasons universeReasons = new Reasons();
            if (universeSpeed) universeReasons.add("exact Speed effect modifier", CFG_UNIVERSE_SPEED);
            if (universeRain) universeReasons.add("rain without the start/stop event", null);
            if (universeFreeze) universeReasons.add("fresh mobs past 64 blocks frozen, odd ids never move", CFG_UNIVERSE_AI);
            if (universeChunks) universeReasons.add("chunks sent out of order by parallel threads", CFG_UNIVERSE_CHUNKS);
            if (universeEquipment) universeReasons.add("mob equipment sent twice by the async tracker", CFG_UNIVERSE_TRACKER);
            if (universeTab) universeReasons.add("players unlisted from the tab list", CFG_UNIVERSE_TAB);
            String statsWhy = "time statistics in steps of 20";
            boolean universeAgainst = purpurFamily || hashMap || rainWithFlip > 0 || floatSpeed > 0 || (statsOffGrid && playTimeMoved);
            String leafWhy = "block entities hashed with Leaf's BlockPos hash";
            String purpurWhy = purpurAttrs >= 2 ? "Purpur mob attributes" : purpurTime ? "Purpur's per-tick time packets" : "Purpur's 15 HP zombie horses";
            String purpurCfg = purpurAttrs >= 2 ? null : purpurTime ? CFG_ADVANCE_TIME : CFG_ZOMBIE_HORSE;
            String notUniverse = healthQuiet ? "no max_health packet on health changes"
                    : colliderQuietSig ? "no position-sync stream for parked vehicles"
                    : golemRemovals ? "batched entity removals" : null;
            Reasons golemReasons = new Reasons();
            if (golemRemovals) golemReasons.add(rmRespawn ? "entity removals batched into one packet, also right after respawning" : "entity removals batched into one packet", null);
            if (colliderHeartbeat) golemReasons.add("parked vehicles and shulkers get a plain move heartbeat instead of a position-sync stream", CFG_GOLEM_HEARTBEAT);
            if (leafHash && proto >= PROTO_26_1 && noPurpurMobs) golemReasons.add("Leaf's BlockPos hash without Purpur mob attributes", null);
            if (healthQuiet && noPurpurMobs) golemReasons.add("no max_health packet on health changes and no Purpur mob attributes", null);
            List<String> golemAgainst = new ArrayList<>();
            if (colliderStream) golemAgainst.add("parked vehicles get Paper's position-sync stream");
            if (unbatchedRemovals) golemAgainst.add("entity removals arrive one per packet");
            if (healthSync) golemAgainst.add("every health change comes with a max_health packet");
            if (hashMap) golemAgainst.add("the attribute lists come from a hash map");
            if (vanillaHash) golemAgainst.add("block entities use Mojang's BlockPos hash");
            int golemTrackerAgainst = golemAgainst.size();
            if (purpurFamily) golemAgainst.add("Purpur mob attributes or time packets");
            Reasons golemSupport = new Reasons();
            if (colliderQuietSig && !colliderHeartbeat) golemSupport.add("parked vehicles silent for 5 s", null);
            if (golemMods > 0) golemSupport.add("a golem: attribute modifier", CFG_GOLEM_MODIFIER);
            if (cullFar) golemSupport.add(golemCulling ? "far chunks culled below the surface and re-sent" : "far chunks sent without their bottom section", CFG_GOLEM_CULLING);
            boolean golemPackets = !golemReasons.isEmpty() && golemAgainst.isEmpty();
            if (golem) {
                fork = "GolemPaper";
                why = (golemNamespace ? "golempaper:golem command" : "membar command") + (golemReasons.isEmpty() ? "" : ", " + golemReasons.text());
                whyCfg = golemReasons.fixed ? null : CFG_GOLEM_COMMAND;
            }
            else if (shredded) { fork = "ShreddedPaper"; why = "mpmap command"; }
            else if (shreddedTick && (shreddedCmd || purpurFamily || "ShreddedPaper".equals(known))) {
                fork = "ShreddedPaper";
                why = "join tick order";
                whyCfg = shreddedCmd || "ShreddedPaper".equals(known) ? null : purpurCfg;
            }
            else if (plutoCmd) { fork = "Pluto"; why = "pluto command"; whyCfg = CFG_OP_FORK_COMMAND; }
            else if (plazmaCmd) { fork = "Plazma"; why = "plazma command"; whyCfg = CFG_OP_FORK_COMMAND; }
            else if (canvas || canvasSorted) {
                fork = "Canvas";
                why = canvas && canvasSorted ? "ticking state without ticking step, block entities in section order"
                        : canvas ? "ticking state without ticking step" : "block entities in section order";
            }
            else if (leaves) { fork = "Leaves"; why = CFG_BLADEREN.equals(leavesCfg) ? "bladeren protocol" : "empty channel list with the Leaves brand"; whyCfg = leavesCfg; }
            else if (divine) { fork = "DivineMC"; why = divineWhy.isEmpty() ? "DivineMC-only packets" : divineWhy.text(); whyCfg = divineWhy.config(); }
            else if (golemPackets) {
                fork = "GolemPaper";
                why = golemSupport.isEmpty() ? golemReasons.text() : golemReasons.text() + ", " + golemSupport.text();
                whyCfg = golemReasons.config();
            }
            else if (purpurTime && !leafHash) { fork = "Purpur"; why = "per-tick time packets"; whyCfg = CFG_ADVANCE_TIME; }
            else if (leafHash && purpurFamily) {
                fork = "Leaf";
                why = leafWhy + ", " + purpurWhy;
                whyCfg = purpurCfg;
            } else if (leafHash && proto >= PROTO_26_1) {
                if (colliderStream || unbatchedRemovals) {
                    fork = "Leaf";
                    why = leafWhy + ", " + (colliderStream ? "parked vehicles get Paper's position-sync stream" : "entity removals one per packet") + " (rules out GolemPaper)";
                } else {
                    fork = "Leaf".equals(known) || "GolemPaper".equals(known) ? known : "Leaf or GolemPaper";
                    why = leafWhy + ", shared by Leaf and GolemPaper";
                }
            } else if (leafHash) {
                Reasons reasons = new Reasons().addAll(universeReasons);
                if (statsStep20 && !"Gale".equals(known) && !"Leaf".equals(known)) reasons.add(statsWhy, null);
                if (mhBare >= 2) reasons.add("mobs without Purpur's max_health attribute", null);
                if (!reasons.isEmpty() && universeAgainst) conflict = true;
                if (!reasons.isEmpty() && !universeAgainst && notUniverse == null) {
                    if (noAdvancements) reasons.add("criterion triggers disabled (UniverseSpigot recommended config)", CFG_NO_ADVANCEMENTS);
                    fork = "UniverseSpigot";
                    why = leafWhy + ", " + reasons.text();
                    whyCfg = reasons.config();
                } else if (notUniverse != null) {
                    fork = "Leaf".equals(known) || "GolemPaper".equals(known) ? known : "Leaf or GolemPaper";
                    why = leafWhy + ", " + notUniverse + " (rules out UniverseSpigot)";
                } else {
                    boolean golemKnown = "GolemPaper".equals(known);
                    fork = "Leaf".equals(known) || "UniverseSpigot".equals(known) || golemKnown ? known : "Leaf or UniverseSpigot";
                    why = leafWhy + (golemKnown ? ", shared by Leaf, UniverseSpigot and GolemPaper" : ", shared by Leaf and UniverseSpigot");
                }
            } else if (purpurFamily && !hashMap && !mojangHash && (proto >= PROTO_26_1 ? seconds >= 3 && !timeFrozen : arrayMap || (ownListArray && "Leaf".equals(known)))) {
                fork = "Leaf";
                why = proto >= PROTO_26_1 ? purpurWhy + " without Purpur's time packets" : purpurWhy + " in an array attribute map";
                whyCfg = proto >= PROTO_26_1 ? CFG_ADVANCE_TIME : purpurCfg;
            } else if (purpurFamily && hashMap && proto < PROTO_26_1) {
                fork = "Purpur";
                why = purpurWhy + " in a hash attribute map";
                whyCfg = purpurCfg;
            } else if (!universeReasons.isEmpty() || (statsStep20 && !"Gale".equals(known))) {
                if (universeAgainst) conflict = true;
                else if (notUniverse == null) {
                    fork = "UniverseSpigot";
                    Reasons reasons = new Reasons().addAll(universeReasons);
                    if (statsStep20) reasons.add(statsWhy, null);
                    why = reasons.text();
                    whyCfg = reasons.config();
                }
            } else if ((arrayMap || ownListArray) && !purpurFamily && "UniverseSpigot".equals(known) && floatSpeed == 0 && rainWithFlip == 0) {
                fork = "UniverseSpigot";
                why = "array attribute map without Purpur's attributes";
            } else if (plutoTeleport && !purpurFamily && !arrayMap) {
                fork = "Pluto";
                why = "endermen land in mid-air after teleporting";
            } else if (plutoChorus && !purpurFamily && !arrayMap) {
                fork = "Pluto";
                why = "End chorus plants branch from the middle of a stalk; they were generated by Pluto and stay in the world if the jar was changed later";
            } else if (pufferfishCmd && !purpurFamily && !arrayMap) {
                fork = proto >= PROTO_26_1 ? "Pluto" : "Pufferfish";
                why = "pufferfish command";
                whyCfg = CFG_OP_FORK_COMMAND;
            } else if (dabOn && !purpurFamily && !arrayMap) {
                fork = "Pluto".equals(known) || proto >= PROTO_26_1 ? "Pluto" : "Pufferfish";
                why = "Dynamic Activation of Brain slows distant mob AI";
                whyCfg = CFG_DAB;
            } else if (foliaTree) {
                fork = "Folia";
                why = "teammsg/tm/trigger missing";
            }

            if (shreddedTick && !"ShreddedPaper".equals(fork)) {
                notes.tag("Join tick order matches ShreddedPaper, but nothing else confirms it, so the name is not changed.", null);
            }
            if (conflict) {
                notes.add("Conflicting UniverseSpigot signals: the Speed modifier or the weather says UniverseSpigot, other packets do not (a per-player weather plugin can cause this).");
            }
            if (purpurFamily && fork == null && proto >= PROTO_26_1 && timeFrozen) {
                notes.add("Time is frozen (gamerule advance_time off or /tick freeze), so Purpur's per-tick time packets cannot show and their absence says nothing.");
            }
            boolean leafSpoof = "Leaf".equals(known) && mojangHash;
            if (leafSpoof) {
                notes.tag("Brand says Leaf, but block entities use Mojang's BlockPos hash (every Leaf build for 26.2 and newer uses its own, brand likely spoofed).", null);
            } else if (vanillaHash && ("Leaf".equals(known) || "UniverseSpigot".equals(known))) {
                notes.tag("Brand says " + known + ", but block entities use Mojang's BlockPos hash (an older build from before January 2026, or not " + known + ").", null);
            }
            if (!"GolemPaper".equals(fork) && !golemReasons.isEmpty() && !golemAgainst.isEmpty()) {
                notes.add("Conflicting GolemPaper signals: " + golemReasons.text() + ", but " + String.join(", ", golemAgainst) + ".");
            } else if (!"GolemPaper".equals(fork) && golemReasons.isEmpty() && !golemSupport.isEmpty() && golemAgainst.isEmpty()) {
                notes.add("Hints at GolemPaper (" + golemSupport.text() + "), but no confirmed GolemPaper signal yet.");
            }
            boolean golemSpoof = "GolemPaper".equals(known) && !golem && golemReasons.isEmpty() && golemTrackerAgainst >= 2;
            if (golemSpoof) {
                notes.tag("Brand says GolemPaper, but " + String.join(", ", golemAgainst.subList(0, golemTrackerAgainst)) + " (GolemPaper does none of this, brand likely spoofed).", null);
                software = fork != null && !"GolemPaper".equals(fork) ? fork : "Paper-based";
                if (fork != null && !"GolemPaper".equals(fork)) notes.named(whyCfg, false);
                else notes.structural();
            } else if (fork != null && known != null && builtOn(known, fork)) {
                notes.tag("Packets match " + fork + ", which " + known + " is built on.", whyCfg);
                notes.named(brandConfig(known) == null ? null : whyCfg, true);
                fork = known;
                why = null;
                software = known;
            } else if (fork != null) {
                if (known != null && !known.equals(fork) && !known.equals("Paper")) {
                    notes.name("Brand says " + known + ", but the packets point to " + fork + " (" + why + ").", whyCfg, false);
                } else if ("Paper".equals(known)) {
                    notes.name("Brand says Paper, but the packets point to " + fork + " (" + why + ").", whyCfg, false);
                } else if (fork.contains(" or ")) {
                    notes.name("The packets point to " + fork + " (" + why + "), nothing seen so far tells the two apart.", whyCfg, false);
                } else {
                    notes.tag(fork + " confirmed by the packets (" + why + ").", whyCfg);
                    notes.named(fork.equals(known) && brandConfig(known) == null ? null : whyCfg, fork.equals(known));
                }
                software = fork;
            } else if (purpurFamily) {
                if (known != null && (known.equals("Purpur") || builtOn(known, "Purpur"))) {
                    notes.tag(mojangHash ? "Purpur-family mobs, and block entities use Mojang's BlockPos hash, so this is not Leaf."
                            : "Purpur-family mobs. Telling Purpur from Leaf needs more attribute kinds in view (horses, other players, or a death and respawn).", purpurCfg);
                } else {
                    software = "Purpur-based";
                    notes.name("Purpur-family " + (purpurAttrs >= 2 ? "mob attributes" : purpurTime ? "time packets" : "zombie horses")
                            + (known != null ? ", although the brand says " + known : "") + ".", purpurCfg, false);
                }
            } else if (known != null && !known.equals("Paper")) {
                if (known.equals("Leaf") && hashMap) {
                    notes.tag("Brand says Leaf, but the mob attributes come from a hash map (not Leaf, brand likely spoofed).", null);
                    software = "Paper-based";
                    notes.structural();
                } else if (leafSpoof) {
                    software = "Paper-based";
                    notes.structural();
                } else if ((known.equals("Purpur") || known.equals("Leaf")) && zombieVanilla) {
                    notes.tag("Brand says " + known + ", but zombie horses spawn with vanilla's 25 HP (Purpur config changed, or not Purpur).", CFG_ZOMBIE_HORSE);
                } else if (known.equals("UniverseSpigot") && (floatSpeed > 0 || hashMap || rainWithFlip > 0)) {
                    notes.tag("Brand says UniverseSpigot, but " + (floatSpeed > 0 ? "the Speed modifier is vanilla's" : rainWithFlip > 0 ? "rain changes come with the start/stop event" : "the mob attributes come from a hash map") + " (brand likely spoofed).", null);
                    software = "Paper-based";
                    notes.structural();
                } else if ((known.equals("Pufferfish") || known.equals("Pluto")) && dabOff) {
                    notes.tag("Brand says " + known + ", but distant mobs run full-speed AI (Dynamic Activation of Brain turned off, or the brand was changed).", CFG_DAB);
                } else if (known.equals("Pufferfish") || known.equals("Pluto")) {
                    notes.tag(known + " sends Paper's join packets. It is confirmed by mob AI timing: stand still for about 30 s with animals 29-32 blocks away and no other player near them"
                            + (known.equals("Pluto") ? ", by endermen landing in mid-air after a teleport" : "") + ", or by its own command if you are op.", brandConfig(known));
                } else {
                    notes.tag("No packet signal for " + known + " yet, the fork name comes from the brand" + (pingAgrees(pingName, known) && !tabEarly && !linksEarly ? " and the server list name." : "."), brandConfig(known));
                }
            }
        } else if (family == Family.HYBRID) {
            String named = arclightLoader != null ? "Arclight (" + arclightLoader + ")" : hybridName(brandLower);
            boolean namedByPing = named == null && pingHybrid != null;
            if (namedByPing) named = pingHybrid;
            if (named == null && purpurTime) {
                software = "Youer (NeoForge + Purpur)";
                notes.name("NeoForge negotiation with Purpur's per-tick time packets; Youer is the only known NeoForge + Purpur hybrid.", CFG_ADVANCE_TIME, false);
            } else {
                software = named != null ? named : "NeoForge + Paper hybrid";
                notes.add("NeoForge networking together with Paper/Purpur server behaviour.");
                if (namedByPing) notes.name(named + " is named from the server list version \"" + pingName + "\".", hybridConfig(named), false);
                else if (named != null) notes.named(arclightLoader != null ? null : hybridConfig(named), !arclightPing);
                if (named != null && !named.equals("Youer") && purpurTime) {
                    notes.tag("This server names itself " + named + ", but Purpur's per-tick time packets point to Youer (the only known NeoForge + Purpur hybrid).", CFG_ADVANCE_TIME);
                }
            }
        } else if (family == Family.CUSTOM) {
            String kind = null;
            if (veloflameLimbo) {
                software = "VeloFlame verification limbo";
                notes.name("This is VeloFlame's antibot check world, the real server comes after the reconnect/transfer.", CFG_VF_LIMBO, "veloflame".equals(brandLower));
            } else if (sonarLimbo) {
                software = "Sonar verification limbo";
                notes.name("This is Sonar's antibot check world, the real server comes after you pass the verification.", sonarLogin ? null : CFG_SONAR_NAME, false);
            } else if (limboKind != null) {
                kind = limboKind;
                software = limboKind;
                List<String> limboKeys = new ArrayList<>();
                if ("NanoLimbo".equals(limboKind) && !nanoLogin && !nanoAbilities && !nanoTeleport) {
                    if (nanoTab) limboKeys.add(CFG_NANO_TAB);
                    if ("NanoLimbo".equals(renamed)) limboKeys.add(CFG_NANO_NAME);
                } else if ("LOOHP Limbo".equals(limboKind) && !loohpBatches && !(biomeServerNbt && !packsReceived) && !loohpTab) {
                    if (noVanillaLevel) limboKeys.add(CFG_LOOHP_LEVEL);
                    if (done && maxP < 0) limboKeys.add(CFG_LOOHP_MAXP);
                }
                String limboCfg = limboKeys.isEmpty() ? null : String.join("; ", limboKeys);
                if (byBrand != null && !byBrand.equals(limboKind)) notes.name("Brand says " + byBrand + ", but the packets look like " + limboKind + ".", limboCfg, false);
                else notes.name(limboKind + " confirmed by the packets" + (byBrand != null ? " and the brand." : " (brand not needed)."), limboCfg, byBrand != null);
            } else if (byBrand != null) {
                kind = byBrand;
                software = byBrand;
                notes.name(byBrand + " recognised from the brand, the packets only show a custom server.",
                        "NanoLimbo".equals(byBrand) ? "NanoLimbo settings.yml brandName.enable (default true, shows it) and brandName.content" : null, true);
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
            if (arclightPing) notes.name("Arclight is named from the server list version \"" + pingName + "\".", null, false);
            else if (arclightLoader == null && namedByPing) notes.name(named + " is named from the server list version \"" + pingName + "\".", hybridConfig(named), false);
            else if (arclightLoader != null) notes.named(null, arclightBrand);
            else if (named != null) notes.named(hybridConfig(named), true);
            String how = loaderWrapped ? "Bukkit's command map wraps " + base + "'s own command (" + (wrappedNeo ? "minecraft:neoforge" : "minecraft:forge") + "), so " + base + " runs under a Bukkit layer."
                    : forgeHybrid && "Forge".equals(base) ? "Forge (FML) signals together with Bukkit commands."
                    : arclightPack && !arclightBrand ? "Arclight's own data pack is offered in the configuration (brand not needed)."
                    : timedHybrid || arclightBrand ? base + " networking with Bukkit commands and Spigot-style timings."
                    : null;
            if (how != null) notes.add(how);
        }

        if (done && configPhase && roots.isEmpty() && family != Family.CUSTOM && System.currentTimeMillis() - doneAt > 3000) {
            notes.tag("No commands were offered at all; Bukkit-based servers do this when spigot.yml commands.tab-complete is below 0, so the command-based signals are missing rather than negative.", CFG_TAB_COMPLETE);
        }
        if (family == Family.UNKNOWN && knownName(backendBrand) != null) {
            software = knownName(backendBrand) + " (brand only)";
            notes.name("Only the brand identifies this, the packet signals were inconclusive (rejoin for a cleaner read).", brandConfig(knownName(backendBrand)), true);
        }
        if (limboApi || brandLower.startsWith("limboapi (") || software.startsWith("LimboAPI")) proxy = proxy == null ? "Velocity + LimboAPI" : appendProxy(proxy, "LimboAPI");
        if (sonarLimbo) proxy = proxy == null ? "Velocity + Sonar (verification)" : appendProxy(proxy, "Sonar (verification)");
        if (notes.nameOrigin == Origin.CONFIG && !notes.nameFromBrand) {
            if ("high".equals(confidence)) confidence = "medium";
            notes.add("Only config-dependent signals name " + software + " (see the config tags), so the name is less certain than the structure.");
        }

        return new Result(software, family, rawBrand == null ? null : strip(rawBrand, "server brand"), proxy, confidence, ev, notes.list, notes.nameOrigin, notes.nameConfig);
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
                    + " golemRm=" + batchedRemovals + "/" + batchedRemovalMax + "/" + singleRemovalBursts + (respawnBatched ? "/respawn" : "")
                    + " collider=" + colliderStreams + "/" + colliderMoves + "/" + colliderQuiet + ":" + colliders.size()
                    + " hp=" + healthBare + "/" + healthSynced + " covered=" + coveredVanilla + "/" + coveredPurpur + " golemMod=" + golemModifiers
                    + " cull=" + nearFloors + "/" + nearHollow + "/" + farCulled + "/" + farFloors + "/" + culledRefills + (cullContrast ? "/contrast" : "")
                    + " ender=" + enderFloats + "/" + enderGrounded + ":" + enderFloatIds.size() + " chorus=" + chorusOdd + "/" + chorusPlants + ":" + chorusOddChunks + "/" + chorusScanned
                    + " clock=" + (clockPaused ? "paused" : "running") + (tickFrozen ? "/frozen" : "") + " mhBare=" + pairingBareNonPurpur
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

    private static boolean builtOn(String known, String fork) {
        for (String p = LINEAGE.get(known); p != null; p = LINEAGE.get(p)) if (p.equals(fork)) return true;
        return false;
    }

    private static String brandConfig(String known) {
        if (known == null) return null;
        return switch (known) {
            case "Purpur", "Plazma", "ShreddedPaper" -> "purpur.yml settings.server-mod-name (default \"" + known + "\", shows it)";
            case "DivineMC" -> "purpur.yml settings.rebrand.f3-name (default \"DivineMC\", shows it)";
            case "Leaf" -> "leaf-global.yml misc.rebrand.server-mod-name (default \"Leaf\", shows it)";
            case "Leaves" -> "leaves.yml settings.misc.server-mod-name (default \"Leaves\", shows it)";
            case "Canvas" -> "canvas-server.yml server-mod-name (default \"Canvas\", shows it)";
            case "FlameMC" -> "canvas-server.yml server-mod-name (default \"FlameMC\", shows it)";
            default -> null;
        };
    }

    private static String hybridConfig(String named) {
        if (named == null) return null;
        return switch (named) {
            case "Youer" -> "purpur.yml settings.server-mod-name (default \"Youer\", shows it)";
            case "Mohist" -> "mohist-config/mohist.yml server_mod_name (default \"mohist\", shows it)";
            case "Magma" -> "Magma's server mod name setting (key not checked, the default shows it)";
            default -> null;
        };
    }

    private static String pingConfig(String pingName) {
        String lower = pingName.toLowerCase(Locale.ROOT).trim();
        if (lower.startsWith("gate ") || lower.contains(" arclight/")) return null;
        if (lower.startsWith("velocity") || lower.startsWith("veloflame")) return CFG_PING_VELOCITY;
        if (lower.startsWith("bungeecord") || lower.startsWith("waterfall")) return CFG_PING_BUNGEE;
        String first = lower.split("\\s+")[0];
        return switch (first) {
            case "youer" -> hybridConfig("Youer");
            case "mohist" -> hybridConfig("Mohist");
            case "magma" -> hybridConfig("Magma");
            case "purpur", "plazma", "shreddedpaper" -> "purpur.yml settings.server-mod-name (shows the fork name by default)";
            case "divinemc" -> "purpur.yml settings.rebrand.server-mod-name (default \"DivineMC\", shows it)";
            case "leaf" -> brandConfig("Leaf");
            case "leaves" -> brandConfig("Leaves");
            case "canvas" -> brandConfig("Canvas");
            case "flamemc" -> brandConfig("FlameMC");
            default -> null;
        };
    }

    private static String name(Family family, String brand, List<Evidence> ev, Notes notes, boolean translated) {
        String known = knownName(brand);
        if (family != Family.UNKNOWN) notes.structural();
        switch (family) {
            case PAPER -> {
                if (known != null && PAPER_FORKS.contains(known)) {
                    notes.named(brandConfig(known), true);
                    return known;
                }
                if (known != null) notes.add("Brand claims \"" + known + "\", but the structure is Paper-based (brand likely spoofed).");
                else if (brand != null && !brand.isBlank()) notes.add("Brand \"" + brand + "\" is not a fork Hontun can confirm, the packets say Paper-based.");
                return "Paper-based";
            }
            case BUKKIT -> {
                if ("Spigot".equals(known) || "CraftBukkit".equals(known)) {
                    notes.named(null, true);
                    return known;
                }
                if (known != null && PAPER_FORKS.contains(known)) {
                    if (translated || !contradictsPaper(ev)) {
                        notes.name("Paper-only markers are not visible here, so the " + known + " brand is taken as-is.", brandConfig(known), true);
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
