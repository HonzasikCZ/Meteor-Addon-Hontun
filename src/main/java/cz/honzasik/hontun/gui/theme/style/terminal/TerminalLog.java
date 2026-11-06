package cz.honzasik.hontun.gui.theme.style.terminal;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;

import java.time.LocalTime;
import java.util.IdentityHashMap;
import java.util.Map;

public final class TerminalLog {
    private static String message = "";
    private static long postedAt;
    private static final Map<Module, Boolean> ACTIVE = new IdentityHashMap<>();
    private static boolean primed;

    private TerminalLog() {}

    public static void post(String text) {
        LocalTime now = LocalTime.now();
        message = String.format("[%02d:%02d:%02d] %s", now.getHour(), now.getMinute(), now.getSecond(), text);
        postedAt = System.currentTimeMillis();
    }

    public static String message() {
        return message;
    }

    public static double alpha() {
        if (message.isEmpty()) return 0;
        double t = Math.max(0, Math.min(1, (System.currentTimeMillis() - postedAt - 3500) / 500.0));
        double e = t * t * (3 - 2 * t);
        return 1 - 0.55 * e;
    }

    public static void watchModules(boolean ids) {
        Modules modules = Modules.get();
        if (modules == null) return;

        Module changed = null;
        for (Module module : modules.getAll()) {
            boolean on = module.isActive();
            Boolean was = ACTIVE.put(module, on);
            if (primed && was != null && was != on) changed = module;
        }

        if (!primed && message.isEmpty()) post("hontun terminal ready  ·  right click a module to open its config");
        primed = true;

        if (changed != null) post((ids ? changed.name : changed.title) + ": " + (changed.isActive() ? "enabled" : "disabled"));
    }
}
