package au.com.f1n.spaceinator.game;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import au.com.f1n.spaceinator.Util;

public class SpeedBoost implements PGraphicParticle {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int N = 32;
	private static final int NM1 = N - 1;
	private static final int N2 = N / 2;
	private static final float RADIUS = PSpaceShip.SHIELD_SIZE * 1.618f;
	private float x;
	private float y;
	private float radius;
	private float addx, addy;
	private World world;
	private static FloatBuffer vertices;
	private static float[] verArray;
	private float angle;
	private static float adder;
	private static long lastUpdate;
	private float[] col = { 1, 1, 1, 1 };
	private long lastSound;

	static {
		vertices = Util.makeFloatBuffer(N * 3);
		verArray = new float[N * 3];
	}
	
	public SpeedBoost(World world, float x, float y, float angle) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.radius = RADIUS;
		this.addx = (float) Math.cos(angle);
		this.addy = (float) Math.sin(angle);
		this.angle = angle * 180 / (float) Math.PI;
	}

	@Override
	public boolean timeStep(int curTimeRate, long timeS) {
		float dx = world.centreSpaceShip.x - x;
		float dy = world.centreSpaceShip.y - y;
		float d2 = dx * dx + dy * dy;
		if (d2 < radius * radius) {
			if (lastSound < timeS + 1000) {
				world.gbRenderer.getSoundManager().beepMenu(2);
				lastSound = timeS;
			}
			world.centreSpaceShip.dx += addx / 2;
			world.centreSpaceShip.dy += addy / 2;
			col[3] = 1;
		} else {
			col[3] = radius * Util.invSqrt(d2);
			if (col[3] < .3f)
				col[3] = .3f;
		}

		updateStaticVertices(curTimeRate, timeS);

		return false;
	}

	private static void updateStaticVertices(int curTimeRate, long timeS) {
		if (lastUpdate == timeS)
			return;
		lastUpdate = timeS;

		adder += .0001f * curTimeRate;
		if (adder > 1f / N)
			adder = 0;

		for (int i = 0; i < N2; i++) {
			float t = (float) i / NM1 + adder - .5f / N;
			verArray[i * 3 + 0] = (8 * t * t - 1) * RADIUS;
			verArray[i * 3 + 1] = 4 * (0.25f - Math.abs(t - 0.25f)) * RADIUS;
		}
		for (int i = N2; i < N; i++) {
			float t = (float) (i - N2) / NM1 + adder - .5f / N;
			verArray[i * 3 + 0] = (8 * t * t - 1) * RADIUS;
			verArray[i * 3 + 1] = -4 * (0.25f - Math.abs(t - 0.25f)) * RADIUS;
		}
		vertices.put(verArray);
		vertices.position(0);
	}

	@Override
	public int getLastNPoints() {
		return N;
	}

	@Override
	public float[] colourArray() {
		return col;
	}

	@Override
	public ByteBuffer colourByte() {
		return null;
	}

	@Override
	public float getWidth() {
		return 3;
	}

	@Override
	public FloatBuffer getFloatBuffer() {
		return vertices;
	}

	@Override
	public float getX() {
		return x;
	}

	@Override
	public float getY() {
		return y;
	}

	@Override
	public float getScale() {
		return 1;
	}

	@Override
	public float getZRot() {
		return angle;
	}

	@Override
	public void explode() {
	}
}
