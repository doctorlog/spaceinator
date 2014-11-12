package au.com.f1n.spaceinator.game.enemy;

import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.PParticleExplosion;
import au.com.f1n.spaceinator.game.World;

public class PWormEnemy extends PEnemy {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final float ANGLE_ROT_SPEED = 0.003f;
	// Effectively, this becomes like a linked list
	PWormEnemy head;
	PWormEnemy tail;
	int groupIndex;
	boolean hovering;
	float hoverX;
	float hoverY;

	public PWormEnemy(World world, float x, float y, PWormEnemy head) {
		super(world, x, y, 0, 0, PEnemy.CLASS_WORM_TAIL);
		radius = 250;
		life = 400;
		this.head = head;
		if (head != null) {
			head.tail = this;
			if (head.head != null) {
				head.enemyClass = PEnemy.CLASS_WORM_BODY;
				head.radius = 150;
			} else
				head.enemyClass = PEnemy.CLASS_WORM_HEAD;

			drag = .9f;
		}
	}

	public boolean isHead() {
		return head == null;
	}

	public boolean isShield() {
		return tail == null || head == null;
	}

	public boolean isTail() {
		return tail == null;
	}

	@Override
	public boolean timeStep(int dTime, long timeS) {
		if (!noWarp && warping(timeS))
			return false;

		if (life <= 0)
			return true;

		float dyOld = dy;
		float dxOld = dx;

		if (head == null) {
			float dd2 = dx * dx + dy * dy;

			if (dd2 < MAX_SPEED_2) {
				dx += accelX;
				dy += accelY;
			}

			facingAngle = facingAngle * .9f + accelAngle * .1f;
		} else {
			float headdx = head.x - x;
			float headdy = head.y - y;
			float dist2 = headdx * headdx + headdy * headdy;

			// if (dist2 > radius2) {
			accelAngle = (float) Math.atan2(headdy, headdx);
			float speed = dist2 * accelSlow * dTime / 250000000f;
			if (speed > .0004f)
				speed = .0004f;

			dx += headdx * speed;
			dy += headdy * speed;

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
			if (Math.abs(accelAngle - facingAngle) < ANGLE_ROT_SPEED)
				facingAngle = accelAngle;
		}

		x += dTime * (dx + dxOld) / 2;
		y += dTime * (dy + dyOld) / 2;

		dx *= drag;
		dy *= drag;

		top = y + radius;
		bottom = y - radius;

		return false;
	}

	/**
	 * Take off the life from this enemy
	 * 
	 * @param damage
	 * @return the amount of "leftover" damage
	 */
	@Override
	public void damage(int damage, long timeS, float fromdx, float fromdy, boolean laser) {
		if (isShield())
			return;

		life -= damage;
		if (life <= 0) {
			world.graphicEffect(new PParticleExplosion(x, y, 0, 0, 250, world.lastTime, 3000, 2000, radius, .7f, PParticleExplosion.COLOUR_HOT));

			if (ai != null)
				ai.killed(this);

			// Now set up the tails and heads of the new worms.
			if (head != null) {
				PWormEnemy cur = head;
				cur.enemyClass = PEnemy.CLASS_WORM_TAIL;
				cur.tail = null;
				cur.radius = 250;

				int sizeWorm = 1;
				// find the head of the upper worm
				while (cur.head != null) {
					cur = cur.head;
					sizeWorm++;
				}

				if (sizeWorm > 2) {
					cur.randomHeadHover();
				} else {
					head.life = 0;
					ai.killed(head);
					world.graphicEffect(new PParticleExplosion(head.x, head.y, 0, 0, 250, world.lastTime, 3000, 2000, head.radius, .7f, PParticleExplosion.COLOUR_HOT));
					if (head.head != null) {
						head.head.life = 0;
						ai.killed(head.head);
						world.graphicEffect(new PParticleExplosion(head.head.x, head.head.y, 0, 0, 250, world.lastTime, 3000, 2000, head.head.radius, .7f, PParticleExplosion.COLOUR_HOT));
					}
				}
			}
			if (tail != null) {
				PWormEnemy cur = tail;
				cur.enemyClass = PEnemy.CLASS_WORM_HEAD;
				cur.head = null;
				cur.radius = 250;

				int sizeWorm = 1;
				// find the head of the upper worm
				while (cur.tail != null) {
					cur = cur.tail;
					sizeWorm++;
				}

				if (sizeWorm > 2) {
					tail.randomHeadHover();
				} else {
					tail.life = 0;
					ai.killed(tail);
					world.graphicEffect(new PParticleExplosion(tail.x, tail.y, 0, 0, 250, world.lastTime, 3000, 2000, tail.radius, .7f, PParticleExplosion.COLOUR_HOT));
					if (tail.tail != null) {
						tail.tail.life = 0;
						ai.killed(tail.tail);
						world.graphicEffect(new PParticleExplosion(tail.tail.x, tail.tail.y, 0, 0, 250, world.lastTime, 3000, 2000, tail.tail.radius, .7f, PParticleExplosion.COLOUR_HOT));
					}
				}
			}

			head = null;
			tail = null;
		}
	}

	private void randomHeadHover() {
		hovering = true;
		switch ((int) (Util.randFloat() * 4)) {
		case 0:
			hoverX = 7000;
			hoverY = 5500;
			break;
		case 1:
			hoverX = 17000;
			hoverY = -12400;
			break;
		case 2:
			hoverX = 45000;
			hoverY = 1780;
			break;
		case 3:
			hoverX = 57000;
			hoverY = -3500;
			break;
		}
	}
}
