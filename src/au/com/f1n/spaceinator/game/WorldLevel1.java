package au.com.f1n.spaceinator.game;

import java.util.ArrayList;

import android.os.Vibrator;
import au.com.f1n.spaceinator.GBRenderer;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.enemy.PAsteroidEnemy;
import au.com.f1n.spaceinator.game.enemy.PEnemy;
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.mesh.intro.IntroSequence;
import au.com.f1n.spaceinator.mesh.intro.TextIntroSequence;

public class WorldLevel1 extends World {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int HELIO_HEIGHT = 2200;
	private static final String[][] TEXTS = { { "THE SPACEINATOR IS EQUIPPED WITH", "A LASER CANNON,CAPABLE OF", "DESTROYING MOST OBSTACLES." } };
	private int phase = 0;
	private int killedAsteroids;
	private int lastScore;
	private long startPhase;
	private boolean shot;
	private double theta;
	private boolean finger;
	private long lastAdd;

	public void init(GBRenderer gbRenderer, Vibrator v) {
		super.init(gbRenderer, v);
		gbRenderer.getSoundManager().playMusic(R.raw.music_training);
	}

	public void start(GameState gameState) {
		super.start(gameState);
		targetAsteroids = true;
		if (centreSpaceShip == null) {
			graphicParticles = new ArrayList<PGraphicParticle>(100);
			objects = new FArrayList<PObject>(MAX_OBJECTS);
			curObjects = new FArrayList<PObject>();
			edgesX = new FArrayList<PEdge>(MAX_OBJECTS);

			planets = new PPlanet[0];

			// Add the spaceship
			centreSpaceShip = new PSpaceShip(this, -SHIP_START_OFFSET, 0, 0, 0);
			addObject(centreSpaceShip);

			// Add the planets
			for (PPlanet p : planets)
				addObject(p);

			camera = new PStandardCamera(this);

			msg = "WELCOME TO LASER TRAINING";
			msgAlpha = 1;

			boundry = new RectBoundry(HELIO_HEIGHT);
		}
	}

	public int timeStep() {
		int dTime = super.timeStep();
		if (dTime == -1)
			return -1;
		if (zooming == -1)
			return dTime;

		if (dead && lastTime > deadTime + 2000) {
			gbRenderer.loadWorld(this.getClass());
		}

		killedAsteroids = sumKills();
		shot = shot || centreSpaceShip.isShooting();

		switch (phase) {
		case 0:
			phase++;
			msg = "DRAG THE RIGHT HALF OF THE SCREEN";
			finger = true;
			msgAlpha = 1;
			centreSpaceShip.dx = 0;
			centreSpaceShip.dy = 0;
			startPhase = lastTime;
			break;
		case 1:
			if (shot && lastTime - startPhase > 1000) {
				finger = false;
				msg = "DESTROY THE ASTEROID";
				phase++;
				addOneAsteroid();
				startPhase = lastTime;
			} else {
				msgAlpha = 1;
			}
			centreSpaceShip.dx = 0;
			centreSpaceShip.dy = 0;
			break;

		case 2:
			if (killedAsteroids == 1) {
				phase++;
				score += 10 + Math.max(0, 12 - (lastTime - startPhase) / 1000);
			}
			break;

		case 3:
			msg = "HOLD DOWN TO FIRE!";
			phase++;
			msgAlpha = 1;
			lastScore = killedAsteroids;
			startPhase = lastTime;
			break;
		case 4:
			score += killedAsteroids - lastScore;
			lastScore = killedAsteroids;
			int numAstr = 0;
			for (Object o : objects.array)
				if (o instanceof PAsteroidEnemy)
					numAstr++;
			if (numAstr < 60) {
				addAsteroid();
			}
			int sec = 30 - (int) (lastTime - startPhase) / 1000;
			if (sec <= 0) {
				countdown = null;
				phase++;
				msg = "MISSION COMPLETE";
				msgAlpha = 1;
			} else {
				countdown = "" + sec;
			}
			break;
		case 5:
			if (zooming == 0) {
				startZoom = lastTime;
				zooming = 1;
			}
		}

		return dTime;
	}

	private void addAsteroid() {
		if (lastTime - lastAdd > 500) {

			theta += Math.PI / 180 * 13;
			float x = (float) Math.cos(theta);
			float y = (float) Math.sin(theta);
			PAsteroidEnemy asteroid = new PAsteroidEnemy(this, x * HELIO_HEIGHT * 2, y * HELIO_HEIGHT * 2, -x * (1 + (float) Util.randFloat()), -y
					* (1 + (float) Util.randFloat()), 3, false);
			addObject(asteroid);
			lastAdd = lastTime;
		}
	}

	private void addOneAsteroid() {
		float x = -HELIO_HEIGHT;
		PAsteroidEnemy asteroid = new PAsteroidEnemy(this, x * 2, 0, -x * .001f, 0, 2, false);
		addObject(asteroid);

		PTargetParticle target = new PTargetParticle(x * 2, 0, 300);
		target.setTracking(asteroid);
		graphicEffect(target);
	}

	@Override
	public int getNebulaID() {
		return R.drawable.nebula1;
	}

	@Override
	public int getStarIndex() {
		return 1;
	}

	@Override
	public void laserKilled(PEnemy otherEnemy) {
		score++;
	}

	public boolean isFinger() {
		return finger;
	}

	@Override
	public IntroSequence getIntroSequence() {
		return new TextIntroSequence(TEXTS);
	}

	public long resumeNow() {
		long dTime = super.resumeNow();
		startPhase += dTime;
		return dTime;
	}

	@Override
	public int getLeaderBoardID() {
		return R.string.leaderboard_lasers;
	}
}
