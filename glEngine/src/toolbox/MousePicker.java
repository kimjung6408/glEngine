package toolbox;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;

public class MousePicker {
	private Vector3f currentRay;
	private Matrix4f projectionMatrix;
	private Matrix4f viewMatrix;
	private Camera cam;
	
	public MousePicker(Camera cam, Matrix4f projectionMatrix)
	{
		this.cam=cam;
		this.projectionMatrix=projectionMatrix;
		this.viewMatrix=Maths.createViewMatrix(cam);
		
	}
	
	public Vector3f getCurrentRay()
	{
		return currentRay;
	}
	
	public void update()
	{
		viewMatrix=Maths.createViewMatrix(cam);
		currentRay=calculateRay();
	}
	
	private Vector3f calculateRay()
	{
		float mouseX=Mouse.getX();
		float mouseY=Mouse.getY();
		
		//convert to normalized device coordinates
		Vector2f normalizedDeviceCoords=getNormalizedDeviceCoords(mouseX, mouseY);
		
		//convert to homogeneous clipping space
		Vector4f clipCoords=new Vector4f(normalizedDeviceCoords.x, normalizedDeviceCoords.y, -1f, 1f);
	
		//convert to camera space
		Vector4f cameraSpaceCoords=toCameraSpace(clipCoords);
		
		//convert to world space
		Vector3f worldRay=toWorldCoords(cameraSpaceCoords);
		
		return worldRay;
	}
	
	private Vector2f getNormalizedDeviceCoords(float mouseX, float mouseY)
	{
		float x=(2f*mouseX)/(float)Display.getWidth()-1;
		float y=(2f*mouseY)/(float)Display.getHeight()-1;
		return new Vector2f(x, y);
	}
	
	private Vector4f toCameraSpace(Vector4f clipCoords)
	{
		Matrix4f inverseProjectionMatrix=Matrix4f.invert(projectionMatrix, null);
		
		Vector4f eyeCoords=Matrix4f.transform(inverseProjectionMatrix, clipCoords, null);
		
		return new Vector4f(eyeCoords.x, eyeCoords.y, -1f,0f);
	}
	
	private Vector3f toWorldCoords(Vector4f cameraSpaceCoords)
	{
		Matrix4f inverseViewMatrix=Matrix4f.invert(viewMatrix, null);
		Vector4f worldRay=Matrix4f.transform(inverseViewMatrix, cameraSpaceCoords, null);
		
		Vector3f mouseRay=new Vector3f(worldRay.x,worldRay.y, worldRay.z);
		mouseRay.normalise();
		
		return mouseRay;		
	}
}
