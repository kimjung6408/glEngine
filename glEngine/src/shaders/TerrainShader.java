package shaders;

import java.util.List;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Light;
import toolbox.Maths;

public class TerrainShader extends ShaderProgram {

private static final int MAX_LIGHTS=4;	

private static final String VERTEX_FILE="src/shaders/terrainVertexShader.glsl";
private static final String FRAGMENT_FILE="src/shaders/terrainFragmentShader.glsl";

private int location_transformationMatrix;
private int location_projectionMatrix;
private int location_viewMatrix;
private int location_lightPosition[];
private int location_lightColor[];
private int location_attenuation[];
private int location_shineDamper;
private int location_reflectivity;
private int location_skyColor;
private int location_backgroundTex;
private int location_rTex;
private int location_gTex;
private int location_bTex;
private int location_blendMap;
private int location_plane;
private int location_toShadowSpace;
private int location_shadowMap;

public TerrainShader() {
	super(VERTEX_FILE, FRAGMENT_FILE);
	// TODO Auto-generated constructor stub
}


@Override
protected void bindAttributes() {
	// TODO Auto-generated method stub
	super.bindAttribute(0, "position");
	super.bindAttribute(1, "texCoords");
	super.bindAttribute(2, "normal");
}


@Override
protected void getAllUniformLocations() {
	// TODO Auto-generated method stub
	location_transformationMatrix=super.getUniformLocation("transformationMatrix");
	location_projectionMatrix=super.getUniformLocation("projectionMatrix");
	location_viewMatrix=super.getUniformLocation("viewMatrix");
	location_shineDamper=super.getUniformLocation("shineDamper");
	location_reflectivity=super.getUniformLocation("reflectivity");
	location_skyColor=super.getUniformLocation("skyColor");
	location_backgroundTex=super.getUniformLocation("backgroundTex");
	location_rTex=super.getUniformLocation("rTex");
	location_gTex=super.getUniformLocation("gTex");
	location_bTex=super.getUniformLocation("bTex");
	location_blendMap=super.getUniformLocation("blendMap");
	location_plane=super.getUniformLocation("plane");
	location_toShadowSpace=super.getUniformLocation("toShadowSpace");
	location_shadowMap=super.getUniformLocation("shadowMap");
	location_lightPosition=new int[MAX_LIGHTS];
	location_lightColor=new int[MAX_LIGHTS];
	location_attenuation=new int[MAX_LIGHTS];
	
	for(int i=0; i<MAX_LIGHTS; i++)
	{
		location_lightPosition[i]=super.getUniformLocation("lightPosition["+i+"]");
		location_lightColor[i]=super.getUniformLocation("lightColor["+i+"]");
		location_attenuation[i]=super.getUniformLocation("attenuation["+i+"]");
	}
}

public void loadClipPlane(Vector4f clipPlane)
{
	super.loadVector4(location_plane, clipPlane);
}

public void connectTextures()
{
	super.loadInt(location_backgroundTex, 0);
	super.loadInt(location_rTex, 1);
	super.loadInt(location_gTex, 2);
	super.loadInt(location_bTex, 3);
	super.loadInt(location_blendMap, 4);
	super.loadInt(location_shadowMap, 5);
}

public void loadShineVariables(float damper, float reflectivity)
{
	super.loadFloat(location_shineDamper, damper);
	super.loadFloat(location_reflectivity, reflectivity);
}

public void loadLights(List<Light> lights)
{
	for(int i=0; i<MAX_LIGHTS; i++)
	{
		if(i<lights.size())
		{
			super.loadVector3(location_lightPosition[i], lights.get(i).getPosition());
			super.loadVector3(location_lightColor[i], lights.get(i).getColor());
			super.loadVector3(location_attenuation[i], lights.get(i).getAttenuation());
		}
		else
		{
			//empty light
			super.loadVector3(location_lightPosition[i], new Vector3f(0,0,0));
			super.loadVector3(location_lightColor[i], new Vector3f(0,0,0));
			super.loadVector3(location_attenuation[i], new Vector3f(1,0,0));
		}
	}
}

public void loadTransformationMatrix(Matrix4f matrix)
{
	super.loadMatrix(location_transformationMatrix, matrix);
}

public void loadProjectionMatrix(Matrix4f projectionMatrix)
{
	super.loadMatrix(location_projectionMatrix, projectionMatrix);
}

public void loadViewMatrix(Camera cam)
{
	Matrix4f viewMatrix=Maths.createViewMatrix(cam);
	super.loadMatrix(location_viewMatrix, viewMatrix);
}

public void loadSkyColor(float r, float g , float b)
{
	super.loadVector3(location_skyColor, new Vector3f(r,g,b));
}

public void loadToShadowSpace(Matrix4f matrix)
{
	super.loadMatrix(location_toShadowSpace, matrix);
}
}

