package au.com.f1n.spaceinator.game;

import au.com.f1n.spaceinator.GBSoundManager;
import au.com.f1n.spaceinator.Util;

public class CircleBoundry extends Boundry {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private float boundryRadius;
	private float boundryRadius2;

	public CircleBoundry(float boundryRadius) {
		this.boundryRadius = boundryRadius;
		this.boundryRadius2 = boundryRadius * boundryRadius;
	}

	@Override
	public void collideTest(PSpaceShip pSpaceShip, GBSoundManager soundManager) {
		float x = pSpaceShip.x;
		float y = pSpaceShip.y;

		float d = x * x + y * y;
		if (d > boundryRadius2) {
			d = Util.invSqrt(d);
			pSpaceShip.dx *= boundryDecel;
			pSpaceShip.dy *= boundryDecel;
			pSpaceShip.dx -= x * d * BOUNDRY_ACCEL;
			pSpaceShip.dy -= y * d * BOUNDRY_ACCEL;
			soundManager.bounce(1 - x * d, x * d);
		}
	}

	public float getBoundryRadius() {
		return boundryRadius;
	}

	@Override
	protected float[][] make2DPoints() {
		int n = 80;
		float[] ret = new float[n * 2];
		for (int i = 0; i < n; i++) {
			double angle = Math.PI * 2 * i / (n - 1);
			ret[i * 2 + 0] = (float) Math.cos(angle) * boundryRadius;
			ret[i * 2 + 1] = (float) Math.sin(angle) * boundryRadius;
		}
		return new float[][] { ret };
	}
}
