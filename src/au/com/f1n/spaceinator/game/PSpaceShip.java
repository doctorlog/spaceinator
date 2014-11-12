package au.com.f1n.spaceinator.game;

import au.com.f1n.spaceinator.GBSoundManager;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.enemy.PEnemy;
import au.com.f1n.spaceinator.game.logic.GameState;

/**
 * Spaceship object in the world.
 * 
 * This requires a lot more information than any spaceship to give to the user.
 * Enemy spaceships are a lot simpler.
 * 
 * @author luke
 */
public class PSpaceShip extends PObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final float SHIELD_SIZE = 230;
	public static final int BASE_SHIELD = 100;
	public static final int BASE_POWER = 200;
	private static final int PLANET_SHIELD_DAMAGE = 20;
	private static final float DRAG = 0.01f;
	private static final float ANGLE_ROT_SPEED = 0.006f;
	private static final float ANGLE_ERROR = 0.1f;
	public static final float[][] LASER_OFFSETS = { { -97, -81, 5.6f }, { -97, 81, 5.6f }, { 8, 0, 5.5f }, { 75, 0, 4 } };
	public static final float[][] TURRET_OFFSETS = { { -28, -83.5f, 0 }, { -28, 83.5f, 0 }, { -100, -110, 0 }, { -100, +110, 0 } };
	public static final long POWER_TIME = 5000;
	public static final int DRAW_ORDER = -1;

	/**
	 * Desired acceleration angle in radians
	 */
	private float accelAngle;
	public float facingAngle;
	public float accelX;
	public float accelY;
	private int lastHit = 0;
	public boolean shooting;
	public float shootAngle;
	private WeaponShipLaser curWeapon;
	private int maxShield;
	public int curShield;

	private long lastShieldStep;
	private long shieldRate = 200;

	public boolean accelerating;
	private int rotatingProp;
	PShipParticles shipParticles;
	private PShipParticlesTop shipParticlesTop;
	private World world;
	public float dx;
	public float dy;
	public float rotX = 0;
	/**
	 * Set this to true for warp-like levels
	 */
	public boolean facingIn = false;
	float accel = 0;
	private int laserCount;
	private int turretCount;
	private WeaponShipTurret[] turrets;
	private transient GBSoundManager soundManager;
	private float shieldAlpha;
	private boolean accelOn;
	public float power;
	public long lastPower;
	public boolean powerDown;
	private boolean worm;
	private int planetHit;

	public PSpaceShip(World world, float x, float y, float dx, float dy) {
		super(x, y, SHIELD_SIZE);
		this.world = world;
		this.dx = dx;
		this.dy = dy;
		drawOrder = DRAW_ORDER;

		GameState gameState = world.getGameState();

		int[] upgrades = gameState.getShipUpgrades();

		laserCount = upgrades[GameState.UPGRADE_LASER_COUNT] + 1;
		turretCount = upgrades[GameState.UPGRADE_TURRET];
		soundManager = world.gbRenderer.getSoundManager();

		accel = 0.0002f + upgrades[GameState.UPGRADE_THRUSTER] * 0.0001f;
		// Add thruster trail
		shipParticles = new PShipParticles(this, upgrades[GameState.UPGRADE_THRUSTER]);
		world.graphicEffect(shipParticles);

		shipParticlesTop = new PShipParticlesTop(this, 100);

		// Make all weapons
		curWeapon = new WeaponShipLaser(world, laserCount, upgrades[GameState.UPGRADE_LASER_POWER], soundManager, shipParticlesTop, this);
		turrets = new WeaponShipTurret[turretCount];
		for (int i = 0; i < turretCount; i++)
			turrets[i] = new WeaponShipTurret(world, i, soundManager, shipParticlesTop, upgrades[GameState.UPGRADE_TURRET_POWER],
					upgrades[GameState.UPGRADE_TURRET_RATE], upgrades[GameState.UPGRADE_TURRET_SPREAD]);

		// setup ship shield
		maxShield = BASE_SHIELD + gameState.getShipUpgrades()[GameState.UPGRADE_SHIELD] * BASE_SHIELD / 2;
		curShield = maxShield;
	}

	/**
	 * Calculate how gravity of all solar bodies accelerates this space ship and
	 * move it.
	 */
	@Override
	public boolean timeStep(int dTime, long timeS) {
		// top particle is a special type of particle
		shipParticlesTop.timeStep(dTime, timeS);

		if (world.zooming > 0) {
			soundManager.setShip(false);
			return false;
		}

		// Deal with power
		if (timeS - lastPower > POWER_TIME && power > 0) {
			power -= 1f;
			powerDown = true;
		} else if (timeS - lastPower > POWER_TIME && power == -1) {
			power = 0;
		}

		float dxOld = dx;
		float dyOld = dy;

		// Recharge shield
		shieldRate = 200;

		if (curShield < maxShield && timeS - lastShieldStep >= shieldRate) {
			curShield++;
			lastShieldStep = timeS;
		}

		if (worm) {
			accelerating = false;
		} else {
			// Calculate gravity force (change in dx and dy)
			for (PPlanet planet : world.getPlanets()) {
				float distX = planet.x - x;
				float distY = planet.y - y;
				float dist2 = distX * distX + distY * distY;

				// F = ma, thus a = F / m (and below is std gravity divided by
				// this.mass)
				float accel = World.G * planet.mass / dist2;

				// Normalise vector
				dist2 = Util.invSqrt(dist2);
				distX *= dist2 * accel;
				distY *= dist2 * accel;

				dx += distX;
				dy += distY;
			}

			// Rotate ship
			accelerating = accelOn;
			if (accelerating && Math.abs(accelAngle - facingAngle) > ANGLE_ERROR) {
				// How do we rotate? accelAngle is between -pi and pi (comes from
				// atan2)
				if (Math.abs(accelAngle - facingAngle) < Math.PI) {
					// No brainer, simple rotation
					boolean rotatingLeft = facingAngle < accelAngle;
					facingAngle += (rotatingLeft ? ANGLE_ROT_SPEED : -ANGLE_ROT_SPEED) * (float) dTime;
				} else {
					// Greater than 180 degrees difference
					boolean rotatingLeft = facingAngle > accelAngle;
					facingAngle += (rotatingLeft ? ANGLE_ROT_SPEED : -ANGLE_ROT_SPEED) * (float) dTime;
					if (facingAngle < -Math.PI)
						facingAngle += Math.PI * 2;
					if (facingAngle > Math.PI)
						facingAngle -= Math.PI * 2;
				}

				accelerating = false;
				// Also reset turrets
				for (int i = 0; i < turretCount; i++)
					turrets[i].resetAngle();
			} else {
				facingAngle = accelAngle;
			}

			if (facingIn) {
				shootAngle = Util.PI / 2;
				// Force turrets to be the same
				for (int i = 0; i < turretCount; i++)
					turrets[i].facingInAngle();
			}

			soundManager.setShip(accelerating);

			// Check helio collision - special case of collision detection
			if (world.zooming == 0) {
				world.boundry.collideTest(this, soundManager);

				if (curWeapon != null && shooting) {
					// Fire weapon (let the weapon decide if it can fire this
					// instant)
					curWeapon.shooting(timeS, shootAngle, dx, dy);
				}

				for (int i = 0; i < turretCount; i++)
					turrets[i].shooting(timeS, 0, dx, dy);

				if (accelerating) {
					dx += accelX * accel * dTime;
					dy += accelY * accel * dTime;
				}
			}

			if (curShield <= 0) {
				soundManager.explode();
				soundManager.stopShip();
			}

			// Dampner - velocity squared resistence!
			dx -= Math.signum(dx) * DRAG * dx * dx;
			dy -= Math.signum(dy) * DRAG * dy * dy;

			x += (dx + dxOld) * dTime / 2f;
			y += (dy + dyOld) * dTime / 2f;

			if (lastHit > 0)
				lastHit--;
		}
		top = y + radius;
		bottom = y - radius;

		return curShield <= 0;
	}

	public void setAccel(float x, float y, float angle) {
		accelOn = true;
		accelX = x;
		accelY = y;
		accelAngle = angle;
	}

	public float getFacingAngle() {
		return facingAngle;
	}

	public boolean isAccelOn() {
		return accelOn;
	}

	public void shootAt(float angle) {
		if (curWeapon != null) {
			shooting = true;
			shootAngle = angle;
		}
	}

	public float getAccelX() {
		return accelX;
	}

	public float getAccelY() {
		return accelY;
	}

	// The absolute highest this can return is 255 - 20 = 235
	public int getCurShield() {
		return curShield;
	}

	public float getShieldAlpha() {
		return shieldAlpha;
	}

	public boolean isAccelerating() {
		return accelerating && world.zooming <= 0;
	}

	public int getRotatingPropulsion() {
		if (rotatingProp > 0) {
			rotatingProp--;
			return 1;
		} else if (rotatingProp < 0) {
			rotatingProp++;
			return -1;
		}
		return 0;
	}

	public boolean isShooting() {
		return shooting;
	}

	@Override
	public void damage(int dmg, long timeS, float dx, float dy, boolean laser) {
		if (dmg <= 0)
			return;

		// make it easier...
		if (dmg > 1)
			dmg /= 2;

		world.hitShip(dmg, dx, dy);

		if (curShield > 25 && curShield - dmg < 25)
			soundManager.alarm();

		curShield -= dmg;
		if (laser)
			soundManager.scratch();
		else
			soundManager.hit();
	}

	@Override
	public void checkCollision(PObject other) {
		if (world.zooming != 0 || z < -.001)
			// When we are zooming, there is no collision with the spaceship...
			return;

		if (other instanceof PPlanet) {
			if (lastHit == 0 && isIn(other)) {
				// Accelerate violently away from the planet - destroying part of
				// the shield
				float hitVecX = x - other.x;
				float hitVecY = y - other.y;
				float dist = Util.invSqrt(hitVecX * hitVecX + hitVecY * hitVecY);

				// Normalised vector tangent of planet where we hit
				hitVecX *= dist;
				hitVecY *= dist;

				// Calculate reflecting vector as -2*(V dot N)*N + V
				float dotNeg2 = -2 * (dx * hitVecX + dy * hitVecY);

				hitVecX *= dotNeg2;
				hitVecY *= dotNeg2;

				// Increase the force (will ensure we go away from the planet)
				dx += hitVecX * 2f;
				dy += hitVecY * 2f;

				damage(PLANET_SHIELD_DAMAGE, world.lastTime, -hitVecX * 30, -hitVecY * 30, false);

				// Dont allow a hit for 10 at least frames
				lastHit = 10;

				planetHit++;
				if (!world.gameState.achievement_planet_basher_25 && planetHit >= 25)
					world.gameState.achievement_planet_basher_25 = world.gameState.achievement(R.string.achievement_planet_basher_25, 25);
			}
		} else if (other instanceof PEnemy) {
			// This is subtle - it means we can put the override code for
			// WorldWarp2 into the Asteroid
			if (other.isIn(this)) {
				// Hitting an enemy hurts me
				PEnemy enemy = (PEnemy) other;
				damage(enemy.life, world.lastTime, enemy.dx, enemy.dy, false);

				// Destroy enemy
				enemy.damage(enemy.life, world.lastTime, dx * 2, dy * 2, false);

				// Enemy slows us down
				dx = dx * .95f + enemy.dx * .05f;
				dy = dy * .95f + enemy.dy * .05f;
			}
		} else if (other instanceof PWeaponParticle) {
			// lasers hit us, not us hit laser
			other.checkCollision(this);
		}
	}

	public float getShootingAngle() {
		return shootAngle;
	}

	public float getAccelAngle() {
		return accelAngle;
	}

	public void setAccelOff() {
		accelX = accelY = 0;
		accelOn = false;
	}

	public void setShootingOff() {
		shooting = false;
		// Forward facing turrets
		shootAngle = Util.PI / 2;
	}

	public int getLaserCount() {
		return laserCount;
	}

	public int getTurretCount() {
		return turretCount;
	}

	public float getShootingTurretAngle(int index) {
		return turrets[index].getFacingAngle();
	}

	public void setShieldAlpha(float shieldAlpha) {
		this.shieldAlpha = shieldAlpha;
	}

	public PShipParticles getShipParticles() {
		return shipParticles;
	}

	public PShipParticlesTop getTopParticles() {
		return shipParticlesTop;
	}

	public void increasePower() {
		if (power < 180 && power != -1) {
			power += .5f;
			lastPower = world.lastTime;
			powerDown = false;
		} else if (power >= 180) {
			power = -1;
			lastPower = world.lastTime;
		}
	}

	public void setWorm(boolean worm) {
		this.worm = worm;
	}

	public void removeWeapon() {
		curWeapon = null;
	}

	public boolean isWeaponless() {
		return curWeapon == null;
	}

	public void topParticleZoom() {
		shipParticlesTop = new PShipParticlesTopZoom(this);
	}

	public WeaponShipLaser getCurWeapon() {
		return curWeapon;
	}

	public void setCurWeapon(WeaponShipLaser curWeapon) {
		this.curWeapon = curWeapon;
	}

	public void setTurretCount(int turretCount) {
		this.turretCount = turretCount;
	}

	public void reloaded(GBSoundManager soundManager) {
		this.soundManager = soundManager;
		if (curWeapon != null)
			curWeapon.setSoundManager(soundManager);
		for (int i = 0; i < turrets.length; i++)
			turrets[i].setSoundManager(soundManager);
	}

	public void resumeNow(long dTime) {
		lastPower += dTime;
		lastShieldStep += dTime;
	}
}
