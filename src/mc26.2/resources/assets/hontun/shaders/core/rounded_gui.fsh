#version 330

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

in vec4 v_color;
in vec2 v_local;
in vec2 v_half;
in float v_radius;
in float v_param;

out vec4 fragColor;

float ign(vec2 p) {
    return fract(52.9829189 * fract(dot(p, vec2(0.06711056, 0.00583715))));
}

float sdRoundRect(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - (b - vec2(r));
    return length(max(q, vec2(0.0))) + min(max(q.x, q.y), 0.0) - r;
}

void main() {
    float maxR = min(v_half.x, v_half.y);
    float r = clamp(v_radius, 0.0, maxR);

    float dist = sdRoundRect(v_local, v_half, r);
    float aa = max(fwidth(dist), 0.0001);

    float d = dist;
    float feather = 0.0;
    if (v_param < 0.0) {
        float bw = -v_param;
        d = abs(dist + bw * 0.5) - bw * 0.5;
    } else {
        feather = v_param;
    }

    float alpha = 1.0 - smoothstep(-aa - feather, aa, d);
    if (alpha <= 0.0) discard;

    vec4 c = v_color * ColorModulator;
    c.a *= alpha;
    if (feather > 0.0) {
        float lum = max(max(c.r, c.g), c.b);
        float amp = (1.0 / 255.0) / max(lum, 0.05);
        c.a = clamp(c.a + (ign(gl_FragCoord.xy) - 0.5) * 2.0 * amp, 0.0, 1.0);
    }
    if (c.a <= 0.0) discard;

    fragColor = c;
}
