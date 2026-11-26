package cz.honzasik.hontun.gui.screen.server;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import cz.honzasik.hontun.commands.PlayerRoster;
import cz.honzasik.hontun.commands.PluginScanner;
import cz.honzasik.hontun.gui.widget.HontunCards;
import cz.honzasik.hontun.gui.widget.TerminalWidgets;
import cz.honzasik.hontun.mixin.network.custompayload.ClientListenerConnectionAccessor;
import cz.honzasik.hontun.modules.AnticheatDetect;
import cz.honzasik.hontun.modules.ChannelFetcher;
import cz.honzasik.hontun.utils.ChannelPayloads;
import cz.honzasik.hontun.utils.CookieObserver;
import cz.honzasik.hontun.utils.HontunTheme;
import cz.honzasik.hontun.utils.Oversize;
import cz.honzasik.hontun.utils.RealVersion;
import cz.honzasik.hontun.utils.ResourcePackInfo;
import cz.honzasik.hontun.utils.ServerSoftware;
import cz.honzasik.hontun.utils.VersionKeeper;
import cz.honzasik.hontun.utils.WorldInfo;
import meteordevelopment.meteorclient.utils.world.TickRate;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ServerPages {
    private static final Set<String> ANTICHEAT = Set.of(
        "nocheatplus", "negativity", "warden", "horizon", "vulcan", "abc", "spartan", "kauri", "anticheatreloaded",
        "witherac", "godseye", "matrix", "wraith", "antixrayheuristics", "grimac", "grim", "themis", "foxaddition",
        "guardianac", "ggintegrity", "lightanticheat", "polar", "intave", "polarloader", "fairplay", "cultac",
        "angleguard", "totemguard", "realac", "karhu", "karhuac", "verus", "grizzly");
    private static final Set<String> ANTIEXPLOIT = Set.of(
        "illegalstack", "exploitsx", "anarchyexploitfixes", "exploitfixer", "coffeeprotect", "lpx", "antispoof",
        "antihealthindicator", "sonar", "foxgate", "foxgateplus");
    private static final Set<String> HIDERS = Set.of("pl-hide-pro", "plhidepro");
    private static final String[] CATEGORIES = {"Anticheat", "Anti-exploit", "Plugin hider", "Other"};
    private static final Pattern PACK = Pattern.compile("^(\\S+) \\((.+)\\)$");
    private static final Pattern PERM = Pattern.compile("^(\\S+) \\((.+)\\)$");
    private static final Pattern CMD = Pattern.compile("\\(([^\\s()]+) command\\)");
    private static final Pattern SURE = Pattern.compile("pings (\\d+)-");
    private static final Pattern PROXY_WORD = Pattern.compile("\\bproxy\\b");
    private static final int[][] BAND_CELLS = {{170, 126, 126, 148}, {184, 130, 136, 140}, {179, 120, 129, 0},
            {230, 170, 170, 170}, {204, 168, 170, 170}, {236, 180, 190, 200}};

    private ServerPages() {}

    static Doc build(ServerInfoScreen s, int w) {
        Doc d = new Doc();
        counts(s);
        switch (s.page) {
            case OVERVIEW -> overview(s, d, w);
            case SOFTWARE -> software(s, d, w);
            case ANTICHEAT -> anticheat(d, w);
            case VERSION -> version(d, w);
            case PLUGINS -> plugins(d, w);
            case PLAYERS -> players(s, d, w);
            case WORLD -> world(s, d, w);
            case CONNECTION -> connection(s, d, w);
            case CHANNELS -> channels(s, d, w);
            case COOKIES -> cookies(s, d, w);
        }
        return d;
    }

    private static Minecraft mc() { return Minecraft.getInstance(); }

    private static ServerSoftware.Result softwareResult() {
        try { return ServerSoftware.detect(); } catch (Throwable t) { return null; }
    }

    private static AnticheatDetect.Verdict verdict() {
        try { return AnticheatDetect.COLLECTOR.evaluate(AnticheatDetect.address()); } catch (Throwable t) { return null; }
    }

    private static RealVersion.Report report() {
        try { return RealVersion.explain(ServerSoftware.realVersionSnapshot()); } catch (Throwable t) { return null; }
    }

    static List<PlayerInfo> tab() {
        ClientPacketListener c = mc().getConnection();
        if (c == null) return List.of();
        List<PlayerInfo> list = new ArrayList<>(c.getListedOnlinePlayers());
        list.sort(Comparator.comparingInt((PlayerInfo p) -> -p.getTabListOrder())
                .thenComparing(p -> p.getTeam() != null ? p.getTeam().getName() : "")
                .thenComparing(p -> p.getProfile().name(), String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    private static List<String> channelNames() {
        Set<String> all = new LinkedHashSet<>();
        synchronized (ChannelFetcher.REGISTERED) { all.addAll(ChannelFetcher.REGISTERED); }
        synchronized (ChannelFetcher.SEEN) { all.addAll(ChannelFetcher.SEEN); }
        List<String> list = new ArrayList<>(all);
        list.sort(String::compareTo);
        return list;
    }

    private static void counts(ServerInfoScreen s) {
        ServerSoftware.Result r = softwareResult();
        if (r != null && r.evidence() != null) s.counts.put(ServerInfoScreen.Page.SOFTWARE, String.valueOf(r.evidence().size()));
        if (PluginScanner.INSTANCE.scanned()) s.counts.put(ServerInfoScreen.Page.PLUGINS, String.valueOf(PluginScanner.INSTANCE.results().size()));
        int ch = channelNames().size();
        if (ch > 0) s.counts.put(ServerInfoScreen.Page.CHANNELS, String.valueOf(ch));
        int ck = CookieObserver.list().size();
        if (ck > 0) s.counts.put(ServerInfoScreen.Page.COOKIES, String.valueOf(ck));
    }

    static String category(String name) {
        String low = name.toLowerCase(Locale.ROOT);
        if (HIDERS.contains(low) || low.contains("pluginhid") || low.startsWith("plhide")) return CATEGORIES[2];
        if (ANTIEXPLOIT.contains(low) || low.contains("exploit")) return CATEGORIES[1];
        if (ANTICHEAT.contains(low) || low.contains("anticheat")) return CATEGORIES[0];
        return CATEGORIES[3];
    }

    static int t6(int sm, int m2, int m1, int hv, int va, int te) {
        if (Paint.term()) return te;
        if (HontunTheme.smog()) return sm;
        if (Paint.vanilla()) return va;
        if (HontunTheme.hvanilla()) return hv;
        return HontunTheme.modern2() ? m2 : m1;
    }

    static int sx(int v, int w) {
        return Math.round(v * w / (float) Paint.iwRef());
    }

    static int titleTop() { return t6(3, 14, 14, 16, 16, 12); }

    static int blockTop() { return t6(24, 34, 32, 40, 40, 30); }

    static int emptyTop() { return t6(24, 34, 32, 36, 40, 30); }

    static Doc.Line head(String title) {
        Doc.Line l = new Doc.Line(10);
        int x = 0, color = Paint.text();
        Component c;
        if (Paint.term()) {
            c = Paint.c("[" + title.trim().toLowerCase(Locale.ROOT).replace(' ', '-') + "]");
            color = Paint.acc();
        } else if (HontunTheme.smog()) {
            c = Paint.lg(title);
        } else {
            c = Paint.c(title);
        }
        if (HontunTheme.modern1()) {
            l.tick = true;
            x = 5;
        }
        Doc.Span s = new Doc.Span(x, c, color, Doc.TEXT);
        l.add(s);
        return l.at(x + s.width() + 6);
    }

    static Doc.Line head(String title, String sub) {
        Doc.Line l = head(title);
        if (sub != null && !sub.isEmpty()) sum(l, sub, Paint.dim(), 0);
        return l;
    }

    static Doc.Line title(Doc d, String title) {
        d.add(new Doc.Gap(titleTop() - 1));
        Doc.Line l = head(title);
        d.add(l);
        return l;
    }

    static Doc.Line sum(Doc.Line l, String s, int color, int max) {
        Doc.Span sp = new Doc.Span(l.cur, HontunTheme.smog() ? Paint.sm(s) : Paint.c(s), color, Doc.TEXT);
        if (max != 0) sp.max(Math.max(20, max));
        l.add(sp);
        return l.sp(sp.width());
    }

    static void gapTo(Doc d, int y) {
        int at = 0;
        for (Doc.Item it : d.full) at += it.h;
        if (y > at) d.add(new Doc.Gap(y - at));
    }

    static int kvGap() { return Paint.term() ? 6 : 5; }

    static int kw(ServerInfoScreen.Page p) {
        return switch (p) {
            case WORLD -> t6(130, 140, 120, 124, 128, 132);
            case ANTICHEAT -> t6(120, 104, 96, 110, 112, 96);
            case CONNECTION -> t6(120, 110, 120, 96, 112, 96);
            case PLAYERS -> t6(120, 150, 120, 96, 128, 120);
            default -> t6(120, 110, 120, 96, 104, 120);
        };
    }

    static boolean rowRules() {
        return HontunTheme.smog() || HontunTheme.modern2() || HontunTheme.modern1();
    }

    static Doc.Line kvRow(String key, int kw, boolean ruleTop) {
        Doc.Line l = new Doc.Line(20).text(0, key, Paint.dim(), kw - 4);
        l.ruleTop = ruleTop && rowRules();
        return l.at(kw);
    }

    static void edge(Doc d, Doc.Line first, Doc.Line last) {
        if (first == null || last == null) return;
        if (rowRules()) {
            first.ruleTop = true;
            last.ruleBottom = true;
            return;
        }
        int a = d.full.indexOf(first), b = d.full.indexOf(last);
        if (a < 0 || b < a) return;
        int h = Paint.term() ? 1 : 2;
        d.full.add(b + 1, new Doc.Sep(h, 0, Paint.vanilla() ? Doc.RULE_F : Doc.RULE));
        d.full.add(a, new Doc.Sep(h, 0, Doc.RULE));
    }

    static Doc.Note note(Doc d, String text) {
        d.add(new Doc.Gap(HontunTheme.smog() ? 8 : 10));
        Doc.Note n = Doc.note(text);
        d.add(n);
        return n;
    }

    static int btnW(String label) {
        int w = Paint.w(Paint.c(label));
        if (Paint.term()) return w + 6 * TerminalWidgets.cw();
        return w + t6(20, 36, 20, 14, 16, 0);
    }

    static int btnGap() { return t6(8, 8, 6, 6, 4, 6); }

    private static Doc.Well panel() {
        Doc.Well w = new Doc.Well();
        w.style = Doc.PANEL;
        if (Paint.vanilla()) w.inner.add(new Doc.Gap(3));
        return w;
    }

    private static Doc.Well close(Doc.Well w) {
        if (Paint.vanilla()) w.inner.add(new Doc.Gap(3));
        return w;
    }

    private static int level(String c) {
        if (c == null) return 0;
        String l = c.toLowerCase(Locale.ROOT);
        return l.contains("high") ? 3 : l.contains("medium") ? 2 : l.contains("low") ? 1 : 0;
    }

    static String conf(String c) {
        if (c == null || c.isBlank()) return "";
        String s = c.toLowerCase(Locale.ROOT).endsWith("confidence") ? c : c + " confidence";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    static String or(String v, String fallback) {
        return v == null || v.isBlank() ? fallback : v;
    }

    static String cap(String s) {
        if (s == null || s.isBlank()) return "";
        String t = s.strip();
        return Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }

    private static int heroPad() { return t6(12, 12, 11, 0, 0, 0); }

    private static int heroRowTop() { return t6(48, 49, 48, 55, 55, 42); }

    private static int heroBase() { return t6(52, 54, 58, 72, 72, 48); }

    private static String fitBig(String s, int max) {
        if (s.length() <= max && Paint.bigWidth(Paint.c(s)) <= max) return s;
        String t = s.length() > max ? s.substring(0, max) : s;
        while (t.length() > 1 && Paint.bigWidth(Paint.c(t + "…")) > max) t = t.substring(0, t.length() - 1);
        return t.stripTrailing() + "…";
    }

    static Doc.Box hero(Doc d, int w, int h, String label, String value, String suffix, String pill, int tone, int icon, String conf, String plain) {
        boolean term = Paint.term(), smog = HontunTheme.smog();
        int top = t6(0, 8, 12, 0, 0, 0), bp = heroPad(), right = w - bp;
        Doc.Box box = new Doc.Box(h);
        if (smog || HontunTheme.modern2() || HontunTheme.modern1()) {
            box.style = Doc.BAND;
        } else {
            box.bleed = true;
            box.ruleBottom = true;
        }
        Doc.Line l = new Doc.Line(Math.min(h, heroRowTop()));
        if (smog) l.small(bp, label, Paint.dim());
        else l.text(bp, label, Paint.dim());
        l.last().dy(t6(10, 9, 10, 16, 16, 12));
        int lv = level(conf);
        String ct = lv > 0 ? conf(conf) : conf != null && !conf.isBlank() ? cap(conf) : cap(plain);
        int left = right;
        if (!ct.isEmpty()) {
            left -= Paint.w(Paint.c(ct));
            l.text(left, ct, Paint.sub()).last().dy(t6(27, 28, 27, 34, 33, 26));
            if (lv > 0) {
                left -= Paint.meterLGap() + Paint.meterLW();
                l.meterL(left, lv).last().dy(t6(27, 29, 28, 35, 33, 26));
            }
        }
        int vy = t6(22, 25, 22, 31, 29, 26), gap = t6(8, 8, 8, 8, 8, 12);
        int pw = pill != null && !pill.isBlank() ? Paint.pillWidth(Paint.c(pill), tone, icon) : 0;
        int sw = suffix != null ? 4 + Paint.w(Paint.c(suffix)) : 0;
        String v = fitBig(value, Math.max(40, left - 12 - bp - sw - (pw > 0 ? gap + pw : 0)));
        if (term) l.text(bp, v, Paint.text());
        else l.big(bp, v, Paint.text());
        l.last().dy(vy);
        if (!v.equals(value)) l.last().tip(List.of(Paint.c(value + (suffix != null ? " " + suffix : ""))));
        int x = bp + Paint.bigWidth(Paint.c(v));
        if (suffix != null) {
            l.text(x + 4, suffix, Paint.sub()).last().dy(term ? vy : vy + 7);
            x += sw;
        }
        if (pw > 0) l.pill(x + gap, pill, tone, icon).last().dy(t6(27, 29, 27, 35, 33, 26));
        box.put(l, 0, 0, w);
        if (top > 0) d.add(new Doc.Gap(top));
        d.add(box);
        d.add(new Doc.Gap(t6(14, 14, 14, 16, 16, 14) - 1));
        return box;
    }

    static Doc.Line heroRow(Doc.Box box, int i, int w) {
        int bp = heroPad();
        Doc.Line l = new Doc.Line(8);
        box.put(l, bp, heroRowTop() + 16 * i, w - 2 * bp);
        return l.at(0);
    }

    private static String account(UUID id) {
        if (id == null) return "Unknown";
        if (id.getMostSignificantBits() == 0) return "Bedrock";
        return id.version() == 4 ? "Premium" : id.version() == 3 ? "Cracked" : "Unknown";
    }

    private static String shortUuid(UUID id) {
        String u = id.toString();
        return u.startsWith("00000000-0000-0000-") ? "…" + u.substring(u.length() - 8) : u.substring(0, 8) + "…";
    }

    private static int avgPing(List<PlayerInfo> list) {
        if (list.isEmpty()) return 0;
        long sum = 0;
        for (PlayerInfo p : list) sum += p.getLatency();
        return (int) (sum / list.size());
    }

    private static String fmtTps() {
        try { return String.format(Locale.ROOT, "%.2f TPS", TickRate.INSTANCE.getTickRate()); } catch (Throwable t) { return null; }
    }

    private static String connectedTo() {
        try {
            ClientPacketListener c = mc().getConnection();
            if (c == null) return null;
            SocketAddress a = ((ClientListenerConnectionAccessor) (Object) c).hontun$getConnection().getRemoteAddress();
            if (a instanceof InetSocketAddress isa) {
                String host = isa.getAddress() != null ? isa.getAddress().getHostAddress() : isa.getHostString();
                return host + ":" + isa.getPort();
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private record Why(String a, int ca, String b, int cb) {}

    private record Cell(String label, String value, String suffix, String conf, String q, boolean warn,
                        List<Why> why, String reason, String link, ServerInfoScreen.Page page) {}

    private static Doc.Line top(Doc.Line l) {
        for (Doc.Span sp : l.spans) if (sp.dy == Integer.MIN_VALUE) sp.dy = 0;
        return l;
    }

    private static Component sm(String s) {
        return HontunTheme.smog() ? Paint.sm(s) : Paint.c(s);
    }

    private static Doc.Line seg(Doc.Line l, String s, int color, int max) {
        Doc.Span sp = new Doc.Span(l.cur, sm(s), color, Doc.TEXT).max(Math.max(1, max - l.cur));
        l.add(sp);
        return l.sp(sp.width());
    }

    private static Doc.Line linkT(Doc.Line l, String s, Runnable a) {
        l.link(l.cur, s, a);
        return l.sp(l.last().width());
    }

    private static int height(List<Doc.Item> items) {
        int h = 0;
        for (Doc.Item it : items) h += it.h;
        return h;
    }

    static String plural(int n, String one, String many) {
        return n + " " + (n == 1 ? one : many);
    }

    private static List<String> wrap(String s, int max) {
        List<String> out = new ArrayList<>();
        String cur = "";
        for (String word : s.strip().split("\\s+")) {
            String t = cur.isEmpty() ? word : cur + " " + word;
            if (!cur.isEmpty() && Paint.w(Paint.c(t)) > max) {
                out.add(cur);
                cur = word;
            } else {
                cur = t;
            }
        }
        if (!cur.isEmpty()) out.add(cur);
        return out;
    }

    private static Component clipRaw(Component c, int max) {
        Font f = Paint.font();
        if (max <= 0 || f.width(c) <= max) return c;
        FormattedText cut = f.substrByWidth(c, Math.max(0, max - f.width("…")));
        MutableComponent out = Component.empty();
        Style[] last = {Style.EMPTY};
        cut.visit((st, str) -> {
            out.append(Component.literal(str).withStyle(st));
            last[0] = st;
            return Optional.empty();
        }, Style.EMPTY);
        return out.append(Component.literal("…").withStyle(last[0]));
    }

    private static boolean rewrites(ServerSoftware.Result r, String proxy) {
        if (r == null || r.evidence() == null) return false;
        for (ServerSoftware.Evidence e : r.evidence()) {
            String dt = e.detail();
            if ("brand".equals(e.signal()) && dt != null && dt.contains("rewrite") && dt.contains(proxy)) return true;
        }
        return false;
    }

    private static void overview(ServerInfoScreen s, Doc d, int w) {
        int by = t6(0, 8, 12, 0, 0, 0);
        if (by > 0) d.add(new Doc.Gap(by));
        d.add(band(s, w));
        d.add(new Doc.Gap(t6(14, 14, 14, 16, 16, 16) - 1));
        lower(s, d, w);
    }

    private static Doc.Box band(ServerInfoScreen s, int w) {
        ServerSoftware.Result r = softwareResult();
        AnticheatDetect.Verdict v = verdict();
        RealVersion.Report rep = report();
        boolean smog = HontunTheme.smog(), boxed = smog || HontunTheme.modern2() || HontunTheme.modern1();
        int bh = t6(108, 114, 114, 126, 126, 122), in = t6(12, 12, 11, 14, 16, 12);
        int[] mock = BAND_CELLS[t6(0, 1, 2, 3, 4, 5)];
        int pad = boxed ? 0 : Paint.pad(), ref = Paint.iwRef() + 2 * pad, tot = w + 2 * pad;
        int[] b = new int[5];
        for (int i = 0, cum = 0; i < 4; cum += mock[i++]) b[i] = Math.round(cum * tot / (float) ref) - pad;
        b[4] = tot - pad;
        int ly = t6(12, 10, 10, 16, 16, 14), vy = t6(24, 26, 22, 31, 29, 30), qy = t6(48, 50, 46, 57, 55, 46);
        int wy = t6(63, 64, 60, 73, 71, 62), wp = t6(10, 12, 12, 12, 11, 12), ky = bh - t6(21, 22, 23, 26, 26, 24);

        List<Why> sw = new ArrayList<>();
        if (r != null && r.family() != null && r.family() != ServerSoftware.Family.UNKNOWN) {
            sw.add(new Why(cap(r.family().label) + " structure", Paint.dim(), null, 0));
        }
        String cmd = null;
        if (r != null && r.notes() != null) {
            for (ServerSoftware.Note n : r.notes()) {
                Matcher m = n.text() != null ? CMD.matcher(n.text()) : null;
                if (m != null && m.find()) {
                    cmd = m.group(1);
                    break;
                }
            }
        }
        if (cmd != null) sw.add(new Why("Confirmed by ", Paint.dim(), cmd, Paint.sub()));

        String proxy = r != null && r.proxy() != null && !r.proxy().isBlank() ? r.proxy().strip() : null;
        String[] hops = proxy != null ? proxy.split(" \\+ ") : new String[0];
        List<Why> pw = new ArrayList<>();
        if (hops.length > 1) pw.add(new Why("Then " + String.join(", ", List.of(hops).subList(1, hops.length)), Paint.dim(), null, 0));
        else if (hops.length == 1 && rewrites(r, hops[0])) pw.add(new Why("Rewrites the brand", Paint.dim(), null, 0));

        RealVersion.Result rr = rep != null ? rep.result() : null;
        String vv, vs = null, vq;
        boolean warn = false;
        List<Why> vw = new ArrayList<>();
        if (rr != null) {
            String range = or(rr.range(), "unknown").strip();
            if (range.endsWith(" or newer") && range.length() > 9) {
                vv = range.substring(0, range.length() - 9);
                vs = "or newer";
            } else {
                vv = cap(range);
            }
            boolean hidden = rr.text() != null && rr.text().contains("(hidden by ");
            vq = rr.method() != null && !rr.method().isBlank() ? "From the " + rr.method() : hidden ? "Hidden by the translation" : "Not enough signals yet";
            warn = rep.disagree() != null && !rep.disagree().isBlank();
            if (rep.through() != null && !rep.through().isBlank()) {
                vw.add(new Why("Joined through", Paint.dim(), null, 0));
                vw.add(new Why(rep.through(), Paint.dim(), null, 0));
            }
        } else {
            boolean known = VersionKeeper.version != null && !"unknown".equals(VersionKeeper.version);
            vv = known ? VersionKeeper.version : "Unknown";
            vq = known ? "From the core pack" : "Not enough signals yet";
        }

        String an = v == null ? "Unknown" : !v.done() && (v.name() == null || v.name().isBlank()) ? "Checking…" : or(v.name(), "None found");
        boolean named = v != null && v.name() != null && !v.name().isBlank() && !"Unknown".equals(v.name());
        String reason = v != null && !named && v.reason() != null && !v.reason().isBlank() ? v.reason() : null;
        List<Why> aw = new ArrayList<>();
        if (v != null && reason == null) {
            if (v.source() != null && !v.source().isBlank()) aw.add(new Why("Detected via " + v.source(), Paint.dim(), null, 0));
            AnticheatDetect.Verdict al = v.also();
            if (al != null && al.name() != null && !al.name().isBlank()) aw.add(new Why("Also ", Paint.dim(), al.name(), Paint.sub()));
        }

        Cell[] cells = {
            new Cell("Software", r != null ? or(r.software(), "Unknown") : "Unknown", null, r != null ? r.confidence() : null, null, false,
                    sw, null, "Software details", ServerInfoScreen.Page.SOFTWARE),
            new Cell("Proxy", r == null ? "Unknown" : hops.length > 0 ? cap(hops[0]) : "None", null, null,
                    hops.length > 0 ? "At least " + plural(hops.length, "hop", "hops") : "Not detected", false,
                    pw, null, "Connection details", ServerInfoScreen.Page.CONNECTION),
            new Cell("Real version", vv, vs, null, vq, warn, vw, null, "Version details", ServerInfoScreen.Page.VERSION),
            new Cell("Anticheat", an, null, reason == null && v != null ? v.confidence() : null, null, false,
                    aw, reason, "Anticheat details", ServerInfoScreen.Page.ANTICHEAT)
        };

        Doc.Box box = new Doc.Box(bh);
        if (boxed) {
            box.style = Doc.BAND;
        } else {
            box.bleed = true;
            box.ruleBottom = true;
        }
        box.vr = new int[]{b[1], b[2], b[3]};
        box.vrTop = t6(14, 10, 10, 16, 16, 14);
        box.vrH = t6(bh - 28, bh - 20, bh - 22, 94, 94, 94);
        for (int i = 0; i < 4; i++) {
            Cell c = cells[i];
            int tx = b[i] + in, cw = Math.max(20, b[i + 1] - b[i] - 2 * in);
            Doc.Line lab = new Doc.Line(10);
            if (smog) lab.small(0, c.label(), Paint.dim());
            else lab.text(0, c.label(), Paint.dim());
            if (i == 0 && r != null && r.nameOrigin() != null) origin(lab, r.nameOrigin(), cw);
            box.put(top(lab), tx, ly, cw);
            box.put(bandValue(c.value(), c.suffix(), cw), tx, vy, cw);
            if (c.reason() != null) {
                int rp = t6(11, 12, 12, 12, 11, 11), max = Math.max(1, (ky - qy - 1) / rp);
                String full = cap(c.reason());
                List<String> lines = wrap(full, cw);
                for (int j = 0; j < Math.min(max, lines.size()); j++) {
                    boolean end = j == max - 1 && lines.size() > max;
                    Doc.Line rl = new Doc.Line(rp).text(0, end ? String.join(" ", lines.subList(j, lines.size())) : lines.get(j), Paint.dim(), cw);
                    if (end) rl.last().tip(List.of(Paint.c(full)));
                    box.put(top(rl), tx, qy + j * rp, cw);
                }
            } else {
                Doc.Line q = quality(c, cw);
                if (q != null) box.put(q, tx, qy, cw);
            }
            for (int j = 0; j < Math.min(2, c.why().size()); j++) {
                Why y = c.why().get(j);
                Doc.Line wl = new Doc.Line(wp).at(0);
                seg(wl, y.a(), y.ca(), cw);
                if (y.b() != null && wl.cur < cw - 4) seg(wl, y.b(), y.cb(), cw);
                box.put(top(wl), tx, wy + j * wp, cw);
            }
            ServerInfoScreen.Page target = c.page();
            box.put(top(new Doc.Line(10).link(0, c.link(), () -> s.setPage(target))), tx, ky, cw);
        }
        return box;
    }

    private static void origin(Doc.Line l, ServerSoftware.Origin o, int right) {
        boolean cfg = o == ServerSoftware.Origin.CONFIG;
        int ic = cfg ? Paint.CONFIG : Paint.BUILTIN, iw = Paint.iconW(ic), gap = iw > 0 ? t6(3, 3, 3, 3, 4, 6) : 0;
        Component t = sm(cfg ? "config" : "built-in");
        int x = right - Paint.w(t) - gap - iw;
        if (iw > 0) l.icon(x, ic, Paint.dim());
        l.add(new Doc.Span(x + iw + gap, t, Paint.dim(), Doc.TEXT));
    }

    static Doc.Line bandValue(String value, String suffix, int cw) {
        boolean term = Paint.term();
        Component vc = Paint.c(value);
        int sg = term ? 6 : 4, sw = suffix != null ? sg + Paint.w(Paint.c(suffix)) : 0;
        Doc.Line l = new Doc.Line(term ? 10 : 18);
        if (!term && Paint.bigWidth(vc) + sw <= cw) {
            l.big(0, value, Paint.text()).last().dy(0);
            if (suffix != null) l.text(Paint.bigWidth(vc) + sg, suffix, Paint.sub()).last().dy(7);
            return l;
        }
        int ty = term ? 0 : 5;
        l.text(0, value, Paint.text(), Math.max(20, cw - sw)).last().dy(ty);
        if (suffix != null) l.text(l.last().width() + sg, suffix, Paint.sub()).last().dy(ty);
        return l;
    }

    private static Doc.Line quality(Cell c, int cw) {
        boolean hasConf = c.conf() != null && !c.conf().isBlank();
        int lv = level(c.conf());
        String text = hasConf ? lv > 0 ? conf(c.conf()) : cap(c.conf()) : c.q();
        if (text == null || text.isEmpty()) return null;
        Doc.Line q = new Doc.Line(10).at(0);
        if (lv > 0) q.m(lv).sp(Paint.meterGap());
        int wi = c.warn() ? Paint.iconW(Paint.WARN) : 0;
        q.t(text, Paint.sub(), Math.max(20, cw - q.cur - (wi > 0 ? wi + 4 : 0)));
        if (wi > 0) q.sp(4).i(Paint.WARN, Paint.yellow());
        return top(q);
    }

    private static void lower(ServerInfoScreen s, Doc d, int w) {
        boolean smog = HontunTheme.smog(), term = Paint.term();
        int col = sx(t6(290, 352, 320, 384, 392, 542) - Paint.pad(), w);
        int lw = col - t6(16, 16, 16, 16, 16, 12), rx = col + t6(14, 14, 14, 14, 16, 12);
        d.colRule = col;
        d.lw = lw;
        d.rx = rx;
        List<Doc.Item> left = d.left;

        PluginScanner ps = PluginScanner.INSTANCE;
        Doc.Line ph = head("Plugins");
        left.add(ph);
        int end;
        if (ps.running() || !ps.scanned()) {
            left.add(new Doc.Gap(t6(13, 11, 11, 11, 11, 9)));
            Doc.Line a = new Doc.Line(t6(16, 16, 16, 20, 20, 16));
            if (ps.running()) {
                a.text(0, "Scanning…", Paint.dim());
            } else {
                String scan = "Scan plugins";
                a.link(0, scan, () -> ps.run(true, true, false, true));
                a.text(Paint.w(Paint.c(scan)) + 8, "Not scanned yet", Paint.dim());
            }
            left.add(a);
            end = height(left);
        } else {
            List<String> names = new ArrayList<>(ps.results());
            names.sort(String.CASE_INSENSITIVE_ORDER);
            int flagged = 0, ac = 0;
            for (String p : names) {
                String cat = category(p);
                if (!cat.equals(CATEGORIES[3])) flagged++;
                if (cat.equals(CATEGORIES[0]) || cat.equals(CATEGORIES[1])) ac++;
            }
            String summary = !smog ? names.size() + " found, " + flagged + " anticheat, anti-exploit or hider"
                    : names.isEmpty() ? "None found" : names.size() + " found" + (ac > 0 ? ", " + ac + " anticheat or exploit" : "");
            String details = "Scan details";
            int dw = Paint.w(Paint.c(details));
            sum(ph, summary, Paint.dim(), lw - dw - 8 - ph.cur);
            ph.link(lw - dw, details, () -> s.setPage(ServerInfoScreen.Page.PLUGINS));
            end = names.isEmpty() ? height(left) : grid(left, names, lw, w) - t6(2, 2, 3, 2, 3, 3);
        }
        int hy = end + t6(14, 12, 12, 14, 13, 14), py = hy + t6(14, 12, 12, 16, 15, 14), now = height(left);
        left.add(new Doc.Sep(py - 1 - now, hy - now, smog ? Doc.FADE : Doc.RULE));

        List<PlayerInfo> tab = tab();
        Doc.Line th = head("Players");
        String all = "All players";
        int alw = Paint.w(Paint.c(all));
        sum(th, tab.isEmpty() ? "Nobody in the tab list" : tab.size() + " in the tab list", Paint.dim(), lw - alw - 8 - th.cur);
        th.link(lw - alw, all, () -> s.setPage(ServerInfoScreen.Page.PLAYERS));
        left.add(th);
        if (!tab.isEmpty()) {
            left.add(new Doc.Gap(t6(13, 11, 11, 12, 12, 13)));
            int pitch = t6(14, 14, 14, 14, 16, 16), lastH = t6(9, 10, 9, 10, 9, 9);
            int px = sx(t6(170, 142, 140, 230, 204, 236), w), nameMax = px - t6(6, 8, 6, 6, 6, 12) - 9;
            int n = Math.min(5, tab.size());
            boolean more = tab.size() > n;
            for (int i = 0; i < n; i++) {
                PlayerInfo p = tab.get(i);
                Doc.Line row = new Doc.Line(i == n - 1 && !more ? lastH : pitch).face(0, p.getSkin());
                Component full = name(p), shown = clipRaw(full, nameMax);
                Doc.Span ns = new Doc.Span(9, shown, Paint.text(), Doc.RAW);
                if (shown != full) ns.tip(List.of(full));
                row.add(ns);
                row.pill(px, account(p.getProfile().id()), Paint.sub());
                String gm = gamemode(p);
                if (!gm.isEmpty()) row.text(px + row.last().width() + t6(6, 6, 6, 6, 6, 12), gm, Paint.sub());
                left.add(top(row));
            }
            if (more) left.add(top(new Doc.Line(lastH).link(9, (tab.size() - n) + " more", () -> s.setPage(ServerInfoScreen.Page.PLAYERS))));
        }

        int kx = t6(56, 56, 52, 56, 56, 60), rp = term ? 14 : 12, vm = Math.max(20, w - rx - kx);
        List<Doc.Line> you = new ArrayList<>();
        Doc.Line yl = grow(you, "You", rp, kx);
        Matcher pm = PERM.matcher(ServerInfoScreen.perms == null ? "" : ServerInfoScreen.perms.strip());
        if (pm.matches()) {
            int lv = -1;
            try { lv = Integer.parseInt(pm.group(1)); } catch (NumberFormatException ignored) {}
            yl.t("Permission level " + pm.group(1), Paint.sub()).t(" (", Paint.sub());
            int close = Paint.w(Paint.c(")"));
            yl.t(pm.group(2), lv >= 2 ? Paint.acc() : Paint.sub(), Math.max(20, kx + vm - yl.cur - close)).t(")", Paint.sub());
        } else {
            yl.t("Unknown", Paint.sub());
        }

        List<Doc.Line> world = new ArrayList<>();
        var lvl = mc().level;
        String dim = or(WorldInfo.dimension, lvl != null ? lvl.dimension().identifier().toString() : null);
        if (dim != null) grow(world, "World", rp, kx).t(dim, Paint.sub(), vm);
        if (lvl != null) {
            grow(world, "World", rp, kx).t(lvl.getDifficulty().getDisplayName().getString() + ", day " + lvl.getGameTime() / 24000L, Paint.sub(), vm);
        }
        String tps = fmtTps();
        if (tps != null) grow(world, "World", rp, kx).t(tps, Paint.sub(), vm);

        List<Doc.Line> net = new ArrayList<>();
        String con = connectedTo();
        if (con != null) {
            Doc.Line cl = grow(net, "Network", rp, kx).t("Connected to ", Paint.dim());
            cl.t(con, Paint.sub(), Math.max(20, kx + vm - cl.cur));
        }
        Doc.Line nl = grow(net, "Network", rp, kx);
        int ch = channelNames().size(), ck = CookieObserver.list().size();
        if (ch > 0) linkT(nl, plural(ch, "channel", "channels"), () -> s.setPage(ServerInfoScreen.Page.CHANNELS));
        else nl.t("No plugin channels", Paint.sub());
        nl.t(", ", Paint.sub());
        if (ck > 0) linkT(nl, plural(ck, "cookie", "cookies"), () -> s.setPage(ServerInfoScreen.Page.COOKIES));
        else nl.t("no cookies", Paint.sub());
        grow(net, "Network", rp, kx).t(!ResourcePackInfo.present ? "No resource pack"
                : ResourcePackInfo.required ? "Required resource pack" : "Optional resource pack", Paint.sub(), vm);

        d.right.add(new Doc.Gap(smog ? 2 : 1));
        boolean first = true;
        for (List<Doc.Line> g : List.of(you, world, net)) {
            if (g.isEmpty()) continue;
            if (!first) d.right.add(new Doc.Sep(t6(19, 18, 15, 20, 19, 17), t6(9, 8, 4, 8, 7, 8), smog ? Doc.FADE : Doc.RULE));
            first = false;
            for (int j = 0; j < g.size(); j++) {
                Doc.Line l = g.get(j);
                if (term && j == g.size() - 1) l.h = 9;
                d.right.add(top(l));
            }
        }
    }

    private static Doc.Line grow(List<Doc.Line> rows, String label, int h, int kx) {
        Doc.Line l = new Doc.Line(h);
        if (rows.isEmpty()) l.text(0, label, Paint.dim());
        rows.add(l);
        return l.at(kx);
    }

    private static int grid(List<Doc.Item> into, List<String> names, int lw, int w) {
        int rp = Paint.term() ? 14 : 12;
        into.add(new Doc.Gap(t6(13, 11, 11, 11, 11, 13)));
        int cw = t6(sx(92, w), (lw + 8) / 3, sx(98, w), lw / 3, sx(120, w), sx(130, w));
        int shown = Math.min(names.size(), 21), rows = (shown + 2) / 3, iw = Paint.iconW(Paint.ANTICHEAT);
        for (int r = 0; r < rows; r++) {
            Doc.Line l = new Doc.Line(rp);
            for (int c = 0; c < 3; c++) {
                int i = c * rows + r;
                if (i >= shown) continue;
                String n = names.get(i), cat = category(n);
                boolean flag = !cat.equals(CATEGORIES[3]);
                l.at(c * cw).t(n, flag ? Paint.acc() : Paint.sub(), Math.max(20, cw - 6 - (flag && iw > 0 ? iw + 4 : 0)));
                if (!flag) continue;
                l.last().tip(List.of(Paint.c(n), Paint.c(cat)));
                if (iw > 0) l.sp(4).i(Paint.ANTICHEAT, Paint.acc());
            }
            into.add(top(l));
        }
        if (names.size() > shown) into.add(top(new Doc.Line(rp).text(0, (names.size() - shown) + " more on the Plugins page", Paint.dim())));
        return height(into);
    }

    private static Component name(PlayerInfo p) {
        Component dn = p.getTabListDisplayName();
        return dn != null ? dn : Component.literal(p.getProfile().name());
    }

    private static String gamemode(PlayerInfo p) {
        try {
            String n = p.getGameMode().getName();
            return n.isEmpty() ? "" : Character.toUpperCase(n.charAt(0)) + n.substring(1);
        } catch (Throwable t) {
            return "";
        }
    }

    private static void software(ServerInfoScreen s, Doc d, int w) {
        ServerSoftware.Result r = softwareResult();
        if (r == null) {
            hero(d, w, heroBase(), "Software", "Unknown", null, null, Paint.NORMAL, -1, null, "Nothing detected yet");
            return;
        }
        boolean cfg = r.nameOrigin() == ServerSoftware.Origin.CONFIG;
        String origin = r.nameOrigin() == null ? null : cfg ? "config" : "built-in";
        boolean m1 = HontunTheme.modern1(), m2 = HontunTheme.modern2();

        List<ServerSoftware.Evidence> ev = r.evidence() == null ? List.of() : r.evidence();
        List<Ev> rows = new ArrayList<>();
        int[] n = new int[4];
        int nb = 0, nc = 0;
        Map<String, Integer> pro = new LinkedHashMap<>(), con = new LinkedHashMap<>();
        for (int i = 0; i < ev.size(); i++) {
            ServerSoftware.Evidence e = ev.get(i);
            if (e == null) continue;
            int c = evClass(r, e);
            rows.add(new Ev(e, c, i));
            n[0]++;
            n[c]++;
            if (e.origin() == ServerSoftware.Origin.CONFIG) nc++;
            else nb++;
            if (e.weight() > 0 && c != 3) (c == 1 ? pro : con).merge(famLabel(e), e.weight(), Integer::sum);
        }
        List<Seg> segs = new ArrayList<>();
        segs(segs, pro, true);
        segs(segs, con, false);

        int bp = heroPad(), br = w - bp - (m1 ? 2 : 0);
        int legTop = t6(74, 76, 76, 82, 84, 70), agTop = m2 ? 90 : legTop;
        int hh = t6(96, 110, 100, 108, 110, 90);
        Doc.Line lf = pro.isEmpty() ? null : legend(segs, true, total(pro));
        Doc.Line la = con.isEmpty() ? null : legend(segs, false, total(con));
        if (!m2 && lf != null && la != null && lf.cur + la.cur + 16 > br - bp) {
            agTop = legTop + 12;
            hh += 12;
        }
        if (segs.isEmpty()) hh = t6(68, 70, 72, 82, 82, 62);
        Doc.Box hb = hero(d, w, hh, "Software", or(r.software(), "Unknown"), null,
                origin, Paint.NORMAL, cfg ? Paint.CONFIG : Paint.BUILTIN, r.confidence(), null);
        boolean fam = r.family() != null && r.family() != ServerSoftware.Family.UNKNOWN;
        Doc.Line sr = heroRow(hb, 0, w).t("Structure", Paint.dim()).sp(4).t(fam ? r.family().label : "Unknown", Paint.sub());
        if (fam) sr.sp(4).t("(what the packet methods think)", Paint.dim());
        sr.sp(10).t("Brand", Paint.dim()).sp(4);
        sr.t(or(r.brand(), "Unknown"), Paint.sub(), Math.max(20, w - 2 * heroPad() - sr.cur));
        if (!segs.isEmpty()) {
            hb.put(bar(segs, bp, br, t6(5, 5, 4, 5, 5, 4)), 0, t6(63, 65, 64, 72, 71, 58), w);
            if (lf != null) hb.put(lf, bp, legTop, lf.cur);
            if (la != null) hb.put(la, w - bp - la.cur, agTop, la.cur);
        }

        if (!d.full.isEmpty() && d.full.getLast() instanceof Doc.Gap) d.full.removeLast();
        d.add(new Doc.Gap(t6(12, 12, 10, 14, 12, 12)));
        filters(s, d, w, n, nb, nc);
        evidence(s, d, w, rows);
        notes(d, w, r.notes());
    }

    private static final String BUILT_IN_WHY = "Built into the server software: no config option, gamerule or server.properties value "
            + "changes it (plugins can). Settings that remove the input on every server, such as no rain or a disabled server list, only hide it.";

    private record Ev(ServerSoftware.Evidence e, int cls, int idx) {}

    private record Seg(boolean pro, String family, int weight, int color) {}

    private static boolean va() { return t6(0, 0, 0, 0, 1, 0) == 1; }

    private static int evClass(ServerSoftware.Result r, ServerSoftware.Evidence e) {
        boolean paperOnBukkit = r.family() == ServerSoftware.Family.PAPER && e.family() == ServerSoftware.Family.BUKKIT && !e.againstPaper();
        boolean supports = e.weight() > 0 && (e.family() == r.family() || paperOnBukkit);
        boolean against = (e.weight() > 0 && !supports) || (e.againstPaper() && r.family() == ServerSoftware.Family.PAPER);
        return supports ? 1 : against ? 2 : 3;
    }

    private static String famLabel(ServerSoftware.Evidence e) {
        return e.family() != null ? e.family().label : "Unknown";
    }

    private static int total(Map<String, Integer> m) {
        int t = 0;
        for (int v : m.values()) t += v;
        return t;
    }

    private static void segs(List<Seg> out, Map<String, Integer> sums, boolean pro) {
        List<Map.Entry<String, Integer>> es = new ArrayList<>(sums.entrySet());
        es.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        for (int i = 0; i < es.size(); i++) {
            int c = pro ? i == 0 ? Paint.green() : Paint.forDim() : i == 0 ? Paint.red() : Paint.agDim();
            out.add(new Seg(pro, es.get(i).getKey(), es.get(i).getValue(), c));
        }
    }

    private static Doc.Line legend(List<Seg> segs, boolean pro, int total) {
        int hg = t6(3, 3, 3, 4, 4, 6), ig = t6(4, 4, 4, 4, 4, 6), sg = t6(10, 10, 10, 10, 10, 12);
        int ss = Paint.term() ? 5 : 6, sy = t6(2, 2, 1, 2, 1, 2);
        Doc.Line l = new Doc.Line(10).at(0);
        l.t(pro ? "For" : "Against", Paint.dim()).sp(hg).t(String.valueOf(total), pro ? Paint.green() : Paint.red());
        for (Seg s : segs) {
            if (s.pro() != pro) continue;
            l.sp(sg);
            Doc.Span sw = new Doc.Span(l.cur, Component.empty(), s.color(), Doc.SWATCH);
            sw.sw = ss;
            sw.sh = ss;
            l.add(sw.dy(sy)).sp(ss);
            l.sp(ig).t(s.family(), Paint.sub()).sp(ig).t("+" + s.weight(), Paint.sub());
        }
        return top(l);
    }

    private static Doc.Bar bar(List<Seg> segs, int left, int right, int h) {
        Doc.Bar b = new Doc.Bar(h);
        int units = 0;
        for (Seg s : segs) units += s.weight();
        if (units <= 0) return b;
        boolean term = Paint.term();
        int inner = term ? 1 : 2, outer = term ? 3 : 8, gaps = 0;
        for (int i = 1; i < segs.size(); i++) gaps += segs.get(i).pro() != segs.get(i - 1).pro() ? outer : inner;
        int span = term ? (right - left + 1) / 6 : right - left, room = Math.max(segs.size(), span - gaps), at = 0;
        for (int i = 0; i < segs.size(); i++) {
            Seg s = segs.get(i);
            if (i > 0) at += s.pro() != segs.get(i - 1).pro() ? outer : inner;
            int k = i == segs.size() - 1 ? span - at : Math.round(s.weight() * room / (float) units);
            if (term) k = i == segs.size() - 1 ? k : Math.max(1, k);
            if (k > 0) b.segs.add(term ? new int[]{left + at * 6, k * 6 - 1, s.color()} : new int[]{left + at, k, s.color()});
            at += Math.max(0, k);
        }
        return b;
    }

    private static void filters(ServerInfoScreen s, Doc d, int w, int[] n, int nb, int nc) {
        boolean va = va();
        int th = t6(16, 18, 16, 16, 24, 13);
        Doc.Line tl = new Doc.Line(th);
        String[] names = {"All", "For", "Against", "Info"};
        int[] marks = {-1, Paint.PLUS, Paint.MINUS, Paint.DOT};
        int[] colors = {0, Paint.green(), Paint.red(), Paint.dim()};
        int vw = 0;
        if (va) {
            vw = 60;
            for (int i = 0; i < 4; i++) vw = Math.max(vw, Paint.tabContentW(Paint.c(names[i]), marks[i], Paint.c(String.valueOf(n[i]))) + 16);
        }
        int pad = t6(2, 3, 0, 0, 0, 0), gap = t6(0, 2, 12, 2, 0, 6), x = pad;
        for (int i = 0; i < 4; i++) {
            Doc.Span sp = new Doc.Span(x, Paint.c(names[i]), colors[i], Doc.TAB);
            sp.icon = marks[i];
            sp.count = Paint.c(String.valueOf(n[i]));
            sp.sel = s.evTab == i;
            sp.sw = vw;
            int k = i;
            sp.on(() -> {
                s.evTab = k;
                s.rebuild();
            });
            tl.add(sp);
            x += sp.width() + (i < 3 ? gap : 0);
        }
        if (HontunTheme.smog() || HontunTheme.modern2() || va) tl.groupW = x + pad;

        Component lb = Paint.c("Built-in"), lc = Paint.c("Config"), cb = Paint.c(String.valueOf(nb)), cc = Paint.c(String.valueOf(nc));
        int w0 = Paint.toggleW(lb, Paint.BUILTIN, cb), w1 = Paint.toggleW(lc, Paint.CONFIG, cc), tg = t6(6, 6, 6, 4, 12, 18);
        int tx = w - w0 - tg - w1;
        Doc.Line target = tx < x + 8 ? new Doc.Line(th) : tl;
        if (target != tl) tx = Math.max(0, tx);
        target.add(toggleSpan(tx, lb, Paint.BUILTIN, cb, s.evBuiltin, () -> {
            s.evBuiltin = !s.evBuiltin;
            s.rebuild();
        }));
        target.add(toggleSpan(tx + w0 + tg, lc, Paint.CONFIG, cc, s.evConfig, () -> {
            s.evConfig = !s.evConfig;
            s.rebuild();
        }));
        d.add(tl);
        if (target != tl) {
            d.add(new Doc.Gap(4));
            d.add(target);
        }
        int rest = t6(24, 26, 22, 26, 24, 21) - th;
        if (rest > 0) d.add(new Doc.Gap(rest));
    }

    private static Doc.Span toggleSpan(int x, Component label, int icon, Component count, boolean on, Runnable act) {
        Doc.Span sp = new Doc.Span(x, label, Paint.acc(), Doc.TOGGLE);
        sp.icon = icon;
        sp.count = count;
        sp.sel = on;
        return sp.on(act);
    }

    private static void evidence(ServerInfoScreen s, Doc d, int w, List<Ev> rows) {
        int theme = t6(0, 1, 2, 3, 4, 5);
        boolean va = theme == 4, rules = rowRules();
        Doc.Well pw = new Doc.Well();
        pw.style = Doc.PANEL;
        if (va) {
            pw.openTop = true;
            pw.inner.add(new Doc.Gap(1));
        }
        List<Ev> shown = new ArrayList<>();
        for (Ev v : rows) {
            boolean co = v.e().origin() == ServerSoftware.Origin.CONFIG;
            if ((s.evTab == 0 || v.cls() == s.evTab) && (co ? s.evConfig : s.evBuiltin)) shown.add(v);
        }
        shown.sort(Comparator.comparingInt((Ev v) -> -v.e().weight())
                .thenComparingInt(v -> v.cls() == 2 ? 0 : v.cls() == 1 ? 1 : 2)
                .thenComparingInt(Ev::idx));

        int rh = t6(16, 14, 16, 16, 14, 14);
        int mx = t6(10, 9, 8, 9, 10, 8), px = t6(24, 22, 20, 22, 22, 20), dx = t6(86, 96, 86, 96, 92, 104);
        int wr = t6(30, 34, 38, 32, 34, 36), ox = w - t6(22, 26, 32, 22, 26, 22);
        int wc = t6(Paint.sub(), Paint.sub(), Paint.text(), Paint.text(), Paint.text(), Paint.text());
        int fmax = sx(150, w), fixX = sx(t6(0, 0, 432, 0, 0, 556), w), fixW = sx(t6(0, 0, 78, 0, 0, 150), w);
        int hfw = 0, hww = 0;
        for (Ev v : shown) {
            if (v.e().weight() <= 0) continue;
            hfw = Math.max(hfw, Math.min(fmax, Paint.w(famText(v.e()))));
            hww = Math.max(hww, Paint.w(Paint.c("+" + v.e().weight())));
        }
        int hfx = w - wr - hww - 12 - hfw;

        int i = 0;
        for (Ev v : shown) {
            ServerSoftware.Evidence e = v.e();
            boolean co = e.origin() == ServerSoftware.Origin.CONFIG;
            Doc.Line l = new Doc.Line(rh);
            l.icon(mx, v.cls() == 1 ? Paint.PLUS : v.cls() == 2 ? Paint.MINUS : Paint.DOT,
                    v.cls() == 1 ? Paint.green() : v.cls() == 2 ? Paint.red() : Paint.dim());
            l.pill(px, or(e.signal(), "?"), Paint.NORMAL, -1);
            int dmax;
            if (e.weight() > 0) {
                Component wt = Paint.c("+" + e.weight()), fc = famText(e);
                int wx = w - wr - Paint.w(wt), fw = Math.min(fmax, Paint.w(fc)), fx = fixX, fm = fixW;
                if (theme == 0) fx = w - 50 - fw;
                else if (theme == 1) fx = w - 54 - fw;
                else if (theme == 4) fx = wx - 6 - fw;
                else if (theme == 3) fx = hfx;
                if (theme == 0 || theme == 1 || theme == 4) fm = fmax;
                else if (theme == 3) fm = hfw;
                dmax = fx - (theme == 3 || theme == 5 ? 12 : 8) - dx;
                l.add(new Doc.Span(fx, fc, Paint.dim(), Doc.TEXT).max(Math.max(20, fm)));
                l.add(new Doc.Span(wx, wt, wc, Doc.TEXT));
            } else {
                dmax = switch (theme) {
                    case 0 -> w - 30 - dx;
                    case 2 -> fixX - 8 - dx;
                    case 3 -> ox - 12 - dx;
                    case 5 -> fixX - 12 - dx;
                    default -> ox - 8 - dx;
                };
            }
            l.text(dx, or(e.detail(), ""), Paint.sub(), Math.max(20, dmax));
            l.icon(ox, co ? Paint.CONFIG : Paint.BUILTIN, Paint.dim());
            Doc.Tip tip = evTip(e, v.cls());
            l.tip = tip;
            for (Doc.Span sp : l.spans) if (sp.clips()) sp.tip(tip);
            l.rowBg = true;
            if (rules && i > 0) {
                l.ruleTop = true;
                l.ruleX = 8;
                l.ruleW = w - t6(20, 22, 26, 0, 0, 0);
            }
            pw.inner.add(l);
            i++;
        }
        if (shown.isEmpty()) {
            pw.inner.add(new Doc.Line(rh).text(px, rows.isEmpty() ? "No signals yet" : "No signals match these filters", Paint.dim()));
        }
        if (va) pw.inner.add(new Doc.Gap(1));
        d.add(pw);
    }

    private static Component famText(ServerSoftware.Evidence e) {
        return HontunTheme.smog() ? Paint.sm(famLabel(e)) : Paint.c(famLabel(e));
    }

    private static Doc.Tip evTip(ServerSoftware.Evidence e, int cls) {
        boolean co = e.origin() == ServerSoftware.Origin.CONFIG;
        Doc.Tip t = new Doc.Tip(t6(280, 270, 0, 0, 0, 330));
        t.line().at(0).p(or(e.signal(), "?"), Paint.NORMAL, -1).sp(5)
                .i(co ? Paint.CONFIG : Paint.BUILTIN, Paint.dim()).sp(5).t(co ? "config" : "built-in", Paint.dim());
        String det = or(e.detail(), "");
        if (!det.isEmpty()) t.para(Paint.c(det), Paint.sub());
        if (e.weight() > 0) {
            t.line().at(0).t("+" + e.weight(), cls == 1 ? Paint.green() : Paint.red()).sp(5).t(famLabel(e), Paint.sub()).sp(5)
                    .t(cls == 1 ? "supports the verdict" : "speaks against the verdict", Paint.dim());
        }
        String why = co ? "Depends on server config: " + or(e.config(), "an option can turn this off.") : BUILT_IN_WHY;
        t.para(Paint.c(why), Paint.dim());
        return t;
    }

    private static void notes(Doc d, int w, List<ServerSoftware.Note> all) {
        List<ServerSoftware.Note> notes = new ArrayList<>();
        if (all != null) for (ServerSoftware.Note nt : all) if (nt != null && nt.text() != null && !nt.text().isBlank()) notes.add(nt);
        if (notes.isEmpty()) return;
        notes.sort(Comparator.comparingInt((ServerSoftware.Note nt) -> nt.origin() != null ? 0 : 1));
        boolean term = Paint.term();
        int pitch = t6(12, 14, 12, 14, 14, 12), cont = t6(10, 12, 10, 10, 10, 10);
        d.add(new Doc.Gap(t6(8, 10, 10, 10, 10, 12) - 1));
        for (ServerSoftware.Note nt : notes) {
            boolean og = nt.origin() != null, nco = nt.origin() == ServerSoftware.Origin.CONFIG;
            int tc = term && !og ? Paint.dim() : Paint.sub();
            Doc.Line l = new Doc.Line(pitch).at(0);
            if (term && !og) l.t("#", Paint.dim());
            else l.i(og ? Paint.CHECK : Paint.INFO, og ? Paint.green() : Paint.dim());
            l.sp(6);
            int in = l.cur, avail = Math.max(40, w - in);
            String pl = og ? nco ? "config" : "built-in" : null;
            int pi = nco ? Paint.CONFIG : Paint.BUILTIN;
            int pw = pl != null ? 6 + Paint.pillWidth(Paint.c(pl), Paint.NORMAL, pi) : 0;
            String text = nt.text().strip();
            List<String> parts = Paint.w(Paint.c(text)) + pw <= avail ? List.of(text) : wrap(text, avail);
            Doc.Line cur = l;
            for (int j = 0; j < parts.size(); j++) {
                if (j > 0) {
                    d.add(cur);
                    cur = new Doc.Line(cont).at(in);
                }
                cur.t(parts.get(j), tc, avail);
            }
            if (pl != null) {
                if (cur.cur + pw > w) {
                    d.add(cur);
                    cur = new Doc.Line(cont).at(in).p(pl, Paint.NORMAL, pi);
                } else {
                    cur.sp(6).p(pl, Paint.NORMAL, pi);
                }
            }
            d.add(cur);
        }
    }

    private static void anticheat(Doc d, int w) {
        AnticheatDetect.Verdict v = verdict();
        if (v == null) {
            hero(d, w, heroBase(), "Anticheat", "Unknown", null, null, Paint.NORMAL, -1, null, "Nothing collected yet");
            return;
        }
        String name = !v.done() && (v.name() == null || v.name().isBlank()) ? "Checking…" : or(v.name(), "None found");
        AnticheatDetect.Verdict also = v.also() != null && v.also().name() != null && !v.also().name().isBlank() ? v.also() : null;
        boolean named = v.name() != null && !v.name().isBlank() && !v.name().equals("Unknown");
        String reason = also == null && !named && v.reason() != null && !v.reason().isBlank() ? v.reason() : null;
        boolean extra = also != null || reason != null;
        Doc.Box hb = hero(d, w, extra ? t6(68, 69, 72, 82, 82, 62) : heroBase(), "Anticheat", name, null,
                v.source() != null && !v.source().isBlank() ? "from " + v.source() : null, Paint.NORMAL, -1, v.confidence(), null);
        if (also != null) {
            Doc.Line ar = heroRow(hb, 0, w).t("Also", Paint.dim()).sp(5).t(also.name(), Paint.text());
            int alv = level(also.confidence());
            boolean ac = also.confidence() != null && !also.confidence().isBlank();
            if (alv > 0 || ac) ar.sp(5);
            if (alv > 0) {
                ar.m(alv);
                if (ac) ar.sp(Paint.meterGap());
            }
            if (ac) ar.t(cap(also.confidence()), Paint.sub());
            if (also.source() != null && !also.source().isBlank()) ar.sp(5).t("from " + also.source(), Paint.dim());
        } else if (reason != null) {
            heroRow(hb, 0, w).t(cap(reason), Paint.sub(), w - 2 * heroPad());
        }
        d.add(head("How it was found"));
        d.add(new Doc.Gap(t6(22, 18, 20, 18, 18, 16) - 9));
        int kw = kw(ServerInfoScreen.Page.ANTICHEAT), vw = t6(76, 56, 56, 70, 64, 96), gap = kvGap();
        PluginScanner ps = PluginScanner.INSTANCE;
        List<Doc.Line> rows = new ArrayList<>();
        Doc.Line pl = next(rows, "Plugins", kw);
        if (v.plugins() == null) {
            if (ps.running()) {
                val(pl, "Scanning…", Paint.dim(), vw, null, w);
            } else {
                val(pl, "Not scanned yet", Paint.sub(), vw, null, w).sp(gap);
                linkT(pl, "Scan plugins", () -> ps.run(true, true, false, true));
            }
        } else {
            found(pl, v.plugins(), plural(ps.results().size(), "scanned plugin", "scanned plugins"), vw, w);
        }
        found(next(rows, "Commands", kw), v.commands(), "the command tree", vw, w);
        int chn = channelNames().size();
        found(next(rows, "Channels", kw), v.channels(), chn > 0 ? plural(chn, "plugin channel", "plugin channels") : "no plugin channels", vw, w);
        List<Integer> ids = v.pings() == null ? List.of() : v.pings();
        val(next(rows, "Pings", kw), ids.isEmpty() ? "none" : String.valueOf(ids.size()), Paint.sub(), vw, null, w);
        for (Doc.Line r : rows) d.add(r);
        int acSkipped = AnticheatDetect.COLLECTOR.skippedChannels();
        if (acSkipped > 0) note(d, acSkipped + " more anticheat channels were not counted (limit 256), their names are in " + Oversize.where(AnticheatDetect.OVERSIZE_CHANNELS));
        String guess = v.guess() != null && !v.guess().isBlank() ? v.guess() : null;
        String check = v.check() != null && !v.check().isBlank() ? v.check() : null;
        if (!ids.isEmpty()) {
            int sure = ids.size();
            Matcher m = check != null ? SURE.matcher(check) : null;
            if (m != null && m.find()) {
                try { sure = Integer.parseInt(m.group(1)) - 1; } catch (NumberFormatException ignored) {}
            }
            Doc.PingChain chain = new Doc.PingChain(ids, sure);
            sure = chain.sure;
            d.add(chain);
            Doc.Line fl = kvRow("Fingerprint", kw, false);
            int at = sure < ids.size() ? chain.cx(sure, w) : kw + vw + gap;
            int cw = check != null ? Paint.iconW(Paint.CHECK) + gap + Paint.w(Paint.c(check)) + 10 : 0;
            fl.t(guess != null ? guess : "none", guess != null ? Paint.text() : Paint.sub(), Math.max(20, w - kw - Math.min(w / 2, cw)));
            if (check != null) {
                fl.at(Math.max(at, fl.cur + 10)).i(Paint.CHECK, Paint.acc());
                if (Paint.iconW(Paint.CHECK) > 0) fl.sp(gap);
                fl.t(check, Paint.sub(), Math.max(20, w - fl.cur));
            }
            d.add(fl);
        } else {
            Doc.Line fl = kvRow("Fingerprint", kw, true);
            d.add(val(fl, guess != null ? guess : "none", Paint.sub(), vw, check, w));
        }
        int tp = v.teleports();
        d.add(val(kvRow("Setbacks", kw, true), or(v.setbacks(), "none"), Paint.sub(), vw,
                tp > 0 ? plural(tp, "normal teleport", "normal teleports") + " ignored" : null, w));
        if (v.conflict() != null && !v.conflict().isBlank()) d.add(kvRow("Conflict", kw, true).t(v.conflict(), Paint.sub(), w - kw));
        note(d, "Pings are the IDs of the server's first ping packets after you join. Each anticheat numbers them in its own pattern.");
    }

    private static Doc.Line val(Doc.Line l, String value, int color, int vw, String scope, int w) {
        int x0 = l.cur, g = kvGap(), sw = scope != null ? Paint.w(Paint.c(scope)) : 0;
        l.t(value, color, Math.max(20, w - x0 - (sw > 0 ? g + Math.min(sw, (w - x0) / 2) : 0)));
        l.at(Math.max(l.cur, x0 + vw));
        if (scope != null) {
            l.sp(g);
            l.t(scope, Paint.dim(), Math.max(20, w - l.cur));
        }
        return l;
    }

    private static Doc.Line found(Doc.Line l, List<String> list, String scope, int vw, int w) {
        boolean any = list != null && !list.isEmpty();
        return val(l, any ? String.join(", ", list) : "none", any ? Paint.text() : Paint.sub(), vw, scope, w);
    }

    private static void version(Doc d, int w) {
        RealVersion.Report rep = report();
        if (rep == null || rep.result() == null) {
            boolean none = VersionKeeper.version == null || "unknown".equals(VersionKeeper.version);
            hero(d, w, heroBase(), "Real version", none ? "Unknown" : VersionKeeper.version, null, null, Paint.NORMAL, -1, null,
                    none ? "Not enough evidence yet" : "From the core pack");
            return;
        }
        RealVersion.Result r = rep.result();
        String value = or(r.range(), "Unknown"), suffix = null;
        if (value.endsWith(" or newer") && value.length() > 9) {
            value = value.substring(0, value.length() - 9);
            suffix = "or newer";
        }
        boolean hasMethod = r.method() != null && !r.method().isBlank();
        boolean dis = rep.disagree() != null && !rep.disagree().isBlank();
        List<RealVersion.Stage> stages = new ArrayList<>();
        if (rep.stages() != null) for (RealVersion.Stage st : rep.stages()) if (st != null && st.range() != null && !st.range().isBlank()) stages.add(st);
        int kw = Paint.w(Paint.c("Translated"));
        if (dis) kw = Math.max(kw, Paint.w(Paint.c("Disagreement")));
        if (!stages.isEmpty()) kw = Math.max(kw, Paint.w(Paint.c("Range")));
        kw += 12;
        int iw = w - 2 * heroPad();
        int[] chainRow = new int[stages.size()];
        int chainRows = stages.isEmpty() ? 0 : 1, run = 0;
        for (int i = 0; i < stages.size(); i++) {
            RealVersion.Stage st = stages.get(i);
            int cw = Paint.chipW(Paint.sm(or(st.step(), "")), Paint.c(st.range()), run > 0);
            if (run > 0 && kw + run + cw > iw) {
                chainRows++;
                run = 0;
                cw = Paint.chipW(Paint.sm(or(st.step(), "")), Paint.c(st.range()), false);
            }
            chainRow[i] = chainRows - 1;
            run += cw;
        }
        int rows = 1 + (dis ? 1 : 0) + chainRows;
        int hh = t6(68 + 16 * (rows - 1), 70 + 16 * (rows - 1), 48 + 16 * rows + 8, 82 + 16 * (rows - 1), 82 + 16 * (rows - 1), 62 + 16 * (rows - 1));
        Doc.Box hb = hero(d, w, hh, "Real version", value, suffix, hasMethod ? r.method() : "no method",
                hasMethod ? Paint.NORMAL : Paint.DIM, -1, rep.confidence(), rep.missing());
        String through = r.translated() ? rep.through() != null && !rep.through().isBlank() ? "Yes, through " + rep.through() : "Yes"
                : "No, the client speaks it natively";
        Doc.Line tr = heroRow(hb, 0, w).t("Translated", Paint.dim()).at(kw).t(through, Paint.sub()).sp(10).t("Client", Paint.dim()).sp(4);
        tr.t(SharedConstants.getCurrentVersion().name() + ", protocol " + ServerSoftware.nativeProtocol(), Paint.sub(), Math.max(20, iw - tr.cur));
        int ri = 1;
        if (dis) {
            Doc.Line dl = heroRow(hb, ri++, w).t("Disagreement", Paint.dim()).at(kw).i(Paint.WARN, Paint.yellow());
            if (Paint.iconW(Paint.WARN) > 0) dl.sp(4);
            dl.t(rep.disagree(), Paint.sub(), Math.max(20, iw - dl.cur));
        }
        Doc.Line cl = null;
        for (int i = 0; i < stages.size(); i++) {
            if (cl == null || chainRow[i] != chainRow[i - 1]) {
                cl = heroRow(hb, ri + chainRow[i], w);
                if (i == 0) cl.t("Range", Paint.dim());
                cl.at(kw);
            }
            RealVersion.Stage st = stages.get(i);
            cl.chip(or(st.step(), ""), st.range(), cl.cur > kw, i == stages.size() - 1);
            Doc.Span cs = cl.last();
            int over = cs.x + cs.width() - iw;
            if (over > 0 && cs.count != null) {
                cs.max(Math.max(12, Paint.w(cs.count) - over));
                cs.tip(List.of(Paint.c(or(st.step(), "") + " " + st.range())));
                cl.at(cs.x + cs.width());
            }
        }
        List<RealVersion.Evidence> ev = rep.evidence() == null ? List.of() : rep.evidence();
        Map<String, List<RealVersion.Evidence>> groups = new LinkedHashMap<>();
        for (RealVersion.Evidence e : ev) if (e != null) groups.computeIfAbsent(or(e.source(), ""), k -> new ArrayList<>()).add(e);
        Doc.Line hl = head("Evidence", groups.size() + (groups.size() == 1 ? " source" : " sources"));
        fateLegend(hl, ev, w);
        d.add(hl);
        d.add(new Doc.Gap(t6(22, 18, 20, 20, 20, 18) - 9));
        versionEvidence(d, w, groups);
    }

    private static final String[] FATE_LABELS = {"Decisive", "Hint", "Agrees", "Ignored", "Not used"};

    private static int fateKind(RealVersion.Evidence e) {
        RealVersion.Fate f = e.fate() == null ? RealVersion.Fate.NONE : e.fate();
        return switch (f) {
            case DECISIVE -> e.hint() ? 1 : 0;
            case AGREES -> 2;
            case IGNORED -> 3;
            case UNUSED -> 4;
            case NONE -> 5;
        };
    }

    private static int fateMark(int k) {
        return k == 0 ? Paint.PLUS : k == 1 ? Paint.TILDE : k == 3 || k == 4 ? Paint.MINUS : Paint.DOT;
    }

    private static int fateColor(int k) {
        return k == 0 ? Paint.acc() : k == 1 ? Paint.sub() : Paint.dim();
    }

    private static void fateLegend(Doc.Line l, List<RealVersion.Evidence> ev, int w) {
        boolean[] has = new boolean[6];
        for (RealVersion.Evidence e : ev) if (e != null) has[fateKind(e)] = true;
        int ig = t6(4, 4, 4, 4, 4, 6), sg = t6(10, 10, 10, 10, 12, 12), total = 0, n = 0;
        for (int k = 0; k < FATE_LABELS.length; k++) {
            if (!has[k]) continue;
            total += (n++ > 0 ? sg : 0) + Paint.iconW(fateMark(k)) + ig + Paint.w(Paint.c(FATE_LABELS[k]));
        }
        int x = w - total;
        if (n == 0 || x < l.cur + 12) return;
        l.at(x);
        boolean first = true;
        for (int k = 0; k < FATE_LABELS.length; k++) {
            if (!has[k]) continue;
            if (!first) l.sp(sg);
            first = false;
            l.i(fateMark(k), fateColor(k)).sp(ig).t(FATE_LABELS[k], Paint.dim());
        }
    }

    private static void versionEvidence(Doc d, int w, Map<String, List<RealVersion.Evidence>> groups) {
        int rh = t6(16, 14, 16, 16, 14, 14);
        int sc = t6(92, 86, 0, 92, 90, 106), cx = t6(106, 99, 0, 106, 102, 118);
        if (HontunTheme.modern1()) {
            int tw = 0;
            for (String s : groups.keySet()) tw = Math.max(tw, Paint.w(Paint.c(cap(s))));
            sc = tw + 18;
            cx = sc + 13;
        }
        int rx = w - 10;
        int ruleH = t6(4, 4, 4, 6, 6, 5), ruleAt = t6(1, 1, 1, 2, 2, 2), ruleW = w - t6(20, 22, 26, 26, 24, 20);
        Doc.Well pw = panel();
        if (groups.isEmpty()) pw.inner.add(new Doc.Line(rh).text(10, "Nothing collected yet", Paint.dim()));
        int gi = 0;
        for (Map.Entry<String, List<RealVersion.Evidence>> en : groups.entrySet()) {
            String title = cap(en.getKey());
            if (gi++ > 0) {
                Doc.Sep sep = new Doc.Sep(ruleH, ruleAt, Doc.RULE);
                sep.inset = 8;
                sep.len = ruleW;
                pw.inner.add(sep);
            }
            boolean first = true;
            for (RealVersion.Evidence e : en.getValue()) {
                int k = fateKind(e);
                boolean strong = k <= 1;
                Doc.Line l = new Doc.Line(rh);
                if (first && !title.isEmpty()) l.text(10, title, Paint.dim(), Math.max(20, sc - 14));
                first = false;
                l.icon(sc, fateMark(k), fateColor(k));
                String imp = e.implies() != null && !e.implies().isBlank() ? e.implies() : null;
                int tone = strong ? Paint.ACC : k == 2 ? Paint.NORMAL : Paint.DIM;
                int pr = imp != null ? 5 + Paint.pillWidth(Paint.c(imp), tone, -1) : 0;
                l.at(cx).t(or(e.signal(), ""), strong ? Paint.text() : Paint.sub(), Math.max(20, rx - cx - pr));
                if (imp != null) l.sp(5).p(imp, tone, -1);
                String why = or(e.reason(), "");
                if (!why.isEmpty()) {
                    l.sp(5);
                    int room = rx - l.cur;
                    if (room >= 20) l.t(why, Paint.dim(), room);
                }
                Doc.Tip tip = versionTip(e, title);
                l.tip = tip;
                if (tip != null) for (Doc.Span sp : l.spans) if (sp.clips()) sp.tip(tip);
                l.rowBg = true;
                pw.inner.add(l);
            }
        }
        d.add(close(pw));
    }

    private static Doc.Tip versionTip(RealVersion.Evidence e, String title) {
        List<String> det = new ArrayList<>();
        if (e.details() != null) for (String s : e.details()) if (s != null && !s.isBlank()) det.add(s);
        String sig = or(e.signal(), ""), why = or(e.reason(), "");
        if (det.isEmpty()) {
            List<Component> lines = new ArrayList<>();
            if (!sig.isEmpty()) lines.add(Paint.c(sig));
            if (!why.isEmpty()) lines.add(Paint.c(why));
            return Doc.Tip.of(lines);
        }
        Doc.Tip t = new Doc.Tip();
        Doc.Line hd = t.line().at(0);
        if (!title.isEmpty()) hd.p(title, Paint.NORMAL, -1).sp(5);
        hd.t(det.size() + (det.size() == 1 ? " signal" : " signals"), Paint.dim());
        for (String s : det) t.line().raw(0, Paint.c(s), Paint.sub());
        if (!why.isEmpty()) t.para(Paint.c(why), Paint.dim());
        return t;
    }

    private static void plugins(Doc d, int w) {
        PluginScanner ps = PluginScanner.INSTANCE;
        List<String> list = ps.results();
        boolean running = ps.running();
        String scan = running ? "Scanning…" : ps.scanned() ? "Scan again" : "Scan";
        String bypass = "Plugin hider bypass";
        int scanW = btnW(scan), bypassW = btnW(bypass);
        Doc.Line h = title(d, "Plugins");
        sum(h, running ? "scanning…" : ps.scanned() ? list.size() + " found by the command tree and /version suggestions" : "not scanned",
                Paint.dim(), w - scanW - btnGap() - bypassW - 8 - h.cur);
        d.buttons.add(new Doc.Btn(scan, h, 0, scanW, running ? null : () -> ps.run(true, true, false, true), Paint.REFRESH));
        d.buttons.add(new Doc.Btn(bypass, h, scanW + btnGap(), bypassW, running ? null : () -> ps.run(false, false, true, false), Paint.SEARCH));
        gapTo(d, blockTop());
        if (ps.skipped() > 0) note(d, ps.skipped() + " suggested names were not listed (over 512 names or longer than 64 chars), see " + Oversize.where(PluginScanner.OVERSIZE));
        if (list.isEmpty()) {
            Doc.Well well = panel();
            Doc.Line l = new Doc.Line(Paint.vanilla() ? 40 : 46);
            int tx = 10;
            if (Paint.iconW(Paint.PLUGINS) > 0) {
                l.icon(10, Paint.PLUGINS, Paint.dim());
                tx = 24;
            }
            String msg = running ? "Waiting for the server's suggestions" : ps.scanned() ? "No plugins found"
                    : "The scan sends tab-complete requests, so it runs only when you start it";
            l.text(tx, msg, Paint.sub(), w - tx - 10);
            well.inner.add(l);
            d.add(close(well));
        } else {
            boolean first = true;
            for (String cat : CATEGORIES) {
                List<String> in = new ArrayList<>();
                for (String p : list) if (category(p).equals(cat)) in.add(p);
                if (in.isEmpty()) continue;
                if (!first) d.add(new Doc.Gap(8));
                first = false;
                Doc.Line ch = new Doc.Line(12).text(0, cat, Paint.sub()).at(Paint.w(Paint.c(cat)) + 6);
                sum(ch, String.valueOf(in.size()), Paint.dim(), 0);
                d.add(ch);
                pluginGrid(d, in, w);
            }
        }
        note(d, "The bypass enumerates /help and /execute run suggestions. Use it when the server hides its plugins.");
    }

    private static void pluginGrid(Doc d, List<String> names, int w) {
        List<String> sorted = new ArrayList<>(names);
        sorted.sort(String.CASE_INSENSITIVE_ORDER);
        int n = sorted.size(), rows = (n + 2) / 3;
        int pg = Paint.term() ? 12 : 6;
        if (HontunTheme.modern1()) {
            int cw = (w - 12) / 3;
            for (int r = 0; r < rows; r++) {
                Doc.Box row = new Doc.Box(20);
                for (int c = 0; c < 3; c++) {
                    int i = c * rows + r;
                    if (i >= n) continue;
                    String name = sorted.get(i);
                    String cat = category(name);
                    boolean flag = !cat.equals(CATEGORIES[3]);
                    int x = c * (cw + 6), tx = flag ? 10 : 7;
                    if (flag) row.put(new FlagCell(), x, 0, cw);
                    Doc.Line cell = new Doc.Line(20);
                    cell.card = !flag;
                    pluginCell(cell, tx, name, cat, flag, cw - tx - 4, pg);
                    row.put(cell, x, 0, cw);
                }
                d.add(row);
            }
            return;
        }
        int cw = (w - 20 - 32) / 3;
        boolean rules = rowRules();
        Doc.Well well = panel();
        for (int r = 0; r < rows; r++) {
            Doc.Line l = new Doc.Line(20);
            for (int c = 0; c < 3; c++) {
                int i = c * rows + r;
                if (i >= n) continue;
                int x = 10 + c * (cw + 16);
                if (r > 0 && rules) l.segs.add(new int[]{x, cw});
                String name = sorted.get(i);
                String cat = category(name);
                pluginCell(l, x, name, cat, !cat.equals(CATEGORIES[3]), cw, pg);
            }
            well.inner.add(l);
        }
        d.add(close(well));
    }

    private static void pluginCell(Doc.Line l, int x, String name, String cat, boolean flag, int max, int pg) {
        String tag = cat.toLowerCase(Locale.ROOT);
        int pw = flag ? Paint.pillWidth(Paint.c(tag), Paint.ACC, Paint.ANTICHEAT) + pg : 0;
        l.at(x).t(name, flag ? Paint.acc() : Paint.sub(), Math.max(20, max - pw));
        if (!flag) return;
        l.last().tip(List.of(Paint.c(name), Paint.c(cat)));
        l.sp(pg).p(tag, Paint.ACC, Paint.ANTICHEAT);
    }

    private static final class FlagCell extends Doc.Item {
        FlagCell() { h = 20; }

        @Override
        void draw(GuiGraphicsExtractor g, int ox, int oy, int mx, int my) {
            int bx = ox + x, by = oy + y + 1, bh = h - 2;
            if (w <= 2) return;
            int fill = 0xFF23334B;
            g.fill(bx + 1, by, bx + w - 1, by + bh, fill);
            g.fill(bx, by + 1, bx + 1, by + bh - 1, fill);
            g.fill(bx + w - 1, by + 1, bx + w, by + bh - 1, fill);
            HontunCards.border(g, bx, by, w, bh, HontunTheme.argb(0x40, HontunTheme.overlay0()));
            g.fillGradient(bx, by + 1, bx + 3, by + 15, Paint.acc(), Paint.accLo());
        }
    }

    private static boolean matches(PlayerInfo p, String q) {
        String low = q.toLowerCase(Locale.ROOT);
        String n = p.getProfile().name().toLowerCase(Locale.ROOT);
        if (n.contains(low)) return true;
        if (name(p).getString().toLowerCase(Locale.ROOT).contains(low)) return true;
        String id = p.getProfile().id().toString();
        String nd = low.replace("-", "");
        return id.startsWith(low) || id.replace("-", "").startsWith(nd);
    }

    private static boolean self(PlayerInfo p) {
        var me = mc().player;
        if (me == null) return false;
        UUID id = p.getProfile().id();
        if (id != null && id.equals(me.getUUID())) return true;
        String n = p.getProfile().name();
        return n != null && n.equalsIgnoreCase(me.getGameProfile().name());
    }

    private static Doc.Line playerRow(int i, int w) {
        Doc.Line l = new Doc.Line(20);
        if (HontunTheme.modern1()) {
            l.card = true;
            return l;
        }
        l.rowBg = true;
        if (Paint.vanilla()) {
            l.hx = 2;
            l.hw = -4;
        }
        if ((HontunTheme.smog() || HontunTheme.modern2()) && i > 0) {
            l.ruleTop = true;
            l.ruleX = 8;
            l.ruleW = w - 16;
        }
        return l;
    }

    private static void players(ServerInfoScreen s, Doc d, int w) {
        List<PlayerInfo> all = tab();
        String q = s.query == null ? "" : s.query.trim(), ql = q.toLowerCase(Locale.ROOT);
        List<PlayerInfo> shown = new ArrayList<>();
        for (PlayerInfo p : all) if (q.isEmpty() || matches(p, q)) shown.add(p);
        PlayerRoster roster = PlayerRoster.INSTANCE;
        boolean running = roster.running(), scanned = roster.finished() && !running;
        Set<String> inTab = new LinkedHashSet<>();
        for (PlayerInfo p : all) if (p.getProfile().name() != null) inTab.add(p.getProfile().name().toLowerCase(Locale.ROOT));
        List<String> hidAll = new ArrayList<>(), hidden = new ArrayList<>();
        if (scanned) for (String n : roster.hidden()) if (!inTab.contains(n.toLowerCase(Locale.ROOT))) hidAll.add(n);
        for (String n : hidAll) if (q.isEmpty() || n.toLowerCase(Locale.ROOT).contains(ql)) hidden.add(n);
        int total = all.size() + hidAll.size();

        boolean smog = HontunTheme.smog(), m2 = HontunTheme.modern2(), m1 = HontunTheme.modern1(), term = Paint.term(), va = Paint.vanilla();
        boolean hv = !term && HontunTheme.hvanilla(), rowBtn = m2 || m1 || smog;
        String label = running ? "Scanning" : scanned ? "Scan again" : "Find hidden players";
        int icon = running || roster.finished() ? Paint.REFRESH : Paint.PLAYERS, bwMax = 0;
        for (String l : new String[]{"Find hidden players", "Scan again", "Scanning"}) bwMax = Math.max(bwMax, btnW(l));
        int bw = m1 ? bwMax : btnW(label);
        Runnable act = running ? null : roster::run;

        Doc.Line h = title(d, "Players");
        int room = w - (rowBtn ? 0 : bw + 8);
        String base = all.isEmpty() ? "Nobody in the tab list" : all.size() + " in the tab list, avg ping " + avgPing(all) + " ms";
        if (q.isEmpty()) {
            sum(h, base, Paint.dim(), room - h.cur);
        } else {
            sum(h, base + ", ", Paint.dim(), room - h.cur);
            sum(h, (shown.size() + hidden.size()) + " of " + total + " shown", Paint.sub(), room - h.cur);
        }
        if (!rowBtn) d.buttons.add(new Doc.Btn(label, h, 0, bw, act, icon));

        gapTo(d, blockTop());
        if (scanned && roster.skipped() > 0) note(d, roster.skipped() + " more suggested names were not checked (over 1000), see " + Oversize.where(PlayerRoster.OVERSIZE));
        int fh = m1 ? 16 : 18;
        Doc.Gap field = new Doc.Gap(fh);
        d.add(field);
        d.searchAnchor = field;
        d.searchX = 0;
        d.searchH = fh;
        d.searchW = rowBtn ? Math.max(40, w - bwMax - (m1 ? 6 : 8)) : w;
        if (rowBtn) d.buttons.add(new Doc.Btn(label, field, 0, bw, act, icon));
        d.add(new Doc.Gap(t6(8, 8, 8, 8, 10, 6)));

        int nameX = sx(t6(10, 12, 8, 10, 10, 10), w), accX = sx(t6(254, 300, 266, 300, 380, 340), w);
        int gmX = sx(t6(326, 362, 344, 380, 450, 450), w), pingX = sx(t6(404, 432, 412, 470, 528, 560), w);
        int uuidX = sx(t6(482, 504, 478, 570, 598, 670), w);
        Doc.Line hd = new Doc.Line(t6(18, 16, 13, 14, 18, 17));
        String[] heads = {"Player", "Account", "Gamemode", "Ping", "UUID"};
        int[] hx = {nameX, accX, gmX, pingX, uuidX};
        int hdy = t6(5, 3, 0, 2, 4, 3);
        for (int i = 0; i < heads.length; i++) hd.small(hx[i], heads[i], Paint.dim()).last().dy(hdy);
        if (smog) {
            hd.ruleBottom = true;
            hd.ruleX = 8;
            hd.ruleW = w - 16;
        } else if (term || va) {
            hd.ruleBottom = true;
        }
        hd.strip = hv;

        Doc.Well panel = null;
        if (smog || m2 || term || hv) {
            panel = new Doc.Well();
            panel.style = Doc.PANEL;
        }
        List<Doc.Item> into = panel != null ? panel.inner : d.full;
        if (hv) d.add(hd);
        else into.add(hd);
        if (m2) {
            Doc.Sep r = new Doc.Sep(0, 0, Doc.RULE);
            r.inset = 8;
            into.add(r);
        }

        int nameGap = t6(8, 8, 8, 8, 10, 12), youGap = t6(5, 6, 5, 5, 6, 12), hidGap = t6(6, 6, 6, 6, 6, 12);
        int pingGap = term ? 6 : 4, uuidGap = term ? 6 : 5;
        boolean bars = smog || hv || term;
        int n = 0;
        for (PlayerInfo p : shown) {
            Doc.Line l = playerRow(n++, w);
            UUID id = p.getProfile().id();
            boolean me = self(p);
            l.face(nameX, p.getSkin());
            int youW = me ? youGap + Paint.pillWidth(Paint.c("You"), Paint.ACC, -1) : 0;
            l.at(nameX + 9).r(clipRaw(name(p), Math.max(20, accX - nameGap - nameX - 9 - youW)), Paint.text());
            if (me) l.sp(youGap).p("You", Paint.ACC, -1);
            l.pill(accX, account(id), Paint.NORMAL, -1);
            String gm = gamemode(p);
            if (!gm.isEmpty()) l.text(gmX, gm, Paint.sub());
            int lat = p.getLatency(), lit = Paint.pingLit(lat);
            l.at(pingX);
            if (bars) l.bars(lit);
            else l.sprite(pingX, Identifier.withDefaultNamespace(lit == 0 ? "server_list/unreachable" : "server_list/ping_" + lit), 10, 8).sp(10);
            l.sp(pingGap);
            if (lat >= 0) l.t(lat + " ms", Paint.sub());
            else l.t("Unknown", Paint.dim());
            if (id != null) {
                String full = id.toString();
                Runnable copy = () -> s.copy(full);
                List<Component> ct = List.of(Paint.c(full), Paint.c("Click to copy"));
                l.at(uuidX).t(shortUuid(id), Paint.sub());
                l.last().on(copy).key(full).tip(ct);
                l.at(Math.max(l.cur, uuidX + 56)).sp(uuidGap).i(Paint.COPY, Paint.dim());
                l.last().on(copy).key(full).tip(ct);
            }
            l.tip = playerTip(p);
            into.add(l);
        }
        int box = term ? TerminalWidgets.empty() : HontunTheme.argb(0xFF, HontunTheme.surface2());
        int hidW = hidGap + Paint.pillWidth(Paint.c("Hidden"), Paint.DIM, -1);
        for (String name : hidden) {
            Doc.Line l = playerRow(n++, w);
            l.box(nameX, 8, 8, box);
            l.at(nameX + 9).t(name, Paint.sub(), Math.max(20, accX - nameGap - nameX - 9 - hidW));
            l.sp(hidGap).p("Hidden", Paint.DIM, -1);
            l.text(accX, "Not in the tab list", Paint.dim());
            into.add(l);
        }
        if (n == 0) {
            Doc.Line l = new Doc.Line(20).at(nameX);
            if (q.isEmpty()) {
                l.t("Nobody in the tab list", Paint.dim());
            } else {
                l.t("No player matches \"", Paint.dim()).t(q, Paint.sub(), Math.max(20, w / 3)).t("\". ", Paint.dim());
                linkT(l, "Clear the search", s::clearSearch);
                l.t(" to see all " + total + ".", Paint.dim());
            }
            into.add(l);
        }
        if (panel != null) d.add(panel);
        if (va) d.add(new Doc.Sep(2, 0, Doc.RULE_F));

        d.add(new Doc.Gap(t6(7, 10, 10, 10, 8, 6)));
        int kw = kw(ServerInfoScreen.Page.PLAYERS), gap = kvGap();
        List<Doc.Line> facts = new ArrayList<>();
        Doc.Line yl = next(facts, "You", kw);
        Matcher pm = PERM.matcher(ServerInfoScreen.perms == null ? "" : ServerInfoScreen.perms.strip());
        if (pm.matches()) {
            int lv = -1;
            try { lv = Integer.parseInt(pm.group(1)); } catch (NumberFormatException ignored) {}
            yl.t("Permission level " + pm.group(1), Paint.sub()).t(" (", Paint.sub());
            int close = Paint.w(Paint.c(")"));
            yl.t(pm.group(2), lv >= 2 ? Paint.acc() : Paint.sub(), Math.max(20, w - yl.cur - close)).t(")", Paint.sub());
        } else {
            yl.t("Unknown", Paint.sub());
        }
        if (running) {
            next(facts, "Suggestion roster", kw).t("Asking /msg, /tell, /tpa and similar commands for names", Paint.dim(), w - kw);
        } else if (scanned) {
            Doc.Line rl = next(facts, "Suggestion roster", kw);
            int found = roster.found().size(), hn = hidAll.size();
            if (found == 0) {
                rl.t("None", Paint.sub()).sp(gap);
                rl.t("the server exposed no player-name suggestions", Paint.dim(), Math.max(20, w - rl.cur));
            } else {
                rl.t(String.valueOf(found), Paint.sub()).t(found == 1 ? " name, " : " names, ", Paint.dim());
                if (hn > 0) rl.t(String.valueOf(hn), Paint.sub()).t(" not in the tab list", Paint.dim());
                else rl.t("all in the tab list", Paint.dim());
            }
        }
        for (Doc.Line r : facts) d.add(r);
        d.add(new Doc.Gap(t6(8, 10, 10, 10, 10, 6)));
        d.add(new Doc.Note(Paint.INFO, Paint.dim(), Paint.sm("The hidden-player search asks message commands for names, so it can miss players."),
                Paint.dim(), m2 ? sx(300, w) : 0));
    }

    private record SkinInfo(String hash, boolean signed, String profileName, boolean premium) {}

    private static SkinInfo skinInfo(PlayerInfo p) {
        Property pr;
        try {
            var list = p.getProfile().properties().get("textures");
            if (list == null || list.isEmpty()) return null;
            pr = list.iterator().next();
        } catch (Throwable t) {
            return null;
        }
        if (pr == null) return null;
        String hash = null, pname = null;
        boolean premium = false, signed = false;
        try {
            JsonObject o = JsonParser.parseString(new String(Base64.getDecoder().decode(pr.value()), StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject tx = o.has("textures") && o.get("textures").isJsonObject() ? o.getAsJsonObject("textures") : null;
            JsonObject sk = tx != null && tx.has("SKIN") && tx.get("SKIN").isJsonObject() ? tx.getAsJsonObject("SKIN") : null;
            if (sk != null && sk.has("url") && sk.get("url").isJsonPrimitive()) {
                String url = sk.get("url").getAsString();
                String seg = url.substring(url.lastIndexOf('/') + 1);
                if (!seg.isBlank()) hash = seg;
            }
            if (o.has("profileName") && o.get("profileName").isJsonPrimitive()) pname = o.get("profileName").getAsString();
            if (o.has("profileId") && o.get("profileId").isJsonPrimitive()) {
                String pid = o.get("profileId").getAsString().replace("-", "");
                premium = pid.length() > 12 && pid.charAt(12) == '4';
            }
        } catch (Throwable ignored) {
        }
        try { signed = pr.hasSignature(); } catch (Throwable ignored) {}
        return new SkinInfo(hash, signed, pname == null || pname.isBlank() ? null : pname, premium);
    }

    private static String skinModel(PlayerInfo p) {
        try { return cap(p.getSkin().model().getSerializedName()); } catch (Throwable t) { return ""; }
    }

    private static Doc.Tip playerTip(PlayerInfo p) {
        UUID id = p.getProfile().id();
        String name = p.getProfile().name(), acc = account(id), team = null;
        try { if (p.getTeam() != null) team = p.getTeam().getName(); } catch (Throwable ignored) {}
        SkinInfo sk = skinInfo(p);
        List<String> keys = new ArrayList<>(List.of("UUID", "Account", "List order", "Chat key", "Hat layer"));
        if (team != null) keys.add("Team");
        if (sk != null) keys.add("Skin");
        int kw = 54;
        if (!HontunTheme.smog()) {
            int m = 0;
            for (String k : keys) m = Math.max(m, Paint.w(Paint.c(k)));
            kw = m + 8;
        }
        Doc.Tip t = new Doc.Tip();
        t.keyRow("UUID", kw).t(id != null ? id.toString() : "Unknown", Paint.sub());
        Doc.Line al = t.keyRow("Account", kw).t(acc, Paint.sub());
        if ("Cracked".equals(acc) && name != null && id.equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8)))) {
            al.t(", matches ", Paint.dim()).t("OfflinePlayer:" + name, Paint.sub());
        }
        if (team != null) t.keyRow("Team", kw).t(team, Paint.sub());
        t.keyRow("List order", kw).t(String.valueOf(p.getTabListOrder()), Paint.sub());
        t.keyRow("Chat key", kw).t(p.hasVerifiableChat() ? "Present" : "None", Paint.sub());
        if (sk != null) {
            List<String> parts = new ArrayList<>();
            String model = skinModel(p);
            if (!model.isEmpty()) parts.add(model);
            if (sk.hash() != null) parts.add(sk.hash().length() > 12 ? sk.hash().substring(0, 12) + "…" : sk.hash());
            parts.add(sk.signed() ? "signed" : "unsigned");
            t.keyRow("Skin", kw).t(String.join(", ", parts), Paint.sub());
            if (sk.profileName() != null) {
                t.keyRow("", kw).t("From the " + (sk.premium() ? "premium" : "offline") + " profile ", Paint.dim()).t(sk.profileName(), Paint.sub());
            }
        }
        t.keyRow("Hat layer", kw).t(p.showHat() ? "On" : "Off", Paint.sub());
        return t;
    }

    private static Doc.Line next(List<Doc.Line> rows, String key, int kw) {
        Doc.Line l = kvRow(key, kw, !rows.isEmpty());
        rows.add(l);
        return l;
    }

    private static void world(ServerInfoScreen s, Doc d, int w) {
        var lvl = mc().level;
        String dim = or(WorldInfo.dimension, lvl != null ? lvl.dimension().identifier().toString() : null);
        Doc.Line h = title(d, "World");
        if (dim != null) {
            sum(h, "You are in ", Paint.dim(), 0);
            sum(h, dim, Paint.sub(), w - h.cur);
        }
        int kw = kw(ServerInfoScreen.Page.WORLD), gap = kvGap(), vw = w - kw;
        List<Doc.Line> rows = new ArrayList<>();
        List<String> levels;
        synchronized (WorldInfo.levels) { levels = new ArrayList<>(WorldInfo.levels); }
        if (dim != null) levels.removeIf(dim::equals);
        if (!levels.isEmpty()) next(rows, levels.size() > 1 ? "Other dimensions" : "Other dimension", kw).t(String.join(", ", levels), Paint.sub(), vw);
        if (lvl != null) {
            next(rows, "Difficulty", kw).t(lvl.getDifficulty().getDisplayName().getString(), Paint.sub(), vw);
            next(rows, "Day", kw).t(String.valueOf(lvl.getGameTime() / 24000L), Paint.sub());
        }
        String tps = fmtTps();
        if (tps != null) next(rows, "Tick rate", kw).t(tps, Paint.sub());
        if (WorldInfo.captured) {
            Doc.Line mp = next(rows, "Max players", kw).t(String.valueOf(WorldInfo.maxPlayers), Paint.sub());
            ServerData sd = mc().getCurrentServer();
            if (sd != null && sd.players != null && sd.players.max() != WorldInfo.maxPlayers) {
                mp.sp(gap).t("the server list says " + sd.players.max(), Paint.dim());
            }
            next(rows, "Render distance", kw).t(WorldInfo.chunkRadius + " chunks", Paint.sub());
            next(rows, "Simulation distance", kw).t(WorldInfo.simulationDistance + " chunks", Paint.sub());
            next(rows, "Hardcore", kw).t(WorldInfo.hardcore ? "On" : "Off", Paint.sub());
            next(rows, "Secure chat", kw).t(WorldInfo.secureChat ? "On" : "Off", Paint.sub());
        }
        List<String> packs;
        synchronized (VersionKeeper.knownPacks) { packs = new ArrayList<>(VersionKeeper.knownPacks); }
        if (!packs.isEmpty()) {
            Doc.Line pl = next(rows, packs.size() > 1 ? "Data packs" : "Data pack", kw);
            int ell = Paint.w(Paint.c("…")) + 10;
            for (int i = 0; i < packs.size(); i++) {
                Matcher m = PACK.matcher(packs.get(i));
                boolean ok = m.matches();
                String id = ok ? m.group(1) : packs.get(i), ver = ok ? m.group(2) : null;
                int need = (i > 0 ? 10 : 0) + Paint.w(Paint.c(id)) + (ver != null ? gap + Paint.w(Paint.c(ver)) : 0);
                if (i > 0 && pl.cur + need > w - (i < packs.size() - 1 ? ell : 0)) {
                    pl.sp(10).t("…", Paint.dim());
                    List<Component> tip = new ArrayList<>();
                    tip.add(Paint.c(packs.size() + " data packs"));
                    for (String p : packs) tip.add(Paint.c(p));
                    pl.tip(tip);
                    break;
                }
                if (i > 0) pl.sp(10);
                pl.t(id, Paint.sub(), i == 0 ? Math.max(20, vw - (ver != null ? gap + Paint.w(Paint.c(ver)) : 0)) : 0);
                if (ver != null) pl.sp(gap).t(ver, Paint.dim());
            }
        }
        if (WorldInfo.captured) {
            String seed = String.valueOf(WorldInfo.hashedSeed);
            List<Component> tip = List.of(Paint.c(seed), Paint.c("Click to copy"));
            Doc.Line sl = next(rows, "Hashed seed", kw).t(seed, Paint.sub());
            sl.last().on(() -> s.copy(seed)).key(seed).tip(tip);
            sl.sp(gap).i(Paint.COPY, Paint.dim());
            sl.last().on(() -> s.copy(seed)).key(seed).tip(tip);
        }
        if (rows.isEmpty()) return;
        boolean va = Paint.vanilla();
        gapTo(d, va ? 38 : t6(24, 34, 32, 36, 40, 28));
        if (va) d.add(new Doc.Sep(2, 0, Doc.RULE));
        for (Doc.Line r : rows) d.add(r);
        if (va) d.add(new Doc.Sep(2, 0, Doc.RULE_F));
    }

    private record Node(String role, String name, List<String> lines) {}

    private static void connection(ServerInfoScreen s, Doc d, int w) {
        ServerData sd = mc().getCurrentServer();
        ServerSoftware.Result r = softwareResult();
        RealVersion.Report rep = report();
        RealVersion.Result rr = rep != null ? rep.result() : null;
        String proxy = r != null && r.proxy() != null && !r.proxy().isBlank() ? r.proxy().strip() : null;
        Doc.Line th = title(d, "Connection");
        sum(th, "How your client reaches the server", Paint.dim(), w - th.cur);

        List<Node> nodes = new ArrayList<>();
        String through = rep != null && rep.through() != null && !rep.through().isBlank() ? rep.through() : null;
        nodes.add(new Node("Client", "You", List.of(
                SharedConstants.getCurrentVersion().name() + (rr != null && rr.translated() && through != null ? " through " + through : " client"),
                "Speaks protocol " + ServerSoftware.nativeProtocol())));
        String[] hops = proxy != null ? proxy.split(" \\+ ") : new String[0];
        for (int i = 0; i < hops.length; i++) {
            nodes.add(new Node("Proxy", cap(hops[i]), i == 0 ? List.of("At least " + plural(hops.length, "hop", "hops")) : List.of()));
        }
        List<String> bl = new ArrayList<>();
        if (rr != null && rr.method() != null && !rr.method().isBlank() && rr.range() != null && !rr.range().isBlank()) {
            bl.add("Real version " + rr.range().strip());
        }
        nodes.add(new Node("Backend", r != null ? or(r.software(), "Unknown") : "Unknown", bl));
        int ry = t6(24, 34, 32, 38, 40, 32);
        gapTo(d, ry);
        Doc.Box rb = route(nodes, w);
        d.add(rb);

        int kw = kw(ServerInfoScreen.Page.CONNECTION), gap = kvGap();
        List<Doc.Line> facts = new ArrayList<>();
        if (sd != null) {
            String typed = "as typed";
            Doc.Line al = next(facts, "Address", kw);
            al.t(sd.ip, Paint.sub(), Math.max(20, w - kw - gap - Paint.w(Paint.c(typed)))).sp(gap).t(typed, Paint.dim());
            try {
                int port = ServerAddress.parseString(sd.ip).getPort();
                next(facts, "Port", kw).t(String.valueOf(port), Paint.sub());
            } catch (Throwable ignored) {
            }
        }
        String con = connectedTo();
        if (con != null) next(facts, "Connected to", kw).t(con, Paint.sub(), w - kw);
        if (sd != null) {
            String ver = sd.version != null && !sd.version.getString().isBlank() ? sd.version.getString() : "unknown";
            String pn = String.valueOf(sd.protocol), pl = "Protocol";
            int ps = t6(10, 10, 10, 10, 12, 12), pw = ps + Paint.w(Paint.c(pl)) + gap + Paint.w(Paint.c(pn));
            next(facts, "Server list", kw).t(ver, Paint.sub(), Math.max(20, w - kw - pw)).sp(ps).t(pl, Paint.dim()).sp(gap).t(pn, Paint.sub());
        }
        int fy = t6(ry + 74, Math.max(102, ry + rb.h + 14), 104, 112, 110, 108);
        gapTo(d, fy);
        for (Doc.Line l : facts) d.add(l);
        if (!facts.isEmpty()) edge(d, facts.getFirst(), facts.getLast());

        d.add(new Doc.Gap(13));
        int hy = height(d.full) + 1;
        Doc.Line ph = head("Proxy");
        List<ServerSoftware.Evidence> sig = proxy != null ? proxySignals(r, proxy) : List.of();
        if (proxy == null) sum(ph, "None detected", Paint.sub(), 0);
        else if (!sig.isEmpty()) sum(ph, plural(sig.size(), "signal", "signals"), Paint.dim(), 0);
        d.add(ph);
        int y;
        if (!sig.isEmpty()) {
            gapTo(d, hy + t6(22, 18, 20, 20, 20, 18));
            proxyRows(d, r, sig, w);
            y = height(d.full) + 10;
        } else {
            y = hy + (proxy == null ? t6(22, 20, 20, 20, 20, 18) : t6(22, 18, 20, 20, 20, 18));
        }
        gapTo(d, y);
        Doc.Note pn = Doc.note(proxy != null
                ? "A transparent proxy or packet logger forwards unchanged and leaves no trace, so the real chain may be longer."
                : "A transparent relay or packet logger would be invisible.");
        pn.measure(w);
        d.add(pn);
        y += 10 * Math.max(1, pn.lines.size()) - t6(0, 0, 1, 0, 1, 1);
        gapTo(d, y + 12);
        d.add(new Doc.Sep(Paint.vanilla() ? 15 : 13, 0, HontunTheme.smog() ? Doc.FADE : Doc.RULE));

        int rpy = height(d.full) + 1;
        List<ResourcePackInfo.Pack> packs = packs(proxy);
        Doc.Line rh = head("Resource pack");
        sum(rh, packSummary(packs), Paint.sub(), 0);
        d.add(rh);
        if (packs.isEmpty() && !ResourcePackInfo.present) return;
        gapTo(d, rpy + t6(20, 20, 17, 20, 17, 18));
        if (packs.isEmpty()) {
            packRows(s, d, w, kw, ResourcePackInfo.url, ResourcePackInfo.hash, ResourcePackInfo.prompt, null);
            return;
        }
        for (int i = 0; i < packs.size(); i++) {
            ResourcePackInfo.Pack p = packs.get(i);
            if (packs.size() > 1) {
                if (i > 0) d.add(new Doc.Gap(t6(14, 12, 12, 14, 12, 12)));
                String name = Paint.term() ? "pack " + (i + 1) : "Pack " + (i + 1);
                Doc.Line pl = new Doc.Line(10).text(0, name, Paint.text());
                pl.at(Paint.w(Paint.c(name)) + 6);
                sum(pl, (p.required() ? "Required" : "Optional") + (p.popped() ? ", removed" : ""), Paint.dim(), 0);
                d.add(pl);
                d.add(new Doc.Gap(t6(6, 6, 5, 6, 5, 4)));
            }
            packRows(s, d, w, kw, p.url(), p.hash(), p.prompt(), p);
        }
    }

    private static List<ResourcePackInfo.Pack> packs(String proxy) {
        List<ResourcePackInfo.Pack> all;
        try { all = ResourcePackInfo.packs(proxy); } catch (Throwable t) { return List.of(); }
        int skip = -2;
        for (ResourcePackInfo.Pack p : all) if (p.popped()) skip++;
        if (skip <= 0) return all;
        List<ResourcePackInfo.Pack> out = new ArrayList<>(all.size() - skip);
        for (ResourcePackInfo.Pack p : all) {
            if (p.popped() && skip > 0) skip--;
            else out.add(p);
        }
        return out;
    }

    private static String packSummary(List<ResourcePackInfo.Pack> packs) {
        if (packs.isEmpty()) return !ResourcePackInfo.present ? "None pushed" : ResourcePackInfo.required ? "Required" : "Optional";
        if (packs.size() == 1) return (packs.getFirst().required() ? "Required" : "Optional") + (packs.getFirst().popped() ? ", removed" : "");
        int required = 0;
        for (ResourcePackInfo.Pack p : packs) if (p.required()) required++;
        return plural(packs.size(), "pack", "packs") + ", " + required + " required";
    }

    private static void packRows(ServerInfoScreen s, Doc d, int w, int kw, String packUrl, String hash, String prompt, ResourcePackInfo.Pack p) {
        int gap = kvGap();
        List<Doc.Line> rp = new ArrayList<>();
        String url = packUrl != null && !packUrl.isBlank() ? packUrl : null;
        Doc.Line ul = next(rp, "URL", kw);
        if (url == null) {
            ul.t("unknown", Paint.sub());
        } else {
            ul.link(kw, url, () -> s.copy(url));
            ul.last().max(w - kw).key(url).tip(List.of(Paint.c(url), Paint.c("Click to copy")));
        }
        if (hash != null && !hash.isBlank()) {
            List<Component> tip = List.of(Paint.c(hash), Paint.c("Click to copy"));
            int iw = Paint.iconW(Paint.COPY);
            Doc.Line hl = next(rp, "SHA-1", kw).t(hash, Paint.sub(), Math.max(20, w - kw - (iw > 0 ? gap + iw : 0)));
            hl.last().on(() -> s.copy(hash)).key(hash).tip(tip);
            if (iw > 0) {
                hl.sp(gap).i(Paint.COPY, Paint.dim());
                hl.last().on(() -> s.copy(hash)).key(hash).tip(tip);
            }
        }
        if (prompt != null && !prompt.isBlank()) next(rp, "Prompt", kw).t(prompt, Paint.sub(), w - kw);
        if (p != null) {
            String source = switch (p.origin()) {
                case PROXY -> "Proxy";
                case BACKEND -> "Backend";
                case UNKNOWN -> "Unknown";
            };
            next(rp, "Source", kw).t(source, Paint.sub(), w - kw).last().tip(List.of(Paint.c(source), Paint.c(cap(p.originWhy()))));
            String loaded = packLoaded(p);
            next(rp, "Loaded", kw).t(loaded, Paint.sub(), w - kw).last().tip(List.of(Paint.c(cap(loaded)), Paint.c(cap(packSent(p)))));
        }
        for (Doc.Line l : rp) d.add(l);
        edge(d, rp.getFirst(), rp.getLast());
    }

    private static String packSeconds(long ms) {
        return ms < 60_000L ? String.format(Locale.ROOT, "%.1f s", ms / 1000.0)
                : ms / 60_000L + " min " + ms / 1000L % 60L + " s";
    }

    private static String packSent(ResourcePackInfo.Pack p) {
        String at = p.phase() == ResourcePackInfo.Phase.CONFIGURATION ? "sent before joining (configuration)"
                : p.preLogin() ? "sent while joining, before the server's join packet"
                : p.pushedAfterJoin() >= 0L ? "sent after joining, " + packSeconds(p.pushedAfterJoin()) + " in" : "sent after joining";
        return p.jumpsBefore() > 0 ? at + ", after a server jump" : at;
    }

    private static String packLoaded(ResourcePackInfo.Pack p) {
        String out = switch (p.state()) {
            case WAITING -> "not yet, " + packSent(p);
            case DECLINED -> "declined, " + packSent(p);
            case FAILED -> "failed, " + packSent(p);
            case LOADED -> {
                String at = p.loadedPhase() == ResourcePackInfo.Phase.CONFIGURATION ? "before joining (configuration)"
                        : p.loadedAfterJoin() < 0L ? "while joining, before the server's join packet"
                        : "after joining, " + packSeconds(p.loadedAfterJoin()) + " in";
                yield p.jumpsBefore() > 0 ? at + ", after a server jump" : at;
            }
        };
        return p.popped() ? out + ", removed since" : out;
    }

    private static Doc.Box route(List<Node> nodes, int w) {
        boolean smog = HontunTheme.smog();
        int n = nodes.size(), dot = Paint.routeDot(), gap = 12;
        int markY = smog ? 14 : 13, nameY = smog ? 27 : 26, subY = smog ? 43 : 40;
        int[] xs = new int[n], kind = new int[n];
        float[] an = new float[n];
        int maxL = 0;
        for (int i = 0; i < n; i++) {
            xs[i] = n > 1 ? Math.round(i * (w - dot) / (float) (n - 1)) : 0;
            kind[i] = i == 0 ? 0 : i == n - 1 ? 2 : 1;
            an[i] = kind[i] == 0 ? 0 : kind[i] == 2 ? w : xs[i] + dot / 2f;
            maxL = Math.max(maxL, nodes.get(i).lines().size());
        }
        int levels = 2 + maxL;
        Component[][] tx = new Component[n][levels];
        int[][] cl = new int[n][levels], lw = new int[n][levels];
        for (int i = 0; i < n; i++) {
            Node nd = nodes.get(i);
            tx[i][0] = smog ? Paint.sm(nd.role()) : Paint.c(nd.role());
            cl[i][0] = Paint.dim();
            tx[i][1] = Paint.lg(nd.name());
            cl[i][1] = Paint.text();
            for (int j = 0; j < nd.lines().size(); j++) {
                tx[i][2 + j] = Paint.c(nd.lines().get(j));
                cl[i][2 + j] = Paint.sub();
            }
        }
        for (int lv = 0; lv < levels; lv++) {
            List<Integer> at = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if (tx[i][lv] == null) continue;
                lw[i][lv] = Paint.w(tx[i][lv]);
                at.add(i);
            }
            for (int j = 1; j < at.size(); j++) {
                int p = at.get(j - 1), q = at.get(j);
                float rp = kind[p] == 0 ? lw[p][lv] : lw[p][lv] / 2f, lq = kind[q] == 2 ? lw[q][lv] : lw[q][lv] / 2f;
                float room = an[q] - an[p] - gap;
                if (rp + lq <= room) continue;
                float np = rp <= room / 2 ? rp : lq <= room / 2 ? room - lq : room / 2;
                lw[p][lv] = Math.max(0, (int) Math.floor(kind[p] == 0 ? np : 2 * np));
                lw[q][lv] = Math.max(0, Math.min(lw[q][lv], (int) Math.floor(kind[q] == 2 ? room - np : 2 * (room - np))));
            }
        }
        int bh = (maxL > 0 ? subY + 12 * (maxL - 1) : nameY) + 8;
        Doc.Box box = new Doc.Box(bh);
        box.put(new Doc.Route(xs, dot, markY), 0, 0, w);
        for (int i = 0; i < n; i++) {
            for (int lv = 0; lv < levels; lv++) {
                Component c = tx[i][lv];
                if (c == null) continue;
                int full = Paint.w(c), sw = Math.min(full, lw[i][lv]);
                if (sw <= 0) continue;
                int x = kind[i] == 0 ? 0 : kind[i] == 2 ? w - sw : Math.round(an[i] - sw / 2f);
                int ly = lv == 0 ? 0 : lv == 1 ? nameY : subY + 12 * (lv - 2);
                Doc.Span sp = new Doc.Span(0, c, cl[i][lv], Doc.TEXT).dy(0);
                if (sw < full) sp.max(sw);
                Doc.Line l = new Doc.Line(10).add(sp);
                box.put(l, x, ly, sw);
            }
        }
        return box;
    }

    private static List<ServerSoftware.Evidence> proxySignals(ServerSoftware.Result r, String proxy) {
        List<String> names = new ArrayList<>();
        for (String part : proxy.split(" \\+ ")) {
            for (String k : part.split(" or ")) {
                String t = k.strip();
                if (!t.isEmpty() && !t.equals("a proxy")) names.add(t);
            }
        }
        List<ServerSoftware.Evidence> out = new ArrayList<>();
        if (r == null || r.evidence() == null) return out;
        for (ServerSoftware.Evidence e : r.evidence()) {
            if (e == null || "ping".equals(e.signal())) continue;
            String dt = e.detail() == null ? "" : e.detail();
            boolean hit = false;
            if (names.isEmpty()) {
                hit = PROXY_WORD.matcher(dt).find();
            } else {
                for (String nm : names) {
                    if (dt.contains(nm)) {
                        hit = true;
                        break;
                    }
                }
            }
            if (hit) out.add(e);
        }
        return out;
    }

    private static void proxyRows(Doc d, ServerSoftware.Result r, List<ServerSoftware.Evidence> sig, int w) {
        boolean rules = rowRules();
        int rh = t6(16, 14, 16, 16, 16, 14), mx = t6(10, 9, 8, 9, 10, 8), px = t6(24, 22, 20, 22, 22, 20);
        int dx = t6(86, 96, 88, 96, 92, 104), right = w - t6(10, 12, 10, 10, 12, 10), og = t6(4, 4, 4, 4, 4, 6);
        Doc.Well pw = null;
        if (!va()) {
            pw = new Doc.Well();
            pw.style = Doc.PANEL;
        }
        for (int i = 0; i < sig.size(); i++) {
            ServerSoftware.Evidence e = sig.get(i);
            int cls = evClass(r, e);
            boolean co = e.origin() == ServerSoftware.Origin.CONFIG;
            Doc.Line l = new Doc.Line(rh);
            l.icon(mx, cls == 1 ? Paint.PLUS : cls == 2 ? Paint.MINUS : Paint.DOT, cls == 1 ? Paint.green() : cls == 2 ? Paint.red() : Paint.dim());
            l.pill(px, or(e.signal(), "?"), Paint.NORMAL, -1);
            Component ot = sm(co ? "config" : "built-in");
            int ic = co ? Paint.CONFIG : Paint.BUILTIN, iw = Paint.iconW(ic), tw = iw > 0 ? iw + og : 0;
            int ox = right - Paint.w(ot) - tw;
            l.text(dx, or(e.detail(), ""), Paint.sub(), Math.max(20, ox - 8 - dx));
            if (iw > 0) l.icon(ox, ic, Paint.dim());
            l.add(new Doc.Span(ox + tw, ot, Paint.dim(), Doc.TEXT));
            Doc.Tip tip = evTip(e, cls);
            l.tip = tip;
            for (Doc.Span sp : l.spans) if (sp.clips()) sp.tip(tip);
            l.rowBg = true;
            if (rules && i > 0) {
                l.ruleTop = true;
                l.ruleX = 8;
                l.ruleW = w - 16;
            }
            if (pw != null) pw.inner.add(l);
            else d.add(l);
        }
        if (pw != null) d.add(pw);
    }

    private static void channels(ServerInfoScreen s, Doc d, int w) {
        List<String> names = channelNames();
        Doc.Line head = title(d, "Plugin channels");
        if (names.isEmpty()) {
            sum(head, "None detected", Paint.dim(), 0);
            gapTo(d, emptyTop());
            d.add(Doc.note("The server registered no plugin channels."));
            return;
        }
        int sent = 0;
        for (String n : names) if (ChannelPayloads.has(n)) sent++;
        sum(head, names.size() + " channels, " + sent + " sent data", Paint.dim(), w - head.cur);
        gapTo(d, blockTop());
        Set<String> reg;
        synchronized (ChannelFetcher.REGISTERED) { reg = new LinkedHashSet<>(ChannelFetcher.REGISTERED); }
        for (String n : names) {
            ChannelPayloads.Payload p = ChannelPayloads.get(n);
            Doc.Line l = new Doc.Line(13).text(0, n, n.equals(s.channel) ? Paint.acc() : Paint.sub(), w - 200);
            if (reg.contains(n)) l.pill(w - 190, "registered", Paint.dim());
            if (p != null) l.pill(w - 110, p.length() + " B" + (p.count() > 1 ? " ×" + p.count() : ""), Paint.acc());
            if (p != null) l.row(() -> { s.channel = n; s.rebuild(); });
            l.active = n.equals(s.channel);
            l.rowBg = true;
            d.add(l);
        }
        if (ChannelPayloads.skipped() > 0) note(d, ChannelPayloads.skipped() + " payloads from channels over the " + ChannelPayloads.limit() + "-channel limit were not kept, their names are in " + Oversize.where(ChannelPayloads.OVERSIZE));
        ChannelPayloads.Payload sel = s.channel != null ? ChannelPayloads.get(s.channel) : null;
        if (sel == null) {
            note(d, sent == 0 ? "No channel has sent data, they are only registered." : "Click a channel that sent data to see it.");
            return;
        }
        d.add(new Doc.Sep(16, 2, Doc.RULE));
        String json = json(sel.data());
        List<String> lines = json != null ? List.of(json.split("\n")) : hex(sel.data());
        String all = String.join("\n", lines);
        int copyW = Math.max(btnW("Copy"), btnW("Copied")), saveW = btnW("Save to file");
        String info = sel.length() + " B" + (json != null ? ", JSON" : ", binary");
        int avail = w - copyW - 6 - saveW - 8 - 6 - Paint.w(Paint.c(info));
        Doc.Line h = new Doc.Line(12).at(0).t(sel.channel(), Paint.text(), Math.max(40, avail));
        h.sp(6).t(info, Paint.dim());
        d.add(h);
        byte[] raw = sel.data();
        String channel = sel.channel();
        d.buttons.add(new Doc.Btn("Copy", h, 0, copyW, () -> s.copy(all), Paint.COPY));
        d.buttons.add(new Doc.Btn("Save to file", h, copyW + 6, saveW,
                () -> s.saveChannel(channel, json != null ? json.getBytes(StandardCharsets.UTF_8) : raw, json != null), Paint.SAVE));
        d.add(new Doc.Gap(8));
        Doc.Well well = new Doc.Well();
        int max = Math.min(lines.size(), 200);
        for (int i = 0; i < max; i++) well.inner.add(new Doc.Line(10).text(0, lines.get(i), json != null ? Paint.text() : Paint.sub()));
        if (lines.size() > max) well.inner.add(new Doc.Line(10).text(0, (lines.size() - max) + " more lines, use Copy", Paint.dim()));
        d.add(well);
    }

    private static String json(byte[] data) {
        if (data == null) return null;
        String s = new String(data, StandardCharsets.UTF_8);
        int a = s.indexOf('{');
        int b = s.lastIndexOf('}');
        if (a < 0 || b <= a) return null;
        try {
            return new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString(s.substring(a, b + 1)));
        } catch (Throwable t) {
            return null;
        }
    }

    private static List<String> hex(byte[] data) {
        List<String> out = new ArrayList<>();
        if (data == null) return out;
        for (int off = 0; off < data.length; off += 16) {
            StringBuilder h = new StringBuilder(String.format(Locale.ROOT, "%08x  ", off));
            StringBuilder a = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                if (off + i < data.length) {
                    int b = data[off + i] & 0xFF;
                    h.append(String.format(Locale.ROOT, "%02x ", b));
                    a.append(b >= 32 && b < 127 ? (char) b : '.');
                } else {
                    h.append("   ");
                }
            }
            out.add(h + " |" + a + "|");
        }
        return out;
    }

    private static void cookies(ServerInfoScreen s, Doc d, int w) {
        List<CookieObserver.View> list = CookieObserver.list();
        Doc.Line head = title(d, "Cookies");
        if (list.isEmpty()) {
            sum(head, "None observed", Paint.dim(), 0);
            gapTo(d, emptyTop());
            d.add(Doc.note("The server has not tried to store or read any cookie on your client."));
            return;
        }
        int stored = 0, requested = 0;
        for (CookieObserver.View v : list) { if (v.stored()) stored++; if (v.requested()) requested++; }
        sum(head, stored + " stored, " + requested + " requested", Paint.dim(), w - head.cur);
        boolean m1 = HontunTheme.modern1(), rules = HontunTheme.smog() || HontunTheme.modern2();
        gapTo(d, emptyTop() - (Paint.vanilla() ? 2 : 0));
        int in = m1 ? 8 : HontunTheme.modern2() ? 12 : 10;
        Doc.Well well = m1 ? null : panel();
        for (int i = 0; i < list.size(); i++) {
            CookieObserver.View v = list.get(i);
            Doc.Line l = new Doc.Line(20);
            String st = v.size() + " B stored";
            int x = w - in, rx = -1, sx = -1;
            if (v.requested()) rx = x -= Paint.pillWidth(Paint.c("requested"), Paint.NORMAL, -1);
            if (v.stored()) sx = x -= (rx >= 0 ? 4 : 0) + Paint.pillWidth(Paint.c(st), Paint.NORMAL, -1);
            l.text(in, v.key(), Paint.sub(), Math.max(20, x - in - 8));
            if (sx >= 0) l.pill(sx, st, Paint.NORMAL, -1);
            if (rx >= 0) l.pill(rx, "requested", Paint.NORMAL, -1);
            if (v.hex() != null) l.tip = Doc.Tip.of(List.of(Paint.c(v.key()), Paint.c(v.hex().length() > 96 ? v.hex().substring(0, 96) + "…" : v.hex())));
            if (m1) {
                l.card = true;
                d.add(l);
                continue;
            }
            if (i > 0 && rules) {
                l.ruleTop = true;
                l.ruleX = 8;
            }
            l.rowBg = true;
            well.inner.add(l);
        }
        if (well != null) d.add(close(well));
        d.add(new Doc.Gap(6));
        if (CookieObserver.blocking()) {
            Doc.Line b = new Doc.Line(16).at(0).i(Paint.SHIELD, Paint.term() ? Paint.dim() : Paint.sub());
            if (Paint.iconW(Paint.SHIELD) > 0) b.sp(Paint.term() ? 6 : 5);
            d.add(b.t("Stores are blocked by HSpoofer", Paint.sub()));
        }
        Path f = CookieObserver.file();
        if (f != null) {
            Runnable openFile = () -> s.openFolder(f);
            Runnable openDir = () -> s.openFolder(f.getParent());
            Doc.Line l = new Doc.Line(16);
            int x = 0, fw = Paint.iconW(Paint.FILE), dw = Paint.iconW(Paint.FOLDER);
            if (fw > 0) {
                l.icon(x, Paint.FILE, Paint.acc()).last().on(openFile);
                x += fw + 5;
            }
            l.link(x, "Open file", openFile);
            x += Paint.w(Paint.c("Open file")) + t6(14, 14, 14, 14, 16, 18);
            if (dw > 0) {
                l.icon(x, Paint.FOLDER, Paint.acc()).last().on(openDir);
                x += dw + 4;
            }
            l.link(x, "hontun/cookies/" + f.getFileName(), openDir);
            l.last().tip(List.of(Paint.c("Show in folder"), Paint.c(f.toAbsolutePath().toString())));
            d.add(l);
        }
        d.add(new Doc.Gap(8));
        d.add(Doc.note("The server tried to store or read these cookies on your client."));
    }
}
