package au.com.f1n.spaceinator.game;

import au.com.f1n.spaceinator.game.enemy.PEnemy;

public class PBlackHole extends PObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private float rotZ;
	private World world;
	float targetRadius;
	public static final int DRAW_ORDER = -2;

	public PBlackHole(World world, float radius) {
		// Black hole is always at the centre of the universe!
		super(0, 0, radius);
		this.world = world;
		drawOrder = DRAW_ORDER;
		targetRadius = radius;
	}

	@Override
	public boolean timeStep(int dTime, long timeS) {
		radius = .9f * radius + .1f * targetRadius;

		top = radius;
		bottom = -radius;
		return false;
	}

	@Override
	public void checkCollision(PObject other) {
		if (other instanceof PSpaceShip) {
			// damage ship when close
			((PSpaceShip) other).damage(5, world.lastTime, 0, 0, false);
		} else if (other instanceof PEnemy && isIn(other)) {
			PEnemy en = (PEnemy) other;
			// Black holes absorb enemies!
			en.scale *= .95f;
			en.radius *= .95f;
			en.dx *= .95f;
			en.dy *= .95f;
			if (en.scale < .1f) {
				targetRadius += en.life / 5;
				en.life = 0;
			}
		}
	}

	@Override
	public void damage(int dmg, long timeS, float dx, float dy, boolean laser) {
		// NA
	}

	public float getRotZ() {
		return rotZ += 1f;
	}
}
