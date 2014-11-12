package au.com.f1n.spaceinator.game;

import au.com.f1n.spaceinator.GBSoundManager;

public class RectBoundry extends Boundry {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final float RECT_RATIO = 1.61803398875f;
	private float boundryHeight;
	private float boundryWidth;

	public RectBoundry(float boundryHeight) {
		this.boundryHeight = boundryHeight;
		this.boundryWidth = boundryHeight * RECT_RATIO;
	}

	public RectBoundry(float boundryHeight, float boundryWidth) {
		this.boundryHeight = boundryHeight;
		this.boundryWidth = boundryWidth;
	}

	@Override
	public void collideTest(PSpaceShip pSpaceShip, GBSoundManager soundManager) {
		float x = pSpaceShip.x;
		float y = pSpaceShip.y;

		if (x > boundryWidth) {
			pSpaceShip.dx *= boundryDecel;
			pSpaceShip.dy *= boundryDecel;
			pSpaceShip.dx -= BOUNDRY_ACCEL;
			soundManager.bounce(0, 1);
		} else if (x < -boundryWidth) {
			pSpaceShip.dx *= boundryDecel;
			pSpaceShip.dy *= boundryDecel;
			pSpaceShip.dx += BOUNDRY_ACCEL;
			soundManager.bounce(1, 0);
		}

		if (y > boundryHeight) {
			pSpaceShip.dx *= boundryDecel;
			pSpaceShip.dy *= boundryDecel;
			pSpaceShip.dy -= BOUNDRY_ACCEL;
			soundManager.bounce(0.5f, 0.5f);
		} else if (y < -boundryHeight) {
			pSpaceShip.dx *= boundryDecel;
			pSpaceShip.dy *= boundryDecel;
			pSpaceShip.dy += BOUNDRY_ACCEL;
			soundManager.bounce(0.5f, 0.5f);
		}
	}

	public float getBoundryHeight() {
		return boundryHeight;
	}

	public float getBoundryWidth() {
		return boundryWidth;
	}

	@Override
	protected float[][] make2DPoints() {
		return new float[][] { { -boundryWidth, -boundryHeight, boundryWidth, -boundryHeight, boundryWidth, boundryHeight, -boundryWidth, boundryHeight,
				-boundryWidth, -boundryHeight } };
	}

}
