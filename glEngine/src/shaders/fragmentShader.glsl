#version 400 core
//korean comment isn't supported

in vec2 pass_texCoords;
in vec3 surfaceNormal;
in vec3 toLightVector[4];
in vec3 toCameraVector;
in float visibility;

out vec4  out_Color;
out vec4  out_BrightColor;

uniform sampler2D texSampler;
uniform sampler2D specularMap;

uniform float useSpecularMap;

//light properties for shading
uniform vec3 lightColor[4];
uniform vec3 attenuation[4];

//materials
//shine damper is a power factor of cosine
uniform float shineDamper;
//reflectivity is intensity of a specular light
uniform float reflectivity;

//skyColor for fog calculation
uniform vec3 skyColor;

void AlphaTest(vec4 textureColor)
{
	if(textureColor.a<0.5f)
	{
		discard;
	}
}

//begin Functions for lighting
vec3 calculateDiffuse(vec3 unitNormal, vec3 unitToLightVector, vec3 lightColorVector, float attFactor)
{
	//calculate diffuse lighting
	float nDotl= dot(unitNormal, unitToLightVector);
	float brightness= max(nDotl, 0.0f);
	vec3 diffuse=(brightness*lightColorVector)/attFactor;
	
	return diffuse;
}

vec3 calculateSpecular(vec3 unitNormal, vec3 unitToCameraVector, vec3 unitToLightVector, vec3 lightColorVector, float attFactor)
{
	//calculate specular lighting
	vec3 reflectedLightDirection= reflect(-unitToLightVector, unitNormal);
	float specularFactor= max(dot(reflectedLightDirection, unitToCameraVector),0.0);
	float dampedFactor= pow(specularFactor, shineDamper);
	vec3 resultSpecular= (reflectivity*dampedFactor*lightColorVector)/attFactor;
	
	return resultSpecular;
}

float calculateAttenuation(vec3 toLight, vec3 attValue)
{
	float distanceFromLight= length(toLight);
	float attFactor= attValue.x+attValue.y*distanceFromLight + attValue.z* pow(distanceFromLight, 2);
	
	return attFactor;
}
//end Functions for lighting

void main(void)
{
	vec3 unitNormal= normalize(surfaceNormal);
	vec3 unitToCameraVector= normalize(toCameraVector);
	
	vec3 totalDiffuse= vec3(0.0);
	vec3 totalSpecular= vec3(0.0);
	
	//lighting using 4 lights
	for(int i=0; i<4; i++)
	{
		//get attenuation
		float attFactor=calculateAttenuation(toLightVector[i], attenuation[i]);
		
		//calculate lighting
		vec3 unitToLightVector= normalize(toLightVector[i]);
		vec3 resultDiffuse=calculateDiffuse(unitNormal, unitToLightVector, lightColor[i], attFactor);
		vec3 resultSpecular=calculateSpecular(unitNormal, unitToCameraVector, unitToLightVector, lightColor[i], attFactor);
		
		totalDiffuse=totalDiffuse+resultDiffuse;
		totalSpecular=totalSpecular+resultSpecular;
	}
	
	//apply ambient lighting
	totalDiffuse= max(totalDiffuse, 0.2f);
	
	//get Texture Color
	vec4 textureColor=texture(texSampler, pass_texCoords);
	
	//alpha value test
	AlphaTest(textureColor);
	
	out_BrightColor= vec4(0.0);
	
	//apply specular map
	if(useSpecularMap>0.5)
	{
		vec4 mapInfo = texture(specularMap, pass_texCoords);
		
		totalSpecular*=mapInfo.r;
		
		if(mapInfo.g>0.5)
		{
			//apply glow map
			out_BrightColor= textureColor+ vec4(totalSpecular, 1.0);
		}
	}
	
	//out_Color=vec4(1,0,0,1);
	out_Color= vec4(totalDiffuse, 1.0f)*textureColor + vec4(totalSpecular, 1.0);
	
	//apply fog
	out_Color= mix(vec4(skyColor, 1.0f), out_Color, visibility);
}