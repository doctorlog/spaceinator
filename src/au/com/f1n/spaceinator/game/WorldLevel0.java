package au.com.f1n.spaceinator.game;

import java.util.ArrayList;

import android.os.Vibrator;
import au.com.f1n.spaceinator.GBRenderer;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.game.enemy.PEnemy;
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.mesh.intro.IntroSequence;
import au.com.f1n.spaceinator.mesh.intro.Level0IntroSequence;

/**
 * This world is the super-simple driver training
 * 
 * @author luke
 */
public class WorldLevel0 extends World {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int HELIO_SIZE = 2200;
	private PTargetParticle target;
	private int targetNum;
	private long ringStartTime;
	private boolean finger;

	public void init(GBRenderer gbRenderer, Vibrator v) {
		super.init(gbRenderer, v);
		gbRenderer.getSoundManager().playMusic(R.raw.music_training);
	}

	public void start(GameState gameState) {
		super.start(gameState);
		if (centreSpaceShip == null) {
			graphicParticles = new ArrayList<PGraphicParticle>(100);
			objects = new FArrayList<PObject>(MAX_OBJECTS);
			curObjects = new FArrayList<PObject>();
			edgesX = new FArrayList<PEdge>(MAX_OBJECTS);
			planets = new PPlanet[0];

			boundry = new RectBoundry(HELIO_SIZE);

			// Add the spaceship
			centreSpaceShip = new PSpaceShip(this, -HELIO_SIZE - SHIP_START_OFFSET, 0, 0, 0);
			centreSpaceShip.removeWeapon();
			addObject(centreSpaceShip);

			camera = new PStandardCamera(this);

			msg = "WELCOME TO DRIVER TRAINING";
			msgAlpha = 1;
		}
	}

	public int timeStep() {
		int dTime = super.timeStep();
		if (dTime == -1)
			return -1;

		if (zooming == -1)
			return dTime;

		// if (zooming == 0) {
		// score = 1200;
		// zooming = 1;
		// startZoom = lastTime;
		// }

		// Start
		if (targetNum == 0 && lastTime - startTime > 2000 || targetNum == 1
				&& (Math.abs(centreSpaceShip.y) > 100 || Math.abs(centreSpaceShip.x + HELIO_SIZE) > 500)) {
			nextTarget();
			if (targetNum == 1 && Math.abs(centreSpaceShip.y) > 100 && Math.abs(centreSpaceShip.x + HELIO_SIZE) > 100) {
				// Skip
				nextTarget();
			}
		}

		if (target != null) {
			float dx = target.getX() - centreSpaceShip.x;
			float dy = target.getY() - centreSpaceShip.y;
			float dist2 = dx * dx + dy * dy;

			target.setInside(dist2 <= target.getR() * target.getR());

			if (target.getProportion() >= 1) {
				target.explode();
				nextTarget();
			}
		}

		if (zooming == 0 && targetNum == 5) {
			zooming = 1;
			startZoom = lastTime;
		}

		if (targetNum == 1 || targetNum == 0)
			msgAlpha = 1;

		return dTime;
	}

	private void nextTarget() {
		score += scoreTime(lastTime - ringStartTime, targetNum);
		ringStartTime = lastTime;

		target = null;
		switch (targetNum) {
		case 0:
			msg = "DRAG THE LEFT HALF OF THE SCREEN";
			finger = true;
			// Stop moving
			centreSpaceShip.dx = 0;
			break;
		case 1:
			msg = "STAY IN THE BIG CIRCLE!";
			finger = false;
			target = new PTargetParticle(HELIO_SIZE, 0, PSpaceShip.SHIELD_SIZE * 10f);
			break;
		case 2:
			msg = "TWO TRAINING CIRCLES REMAINING...";
			target = new PTargetParticle(-HELIO_SIZE, 0, PSpaceShip.SHIELD_SIZE * 5f);
			break;
		case 3:
			msg = "NOW FOR THE TRICKY ONE!";
			target = new PTargetParticle(HELIO_SIZE, 0, PSpaceShip.SHIELD_SIZE * 3f);
			break;
		case 4:
			msg = "MISSION COMPLETE";
		default:
			break;
		}

		msgAlpha = 1;
		targetNum++;
		if (target != null)
			graphicParticles.add(target);
	}

	private int scoreTime(long time, int targetNum) {
		if (targetNum <= 1)
			return 0;
		return 40 * targetNum + 2 * Math.max(0, 190 - (int) time / 100);
	}

	@Override
	public int getNebulaID() {
		return -1;
	}

	@Override
	public int getStarIndex() {
		return 0;
	}

	@Override
	public void laserKilled(PEnemy otherEnemy) {
	}

	public boolean isFinger() {
		return finger;
	}

	@Override
	public IntroSequence getIntroSequence() {
		return new Level0IntroSequence(gbRenderer);
	}

	public long resumeNow() {
		long dTime = super.resumeNow();
		ringStartTime += dTime;
		return dTime;
	}

	@Override
	public int getLeaderBoardID() {
		return R.string.leaderboard_driver_training;
	}
}
