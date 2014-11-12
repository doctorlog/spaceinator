package au.com.f1n.spaceinator.game.enemy;

import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.CircleBoundry;
import au.com.f1n.spaceinator.game.FArrayList;
import au.com.f1n.spaceinator.game.PParticleExplosion;
import au.com.f1n.spaceinator.game.PPlanet;
import au.com.f1n.spaceinator.game.RectBoundry;
import au.com.f1n.spaceinator.game.World;

public class AIGroupSpike implements PEnemyAIGroup {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final long AI_UPDATE = 100;
	private static final float FLANK_R = 3000;
	private static final float CLOSE_DIST2 = FLANK_R * FLANK_R;
	private static final int N_GUARD = 50;

	private FArrayList<PEnemy> enemies;
	private long nextUpdate = -1;

	private World world;
	private PEnemy spike;
	private int aiMode;
	private float bestx;
	private float besty;
	private float lifeMultiply = 1;
	private long lastAdd;
	private float radius;
	private float radius2Max;

	public AIGroupSpike(World world, int expectedSize) {
		enemies = new FArrayList<PEnemy>(expectedSize);
		this.world = world;

		if (world.getBoundry() instanceof RectBoundry) {
			radius = ((RectBoundry) world.getBoundry()).getBoundryWidth();
		} else if (world.getBoundry() instanceof CircleBoundry) {
			radius = ((CircleBoundry) world.getBoundry()).getBoundryRadius();
		}
		radius2Max = radius * radius * 1.2f * 1.2f;
	}

	@Override
	public boolean add(PEnemy enemy) {
		// The first enemy that is added MUST be the spike
		if (spike == null) {
			spike = enemy;
		}
		enemy.ai = this;
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

		if (spike.life <= 0) {
			world.graphicEffect(new PParticleExplosion(spike.x, spike.y, 0, 0, 500, timeS, 5000, 2000, spike.radius, .8f, PParticleExplosion.COLOUR_HOT));
			return true;
		}

		return false;
	}

	@Override
	public void killed(PEnemy enemy) {
		enemies.remove(enemy);
	}

	private void decideAI(long timeS) {
		Object[] en = enemies.array;

		float sX = spike.x;
		float sY = spike.y;
		float ssX = world.getCentreSpaceShip().x;
		float ssY = world.getCentreSpaceShip().y;
		float dx, dy;

		dx = ssX - sX;
		dy = ssY - sY;

		if (enemies.size == 1 && spike.life > 0) {
			// Only spike left!?
			newGuard();
		}

		if (dx * dx + dy * dy <= CLOSE_DIST2 || enemies.size < N_GUARD * 3 / 4) {
			if (spike.life < 250)
				aiMode = 2;
			else
				aiMode = 1;
		} else {
			aiMode = 0;
		}

		if (aiMode == 0) {
			// cruise around
			float baseAngle = (float) Math.atan2(dy, dx);
			for (int i = 0; i < enemies.size; i++) {
				PEnemy cur = (PEnemy) en[i];
				if (cur == spike) {
					if (cur.targetClose || !cur.targetDest) {
						cur.targetDest = true;
						float destX = 0;
						float destY = 0;
						boolean bad = true;
						while (bad) {
							float r = (float) Util.randFloat() * 800;
							double t = Util.randFloat() * Math.PI * 2;
							destX = sX + r * (float) Math.cos(t);
							destY = sY + r * (float) Math.sin(t);

							// Dont go outside helio
							bad = destX * destX + destY * destY > radius2Max;

							// if (!bad)
							// // Spike can NOT go into a planet!
							// for (PPlanet p : world.planets) {
							// dx = p.x - destX;
							// dy = p.y - destY;
							//
							// if (dx * dx + dy * dy < (spike.radius + p.radius) *
							// (spike.radius + p.radius)) {
							// bad = true;
							// break;
							// }
							// }
						}
						// Found a good point for spike
						cur.setTargetDest(destX, destY, 0);
						cur.targetClose = false;
					}
				} else {
					if (cur.targetClose || !cur.targetDest) {
						// Defend spike
						float r = 800 + (float) Util.randFloat() * 200;
						float t = baseAngle - 1 + (float) Util.randFloat() * 2;
						cur.targetDest = true;
						cur.setTargetDest(sX + r * (float) Math.cos(t), sY + r * (float) Math.sin(t), 0);
						cur.targetClose = false;
					}
				}
			}
		} else if (aiMode == 1) {
			// Dump the enemies into a normal basic AI group and teleport the spike
			spike.startTime = timeS;
			spike.warpZ = 0;
			spike.noWarp = false;
			// Red puff
			world.graphicEffect(new PParticleExplosion(sX, sY, 0, 0, 200, timeS, 3000, 3000, spike.radius, .8f, PParticleExplosion.COLOUR_RED));

			findGoodSpot();
			spike.x = bestx;
			spike.y = besty;

			// Teleport harms spike
			spike.life -= 200;
			newGuard();
		} else if (aiMode == 2) {
			// ship is close but spike cant teleport anymore
			if (spike.life > 0 && timeS - lastAdd > 5000) {
				newGuard();
				lastAdd = timeS;
			}
		}
	}

	private void newGuard() {
		// His guard stays behind
		if (enemies.size > 1) {
			AIGroupBasic newAIGroup = new AIGroupBasic(world, enemies.size - 1);
			for (int i = enemies.size - 1; i > 0; i--) {
				newAIGroup.add(enemies.remove(i));
			}
			world.enemyAIGroups.add(newAIGroup);
		}
		// he gets a new guard
		if (spike.life > 0)
			addEnemy();
	}

	/**
	 * Set tmpx and tmpy to a good random location for spike
	 */
	private void findGoodSpot() {
		float r, dx, dy;

		// Select a spot to warp to
		boolean badSpot = true;
		int tryLeft = 20;

		float tmpx = 0;
		float tmpy = 0;
		float bestScore = Float.MAX_VALUE;
		float curScore;

		while (badSpot && tryLeft-- > 0) {
			badSpot = false;
			// select any random location in the helio
			double t = Util.randFloat() * Math.PI * 2;
			r = (float) (radius * (Util.randFloat() * .7 + .3));
			tmpx = r * (float) Math.cos(t);
			tmpy = r * (float) Math.sin(t);

			// Check if new loc is close to himself
			dx = tmpx - spike.x;
			dy = tmpy - spike.y;
			if (dx * dx + dy * dy < CLOSE_DIST2 * 4)
				badSpot = true;

			if (!badSpot) {
				// Check if that new loc is near us
				dx = tmpx - world.getCentreSpaceShip().x;
				dy = tmpy - world.getCentreSpaceShip().y;
				if (!badSpot && dx * dx + dy * dy < CLOSE_DIST2 * 4)
					badSpot = true;
			}

			curScore = 0;

			if (!badSpot) {
				// check all planets
				for (PPlanet p : world.getPlanets()) {
					dx = tmpx - p.x;
					dy = tmpy - p.y;

					float d2 = dx * dx + dy * dy;
					curScore += d2;

					if (d2 < CLOSE_DIST2 * 4) {
						badSpot = true;
						break;
					}
				}
			}

			if (badSpot) {
				if (curScore < bestScore) {
					bestScore = curScore;
					bestx = tmpx;
					besty = tmpy;
				}
			}
		}

		if (!badSpot) {
			bestx = tmpx;
			besty = tmpy;
		}
	}

	private void addEnemy() {
		for (int i = 0; i < N_GUARD; i++) {
			PEnemy newEnemy = new PEnemy(world, spike.x + (float) Util.randFloat() * 800, spike.y + (float) Util.randFloat() * 800, 0, 0,
					Util.randFloat() > .5 ? PEnemy.CLASS_SCOUT : PEnemy.CLASS_SHOOTER);
			add(newEnemy);
			world.addObject(newEnemy);
			newEnemy.life *= lifeMultiply;
			if (newEnemy.getWeapon() != null)
				newEnemy.getWeapon().setBaseDamage((int) (3 * lifeMultiply));
		}

		lifeMultiply += .1f;
	}
}
