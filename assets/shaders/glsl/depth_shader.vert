#version 430 core
layout (location = 0) in vec3 vertex_position;

uniform struct matrix_t {
	mat4 model;
	mat4 lightSpaceMatrix;
} matrix;

void main()
{
    gl_Position = matrix.lightSpaceMatrix * matrix.model * vec4(vertex_position, 1.0);
	// gl_Position = model * vec4(aPos, 1.0);
};