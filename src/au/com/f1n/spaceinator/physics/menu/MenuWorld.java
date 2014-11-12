package au.com.f1n.spaceinator.physics.menu;

import java.util.ArrayList;
import java.util.ListIterator;

import android.os.Vibrator;
import au.com.f1n.spaceinator.GBRenderer;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.game.FArrayList;
import au.com.f1n.spaceinator.game.PEdge;
import au.com.f1n.spaceinator.game.PGraphicParticle;
import au.com.f1n.spaceinator.game.PObject;
import au.com.f1n.spaceinator.game.PParticleExplosion;
import au.com.f1n.spaceinator.game.World;
import au.com.f1n.spaceinator.game.enemy.PEnemy;
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.mesh.intro.IntroSequence;

/**
 * The main menu as a world - where a cool galaxy is displayed and the user can
 * select which level to load.
 * 
 * @author luke
 */
public class MenuWorld extends World {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int N_GALAXY = 4;
	private transient GalaxyParticles[] galaxyParticles;

	public static final byte[] COLOUR_BASE = { (byte) 70, (byte) 147, (byte) 193, (byte) 255 };
	public static final float[] COLOUR_BASE_F = { .27451f, .57647f, .75686f, 1 };
	public static final float[] COLOUR_WIN = { .85f, .87f, 0, 1 };
	public static final float[] COLOUR_GREEN = { 0.69804f, 0.79608f, 0.22353f, 1 };
	public static final byte[] COLOUR_WIN_1 = { (byte) 218, (byte) 222, (byte) 0, (byte) 255 };
	public static final byte[] COLOUR_WIN_2 = { (byte) 104, (byte) 106, (byte) 0, (byte) 255 };
	public static final byte[] COLOUR_UNAVAILABLE = { (byte) 70, (byte) 147, (byte) 193, (byte) 255 };
	public static final float[] COLOUR_UNAVAILABLE_F = { .27451f, .57647f, .75686f, 1 };
	public static final byte[] COLOUR_AVAILABLE = { (byte) 70, (byte) 147, (byte) 193, (byte) 255 };
	public static final float[] COLOUR_AVAILABLE_F = { .666666f, .9333333f, 1, 1 };
	public static final byte[] COLOUR_BLACK_TRANS = { 0, 0, 0, 0 };
	public static final float[] COLOUR_GREY_F = { .5f, .5f, .5f, 1 };
	public static final float[] COLOUR_RED = { .82f, .04f, .01f, 1 };

	public void init(GBRenderer gbRenderer, Vibrator v) {
		super.init(gbRenderer, v);
	}

	public void start(GameState gameState) {
		// This is the only world that can have a null gamestate
		super.start(gameState);

		// Play menu music only for the menuworld
		gbRenderer.getSoundManager().playMusic(R.raw.music_menu);

		// Have the objects array with nothing in it
		objects = new FArrayList<PObject>(1);
		edgesX = new FArrayList<PEdge>(1);

		// This overrides the "fly in" sequence
		zooming = 0;
		graphicParticles = new ArrayList<PGraphicParticle>(N_GALAXY);
		galaxyParticles = new GalaxyParticles[N_GALAXY];

		camera = new PMenuCamera(this);

		galaxyParticles[0] = new Galaxy0Particles();
		galaxyParticles[1] = new Galaxy1Particles();
		galaxyParticles[2] = new Galaxy2Particles();
		galaxyParticles[3] = new GalaxyCreditParticles((PMenuCamera) camera, this);
		for (int i = 0; i < N_GALAXY; i++)
			graphicParticles.add(galaxyParticles[i]);

	}

	public int timeStep() {
		long timeS = System.currentTimeMillis();
		int dTime = (int) (timeS - lastTime);
		if (startTime == 0) {
			dTime = 1;
			startTime = lastTime = timeS;
		}
		lastTime = timeS;

		camera.timeStep(dTime);

		ListIterator<PGraphicParticle> graphicalIter = graphicParticles.listIterator();
		while (graphicalIter.hasNext()) {
			PGraphicParticle pgp = graphicalIter.next();
			if (pgp.timeStep(dTime, timeS)) {
				if (pgp instanceof PParticleExplosion && pgp.getLastNPoints() == PParticleExplosion.BASE_PARTICLE_N)
					leftoverExplosions.add((PParticleExplosion) pgp);
				graphicalIter.remove();
			}
		}
		return dTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public void clickedStar(int starID) {
		Class<? extends World> clazz = STARS[starID].getWorldClass();
		gbRenderer.loadWorld(clazz);
	}

	@Override
	public int getNebulaID() {
		return R.drawable.nebula2;
	}

	@Override
	public int getStarIndex() {
		return 0;
	}

	@Override
	public void laserKilled(PEnemy otherEnemy) {
		// NA
	}

	public GalaxyParticles[] getGalaxyParticles() {
		return galaxyParticles;
	}

	@Override
	public IntroSequence getIntroSequence() {
		return new IntroSequence();
	}

	@Override
	public int getLeaderBoardID() {
		// NA
		return 0;
	}
}
