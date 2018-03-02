package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import models.TexturedModel;
import renderEngine.DisplayManager;
import terrains.Terrain;

public class Player extends Entity {
	public static final float RUN_SPEED=20;
	public static final float TURN_SPEED=160;
	public static final float GRAVITY=-50;
	private static final float JUMP_POWER=30;
	
	private float currentSpeed=0;
	private float currentTurnSpeed=0;
	private float upwardSpeed=0;
	
	private boolean isJump=false;
	
	public Player(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
		super(model, position, rotX, rotY, rotZ, scale);
		// TODO Auto-generated constructor stub
	}
	
	public void move(Terrain terrain)
	{
		checkInputs();
		super.increaseRotation(0, currentTurnSpeed*DisplayManager.getFrameTimeSeconds(), 0);
		
		//get delta distance in xyz coordinates
		float distance=currentSpeed*DisplayManager.getFrameTimeSeconds();
		float dx=distance*(float)Math.sin(Math.toRadians(super.getRotY()));
		float dz=distance*(float)Math.cos(Math.toRadians(super.getRotY()));
		//move position
		super.increasePosition(dx, 0, dz);
		
		//jump calculation
		upwardSpeed+=GRAVITY*DisplayManager.getFrameTimeSeconds();
		super.increasePosition(0, upwardSpeed*DisplayManager.getFrameTimeSeconds(), 0);
		
		
		//test terrain collision
		float terrainHeight=terrain.getHeightOfTerrain(super.getPosition().x, super.getPosition().z);
		if(super.getPosition().y<terrainHeight)
		{
			upwardSpeed=0;
			super.getPosition().y=terrainHeight;
			isJump=false;
		}
		
	}
	
	private void jump()
	{
		if(!isJump)
		{
			isJump=true;
			this.upwardSpeed=JUMP_POWER;
		}
	}
	
	private void checkInputs()
	{
		//Moving
		if(Keyboard.isKeyDown(Keyboard.KEY_W))
		{
			this.currentSpeed=RUN_SPEED;
		}
		else if(Keyboard.isKeyDown(Keyboard.KEY_S))
		{
			this.currentSpeed=-RUN_SPEED;
		}
		else
		{
			this.currentSpeed=0;
		}
		
		//Rotating
		if(Keyboard.isKeyDown(Keyboard.KEY_D))
		{
			this.currentTurnSpeed=-TURN_SPEED;
		}
		else if(Keyboard.isKeyDown(Keyboard.KEY_A))
		{
			this.currentTurnSpeed=TURN_SPEED;
		}
		else
		{
			this.currentTurnSpeed=0;
		}
		
		//Jumping
		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE))
		{
			jump();
		}
	}
	
}
