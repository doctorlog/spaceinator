package au.com.f1n.spaceinator.game;

import java.io.Serializable;

public abstract class PObject implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public float x;
	public float y;
	public float z;
	public float radius;
	public float top;
	public float bottom;

	// Similar types of objects can be drawn together for fewer GL calls
	public int drawOrder;
	public PEdge leftEdge;
	public PEdge rightEdge;

	public PObject(float x, float y, float radius) {
		super();
		this.x = x;
		this.y = y;
		this.radius = radius;
	}

	public abstract boolean timeStep(int dTime, long timeS);

	public abstract void checkCollision(PObject other);

	public abstract void damage(int dmg, long timeS, float dx, float dy, boolean laser);

	public boolean isIn(PObject other) {
		float r2 = radius + other.radius;
		float dx = x - other.x;
		float dy = y - other.y;

		return dx * dx + dy * dy <= r2 * r2;
	}
}
