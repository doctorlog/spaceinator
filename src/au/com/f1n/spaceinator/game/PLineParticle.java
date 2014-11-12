package au.com.f1n.spaceinator.game;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import au.com.f1n.spaceinator.Util;

public class PLineParticle implements PGraphicParticle {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int N_PARTICLES_X = 5;
	public static final int N_PARTICLES_Y = 30;
	private static final int N_PARTICLES = N_PARTICLES_X * N_PARTICLES_Y;
	private float x;
	private float y;

	private float[] colours;
	private float[] particles;
	private transient FloatBuffer particleBuffer;
	private boolean exploding;
	private float exploded = 0;
	public float zRot;

	public PLineParticle(float x, float y, float w, float h) {
		this.x = x;
		this.y = y;

		particles = new float[N_PARTICLES * 3];

		colours = new float[4];
		// inner
		colours[0] = 1;
		colours[1] = 1;
		colours[2] = 1;
		colours[3] = .8f;

		for (int xp = 0; xp < N_PARTICLES_X; xp++) {
			for (int yp = 0; yp < N_PARTICLES_Y; yp++) {
				particles[(xp + yp * N_PARTICLES_X) * 3 + 0] = -w / 2 + w * xp / N_PARTICLES_X;
				particles[(xp + yp * N_PARTICLES_X) * 3 + 1] = -h / 2 + step(yp, h);
			}
		}
	}

	private float step(int yp, float h) {
		return (h * yp / N_PARTICLES_Y / 2 + (float) ((int) (yp * 5 / N_PARTICLES_Y)) * h / 10f) * 1.1f;
	}

	@Override
	public boolean timeStep(int curTimeRate, long timeS) {
		if (exploding) {
			exploded += (float) curTimeRate / 100f;
			colours[3] -= 0.03f;
		} else {

			for (int yp = 0; yp < N_PARTICLES_Y; yp++) {
				float sVal = 200 * (float) Math.sin((double) timeS / 200.0 + yp);
				for (int xp = 0; xp < N_PARTICLES_X; xp++) {
					particles[(xp + yp * N_PARTICLES_X) * 3 + 2] = sVal;
				}
			}

		}
		return colours[3] <= 0;
	}

	@Override
	public int getLastNPoints() {
		return N_PARTICLES;
	}

	@Override
	public float[] colourArray() {
		return colours;
	}

	@Override
	public ByteBuffer colourByte() {
		return null;
	}

	@Override
	public float getWidth() {
		return 4;
	}

	@Override
	public FloatBuffer getFloatBuffer() {
		if (particleBuffer == null)
			particleBuffer = Util.makeFloatBuffer(N_PARTICLES * 3);
		particleBuffer.put(particles);
		particleBuffer.position(0);
		return particleBuffer;
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
	public float getZRot() {
		return zRot;
	}

	@Override
	public float getScale() {
		if (exploding)
			return exploded + .05f;

		return 1;
	}

	public void explode() {
		colours[0] = 0;
		colours[1] = 1;
		colours[2] = 0;
		exploding = true;
		exploded = 0;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}
}
