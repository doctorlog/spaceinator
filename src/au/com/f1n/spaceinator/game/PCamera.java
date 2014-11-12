package au.com.f1n.spaceinator.game;

import java.io.Serializable;

public abstract class PCamera implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected World world;
	public static final float BASE_Z = 15000;
	public float z = BASE_Z;

	public PCamera(World world) {
		this.world = world;
	}

	public abstract float getX();

	public abstract float getY();

	public float getZ() {
		return z;
	}

	public abstract void timeStep(int dTime);

	public abstract float getRotX();

	public abstract float getRotZ();

	// Override as required
	public float flyin() {
		return 0;
	}

	public void setY(float f) {
	}

	public float getRotY() {
		return 0;
	}

	public abstract void resumeNow(long dTime);
}
