package au.com.f1n.spaceinator.game.enemy;

import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.PCamera;
import au.com.f1n.spaceinator.game.PObject;
import au.com.f1n.spaceinator.game.PParticleExplosion;
import au.com.f1n.spaceinator.game.PPlanet;
import au.com.f1n.spaceinator.game.PPlasmaParticle;
import au.com.f1n.spaceinator.game.PSpaceShip;
import au.com.f1n.spaceinator.game.PWeaponParticle;
import au.com.f1n.spaceinator.game.Weapon;
import au.com.f1n.spaceinator.game.WeaponEnemyLaser;
import au.com.f1n.spaceinator.game.World;

public class PEnemy extends PObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int CLASS_SCOUT = 0;
	public static final int CLASS_SHOOTER = 1;
	public static final int CLASS_TANK = 2;
	public static final int CLASS_FIGHTER = 3;
	public static final int CLASS_SPIKE = 4;
	public static final int CLASS_BOMB = 5;
	public static final int CLASS_MISSILE = 6;
	public static final int CLASS_WORM_HEAD = 7;
	public static final int CLASS_WORM_BODY = 8;
	public static final int CLASS_WORM_TAIL = 9;
	public static final int CLASS_DART = 10;
	public static final int CLASS_DART1 = 11;
	public static final int CLASS_DART2 = 12;

	public static final int CLASS_ASTEROID_SMALL = 13;
	public static final int CLASS_ASTEROID = 14;
	public static final int CLASS_ASTEROID_ALT = 15;
	public static final int CLASS_ASTEROID_BIG = 16;

	public static final boolean[] ASTEROID_CLASS = { false, false, false, false, false, false, false, false, false, false, false, false, false, true, true,
			true, true };
	/**
	 * The highest AI relevant enemy
	 */
	public static final int AI_CLASS_COUNT = 13;
	public static final int CLASS_COUNT = 17;

	public static final int LIFE = 10;
	public static final float MAX_SPEED_2 = .4f;
	private static final float MAX_SPEED_2_TARGET = 1f;
	private static final float SLOW_DIST2 = 400 * 400;
	private static final float BLOW_DIST2 = 800 * 800;
	private static final float ACCEL_FAST = 0.2f;
	public static final float ACCEL_SLOW = 0.05f;
	public static final float WARP_TIME = 500;
	public static final float BASE_WARP = PCamera.BASE_Z * 10;

	/**
	 * The acceleration angle in radians
	 */
	protected float accelAngle;
	public float facingAngle;
	float accelX;
	float accelY;
	boolean shooting;
	float shootAngle;
	private Weapon curWeapon;
	public int life;
	public boolean targetDest;
	private float targetDestX;
	private float targetDestY;
	public float targetDist2;
	public boolean targetClose;

	/**
	 * Rotation around the y-axis means the AI changed it's mind.
	 */
	public float yRot = 0;
	private boolean rotating = false;
	private long startRot;
	public int enemyClass;
	protected World world;
	public float dx;
	public float dy;
	public PEnemyAIGroup ai;
	private int targetTrack;
	public float drag = 0.99f;
	public float warpZ;
	protected long startTime = -1;
	protected float accelFast = ACCEL_FAST;
	protected float accelSlow = ACCEL_SLOW;
	protected boolean noWarp;
	protected boolean particledDeath = true;
	public float scale = 1;

	public PEnemy(World world, float x, float y, float dx, float dy, int enemyClass) {
		super(x, y, enemyClass == CLASS_SCOUT ? 75 : 90);
		this.world = world;
		this.dx = dx;
		this.dy = dy;
		this.enemyClass = enemyClass;
		drawOrder = enemyClass;

		accelAngle = accelX = accelY = 0;
		yRot = 0;
		life = LIFE;

		warpZ = 0;
		// While warping, can't collide with anything
		top = bottom = Float.MAX_VALUE;

		switch (enemyClass) {
		case CLASS_SCOUT:
			curWeapon = null;
			radius = 60;
			break;
		case CLASS_ASTEROID:
			curWeapon = null;
			radius = 75;
			break;
		case CLASS_SHOOTER:
			curWeapon = new WeaponEnemyLaser(world, this, (long) (600 + Util.randFloat() * 400), 1, false);
			curWeapon.setBaseDamage(4);
			radius = 95;
			break;
		case CLASS_TANK:
			life = 150;
			radius = 180;
			accelFast /= 2;
			accelSlow /= 2;
			break;
		case CLASS_SPIKE:
			life = 2000;
			radius = 400;
			accelFast /= 10;
			accelSlow /= 10;
			break;
		case CLASS_FIGHTER:
			life = 30;
			radius = 100;
			accelFast *= 2;
			accelSlow *= 2;
			curWeapon = new WeaponEnemyLaser(world, this, (long) (100 + Util.randFloat() * 20), 200, true);
			curWeapon.setBaseDamage(3);
			break;
		case CLASS_BOMB:
			life = 50;
			radius = 110;
			accelFast *= 5;
			accelSlow *= 5;
			break;
		case CLASS_MISSILE:
			life = 5;
			radius = 50;
			accelFast *= 10;
			break;
		case CLASS_DART:
			life = 70;
			radius = 250;
			accelFast *= 10;
			break;
		case CLASS_WORM_BODY:
		case CLASS_WORM_TAIL:
			// no break;
		case CLASS_WORM_HEAD:
			break;
		}
	}

	/**
	 * Calculate how gravity of all solar bodies accelerates this enemy ship and
	 * move it.
	 * 
	 * @return
	 */
	@Override
	public boolean timeStep(int dTime, long timeS) {
		if (!noWarp && warping(timeS))
			return false;

		if (life <= 0) {
			if (ai != null && enemyClass < CLASS_DART)
				ai.killed(this);
			return true;
		}

		facingAngle = facingAngle * .9f + accelAngle * .1f;

		// rotation
		if (rotating) {
			if (timeS - startRot > 2000)
				rotating = false;
			else
				yRot = 360 * Util.slowInOut((float) (timeS - startRot) / 2000f);
		}

		if (enemyClass == CLASS_BOMB) {
			float tX = world.getCentreSpaceShip().x - x;
			float tY = world.getCentreSpaceShip().y - y;
			targetDist2 = tX * tX + tY * tY;
			if (targetDist2 < BLOW_DIST2) {
				world.addObject(new PPlasmaParticle(world, x, y, false));
				// no graphic death required, just blow
				return true;
			}
			yRot += .1f * dTime;
		}

		// This is really part of the AI - simple accelerate toward a point.
		if (targetDest)
			if (targetTrack <= 0) {
				float tX = targetDestX - x;
				float tY = targetDestY - y;
				targetDist2 = tX * tX + tY * tY;
				float distInv = Util.invSqrt(targetDist2);
				tX *= distInv;
				tY *= distInv;
				targetClose = targetDist2 < SLOW_DIST2;

				if (targetClose) {
					// Close, go slow
					setAccel(tX * accelSlow, tY * accelSlow);
				} else {
					// Far away, ACCELERATE
					setAccel(tX * accelFast, tY * accelFast);
				}

				targetTrack = 10;
			} else {
				targetTrack--;
			}

		float dyOld = dy;
		float dxOld = dx;

		if (shooting && curWeapon != null) {
			// Fire weapon (let the weapon decide if it can fire this instant)
			curWeapon.shooting(timeS, shootAngle, dx, dy);
		}

		// Calculate force (change in dx and dy)
		for (PPlanet planet : world.planets) {
			float distX = planet.x - x;
			float distY = planet.y - y;
			float dist2 = distX * distX + distY * distY;

			// F = ma, thus a = F / m (and below is std gravity divided by
			// this.mass)
			// Spike cant hit planets - they have negative gravity for him
			float accel = (enemyClass == CLASS_SPIKE ? -World.G * 10 : World.G) * planet.mass / dist2;

			distX *= accel / dist2;
			distY *= accel / dist2;

			dx += distX;
			dy += distY;
		}

		float dd2 = dx * dx + dy * dy;
		if (targetDest) {
			dx *= .9f;
			dy *= .9f;
			if (dd2 < MAX_SPEED_2_TARGET) {
				dx += accelX;
				dy += accelY;
			}
		} else if (dd2 < MAX_SPEED_2) {
			dx += accelX;
			dy += accelY;
		}

		x += dTime * (dx + dxOld) / 2;
		y += dTime * (dy + dyOld) / 2;

		dx *= drag;
		dy *= drag;

		top = y + radius;
		bottom = y - radius;

		return false;
	}

	protected boolean warping(long timeS) {
		if (startTime == -1)
			startTime = timeS;

		if (timeS - startTime < WARP_TIME) {
			// warping in.
			warpZ = Util.slowOut((float) (timeS - startTime) / WARP_TIME);
			return true;
		} else {
			warpZ = 1;
			noWarp = true;
		}
		return false;
	}

	public float getDX() {
		return dx;
	}

	public float getDY() {
		return dy;
	}

	public void setAccel(float x, float y) {
		accelX = x;
		accelY = y;
		accelAngle = (float) Math.atan2(y, x);
	}

	public float getAccelAngle() {
		return accelAngle;
	}

	public float getAccelX() {
		return accelX;
	}

	public float getAccelY() {
		return accelY;
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
		if (life <= 0 && (particledDeath || laser))
			world.graphicEffect(x, y, fromdx, fromdy, 8, timeS, 3000, radius * .9f, .97f, PParticleExplosion.COLOUR_HOT);
	}

	public boolean isDead() {
		return life <= 0;
	}

	public float getScale() {
		return scale;
	}

	@Override
	public void checkCollision(PObject other) {
		if (other instanceof PWeaponParticle || other instanceof PSpaceShip) {
			other.checkCollision(this);
		} else if (other instanceof PAsteroidEnemy) {
			// Enemy can hit an asteroid but not each other
			if (isIn(other)) {
				PAsteroidEnemy otherEnemy = (PAsteroidEnemy) other;
				// laser hits enemy
				int dmg = Math.min(life, otherEnemy.life);
				other.damage(dmg, world.lastTime, dx * 30, dy * 30, false);
				damage(dmg, world.lastTime, otherEnemy.dx * 30, otherEnemy.dy * 30, false);
			}
		} else if (other instanceof PPlanet) {
			if (isIn(other)) {
				if (enemyClass == CLASS_SPIKE) {
					// Spike only bounces off planets
					float ddx = other.x - x;
					float ddy = other.y - y;
					float inv = Util.invSqrt(ddx * ddx + ddy * ddy);
					dx -= ddx * inv;
					dy -= ddy * inv;
				} else {
					damage(life, world.lastTime, -dx * 2, -dy * 2, false);
				}
			}
		}
	}

	public Weapon getWeapon() {
		return curWeapon;
	}

	public void setTargetDest(float tX, float tY, int n) {
		// Check if we would be going through a planet
		if (n < 3) {
			// Maximum of 3 times
			for (int i = 0; i < world.planets.length; i++) {
				PPlanet planet = world.planets[i];
				// For collision check, multiply this enemy radius by 1.5f
				if (Util.distLT(x, y, tX, tY, planet.x, planet.y, radius * 1.5f + planet.radius)) {
					float dx = tX - x;
					float dy = tY - y;

					// Recursively find a tangent to go along
					setTargetDest(tX - dy * .5f, tY + dx * .5f, ++n);
					return;
				}
			}
		}

		targetDestX = tX;
		targetDestY = tY;
	}

	public float getTargetDestY() {
		return targetDestY;
	}

	public float getTargetDestX() {
		return targetDestX;
	}

	public int getScore() {
		switch (enemyClass) {
		case CLASS_SCOUT:
		case CLASS_SHOOTER:
			return 1;
		case CLASS_TANK:
			return 10;
		case CLASS_FIGHTER:
			return 3;
		case CLASS_SPIKE:
			return 200;
		case CLASS_BOMB:
			return 2;
		}
		return 0;
	}

	public void rotate() {
		startRot = world.lastTime;
		rotating = true;
	}

	public void setNoWarp() {
		warpZ = 1;
		noWarp = true;
	}

	public void setNoParticledDeath() {
		particledDeath = false;
	}
}
