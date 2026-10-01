package cz.honzasik.hontun.gui.theme.style;

public final class TabGlide {
    public static double x, y, w, h;
    public static long at;

    private TabGlide() {}

    public static void store(double x, double y, double w, double h) {
        TabGlide.x = x;
        TabGlide.y = y;
        TabGlide.w = w;
        TabGlide.h = h;
        at = System.currentTimeMillis();
    }

    public static boolean fresh(long maxMs) {
        return at > 0 && System.currentTimeMillis() - at <= maxMs;
    }

    public static void reset() {
        x = y = w = h = 0;
        at = 0;
    }
}
