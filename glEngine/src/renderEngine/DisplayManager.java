package renderEngine;

import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.*;
import org.lwjgl.*;


public class DisplayManager {
	
	private static final int WIDTH=1280;
	private static final int HEIGHT=720;
	private static final int FPS_CAP=120;

	private static long lastFrameTime;
	private static float delta;
	
	//���÷��̸� �����ϰ�, ȭ���� ũ�⸦ �����Ѵ�.
	public static void createDisplay(){
		ContextAttribs attribs = new ContextAttribs(3,3)
				.withForwardCompatible(true)
				.withProfileCore(true);
		
		
		//Determine the size of display
		try
		{
		Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
		Display.create(new PixelFormat().withDepthBits(24), attribs);
		Display.setTitle("Our First Display");
		GL11.glEnable(GL13.GL_MULTISAMPLE);
		}
		catch(LWJGLException e)
		{
			e.printStackTrace();			
		}
		
		GL11.glViewport(0,0,WIDTH, HEIGHT);
		lastFrameTime=getCurrentTime();
	}
	
	
	//game�� FPS�� ���� Synchronize�Ѵ�.
	public static void updateDisplay(){
		
		Display.sync(FPS_CAP);
		Display.update();
		long currentFrameTime=getCurrentTime();
		delta=(currentFrameTime-lastFrameTime)/1000.0f;
		lastFrameTime=currentFrameTime;
	}
	
	public static void closeDisplay(){
		
		Display.destroy();
		
	}
	
	public static float getFrameTimeSeconds()
	{
		return delta;		
	}
	
	private static long getCurrentTime()
	{
		return Sys.getTime()*1000/Sys.getTimerResolution();
	}
}