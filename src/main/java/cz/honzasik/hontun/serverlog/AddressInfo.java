package cz.honzasik.hontun.serverlog;

import cz.honzasik.hontun.utils.HontunGeo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AddressInfo {
    public enum Protection {
        NONE(null), TCPSHIELD("TCPShield"), NEOPROTECT("NeoProtect"), INFINITY_FILTER("Infinity-Filter"), UNKNOWN(null);

        private final String label;

        Protection(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public boolean shield() {
            return label != null;
        }

        public static Protection of(String name) {
            if (name == null) return UNKNOWN;
            for (Protection p : values()) if (p.name().equals(name)) return p;
            return UNKNOWN;
        }
    }

    public record Evidence(String source, String text, Protection provider) {
        public boolean supports() {
            return provider != null;
        }
    }

    private static final long NEOPROTECT_ASN = 199414L;

    public record View(String address, String hostname, int port, boolean literal, boolean loading, boolean failed,
                       String srv, List<String> ips, List<String> cnames, String ptr, String country, String org, long asn,
                       Protection protection, List<Evidence> evidence, long at) {
        public String ip() {
            return ips.isEmpty() ? "" : ips.get(0);
        }
    }

    private static final class Entry {
        final String raw;
        final String address;
        volatile DnsLookup.Result dns;
        volatile View view;
        volatile boolean running;
        volatile long at;
        HontunGeo.Info geo;
        DnsLookup.Result built;
        boolean logged;

        Entry(String raw) {
            this.raw = raw;
            this.address = LogModel.normalise(raw);
            this.view = new View(address, host(address), port(address), false, true, false, null, List.of(), List.of(), null,
                    "", "", 0L, Protection.UNKNOWN, List.of(), 0L);
        }
    }

    private static final long TTL_MS = 600_000L;
    private static final long RETRY_MS = 30_000L;
    private static final long GEO_WAIT_MS = 20_000L;
    private static final int CAP = 256;
    private static final Map<String, Entry> CACHE = new ConcurrentHashMap<>();
    private static final ExecutorService POOL = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "Hontun-dns");
        t.setDaemon(true);
        return t;
    });

    private AddressInfo() {}

    public static View get(String hostPort) {
        String raw = hostPort == null ? "" : hostPort.strip();
        Entry e = CACHE.get(raw);
        if (e == null) {
            if (CACHE.size() >= CAP) CACHE.clear();
            Entry made = new Entry(raw), old = CACHE.putIfAbsent(raw, made);
            e = old == null ? made : old;
        }
        try {
            refresh(e);
        } catch (Throwable ignored) {
        }
        return e.view;
    }

    private static void refresh(Entry e) {
        long now = System.currentTimeMillis();
        synchronized (e) {
            DnsLookup.Result dns = e.dns;
            if (!e.running && (dns == null || now - e.at >= (dns.resolved() ? TTL_MS : RETRY_MS))) {
                e.running = true;
                POOL.execute(() -> {
                    DnsLookup.Result got = null;
                    try {
                        got = DnsLookup.lookup(e.address);
                    } catch (Throwable ignored) {
                    } finally {
                        e.at = System.currentTimeMillis();
                        if (got != null) e.dns = got;
                        else if (e.dns == null) e.dns = failed(e.address);
                        e.running = false;
                    }
                });
            }
            if (dns == null) return;
            HontunGeo.Info geo = HontunGeo.info(e.raw);
            if (e.built != dns || e.geo != geo) {
                e.logged = false;
                e.built = dns;
                e.geo = geo;
                e.view = build(e, dns, geo);
            }
            if (!e.logged && e.at != 0L && dns.resolved() && (geo != null || !HontunGeo.enabled() || now - e.at > GEO_WAIT_MS)) {
                e.logged = true;
                View v = e.view;
                ServerLog.forAddress(e.raw).addressInfo(new LogModel.Address(v.ip(), v.ips(), v.protection().name(), v.org(), v.country(), e.at, e.at));
            }
        }
    }

    private static DnsLookup.Result failed(String address) {
        return new DnsLookup.Result(host(address), port(address), false, false, null, null, port(address), List.of(), List.of(), null);
    }

    private static View build(Entry e, DnsLookup.Result dns, HontunGeo.Info geo) {
        List<Evidence> ev = new ArrayList<>();
        if (dns.literal()) {
            ev.add(new Evidence("dns", "Entered as an IP address, there is no name to look up", null));
        } else {
            Protection own = DnsLookup.shieldName(dns.host());
            if (own != null) ev.add(new Evidence("dns", "The address is a " + own.label() + " name", own));
            if (dns.srv() != null) {
                ev.add(new Evidence("dns", "SRV record points to " + dns.srv(), DnsLookup.shieldName(dns.srvHost())));
            } else {
                ev.add(new Evidence("dns", dns.port() == 25565 ? "No SRV record, default port" : "Own port " + dns.port() + ", no SRV lookup", null));
            }
            for (String c : dns.cnames()) ev.add(new Evidence("dns", "CNAME " + c, DnsLookup.shieldName(c)));
            if (dns.cnames().isEmpty()) ev.add(new Evidence("dns", "No CNAME, the name points straight at its address", null));
        }
        if (dns.resolved()) {
            ev.add(dns.ptr() == null ? new Evidence("ptr", "No reverse DNS", null)
                    : new Evidence("ptr", "Reverse DNS " + dns.ptr(), DnsLookup.shieldName(dns.ptr())));
        } else {
            ev.add(new Evidence("dns", "The name did not resolve to an address", null));
        }
        String org = geo == null ? "" : geo.org();
        if (!org.isEmpty()) {
            ev.add(new Evidence("ip", "The address belongs to " + org + (geo.asn() > 0L ? ", AS" + geo.asn() : ""), network(org, geo.asn())));
        }
        Protection shield = null;
        for (Evidence x : ev) if (shield == null && x.supports()) shield = x.provider();
        ev.sort(Comparator.comparingInt(x -> x.supports() ? 0 : 1));
        Protection protection = shield != null ? shield
                : dns.resolved() && (!org.isEmpty() || DnsLookup.local(dns.ips().get(0))) ? Protection.NONE : Protection.UNKNOWN;
        String country = geo == null ? "" : geo.country();
        return new View(e.address, dns.host(), dns.targetPort(), dns.literal(), false, !dns.resolved(), dns.srv(), dns.ips(),
                dns.cnames(), dns.ptr(), country, org, geo == null ? 0L : geo.asn(), protection, List.copyOf(ev), e.at);
    }

    private static Protection network(String org, long asn) {
        if (asn == NEOPROTECT_ASN) return Protection.NEOPROTECT;
        String o = org.toLowerCase(Locale.ROOT);
        if (o.contains("tcpshield")) return Protection.TCPSHIELD;
        if (o.contains("neoprotect")) return Protection.NEOPROTECT;
        if (o.contains("infinity-filter") || o.contains("infinity filter")) return Protection.INFINITY_FILTER;
        return null;
    }

    private static String host(String address) {
        String h = address.substring(0, address.lastIndexOf(':'));
        return h.startsWith("[") ? h.substring(1, h.length() - 1) : h;
    }

    private static int port(String address) {
        return Integer.parseInt(address.substring(address.lastIndexOf(':') + 1));
    }
}
