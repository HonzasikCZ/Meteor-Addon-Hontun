package cz.honzasik.hontun.serverlog;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.IDN;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

public final class LogModel {
    public static final int FORMAT = 1;
    public static final int MAX_VISITS = 200;
    public static final int MAX_PLAYERS = 50;
    public static final int MAX_PINGS = 40;
    public static final int MAX_ADDRESSES = 40;
    public static final int MAX_SERVERS = 100;
    public static final int MAX_PLUGINS = 400;
    public static final int MAX_HOPS = 100;
    public static final int MAX_LIST = 64;
    public static final int MAX_TEXT = 200;

    public static final String JOIN = "join";
    public static final String TRANSFER = "transfer";
    public static final String CONFIGURATION = "configuration";
    public static final String LOGIN = "login";
    public static final String NONE_FOUND = "None found";

    private static final String NONE = "NONE";
    private static final String UNKNOWN = "UNKNOWN";
    private static final long RESOLVE_MS = 8_000L;
    private static final long TRUST_MS = 20_000L;
    private static final long NONE_MS = 60_000L;

    private LogModel() {}

    public record Ping(long at, int ms, int online, int max, int protocol, String version, boolean secureChat) {
        public Ping {
            ms = Math.max(0, ms);
            version = clip(version);
        }
    }

    public record Address(String ip, List<String> ips, String protection, String org, String country, long from, long to) {
        public Address {
            ip = clip(ip);
            ips = clipAll(ips, MAX_LIST);
            protection = clip(protection);
            org = clip(org);
            country = clip(country);
        }

        private boolean shares(Address other) {
            for (String s : ips) if (other.ips.contains(s)) return true;
            return false;
        }
    }

    public record Pack(String origin, String why, boolean required, String state, String loaded, String url) {
        public Pack {
            origin = clip(origin);
            why = clip(why);
            state = clip(state);
            loaded = clip(loaded);
            url = clip(url);
        }
    }

    public record Hop(int server, long at, String how, List<String> changes) {}

    public record Snapshot(String software, String family, String confidence, String nameOrigin, String brand,
                           String proxy, boolean rewritesBrand, String version, String versionWhy, boolean versionExact,
                           String anticheat, String anticheatConfidence, String anticheatSource, boolean anticheatNone,
                           String world, List<String> levels, int maxPlayers, int viewDistance, int simulationDistance,
                           int secureChat, int hardcore, List<String> plugins, List<String> players, List<Pack> packs,
                           int cookies, int channels) {}

    public static final class ServerEntry {
        public int index;
        public String key = "";
        public String how = "";
        public String software = "";
        public String family = "";
        public String confidence = "";
        public String nameOrigin = "";
        public String brand = "";
        public String proxy = "";
        public boolean proxyKnown;
        public boolean rewritesBrand;
        public String version = "";
        public String versionWhy = "";
        public boolean versionExact;
        public String anticheat = "";
        public String anticheatConfidence = "";
        public String anticheatSource = "";
        public String world = "";
        public List<String> levels = List.of();
        public int maxPlayers = -1;
        public int viewDistance = -1;
        public int simulationDistance = -1;
        public int secureChat = -1;
        public int hardcore = -1;
        public List<String> plugins = List.of();
        public long pluginsAt;
        public List<String> players = List.of();
        public List<Pack> packs = List.of();
        public int cookies;
        public int channels;
        public long firstSeen;
        public long lastSeen;
        public int visits;

        public String label() {
            return "Server " + index;
        }

        public ServerEntry copy() {
            ServerEntry e = new ServerEntry();
            e.index = index;
            e.key = key;
            e.how = how;
            e.software = software;
            e.family = family;
            e.confidence = confidence;
            e.nameOrigin = nameOrigin;
            e.brand = brand;
            e.proxy = proxy;
            e.proxyKnown = proxyKnown;
            e.rewritesBrand = rewritesBrand;
            e.version = version;
            e.versionWhy = versionWhy;
            e.versionExact = versionExact;
            e.anticheat = anticheat;
            e.anticheatConfidence = anticheatConfidence;
            e.anticheatSource = anticheatSource;
            e.world = world;
            e.levels = levels;
            e.maxPlayers = maxPlayers;
            e.viewDistance = viewDistance;
            e.simulationDistance = simulationDistance;
            e.secureChat = secureChat;
            e.hardcore = hardcore;
            e.plugins = plugins;
            e.pluginsAt = pluginsAt;
            e.players = players;
            e.packs = packs;
            e.cookies = cookies;
            e.channels = channels;
            e.firstSeen = firstSeen;
            e.lastSeen = lastSeen;
            e.visits = visits;
            return e;
        }
    }

    public static final class Visit {
        public long start;
        public long end;
        public boolean open;
        public String note = "";
        public List<String> notes = List.of();
        public List<Hop> hops = List.of();

        public long duration() {
            return Math.max(0L, end - start);
        }

        public List<Integer> path() {
            return hops.stream().map(Hop::server).toList();
        }

        public int lastServer() {
            for (int i = hops.size() - 1; i >= 0; i--) if (hops.get(i).server() > 0) return hops.get(i).server();
            return 0;
        }

        public List<String> changes() {
            LinkedHashSet<String> out = new LinkedHashSet<>(notes);
            for (Hop h : hops) out.addAll(h.changes());
            return List.copyOf(out);
        }

        public boolean changed() {
            if (!notes.isEmpty()) return true;
            for (Hop h : hops) if (!h.changes().isEmpty()) return true;
            return false;
        }

        private Visit copy() {
            Visit v = new Visit();
            v.start = start;
            v.end = end;
            v.open = open;
            v.note = note;
            v.notes = List.copyOf(notes);
            v.hops = List.copyOf(hops);
            return v;
        }
    }

    public record View(String address, boolean loaded, long firstSeen, long lastSeen, List<ServerEntry> servers,
                       List<Visit> visits, List<Address> addresses, List<Ping> pings) {
        public static View empty(String address) {
            return new View(address, false, 0L, 0L, List.of(), List.of(), List.of(), List.of());
        }

        public boolean visited() {
            return !visits.isEmpty();
        }

        public Visit lastVisit() {
            return visits.isEmpty() ? null : visits.get(0);
        }

        public ServerEntry server(int index) {
            for (ServerEntry e : servers) if (e.index == index) return e;
            return null;
        }

        public ServerEntry lastServer() {
            Visit v = lastVisit();
            return v == null ? null : server(v.lastServer());
        }

        public Ping lastPing() {
            return pings.isEmpty() ? null : pings.get(pings.size() - 1);
        }

        public int pingMin() {
            int out = -1;
            for (Ping p : pings) if (out < 0 || p.ms() < out) out = p.ms();
            return out;
        }

        public int pingMax() {
            int out = -1;
            for (Ping p : pings) out = Math.max(out, p.ms());
            return out;
        }

        public int pingAverage() {
            if (pings.isEmpty()) return -1;
            long sum = 0L;
            for (Ping p : pings) sum += p.ms();
            return (int) Math.round(sum / (double) pings.size());
        }
    }

    private static final class HopState {
        final int position;
        final long at;
        final String how;
        final ServerKey key;
        ServerEntry entry;
        ServerEntry before;
        Snapshot last;
        long lastAt;
        boolean fresh;
        boolean named;

        HopState(int position, long at, String how, ServerKey key) {
            this.position = position;
            this.at = at;
            this.how = how;
            this.key = key;
        }
    }

    public static final class Log {
        public final String address;
        public long firstSeen;
        public long lastSeen;
        public final List<ServerEntry> servers = new ArrayList<>();
        public final List<Visit> visits = new ArrayList<>();
        public final List<Address> addresses = new ArrayList<>();
        public final List<Ping> pings = new ArrayList<>();
        private final List<String> pending = new ArrayList<>();
        private int nextIndex = 1;
        private Visit active;

        public boolean idle() {
            return active == null;
        }
        private boolean hadVisits;
        private final List<HopState> states = new ArrayList<>();
        private final Set<Integer> counted = new HashSet<>();

        public Log(String address) {
            this.address = address;
        }

        public View view() {
            List<ServerEntry> s = new ArrayList<>(servers.size());
            for (ServerEntry e : servers) s.add(e.copy());
            List<Visit> v = new ArrayList<>(visits.size());
            for (Visit x : visits) v.add(x.copy());
            return new View(address, true, firstSeen, lastSeen, List.copyOf(s), List.copyOf(v), List.copyOf(addresses), List.copyOf(pings));
        }

        private void seen(long at) {
            if (firstSeen == 0L || at < firstSeen) firstSeen = at;
            if (at > lastSeen) lastSeen = at;
        }

        public void ping(Ping p) {
            seen(p.at());
            pings.add(p);
            while (pings.size() > MAX_PINGS) pings.remove(0);
        }

        public void address(Address a) {
            if (a.ips().isEmpty()) return;
            seen(a.to());
            if (addresses.isEmpty()) {
                addresses.add(a);
                return;
            }
            Address top = addresses.get(0);
            AddressInfo.Protection now = AddressInfo.Protection.of(a.protection()), before = AddressInfo.Protection.of(top.protection());
            boolean shield = now.shield(), was = before.shield();
            if (top.shares(a) || (shield && now == before)) {
                addresses.set(0, new Address(a.ip(), a.ips(), was || UNKNOWN.equals(a.protection()) ? top.protection() : a.protection(),
                        a.org().isEmpty() ? top.org() : a.org(), a.country().isEmpty() ? top.country() : a.country(), top.from(), a.to()));
                return;
            }
            addresses.add(0, a);
            while (addresses.size() > MAX_ADDRESSES) addresses.remove(addresses.size() - 1);
            if (shield && was && now != before) later("Moved from " + before.label() + " to " + now.label());
            else if (shield && NONE.equals(top.protection())) later("Moved behind " + now.label());
            else if (was && NONE.equals(a.protection())) later("No longer behind " + before.label());
            else later("Address changed to " + a.ip());
        }

        private void later(String text) {
            if (active != null) note(text);
            else if (pending.size() < MAX_LIST && !pending.contains(text)) pending.add(text);
        }

        public void startVisit(long at, String note) {
            if (active != null) endVisit(at);
            hadVisits = !visits.isEmpty();
            seen(at);
            active = new Visit();
            active.start = at;
            active.end = at;
            active.open = true;
            active.note = clip(note);
            active.notes = List.copyOf(pending);
            pending.clear();
            states.clear();
            counted.clear();
            visits.add(0, active);
            while (visits.size() > MAX_VISITS) visits.remove(visits.size() - 1);
        }

        public void hop(long at, String how, ServerKey key) {
            if (active == null) return;
            touch(at);
            settle();
            if (states.size() >= MAX_HOPS) return;
            HopState h = new HopState(active.hops.size(), at, how, key);
            states.add(h);
            List<Hop> hops = new ArrayList<>(active.hops);
            hops.add(new Hop(0, at, how, List.of()));
            active.hops = hops;
            if (key != null && key.known()) assign(h, byKey(key), key.text());
        }

        public void snapshot(int hop, long at, Snapshot s) {
            if (active == null || s == null) return;
            touch(at);
            HopState h = hop >= 1 && hop <= states.size() ? states.get(hop - 1) : null;
            if (h == null) return;
            h.last = s;
            h.lastAt = at;
            if (h.entry != null) apply(h);
            else if (softwareKnown(s.software()) && at - h.at >= RESOLVE_MS) resolve(h);
        }

        public void note(String text) {
            String t = clip(text);
            if (active == null || t.isEmpty() || active.notes.contains(t) || active.notes.size() >= MAX_LIST) return;
            List<String> notes = new ArrayList<>(active.notes);
            notes.add(t);
            active.notes = notes;
        }

        public void endVisit(long at) {
            if (active == null) return;
            touch(at);
            settle();
            active.open = false;
            active = null;
            states.clear();
            counted.clear();
        }

        private void touch(long at) {
            seen(at);
            if (at > active.end) active.end = at;
        }

        private void settle() {
            for (HopState h : states) if (h != null && h.entry == null) resolve(h);
        }

        private void resolve(HopState h) {
            Snapshot s = h.last;
            if (s == null || !softwareKnown(s.software())) return;
            String software = clip(s.software()), version = clip(s.version());
            String text = ServerKey.fallback(software, version);
            String prefix = ServerKey.fallback(software, "");
            prefix = prefix.substring(0, prefix.lastIndexOf('|') + 1);
            ServerEntry found = null;
            for (ServerEntry e : servers) if (text.equals(e.key)) found = e;
            if (found == null) {
                for (ServerEntry e : servers) {
                    if (e.key == null || !e.key.startsWith(prefix)) continue;
                    String stored = e.key.substring(prefix.length());
                    if (stored.equals("unknown") || version.isEmpty() || !e.versionExact || !s.versionExact()) {
                        found = e;
                        break;
                    }
                }
                if (found != null && s.versionExact() && !version.isEmpty() && !found.versionExact) found.key = text;
            }
            assign(h, found, text);
            apply(h);
        }

        private ServerEntry byKey(ServerKey key) {
            String text = key.text();
            for (ServerEntry e : servers) if (text.equals(e.key)) return e;
            for (ServerEntry e : servers) if (key.sameServer(e)) return e;
            return null;
        }

        private void assign(HopState h, ServerEntry found, String key) {
            ServerEntry e = found;
            if (e == null) {
                if (servers.size() >= MAX_SERVERS) evict();
                e = new ServerEntry();
                e.index = nextIndex++;
                e.key = key;
                e.firstSeen = h.at;
                servers.add(e);
                h.fresh = true;
            } else {
                h.before = e.copy();
            }
            h.entry = e;
            e.lastSeen = Math.max(e.lastSeen, h.at);
            e.how = h.how;
            if (h.key != null) {
                if (!h.key.levels().isEmpty()) e.levels = clipAll(h.key.levels(), MAX_LIST);
                if (!h.key.dimension().isEmpty()) e.world = clip(h.key.dimension());
            }
            if (counted.add(e.index)) e.visits++;
            changes(h);
        }

        private void evict() {
            ServerEntry oldest = null;
            for (ServerEntry e : servers) {
                if (!counted.contains(e.index) && (oldest == null || e.lastSeen < oldest.lastSeen)) oldest = e;
            }
            if (oldest != null) servers.remove(oldest);
        }

        private void apply(HopState h) {
            ServerEntry e = h.entry;
            Snapshot s = h.last;
            long age = h.lastAt - h.at;
            e.lastSeen = Math.max(e.lastSeen, h.lastAt);
            if (softwareKnown(s.software())) {
                String software = clip(s.software());
                if (age >= TRUST_MS || rank(s.confidence()) >= rank(e.confidence) || software.equals(e.software)) {
                    e.software = software;
                    e.family = clip(s.family());
                    e.confidence = clip(s.confidence());
                    e.nameOrigin = clip(s.nameOrigin());
                }
                e.proxy = clip(s.proxy());
                e.proxyKnown = true;
                e.rewritesBrand = s.rewritesBrand();
            }
            if (!clip(s.brand()).isEmpty()) e.brand = clip(s.brand());
            if (!clip(s.version()).isEmpty() && (s.versionExact() || !e.versionExact || age >= TRUST_MS)) {
                e.version = clip(s.version());
                e.versionWhy = clip(s.versionWhy());
                e.versionExact = s.versionExact();
            }
            if (!clip(s.anticheat()).isEmpty()) {
                if (!h.named || rank(s.anticheatConfidence()) >= rank(e.anticheatConfidence) || age >= TRUST_MS) {
                    e.anticheat = clip(s.anticheat());
                    e.anticheatConfidence = clip(s.anticheatConfidence());
                    e.anticheatSource = clip(s.anticheatSource());
                }
                h.named = true;
            } else if (s.anticheatNone() && (e.anticheat.isEmpty() || (!h.named && age >= NONE_MS))) {
                e.anticheat = NONE_FOUND;
                e.anticheatConfidence = "";
                e.anticheatSource = "";
            }
            if (!clip(s.world()).isEmpty()) e.world = clip(s.world());
            if (s.levels() != null && !s.levels().isEmpty()) e.levels = clipAll(s.levels(), MAX_LIST);
            if (s.maxPlayers() >= 0) e.maxPlayers = s.maxPlayers();
            if (s.viewDistance() >= 0) e.viewDistance = s.viewDistance();
            if (s.simulationDistance() >= 0) e.simulationDistance = s.simulationDistance();
            if (s.secureChat() >= 0) e.secureChat = s.secureChat();
            if (s.hardcore() >= 0) e.hardcore = s.hardcore();
            if (s.plugins() != null && !s.plugins().isEmpty()) {
                e.plugins = clipAll(s.plugins(), MAX_PLUGINS);
                e.pluginsAt = h.lastAt;
            } else if (s.plugins() != null && e.plugins.isEmpty()) {
                e.pluginsAt = h.lastAt;
            }
            if (s.players() != null && !s.players().isEmpty()) {
                LinkedHashSet<String> all = new LinkedHashSet<>(clipAll(s.players(), MAX_PLAYERS));
                all.addAll(e.players);
                e.players = clipAll(all, MAX_PLAYERS);
            }
            if (s.packs() != null) e.packs = List.copyOf(s.packs().size() > MAX_LIST ? s.packs().subList(0, MAX_LIST) : s.packs());
            if (s.cookies() > 0) e.cookies = s.cookies();
            if (s.channels() > 0) e.channels = s.channels();
            changes(h);
        }

        private void changes(HopState h) {
            if (h.position >= active.hops.size()) return;
            List<String> out = new ArrayList<>();
            if (h.fresh) {
                if (hadVisits) out.add("First visit to " + h.entry.label());
            } else if (h.before != null) {
                for (String c : diff(h.before, h.entry)) out.add(clip(h.entry.label() + ": " + c));
            }
            List<Hop> hops = new ArrayList<>(active.hops);
            hops.set(h.position, new Hop(h.entry.index, h.at, h.how, List.copyOf(out)));
            active.hops = hops;
        }
    }

    public static List<String> diff(ServerEntry before, ServerEntry now) {
        List<String> out = new ArrayList<>();
        if (softwareKnown(before.software) && softwareKnown(now.software) && rank(before.confidence) >= 2 && rank(now.confidence) >= 2
                && !before.software.equalsIgnoreCase(now.software)) {
            out.add(before.software + " to " + now.software);
        }
        if (before.versionExact && now.versionExact && !before.version.isEmpty() && !now.version.isEmpty() && !before.version.equals(now.version)) {
            out.add(before.version + " to " + now.version);
        }
        if (named(now.anticheat) && rank(now.anticheatConfidence) >= 2) {
            if (named(before.anticheat) && rank(before.anticheatConfidence) >= 2 && !before.anticheat.equalsIgnoreCase(now.anticheat)) {
                out.add("anticheat " + before.anticheat + " to " + now.anticheat);
            } else if (NONE_FOUND.equals(before.anticheat)) {
                out.add("anticheat " + now.anticheat + " found");
            }
        }
        if (before.pluginsAt > 0L && now.pluginsAt > before.pluginsAt) {
            String added = plugins("+", minus(now.plugins, before.plugins)), removed = plugins("-", minus(before.plugins, now.plugins));
            if (added != null) out.add(added);
            if (removed != null) out.add(removed);
        }
        return out;
    }

    private static List<String> minus(List<String> a, List<String> b) {
        Set<String> skip = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        skip.addAll(b);
        List<String> out = new ArrayList<>();
        for (String s : a) if (!skip.contains(s)) out.add(s);
        return out;
    }

    private static String plugins(String sign, List<String> list) {
        if (list.isEmpty()) return null;
        String head = sign + list.size() + (list.size() == 1 ? " plugin, " : " plugins, ");
        if (list.size() <= 4) return head + String.join(", ", list);
        return head + String.join(", ", list.subList(0, 3)) + " and " + (list.size() - 3) + " more";
    }

    public static boolean softwareKnown(String software) {
        return software != null && !software.isBlank() && !"Unknown".equalsIgnoreCase(software.strip());
    }

    public static boolean named(String anticheat) {
        return anticheat != null && !anticheat.isBlank() && !NONE_FOUND.equals(anticheat) && !"Unknown".equalsIgnoreCase(anticheat);
    }

    public static int rank(String confidence) {
        if (confidence == null) return 0;
        String c = confidence.toLowerCase(Locale.ROOT);
        return c.startsWith("high") ? 3 : c.startsWith("medium") ? 2 : c.isBlank() ? 0 : 1;
    }

    public static String howText(String how) {
        return switch (how == null ? "" : how) {
            case JOIN -> "first server after joining";
            case TRANSFER -> "arrived by a transfer packet";
            case CONFIGURATION -> "went back to configuration during play";
            case LOGIN -> "second login packet during play";
            default -> "";
        };
    }

    public static String normalise(String raw) {
        String s = raw == null ? "" : raw.strip();
        int slash = s.indexOf('/');
        if (slash >= 0) s = s.substring(0, slash);
        String host = s;
        int port = 25565;
        if (s.startsWith("[")) {
            int end = s.indexOf(']');
            if (end > 0) {
                host = s.substring(1, end);
                if (end + 1 < s.length() && s.charAt(end + 1) == ':') port = port(s.substring(end + 2));
            }
        } else {
            int colon = s.lastIndexOf(':');
            if (colon >= 0 && s.indexOf(':') == colon) {
                host = s.substring(0, colon);
                port = port(s.substring(colon + 1));
            }
        }
        host = host.toLowerCase(Locale.ROOT);
        while (host.endsWith(".")) host = host.substring(0, host.length() - 1);
        try {
            if (host.indexOf(':') < 0) host = IDN.toASCII(host).toLowerCase(Locale.ROOT);
        } catch (RuntimeException ignored) {
        }
        return (host.indexOf(':') >= 0 ? "[" + host + "]" : host) + ":" + port;
    }

    private static int port(String s) {
        try {
            int p = Integer.parseInt(s.strip());
            return p > 0 && p < 65536 ? p : 25565;
        } catch (NumberFormatException e) {
            return 25565;
        }
    }

    public static String fileName(String address) {
        StringBuilder sb = new StringBuilder(address.length());
        for (int i = 0; i < address.length(); i++) {
            char c = Character.toLowerCase(address.charAt(i));
            sb.append((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '.' || c == '-' ? c : '_');
        }
        String s = sb.toString();
        return s.length() <= 100 ? s : s.substring(0, 90) + "-" + Integer.toHexString(address.hashCode());
    }

    public static String clip(String s) {
        if (s == null || s.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(Math.min(s.length(), MAX_TEXT));
        for (int i = 0; i < s.length(); ) {
            int c = s.codePointAt(i);
            i += Character.charCount(c);
            if (c == '§') {
                if (i < s.length()) i += Character.charCount(s.codePointAt(i));
                continue;
            }
            if (c >= Character.MIN_SURROGATE && c <= Character.MAX_SURROGATE) continue;
            if (sb.length() + Character.charCount(c) > MAX_TEXT) break;
            sb.appendCodePoint(Character.isISOControl(c) ? ' ' : c);
        }
        return sb.toString().strip();
    }

    private static List<String> clipAll(Collection<String> in, int max) {
        if (in == null || in.isEmpty()) return List.of();
        List<String> out = new ArrayList<>(Math.min(in.size(), max));
        for (String s : in) {
            if (out.size() >= max) break;
            String c = clip(s);
            if (!c.isEmpty()) out.add(c);
        }
        return List.copyOf(out);
    }

    public static JsonObject write(Log log) {
        JsonObject o = new JsonObject();
        o.addProperty("format", FORMAT);
        o.addProperty("address", log.address);
        o.addProperty("firstSeen", log.firstSeen);
        o.addProperty("lastSeen", log.lastSeen);
        o.addProperty("nextServer", log.nextIndex);
        if (!log.pending.isEmpty()) o.add("pending", array(log.pending));
        JsonArray servers = new JsonArray();
        for (ServerEntry e : log.servers) servers.add(write(e));
        o.add("servers", servers);
        JsonArray visits = new JsonArray();
        for (Visit v : log.visits) visits.add(write(v));
        o.add("visits", visits);
        JsonArray addresses = new JsonArray();
        for (Address a : log.addresses) {
            JsonObject x = new JsonObject();
            x.addProperty("ip", a.ip());
            x.add("ips", array(a.ips()));
            x.addProperty("protection", a.protection());
            x.addProperty("org", a.org());
            x.addProperty("country", a.country());
            x.addProperty("from", a.from());
            x.addProperty("to", a.to());
            addresses.add(x);
        }
        o.add("addresses", addresses);
        JsonArray pings = new JsonArray();
        for (Ping p : log.pings) {
            JsonObject x = new JsonObject();
            x.addProperty("at", p.at());
            x.addProperty("ms", p.ms());
            x.addProperty("online", p.online());
            x.addProperty("max", p.max());
            x.addProperty("protocol", p.protocol());
            x.addProperty("version", p.version());
            x.addProperty("secureChat", p.secureChat());
            pings.add(x);
        }
        o.add("pings", pings);
        return o;
    }

    private static JsonObject write(ServerEntry e) {
        JsonObject o = new JsonObject();
        o.addProperty("index", e.index);
        o.addProperty("key", e.key);
        o.addProperty("how", e.how);
        o.addProperty("software", e.software);
        o.addProperty("family", e.family);
        o.addProperty("confidence", e.confidence);
        o.addProperty("nameOrigin", e.nameOrigin);
        o.addProperty("brand", e.brand);
        o.addProperty("proxy", e.proxy);
        o.addProperty("proxyKnown", e.proxyKnown);
        o.addProperty("rewritesBrand", e.rewritesBrand);
        o.addProperty("version", e.version);
        o.addProperty("versionWhy", e.versionWhy);
        o.addProperty("versionExact", e.versionExact);
        o.addProperty("anticheat", e.anticheat);
        o.addProperty("anticheatConfidence", e.anticheatConfidence);
        o.addProperty("anticheatSource", e.anticheatSource);
        o.addProperty("world", e.world);
        o.add("levels", array(e.levels));
        o.addProperty("maxPlayers", e.maxPlayers);
        o.addProperty("viewDistance", e.viewDistance);
        o.addProperty("simulationDistance", e.simulationDistance);
        o.addProperty("secureChat", e.secureChat);
        o.addProperty("hardcore", e.hardcore);
        o.add("plugins", array(e.plugins));
        o.addProperty("pluginsAt", e.pluginsAt);
        o.add("players", array(e.players));
        JsonArray packs = new JsonArray();
        for (Pack p : e.packs) {
            JsonObject x = new JsonObject();
            x.addProperty("origin", p.origin());
            x.addProperty("why", p.why());
            x.addProperty("required", p.required());
            x.addProperty("state", p.state());
            x.addProperty("loaded", p.loaded());
            x.addProperty("url", p.url());
            packs.add(x);
        }
        o.add("packs", packs);
        o.addProperty("cookies", e.cookies);
        o.addProperty("channels", e.channels);
        o.addProperty("firstSeen", e.firstSeen);
        o.addProperty("lastSeen", e.lastSeen);
        o.addProperty("visits", e.visits);
        return o;
    }

    private static JsonObject write(Visit v) {
        JsonObject o = new JsonObject();
        o.addProperty("start", v.start);
        o.addProperty("end", v.end);
        o.addProperty("note", v.note);
        JsonArray hops = new JsonArray();
        for (Hop h : v.hops) {
            JsonObject x = new JsonObject();
            x.addProperty("server", h.server());
            x.addProperty("at", h.at());
            x.addProperty("how", h.how());
            if (!h.changes().isEmpty()) x.add("changes", array(h.changes()));
            hops.add(x);
        }
        o.add("hops", hops);
        o.add("changes", array(v.notes));
        return o;
    }

    public static Log read(JsonElement root, String address) {
        if (root == null || !root.isJsonObject()) throw new IllegalArgumentException("not an object");
        JsonObject o = root.getAsJsonObject();
        Log log = new Log(address);
        log.firstSeen = num(o, "firstSeen", 0L);
        log.lastSeen = num(o, "lastSeen", 0L);
        log.pending.addAll(strings(o, "pending", MAX_LIST));
        Set<Integer> used = new HashSet<>();
        int top = 0;
        for (ServerEntry e : each(o, "servers", LogModel::readServer)) {
            if (e.index < 1 || !used.add(e.index)) continue;
            top = Math.max(top, e.index);
            log.servers.add(e);
        }
        log.servers.sort((a, b) -> Integer.compare(a.index, b.index));
        while (log.servers.size() > MAX_SERVERS) log.servers.remove(0);
        for (Visit v : each(o, "visits", LogModel::readVisit)) {
            for (Hop h : v.hops) top = Math.max(top, h.server());
            log.visits.add(v);
        }
        log.visits.sort((a, b) -> Long.compare(b.start, a.start));
        while (log.visits.size() > MAX_VISITS) log.visits.remove(log.visits.size() - 1);
        log.nextIndex = (int) Math.max(Math.min(top, 1_000_000) + 1L, Math.min(num(o, "nextServer", 1L), 1_000_000L));
        log.addresses.addAll(each(o, "addresses", LogModel::readAddress));
        while (log.addresses.size() > MAX_ADDRESSES) log.addresses.remove(log.addresses.size() - 1);
        log.pings.addAll(each(o, "pings", LogModel::readPing));
        while (log.pings.size() > MAX_PINGS) log.pings.remove(0);
        return log;
    }

    private static ServerEntry readServer(JsonObject o) {
        ServerEntry e = new ServerEntry();
        e.index = (int) Math.min(num(o, "index", 0L), 1_000_000L);
        e.key = str(o, "key");
        e.how = str(o, "how");
        e.software = str(o, "software");
        e.family = str(o, "family");
        e.confidence = str(o, "confidence");
        e.nameOrigin = str(o, "nameOrigin");
        e.brand = str(o, "brand");
        e.proxy = str(o, "proxy");
        e.proxyKnown = bool(o, "proxyKnown");
        e.rewritesBrand = bool(o, "rewritesBrand");
        e.version = str(o, "version");
        e.versionWhy = str(o, "versionWhy");
        e.versionExact = bool(o, "versionExact");
        e.anticheat = str(o, "anticheat");
        e.anticheatConfidence = str(o, "anticheatConfidence");
        e.anticheatSource = str(o, "anticheatSource");
        e.world = str(o, "world");
        e.levels = strings(o, "levels", MAX_LIST);
        e.maxPlayers = (int) num(o, "maxPlayers", -1L);
        e.viewDistance = (int) num(o, "viewDistance", -1L);
        e.simulationDistance = (int) num(o, "simulationDistance", -1L);
        e.secureChat = (int) num(o, "secureChat", -1L);
        e.hardcore = (int) num(o, "hardcore", -1L);
        e.plugins = strings(o, "plugins", MAX_PLUGINS);
        e.pluginsAt = num(o, "pluginsAt", 0L);
        e.players = strings(o, "players", MAX_PLAYERS);
        List<Pack> packs = each(o, "packs", x -> new Pack(str(x, "origin"), str(x, "why"), bool(x, "required"), str(x, "state"), str(x, "loaded"), str(x, "url")));
        e.packs = List.copyOf(packs.size() > MAX_LIST ? packs.subList(0, MAX_LIST) : packs);
        e.cookies = (int) num(o, "cookies", 0L);
        e.channels = (int) num(o, "channels", 0L);
        e.firstSeen = num(o, "firstSeen", 0L);
        e.lastSeen = num(o, "lastSeen", 0L);
        e.visits = (int) num(o, "visits", 0L);
        return e;
    }

    private static Visit readVisit(JsonObject o) {
        Visit v = new Visit();
        v.start = num(o, "start", 0L);
        if (v.start <= 0L) return null;
        v.end = Math.max(v.start, num(o, "end", v.start));
        v.note = str(o, "note");
        List<Hop> hops = each(o, "hops", x -> new Hop((int) Math.max(0L, Math.min(num(x, "server", 0L), 1_000_000L)),
                num(x, "at", 0L), str(x, "how"), strings(x, "changes", MAX_LIST)));
        v.hops = List.copyOf(hops.size() > MAX_HOPS ? hops.subList(0, MAX_HOPS) : hops);
        v.notes = strings(o, "changes", MAX_LIST);
        return v;
    }

    private static Address readAddress(JsonObject o) {
        List<String> ips = strings(o, "ips", MAX_LIST);
        String ip = str(o, "ip");
        if (ips.isEmpty()) ips = ip.isEmpty() ? List.of() : List.of(ip);
        if (ips.isEmpty()) return null;
        return new Address(ip.isEmpty() ? ips.get(0) : ip, ips, str(o, "protection"), str(o, "org"), str(o, "country"), num(o, "from", 0L), num(o, "to", 0L));
    }

    private static Ping readPing(JsonObject o) {
        long at = num(o, "at", 0L);
        if (at <= 0L) return null;
        return new Ping(at, (int) num(o, "ms", 0L), (int) num(o, "online", -1L), (int) num(o, "max", -1L),
                (int) num(o, "protocol", 0L), str(o, "version"), bool(o, "secureChat"));
    }

    private static <T> List<T> each(JsonObject o, String key, Function<JsonObject, T> reader) {
        List<T> out = new ArrayList<>();
        JsonElement e = o.get(key);
        if (e == null || !e.isJsonArray()) return out;
        for (JsonElement x : e.getAsJsonArray()) {
            if (!x.isJsonObject()) continue;
            try {
                T t = reader.apply(x.getAsJsonObject());
                if (t != null) out.add(t);
            } catch (RuntimeException ignored) {
            }
        }
        return out;
    }

    private static JsonArray array(List<String> list) {
        JsonArray a = new JsonArray();
        for (String s : list) a.add(s);
        return a;
    }

    private static List<String> strings(JsonObject o, String key, int max) {
        JsonElement e = o.get(key);
        if (e == null || !e.isJsonArray()) return List.of();
        List<String> out = new ArrayList<>();
        for (JsonElement x : e.getAsJsonArray()) if (x.isJsonPrimitive()) out.add(x.getAsString());
        return clipAll(out, max);
    }

    private static String str(JsonObject o, String key) {
        JsonElement e = o.get(key);
        return e != null && e.isJsonPrimitive() ? clip(e.getAsString()) : "";
    }

    private static long num(JsonObject o, String key, long fallback) {
        JsonElement e = o.get(key);
        if (e == null || !e.isJsonPrimitive()) return fallback;
        try {
            return e.getAsLong();
        } catch (RuntimeException x) {
            return fallback;
        }
    }

    private static boolean bool(JsonObject o, String key) {
        JsonElement e = o.get(key);
        return e != null && e.isJsonPrimitive() && e.getAsBoolean();
    }
}
