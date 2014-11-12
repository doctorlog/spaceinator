package au.com.f1n.spaceinator.game;

import java.util.ArrayList;

import android.os.Vibrator;
import au.com.f1n.spaceinator.GBGLSurfaceView;
import au.com.f1n.spaceinator.GBRenderer;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.enemy.AIGroupSpike;
import au.com.f1n.spaceinator.game.enemy.PAsteroidEnemy;
import au.com.f1n.spaceinator.game.enemy.PEnemy;
import au.com.f1n.spaceinator.game.enemy.PEnemyAIGroup;
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.mesh.intro.IntroSequence;
import au.com.f1n.spaceinator.mesh.intro.TextIntroSequence;

public class WorldSolar3 extends World {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final float BOUNDRY_RADIUS = 8000;
	private static final String[][] TEXTS = { { "WE FOUND THE SOURCE OF THE", "NACHT IN THE MILKY WAY!", "", "SPACEINATOR: DESTROY SPIKE!" } };
	private int phase;
	private long phaseTimeStart;
	private int lastKilled;
	private PEnemy spike;

	public void init(GBRenderer gbRenderer, Vibrator v) {
		super.init(gbRenderer, v);
		gbRenderer.getSoundManager().playMusic(R.raw.music_namaste);
	}

	public void start(GameState gameState) {
		super.start(gameState);
		if (centreSpaceShip == null) {
			targetScale = GBGLSurfaceView.MAX_SCALE;
			graphicParticles = new ArrayList<PGraphicParticle>(100);
			objects = new FArrayList<PObject>(MAX_OBJECTS);
			curObjects = new FArrayList<PObject>();
			edgesX = new FArrayList<PEdge>(MAX_OBJECTS);

			planets = new PPlanet[3];
			planets[0] = new PPlanet(R.drawable.sun, 1e10f, 0, 1000, 0, new byte[] { (byte) 23, (byte) 24, (byte) 255, (byte) 255 }, new byte[] { (byte) 50,
					(byte) 1, (byte) 55, (byte) 0 }, 2f, new float[] { .3f, .1f, .8f, 1 }, false);
			planets[1] = new PPlanet(R.drawable.mercury, 5e9f, 0.01f, 500, AU * 1.6f, null, null, 1.3f, true);
			planets[2] = new PPlanet(R.drawable.mars, 2e10f, 0.007f, 600, AU, new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 0 }, new byte[] {
					(byte) 240, (byte) 10, (byte) 0, (byte) 20 }, 1.2f, true);

			// Add the spaceship
			centreSpaceShip = new PSpaceShip(this, -BOUNDRY_RADIUS / 2 - SHIP_START_OFFSET, 0, 0, 0);
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
			msg = "SPIKE";
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

		if (dead && lastTime > deadTime + 2000) {
			gbRenderer.loadWorld(this.getClass());
		}

		int killedEnemyCount = sumEnemyKills();
		score += killedEnemyCount - lastKilled;
		lastKilled = killedEnemyCount;

		switch (phase) {
		case 0:
			AIGroupSpike aiGroup = new AIGroupSpike(this, 200);
			// Spike starts in the middle of nowhere but will telelport
			spike = new PEnemy(this, BOUNDRY_RADIUS / 2, 0, 0, 0, PEnemy.CLASS_SPIKE);
			aiGroup.add(spike);
			addObject(spike);
			enemyAIGroups.add(aiGroup);
			phase++;
			msg = "DESTROY SPIKE";
			msgAlpha = 1;
			phaseTimeStart = lastTime;
			break;
		case 1:
			if (msgAlpha < 0.1) {
				msg = "...AND HIS DEFENDERS!";
				msgAlpha = 1;
				phase++;
			}
			break;
		case 2:
			// Wait for them to kill spike - let the AIGroupSpike handle all of the
			// game logic.
			if (spike.isDead()) {
				countdown = null;
				// Begin ending
				phase++;
				addTimeScore();
				msg = "MISSION COMPLETE";
				msgAlpha = 1;

				for (Object o : objects.array) {
					if (o != null && o instanceof PEnemy) {
						PEnemy pe = (PEnemy) o;
						if (pe.life > 0)
							pe.damage(pe.life, lastTime, 0, 0, false);
					}
				}
			} else {
				countdown = "SPIKE:" + (spike.life / 20) + "%";
			}
			break;
		case 3:
			// End now
			if (zooming == 0 && msgAlpha < .1f) {
				startZoom = lastTime;
				zooming = 1;
			}
			break;
		}

		return dTime;
	}

	private void addTimeScore() {
		score += 500 + Math.max(0, 300 - (lastTime - phaseTimeStart) / 1000);
	}

	private int sumEnemyKills() {
		return killCount[PEnemy.CLASS_SCOUT] + killCount[PEnemy.CLASS_SHOOTER] + killCount[PEnemy.CLASS_TANK] * 100;
	}

	@Override
	public int getNebulaID() {
		return R.drawable.nebula4;
	}

	@Override
	public int getStarIndex() {
		return 8;
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
		return dTime;
	}

	@Override
	public int getLeaderBoardID() {
		return R.string.leaderboard_spike;
	}
}
