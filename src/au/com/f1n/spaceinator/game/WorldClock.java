package au.com.f1n.spaceinator.game;

import java.util.ArrayList;
import java.util.Random;

import android.os.Vibrator;
import au.com.f1n.spaceinator.GBRenderer;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.game.enemy.AIGroupBasic;
import au.com.f1n.spaceinator.game.enemy.AIGroupSpike;
import au.com.f1n.spaceinator.game.enemy.PEnemy;
import au.com.f1n.spaceinator.game.enemy.PEnemyAIGroup;
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.mesh.intro.IntroSequence;
import au.com.f1n.spaceinator.mesh.intro.TextIntroSequence;

/**
 * 
 * @author luke
 */
public class WorldClock extends World {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String[] PHASE_NAME = { "TIC TOC", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEN", "ELEVEN", "TWELVE" };
	private static final int[][] ENEMY_PHASE = {
			{},
			{ 10, PEnemy.CLASS_SCOUT },// one
			{ 10, PEnemy.CLASS_SCOUT, 5, PEnemy.CLASS_SHOOTER },// two
			{ 10, PEnemy.CLASS_SCOUT, 5, PEnemy.CLASS_SHOOTER, 1, PEnemy.CLASS_TANK },
			{ 10, PEnemy.CLASS_SCOUT, 2, PEnemy.CLASS_SHOOTER, 1, PEnemy.CLASS_TANK, 1, PEnemy.CLASS_FIGHTER },
			{ 10, PEnemy.CLASS_SCOUT, 1, PEnemy.CLASS_TANK, 2, PEnemy.CLASS_FIGHTER },// five
			{ 25, PEnemy.CLASS_BOMB },// six
			{ 8, PEnemy.CLASS_FIGHTER }, // seven
			{ 5, PEnemy.CLASS_FIGHTER, 5, PEnemy.CLASS_BOMB }, // eight
			{ 15, PEnemy.CLASS_TANK },// nine
			{ 100, PEnemy.CLASS_SCOUT, 7, PEnemy.CLASS_SHOOTER },// ten
			{ 10, PEnemy.CLASS_FIGHTER }, // eleven
			{ 1, PEnemy.CLASS_SPIKE } };
	private static final float BOUNDRY_RAD = 4000;
	private int phase;
	private long phaseTimeStart;
	private int lastKilled;
	private long PHASE_TIME = 7000;
	private Random rand = new Random();

	public void init(GBRenderer gbRenderer, Vibrator v) {
		super.init(gbRenderer, v);
		gbRenderer.getSoundManager().playMusic(R.raw.music_myluck);
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

			// Add the planets
			for (PPlanet p : planets)
				addObject(p);

			camera = new PStandardCamera(this);

			enemyAIGroups = new FArrayList<PEnemyAIGroup>();

			msg = "CLOCK";
			msgAlpha = 1;

			boundry = new CircleBoundry(BOUNDRY_RAD);
		}
	}

	public int timeStep() {
		int dTime = super.timeStep();
		if (dTime == -1)
			return -1;
		if (zooming == -1)
			return dTime;

		if (dead && lastTime - deadTime > 1000 && zooming == 0) {
			// When dead, mission is complete
			zooming = 1;
			startZoom = lastTime;
			phase++;
		}

		int killedEnemyCount = sumEnemyKills();
		score += killedEnemyCount - lastKilled;
		lastKilled = killedEnemyCount;

		if (phase == 0) {
			msg = PHASE_NAME[phase];
			phase++;
			msgAlpha = 1;
		} else if (phase < 12) {
			if (msgAlpha < 0.01 && lastTime - phaseTimeStart > PHASE_TIME && !dead) {
				phaseTimeStart = lastTime;
				addEnemy();
				msg = PHASE_NAME[phase];
				msgAlpha = 1;
				phase++;
			}
		} else if (phase == 12) {
			if (lastTime - phaseTimeStart > PHASE_TIME && !dead) {
				phaseTimeStart = lastTime;
				msg = PHASE_NAME[phase];
				PEnemyAIGroup group = new AIGroupSpike(this, 100);

				PEnemy spike = new PEnemy(this, 0, BOUNDRY_RAD * 1.1f, 0, 0, PEnemy.CLASS_SPIKE);
				group.add(spike);
				addObject(spike);

				enemyAIGroups.add(group);
				msgAlpha = 1;
				phase++;
			}
		} else {
			if (zooming == 0 && lastTime - phaseTimeStart > PHASE_TIME * 4 && !dead) {
				msg = "MISSION COMPLETE!";
				msgAlpha = 1;
				startZoom = lastTime;
				zooming = 1;
			}
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
		int nEnemy = 0;
		int[] enemies = ENEMY_PHASE[phase];

		for (int i = 0; i < enemies.length; i += 2)
			nEnemy += enemies[i];
		PEnemyAIGroup group = new AIGroupBasic(this, nEnemy);

		double angle = Math.PI / 2 - Math.PI * 2 * phase / 12;
		float r = BOUNDRY_RAD * 1.1f;
		for (int i = 0; i < enemies.length; i += 2) {
			for (int j = 0; j < enemies[i]; j++) {

				PEnemy newEnemy = new PEnemy(this, (float) (Math.cos(angle) * r + rand.nextGaussian() * 100),
						(float) (Math.sin(angle) * r + rand.nextGaussian() * 100), 0, 0, enemies[i + 1]);
				group.add(newEnemy);
				addObject(newEnemy);
			}
		}

		enemyAIGroups.add(group);
	}

	@Override
	public int getNebulaID() {
		return R.drawable.nebula9;
	}

	@Override
	public int getStarIndex() {
		return 15;
	}

	@Override
	public void laserKilled(PEnemy otherEnemy) {
		score += otherEnemy.getScore();
	}

	@Override
	public IntroSequence getIntroSequence() {
		int raceStars = 0;
		try {
			raceStars = GameState.starCount(gbRenderer.getGameState().getScoreList(World.STARS[14]).getBest(), World.STARS[14].getStarScores());
		} catch (Exception e) {
		}

		switch (raceStars) {
		case 0:
			return new TextIntroSequence(new String[][] { { "HOW DID WE GET HERE?" } });
		case 1:
			return new TextIntroSequence(new String[][] { { "THE NACHT WERE UNIMPRESSED BY", "YOUR RACE TIME" } });
		case 2:
			return new TextIntroSequence(new String[][] { { "THE NACHT COULD BEAT", "YOUR RACE TIME" } });
		case 3:
			return new TextIntroSequence(new String[][] { { "THE NACHT WERE UNHAPPY WITH", "YOUR RACE TIME" } });
		case 4:
			return new TextIntroSequence(new String[][] { { "THE NACHT WERE AMAZED WITH", "YOUR RACE TIME" } });
		default:
			return new TextIntroSequence(new String[][] { { "THE NACHT COULDN'T BELIEVE", "YOUR RACE TIME" } });
		}
	}
	
	public long resumeNow() {
		long dTime = super.resumeNow();
		phaseTimeStart += dTime;
		return dTime;
	}

	@Override
	public int getLeaderBoardID() {
		return R.string.leaderboard_clock;
	}
}
