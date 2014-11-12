package au.com.f1n.spaceinator.game.enemy;

import java.util.Random;

import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.FArrayList;
import au.com.f1n.spaceinator.game.World;

public class AIWormEnemy implements PEnemyAIGroup {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int AI_UPDATE = 100;
	public static final int N_SEGMENTS = 20;
	private FArrayList<PEnemy> segments;
	private long nextUpdate = -1;

	private World world;
	private Random rand = new Random();

	private int splits;

	public AIWormEnemy(World world, float x, float y) {
		segments = new FArrayList<PEnemy>(10);
		this.world = world;

		PWormEnemy head = null;
		for (int i = 0; i < N_SEGMENTS; i++) {
			head = new PWormEnemy(world, x + rand.nextFloat() * 3000, y + rand.nextFloat() * 3000, head);
			world.addObject(head);
			add(head);
		}
	}

	@Override
	public boolean add(PEnemy enemy) {
		// The first enemy that is added MUST be the worm
		enemy.ai = this;
		return segments.add(enemy);
	}

	public boolean timeStep(int dTime, long timeS) {
		if (nextUpdate == -1)
			// Dont do anything while they warp in
			nextUpdate = timeS + (long) (PEnemy.WARP_TIME);

		if (timeS > nextUpdate && !world.isDead()) {
			// Update what this group is doing
			decideAI(timeS);
			nextUpdate = timeS + AI_UPDATE + rand.nextInt(AI_UPDATE);
		}

		return segments.size == 0;
	}

	@Override
	public void killed(PEnemy enemy) {
		splits++;

		segments.remove(enemy);

		AIGroupBasic newAI = new AIGroupBasic(world, segments.size / 2);
		for (int i = 0; i < segments.size / 2; i++) {
			PEnemy newEnemy = new PEnemy(world, enemy.x + rand.nextFloat(), enemy.y + rand.nextFloat(), 0, 0,
					rand.nextBoolean() ? rand.nextBoolean() ? PEnemy.CLASS_SCOUT : PEnemy.CLASS_BOMB : rand.nextBoolean() ? PEnemy.CLASS_SHOOTER
							: PEnemy.CLASS_FIGHTER);
			newEnemy.warpZ = 1;
			world.addObject(newEnemy);
			newAI.add(newEnemy);
		}
		world.enemyAIGroups.add(newAI);
	}

	private void decideAI(long timeS) {
		Object[] en = segments.array;
		// int count;
		// int curGroupIndex = 0;
		for (int i = 0; i < segments.size; i++) {
			PWormEnemy w = (PWormEnemy) en[i];

			if (w.isHead()) {
				float ssdx = world.getCentreSpaceShip().x - w.x;
				float ssdy = world.getCentreSpaceShip().y - w.y;
				float dist = ssdx * ssdx + ssdy * ssdy;
				boolean close = dist < 3000 * 3000;

				if (w.hovering) {
					ssdx = w.hoverX - w.x;
					ssdy = w.hoverY - w.y;
					dist = ssdx * ssdx + ssdy * ssdy;
				}

				float angle;
				if (dist < 1000 * 1000)
					// Go away from spaceship
					angle = (float) Math.atan2(ssdy, ssdx) - Util.PI;
				else if (dist > 3000 * 3000)
					// go toward spaceship
					angle = (float) Math.atan2(ssdy, ssdx);
				else {
					// tangent
					angle = (float) Math.atan2(-ssdx, ssdy);
					if (close && rand.nextFloat() > .8f)
						world.addObject(new PEnemyMissile(world, w.x, w.y, 0, 0, rand.nextFloat() * 2 * Util.PI, world.lastTime));
				}

				w.setAccel((float) Math.cos(angle) * w.accelFast * 7, (float) Math.sin(angle) * w.accelFast * 7);

				// if (w.tail == null) {
				// w.life = 0;
				// world.graphicEffect(new PParticleExplosion(w.x, w.y, 0, 0, 0,
				// Math.PI * 2, 50, world.lastTime, 3000, 2000, w.radius, .99f,
				// PParticleExplosion.COLOUR_HOT));
				// }
				// } else if (w.isTail()) {
				// count = 0;
				// PWormEnemy cur = w;
				// while (cur != null) {
				// count++;
				// cur.groupIndex = curGroupIndex;
				// cur = cur.head;
				// }
				//
				// if (count <= 2) {
				// w.life = 0;
				// w.head.life = 0;
				// float dx = w.x - w.head.x;
				// float dy = w.y - w.head.y;
				// float dInv = Util.invSqrt(dx * dx + dy * dy);
				// dx *= dInv;
				// dy *= dInv;
				// world.graphicEffect(new PParticleExplosion(w.x, w.y, dx, dy, 0,
				// Math.PI * 2, 50, world.lastTime, 3000, 2000, w.radius, .99f,
				// PParticleExplosion.COLOUR_HOT));
				// world.graphicEffect(new PParticleExplosion(w.head.x, w.head.y,
				// -dx, -dy, 0, Math.PI * 2, 50, world.lastTime, 3000, 2000,
				// w.radius, .99f,
				// PParticleExplosion.COLOUR_HOT));
				// }
				//
				// curGroupIndex++;
			}
		}

		// Make the aliens repel each other (this only works if they are in the
		// same AI group)
		for (int i = 0; i < segments.size; i++) {
			PWormEnemy curI = (PWormEnemy) en[i];
			for (int j = 0; j < segments.size; j++) {
				PWormEnemy curJ = (PWormEnemy) en[j];

				if (curI.groupIndex != curJ.groupIndex) {
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

	public int getSplits() {
		return splits;
	}

	public boolean isEmpty() {
		return segments.isEmpty();
	}

	public int getSegmentCount() {
		return segments.size;
	}
}
