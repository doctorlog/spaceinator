package au.com.f1n.spaceinator.game.enemy;

import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.FArrayList;
import au.com.f1n.spaceinator.game.World;

public class AIGroupCharge implements PEnemyAIGroup {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final long AI_UPDATE = 100;
	/**
	 * Distance for an alien to be on screen when fully zoomed out
	 */
	private static final float SHOOT_DIST_2 = 6000 * 6000;

	private FArrayList<PEnemy> enemies;
	private int alive = 0;
	private int[] enemyTypeCounts;
	private long nextUpdate = -1;
	private World world;
	private boolean alwaysAlive;

	public AIGroupCharge(World world, int expectedSize, boolean alwaysAlive) {
		enemies = new FArrayList<PEnemy>(expectedSize);
		this.world = world;
		this.alwaysAlive = alwaysAlive;

		enemyTypeCounts = new int[PEnemy.AI_CLASS_COUNT];
	}

	@Override
	public boolean add(PEnemy enemy) {
		enemy.ai = this;
		alive++;
		enemyTypeCounts[enemy.enemyClass]++;
		return enemies.add(enemy);
	}

	public boolean timeStep(int dTime, long timeS) {
		if (nextUpdate == -1)
			// Dont do anything while they warp in
			nextUpdate = timeS + (long) (PEnemy.WARP_TIME);

		if (timeS > nextUpdate && !world.isDead()) {
			// Update what this group is doing
			decideAI(timeS);
			nextUpdate = timeS + AI_UPDATE;
		}

		return !alwaysAlive && alive == 0;
	}

	@Override
	public void killed(PEnemy enemy) {
		enemies.remove(enemy);
		enemyTypeCounts[enemy.enemyClass]--;
		alive--;
	}

	private void decideAI(long timeS) {
		Object[] en = enemies.array;

		float ssX = world.getCentreSpaceShip().x;
		float ssY = world.getCentreSpaceShip().y;
		float dx, dy;

		// Charge!
		for (int i = 0; i < enemies.size; i++) {
			PEnemy cur = (PEnemy) en[i];
			if (cur.enemyClass == PEnemy.CLASS_BOMB) {
				cur.targetDest = true;
				cur.setTargetDest(ssX, ssY, 0);
				cur.targetClose = false;
			} else if (cur.enemyClass == PEnemy.CLASS_SCOUT || cur.enemyClass == PEnemy.CLASS_TANK) {
				cur.targetDest = true;
				cur.setTargetDest(ssX, ssY, 0);
				cur.targetClose = false;
			} else {
				// some kind of shooter
				cur.targetDest = false;
				dx = ssX - cur.x;
				dy = ssY - cur.y;
				cur.shooting = dx * dx + dy * dy < SHOOT_DIST_2;
				if (cur.shooting) {
					// Fly toward, ignoring everything and shooting
					cur.shootAngle = (float) (Math.atan2(dy, dx) + Util.randFloat() * .2 - .1);
					float dinv = Util.invSqrt(dx * dx + dy * dy);
					cur.setAccel(dx * dinv * PEnemy.ACCEL_SLOW, dy * dinv * PEnemy.ACCEL_SLOW);
					cur.yRot += 0.5f;
				} else {
					// Target fly toward
					cur.targetDest = true;
					cur.setTargetDest(ssX, ssY, 0);
				}
			}
		}

	}
}
