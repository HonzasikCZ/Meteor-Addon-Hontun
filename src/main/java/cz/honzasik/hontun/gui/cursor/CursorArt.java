package cz.honzasik.hontun.gui.cursor;

import cz.honzasik.hontun.utils.HontunTheme;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CursorArt {
    public enum Kind { ARROW, HAND, IBEAM, CROSSHAIR, RESIZE_NS, RESIZE_EW, RESIZE_ALL, NOT_ALLOWED }

    public record Art(BufferedImage image, int hotX, int hotY) {}

    private enum Look { PIXEL, FLAT, CHAMFER, SMOG }

    private enum Role { MAIN, ACTIVE, RING, LINE }

    private record Part(List<Shape> pieces, Area sil, Area body, Role role, double rim) {}

    private static final double AXIS = Math.toRadians(43);

    private CursorArt() {}

    public static Kind kindOf(String name) {
        return switch (name) {
            case "pointing_hand" -> Kind.HAND;
            case "ibeam" -> Kind.IBEAM;
            case "crosshair" -> Kind.CROSSHAIR;
            case "resize_ns" -> Kind.RESIZE_NS;
            case "resize_ew" -> Kind.RESIZE_EW;
            case "resize_all" -> Kind.RESIZE_ALL;
            case "not_allowed" -> Kind.NOT_ALLOWED;
            default -> Kind.ARROW;
        };
    }

    public static Art draw(Kind kind, int scale) {
        Look look = HontunTheme.smog() ? Look.SMOG
                : HontunTheme.modern2() ? Look.CHAMFER
                : HontunTheme.modern1() ? Look.FLAT
                : Look.PIXEL;
        int s = Math.max(1, Math.min(6, scale));
        if (look == Look.PIXEL) return pixel(kind, s);
        return smooth(kind, look, s);
    }

    private static int npx(double v, double u) {
        return (int) Math.max(1, Math.round(v * u));
    }

    private static double px(double v, double u) {
        return npx(v, u) / u;
    }

    private static double par(double v, double u, boolean odd) {
        long p = Math.max(1, Math.round(v * u));
        if ((p % 2 == 1) != odd) p = v * u > p || p == 1 ? p + 1 : p - 1;
        return p / u;
    }

    private static BasicStroke pen(double w, boolean ch) {
        return ch ? new BasicStroke((float) w, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 2.2f)
                : new BasicStroke((float) w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    private static Area shrink(Shape s, double d, boolean ch) {
        Area a = new Area(s);
        a.subtract(new Area(pen(2 * d, ch).createStrokedShape(s)));
        return a;
    }

    private static Shape rot(Shape s, double a) {
        return AffineTransform.getRotateInstance(a).createTransformedShape(s);
    }

    private static Path2D poly(double[] p, double[] r, boolean ch) {
        int n = p.length / 2;
        Path2D path = new Path2D.Double();
        for (int i = 0; i < n; i++) {
            int a = (i + n - 1) % n, c = (i + 1) % n;
            double x = p[2 * i], y = p[2 * i + 1];
            double ax = p[2 * a] - x, ay = p[2 * a + 1] - y, cx = p[2 * c] - x, cy = p[2 * c + 1] - y;
            double la = Math.hypot(ax, ay), lc = Math.hypot(cx, cy);
            double k = Math.min(r[i], Math.min(la, lc) * 0.45);
            double x1 = x + ax / la * k, y1 = y + ay / la * k, x2 = x + cx / lc * k, y2 = y + cy / lc * k;
            if (i == 0) path.moveTo(x1, y1);
            else path.lineTo(x1, y1);
            if (ch) path.lineTo(x2, y2);
            else path.quadTo(x, y, x2, y2);
        }
        path.closePath();
        return path;
    }

    private static double[] dartPts() {
        double l = Math.toRadians(23), r = Math.toRadians(63);
        return new double[]{0, 0, 17.0 * Math.sin(l), 17.0 * Math.cos(l),
                11.7 * Math.sin(AXIS), 11.7 * Math.cos(AXIS), 16.0 * Math.sin(r), 16.0 * Math.cos(r)};
    }

    private static Area dart(boolean ch) {
        if (ch) return new Area(poly(dartPts(), new double[]{0, 1.4, 0.9, 1.4}, true));
        return new Area(poly(dartPts(), new double[]{0.25, 1.9, 1.4, 1.9}, false));
    }

    private static List<Shape> burst(double u, boolean ch, boolean flat, double k) {
        double bw = px(flat || ch ? 1.15 : 0.8, u), r1 = (ch ? 4.4 : 3.9) * k, r2 = (ch ? 7.6 : 7.5) * k;
        double sh = ch && Math.round(bw * u) % 2 == 1 ? 0.5 / u : 0;
        double mid = ch ? -0.75 * Math.PI : -Math.PI / 2 - AXIS, fan = ch ? Math.PI / 4 : Math.toRadians(55);
        List<Shape> out = new ArrayList<>();
        for (int i = -1; i <= 1; i++) out.add(rot(bar(r1, -bw / 2 + sh, r2 - r1, bw, ch), mid + i * fan));
        return out;
    }

    private static Shape octagon(double r) {
        double c = r / Math.cos(Math.PI / 8);
        Path2D p = new Path2D.Double();
        for (int i = 0; i < 8; i++) {
            double t = Math.PI / 8 + i * Math.PI / 4;
            if (i == 0) p.moveTo(c * Math.cos(t), c * Math.sin(t));
            else p.lineTo(c * Math.cos(t), c * Math.sin(t));
        }
        p.closePath();
        return p;
    }

    private static Shape bar(double x, double y, double w, double h, boolean ch) {
        if (ch) return new Rectangle2D.Double(x, y, w, h);
        double m = Math.min(w, h);
        return new RoundRectangle2D.Double(x, y, w, h, m, m);
    }

    private static Shape dot(double d, boolean ch) {
        if (!ch) return new Ellipse2D.Double(-d / 2, -d / 2, d, d);
        double k = d * 0.62;
        Path2D p = new Path2D.Double();
        p.moveTo(0, -k);
        p.lineTo(k, 0);
        p.lineTo(0, k);
        p.lineTo(-k, 0);
        p.closePath();
        return p;
    }

    private static Shape chev(double v, double a, double cw, boolean ch) {
        Path2D p = new Path2D.Double();
        p.moveTo(v - a, -a);
        p.lineTo(v, 0);
        p.lineTo(v - a, a);
        BasicStroke st = ch ? new BasicStroke((float) cw, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 4f)
                : new BasicStroke((float) cw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        return st.createStrokedShape(p);
    }

    private static List<Part> build(Kind kind, boolean ch, boolean flat, double u, double o, double od, boolean[] half) {
        List<Part> out = new ArrayList<>();
        List<Shape> ps = new ArrayList<>();
        double k = u < 1.2 ? 1.25 : 1.0;
        switch (kind) {
            case ARROW -> {
                Area s = dart(ch);
                out.add(new Part(null, s, shrink(s, od, ch), Role.MAIN, 0));
            }
            case HAND -> {
                out.add(new Part(burst(u, ch, flat, k), null, null, Role.RING, flat ? 0 : o));
                Area s = dart(ch);
                out.add(new Part(null, s, shrink(s, o, ch), Role.ACTIVE, 0));
            }
            case IBEAM -> {
                half[0] = npx(1.15, u) % 2 == 1;
                double bw = par(1.15, u, half[0]), h = 13.4, sw = 4.8;
                ps.add(bar(-bw / 2, -h / 2, bw, h, ch));
                ps.add(bar(-sw / 2, -h / 2, sw, bw, ch));
                ps.add(bar(-sw / 2, h / 2 - bw, sw, bw, ch));
                out.add(new Part(ps, null, null, Role.LINE, o));
            }
            case CROSSHAIR -> {
                half[0] = npx(1.0, u) % 2 == 1;
                double bw = par(1.0, u, half[0]), r1 = 3.3 * k, r2 = 7.2 * k;
                for (int i = 0; i < 4; i++) ps.add(rot(bar(-bw / 2, -r2, bw, r2 - r1, ch), i * Math.PI / 2));
                ps.add(dot(par(1.8, u, half[0]), ch));
                out.add(new Part(ps, null, null, Role.LINE, o));
            }
            case RESIZE_EW, RESIZE_NS -> {
                half[0] = npx(2.2, u) % 2 == 1;
                double cw = px(1.35, u), t = kind == Kind.RESIZE_NS ? Math.PI / 2 : 0;
                ps.add(rot(chev(6.2 * k, 2.6 * k, cw, ch), t));
                ps.add(rot(chev(6.2 * k, 2.6 * k, cw, ch), t + Math.PI));
                ps.add(dot(par(2.2, u, half[0]), ch));
                out.add(new Part(ps, null, null, Role.LINE, o));
            }
            case RESIZE_ALL -> {
                half[0] = npx(2.2, u) % 2 == 1;
                double cw = px(1.35, u);
                for (int i = 0; i < 4; i++) ps.add(rot(chev(7.8 * k, 2.2 * k, cw, ch), i * Math.PI / 2));
                ps.add(dot(par(2.2, u, half[0]), ch));
                out.add(new Part(ps, null, null, Role.LINE, o));
            }
            case NOT_ALLOWED -> {
                half[0] = true;
                double bw = px(1.45, u), r = 5.3 * k;
                Shape circle = ch ? octagon(r) : new Ellipse2D.Double(-r, -r, 2 * r, 2 * r);
                ps.add(new BasicStroke((float) bw, BasicStroke.CAP_BUTT, ch ? BasicStroke.JOIN_MITER : BasicStroke.JOIN_ROUND, 4f).createStrokedShape(circle));
                ps.add(rot(new Rectangle2D.Double(-r, -bw / 2, 2 * r, bw), Math.PI / 4));
                out.add(new Part(ps, null, null, Role.LINE, o));
            }
        }
        return out;
    }

    private static double luma(int rgb) {
        return (0.299 * ((rgb >> 16) & 255) + 0.587 * ((rgb >> 8) & 255) + 0.114 * (rgb & 255)) / 255.0;
    }

    private static int[] colors(Look look, Role role) {
        int acc = HontunTheme.accent();
        return switch (look) {
            case FLAT -> {
                int ink = luma(acc) > 0.68 ? 0x15171C : 0xFFFFFF;
                yield role == Role.ACTIVE ? new int[]{acc, ink} : role == Role.RING ? new int[]{acc, acc} : new int[]{ink, acc};
            }
            case CHAMFER -> {
                int core = HontunTheme.lerp(0x0A0D13, acc, 0.10f);
                yield role == Role.MAIN ? new int[]{acc, core} : new int[]{0x0A0D13, acc};
            }
            default -> role == Role.ACTIVE ? new int[]{0xFFFFFF, 0x0B0B0D} : new int[]{0x0B0B0D, 0xFFFFFF};
        };
    }

    private static int facet(Look look, Role role, int bodyRgb, boolean light) {
        int acc = HontunTheme.accent(), hi = HontunTheme.accentHi();
        if (light) return HontunTheme.lerp(bodyRgb, look == Look.SMOG ? 0x8E949C : acc, 0.15f);
        return switch (look) {
            case FLAT -> HontunTheme.lerp(bodyRgb, role == Role.ACTIVE ? acc : hi, role == Role.ACTIVE ? 0.22f : 0.42f);
            case CHAMFER -> role == Role.ACTIVE ? HontunTheme.lerp(bodyRgb, hi, 0.38f) : HontunTheme.lerp(bodyRgb, acc, 0.16f);
            default -> 0x2A2B30;
        };
    }

    private static Color c(int rgb, int a) {
        return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, Math.max(0, Math.min(255, a)));
    }

    private static void sil(Graphics2D g, Part p, double extra, boolean ch) {
        if (p.sil() != null) {
            g.fill(p.sil());
            if (extra > 0) {
                g.setStroke(pen(2 * extra, ch));
                g.draw(p.sil());
            }
            return;
        }
        double w = p.rim() + extra;
        if (w > 0) g.setStroke(pen(2 * w, ch));
        for (Shape s : p.pieces()) {
            g.fill(s);
            if (w > 0) g.draw(s);
        }
    }

    private static void body(Graphics2D g, Part p) {
        if (p.body() != null) g.fill(p.body());
        else for (Shape s : p.pieces()) g.fill(s);
    }

    private static Graphics2D gfx(BufferedImage img, AffineTransform at) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.transform(at);
        return g;
    }

    private static Art smooth(Kind kind, Look look, int s) {
        double u = s == 1 ? 1.0 : s * 0.72;
        boolean ch = look == Look.CHAMFER;
        double o = px(0.62, u);
        double od = ch ? px(1.05, u) : o;
        boolean[] half = {false};
        List<Part> parts = build(kind, ch, look == Look.FLAT, u, o, od, half);
        double grow = ch ? 2.2 * o : o;
        Rectangle2D b = null;
        for (Part p : parts) {
            if (p.sil() != null) {
                b = b == null ? p.sil().getBounds2D() : b.createUnion(p.sil().getBounds2D());
                continue;
            }
            for (Shape sh : p.pieces()) {
                Rectangle2D r = sh.getBounds2D();
                r = new Rectangle2D.Double(r.getX() - grow, r.getY() - grow, r.getWidth() + 2 * grow, r.getHeight() + 2 * grow);
                b = b == null ? r : b.createUnion(r);
            }
        }
        double pad = 3.0 * u;
        int ax = (int) Math.ceil(-b.getMinX() * u + pad), ay = (int) Math.ceil(-b.getMinY() * u + pad);
        double sh = half[0] ? 0.5 : 0;
        double ox = ax + sh, oy = ay + sh;
        int w = (int) Math.ceil(ox + b.getMaxX() * u + pad), h = (int) Math.ceil(oy + b.getMaxY() * u + pad);
        AffineTransform at = new AffineTransform(u, 0, 0, u, ox, oy);
        int[] buf = new int[w * h];
        AffineTransform drop = AffineTransform.getTranslateInstance(0.25 * u, 0.8 * u);
        drop.concatenate(at);
        over(buf, blur(mask(w, h, drop, parts, ch), w, h, 0.85 * u), 0x000000, look == Look.SMOG ? 0.5 : 0.42);
        if (ch) over(buf, blur(mask(w, h, at, parts, ch), w, h, 1.1 * u), HontunTheme.accent(), kind == Kind.HAND ? 0.8 : 0.55);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, w, h, buf, 0, w);
        Graphics2D g = gfx(img, at);
        if (look == Look.FLAT) {
            g.setColor(c(HontunTheme.lerp(HontunTheme.accentLo(), 0x000000, 0.55f), 0xDC));
            for (Part p : parts) sil(g, p, px(0.5, u), false);
        }
        for (Part p : parts) {
            int[] col = colors(look, p.role());
            g.setColor(c(col[0], 255));
            sil(g, p, 0, ch);
            g.setColor(c(col[1], 255));
            body(g, p);
            if (p.body() != null) {
                double[] d = dartPts();
                Path2D f = new Path2D.Double();
                f.moveTo(0, 0);
                f.lineTo(d[4], d[5]);
                f.lineTo(d[6] * 1.3, d[7] * 1.3);
                f.lineTo(d[6] * 1.3, -2);
                f.closePath();
                Area fold = new Area(f);
                fold.intersect(p.body());
                boolean light = luma(col[1]) > 0.85;
                if (light) {
                    Area rest = new Area(p.body());
                    rest.subtract(fold);
                    fold = rest;
                }
                g.setColor(c(facet(look, p.role(), col[1], light), 255));
                g.fill(fold);
            }
        }
        if (ch && kind == Kind.ARROW) {
            Path2D t = new Path2D.Double();
            t.moveTo(0, 0);
            t.lineTo(0, 5.0);
            t.lineTo(3.6, 3.6);
            t.closePath();
            Area spark = new Area(t);
            spark.intersect(parts.get(0).sil());
            spark.subtract(parts.get(0).body());
            g.setColor(c(HontunTheme.accentHi(), 255));
            g.fill(spark);
        }
        g.dispose();
        return new Art(img, Math.max(0, Math.min(w - 1, ax)), Math.max(0, Math.min(h - 1, ay)));
    }

    private static float[] mask(int w, int h, AffineTransform at, List<Part> parts, boolean ch) {
        BufferedImage m = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = gfx(m, at);
        g.setColor(Color.WHITE);
        for (Part p : parts) sil(g, p, 0, ch);
        g.dispose();
        int[] px = m.getRGB(0, 0, w, h, null, 0, w);
        float[] a = new float[w * h];
        for (int i = 0; i < a.length; i++) a[i] = (px[i] >>> 24) / 255f;
        return a;
    }

    private static float[] blur(float[] a, int w, int h, double sigma) {
        int r = Math.max(1, (int) Math.ceil(sigma * 2.5));
        float[] k = new float[2 * r + 1];
        float sum = 0;
        for (int i = -r; i <= r; i++) {
            k[i + r] = (float) Math.exp(-i * i / (2 * sigma * sigma));
            sum += k[i + r];
        }
        for (int i = 0; i < k.length; i++) k[i] /= sum;
        float[] t = new float[w * h], o = new float[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float v = 0;
                for (int i = -r; i <= r; i++) {
                    int xx = x + i;
                    if (xx >= 0 && xx < w) v += a[y * w + xx] * k[i + r];
                }
                t[y * w + x] = v;
            }
        }
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float v = 0;
                for (int i = -r; i <= r; i++) {
                    int yy = y + i;
                    if (yy >= 0 && yy < h) v += t[yy * w + x] * k[i + r];
                }
                o[y * w + x] = v;
            }
        }
        return o;
    }

    private static void over(int[] buf, float[] a, int rgb, double k) {
        int sr = (rgb >> 16) & 255, sg = (rgb >> 8) & 255, sb = rgb & 255;
        for (int i = 0; i < buf.length; i++) {
            double sa = Math.min(1, a[i] * k);
            if (sa <= 0.002) continue;
            int d = buf[i];
            double da = (d >>> 24) / 255.0;
            double f = da * (1 - sa);
            double oa = sa + f;
            int r = (int) Math.round((sr * sa + ((d >> 16) & 255) * f) / oa);
            int g = (int) Math.round((sg * sa + ((d >> 8) & 255) * f) / oa);
            int bl = (int) Math.round((sb * sa + (d & 255) * f) / oa);
            buf[i] = ((int) Math.round(oa * 255) << 24) | (r << 16) | (g << 8) | bl;
        }
    }

    private static final String[] PX_DART = {
            "BB...........",
            "BhBB.........",
            ".BhhBB.......",
            ".BhfhhBB.....",
            "..BhflhhBB...",
            "..BhffllhhBB.",
            "...BhfflllhhB",
            "...BhfffddddB",
            "....BhfdBBBB.",
            "....BhfdB....",
            ".....BhdB....",
            ".....BhdB....",
            "......BB.....",
    };

    private static final String[] PX_IBEAM = {
            "hffff",
            "..f..",
            "..f..",
            "..f..",
            "..f..",
            "..f..",
            "..f..",
            "..f..",
            "..f..",
            "..f..",
            "hffff",
    };

    private static final String[] PX_CROSS = {
            ".....f.....",
            ".....f.....",
            "...........",
            "...........",
            "...........",
            "ff...h...ff",
            "...........",
            "...........",
            "...........",
            ".....f.....",
            ".....f.....",
    };

    private static final String[] PX_EW = {
            "..h.......h..",
            ".hf.......fh.",
            "hff...h...fff",
            ".df.......fd.",
            "..d.......d..",
    };

    private static final int[][] PX_RAYS = {{0, -4}, {0, -5}, {-4, 0}, {-5, 0}, {-3, -3}, {-4, -4}};

    private static final String[] PX_HEAD = {
            "..h",
            ".hf",
            "hff",
            ".df",
            "..d",
    };

    private static char[][] grid(String[] rows) {
        char[][] g = new char[rows.length][];
        for (int i = 0; i < rows.length; i++) g[i] = rows[i].toCharArray();
        return g;
    }

    private static boolean solid(char ch) {
        return ch != '.' && ch != 'B';
    }

    private static char[][] blank(int w, int h) {
        char[][] g = new char[h][w];
        for (char[] row : g) Arrays.fill(row, '.');
        return g;
    }

    private static char[][] edge(char[][] g) {
        int h = g.length, w = g[0].length;
        char[][] o = new char[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                o[y][x] = g[y][x];
                if (g[y][x] != '.') continue;
                boolean n = (x > 0 && solid(g[y][x - 1])) || (x < w - 1 && solid(g[y][x + 1]))
                        || (y > 0 && solid(g[y - 1][x])) || (y < h - 1 && solid(g[y + 1][x]));
                if (n) o[y][x] = 'B';
            }
        }
        return o;
    }

    private static char[][] outline(char[][] f) {
        char[][] g = blank(f[0].length + 2, f.length + 2);
        for (int y = 0; y < f.length; y++) System.arraycopy(f[y], 0, g[y + 1], 1, f[y].length);
        return edge(g);
    }

    private static char[][] transpose(char[][] g) {
        int h = g.length, w = g[0].length;
        char[][] o = new char[w][h];
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) o[x][y] = g[y][x];
        return o;
    }

    private static char[][] fourWay() {
        int n = 15;
        char[][] g = blank(n, n);
        for (int y = 0; y < PX_HEAD.length; y++) {
            for (int x = 0; x < PX_HEAD[y].length(); x++) {
                char t = PX_HEAD[y].charAt(x);
                if (t == '.') continue;
                char flip = t == 'h' ? 'd' : t == 'd' ? 'h' : t;
                g[y + 5][x] = t;
                g[y + 5][n - 1 - x] = t;
                g[x][y + 5] = t;
                g[n - 1 - x][y + 5] = flip;
            }
        }
        g[7][7] = 'h';
        return outline(g);
    }

    private static char[][] noGrid() {
        int n = 13;
        double c = (n - 1) / 2.0;
        char[][] g = blank(n, n);
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                double d = Math.hypot(x - c, y - c);
                boolean ring = d >= 4.4 && d <= 6.2;
                boolean bar = x == y && d < 4.4;
                if (!ring && !bar) continue;
                g[y][x] = x + y < n - 1 ? 'h' : 'f';
                if (ring && x + y > n + 3) g[y][x] = 'd';
            }
        }
        return outline(g);
    }

    private static Art pixel(Kind kind, int s) {
        char[][] g;
        switch (kind) {
            case ARROW -> {
                return paintGrid(grid(PX_DART), s, 0, 0);
            }
            case HAND -> {
                return pixelHand(s);
            }
            case IBEAM -> g = outline(grid(PX_IBEAM));
            case CROSSHAIR -> g = outline(grid(PX_CROSS));
            case RESIZE_EW -> g = outline(grid(PX_EW));
            case RESIZE_NS -> g = transpose(outline(grid(PX_EW)));
            case RESIZE_ALL -> g = fourWay();
            default -> g = noGrid();
        }
        return paintGrid(g, s, g[0].length / 2, g.length / 2);
    }

    private static Art pixelHand(int s) {
        char[][] d = grid(PX_DART);
        for (char[] row : d) {
            for (int x = 0; x < row.length; x++) {
                row[x] = switch (row[x]) {
                    case 'h' -> 'W';
                    case 'f' -> 'w';
                    case 'd' -> 'g';
                    case 'l' -> 'W';
                    default -> row[x];
                };
            }
        }
        int m = 7, dw = d[0].length, dh = d.length;
        int gw = dw + m, gh = dh + m;
        char[][] g = blank(gw, gh);
        for (int[] c : PX_RAYS) g[m + c[1]][m + c[0]] = 'f';
        char[][] o = edge(g);
        for (int y = 0; y < dh; y++) {
            for (int x = 0; x < dw; x++) {
                if (d[y][x] != '.') o[y + m][x + m] = d[y][x];
            }
        }
        int minX = gw, minY = gh;
        for (int y = 0; y < gh; y++) {
            for (int x = 0; x < gw; x++) {
                if (o[y][x] != '.') {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                }
            }
        }
        char[][] cut = new char[gh - minY][gw - minX];
        for (int y = minY; y < gh; y++) System.arraycopy(o[y], minX, cut[y - minY], 0, gw - minX);
        return paintGrid(cut, s, m - minX, m - minY);
    }

    private static Art paintGrid(char[][] rows, int s, int hx, int hy) {
        int gw = rows[0].length, gh = rows.length;
        int sh = Math.max(1, s / 2);
        BufferedImage img = new BufferedImage(gw * s + sh, gh * s + sh, BufferedImage.TYPE_INT_ARGB);
        int acc = HontunTheme.accent();
        int fill = 0xFF000000 | acc;
        int light = 0xFF000000 | HontunTheme.accentHi();
        int dark = 0xFF000000 | HontunTheme.lerp(acc, HontunTheme.accentLo(), 0.55f);
        int edge = 0xFF000000;
        for (int y = 0; y < gh; y++) {
            for (int x = 0; x < gw; x++) {
                if (rows[y][x] == '.') continue;
                rect(img, x * s + sh, y * s + sh, s, s, 0x55000000, true);
            }
        }
        for (int y = 0; y < gh; y++) {
            for (int x = 0; x < gw; x++) {
                int col = switch (rows[y][x]) {
                    case 'B' -> edge;
                    case 'h' -> light;
                    case 'f' -> fill;
                    case 'd' -> dark;
                    case 'l' -> 0xFF000000 | HontunTheme.lerp(acc, HontunTheme.accentHi(), 0.5f);
                    case 'W' -> 0xFFFFFFFF;
                    case 'w' -> 0xFFE9EAEE;
                    case 'g' -> 0xFFB2B4BC;
                    default -> 0;
                };
                if (col != 0) rect(img, x * s, y * s, s, s, col, false);
            }
        }
        return new Art(img, Math.min(img.getWidth() - 1, hx * s + s / 2), Math.min(img.getHeight() - 1, hy * s + s / 2));
    }

    private static void rect(BufferedImage img, int x, int y, int w, int h, int argb, boolean under) {
        for (int yy = y; yy < y + h && yy < img.getHeight(); yy++) {
            for (int xx = x; xx < x + w && xx < img.getWidth(); xx++) {
                if (under && (img.getRGB(xx, yy) >>> 24) != 0) continue;
                img.setRGB(xx, yy, argb);
            }
        }
    }
}
