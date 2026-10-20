#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D Ghost;

layout(std140) uniform TransitionData {
    vec4 u_Fade;
    vec4 u_Mask;
};

layout(location = 0) in vec2 v_uv;

layout(location = 0) out vec4 fragColor;

void main() {
    vec2 uv = (v_uv - 0.5) / u_Fade.y + 0.5;
    vec2 inside = step(u_Mask.xy, v_uv) * step(v_uv, u_Mask.zw);
    fragColor = vec4(texture(Ghost, uv).rgb, u_Fade.x * (1.0 - inside.x * inside.y));
}
