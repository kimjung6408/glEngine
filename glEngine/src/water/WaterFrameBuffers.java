package water;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

/*
 * Frame Buffer
 * 여러가지 버퍼 혹은 텍스쳐로의 포인터를 가지고 저장할 수 있는 data structure
 * frame buffer에 attach한다는 것은, attach하는 해당 오브젝트의 주소를 프레임 버퍼가 저장하도록(가리키도록) 한다는 것이다.
 * attach 가능한 오브젝트로는 Render Buffer와 Texture가 있다. 이 두 대상에 렌더링할 수 있다.
 * 여러 개의 object들을 attach할 수 있다.
 * 보통은 texture에 렌더링을 하기 위하여 많이 사용한다. 다만 texture에 렌더링하는 것은 native가 아닌 format을 사용하기 때문에 속도가 느리다.
 * 멀티 패스 렌더링에 많이 이용한다.
 * 
 * Render Buffer
 * 렌더 버퍼는 pixel값을 native format으로 저장하는 버퍼이다. native format을 사용하기 때문에 텍스쳐에 비해 속도가 빠르다.
 * 다만, Render buffer는 pixel값 native로 저장하기 때문에 화면의 특정 위치에 바로 copy하기가 편하다. (pixel transfer operation)
 * texture에 비하여, 멀티 패스 렌더링을 할 때 sampling 하는 것은 더 어렵다. (interpolation 등의 비용이 더 큼)
 * single draw procedure에서의 depth test, stencil test, 그리고 double buffering에 유용하다.
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
		//default frame buffer를 설정한다.
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
	}
	
	
	
	
	//반드시 렌더링 전에 호출해야 할 함수들 BEGIN
	public void bindReflectionFrameBuffer()
	{
		bindFrameBuffer(reflectionFrameBufferID, REFLECTION_WIDTH, REFLECTION_HEIGHT);
	}
	
	public void bindRefractionFrameBuffer()
	{
		bindFrameBuffer(refractionFrameBufferID, REFRACTION_WIDTH, REFRACTION_HEIGHT);
	}
	
	//반드시 렌더링 전에 호출해야 할 함수들 END
	
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
		//엉뚱한 텍스쳐가 bind 되지 않도록 초기화한다.
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		
		//해당 framebuffer를 바인딩한다.
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBufferID);
		
		//렌더링 대상의 viewport를 설정한다.
		GL11.glViewport(0, 0, width, height);
	}
	
	
	
	//frame buffer를 생성하고 ID를 리턴한다.
	private int createFrameBuffer()
	{
		//프레임 버퍼 ID를 부여한다.
		int frameBuffer=GL30.glGenFramebuffers();
		
		//프레임 버퍼 생성을 완료하려면 bind를 해야 한다. GL_FRAMEBUFFER 바인딩 포인트에 frameBuffer를 바인딩하여 생성 완료한다.
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
		
		//0번 attachment point에 항상 렌더링할 것임을 알린다.
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
	
	//depth render buffer를 생성하고,  GL_FRAMEBUFFER 바인딩 포인트에 GL_DEPTH_ATTACHMENT 포인트에 attach한다.
	private int createDepthBufferAttachment(int width, int height)
	{
		//depthBuffer ID를 부여한다.
		int depthBuffer=GL30.glGenRenderbuffers();
		
		//GL_RENDERBUFFER 바인딩포인트에 최초 바이딩하여 depthBuffer (Render Buffer)를 생성한다.
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
		
		//Render buffer의 용도를 명시하고, 가로 세로 크기를 알려준다.
		GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, width, height);
		
		//frame buffer의 depth attachment 포인트에 depthBuffer를 attach한다.
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBuffer);
	
		return depthBuffer;
	}

	//depth texture를 생성하고, GL_FRAMEBUFFER 바인딩 포인트의 GL_DEPTH_ATTACHMENT 포인트에 ATTACH한다.
	private int createDepthTextureAttachment(int width, int height)
	{
		int texture=GL11.glGenTextures();
		
		//텍스쳐를 최초로 바인딩하여 생성 완료한다.
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		
		//텍스쳐의 크기 및 형식을 설정한다. 초기 텍스쳐 데이터는 없다(null)
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
				0, 
				GL14.GL_DEPTH_COMPONENT32, //포맷
				width, //가로
				height, //세로
				0, 
				GL11.GL_DEPTH_COMPONENT, 
				GL11.GL_FLOAT, //바이트 단위
				(ByteBuffer)null);
		
		//Linear interpolation을 적용한다.
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		
		//framebuffer 바인딩 포인트의 depth attachment point에 이 텍스쳐를 사용하겠음을 알린다.
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, texture, 0);
		
		return texture;
	}

	//텍스쳐를 생성하고, GL_FRAMEBUFFER 바인딩 포인트의 0번 attachment point에 사용할 것임을 명시한다.
	private int createTextureAttachment(int width, int height)
	{
		//texture ID를 부여한다.
		int texture=GL11.glGenTextures();
		
		//텍스쳐를 최초로 바인딩하여 생성 완료한다.
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		
		//텍스쳐의 크기 및 형식을 설정한다. 초기 텍스쳐 데이터는 없다(null)
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
				0, 
				GL11.GL_RGB, //포맷
				width, //가로
				height, //세로
				0, 
				GL11.GL_RGB, 
				GL11.GL_UNSIGNED_BYTE, //바이트 단위
				(ByteBuffer)null);
		
		//Linear interpolation을 적용한다.
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		//framebuffer 바인딩 포인트의 0번 attachment point에 이 텍스쳐를 사용하겠음을 알린다.
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, texture, 0);
		
		return texture;
	}


}
