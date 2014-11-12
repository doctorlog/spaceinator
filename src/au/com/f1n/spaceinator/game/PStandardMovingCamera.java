package au.com.f1n.spaceinator.game;

import au.com.f1n.spaceinator.Util;

public class PStandardMovingCamera extends PCamera {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PSpaceShip following;
	public float x;
	public float y;
	private float rotX;
	private float rotZ;
	private long timeStart = -1;
	private boolean finishedFlyIn;

	public PStandardMovingCamera(World world, float startx) {
		super(world);
		x = startx;

		following = world.getCentreSpaceShip();
	}

	/**
	 * This is really only a graphical effect
	 * 
	 * @param dTime
	 */
	public void timeStep(int dTime) {
		if (finishedFlyIn) {
			// slow movement
			x += dTime;
			if (following.x > x)
				x = following.x;

			if (following.x < x - 2000) {
				following.dx += .1f;
			}
		}
	}

	public float getX() {
		if (!finishedFlyIn)
			return following.x;
		return x;
	}

	public float getY() {
		if (!finishedFlyIn)
			return following.y + y;
		return y;
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
			finishedFlyIn = true;
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
