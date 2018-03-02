package bloom;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import postProcessing.ImageRenderer;

public class CombineFilter {
	
	private ImageRenderer renderer;
	private CombineShader shader;
	
	public CombineFilter(){
		shader = new CombineShader();
		shader.startShader();
		shader.connectTextureUnits();
		shader.stopShader();
		renderer = new ImageRenderer();
	}
	
	public void render(int colourTexture, int highlightTexture){
		shader.startShader();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, colourTexture);
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, highlightTexture);
		renderer.renderQuad();
		shader.stopShader();
	}
	
	public void cleanUp(){
		renderer.cleanUp();
		shader.cleanUp();
	}

}
