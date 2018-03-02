package renderEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import models.TexturedModel;
import normalMappingRenderer.NormalMappingRenderer;
import shaders.StaticShader;
import shaders.TerrainShader;
import shadows.ShadowMapMasterRenderer;
import skybox.SkyboxRenderer;
import terrains.Terrain;

public class MasterRenderer {
	
	//Perspective projection constants;
	public static final float FOV=70.0f;
	public static final float NEAR_PLANE=0.01f;
	public static final float FAR_PLANE=1000.0f;
	
	private static Vector3f skyColor=new Vector3f(0.68f, 0.68f, 0.8f);
	
	private StaticShader shader=new StaticShader();
	private EntityRenderer renderer;
	private SkyboxRenderer skyboxRenderer;
	private TerrainRenderer terrainRenderer;
	private ShadowMapMasterRenderer shadowMapRenderer;
	
	private NormalMappingRenderer normalMapRenderer;
	private TerrainShader terrainShader=new TerrainShader();
	
	private Matrix4f projectionMatrix;
	
	private Map<TexturedModel, List<Entity>> entities
	=new HashMap<TexturedModel, List<Entity>>();
	
	private Map<TexturedModel, List<Entity>> normalMapEntities
	=new HashMap<TexturedModel, List<Entity>>();
	
	private List<Terrain> terrains
	=new ArrayList<Terrain>();
	
	public MasterRenderer(Loader loader, Camera cam)
	{
		createProjectionMatrix();
		enableCulling();
		
		renderer=new EntityRenderer(shader, projectionMatrix);
		terrainRenderer=new TerrainRenderer(terrainShader, projectionMatrix);
		skyboxRenderer=new SkyboxRenderer(loader, projectionMatrix);
		normalMapRenderer= new NormalMappingRenderer(projectionMatrix);
		shadowMapRenderer=new ShadowMapMasterRenderer(cam);
	}
	
	public void cleanUp()
	{
		shader.cleanUp();
		terrainShader.cleanUp();
		normalMapRenderer.cleanUp();
		shadowMapRenderer.cleanUp();
	}
	
	public int getShadowMapTexture()
	{
		return shadowMapRenderer.getShadowMap();
	}
	
	public void renderShadowMap(List<Entity> entityList, Light sun)
	{
		for(Entity entity : entityList)
		{
			processEntity(entity);
		}
		
		shadowMapRenderer.render(entities, sun);
		entities.clear();
	}
	
	public void prepare()
	{
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClearColor(skyColor.x, skyColor.y	, skyColor.z, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		//activating texture for shadow mapping
		GL13.glActiveTexture(GL13.GL_TEXTURE5);
		//bind shadow map texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D,getShadowMapTexture());
		
	}
	
	public void renderScene(List<Entity> entities, List<Entity> normalMapEntities, Terrain terrain, List<Light> lights, Camera camera, Vector4f clipPlane)
	{

		processTerrain(terrain);
		
		for(Entity  entity : entities)
		{
			processEntity(entity);
		}
		
		//for(Entity  entity : normalMapEntities)
		//{
		//	processEntity(entity);
		//}
		
		render(lights, camera, clipPlane);
	}
	
	public void render(List<Light> lights, Camera cam, Vector4f clipPlane)
	{
		prepare();
		
		//Render entities
		shader.startShader();
		shader.loadClipPlane(clipPlane);
		shader.loadSkyColor(skyColor.x, skyColor.y, skyColor.z);
		shader.loadLights(lights);
		shader.loadViewMatrix(cam);
		renderer.render(entities);
		shader.stopShader();
		
		//Render terrains
		terrainShader.startShader();
		terrainShader.loadClipPlane(clipPlane);
		terrainShader.loadLights(lights);
		terrainShader.loadViewMatrix(cam);
		terrainShader.loadSkyColor(skyColor.x, skyColor.y, skyColor.z);
		terrainRenderer.render(terrains, shadowMapRenderer.getToShadowMapSpaceMatrix());
		terrainShader.stopShader();
		
		//render skybox
		skyboxRenderer.render(cam, skyColor.x, skyColor.y, skyColor.z);
		terrains.clear();
		entities.clear();
	}
	
	public void processTerrain(Terrain terrain)
	{
		terrains.add(terrain);
	}

	//entity의 모델이 이미 로딩되어 있는지 확인한다.
	//이미 로딩되어 있지 않으면, 새로운 texturedModel List를 생성한다.
	//거기에 entity를 저장한다.
	//이미 로딩되어 있으면, 이미 존재하는 texturedModel List에 저장한다.
	public void processEntity(Entity entity)
	{
		TexturedModel entityModel=entity.getModel();
		List<Entity> batch=entities.get(entityModel);
		
		if(batch!=null)
		{
			batch.add(entity);
		}
		else
		{
			List<Entity> newBatch=new ArrayList<Entity>();
			newBatch.add(entity);
			entities.put(entityModel, newBatch);
		}
	}
	
	public void processNormalMapEntity(Entity entity)
	{
		TexturedModel entityModel=entity.getModel();
		List<Entity> batch=normalMapEntities.get(entityModel);
		
		if(batch!=null)
		{
			batch.add(entity);
		}
		else
		{
			List<Entity> newBatch=new ArrayList<Entity>();
			newBatch.add(entity);
			normalMapEntities.put(entityModel, newBatch);
		}
	}
	
    private void createProjectionMatrix(){
    	projectionMatrix = new Matrix4f();
		float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
		float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))));
		float x_scale = y_scale / aspectRatio;
		float frustum_length = FAR_PLANE - NEAR_PLANE;

		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
		projectionMatrix.m33 = 0;
    }
	
	public Matrix4f getProjectionMatrix()
	{
		return projectionMatrix;
	}
	
	//culling mask 활성화
	public static void enableCulling()
	{
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
	}

	public static void disableCulling()
	{
		GL11.glDisable(GL11.GL_CULL_FACE);
	}

	public static Vector3f getSkyColor() {
		return skyColor;
	}

	
}
