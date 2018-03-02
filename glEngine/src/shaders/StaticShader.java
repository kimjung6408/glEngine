package shaders;

import java.util.List;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Light;
import toolbox.Maths;

public class StaticShader extends ShaderProgram {
	
	private static final int MAX_LIGHTS=4;

	private static final String VERTEX_FILE="src/shaders/vertexShader.glsl";
	private static final String FRAGMENT_FILE="src/shaders/fragmentShader.glsl";
	
	private int location_transformationMatrix;
	private int location_projectionMatrix;
	private int location_viewMatrix;
	private int location_lightPosition[];
	private int location_lightColor[];
	private int location_attenuation[];
	private int location_shineDamper;
	private int location_reflectivity;
	private int location_useFakeLighting;
	private int location_skyColor;
	private int location_numOfRows;
	private int location_offset;
	private int location_specularMap;
	private int location_texSampler;
	private int location_useSpecularMap;
	
	//plane for clip distance
	private int location_plane;
	
	public StaticShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
		// TODO Auto-generated constructor stub
	}


	@Override
	protected void bindAttributes() {
		// TODO Auto-generated method stub
		super.bindAttribute(0, "position");
		super.bindAttribute(1, "texCoords");
		super.bindAttribute(2, "normal");
		super.bindFragOutput(0, "out_Color");
		super.bindFragOutput(1, "out_BrightColor");
	}


	@Override
	protected void getAllUniformLocations() {
		// TODO Auto-generated method stub
		location_transformationMatrix=super.getUniformLocation("transformationMatrix");
		location_projectionMatrix=super.getUniformLocation("projectionMatrix");
		location_viewMatrix=super.getUniformLocation("viewMatrix");
		location_shineDamper=super.getUniformLocation("shineDamper");
		location_reflectivity=super.getUniformLocation("reflectivity");
		location_useFakeLighting=super.getUniformLocation("useFakeLighting");
		location_skyColor=super.getUniformLocation("skyColor");
		location_numOfRows=super.getUniformLocation("numOfRows");
		location_offset=super.getUniformLocation("offset");
		location_plane=super.getUniformLocation("plane");
		location_specularMap=super.getUniformLocation("specularMap");
		location_useSpecularMap=super.getUniformLocation("useSpecularMap");
		location_texSampler=super.getUniformLocation("texSampler");
		
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
	
	public void connectTextureUnits()
	{
		super.loadInt(location_texSampler, 0);
		super.loadInt(location_specularMap, 1);
	}
	
	public void loadUseSpecularMap(boolean useSpecularMap)
	{
		super.loadBoolean(location_useSpecularMap, useSpecularMap);
	}
	
	//load clip plane Ax+By+Cz+D=0
	public void loadClipPlane(Vector4f clipPlane)
	{
		super.loadVector4(location_plane, clipPlane);
	}
	
	public void loadNumberOfRows(int numOfRows)
	{
		super.loadFloat(location_numOfRows, numOfRows);
	}
	
	public void loadTexOffset(float x, float y)
	{
		super.loadVector2(location_offset, new Vector2f(x,y));
	}
	
	public void loadFakeLightingVariable(boolean useFakeLighting)
	{
		super.loadBoolean(location_useFakeLighting, useFakeLighting);
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
}
