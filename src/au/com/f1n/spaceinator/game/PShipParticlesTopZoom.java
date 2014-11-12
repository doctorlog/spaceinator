package au.com.f1n.spaceinator.game;

import java.util.Random;

public class PShipParticlesTopZoom extends PShipParticlesTop {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Random random = new Random();

	public PShipParticlesTopZoom(PSpaceShip spaceShip) {
		super(spaceShip, 2000);
		endTime = 600;
	}

	public boolean timeStep(int curTimeRate, long timeS) {
		double cosA1 = Math.cos(spaceShip.getFacingAngle() + Math.PI / 2);
		double sinA1 = Math.sin(spaceShip.getFacingAngle() + Math.PI / 2);

		for (int i = 0; i < PShipParticles.ADDERS.length; i++) {
			for (int xShift = -10; xShift <= 10; xShift += 5) {
				// Make new particles!
				startTime[next] = timeS;

				particles[next * 3] = spaceShip.x + (xShift + PShipParticles.ADDERS[i][0]) * (float) cosA1;
				particles[next * 3 + 1] = spaceShip.y + (xShift + PShipParticles.ADDERS[i][0]) * (float) sinA1;
				particles[next * 3 + 2] = spaceShip.z + 144f;

				particleD[next * 3] = (float) random.nextGaussian() / 32 + spaceShip.dx;
				particleD[next * 3 + 1] = (float) random.nextGaussian() / 32 + spaceShip.dy;
				particleD[next * 3 + 2] = 10;

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

		// This particle dies with the ship
		return spaceShip.curShield <= 0;
	}

	public void puff(float lasX, float lasY, long time, byte[] colour, float shootAngle, float dx, float dy, boolean green) {
		// NA
	}
}
