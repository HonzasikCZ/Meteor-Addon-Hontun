package cz.honzasik.hontun.copy;

import cz.honzasik.hontun.copy.capture.Motion;
import cz.honzasik.hontun.copy.capture.SidebarCapture;
import cz.honzasik.hontun.mixin.client.copy.PlayerTabOverlayAccessor;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class Recorder {
    public static final Recorder INSTANCE = new Recorder();

    public static final long MIN_MS = 4000;
    public static final long MAX_MS = 12000;
    private static final int TICK_MS = 50;
    private static final int MAX_FRAMES = 300;

    public record Result(Motion.Tab tab, Motion.Sidebar sidebar, String error) {}

    private static final class Track {
        final List<StyledText> values = new ArrayList<>();
        final List<String> keys = new ArrayList<>();
        final List<Long> starts = new ArrayList<>();

        void add(StyledText value, long now) {
            String key = value.key();
            if (!keys.isEmpty() && keys.getLast().equals(key)) return;
            values.add(value);
            keys.add(key);
            starts.add(now);
        }
    }

    private enum State { STATIC, DYNAMIC, CYCLE, OPEN }

    private record Verdict(State state, Motion.Anim anim, boolean settled) {}

    private boolean subscribed;
    private Target.Kind kind;
    private long start;
    private Consumer<Result> done;
    private final Map<String, Track> tracks = new LinkedHashMap<>();
    private final Set<Integer> headerCounts = new HashSet<>();
    private final Set<Integer> footerCounts = new HashSet<>();
    private final Set<Integer> sidebarCounts = new HashSet<>();
    private boolean sawSidebar;

    private Recorder() {}

    public boolean busy() {
        return done != null;
    }

    public void start(Target.Kind kind, Consumer<Result> done) {
        if (!subscribed) {
            MeteorClient.EVENT_BUS.subscribe(this);
            subscribed = true;
        }
        this.kind = kind;
        this.done = done;
        this.start = System.currentTimeMillis();
        tracks.clear();
        headerCounts.clear();
        footerCounts.clear();
        sidebarCounts.clear();
        sawSidebar = false;
        sample(start);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (done == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.getConnection() == null) {
            finish(new Result(null, null, "Disconnected while recording."));
            return;
        }
        long now = System.currentTimeMillis();
        sample(now);
        long elapsed = now - start;
        if (elapsed >= MAX_MS || (elapsed >= MIN_MS && settled())) finish(result());
    }

    public void onTabPacket() {
        if (done == null || kind != Target.Kind.TAB) return;
        sample(System.currentTimeMillis());
    }

    @EventHandler
    private void onFrame(Render2DEvent event) {
        if (done == null || kind != Target.Kind.SCOREBOARD) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        sample(System.currentTimeMillis());
    }

    @EventHandler
    private void onLeave(GameLeftEvent event) {
        if (done != null) finish(new Result(null, null, "Disconnected while recording."));
    }

    private void finish(Result result) {
        Consumer<Result> callback = done;
        done = null;
        if (callback != null) callback.accept(result);
    }

    private void sample(long now) {
        Minecraft mc = Minecraft.getInstance();
        if (kind == Target.Kind.TAB) {
            PlayerTabOverlayAccessor tab = (PlayerTabOverlayAccessor) mc.gui.hud.getTabList();
            block("header", StyledText.of(tab.hontun$header()), headerCounts, now);
            block("footer", StyledText.of(tab.hontun$footer()), footerCounts, now);
        } else if (kind == Target.Kind.SCOREBOARD) {
            SidebarCapture.Snapshot s = SidebarCapture.take();
            if (s == null) return;
            sawSidebar = true;
            track("title").add(s.title(), now);
            sidebarCounts.add(s.lines().size());
            for (int i = 0; i < s.lines().size(); i++) {
                SidebarCapture.Line line = s.lines().get(i);
                track("line:" + i).add(line.text(), now);
                track("right:" + i).add(line.right() == null ? StyledText.EMPTY : line.right(), now);
            }
        }
    }

    private void block(String name, StyledText text, Set<Integer> counts, long now) {
        track(name).add(text, now);
        List<StyledText> lines = text.isEmpty() ? List.of() : text.lines();
        counts.add(lines.size());
        for (int i = 0; i < lines.size(); i++) track(name + ":" + i).add(lines.get(i), now);
    }

    private Track track(String key) {
        return tracks.computeIfAbsent(key, k -> new Track());
    }

    private boolean settled() {
        for (Track t : tracks.values()) {
            if (!verdict(t).settled()) return false;
        }
        return true;
    }

    private Result result() {
        if (kind == Target.Kind.TAB) {
            List<String> notes = new ArrayList<>();
            Motion.Block header = block("header", headerCounts, notes);
            Motion.Block footer = block("footer", footerCounts, notes);
            return new Result(new Motion.Tab(header, footer, List.copyOf(notes)), null, null);
        }
        if (!sawSidebar) return new Result(null, null, "No sidebar scoreboard is shown right now.");
        List<String> notes = new ArrayList<>();
        Motion.Anim title = anim("title", "The scoreboard title", notes);
        Map<Integer, Motion.Anim> lines = new LinkedHashMap<>();
        Map<Integer, Motion.Anim> rights = new LinkedHashMap<>();
        if (sidebarCounts.size() == 1) {
            int n = sidebarCounts.iterator().next();
            for (int i = 0; i < n; i++) {
                Motion.Anim a = anim("line:" + i, "Scoreboard line " + (i + 1), notes);
                if (a != null) lines.put(i, a);
                Motion.Anim r = anim("right:" + i, "The right side of scoreboard line " + (i + 1), notes);
                if (r != null) rights.put(i, r);
            }
        } else {
            notes.add("The number of scoreboard lines changed while recording, only the current lines were copied");
        }
        return new Result(null, new Motion.Sidebar(title, Map.copyOf(lines), Map.copyOf(rights), List.copyOf(notes)), null);
    }

    private Motion.Block block(String name, Set<Integer> counts, List<String> notes) {
        String label = name.equals("header") ? "Tab header" : "Tab footer";
        if (counts.size() <= 1) {
            int n = counts.isEmpty() ? 0 : counts.iterator().next();
            Map<Integer, Motion.Anim> lines = new LinkedHashMap<>();
            for (int i = 0; i < n; i++) {
                Motion.Anim a = anim(name + ":" + i, label + " line " + (i + 1), notes);
                if (a != null) lines.put(i, a);
            }
            return new Motion.Block(Map.copyOf(lines), null);
        }
        return new Motion.Block(Map.of(), anim(name, label, notes));
    }

    private Motion.Anim anim(String key, String label, List<String> notes) {
        Track t = tracks.get(key);
        if (t == null) return null;
        Verdict v = verdict(t);
        if (v.state() == State.CYCLE) return v.anim();
        if (v.state() == State.DYNAMIC || v.state() == State.OPEN) {
            notes.add(label + " kept changing without repeating (probably a placeholder), its current value was copied");
        }
        return null;
    }

    private Verdict verdict(Track t) {
        int len = t.keys.size();
        if (len <= 1) return new Verdict(State.STATIC, null, true);
        int period = period(t);
        if (period > 0) {
            Motion.Anim anim = expand(t, period, start);
            boolean twice = len - period >= period;
            return new Verdict(State.CYCLE, anim, twice);
        }
        if (len >= 6 && new HashSet<>(t.keys).size() == len) {
            long[] d = new long[len - 2];
            for (int j = 1; j < len - 1; j++) d[j - 1] = t.starts.get(j + 1) - t.starts.get(j);
            Arrays.sort(d);
            return new Verdict(State.DYNAMIC, null, d[d.length / 2] >= 900);
        }
        return new Verdict(State.OPEN, null, false);
    }

    private static int period(Track t) {
        List<String> keys = t.keys;
        int len = keys.size();
        for (int p = 2; p < len; p++) {
            int checks = len - p;
            int need = p <= 3 ? 2 * p : 3;
            if (checks < need) {
                if (p > 3) break;
                continue;
            }
            int matches = 0;
            for (int i = 0; i < checks; i++) if (keys.get(i).equals(keys.get(i + p))) matches++;
            boolean ok = checks < 10 ? matches == checks : matches >= Math.ceil(checks * 0.9);
            if (ok && regular(t, p)) return p;
        }
        return -1;
    }

    private static boolean regular(Track t, int p) {
        int len = t.keys.size();
        for (int q = 0; q < p; q++) {
            long min = Long.MAX_VALUE;
            long max = Long.MIN_VALUE;
            long sum = 0;
            int n = 0;
            for (int j = 1; j < len - 1; j++) {
                if (j % p != q) continue;
                long d = t.starts.get(j + 1) - t.starts.get(j);
                min = Math.min(min, d);
                max = Math.max(max, d);
                sum += d;
                n++;
            }
            if (n >= 2 && max - min > Math.max(120, 0.25 * (sum / (double) n))) return false;
        }
        return true;
    }

    private static Motion.Anim expand(Track t, int period, long origin) {
        int len = t.keys.size();
        double[] mean = new double[period];
        int[] count = new int[period];
        for (int j = 1; j < len - 1; j++) {
            mean[j % period] += t.starts.get(j + 1) - t.starts.get(j);
            count[j % period]++;
        }
        double min = Double.MAX_VALUE;
        for (int q = 0; q < period; q++) {
            if (count[q] == 0) continue;
            mean[q] /= count[q];
            min = Math.min(min, mean[q]);
        }
        int first = len > 2 ? 1 : 0;
        int cycles = (len - 1 - first) / period;
        double cycle = cycles >= 1 ? (t.starts.get(first + cycles * period) - t.starts.get(first)) / (double) cycles : -1;

        int base = TICK_MS;
        int[] k = new int[period];
        if (min != Double.MAX_VALUE) {
            int top = (int) Math.max(TICK_MS, Math.floor((min + 25) / TICK_MS) * TICK_MS);
            boolean found = false;
            for (int b = top; b >= TICK_MS && !found; b -= TICK_MS) {
                int total = 0;
                boolean ok = true;
                for (int q = 0; q < period; q++) {
                    if (count[q] == 0) {
                        k[q] = 1;
                        total += 1;
                        continue;
                    }
                    double ratio = mean[q] / b;
                    long r = Math.round(ratio);
                    if (r < 1 || Math.abs(ratio - r) > 0.25) {
                        ok = false;
                        break;
                    }
                    k[q] = (int) r;
                    total += k[q];
                }
                if (ok && cycle > 0 && Math.abs(total * (double) b - cycle) > 50 + cycle * 0.05) ok = false;
                if (ok) {
                    base = b;
                    found = true;
                }
            }
            if (!found) {
                base = TICK_MS;
                for (int q = 0; q < period; q++) k[q] = count[q] == 0 ? 1 : (int) Math.max(1, Math.round(mean[q] / TICK_MS));
            }
        } else {
            base = 1000;
            Arrays.fill(k, 1);
        }

        List<StyledText> frames = new ArrayList<>();
        for (int i = 0; i < period && frames.size() < MAX_FRAMES; i++) {
            int j = Math.min(first + i, len - 1);
            StyledText value = t.values.get(j);
            for (int r = 0; r < k[j % period] && frames.size() < MAX_FRAMES; r++) frames.add(value);
        }
        long offset = t.starts.get(first) - origin;
        int shift = (int) Math.floorMod(Math.round(offset / (double) base), (long) frames.size());
        Collections.rotate(frames, shift);
        return new Motion.Anim(List.copyOf(frames), base);
    }

    public static List<List<StyledText>> compose(List<StyledText> fixed, Map<Integer, Motion.Anim> lines, Motion.Anim whole, int step, int count) {
        List<List<StyledText>> out = new ArrayList<>();
        for (int f = 0; f < count; f++) {
            long t = (long) f * step;
            if (whole != null) {
                out.add(whole.frames().get((int) ((t / whole.interval()) % whole.frames().size())).lines());
                continue;
            }
            List<StyledText> frame = new ArrayList<>(fixed);
            for (Map.Entry<Integer, Motion.Anim> e : lines.entrySet()) {
                if (e.getKey() >= frame.size()) continue;
                Motion.Anim a = e.getValue();
                frame.set(e.getKey(), a.frames().get((int) ((t / a.interval()) % a.frames().size())));
            }
            out.add(frame);
        }
        return out;
    }

    public static long gcd(long a, long b) {
        while (b != 0) {
            long r = a % b;
            a = b;
            b = r;
        }
        return Math.abs(a);
    }

    public static long lcm(long a, long b) {
        if (a == 0 || b == 0) return Math.max(a, b);
        return a / gcd(a, b) * b;
    }

    public static int[] intervals(Motion.Block... blocks) {
        List<Integer> out = new ArrayList<>();
        for (Motion.Block b : blocks) {
            if (b == null) continue;
            if (b.whole() != null) out.add(b.whole().interval());
            if (b.lines() != null) for (Motion.Anim a : b.lines().values()) out.add(a.interval());
        }
        return out.stream().mapToInt(Integer::intValue).toArray();
    }

    public static long cycle(Motion.Block... blocks) {
        long c = 1;
        for (Motion.Block b : blocks) {
            if (b == null) continue;
            if (b.whole() != null) c = lcm(c, b.whole().cycle());
            if (b.lines() != null) for (Motion.Anim a : b.lines().values()) c = lcm(c, a.cycle());
        }
        return c;
    }
}
