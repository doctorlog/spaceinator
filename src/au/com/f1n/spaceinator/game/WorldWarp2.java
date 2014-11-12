package au.com.f1n.spaceinator.game;

import java.util.ArrayList;
import java.util.Random;

import android.os.Vibrator;
import au.com.f1n.spaceinator.GBRenderer;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.enemy.PAsteroidEnemyDrWho;
import au.com.f1n.spaceinator.game.enemy.PEnemy;
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.mesh.intro.IntroSequence;
import au.com.f1n.spaceinator.mesh.intro.TextIntroSequence;

public class WorldWarp2 extends World {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final float BOUNDRY_HEIGHT = 1500;
	private static final float BOUNDRY_WIDTH = BOUNDRY_HEIGHT;
	private static final String[][] TEXTS = { { "WE DEFEATED THE NACHT'S BLACK HOLE!", "WE MUST TRAVEL TO THEIR HOME GALAXY:", "OMEGA 5" } };
	private boolean intro = true;
	private PDrWho drWho;
	private boolean finished;
	private float nextAdd;
	private Random rand = new Random(923764);
	private float nextAY;
	private float nextAX;
	private float dax;
	private float day;

	public void init(GBRenderer gbRenderer, Vibrator v) {
		super.init(gbRenderer, v);
		gbRenderer.getSoundManager().playMusic(R.raw.music_warp);
	}

	public void start(GameState gameState) {
		super.start(gameState);
		if (centreSpaceShip == null) {
			targetScale = 0.7f;
			gbRenderer.setScale(targetScale);
			graphicParticles = new ArrayList<PGraphicParticle>(100);
			objects = new FArrayList<PObject>(MAX_OBJECTS);
			curObjects = new FArrayList<PObject>();
			edgesX = new FArrayList<PEdge>(MAX_OBJECTS);

			planets = new PPlanet[0];

			// Add the spaceship
			centreSpaceShip = new PSpaceShip(this, 0, 0, 0, 0);
			centreSpaceShip.facingIn = true;
			centreSpaceShip.removeWeapon();
			addObject(centreSpaceShip);

			camera = new PStandardCamera(this);

			msg = "ENTERING LIGHT WARP";
			msgAlpha = 1;

			boundry = new CircleBoundry(BOUNDRY_WIDTH);
			boundry.setRed(true);
			drWho = new PDrWho(this, BOUNDRY_WIDTH + Boundry.HELIO_SIZE / 2, R.drawable.warp2);
			drWho.timeStep(0, lastTime);

			zooming = 0;
		}
	}

	public int timeStep() {
		int dTime = super.timeStep();
		if (dTime == -1)
			return -1;
		if (intro) {
			// Now start the level
			centreSpaceShip.setShieldAlpha(1);

			float amt = (float) (lastTime - startTime) / 2000f;
			float amt2 = Util.slowInOut(amt) * 1.5f;
			drWho.stretch(amt2);
			fade = 1f - amt;
			if (amt < .5f) {
				amt2 = Util.slowInOut(amt * 2);
				centreSpaceShip.rotX = -Util.slowInOut(amt * 2) * 90;
				centreSpaceShip.setAccel(0, 0, Util.slowInOut(amt * 2) * Util.PI / 2);
			} else {
				centreSpaceShip.rotX = -90;
			}

			centreSpaceShip.setAccelOff();

			if (fade <= 0) {
				intro = false;
				fade = 0;
				msgAlpha = 1;
				msg = "STAY IN THE WARP!";
				centreSpaceShip.topParticleZoom();
			}
			return dTime;
		} else if (!finished) {
			if (drWho.prop > nextAdd && objects.size + newObjects.size < 150) {
				if (rand.nextFloat() < .009f) {
					// Add a circle
					for (double angle = 0; angle < Math.PI * 2; angle += 0.15708) {
						addObject(new PAsteroidEnemyDrWho(this, BOUNDRY_WIDTH * .8f * (float) Math.cos(angle), BOUNDRY_WIDTH * .8f * (float) Math.sin(angle),
								-120000, 2, drWho));
					}

					nextAdd = drWho.prop + .01f;
				} else {
					if (rand.nextFloat() < .012f) {
						addObject(new PAsteroidEnemyDrWho(this, nextAX * .5f, nextAY * .5f, -103000, 2, drWho));
						nextAX *= -1;
						nextAY *= -1;
						addObject(new PAsteroidEnemyDrWho(this, nextAX * .5f, nextAY * .5f, -103000, 2, drWho));
						addObject(new PAsteroidEnemyDrWho(this, 0, 0, -106000, 2, drWho));
					}

					addObject(new PAsteroidEnemyDrWho(this, nextAX, nextAY, -100000, rand.nextInt(4) + 1, drWho));

					nextAX += dax;
					nextAY += day;

					if (nextAX * nextAX + nextAY * nextAY > BOUNDRY_WIDTH * BOUNDRY_WIDTH * .81f) {
						// reset
						dax = -nextAX * .01f;
						day = -nextAY * .01f;
					}

					if (rand.nextBoolean()) {
						dax += rand.nextGaussian() * 7;
						day += rand.nextGaussian() * 7;
					}

					nextAdd = drWho.prop + .0012f;
				}
			}
		}
		drWho.timeStep(dTime, lastTime);

		// Special case for hitting the boundary
		if (zooming == 0 && centreSpaceShip.x * centreSpaceShip.x + centreSpaceShip.y * centreSpaceShip.y > BOUNDRY_WIDTH * BOUNDRY_WIDTH) {
			centreSpaceShip.curShield -= 12;
			drWho.speed -= .03f;
			if (drWho.speed < 0)
				drWho.speed = 0;
		}

		// Special case for ending the warp
		if (dead) {
			// death
			gbRenderer.loadWorld(getClass());
		}

		if (drWho.finished && !finished) {
			skipLightBoom = true;
			finished = true;
			zooming = 2;
			startZoom = lastTime;
			centreSpaceShip.curShield = 100;
			completed();
			msg = "LEAVING LIGHT WARP";
			msgAlpha = 1;
		}

		if (finished) {
			centreSpaceShip.x *= .98f;
			centreSpaceShip.y *= .98f;
			drWho.straight -= .0002f * dTime;
			if (drWho.straight <= 0) {
				drWho.straight = 0;
			}
		}

		return dTime;
	}

	@Override
	public int getNebulaID() {
		return -1;
	}

	@Override
	public int getStarIndex() {
		return 19;
	}

	@Override
	public void laserKilled(PEnemy otherEnemy) {
		score += otherEnemy.getScore();
	}

	public boolean drawStars() {
		return false;
	}

	public PDrWho getDrWho() {
		return drWho;
	}

	@Override
	public IntroSequence getIntroSequence() {
		return new TextIntroSequence(TEXTS);
	}

	@Override
	public int getLeaderBoardID() {
		return R.string.leaderboard_warp_andromeda;
	}
}
