package au.com.f1n.spaceinator.game;

import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.enemy.PEnemy;

public abstract class PWeaponParticle extends PObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static byte[] SHIP_COL = { (byte) 255, (byte) 135, (byte) 0, (byte) 255 };
	public static byte[] BURST_COL = { (byte) 204, (byte) 255, (byte) 0, (byte) 255 };
	public static byte[] TURRET_COL = { (byte) 100, (byte) 255, (byte) 0, (byte) 255 };
	public static byte[] ENEMY_COL = { (byte) 255, (byte) 0, (byte) 255, (byte) 255 };
	public static byte[] FLASH_COL = { (byte) 255, (byte) 255, (byte) 255, (byte) 255 };

	public static byte[][] ALL_COL = { SHIP_COL, BURST_COL, TURRET_COL, ENEMY_COL, FLASH_COL };

	public float lastx;
	public float lasty;
	protected float shootAngle;
	public static final int DRAW_ORDER = PEnemy.CLASS_COUNT;

	public PWeaponParticle(float x, float y, float radius) {
		super(x, y, radius);
		drawOrder = DRAW_ORDER;
	}

	public abstract int getColour();

	public abstract void remove();

	public float getAngleDegrees() {
		return shootAngle * Util.RAD_TO_DEG;
	}

	public abstract float getScale();
}
