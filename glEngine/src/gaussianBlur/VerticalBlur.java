package gaussianBlur;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import postProcessing.ImageRenderer;

public class VerticalBlur {
	
	private ImageRenderer renderer;
	private VerticalBlurShader shader;
	
	public VerticalBlur(int targetFboWidth, int targetFboHeight){
		shader = new VerticalBlurShader();
		renderer = new ImageRenderer(targetFboWidth, targetFboHeight);
		shader.startShader();
		shader.loadTargetHeight(targetFboHeight);
		shader.stopShader();
	}

	
	public void render(int texture){
		shader.startShader();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		renderer.renderQuad();
		shader.stopShader();
	}
	
	public int getOutputTexture(){
		return renderer.getOutputTexture();
	}
	
	public void cleanUp(){
		renderer.cleanUp();
		shader.cleanUp();
	}
}
