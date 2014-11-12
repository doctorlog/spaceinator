package au.com.f1n.spaceinator.game;

import java.util.ArrayList;
import java.util.Random;

import android.os.Vibrator;
import au.com.f1n.spaceinator.GBRenderer;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.enemy.AIGroupBasic;
import au.com.f1n.spaceinator.game.enemy.PAsteroidEnemy;
import au.com.f1n.spaceinator.game.enemy.PEnemy;
import au.com.f1n.spaceinator.game.enemy.PEnemyAIGroup;
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.game.logic.PolyReader;
import au.com.f1n.spaceinator.mesh.intro.IntroSequence;
import au.com.f1n.spaceinator.mesh.intro.TextIntroSequence;

/**
 * This world is a simple timed race
 * 
 * @author luke
 */
public class WorldRace1 extends World {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final float SCALE = 100;
	private static final float[][] CHECK_POINTS = { { 2000, -240 }, { 9000, -3400 }, { 0, -5100 }, { 8100, -8300 }, { -5800, -9700 }, { -8600, -3600 } };

	private static final float[][] CHECK_POINT_FUN = { { 4200, -3500 }, { 4200, -6500 }, { 4200, -9500 }, { -7000, -7000 } };
	private static final String[][] TEXTS = { { "THE NACHT HAVE THEIR OWN SPACE RACE.", "SHOW THAT THE SPACEINATOR", "IS UNPARALLELED!" } };

	private PGraphicParticle target;
	private int checkPointNum = -1;
	private long raceStartTime;
	private long checkStartTime = -1;
	private Random rand;
	private int lap;

	public void init(GBRenderer gbRenderer, Vibrator v) {
		super.init(gbRenderer, v);
		gbRenderer.getSoundManager().playMusic(R.raw.music_race);
	}

	public void start(GameState gameState) {
		super.start(gameState);
		if (centreSpaceShip == null) {
			graphicParticles = new ArrayList<PGraphicParticle>(100);
			objects = new FArrayList<PObject>(MAX_OBJECTS);
			curObjects = new FArrayList<PObject>();
			edgesX = new FArrayList<PEdge>(MAX_OBJECTS);
			planets = new PPlanet[0];
			enemyAIGroups = new FArrayList<PEnemyAIGroup>();

			PolyReader pr = new PolyReader(gbRenderer.getContext().getResources().openRawResource(R.raw.track1));
			boundry = new PolyBoundry(pr.getPoints(), SCALE);
			pr = null;

			// Add the spaceship
			centreSpaceShip = new PSpaceShip(this, -SHIP_START_OFFSET, 0, 0, 0);
			addObject(centreSpaceShip);

			camera = new PStandardCamera(this);

			msg = "PURPLE RACE";
			msgAlpha = 1;
			rand = new Random(69);
			targetAsteroids = true;
			updateNewObjects();
		}
	}

	public int timeStep() {
		int dTime = super.timeStep();
		if (dTime == -1)
			return -1;
		if (zooming == -1)
			return dTime;

		// Start
		if (checkPointNum == -1 && lastTime - startTime > 1000) {
			msg = "GO GO GO!";
			msgAlpha = 1;
			nextTarget();

			// Add the speed boosters
			graphicEffect(new SpeedBoost(this, -8300, -2500, 0));
			graphicEffect(new SpeedBoost(this, -4200, -1700, .5f));
			graphicEffect(new SpeedBoost(this, -1800, 0, -.2f));
		}

		if (checkPointNum == 0 && centreSpaceShip.x > CHECK_POINTS[0][0]) {
			target.explode();
			// Just passed a lap finish
			if (lap == 0) {
				raceStartTime = lastTime;
			}
			lap++;
			msg = "LAP " + lap + "/3";
			msgAlpha = 1;

			nextTarget();
		} else if (checkPointNum > 0) {
			float dx = centreSpaceShip.x - CHECK_POINTS[checkPointNum][0];
			float dy = centreSpaceShip.y - CHECK_POINTS[checkPointNum][1];
			if (dx * dx + dy * dy < 1000000) {
				// just passed a normal target
				target.explode();
				nextTarget();

				if (checkPointNum == 2) {
					// Add asteroids at fun location #1
					for (int i = 0; i < 40; i++) {
						float x = CHECK_POINT_FUN[0][0] + (float) rand.nextGaussian() * 1500;
						float y = CHECK_POINT_FUN[0][1] + (float) rand.nextGaussian() * 1500;

						addObject(new PAsteroidEnemy(this, x, y, 0, 0, 1, false));
					}
				} else if (checkPointNum == 3) {
					// Add asteroids at fun location #2
					for (int i = 0; i < 20; i++) {
						float x = CHECK_POINT_FUN[1][0] + (float) rand.nextGaussian() * 1500;
						float y = CHECK_POINT_FUN[1][1] + (float) rand.nextGaussian() * 1500;

						addObject(new PAsteroidEnemy(this, x, y, 0, 0, rand.nextInt(3) + 1, false));
					}
				} else if (checkPointNum == 4) {
					// Add enemy scouts at fun location #3
					int n = 30;
					AIGroupBasic group = new AIGroupBasic(this, n);
					for (int i = 0; i < n; i++) {
						float x = CHECK_POINT_FUN[2][0] + (float) rand.nextGaussian() * 100;
						float y = CHECK_POINT_FUN[2][1] + (float) rand.nextGaussian() * 100;

						PEnemy en = new PEnemy(this, x, y, 0, 0, rand.nextDouble() > .8 ? PEnemy.CLASS_SHOOTER : PEnemy.CLASS_SCOUT);
						addObject(en);
						group.add(en);
					}
					enemyAIGroups.add(group);
				} else if (checkPointNum == 5) {
					// Add enemy fighters at fun location #3
					int n = 3;
					AIGroupBasic group = new AIGroupBasic(this, n);
					for (int i = 0; i < n; i++) {
						float x = CHECK_POINT_FUN[3][0] + (float) rand.nextGaussian() * 1000;
						float y = CHECK_POINT_FUN[3][1] + (float) rand.nextGaussian() * 1000;

						PEnemy en = new PEnemy(this, x, y, 0, 0, PEnemy.CLASS_FIGHTER);
						addObject(en);
						group.add(en);
					}
					enemyAIGroups.add(group);
				}

			}
		}

		if (dead && lastTime > deadTime + 2000) {
			gbRenderer.loadWorld(this.getClass());
		}

		if (zooming == 0 && lap == 4) {
			msg = "RACE OVER";
			msgAlpha = 1;
			zooming = 1;
			startZoom = lastTime;
		}

		if (lap > 0 && zooming == 0) {
			countdown = Util.FMT.format((double) (lastTime - raceStartTime) / 1000);
		} else {
			countdown = null;
		}

		return dTime;
	}

	private void nextTarget() {
		target = null;

		if (checkStartTime == -1) {
			checkStartTime = 0;
		} else {
			if (checkStartTime > 0)
				score += 2000 - Math.min(lastTime - checkStartTime, 10000) / 20;
			checkStartTime = lastTime;
		}

		checkPointNum++;
		if (checkPointNum >= CHECK_POINTS.length) {
			checkPointNum = 0;
		}

		if (checkPointNum == 0) {
			target = new PLineParticle(CHECK_POINTS[0][0], CHECK_POINTS[0][1], 200, 1780);
		} else {
			// Standard target
			target = new PTargetParticle(CHECK_POINTS[checkPointNum][0], CHECK_POINTS[checkPointNum][1], 1200);
		}

		if (target != null)
			graphicParticles.add(target);
	}

	@Override
	public int getNebulaID() {
		return R.drawable.nebula8;
	}

	@Override
	public int getStarIndex() {
		return 14;
	}

	@Override
	public void laserKilled(PEnemy otherEnemy) {
		score++;
	}

	@Override
	public IntroSequence getIntroSequence() {
		return new TextIntroSequence(TEXTS);
	}

	public long resumeNow() {
		long dTime = super.resumeNow();
		raceStartTime += dTime;
		checkStartTime += dTime;
		return dTime;
	}

	@Override
	public int getLeaderBoardID() {
		return R.string.leaderboard_purple_race;
	}
}
