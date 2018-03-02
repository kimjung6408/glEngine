package terrains;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import models.RawModel;
import renderEngine.Loader;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.Maths;

public class Terrain {

	private static final float SIZE=800;
	private static final float MAX_HEIGHT=70;
	private static final float MAX_PIXEL_COLOR=256*256*256;
	
	private float x;
	private float z;
	private RawModel model;
	private TerrainTexturePack texturePack;
	private TerrainTexture blendMap;
	
	//heights for collision detection
	private float[][] heights;
	
	public Terrain(int gridX, int gridY, Loader loader, TerrainTexturePack texPack, TerrainTexture blendMap, String heightMap)
	{
		this.texturePack=texPack;
		this.blendMap=blendMap;
		this.x=gridX*SIZE;
		this.z=gridY*SIZE;
		this.model=generateTerrain(loader, heightMap);
		
	}
	
	
	
	public float getX() {
		return x;
	}



	public float getZ() {
		return z;
	}



	public RawModel getModel() {
		return model;
	}

	

	public TerrainTexturePack getTexturePack() {
		return texturePack;
	}



	public TerrainTexture getBlendMap() {
		return blendMap;
	}



	private RawModel generateTerrain(Loader loader, String heightMap)
	{
		BufferedImage image=null;
		//load heightMap data
		try
		{
			image=ImageIO.read(new File("res/"+heightMap+".png"));
		}catch(IOException e)
		{
			e.printStackTrace();
		}
		
		int VERTEX_COUNT=image.getHeight();
		heights=new float[VERTEX_COUNT][VERTEX_COUNT];
		
		int count=VERTEX_COUNT*VERTEX_COUNT;
		
		float[] vertices=new float[count*3];
		float[] normals=new float[count*3];
		float[] texCoords=new float[count*2];
		int[] indices=new int[6*(VERTEX_COUNT-1)*(VERTEX_COUNT-1)];
		int vertexPointer=0;
		
		for(int i=0; i<VERTEX_COUNT; i++)
		{
			for(int j=0; j<VERTEX_COUNT; j++)
			{
				//가로를 따라가며 정점을 생성.
				vertices[3*vertexPointer]=(float)j/((float)VERTEX_COUNT-1)*SIZE; //x
				
				float height=getHeight(j,i,image);
				heights[j][i]=height;
				
				vertices[3*vertexPointer+1]=height; //y
				vertices[3*vertexPointer+2]=(float)i/((float)VERTEX_COUNT-1)*SIZE; //z
				
				Vector3f normal=calculateNormal(j,i, image);
				normals[3*vertexPointer]=normal.x; //x
				normals[3*vertexPointer+1]=normal.y; //y
				normals[3*vertexPointer+2]=normal.z; //z
				
				texCoords[2*vertexPointer]=(float)j/((float)VERTEX_COUNT-1);
				texCoords[2*vertexPointer+1]=(float)i/((float)VERTEX_COUNT-1);
				
				vertexPointer++;
			}
		}
		
		int pointer=0;
		
		for(int gz=0; gz<VERTEX_COUNT-1; gz++)
		{
			for(int gx=0; gx<VERTEX_COUNT-1; gx++)
			{
				int topLeft=(gz*VERTEX_COUNT)+gx;
				int topRight=topLeft+1;
				int bottomLeft=((gz+1)*VERTEX_COUNT)+gx;
				int bottomRight=bottomLeft+1;
				
				indices[pointer++]=topLeft;
				indices[pointer++]=bottomLeft;
				indices[pointer++]=topRight;
				
				indices[pointer++]=topRight;
				indices[pointer++]=bottomLeft;
				indices[pointer++]=bottomRight;
				
			}
		}
		
		return loader.loadToVAO(vertices, texCoords, normals, indices);
	}
	
	private float getHeight(int x, int z, BufferedImage image)
	{
		if(x<0 || x>=image.getHeight() || z<0 || z>=image.getHeight())
		{
			return 0;
		}
		
		//[-MAX_PIXEL_COLOR, 0]
		float height=image.getRGB(x, z);
		
		//[-MAX_PIXEL_COLOR/2f, MAX_PIXEL_COLOR/2f]
		height+=MAX_PIXEL_COLOR/2f;
		
		//[-1,1]
		height/=MAX_PIXEL_COLOR/2f;
		
		//[-MAX_HEIGHT, MAX_HEIGHT]
		height*=MAX_HEIGHT;
		
		return height;
	}
	
	private Vector3f calculateNormal(int x, int z, BufferedImage image)
	{
		float heightL=getHeight(x-1, z, image);
		float heightR=getHeight(x+1, z, image);
		float heightD=getHeight(x,z-1, image);
		float heightU=getHeight(x, z+1, image);
		
		Vector3f normal=new Vector3f(heightL-heightR, 2f, heightD-heightU);
		normal.normalise();
		return normal;
	}

	//world좌표를 기준으로 terrain의 높이를 구한다.
	public float getHeightOfTerrain(float worldX, float worldZ)
	{
		float terrainX=worldX-this.x;
		float terrainZ=worldZ-this.z;
		
		//get grid square size
		float gridSquareSize=SIZE/((float)heights.length-1.0f);
		
		//get grid index
		int gridX=(int)Math.floor(terrainX/gridSquareSize);
		int gridZ=(int)Math.floor(terrainZ/gridSquareSize);
		
		//out of terrain, then height is 0
		if(gridX>=heights.length-1 || gridZ>=heights.length-1 || gridX<0 || gridZ<0)
		{
			return 0;
		}
		
		//get x,z coord inner part of a grid
		float xCoord=(terrainX%gridSquareSize)/gridSquareSize;
		float zCoord=(terrainX%gridSquareSize)/gridSquareSize;
		
		//어떤 triangle에 속하는지 판정한다.
		float answer;
		
		if (xCoord <= (1-zCoord)) {
			answer = Maths
					.barryCentric(new Vector3f(0, heights[gridX][gridZ], 0), new Vector3f(1,
							heights[gridX + 1][gridZ], 0), new Vector3f(0,
							heights[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));
		} else {
			answer = Maths
					.barryCentric(new Vector3f(1, heights[gridX + 1][gridZ], 0), new Vector3f(1,
							heights[gridX + 1][gridZ + 1], 1), new Vector3f(0,
							heights[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));
		}
		
		return answer;
	}
}
