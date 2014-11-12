package au.com.f1n.spaceinator.game;

import java.util.ArrayList;
import java.util.Random;

import android.os.Vibrator;
import au.com.f1n.spaceinator.GBRenderer;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.enemy.PAsteroidEnemy;
import au.com.f1n.spaceinator.game.enemy.PEnemy;
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.mesh.intro.IntroSequence;
import au.com.f1n.spaceinator.mesh.intro.TextIntroSequence;

/**
 * This world is a simple timed race
 * 
 * @author luke
 */
public class WorldSuperGauntlet extends World {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final float BOUNDRY_WIDTH = 100000;
	private static final float BOUNDRY_HEIGHT = 2000;
	private PLineParticle target;
	private int targetNum;
	private long raceStartTime;
	private Random rand;
	private float maxX;
	private boolean halfway;
	private long segmentTime;
	private String[][] TEXTS = { { "AFTER THE LONG JOURNEY TO", "OMEGA 5, THE NACHT HAVE PLACED", "MORE OBSTACLES", "", "THE SPACEINATOR LAUGHS!" } };

	public void init(GBRenderer gbRenderer, Vibrator v) {
		super.init(gbRenderer, v);
		gbRenderer.getSoundManager().playMusic(R.raw.music_gauntlet);
	}

	public void start(GameState gameState) {
		super.start(gameState);
		if (centreSpaceShip == null) {
			graphicParticles = new ArrayList<PGraphicParticle>(100);
			objects = new FArrayList<PObject>(MAX_OBJECTS);
			curObjects = new FArrayList<PObject>();
			edgesX = new FArrayList<PEdge>(MAX_OBJECTS);
			planets = new PPlanet[0];

			PolyBoundry pb;
			boundry = pb = new PolyBoundry(new int[] { 7, 5, 5 });
			pb.addPoint(-BOUNDRY_WIDTH, -BOUNDRY_HEIGHT);
			pb.addPoint(0, -BOUNDRY_HEIGHT / 2);
			pb.addPoint(BOUNDRY_WIDTH, -BOUNDRY_HEIGHT);
			pb.addPoint(BOUNDRY_WIDTH, BOUNDRY_HEIGHT);
			pb.addPoint(0, BOUNDRY_HEIGHT / 2);
			pb.addPoint(-BOUNDRY_WIDTH, BOUNDRY_HEIGHT);
			pb.addPoint(-BOUNDRY_WIDTH, -BOUNDRY_HEIGHT);
			pb.move();
			float w = BOUNDRY_HEIGHT * 2;
			float h = BOUNDRY_HEIGHT / 3;
			pb.addPoint(-BOUNDRY_WIDTH + w * 1.8f, -h);
			pb.addPoint(-BOUNDRY_WIDTH + w * 2, h);
			pb.addPoint(-BOUNDRY_WIDTH + w * 8, h * .5f);
			pb.addPoint(-BOUNDRY_WIDTH + w * 8, -h * .5f);
			pb.addPoint(-BOUNDRY_WIDTH + w * 1.8f, -h);
			pb.move();
			pb.addPoint(BOUNDRY_WIDTH - w * 8, -h);
			pb.addPoint(BOUNDRY_WIDTH - w * 8.2f, h);
			pb.addPoint(BOUNDRY_WIDTH - w * 2, h * .2f);
			pb.addPoint(BOUNDRY_WIDTH - w * 2, -h * .2f);
			pb.addPoint(BOUNDRY_WIDTH - w * 8, -h);

			// Add the spaceship
			centreSpaceShip = new PSpaceShip(this, -BOUNDRY_WIDTH - SHIP_START_OFFSET + PSpaceShip.SHIELD_SIZE * 4, 0, 0, 0);
			addObject(centreSpaceShip);

			camera = new PStandardMovingCamera(this, -BOUNDRY_WIDTH + PSpaceShip.SHIELD_SIZE * 4);
			msg = "SUPER GAUNTLET";
			msgAlpha = 1;
			rand = new Random(69);
			maxX = -BOUNDRY_WIDTH;
			targetAsteroids = true;
		}
	}

	public int timeStep() {
		int dTime = super.timeStep();
		if (dTime == -1)
			return -1;
		if (zooming == -1)
			return dTime;
		// Start
		if (targetNum == 0 && lastTime - startTime > 1000) {
			nextTarget();
		}

		if (dead && lastTime > deadTime + 2000) {
			gbRenderer.loadWorld(this.getClass());
		}

		if (target != null) {
			if (centreSpaceShip.x > target.getX()) {
				target.explode();
				nextTarget();
			}
		}

		if (zooming == 0 && targetNum == 3) {
			zooming = 1;
			startZoom = lastTime;
		}

		if (targetNum == 1 || targetNum == 0)
			msgAlpha = 1;

		if (targetNum == 2) {
			countdown = "" + Util.FMT.format((double) (lastTime - raceStartTime) / 1000);
		} else {
			countdown = null;
		}

		if (targetNum == 2 && centreSpaceShip.x - maxX > 1000) {
			addObject(new PAsteroidEnemy(this, maxX + BOUNDRY_HEIGHT * 3 + rand.nextFloat() * BOUNDRY_HEIGHT, (2 * rand.nextFloat() - 1) * BOUNDRY_HEIGHT,
					rand.nextFloat() * 4 - 2, rand.nextFloat() * 4 - 2, 3, false));
			if (centreSpaceShip.x > -BOUNDRY_WIDTH / 2)
				addObject(new PAsteroidEnemy(this, maxX + BOUNDRY_HEIGHT * 3 + rand.nextFloat() * BOUNDRY_HEIGHT, (2 * rand.nextFloat() - 1) * BOUNDRY_HEIGHT,
						rand.nextFloat() * 4 - 2, rand.nextFloat() * 4 - 2, 2, false));
			if (centreSpaceShip.x > 0) {
				addObject(new PAsteroidEnemy(this, maxX + BOUNDRY_HEIGHT * 3 + rand.nextFloat() * BOUNDRY_HEIGHT, (2 * rand.nextFloat() - 1) * BOUNDRY_HEIGHT,
						rand.nextFloat() * 4 - 2, rand.nextFloat() * 4 - 2, 1, false));
				if (!halfway) {
					halfway = true;
					msg = "HALF WAY...";
					msgAlpha = 1;
				}
			}
			if (centreSpaceShip.x > BOUNDRY_WIDTH / 2)
				addObject(new PAsteroidEnemy(this, maxX + BOUNDRY_HEIGHT * 3 + rand.nextFloat() * BOUNDRY_HEIGHT, (2 * rand.nextFloat() - 1) * BOUNDRY_HEIGHT,
						rand.nextFloat() * 4 - 2, rand.nextFloat() * 4 - 2, 4, false));

			cullOld();

			if (segmentTime == 0)
				segmentTime = lastTime;

			score += 20 - Math.min(20, (lastTime - segmentTime) / 50);

			segmentTime = lastTime;

			maxX = Math.max(centreSpaceShip.x, maxX);
		}

		return dTime;
	}

	private void cullOld() {
		for (int i = 0; i < objects.size; i++) {
			PObject obj = (PObject) objects.array[i];
			if (obj.x < maxX - BOUNDRY_HEIGHT * 4) {
				removeObject(obj, i);
			}
		}
	}

	private void nextTarget() {
		target = null;
		switch (targetNum) {
		case 0:
			msg = "GO GO GO!";
			target = new PLineParticle(-BOUNDRY_WIDTH + BOUNDRY_HEIGHT * 2, 0, 200, BOUNDRY_HEIGHT * 2);
			break;
		case 1:
			msg = "KEEP GOING!";
			raceStartTime = lastTime;
			target = new PLineParticle(BOUNDRY_WIDTH - 200, 0, 200, BOUNDRY_HEIGHT * 2);
			break;
		case 2:
			msg = "GAUNTLET OVER";
		default:
			break;
		}

		msgAlpha = 1;
		targetNum++;
		if (target != null)
			graphicParticles.add(target);
	}

	@Override
	public int getNebulaID() {
		return R.drawable.nebulawide;
	}

	@Override
	public int getStarIndex() {
		return 20;
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
		segmentTime += dTime;
		return dTime;
	}

	@Override
	public int getLeaderBoardID() {
		return R.string.leaderboard_super_gauntlet;
	}
}
