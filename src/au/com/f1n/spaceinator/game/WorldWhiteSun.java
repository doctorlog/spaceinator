package au.com.f1n.spaceinator.game;

import java.util.ArrayList;

import android.os.Vibrator;
import au.com.f1n.spaceinator.GBGLSurfaceView;
import au.com.f1n.spaceinator.GBRenderer;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.enemy.AIGroupBasic;
import au.com.f1n.spaceinator.game.enemy.PAsteroidEnemyOrbiting;
import au.com.f1n.spaceinator.game.enemy.PEnemy;
import au.com.f1n.spaceinator.game.enemy.PEnemyAIGroup;
import au.com.f1n.spaceinator.game.enemy.PEnemyDart;
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.mesh.intro.IntroSequence;
import au.com.f1n.spaceinator.mesh.intro.TextIntroSequence;

public class WorldWhiteSun extends World {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final float BOUNDRY_RADIUS = 25000;
	private static final String[][] TEXTS = { { "ONE OF THE NACHT'S HOME", "SOLAR SYSTEMS", "", "THEY WILL NOT LIKE US HERE!" } };
	private int phase;
	private long phaseTimeStart;
	private int lastKilled;
	private int nEnemy = 4;
	private long nextEnemies;
	private long nextEnemyTime = 12000;
	private PEnemyDart dart;

	public void init(GBRenderer gbRenderer, Vibrator v) {
		super.init(gbRenderer, v);
		gbRenderer.getSoundManager().playMusic(R.raw.music_snakecharm);
	}

	public void start(GameState gameState) {
		super.start(gameState);
		if (centreSpaceShip == null) {
			targetScale = GBGLSurfaceView.MAX_SCALE;
			graphicParticles = new ArrayList<PGraphicParticle>(100);
			objects = new FArrayList<PObject>(MAX_OBJECTS);
			curObjects = new FArrayList<PObject>();
			edgesX = new FArrayList<PEdge>(MAX_OBJECTS);

			planets = new PPlanet[6];
			planets[0] = new PPlanet(R.drawable.sun, 4e12f, 0, 5000, 0, new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255 }, new byte[] { (byte) 255,
					(byte) 255, (byte) 255, (byte) 0 }, 2f, new float[] { .99f, .98f, 1, 1 }, false);
			planets[1] = new PPlanet(R.drawable.neptune, 1.6e10f, 0.02f, 800, 14000, null, null, 1, true);
			planets[2] = new PPlanet(R.drawable.mars, 1.1e10f, 0.09f, 600, 12000, null, null, 1, true);
			planets[3] = new PPlanet(R.drawable.uranus, 5e10f, -0.01f, 700, 23000, null, null, 1, true);
			planets[4] = new PPlanet(R.drawable.venus, 6e10f, 0.022f, 500, 19000, null, null, 1, true);
			planets[5] = new PPlanet(R.drawable.mercury, 3e10f, 0.002f, 400, 16500, null, null, 1, true);

			// Add the spaceship
			centreSpaceShip = new PSpaceShip(this, -AU * 2.5f - SHIP_START_OFFSET, 0, 0, 0);
			addObject(centreSpaceShip);

			// Add the planets
			for (PPlanet p : planets)
				addObject(p);

			// Add the dart
			dart = new PEnemyDart(this, -AU * 2.5f + 360, 3500, 0, 0);
			addObject(dart);
			// No AI, just darting
			dart.dart();

			camera = new PStandardCamera(this);

			enemyAIGroups = new FArrayList<PEnemyAIGroup>();

			// Add initial asteroids
			while (newObjects.size < 100) {
				float r = (float) Math.random() * 12500 + 12000;
				boolean valid = true;
				for (PPlanet p : planets) {
					if (Math.abs(r - p.orbitR) < p.radius + 200) {
						valid = false;
						break;
					}
				}
				if (valid) {
					double angle = Math.random() * Math.PI * 2;
					addObject(new PAsteroidEnemyOrbiting(this, (float) Math.cos(angle) * r, (float) Math.sin(angle) * r, 0, 0, (int) (Math.random() * 4), true));
				}
			}

			boundry = new CircleBoundry(BOUNDRY_RADIUS);
			msg = "WHITE SUN";
			msgAlpha = 1;
			updateNewObjects();
		}
	}

	public int timeStep() {
		int dTime = super.timeStep();
		if (dTime == -1)
			return -1;
		if (zooming == -1)
			return dTime;

		if (dart != null) {
			dart.dart();
			dart = null;
		}

		if (dead) {
			if (phase > 2) {
				if (zooming == 0) {
					zooming = 1;
					phase++;
				}
			} else if (lastTime > deadTime + 2000) {
				gbRenderer.loadWorld(this.getClass());
			}
		}

		int killedEnemyCount = sumEnemyKills();
		score += killedEnemyCount - lastKilled;
		lastKilled = killedEnemyCount;

		switch (phase) {
		case 0:
			phase++;
			msg = "AVOID THE DARTS";
			msgAlpha = 1;
			break;
		case 1:
			if (msgAlpha < 0.1) {
				phaseTimeStart = lastTime;
				nextEnemies = phaseTimeStart;
				msg = "SURVIVE 2 MINUTES";
				msgAlpha = 1;
				phase++;
			}
			break;
		case 2:
			int sec = 120 - (int) (lastTime - phaseTimeStart) / 1000;
			if (sec <= 0 && !isDead()) {
				countdown = null;
				phase++;
				msg = "SUCCESS! SEE HOW LONG YOU LAST NOW.";
				msgAlpha = 1;
			} else {
				countdown = Util.timeFormat(sec);
			}
			// break;
		case 3:
			addEnemy();
			break;
		}

		return dTime;
	}

	private int sumEnemyKills() {
		int ret = 0;
		for (int i = 0; i < PEnemy.AI_CLASS_COUNT; i++) {
			ret += killCount[i];
		}

		return ret;
	}

	private void addEnemy() {
		if (objects.size > 200 || lastTime < nextEnemies)
			// Next frame there may be fewer objects
			return;

		nextEnemies = lastTime + nextEnemyTime;

		if (nextEnemyTime > 4000)
			nextEnemyTime -= 500;

		float angle = (float) (Util.randFloat() * Math.PI * 2);
		addEnemies(angle, BOUNDRY_RADIUS);
		addEnemies(angle + Util.PI, BOUNDRY_RADIUS);

		if (nEnemy < 10)
			nEnemy++;
		else
			nEnemy = Math.min((int) (nEnemy * 1.1), 58);
	}

	private void addEnemies(float baseAngle, float boundryRadius) {
		PEnemyAIGroup group = new AIGroupBasic(this, 100);

		int extras = 0;
		if (nEnemy > 30)
			extras = 1;
		if (nEnemy > 50)
			extras = nEnemy / 22;

		for (int i = 0; i < extras - 1; i++) {
			// Add a tank
			float r = BOUNDRY_RADIUS + 700 + (float) Util.randFloat() * 300;
			PEnemy newEnemy = new PEnemy(this, (float) Math.cos(baseAngle) * r, (float) Math.sin(baseAngle) * r, 0, 0, PEnemy.CLASS_TANK);
			group.add(newEnemy);
			addObject(newEnemy);
		}

		for (int i = 0; i < extras + 1; i++) {
			// Add a dart
			float r = BOUNDRY_RADIUS + 700 + (float) Util.randFloat() * 300;
			PEnemy newEnemy = new PEnemyDart(this, (float) Math.cos(baseAngle) * r, (float) Math.sin(baseAngle) * r, 0, 0);
			group.add(newEnemy);
			addObject(newEnemy);
		}

		for (int i = 0; i < nEnemy - extras; i++) {
			double angle = baseAngle + Util.randFloat() * .5;
			float r = (float) Util.randFloat() * 1000 + BOUNDRY_RADIUS;

			PEnemy newEnemy = new PEnemy(this, (float) Math.cos(angle) * r, (float) Math.sin(angle) * r, 0, 0, Util.randFloat() > .8 ? PEnemy.CLASS_FIGHTER
					: Util.randFloat() > .5 ? PEnemy.CLASS_SCOUT : PEnemy.CLASS_SHOOTER);
			group.add(newEnemy);
			addObject(newEnemy);

			if (phase == 3) {
				// Tripple life - that should end the level
				newEnemy.life *= 3;
				if (newEnemy.enemyClass == PEnemy.CLASS_SHOOTER) {
					newEnemy.getWeapon().setBaseDamage(5);
				}
			}

		}

		enemyAIGroups.add(group);
	}

	@Override
	public int getNebulaID() {
		return R.drawable.nebulawhitesun;
	}

	@Override
	public int getStarIndex() {
		return 21;
	}

	@Override
	public void laserKilled(PEnemy otherEnemy) {
		score += otherEnemy.getScore();
	}

	@Override
	public IntroSequence getIntroSequence() {
		return new TextIntroSequence(TEXTS);
	}

	public long resumeNow() {
		long dTime = super.resumeNow();
		phaseTimeStart += dTime;
		nextEnemies += dTime;
		return dTime;
	}

	@Override
	public int getLeaderBoardID() {
		return R.string.leaderboard_white_sun;
	}
}
