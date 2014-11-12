package au.com.f1n.spaceinator.game;

import java.io.Serializable;

import au.com.f1n.spaceinator.GBSoundManager;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.enemy.PAsteroidEnemy;
import au.com.f1n.spaceinator.game.enemy.PEnemy;

/**
 * Automatic aiming and firing!
 * 
 * @author luke
 * 
 */
public class WeaponShipTurret extends Weapon implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int MAX_DIST2 = 7000 * 7000;
	public static final float[][] ANGLES = { { -0.2f, -0.1f }, { 0.1f, 0.2f }, { -2f, -1.9f }, { 1.9f, 2f } };

	private long lastTime = 0;
	private long lastSeek = 0;
	private long fireRate = 100;
	private int baseDamage;
	private int turretIndex;
	private float shootAngle;
	private float minAngle;
	private float maxAngle;
	private boolean firing = false;
	private transient GBSoundManager soundManager;
	private PShipParticlesTop particles;

	public WeaponShipTurret(World world, int turretIndex, GBSoundManager soundManager, PShipParticlesTop particles, int powerLevel, int fireRate, int spread) {
		super(world);
		this.baseDamage = 8 + powerLevel * 3;
		this.turretIndex = turretIndex;
		this.soundManager = soundManager;
		this.particles = particles;
		minAngle = ANGLES[turretIndex][0] - spread * .1f;
		maxAngle = ANGLES[turretIndex][1] + spread * .1f;

		shootAngle = (minAngle + maxAngle) / 2;

		this.fireRate = 500 - fireRate * 50;
	}

	private void findTarget(long time) {
		if (time - lastSeek > fireRate * 2) {
			float ssf = world.getCentreSpaceShip().facingAngle;
			float ssx = world.getCentreSpaceShip().x;
			float ssy = world.getCentreSpaceShip().y;

			firing = false;

			Object[] obj = world.objects.array;
			int s = world.objects.size;
			for (int i = 0; i < s; i++) {
				if (obj[i] instanceof PEnemy && (!(obj[i] instanceof PAsteroidEnemy) || world.targetAsteroids)) {
					PObject e = (PObject) obj[i];
					float dx = e.x - ssx;
					float dy = e.y - ssy;

					if (dx * dx + dy * dy < MAX_DIST2) {
						// Close enough - correct angle?
						float angle = (float) Math.atan2(dy, dx);
						float angleRef = angle - ssf;
						if (angleRef > Util.PI)
							angleRef -= 2 * Util.PI;
						if (angleRef < -Util.PI)
							angleRef += 2 * Util.PI;

						if (angleRef > minAngle && angleRef < maxAngle) {
							// Found a target, break loop
							firing = true;
							shootAngle = angle;
							break;
						}
					}
				}
			}
			lastSeek = time;
		}
	}

	@Override
	public void shooting(long time, float shootAngle, float dx, float dy) {
		findTarget(time);

		if (firing && time - lastTime > fireRate && world.zooming == 0) {
			soundManager.laser();
			float x = PSpaceShip.TURRET_OFFSETS[turretIndex][0];
			float y = PSpaceShip.TURRET_OFFSETS[turretIndex][1];

			float sinS = (float) Math.sin(world.centreSpaceShip.facingAngle);
			float cosS = (float) Math.cos(world.centreSpaceShip.facingAngle);

			float lasX = world.centreSpaceShip.x + cosS * x - sinS * y;
			float lasY = world.centreSpaceShip.y + sinS * x + cosS * y;

			world.addObject(new LaserParticle(world, world.centreSpaceShip, lasX, lasY, dx, dy, this.shootAngle, time, 2, baseDamage, LaserParticle.SPEED_BASE));

			particles.puff(lasX, lasY, time, PWeaponParticle.TURRET_COL, this.shootAngle, dx, dy, true);
			lastTime = time;
		}
	}

	public int getBaseDamage() {
		return baseDamage;
	}

	public void setBaseDamage(int baseDamage) {
		this.baseDamage = baseDamage;
	}

	public float getFacingAngle() {
		return shootAngle;
	}

	public void resetAngle() {
		float ssf = world.getCentreSpaceShip().facingAngle;
		shootAngle = (minAngle + maxAngle) / 2 + ssf;
		firing = false;
	}

	public void facingInAngle() {
		shootAngle = (minAngle + maxAngle) / 2 + Util.PI / 2;
	}

	public void setSoundManager(GBSoundManager soundManager) {
		this.soundManager = soundManager;
	}
}
