package au.com.f1n.spaceinator.game.enemy;

import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.PParticleExplosion;
import au.com.f1n.spaceinator.game.World;

/**
 * Independent of any AI - an enemy missile
 * 
 * @author luke
 * 
 */
public class PEnemyMissile extends PEnemy {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final float SPEED = 4;
	private static final float ANGLE_ROT_SPEED = 0.003f;
	private static final long LIFETIME = 5000;
	private long startTime;

	public PEnemyMissile(World world, float x, float y, float dx, float dy, float shootAngle, long time) {
		super(world, x, y, dx, dy, CLASS_MISSILE);
		// missiles dont warp
		warpZ = 1;
		noWarp = true;
		facingAngle = shootAngle;
		startTime = time;
	}

	@Override
	public boolean timeStep(int dTime, long timeS) {
		if ((timeS - startTime) > LIFETIME) {
			life = 0;
			world.graphicEffect(x, y, dx * 10, dy * 10, 8, timeS, 3000, radius * .9f, .97f, PParticleExplosion.COLOUR_HOT);

			return true;
		}

		x += Math.cos(facingAngle) * dTime * SPEED;
		y += Math.sin(facingAngle) * dTime * SPEED;

		accelAngle = (float) Math.atan2(world.getCentreSpaceShip().y - y, world.getCentreSpaceShip().x - x);

		if (Math.abs(accelAngle - facingAngle) < Math.PI) {
			// No brainer, simple rotation
			facingAngle += (facingAngle < accelAngle ? ANGLE_ROT_SPEED : -ANGLE_ROT_SPEED) * (float) dTime;
		} else {
			// Greater than 180 degrees difference
			facingAngle += (facingAngle > accelAngle ? ANGLE_ROT_SPEED : -ANGLE_ROT_SPEED) * (float) dTime;
			if (facingAngle < -Math.PI)
				facingAngle += Math.PI * 2;
			if (facingAngle > Math.PI)
				facingAngle -= Math.PI * 2;
		}

		top = y + radius;
		bottom = y - radius;

		yRot += dTime;

		world.getMissilesParticle().addParticleFor(this, (byte) 255, (byte) 0, (byte) 0, (float) Util.randFloat() / 2 - .25f, (float) Util.randFloat() / 2 - .25f);

		return life <= 0;
	}
}
