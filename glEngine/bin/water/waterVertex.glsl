#version 400 core

in vec2 position;

out vec4 clipSpaceCoords;
out vec2 texCoords;
out vec3 toCameraVector;
out vec3 fromLightVector;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform vec3 lightPosition;

const float tiling=2.2f;

//camera position for fresnel effect
uniform vec3 cameraPosition;

void main(void) {
	vec4 worldPosition = modelMatrix * vec4(position.x, 0.0, position.y, 1.0);
	clipSpaceCoords = projectionMatrix * viewMatrix * worldPosition;
	gl_Position = clipSpaceCoords;
 
 	texCoords= vec2(position.x/2.0f + 0.5f, position.y/2.0f +0.5f)*tiling;
 	
 	//to Camera vector for fresnel effect
 	toCameraVector=cameraPosition-worldPosition.xyz;
 	
 	//fromLightVector for normal mapping
 	fromLightVector=worldPosition.xyz-lightPosition;
}