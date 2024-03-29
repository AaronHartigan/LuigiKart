#version 430

// Texture map bindings for base diffuse color, height, and normals
layout (binding = 0) uniform sampler2D tex_color;
layout (binding = 1) uniform sampler2D tex_height;
layout (binding = 2) uniform sampler2D tex_normal;

// Stage inputs and outputs
in  vec2 tcs_out[];
layout (quads, fractional_even_spacing, cw) in;
out vec4 FragPosLightSpace;
out vec2 tes_out;
out vec3 varyingVertPos;
out vec3 varyingNormal;

// Uniforms
uniform mat4 mat4_norm;
uniform mat4 mat4_mvp;
uniform mat4 mat4_mv;
uniform mat4 mat4_m;
uniform mat4 mat4_p;
uniform float multiplier;
uniform float subdivisions;
uniform int  patchSize;
uniform int  tileTx;
uniform int  tileTy;
uniform int  tileHx;
uniform int  tileHy;
uniform int  tileNx;
uniform int  tileNy;
uniform int  hasTexture;
uniform int  hasHeightM;
uniform int  hasNormalM;
uniform struct ambient_light_t { vec4 intensity; } global_light;
uniform struct material_t
{
    vec4  ambient;
    vec4  diffuse;
    vec4  specular;
    vec4  emissive;
    float shininess;
} material;

uniform struct matrix_t {
	mat4 lightSpaceMatrix;
} matrix;


// Light Struct
struct light_t {
    vec4  ambient;
    vec4  diffuse;
    vec4  specular;
    vec4  position;

    float const_attenuation;
    float linear_attenuation;
    float quadratic_attenuation;
    float range;

    vec3  cone_direction;
    float cone_cutoff_angle;
    float cone_falloff;
};
layout (std430, binding = 0) buffer ssbo_t { light_t lights[]; } ssbo;


/**
 * Estimates normals using 3-point interpolation (based on neighboring height values
 * pulled from the height map.
 *
 * If a normal map is provided, normals will be pulled from it instead of being
 * estimated.
 */
vec3 calcNewNormal(vec2 tn) {
	
	vec3 newNormal;
	
	// Pull normals from a normal map...
	if (hasNormalM == 1) {
		vec3 normal = vec3(0,1,0);
		vec3 tangent = vec3(1,0,0);
		vec3 bitangent = cross(tangent, normal);
		mat3 tbn = mat3(tangent, bitangent, normal);
		vec3 retrievedNormal = texture(tex_normal, tn).xyz;
		retrievedNormal = retrievedNormal * 2.0 - 1.0;
		newNormal = tbn * retrievedNormal;
	}
	
	// ...or else estimate normals via interpolation if the normal map is unavailable
	else {
		float ofs = 0.005;
		float height_adjust = 2.0;
		float h1 = height_adjust * texture(tex_height, (vec2((tn.s)      , (tn.t) + ofs))).r;
		float h2 = height_adjust * texture(tex_height, (vec2((tn.s) - ofs, (tn.t) - ofs))).r;
		float h3 = height_adjust * texture(tex_height, (vec2((tn.s) + ofs, (tn.t) - ofs))).r;

		vec3 v1 = vec3( 0,  h1, -1);
		vec3 v2 = vec3(-1,  h2,  1);
		vec3 v3 = vec3( 1,  h3,  1);
		
		vec3 v4 = v2 - v1;
		vec3 v5 = v3 - v1;
		
		newNormal = cross(v4, v5);
	}
	
	// Normalize the vector before returning it
	newNormal = normalize(newNormal);
	return newNormal;
}


/**
 * MAIN METHOD
 * 
 * Determines the elevations to assign to each vertex, and determines the normal as well
 */
void main (void)
{	// tc is the color coordinate. This is what enables the tiling effect with GL_REPEAT or MIRRORED_REPEAT
	vec2 tc = vec2( (tcs_out[0].x + (gl_TessCoord.x) / patchSize) * tileTx, (tcs_out[0].y + (1.0-gl_TessCoord.y) / patchSize) * tileTy);
	
	// th is the height coordinate. Allows the user to tile the height map
	vec2 th = vec2(
		(tcs_out[0].x + (gl_TessCoord.x) / patchSize) * tileHx,
		(tcs_out[0].y + (1.0-gl_TessCoord.y) / patchSize) * tileHy
	);
	
	// tn is the normal coordinate. Allows the user to tile the normal map
	vec2 tn = vec2( (tcs_out[0].x + (gl_TessCoord.x) / patchSize) * tileNx, (tcs_out[0].y + (1.0-gl_TessCoord.y) / patchSize) * tileNy);

	// map the tessellated grid onto the texture rectangle:
	vec4 tessellatedPoint = vec4(
		gl_in[0].gl_Position.x + gl_TessCoord.x/patchSize,
		0.0,
		gl_in[0].gl_Position.z + gl_TessCoord.y/patchSize,
		1.0
	);
	
	// add the height from the height map to the vertex:
	float colorHeight = texture(tex_height, th).r;
	tessellatedPoint.y += (multiplier * colorHeight);
	
	gl_Position = mat4_mvp * tessellatedPoint;
	tes_out = tc;
	
	// Calculate lighting positions
	varyingVertPos  = (mat4_mv * tessellatedPoint).xyz;
	
	// Calculate normal
	varyingNormal  = calcNewNormal(tn);

	FragPosLightSpace = matrix.lightSpaceMatrix * mat4_m * tessellatedPoint;
}