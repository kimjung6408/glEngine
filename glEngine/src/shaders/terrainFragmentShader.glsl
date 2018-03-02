#version 400 core
//korean comment isn't supported

in vec2 pass_texCoords;
in vec3 surfaceNormal;
in vec3 toLightVector[4];
in vec3 toCameraVector;
in vec4 shadowCoords;

//visibility for fogging
in float visibility;

//output pixel color for single pass rendering
out vec4 out_Color;

//textures for terrain blend mapping
//0
uniform sampler2D backgroundTex;
//1
uniform sampler2D rTex;
//2
uniform sampler2D gTex;
//3
uniform sampler2D bTex;
//4
uniform sampler2D blendMap;
//5. texture for shadowMapping
uniform sampler2D shadowMap;

//light properties for shading
uniform vec3 lightColor[4];
uniform vec3 attenuation[4];

//materials
//shine damper is a power factor of cosine
uniform float shineDamper;
//reflectivity is intensity of a specular light
uniform float reflectivity;

//skycolor for fog calculation
uniform vec3 skyColor;

//values for soft shadow
const int pcfCount=3;
const float totalTexels=(pcfCount*2.0+1.0)*(pcfCount*2.0+1.0);

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

float applyShadowMapping(vec2 offset)
{
	float objectNearestLight=texture(shadowMap, shadowCoords.xy+offset).r;
	float lightFactor=1.0f;
	
	//behind of nearest object
	if(shadowCoords.z>objectNearestLight+0.002)
	{
		lightFactor =0.0;
	}
	
	return lightFactor;
}

void main(void)
{
	//values for pcf
	float mapsize=4096.0;
	float texelSize=1.0/mapsize;
	float total=0.0;
	
	for(int x=-pcfCount; x<=pcfCount; x++)
	{
		for(int y=-pcfCount; y<=pcfCount; y++)
		{
			total+=applyShadowMapping(vec2(x,y)*texelSize);
		}
	}
	
	total/=totalTexels;
	float lightFactor= clamp((total*shadowCoords.w), 0.3, 1.0);
	
	//get blendMap color for blend mapping
	vec4 blendMapColor= texture(blendMap, pass_texCoords);
	
	//get Background grass color
	float backgroundTexAmount=1-(blendMapColor.r+blendMapColor.g+blendMapColor.b);
	vec2 tiledCoords=pass_texCoords*40.0f;
	vec4 backgroundColor=texture(backgroundTex, tiledCoords)*backgroundTexAmount;
	//get r g b texture color
	vec4 rColor=texture(rTex, tiledCoords)*blendMapColor.r;
	vec4 gColor=texture(gTex, tiledCoords)*blendMapColor.g;
	vec4 bColor=texture(bTex, tiledCoords)*blendMapColor.b;
	
	//calculate total terrain color
	vec4 totalColor=backgroundColor+rColor+gColor+bColor;


	vec3 unitNormal= normalize(surfaceNormal);
	vec3 unitToCameraVector= normalize(toCameraVector);
	
	vec3 totalDiffuse= vec3(0.0);
	vec3 totalSpecular= vec3(0.0);
	
	//lighting using 4 lights
	for(int i=0; i<4; i++)
	{
		float attFactor=calculateAttenuation(toLightVector[i], attenuation[i]);
		vec3 unitToLightVector= normalize(toLightVector[i]);
		vec3 resultDiffuse=calculateDiffuse(unitNormal, unitToLightVector, lightColor[i], attFactor);
		vec3 resultSpecular=calculateSpecular(unitNormal, unitToCameraVector, unitToLightVector, lightColor[i], attFactor);
		
		totalDiffuse=totalDiffuse+resultDiffuse;
		totalSpecular=totalSpecular+resultSpecular;
	}
	
	//apply diffuse lighting and shadow
	totalDiffuse= max(totalDiffuse, 0.2f) * lightFactor;
	
	//out_Color=vec4(1,0,0,1);
	out_Color= vec4(totalDiffuse,1.0f)* totalColor+ vec4(totalSpecular, 1.0);

	//apply fog
	out_Color= mix(vec4(skyColor ,1.0f), out_Color, visibility);

}