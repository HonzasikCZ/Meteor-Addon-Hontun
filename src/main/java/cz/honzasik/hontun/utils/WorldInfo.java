package cz.honzasik.hontun.utils;

import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorldInfo {
    public static volatile boolean captured = false;
    public static volatile String dimension = "";
    public static volatile long hashedSeed = 0L;
    public static final List<String> levels = Collections.synchronizedList(new ArrayList<>());
    public static volatile boolean secureChat = false;
    public static volatile boolean hardcore = false;
    public static volatile int maxPlayers = 0;
    public static volatile int chunkRadius = 0;
    public static volatile int simulationDistance = 0;

    public static void gotLogin(ClientboundLoginPacket packet) {
        dimension = packet.commonPlayerSpawnInfo().dimension().identifier().toString();
        hashedSeed = packet.commonPlayerSpawnInfo().seed();
        levels.clear();
        for (ResourceKey<Level> lvl : packet.levels()) {
            levels.add(lvl.identifier().toString());
        }
        secureChat = packet.enforcesSecureChat();
        hardcore = packet.hardcore();
        maxPlayers = packet.maxPlayers();
        chunkRadius = packet.chunkRadius();
        simulationDistance = packet.simulationDistance();
        captured = true;
    }

    public static void clear() {
        captured = false;
        dimension = "";
        hashedSeed = 0L;
        levels.clear();
        secureChat = false;
        hardcore = false;
        maxPlayers = 0;
        chunkRadius = 0;
        simulationDistance = 0;
    }
}
