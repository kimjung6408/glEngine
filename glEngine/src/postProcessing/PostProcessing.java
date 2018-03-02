package postProcessing;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import bloom.BrightFilter;
import bloom.CombineFilter;
import gaussianBlur.HorizontalBlur;
import gaussianBlur.VerticalBlur;
import models.RawModel;
import renderEngine.Loader;

public class PostProcessing {
	
	private static final float[] POSITIONS = { -1, 1, -1, -1, 1, 1, 1, -1 };	
	private static RawModel quad;
	private static ContrastChanger contrastChanger;
	private static HorizontalBlur hBlur;
	private static VerticalBlur	vBlur;
	private static HorizontalBlur hBlur2;
	private static VerticalBlur	vBlur2;
	
	private static BrightFilter brightFilter;
	
	private static CombineFilter combineFilter;
	
	
	public static void init(Loader loader){
		quad = loader.loadToVAO(POSITIONS, 2);
		contrastChanger=new ContrastChanger();
		hBlur=new HorizontalBlur(Display.getWidth()/2, Display.getHeight()/2);
		vBlur=new VerticalBlur(Display.getWidth()/4, Display.getHeight()/4);
	
		hBlur2=new HorizontalBlur(Display.getWidth()/2, Display.getHeight()/2);
		vBlur2=new VerticalBlur(Display.getWidth()/2, Display.getHeight()/2);
	
		brightFilter=new BrightFilter(Display.getWidth()/2, Display.getHeight()/2);
		combineFilter =new CombineFilter();
	}
	
	public static void doPostProcessing(int colourTexture){
		start();
		hBlur2.render(colourTexture);
		vBlur2.render(hBlur2.getOutputTexture());
		hBlur.render(vBlur2.getOutputTexture());
		vBlur.render(hBlur.getOutputTexture());
		brightFilter.render(vBlur2.getOutputTexture());
		hBlur.render(brightFilter.getOutputTexture());
		vBlur.render(hBlur.getOutputTexture());
		combineFilter.render(colourTexture, vBlur.getOutputTexture());
		end();
	}
	
	public static void cleanUp(){
		hBlur.cleanUp();
		vBlur.cleanUp();
		hBlur2.cleanUp();
		vBlur2.cleanUp();
		brightFilter.cleanUp();
		combineFilter.cleanUp();
		contrastChanger.cleanUp();
	}
	
	private static void start(){
		GL30.glBindVertexArray(quad.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
	}
	
	private static void end(){
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
	}


}
