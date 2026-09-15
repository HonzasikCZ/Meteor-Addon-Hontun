package cz.honzasik.hontun.utils;

import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;
import net.minecraft.server.packs.repository.KnownPack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VersionKeeper {
    public static volatile String version = "unknown";
    public static final List<String> knownPacks = Collections.synchronizedList(new ArrayList<>());

    public static void gotPacket(ClientboundSelectKnownPacks packet) {
        knownPacks.clear();
        for (KnownPack pack : packet.knownPacks()) {
            if (pack.isVanilla() && "core".equals(pack.id())) {
                version = pack.version();
            }
            knownPacks.add(pack.namespace() + ":" + pack.id() + " (v" + pack.version() + ")");
        }
    }

    public static void clear() {
        version = "unknown";
        knownPacks.clear();
    }
}
