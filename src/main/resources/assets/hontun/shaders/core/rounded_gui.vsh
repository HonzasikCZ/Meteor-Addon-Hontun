#version 330

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};
layout(std140) uniform Projection {
    mat4 ProjMat;
};

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;

out vec4 v_color;
out vec2 v_local;
out vec2 v_half;
out float v_radius;
out float v_param;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    v_color = Color;
    v_local = UV0;
    v_half = vec2(UV1) * 0.5;
    v_radius = float(UV2.x);
    v_param = float(UV2.y);
}
