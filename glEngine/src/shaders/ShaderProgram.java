package shaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public abstract class ShaderProgram {

	private int programID;
	private int vertexShaderID;
	private int fragmentShaderID;
	
	private static FloatBuffer matrixBuffer=BufferUtils.createFloatBuffer(16);
	
	public ShaderProgram(String vertexFile, String fragmentFile)
	{
		vertexShaderID=loadShader(vertexFile, GL20.GL_VERTEX_SHADER);
		fragmentShaderID=loadShader(fragmentFile, GL20.GL_FRAGMENT_SHADER);
		
		//쉐이더 프로그램을 생성한다. (즉 , prgrammable stage를 조작할 수 있는 프로그램을 생성)
		programID=GL20.glCreateProgram();
		GL20.glAttachShader(programID, vertexShaderID);
		GL20.glAttachShader(programID, fragmentShaderID);
		
		//shader code의 attribute와 메모리상의 데이터를 연결한다.
		bindAttributes();
		
		GL20.glLinkProgram(programID);
		GL20.glValidateProgram(programID);
		
		getAllUniformLocations();
	}
	
	protected void bindFragOutput(int attachment, String variableName)
	{
		GL30.glBindFragDataLocation(programID, attachment, variableName);
	}
	
	protected int getUniformLocation(String uniformName)
	{
		return GL20.glGetUniformLocation(programID, uniformName);
	}
	
	
	
	protected abstract void getAllUniformLocations();
	protected abstract void bindAttributes();
	
	public void startShader(){
		GL20.glUseProgram(programID);
	}
	
	public void stopShader()
	{
		GL20.glUseProgram(0);		
	}
	
	public void cleanUp()
	{
		stopShader();
		
		GL20.glDetachShader(programID, vertexShaderID);
		GL20.glDetachShader(programID, fragmentShaderID);
		GL20.glDeleteShader(vertexShaderID);
		GL20.glDeleteShader(fragmentShaderID);
		
		GL20.glDeleteProgram(programID);
		
	}
	
	protected void bindAttribute(int attribute, String variableName)
	{
		GL20.glBindAttribLocation(programID, attribute, variableName);
	}
	
	private static int loadShader(String file, int type)
	{
		
		//문자열 변경 작업이 많을 경우 StringBuffer 혹은 StringBuilder를 사용한다.
		//내부버퍼에 문자열을 저장해두고 그 안에서 추가, 수정, 삭제작업을 하는 오브젝트.
		StringBuilder shaderSource=new StringBuilder();
		
		try {
			BufferedReader reader=new BufferedReader(new FileReader(file));
		
			String line;
			
			while((line=reader.readLine())!=null)
			{
				shaderSource.append(line).append("\n");
				
			}
			
			reader.close();
		} catch (IOException e) {
			// TODO: handle exception
			System.err.println("Could not read shader file!");
			e.printStackTrace();
			System.exit(-1);
		}
		
		int shaderID=GL20.glCreateShader(type);
		GL20.glShaderSource(shaderID, shaderSource);
		GL20.glCompileShader(shaderID);
		
		//shader가 컴파일이 되었는지 검사한다.
		if(GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS)==GL11.GL_FALSE)
		{
			System.out.println(GL20.glGetShaderInfoLog(shaderID, 500));
			System.err.println("Could not compile shader.");
			System.exit(-1);
			
		}
		
		return shaderID;
	}
	
	
	//아래는 uniform value들을 로딩해주는 함수들.
	protected void loadFloat(int location, float value)
	{
		GL20.glUniform1f(location, value);
	}
	
	protected void loadInt(int location, int value)
	{
		GL20.glUniform1i(location, value);
	}
	
	protected void loadVector3(int location, Vector3f vector)
	{
		GL20.glUniform3f(location, vector.x, vector.y, vector.z);
	}
	
	protected void loadVector2(int location, Vector2f vector)
	{
		GL20.glUniform2f(location, vector.x, vector.y);
	}

	protected void loadVector4(int location, Vector4f vector)
	{
		GL20.glUniform4f(location, vector.x, vector.y, vector.z, vector.w);
	}
	
	protected void loadBoolean(int location, boolean value)
	{
		float toLoad=0;
		
		if(value)
		{
			toLoad=1;
		}
		
		GL20.glUniform1f(location, toLoad);
	}
	
	protected void loadMatrix(int location ,Matrix4f matrix)
	{
		matrix.store(matrixBuffer);
		matrixBuffer.flip();
		
		GL20.glUniformMatrix4(location, false, matrixBuffer);
	}
}
