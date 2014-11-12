package au.com.f1n.spaceinator.game;

public class PPlanet extends PObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int DRAW_ORDER = -1;
	protected int textureID;
	protected float curRotate;
	protected float tilt;
	protected byte[] haloInner;
	protected byte[] haloOuter;
	protected float haloProportion;
	public float mass;
	private float thetaRate;
	private float theta;
	float orbitR;
	private float[] sunCol;
	private boolean rotate = false;
	private boolean drawMiniHalos = true;

	/**
	 * 
	 * @param world
	 * @param textureID
	 * @param mass
	 * @param thetaStart
	 * @param r
	 * @param orbitR
	 */
	public PPlanet(int textureID, float mass, float thetaRate, float r, float orbitR, byte[] haloInner, byte[] haloOuter, float haloProportion, boolean rotate) {
		super((float) Math.cos(thetaRate * 100) * orbitR, (float) Math.sin(thetaRate * 100) * orbitR, r);
		this.theta = thetaRate * 100;
		this.mass = mass;
		this.haloInner = haloInner;
		this.haloOuter = haloOuter;
		this.haloProportion = haloProportion;
		this.textureID = textureID;
		this.thetaRate = thetaRate;
		this.orbitR = orbitR;
		this.rotate = rotate;
		drawOrder = DRAW_ORDER;

		top = y + radius;
		bottom = y - radius;
	}

	public PPlanet(int textureID, float mass, float thetaRate, float r, float orbitR, byte[] haloInner, byte[] haloOuter, float haloProportion, float[] sunCol,
			boolean rotate) {
		this(textureID, mass, thetaRate, r, orbitR, haloInner, haloOuter, haloProportion, rotate);
		this.sunCol = sunCol;
	}

	@Override
	public boolean timeStep(int dTime, long timeS) {
		if (rotate) {
			theta += thetaRate * dTime / 1000f;
			x = (float) Math.cos(theta) * orbitR;
			y = (float) Math.sin(theta) * orbitR;

			top = y + radius;
			bottom = y - radius;
		}
		return false;
	}

	public int getTextureID() {
		return textureID;
	}

	public float getCurRotate() {
		return curRotate;
	}

	public byte[] getHaloInner() {
		return haloInner;
	}

	public byte[] getHaloOuter() {
		return haloOuter;
	}

	public float getHaloProportion() {
		return haloProportion;
	}

	@Override
	public void damage(int dmg, long timeS, float dx, float dy, boolean laser) {
		// NA!
	}

	@Override
	public void checkCollision(PObject other) {
		// Planets dont hit things, things hit planets
		if (!(other instanceof PPlanet))
			other.checkCollision(this);
	}

	public float[] getSunCol() {
		return sunCol;
	}

	public boolean drawMiniHalos() {
		return drawMiniHalos;
	}

	public void setDrawMiniHalos(boolean drawMiniHalos) {
		this.drawMiniHalos = drawMiniHalos;
	}
}
