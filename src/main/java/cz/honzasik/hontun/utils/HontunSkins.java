package cz.honzasik.hontun.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.util.UndashedUuid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class HontunSkins {
    private static final long[] BACKOFF = {15_000L, 60_000L, 300_000L, 900_000L};
    private static final Map<String, Slot> SLOTS = new ConcurrentHashMap<>();

    private HontunSkins() {
    }

    private static final class Slot {
        volatile GameProfile profile;
        volatile boolean loading;
        volatile long retryAt;
        volatile int fails;
        Supplier<PlayerSkin> lookup;
        SkinManager owner;
    }

    public static PlayerSkin skin(String name, UUID id) {
        String n = name == null ? "" : name;
        UUID real = premium(n, id) ? id : null;
        Slot s = SLOTS.computeIfAbsent(real != null ? real.toString() : "name:" + n.toLowerCase(Locale.ROOT), k -> new Slot());
        GameProfile p = s.profile;
        if (p == null) {
            load(s, n, real);
            return DefaultPlayerSkin.get(real != null ? real : UUIDUtil.createOfflinePlayerUUID(n));
        }
        SkinManager sm = Minecraft.getInstance().getSkinManager();
        if (s.lookup == null || s.owner != sm) {
            s.lookup = sm.createLookup(p, false);
            s.owner = sm;
        }
        return s.lookup.get();
    }

    public static Supplier<PlayerSkin> lookup(String name, UUID id) {
        return () -> skin(name, id);
    }

    public static UUID parse(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return UndashedUuid.fromStringLenient(raw);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean premium(String name, UUID id) {
        if (id == null || (id.getMostSignificantBits() | id.getLeastSignificantBits()) == 0L) return false;
        return !id.equals(UUIDUtil.createOfflinePlayerUUID(name));
    }

    private static void load(Slot s, String name, UUID id) {
        if (s.loading || System.currentTimeMillis() < s.retryAt) return;
        s.loading = true;
        Minecraft mc = Minecraft.getInstance();
        CompletableFuture.supplyAsync(() -> fetch(mc, name, id), Util.nonCriticalIoPool()).whenComplete((p, t) -> {
            if (p != null) {
                s.profile = p;
                s.fails = 0;
            } else {
                s.retryAt = System.currentTimeMillis() + BACKOFF[Math.min(s.fails, BACKOFF.length - 1)];
                s.fails++;
            }
            s.loading = false;
        });
    }

    private static GameProfile fetch(Minecraft mc, String name, UUID id) {
        UUID target = id;
        if (target == null) {
            if (!StringUtil.isValidPlayerName(name)) return null;
            var found = mc.services().profileRepository().findProfileByName(name);
            if (found.isEmpty()) return null;
            target = found.get().id();
        }
        var r = mc.services().sessionService().fetchProfile(target, true);
        return r == null ? null : r.profile();
    }
}
