package cz.honzasik.hontun.captcha;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CaptchaNet {
    public static final class Result {
        public final String text;
        public final float confidence;
        public final int[] argmax;

        Result(String text, float confidence, int[] argmax) {
            this.text = text;
            this.confidence = confidence;
            this.argmax = argmax;
        }
    }

    private static final class Op {
        String type;
        int in, out, kh, kw, ph, pw, sh, sw, wOff, bOff;
    }

    private static final class Tensor {
        float[] d;
        int c, h, w;

        Tensor(int c, int h, int w) {
            this.c = c;
            this.h = h;
            this.w = w;
            this.d = new float[c * h * w];
        }
    }

    private final String classes;
    private final int blank;
    private final int inH, inW;
    private final double gr, gg, gb, scale, bias;
    private final List<Op> ops;
    private final float[] weights;

    private CaptchaNet(String classes, int blank, int inH, int inW,
                       double gr, double gg, double gb, double scale, double bias,
                       List<Op> ops, float[] weights) {
        this.classes = classes;
        this.blank = blank;
        this.inH = inH;
        this.inW = inW;
        this.gr = gr;
        this.gg = gg;
        this.gb = gb;
        this.scale = scale;
        this.bias = bias;
        this.ops = ops;
        this.weights = weights;
    }

    public int width() { return inW; }

    public int height() { return inH; }

    public static CaptchaNet load(InputStream jsonStream, InputStream weightsStream) throws IOException {
        Object root = new Json(readAll(jsonStream, true)).parse();
        Map<?, ?> m = (Map<?, ?>) root;
        String classes = (String) m.get("classes");
        int blank = ((Number) m.get("blank")).intValue();
        List<?> in = (List<?>) m.get("input");
        int inH = ((Number) in.get(1)).intValue();
        int inW = ((Number) in.get(2)).intValue();
        Map<?, ?> pre = (Map<?, ?>) m.get("preprocess");
        List<?> gray = (List<?>) pre.get("gray");
        double gr = ((Number) gray.get(0)).doubleValue();
        double gg = ((Number) gray.get(1)).doubleValue();
        double gb = ((Number) gray.get(2)).doubleValue();
        double scale = ((Number) pre.get("scale")).doubleValue();
        double bias = ((Number) pre.get("bias")).doubleValue();
        List<Op> ops = new ArrayList<>();
        for (Object o : (List<?>) m.get("ops")) {
            Map<?, ?> om = (Map<?, ?>) o;
            Op op = new Op();
            op.type = (String) om.get("type");
            if (op.type.equals("conv")) {
                op.in = i(om, "in");
                op.out = i(om, "out");
                op.kh = i(om, "kh");
                op.kw = i(om, "kw");
                op.ph = i(om, "ph");
                op.pw = i(om, "pw");
                op.sh = i(om, "sh");
                op.sw = i(om, "sw");
                op.wOff = i(om, "wOffset");
                op.bOff = i(om, "bOffset");
            } else if (op.type.equals("maxpool")) {
                op.kh = i(om, "kh");
                op.kw = i(om, "kw");
                op.sh = i(om, "sh");
                op.sw = i(om, "sw");
            }
            ops.add(op);
        }
        byte[] wb = readAll(weightsStream, false);
        ByteBuffer bb = ByteBuffer.wrap(wb).order(ByteOrder.LITTLE_ENDIAN);
        float[] weights = new float[wb.length / 4];
        bb.asFloatBuffer().get(weights);
        return new CaptchaNet(classes, blank, inH, inW, gr, gg, gb, scale, bias, ops, weights);
    }

    private static int i(Map<?, ?> m, String k) {
        return ((Number) m.get(k)).intValue();
    }

    public int grayOf(int rgb) {
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        int v = (int) Math.floor(gr * r + gg * g + gb * b + 0.5);
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }

    public Result recognizeGray(byte[] gray) {
        Tensor t = new Tensor(1, inH, inW);
        for (int k = 0; k < inH * inW; k++) {
            t.d[k] = (float) ((gray[k] & 0xFF) * scale + bias);
        }
        for (Op op : ops) {
            if (op.type.equals("conv")) t = conv(t, op);
            else if (op.type.equals("relu")) relu(t);
            else if (op.type.equals("maxpool")) t = pool(t, op);
        }
        int steps = t.w;
        int cls = t.c;
        int[] argmax = new int[steps];
        float[] stepConf = new float[steps];
        for (int x = 0; x < steps; x++) {
            int best = 0;
            float bestV = Float.NEGATIVE_INFINITY;
            double sum = 0;
            float max = Float.NEGATIVE_INFINITY;
            for (int ccl = 0; ccl < cls; ccl++) {
                float v = t.d[ccl * steps + x];
                if (v > max) max = v;
            }
            for (int ccl = 0; ccl < cls; ccl++) {
                float v = t.d[ccl * steps + x];
                sum += Math.exp(v - max);
                if (v > bestV) {
                    bestV = v;
                    best = ccl;
                }
            }
            argmax[x] = best;
            stepConf[x] = (float) (Math.exp(bestV - max) / sum);
        }
        StringBuilder sb = new StringBuilder();
        float conf = 1f;
        int prev = -1;
        for (int x = 0; x < steps; x++) {
            int a = argmax[x];
            if (a != prev && a != blank) {
                sb.append(classes.charAt(a));
                conf = Math.min(conf, stepConf[x]);
            }
            prev = a;
        }
        if (sb.length() == 0) conf = 0f;
        return new Result(sb.toString(), conf, argmax);
    }

    private Tensor conv(Tensor in, Op op) {
        int outH = (in.h + 2 * op.ph - op.kh) / op.sh + 1;
        int outW = (in.w + 2 * op.pw - op.kw) / op.sw + 1;
        Tensor out = new Tensor(op.out, outH, outW);
        int ic = in.c, ih = in.h, iw = in.w;
        for (int o = 0; o < op.out; o++) {
            float b = weights[op.bOff + o];
            int wBase = op.wOff + o * ic * op.kh * op.kw;
            for (int oy = 0; oy < outH; oy++) {
                int iy0 = oy * op.sh - op.ph;
                for (int ox = 0; ox < outW; ox++) {
                    int ix0 = ox * op.sw - op.pw;
                    float acc = b;
                    for (int c = 0; c < ic; c++) {
                        int inC = c * ih * iw;
                        int wC = wBase + c * op.kh * op.kw;
                        for (int ky = 0; ky < op.kh; ky++) {
                            int iy = iy0 + ky;
                            if (iy < 0 || iy >= ih) continue;
                            int inRow = inC + iy * iw;
                            int wRow = wC + ky * op.kw;
                            for (int kx = 0; kx < op.kw; kx++) {
                                int ix = ix0 + kx;
                                if (ix < 0 || ix >= iw) continue;
                                acc += in.d[inRow + ix] * weights[wRow + kx];
                            }
                        }
                    }
                    out.d[(o * outH + oy) * outW + ox] = acc;
                }
            }
        }
        return out;
    }

    private void relu(Tensor t) {
        for (int k = 0; k < t.d.length; k++) if (t.d[k] < 0) t.d[k] = 0;
    }

    private Tensor pool(Tensor in, Op op) {
        int outH = (in.h - op.kh) / op.sh + 1;
        int outW = (in.w - op.kw) / op.sw + 1;
        Tensor out = new Tensor(in.c, outH, outW);
        for (int c = 0; c < in.c; c++) {
            int inC = c * in.h * in.w;
            for (int oy = 0; oy < outH; oy++) {
                for (int ox = 0; ox < outW; ox++) {
                    float max = Float.NEGATIVE_INFINITY;
                    for (int ky = 0; ky < op.kh; ky++) {
                        int iy = oy * op.sh + ky;
                        int inRow = inC + iy * in.w;
                        for (int kx = 0; kx < op.kw; kx++) {
                            float v = in.d[inRow + ox * op.sw + kx];
                            if (v > max) max = v;
                        }
                    }
                    out.d[(c * outH + oy) * outW + ox] = max;
                }
            }
        }
        return out;
    }

    private static byte[] readAll(InputStream s, boolean close) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = s.read(buf)) != -1) bos.write(buf, 0, n);
        if (close) s.close();
        return bos.toByteArray();
    }

    private static final class Json {
        private final String s;
        private int p;

        Json(byte[] bytes) {
            this.s = new String(bytes, StandardCharsets.UTF_8);
        }

        Object parse() {
            ws();
            return value();
        }

        private Object value() {
            char c = s.charAt(p);
            if (c == '{') return obj();
            if (c == '[') return arr();
            if (c == '"') return str();
            if (c == 't') { p += 4; return Boolean.TRUE; }
            if (c == 'f') { p += 5; return Boolean.FALSE; }
            if (c == 'n') { p += 4; return null; }
            return num();
        }

        private Map<String, Object> obj() {
            java.util.LinkedHashMap<String, Object> m = new java.util.LinkedHashMap<>();
            p++;
            ws();
            if (s.charAt(p) == '}') { p++; return m; }
            while (true) {
                ws();
                String k = str();
                ws();
                p++;
                ws();
                m.put(k, value());
                ws();
                char c = s.charAt(p++);
                if (c == '}') break;
            }
            return m;
        }

        private List<Object> arr() {
            List<Object> l = new ArrayList<>();
            p++;
            ws();
            if (s.charAt(p) == ']') { p++; return l; }
            while (true) {
                ws();
                l.add(value());
                ws();
                char c = s.charAt(p++);
                if (c == ']') break;
            }
            return l;
        }

        private String str() {
            StringBuilder sb = new StringBuilder();
            p++;
            while (true) {
                char c = s.charAt(p++);
                if (c == '"') break;
                if (c == '\\') {
                    char e = s.charAt(p++);
                    switch (e) {
                        case 'n': sb.append('\n'); break;
                        case 't': sb.append('\t'); break;
                        case 'r': sb.append('\r'); break;
                        case 'u':
                            sb.append((char) Integer.parseInt(s.substring(p, p + 4), 16));
                            p += 4;
                            break;
                        default: sb.append(e);
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private Number num() {
            int start = p;
            while (p < s.length()) {
                char c = s.charAt(p);
                if (c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E' || (c >= '0' && c <= '9')) p++;
                else break;
            }
            String t = s.substring(start, p);
            if (t.indexOf('.') >= 0 || t.indexOf('e') >= 0 || t.indexOf('E') >= 0) return Double.parseDouble(t);
            return Long.parseLong(t);
        }

        private void ws() {
            while (p < s.length()) {
                char c = s.charAt(p);
                if (c == ' ' || c == '\n' || c == '\r' || c == '\t') p++;
                else break;
            }
        }
    }
}
