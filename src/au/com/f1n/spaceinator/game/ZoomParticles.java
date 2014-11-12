package au.com.f1n.spaceinator.game;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Random;

import au.com.f1n.spaceinator.Util;

public class ZoomParticles implements PGraphicParticle {
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
	private int nParticles;
	private PSpaceShip spaceShip;
	private Random random;

	public ZoomParticles(PSpaceShip spaceShip, int nParticles) {
		this.spaceShip = spaceShip;
		this.nParticles = nParticles;
		particles = new float[nParticles * 3];
		particleD = new float[nParticles * 3];
		colours = new byte[nParticles * 4];
		startTime = new long[nParticles];
		endTime = 2000;

		random = new Random();
	}

	@Override
	public boolean timeStep(int curTimeRate, long timeS) {
		double cosA1 = Math.cos(spaceShip.getFacingAngle() + Math.PI / 2);
		double sinA1 = Math.sin(spaceShip.getFacingAngle() + Math.PI / 2);

		for (int i = 0; i < PShipParticles.ADDERS.length; i++) {
			for (int xShift = -10; xShift <= 10; xShift += 4) {
				// Make new particles!
				startTime[next] = timeS;

				particles[next * 3] = spaceShip.x + (xShift + PShipParticles.ADDERS[i][0]) * (float) cosA1;
				particles[next * 3 + 1] = spaceShip.y + (xShift + PShipParticles.ADDERS[i][0]) * (float) sinA1;
				particles[next * 3 + 2] = spaceShip.z - 100f;

				particleD[next * 3] = (float) random.nextGaussian() / 8;
				particleD[next * 3 + 1] = (float) random.nextGaussian() / 8;
				particleD[next * 3 + 2] = .05f;

				next = (next + 1) % nParticles;
			}
		}

		for (int i = 0; i < particleD.length; i++) {
			particles[i] += particleD[i] * curTimeRate;
		}

		for (int i = 0; i < nParticles; i++) {
			float amount = 255f - 255f * (timeS - startTime[i]) / endTime;
			if (amount < 0) {
				colours[i * 4 + 3] = 0;
			} else {
				colours[i * 4] = (byte) (Math.min(255, amount));
				colours[i * 4 + 1] = (byte) (Math.min(255, amount + 102));
				colours[i * 4 + 2] = (byte) (Math.min(255, amount + 200));
				colours[i * 4 + 3] = (byte) amount;
			}
		}

		return false;
	}

	@Override
	public int getLastNPoints() {
		return nParticles;
	}

	@Override
	public ByteBuffer colourByte() {
		if (colourBuffer == null) {
			colourBuffer = ByteBuffer.allocateDirect(nParticles * 4);
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
			particleBuffer = Util.makeFloatBuffer(nParticles * 3);
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

	@Override
	public void explode() {
	}
}
