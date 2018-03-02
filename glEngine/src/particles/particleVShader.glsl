#version 140

in vec2 position;

//changing variables per object
in mat4 modelViewMatrix;
in vec4 texOffsets;
in float blendFactor;

//out to fragment shader
out vec2 textureCoords1;
out vec2 textureCoords2;
out float blend;

//non-changing variables per object
uniform mat4 projectionMatrix;

uniform float numberOfRows;

float getBlendFactor(void)
{
	return blendFactor;
}

float getNumOfRows(void)
{
	return numberOfRows;
}

void main(void){

	vec2 textureCoords = position + vec2(0.5, 0.5);
	textureCoords.y = 1.0-textureCoords.y;
	
	textureCoords /=getNumOfRows();
	
	textureCoords1= textureCoords + texOffsets.xy;
	textureCoords2= textureCoords + texOffsets.zw;
	
	blend=getBlendFactor();

	gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 0.0, 1.0);

}