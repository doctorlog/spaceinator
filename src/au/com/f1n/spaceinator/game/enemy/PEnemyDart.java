package au.com.f1n.spaceinator.game.enemy;

import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.PParticleExplosion;
import au.com.f1n.spaceinator.game.World;

/**
 * A dart enemy - will remove itself from AI when required
 * 
 * @author luke
 * 
 */
public class PEnemyDart extends PEnemy {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int darting;
	private float spinSpeed;
	private long nextParticle;
	private long startDart;
	private int level;

	public PEnemyDart(World world, float x, float y, float dx, float dy) {
		super(world, x, y, dx, dy, CLASS_DART);
	}

	@Override
	public boolean timeStep(int dTime, long timeS) {
		boolean ret = super.timeStep(dTime, timeS);

		if (!ret) {
			if (!noWarp && warping(timeS))
				return false;

			switch (darting) {
			case 0:
				// dont care about AI - if in sight, DART!
				float tX = world.getCentreSpaceShip().x - x;
				float tY = world.getCentreSpaceShip().y - y;
				if (tX * tX + tY * tY < 4000 * 4000)
					dart();
				break;
			case 1:
				// spinning and facing
				yRot += spinSpeed * dTime;
				spinSpeed += .0004f * dTime * (3 - level);

				if (spinSpeed < 2)
					accelAngle = (float) Math.atan2(world.getCentreSpaceShip().y - y, world.getCentreSpaceShip().x - x);

				if (spinSpeed > 3) {
					darting = 2;
					for (double angle = facingAngle - Math.PI - .5; angle < facingAngle - Math.PI + .5; angle += .05)
						world.getMissilesParticle().addParticleFor(this, (byte) 255, (byte) 255, (byte) 255, (float) Math.cos(angle), (float) Math.sin(angle));
					drag = .92f;

					dx = (float) Math.cos(facingAngle) * 32;
					dy = (float) Math.sin(facingAngle) * 32;
					yRot = yRot % (Util.PI * 2);
					startDart = timeS;
				} else if (timeS > nextParticle) {
					double angle = facingAngle - Math.PI + Util.randFloat() - .5;
					world.getMissilesParticle().addParticleFor(this, (byte) 255, (byte) 255, (byte) 255, (float) Math.cos(angle), (float) Math.sin(angle));
					nextParticle = timeS + (long) (spinSpeed - 3);
				}
				break;
			case 2:
				yRot *= .95f;
				if (startDart + 1300 < timeS)
					dart();
				else
					world.getMissilesParticle().addParticleFor(this, (byte) 255, (byte) 128, (byte) 128, (float) Util.randFloat() / 2 - .25f,
							(float) Util.randFloat() / 2 - .25f);
			default:
				break;
			}
		} else {
			// unit lost a pylon
			if (level == 2)
				return true;

			double a = facingAngle;
			if (level == 0)
				a += .2;
			else
				a -= .2;

			float gdx = (float) Math.cos(a) * radius / 3;
			float gdy = (float) Math.sin(a) * radius / 3;

			world.graphicEffect(x + gdx, y + gdy, gdx, gdy, 2f, timeS, 2000, radius / 2, .99f, PParticleExplosion.COLOUR_RED);

			if (darting == 2) {
				darting = 0;
				dx *= -1;
				dy *= -1;
			}

			level++;
			enemyClass++;
			drawOrder++;
			life = 70 * level;
			return false;
		}

		return ret;
	}

	/**
	 * This is called by the AI!
	 */
	public void dart() {
		// Unhook ai
		if (ai != null)
			ai.killed(this);
		darting = 1;
		drag = .7f;
		spinSpeed = 0;
		targetDest = false;
		accelX = accelY = 0;
		accelAngle = (float) Math.atan2(world.getCentreSpaceShip().y - y, world.getCentreSpaceShip().x - x);
	}

	/**
	 * Take off the life from this enemy
	 * 
	 * @param damage
	 * @return the amount of "leftover" damage
	 */
	@Override
	public void damage(int damage, long timeS, float fromdx, float fromdy, boolean laser) {
		life -= damage;
		if (life <= 0 && (particledDeath || laser) && level < 2)
			world.graphicEffect(x, y, fromdx, fromdy, 8, timeS, 3000, radius * .9f, .97f, PParticleExplosion.COLOUR_HOT);
	}
}
