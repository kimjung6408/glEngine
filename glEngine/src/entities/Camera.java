package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

public class Camera {
	
	private float distanceFromPlayer=50;
	private float angleAroundPlayer=0;
	
	
	private Vector3f position=new Vector3f(100,20,800);
	private float pitch=20;
	private float yaw=0;
	private float roll;
	
	private Player player;
	
	public Camera(Player player)
	{
		this.player=player;
	}
	
	public void move()
	{
		calculateZoom();
		calculatePitch();
		calculateAngleAroundPlayer();
		
		float hDistance=calculateHorizontalDistance();
		float vDistance=calculateVerticalDistance();
		
		calculateCameraPosition(hDistance, vDistance);
	
		//calculate yaw angle
		this.yaw=180-(player.getRotY()+angleAroundPlayer);
	
	}

	public Vector3f getPosition() {
		return position;
	}

	public float getPitch() {
		return pitch;
	}
	
	public void invertPitch()
	{
		this.pitch=-pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public float getRoll() {
		return roll;
	}
	
	//마우스 휠을 이용하여 줌인, 줌아웃
	private void calculateZoom()
	{
		float zoomLevel=Mouse.getDWheel()*0.1f;
		
		distanceFromPlayer-=zoomLevel;
		
		if(distanceFromPlayer<10)
		{
			distanceFromPlayer=10;
		}
		
		if(distanceFromPlayer>150)
		{
			distanceFromPlayer=150;
		}
	}

	//마우스 버튼 누르고 이동에 따라 위아래 피치각 계산
	private void calculatePitch()
	{
		if(Mouse.isButtonDown(1))
		{
			float pitchChange=Mouse.getDY()*0.1f;
			pitch-=pitchChange;
		}
	}
	
	//마우스 버튼 누르고 플레이어 주변각을 계산
	private void calculateAngleAroundPlayer()
	{
		if(Mouse.isButtonDown(1))
		{
			float angleChange=Mouse.getDX()*0.3f;
			angleAroundPlayer-=angleChange;
		}
	}

	//플레이어로부터 수평 거리를 구함
	private float calculateHorizontalDistance()
	{
		return distanceFromPlayer*(float)Math.cos(Math.toRadians(pitch));
	}
	
	//플레이어로부터 수직 거리를 구함.
	private float calculateVerticalDistance()
	{
		return distanceFromPlayer*(float)Math.sin(Math.toRadians(pitch));
	}

	private void calculateCameraPosition(float hDistance, float vDistance)
	{
		
		//apply x offset, z offset
		float theta=player.getRotY()+angleAroundPlayer;
		float offsetX=hDistance*(float)Math.sin(Math.toRadians(theta));
		float offsetZ=hDistance*(float)Math.cos(Math.toRadians(theta));
		
		//calculate camera position
		position.x=player.getPosition().x-offsetX;
		position.z=player.getPosition().z-offsetZ;
		position.y=player.getPosition().y+vDistance;
	}
}
