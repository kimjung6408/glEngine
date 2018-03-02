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
	//vao로 vertex, texCoords, index 정보를 로드한다.
	public RawModel loadToVAO(float[] positions, float[] texCoords, float[] normals, int[] indices)
	{
		int vaoID=createVAO();
		bindIndicesBuffer(indices);
		//position은 0번 레이아웃에 store, position은 x,y,z 3차원이므로 dataDimension은 3
		storeDataInAttributeSet(0,3,positions);
		//texCoords는 1번 레이아웃에 store, texCoords는 u,v 2차원이므로 dataDimension은 2
		storeDataInAttributeSet(1,2,texCoords);
		//normal은 2번 레이아웃에 store, normal은 x,y,z 3차원이므로 dataDimension은 2
		storeDataInAttributeSet(2,3,normals);
		unbindVAO();
		
		//vaoID와 vertex 개수를 전달한다. x,y,z 를 받고 이것이 하나의 vertex이므로 3으로 나눠준다.
		return new RawModel(vaoID, indices.length);
	}
	
	//GUI 로딩용 함수.
	public RawModel loadToVAO(float[] positions, int dataDimension)
	{
		int vaoID=createVAO();
		this.storeDataInAttributeSet(0, dataDimension, positions);
		unbindVAO();
		return new RawModel(vaoID, positions.length/2);
	}
	
	//Texture를 로딩한다.
	public int loadTexture(String fileName)
	{
		Texture texture=null;
		try
		{
		texture=TextureLoader.getTexture("PNG", new FileInputStream("res/"+fileName+".png"));
		
		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
		
		//level of detail bias. 약간 더 좋은 해상도로 렌더링되도록 설정한다.
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
		
		//개개의 instance (1개의 인스턴스) 마다 이 attribute가 바뀐다.
		GL33.glVertexAttribDivisor(attributeNumber, 1);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		GL30.glBindVertexArray(0);
	}
	
	//TexturedModel을 로딩한다.
	public TexturedModel loadTexturedModel(String ModelFileName, String TextureFileName)
	{
		return new TexturedModel(OBJLoader.loadObjModel(ModelFileName, this), new ModelTexture(loadTexture(TextureFileName)));
	}
	
	//vertex array object를 생성한다.
	private int createVAO()
	{
		int vaoID=GL30.glGenVertexArrays();
		
		//bind란, 해당 오브젝트가 사용중이도록 활성화하는 것이다.
		//OpenGL의 메모리 공간에는 "Target"이라는 영역이 존재한다.
		//"Target"공간은 활성화된 오브젝트들을 올려두는 공간이다.
		//즉, 생성된 오브젝트를 Target space에 옮기는 역할을 하는 것이 Bind 함수들이다.
		GL30.glBindVertexArray(vaoID);
		return vaoID;
	}
	
	//attribute에 데이터를 전달한다.
	private void storeDataInAttributeSet(int attributeNumber, int dataDimension, float[] data)
	{
		int vboID=GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		FloatBuffer buffer=storeDataInFloatBuffer(data);
		
		//해당 버퍼의 데이터의 용도를 명시함.
		//GL_ARRAY_BUFFER는 해당 buffer에 저장된 데이터를 attribute data로 사용하겠다고 명시하는 것임. 즉, 용도만 명시하는 것임.
		//실제 attribute data로 사용하기 위해서는 glVertexAttribPointer 함수를 호출해서 적용해야 함.
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

		//attributeNumber : vertexShader 상에서의 layout 번호.
		//vertex attribute를 구성하는 멤버의 개수. 1,2,3,4만 가능. x,y,z 이므로 3개
		//GL_FLOAT형 데이터
		//normalise=false
		//stride : pointer가 가리키는 배열에서 다음 구성요소까지의  byte수.
		//pointer : Vertex attribute의 배열 주소.
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
		
		//데이터를 넣는다.
		buffer.put(data);
		
		//버퍼를 쓰기모드에서 읽기모드로 변경한다.
		buffer.flip();
		return buffer;
	}
	
	//메모리상에서 vao와 vbo를 해제한다.
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
			
			//맨 첫 번째 파라미터는, cube map의 어떤 면에 bind 할 것인지를 설정.
			GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X+i, 0, GL11.GL_RGBA, data.getWidth(), data.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.getBuffer());
		}
		
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		
		//skybox상에 이미지상 경계에 존재하는 seam을 없게 한다.
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
