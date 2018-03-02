package water;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

/*
 * Frame Buffer
 * �������� ���� Ȥ�� �ؽ��ķ��� �����͸� ������ ������ �� �ִ� data structure
 * frame buffer�� attach�Ѵٴ� ����, attach�ϴ� �ش� ������Ʈ�� �ּҸ� ������ ���۰� �����ϵ���(����Ű����) �Ѵٴ� ���̴�.
 * attach ������ ������Ʈ�δ� Render Buffer�� Texture�� �ִ�. �� �� ��� �������� �� �ִ�.
 * ���� ���� object���� attach�� �� �ִ�.
 * ������ texture�� �������� �ϱ� ���Ͽ� ���� ����Ѵ�. �ٸ� texture�� �������ϴ� ���� native�� �ƴ� format�� ����ϱ� ������ �ӵ��� ������.
 * ��Ƽ �н� �������� ���� �̿��Ѵ�.
 * 
 * Render Buffer
 * ���� ���۴� pixel���� native format���� �����ϴ� �����̴�. native format�� ����ϱ� ������ �ؽ��Ŀ� ���� �ӵ��� ������.
 * �ٸ�, Render buffer�� pixel�� native�� �����ϱ� ������ ȭ���� Ư�� ��ġ�� �ٷ� copy�ϱⰡ ���ϴ�. (pixel transfer operation)
 * texture�� ���Ͽ�, ��Ƽ �н� �������� �� �� sampling �ϴ� ���� �� ��ƴ�. (interpolation ���� ����� �� ŭ)
 * single draw procedure������ depth test, stencil test, �׸��� double buffering�� �����ϴ�.
 * 
 * */




public class WaterFrameBuffers {
	protected static final int REFLECTION_WIDTH=320;
	private static final int REFLECTION_HEIGHT=180;
	
	protected static final int REFRACTION_WIDTH=1280;
	private static final int REFRACTION_HEIGHT=720;
	
	private int reflectionFrameBufferID;
	private int reflectionTextureID;
	private int reflectionDepthBufferID;
	
	private int refractionFrameBufferID;
	private int refractionTextureID;
	private int refractionDepthTextureID;
	
	public WaterFrameBuffers()
	{
		initReflectionFrameBuffer();
		initRefractionFrameBuffer();
	}
	
	public void cleanUp()
	{
		GL30.glDeleteFramebuffers(reflectionFrameBufferID);
		GL11.glDeleteTextures(reflectionTextureID);
		GL30.glDeleteRenderbuffers(reflectionDepthBufferID);
		
		GL30.glDeleteFramebuffers(refractionFrameBufferID);
		GL11.glDeleteTextures(refractionTextureID);
		GL11.glDeleteTextures(refractionDepthTextureID);
	}
	
	public void unbindCurrentFrameBuffer()
	{
		//default frame buffer�� �����Ѵ�.
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
	}
	
	
	
	
	//�ݵ�� ������ ���� ȣ���ؾ� �� �Լ��� BEGIN
	public void bindReflectionFrameBuffer()
	{
		bindFrameBuffer(reflectionFrameBufferID, REFLECTION_WIDTH, REFLECTION_HEIGHT);
	}
	
	public void bindRefractionFrameBuffer()
	{
		bindFrameBuffer(refractionFrameBufferID, REFRACTION_WIDTH, REFRACTION_HEIGHT);
	}
	
	//�ݵ�� ������ ���� ȣ���ؾ� �� �Լ��� END
	
	public int getReflectionTexture()
	{
		return reflectionTextureID;
	}
	
	public int getRefractionTexture()
	{
		return refractionTextureID;
	}
	
	public int getRefractionDepthTexture()
	{
		return refractionDepthTextureID;
	}
	
	private void bindFrameBuffer(int frameBufferID, int width, int height)
	{
		//������ �ؽ��İ� bind ���� �ʵ��� �ʱ�ȭ�Ѵ�.
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		
		//�ش� framebuffer�� ���ε��Ѵ�.
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBufferID);
		
		//������ ����� viewport�� �����Ѵ�.
		GL11.glViewport(0, 0, width, height);
	}
	
	
	
	//frame buffer�� �����ϰ� ID�� �����Ѵ�.
	private int createFrameBuffer()
	{
		//������ ���� ID�� �ο��Ѵ�.
		int frameBuffer=GL30.glGenFramebuffers();
		
		//������ ���� ������ �Ϸ��Ϸ��� bind�� �ؾ� �Ѵ�. GL_FRAMEBUFFER ���ε� ����Ʈ�� frameBuffer�� ���ε��Ͽ� ���� �Ϸ��Ѵ�.
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
		
		//0�� attachment point�� �׻� �������� ������ �˸���.
		GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
		
		return frameBuffer;
	}
	
	
	private void initReflectionFrameBuffer()
	{
		reflectionFrameBufferID=createFrameBuffer();
		reflectionTextureID=createTextureAttachment(REFLECTION_WIDTH, REFLECTION_HEIGHT);
		reflectionDepthBufferID=createDepthBufferAttachment(REFLECTION_WIDTH, REFLECTION_HEIGHT);
		unbindCurrentFrameBuffer();
	}
	
	private void initRefractionFrameBuffer()
	{
		refractionFrameBufferID=createFrameBuffer();
		refractionTextureID=createTextureAttachment(REFRACTION_WIDTH, REFRACTION_HEIGHT);
		refractionDepthTextureID=createDepthTextureAttachment(REFRACTION_WIDTH, REFRACTION_HEIGHT);
		unbindCurrentFrameBuffer();
	}
	
	//depth render buffer�� �����ϰ�,  GL_FRAMEBUFFER ���ε� ����Ʈ�� GL_DEPTH_ATTACHMENT ����Ʈ�� attach�Ѵ�.
	private int createDepthBufferAttachment(int width, int height)
	{
		//depthBuffer ID�� �ο��Ѵ�.
		int depthBuffer=GL30.glGenRenderbuffers();
		
		//GL_RENDERBUFFER ���ε�����Ʈ�� ���� ���̵��Ͽ� depthBuffer (Render Buffer)�� �����Ѵ�.
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
		
		//Render buffer�� �뵵�� ����ϰ�, ���� ���� ũ�⸦ �˷��ش�.
		GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, width, height);
		
		//frame buffer�� depth attachment ����Ʈ�� depthBuffer�� attach�Ѵ�.
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBuffer);
	
		return depthBuffer;
	}

	//depth texture�� �����ϰ�, GL_FRAMEBUFFER ���ε� ����Ʈ�� GL_DEPTH_ATTACHMENT ����Ʈ�� ATTACH�Ѵ�.
	private int createDepthTextureAttachment(int width, int height)
	{
		int texture=GL11.glGenTextures();
		
		//�ؽ��ĸ� ���ʷ� ���ε��Ͽ� ���� �Ϸ��Ѵ�.
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		
		//�ؽ����� ũ�� �� ������ �����Ѵ�. �ʱ� �ؽ��� �����ʹ� ����(null)
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
				0, 
				GL14.GL_DEPTH_COMPONENT32, //����
				width, //����
				height, //����
				0, 
				GL11.GL_DEPTH_COMPONENT, 
				GL11.GL_FLOAT, //����Ʈ ����
				(ByteBuffer)null);
		
		//Linear interpolation�� �����Ѵ�.
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		
		//framebuffer ���ε� ����Ʈ�� depth attachment point�� �� �ؽ��ĸ� ����ϰ����� �˸���.
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, texture, 0);
		
		return texture;
	}

	//�ؽ��ĸ� �����ϰ�, GL_FRAMEBUFFER ���ε� ����Ʈ�� 0�� attachment point�� ����� ������ ����Ѵ�.
	private int createTextureAttachment(int width, int height)
	{
		//texture ID�� �ο��Ѵ�.
		int texture=GL11.glGenTextures();
		
		//�ؽ��ĸ� ���ʷ� ���ε��Ͽ� ���� �Ϸ��Ѵ�.
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		
		//�ؽ����� ũ�� �� ������ �����Ѵ�. �ʱ� �ؽ��� �����ʹ� ����(null)
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
				0, 
				GL11.GL_RGB, //����
				width, //����
				height, //����
				0, 
				GL11.GL_RGB, 
				GL11.GL_UNSIGNED_BYTE, //����Ʈ ����
				(ByteBuffer)null);
		
		//Linear interpolation�� �����Ѵ�.
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		//framebuffer ���ε� ����Ʈ�� 0�� attachment point�� �� �ؽ��ĸ� ����ϰ����� �˸���.
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, texture, 0);
		
		return texture;
	}


}
