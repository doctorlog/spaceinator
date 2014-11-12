package au.com.f1n.spaceinator.game;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import au.com.f1n.spaceinator.Util;

/**
 * This is a the fluff on top of the ship (very subtle effect)
 * 
 * @author luke
 * 
 */
public class PShipParticlesTop implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected float[] particles;
	protected transient FloatBuffer particleBuffer;
	protected float[] particleD;
	protected byte[] colours;
	protected transient ByteBuffer colourBuffer;
	protected long[] startTime;
	protected long endTime;
	protected int next;

	protected int nParticles;
	protected PSpaceShip spaceShip;

	public PShipParticlesTop(PSpaceShip spaceShip, int nParticles) {
		this.spaceShip = spaceShip;
		this.nParticles = nParticles;
		particles = new float[nParticles * 3];
		particleD = new float[nParticles * 3];
		colours = new byte[nParticles * 4];
		startTime = new long[nParticles];
		endTime = 400;
	}

	public boolean timeStep(int curTimeRate, long timeS) {
		for (int i = 0; i < particleD.length; i++) {
			particles[i] += particleD[i] * curTimeRate;
			particleD[i] *= .9f;
		}

		for (int i = 0; i < nParticles; i++) {
			float amount = 255f - 255f * (timeS - startTime[i]) / endTime;
			if (amount <= 0) {
				colours[i * 4 + 3] = 0;
			} else {
				colours[i * 4 + 3] = (byte) amount;
			}
		}

		// This particle dies with the ship
		return spaceShip.curShield <= 0;
	}

	public int getLastNPoints() {
		return nParticles;
	}

	public ByteBuffer colourByte() {
		if (colourBuffer == null) {
			colourBuffer = ByteBuffer.allocateDirect(nParticles * 4);
			colourBuffer.position(0);
		}
		colourBuffer.put(colours);
		colourBuffer.position(0);

		return colourBuffer;
	}

	public FloatBuffer getFloatBuffer() {
		if (particleBuffer == null) {
			particleBuffer = Util.makeFloatBuffer(nParticles * 3);
			particleBuffer.position(0);
		}

		particleBuffer.put(particles);
		particleBuffer.position(0);
		return particleBuffer;
	}

	public void puff(float lasX, float lasY, long time, byte[] colour, float shootAngle, float dx, float dy, boolean green) {
		float sindx = (float) Math.sin(shootAngle);
		float cosdx = (float) Math.cos(shootAngle);

		for (int i = 0; i < 10; i++) {
			// Make new particles!
			startTime[next] = time;

			// double angleAdd = (float) i / 20 - .5f;
			float power = Util.randFloat() * .8f + .1f;
			particles[next * 3] = lasX;
			particles[next * 3 + 1] = lasY;
			// particles[next * 3 + 2] = 0;

			particleD[next * 3] = cosdx * power + dx + Util.randFloat() * .15f;
			particleD[next * 3 + 1] = sindx * power + dy + Util.randFloat() * .15f;
			// particleD[next * 3 + 2] = 0;

			colours[next * 4] = green ? 0 : (byte) (255);
			colours[next * 4 + 1] = (byte) (255);
			// colours[next * 4 + 2] = 0;

			next = (next + 1) % nParticles;
		}
	}
}
