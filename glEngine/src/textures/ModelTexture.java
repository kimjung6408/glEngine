package textures;

//material�� ����.
public class ModelTexture {
	private int textureID;
	private int normalMapID;
	private int specularMapID;
	
	//���ݻ� ���. ���� ���� ���ݻ� ������ ����, �������� ���ݻ� ������ ����.
	private float shineDamper=1;
	
	//�ݻ籤�� ����
	private float reflectivity=0;
	
	//culling mask ���� ����
	private boolean hasTransparency=false;
	
	//��� �����ø� ���������� ����.
	private boolean useFakeLighting=false;
	
	//specular map ���� ����
	private boolean hasSpecularMap=false;
	
	//texture atlas�� ���� row ����
	int numOfRows=1;
	
	public ModelTexture(int id)
	{
		this.textureID=id;
	}
	
	public void setSpecularMap(int specularMapID)
	{
		this.specularMapID=specularMapID;
		hasSpecularMap=true;
	}
	
	public boolean hasSpecularMap()
	{
		return hasSpecularMap;
	}
	
	public int getSpecularMap()
	{
		return specularMapID;
	}
	
	public int getID()
	{
		return this.textureID;
	}

	public float getShineDamper() {
		return shineDamper;
	}

	public void setShineDamper(float shineDamper) {
		this.shineDamper = shineDamper;
	}

	public float getReflectivity() {
		return reflectivity;
	}

	public void setReflectivity(float reflectivity) {
		this.reflectivity = reflectivity;
	}
	
	public boolean isHasTransparency()
	{
		return hasTransparency;
	}
	
	public void setHasTransparency(boolean hasTransparency)
	{
		this.hasTransparency=hasTransparency;
	}

	public boolean isUseFakeLighting() {
		return useFakeLighting;
	}

	public void setUseFakeLighting(boolean useFakeLighting) {
		this.useFakeLighting = useFakeLighting;
	}

	public int getNumOfRows() {
		return numOfRows;
	}

	public void setNumOfRows(int numOfRows) {
		this.numOfRows = numOfRows;
	}
	
	
}
