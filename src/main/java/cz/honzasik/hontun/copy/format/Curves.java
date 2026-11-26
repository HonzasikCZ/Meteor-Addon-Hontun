package cz.honzasik.hontun.copy.format;

public final class Curves {
    public static final GradientFit.Curve MINI_MESSAGE = (from, to, n) -> {
        int[] out = new int[n];
        double multiplier = n == 1 ? 0.0 : 1.0 / (double) (n - 1);
        for (int i = 0; i < n; i++) {
            double position = (double) i * multiplier;
            int low = (int) Math.floor(position);
            if (low >= 1) {
                out[i] = to;
                continue;
            }
            float t = (float) ((float) position - (float) low);
            out[i] = roundLerp(from, to, Math.min(1.0f, Math.max(0.0f, t)));
        }
        return out;
    };

    public static final GradientFit.Curve DECENT = (from, to, n) -> {
        if (n <= 1) return null;
        int[] out = new int[n];
        int[] a = rgb(from);
        int[] b = rgb(to);
        int[] step = new int[3];
        int[] dir = new int[3];
        for (int c = 0; c < 3; c++) {
            step[c] = Math.abs(a[c] - b[c]) / (n - 1);
            dir[c] = a[c] < b[c] ? 1 : -1;
        }
        for (int i = 0; i < n; i++) {
            int r = a[0] + step[0] * i * dir[0];
            int g = a[1] + step[1] * i * dir[1];
            int bl = a[2] + step[2] * i * dir[2];
            out[i] = pack(r, g, bl);
        }
        return out;
    };

    public static final GradientFit.Curve CMI = (from, to, n) -> {
        int[] out = new int[n];
        int[] a = rgb(from);
        int[] b = rgb(to);
        int length = n < 2 ? 2 : n - 1;
        for (int i = 0; i < n; i++) {
            double p = (double) i / (double) length;
            double q = 1.0 - p;
            out[i] = pack((int) (b[0] * p + a[0] * q), (int) (b[1] * p + a[1] * q), (int) (b[2] * p + a[2] * q));
        }
        return out;
    };

    private Curves() {}

    private static int roundLerp(int a, int b, float t) {
        int[] x = rgb(a);
        int[] y = rgb(b);
        int r = Math.round((float) x[0] + t * (float) (y[0] - x[0]));
        int g = Math.round((float) x[1] + t * (float) (y[1] - x[1]));
        int bl = Math.round((float) x[2] + t * (float) (y[2] - x[2]));
        return pack(r, g, bl);
    }

    private static int[] rgb(int c) {
        return new int[]{(c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF};
    }

    private static int pack(int r, int g, int b) {
        return (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
