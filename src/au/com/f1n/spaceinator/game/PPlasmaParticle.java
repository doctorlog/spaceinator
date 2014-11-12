package au.com.f1n.spaceinator.game;

import java.util.HashSet;

import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.enemy.PEnemy;

public class PPlasmaParticle extends PObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long lastTime;
	private long startTime = -1;
	private HashSet<PObject> attacked;
	private float amt;
	private World world;
	private boolean friendly;
	public static final int DRAW_ORDER = PEnemy.CLASS_COUNT + 1;

	public PPlasmaParticle(World world, float x, float y, boolean friendly) {
		super(x, y, 0.001f);
		this.world = world;
		this.friendly = friendly;
		attacked = new HashSet<PObject>();
		drawOrder = DRAW_ORDER;
	}

	/**
	 * Expand at a linear rate
	 */
	@Override
	public boolean timeStep(int dTime, long timeS) {
		if (startTime == -1)
			startTime = timeS;

		amt = 1f - (float) (timeS - startTime) / 1000f;

		if (friendly)
			radius = Util.slowOut((float) (timeS - startTime) / 1000f) * 3000;
		else
			radius = Util.slowOut((float) (timeS - startTime) / 1000f) * 1300;
		lastTime = timeS;

		top = y + radius;
		bottom = y - radius;
		return timeS - startTime > 1000;
	}

	@Override
	public void checkCollision(PObject other) {
		if (other instanceof PEnemy && !attacked.contains(other) && isIn(other)) {
			PEnemy pEnemy = (PEnemy) other;
			float dx = other.x - x;
			float dy = other.y - y;
			float dInv = Util.invSqrt(dx * dx + dy * dy);
			pEnemy.damage(friendly ? 50 : 10, lastTime, dx * dInv * 8, dy * dInv * 8, true);
			if (pEnemy.life > 0) {
				attacked.add(pEnemy);
				pEnemy.dx += dx * dInv;
				pEnemy.dy += dy * dInv;
			} else
				world.goodKill((PEnemy) other);
		} else if (!friendly && other instanceof PSpaceShip && !attacked.contains(other)) {
			PSpaceShip pSS = (PSpaceShip) other;
			float dx = other.x - x;
			float dy = other.y - y;
			float dInv = Util.invSqrt(dx * dx + dy * dy);
			pSS.damage(10, lastTime, dx * dInv * 3, dy * dInv * 3, true);
			attacked.add(pSS);
			pSS.dx += dx * dInv;
			pSS.dy += dy * dInv;
		}
	}

	@Override
	public void damage(int dmg, long timeS, float dx, float dy, boolean laser) {
		// NA
	}

	public float getAmt() {
		return amt;
	}

	public boolean isFriendly() {
		return friendly;
	}
}
