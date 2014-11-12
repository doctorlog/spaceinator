package au.com.f1n.spaceinator.physics.menu;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Random;

import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;

public class GalaxyCreditParticles implements GalaxyParticles {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int N_PARTICLES = 1000;
	public static final int N_PARTICLE_START = 300;
	private static final long EXPLODE_TIME = 1500;
	public static final String[][] CREDITS = { null, { "SPACEINATOR", "ULTIMATE", "ALIEN", "DESTRUCTION" }, { "PRODUCED BY", "LUKE FINLAY" },
			{ "PROGRAMMER", "LUKE FINLAY" }, { "LEAD TESTER", "ROB MCGRATH" }, { "3D MODELLING", "LUKE FINLAY" },
			{ "TEXTURING", "LUKE FINLAY", "ROB MCGRATH", "GLEN RIDLEY" },
			{ "MUSIC BY", "JASON SHAW", "GOLDEN HITS", "LANGUIS", "HENRY HOMESWEET", "BROKE FOR FREE", "AMBIENTEER", "MOON VEIL" },
			{ "BETA TESTERS", "ROB MCGRATH", "DAVID MALOVKA", "VICTORIA GILLILAND", "REMY KOH" } };

	private float[] particles;
	private FloatBuffer particleBuffer;
	private ByteBuffer colourBuffer;

	private float[] particleStart;

	private long startTime;

	private float proportion;
	private float proportionSlow;

	private float x = 0;// 3500;
	private float y = 0;// -8800;

	private float tracking;
	private float stepI;
	private PMenuCamera camera;

	private MenuWorld menuWorld;

	public GalaxyCreditParticles(PMenuCamera camera, MenuWorld menuWorld) {
		this.camera = camera;
		this.menuWorld = menuWorld;
		particles = new float[N_PARTICLES * 3];
		particleStart = new float[N_PARTICLE_START * 3];

		byte[] colours = new byte[N_PARTICLES * 4];
		colourBuffer = ByteBuffer.allocateDirect(N_PARTICLES * 4);

		Random rand = new Random(69);
		for (int i = 0; i < N_PARTICLES; i++) {
			if (i < N_PARTICLE_START) {
				particleStart[i * 3 + 0] = (float) (rand.nextGaussian() * 120);
				particleStart[i * 3 + 1] = (float) (rand.nextGaussian() * 120);
				particleStart[i * 3 + 2] = (float) (rand.nextGaussian() * 120);

				colours[i * 4 + 0] = (byte) (255);
				colours[i * 4 + 1] = (byte) (255);
				colours[i * 4 + 2] = (byte) (255);
				colours[i * 4 + 3] = (byte) (128);
			} else {
				particles[i * 3 + 0] = funcX(1000 - i) + (float) rand.nextGaussian() * (1 + i / 10);
				particles[i * 3 + 1] = funcY(1000 - i) + (float) rand.nextGaussian() * (1 + i / 10);
				particles[i * 3 + 2] = funcZ(1000 - i) + (float) rand.nextGaussian() * (1 + i / 10);

				float arQty = Math.min(1, (float) i / 1000f);
				colours[i * 4 + 0] = (byte) (255 - arQty * 255);
				colours[i * 4 + 1] = (byte) (255 - arQty * 91);
				colours[i * 4 + 2] = (byte) (255 - arQty * 180);
				colours[i * 4 + 3] = (byte) (128);
			}
		}

		particleBuffer = Util.makeFloatBuffer(particles);
		colourBuffer.put(colours);
		colourBuffer.position(0);
	}

	public float funcZ(float i) {
		return (700 - i) * i * 0.032f;
	}

	public float funcY(float i) {
		return i * 23 - 8500;
	}

	public float funcX(float i) {
		return (i - 600) * i * 0.12f + 3500;
	}

	@Override
	public boolean timeStep(int curTimeRate, long timeS) {
		if (tracking > 0) {
			stepI += 0.025f * curTimeRate;

			float slow = Util.slowInOut(tracking);

			camera.addCredits(slow * funcX(stepI), slow * funcY(stepI), slow * funcZ(stepI), slow * 35, slow * (600 - stepI) * stepI * .00003f);
			if (stepI >= 600) {
				if (!menuWorld.getGameState().achievement_who_did_this_10) {
					menuWorld.getGameState().achievement_who_did_this_10 = menuWorld.getGameState().achievement(R.string.achievement_who_did_this_10, 10);
				}
				camera.setTracking(null);
			}
		} else {
			stepI = 0;
		}

		if (proportion >= 1) {
			// Save some memory
			particles = null;
			return false;
		}

		if (startTime == 0)
			startTime = timeS;

		if (timeS - startTime < EXPLODE_TIME) {
			proportion = (float) (timeS - startTime) / EXPLODE_TIME;
		} else {
			proportion = 1;
		}

		proportionSlow = Util.slowOut(proportion);
		// Move the ball
		float sinS = (float) Math.sin(proportionSlow * 80);
		float cosS = (float) Math.cos(proportionSlow * 80);

		for (int i = 0; i < N_PARTICLE_START; i++) {
			float x = particleStart[i * 3 + 0];
			float y = particleStart[i * 3 + 1];

			particles[i * 3 + 0] = x * cosS - y * sinS + funcX(700 - proportionSlow * 700);
			particles[i * 3 + 1] = x * sinS + y * cosS + funcY(700 - proportionSlow * 700);
			particles[i * 3 + 2] = particleStart[i * 3 + 2] + funcZ(700 - proportionSlow * 700);
		}
		particleBuffer.put(particles, 0, N_PARTICLE_START * 3);
		particleBuffer.position(0);

		return false;
	}

	@Override
	public int getLastNPoints() {
		return (int) (N_PARTICLE_START + (N_PARTICLES - N_PARTICLE_START) * proportionSlow);
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
		return (1 - proportion) * 180;
	}

	@Override
	public float getScale() {
		return 1;
	}

	public float getStarCoord(int starLevel, int coord) {
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
		return 3;
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
		return 0;
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
		return 0.6f;
	}

	public float getStepI() {
		return stepI;
	}

	@Override
	public float getGlowX() {
		return funcX(700 - proportionSlow * 700);
	}

	@Override
	public float getGlowY() {
		return funcY(700 - proportionSlow * 700);
	}

	@Override
	public float getGlowZ() {
		return funcZ(700 - proportionSlow * 700);
	}

	@Override
	public FloatBuffer getTextCoords() {
		return null;
	}

	@Override
	public float getTextX() {
		return 0;
	}

	@Override
	public float getTextY() {
		return 0;
	}

	@Override
	public float getLabelScale() {
		return 0;
	}
}
