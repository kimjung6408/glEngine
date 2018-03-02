#version 400 core

out vec4 out_Color;

in vec4 clipSpaceCoords;
in vec2 texCoords;
in vec3 toCameraVector;
in vec3 fromLightVector;

uniform sampler2D reflectionTexture;
uniform sampler2D refractionTexture;
uniform sampler2D dudvMap;
uniform sampler2D normalMap;
uniform sampler2D depthMap;

//light for normal mapping
uniform vec3 lightColor;

//changing distortion values
const float waveStrength=0.1018f;

//water material property for normal mapping
const float shineDamper = 40.0f;
const float reflectivity = 0.7f;

//offset for moving water
uniform float moveFactor;

//values for calculating distance from window
const float near=0.1f;
const float far=1000.0f;

vec3 calculateSpecular( vec3 unitNormal, vec3 unitToCamera, vec3 fromLight, vec3 lightColorValue)
{
	vec3 reflectedLight=reflect(normalize(fromLight), unitNormal);
	float specular = max(dot(reflectedLight, unitToCamera) , 0.0);
	specular = pow(specular, shineDamper);
	
	vec3 resultSpecular = lightColorValue*specular*reflectivity;
	
	return resultSpecular;
}

//convert depth to distance
float convertToDistance(float depth)
{
	float Distance= 2.0f*near*far/(far+near-(2.0*depth-1.0)*(far-near));

	return Distance;
}


void main(void) {

	//calculate normalized coordinate device coords
	//center is (0,0) and boundary value is x to [-1,1] and y to [-1,1]
	vec2 ndc=(clipSpaceCoords.xy/clipSpaceCoords.w)/2.0f + 0.5f;
	vec2 refractTexCoords=vec2(ndc.x, ndc.y);
	vec2 reflectTexCoords=vec2(ndc.x, -ndc.y);
	
	//sample depth map (terrain and object surface) and after, distort it.
	//r component is depth in a depth map.
	float depth= texture(depthMap, refractTexCoords).r;
	//convert object depth to distance
	float floorDistance=convertToDistance(depth);
	
	//get water surface depth
	depth= gl_FragCoord.z;
	//convert water surface depth to distance
	float waterDistance=convertToDistance(depth);
	
	//calculate water depth
	float waterDepth=floorDistance-waterDistance;
	
	//calculate distortion by DuDvMap for realistic water
	vec2 distortion1=texture(dudvMap, vec2(texCoords.x+moveFactor, texCoords.y)).rg*0.1;
	distortion1=texCoords+vec2(distortion1.x, distortion1.y+moveFactor);
	vec2 distortion2=texture(dudvMap, distortion1.rg*2.0-1.0).rg*2.0f-1.0f;
	
	//calculate total distortion
	vec2 totalDistortion=distortion2*waveStrength;
	
	//multiply waterDepth value for solving shiny boundary glitch
	totalDistortion *= clamp(waterDepth/16.0f ,0.0 ,1.0);
	
	//apply distortion when sampling
	refractTexCoords +=totalDistortion;
	reflectTexCoords+=totalDistortion;
	
	//fix underscreen glitch problem
	refractTexCoords = clamp(refractTexCoords, 0.001f, 0.999f);
	reflectTexCoords.x = clamp(reflectTexCoords.x, 0.001f, 0.999f);
	reflectTexCoords.y = clamp(reflectTexCoords.y, -0.999f, -0.001f);
	
	vec4 reflectColor = texture(reflectionTexture, reflectTexCoords);
	vec4 refractColor= texture(refractionTexture, refractTexCoords);
	
	//apply normal mapping
	vec3 viewVector= normalize(toCameraVector);	
	vec4 normalMapColor= texture(normalMap, totalDistortion);
	vec3 normal = vec3(normalMapColor.r * 2.0 - 1.0 , normalMapColor.b *10.0, normalMapColor.g*2.0 -1.0);
	normal = normalize(normal);
	//calculate specular color using normal vector that is from normal resource view
	vec3 resultSpecular=calculateSpecular(normal, viewVector, fromLightVector, lightColor); 
	
	//calculate fresnel effect
	float refractiveFactor= dot(viewVector, normal);
	
	
	
	//calculate how much water reflective
	refractiveFactor= pow(refractiveFactor , 0.5f);
	
	out_Color = mix(reflectColor, refractColor, refractiveFactor);

	//add blue tint
	out_Color = mix(out_Color, vec4(0.0, 0.3, 0.5, 1.0), 0.2);
	
	//add specular light
	out_Color+= vec4(resultSpecular, 1.0);
	
	//apply water depth for solving glitch problem by alpha blending
	out_Color.a= clamp(waterDepth/10.0f, 0.0 ,1.0) ;
	
	
}