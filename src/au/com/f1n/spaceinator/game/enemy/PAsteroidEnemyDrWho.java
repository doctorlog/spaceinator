package au.com.f1n.spaceinator.game.enemy;

import au.com.f1n.spaceinator.game.PCamera;
import au.com.f1n.spaceinator.game.PDrWho;
import au.com.f1n.spaceinator.game.PObject;
import au.com.f1n.spaceinator.game.PSpaceShip;
import au.com.f1n.spaceinator.game.World;

public class PAsteroidEnemyDrWho extends PAsteroidEnemy {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private float xBase;
	private float yBase;
	private float lastZ;
	private PDrWho pDRWho;

	public PAsteroidEnemyDrWho(World world, float xBase, float yBase, float zBase, int level, PDrWho pDRWho) {
		super(world, xBase, yBase, 0, 0, level, true);
		this.xBase = xBase;
		this.yBase = yBase;
		this.pDRWho = pDRWho;
		z = lastZ = zBase;
	}

	@Override
	public boolean timeStep(int dTime, long timeS) {
		if (startTime == -1)
			startTime = timeS;

		if (!noWarp && warping(timeS))
			return false;

		if (life <= 0)
			return true;

		facingAngle += (float) dTime * rotSpeed1;
		yRot += (float) dTime * rotSpeed2;

		// This will change automatically
		lastZ = z;
		z += pDRWho.speed * 6000;

		float uFloat = (z - PCamera.BASE_Z) / 1000 + PDrWho.N_U;
		int u = (int) (uFloat);

		if (u > PDrWho.N_U - 2)
			return true;

		if (u < 0)
			uFloat = u = 0;
		float propNext = uFloat - u;

		x = xBase + propNext * pDRWho.xOff[u + 1] + (1 - propNext) * pDRWho.xOff[u];
		y = yBase + propNext * pDRWho.yOff[u + 1] + (1 - propNext) * pDRWho.yOff[u];

		top = yBase + radius;
		bottom = yBase - radius;

		return z > PCamera.BASE_Z / 2;
	}

	@Override
	public void checkCollision(PObject other) {
		if (other instanceof PSpaceShip) {
			other.checkCollision(this);
		}
	}

	public boolean isIn(PObject other) {
		// other.top > curObject.bottom && curObject.top > other.bottom
		if (other instanceof PSpaceShip
				&& (lastZ < -PSpaceShip.SHIELD_SIZE && z > -PSpaceShip.SHIELD_SIZE || lastZ < PSpaceShip.SHIELD_SIZE && z > PSpaceShip.SHIELD_SIZE || lastZ < -PSpaceShip.SHIELD_SIZE
						&& z > PSpaceShip.SHIELD_SIZE)) {
			if (super.isIn(other)) {
				// This is a hit!
				pDRWho.speed *= 0.7f;
				world.gbRenderer.getSoundManager().hit();
				return true;
			}
		}
		return false;
	}
}
