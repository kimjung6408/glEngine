package textures;

//material과 같음.
public class ModelTexture {
	private int textureID;
	private int normalMapID;
	private int specularMapID;
	
	//정반사 계수. 높을 수록 정반사 비율이 높고, 낮을수록 난반사 비율이 높음.
	private float shineDamper=1;
	
	//반사광의 세기
	private float reflectivity=0;
	
	//culling mask 적용 여부
	private boolean hasTransparency=false;
	
	//상단 라이팅만 적용할지의 여부.
	private boolean useFakeLighting=false;
	
	//specular map 적용 여부
	private boolean hasSpecularMap=false;
	
	//texture atlas를 위한 row 개수
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
