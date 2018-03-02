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
	
	//���콺 ���� �̿��Ͽ� ����, �ܾƿ�
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

	//���콺 ��ư ������ �̵��� ���� ���Ʒ� ��ġ�� ���
	private void calculatePitch()
	{
		if(Mouse.isButtonDown(1))
		{
			float pitchChange=Mouse.getDY()*0.1f;
			pitch-=pitchChange;
		}
	}
	
	//���콺 ��ư ������ �÷��̾� �ֺ����� ���
	private void calculateAngleAroundPlayer()
	{
		if(Mouse.isButtonDown(1))
		{
			float angleChange=Mouse.getDX()*0.3f;
			angleAroundPlayer-=angleChange;
		}
	}

	//�÷��̾�κ��� ���� �Ÿ��� ����
	private float calculateHorizontalDistance()
	{
		return distanceFromPlayer*(float)Math.cos(Math.toRadians(pitch));
	}
	
	//�÷��̾�κ��� ���� �Ÿ��� ����.
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
