package au.com.f1n.spaceinator.physics.menu;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Random;

import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.World;

public class Galaxy0Particles implements GalaxyParticles {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int N_PARTICLES = 3000;

	private static final long EXPLODE_TIME = 2700;

	private float[] particleBase;
	private float[] particles;
	private float[] particleRadius;
	private float[] adder;
	private FloatBuffer particleBuffer;
	private ByteBuffer colourBuffer;

	private long startTime;

	private float proportion;

	private float x = -4000;
	private float y = 6000;
	private float tracking;

	private FloatBuffer texCoords;

	public Galaxy0Particles() {
		particles = new float[N_PARTICLES * 3];
		particleBase = new float[N_PARTICLES * 2];
		adder = new float[N_PARTICLES * 2];
		particleRadius = new float[N_PARTICLES];
		particleBuffer = Util.makeFloatBuffer(N_PARTICLES * 3);

		byte[] colours = new byte[N_PARTICLES * 4];
		colourBuffer = ByteBuffer.allocateDirect(N_PARTICLES * 4);

		Random rand = new Random(234);
		for (int i = 0; i < N_PARTICLES; i++) {
			float x = (float) (rand.nextGaussian() * 2500);
			float y = (float) (rand.nextGaussian() * 100);
			float r = (float) Math.sqrt(x * x + y * y);
			particleBase[i * 2 + 0] = x;
			particleBase[i * 2 + 1] = y;
			particles[i * 3 + 2] = (float) (150 * rand.nextGaussian() * Math.cos(r / 2500));

			adder[i * 2 + 0] = (float) rand.nextGaussian() * 20;
			adder[i * 2 + 1] = (float) rand.nextGaussian() * 20;
			particleRadius[i] = r;

			// Colours:
			// 0-500:
			// 255,2555,255
			// to 2000
			// 167,134,136
			// to 5000
			// 115,91,103
			// to 12000
			// 64,64,88

			// bonus:105,95,147

			if (r < 500) {
				// inner
				colours[i * 4 + 0] = (byte) (255);
				colours[i * 4 + 1] = (byte) (255);
				colours[i * 4 + 2] = (byte) (255);
				colours[i * 4 + 3] = (byte) (128);
			} else if (r < 800) {
				float arQty = (r - 500) / 300;
				colours[i * 4 + 0] = (byte) (255 - arQty * 88);
				colours[i * 4 + 1] = (byte) (255 - arQty * 121);
				colours[i * 4 + 2] = (byte) (255 - arQty * 119);
				colours[i * 4 + 3] = (byte) (128);
			} else if (r < 1000) {
				float arQty = (r - 800) / 200;
				colours[i * 4 + 0] = (byte) (167 - arQty * 52);
				colours[i * 4 + 1] = (byte) (134 - arQty * 43);
				colours[i * 4 + 2] = (byte) (136 - arQty * 33);
				colours[i * 4 + 3] = (byte) (128);
			} else {
				float arQty = Math.min(1, (r - 1000) / 1000);
				colours[i * 4 + 0] = (byte) (115 - arQty * 51);
				colours[i * 4 + 1] = (byte) (91 - arQty * 27);
				colours[i * 4 + 2] = (byte) (103 - arQty * 15);
				colours[i * 4 + 3] = (byte) (128);
			}
		}

		colourBuffer.put(colours);
		colourBuffer.position(0);

		texCoords = Util.makeFloatBuffer(4 * 2);
		texCoords.put(0);
		texCoords.put(.25781f);

		texCoords.put(1);
		texCoords.put(.25781f);

		texCoords.put(1);
		texCoords.put(0);

		texCoords.put(0);
		texCoords.put(0);

		texCoords.position(0);
	}

	@Override
	public boolean timeStep(int curTimeRate, long timeS) {
		if (particleBase == null)
			return false;

		if (startTime == 0)
			startTime = timeS;

		if (timeS - startTime < EXPLODE_TIME)
			proportion = (float) (timeS - startTime) / EXPLODE_TIME;
		else
			proportion = 1;

		float timeSpeed = Util.slowOut(proportion);
		for (int i = 0; i < N_PARTICLES; i++) {
			float x = particleBase[i * 2 + 0];
			float y = particleBase[i * 2 + 1];
			float r = particleRadius[i];
			float theta = Util.slowInOut(proportion) * -4000 / r;

			float cosT = (float) Math.cos(theta);
			float sinT = (float) Math.sin(theta);

			particles[i * 3 + 0] = (cosT * x - sinT * y) * timeSpeed + adder[i * 2 + 0];
			particles[i * 3 + 1] = (sinT * x + cosT * y) * timeSpeed + adder[i * 2 + 1];
		}

		if (proportion >= 1) {
			particleBase = null;
			adder = null;
			particleRadius = null;
		}

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
		return 1 + tracking * 2;
	}

	@Override
	public FloatBuffer getFloatBuffer() {
		if (particles != null) {
			particleBuffer.put(particles);
			particleBuffer.position(0);
		}
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
		int i = World.STARS[starLevel].getStarIndex();
		return particles[i * 3 + coord];
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
		return proportion > .25f;
	}

	@Override
	public int getIndex() {
		return 0;
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
		return Util.slowInOut(1 - tracking) * -14;
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
		return Util.slowOut(proportion) * 1.5f;
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
		return x - Util.slowInOut(tracking) * 3000;
	}

	@Override
	public float getTextY() {
		return y + 3500 - Util.slowInOut(tracking) * 6000;
	}

	@Override
	public float getLabelScale() {
		return proportion * .2f + .8f;
	}
}
