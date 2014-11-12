package au.com.f1n.spaceinator.game;

import java.io.Serializable;

import au.com.f1n.spaceinator.GBSoundManager;
import au.com.f1n.spaceinator.Util;

public class WeaponShipLaser extends Weapon implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long lastTime = 0;
	long fireRate;
	private int baseDamage;
	private int laserCount;
	private transient GBSoundManager soundManager;
	private PShipParticlesTop particles;
	private float speed;
	private boolean toggleFire;
	private PSpaceShip pSpaceShip;

	public WeaponShipLaser(World world, int laserCount, int baseDamage, GBSoundManager soundManager, PShipParticlesTop particles, PSpaceShip pSpaceShip) {
		super(world);
		this.laserCount = laserCount;
		this.baseDamage = 10 + 5 * baseDamage;
		this.soundManager = soundManager;
		this.particles = particles;
		this.pSpaceShip = pSpaceShip;
		fireRate = laserCount > 2 ? 180 : 350;
		speed = LaserParticle.SPEED_BASE;

		// colour[1] += baseDamage * 4;
	}

	@Override
	public void shooting(long time, float shootAngle, float dx, float dy) {
		boolean burst = pSpaceShip.power == -1;

		if (time - lastTime > fireRate - (burst ? 150 : pSpaceShip.power / 2)) {
			soundManager.laser();
			for (int i = (toggleFire ? 2 : 0); i < (laserCount < 3 ? laserCount : (toggleFire ? laserCount : 2)); i++) {
				float x = PSpaceShip.LASER_OFFSETS[i][0];
				float y = PSpaceShip.LASER_OFFSETS[i][1];

				float sinS = (float) Math.sin(world.centreSpaceShip.facingAngle);
				float cosS = (float) Math.cos(world.centreSpaceShip.facingAngle);

				float lasX = world.centreSpaceShip.x + cosS * x - sinS * y;
				float lasY = world.centreSpaceShip.y + sinS * x + cosS * y;

				if (burst)
					shootAngle += Util.randFloat() * .25f - .125f;

				byte[] col = burst ? PWeaponParticle.BURST_COL : PWeaponParticle.SHIP_COL;

				world.addObject(new LaserParticle(world, world.centreSpaceShip, lasX, lasY, dx, dy, shootAngle, time, burst ? 1 : 0, baseDamage, speed));

				particles.puff(lasX, lasY, time, col, shootAngle, dx, dy, false);
				lastTime = time;
			}

			if (laserCount > 2)
				toggleFire = !toggleFire;

		}
	}

	public int getBaseDamage() {
		return baseDamage;
	}

	public void setBaseDamage(int baseDamage) {
		this.baseDamage = baseDamage;
	}

	public void setSoundManager(GBSoundManager soundManager) {
		this.soundManager = soundManager;
	}
}
