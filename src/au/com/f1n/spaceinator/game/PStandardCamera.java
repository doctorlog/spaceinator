package au.com.f1n.spaceinator.game;

import au.com.f1n.spaceinator.Util;

public class PStandardCamera extends PCamera {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PSpaceShip following;
	public float x;
	public float y;
	public float dx;
	public float dy;
	private float rotX;
	private float rotZ;
	private long timeStart = -1;

	public PStandardCamera(World world) {
		super(world);

		following = world.getCentreSpaceShip();
	}

	/**
	 * This is really only a graphical effect
	 * 
	 * @param dTime
	 */
	public void timeStep(int dTime) {
		dx = dx * 0.9f + following.dx * .1f;
		dy = dy * 0.9f + following.dy * .1f;
	}

	public float getX() {
		return following.x + x;
	}

	public float getY() {
		return following.y + y;
	}

	public float flyin() {
		if (timeStart == -1)
			timeStart = world.lastTime;
		float amt = (float) (world.lastTime - timeStart) / 4000f;

		if (amt > .4) {
			float amt2 = Util.slowInOut((amt - .4f) / .6f);
			// rotX = amt2 * 70 - 70;
			rotZ = amt2 * 15 - 15;
			z = BASE_Z / 4 + amt2 * BASE_Z * 3 / 4;
		} else {
			// rotX = -70;
			rotZ = -15;
			z = BASE_Z / 4;
		}

		rotX = (float) (-70 * Math.cos(amt * 2) * Math.exp(-amt * 4)) - 0.53354f;

		if (amt >= 1) {
			// Ensure end is "perfect"
			z = BASE_Z;
			rotX = 0;
		}

		return amt;
	}

	@Override
	public float getRotX() {
		return rotX;
	}

	@Override
	public float getRotZ() {
		return rotZ;
	}

	@Override
	public void setY(float y) {
		this.y = y;
	}

	@Override
	public void resumeNow(long dTime) {
		timeStart += dTime;
	}
}
