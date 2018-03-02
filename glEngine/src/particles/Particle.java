package particles;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Player;
import renderEngine.DisplayManager;

public class Particle {
	private Vector3f position;
	private Vector3f velocity;
	private float gravityEffect;
	private float lifeLength;
	private float rotation;
	private float scale;
	
	
	
	private ParticleTexture texture;
	private Vector2f texOffset1=new Vector2f();
	private Vector2f texOffset2=new Vector2f();
	private float blend=0;
	
	private float elapsedTime=0;
	
	
	//particle들을 카메라와의 거리에 따라 ordering 하기 위하여 필요.
	private float distanceFromCamera;

	public Particle(ParticleTexture texture, Vector3f position, Vector3f velocity, float gravityEffect, float lifeLength, float rotation,
			float scale) {
		this.texture=texture;
		this.position = position;
		this.velocity = velocity;
		this.gravityEffect = gravityEffect;
		this.lifeLength = lifeLength;
		this.rotation = rotation;
		this.scale = scale;
		
		ParticleMaster.addParticle(this);
	}
	
	public ParticleTexture getTexture()
	{
		return texture;
	}

	public Vector3f getPosition() {
		return position;
	}

	public float getRotation() {
		return rotation;
	}

	public float getScale() {
		return scale;
	}
	
	//파티클이 살아있다면, true
	//수명이 다했다면 false;
	protected boolean update(Camera cam)
	{
		//파티클을 이동시킨다.
		velocity.y+=Player.GRAVITY*gravityEffect*DisplayManager.getFrameTimeSeconds();
		Vector3f change=new Vector3f(velocity);
		change.scale(DisplayManager.getFrameTimeSeconds());
		Vector3f.add(change, position, position);
		
		distanceFromCamera=Vector3f.sub(cam.getPosition(), position, null).lengthSquared();
		
		
		elapsedTime+=DisplayManager.getFrameTimeSeconds();
		updateTextureCoordsInfo();
		boolean isAlive=(elapsedTime<lifeLength);
		
		return isAlive;
	}

	public Vector2f getTexOffset1() {
		return texOffset1;
	}

	public Vector2f getTexOffset2() {
		return texOffset2;
	}
	
	private void updateTextureCoordsInfo()
	{
		float lifeFactor=elapsedTime/lifeLength;
		
		int stageCount=texture.getNumOfRows()*texture.getNumOfRows();
		float atlasProgress=lifeFactor*(float)stageCount;
		
		int index1=(int)Math.floor(atlasProgress);
		int index2=index1< stageCount-1 ? index1+1 : index1;
		
		this.blend=atlasProgress%1;
		
		setTextureOffset(texOffset1 ,index1);
		setTextureOffset(texOffset2 ,index2);
	}
	
	private void setTextureOffset(Vector2f offset, int index)
	{
		int column=index%texture.getNumOfRows();
		int row=index/texture.getNumOfRows();
		
		offset.x= (float)column /(float) texture.getNumOfRows();
		offset.y=(float) row /(float) texture.getNumOfRows();
	}

	public float getBlend()
	{
		return blend;
	}

	public float getDistanceFromCamera() {
		return distanceFromCamera;
	}

}
