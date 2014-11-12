package au.com.f1n.spaceinator.game;

import java.util.ArrayList;
import java.util.Random;

import android.os.Vibrator;
import au.com.f1n.spaceinator.GBGLSurfaceView;
import au.com.f1n.spaceinator.GBRenderer;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.enemy.AIGroupCharge;
import au.com.f1n.spaceinator.game.enemy.PAsteroidEnemy;
import au.com.f1n.spaceinator.game.enemy.PEnemy;
import au.com.f1n.spaceinator.game.enemy.PEnemyAIGroup;
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.mesh.intro.IntroSequence;
import au.com.f1n.spaceinator.mesh.intro.TextIntroSequence;

public class WorldBlackHole extends World {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final long ENEMY_SPEED = 70;
	private static final String[][] TEXTS = { { "THE NACHT ARE USING ANDROMEDA'S", "BLACK HOLE TO TRAVEL TO OUR GALAXY!", "REMOVE THE BLACK HOLE AND WE WILL",
			"STOP THE NACHT FOREVER!" } };
	private AIGroupCharge ai;
	private Random rand = new Random();
	private long lastEnemy;
	private PBlackHole blackHole;
	private PTargetParticle curTarget;
	private int targetRadius;
	private int targetSize;
	private long targetTime;

	public void init(GBRenderer gbRenderer, Vibrator v) {
		super.init(gbRenderer, v);
		gbRenderer.getSoundManager().playMusic(R.raw.music_blackhole);
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
			targetScale = GBGLSurfaceView.MAX_SCALE;

			boundry = new BlackHoleBoundry(6000);

			// Add the spaceship
			centreSpaceShip = new PSpaceShip(this, -SHIP_START_OFFSET - 4000, 0, 0, 0);
			addObject(centreSpaceShip);

			camera = new PStandardCamera(this);

			msg = "REDUCE THE BLACK HOLE";
			msgAlpha = 1;
			targetAsteroids = false;
			ai = new AIGroupCharge(this, 250, true);
			enemyAIGroups.add(ai);
			targetAsteroids = true;

			blackHole = new PBlackHole(this, 1000);
			addObject(blackHole);
			updateNewObjects();
		}
	}

	public int timeStep() {
		int dTime = super.timeStep();
		if (dTime == -1)
			return -1;
		((BlackHoleBoundry) boundry).timeStep(dTime, lastTime);

		if (zooming == -1)
			return dTime;

		if (dead && lastTime > deadTime + 2000)
			gbRenderer.loadWorld(this.getClass());

		if (lastTime - lastEnemy > ENEMY_SPEED && objects.size < 250) {
			// Add a random object, flung toward black hole
			float r = 20000;
			double angle = Util.randFloat() * 2 * Math.PI;
			float newX = (float) Math.cos(angle) * r;
			float newY = (float) Math.sin(angle) * r;
			float newDX = newY / 6000f;
			float newDY = newX / 6000f;
			if (rand.nextBoolean())
				newDX *= -1;
			else
				newDY *= -1;

			if (rand.nextFloat() > .8f) {
				// Enemy
				int enemyClass = rand.nextInt(PEnemy.AI_CLASS_COUNT);

				if (enemyClass != PEnemy.CLASS_SPIKE && enemyClass < PEnemy.CLASS_MISSILE) {
					PEnemy enemy = new PEnemy(this, newX, newY, newDX, newDY, enemyClass);
					enemy.drag = 0.99f;
					enemy.setNoWarp();
					enemy.setNoParticledDeath();
					addObject(enemy);
					ai.add(enemy);
				}
			} else {
				// Asteroid
				PAsteroidEnemy enemy = new PAsteroidEnemy(this, newX, newY, newDX, newDY, rand.nextInt(4), true);
				enemy.drag = 0.99f;
				enemy.setNoParticledDeath();
				addObject(enemy);
			}
			lastEnemy = lastTime;
		}

		float g = 0.04f * blackHole.targetRadius / 1000f;
		Object[] arr = objects.array;
		int s = objects.size;
		for (int i = 0; i < s; i++) {
			if (arr[i] instanceof PEnemy) {
				PEnemy en = (PEnemy) arr[i];
				float d = Util.invSqrt(en.x * en.x + en.y * en.y);

				// Simple "gravity"
				d *= g;
				en.dx -= en.x * d;
				en.dy -= en.y * d;
			} else if (arr[i] instanceof LaserParticle) {
				LaserParticle lp = (LaserParticle) arr[i];
				float d = Util.invSqrt(lp.x * lp.x + lp.y * lp.y);

				// Simple "gravity"
				d *= g;
				lp.dx -= lp.x * d * 4;
				lp.dy -= lp.y * d * 4;
			}
		}

		if (curTarget == null) {
			targetRadius = 5000;
			targetSize = 1200;
			targetTime = lastTime;
			curTarget = new PTargetParticle(targetRadius, 0, targetSize);
			graphicEffect(curTarget);
		}

		float dx = centreSpaceShip.x - curTarget.getX();
		float dy = centreSpaceShip.y - curTarget.getY();
		curTarget.setInside(dx * dx + dy * dy < targetSize * targetSize);
		if (curTarget.getProportion() >= 1) {
			curTarget.explode();
			blackHole.targetRadius *= .5f;
			int scoreAdd = 4 * Math.max(0, 190 - (int) (lastTime - targetTime) / 100);
			score += scoreAdd;

			msg = "REDUCE THE BLACK HOLE TO 0";
			msgAlpha = 1;

			// help them out a bit
			((BlackHoleBoundry) boundry).addRadius(200);

			if (blackHole.targetRadius < 1) {
				msg = "MISSION COMPLETE";
				msgAlpha = 1;
				startZoom = lastTime;
				zooming = 1;
			} else {
				targetTime = lastTime;
				double theta = Util.randFloat() * Math.PI * 2;
				if (targetSize > 500)
					targetSize -= 200;
				curTarget = new PTargetParticle((float) Math.cos(theta) * targetRadius, (float) Math.sin(theta) * targetRadius, targetSize);
				graphicEffect(curTarget);
			}
		}

		float d = Util.invSqrt(centreSpaceShip.x * centreSpaceShip.x + centreSpaceShip.y * centreSpaceShip.y);
		centreSpaceShip.dx -= 0.000001f * centreSpaceShip.x * g * d;
		centreSpaceShip.dy -= 0.000001f * centreSpaceShip.y * g * d;

		countdown = "MASS:" + (int) (blackHole.targetRadius);

		return dTime;
	}

	@Override
	public int getNebulaID() {
		return R.drawable.nebula13;
	}

	@Override
	public int getStarIndex() {
		return 18;
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
		lastEnemy += dTime;
		targetTime += dTime;
		return dTime;
	}

	@Override
	public int getLeaderBoardID() {
		return R.string.leaderboard_black_hole;
	}
}
