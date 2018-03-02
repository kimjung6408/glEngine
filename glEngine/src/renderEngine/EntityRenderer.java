package renderEngine;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

import entities.Camera;
import entities.Entity;
import models.RawModel;
import models.TexturedModel;
import shaders.StaticShader;
import textures.ModelTexture;
import toolbox.Maths;

//renaming : shift + alt + R

public class EntityRenderer{
	
	private StaticShader shader;
	
	
	public EntityRenderer(StaticShader shader, Matrix4f projectionMatrix)
	{
		this.shader=shader;
		shader.startShader();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stopShader();
	}
	
	public void render(Map<TexturedModel, List<Entity>> entities)
	{
		for(TexturedModel model : entities.keySet())
		{
			prepareTexturedModel(model);
			List<Entity> batch=entities.get(model);
			
			for(Entity entity : batch)
			{
				prepareInstance(entity);
				
				//render
				//draw indexed object
				GL11.glDrawElements(GL11.GL_TRIANGLES, 
												 model.getRawModel().getVertexCount(),
												 GL11.GL_UNSIGNED_INT,
												 0);
			}
			
			unbindTexturedModel();
		}
	}
	
	private void prepareTexturedModel(TexturedModel texModel)
	{
		RawModel model=texModel.getRawModel();
		GL30.glBindVertexArray(model.getVaoID());
		
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		
		ModelTexture texture=texModel.getTexture();
		shader.loadNumberOfRows(texture.getNumOfRows());
		//set Culling Mask by transparency
		if(texture.isHasTransparency()) 
			{
				MasterRenderer.disableCulling();
			}
		
		//load specular factors
		shader.loadFakeLightingVariable(texture.isUseFakeLighting());
		shader.loadShineVariables(texture.getShineDamper(), texture.getReflectivity());
		
		
		//load textures
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texModel.getTexture().getID());
		
		shader.loadUseSpecularMap(texture.hasSpecularMap());
	
		if(texture.hasSpecularMap())
		{
			GL13.glActiveTexture(GL13.GL_TEXTURE1);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getSpecularMap());
		}
	}
	
	private void unbindTexturedModel()
	{
		MasterRenderer.enableCulling();
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		
		GL30.glBindVertexArray(0);
	}
	
	private void prepareInstance(Entity entity)
	{
		//load matrices
		Matrix4f transformationMatrix=Maths.createTransformationMatrix(entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
		shader.loadTransformationMatrix(transformationMatrix);
		shader.loadTexOffset(entity.getTextureXOffset(), entity.getTextureYOffset());
	}
}
