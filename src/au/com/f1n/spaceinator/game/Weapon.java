package au.com.f1n.spaceinator.game;

import java.io.Serializable;

public abstract class Weapon implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected World world;

	public Weapon(World world) {
		this.world = world;
	}

	public abstract void shooting(long time, float angle, float dx, float dy);

	public abstract void setBaseDamage(int damage);
}
