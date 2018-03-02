package renderEngine;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;
import entities.Entity;
import models.RawModel;
import models.TexturedModel;
import textures.ModelTexture;
import textures.TextureData;
//Load 3dModel into memory
public class Loader {
	
	private List<Integer> vaos=new ArrayList<Integer>();
	private List<Integer> vbos=new ArrayList<Integer>();
	private List<Integer> textures=new ArrayList<Integer>();
	//vao�� vertex, texCoords, index ������ �ε��Ѵ�.
	public RawModel loadToVAO(float[] positions, float[] texCoords, float[] normals, int[] indices)
	{
		int vaoID=createVAO();
		bindIndicesBuffer(indices);
		//position�� 0�� ���̾ƿ��� store, position�� x,y,z 3�����̹Ƿ� dataDimension�� 3
		storeDataInAttributeSet(0,3,positions);
		//texCoords�� 1�� ���̾ƿ��� store, texCoords�� u,v 2�����̹Ƿ� dataDimension�� 2
		storeDataInAttributeSet(1,2,texCoords);
		//normal�� 2�� ���̾ƿ��� store, normal�� x,y,z 3�����̹Ƿ� dataDimension�� 2
		storeDataInAttributeSet(2,3,normals);
		unbindVAO();
		
		//vaoID�� vertex ������ �����Ѵ�. x,y,z �� �ް� �̰��� �ϳ��� vertex�̹Ƿ� 3���� �����ش�.
		return new RawModel(vaoID, indices.length);
	}
	
	//GUI �ε��� �Լ�.
	public RawModel loadToVAO(float[] positions, int dataDimension)
	{
		int vaoID=createVAO();
		this.storeDataInAttributeSet(0, dataDimension, positions);
		unbindVAO();
		return new RawModel(vaoID, positions.length/2);
	}
	
	//Texture�� �ε��Ѵ�.
	public int loadTexture(String fileName)
	{
		Texture texture=null;
		try
		{
		texture=TextureLoader.getTexture("PNG", new FileInputStream("res/"+fileName+".png"));
		
		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
		
		//level of detail bias. �ణ �� ���� �ػ󵵷� �������ǵ��� �����Ѵ�.
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0f);
		
		if(GLContext.getCapabilities().GL_EXT_texture_filter_anisotropic)
		{
			float amount=Math.min(4f, GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));
			
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, amount);
		}
		
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();			
		}
		
		int textureID=texture.getTextureID();
		textures.add(textureID);
		return textureID;
	}
	
	public int createEmptyVBO(int floatCount)
	{
		int vbo=GL15.glGenBuffers();
		vbos.add(vbo);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, floatCount*4, GL15.GL_STATIC_DRAW);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		return vbo;
	}
	
	public void addInstancedAttribute(int vao, int vbo, int attributeNumber, int dataSize, int instancedDataLength, int offset)
	{
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL30.glBindVertexArray(vao);
		GL20.glVertexAttribPointer(attributeNumber, dataSize, GL11.GL_FLOAT, false, instancedDataLength*4, offset*4);
		
		//������ instance (1���� �ν��Ͻ�) ���� �� attribute�� �ٲ��.
		GL33.glVertexAttribDivisor(attributeNumber, 1);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		GL30.glBindVertexArray(0);
	}
	
	//TexturedModel�� �ε��Ѵ�.
	public TexturedModel loadTexturedModel(String ModelFileName, String TextureFileName)
	{
		return new TexturedModel(OBJLoader.loadObjModel(ModelFileName, this), new ModelTexture(loadTexture(TextureFileName)));
	}
	
	//vertex array object�� �����Ѵ�.
	private int createVAO()
	{
		int vaoID=GL30.glGenVertexArrays();
		
		//bind��, �ش� ������Ʈ�� ������̵��� Ȱ��ȭ�ϴ� ���̴�.
		//OpenGL�� �޸� �������� "Target"�̶�� ������ �����Ѵ�.
		//"Target"������ Ȱ��ȭ�� ������Ʈ���� �÷��δ� �����̴�.
		//��, ������ ������Ʈ�� Target space�� �ű�� ������ �ϴ� ���� Bind �Լ����̴�.
		GL30.glBindVertexArray(vaoID);
		return vaoID;
	}
	
	//attribute�� �����͸� �����Ѵ�.
	private void storeDataInAttributeSet(int attributeNumber, int dataDimension, float[] data)
	{
		int vboID=GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		FloatBuffer buffer=storeDataInFloatBuffer(data);
		
		//�ش� ������ �������� �뵵�� �����.
		//GL_ARRAY_BUFFER�� �ش� buffer�� ����� �����͸� attribute data�� ����ϰڴٰ� ����ϴ� ����. ��, �뵵�� ����ϴ� ����.
		//���� attribute data�� ����ϱ� ���ؼ��� glVertexAttribPointer �Լ��� ȣ���ؼ� �����ؾ� ��.
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

		//attributeNumber : vertexShader �󿡼��� layout ��ȣ.
		//vertex attribute�� �����ϴ� ����� ����. 1,2,3,4�� ����. x,y,z �̹Ƿ� 3��
		//GL_FLOAT�� ������
		//normalise=false
		//stride : pointer�� ����Ű�� �迭���� ���� ������ұ�����  byte��.
		//pointer : Vertex attribute�� �迭 �ּ�.
		GL20.glVertexAttribPointer(attributeNumber,dataDimension, GL11.GL_FLOAT, false,0,0);
		//unbind buffer
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}
	
	private void unbindVAO()
	{
		GL30.glBindVertexArray(0);
	}
	
	public void updateVbo(int vbo, float[] data, FloatBuffer buffer)
	{
		buffer.clear();
		buffer.put(data);
		buffer.flip();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer.capacity(), GL15.GL_STATIC_DRAW);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}
	
	private void bindIndicesBuffer(int[] indices)
	{
		int vboID=GL15.glGenBuffers();
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
		IntBuffer buffer=storeDataInIntBuffer(indices);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
	}
	
	private IntBuffer storeDataInIntBuffer(int[] data)
	{
		IntBuffer buffer=BufferUtils.createIntBuffer(data.length);
		
		buffer.put(data);
		buffer.flip();
		
		return buffer;
	}
	
	private FloatBuffer storeDataInFloatBuffer(float[] data)
	{
		FloatBuffer buffer=BufferUtils.createFloatBuffer(data.length);
		
		//�����͸� �ִ´�.
		buffer.put(data);
		
		//���۸� �����忡�� �б���� �����Ѵ�.
		buffer.flip();
		return buffer;
	}
	
	//�޸𸮻󿡼� vao�� vbo�� �����Ѵ�.
	public void cleanUp()
	{
		for(int vao : vaos)
		{
			GL30.glDeleteVertexArrays(vao);
		}
		
		for(int vbo : vbos)
		{
			GL15.glDeleteBuffers(vbo);			
		}
		
		for(int texture :textures)
		{
			GL11.glDeleteTextures(texture);
		}
	}
	
	public Entity loadEntity(String ObjectFileName, String textureFileName, Vector3f position, float rotX, float rotY, float rotZ, float scale)
	{
		TexturedModel model=loadTexturedModel(ObjectFileName, textureFileName);
		Entity entity=new Entity(model, position, rotX, rotY, rotZ, scale);
		
		return entity;
	}
	
	public int loadCubeMap(String[] textureFiles)
	{
		int texID=GL11.glGenTextures();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texID);
		
		for(int i=0; i<textureFiles.length; i++)
		{
			TextureData data=decodeTextureFile("res/sky/"+textureFiles[i]+".png");
			
			//�� ù ��° �Ķ���ʹ�, cube map�� � �鿡 bind �� �������� ����.
			GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X+i, 0, GL11.GL_RGBA, data.getWidth(), data.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.getBuffer());
		}
		
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		
		//skybox�� �̹����� ��迡 �����ϴ� seam�� ���� �Ѵ�.
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		
		
		textures.add(texID);
		return texID;
	}
	
	private TextureData decodeTextureFile(String fileName) {
		int width = 0;
		int height = 0;
		ByteBuffer buffer = null;
		try {
			FileInputStream in = new FileInputStream(fileName);
			PNGDecoder decoder = new PNGDecoder(in);
			width = decoder.getWidth();
			height = decoder.getHeight();
			buffer = ByteBuffer.allocateDirect(4 * width * height);
			decoder.decode(buffer, width * 4, Format.RGBA);
			buffer.flip();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Tried to load texture " + fileName + ", didn't work");
			System.exit(-1);
		}
		return new TextureData(buffer, width, height);
	}
}
