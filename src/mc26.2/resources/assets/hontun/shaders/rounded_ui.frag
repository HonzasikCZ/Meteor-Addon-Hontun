#version 330 core

out vec4 fragColor;

in vec2 v_LocalPos;
in vec2 v_ScreenPos;
in vec4 v_Color;

layout(std140) uniform RoundedRectData {
    vec4 u_FillColor;
    vec4 u_BorderColor;
    vec4 u_BorderData;
    vec4 u_Radii;
    vec2 u_HalfSize;
    vec4 u_ClipRect;
};

float roundedRectSDF(vec2 p, vec2 halfSize, vec4 radii) {
    float r = radii.x;
    if (p.x > 0.0 && p.y < 0.0) r = radii.y;
    else if (p.x > 0.0 && p.y > 0.0) r = radii.z;
    else if (p.x < 0.0 && p.y > 0.0) r = radii.w;

    float maxRadius = min(halfSize.x, halfSize.y);
    r = clamp(r, 0.0, maxRadius);

    vec2 q = abs(p) - (halfSize - vec2(r));
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

float ign(vec2 p) {
    return fract(52.9829189 * fract(dot(p, vec2(0.06711056, 0.00583715))));
}

float luminance(vec3 c) {
    return max(max(c.r, c.g), c.b);
}

void main() {
    if (u_ClipRect.z > u_ClipRect.x) {
        if (v_ScreenPos.x < u_ClipRect.x || v_ScreenPos.y < u_ClipRect.y ||
            v_ScreenPos.x > u_ClipRect.z || v_ScreenPos.y > u_ClipRect.w) {
            discard;
        }
    }

    float dist = roundedRectSDF(v_LocalPos, u_HalfSize, u_Radii);

    float aa = fwidth(dist) * 0.5;
    float feather = u_BorderData.y;

    float shapeAlpha;
    if (feather <= 0.0) shapeAlpha = smoothstep(aa, -aa, dist);
    else shapeAlpha = 1.0 - smoothstep(-feather, aa, dist);
    if (shapeAlpha <= 0.0) discard;

    vec4 fillC = u_FillColor * v_Color;
    vec4 color = fillC;
    float borderWidth = u_BorderData.x;

    if (borderWidth > 0.0 && feather <= 0.0) {
        float innerDist = dist + borderWidth;
        float innerAlpha = smoothstep(aa, -aa, innerDist);

        vec4 border = vec4(u_BorderColor.rgb * u_BorderColor.a, u_BorderColor.a);
        vec4 fill = vec4(fillC.rgb * fillC.a, fillC.a);
        vec4 mixed = mix(border, fill, innerAlpha);
        color = vec4(mixed.rgb / max(mixed.a, 0.0001), mixed.a);
    }

    color.a *= shapeAlpha * u_BorderData.z;

    if (u_BorderData.w > 0.0 && color.a > 0.0) {
        float amp = u_BorderData.w / 255.0 / max(luminance(fillC.rgb), 0.08);
        color.a = clamp(color.a + (ign(gl_FragCoord.xy) - 0.5) * amp, 0.0, 1.0);
    }

    if (color.a <= 0.0) discard;

    fragColor = color;
}
