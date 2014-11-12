package au.com.f1n.spaceinator.physics.menu;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Random;

import au.com.f1n.spaceinator.Util;

public class Galaxy2Particles implements GalaxyParticles {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int N_PARTICLES = 3000;
	private static final long EXPLODE_TIME = 1500;

	private float[] particles;
	private FloatBuffer particleBuffer;
	private ByteBuffer colourBuffer;

	private long startTime;

	private float proportion;

	private float x = 5000;
	private float y = 3000;

	private float tracking;
	private FloatBuffer texCoords;

	public Galaxy2Particles() {
		particles = new float[N_PARTICLES * 3];

		byte[] colours = new byte[N_PARTICLES * 4];
		colourBuffer = ByteBuffer.allocateDirect(N_PARTICLES * 4);

		Random rand = new Random(234);
		for (int i = 0; i < N_PARTICLES; i++) {
			float x = (float) (rand.nextGaussian() * 1300);
			float y = (float) (rand.nextGaussian() * 1300);
			float r = (float) Math.sqrt(x * x + y * y);
			particles[i * 3 + 0] = x;
			particles[i * 3 + 1] = y;
			particles[i * 3 + 2] = (float) (rand.nextGaussian() * 100 / (r / 1300));

			if (Math.abs(particles[i * 3 + 2]) > 2000)
				particles[i * 3 + 2] = 0;

			if (r < 500) {
				// inner
				colours[i * 4 + 0] = (byte) (255);
				colours[i * 4 + 1] = (byte) (255);
				colours[i * 4 + 2] = (byte) (255);
				colours[i * 4 + 3] = (byte) (128);
			} else {
				float arQty = Math.min(1, r / 500);
				colours[i * 4 + 0] = (byte) (220);
				colours[i * 4 + 1] = (byte) (91 - arQty * 91);
				colours[i * 4 + 2] = (byte) (103 - arQty * 103);
				colours[i * 4 + 3] = (byte) (128);
			}
		}

		particleBuffer = Util.makeFloatBuffer(particles);
		colourBuffer.put(colours);
		colourBuffer.position(0);

		texCoords = Util.makeFloatBuffer(4 * 2);
		texCoords.put(0);
		texCoords.put(.55f);

		texCoords.put(1);
		texCoords.put(.55f);

		texCoords.put(1);
		texCoords.put(.36f);

		texCoords.put(0);
		texCoords.put(.36f);

		texCoords.position(0);
	}

	@Override
	public boolean timeStep(int curTimeRate, long timeS) {
		if (proportion >= 1)
			return false;

		if (startTime == 0)
			startTime = timeS;

		if (timeS - startTime < EXPLODE_TIME)
			proportion = (float) (timeS - startTime) / EXPLODE_TIME;
		else
			proportion = 1;

		return false;
	}

	@Override
	public int getLastNPoints() {
		return N_PARTICLES;
	}

	@Override
	public float[] colourArray() {
		return null;
	}

	@Override
	public ByteBuffer colourByte() {
		return colourBuffer;
	}

	@Override
	public float getWidth() {
		return 2 + tracking * 3;
	}

	@Override
	public FloatBuffer getFloatBuffer() {
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
		return 0;
	}

	@Override
	public float getScale() {
		return Util.slowInOut(proportion);
	}

	public float getStarCoord(int starLevel, int coord) {
		// If memory is a problem, these could be cached (I doubt it will be since
		// this is only for the main menu)
		int i = MenuWorld.STARS[starLevel].getStarIndex();
		float x = particles[i * 3 + 0];
		float y = particles[i * 3 + 1];
		float z = particles[i * 3 + 2];

		switch (coord) {
		case 0:
			return x;
		case 1:
			return y;
		case 2:
			return z;
		}

		return 0;
	}

	@Override
	public void explode() {
	}

	@Override
	public float getProportion() {
		return proportion;
	}

	@Override
	public void setX(float x) {
		this.x = x;
	}

	@Override
	public void setY(float y) {
		this.y = y;
	}

	@Override
	public boolean hasExploded() {
		return proportion > .05f;
	}

	@Override
	public int getIndex() {
		return 2;
	}

	@Override
	public float getTargetY() {
		return y * .5f;
	}

	@Override
	public void setTracking(float tracking) {
		this.tracking = tracking;
	}

	@Override
	public float getYRot() {
		return Util.slowInOut(1 - tracking) * 59;
	}

	@Override
	public float getTracking() {
		return tracking;
	}

	@Override
	public float getTextScale() {
		return 200;
	}

	@Override
	public float getGlowSize() {
		return 3 * Util.slowOut(proportion);
	}

	@Override
	public float getGlowX() {
		return x;
	}

	@Override
	public float getGlowY() {
		return y;
	}

	@Override
	public float getGlowZ() {
		return 0;
	}

	@Override
	public FloatBuffer getTextCoords() {
		return texCoords;
	}

	@Override
	public float getTextX() {
		return x - 1000 + 1000 * Util.slowInOut(tracking);
	}

	@Override
	public float getTextY() {
		return y - 3000 - Util.slowInOut(tracking) * 500;
	}

	@Override
	public float getLabelScale() {
		return Util.slowInOut(proportion) * .2f + .5f;
	}
}
