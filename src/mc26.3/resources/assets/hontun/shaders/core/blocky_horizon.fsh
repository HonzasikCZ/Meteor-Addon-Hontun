#version 330
#extension GL_ARB_separate_shader_objects : require

layout(std140) uniform BlockyData {
    vec4 u_Frame;
    vec4 u_Pointer;
    vec4 u_Accent;
    vec4 u_AccentHi;
    vec4 u_AccentLo;
    vec4 u_Base;
};

layout(location = 0) in vec2 v_uv;

layout(location = 0) out vec4 fragColor;

const float OVERLAY_LAND = 0.56;
const vec4 OVERLAY_SOLID = vec4(0.88, 0.72, 0.48, 0.26);
const float OVERLAY_VEIL_LOW = 0.16;
const float OVERLAY_VEIL_HIGH = 0.06;
const float OVERLAY_MOON = 0.90;
const float OVERLAY_CLOUD = 0.55;
const float OVERLAY_CENTRE = 0.08;

vec2 iResolution;
float iTime;
vec2 iMouse;
vec3 uAccent;
vec3 uAccentHi;
vec3 uAccentLo;
vec3 uBase;

vec3 gAcc;
vec3 gHi;
vec3 gLo;
vec3 gBase;
vec3 gHor;
vec3 gDeep;
vec3 gMoon;
vec3 gMist;
float gMf;
float gT;

float hash11(float p) {
    p = fract(p * 0.1031);
    p *= p + 33.33;
    p *= p + p;
    return fract(p);
}

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

vec2 hash22(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * vec3(0.1031, 0.1030, 0.0973));
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.xx + p3.yz) * p3.zy);
}

float vnoise(float x) {
    float i = floor(x);
    float f = x - i;
    float u = f * f * (3.0 - 2.0 * f);
    return mix(hash11(i), hash11(i + 1.0), u);
}

float vnoise2(vec2 p) {
    vec2 i = floor(p);
    vec2 f = p - i;
    vec2 u = f * f * (3.0 - 2.0 * f);
    float a = hash12(i);
    float b = hash12(i + vec2(1.0, 0.0));
    float c = hash12(i + vec2(0.0, 1.0));
    float d = hash12(i + vec2(1.0, 1.0));
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float ss(float a, float b, float x) {
    float t = clamp((x - a) / (b - a), 0.0, 1.0);
    return t * t * (3.0 - 2.0 * t);
}

float luma(vec3 c) {
    return dot(c, vec3(0.2126, 0.7152, 0.0722));
}

vec3 tintNorm(vec3 c, float target) {
    vec3 r = c * (target / max(luma(c), 0.04));
    return r / max(1.0, max(r.r, max(r.g, r.b)));
}

float peakAt(float cc, float xw, float cw, float seed) {
    float px = (cc + 0.15 + 0.7 * hash11(cc * 1.73 + seed)) * cw;
    float ph = 0.42 + 0.58 * hash11(cc * 2.91 + seed + 8.1);
    float sl = 3.7 + 2.6 * hash11(cc * 5.37 + seed + 1.1);
    return ph - abs(xw - px) * sl;
}

float peaks(float xw, float seed) {
    float cw = 0.34;
    float q = xw / cw;
    float ci = floor(q);
    float cj = ci + (q - ci < 0.5 ? -1.0 : 1.0);
    return max(peakAt(ci, xw, cw, seed), peakAt(cj, xw, cw, seed));
}

float groundRows(float c, float bs, float baseR, float ampR, float freqW, float seed, float ridge) {
    float xw = c * bs;
    float x = xw * freqW + seed;
    float n = 0.62 * vnoise(x) + 0.38 * vnoise(x * 2.37 + 13.7);
    n = ss(0.10, 0.90, n);
    float h = n;
    if (ridge > 0.0) {
        float pk = peaks(xw, seed) + 0.10 * (vnoise(xw * 22.0 + seed) - 0.5);
        h = mix(n, clamp(max(pk, 0.32 * n), 0.0, 1.0), ridge);
    }
    return floor(baseR + ampR * h);
}

float pineOcc(float dx, float dy, float top, float mw) {
    if (dy < -0.5 || dy > top + 0.5) return 0.0;
    float a = abs(dx);
    if (a < 0.5) return 1.0;
    if (dy < 1.5) return 0.0;
    float u = top - dy;
    float j = floor(u / 3.0 + 0.001);
    float w = min(j + (u - 3.0 * j), mw);
    return step(a, w + 0.5);
}

float oakOcc(float dx, float dy, float tr, float cutL, float cutR) {
    if (dy < -0.5 || dy > tr + 4.5) return 0.0;
    float a = abs(dx);
    if (a < 0.5 && dy < tr + 3.5) return 1.0;
    float k = dy - tr;
    if (k < -0.5) return 0.0;
    float w = k < 0.5 ? 2.0 : (k < 2.5 ? 3.0 : (k < 3.5 ? 2.0 : 1.0));
    if (a > w + 0.5) return 0.0;
    if (a > w - 0.5 && (k < 0.5 || (k > 2.5 && k < 3.5))) return dx < 0.0 ? cutL : cutR;
    if (a > w - 0.5 && k > 3.5) return dx < 0.0 ? cutR : cutL;
    return 1.0;
}

vec3 mistColor(vec2 p) {
    float mx = exp(-abs(p.x - gMoon.x) * 2.4);
    return gHor * 1.08 + gHi * 0.10 * mx + gAcc * 0.015;
}

vec3 skyColor(vec2 p) {
    vec3 zen = gBase * 0.62 + gLo * 0.025;
    float g = ss(0.98, 0.28, p.y);
    g *= g;
    vec3 c = mix(zen, gHor, g);
    float r = length(p - gMoon.xy);
    float e = max(r - gMoon.z, 0.0);
    c += gHi * (0.24 * exp(-e * 11.0) + 0.09 * exp(-e * 3.2));
    float hx = exp(-abs(p.x - gMoon.x) * 1.5);
    c += gAcc * 0.09 * hx * exp(-max(p.y - 0.30, 0.0) * 6.0);
    return c;
}

vec3 starField(vec2 fc, vec2 p) {
    if (p.y < 0.36) return vec3(0.0);
    float cpx = max(8.0, floor(iResolution.y * 0.045));
    vec2 cell = floor(fc / cpx);
    float hs = hash12(cell + 17.31);
    if (hs > 0.45) return vec3(0.0);
    vec2 h = hash22(cell + 3.7);
    float spx = max(1.0, floor(iResolution.y / 520.0));
    vec2 corner = cell * cpx + floor((0.22 + 0.56 * h) * cpx);
    vec2 rel = floor((fc - corner) / spx);
    float u = hs / 0.45;
    float mag = (1.0 - u) * (1.0 - u) * (1.0 - u);
    float tw = 0.62 + 0.38 * sin(gT * (0.35 + 1.0 * h.x) + 6.2831 * h.y);
    float core = (abs(rel.x) < 0.5 && abs(rel.y) < 0.5) ? 1.0 : 0.0;
    float arm = 0.0;
    if (mag > 0.42) {
        bool hArm = abs(rel.x) < 1.5 && abs(rel.y) < 0.5;
        bool vArm = abs(rel.y) < 1.5 && abs(rel.x) < 0.5;
        arm = (hArm || vArm) ? 1.0 - core : 0.0;
    }
    float halo = 0.0;
    if (mag > 0.12) {
        vec2 dpx = fc - (corner + 0.5 * spx);
        halo = exp(-length(dpx) / (3.0 * spx)) * mag * 0.14;
    }
    float vis = ss(0.36, 0.64, p.y) * ss(gMoon.z * 1.05, gMoon.z * 2.7, length(p - gMoon.xy));
    vis *= 1.0 - 0.35 * exp(-p.x * p.x / 0.04);
    vec3 sc = mix(gHi, vec3(1.0), 0.55);
    return sc * vis * (core * (0.22 + 0.78 * mag) * tw + arm * 0.5 * mag * tw * tw + halo * tw);
}

vec4 moonDisc(vec2 fc) {
    float mp = max(2.0, floor(iResolution.y / 160.0 + 0.5));
    vec2 mc = floor(vec2(0.5 * iResolution.x + gMoon.x * iResolution.y, gMoon.y * iResolution.y) + 0.5);
    float rpx = gMoon.z * iResolution.y;
    vec2 g = floor((fc - mc) / mp);
    vec2 cc = (g + 0.5) * mp;
    float d = length(cc);
    if (d > rpx) return vec4(0.0);
    float maria = 0.75 * vnoise2(g * 0.19 + vec2(4.3, 1.9)) + 0.25 * vnoise2(g * 0.47 + vec2(11.0, 3.0));
    float shade = 1.0;
    shade -= 0.10 * step(0.50, maria);
    shade -= 0.09 * step(0.58, maria);
    shade -= 0.10 * step(0.955, hash12(g + 5.5));
    float lim = d / rpx;
    shade *= 1.0 - 0.26 * lim * lim * lim * lim;
    shade *= 1.0 + 0.07 * clamp((cc.y - cc.x) / rpx, -1.0, 1.0);
    vec3 mcol = mix(gHi, vec3(1.0), 0.42) * 0.93;
    return vec4(mcol * shade, 1.0);
}

float cloudCell(vec2 g) {
    float n = vnoise2(g * vec2(0.085, 0.24) + vec2(3.1, 7.7));
    n += 0.16 * (hash12(g + 13.13) - 0.5);
    return step(0.66, n);
}

vec4 cloudLayer(vec2 fc, vec2 p) {
    float band = ss(0.57, 0.67, p.y) * ss(0.97, 0.86, p.y);
    if (band <= 0.0) return vec4(0.0);
    float cw = max(4.0, floor(iResolution.y / 44.0 + 0.5));
    float ch = max(2.0, floor(iResolution.y / 100.0 + 0.5));
    float x = fc.x + (gT * 0.0045 + (iMouse.x - 0.5) * 0.003) * iResolution.y;
    float gx = x / cw;
    float ci = floor(gx);
    float f = gx - ci;
    float gy = floor(fc.y / ch);
    float side = f < 0.5 ? -1.0 : 1.0;
    float w = clamp(0.5 - min(f, 1.0 - f) * cw, 0.0, 0.5);
    float o = cloudCell(vec2(ci, gy));
    float on = w > 0.0 ? cloudCell(vec2(ci + side, gy)) : o;
    float cov = mix(o, on, w);
    if (cov <= 0.001) return vec4(0.0);
    float up = o > 0.5 ? cloudCell(vec2(ci, gy + 1.0)) : 1.0;
    float dn = o > 0.5 ? cloudCell(vec2(ci, gy - 1.0)) : 1.0;
    float mr = length(p - gMoon.xy);
    vec3 cc = mix(gBase * 1.2, gLo * 0.6, 0.4);
    cc += gHi * 0.05 * (1.0 - up) * o;
    cc *= 1.0 - 0.18 * (1.0 - dn) * o;
    cc += gHi * 0.30 * exp(-mr * 5.0);
    float a = 0.4 * band * cov * (1.0 - 0.6 * exp(-p.x * p.x / 0.035));
    return vec4(cc, a);
}

vec4 terrainLayer(vec2 fc, vec2 p, vec4 A, vec4 B, vec4 C, vec4 D, vec2 E) {
    float bsPx = max(2.0, floor(iResolution.y * A.x + 0.5));
    float bs = bsPx / iResolution.y;
    float baseR = A.y / bs;
    float ampR = A.z / bs;
    float ri = floor(fc.y / bsPx);
    float reach = C.x > 0.0 ? 11.0 + E.y : 1.0;
    if (ri > floor(baseR + ampR) + reach) return vec4(0.0);

    float wx = p.x + gT * B.z + (iMouse.x - 0.5) * B.w;
    float cx = wx / bs;
    float c = floor(cx);
    float f = cx - c;
    float side = f < 0.5 ? -1.0 : 1.0;
    float n = c + side;
    float dEdge = min(f, 1.0 - f) * bsPx;
    float wN = clamp(0.5 - dEdge, 0.0, 0.5);

    bool nearEdge = dEdge < 2.2;
    float hC = groundRows(c, bs, baseR, ampR, A.w, B.x, B.y);
    float hN = nearEdge ? groundRows(n, bs, baseR, ampR, A.w, B.x, B.y) : hC;
    float terr = step(ri, hC - 1.0);
    float occC = terr;
    float occN = step(ri, hN - 1.0);
    float upC = step(ri + 1.0, hC - 1.0);

    bool want = occC < 0.5 || upC < 0.5 || (nearEdge && occN < 0.5);
    if (C.x > 0.0 && want && ri < max(hC, hN) + 12.0 + E.y) {
        float cell0 = ceil((min(c, n) - 6.0) / 5.0);
        for (int k = 0; k < 2; k++) {
            float cell = cell0 + float(k);
            float pres = hash12(vec2(cell, B.x));
            if (pres > C.x) continue;
            if (pres > C.x * ss(0.30, 0.70, vnoise(cell * 0.29 + B.x * 1.37))) continue;
            vec2 hh = hash22(vec2(cell, B.x + 11.0));
            float h4 = fract(hh.x * 37.13 + hh.y * 11.71);
            float tc = cell * 5.0 + 1.0 + floor(hh.x * 3.0);
            float g = groundRows(tc, bs, baseR, ampR, A.w, B.x, B.y);
            float dy = ri - g;
            if (dy < -1.5 || dy > 11.5) continue;
            float oC;
            float oN = 0.0;
            float oU;
            if (hh.y < C.y) {
                float tr = 2.0 + floor(h4 * 2.0);
                float cl = step(0.5, fract(h4 * 7.31));
                float cr = step(0.5, fract(h4 * 13.71));
                oC = oakOcc(c - tc, dy, tr, cl, cr);
                oU = oakOcc(c - tc, dy + 1.0, tr, cl, cr);
                if (nearEdge) oN = oakOcc(n - tc, dy, tr, cl, cr);
            } else {
                float top = 6.0 + E.y + floor(h4 * 5.0);
                float mw = top > 7.5 ? 3.0 : 2.0;
                oC = pineOcc(c - tc, dy, top, mw);
                oU = pineOcc(c - tc, dy + 1.0, top, mw);
                if (nearEdge) oN = pineOcc(n - tc, dy, top, mw);
            }
            occC = max(occC, oC);
            occN = max(occN, oN);
            upC = max(upC, oU);
        }
    }

    float cov = mix(occC, occN, wN);
    if (cov <= 0.001) return vec4(0.0);

    vec3 mist = gMist;
    vec3 col = mix(mist, gDeep, C.z);
    float m = 0.0;
    if (D.z > 0.0) {
        m = D.z * ss(D.x, D.y, p.y);
        if (m > 0.0) m *= 0.55 + 0.9 * vnoise(wx * 2.6 - gT * 0.035 + B.x);
        m = clamp(m, 0.0, 1.0);
        col = mix(col, mist, m);
    }

    if (E.x > 0.5 && terr > 0.5) {
        float sr = floor(baseR + ampR * 0.58 + hash11(c * 0.73 + 3.1) * 2.0);
        float depthS = 1.0 + floor(hash11(c * 1.91 + 7.7) * 2.0) + step(sr + 3.0, ri);
        float s = step(sr, ri) * step(hC - 1.0 - depthS, ri);
        col = mix(col, mix(mist, gHi, 0.30) * 1.08, 0.5 * s);
    }

    if (D.w > 0.0) {
        float blk = hash12(vec2(c, ri) + B.x * 3.17);
        col *= 1.0 + D.w * (blk - 0.5) * 2.0;
        float below = max(hC - 1.0 - ri, 0.0) * terr;
        col *= 1.0 - D.w * 2.0 * clamp(below / 5.0, 0.0, 1.0);
    }

    float exposed = occC * (1.0 - upC);
    col += gAcc * D.w * 0.45 * exposed;

    float mf = gMf;
    float rimT = exposed * ss(2.2, 0.0, (ri + 1.0) * bsPx - fc.y);
    float face = side * (gMoon.x - p.x) > 0.0 ? 1.0 : 0.3;
    float rimS = occC * (1.0 - occN) * ss(2.2, 0.0, dEdge) * face * 0.8;
    col += gHi * C.w * mf * max(rimT, rimS) * 0.55 * (1.0 - m);

    return vec4(col, cov);
}

vec3 fireflies(vec2 fc, vec2 p) {
    if (p.y > 0.36) return vec3(0.0);
    float cs = 0.075;
    vec2 q = vec2(p.x + gT * 0.011 + (iMouse.x - 0.5) * 0.02, p.y);
    vec2 cell = floor(q / cs);
    float hs = hash12(cell + 91.7);
    if (hs > 0.38) return vec3(0.0);
    vec2 h = hash22(cell + 41.3);
    vec2 ctr = (cell + 0.5) * cs + 0.26 * cs * vec2(sin(gT * (0.21 + 0.25 * h.x) + 6.2831 * h.y), sin(gT * (0.17 + 0.22 * h.y) + 6.2831 * h.x));
    vec2 dq = q - ctr;
    vec2 d = dq * iResolution.y;
    float sz = max(1.0, floor(iResolution.y / 640.0));
    float core = clamp(sz + 0.5 - max(abs(d.x), abs(d.y)), 0.0, 1.0);
    float r = length(dq);
    float glow = exp(-r / 0.0042) * ss(0.24 * cs, 0.10 * cs, r);
    float pulse = ss(0.25, 0.95, 0.5 + 0.5 * sin(gT * (0.30 + 0.9 * hs) + 6.2831 * h.x));
    float fade = ss(0.36, 0.22, p.y);
    return mix(gHi, vec3(1.0), 0.25) * (core * 0.85 + glow * 0.32) * pulse * fade;
}

vec4 overlaySky(vec2 fc, vec2 p) {
    float a = mix(OVERLAY_VEIL_LOW, OVERLAY_VEIL_HIGH, ss(0.12, 0.85, p.y));
    float e = max(length(p - gMoon.xy) - gMoon.z, 0.0);
    a += (1.0 - a) * (0.30 * exp(-e * 11.0) + 0.10 * exp(-e * 3.2));
    vec3 c = skyColor(p) * a;
    vec3 st = starField(fc, p);
    float sa = min(max(st.r, max(st.g, st.b)), 1.0);
    c = c * (1.0 - sa) + st;
    a += sa * (1.0 - a);
    vec4 mo = moonDisc(fc);
    float ma = mo.a * OVERLAY_MOON;
    c = c * (1.0 - ma) + mo.rgb * ma;
    a += ma * (1.0 - a);
    vec4 cl = cloudLayer(fc, p);
    float ca = cl.a * OVERLAY_CLOUD;
    c = c * (1.0 - ca) + cl.rgb * ca;
    a += ca * (1.0 - a);
    return vec4(c, a);
}

void main() {
    iResolution = u_Frame.xy;
    iTime = u_Frame.z;
    iMouse = u_Pointer.xy;
    uAccent = u_Accent.rgb;
    uAccentHi = u_AccentHi.rgb;
    uAccentLo = u_AccentLo.rgb;
    uBase = u_Base.rgb;
    vec2 fc = floor(v_uv * iResolution) + 0.5;
    float halfW = 0.5 * iResolution.x / iResolution.y;
    vec2 p = vec2((fc.x - 0.5 * iResolution.x) / iResolution.y, fc.y / iResolution.y);

    gT = iTime;
    gBase = uBase;
    gAcc = tintNorm(uAccent, 0.50);
    gHi = tintNorm(uAccentHi, 0.62);
    gLo = tintNorm(uAccentLo, 0.30);
    gHor = mix(gBase, mix(gLo, gAcc, 0.3), 0.42);
    gDeep = gBase * 0.30 + gLo * 0.02;
    float moonX = min(clamp(halfW * 0.56, 0.17, 0.62), halfW - 0.1);
    gMoon = vec3(moonX, halfW < 0.45 ? 0.42 : 0.47, 0.085);
    gMist = mistColor(p);
    gMf = 0.18 + 0.82 * exp(-abs(p.x - gMoon.x) * 2.2);

    bool overlay = u_Pointer.z > 0.5;
    float land = overlay ? OVERLAY_LAND : 1.0;
    vec4 solid = overlay ? OVERLAY_SOLID : vec4(1.0);

    vec3 col = vec3(0.0);
    float T = 1.0;
    vec4 L;

    L = terrainLayer(fc, p,
        vec4(1.0 / 52.0, 0.035 * land, 0.075 * land, 4.0),
        vec4(129.7, 0.0, 0.015, 0.024),
        vec4(0.30, 0.18, 0.94, 0.55),
        vec4(0.10 * land, 0.0, 0.0, 0.06), vec2(0.0, 1.0));
    L.a *= solid.x;
    col += T * L.a * L.rgb;
    T *= 1.0 - L.a;

    if (T > 0.002) {
        L = terrainLayer(fc, p,
            vec4(1.0 / 82.0, 0.12 * land, 0.10 * land, 5.5),
            vec4(83.1, 0.0, 0.009, 0.014),
            vec4(0.85, 0.35, 0.80, 0.50),
            vec4(0.20 * land, 0.08 * land, 0.55, 0.04), vec2(0.0, 0.0));
        L.a *= solid.y;
        col += T * L.a * L.rgb;
        T *= 1.0 - L.a;
    }

    if (T > 0.002) {
        L = terrainLayer(fc, p,
            vec4(1.0 / 120.0, 0.205 * land, 0.12 * land, 4.8),
            vec4(47.9, 0.0, 0.005, 0.008),
            vec4(0.35, 0.2, 0.60, 0.45),
            vec4(0.30 * land, 0.17 * land, 0.60, 0.025), vec2(0.0, 0.0));
        L.a *= solid.z;
        col += T * L.a * L.rgb;
        T *= 1.0 - L.a;
    }

    if (T > 0.002) {
        L = terrainLayer(fc, p,
            vec4(1.0 / 170.0, 0.20 * land, 0.29 * land, 3.0),
            vec4(11.3, 1.0, 0.0025, 0.004),
            vec4(0.0, 0.0, 0.42, 0.35),
            vec4(0.32 * land, 0.19 * land, 0.70, 0.0), vec2(1.0, 0.0));
        L.a *= solid.w;
        col += T * L.a * L.rgb;
        T *= 1.0 - L.a;
    }

    if (overlay) {
        vec4 sky = overlaySky(fc, p);
        col += T * sky.rgb;
        T *= 1.0 - sky.a;
        vec3 ff = fireflies(fc, p);
        float fa = min(max(ff.r, max(ff.g, ff.b)), 1.0);
        col = col * (1.0 - fa) + ff;
        T *= 1.0 - fa;
    } else {
        if (T > 0.002) {
            vec3 sky = skyColor(p);
            sky += starField(fc, p);
            vec4 mo = moonDisc(fc);
            sky = mix(sky, mo.rgb, mo.a);
            vec4 cl = cloudLayer(fc, p);
            sky = mix(sky, cl.rgb, cl.a);
            col += T * sky;
        }
        col += fireflies(fc, p);
    }

    float centre = 1.0 - (overlay ? OVERLAY_CENTRE : 0.20) * exp(-p.x * p.x / 0.045) * ss(0.42, 0.78, p.y);
    col *= centre;
    vec2 vq = vec2(p.x / max(halfW, 0.5), (p.y - 0.55) * 1.6);
    float edge = 1.0 - 0.32 * ss(0.75, 1.6, length(vq));
    col *= edge;
    T *= centre * edge;

    float grain = (hash12(fc) - 0.5) / 255.0;
    if (overlay) {
        float alpha = 1.0 - T;
        fragColor = vec4(clamp(col / max(alpha, 0.004), 0.0, 1.0), clamp(alpha + grain, 0.0, 1.0));
    } else {
        fragColor = vec4(clamp(col + grain, 0.0, 1.0), 1.0);
    }
}
