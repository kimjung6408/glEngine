#version 400 core

in vec3 position;
in vec2 texCoords;
in vec3 normal;

out vec2 pass_texCoords;
out vec3 surfaceNormal;
out vec3 toLightVector[4];
out vec3 toCameraVector;
out float visibility;

//shadowCoords xy is sample position
//shadowCoords z is distance from light;
out vec4 shadowCoords;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

uniform vec3 lightPosition[4];

//uniform value for shadow mapping
uniform mat4 toShadowSpace;

//fog factors
const float density=0.007f;
const float gradient=1.0f;

//clip for water rendering
uniform vec4 plane;

void main(void)
{
	vec4 worldPosition=transformationMatrix*vec4(position, 1.0);
	
	
	//calculate orthographic shadow space position
	//for shadow mapping
	shadowCoords=toShadowSpace*worldPosition;
	
	
	//calculate clip distance for water rendering
	gl_ClipDistance[0]=dot(worldPosition, plane);
	
	vec4 positionRelativeToCam=viewMatrix*worldPosition;
	gl_Position=projectionMatrix*viewMatrix*worldPosition;	
	pass_texCoords=texCoords;
	
		//a normal vector is transformed by an inverse-tranpose matrix of the transformationMatrix
		surfaceNormal=( transpose(inverse(transformationMatrix) )*vec4(normal, 0.0)).xyz;
		
		//calculate toLightVector for lighting
		for(int i=0; i<4; i++)
		{
			toLightVector[i]=lightPosition[i]-worldPosition.xyz;
		}
		
		toCameraVector=(inverse(viewMatrix)*vec4(0.0,0.0,0.0,1.0)).xyz-worldPosition.xyz;
		
		//calculate fog factors
		float distance= length(positionRelativeToCam.xyz);
		visibility= exp(-pow((distance*density), gradient));
		visibility= clamp(visibility, 0.0f, 1.0f);
}