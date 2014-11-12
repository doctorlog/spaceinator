package au.com.f1n.spaceinator.game;

import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.enemy.PEnemy;

/**
 * Class for the particle of laser
 * 
 * @author luke
 * 
 */
public class LaserParticle extends PWeaponParticle {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int SPEED_BASE = 8;
	private static final long LIFETIME = 1500;

	public float dx;
	public float dy;
	public long startTime;
	public int damage;
	public int colour;
	private PObject owner;
	private World world;
	private float scale;

	public LaserParticle(World world, PObject owner, float xOrigin, float yOrigin, float baseDx, float baseDy, float shootAngle, long startTime, int colour,
			int baseDamage, float speed) {
		super(xOrigin, yOrigin, 50);
		this.startTime = startTime;
		this.shootAngle = shootAngle;
		this.owner = owner;
		this.world = world;
		this.colour = colour;

		// I have no idea why I have to multiply baseDx by 2 but I just do
		// (otherwise it doesnt work..!?!?)
		dx = (float) Math.cos(shootAngle) * speed + baseDx * 2;
		dy = (float) Math.sin(shootAngle) * speed + baseDy * 2;

		x = lastx = xOrigin + dx * 2;
		y = lasty = yOrigin + dy * 2;

		top = bottom = y;
		damage = baseDamage;
	}

	public boolean timeStep(int dTime, long timeMS) {
		if (timeMS - startTime > LIFETIME || damage == 0)
			return true;

		lastx = x;
		lasty = y;

		// Straight line weapons are easy - no fancy integration required.
		x += dx * dTime;
		y += dy * dTime;

		// More complicated top and bottom for lasers
		if (y > lasty) {
			// Moving "up"
			top = y;
			bottom = lasty;
		} else {
			// Moving "down"
			top = lasty;
			bottom = y;
		}

		// 4 frames of smaller laser
		if (scale < 1)
			scale += .25f;
		else {
			byte[] c = ALL_COL[colour];
			world.getMissilesParticle().addParticleFor(this, c[0], c[1], c[2], (float) Util.randFloat() - .5f, (float) Util.randFloat() - .5f);
		}

		return false;
	}

	@Override
	public void damage(int dmg, long timeS, float dx, float dy, boolean laser) {
		// NA
	}

	@Override
	public void checkCollision(PObject other) {
		if (other == owner)
			return;
		if (other instanceof PPlanet) {
			if (isIn(other)) {
				float dx = x - other.x;
				float dy = y - other.y;
				float scale = Util.invSqrt(dx * dx + dy * dy) * 5;
				world.graphicEffect(x, y, scale * dx, scale * dy, 1.5f, world.lastTime, 1200, 0, 1f, PParticleExplosion.COLOUR_HOT);
				damage = 0;
			}
		} else if (other instanceof PEnemy) {
			if (isIn(other)) {
				PEnemy otherEnemy = (PEnemy) other;
				// laser hits enemy
				int dmg = Math.min(damage, otherEnemy.life);
				other.damage(dmg, world.lastTime, dx * 8, dy * 8, true);
				damage -= dmg;

				// Check to see if it was our laser that did it?
				if (otherEnemy.life <= 0 && owner instanceof PSpaceShip)
					world.goodKill(otherEnemy);
			}
		} else if (other instanceof PSpaceShip) {
			if (isIn(other) && world.zooming == 0 && other.z > -.001) {
				// laser hits us
				other.damage(damage, world.lastTime, dx * 8, dy * 8, true);
				damage = 0;
				float dx = x - other.x;
				float dy = y - other.y;
				float scale = Util.invSqrt(dx * dx + dy * dy) * 4;
				world.graphicEffect(x, y, scale * dx, scale * dy, 1.5f, world.lastTime, 1200, 0, 1f, PParticleExplosion.COLOUR_BLUE);
			}
		}
	}

	/**
	 * Lasers move fast so check for collisions using distance to a line
	 * 
	 * vector v = x,y <br />
	 * vector w = lastx, lasty <br />
	 * point p of interest = other.x, other.y
	 */
	@Override
	public boolean isIn(PObject other) {
		float px = lastx - x;
		float py = lasty - y;

		float d = px * px + py * py;
		float u = ((other.x - x) * px + (other.y - y) * py) / d;

		if (u > 1)
			u = 1;
		else if (u < 0)
			u = 0;

		float distx = x + u * px - other.x;
		float disty = y + u * py - other.y;

		float distReq = radius + other.radius;

		return distx * distx + disty * disty < distReq * distReq;
	}

	@Override
	public void remove() {
		damage = 0;
	}

	@Override
	public int getColour() {
		return colour;
	}

	@Override
	public float getScale() {
		return scale;
	}
}
