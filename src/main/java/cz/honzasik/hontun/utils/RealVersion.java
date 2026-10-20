package cz.honzasik.hontun.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
    }

    public record Result(String text, String range, String method, boolean translated, int lo, int hi, List<String> notes) {}

    private record TagMarker(int present, int core, String registry, String tag, String entry) {}

    private record NameMarker(int present, int core, String registry, String tag) {}

    private record BiomeMarker(String name, int present, int core, int last) {}

    private record Data(String[] names, int[] protocols, List<TagMarker> tags, List<NameMarker> tagNames, List<BiomeMarker> biomes) {}

    private record Rule(int lo, int hi, String method, String why, boolean exclude) {}

    private static final String RESOURCE = "/assets/hontun/realversion.txt";
    private static final Set<String> CONTENT = Set.of("block", "item", "entity_type", "fluid");
    private static final Set<String> TAB_INIT = Set.of("ADD_PLAYER", "INITIALIZE_CHAT", "UPDATE_GAME_MODE", "UPDATE_LISTED", "UPDATE_LATENCY", "UPDATE_DISPLAY_NAME");
    private static final Pattern UUID_NAME = Pattern.compile("^(?:[a-z0-9_.-]+:)?[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    private static final Pattern PING_VERSION = Pattern.compile("^(?:\\S+ )?(\\d+\\.\\d+(?:\\.\\d+)?)$");
    private static final Pattern VERSION_SPAN = Pattern.compile("^(\\d+(?:\\.\\d+)+)(?:\\.x)?(?:-(\\d+(?:\\.\\d+)+)(?:\\.x)?)?$");
    private static final String TAGS = "tags";
    private static final String REGISTRIES = "registries";
    private static final String JOIN = "join packets";
    private static final String CORE = "core pack";
    private static final String VIA = "ViaVersion details";
    private static final String PING = "ping";
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
        Data d = data();
        if (d == null || s == null) return null;
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
        if (tagsUsable && via && !s.playTags) {
            Set<String> hosts = new HashSet<>();
            for (TagMarker m : d.tags) if (tags.size(m.registry, m.tag) > 0) hosts.add(m.registry + "/" + m.tag);
            tagsUsable = hosts.size() >= 3;
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
                } else if (c == 0) {
                    st[1]++;
                }
            }
            for (NameMarker m : d.tagNames) {
                int size = tags.size(m.registry, m.tag);
                int[] st = nameSteps.computeIfAbsent(m.core, k -> new int[3]);
                if (CONTENT.contains(m.registry)) st[2]++;
                if (size >= 0) {
                    st[0]++;
                    hard.add(new Rule(m.present, top, TAGS, "tag #" + m.registry + "/" + m.tag, false));
                } else if (CONTENT.contains(m.registry) && tags.tagCount(m.registry) >= 10) {
                    st[1]++;
                }
            }
        }

        for (Map.Entry<Integer, int[]> e : tagSteps.entrySet()) {
            int[] st = e.getValue();
            if (st[0] == 0 && st[1] > 0 && st[1] >= Math.min(2, st[2])) soft.add(new Rule(0, e.getKey() - 1, TAGS, st[1] + " tag entries of " + d.names[e.getKey()] + " missing", false));
        }
        for (Map.Entry<Integer, int[]> e : nameSteps.entrySet()) {
            int[] st = e.getValue();
            if (st[0] == 0 && st[1] >= Math.min(2, st[2]) && st[1] > 0) soft.add(new Rule(0, e.getKey() - 1, TAGS, st[1] + " tags of " + d.names[e.getKey()] + " missing", false));
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
                if (!e.getValue()) soft.add(new Rule(0, e.getKey() - 1, REGISTRIES, "no biome of " + d.names[e.getKey()], false));
            }
            if (anyOld && !oldNames) {
                int after = 0;
                for (BiomeMarker m : d.biomes) if (m.present < 0 && m.last < top) after = Math.max(after, m.last + 1);
                soft.add(new Rule(after, top, REGISTRIES, "none of the biomes removed in " + d.names[after], false));
            }
        }
        if (tabShape && !s.firstTabActions.contains("UPDATE_LIST_ORDER")) soft.add(at(d, null, "1.21.1", JOIN, "tab list without the list-order action"));
        else if (tabShape && !s.firstTabActions.contains("UPDATE_HAT")) soft.add(at(d, null, "1.21.3", JOIN, "tab list without the hat action"));
        boolean limbo = s.configFinished && !s.featuresSeen && !via;
        if (s.loggedIn && !s.tickingState && !limbo && s.sinceLoginMs >= 3000) soft.add(at(d, null, "1.20.2", JOIN, "no ticking-state packet"));
        if (s.loggedIn && s.seaLevel == 64) soft.add(at(d, null, "1.21.1", JOIN, "sea level 64 (ViaVersion default for 1.21.1 and older)"));
        if (s.overworldMinY == 0 && s.overworldHeight == 256) soft.add(at(d, null, "1.17.1", REGISTRIES, "overworld height 0-256"));
        else if (s.overworldMinY == -64 && s.overworldHeight == 384) soft.add(at(d, "1.18", null, REGISTRIES, "overworld height -64-320"));
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
        for (Rule r : hard) if (seenRules.add(r.method + "|" + r.lo + "|" + r.hi + "|" + r.exclude)) unique.add(r);
        int[] score = new int[n];
        for (Rule r : unique) for (int i = 0; i < n; i++) if (satisfies(r, i)) score[i]++;
        int best = 0;
        for (int i = 0; i < n; i++) best = Math.max(best, score[i]);
        List<Rule> accepted = new ArrayList<>();
        for (String method : METHOD_ORDER) {
            for (Rule r : unique) {
                if (!r.method.equals(method)) continue;
                boolean agrees = false;
                for (int i = 0; i < n && !agrees; i++) if (score[i] == best && satisfies(r, i)) agrees = true;
                if (agrees) accepted.add(r);
                else notes.add("ignored: " + r.why);
            }
        }
        Set<String> credited = new LinkedHashSet<>();
        boolean[] hardMask = run(n, accepted, List.of(), Set.of(), new HashSet<>(), notes);
        boolean[] mask = run(n, accepted, soft, Set.of(), credited, null);
        Set<String> removed = new HashSet<>();
        for (String m : List.of(REGISTRIES, JOIN, TAGS)) {
            Set<String> skip = new HashSet<>(removed);
            skip.add(m);
            if (Arrays.equals(run(n, accepted, soft, skip, new HashSet<>(), null), mask)) removed.add(m);
        }
        credited.clear();
        run(n, accepted, soft, removed, credited, null);

        boolean vfp = s.vfpProtocol > 0 && s.vfpProtocol != s.clientProtocol;
        String vfpLabel = s.vfpName != null ? s.vfpName : "protocol " + s.vfpProtocol;
        int[] target = null;
        boolean opened = false;
        if (vfp) {
            target = s.vfpName != null ? spec(d, s.vfpName) : null;
            if (target == null) target = protocolRange(d, s.vfpProtocol);
            open(hardMask, target);
            opened = open(mask, target);
            if (opened) notes.add("ViaFabricPlus translates from " + vfpLabel + ", every newer backend looks the same");
        }

        String core = core(s.corePack);
        boolean viaBackend = s.vvProtocol > 0 && (VIA_SERVER.equals(s.vvChannel) || VIA_APP.equals(s.vvChannel));
        String viaName = s.vvName != null ? s.vvName : "protocol " + s.vvProtocol;
        String disagree = null;
        String pinned = null;
        boolean[] exactMask = hardMask.clone();
        Set<String> exactBy = new LinkedHashSet<>();
        for (int k = 0; k < 2; k++) {
            boolean isCore = k == 0;
            if (isCore ? core == null : !viaBackend) continue;
            String method = isCore ? CORE : VIA;
            String name = isCore ? core : viaName;
            String why = isCore ? "core pack " + s.corePack : s.vvChannel + " says " + viaName;
            int[] r = isCore ? spec(d, core) : s.vvName != null ? spec(d, s.vvName) : null;
            if (r == null && !isCore) r = protocolRange(d, s.vvProtocol);
            if (r != null) {
                if (pinned == null && narrow(exactMask, new Rule(r[0], r[1], method, why, false), new HashSet<>())) {
                    exactBy.add(method);
                    continue;
                }
            } else if (pinned != null && pinned.equals(name)) {
                exactBy.add(method);
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
                    continue;
                }
            }
            if (disagree == null) disagree = (isCore ? "core pack says " : "ViaVersion says ") + name;
            notes.add("not used: " + why + " (evidence says " + maskText(d, exactMask) + ")");
        }
        if (!exactBy.isEmpty()) {
            mask = exactMask;
            credited.clear();
            credited.add(exactBy.contains(CORE) ? CORE : VIA);
        }

        int lo = -1, hi = -1;
        for (int i = 0; i < n; i++) {
            if (!mask[i]) continue;
            if (lo < 0) lo = i;
            hi = i;
        }
        if (lo < 0) return null;
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
                }
            }
        }
        if (lo == 0 && hi == top && pinned == null) {
            if (!opened) return null;
            String range = "unknown";
            return new Result(range + " (hidden by ViaFabricPlus " + vfpLabel + ")", range, "", true, lo, hi, List.copyOf(notes));
        }
        if (credited.isEmpty()) return null;

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
        return new Result(text.toString(), range, method, translated, lo, hi, List.copyOf(notes));
    }

    private static boolean[] run(int n, List<Rule> hard, List<Rule> soft, Set<String> skip, Set<String> credited, List<String> notes) {
        boolean[] mask = new boolean[n];
        Arrays.fill(mask, true);
        for (Rule r : hard) {
            if (skip.contains(r.method)) continue;
            if (!narrow(mask, r, credited) && notes != null) notes.add("ignored: " + r.why);
        }
        for (Rule r : soft) {
            if (skip.contains(r.method)) continue;
            narrow(mask, r, credited);
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
}
