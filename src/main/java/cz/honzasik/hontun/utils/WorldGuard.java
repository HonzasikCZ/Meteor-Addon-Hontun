package cz.honzasik.hontun.utils;

import cz.honzasik.hontun.Hontun;
import cz.honzasik.hontun.modules.AntiExploit;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;

public final class WorldGuard {
    public static final int MAX_METADATA_VALUES = 1024;
    public static final int NAME_CHARS = 8192;
    public static final int NAME_NODES = 2048;
    public static final int TEXT_CHARS = 32768;
    public static final int TEXT_NODES = 4096;
    public static final int MAX_DECORATIONS = 4096;
    public static final int DECORATION_CHARS = 512;
    public static final int DECORATION_NODES = 2048;
    public static final int MAX_MAP_IDS = 16384;
    public static final int MAX_CHAIN = 128;
    public static final int MAX_SUBTREE = 4096;

    private WorldGuard() {}

    public static List<SynchedEntityData.DataValue<?>> metadata(List<SynchedEntityData.DataValue<?>> values, SynchedEntityData.DataItem<?>[] items) {
        if (values == null || values.isEmpty() || !AntiExploit.active()) return values;
        boolean text = TextBudget.enabled;
        boolean collapsed = values.size() > MAX_METADATA_VALUES;
        List<SynchedEntityData.DataValue<?>> source = collapsed ? lastPerId(values) : values;
        List<SynchedEntityData.DataValue<?>> out = null;
        int dropped = 0;
        boolean shortened = false;
        int index = 0;
        for (SynchedEntityData.DataValue<?> value : source) {
            SynchedEntityData.DataValue<?> kept = invalid(value, items) ? null : text ? bounded(value) : value;
            if (kept != value) {
                if (out == null) out = new ArrayList<>(source.subList(0, index));
                if (kept == null) dropped++;
                else shortened = true;
            }
            if (out != null && kept != null) out.add(kept);
            index++;
        }
        if (collapsed) {
            AntiExploit.blocked("Entity metadata", "repeated values collapsed");
            if (AntiExploit.shouldWarn("metadata|collapsed")) {
                Hontun.LOG.warn("[AntiExploit] collapsed {} entity metadata values to the last value of each field", values.size());
            }
        }
        if (dropped > 0) {
            AntiExploit.blocked("Entity metadata", "invalid value removed");
            if (AntiExploit.shouldWarn("metadata|invalid")) {
                Hontun.LOG.warn("[AntiExploit] removed {} invalid entity metadata value(s) and applied the rest", dropped);
            }
        }
        if (shortened && AntiExploit.shouldWarn("metadata|text")) {
            Hontun.LOG.warn("[AntiExploit] shortened an oversized entity name or display text");
            AntiExploit.blocked("Entity metadata", "oversized text shortened");
        }
        return out != null ? out : source;
    }

    public static List<MapDecoration> decorations(List<MapDecoration> decorations) {
        if (decorations == null || decorations.isEmpty() || !AntiExploit.packetGuard()) return decorations;
        boolean capped = decorations.size() > MAX_DECORATIONS;
        List<MapDecoration> source = capped ? decorations.subList(0, MAX_DECORATIONS) : decorations;
        List<MapDecoration> out = null;
        int index = 0;
        for (MapDecoration decoration : source) {
            MapDecoration kept = decoration;
            if (decoration.name().orElse(null) instanceof Component name) {
                Component bound = TextBudget.bound(name, DECORATION_CHARS, DECORATION_NODES);
                if (bound != name) kept = new MapDecoration(decoration.type(), decoration.x(), decoration.y(), decoration.rot(), Optional.of(bound));
            }
            if (kept != decoration && out == null) out = new ArrayList<>(source.subList(0, index));
            if (out != null) out.add(kept);
            index++;
        }
        if (capped) {
            AntiExploit.blocked("Map", "decorations over " + MAX_DECORATIONS);
            if (AntiExploit.shouldWarn("map|decorations")) {
                Hontun.LOG.warn("[AntiExploit] map sent {} decorations, kept the first {}", decorations.size(), MAX_DECORATIONS);
            }
        }
        if (out != null && AntiExploit.shouldWarn("map|names")) {
            Hontun.LOG.warn("[AntiExploit] shortened an oversized map marker name");
            AntiExploit.blocked("Map", "oversized marker name shortened");
        }
        return out != null ? out : source;
    }

    public static boolean chainTooDeep(Entity passenger, Entity vehicle) {
        if (passenger == null || vehicle == null) return false;
        if (passenger == vehicle) return true;
        int above = 0;
        for (Entity e = vehicle.getVehicle(); e != null; e = e.getVehicle()) {
            if (++above >= MAX_CHAIN) return true;
        }
        return taller(passenger, MAX_CHAIN - above - 1);
    }

    private static boolean taller(Entity root, int limit) {
        List<Entity> level = List.of(root);
        int visited = 1;
        for (int height = 0; ; height++) {
            List<Entity> next = new ArrayList<>();
            for (Entity e : level) {
                for (Entity passenger : e.getPassengers()) {
                    if (++visited > MAX_SUBTREE) return true;
                    next.add(passenger);
                }
            }
            if (next.isEmpty()) return false;
            if (height + 1 > limit) return true;
            level = next;
        }
    }

    private static List<SynchedEntityData.DataValue<?>> lastPerId(List<SynchedEntityData.DataValue<?>> values) {
        boolean[] seen = new boolean[256];
        List<SynchedEntityData.DataValue<?>> kept = new ArrayList<>();
        for (ListIterator<SynchedEntityData.DataValue<?>> it = values.listIterator(values.size()); it.hasPrevious(); ) {
            SynchedEntityData.DataValue<?> value = it.previous();
            if (value == null) continue;
            int id = value.id();
            if (id < 0 || id >= seen.length || seen[id]) continue;
            seen[id] = true;
            kept.add(value);
        }
        Collections.reverse(kept);
        return kept;
    }

    private static boolean invalid(SynchedEntityData.DataValue<?> value, SynchedEntityData.DataItem<?>[] items) {
        if (value == null) return true;
        if (items != null) {
            int id = value.id();
            if (id < 0 || id >= items.length || items[id] == null) return true;
            if (!Objects.equals(items[id].getAccessor().serializer(), value.serializer())) return true;
        }
        return AntiExploit.invalidDataValue(value.value());
    }

    private static SynchedEntityData.DataValue<?> bounded(SynchedEntityData.DataValue<?> value) {
        if (value.serializer() == EntityDataSerializers.COMPONENT && value.value() instanceof Component text) {
            Component bound = TextBudget.bound(text, TEXT_CHARS, TEXT_NODES);
            return bound == text ? value : new SynchedEntityData.DataValue<>(value.id(), EntityDataSerializers.COMPONENT, bound);
        }
        if (value.serializer() == EntityDataSerializers.OPTIONAL_COMPONENT && value.value() instanceof Optional<?> name && name.orElse(null) instanceof Component text) {
            Component bound = TextBudget.bound(text, NAME_CHARS, NAME_NODES);
            return bound == text ? value : new SynchedEntityData.DataValue<>(value.id(), EntityDataSerializers.OPTIONAL_COMPONENT, Optional.of(bound));
        }
        return value;
    }
}
