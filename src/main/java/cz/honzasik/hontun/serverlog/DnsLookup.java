package cz.honzasik.hontun.serverlog;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class DnsLookup {
    private static final int MAX_CNAMES = 8;
    private static final int MAX_IPS = 16;
    private static final long PTR_LIMIT_MS = 3_000L;
    private static final ExecutorService PTR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "Hontun-ptr");
        t.setDaemon(true);
        return t;
    });

    private DnsLookup() {}

    public record Result(String host, int port, boolean literal, boolean resolved, String srv, String srvHost, int srvPort,
                         List<String> ips, List<String> cnames, String ptr) {
        public int targetPort() {
            return srv == null ? port : srvPort;
        }
    }

    public static Result lookup(String normalised) {
        int colon = normalised.lastIndexOf(':');
        String host = normalised.substring(0, colon);
        int port = Integer.parseInt(normalised.substring(colon + 1));
        if (host.startsWith("[")) host = host.substring(1, host.length() - 1);
        boolean literal = literal(host);
        String srvHost = null;
        int srvPort = port;
        Set<String> cnames = new LinkedHashSet<>();
        if (!literal && !host.isEmpty()) {
            if (port == 25565) {
                String[] srv = srv(host);
                if (srv != null) {
                    srvHost = srv[0];
                    srvPort = Integer.parseInt(srv[1]);
                }
            }
            chain(srvHost == null ? host : srvHost, cnames);
        }
        List<String> ips = new ArrayList<>();
        InetAddress first = null;
        try {
            if (!host.isEmpty()) {
                for (InetAddress a : InetAddress.getAllByName(srvHost == null ? host : srvHost)) {
                    if (ips.size() >= MAX_IPS) break;
                    if (first == null || (!(first instanceof Inet4Address) && a instanceof Inet4Address)) first = a;
                    String ip = a.getHostAddress();
                    int zone = ip.indexOf('%');
                    if (zone > 0) ip = ip.substring(0, zone);
                    if (!ips.contains(ip)) ips.add(ip);
                }
            }
        } catch (Throwable ignored) {
        }
        String ptr = first == null || local(first) ? null : reverse(first);
        String srv = srvHost == null ? null : srvHost + ":" + srvPort;
        return new Result(host, port, literal, !ips.isEmpty(), srv, srvHost, srvPort, List.copyOf(ips), List.copyOf(cnames), ptr);
    }

    public static AddressInfo.Protection shieldName(String name) {
        if (name == null) return null;
        String n = strip(name);
        if (under(n, "tcpshield.com")) return AddressInfo.Protection.TCPSHIELD;
        if (under(n, "neoprotect.net") || under(n, "neoprotect.ovh")) return AddressInfo.Protection.NEOPROTECT;
        if (under(n, "infinity-filter.com")) return AddressInfo.Protection.INFINITY_FILTER;
        return null;
    }

    private static boolean under(String name, String domain) {
        return name.equals(domain) || name.endsWith("." + domain);
    }

    public static boolean literal(String host) {
        if (host.indexOf(':') >= 0) return true;
        String[] parts = host.split("\\.", -1);
        if (parts.length != 4) return false;
        for (String p : parts) {
            if (p.isEmpty() || p.length() > 3) return false;
            for (int i = 0; i < p.length(); i++) if (p.charAt(i) < '0' || p.charAt(i) > '9') return false;
            if (Integer.parseInt(p) > 255) return false;
        }
        return true;
    }

    public static boolean local(String ip) {
        try {
            return literal(ip) && local(InetAddress.getByName(ip));
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean local(InetAddress a) {
        return a.isLoopbackAddress() || a.isSiteLocalAddress() || a.isLinkLocalAddress() || a.isAnyLocalAddress();
    }

    private static String[] srv(String host) {
        String record = query("_minecraft._tcp." + host, "SRV");
        if (record == null) return null;
        String[] p = record.split(" ", 4);
        if (p.length < 4) return null;
        try {
            int port = Integer.parseInt(p[2]);
            String target = strip(p[3]);
            return target.isEmpty() || port < 1 || port > 65535 ? null : new String[]{target, String.valueOf(port)};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void chain(String host, Set<String> out) {
        String at = strip(host);
        for (int i = 0; i < MAX_CNAMES; i++) {
            String next = query(at, "CNAME");
            if (next == null) return;
            next = strip(next);
            if (next.isEmpty() || next.equals(strip(host)) || !out.add(next)) return;
            at = next;
        }
    }

    private static String reverse(InetAddress a) {
        Future<String> f = PTR.submit(() -> query(arpa(a.getAddress()), "PTR"));
        try {
            String s = f.get(PTR_LIMIT_MS, TimeUnit.MILLISECONDS);
            s = s == null ? null : strip(s);
            return s == null || s.isEmpty() ? null : s;
        } catch (Throwable t) {
            f.cancel(true);
            return null;
        }
    }

    static String arpa(byte[] ip) {
        StringBuilder sb = new StringBuilder();
        for (int i = ip.length - 1; i >= 0; i--) {
            int b = ip[i] & 0xFF;
            if (ip.length == 4) sb.append(b).append('.');
            else sb.append(Character.forDigit(b & 15, 16)).append('.').append(Character.forDigit(b >> 4, 16)).append('.');
        }
        return sb.append(ip.length == 4 ? "in-addr.arpa" : "ip6.arpa").toString();
    }

    private static String query(String name, String type) {
        DirContext ctx = null;
        try {
            Class.forName("com.sun.jndi.dns.DnsContextFactory");
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            env.put("java.naming.provider.url", "dns:");
            env.put("com.sun.jndi.dns.timeout.initial", "1500");
            env.put("com.sun.jndi.dns.timeout.retries", "1");
            ctx = new InitialDirContext(env);
            Attribute a = ctx.getAttributes(name, new String[]{type}).get(type);
            return a == null || a.size() == 0 ? null : a.get().toString();
        } catch (Throwable t) {
            return null;
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException ignored) {
                }
            }
        }
    }

    private static String strip(String name) {
        String n = name.strip().toLowerCase(Locale.ROOT);
        while (n.endsWith(".")) n = n.substring(0, n.length() - 1);
        return n;
    }
}
