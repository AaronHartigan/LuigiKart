#version 430 core
//out vec4 FragColor;

float LinearizeDepth(float depth) 
{
    float z = depth * 2.0 - 1.0;
    return (2.0 * 0.1f * 100f) / (100f + 0.1f - z * (100f - 0.1f));	
}

void main()
{             
    // gl_FragDepth = gl_FragCoord.z;
	//FragColor = vec4(1.0, 0.8, 0.5, 1.0);
	//float depth = LinearizeDepth(gl_FragCoord.z) / 100f;
	//gl_FragDepth = depth;
}

