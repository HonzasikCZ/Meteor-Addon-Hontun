package cz.honzasik.hontun.serverlog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class ServerLog {
    private static final long SAVE_EVERY_MS = 5_000L;
    private static final long MAX_FILE = 16L << 20;
    private static final int KEY_CAP = 1024;
    private static final int LOG_CAP = 256;
    private static final long IDLE_MS = 600_000L;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Map<String, ServerLog> LOGS = new ConcurrentHashMap<>();
    private static final Map<String, String> KEYS = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService EXEC = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Hontun-serverlog");
        t.setDaemon(true);
        return t;
    });

    private static volatile Path root;
    private static boolean hooked;

    private final String address;
    private volatile LogModel.View view;
    private LogModel.Log log;
    private boolean loaded;
    private boolean readOnly;
    private boolean dirty;
    private boolean scheduled;
    private long savedAt;
    private volatile long usedAt = System.currentTimeMillis();

    private ServerLog(String address) {
        this.address = address;
        this.view = LogModel.View.empty(address);
        this.log = new LogModel.Log(address);
    }

    public static synchronized void init(Path dir) {
        root = dir;
        if (hooked) return;
        hooked = true;
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(ServerLog::shutdown, "Hontun-serverlog-flush"));
        } catch (Throwable ignored) {
        }
    }

    public static Path dir() {
        return root;
    }

    public static ServerLog forAddress(String hostPort) {
        String raw = hostPort == null ? "" : hostPort;
        String key = KEYS.get(raw);
        if (key == null) {
            key = LogModel.normalise(raw);
            if (KEYS.size() >= KEY_CAP) KEYS.clear();
            KEYS.put(raw, key);
        }
        ServerLog found = LOGS.get(key);
        if (found != null) {
            found.usedAt = System.currentTimeMillis();
            return found;
        }
        ServerLog made = new ServerLog(key), old = LOGS.putIfAbsent(key, made);
        if (old != null) {
            old.usedAt = System.currentTimeMillis();
            return old;
        }
        EXEC.execute(made::load);
        if (LOGS.size() > LOG_CAP) EXEC.execute(ServerLog::evict);
        return made;
    }

    public String address() {
        return address;
    }

    public Path file() {
        Path dir = root;
        return dir == null ? null : dir.resolve(LogModel.fileName(address) + ".json");
    }

    public LogModel.View view() {
        return view;
    }

    public void ping(LogModel.Ping ping) {
        if (ping != null) post(l -> l.ping(ping));
    }

    public void addressInfo(LogModel.Address info) {
        if (info != null) post(l -> l.address(info));
    }

    public void startVisit(long at, String note) {
        post(l -> l.startVisit(at, note));
    }

    public void hop(long at, String how, ServerKey key) {
        post(l -> l.hop(at, how, key));
    }

    public void snapshot(int hop, long at, LogModel.Snapshot snapshot) {
        post(l -> l.snapshot(hop, at, snapshot));
    }

    public void note(String text) {
        post(l -> l.note(text));
    }

    public void endVisit(long at) {
        post(l -> l.endVisit(at));
        flush();
    }

    public void flush() {
        EXEC.execute(this::save);
    }

    private void post(Consumer<LogModel.Log> change) {
        EXEC.execute(() -> {
            load();
            try {
                change.accept(log);
                dirty = true;
                view = log.view();
            } catch (Throwable ignored) {
            }
            schedule();
        });
    }

    private void schedule() {
        if (scheduled || !dirty || readOnly || root == null) return;
        scheduled = true;
        long wait = Math.max(0L, SAVE_EVERY_MS - (System.currentTimeMillis() - savedAt));
        EXEC.schedule(() -> {
            scheduled = false;
            save();
        }, Math.min(wait, SAVE_EVERY_MS), TimeUnit.MILLISECONDS);
    }

    private void load() {
        if (loaded) return;
        loaded = true;
        Path file = file();
        if (file != null && Files.isRegularFile(file)) {
            String text = null;
            boolean bad = false;
            try {
                if (Files.size(file) > MAX_FILE) bad = true;
                else text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            } catch (Throwable t) {
                readOnly = true;
            }
            if (text != null) {
                try {
                    log = LogModel.read(JsonParser.parseString(text), address);
                } catch (Throwable t) {
                    bad = true;
                }
            }
            if (bad) {
                log = new LogModel.Log(address);
                try {
                    Files.move(file, file.resolveSibling(file.getFileName() + ".bad"), StandardCopyOption.REPLACE_EXISTING);
                } catch (Throwable t) {
                    readOnly = true;
                }
            }
        }
        view = log.view();
    }

    private void save() {
        Path file = file();
        if (!dirty || readOnly || file == null) return;
        savedAt = System.currentTimeMillis();
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(file.getParent());
            Files.write(temp, GSON.toJson(LogModel.write(log)).getBytes(StandardCharsets.UTF_8));
            move(temp, file);
            dirty = false;
        } catch (Throwable t) {
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {
            }
        }
    }

    private static void move(Path from, Path to) throws IOException {
        try {
            Files.move(from, to, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void evict() {
        if (LOGS.size() <= LOG_CAP) return;
        long now = System.currentTimeMillis();
        LOGS.entrySet().removeIf(e -> {
            ServerLog l = e.getValue();
            return LOGS.size() > LOG_CAP && l.loaded && !l.dirty && !l.scheduled && l.log.idle() && now - l.usedAt > IDLE_MS;
        });
    }

    public static void openDir(Runnable then) {
        EXEC.execute(() -> {
            Path dir = root;
            if (dir == null) return;
            try {
                Files.createDirectories(dir);
            } catch (Throwable ignored) {
            }
            then.run();
        });
    }

    private static void shutdown() {
        try {
            EXEC.submit(() -> {
                for (ServerLog l : LOGS.values()) {
                    try {
                        l.save();
                    } catch (Throwable ignored) {
                    }
                }
            }).get(4, TimeUnit.SECONDS);
        } catch (Throwable ignored) {
        }
    }
}
