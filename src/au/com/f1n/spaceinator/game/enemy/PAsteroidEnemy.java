package au.com.f1n.spaceinator.game.enemy;

import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.PParticleExplosion;
import au.com.f1n.spaceinator.game.World;

public class PAsteroidEnemy extends PEnemy {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int level;
	protected float rotSpeed1;
	protected float rotSpeed2;

	public PAsteroidEnemy(World world, float x, float y, float dx, float dy, int level, boolean noWarp) {
		super(world, x, y, dx, dy, level == 6 ? CLASS_ASTEROID_BIG : (level == 1 ? CLASS_ASTEROID_SMALL : Util.randFloat() > .5 ? CLASS_ASTEROID_ALT
				: CLASS_ASTEROID));
		this.level = level;

		rotSpeed1 = (float) (Util.randFloat() * 0.004) - .002f;
		rotSpeed2 = (float) (Util.randFloat() * 0.004) - .002f;

		if (level == 6) {
			radius = 800;
			life = level * LIFE * 6;
		} else {
			radius = level * 47;
			life = level * LIFE;
		}

		drag = 0.99f;

		this.noWarp = noWarp;
		if (noWarp)
			warpZ = 1;

		scale = level * 5;
	}

	/**
	 * Asteroids magically aren't effected by gravity
	 * 
	 * @return
	 */
	@Override
	public boolean timeStep(int dTime, long timeS) {
		if (startTime == -1)
			startTime = timeS;

		if (!noWarp && warping(timeS))
			return false;

		if (life <= 0) {
			if (level == 6) {
				int n = 11;

				for (int i = 0; i < n; i++) {
					double angle = Math.PI * 2 * i / n;

					float dx = (float) Math.cos(angle + facingAngle);
					float dy = (float) Math.sin(angle + facingAngle);
					float power = (float) Util.randFloat() * level / 2;
					// recursive asteroids!
					int l = (int) (Util.randFloat() * 4 + 2);

					world.addObject(new PAsteroidEnemy(world, x + dx * radius, y + dy * radius, dx * power, dy * power, l, true));
				}
				// Extra special explsion
				world.graphicEffect(new PParticleExplosion(x, y, 0, 0, 2000, timeS, 3000, 2000, radius, .5f, PParticleExplosion.COLOUR_BROWN));

			} else if (level > 1) {
				int n = 3;

				for (int i = 0; i < n; i++) {
					double angle = Math.PI * 2 * i / n;

					float dx = (float) Math.cos(angle + facingAngle);
					float dy = (float) Math.sin(angle + facingAngle);
					float power = (float) Util.randFloat() * level / 2;
					// recursive asteroids!
					world.addObject(new PAsteroidEnemy(world, x + dx * (level * 100), y + dy * (level * 100), dx * power, dy * power, level - 1, true));
				}
			}

			return true;
		}

		facingAngle += (float) dTime * rotSpeed1;
		yRot += (float) dTime * rotSpeed2;

		x += dTime * dx;
		y += dTime * dy;

		dx *= drag;
		dy *= drag;

		top = y + radius;
		bottom = y - radius;

		return false;
	}

	/**
	 * Take off the life from this asteroid
	 * 
	 * Only difference with PEnemy is the different colour
	 * 
	 * @param damage
	 * @return the amount of "leftover" damage
	 */
	@Override
	public void damage(int damage, long timeS, float fromdx, float fromdy, boolean laser) {
		life -= damage;
		if (life <= 0)
			world.graphicEffect(x, y, fromdx, fromdy, 8, timeS, 2000, radius, .97f, PParticleExplosion.COLOUR_BROWN);
	}
}
