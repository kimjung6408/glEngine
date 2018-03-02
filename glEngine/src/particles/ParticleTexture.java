package particles;

public class ParticleTexture {
	private int textureID;
	private int numOfRows;
	private boolean additiveBlending=true;
	
	
	
	public ParticleTexture(int textureID, int numOfRows) {
		this.textureID = textureID;
		this.numOfRows = numOfRows;
	}
	
	
	public ParticleTexture(int textureID, int numOfRows, boolean additiveBlending) {
		super();
		this.textureID = textureID;
		this.numOfRows = numOfRows;
		this.additiveBlending = additiveBlending;
	}



	public int getTextureID() {
		return textureID;
	}
	public int getNumOfRows() {
		return numOfRows;
	}


	public boolean isAdditiveBlending() {
		return additiveBlending;
	}

}
