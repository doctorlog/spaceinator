package au.com.f1n.spaceinator.game;

import java.util.ArrayList;
import java.util.Random;

import android.os.Vibrator;
import au.com.f1n.spaceinator.GBRenderer;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.enemy.AIGroupBasic;
import au.com.f1n.spaceinator.game.enemy.PAsteroidEnemyOrbiting;
import au.com.f1n.spaceinator.game.enemy.PEnemy;
import au.com.f1n.spaceinator.game.enemy.PEnemyAIGroup;
import au.com.f1n.spaceinator.game.enemy.PEnemyMissile;
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.game.logic.PolyReader;
import au.com.f1n.spaceinator.mesh.intro.IntroSequence;
import au.com.f1n.spaceinator.mesh.intro.TextIntroSequence;

/**
 * This world is a simple timed race
 * 
 * @author luke
 */
public class WorldRace2 extends World {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final float[][] CHECK_POINTS = { { -4567, 1879 }, { 7000, 3500, 1370 }, { 12600, -333, 1530 }, { 0, -8698, 1140 }, { -9725, -4000, 1052 },
			{ -2080, -4539, 937 } };
	private static final float[][] CHECK_POINT_FUN = { { 10000, 3387 }, { 13450, -3500 }, { 7839, -3200 }, { -6288, -8000 }, { -7000, -7000 } };
	private static final float[] SPECIAL = { 12857, -7900, PSpaceShip.SHIELD_SIZE * 2 };
	private static final String[][] TEXTS = { { "THE NACHT'S MOST DIFFICULT RACE" } };

	private PGraphicParticle target;
	private int checkPointNum = -1;
	private long raceStartTime;
	private long checkStartTime = -1;
	private Random rand;
	private int lap;
	private PTargetParticle special;
	private long boostTime;
	private float boostReset;

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

			planets = new PPlanet[4];
			planets[0] = new PPlanet(R.drawable.sun, 1e11f, 0, 2100, 0, new byte[] { (byte) 239, (byte) 241, (byte) 150, (byte) 255 }, new byte[] { (byte) 239,
					(byte) 20, (byte) 12, (byte) 0 }, 2f, new float[] { 1, .95f, .4f, 1 }, false);
			planets[1] = new PPlanet(R.drawable.neptune, 1.6e10f, 0.00115f, 1200, 10000, null, null, 1, false);
			planets[2] = new PPlanet(R.drawable.earth, 2e10f, -0.01f, 800, 8000, null, null, 1, false);
			planets[3] = new PPlanet(R.drawable.venus, 1e10f, -0.024f, 600, 5100, null, null, 1, false);

			// Add the planets
			for (PPlanet p : planets)
				addObject(p);

			PolyReader pr = new PolyReader(gbRenderer.getContext().getResources().openRawResource(R.raw.track2));
			boundry = new PolyBoundry(pr.getPoints(), 1);
			pr = null;

			// Add the spaceship
			centreSpaceShip = new PSpaceShip(this, -SHIP_START_OFFSET - 5500, 0, 0, 0);
			addObject(centreSpaceShip);

			camera = new PStandardCamera(this);

			msg = "AMBER RACE";
			msgAlpha = 1;
			rand = new Random(69);
			targetAsteroids = true;

			// Add orbiting asteroids
			for (int i = 0; i < 50; i++) {
				double angle = rand.nextDouble() * Math.PI * 2;
				float r = 2600 + rand.nextFloat() * 800;
				addObject(new PAsteroidEnemyOrbiting(this, (float) Math.cos(angle) * r, (float) (Math.sin(angle) * r), 0, 0, rand.nextInt(3), true));
			}

			// Add the speed boosters
			graphicEffect(new SpeedBoost(this, -5000, 3200, 0));
			graphicEffect(new SpeedBoost(this, -3500, 3200, 0));
			graphicEffect(new SpeedBoost(this, -2100, 3200, 0));
			graphicEffect(new SpeedBoost(this, -800, 3200, 0));
			graphicEffect(new SpeedBoost(this, 400, 3200, 0));
			graphicEffect(new SpeedBoost(this, 1500, 3200, 0));
			graphicEffect(new SpeedBoost(this, 2500, 3200, 0));
			graphicEffect(new SpeedBoost(this, 3400, 3200, 0));

			// Add special
			special = new PTargetParticle(SPECIAL[0], SPECIAL[1], SPECIAL[2]);
			graphicEffect(special);
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
		}

		if (checkPointNum == 0 && centreSpaceShip.y > -0.52474 * centreSpaceShip.x - 596.45) {
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
			float r = CHECK_POINTS[checkPointNum][2];
			if (dx * dx + dy * dy < r * r) {
				// just passed a normal target
				target.explode();
				nextTarget();

				if (checkPointNum == 2) {
					// Add enemies at fun location #1
					int n = 30;
					AIGroupBasic group = new AIGroupBasic(this, n);
					for (int i = 0; i < n; i++) {
						float x = CHECK_POINT_FUN[0][0] + (float) rand.nextGaussian() * 100;
						float y = CHECK_POINT_FUN[0][1] + (float) rand.nextGaussian() * 100;

						PEnemy en = new PEnemy(this, x, y, 0, 0, rand.nextDouble() > .8 ? PEnemy.CLASS_SHOOTER : PEnemy.CLASS_SCOUT);
						addObject(en);
						group.add(en);
					}
					enemyAIGroups.add(group);
				} else if (checkPointNum == 3) {
					// Add enemies at fun location #2 and 3
					int n = 2;
					AIGroupBasic group = new AIGroupBasic(this, n * 2);
					for (int i = 0; i < n; i++) {
						float x = CHECK_POINT_FUN[1][0] + (float) rand.nextGaussian() * 100;
						float y = CHECK_POINT_FUN[1][1] + (float) rand.nextGaussian() * 100;

						PEnemy en = new PEnemy(this, x, y, 0, 0, PEnemy.CLASS_FIGHTER);
						addObject(en);
						group.add(en);
					}
					for (int i = 0; i < n; i++) {
						float x = CHECK_POINT_FUN[2][0] + (float) rand.nextGaussian() * 100;
						float y = CHECK_POINT_FUN[2][1] + (float) rand.nextGaussian() * 100;

						PEnemy en = new PEnemy(this, x, y, 0, 0, PEnemy.CLASS_FIGHTER);
						addObject(en);
						group.add(en);
					}
					enemyAIGroups.add(group);
				} else if (checkPointNum == 4) {
					// Add MISSILES
					int n = 20;
					for (int i = 0; i < n; i++) {
						float x = CHECK_POINT_FUN[3][0] + (float) rand.nextGaussian() * 2000;
						float y = CHECK_POINT_FUN[3][1] + (float) rand.nextGaussian() * 2000;

						PEnemy en = new PEnemyMissile(this, x, y, rand.nextFloat() * Util.PI * 2, rand.nextFloat() * Util.PI * 2, rand.nextFloat() * Util.PI * 2,
								lastTime);
						addObject(en);
					}
				} else if (checkPointNum == 5) {
					// Add enemy fighters at fun location #4
					int n = 10;
					AIGroupBasic group = new AIGroupBasic(this, n);
					for (int i = 0; i < n; i++) {
						float x = CHECK_POINT_FUN[3][0] + (float) rand.nextGaussian() * 1000;
						float y = CHECK_POINT_FUN[3][1] + (float) rand.nextGaussian() * 1000;

						PEnemy en = new PEnemy(this, x, y, 0, 0, PEnemy.CLASS_TANK);
						addObject(en);
						group.add(en);
					}
					enemyAIGroups.add(group);
				}

			}
		}

		// Check special location
		if (special != null) {
			float dx = centreSpaceShip.x - SPECIAL[0];
			float dy = centreSpaceShip.y - SPECIAL[1];
			if (dx * dx + dy * dy < SPECIAL[2] * SPECIAL[2]) {
				special.setInside(true);
				if (special.getProportion() >= 1) {
					special.explode();
					boostTime = lastTime;
					boostReset = centreSpaceShip.accel;
					centreSpaceShip.shipParticles.setBoost(true);
					centreSpaceShip.accel = 0.0015f;
					msg = "BOOST";
					msgAlpha = 1;
					special = null;
				}
			} else {
				special.setInside(false);
			}
		} else if (lastTime - boostTime > 10000) {
			centreSpaceShip.accel = boostReset;
			msg = "BOOST OVER";
			centreSpaceShip.shipParticles.setBoost(false);
			msgAlpha = 1;
			boostTime = Long.MAX_VALUE;
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
			target = new PLineParticle(CHECK_POINTS[0][0], CHECK_POINTS[0][1], 200, 4504);
			((PLineParticle) target).zRot = 62.366f;
		} else {
			// Standard target
			target = new PTargetParticle(CHECK_POINTS[checkPointNum][0], CHECK_POINTS[checkPointNum][1], CHECK_POINTS[checkPointNum][2]);
		}

		if (target != null)
			graphicParticles.add(target);
	}

	@Override
	public int getNebulaID() {
		return R.drawable.nebula11;
	}

	@Override
	public int getStarIndex() {
		return 16;
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
		return R.string.leaderboard_amber_race;
	}
}
