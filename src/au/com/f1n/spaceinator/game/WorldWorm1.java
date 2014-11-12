package au.com.f1n.spaceinator.game;

import java.util.ArrayList;
import java.util.Random;

import android.os.Vibrator;
import au.com.f1n.spaceinator.GBRenderer;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.enemy.AIGroupBasic;
import au.com.f1n.spaceinator.game.enemy.AIWormEnemy;
import au.com.f1n.spaceinator.game.enemy.PAsteroidEnemy;
import au.com.f1n.spaceinator.game.enemy.PEnemy;
import au.com.f1n.spaceinator.game.enemy.PEnemyAIGroup;
import au.com.f1n.spaceinator.game.enemy.PWormEnemy;
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.game.logic.PolyReader;
import au.com.f1n.spaceinator.mesh.intro.IntroSequence;
import au.com.f1n.spaceinator.mesh.intro.TextIntroSequence;

/**
 * This world is the ultimate wormy goodness
 * 
 * @author luke
 */
public class WorldWorm1 extends World {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String[][] TEXTS = { { "IN A FEEBLE ATTEMPT TO DEFEAT", "THE SPACEINATOR,THE NACHT", "CREATED 'THE WORM'" } };
	private Random rand;
	private boolean fightersAdded;
	private boolean shootersAdded;
	private boolean wormAdded;
	private boolean wormHolesAdded;
	private boolean saidHello;
	private AIWormEnemy wormAI;
	private long segmentStartTime;

	public void init(GBRenderer gbRenderer, Vibrator v) {
		super.init(gbRenderer, v);
		gbRenderer.getSoundManager().playMusic(R.raw.music_boss);
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

			PolyReader pr = new PolyReader(gbRenderer.getContext().getResources().openRawResource(R.raw.worm1));
			boundry = new PolyBoundry(pr.getPoints(), 1);
			pr = null;

			// Add the spaceship
			centreSpaceShip = new PSpaceShip(this, -SHIP_START_OFFSET, 0, 0, 0);
			// centreSpaceShip = new PSpaceShip(this, -SHIP_START_OFFSET + 31597,
			// -4198, 0, 0);
			addObject(centreSpaceShip);

			camera = new PStandardCamera(this);

			msg = "ALIEN WORM";
			msgAlpha = 1;
			rand = new Random(69);
			targetAsteroids = false;

			addObject(new PAsteroidEnemy(this, 3160, 1440, 0, 0, 6, true));

			// Add asteroids at start
			for (int x = 5170; x < 16200; x += 100) {
				addObject(new PAsteroidEnemy(this, x, -0.6359f * x + 3000 + (float) rand.nextGaussian() * 1000, 0, 0, 1, true));
			}
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

		if (!saidHello) {
			saidHello = true;
			msg = "THIS ASTEROID IS BLOCKING OUR PATH";
			msgAlpha = 1;
		}

		if (!fightersAdded) {
			float dx = centreSpaceShip.x - 17600;
			float dy = centreSpaceShip.y + 12555;

			if (dx * dx + dy * dy < 3695 * 3695) {
				msg = "KILL THOSE FIGHTERS!";
				msgAlpha = 1;

				fightersAdded = true;
				AIGroupBasic ai = new AIGroupBasic(this, 7);
				for (int i = 0; i < 7; i++) {
					PEnemy fighter = new PEnemy(this, 17600 + rand.nextFloat(), -12000 + rand.nextFloat(), 0, 0, PEnemy.CLASS_FIGHTER);
					ai.add(fighter);
					addObject(fighter);
				}
				enemyAIGroups.add(ai);
			}
		}

		if (!shootersAdded) {
			float dx = centreSpaceShip.x - 29355;
			float dy = centreSpaceShip.y + 9308;

			if (dx * dx + dy * dy < 5256 * 5256) {
				msg = "KEEP GOING...";
				msgAlpha = 1;

				shootersAdded = true;
				AIGroupBasic ai = new AIGroupBasic(this, 50);
				for (int i = 0; i < 50; i++) {
					PEnemy en = new PEnemy(this, 29355 + rand.nextFloat(), -9308 + rand.nextFloat(), 0, 0, rand.nextBoolean() ? PEnemy.CLASS_SCOUT
							: PEnemy.CLASS_SHOOTER);
					ai.add(en);
					addObject(en);
				}
				enemyAIGroups.add(ai);
			}
		}

		if (!wormAdded) {
			float dx = centreSpaceShip.x - 31597;
			float dy = centreSpaceShip.y + 4198;

			if (dx * dx + dy * dy < 2152 * 2152) {
				msg = "LOOK OUT! AN ALIEN WORM!";
				msgAlpha = 1;

				wormAI = new AIWormEnemy(this, 31597, 0);

				enemyAIGroups.add(wormAI);
				wormAdded = true;
			}
		} else if (!wormHolesAdded && wormAI.getSplits() > 0) {
			msg = "HE SPLIT IN TWO! GET THEM!";
			msgAlpha = 1;
			// Add worm holes
			graphicEffect(new PWormHole(this, 500, 0, 200, 30000, 2000));
			graphicEffect(new PWormHole(this, 7250, 5475, 500, 57180, -4360));
			graphicEffect(new PWormHole(this, 56464, -1795, 200, 47310, 2000));
			graphicEffect(new PWormHole(this, 45000, 3750, 200, 15000, -9000));
			graphicEffect(new PWormHole(this, 21400, -13550, 200, 39806, -8329));

			// Add the speed boosters...
			graphicEffect(new SpeedBoost(this, 26000, -1500, -173 * Util.PI / 180));
			graphicEffect(new SpeedBoost(this, 25000, -1600, -174 * Util.PI / 180));
			graphicEffect(new SpeedBoost(this, 24000, -1700, -Util.PI));
			graphicEffect(new SpeedBoost(this, 23000, -1700, -202 * Util.PI / 180));
			graphicEffect(new SpeedBoost(this, 22000, -1500, -208 * Util.PI / 180));
			graphicEffect(new SpeedBoost(this, 21060, -1146, -225 * Util.PI / 180));
			graphicEffect(new SpeedBoost(this, 20357, -442, -260 * Util.PI / 180));

			// RHS speed boosters
			graphicEffect(new SpeedBoost(this, 38805, -8195, -215 * Util.PI / 180));
			graphicEffect(new SpeedBoost(this, 37986, -7630, -222 * Util.PI / 180));
			graphicEffect(new SpeedBoost(this, 37232, -6974, -234 * Util.PI / 180));
			graphicEffect(new SpeedBoost(this, 36642, -6165, -240 * Util.PI / 180));
			wormHolesAdded = true;
		}

		if (wormAdded) {
			countdown = "WORM SEGMENTS:" + wormAI.getSegmentCount();
		}

		if (zooming == 0 && wormAI != null && wormAI.isEmpty()) {
			msg = "WORM DEFEATED!";
			msgAlpha = 1;
			zooming = 1;
			startZoom = lastTime;
		}

		return dTime;
	}

	@Override
	public int getNebulaID() {
		return R.drawable.nebula12;
	}

	@Override
	public int getStarIndex() {
		return 17;
	}

	@Override
	public void laserKilled(PEnemy otherEnemy) {
		if (otherEnemy instanceof PWormEnemy) {
			// Segment speed bonus
			if (segmentStartTime != 0)
				score += Math.max(0, 600 - (lastTime - segmentStartTime) / 100);
			segmentStartTime = lastTime;
		} else {
			score++;
		}
	}

	@Override
	public IntroSequence getIntroSequence() {
		return new TextIntroSequence(TEXTS);
	}

	public long resumeNow() {
		long dTime = super.resumeNow();
		segmentStartTime += dTime;
		return dTime;
	}

	@Override
	public int getLeaderBoardID() {
		return R.string.leaderboard_alien_worm;
	}
}
