#version 400 core

in vec3 position;
in vec2 texCoords;
in vec3 normal;

out vec2 pass_texCoords;
out vec3 surfaceNormal;
out vec3 toLightVector[4];
out vec3 toCameraVector;
out float visibility;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

uniform vec3 lightPosition[4];

//additional material properties
uniform float useFakeLighting;

//using for texture atlas
uniform float numOfRows;
uniform vec2 offset;

//fog factors
const float density=0.007f;
const float gradient=1.0f;

//plane for clipping Ax+By+Cz+D=0, (A,B,C,D)
uniform vec4 plane= vec4(0, -1, 0, 15);

void main(void)
{


	vec4 worldPosition=transformationMatrix*vec4(position, 1.0);
	
	//calculate clip distance for surface rendering
	gl_ClipDistance[0]= dot(worldPosition, plane);
	
	
	vec4 positionRelativeToCam=viewMatrix*worldPosition;
	gl_Position=projectionMatrix*viewMatrix*worldPosition;	
	pass_texCoords=(texCoords/numOfRows)+offset;
	
	//check fake lighting. if it use fake lighting, then normal vector is directed to y.
	vec3 actualNormal=normal;
	if(useFakeLighting>0.5f)
	{
		actualNormal= vec3(0,1,0);
	}
	
		//a normal vector is transformed by an inverse-tranpose matrix of the transformationMatrix
		surfaceNormal=( transpose(inverse(transformationMatrix) )*vec4(actualNormal, 0.0)).xyz;
		
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