package au.com.f1n.spaceinator.game;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import au.com.f1n.spaceinator.Util;

/**
 * This is a weapon/kill effect, not really a "weapon" particle.
 * 
 * @author luke
 * 
 */
public class PShipParticles implements PGraphicParticle {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private float[] particles;
	private transient FloatBuffer particleBuffer;
	private float[] particleD;
	private byte[] colours;
	private transient ByteBuffer colourBuffer;
	private long[] startTime;
	private long endTime;
	private int next;
	public static float ADDERS[][] = { { 83, -110, 0 }, { -83, -110, 0 } };

	private final static int N_PARTICLES = 300;
	private static final float ROT[][] = new float[2][8];
	private PSpaceShip spaceShip;
	private float accel;
	private int level;
	private boolean boost;

	public PShipParticles() {
		particles = new float[N_PARTICLES * 3];
		particleD = new float[N_PARTICLES * 3];
		colours = new byte[N_PARTICLES * 4];
		startTime = new long[N_PARTICLES];
		endTime = 800;

		for (int j = 0; j < ROT[0].length; j++) {
			double t = (double) j / ROT[0].length * Math.PI * 2;
			ROT[0][j] = (float) Math.cos(t) * 10;
			ROT[1][j] = (float) Math.sin(t) * 10;
		}
	}

	public PShipParticles(PSpaceShip spaceShip, int level) {
		this();
		this.spaceShip = spaceShip;
		setAccel(level);
	}

	@Override
	public boolean timeStep(int dTime, long timeS) {

		particleStep(timeS, dTime, spaceShip.getAccelX(), spaceShip.getAccelY(), spaceShip.x, spaceShip.y, spaceShip.z, spaceShip.dx, spaceShip.dy,
				spaceShip.isAccelerating());
		Util.randFloat();
		// This particle dies with the ship
		return spaceShip.curShield <= 0;
	}

	public void particleStep(long timeS, int dTime, float cosA1, float sinA1, float x, float y, float z, float dx, float dy, boolean newParticles) {
		if (newParticles) {
			if (spaceShip != null && spaceShip.facingIn) {
				for (int j = 0; j < 8; j++) {
					float xShift = -70 + j * 20;
					float zShift = 0;

					// Make new particles!
					startTime[next] = timeS - 400 + level * 100;

					float power = Util.randFloat() + accel;
					particles[next * 3] = cosA1 * (+power) - sinA1 * (+xShift) + x;
					particles[next * 3 + 1] = sinA1 * (+power) + cosA1 * (+xShift) + y;
					particles[next * 3 + 2] = 0 + z + zShift;

					particleD[next * 3] = dx - power * cosA1 - (accel * Util.randFloat() - accel / 2) * sinA1;
					particleD[next * 3 + 1] = dy - power * sinA1 + (accel * Util.randFloat() - accel / 2) * cosA1;
					particleD[next * 3 + 2] = 0;

					next = (next + 1) % N_PARTICLES;

				}
			} else {
				for (int j = 0; j < ROT[0].length; j++) {
					float xShift = ROT[0][j];
					float zShift = ROT[1][j];

					for (int i = 0; i < ADDERS.length; i++) {
						// Make new particles!
						startTime[next] = timeS - 400 + level * 100;

						float power = (float) (Util.randFloat() + accel);
						particles[next * 3] = cosA1 * (ADDERS[i][1] + power) - sinA1 * (ADDERS[i][0] + xShift) + x;
						particles[next * 3 + 1] = sinA1 * (ADDERS[i][1] + power) + cosA1 * (ADDERS[i][0] + xShift) + y;
						particles[next * 3 + 2] = ADDERS[i][2] + z + zShift;

						particleD[next * 3] = (float) (dx - power * cosA1 - (accel * Util.randFloat() - accel / 2) * sinA1);
						particleD[next * 3 + 1] = (float) (dy - power * sinA1 + (accel * Util.randFloat() - accel / 2) * cosA1);
						particleD[next * 3 + 2] = 0;

						next = (next + 1) % N_PARTICLES;
					}
				}
			}
		}

		for (int i = 0; i < particleD.length; i++) {
			particles[i] += particleD[i] * dTime;
			particleD[i] *= .96f;
		}

		for (int i = 0; i < N_PARTICLES; i++) {
			float amount = 255f - 255f * (timeS - startTime[i]) / endTime;
			byte aB = (byte) amount;
			if (amount <= 0) {
				colours[i * 4 + 3] = 0;
			} else if (boost) {
				colours[i * 4] = (byte) (Math.min(255, amount + 200));
				colours[i * 4 + 1] = (byte) (Math.min(255, amount + 102));
				colours[i * 4 + 2] = aB;
				colours[i * 4 + 3] = aB;
			} else {
				colours[i * 4] = aB;
				float tmp = amount + 102;
				colours[i * 4 + 1] = tmp > 255 ? (byte) 255 : (byte) tmp;
				tmp = amount + 200;
				// colours[i * 4 + 2] = (byte) (Math.min(255, amount + 200));
				colours[i * 4 + 2] = tmp > 255 ? (byte) 255 : (byte) tmp;
				colours[i * 4 + 3] = aB;
			}
		}
	}

	@Override
	public int getLastNPoints() {
		return N_PARTICLES;
	}

	@Override
	public ByteBuffer colourByte() {
		if (colourBuffer == null) {
			colourBuffer = ByteBuffer.allocateDirect(N_PARTICLES * 4);
			colourBuffer.position(0);
		}

		colourBuffer.put(colours);
		colourBuffer.position(0);
		return colourBuffer;
	}

	@Override
	public float getWidth() {
		return 2;
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
	public float[] colourArray() {
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
	public float getScale() {
		return 1;
	}

	@Override
	public float getZRot() {
		return 0;
	}

	public void setAccel(int level) {
		this.level = level;
		this.accel = .2f + level * .1f;
	}

	@Override
	public void explode() {
	}

	public void setBoost(boolean boost) {
		this.boost = boost;
	}
}
