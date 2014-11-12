package au.com.f1n.spaceinator.game;

import java.util.ArrayList;

import android.os.Vibrator;
import au.com.f1n.spaceinator.GBGLSurfaceView;
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

public class WorldSolar2 extends World {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int BOUNDRY_RADIUS = 9000;
	private static final String[][] TEXTS = { { "PLANETS AND SUNS CAN OFTEN BE", "USED AS AN ADDITIONAL SHIELD", "FROM THE NACHT." } };
	private int phase;
	private long phaseTimeStart;
	private int lastKilled;
	private int nEnemy = 7;
	private long nextEnemies;
	private long timeGap = 8000;

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

			planets = new PPlanet[2];
			planets[0] = new PPlanet(R.drawable.sun, 1e10f, 0, 1000, 0, new byte[] { (byte) 239, (byte) 241, (byte) 150, (byte) 255 }, new byte[] { (byte) 239,
					(byte) 20, (byte) 12, (byte) 0 }, 2f, new float[] { 1, .95f, .4f, 1 }, false);
			planets[1] = new PPlanet(R.drawable.neptune, 1.6e10f, 0.01f, 400, AU * 1.8f, null, null, 1, true);

			// Add the spaceship
			centreSpaceShip = new PSpaceShip(this, -AU * 2f - SHIP_START_OFFSET, 0, 0, 0);
			addObject(centreSpaceShip);

			// Add the planets
			for (PPlanet p : planets)
				addObject(p);

			camera = new PStandardCamera(this);

			enemyAIGroups = new FArrayList<PEnemyAIGroup>();

			// Add initial asteroids
			for (int i = 0; i < 10; i++) {
				double angle = Util.randFloat() * Math.PI * 2;
				float r = (float) Util.randFloat() * 10000 + 2000;
				addObject(new PAsteroidEnemy(this, (float) Math.cos(angle) * r, (float) Math.sin(angle) * r, 0, 0, 4, true));
			}

			boundry = new CircleBoundry(BOUNDRY_RADIUS);
			updateNewObjects();
		}
	}

	public int timeStep() {
		int dTime = super.timeStep();
		if (dTime == -1)
			return dTime;

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
			msg = "UNLIMITED ALIEN INVASION";
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
			addEnemy();
			break;
		case 3:
			addEnemy();
			break;
		}

		return dTime;
	}

	private int sumEnemyKills() {
		return killCount[PEnemy.CLASS_SCOUT] + killCount[PEnemy.CLASS_SHOOTER] + killCount[PEnemy.CLASS_TANK] * 100;
	}

	private void addEnemy() {
		if (lastTime < nextEnemies)
			return;
		nextEnemies = lastTime + timeGap;
		if (phase == 3 && timeGap > 1000)
			timeGap -= 1000;

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

			PEnemy newEnemy = new PEnemy(this, (float) Math.cos(angle) * r, (float) Math.sin(angle) * r, 0, 0,
					Util.randFloat() > .5 || phase == 3 ? PEnemy.CLASS_SHOOTER : PEnemy.CLASS_SCOUT);

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
		nEnemy = Math.min((int) (nEnemy * 1.25), 44);
	}

	@Override
	public int getNebulaID() {
		return R.drawable.nebula15;
	}

	@Override
	public int getStarIndex() {
		return 5;
	}

	@Override
	public void laserKilled(PEnemy otherEnemy) {
		if (otherEnemy.enemyClass == PEnemy.CLASS_SCOUT || otherEnemy.enemyClass == PEnemy.CLASS_SHOOTER)
			score++;
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
		return R.string.leaderboard_infinity;
	}
}
