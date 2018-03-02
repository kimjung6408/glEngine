package engineTester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import GUIs.GUIRenderer;
import GUIs.GUITexture;
import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import models.RawModel;
import models.TexturedModel;
import particles.Particle;
import particles.ParticleMaster;
import particles.ParticleSystem;
import particles.ParticleTexture;
import postProcessing.Fbo;
import postProcessing.PostProcessing;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import renderEngine.EntityRenderer;
import shaders.StaticShader;
import terrains.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.MousePicker;
import water.WaterFrameBuffers;
import water.WaterRenderer;
import water.WaterShader;
import water.WaterTile;

public class MainGameLoop {
	

	public static void main(String[] args)
	{
		DisplayManager.createDisplay();
		
		Loader loader = new Loader();
		Random random=new Random();
		GUIRenderer guiRenderer=new GUIRenderer(loader);
		
		
		//create Player
		TexturedModel playerModel=loader.loadTexturedModel("person", "playerTexture");
		Player player=new Player(playerModel,new Vector3f(100,0,700),0,0,0,1);
		
		//declare eye (camera)
		Camera cam=new Camera(player);
		
		
		//core renderer that can render objects
		MasterRenderer renderer=new MasterRenderer(loader , cam);
		
		
		//declare GUIs
		GUITexture shadowMap=new GUITexture(renderer.getShadowMapTexture(),
				new Vector2f(0.5f, 0.5f), new Vector2f(0.5f, 0.5f));
	
		//init ParticleMaster
		ParticleMaster.init(loader, renderer.getProjectionMatrix());
		
		//get particle system
		ParticleTexture particleTex=new ParticleTexture(loader.loadTexture("fire"), 8); 
		ParticleSystem starEmitter=new ParticleSystem(particleTex,40.0f, 15.0f, 0.1f, 2f, 10.0f);
		starEmitter.randomizeRotation();
		starEmitter.setDirection(new Vector3f(0,1,0), 0.03f);
		starEmitter.setLifeError(0.07f);
		starEmitter.setSpeedError(0.2f);
		starEmitter.setScaleError(0.5f);
		
		
		//declare mouse picker
		MousePicker picker=new MousePicker(cam, renderer.getProjectionMatrix());
		
		//declare entity list;
		List<Entity> entities=new ArrayList<Entity>();
		entities.add(player);
		
		List<Entity> normalMapEntities=new ArrayList<Entity>();
		List<GUITexture> staticGUIs=new ArrayList<GUITexture>();
		//staticGUIs.add(shadowMap);
		//declare waters
		WaterShader waterShader=new WaterShader();
		List<WaterTile> waters=new ArrayList<WaterTile>();
		waters.add(new WaterTile(100,700,0));
		
		/* delcare water frame buffer for water rendering*/
		WaterFrameBuffers waterFbos=new WaterFrameBuffers();
		WaterRenderer waterRenderer=new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), waterFbos);
		
		
		//declare light
		List<Light> lights=new ArrayList<Light>();
		Light sunlight=new Light(new Vector3f(100000,150000,-10000), new Vector3f(1,1,1));
		lights.add(sunlight);
		//lights.add(new Light(new Vector3f(50, 2, 0), new Vector3f(1,0,0),new Vector3f(1, 0.02f, 0.002f)));
		//lights.add(new Light(new Vector3f(100, 10, 700), new Vector3f(1,1,1),new Vector3f(0.1f, 0.02f, 0.002f)));
		//lights.add(new Light(new Vector3f(400, 2, -20), new Vector3f(0,1,0),new Vector3f(1, 0.02f, 0.002f)));
		
		//create terrain texture pack
		TerrainTexture backgroundTexture=new TerrainTexture(loader.loadTexture("grassy"));
		TerrainTexture rTexture=new TerrainTexture(loader.loadTexture("mud"));
		TerrainTexture gTexture=new TerrainTexture(loader.loadTexture("grassFlowers"));
		TerrainTexture bTexture=new TerrainTexture(loader.loadTexture("path"));
		TerrainTexture blendMap=new TerrainTexture(loader.loadTexture("blendMap"));
		TerrainTexturePack texPack=new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
		//declare terrain
		Terrain grassTerrain=new Terrain(0, 0, loader, texPack, blendMap, "heightmap");
		
		//load forest objects
		TexturedModel treeModel=	loader.loadTexturedModel("tree", "tree");
		TexturedModel grassModel=loader.loadTexturedModel("grassModel", "grassTexture");
		TexturedModel fernModel=	loader.loadTexturedModel("fern", "fern");
		grassModel.getTexture().setHasTransparency(true);
		fernModel.getTexture().setHasTransparency(true);
		fernModel.getTexture().setNumOfRows(2);
		
		// load lamp
		Entity lamp=loader.loadEntity("lamp", "lamp", new Vector3f(100, grassTerrain.getHeightOfTerrain(100, 700)-1, 700), 0, 0, 0, 1);
		entities.add(lamp);
		
		//generate forest entities
		for(int i=0; i<600; i++)
		{
			float x=random.nextFloat()*1600.0f;
			float z=random.nextFloat()*800.0f;
			float y=grassTerrain.getHeightOfTerrain(x, z);
			
			entities.add(new Entity(treeModel, new Vector3f(x,y, z),0,0,0,3));
		}
		
		for(int i=0; i<600; i++)
		{
			float x=random.nextFloat()*1600.0f;
			float z=random.nextFloat()*800.0f;
			float y=grassTerrain.getHeightOfTerrain(x, z);
			entities.add(new Entity(grassModel, new Vector3f(x,y, z),0,0,0,1));
		}
		
		for(int i=0; i<1600; i++)
		{
			float x=random.nextFloat()*1600.0f;
			float z=random.nextFloat()*800.0f;
			float y=grassTerrain.getHeightOfTerrain(x, z);
			entities.add(new Entity(fernModel, random.nextInt(4),new Vector3f(x,y, z),0,0,0,0.6f));
		}
		
		//PostProcessing
		Fbo multisampleFbo=new Fbo(Display.getWidth(), Display.getHeight());
		Fbo outputFbo=new Fbo(Display.getWidth(), Display.getHeight(), Fbo.DEPTH_TEXTURE);
		
		PostProcessing.init(loader);
		
		//game loop
		while(!Display.isCloseRequested())
		{
			//Game Logic
			cam.move();
			picker.update();
			
			//update particles
			ParticleMaster.update(cam);
			starEmitter.generateParticles(player.getPosition());
			
			renderer.renderShadowMap(entities, sunlight);
			
			
			
			//for water rendering, set clip distance for refraction
			//각 Vertex마다, clipping plane으로부터 얼마나 떨어져 있는지를 계산하여 리턴함.
			//horizontal clip plane의 높이를 결정하면, 해당 높이 아래에 있는 것들은 렌더링되고, 위에 있는 것들은 렌더링 되지 않는다.
			GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
			
			//render to reflection render targets that are attached to this frame buffer
			waterFbos.bindReflectionFrameBuffer();
			float distance = 2*(cam.getPosition().y-waters.get(0).getHeight());
			cam.getPosition().y-=distance;
			cam.invertPitch();
			renderer.renderScene(entities, null, grassTerrain, lights, cam, new Vector4f(0,1,0,-waters.get(0).getHeight()+1f));
			cam.invertPitch();
			cam.getPosition().y+=distance;
			waterFbos.bindRefractionFrameBuffer();
			renderer.renderScene(entities, null, grassTerrain, lights, cam, new Vector4f(0,-1,0,waters.get(0).getHeight()+1));
			
			//end water rendering
			GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
			waterFbos.unbindCurrentFrameBuffer();
			
			player.move(grassTerrain);
			
			multisampleFbo.bindFrameBuffer();
			renderer.renderScene(entities, null, grassTerrain, lights, cam, new Vector4f(0,-1,0,1000));
			waterRenderer.render(waters, cam, lights.get(0));
			
			//render particles
			ParticleMaster.renderParticles(cam);
			
			multisampleFbo.unbindFrameBuffer();
			multisampleFbo.resolveToFbo(GL30.GL_COLOR_ATTACHMENT0, outputFbo);
			//do post processing
			PostProcessing.doPostProcessing(outputFbo.getColourTexture());
			
			//render GUIs
			guiRenderer.render(staticGUIs);
			
			DisplayManager.updateDisplay();
		}
		
		PostProcessing.cleanUp();
		outputFbo.cleanUp();
		multisampleFbo.cleanUp();
		ParticleMaster.cleanUp();
		waterFbos.cleanUp();
		waterShader.cleanUp();
		guiRenderer.cleanUp();
		renderer.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();
	}
}
