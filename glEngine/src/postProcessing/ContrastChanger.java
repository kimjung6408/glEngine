package postProcessing;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class ContrastChanger {
	private ImageRenderer renderer;
	private ContrastShader shader;
	
	public ContrastChanger()
	{
		shader=new ContrastShader();
		//����Ʈ�� Image renderer�� �����ϸ�, ���� ȭ�鿡 �������ȴ�.
		renderer=new ImageRenderer();
	}
	
	public void cleanUp()
	{
		renderer.cleanUp();
		shader.cleanUp();
	}
	
	public void render(int texture)
	{
		shader.startShader();
		
		//activate texture for rendering
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		
		renderer.renderQuad();
		
		shader.stopShader();
	}
}
