package au.com.f1n.spaceinator.game;

import java.util.ArrayList;

import android.os.Vibrator;
import au.com.f1n.spaceinator.GBRenderer;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.enemy.AIGroupCharge;
import au.com.f1n.spaceinator.game.enemy.PEnemy;
import au.com.f1n.spaceinator.game.enemy.PEnemyAIGroup;
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.mesh.intro.IntroSequence;
import au.com.f1n.spaceinator.mesh.intro.TextIntroSequence;

public class WorldSolar5Bomb extends World {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final float BOUNDRY_HEIGHT = 3200;
	private static final float BOUNDRY_WIDTH = BOUNDRY_HEIGHT * 1.618f;
	private static final String[][] TEXTS = { { "DESPERATE TO STOP THE SPACEINATOR,", "THE NACHT CREATED A NEW WEAPON..." } };
	private int phase;
	private long phaseTimeStart;
	private int lastKilled;
	private int nEnemy = 7;
	private long nextEnemies;
	private long nextEnemyTime = 10000;

	public void init(GBRenderer gbRenderer, Vibrator v) {
		super.init(gbRenderer, v);
		gbRenderer.getSoundManager().playMusic(R.raw.music_mantilla);
	}

	public void start(GameState gameState) {
		super.start(gameState);
		if (centreSpaceShip == null) {
			targetScale = 1.5f;
			graphicParticles = new ArrayList<PGraphicParticle>(100);
			objects = new FArrayList<PObject>(MAX_OBJECTS);
			curObjects = new FArrayList<PObject>();
			edgesX = new FArrayList<PEdge>(MAX_OBJECTS);

			planets = new PPlanet[0];

			// Add the spaceship
			centreSpaceShip = new PSpaceShip(this, -SHIP_START_OFFSET, 0, 0, 0);
			addObject(centreSpaceShip);

			camera = new PStandardCamera(this);

			enemyAIGroups = new FArrayList<PEnemyAIGroup>();

			msg = "BOMB";
			msgAlpha = 1;

			boundry = new EllipseBoundry(BOUNDRY_WIDTH, BOUNDRY_HEIGHT);
		}
	}

	public int timeStep() {
		int dTime = super.timeStep();
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
			msg = "SURVIVE THEIR ATTACK!";
			msgAlpha = 1;
			break;
		case 1:
			if (msgAlpha < 0.01) {
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
		if (lastTime < nextEnemies)
			return;
		nextEnemies = lastTime + nextEnemyTime;
		if (nextEnemyTime > 3500)
			nextEnemyTime -= 350;

		PEnemyAIGroup group = new AIGroupCharge(this, nEnemy, false);
		float probNotBomb = (float) nextEnemyTime / 12000;

		for (int i = 0; i < nEnemy; i++) {
			double angle = Util.randFloat() * Math.PI * 2;
			float r = (float) Util.randFloat() * 1000 + BOUNDRY_WIDTH;

			PEnemy newEnemy = new PEnemy(this, (float) Math.cos(angle) * r, (float) Math.sin(angle) * r, 0, 0, Util.randFloat() > probNotBomb ? PEnemy.CLASS_SCOUT
					: PEnemy.CLASS_BOMB);
			group.add(newEnemy);
			addObject(newEnemy);

			if (phase == 3) {
				// Double life - that should end the level
				newEnemy.life *= 2;
				if (newEnemy.enemyClass == PEnemy.CLASS_SHOOTER)
					newEnemy.getWeapon().setBaseDamage(5);
			}
		}

		enemyAIGroups.add(group);
		if (phase == 3)
			nEnemy = Math.min((int) (nEnemy * 1.2), 60);
		else
			nEnemy = Math.min((int) (nEnemy * 1.15), 100);
	}

	@Override
	public int getNebulaID() {
		return R.drawable.nebula10;
	}

	@Override
	public int getStarIndex() {
		return 13;
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
		nextEnemyTime += dTime;
		return dTime;
	}

	@Override
	public int getLeaderBoardID() {
		return R.string.leaderboard_bomb;
	}
}
