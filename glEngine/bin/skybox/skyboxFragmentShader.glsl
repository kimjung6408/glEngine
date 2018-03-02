#version 400

in vec3 textureCoords;
out vec4 out_Color;

uniform samplerCube cubeMap;

//const value for fog
uniform vec3 fogColor;
const float lowerLimit=-30.0f;
const float upperLimit=70.0f;

void main(void){
	vec4 finalColor = texture(cubeMap, textureCoords);

	float factor=(textureCoords.y-lowerLimit)/(upperLimit-lowerLimit);
	
	factor= clamp(factor, 0.0f, 1.0f);
	
	out_Color= mix(vec4(fogColor, 1.0), finalColor, factor);

}