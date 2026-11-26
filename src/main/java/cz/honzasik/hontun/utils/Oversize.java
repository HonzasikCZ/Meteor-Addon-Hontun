package cz.honzasik.hontun.utils;

import cz.honzasik.hontun.serverlog.LogModel;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class Oversize {
    public static final long FILE_CAP = 4L << 20;
    private static final long DIR_CAP = 32L << 20;
    private static final long KIND_CAP = 1L << 20;
    private static final int ENTRY_CHARS = 1 << 18;
    private static final int ITEM_CHARS = 1024;
    private static final int CUT_SEEN_CAP = 1024;
    private static final int ITEM_SEEN_CAP = 8192;
    private static final String SUFFIX = ".oversized.txt";
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Object LOCK = new Object();
    private static final Map<String, Target> TARGETS = new HashMap<>();
    private static final Set<String> CUT_SEEN = new HashSet<>();
    private static final Set<String> ITEM_SEEN = new HashSet<>();
    private static final ScheduledExecutorService EXEC = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Hontun oversize log");
        t.setDaemon(true);
        return t;
    });
    private static boolean scheduled;

    private static final int SAVED = 0;
    private static final int KIND_FULL = 1;
    private static final int FILE_FULL = 2;

    private static final class Target {
        final String name;
        long used;
        boolean full;
        boolean fresh = true;
        final Map<String, Long> kinds = new HashMap<>();
        final Set<String> fullKinds = new HashSet<>();
        ByteArrayOutputStream pending = new ByteArrayOutputStream();

        Target(String name) {
            this.name = name;
        }
    }

    private Oversize() {}

    public static String where() {
        return "hontun/server-log/" + fileName();
    }

    public static String where(String what) {
        String name = fileName();
        boolean complete;
        synchronized (LOCK) {
            Target t = TARGETS.get(name);
            complete = t == null || (!t.full && !t.fullKinds.contains(what));
        }
        return "hontun/server-log/" + name + (complete ? "" : " (only partly, its size limit was reached)");
    }

    public static String cut(String what, String full) {
        int n = full == null ? 0 : full.length();
        int r = save(CUT_SEEN, CUT_SEEN_CAP, what, what + "|" + n + "|" + (full == null ? 0 : full.hashCode()), () -> "[" + time() + "] " + what + " (" + n + " chars"
                + (n > ENTRY_CHARS ? ", first " + ENTRY_CHARS + " saved" : "") + ")\n" + (n > ENTRY_CHARS ? full.substring(0, ENTRY_CHARS) : full) + "\n\n");
        if (r == FILE_FULL) return "[cut from " + n + " chars, not saved: " + where() + " is at its " + (FILE_CAP >> 20) + " MB limit] ";
        if (r == KIND_FULL) return "[cut from " + n + " chars, not saved: the " + (KIND_CAP >> 20) + " MB share for " + what + " in " + where() + " is used up] ";
        return "[cut from " + n + " chars, " + (n > ENTRY_CHARS ? "first " + ENTRY_CHARS + " chars" : "full text") + " in " + where() + "] ";
    }

    public static boolean item(String what, String item) {
        String s = item == null ? "" : item.length() > ITEM_CHARS ? item.substring(0, ITEM_CHARS) + " ... (" + item.length() + " chars)" : item;
        return save(ITEM_SEEN, ITEM_SEEN_CAP, what, what + "|" + s, () -> "[" + time() + "] " + what + ": " + s + "\n") == SAVED;
    }

    private static int save(Set<String> seenSet, int seenCap, String what, String key, Supplier<String> text) {
        String name = fileName();
        String seen = name + "|" + key;
        synchronized (LOCK) {
            Target t = TARGETS.computeIfAbsent(name, Target::new);
            if (seenSet.contains(seen)) return SAVED;
            if (t.full) return FILE_FULL;
            if (t.fullKinds.contains(what)) return KIND_FULL;
            byte[] bytes = text.get().getBytes(StandardCharsets.UTF_8);
            long kind = t.kinds.getOrDefault(what, 0L);
            if (t.used + bytes.length > FILE_CAP) {
                t.full = true;
                mark(t, "limit of " + (FILE_CAP >> 20) + " MB reached, later oversized data from this server was not saved");
                return FILE_FULL;
            }
            if (kind + bytes.length > KIND_CAP) {
                t.fullKinds.add(what);
                mark(t, "share of " + (KIND_CAP >> 20) + " MB for \"" + what + "\" reached, later entries of this kind were not saved");
                return KIND_FULL;
            }
            if (seenSet.size() < seenCap) seenSet.add(seen);
            t.used += bytes.length;
            t.kinds.put(what, kind + bytes.length);
            t.pending.write(bytes, 0, bytes.length);
            schedule();
            return SAVED;
        }
    }

    private static void mark(Target t, String text) {
        byte[] b = ("[" + time() + "] " + text + "\n").getBytes(StandardCharsets.UTF_8);
        t.used += b.length;
        t.pending.write(b, 0, b.length);
        schedule();
    }

    private static void schedule() {
        if (scheduled) return;
        scheduled = true;
        EXEC.schedule(Oversize::flush, 1, TimeUnit.SECONDS);
    }

    private static void flush() {
        List<Object[]> work = new ArrayList<>();
        synchronized (LOCK) {
            scheduled = false;
            for (Target t : TARGETS.values()) {
                if (t.pending.size() == 0) continue;
                work.add(new Object[]{t.name, t.pending.toByteArray(), t.fresh});
                t.pending = new ByteArrayOutputStream();
                t.fresh = false;
            }
        }
        Path dir = dir();
        for (Object[] w : work) {
            try {
                Files.createDirectories(dir);
                Path file = dir.resolve((String) w[0]);
                if ((Boolean) w[2]) {
                    Files.deleteIfExists(file);
                    trim(dir, FILE_CAP);
                }
                try (OutputStream out = Files.newOutputStream(file, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                    out.write((byte[]) w[1]);
                }
            } catch (Throwable ignored) {
            }
        }
    }

    private static void trim(Path dir, long room) {
        try (var files = Files.list(dir)) {
            List<Path> list = new ArrayList<>(files.filter(p -> p.getFileName().toString().endsWith(SUFFIX)).toList());
            long total = 0;
            for (Path p : list) total += Files.size(p);
            if (total + room <= DIR_CAP) return;
            list.sort((a, b) -> {
                try {
                    return Files.getLastModifiedTime(a).compareTo(Files.getLastModifiedTime(b));
                } catch (Throwable e) {
                    return 0;
                }
            });
            for (Path p : list) {
                if (total + room <= DIR_CAP) break;
                long size = Files.size(p);
                Files.deleteIfExists(p);
                total -= size;
            }
        } catch (Throwable ignored) {
        }
    }

    private static Path dir() {
        return FabricLoader.getInstance().getGameDir().resolve("hontun").resolve("server-log");
    }

    private static String fileName() {
        ServerData d = Minecraft.getInstance().getCurrentServer();
        String address = d == null || d.ip == null || d.ip.isBlank() ? "direct" : d.ip;
        return LogModel.fileName(address) + SUFFIX;
    }

    private static String time() {
        return LocalTime.now().format(TIME);
    }
}
