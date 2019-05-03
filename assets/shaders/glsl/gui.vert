#version 410 core

// stage input(s)
layout (location = 0) in vec3 vertex_position;

uniform mat4 transformationMatrix;

// stage output(s)
out vertex_t
{
    vec2 vertex_texcoord;       // in texture-space
} vs_out;

void main()
{
	vec2 position = vec2(vertex_position.x, vertex_position.y);
    gl_Position             = transformationMatrix * vec4(position, 0.0, 1.0);
    vs_out.vertex_texcoord  = vec2((position.x + 1.0) / 2.0, (position.y + 1.0) / 2.0);
}
