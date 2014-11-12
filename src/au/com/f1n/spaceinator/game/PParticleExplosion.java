package au.com.f1n.spaceinator.game;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import au.com.f1n.spaceinator.Util;

/**
 * This is a weapon/kill effect
 * 
 * @author luke
 * 
 */
public class PParticleExplosion implements PGraphicParticle {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int COLOUR_HOT = 0;
	public static final int COLOUR_RED = 1;
	public static final int COLOUR_BROWN = 2;
	public static final int COLOUR_BLUE = 3;
	/**
	 * Most effects use this number of particles. If the effect DOES use 100
	 * particles
	 */
	public static final int BASE_PARTICLE_N = 47;
	public static final int BASE_PARTICLE_NUM = 211;

	private static final float[] BASE_EXPLOSION = new float[BASE_PARTICLE_NUM * 3];

	static {
		for (int i = 0; i < BASE_PARTICLE_NUM; i++) {
			double angle = Math.random() * Math.PI * 2;
			double rad = Util.slowOut((float) Math.random());
			BASE_EXPLOSION[i * 3] = (float) (Math.cos(angle) * rad);
			BASE_EXPLOSION[i * 3 + 1] = (float) (Math.sin(angle) * rad);
			// The amount of movement taken is more at the edges
			BASE_EXPLOSION[i * 3 + 2] = (float) (rad * .8 + Math.random() * .2);
		}
	}

	private float[] particles;
	private transient FloatBuffer particleBuffer;
	private float[] particleD;
	private float[] colour;
	private long startTime;
	private long timeRun;
	private int nParticles;
	private float dampner;
	private int colourType;
	private static int curExp;

	public PParticleExplosion(float xOrigin, float yOrigin, float dx, float dy, float maxVelocity, long startTime, long exploisionTime, int nParticles,
			float initRadius, float dampner, int colourType) {
		this(nParticles);
		setTo(xOrigin, yOrigin, dx, dy, maxVelocity, startTime, exploisionTime, initRadius, dampner, colourType);
	}

	/**
	 * Create the explosion but dont set it to anything in particular
	 */
	public PParticleExplosion(int nParticles) {
		this.nParticles = nParticles;
		particles = new float[nParticles * 3];
		particleD = new float[nParticles * 2];
		colour = new float[4];
	}

	public void setTo(float xOrigin, float yOrigin, float dx, float dy, float maxVelocity, long startTime, long exploisionTime, float initRadius, float dampner,
			int colourType) {
		this.dampner = dampner;
		this.colourType = colourType;

		if (nParticles == BASE_PARTICLE_N) {
			// Cheap way of using cached data
			initRadius *= 1.1f;

			for (int i = 0; i < nParticles; i++) {
				particles[i * 3] = xOrigin + BASE_EXPLOSION[curExp * 3] * initRadius;
				particles[i * 3 + 1] = yOrigin + BASE_EXPLOSION[curExp * 3 + 1] * initRadius;

				float dmov = BASE_EXPLOSION[curExp * 3 + 2];
				particleD[i * 2] = dx * dmov + maxVelocity * BASE_EXPLOSION[curExp * 3];
				particleD[i * 2 + 1] = dy * dmov + maxVelocity * BASE_EXPLOSION[curExp * 3 + 1];
				curExp = (curExp + 1) % BASE_PARTICLE_NUM;
			}
		} else {
			double angle;
			for (int i = 0; i < nParticles; i++) {

				angle = Util.randFloat() * 2 * Math.PI;
				double rad = Util.randFloat() * initRadius;
				particles[i * 3] = (float) (xOrigin + Math.cos(angle) * rad);
				particles[i * 3 + 1] = (float) (yOrigin + Math.sin(angle) * rad);

				double vel = Util.randFloat() * maxVelocity;

				particleD[i * 2] = (float) (dx * maxVelocity + vel * (Util.randFloat() - .5));
				particleD[i * 2 + 1] = (float) (dy * maxVelocity + vel * (Util.randFloat() - .5));
			}
		}

		this.timeRun = (long) (exploisionTime);
		this.startTime = startTime;
	}

	public boolean timeStep(int curTimeRate, long timeS) {
		for (int i = 0; i < nParticles; i++) {
			particles[i * 3] += particleD[i * 2];
			particles[i * 3 + 1] += particleD[i * 2 + 1];
			particleD[i * 2] *= dampner;
			particleD[i * 2 + 1] *= dampner;
		}

		float amount = 1f - 1f * (timeS - startTime) / timeRun;

		if (amount < 0)
			amount = 0;

		switch (colourType) {
		case COLOUR_HOT:
			colour[0] = Math.min(1f, amount + .7f);
			colour[1] = Math.min(1f, amount + .3f);
			colour[2] = Math.min(1f, amount);
			colour[3] = amount;
			break;
		case COLOUR_RED:
			colour[0] = 1;
			colour[1] = 0;
			colour[2] += Util.randFloat() * .001;
			colour[2] = Math.min(Math.max(0, colour[2]), 1);
			colour[3] = amount;
			break;
		case COLOUR_BROWN:
			float amountm1 = 1 - amount;
			colour[0] = amountm1 * .148f + amount;
			colour[1] = amountm1 * .144f + amount;
			colour[2] = amountm1 * .009f + amount;
			colour[3] = amount;
			break;
		case COLOUR_BLUE:
			colour[0] = amount / 10;
			colour[1] = amount / 10;
			colour[2] = amount;
			colour[3] = amount;
		default:
			break;
		}

		return timeS - startTime > timeRun;
	}

	public int getLastNPoints() {
		return nParticles;
	}

	public boolean isLine() {
		return false;
	}

	public float[] colourArray() {
		return colour;
	}

	public float getWidth() {
		return 1;
	}

	public FloatBuffer getFloatBuffer() {
		if (particleBuffer == null)
			particleBuffer = Util.makeFloatBuffer(nParticles * 3);

		particleBuffer.put(particles);
		particleBuffer.position(0);
		return particleBuffer;
	}

	@Override
	public ByteBuffer colourByte() {
		return null;
	}

	@Override
	public float getX() {
		return 0;
	}

	@Override
	public float getY() {
		return 0;
	}

	@Override
	public float getZRot() {
		return 0;
	}

	@Override
	public float getScale() {
		return 1;
	}

	@Override
	public void explode() {
	}
}
