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

out vec4 fragColor;

float sdRoundRect(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - (b - vec2(r));
    return length(max(q, vec2(0.0))) + min(max(q.x, q.y), 0.0) - r;
}

void main() {
    float maxR = min(v_half.x, v_half.y);
    float r = clamp(v_radius, 0.0, maxR);

    float dist = sdRoundRect(v_local, v_half, r);

    float aa = max(fwidth(dist), 0.0001);
    float alpha = 1.0 - smoothstep(-aa, aa, dist);
    if (alpha <= 0.0) discard;

    vec4 c = v_color * ColorModulator;
    c.a *= alpha;
    if (c.a <= 0.0) discard;

    fragColor = c;
}
