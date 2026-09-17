package cz.honzasik.hontun.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;

import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class HontunGeo {
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    private static final Map<String, Long> FAILED = new ConcurrentHashMap<>();
    private static final long RETRY_AFTER_MS = 60_000L;
    private static final Set<String> INFLIGHT = ConcurrentHashMap.newKeySet();

    private static final ExecutorService POOL = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r, "Hontun-geo");
        t.setDaemon(true);
        return t;
    });

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static volatile boolean enabled = true;

    private HontunGeo() {}

    public static void setEnabled(boolean b) { enabled = b; }
    public static boolean enabled() { return enabled; }

    public static String country(String address) {
        if (!enabled || address == null || address.isEmpty()) return null;

        String v = CACHE.get(address);
        if (v != null) return v;

        Long failedAt = FAILED.get(address);
        if (failedAt != null && System.currentTimeMillis() - failedAt < RETRY_AFTER_MS) return null;

        if (INFLIGHT.add(address)) POOL.execute(() -> lookup(address));
        return null;
    }

    private static String host(String address) {
        String s = address.trim();
        int slash = s.indexOf('/');
        if (slash >= 0) s = s.substring(0, slash);
        if (s.startsWith("[")) {
            int end = s.indexOf(']');
            if (end > 1) return s.substring(1, end).toLowerCase(Locale.ROOT);
        }
        int colon = s.lastIndexOf(':');
        if (colon > 0 && s.indexOf(':') == colon) s = s.substring(0, colon);
        return s.toLowerCase(Locale.ROOT);
    }

    private static String resolve(String address) {
        try {
            Optional<ResolvedServerAddress> resolved =
                    ServerNameResolver.DEFAULT.resolveAddress(ServerAddress.parseString(address));
            if (resolved.isPresent()) {
                String ip = resolved.get().getHostIp();
                if (ip != null && !ip.isEmpty()) return ip;
            }
        } catch (Throwable ignored) {
        }
        try {
            return InetAddress.getByName(host(address)).getHostAddress();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void lookup(String key) {
        String result = "";
        String ip = resolve(key);
        if (ip != null) try {
            HttpRequest req = HttpRequest.newBuilder(
                    URI.create("https://ipwho.is/" + ip + "?fields=country_code,success"))
                    .timeout(Duration.ofSeconds(8))
                    .header("User-Agent", "Hontun-Addon")
                    .GET().build();
            HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == 200) {
                JsonObject o = JsonParser.parseString(res.body()).getAsJsonObject();
                if (o.has("country_code") && !o.get("country_code").isJsonNull()) {
                    String cc = o.get("country_code").getAsString();
                    if (cc != null && cc.length() == 2) result = cc.toLowerCase(Locale.ROOT);
                }
            }
        } catch (Throwable ignored) {
        }
        finish(key, result);
    }

    private static void finish(String key, String result) {
        if (result.isEmpty()) FAILED.put(key, System.currentTimeMillis());
        else { CACHE.put(key, result); FAILED.remove(key); }
        INFLIGHT.remove(key);
    }
}
