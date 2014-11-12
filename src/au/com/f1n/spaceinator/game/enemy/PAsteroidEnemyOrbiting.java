package au.com.f1n.spaceinator.game.enemy;

import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.PParticleExplosion;
import au.com.f1n.spaceinator.game.World;

public class PAsteroidEnemyOrbiting extends PAsteroidEnemy {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double theta;
	private double thetaSpeed;
	private float r;
	private float dxAdd;
	private float dyAdd;

	private float xAdd;
	private float yAdd;
	private boolean anticlockwise;

	public PAsteroidEnemyOrbiting(World world, float x, float y, float dxStart, float dyStart, int level, boolean clockwise) {
		super(world, x, y, 0, 0, level, true);
		this.anticlockwise = clockwise;
		dxAdd = dxStart;
		dyAdd = dyStart;

		r = (float) Math.sqrt(x * x + y * y);
		theta = Math.atan2(y, x);
		thetaSpeed = 1 / r;
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
				for (int i = 0; i < 3; i++) {
					double angle = Math.PI * 2 * i / 3;

					float dx = (float) Math.cos(angle + facingAngle);
					float dy = (float) Math.sin(angle + facingAngle);
					float power = (float) Util.randFloat() * level / 2;
					// recursive asteroids!
					world.addObject(new PAsteroidEnemyOrbiting(world, x + dx * (level * 100), y + dy * (level * 100), dx * power, dy * power, level - 1,
							anticlockwise));
				}
			}

			return true;
		}

		facingAngle += (float) dTime * rotSpeed1;
		yRot += (float) dTime * rotSpeed2;

		theta += anticlockwise ? thetaSpeed * dTime : -thetaSpeed * dTime;

		x = (float) (Math.cos(theta) * r) + xAdd;
		y = (float) (Math.sin(theta) * r) + yAdd;

		dx = 1.5f * -y / r;
		dy = 1.5f * x / r;

		xAdd += dTime * dxAdd;
		yAdd += dTime * dyAdd;

		dxAdd *= drag;
		dyAdd *= drag;

		top = y + radius;
		bottom = y - radius;

		return false;
	}

	// @Override
	// public float getScale() {
	// return level * 5;
	// }
}
