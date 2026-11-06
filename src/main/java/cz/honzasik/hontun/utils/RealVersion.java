package cz.honzasik.hontun.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RealVersion {
    public interface Tags {
        int tagCount(String registry);

        int size(String registry, String tag);

        int contains(String registry, String tag, String entry);
    }

    public static final class Snapshot {
        public int clientProtocol;
        public String clientVersion;
        public int vfpProtocol = -1;
        public String vfpName;
        public String vvChannel;
        public int vvProtocol;
        public String vvName;
        public int proxyVvProtocol;
        public String proxyVvName;
        public String corePack;
        public boolean knownPacksReceived;
        public boolean knownPacksEmpty;
        public boolean tagsFirst;
        public boolean featuresSeen;
        public boolean configFinished;
        public boolean brandInConfig;
        public boolean brandInPlay;
        public final Map<String, List<String>> registries = new HashMap<>();
        public int overworldMinY = Integer.MIN_VALUE;
        public int overworldHeight = Integer.MIN_VALUE;
        public boolean loggedIn;
        public long sinceLoginMs = -1;
        public int loginView = -1;
        public long loginSeed;
        public int seaLevel = Integer.MIN_VALUE;
        public int teleports;
        public boolean teleportNonZero;
        public final Set<String> firstTabActions = new HashSet<>();
        public boolean tickingState;
        public boolean serverData;
        public boolean chunkBatches;
        public boolean simulationDistance;
        public boolean syntheticCommands;
        public boolean commandsSeen;
        public boolean advancementsOrRecipes;
        public long[] keepAliveIds = new long[0];
        public Tags tags;
        public boolean playTags;
        public String pingName;
        public String pingSeen;
        public int pingProtocol = -1;
    }

    public record Result(String text, String range, String method, boolean translated, int lo, int hi, List<String> notes) {}

    public enum Fate { DECISIVE, AGREES, IGNORED, UNUSED, NONE }

    public record Evidence(String source, String signal, List<String> details, String implies, boolean hint, Fate fate, String reason) {}

    public record Stage(String step, String range) {}

    public record Report(Result result, String missing, String confidence, String through, String disagree, List<Stage> stages, List<Evidence> evidence) {}

    private record TagMarker(int present, int core, String registry, String tag, String entry) {}

    private record NameMarker(int present, int core, String registry, String tag) {}

    private record BiomeMarker(String name, int present, int core, int last) {}

    private record Data(String[] names, int[] protocols, List<TagMarker> tags, List<NameMarker> tagNames, List<BiomeMarker> biomes) {}

    private record Rule(int lo, int hi, String method, String why, boolean exclude) {}

    public static final String TAGS = "tags";
    public static final String REGISTRIES = "registries";
    public static final String JOIN = "join packets";
    public static final String CORE = "core pack";
    public static final String VIA = "ViaVersion details";
    public static final String PING = "ping";
    public static final String VFP = "ViaFabricPlus";
    public static final String OVERWORLD = "overworld";
    public static final List<String> SOURCES = List.of(CORE, VIA, VFP, PING, TAGS, REGISTRIES, OVERWORLD, JOIN);

    private static final String RESOURCE = "/assets/hontun/realversion.txt";
    private static final Set<String> CONTENT = Set.of("block", "item", "entity_type", "fluid");
    private static final Set<String> TAB_INIT = Set.of("ADD_PLAYER", "INITIALIZE_CHAT", "UPDATE_GAME_MODE", "UPDATE_LISTED", "UPDATE_LATENCY", "UPDATE_DISPLAY_NAME");
    private static final Pattern UUID_NAME = Pattern.compile("^(?:[a-z0-9_.-]+:)?[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    private static final Pattern PING_VERSION = Pattern.compile("^(?:\\S+ )?(\\d+\\.\\d+(?:\\.\\d+)?)$");
    private static final Pattern VERSION_SPAN = Pattern.compile("^(\\d+(?:\\.\\d+)+)(?:\\.x)?(?:-(\\d+(?:\\.\\d+)+)(?:\\.x)?)?$");
    private static final String VIA_SERVER = "vv:server_details";
    private static final String VIA_APP = "vv:app_details";
    private static final List<String> METHOD_ORDER = List.of(CORE, VIA, TAGS, REGISTRIES, JOIN, PING);

    private static volatile Data data;
    private static volatile boolean loadFailed;

    private RealVersion() {}

    public static void load(InputStream in) throws Exception {
        List<String> names = new ArrayList<>();
        List<Integer> protocols = new ArrayList<>();
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.trim().split(" ");
                if (p.length < 2) continue;
                if (p[0].equals("v") && p.length >= 3) {
                    names.add(p[1]);
                    protocols.add(Integer.parseInt(p[2]));
                } else {
                    rows.add(p);
                }
            }
        }
        String[] n = names.toArray(new String[0]);
        int[] pr = new int[protocols.size()];
        for (int i = 0; i < pr.length; i++) pr[i] = protocols.get(i);
        List<TagMarker> tags = new ArrayList<>();
        List<NameMarker> tagNames = new ArrayList<>();
        List<BiomeMarker> biomes = new ArrayList<>();
        for (String[] p : rows) {
            switch (p[0]) {
                case "t" -> {
                    if (p.length < 6) continue;
                    int present = exact(n, p[1]), core = exact(n, p[2]);
                    if (present >= 0 && core >= 0) tags.add(new TagMarker(present, core, p[3], p[4], p[5]));
                }
                case "n" -> {
                    if (p.length < 5) continue;
                    int present = exact(n, p[1]), core = exact(n, p[2]);
                    if (present >= 0 && core >= 0) tagNames.add(new NameMarker(present, core, p[3], p[4]));
                }
                case "b" -> {
                    if (p.length < 5) continue;
                    int present = p[2].equals("-") ? -1 : exact(n, p[2]);
                    int core = p[3].equals("-") ? -1 : exact(n, p[3]);
                    int last = p[4].equals("-") ? n.length - 1 : exact(n, p[4]);
                    if (last >= 0) biomes.add(new BiomeMarker("minecraft:" + p[1], present, core, last));
                }
                default -> {}
            }
        }
        data = new Data(n, pr, List.copyOf(tags), List.copyOf(tagNames), List.copyOf(biomes));
    }

    private static Data data() {
        Data d = data;
        if (d != null || loadFailed) return d;
        synchronized (RealVersion.class) {
            if (data != null || loadFailed) return data;
            try (InputStream in = RealVersion.class.getResourceAsStream(RESOURCE)) {
                if (in == null) {
                    loadFailed = true;
                    return null;
                }
                load(in);
            } catch (Throwable t) {
                loadFailed = true;
            }
            return data;
        }
    }

    private static int exact(String[] names, String v) {
        for (int i = 0; i < names.length; i++) if (names[i].equals(v)) return i;
        return -1;
    }

    private static int[] parse(String v) {
        String[] parts = v.split("\\.");
        int[] out = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                out[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return out;
    }

    private static int compare(int[] a, int[] b) {
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int x = i < a.length ? a[i] : 0, y = i < b.length ? b[i] : 0;
            if (x != y) return Integer.compare(x, y);
        }
        return 0;
    }

    private static int indexOf(Data d, String v) {
        if (v == null) return -1;
        int e = exact(d.names, v);
        if (e >= 0) return e;
        int[] want = parse(v.endsWith(".x") ? v.substring(0, v.length() - 2) : v);
        if (want == null) return -1;
        for (int i = 0; i < d.names.length; i++) {
            String name = d.names[i];
            int dash = name.indexOf('-');
            if (dash < 0) continue;
            int[] a = parse(name.substring(0, dash)), b = parse(name.substring(dash + 1));
            if (a != null && b != null && compare(want, a) >= 0 && compare(want, b) <= 0) return i;
        }
        return -1;
    }

    private static int[] spec(Data d, String spec) {
        if (spec == null || spec.isBlank()) return null;
        String s = spec.trim();
        int whole = indexOf(d, s);
        if (whole >= 0) return new int[]{whole, whole};
        int dash = s.indexOf('-');
        if (dash < 0) return null;
        int a = indexOf(d, s.substring(0, dash)), b = indexOf(d, s.substring(dash + 1));
        return a >= 0 && b >= a ? new int[]{a, b} : null;
    }

    private static int[] protocolRange(Data d, int protocol) {
        int lo = -1, hi = -1;
        for (int i = 0; i < d.protocols.length; i++) {
            if (d.protocols[i] != protocol) continue;
            if (lo < 0) lo = i;
            hi = i;
        }
        return lo < 0 ? null : new int[]{lo, hi};
    }

    private static String first(String name) {
        int dash = name.indexOf('-');
        return dash < 0 ? name : name.substring(0, dash);
    }

    private static String last(String name) {
        int dash = name.indexOf('-');
        return dash < 0 ? name : name.substring(dash + 1);
    }

    private static String rangeText(Data d, int lo, int hi) {
        if (lo == hi) return d.names[lo];
        return first(d.names[lo]) + "-" + last(d.names[hi]);
    }

    private static String core(String pack) {
        if (pack == null) return null;
        return pack.endsWith("_unobfuscated") ? pack.substring(0, pack.length() - "_unobfuscated".length()) : pack;
    }

    public static Result resolve(Snapshot s) {
        return compute(s, null);
    }

    public static Report explain(Snapshot s) {
        Trace t = new Trace();
        Result r = compute(s, t);
        return t.report(r);
    }

    private static Result compute(Snapshot s, Trace t) {
        Data d = data();
        if (d == null || s == null) {
            if (t != null) t.missing = d == null ? "the version table did not load" : "nothing captured yet";
            return null;
        }
        if (t != null) {
            t.d = d;
            t.s = s;
        }
        int top = d.names.length - 1;
        List<Rule> hard = new ArrayList<>();
        List<Rule> soft = new ArrayList<>();
        List<String> notes = new ArrayList<>();

        boolean via = s.knownPacksReceived && s.knownPacksEmpty;
        boolean legacy = s.syntheticCommands || (via && s.tagsFirst);

        if (s.syntheticCommands) hard.add(at(d, null, "1.12.2", JOIN, "ViaVersion's placeholder command tree (backend 1.12.2 or older)"));
        if (via && s.tagsFirst) hard.add(at(d, null, "1.12.2", JOIN, "tags sent before everything else in the configuration (ViaVersion, backend 1.12.2 or older)"));
        if (via) hard.add(at(d, null, "1.20.4", JOIN, "empty known-packs list (ViaVersion, backend 1.20.4 or older)"));
        if (via && s.configFinished && !s.featuresSeen) hard.add(at(d, "1.19.3", "1.20.1", JOIN, "no enabled-features packet (ViaVersion drops it for 1.19.3-1.20.1)"));
        if (via && s.featuresSeen) hard.add(new Rule(indexOf(d, "1.19.3"), indexOf(d, "1.20.1"), JOIN, "enabled-features packet present", true));
        if (via && s.teleports > 0) {
            if (s.teleportNonZero) hard.add(at(d, "1.9", null, JOIN, "teleport ids above 0"));
            else hard.add(at(d, null, "1.8.9", JOIN, "every teleport id is 0 (ViaVersion, backend 1.8)"));
        }
        if (via && s.loggedIn && s.loginView > 0) {
            if (s.loginView == 64) hard.add(at(d, null, "1.13.2", JOIN, "view distance 64 in the login packet (ViaVersion, backend 1.13.2 or older)"));
            else hard.add(at(d, "1.14", null, JOIN, "a real view distance in the login packet"));
        }
        if (via && s.loggedIn && s.loginSeed != 0) hard.add(at(d, "1.15", null, JOIN, "non-zero hashed seed"));
        if (s.loggedIn && s.seaLevel != Integer.MIN_VALUE && s.seaLevel != 64) hard.add(at(d, "1.21.2", null, JOIN, "sea level " + s.seaLevel + " in the login packet"));
        boolean tabShape = s.firstTabActions.containsAll(TAB_INIT);
        if (tabShape && s.firstTabActions.contains("UPDATE_HAT")) hard.add(at(d, "1.21.4", null, JOIN, "tab list carries the hat action"));
        if (tabShape && s.firstTabActions.contains("UPDATE_LIST_ORDER")) hard.add(at(d, "1.21.2", null, JOIN, "tab list carries the list-order action"));
        if (s.tickingState) hard.add(at(d, "1.20.3", null, JOIN, "ticking-state packet"));
        if (s.serverData) hard.add(at(d, "1.19", null, JOIN, "server-data packet"));
        if (s.chunkBatches) hard.add(at(d, "1.20.2", null, JOIN, "chunk batches"));
        if (s.simulationDistance) hard.add(at(d, "1.18", null, JOIN, "simulation-distance packet"));
        if (legacy && s.advancementsOrRecipes) hard.add(at(d, "1.12", null, JOIN, "advancements or recipe book at join"));
        if (via && s.commandsSeen && !s.syntheticCommands) hard.add(at(d, "1.13", null, JOIN, "a real command tree"));
        if (via) {
            for (long id : s.keepAliveIds) {
                if (id > Integer.MAX_VALUE) {
                    hard.add(at(d, "1.12.2", null, JOIN, "keep-alive id wider than an int"));
                    break;
                }
            }
        }

        List<String> biomes = s.registries.get("minecraft:worldgen/biome");
        boolean vanillaBiomes = biomes != null && biomes.size() >= 20 && biomes.contains("minecraft:plains");
        if (biomes != null) {
            Set<String> have = new HashSet<>(biomes);
            int uuid = 0;
            for (String b : biomes) if (UUID_NAME.matcher(b).matches()) uuid++;
            if (uuid > 0) hard.add(at(d, null, "1.17.1", REGISTRIES, uuid + " placeholder biomes (ViaVersion fills sparse pre-1.18 biome ids)"));
            for (BiomeMarker m : d.biomes) {
                if (!have.contains(m.name)) continue;
                int lo = m.present < 0 ? 0 : m.present;
                hard.add(new Rule(lo, m.last, REGISTRIES, "biome " + m.name, false));
            }
        }
        List<String> chats = s.registries.get("minecraft:chat_type");
        if (chats != null) {
            for (String c : chats) {
                if (UUID_NAME.matcher(c).matches()) {
                    hard.add(at(d, null, "1.18.2", REGISTRIES, "placeholder chat type (ViaVersion, backend 1.18.2 or older)"));
                    break;
                }
            }
        }

        Tags tags = s.tags;
        boolean tagsUsable = tags != null && !legacy;
        int hostCount = -1;
        if (tagsUsable && via && !s.playTags) {
            Set<String> hosts = new HashSet<>();
            for (TagMarker m : d.tags) if (tags.size(m.registry, m.tag) > 0) hosts.add(m.registry + "/" + m.tag);
            tagsUsable = hosts.size() >= 3;
            hostCount = hosts.size();
        }
        Map<Integer, int[]> tagSteps = new LinkedHashMap<>();
        Map<Integer, int[]> nameSteps = new LinkedHashMap<>();
        if (tagsUsable) {
            for (TagMarker m : d.tags) {
                int c = tags.size(m.registry, m.tag) > 0 ? tags.contains(m.registry, m.tag, m.entry) : -1;
                int[] st = tagSteps.computeIfAbsent(m.core, k -> new int[3]);
                st[2]++;
                if (c == 1) {
                    st[0]++;
                    hard.add(new Rule(m.present, top, TAGS, "#" + m.registry + "/" + m.tag + " contains " + m.entry, false));
                    if (t != null) t.entryPresent++;
                } else if (c == 0) {
                    st[1]++;
                    if (t != null) t.missingEntries.computeIfAbsent(m.core, k -> new ArrayList<>()).add("#" + m.registry + "/" + m.tag + " without " + m.entry);
                }
            }
            for (NameMarker m : d.tagNames) {
                int size = tags.size(m.registry, m.tag);
                int[] st = nameSteps.computeIfAbsent(m.core, k -> new int[3]);
                if (CONTENT.contains(m.registry)) st[2]++;
                if (size >= 0) {
                    st[0]++;
                    hard.add(new Rule(m.present, top, TAGS, "tag #" + m.registry + "/" + m.tag, false));
                    if (t != null) t.namePresent++;
                } else if (CONTENT.contains(m.registry) && tags.tagCount(m.registry) >= 10) {
                    st[1]++;
                    if (t != null) t.missingNames.computeIfAbsent(m.core, k -> new ArrayList<>()).add("no tag #" + m.registry + "/" + m.tag);
                }
            }
        }
        if (t != null) {
            if (tags == null) t.tagState = "no tags received yet";
            else if (legacy) t.tagState = "not used, ViaVersion makes them up for a 1.12.2 or older backend";
            else if (!tagsUsable) t.tagState = "not used, ViaVersion's configuration tags are nearly empty (" + hostCount + " marker tags filled)";
        }

        for (Map.Entry<Integer, int[]> e : tagSteps.entrySet()) {
            int[] st = e.getValue();
            if (st[0] == 0 && st[1] > 0 && st[1] >= Math.min(2, st[2])) add(t, soft, new Rule(0, e.getKey() - 1, TAGS, st[1] + " tag entries of " + d.names[e.getKey()] + " missing", false), null, null, t == null ? null : t.missingEntries.get(e.getKey()));
        }
        for (Map.Entry<Integer, int[]> e : nameSteps.entrySet()) {
            int[] st = e.getValue();
            if (st[0] == 0 && st[1] >= Math.min(2, st[2]) && st[1] > 0) add(t, soft, new Rule(0, e.getKey() - 1, TAGS, st[1] + " tags of " + d.names[e.getKey()] + " missing", false), null, null, t == null ? null : t.missingNames.get(e.getKey()));
        }
        if (vanillaBiomes) {
            Set<String> have = new HashSet<>(biomes);
            Map<Integer, Boolean> steps = new LinkedHashMap<>();
            boolean oldNames = false, anyOld = false;
            for (BiomeMarker m : d.biomes) {
                if (m.core > 0) steps.merge(m.core, have.contains(m.name), (a, b) -> a || b);
                if (m.present < 0 && m.last < top) {
                    anyOld = true;
                    if (have.contains(m.name)) oldNames = true;
                }
            }
            for (Map.Entry<Integer, Boolean> e : steps.entrySet()) {
                if (!e.getValue()) add(t, soft, new Rule(0, e.getKey() - 1, REGISTRIES, "no biome of " + d.names[e.getKey()], false), null, null, t == null ? null : biomesOf(d, e.getKey(), false));
            }
            if (anyOld && !oldNames) {
                int after = 0;
                for (BiomeMarker m : d.biomes) if (m.present < 0 && m.last < top) after = Math.max(after, m.last + 1);
                add(t, soft, new Rule(after, top, REGISTRIES, "none of the biomes removed in " + d.names[after], false), null, null, t == null ? null : biomesOf(d, after, true));
            }
        }
        if (tabShape && !s.firstTabActions.contains("UPDATE_LIST_ORDER")) soft.add(at(d, null, "1.21.1", JOIN, "tab list without the list-order action"));
        else if (tabShape && !s.firstTabActions.contains("UPDATE_HAT")) soft.add(at(d, null, "1.21.3", JOIN, "tab list without the hat action"));
        boolean limbo = s.configFinished && !s.featuresSeen && !via;
        if (s.loggedIn && !s.tickingState && !limbo && s.sinceLoginMs >= 3000) soft.add(at(d, null, "1.20.2", JOIN, "no ticking-state packet"));
        if (s.loggedIn && s.seaLevel == 64) soft.add(at(d, null, "1.21.1", JOIN, "sea level 64 (ViaVersion default for 1.21.1 and older)"));
        if (s.overworldMinY == 0 && s.overworldHeight == 256) add(t, soft, at(d, null, "1.17.1", REGISTRIES, "overworld height 0-256"), OVERWORLD, "height 256 from y 0", null);
        else if (s.overworldMinY == -64 && s.overworldHeight == 384) add(t, soft, at(d, "1.18", null, REGISTRIES, "overworld height -64-320"), OVERWORLD, "height 384 from y -64", null);
        if (s.brandInConfig) soft.add(at(d, "1.20.2", null, JOIN, "brand sent in the configuration phase"));
        else if (via && s.brandInPlay) soft.add(at(d, null, "1.20.1", JOIN, "brand sent only after login"));
        if (via && s.loggedIn && s.loginSeed == 0) soft.add(at(d, null, "1.14.4", JOIN, "hashed seed 0"));
        if (via && s.configFinished && !s.tagsFirst) soft.add(at(d, "1.13", null, JOIN, "tags not sent first in the configuration"));
        if (via) {
            for (long id : s.keepAliveIds) {
                if (id < 0) {
                    soft.add(at(d, null, "1.12.1", JOIN, "negative keep-alive id"));
                    break;
                }
            }
        }
        if (legacy && !s.advancementsOrRecipes && s.sinceLoginMs >= 5000) soft.add(at(d, null, "1.11.2", JOIN, "no advancements or recipe book"));

        int n = top + 1;
        List<Rule> unique = new ArrayList<>();
        Set<String> seenRules = new HashSet<>();
        Map<String, Rule> firstByKey = new HashMap<>();
        for (Rule r : hard) {
            String key = r.method + "|" + r.lo + "|" + r.hi + "|" + r.exclude;
            if (seenRules.add(key)) {
                unique.add(r);
                firstByKey.put(key, r);
            }
            if (t != null) t.uniqueOf.put(r, firstByKey.get(key));
        }
        int[] score = new int[n];
        for (Rule r : unique) for (int i = 0; i < n; i++) if (satisfies(r, i)) score[i]++;
        int best = 0;
        for (int i = 0; i < n; i++) best = Math.max(best, score[i]);
        if (t != null) {
            boolean[] fit = new boolean[n];
            for (int i = 0; i < n; i++) fit[i] = score[i] == best;
            t.bestText = t.span(fit);
            t.hard = hard;
            t.soft = soft;
        }
        List<Rule> accepted = new ArrayList<>();
        for (String method : METHOD_ORDER) {
            for (Rule r : unique) {
                if (!r.method.equals(method)) continue;
                boolean agrees = false;
                for (int i = 0; i < n && !agrees; i++) if (score[i] == best && satisfies(r, i)) agrees = true;
                if (agrees) accepted.add(r);
                else {
                    notes.add("ignored: " + r.why);
                    if (t != null) t.rejected.add(r);
                }
            }
        }
        Set<String> credited = new LinkedHashSet<>();
        boolean[] hardMask = run(n, accepted, List.of(), Set.of(), new HashSet<>(), notes, t == null ? null : t.clashed);
        boolean[] mask = run(n, accepted, soft, Set.of(), credited, null, t == null ? null : t.failed);
        Set<String> removed = new HashSet<>();
        for (String m : List.of(REGISTRIES, JOIN, TAGS)) {
            Set<String> skip = new HashSet<>(removed);
            skip.add(m);
            if (Arrays.equals(run(n, accepted, soft, skip, new HashSet<>(), null, null), mask)) removed.add(m);
        }
        credited.clear();
        run(n, accepted, soft, removed, credited, null, null);
        if (t != null) {
            t.hardMask = hardMask.clone();
            t.softMask = mask.clone();
            t.stage("hard evidence", hardMask);
            t.stage("hints", mask);
        }

        boolean vfp = s.vfpProtocol > 0 && s.vfpProtocol != s.clientProtocol;
        String vfpLabel = s.vfpName != null ? s.vfpName : "protocol " + s.vfpProtocol;
        int[] target = null;
        boolean opened = false;
        if (vfp) {
            target = s.vfpName != null ? spec(d, s.vfpName) : null;
            if (target == null) target = protocolRange(d, s.vfpProtocol);
            boolean[] before = t == null ? null : mask.clone();
            open(hardMask, target);
            opened = open(mask, target);
            if (opened) notes.add("ViaFabricPlus translates from " + vfpLabel + ", every newer backend looks the same");
            if (t != null) t.viaFabricPlus(vfpLabel, target, before, opened, mask);
        } else if (t != null) {
            t.fix(new Evidence(VFP, s.vfpProtocol > 0 ? vfpLabel : "not installed", List.of(), null, false, s.vfpProtocol > 0 ? Fate.UNUSED : Fate.NONE,
                    s.vfpProtocol > 0 ? "not translating, it targets the client's own version" : null));
        }

        String core = core(s.corePack);
        boolean viaBackend = s.vvProtocol > 0 && (VIA_SERVER.equals(s.vvChannel) || VIA_APP.equals(s.vvChannel));
        String viaName = s.vvName != null ? s.vvName : "protocol " + s.vvProtocol;
        String disagree = null;
        String pinned = null;
        int backendProto = -1;
        if (viaBackend && s.vvProtocol > 0) backendProto = s.vvProtocol;
        else if (core != null) { int[] cr = spec(d, core); if (cr != null) backendProto = d.protocols()[cr[0]]; }
        boolean viaTranslated = vfp || (backendProto > 0 && s.clientProtocol > 0 && backendProto != s.clientProtocol);
        boolean[] exactMask;
        if (viaTranslated) {
            exactMask = new boolean[n];
            Arrays.fill(exactMask, true);
        } else {
            exactMask = hardMask.clone();
        }
        Set<String> exactBy = new LinkedHashSet<>();
        for (int k = 0; k < 2; k++) {
            boolean isCore = k == 0;
            if (isCore ? core == null : !viaBackend) continue;
            String method = isCore ? CORE : VIA;
            String name = isCore ? core : viaName;
            String why = isCore ? "core pack " + s.corePack : s.vvChannel + " says " + viaName;
            String signal = isCore ? "minecraft:core " + s.corePack : s.vvChannel + " says " + viaName + (s.vvName != null ? " (protocol " + s.vvProtocol + ")" : "");
            int[] r = isCore ? spec(d, core) : s.vvName != null ? spec(d, s.vvName) : null;
            if (r == null && !isCore) r = protocolRange(d, s.vvProtocol);
            String implied = r != null ? rangeText(d, r[0], r[1]) : name;
            boolean[] before = t == null ? null : exactMask.clone();
            if (r != null) {
                if (pinned == null && narrow(exactMask, new Rule(r[0], r[1], method, why, false), new HashSet<>())) {
                    exactBy.add(method);
                    if (t != null) t.pin(method, signal, implied, before, exactMask, "pins it");
                    continue;
                }
            } else if (pinned != null && pinned.equals(name)) {
                exactBy.add(method);
                if (t != null) t.fix(new Evidence(method, signal, List.of(), implied, false, Fate.AGREES, "agrees with the core pack"));
                continue;
            } else {
                int side = outside(d, isCore ? core : s.vvName);
                int edge = side > 0 ? top : side < 0 ? 0 : -1;
                if (edge >= 0 && pinned == null && exactBy.isEmpty() && exactMask[edge]) {
                    Arrays.fill(exactMask, false);
                    exactMask[edge] = true;
                    pinned = name;
                    exactBy.add(method);
                    notes.add(why + " is " + (side > 0 ? "newer" : "older") + " than the table");
                    if (t != null) {
                        t.fix(new Evidence(method, signal, List.of(), implied, false, Fate.DECISIVE, "pins it, " + (side > 0 ? "newer" : "older") + " than the version table so taken as is"));
                        t.stages.add(new Stage(method, name));
                    }
                    continue;
                }
            }
            if (disagree == null) disagree = (isCore ? "core pack says " : "ViaVersion says ") + name;
            notes.add("not used: " + why + " (evidence says " + maskText(d, exactMask) + ")");
            if (t != null) t.fix(new Evidence(method, signal, List.of(), implied, false, Fate.IGNORED,
                    r == null && outside(d, isCore ? core : s.vvName) == 0 ? "ignored, not in the version table" : "ignored, the evidence says " + maskText(d, exactMask)));
        }
        if (!exactBy.isEmpty()) {
            mask = exactMask;
            credited.clear();
            credited.add(exactBy.contains(CORE) ? CORE : VIA);
            if (viaTranslated) notes.add((vfp ? "ViaFabricPlus" : "ViaVersion") + " synthesises the translated version's tags and registries, so the backend version is taken from the " + (exactBy.contains(CORE) ? "core pack" : "ViaVersion report") + " instead");
        }

        int lo = -1, hi = -1;
        for (int i = 0; i < n; i++) {
            if (!mask[i]) continue;
            if (lo < 0) lo = i;
            hi = i;
        }
        if (t != null) {
            t.exactBy = exactBy.isEmpty() ? null : exactBy.contains(CORE) ? CORE : VIA;
            t.disagree = disagree;
            t.ping(mask, lo, hi, pinned);
        }
        if (lo < 0) {
            if (t != null) t.missing = "the signals leave no version possible";
            return null;
        }
        boolean pingUsed = false;
        if (s.pingName != null && pinned == null && lo < hi) {
            Matcher m = PING_VERSION.matcher(s.pingName.trim());
            if (m.matches()) {
                int p = indexOf(d, m.group(1));
                if (p >= 0 && mask[p]) {
                    boolean hidden = opened && (target == null || p > target[1]);
                    Arrays.fill(mask, false);
                    mask[p] = true;
                    lo = p;
                    hi = p;
                    if (hidden && exactBy.isEmpty()) credited.clear();
                    credited.add(PING);
                    pingUsed = true;
                    if (t != null) t.stages.add(new Stage(PING, d.names[p]));
                }
            }
        }
        if (lo == 0 && hi == top && pinned == null) {
            if (!opened) {
                if (t != null) t.missing = "nothing has narrowed the range yet";
                return null;
            }
            String range = "unknown";
            if (t != null) {
                t.through = VFP + " " + vfpLabel;
                t.confidence = "low";
            }
            return new Result(range + " (hidden by ViaFabricPlus " + vfpLabel + ")", range, "", true, lo, hi, List.copyOf(notes));
        }
        if (credited.isEmpty()) {
            if (t != null) t.missing = "no signal narrowed the range";
            return null;
        }

        List<String> ordered = new ArrayList<>();
        for (String m : METHOD_ORDER) if (credited.contains(m)) ordered.add(m);
        String method = String.join(" + ", ordered);
        int client = indexOf(d, s.clientVersion);
        if (client < 0 && s.clientProtocol > 0) {
            int[] r = protocolRange(d, s.clientProtocol);
            if (r != null) client = r[1];
        }
        boolean translated = vfp || pinned != null || (client >= 0 && !mask[client]);
        boolean loose = exactBy.isEmpty() && !credited.contains(PING);
        String range = pinned != null ? pinned : openText(d, mask, loose);
        StringBuilder text = new StringBuilder(range).append(" (").append(method);
        if (vfp) text.append(", through ViaFabricPlus ").append(vfpLabel);
        else if (translated) text.append(", through ViaVersion");
        if (disagree != null) text.append("; ").append(disagree);
        text.append(')');
        if (t != null) {
            t.through = vfp ? VFP + " " + vfpLabel : translated ? "ViaVersion" : null;
            t.confidence = confidence(t, !exactBy.isEmpty(), disagree, opened, pingUsed);
        }
        return new Result(text.toString(), range, method, translated, lo, hi, List.copyOf(notes));
    }

    private static String confidence(Trace t, boolean exact, String disagree, boolean opened, boolean pingUsed) {
        if (exact) return disagree == null ? "high" : "medium";
        if (opened) return pingUsed ? "medium" : "low";
        boolean hardNarrowed = false;
        for (boolean b : t.hardMask) hardNarrowed |= !b;
        if (!hardNarrowed) return "low";
        boolean contradicted = !t.rejected.isEmpty() || !t.clashed.isEmpty();
        if (pingUsed || contradicted || !Arrays.equals(t.hardMask, t.softMask)) return "medium";
        return "high";
    }

    private static void add(Trace t, List<Rule> list, Rule r, String source, String signal, List<String> details) {
        list.add(r);
        if (t == null) return;
        if (source != null) t.sourceOf.put(r, source);
        if (signal != null) t.signalOf.put(r, signal);
        if (details != null) t.details.put(r, List.copyOf(details));
    }

    private static List<String> biomesOf(Data d, int version, boolean removed) {
        List<String> out = new ArrayList<>();
        int top = d.names.length - 1;
        for (BiomeMarker m : d.biomes) {
            if (removed ? m.present < 0 && m.last < top : m.core == version) out.add(m.name);
        }
        return out;
    }

    private static boolean[] run(int n, List<Rule> hard, List<Rule> soft, Set<String> skip, Set<String> credited, List<String> notes, Set<Rule> failed) {
        boolean[] mask = new boolean[n];
        Arrays.fill(mask, true);
        for (Rule r : hard) {
            if (skip.contains(r.method)) continue;
            if (!narrow(mask, r, credited)) {
                if (notes != null) notes.add("ignored: " + r.why);
                if (failed != null) failed.add(r);
            }
        }
        for (Rule r : soft) {
            if (skip.contains(r.method)) continue;
            if (!narrow(mask, r, credited) && failed != null) failed.add(r);
        }
        return mask;
    }

    private static boolean satisfies(Rule r, int i) {
        boolean inside = i >= r.lo && i <= r.hi;
        return r.exclude != inside;
    }

    private static boolean narrow(boolean[] mask, Rule r, Set<String> credited) {
        boolean any = false, changed = false;
        for (int i = 0; i < mask.length; i++) {
            if (!mask[i]) continue;
            if (satisfies(r, i)) any = true;
            else changed = true;
        }
        if (!any) return false;
        if (!changed) return true;
        for (int i = 0; i < mask.length; i++) if (mask[i] && !satisfies(r, i)) mask[i] = false;
        credited.add(r.method);
        return true;
    }

    private static String maskText(Data d, boolean[] mask) {
        List<String> parts = new ArrayList<>();
        int i = 0;
        while (i < mask.length) {
            if (!mask[i]) {
                i++;
                continue;
            }
            int j = i;
            while (j + 1 < mask.length && mask[j + 1]) j++;
            parts.add(rangeText(d, i, j));
            i = j + 1;
        }
        return parts.isEmpty() ? "?" : String.join(" or ", parts);
    }

    private static String openText(Data d, boolean[] mask, boolean openTop) {
        int top = mask.length - 1;
        if (!openTop || !mask[top]) return maskText(d, mask);
        int start = top;
        while (start > 0 && mask[start - 1]) start--;
        String newest = first(d.names[start]) + " or newer";
        boolean[] rest = mask.clone();
        boolean older = false;
        for (int i = 0; i < start; i++) older |= rest[i];
        if (!older) return newest;
        for (int i = start; i <= top; i++) rest[i] = false;
        return maskText(d, rest) + " or " + newest;
    }

    private static boolean open(boolean[] mask, int[] target) {
        int from = target == null ? 0 : target[0], to = target == null ? mask.length - 1 : target[1];
        boolean reach = false;
        for (int i = from; i <= to; i++) reach |= mask[i];
        if (!reach) return false;
        for (int i = target == null ? 0 : to + 1; i < mask.length; i++) mask[i] = true;
        return true;
    }

    private static int outside(Data d, String name) {
        if (name == null) return 0;
        Matcher m = VERSION_SPAN.matcher(name.trim());
        if (!m.matches()) return 0;
        int[] a = parse(m.group(1)), b = parse(m.group(2) != null ? m.group(2) : m.group(1));
        int[] floor = parse(first(d.names[0])), ceil = parse(last(d.names[d.names.length - 1]));
        if (a == null || b == null || floor == null || ceil == null) return 0;
        if (compare(b, floor) < 0) return -1;
        if (compare(a, ceil) > 0) return 1;
        return 0;
    }

    private static Rule at(Data d, String from, String to, String method, String why) {
        int lo = from == null ? 0 : indexOf(d, from);
        int hi = to == null ? d.names.length - 1 : indexOf(d, to);
        if (lo < 0) lo = 0;
        if (hi < 0) hi = d.names.length - 1;
        return new Rule(lo, hi, method, why, false);
    }

    private static final class Trace {
        Data d;
        Snapshot s;
        String missing;
        String disagree;
        String through;
        String confidence;
        String exactBy;
        String bestText;
        String tagState;
        int entryPresent;
        int namePresent;
        boolean[] hardMask;
        boolean[] softMask;
        List<Rule> hard = List.of();
        List<Rule> soft = List.of();
        final Map<Integer, List<String>> missingEntries = new HashMap<>();
        final Map<Integer, List<String>> missingNames = new HashMap<>();
        final Map<Rule, Rule> uniqueOf = new IdentityHashMap<>();
        final Map<Rule, String> sourceOf = new IdentityHashMap<>();
        final Map<Rule, String> signalOf = new IdentityHashMap<>();
        final Map<Rule, List<String>> details = new IdentityHashMap<>();
        final Set<Rule> rejected = Collections.newSetFromMap(new IdentityHashMap<>());
        final Set<Rule> failed = Collections.newSetFromMap(new IdentityHashMap<>());
        final Set<Rule> clashed = Collections.newSetFromMap(new IdentityHashMap<>());
        final Map<String, List<Evidence>> fixed = new HashMap<>();
        final List<Stage> stages = new ArrayList<>();

        String span(boolean[] mask) {
            boolean all = true;
            for (boolean b : mask) all &= b;
            return all ? "any version" : maskText(d, mask);
        }

        void stage(String step, boolean[] mask) {
            stages.add(new Stage(step, span(mask)));
        }

        void fix(Evidence e) {
            fixed.computeIfAbsent(e.source(), k -> new ArrayList<>()).add(e);
        }

        void pin(String method, String signal, String implied, boolean[] before, boolean[] after, String reason) {
            boolean changed = !Arrays.equals(before, after);
            fix(new Evidence(method, signal, List.of(), implied, false, changed ? Fate.DECISIVE : Fate.AGREES, changed ? reason : "confirms it"));
            if (changed) stage(method, after);
        }

        void viaFabricPlus(String label, int[] target, boolean[] before, boolean opened, boolean[] after) {
            String implied = target == null ? null : rangeText(d, target[0], target[1]);
            if (opened) {
                boolean changed = !Arrays.equals(before, after);
                fix(new Evidence(VFP, "translating from " + label, List.of(), implied, false, changed ? Fate.DECISIVE : Fate.AGREES,
                        changed ? "opens the range upward, every newer backend looks the same through it" : "agrees, nothing newer was ruled out"));
                if (changed) stage(VFP, after);
            } else {
                fix(new Evidence(VFP, "translating from " + label, List.of(), implied, false, Fate.UNUSED,
                        "not used, the evidence (" + span(before) + ") does not reach its target"));
            }
        }

        void ping(boolean[] mask, int lo, int hi, String pinned) {
            if (s.pingName == null) {
                if (s.pingSeen != null) {
                    fix(new Evidence(PING, quote(s.pingSeen), List.of(), null, false, Fate.UNUSED, "not used, a proxy moved you to another backend since the ping"));
                } else {
                    fix(new Evidence(PING, "none", List.of(), null, false, Fate.NONE, "no server-list ping for this server"));
                }
                return;
            }
            String signal = quote(s.pingName);
            Matcher m = PING_VERSION.matcher(s.pingName.trim());
            if (!m.matches()) {
                fix(new Evidence(PING, signal, List.of(), null, false, Fate.UNUSED, "not used, not a plain version number"));
                return;
            }
            int p = indexOf(d, m.group(1));
            if (p < 0) {
                fix(new Evidence(PING, signal, List.of(), m.group(1), false, Fate.UNUSED, "not used, not in the version table"));
                return;
            }
            String implied = d.names[p];
            if (pinned != null) fix(new Evidence(PING, signal, List.of(), implied, false, Fate.UNUSED, "not used, " + pinned + " is already pinned"));
            else if (lo < 0 || !mask[p]) fix(new Evidence(PING, signal, List.of(), implied, false, Fate.IGNORED, "ignored, the evidence says " + (lo < 0 ? "?" : maskText(d, mask))));
            else if (lo == hi) fix(new Evidence(PING, signal, List.of(), implied, false, Fate.AGREES, "agrees"));
            else fix(new Evidence(PING, signal, List.of(), implied, false, Fate.DECISIVE, "picks it out of " + maskText(d, mask)));
        }

        String quote(String ping) {
            return "\"" + ping + "\"" + (s.pingProtocol > 0 ? " (protocol " + s.pingProtocol + ")" : "");
        }

        Report report(Result r) {
            if (d == null || s == null) return new Report(r, missing, confidence, through, disagree, List.copyOf(stages), List.of());
            if (r != null) stages.add(new Stage("named", r.range()));
            List<Evidence> out = new ArrayList<>();
            List<Evidence> core = fixed.get(CORE);
            if (core != null) out.addAll(core);
            else {
                String why = !s.knownPacksReceived ? "no known-packs list received" : s.knownPacksEmpty ? "the known-packs list was empty" : "no minecraft:core entry in the known-packs list";
                out.add(new Evidence(CORE, "none", List.of(), null, false, Fate.NONE, why));
            }
            List<Evidence> via = fixed.get(VIA);
            if (via != null) out.addAll(via);
            else if (s.vvChannel != null) out.add(new Evidence(VIA, s.vvChannel, List.of(), null, false, Fate.NONE, "carried no version"));
            else out.add(new Evidence(VIA, "none", List.of(), null, false, Fate.NONE, "no vv:server_details or vv:app_details payload"));
            if (s.proxyVvProtocol > 0) {
                String name = s.proxyVvName != null ? s.proxyVvName : "protocol " + s.proxyVvProtocol;
                out.add(new Evidence(VIA, "vv:proxy_details says " + name + (s.proxyVvName != null ? " (protocol " + s.proxyVvProtocol + ")" : ""), List.of(), null, false, Fate.UNUSED,
                        "not used, it describes the proxy, not the backend"));
            }
            out.addAll(fixed.getOrDefault(VFP, List.of(new Evidence(VFP, "not installed", List.of(), null, false, Fate.NONE, null))));
            out.addAll(fixed.getOrDefault(PING, List.of(new Evidence(PING, "none", List.of(), null, false, Fate.NONE, "not checked"))));

            out.add(new Evidence(TAGS, tagState != null ? tagState
                    : entryPresent + " of " + d.tags.size() + " entry markers and " + namePresent + " of " + d.tagNames.size() + " tag names present"
                    + (s.playTags ? "" : ", configuration tags only"), List.of(), null, false, Fate.NONE, null));
            groups(out, TAGS);
            hints(out, TAGS);

            List<String> biomes = s.registries.get("minecraft:worldgen/biome");
            out.add(new Evidence(REGISTRIES, biomes == null ? "no biome registry received" : biomes.size() + " biomes received", List.of(), null, false, Fate.NONE, null));
            groups(out, REGISTRIES);
            hints(out, REGISTRIES);

            int before = out.size();
            hints(out, OVERWORLD);
            if (out.size() == before) {
                out.add(new Evidence(OVERWORLD, s.overworldHeight == Integer.MIN_VALUE ? "no overworld dimension received"
                        : "height " + s.overworldHeight + " from y " + s.overworldMinY, List.of(), null, false, Fate.NONE,
                        s.overworldHeight == Integer.MIN_VALUE ? null : "custom, says nothing"));
            }

            before = out.size();
            for (Rule raw : hard) if (raw.method.equals(JOIN)) out.add(evidence(JOIN, raw.why, List.of(), uniqueOf.get(raw), false));
            hints(out, JOIN);
            if (out.size() == before) out.add(new Evidence(JOIN, "nothing telling yet", List.of(), null, false, Fate.NONE, null));

            return new Report(r, missing, confidence, through, disagree, List.copyOf(stages), List.copyOf(out));
        }

        private void groups(List<Evidence> out, String source) {
            Map<Rule, List<String>> groups = new LinkedHashMap<>();
            for (Rule raw : hard) if (raw.method.equals(source)) groups.computeIfAbsent(uniqueOf.get(raw), k -> new ArrayList<>()).add(raw.why);
            List<Map.Entry<Rule, List<String>>> list = new ArrayList<>(groups.entrySet());
            list.sort((a, b) -> a.getKey().lo != b.getKey().lo ? Integer.compare(b.getKey().lo, a.getKey().lo) : Integer.compare(a.getKey().hi, b.getKey().hi));
            for (Map.Entry<Rule, List<String>> e : list) out.add(evidence(source, e.getValue().get(0), List.copyOf(e.getValue()), e.getKey(), false));
        }

        private void hints(List<Evidence> out, String source) {
            for (Rule r : soft) {
                if (!sourceOf.getOrDefault(r, r.method).equals(source)) continue;
                out.add(evidence(source, signalOf.getOrDefault(r, r.why), details.getOrDefault(r, List.of()), r, true));
            }
        }

        private Evidence evidence(String source, String signal, List<String> list, Rule r, boolean hint) {
            Fate fate;
            String reason;
            if (!hint && rejected.contains(r)) {
                fate = Fate.IGNORED;
                reason = "ignored, the rest of the evidence points to " + bestText;
            } else if (failed.contains(r)) {
                fate = Fate.IGNORED;
                reason = hint ? "hint ignored, clashes with stronger signals" : "ignored, clashes with the signals applied before it";
            } else if (hint && exactBy != null) {
                fate = Fate.UNUSED;
                reason = "hint dropped, the version is pinned by the " + exactBy;
            } else {
                String bound = hint ? bind(r, hardMask, softMask) : bind(r, null, hardMask);
                fate = bound != null ? Fate.DECISIVE : Fate.AGREES;
                reason = bound != null ? (hint ? "hint, " + bound : bound) : hint ? "hint, agrees" : "already implied";
            }
            return new Evidence(source, signal, list, implies(r), hint, fate, reason);
        }

        private String bind(Rule r, boolean[] base, boolean[] now) {
            int top = now.length - 1;
            int lo = low(now), hi = high(now);
            int blo = base == null ? 0 : low(base), bhi = base == null ? top : high(base);
            if (lo < 0) return null;
            if (r.exclude) {
                if (r.hi + 1 == lo && lo > blo) return "sets the floor";
                if (r.lo - 1 == hi && hi < bhi) return "sets the ceiling";
                return r.lo > lo && r.hi < hi ? "cuts out " + rangeText(d, r.lo, r.hi) : null;
            }
            boolean floor = r.lo > 0 && r.lo == lo && lo > blo;
            boolean ceiling = r.hi < top && r.hi == hi && hi < bhi;
            if (floor && ceiling) return "sets both ends";
            if (floor) return "sets the floor";
            if (ceiling) return "sets the ceiling";
            return null;
        }

        private String implies(Rule r) {
            int top = d.names.length - 1;
            if (r.exclude) return "not " + rangeText(d, r.lo, r.hi);
            if (r.lo <= 0 && r.hi >= top) return "any version";
            if (r.lo <= 0) return last(d.names[r.hi]) + " or older";
            if (r.hi >= top) return first(d.names[r.lo]) + " or newer";
            return rangeText(d, r.lo, r.hi);
        }

        private static int low(boolean[] mask) {
            for (int i = 0; i < mask.length; i++) if (mask[i]) return i;
            return -1;
        }

        private static int high(boolean[] mask) {
            for (int i = mask.length - 1; i >= 0; i--) if (mask[i]) return i;
            return -1;
        }
    }
}
