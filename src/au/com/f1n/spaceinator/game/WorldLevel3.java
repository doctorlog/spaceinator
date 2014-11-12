package au.com.f1n.spaceinator.game;

import java.util.ArrayList;
import java.util.Random;

import android.os.Vibrator;
import au.com.f1n.spaceinator.GBGLSurfaceView;
import au.com.f1n.spaceinator.GBRenderer;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.game.enemy.PAsteroidEnemy;
import au.com.f1n.spaceinator.game.enemy.PEnemy;
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.mesh.intro.IntroSequence;
import au.com.f1n.spaceinator.mesh.intro.TextIntroSequence;

public class WorldLevel3 extends World {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int N = 200;
	private static final float BOUNDRY_HEIGHT = 6000;
	private static final float BOUNDRY_WIDTH = BOUNDRY_HEIGHT * RectBoundry.RECT_RATIO;
	private static final String[][] TEXTS = { { "THE LATEST PLASMA TECHNOLOGY", "WAS INCORPORATED INTO THE", "SPACEINATOR'S OFFENCE BUT",
			"COMES AT A LARGE RESOURCE COST" } };
	private int phase = -1;
	private int killedAsteroids;
	private int lastScore;
	private long startPhase;
	private Random r = new Random(1);
	private int finger;
	private boolean firedPlasma;
	private boolean firedShield;
	private WeaponShipLaser tmpWeapon;
	private int turretCount;

	public void init(GBRenderer gbRenderer, Vibrator v) {
		super.init(gbRenderer, v);
		gbRenderer.getSoundManager().playMusic(R.raw.music_training);
	}

	public void start(GameState gameState) {
		super.start(gameState);
		if (centreSpaceShip == null) {
			targetAsteroids = true;
			targetScale = GBGLSurfaceView.MAX_SCALE;
			graphicParticles = new ArrayList<PGraphicParticle>(100);
			objects = new FArrayList<PObject>(MAX_OBJECTS);
			curObjects = new FArrayList<PObject>();
			edgesX = new FArrayList<PEdge>(MAX_OBJECTS);
			// enemyAIGroups = new FArrayList<PEnemyAIGroup>(10);

			planets = new PPlanet[0];

			// Add the spaceship
			centreSpaceShip = new PSpaceShip(this, -SHIP_START_OFFSET, 0, 0, 0);
			addObject(centreSpaceShip);
			tmpWeapon = centreSpaceShip.getCurWeapon();
			turretCount = centreSpaceShip.getTurretCount();
			centreSpaceShip.setTurretCount(0);
			// Add back later
			centreSpaceShip.removeWeapon();

			// Add the planets
			for (PPlanet p : planets)
				addObject(p);

			msg = "ASTEROID CHALLENGE " + N;
			msgAlpha = 1;

			camera = new PStandardCamera(this);

			gameState.bonusUpgrades();

			boundry = new RectBoundry(BOUNDRY_HEIGHT);
			addAsteroids();
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

		killedAsteroids = sumKills();

		switch (phase) {
		case -1:
			phase++;
			break;
		case 0:
			msg = "SHIELD CELLS INCREASE YOUR SHIELD";
			msgAlpha = 1;
			centreSpaceShip.dx = 0;
			centreSpaceShip.dy = 0;
			finger = 2;
			if (firedShield) {
				phase++;
				score += 100;
			}
			break;
		case 1:

			msg = "PLASMA WAVES DAMAGE ALL NEARBY TARGETS";
			msgAlpha = 1;
			centreSpaceShip.dx = 0;
			centreSpaceShip.dy = 0;
			finger = 1;
			if (firedPlasma) {
				phase++;
				score += 100;
			}
			break;
		case 2:
			centreSpaceShip.setCurWeapon(tmpWeapon);
			tmpWeapon = null;
			centreSpaceShip.setTurretCount(turretCount);
			msg = "THERE ARE " + N + " ASTEROIDS.";
			startPhase = lastTime;
			msgAlpha = 1;

			finger = 0;
			phase++;
			break;
		case 3:
			msg = "YOU HAVE 30 SECONDS.";
			msgAlpha = 1;
			phase++;
			break;
		case 4:
			score += killedAsteroids - lastScore;
			lastScore = killedAsteroids;

			int sec = 30 - (int) (lastTime - startPhase) / 1000;
			if (sec <= 0 && !isDead()) {
				countdown = null;
				phase++;
				msg = "MISSION COMPLETE";
				msgAlpha = 1;
			} else {
				countdown = "" + sec;
			}
			break;
		case 5:
			if (zooming == 0) {
				startZoom = lastTime;
				zooming = 1;
			}
		default:
			break;
		}

		return dTime;
	}

	private void addAsteroids() {
		for (int a = 0; a < N; a++) {
			float x = 0;
			float y = 0;
			boolean hit = true;

			while (hit) {
				// Make new coordinates
				x = r.nextInt((int) (BOUNDRY_WIDTH * 2)) - BOUNDRY_WIDTH;
				y = r.nextInt((int) (BOUNDRY_HEIGHT * 2)) - BOUNDRY_HEIGHT;
				// Check if they would hit something
				int n = newObjects.size;
				hit = false;
				for (int i = 0; i < n; i++) {
					PObject o = (PObject) newObjects.array[i];
					float dx = x - o.x;
					float dy = y - o.y;
					if (dx * dx + dy * dy < 400 * 400) {
						hit = true;
						break;
					}
				}
			}

			// could be up to a level 5 asteroid (with very low probability)
			addObject(new PAsteroidEnemy(this, x, y, 0, 0, 2 + r.nextInt(2) + (r.nextDouble() < .05 ? 1 : 0), true));
		}
		updateNewObjects();
	}

	@Override
	public int getNebulaID() {
		return R.drawable.nebula14;
	}

	@Override
	public int getStarIndex() {
		return 3;
	}

	@Override
	public void laserKilled(PEnemy otherEnemy) {
		score++;
	}

	public int getFinger() {
		return finger;
	}

	public void firePlasma() {
		if (phase == -1 || !firedShield)
			return;
		firedPlasma = true;
		// Note: I can't call the super method because the ship will have no
		// weapon
		if (!dead && zooming == 0 && lastTime - lastPlasma > 2000 && gameState.spendPlasma()) {
			addObject(new PPlasmaParticle(this, centreSpaceShip.x, centreSpaceShip.y, true));
			lastPlasma = lastTime;
			gbRenderer.getSoundManager().plasma();
			spentPlasma++;
		}
	}

	public void fireShieldRegen() {
		if (phase == -1)
			return;
		firedShield = true;
		super.fireShieldRegen();
	}

	public void unloading() {
		if (!firedPlasma)
			gameState.spendPlasma();
		if (!firedShield)
			gameState.spendShieldCell();
	}

	@Override
	public IntroSequence getIntroSequence() {
		return new TextIntroSequence(TEXTS);
	}

	public float canPlasma() {
		if (!dead && zooming == 0 && gameState.getShipUpgrades()[GameState.PLASMA_WAVE] > 0) {
			if (lastTime - lastPlasma > PLASMA_RECHARGE)
				return 1;
			return (float) (lastTime - lastPlasma) / PLASMA_RECHARGE;
		}
		return 0;
	}

	public long resumeNow() {
		long dTime = super.resumeNow();
		if (tmpWeapon != null)
			tmpWeapon.setSoundManager(gbRenderer.getSoundManager());
		startPhase += dTime;
		return dTime;
	}

	@Override
	public int getLeaderBoardID() {
		return R.string.leaderboard_asteroid_challenge;
	}
}
