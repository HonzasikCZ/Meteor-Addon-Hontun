package cz.honzasik.hontun.gui.cursor;

import cz.honzasik.hontun.utils.HontunTheme;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
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
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public final class CursorArt {
    public enum Kind { ARROW, HAND, IBEAM, CROSSHAIR, RESIZE_NS, RESIZE_EW, RESIZE_ALL, NOT_ALLOWED }

    public record Art(BufferedImage image, int hotX, int hotY) {}

    private enum Look { PIXEL, FLAT, CHAMFER, SMOG }

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

    private static Shape shape(Kind kind) {
        return switch (kind) {
            case ARROW -> arrow();
            case HAND -> hand();
            case IBEAM -> ibeam();
            case CROSSHAIR -> cross();
            case RESIZE_NS -> rotate(doubleArrow(), Math.PI / 2, 0, 0);
            case RESIZE_EW -> doubleArrow();
            case RESIZE_ALL -> fourWay();
            case NOT_ALLOWED -> notAllowed();
        };
    }

    private static double[] hot(Kind kind) {
        return switch (kind) {
            case ARROW -> new double[]{0, 0};
            case HAND -> new double[]{3.6, 0};
            default -> new double[]{0, 0};
        };
    }

    private static Path2D arrow() {
        Path2D p = new Path2D.Double();
        p.moveTo(0, 0);
        p.lineTo(0, 14.2);
        p.lineTo(3.5, 10.9);
        p.lineTo(5.9, 16.2);
        p.lineTo(8.1, 15.2);
        p.lineTo(5.8, 10.1);
        p.lineTo(10.4, 10.1);
        p.closePath();
        return p;
    }

    private static Shape hand() {
        Area a = new Area(new RoundRectangle2D.Double(2.4, 0, 2.6, 9.5, 2.6, 2.6));
        a.add(new Area(new RoundRectangle2D.Double(5.0, 5.0, 2.5, 5.5, 2.4, 2.4)));
        a.add(new Area(new RoundRectangle2D.Double(7.5, 5.6, 2.5, 5.2, 2.4, 2.4)));
        a.add(new Area(new RoundRectangle2D.Double(10.0, 6.4, 2.3, 4.8, 2.2, 2.2)));
        a.add(new Area(new RoundRectangle2D.Double(2.4, 7.6, 9.9, 6.8, 3.6, 3.6)));
        Path2D thumb = new Path2D.Double();
        thumb.moveTo(2.6, 8.2);
        thumb.lineTo(0.4, 7.0);
        thumb.quadTo(-0.6, 7.6, 0.0, 8.8);
        thumb.lineTo(2.8, 12.6);
        thumb.closePath();
        a.add(new Area(thumb));
        return a;
    }

    private static Shape ibeam() {
        Area a = new Area(new Rectangle2D.Double(-0.6, -6.5, 1.2, 13));
        a.add(new Area(new Rectangle2D.Double(-3.0, -7.3, 6.0, 1.3)));
        a.add(new Area(new Rectangle2D.Double(-3.0, 6.0, 6.0, 1.3)));
        return a;
    }

    private static Shape cross() {
        Area a = new Area(new Rectangle2D.Double(-0.6, -7, 1.2, 5));
        a.add(new Area(new Rectangle2D.Double(-0.6, 2, 1.2, 5)));
        a.add(new Area(new Rectangle2D.Double(-7, -0.6, 5, 1.2)));
        a.add(new Area(new Rectangle2D.Double(2, -0.6, 5, 1.2)));
        return a;
    }

    private static Path2D doubleArrow() {
        Path2D p = new Path2D.Double();
        p.moveTo(-8, 0);
        p.lineTo(-4.4, -3.6);
        p.lineTo(-4.4, -1.2);
        p.lineTo(4.4, -1.2);
        p.lineTo(4.4, -3.6);
        p.lineTo(8, 0);
        p.lineTo(4.4, 3.6);
        p.lineTo(4.4, 1.2);
        p.lineTo(-4.4, 1.2);
        p.lineTo(-4.4, 3.6);
        p.closePath();
        return p;
    }

    private static Shape fourWay() {
        Area a = new Area(doubleArrow());
        a.add(new Area(rotate(doubleArrow(), Math.PI / 2, 0, 0)));
        return a;
    }

    private static Shape notAllowed() {
        Area ring = new Area(new Ellipse2D.Double(-6.5, -6.5, 13, 13));
        ring.subtract(new Area(new Ellipse2D.Double(-4.6, -4.6, 9.2, 9.2)));
        Shape bar = rotate(new Rectangle2D.Double(-5.4, -0.95, 10.8, 1.9), Math.PI / 4, 0, 0);
        ring.add(new Area(bar));
        return ring;
    }

    private static Shape rotate(Shape s, double a, double cx, double cy) {
        return AffineTransform.getRotateInstance(a, cx, cy).createTransformedShape(s);
    }

    private static Color c(int rgb, int a) {
        return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, Math.max(0, Math.min(255, a)));
    }

    private static Art smooth(Kind kind, Look look, int s) {
        double u = s * 0.72;
        Shape base = shape(kind);
        double[] hs = hot(kind);
        boolean centered = kind != Kind.ARROW && kind != Kind.HAND;
        Rectangle2D b = base.getBounds2D();
        double pad = 3.0 * u;
        int w = (int) Math.ceil(b.getWidth() * u + pad * 2), h = (int) Math.ceil(b.getHeight() * u + pad * 2);
        double ox = pad - b.getX() * u, oy = pad - b.getY() * u;
        AffineTransform at = new AffineTransform(u, 0, 0, u, ox, oy);
        Shape sh = at.createTransformedShape(base);

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        shadow(img, sh, u, look == Look.SMOG ? 0x60 : 0x48);

        int acc = HontunTheme.accent(), hi = HontunTheme.accentHi(), lo = HontunTheme.accentLo();
        float stroke = (float) Math.max(1.0, 0.9 * u);
        switch (look) {
            case SMOG -> {
                g.setColor(c(0xFFFFFF, 255));
                g.fill(sh);
                g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(c(0x0B0B0D, 255));
                g.draw(sh);
            }
            case FLAT -> {
                Rectangle2D sb = sh.getBounds2D();
                g.setPaint(new GradientPaint((float) sb.getX(), (float) sb.getY(), c(hi, 255),
                        (float) sb.getX(), (float) sb.getMaxY(), c(acc, 255)));
                g.fill(sh);
                g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(c(0xFFFFFF, 0xF0));
                g.draw(sh);
            }
            case CHAMFER -> {
                glow(img, sh, acc, u);
                g.setColor(c(0x0C1018, 0xF2));
                g.fill(sh);
                g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
                g.setColor(c(acc, 255));
                g.draw(sh);
                if (kind == Kind.ARROW) {
                    Path2D tip = new Path2D.Double();
                    tip.moveTo(0, 0);
                    tip.lineTo(0, 5.2);
                    tip.lineTo(3.7, 3.7);
                    tip.closePath();
                    g.setColor(c(hi, 255));
                    g.fill(at.createTransformedShape(tip));
                } else {
                    g.setColor(c(hi, 0xC0));
                    g.setStroke(new BasicStroke((float) Math.max(1.0, 0.5 * u)));
                    Area inner = new Area(sh);
                    g.setClip(inner);
                    g.draw(AffineTransform.getTranslateInstance(0, 0.6 * u).createTransformedShape(sh));
                    g.setClip(null);
                }
            }
            default -> {
            }
        }
        g.dispose();
        int hx = (int) Math.round(centered ? ox : hs[0] * u + ox);
        int hy = (int) Math.round(centered ? oy : hs[1] * u + oy);
        return new Art(img, Math.max(0, Math.min(w - 1, hx)), Math.max(0, Math.min(h - 1, hy)));
    }

    private static void shadow(BufferedImage dst, Shape sh, double u, int alpha) {
        int w = dst.getWidth(), h = dst.getHeight();
        BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tmp.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0, 0, 0, alpha));
        g.fill(AffineTransform.getTranslateInstance(0.5 * u, 0.9 * u).createTransformedShape(sh));
        g.dispose();
        BufferedImage blurred = blur(tmp, Math.max(1, (int) Math.round(u)));
        Graphics2D d = dst.createGraphics();
        d.setComposite(AlphaComposite.SrcOver);
        d.drawImage(blurred, 0, 0, null);
        d.dispose();
    }

    private static void glow(BufferedImage dst, Shape sh, int rgb, double u) {
        int w = dst.getWidth(), h = dst.getHeight();
        BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tmp.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke((float) (1.6 * u)));
        g.setColor(c(rgb, 0x70));
        g.draw(sh);
        g.dispose();
        BufferedImage blurred = blur(tmp, Math.max(1, (int) Math.round(1.2 * u)));
        Graphics2D d = dst.createGraphics();
        d.drawImage(blurred, 0, 0, null);
        d.dispose();
    }

    private static BufferedImage blur(BufferedImage src, int radius) {
        int n = radius * 2 + 1;
        float[] k = new float[n];
        double sigma = Math.max(0.6, radius / 2.0), sum = 0;
        for (int i = 0; i < n; i++) {
            double x = i - radius;
            k[i] = (float) Math.exp(-x * x / (2 * sigma * sigma));
            sum += k[i];
        }
        for (int i = 0; i < n; i++) k[i] /= (float) sum;
        BufferedImage a = new ConvolveOp(new Kernel(n, 1, k), ConvolveOp.EDGE_NO_OP, null).filter(src, null);
        return new ConvolveOp(new Kernel(1, n, k), ConvolveOp.EDGE_NO_OP, null).filter(a, null);
    }

    private static final String[] PX_ARROW = {
            "B..........",
            "BB.........",
            "BhB........",
            "BhfB.......",
            "BhffB......",
            "BhfffB.....",
            "BhffffB....",
            "BhfffffB...",
            "BhffffffB..",
            "BhfffffffB.",
            "BhffffBBBBB",
            "BhffBfB....",
            "BhfBBfB....",
            "BfB..BfB...",
            "BB...BfB...",
            "B.....BfB..",
            "......BBB..",
    };

    private static final String[] PX_HAND = {
            "...BB.......",
            "..BhfB......",
            "..BhfB......",
            "..BhfB......",
            "..BhfBBB....",
            "..BhfBhfBB..",
            "BBBhfBhfBhB.",
            "BhBhfffffhfB",
            "BhfBfffffffB",
            ".BhffffffffB",
            ".BhffffffffB",
            "..BffffffffB",
            "..BfffffffB.",
            "...BffffffB.",
            "...BffffffB.",
            "...BBBBBBBB.",
    };

    private static final String[] PX_IBEAM = {
            "BBB.BBB",
            "BhfBfhB",
            "BBBfBBB",
            "..BfB..",
            "..BfB..",
            "..BfB..",
            "..BfB..",
            "..BfB..",
            "..BfB..",
            "..BfB..",
            "BBBfBBB",
            "BhfBfhB",
            "BBB.BBB",
    };

    private static final String[] PX_CROSS = {
            "....BBB....",
            "....BfB....",
            "....BfB....",
            "....BBB....",
            "BBBB...BBBB",
            "BffB...BffB",
            "BBBB...BBBB",
            "....BBB....",
            "....BfB....",
            "....BfB....",
            "....BBB....",
    };

    private static final String[] PX_EW = {
            "...B.......B...",
            "..BhB.....BfB..",
            ".BhfBBBBBBBffB.",
            "BhfffffffffffhB",
            ".BffBBBBBBBffB.",
            "..BfB.....BfB..",
            "...B.......B...",
    };

    private static String[] notAllowedGrid() {
        int n = 13;
        double c = (n - 1) / 2.0;
        char[][] g = new char[n][n];
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                double d = Math.hypot(x - c, y - c);
                boolean ring = d >= 3.6 && d <= 5.4;
                boolean bar = Math.abs(x - y) <= 0 && d < 4.2;
                g[y][x] = ring || bar ? 'f' : '.';
            }
        }
        char[][] o = new char[n][n];
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                o[y][x] = g[y][x];
                if (g[y][x] != '.') continue;
                for (int dy = -1; dy <= 1 && o[y][x] == '.'; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int xx = x + dx, yy = y + dy;
                        if (xx >= 0 && yy >= 0 && xx < n && yy < n && g[yy][xx] == 'f') {
                            o[y][x] = 'B';
                            break;
                        }
                    }
                }
            }
        }
        String[] rows = new String[n];
        for (int y = 0; y < n; y++) rows[y] = new String(o[y]);
        return rows;
    }

    private static Art pixel(Kind kind, int s) {
        String[] rows;
        int hx, hy;
        boolean rotate = false;
        switch (kind) {
            case HAND -> { rows = PX_HAND; hx = 3; hy = 0; }
            case IBEAM -> { rows = PX_IBEAM; hx = 3; hy = 6; }
            case CROSSHAIR -> { rows = PX_CROSS; hx = 5; hy = 5; }
            case RESIZE_EW -> { rows = PX_EW; hx = 7; hy = 3; }
            case RESIZE_NS -> { rows = PX_EW; hx = 3; hy = 7; rotate = true; }
            case RESIZE_ALL -> { return pixelFourWay(s); }
            case NOT_ALLOWED -> { rows = notAllowedGrid(); hx = 6; hy = 6; }
            default -> { rows = PX_ARROW; hx = 0; hy = 0; }
        }
        if (rotate) rows = transpose(rows);
        return paintGrid(rows, s, hx, hy);
    }

    private static Art pixelFourWay(int s) {
        String[] ew = PX_EW;
        String[] ns = transpose(PX_EW);
        int n = Math.max(ew[0].length(), ns.length);
        char[][] grid = new char[n][n];
        for (char[] row : grid) java.util.Arrays.fill(row, '.');
        overlay(grid, ew, (n - ew[0].length()) / 2, (n - ew.length) / 2);
        overlay(grid, ns, (n - ns[0].length()) / 2, (n - ns.length) / 2);
        String[] rows = new String[n];
        for (int i = 0; i < n; i++) rows[i] = new String(grid[i]);
        return paintGrid(rows, s, n / 2, n / 2);
    }

    private static void overlay(char[][] grid, String[] rows, int ox, int oy) {
        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < rows[y].length(); x++) {
                char ch = rows[y].charAt(x);
                if (ch == '.') continue;
                char cur = grid[oy + y][ox + x];
                if (cur == '.' || ch != 'B') grid[oy + y][ox + x] = ch;
            }
        }
    }

    private static String[] transpose(String[] rows) {
        int h = rows.length, w = rows[0].length();
        String[] out = new String[w];
        for (int x = 0; x < w; x++) {
            StringBuilder sb = new StringBuilder();
            for (int y = 0; y < h; y++) sb.append(rows[y].charAt(x));
            out[x] = sb.toString();
        }
        return out;
    }

    private static Art paintGrid(String[] rows, int s, int hx, int hy) {
        int gw = rows[0].length(), gh = rows.length;
        int sh = Math.max(1, s / 2);
        BufferedImage img = new BufferedImage(gw * s + sh, gh * s + sh, BufferedImage.TYPE_INT_ARGB);
        int fill = 0xFF000000 | HontunTheme.accent();
        int light = 0xFF000000 | HontunTheme.accentHi();
        int edge = 0xFF000000;
        for (int y = 0; y < gh; y++) {
            for (int x = 0; x < gw; x++) {
                if (rows[y].charAt(x) == '.') continue;
                rect(img, x * s + sh, y * s + sh, s, s, 0x55000000, true);
            }
        }
        for (int y = 0; y < gh; y++) {
            for (int x = 0; x < gw; x++) {
                char ch = rows[y].charAt(x);
                int col = switch (ch) {
                    case 'B' -> edge;
                    case 'h' -> light;
                    case 'f' -> fill;
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
