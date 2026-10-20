package cz.honzasik.hontun.utils;

import cz.honzasik.hontun.mixin.network.software.TagPayloadAccessor;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagNetworkSerialization;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class RealVersionTags implements RealVersion.Tags {
    private final Map<String, Map<Identifier, IntList>> byRegistry = new HashMap<>();

    public RealVersionTags(Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> config,
                           Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> play) {
        add(config);
        add(play);
    }

    private void add(Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> tags) {
        if (tags == null) return;
        for (Map.Entry<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> e : tags.entrySet()) {
            Map<Identifier, IntList> payload = ((TagPayloadAccessor) (Object) e.getValue()).hontun$tags();
            if (payload != null) byRegistry.put(e.getKey().identifier().getPath(), payload);
        }
    }

    @Override
    public int tagCount(String registry) {
        Map<Identifier, IntList> tags = byRegistry.get(registry);
        return tags == null ? 0 : tags.size();
    }

    @Override
    public int size(String registry, String tag) {
        IntList list = list(registry, tag);
        return list == null ? -1 : list.size();
    }

    @Override
    public int contains(String registry, String tag, String entry) {
        IntList list = list(registry, tag);
        if (list == null) return -1;
        int id = id(registry, entry);
        if (id < 0) return -1;
        return list.contains(id) ? 1 : 0;
    }

    private IntList list(String registry, String tag) {
        Map<Identifier, IntList> tags = byRegistry.get(registry);
        if (tags == null) return null;
        Identifier key = Identifier.tryParse("minecraft:" + tag);
        return key == null ? null : tags.get(key);
    }

    private static int id(String registry, String entry) {
        Identifier key = Identifier.tryParse("minecraft:" + entry);
        if (key == null) return -1;
        return switch (registry) {
            case "block" -> id(BuiltInRegistries.BLOCK, key);
            case "item" -> id(BuiltInRegistries.ITEM, key);
            case "fluid" -> id(BuiltInRegistries.FLUID, key);
            case "entity_type" -> id(BuiltInRegistries.ENTITY_TYPE, key);
            default -> -1;
        };
    }

    private static <T> int id(Registry<T> registry, Identifier key) {
        if (!registry.containsKey(key)) return -1;
        Optional<T> value = registry.getOptional(key);
        return value.map(registry::getId).orElse(-1);
    }
}
