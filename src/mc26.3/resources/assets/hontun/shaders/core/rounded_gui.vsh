#version 330
#extension GL_ARB_separate_shader_objects : require

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    mat4 TextureMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
};
layout(std140) uniform Projection {
    mat4 ProjMat;
};

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in ivec2 UV1;
layout(location = 4) in ivec2 UV2;

layout(location = 0) out vec4 v_color;
layout(location = 1) out vec2 v_local;
layout(location = 2) out vec2 v_half;
layout(location = 3) out float v_radius;
layout(location = 4) out float v_param;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    v_color = Color;
    v_local = UV0;
    v_half = vec2(UV1) * 0.5;
    v_radius = float(UV2.x);
    v_param = float(UV2.y);
}
