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
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.mesh.intro.IntroSequence;
import au.com.f1n.spaceinator.mesh.intro.TextIntroSequence;

public class WorldSolar4 extends World {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final float BOUNDRY_RADIUS = 20000;
	private static final String[][] TEXTS = { { "AN EPIC BATTLE WAS SET!", "THE NACHT, DESPERATE TO DEFEAT", "THE SPACEINATOR, CREATED A NEW",
			"CLASS OF FIGHTER." } };
	private int phase;
	private long phaseTimeStart;
	private int lastKilled;
	private int nEnemy = 7;
	private long nextEnemies;
	private long nextEnemyTime = 12000;

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

			planets = new PPlanet[7];
			planets[0] = new PPlanet(R.drawable.sun, 2e11f, 0, 1800, 0, new byte[] { (byte) 239, (byte) 241, (byte) 150, (byte) 255 }, new byte[] { (byte) 239,
					(byte) 20, (byte) 12, (byte) 0 }, 2f, new float[] { 1, .95f, .4f, 1 }, false);
			planets[1] = new PPlanet(R.drawable.neptune, 1.6e10f, 0.02f, 800, AU * 2.2f, null, null, 1, true);
			planets[2] = new PPlanet(R.drawable.mars, 1.1e10f, 0.025f, 600, AU * 1.4f, null, null, 1, true);
			planets[3] = new PPlanet(R.drawable.uranus, 5e10f, 0.01f, 900, 18000, null, null, 1, true);
			planets[4] = new PPlanet(R.drawable.venus, 6e10f, 0.022f, 700, 14000, null, null, 1, true);
			planets[5] = new PPlanet(R.drawable.mercury, 3e10f, 0.002f, 300, 14000, null, null, 1, true);
			planets[6] = new PPlanet(R.drawable.earth, 4e10f, 0.004f, 380, 6000, null, null, 1, true);

			// Add the spaceship
			centreSpaceShip = new PSpaceShip(this, -AU * 2f - SHIP_START_OFFSET, 0, 0, 0);
			addObject(centreSpaceShip);

			// Add the planets
			for (PPlanet p : planets)
				addObject(p);

			camera = new PStandardCamera(this);

			enemyAIGroups = new FArrayList<PEnemyAIGroup>();

			// Add initial asteroids
			for (int i = 0; i < 200; i++) {
				double angle = Math.random() * Math.PI * 2;
				float r = (float) Math.random() * 1200 + 12000;
				addObject(new PAsteroidEnemyOrbiting(this, (float) Math.cos(angle) * r, (float) Math.sin(angle) * r, 0, 0, (int) (Math.random() * 4), true));
			}

			boundry = new CircleBoundry(BOUNDRY_RADIUS);
			msg = "SOLAR FIGHTER";
			msgAlpha = 1;
			updateNewObjects();

			PEnemyAIGroup group = new AIGroupBasic(this, 100);
			PEnemy firstFighter = new PEnemy(this, -AU, 0, 0, 1, PEnemy.CLASS_FIGHTER);
			group.add(firstFighter);
			addObject(firstFighter);

			enemyAIGroups.add(group);
		}
	}

	public int timeStep() {
		int dTime = super.timeStep();
		if (dTime == -1)
			return -1;
		if (zooming == -1)
			return dTime;

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
			msg = "ENDLESS WAVES OF ALIENS";
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
				phase++;
				msg = "SUCCESS! SEE HOW LONG YOU LAST NOW.";
				msgAlpha = 1;
			} else {
				countdown = Util.timeFormat(sec);
			}
			addEnemy();
			break;
		case 3:
			sec = (int) (lastTime - phaseTimeStart) / 1000 - 120;
			countdown = Util.timeFormat(sec);

			if (!gameState.achievement_survivor_50 && sec >= 120)
				gameState.achievement_survivor_50 = gameState.achievement(R.string.achievement_survivor_50, 50);

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
		if (lastTime < nextEnemies)
			return;

		if (objects.size > 500)
			// Next frame there may be fewer objects
			return;

		nextEnemies = lastTime + nextEnemyTime;

		if (nextEnemyTime > 4000)
			nextEnemyTime -= 500;

		PEnemyAIGroup group = new AIGroupBasic(this, 100);
		double baseAngle = Util.randFloat() * Math.PI * 2;

		int extras = 0;
		if (nEnemy > 30)
			extras = 1;
		if (nEnemy > 50)
			extras = nEnemy / 22;

		for (int i = 0; i < extras; i++) {
			// Add a tank
			float r = BOUNDRY_RADIUS + 700 + (float) Util.randFloat() * 300;
			PEnemy newEnemy = new PEnemy(this, (float) Math.cos(baseAngle) * r, (float) Math.sin(baseAngle) * r, 0, 0, PEnemy.CLASS_TANK);
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
		nEnemy = Math.min((int) (nEnemy * 1.25), 48);
	}

	@Override
	public int getNebulaID() {
		return R.drawable.nebula6;
	}

	@Override
	public int getStarIndex() {
		return 11;
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
		return R.string.leaderboard_solar_fighter;
	}
}
