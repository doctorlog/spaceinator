package au.com.f1n.spaceinator.game;

import java.util.ArrayList;

import android.os.Vibrator;
import au.com.f1n.spaceinator.GBRenderer;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.enemy.AIGroupBasic;
import au.com.f1n.spaceinator.game.enemy.PAsteroidEnemy;
import au.com.f1n.spaceinator.game.enemy.PEnemy;
import au.com.f1n.spaceinator.game.enemy.PEnemyAIGroup;
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.mesh.intro.IntroSequence;
import au.com.f1n.spaceinator.mesh.intro.TextIntroSequence;

/**
 * 3rd training level
 * 
 * @author luke
 * 
 */
public class WorldLevel2 extends World {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final float HELIO_HEIGHT = 2200;
	private static final float HELIO_WIDTH = HELIO_HEIGHT * RectBoundry.RECT_RATIO;
	private int phase = 0;
	private int killedEnemyCount;
	private int lastScore;
	private long startPhase;
	private double sProb = .8;
	private int nextSpawn;
	private boolean finger;
	private float zoom;
	private String[][] TEXTS = { { "EARLY TESTING SHOWED THAT", "THE NACHT ARE NO MATCH FOR", "    ", "THE SPACEINATOR" } };

	public void init(GBRenderer gbRenderer, Vibrator v) {
		super.init(gbRenderer, v);
		gbRenderer.getSoundManager().playMusic(R.raw.music_training);
	}

	public void start(GameState gameState) {
		super.start(gameState);
		if (centreSpaceShip == null) {
			graphicParticles = new ArrayList<PGraphicParticle>(100);
			objects = new FArrayList<PObject>(MAX_OBJECTS);
			curObjects = new FArrayList<PObject>();
			edgesX = new FArrayList<PEdge>(MAX_OBJECTS);
			enemyAIGroups = new FArrayList<PEnemyAIGroup>(10);

			planets = new PPlanet[0];

			// Add the spaceship
			centreSpaceShip = new PSpaceShip(this, -SHIP_START_OFFSET, 0, 0, 0);
			addObject(centreSpaceShip);

			// Add the planets
			for (PPlanet p : planets)
				addObject(p);

			camera = new PStandardCamera(this);

			msg = "WELCOME TO ALIENS 101";
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

		killedEnemyCount = sumEnemyKills();

		switch (phase) {
		case 0:
			phase++;
			finger = true;
			msg = "DRAG BOTTOM TO ZOOM";
			msgAlpha = 1;
			zoom = gbRenderer.getScale();
			break;
		case 1:
			msgAlpha = 1;
			if (zoom != gbRenderer.getScale()) {
				finger = false;
				phase++;
			}
			break;
		case 2:
			if (msgAlpha < 0.1f) {
				msg = "SCOUT ALIENS WILL TRY TO HIT YOU!";
				msgAlpha = 1;
				phase++;
				addEnemy(5, 1);
				startPhase = lastTime;
			}
			break;
		case 3:
			if (killedEnemyCount == 5) {
				phase++;
				score += 10 + Math.max(0, 15 - (lastTime - startPhase) / 1000);
			}
			break;

		case 4:
			msg = "USE ASTEROIDS AS A SHIELD";
			addEnemy(15, 1);
			addAsteroids();

			phase++;
			msgAlpha = 1;
			lastScore = killedEnemyCount;
			startPhase = lastTime;
			break;
		case 5:
			if (killedEnemyCount == 20) {
				msg = "NOW THEY SHOOT BACK!";
				msgAlpha = 1;
				phase++;
				score += 50 + Math.max(0, 100 - (lastTime - startPhase) / 1000);
				nextSpawn = killedEnemyCount;
			}
			break;
		case 6:
			score += (killedEnemyCount - lastScore) * 3;
			lastScore = killedEnemyCount;
			if (killedEnemyCount == nextSpawn) {
				addEnemy(10, sProb -= .1);
				nextSpawn = killedEnemyCount + 10;
			}

			int sec = 30 - (int) (lastTime - startPhase) / 1000;
			if (sec <= 0 && !isDead()) {
				countdown = null;
				phase++;
				msg = "MISSION COMPLETE";
				msgAlpha = 1;
			} else {
				countdown = "" + sec;
			}

			break;

		case 7:
			if (zooming == 0) {
				startZoom = lastTime;
				zooming = 1;
			}
		default:
			break;
		}

		return dTime;
	}

	private int sumEnemyKills() {
		return killCount[PEnemy.CLASS_SCOUT] + killCount[PEnemy.CLASS_SHOOTER];
	}

	private void addAsteroids() {
		for (int i = 0; i < 20; i++) {
			float x = HELIO_WIDTH;
			float y = -HELIO_HEIGHT + HELIO_HEIGHT * 2 * i / 20;
			PAsteroidEnemy asteroid = new PAsteroidEnemy(this, x, y, 0, 0, 2, false);
			addObject(asteroid);
		}
	}

	private void addEnemy(int n, double scoutProb) {
		PEnemyAIGroup group = new AIGroupBasic(this, n);

		for (int i = 0; i < n; i++) {
			PEnemy newEnemy = new PEnemy(this, HELIO_WIDTH + (float) Util.randFloat() * HELIO_HEIGHT, (float) (HELIO_HEIGHT * 2 * (Util.randFloat() - .5)), 0, 0,
					Util.randFloat() > scoutProb ? PEnemy.CLASS_SHOOTER : PEnemy.CLASS_SCOUT);
			group.add(newEnemy);
			addObject(newEnemy);
		}
		enemyAIGroups.add(group);
	}

	@Override
	public int getNebulaID() {
		return R.drawable.nebula5;
	}

	@Override
	public int getStarIndex() {
		return 2;
	}

	@Override
	public void laserKilled(PEnemy otherEnemy) {
		if (otherEnemy.enemyClass == PEnemy.CLASS_SCOUT || otherEnemy.enemyClass == PEnemy.CLASS_SHOOTER)
			score++;
	}

	public boolean isFinger() {
		return finger;
	}

	@Override
	public IntroSequence getIntroSequence() {
		return new TextIntroSequence(TEXTS);
	}

	@Override
	public int getLeaderBoardID() {
		return R.string.leaderboard_aliens;
	}
}
