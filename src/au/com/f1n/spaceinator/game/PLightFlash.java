package au.com.f1n.spaceinator.game;

public class PLightFlash extends PWeaponParticle {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private float amt;

	public PLightFlash(float x, float y, float radius) {
		super(x, y, radius);
	}

	@Override
	public int getColour() {
		return 4;
	}

	@Override
	public void remove() {
		// NA
	}

	@Override
	public float getScale() {
		return radius;
	}

	@Override
	public boolean timeStep(int dTime, long timeS) {
		// NA
		return false;
	}

	@Override
	public void checkCollision(PObject other) {
		// NA
	}

	@Override
	public void damage(int dmg, long timeS, float dx, float dy, boolean laser) {
		// NA
	}

	public float getAmt() {
		return amt;
	}

	public void setAmt(float amt) {
		this.amt = amt;
	}
}
