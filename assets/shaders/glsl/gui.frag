#version 430 core

layout (binding = 0) uniform sampler2D guiTexture;

// stage input(s)
in vertex_t
{
    vec2 vertex_texcoord;       // texture coordinate
} fs_in;

// stage output(s)
out vec4 fragment;

void main(void){
	fragment = texture(guiTexture, fs_in.vertex_texcoord);
}