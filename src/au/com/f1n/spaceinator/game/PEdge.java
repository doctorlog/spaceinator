package au.com.f1n.spaceinator.game;

import java.io.Serializable;

/**
 * The left or right edge of a PObject (with a reference to the object)
 * 
 * @author luke
 */
public class PEdge implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public float x;
	public boolean isStart;
	public PObject object;

	public PEdge(boolean isStart, PObject object) {
		super();
		this.isStart = isStart;
		this.object = object;
	}

	public void reCalcVal() {
		if (object instanceof PWeaponParticle) {
			x = isStart ? Math.min(object.x, ((PWeaponParticle) object).lastx) : Math.max(object.x, ((PWeaponParticle) object).lastx);
		} else {
			x = object.x + (isStart ? -object.radius : object.radius);
		}
	}
}
