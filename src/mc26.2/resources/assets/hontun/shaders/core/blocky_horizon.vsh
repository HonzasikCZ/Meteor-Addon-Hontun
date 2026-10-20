#version 330

in vec2 Position;

out vec2 v_uv;

void main() {
    gl_Position = vec4(Position, 0.0, 1.0);
    v_uv = Position * 0.5 + 0.5;
}
