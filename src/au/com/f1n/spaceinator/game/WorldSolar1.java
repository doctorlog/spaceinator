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
import au.com.f1n.spaceinator.mesh.intro.ProtectionIntroSequence;

public class WorldSolar1 extends World {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int[] N_ENEMY = { 0, 0, 10, 20, 25, 30, 180 };
	private static final float BOUNDRY_RADIUS = 7000;
	private int phase;
	private int startPhaseEnemies;
	private long phaseTimeStart;
	private int lastKilled;

	public void init(GBRenderer gbRenderer, Vibrator v) {
		super.init(gbRenderer, v);
		gbRenderer.getSoundManager().playMusic(R.raw.music_mantilla);
	}

	public void start(GameState gameState) {
		super.start(gameState);
		if (centreSpaceShip == null) {
			targetScale = GBGLSurfaceView.MAX_SCALE;
			graphicParticles = new ArrayList<PGraphicParticle>(100);
			objects = new FArrayList<PObject>(MAX_OBJECTS);
			curObjects = new FArrayList<PObject>();
			edgesX = new FArrayList<PEdge>(MAX_OBJECTS);

			planets = new PPlanet[1];
			planets[0] = new PPlanet(R.drawable.sun, 1e10f, 0, 1000, 0, new byte[] { (byte) 239, (byte) 241, (byte) 150, (byte) 255 }, new byte[] { (byte) 239,
					(byte) 20, (byte) 12, (byte) 0 }, 2f, new float[] { 1, .95f, .4f, 1 }, false);

			// Add the spaceship
			centreSpaceShip = new PSpaceShip(this, -SHIP_START_OFFSET - AU * 1.5f, 0, 0, 0);
			addObject(centreSpaceShip);

			// Add the planets
			for (PPlanet p : planets)
				addObject(p);

			camera = new PStandardCamera(this);

			enemyAIGroups = new FArrayList<PEnemyAIGroup>();

			// Add initial asteroids
			for (int i = 0; i < 30; i++) {
				double angle = Util.randFloat() * Math.PI * 2;
				float r = (float) Util.randFloat() * 10000 + 2000;
				addObject(new PAsteroidEnemy(this, (float) Math.cos(angle) * r, (float) Math.sin(angle) * r, 0, 0, 2, false));
			}

			boundry = new CircleBoundry(BOUNDRY_RADIUS);
			updateNewObjects();
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

		int killedEnemyCount = sumEnemyKills();
		score += killedEnemyCount - lastKilled;
		lastKilled = killedEnemyCount;

		switch (phase) {
		case 0:
			phase++;
			msg = "STOP THE ALIEN INVASION";
			msgAlpha = 1;
			break;
		case 1:
			if (msgAlpha < 0.1) {
				phaseTimeStart = lastTime;
				msg = "SURVIVE 5 WAVES OF ALIENS";
				msgAlpha = 1;
				phase++;
				addEnemy();
				startPhaseEnemies = 0;
			}
			break;
		case 6:
			if (killedEnemyCount - startPhaseEnemies >= N_ENEMY[phase]) {
				// Begin ending
				phase++;
				addTimeScore();
				msg = "MISSION COMPLETE";
				msgAlpha = 1;
			}
			break;
		case 7:
			// End now
			if (zooming == 0 && msgAlpha < .1f) {
				startZoom = lastTime;
				zooming = 1;
			}
			break;
		default:
			if (killedEnemyCount - startPhaseEnemies >= N_ENEMY[phase]) {
				if (phase == 5)
					msg = "FINAL WAVE";
				else
					msg = "WAVE " + phase;
				msgAlpha = 1;
				phase++;
				startPhaseEnemies = killedEnemyCount;
				addTimeScore();
				addEnemy();
				phaseTimeStart = lastTime;
			}
			break;
		}

		return dTime;
	}

	private void addTimeScore() {
		score += 22 + Math.max(0, 30 - (lastTime - phaseTimeStart) / 1000);
	}

	private int sumEnemyKills() {
		return killCount[PEnemy.CLASS_SCOUT] + killCount[PEnemy.CLASS_SHOOTER] + killCount[PEnemy.CLASS_TANK] * 100;
	}

	private void addEnemy() {
		PEnemyAIGroup group = new AIGroupBasic(this, 100);
		double baseAngle = Util.randFloat() * Math.PI * 2;

		int extras = 0;
		if (phase == 6) {
			extras = 200;
			for (int i = 0; i < 2; i++) {
				// Add a tank
				float r = BOUNDRY_RADIUS + 700 + (float) Util.randFloat() * 300;
				PEnemy newEnemy = new PEnemy(this, (float) Math.cos(baseAngle) * r, (float) Math.sin(baseAngle) * r, 0, 0, PEnemy.CLASS_TANK);
				group.add(newEnemy);
				addObject(newEnemy);
			}
		}

		for (int i = 0; i < N_ENEMY[phase] - extras; i++) {
			double angle = baseAngle + Util.randFloat() * .5;
			float r = (float) Util.randFloat() * 1000 + BOUNDRY_RADIUS;

			PEnemy newEnemy = new PEnemy(this, (float) Math.cos(angle) * r, (float) Math.sin(angle) * r, 0, 0, Util.randFloat() > .5 ? PEnemy.CLASS_SCOUT
					: PEnemy.CLASS_SHOOTER);
			group.add(newEnemy);
			addObject(newEnemy);
		}

		enemyAIGroups.add(group);
	}

	@Override
	public int getNebulaID() {
		return R.drawable.nebula3;
	}

	@Override
	public int getStarIndex() {
		return 4;
	}

	@Override
	public void laserKilled(PEnemy otherEnemy) {
		if (otherEnemy.enemyClass == PEnemy.CLASS_SCOUT || otherEnemy.enemyClass == PEnemy.CLASS_SHOOTER)
			score++;
	}

	@Override
	public IntroSequence getIntroSequence() {
		return new ProtectionIntroSequence(gbRenderer);
	}
	
	public long resumeNow() {
		long dTime = super.resumeNow();
		phaseTimeStart += dTime;
		return dTime;
	}

	@Override
	public int getLeaderBoardID() {
		return R.string.leaderboard_protection;
	}
}
