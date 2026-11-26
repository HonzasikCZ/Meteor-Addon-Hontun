package cz.honzasik.hontun.gui.screen.server;

import cz.honzasik.hontun.serverlog.AddressInfo;
import cz.honzasik.hontun.serverlog.DnsLookup;
import cz.honzasik.hontun.serverlog.LogModel;
import cz.honzasik.hontun.utils.HontunFlags;
import cz.honzasik.hontun.utils.HontunGeo;
import cz.honzasik.hontun.utils.HontunTheme;
import cz.honzasik.hontun.utils.ServerSoftware;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class PanelPages {
    static final int OVERVIEW = 0, NETWORK = 1, SERVERS = 2, HISTORY = 3;
    static final String[] TABS = {"Overview", "Network", "Servers", "History"};

    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);
    private static final DateTimeFormatter DAY_YEAR = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
    private static final int ROW = 18;
    private static final int MAX_CHANGES = 6;

    private PanelPages() {}

    record Seg(String text, int color) {}

    private record Cell(String label, String value, String suffix, int level, String quality, String why, String origin) {}

    private record Node(String label, String value, int kind) {}

    static Doc build(ServerListPanel p, int tab, int w) {
        Doc d = new Doc();
        switch (tab) {
            case NETWORK -> network(p, d, w);
            case SERVERS -> servers(p, d, w);
            case HISTORY -> history(p, d, w);
            default -> overview(p, d, w);
        }
        d.layout(w, 10);
        return d;
    }

    private static ZonedDateTime time(long ms) {
        return Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault());
    }

    static String day(long ms) {
        ZonedDateTime t = time(ms);
        return t.format(t.getYear() == LocalDate.now().getYear() ? DAY : DAY_YEAR);
    }

    static String ago(long ms) {
        long s = Math.max(0L, (System.currentTimeMillis() - ms) / 1000L);
        if (s < 60) return "just now";
        if (s < 3600) return s / 60 + " min ago";
        return s < 86400 ? s / 3600 + " h ago" : day(ms);
    }

    static String duration(long ms) {
        long s = Math.max(0L, ms / 1000L);
        if (s < 60) return s + " s";
        if (s < 3600) return s / 60 + " min";
        long m = s % 3600 / 60;
        return s / 3600 + " h" + (m > 0 ? " " + m + " min" : "");
    }

    private static String number(int n) {
        return String.format(Locale.ROOT, "%,d", n).replace(',', ' ');
    }

    private static String plain(Component c) {
        if (c == null) return "";
        String s = ChatFormatting.stripFormatting(c.getString());
        return s == null ? "" : s.strip();
    }

    static String country(String cc) {
        if (cc == null || cc.isBlank()) return "";
        String n = Locale.of("", cc.toUpperCase(Locale.ROOT)).getDisplayCountry(Locale.ENGLISH);
        return n.isBlank() ? cc.toUpperCase(Locale.ROOT) : n;
    }

    static boolean online(ServerData s) {
        return s != null && (s.state() == ServerData.State.SUCCESSFUL || s.state() == ServerData.State.INCOMPATIBLE);
    }

    static boolean down(ServerData s) {
        if (s == null || online(s)) return false;
        return s.state() == ServerData.State.UNREACHABLE || s.motd != null && s.motd.getContents() instanceof TranslatableContents t
                && t.getKey().startsWith("multiplayer.status.cannot_");
    }

    private static int width(String s) { return Paint.w(Paint.c(s)); }

    private static int filled(Doc d) {
        int h = 0;
        for (Doc.Item it : d.full) h += it.h;
        return h;
    }

    private static Doc.Line sec(Doc d, int w, String title, String sub) {
        Doc.Line l = ServerPages.head(title);
        if (sub != null && !sub.isEmpty()) ServerPages.sum(l, sub, Paint.dim(), w - l.cur);
        d.add(l);
        d.add(new Doc.Gap(4));
        return l;
    }

    private static void counted(Doc d, int w, String title, int n, String sub) {
        if (Paint.term()) sec(d, w, title, n + ", " + sub);
        else sec(d, w, title + " " + n, sub);
    }

    private static void gap(Doc d) { d.add(new Doc.Gap(10)); }

    private static Doc.Box card(int h) {
        Doc.Box b = new Doc.Box(h);
        b.style = Doc.PANEL;
        return b;
    }

    private static Doc.Line fixed(Doc.Line l) {
        for (Doc.Span s : l.spans) if (s.dy == Integer.MIN_VALUE) s.dy = 0;
        return l;
    }

    private static Doc.Line kv(Doc.Box box, int y, int w, String key, int kw) {
        Doc.Line l = new Doc.Line(ROW).text(0, key, Paint.dim(), kw - 4);
        box.put(l, 10, y, w - 20);
        return l.at(kw);
    }

    private static void kv(Doc.Box box, int y, int w, String key, int kw, String value, String extra) {
        int in = w - 20, gap = ServerPages.kvGap();
        Doc.Line l = kv(box, y, w, key, kw);
        boolean more = extra != null && !extra.isEmpty();
        int keep = more ? Math.min(width(extra), (in - kw) / 2) + gap : 0;
        l.t(value, Paint.sub(), Math.max(20, in - kw - keep));
        if (more && in - l.cur - gap > 16) l.sp(gap).t(extra, Paint.dim(), in - l.cur);
    }

    private static void right(Doc.Line l, int edge, String s, int color) {
        l.text(edge - width(s), s, color);
    }

    private static void copy(Doc.Line l, ServerListPanel p, String value, int max) {
        int iw = Paint.iconW(Paint.COPY), g = iw > 0 ? 4 : 0;
        List<Component> tip = List.of(Paint.c(value), Paint.c("Click to copy"));
        String key = "panel:" + value;
        l.t(value, Paint.sub(), Math.max(20, max - iw - g));
        l.last().on(() -> p.copy(value)).key(key).tip(tip);
        if (iw > 0) {
            l.sp(g).i(Paint.COPY, Paint.dim());
            l.last().on(() -> p.copy(value)).key(key).tip(tip);
        }
    }

    private static final class Flag extends Doc.Item {
        private final String cc;

        Flag(String cc) {
            this.cc = cc;
            this.h = HontunFlags.H;
        }

        @Override
        void draw(GuiGraphicsExtractor g, int ox, int oy, int mx, int my) {
            HontunFlags.draw(g, cc, ox + x, oy + y);
        }
    }

    private static void overview(ServerListPanel p, Doc d, int w) {
        if (p.log.visited()) lastVisit(p, d, w);
        else if (p.log.loaded()) notVisited(d, w);
        int kw = ServerPages.t6(72, 72, 72, 72, 72, 78), ch = 14 + 5 * ROW + 8;
        if (p.wide) {
            int cw = (w - 10) / 2;
            Doc.Box row = new Doc.Box(ch);
            row.put(pingCard(p, cw, ch, kw), 0, 0, cw);
            row.put(addressCard(p, w - cw - 10, ch, kw), cw + 10, 0, w - cw - 10);
            d.add(row);
        } else {
            d.add(pingCard(p, w, ch, kw));
            d.add(new Doc.Gap(8));
            d.add(addressCard(p, w, ch, kw));
        }
        gap(d);
        int hh = p.wide ? Math.max(90, p.viewH - filled(d)) : 90;
        d.add(new PingBars(p.log.pings(), hh));
    }

    private static void notVisited(Doc d, int w) {
        Doc.Para text = new Doc.Para(Paint.c("Join once and Hontun logs what it finds here: software, real version, anticheat, plugins, and every server you jump to."),
                Paint.sub(), 0);
        text.measure(w - 24);
        Doc.Box box = card(34 + text.h + 8);
        Doc.Line big = new Doc.Line(18);
        String t = "Not visited yet";
        if (!Paint.term() && Paint.bigWidth(Paint.c(t)) <= w - 24) big.big(0, t, Paint.text());
        else big.text(0, t, Paint.text());
        box.put(big, 12, 9, w - 24);
        box.put(text, 12, 33, w - 24);
        d.add(box);
        gap(d);
    }

    private static void lastVisit(ServerListPanel p, Doc d, int w) {
        LogModel.Visit v = p.log.lastVisit();
        LogModel.ServerEntry e = p.log.lastServer();
        ZonedDateTime end = time(v.end);
        String when = p.wide ? end.format(DAY_YEAR) + " " + end.format(HM) : day(v.end);
        sec(d, w, "Last visit", when + (e != null ? ", " + e.label() : "") + ", " + duration(v.duration()));
        int bh = 152, cw = w / 2;
        Doc.Box band = card(bh);
        Cell[] cells = cells(e);
        for (int i = 0; i < 4; i++) cell(band, cells[i], i % 2 * cw, i / 2 * 76, cw);
        band.vr = new int[]{cw};
        band.vrTop = 8;
        band.put(new Doc.Sep(2, 0, Doc.RULE), 8, 76, w - 16);
        d.add(band);
        gap(d);
    }

    private static Cell[] cells(LogModel.ServerEntry e) {
        if (e == null) {
            String none = "Not enough signals";
            return new Cell[]{new Cell("Software", "Unknown", null, 0, none, "", ""), new Cell("Proxy", "Unknown", null, 0, none, "", ""),
                    new Cell("Real version", "Unknown", null, 0, none, "", ""), new Cell("Anticheat", "Unknown", null, 0, none, "", "")};
        }
        boolean soft = LogModel.softwareKnown(e.software);
        Cell software = new Cell("Software", soft ? e.software : "Unknown", null, soft ? LogModel.rank(e.confidence) : 0,
                soft && !e.confidence.isBlank() ? ServerPages.conf(e.confidence) : "Not enough signals",
                e.family.isBlank() ? "" : ServerPages.cap(e.family) + " structure", e.nameOrigin);

        String[] hops = e.proxy.isBlank() ? new String[0] : e.proxy.split(" \\+ ");
        Cell proxy;
        if (hops.length > 0) {
            String why = hops.length > 1 ? "Then " + String.join(", ", List.of(hops).subList(1, hops.length)) : e.rewritesBrand ? "Rewrites the brand" : "";
            proxy = new Cell("Proxy", ServerPages.cap(hops[0]), null, 3, "At least " + ServerPages.plural(hops.length, "hop", "hops"), why, "");
        } else {
            proxy = new Cell("Proxy", e.proxyKnown ? "None" : "Unknown", null, 0, e.proxyKnown ? "Not detected" : "Not known yet", "", "");
        }

        String range = e.version.strip(), suffix = null;
        boolean known = !range.isEmpty() && !"unknown".equalsIgnoreCase(range);
        if (range.endsWith(" or newer") && range.length() > 9) {
            range = range.substring(0, range.length() - 9);
            suffix = "or newer";
        }
        Cell version = new Cell("Real version", known ? ServerPages.cap(range) : "Unknown", known ? suffix : null, known ? e.versionExact ? 3 : 2 : 0,
                known && !e.versionWhy.isBlank() ? ServerPages.cap(e.versionWhy) : "Not enough signals",
                known ? e.versionExact ? "Exact version" : "A range, not exact" : "", "");

        boolean named = LogModel.named(e.anticheat), none = LogModel.NONE_FOUND.equals(e.anticheat);
        String ac = named && !e.anticheatConfidence.isBlank() ? ServerPages.cap(e.anticheatConfidence) : named ? "Detected" : none ? "No signs of one" : "Not enough signals";
        Cell anticheat = new Cell("Anticheat", named ? e.anticheat : none ? LogModel.NONE_FOUND : "Unknown", null,
                named ? LogModel.rank(e.anticheatConfidence) : 0, ac, named && !e.anticheatSource.isBlank() ? "Detected via " + e.anticheatSource : "", "");
        return new Cell[]{software, proxy, version, anticheat};
    }

    private static void cell(Doc.Box band, Cell c, int x, int y, int cw) {
        int tx = x + 12, in = Math.max(20, cw - 16);
        boolean term = Paint.term();
        Doc.Line lab = new Doc.Line(10);
        if (HontunTheme.smog()) lab.small(0, c.label, Paint.dim());
        else lab.text(0, c.label, Paint.dim());
        fixed(lab);
        if (!c.origin.isBlank()) {
            boolean cfg = "config".equals(c.origin);
            int icon = cfg ? Paint.CONFIG : Paint.BUILTIN, pw = Paint.pillWidth(Paint.c(c.origin), Paint.NORMAL, icon), pr = cw - 22;
            if (lab.extent() + 6 + pw <= pr) lab.pill(pr - pw, c.origin, Paint.NORMAL, icon);
        }
        band.put(lab, tx, y + 8, in);
        band.put(ServerPages.bandValue(c.value, c.suffix, in), tx, y + (term ? 22 : 20), in);
        Doc.Line q = new Doc.Line(10).at(0);
        if (c.level > 0) q.m(c.level).sp(Paint.meterGap());
        String text = c.quality;
        int cut = text.toLowerCase(Locale.ROOT).lastIndexOf(" confidence");
        if (cut > 0 && q.cur + width(text) > in) text = text.substring(0, cut);
        q.t(text, Paint.sub(), Math.max(20, in - q.cur));
        band.put(fixed(q), tx, y + 42, in);
        if (!c.why.isEmpty()) band.put(fixed(new Doc.Line(10).text(0, c.why, Paint.dim(), in)), tx, y + 56, in);
    }

    private static Doc.Box pingCard(ServerListPanel p, int w, int h, int kw) {
        ServerData s = p.data;
        LogModel.Ping last = p.log.lastPing();
        boolean on = online(s);
        int vm = w - 20 - kw;
        Doc.Box c = card(h);
        Doc.Line head = ServerPages.head("Ping");
        if (!on) ServerPages.sum(head, last != null ? "last answer " + ago(last.at()) : down(s) ? "no answer yet" : "waiting for an answer",
                Paint.dim(), w - 20 - head.cur);
        c.put(head, 10, 8, w - 20);

        int online = -1, max = -1;
        if (on) {
            if (s.players != null) {
                online = s.players.online();
                max = s.players.max();
            }
        } else if (last != null) {
            online = last.online();
            max = last.max();
        }
        boolean any = on || last != null;
        String unknown = "unknown";
        Doc.Line pl = kv(c, 20, w, "Players", kw);
        pl.t(online >= 0 ? number(online) + " / " + number(max) : any ? "hidden" : unknown, Paint.sub(), vm);
        if (online >= 0 && max > 0 && w >= 260 && w - 20 - pl.cur > 74) {
            int x = w - 20 - 62, fill = Math.round(62 * Math.min(1f, online / (float) max));
            pl.box(x, 62, 4, PanelPaint.meterBack());
            if (fill > 0) pl.box(x, fill, 4, PanelPaint.meterFill());
        }

        String version = on ? LogModel.clip(plain(s.version)) : last != null ? last.version() : "";
        kv(c, 20 + ROW, w, "Versions", kw).t(version.isBlank() ? unknown : version, Paint.sub(), vm);

        int proto = on ? s.protocol : last != null ? last.protocol() : -1, mine = ServerSoftware.nativeProtocol();
        kv(c, 20 + 2 * ROW, w, "Protocol", kw).t(!any ? unknown : proto == mine ? proto + ", same as yours" : proto + ", yours is " + mine, Paint.sub(), vm);

        int ms = on && s.ping >= 0 ? (int) Math.min(s.ping, Integer.MAX_VALUE) : last != null ? last.ms() : -1;
        int lo = p.log.pingMin(), hi = p.log.pingMax();
        Doc.Line ll = kv(c, 20 + 3 * ROW, w, "Latency", kw);
        if (ms < 0) {
            ll.t(unknown, Paint.sub(), vm);
        } else {
            ll.t(ms + " ms", Paint.sub(), vm);
            if (lo >= 0) ll.sp(ServerPages.kvGap()).t("min " + Math.min(lo, ms) + ", max " + Math.max(hi, ms), Paint.dim(), Math.max(20, w - 20 - ll.cur));
        }
        kv(c, 20 + 4 * ROW, w, "Secure chat", kw).t(last == null ? unknown : last.secureChat() ? "Enforced" : "Not enforced", Paint.sub(), vm);
        return c;
    }

    private static Doc.Box addressCard(ServerListPanel p, int w, int h, int kw) {
        AddressInfo.View a = p.addr;
        int in = w - 20, vm = in - kw;
        Doc.Box c = card(h);
        Doc.Line head = ServerPages.head("Address");
        if (a.loading()) ServerPages.sum(head, "looking up", Paint.dim(), in - head.cur);
        else if (a.failed()) ServerPages.sum(head, "did not resolve", Paint.dim(), in - head.cur);
        c.put(head, 10, 8, in);
        String wait = a.loading() ? "…" : "unknown";
        Doc.Line ip = kv(c, 20, w, "IP", kw);
        if (a.ip().isEmpty()) ip.t(wait, Paint.sub(), vm);
        else copy(ip, p, a.ip(), vm);
        countryRow(c, 20 + ROW, w, kw, a, wait);
        kv(c, 20 + 2 * ROW, w, "Host", kw).t(a.org().isEmpty() ? geoOff(wait) : a.org(), Paint.sub(), vm);
        protection(kv(c, 20 + 3 * ROW, w, "Protection", kw), a);
        String srv = a.loading() ? "…" : a.srv() != null ? a.srv() : a.literal() ? "none, IP address" : a.port() == 25565 ? "none, default port" : "none, port " + a.port();
        kv(c, 20 + 4 * ROW, w, "SRV record", kw).t(srv, Paint.sub(), vm);
        return c;
    }

    private static String geoOff(String wait) {
        return !HontunGeo.enabled() ? "flags are off" : Paint.vanilla() ? "not looked up" : wait;
    }

    private static void countryRow(Doc.Box c, int y, int w, int kw, AddressInfo.View a, String wait) {
        Doc.Line l = kv(c, y, w, "Country", kw);
        if (a.country().isEmpty()) {
            l.t(geoOff(wait), Paint.sub(), w - 20 - kw);
            return;
        }
        if (HontunFlags.has(a.country())) {
            c.put(new Flag(a.country()), 10 + kw, y + 5, HontunFlags.W);
            l.sp(HontunFlags.W + 5);
        }
        l.t(country(a.country()), Paint.sub(), Math.max(20, w - 20 - l.cur));
    }

    private static void protection(Doc.Line l, AddressInfo.View a) {
        AddressInfo.Protection p = a.protection();
        if (p.shield()) l.p(p.label(), Paint.ACC, Paint.SHIELD);
        else if (p == AddressInfo.Protection.NONE) l.p("None detected", Paint.DIM, Paint.SHIELD);
        else l.t(a.loading() ? "…" : "unknown", Paint.sub());
    }

    private static final class PingBars extends Doc.Item {
        private final List<LogModel.Ping> pings;
        private final int peak, avg;
        private final Doc.Line head, foot;

        PingBars(List<LogModel.Ping> pings, int h) {
            this.pings = pings;
            this.h = h;
            long sum = 0;
            int top = 0;
            for (LogModel.Ping q : pings) {
                sum += q.ms();
                top = Math.max(top, q.ms());
            }
            peak = top;
            avg = pings.isEmpty() ? 0 : (int) Math.round(sum / (double) pings.size());
            head = ServerPages.head("Ping history");
            foot = new Doc.Line(10);
            if (pings.isEmpty()) foot.text(0, "No answers logged yet", Paint.dim());
            else foot.text(0, pings.size() == 1 ? "last refresh" : "last " + pings.size() + " refreshes", Paint.dim());
        }

        @Override
        void measure(int width) {
            head.x = 10;
            head.y = 8;
            head.w = width - 20;
            foot.x = 10;
            foot.y = h - 16;
            foot.w = width - 20;
            if (head.spans.size() > 1 || pings.isEmpty()) return;
            String a = "avg " + avg + " ms", b = "peak " + peak;
            int bw = width(b), aw = width(a), bx = width - 20 - bw, ax = bx - 4 - aw;
            if (ax <= head.cur) return;
            head.text(ax, a, Paint.sub());
            head.text(bx, b, Paint.dim());
        }

        private int max() { return Math.max(60, Math.round(peak * 1.15f)); }

        private int slot() {
            return Math.min(11, Math.max(2, (w - 20) / Math.max(1, pings.size()) - 2)) + 2;
        }

        private int left(int i) {
            int s = slot(), n = pings.size();
            return x + w - 10 - n * s + 2 + i * s;
        }

        @Override
        void draw(GuiGraphicsExtractor g, int ox, int oy, int mx, int my) {
            int bx = ox + x, by = oy + y;
            Paint.panel(g, bx, by, w, h);
            head.draw(g, bx, by, -1, -1);
            foot.draw(g, bx, by, -1, -1);
            int base = by + h - 22, span = h - 54, bw = slot() - 2, max = max();
            if (span <= 4) return;
            for (int i = 0; i < 3; i++) {
                int gy = base - Math.round(span * i / 2f);
                g.fill(bx + 10, gy, bx + w - 10, gy + 1, PanelPaint.barGrid());
            }
            for (int i = 0; i < pings.size(); i++) {
                int ms = pings.get(i).ms(), bh = Math.max(3, Math.round(span * ms / (float) max)), lx = ox + left(i);
                boolean last = i == pings.size() - 1, high = ms > avg * 1.25f;
                int col = PanelPaint.bar(last, high);
                if (over(i, ox, oy, mx, my)) col = Paint.mix(col, 0xFFFFFFFF, 0.25f);
                g.fill(lx, base - bh, lx + bw, base, col);
            }
        }

        private boolean over(int i, int ox, int oy, int mx, int my) {
            int lx = ox + left(i), base = oy + y + h - 22;
            return mx >= lx - 1 && mx < lx + slot() - 1 && my >= base - (h - 54) && my < base;
        }

        @Override
        Doc.Span spanAt(int ox, int oy, int mx, int my) {
            for (int i = 0; i < pings.size(); i++) {
                if (!over(i, ox, oy, mx, my)) continue;
                LogModel.Ping q = pings.get(i);
                Doc.Span s = new Doc.Span(0, Component.empty(), 0, Doc.BOX);
                ZonedDateTime t = time(q.at());
                return s.tip(List.of(Paint.c(q.ms() + " ms"), Paint.c(day(q.at()) + " " + t.format(HM))));
            }
            return null;
        }

        @Override
        String copy() { return pings.isEmpty() ? "" : "Ping history avg " + avg + " ms, peak " + peak + " ms"; }
    }

    private static final class RouteDots extends Doc.Item {
        private final List<Node> nodes;

        RouteDots(List<Node> nodes) {
            this.nodes = nodes;
            this.h = nodes.size() * 26;
        }

        @Override
        void draw(GuiGraphicsExtractor g, int ox, int oy, int mx, int my) {
            int bx = ox + x, by = oy + y;
            for (int i = 0; i < nodes.size(); i++) {
                int ny = by + i * 26;
                if (i < nodes.size() - 1) g.fill(bx + 3, ny + 14, bx + 4, ny + 31, nodes.get(i + 1).kind == 0 ? PanelPaint.wire() : Paint.accLo());
                if (nodes.get(i).kind == 0) PanelPaint.hollow(g, bx, ny + 6);
                else Paint.routeMark(g, bx, ny + 6, true);
            }
        }
    }

    private static boolean shieldName(String s) {
        return DnsLookup.shieldName(s) != null;
    }

    private static void network(ServerListPanel p, Doc d, int w) {
        AddressInfo.View a = p.addr;
        boolean shield = a.protection().shield();
        sec(d, w, "Route", a.loading() ? "looking up" : a.failed() ? "the name did not resolve" : null);
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node("Your address", a.hostname(), 1));
        if (a.srv() != null) nodes.add(new Node("SRV record", a.srv(), shieldName(a.srv().replaceAll(":\\d+$", "")) ? 2 : 1));
        for (String c : a.cnames()) nodes.add(new Node("DNS alias", c, shieldName(c) ? 2 : 1));
        if (!a.ip().isEmpty() && !(a.literal() && !shield)) {
            String more = a.ips().size() > 1 ? ", +" + (a.ips().size() - 1) + " more" : "";
            nodes.add(shield ? new Node("Edge", a.ip() + ", " + a.protection().label() + more, 2) : new Node("Server", a.ip() + (a.org().isEmpty() ? "" : ", " + a.org()) + more, 1));
        }
        if (shield) nodes.add(new Node("Origin", "hidden", 0));
        else if (a.loading()) nodes.add(new Node("Server", "…", 0));
        int lx = 18, vx = 92;
        for (Node n : nodes) vx = Math.max(vx, lx + width(n.label) + 8);
        Doc.Box route = new Doc.Box(nodes.size() * 26 - 6);
        route.put(new RouteDots(nodes), 0, 0, 8);
        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            Doc.Line l = new Doc.Line(10).text(0, n.label, Paint.dim());
            l.text(vx - lx, n.value, n.kind == 0 ? Paint.dim() : n.kind == 2 ? Paint.acc() : Paint.text(), Math.max(20, w - vx));
            route.put(fixed(l), lx, i * 26 + 5, w - lx);
        }
        d.add(route);
        gap(d);

        int sup = 0, pw = 0;
        for (AddressInfo.Evidence e : a.evidence()) {
            if (e.supports()) sup++;
            pw = Math.max(pw, Paint.pillWidth(Paint.c(e.source()), Paint.NORMAL, -1));
        }
        if (!a.evidence().isEmpty()) {
            String label = a.protection().shield() ? a.protection().label() : "protection";
            sec(d, w, "How it was found", sup > 0 ? ServerPages.plural(sup, "signal", "signals") + " for " + label : "no protection found");
            int rw = 0;
            if (p.wide) {
                rw = width("info");
                for (AddressInfo.Evidence e : a.evidence()) if (e.supports()) rw = Math.max(rw, width(e.provider().label()));
                rw += 4;
            }
            int dx = 14 + pw + 8;
            for (AddressInfo.Evidence e : a.evidence()) {
                Doc.Line l = new Doc.Line(ROW);
                l.icon(2, e.supports() ? Paint.PLUS : Paint.DOT, e.supports() ? Paint.green() : Paint.dim());
                l.pill(14, e.source(), Paint.NORMAL, -1);
                l.text(dx, e.text(), Paint.sub(), Math.max(20, w - dx - (rw > 0 ? rw + 8 : 4)));
                if (rw > 0) right(l, w - 4, e.supports() ? e.provider().label() : "info", Paint.dim());
                l.ruleBottom = ServerPages.rowRules();
                d.add(l);
            }
            gap(d);
        }

        sec(d, w, "Address", null);
        int kw = ServerPages.t6(84, 84, 84, 84, 84, 90), vm = w - 20 - kw, y = 4;
        Doc.Box c = card(0);
        String wait = a.loading() ? "…" : "unknown";
        kv(c, y, w, "Hostname", kw).t(a.literal() ? "none, IP address" : a.hostname(), Paint.sub(), vm);
        Doc.Line ipl = kv(c, y += ROW, w, "IP and port", kw);
        if (a.ip().isEmpty()) ipl.t(wait, Paint.sub(), vm);
        else copy(ipl, p, (a.ip().indexOf(':') >= 0 ? "[" + a.ip() + "]" : a.ip()) + ":" + a.port(), vm);
        if (a.ips().size() > 1) {
            String rest = String.join(", ", a.ips().subList(1, a.ips().size()));
            kv(c, y += ROW, w, "Also", kw).t(rest, Paint.sub(), vm);
        }
        kv(c, y += ROW, w, "Reverse DNS", kw).t(a.ptr() != null ? a.ptr() : a.loading() ? "…" : "none", Paint.sub(), vm);
        countryRow(c, y += ROW, w, kw, a, wait);
        if (!a.org().isEmpty()) kv(c, y += ROW, w, "Host", kw, a.org(), a.asn() > 0 ? "AS" + a.asn() : null);
        protection(kv(c, y += ROW, w, "Protection", kw), a);
        c.h = y + ROW + 4;
        d.add(c);
        addressHistory(p, d, w);
    }

    private static void addressHistory(ServerListPanel p, Doc d, int w) {
        List<LogModel.Address> all = p.log.addresses();
        if (all.isEmpty()) return;
        gap(d);
        sec(d, w, "Address history", p.wide ? "what the name pointed to" : null);
        int n = all.size(), dw = 0, iw = 0;
        String[] dates = new String[n];
        for (int i = 0; i < n; i++) {
            LogModel.Address a = all.get(i);
            String from = day(a.from()), to = i == 0 ? "now" : day(i > 0 ? all.get(i - 1).from() : a.to());
            dates[i] = from.equals(to) ? from : from + " to " + to;
            dw = Math.max(dw, width(dates[i]));
            iw = Math.max(iw, Math.min(width(a.ip()), p.wide ? 110 : 92));
        }
        int in = w - 20, ix = dw + 10, px = ix + iw + 10;
        Doc.Box c = card(8 + n * 20 + 4);
        for (int i = 0; i < n; i++) {
            LogModel.Address a = all.get(i);
            AddressInfo.Protection own = AddressInfo.Protection.of(a.protection());
            AddressInfo.Protection after = i > 0 ? AddressInfo.Protection.of(all.get(i - 1).protection()) : AddressInfo.Protection.UNKNOWN;
            boolean shield = own.shield(), next = after.shield();
            Doc.Line l = new Doc.Line(10).text(0, dates[i], Paint.dim());
            l.text(ix, a.ip(), Paint.sub(), Math.min(iw, Math.max(20, in - ix)));
            if (a.ips().size() > 1) l.last().tip(a.ips().stream().map(s -> (Component) Paint.c(s)).toList());
            boolean moved = i > 0 && own != after && (shield || next);
            String note = i == 0 ? "current" : next && own != after ? "then moved to " + after.label() : !next && shield ? "then left " + own.label() : "then changed";
            int nc = moved ? Paint.yellow() : Paint.dim(), room = in - px, nw = p.wide ? width(note) + 8 : 0;
            String prot = shield ? own.label() + " edge" : own == AddressInfo.Protection.NONE ? "direct" : "unknown";
            if (nw > 0 && room - nw < width(prot)) {
                nw = 0;
            }
            if (room > 24) l.text(px, prot, shield ? Paint.acc() : Paint.dim(), room - nw);
            if (nw > 0) right(l, in, note, nc);
            c.put(fixed(l), 10, 8 + i * 20, in);
        }
        d.add(c);
    }

    private static final class RowBg extends Doc.Item {
        private final boolean active;

        RowBg(boolean active) {
            this.active = active;
            this.h = 22;
        }

        @Override
        void draw(GuiGraphicsExtractor g, int ox, int oy, int mx, int my) {
            PanelPaint.row(g, ox + x, oy + y, w, h, active, hit(ox, oy, mx, my));
        }
    }

    private static final class Wire extends Doc.Item {
        private final int top, bottom, arm;

        Wire(int top, int bottom, int arm) {
            this.top = top;
            this.bottom = bottom;
            this.arm = arm;
            this.h = 26;
        }

        @Override
        void draw(GuiGraphicsExtractor g, int ox, int oy, int mx, int my) {
            int bx = ox + x + 9, by = oy + y, c = PanelPaint.wire();
            g.fill(bx, by + top, bx + 1, by + bottom, c);
            if (arm > 0) g.fill(bx + 1, by + 10, ox + x + arm, by + 11, c);
        }
    }

    private static Set<Integer> roots(LogModel.View log) {
        Set<Integer> out = new HashSet<>();
        for (LogModel.Visit v : log.visits()) {
            List<Integer> path = v.path();
            if (!path.isEmpty() && path.getFirst() > 0) out.add(path.getFirst());
        }
        for (LogModel.ServerEntry e : log.servers()) {
            if (LogModel.JOIN.equals(e.how) || LogModel.TRANSFER.equals(e.how)) out.add(e.index);
        }
        return out;
    }

    static int selected(ServerListPanel p) {
        if (p.log.server(p.selServer) != null) return p.selServer;
        LogModel.ServerEntry e = p.log.lastServer();
        if (e != null) return e.index;
        return p.log.servers().isEmpty() ? 0 : p.log.servers().getFirst().index;
    }

    private static String about(LogModel.ServerEntry e) {
        String soft = LogModel.softwareKnown(e.software) ? e.software : "software not known";
        return e.version.isBlank() || "unknown".equalsIgnoreCase(e.version) ? soft : soft + " " + e.version;
    }

    private static String proxyOf(LogModel.View log) {
        LogModel.ServerEntry last = log.lastServer();
        if (last != null && !last.proxy.isBlank()) return last.proxy;
        for (LogModel.ServerEntry e : log.servers()) if (!e.proxy.isBlank()) return e.proxy;
        return "";
    }

    private static void servers(ServerListPanel p, Doc d, int w) {
        List<LogModel.ServerEntry> all = p.log.servers();
        sec(d, w, "Servers", "by how you got there");
        String proxy = proxyOf(p.log);
        boolean known = false;
        for (LogModel.ServerEntry e : all) known |= e.proxyKnown;
        Doc.Box pb = card(20);
        Doc.Line pl = new Doc.Line(20).at(0);
        if (Paint.iconW(Paint.CONNECTION) > 0) pl.i(Paint.CONNECTION, Paint.acc()).sp(7);
        if (!proxy.isBlank()) {
            String[] hops = proxy.split(" \\+ ");
            String name = ServerPages.cap(hops[0]);
            String low = name.toLowerCase(Locale.ROOT);
            pl.t(low.endsWith("proxy") ? name : name + " proxy", Paint.text()).sp(7);
            pl.t("at least " + ServerPages.plural(hops.length, "hop", "hops"), Paint.dim(), Math.max(20, w - 16 - pl.cur));
        } else {
            pl.t(known ? "No proxy detected" : "Proxy not known yet", Paint.text(), w - 16 - pl.cur);
        }
        pb.put(pl, 8, 0, w - 16);
        d.add(pb);
        d.add(new Doc.Gap(6));

        Set<Integer> roots = roots(p.log);
        List<LogModel.ServerEntry> order = new ArrayList<>();
        for (LogModel.ServerEntry e : all) if (roots.contains(e.index)) order.add(e);
        int first = order.size();
        for (LogModel.ServerEntry e : all) if (!roots.contains(e.index)) order.add(e);
        int sel = selected(p), lw = 0;
        for (LogModel.ServerEntry e : order) lw = Math.max(lw, width(e.label()));
        for (int i = 0; i < order.size(); i++) {
            LogModel.ServerEntry e = order.get(i);
            boolean child = first > 0 && i >= first, on = e.index == sel, more = i < order.size() - 1;
            int ind = child ? 16 : 0, rw = w - ind;
            Doc.Box box = new Doc.Box(more ? 26 : 22);
            if (child) box.put(new Wire(i == first ? -4 : 0, more ? 26 : 11, ind), 0, 0, ind);
            box.put(new RowBg(on), ind, 0, rw);
            Doc.Line l = new Doc.Line(22);
            String seen = ago(e.lastSeen);
            int tx = 10 + lw + 10, sx = rw - 8 - width(seen);
            l.text(10, e.label(), on ? Paint.text() : Paint.sub());
            l.text(tx, about(e), Paint.dim(), Math.max(20, sx - 8 - tx));
            l.text(sx, seen, Paint.dim());
            l.act = () -> p.selectServer(e.index);
            box.put(l, ind, 0, rw);
            d.add(box);
        }
        LogModel.ServerEntry e = p.log.server(sel);
        if (e != null) detail(p, d, w, e);
    }

    private static List<Integer> way(LogModel.View log, int index) {
        for (LogModel.Visit v : log.visits()) {
            List<Integer> path = v.path();
            int at = path.indexOf(index);
            if (at >= 0) return path.subList(0, at + 1);
        }
        return List.of(index);
    }

    private static void detail(ServerListPanel p, Doc d, int w, LogModel.ServerEntry e) {
        d.add(new Doc.Gap(8));
        Doc.Line t = new Doc.Line(20);
        int tw;
        if (Paint.term()) {
            t.text(0, e.label(), Paint.text());
            tw = width(e.label());
        } else {
            t.big(0, e.label(), Paint.text());
            tw = Paint.bigWidth(Paint.c(e.label()));
        }
        boolean soft = LogModel.softwareKnown(e.software);
        if (soft && tw + 8 + Paint.pillWidth(Paint.c(e.software), Paint.NORMAL, -1) <= w) t.pill(tw + 8, e.software, Paint.NORMAL, -1);
        d.add(t);
        d.add(new Doc.Gap(2));

        List<Integer> way = way(p.log, e.index);
        Doc.Line b = new Doc.Line(12).at(0);
        int tail = 0;
        for (int i : way) tail += width(" > ") + width(i > 0 ? "Server " + i : "?");
        boolean all = tail + 40 <= w;
        if (!all) tail = width(" > ") + width(e.label());
        b.t(p.addr.hostname(), Paint.dim(), Math.max(40, w - tail));
        List<Integer> show = all ? way : List.of(e.index);
        for (int i = 0; i < show.size(); i++) {
            b.t(" > ", Paint.dim());
            b.t(show.get(i) > 0 ? "Server " + show.get(i) : "?", i == show.size() - 1 ? Paint.acc() : Paint.dim());
        }
        d.add(b);
        d.add(new Doc.Gap(4));

        int kw = ServerPages.t6(88, 88, 88, 88, 88, 90), y = 4;
        Doc.Box c = card(0);
        kv(c, y, w, "Software", kw, soft ? e.software : "unknown", soft && !e.confidence.isBlank() ? e.confidence.toLowerCase(Locale.ROOT).replace(" confidence", "") : null);
        boolean ver = !e.version.isBlank() && !"unknown".equalsIgnoreCase(e.version);
        String why = e.versionWhy.strip();
        if (why.toLowerCase(Locale.ROOT).startsWith("from the ")) why = why.substring(9);
        kv(c, y += ROW, w, "Real version", kw, ver ? e.version : "unknown", ver ? why : null);
        boolean named = LogModel.named(e.anticheat);
        kv(c, y += ROW, w, "Anticheat", kw, named ? e.anticheat : LogModel.NONE_FOUND.equals(e.anticheat) ? "none found" : "unknown",
                named ? e.anticheatConfidence.toLowerCase(Locale.ROOT).replace(" confidence", "") : null);
        kv(c, y += ROW, w, "World", kw, e.world.isBlank() ? "unknown" : e.world, null);
        String how = LogModel.howText(e.how);
        kv(c, y += ROW, w, "Jump seen by", kw, how.isEmpty() ? "unknown" : how, null);
        for (LogModel.Pack pack : e.packs) {
            Doc.Line l = kv(c, y += ROW, w, "Resource pack", kw);
            String origin = "PROXY".equals(pack.origin()) ? "Proxy" : "BACKEND".equals(pack.origin()) ? "Backend" : "Unknown";
            Doc.Tip tip = Doc.Tip.of(List.of(Paint.c(origin), Paint.c(ServerPages.cap(ServerPages.or(pack.why(), "no reason logged"))),
                    Paint.c(ServerPages.cap(ServerPages.or(pack.loaded(), "")))));
            l.t(origin, Paint.sub()).last().tip(tip);
            if (!pack.loaded().isBlank()) l.sp(ServerPages.kvGap()).t(pack.loaded(), Paint.dim(), Math.max(20, w - 20 - l.cur)).last().tip(tip);
        }
        kv(c, y += ROW, w, "Visits", kw, String.valueOf(e.visits), e.firstSeen > 0 ? "first " + day(e.firstSeen) : null);
        c.h = y + ROW + 4;
        d.add(c);

        gap(d);
        if (e.pluginsAt <= 0) {
            sec(d, w, "Plugins", "not scanned on this server");
        } else {
            List<String> names = new ArrayList<>(e.plugins);
            names.sort(String.CASE_INSENSITIVE_ORDER);
            if (names.isEmpty()) sec(d, w, "Plugins", "none found");
            else counted(d, w, "Plugins", names.size(), "from " + day(e.pluginsAt));
            if (!names.isEmpty()) grid(d, w, names, p.wide ? 3 : 2, true);
        }
        if (!e.players.isEmpty()) {
            gap(d);
            counted(d, w, "Players seen", e.players.size(), "latest first");
            grid(d, w, e.players, p.wide ? 3 : 2, false);
        }
    }

    private static void grid(Doc d, int w, List<String> names, int cols, boolean flags) {
        int n = names.size(), per = (n + cols - 1) / cols, colw = (w - 16) / cols, iw = Paint.iconW(Paint.ANTICHEAT);
        Doc.Box box = card(per * 12 + 14);
        for (int r = 0; r < per; r++) {
            Doc.Line l = new Doc.Line(12);
            for (int c = 0; c < cols; c++) {
                int i = c * per + r;
                if (i >= n) continue;
                String s = names.get(i), cat = flags ? ServerPages.category(s) : "Other";
                boolean hot = !"Other".equals(cat);
                l.at(10 + c * colw).t(s, hot ? Paint.acc() : Paint.sub(), Math.max(20, colw - 8 - (hot && iw > 0 ? iw + 4 : 0)));
                if (!hot) continue;
                Doc.Tip tip = Doc.Tip.of(List.of(Paint.c(s), Paint.c(cat)));
                l.last().tip(tip);
                if (iw > 0) {
                    l.sp(4).i(Paint.ANTICHEAT, Paint.acc());
                    l.last().tip(tip);
                }
            }
            box.put(l, 0, 7 + r * 12, w);
        }
        d.add(box);
    }

    private static void history(ServerListPanel p, Doc d, int w) {
        List<LogModel.Visit> all = p.log.visits();
        int changed = 0;
        for (LogModel.Visit v : all) if (v.changed()) changed++;
        filters(p, d, w, all.size(), changed);

        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone), current = null;
        boolean none = true;
        for (LogModel.Visit v : all) {
            if (p.changesOnly && !v.changed()) continue;
            LocalDate date = time(v.start).toLocalDate();
            if (!date.equals(current)) {
                if (current != null) d.add(new Doc.Gap(4));
                current = date;
                String near = date.equals(today) ? "Today" : date.equals(today.minusDays(1)) ? "Yesterday" : "", when = day(v.start);
                if (near.isEmpty()) sec(d, w, when, null);
                else if (Paint.term()) sec(d, w, near, when);
                else sec(d, w, near + ", " + when, null);
            }
            visit(p, d, w, v);
            d.add(new Doc.Gap(4));
            none = false;
        }
        if (none) d.add(Doc.note(p.changesOnly ? "No visit changed anything yet." : "No visits yet."));
    }

    private static void filters(ServerListPanel p, Doc d, int w, int all, int changed) {
        boolean va = Paint.vanilla();
        int th = ServerPages.t6(16, 18, 16, 16, 24, 13), pad = ServerPages.t6(2, 3, 0, 0, 0, 0), g = ServerPages.t6(0, 2, 12, 2, 0, 6), x = pad;
        String[] names = {"All", "Changes"};
        int[] n = {all, changed};
        Doc.Line l = new Doc.Line(Math.max(th, va || HontunTheme.hvanilla() ? 20 : 16));
        int vw = 0;
        if (va) for (int i = 0; i < 2; i++) vw = Math.max(vw, Paint.tabContentW(Paint.c(names[i]), -1, Paint.c(String.valueOf(n[i]))) + 16);
        for (int i = 0; i < 2; i++) {
            Doc.Span sp = new Doc.Span(x, Paint.c(names[i]), Paint.sub(), Doc.TAB);
            sp.count = Paint.c(String.valueOf(n[i]));
            sp.sel = p.changesOnly == (i == 1);
            sp.sw = vw;
            boolean only = i == 1;
            sp.on(() -> p.showChanges(only));
            l.add(sp);
            x += sp.width() + (i == 0 ? g : 0);
        }
        if (HontunTheme.smog() || HontunTheme.modern2()) l.groupW = x + pad;
        d.add(l);
        String label = p.wide ? "Open log folder" : "Logs";
        int bw = ServerPages.btnW(label);
        if (w - bw < x + 8) {
            label = "Logs";
            bw = ServerPages.btnW(label);
        }
        d.buttons.add(new Doc.Btn(label, l, 0, bw, p::openLogFolder, Paint.FOLDER));
        gap(d);
    }

    private static String label(int server, boolean brief) {
        return server <= 0 ? "?" : brief ? String.valueOf(server) : "Server " + server;
    }

    private static void path(ServerListPanel p, Doc.Line l, List<Integer> path, int max) {
        if (path.isEmpty()) {
            l.t("no server reached", Paint.dim(), max);
            return;
        }
        int aw = width(">") + 8, total = 0;
        for (int i = 0; i < path.size(); i++) total += (i > 0 ? aw : 0) + Paint.pillWidth(Paint.c(label(path.get(i), false)), Paint.NORMAL, -1);
        boolean brief = total > max;
        int end = l.cur + max;
        for (int i = 0; i < path.size(); i++) {
            int left = path.size() - i;
            String s = label(path.get(i), brief);
            int need = (i > 0 ? aw : 0) + Paint.pillWidth(Paint.c(s), Paint.NORMAL, -1);
            int rest = left > 1 ? 8 + width("+" + (left - 1)) : 0;
            if (i > 0 && l.cur + need + rest > end) {
                l.sp(8).t("+" + left, Paint.dim());
                List<Component> tip = new ArrayList<>();
                tip.add(Paint.c(left + " more"));
                StringBuilder b = new StringBuilder();
                for (int k = i; k < path.size(); k++) b.append(b.isEmpty() ? "" : " > ").append(label(path.get(k), false));
                tip.add(Paint.c(b.toString()));
                l.last().tip(tip);
                return;
            }
            if (i > 0) l.sp(4).t(">", Paint.dim()).sp(4);
            l.p(s, Paint.NORMAL, -1);
            LogModel.ServerEntry e = p.log.server(path.get(i));
            l.last().tip(e == null ? List.of(Paint.c(path.get(i) > 0 ? "Server " + path.get(i) : "Could not be told apart"))
                    : List.of(Paint.c(e.label()), Paint.c(about(e))));
        }
    }

    private static void visit(ServerListPanel p, Doc d, int w, LogModel.Visit v) {
        List<String> changes = v.changes();
        int shown = changes.size() > MAX_CHANGES ? MAX_CHANGES - 1 : changes.size(), extra = changes.size() - shown;
        int lines = shown + (extra > 0 ? 1 : 0) + (v.note.isBlank() ? 0 : 1), in = w - 20;
        int head = p.wide ? 24 : 38;
        Doc.Box c = card(head + lines * 14);
        ZonedDateTime a = time(v.start), b = time(v.end);
        Doc.Line t = new Doc.Line(10).at(0);
        String dur = v.open ? "playing" : duration(v.duration());
        int dw = width(dur);
        t.t(a.format(HM) + " – " + (v.open ? "now" : b.format(HM)), Paint.dim());
        int px = Math.max(78, width("00:00 – 00:00") + 10);
        if (p.wide) path(p, t.at(px), v.path(), in - px - dw - 10);
        right(t, in, dur, Paint.sub());
        c.put(fixed(t), 10, 7, in);
        if (!p.wide) {
            Doc.Line l = new Doc.Line(10).at(0);
            path(p, l, v.path(), in);
            c.put(fixed(l), 10, 22, in);
        }
        int y = head;
        if (!v.note.isBlank()) {
            c.put(fixed(new Doc.Line(10).text(0, v.note, Paint.dim(), in)), 10, y, in);
            y += 14;
        }
        for (int i = 0; i < shown; i++) {
            c.put(fixed(new Doc.Line(10).text(0, changes.get(i), Paint.acc(), in)), 10, y, in);
            y += 14;
        }
        if (extra > 0) {
            Doc.Line l = new Doc.Line(10).text(0, extra + " more changes", Paint.dim());
            l.last().tip(changes.subList(shown, changes.size()).stream().map(s -> (Component) Paint.c(s)).toList());
            c.put(fixed(l), 10, y, in);
        }
        d.add(c);
    }
}
