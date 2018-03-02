package water;

import java.util.List;

import models.RawModel;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import renderEngine.DisplayManager;
import renderEngine.Loader;
import toolbox.Maths;
import entities.Camera;
import entities.Light;

public class WaterRenderer {
	
	private static final String DUDV_MAP="waterDUDV";
	private static final String NORMAL_MAP="matchingNormalMap";
	private static final float WAVE_SPEED=0.1f;

	private RawModel quad;
	private WaterShader shader;
	private WaterFrameBuffers fbos;
	
	private int dudvTextureID;
	private int normalMapID;
	private float moveFactor=0.0f;

	public WaterRenderer(Loader loader, WaterShader shader, Matrix4f projectionMatrix, WaterFrameBuffers fbos) {
		this.shader = shader;
		this.fbos=fbos;
		dudvTextureID=loader.loadTexture(DUDV_MAP);
		normalMapID=loader.loadTexture(NORMAL_MAP);
		shader.startShader();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.connectTextures();
		shader.stopShader();
		setUpVAO(loader);
	}

	public void render(List<WaterTile> water, Camera camera, Light sun) {
		prepareRender(camera, sun);	
		for (WaterTile tile : water) {
			Matrix4f modelMatrix = Maths.createTransformationMatrix(
					new Vector3f(tile.getX(), tile.getHeight(), tile.getZ()), 0, 0, 0,
					WaterTile.TILE_SIZE);
			shader.loadModelMatrix(modelMatrix);
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, quad.getVertexCount());
		}
		unbind();
	}
	
	private void prepareRender(Camera camera, Light sun){
		shader.startShader();
		shader.loadViewMatrix(camera);
		GL30.glBindVertexArray(quad.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		
		//increase move factor
		moveFactor+=WAVE_SPEED*DisplayManager.getFrameTimeSeconds();
		moveFactor%=1; //prevent overflow
		shader.loadMoveFactor(moveFactor);
		
		//load light for normal mapping
		shader.loadLight(sun);
		
		//activate reflection texture
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		//0번 바인딩 포인트에 2D Reflection texture를 바인딩한다.
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbos.getReflectionTexture());
		
		//activate refraction texture
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		//1번 바인딩 포인트에 2D Refraction texture를 바인딩한다.
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbos.getRefractionTexture());
		
		//activate DUDV texture
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		//2번 바인딩 포인트에 2D DUDV를 바인딩한다.
		GL11.glBindTexture(GL11.GL_TEXTURE_2D,dudvTextureID);
		
		//activate normalMap
		GL13.glActiveTexture(GL13.GL_TEXTURE3);
		//3번 바인딩 포인트에 Normal Map을 바인딩한다.
		GL11.glBindTexture(GL11.GL_TEXTURE_2D,normalMapID);
		
		//activate depthMap
		GL13.glActiveTexture(GL13.GL_TEXTURE4);
		//4번 바인딩 포인트에 depth Map을 바인딩한다.
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbos.getRefractionDepthTexture());
	
		//boundary glitch 해결을 위하여 alpha blending을 허용한다.
		AlphaBlending(true);
	
	
	}
	
	private void unbind(){
		AlphaBlending(false);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		shader.stopShader();
	}

	private void setUpVAO(Loader loader) {
		// Just x and z vectex positions here, y is set to 0 in v.shader
		float[] vertices = { -1, -1, -1, 1, 1, -1, 1, -1, -1, 1, 1, 1 };
		quad = loader.loadToVAO(vertices, 2);
	}
	
	private void AlphaBlending(boolean doAlphaBlending)
	{
		if(doAlphaBlending)
		{
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}
		else
		{
			GL11.glDisable(GL11.GL_BLEND);
		}
		
	}
}
