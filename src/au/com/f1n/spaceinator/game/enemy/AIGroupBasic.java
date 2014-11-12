package au.com.f1n.spaceinator.game.enemy;

import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.FArrayList;
import au.com.f1n.spaceinator.game.World;

public class AIGroupBasic implements PEnemyAIGroup {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final long AI_UPDATE = 100;
	private static final float FLANK_R = 2000;
	private static final float CLOSE_DIST2 = FLANK_R * FLANK_R;
	/**
	 * Distance for an alien to be on screen when fully zoomed out
	 */
	private static final float SHOOT_DIST_2 = 6000 * 6000;

	private FArrayList<PEnemy> enemies;
	private int alive = 0;
	private int[] enemyTypeCounts;
	private long nextUpdate = -1;

	private World world;

	private int aiMode;

	public AIGroupBasic(World world, int expectedSize) {
		enemies = new FArrayList<PEnemy>(expectedSize);
		this.world = world;

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

		return alive == 0;
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

		if (enemyTypeCounts[PEnemy.CLASS_SCOUT] + enemyTypeCounts[PEnemy.CLASS_BOMB] + enemyTypeCounts[PEnemy.CLASS_TANK] + enemyTypeCounts[PEnemy.CLASS_DART]
				+ enemyTypeCounts[PEnemy.CLASS_DART1] + enemyTypeCounts[PEnemy.CLASS_DART2] <= (enemyTypeCounts[PEnemy.CLASS_SHOOTER] + enemyTypeCounts[PEnemy.CLASS_FIGHTER] / 4))
			// Not enough scouts to warrant a flank
			aiMode = 4;

		if (aiMode == 0) {
			// Get "close"
			for (int i = 0; i < enemies.size; i++) {
				// We dont want them all to bunch up, so add random movement
				// sometimes
				PEnemy cur = (PEnemy) en[i];
				cur.targetDest = true;
				cur.setTargetDest(ssX, ssY, 0);
				cur.targetClose = false;
			}
			aiMode++;
		} else if (aiMode == 1) {
			// Update destination
			for (int i = 0; i < enemies.size; i++) {
				PEnemy cur = (PEnemy) en[i];
				cur.targetDest = true;
				cur.setTargetDest(ssX, ssY, 0);
				cur.targetClose = false;
			}
			// Are we there yet?
			for (int i = 0; i < enemies.size; i++) {
				PEnemy cur = (PEnemy) en[i];
				if (cur.targetDist2 < CLOSE_DIST2 * 2) {
					aiMode++;
					break;
				}
			}
		} else if (aiMode == 2) {
			// Flank
			for (int i = 0; i < enemies.size; i++) {
				PEnemy cur = (PEnemy) en[i];
				// Flank
				dx = cur.x - ssX;
				dy = cur.y - ssY;
				double theta = Math.atan2(dy, dx) + Util.randFloat() - .5;
				float r = (float) (FLANK_R * (0.8 + 0.4 * Util.randFloat()));
				if (cur.enemyClass == PEnemy.CLASS_SHOOTER || cur.enemyClass == PEnemy.CLASS_FIGHTER)
					// Shooters stay back
					r *= 2;
				else if (cur.enemyClass == PEnemy.CLASS_SCOUT)
					// Scouts go wide
					theta += (Util.randFloat() > .5 ? -1 : 1);
				else if (cur.enemyClass == PEnemy.CLASS_TANK)
					// Tanks stay back (but are probably still on their way)
					r *= 2;
				else if (cur.enemyClass == PEnemy.CLASS_BOMB)
					// Bombs always charge
					r = 0;
				else if (cur.enemyClass == PEnemy.CLASS_DART || cur.enemyClass == PEnemy.CLASS_DART1 || cur.enemyClass == PEnemy.CLASS_DART2)
					// Darts spin up!
					((PEnemyDart) cur).dart();

				cur.setTargetDest(ssX + (float) Math.cos(theta) * r, ssY + (float) Math.sin(theta) * r, 0);
			}
			aiMode++;
		} else if (aiMode == 3) {
			// Flanking
			int closeCount = 0;
			for (int i = 0; i < enemies.size; i++) {
				PEnemy cur = (PEnemy) en[i];
				if (cur.targetClose) {
					closeCount++;
					// Are we close yet?
					if (closeCount >= enemies.size * .5f) {
						aiMode++;
						break;
					}
				}

				// When close, tangent accel
				if (cur.targetClose && cur.targetDest) {
					cur.targetDest = false;
					if (Util.randFloat() > .5) {
						dx = -cur.getTargetDestY();
						dy = cur.getTargetDestX();
					} else {
						dx = cur.getTargetDestY();
						dy = -cur.getTargetDestX();
					}
					float invD = Util.invSqrt(dx * dx + dy * dy);
					cur.setAccel(dx * invD * PEnemy.ACCEL_SLOW, dy * invD * PEnemy.ACCEL_SLOW);
				}
			}
		} else if (aiMode == 4) {
			// Charge!
			for (int i = 0; i < enemies.size; i++) {
				PEnemy cur = (PEnemy) en[i];
				if (cur.enemyClass == PEnemy.CLASS_SCOUT || cur.enemyClass == PEnemy.CLASS_TANK || cur.enemyClass == PEnemy.CLASS_BOMB) {
					cur.targetDest = true;
					cur.setTargetDest(ssX, ssY, 0);
					cur.targetClose = false;
				} else if (cur.enemyClass == PEnemy.CLASS_DART) {
					// Darts spin up!
					((PEnemyDart) cur).dart();
				} else {
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

		// Make the aliens repel each other (this only works if they are in the
		// same AI group)
		for (int i = 0; i < enemies.size; i++) {
			PEnemy curI = (PEnemy) en[i];
			for (int j = 0; j < enemies.size; j++) {
				PEnemy curJ = (PEnemy) en[j];
				float dX = curI.x - curJ.x;
				float dY = curI.y - curJ.y;
				float dR = (curI.radius + curJ.radius) * 1.2f;
				float d2 = dX * dX + dY * dY;
				if (d2 < dR * dR) {
					float dInv = Util.invSqrt(d2);
					// enemies are close
					curI.dx += dX * dInv * .1f;
					curI.dy += dY * dInv * .1f;
					curJ.dx -= dX * dInv * .1f;
					curJ.dy -= dY * dInv * .1f;
				}
			}
		}
	}
}
