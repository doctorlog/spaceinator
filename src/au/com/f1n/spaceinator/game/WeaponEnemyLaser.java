package au.com.f1n.spaceinator.game;

import java.io.Serializable;

import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.enemy.PEnemy;
import au.com.f1n.spaceinator.game.enemy.PEnemyMissile;

public class WeaponEnemyLaser extends Weapon implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int MAX_LASERS = 200;
	public static final int SPEED = 5;
	//private final static byte[] COLOUR_BYTES = { (byte) 255, (byte) 0, (byte) 255, (byte) 255 };

	private long lastTime = 0;
	private long fireRate;
	private long fireRate2;
	private PEnemy owner;
	private int baseDamage = 10;
	private boolean missile;

	public WeaponEnemyLaser(World world, PEnemy owner, long fireRate, long fireRate2, boolean missile) {
		super(world);
		this.owner = owner;
		this.fireRate = fireRate;
		this.fireRate2 = fireRate2;
		this.missile = missile;
	}

	@Override
	public void shooting(long time, float shootAngle, float dx, float dy) {
		if ((time / fireRate2) % 2 == 0) {
			if (time - lastTime > fireRate) {
				if (missile && Util.randFloat() > .9) {
					owner.rotate();
					// Missiles!
					world.addObject(new PEnemyMissile(world, owner.x, owner.y, dx, dy, shootAngle + 1.2f, time));
					world.addObject(new PEnemyMissile(world, owner.x, owner.y, dx, dy, shootAngle - 1.2f, time));
					// When they shoot missiles, wait a long time...
					lastTime = time + fireRate2 * 5 + fireRate * 5;
				} else {
					// standard laser
					world.addObject(new LaserParticle(world, owner, owner.x, owner.y, dx, dy, shootAngle, time, 3, baseDamage, LaserParticle.SPEED_BASE));
					lastTime = time;
				}

			}
		}
	}

	public int getBaseDamage() {
		return baseDamage;
	}

	public void setBaseDamage(int baseDamage) {
		this.baseDamage = baseDamage;
	}
}
