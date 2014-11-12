package au.com.f1n.spaceinator.game;

import java.util.ArrayList;

import android.os.Vibrator;
import au.com.f1n.spaceinator.GBGLSurfaceView;
import au.com.f1n.spaceinator.GBRenderer;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.enemy.PEnemy;
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.mesh.intro.IntroSequence;
import au.com.f1n.spaceinator.mesh.intro.TextIntroSequence;

public class WorldWarp1 extends World {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final float BOUNDRY_HEIGHT = 1000;
	private static final float BOUNDRY_WIDTH = BOUNDRY_HEIGHT;
	private static final String[][] TEXTS = {
			{ "AFTER DEFEATING SPIKE,", "WE REALISED THEY WOULD ONLY", "COME BACK. WE MUST TRAVEL", "TO THE CLOSEST GALAXY, ANDROMEDA." },
			{ "LARGE AMOUNTS OF LIGHT SPEED", "TRAVEL CAN HAVE AN INTERESTING", "EFFECT ON MERE HUMANS..." } };
	private boolean intro = true;
	private PDrWho drWho;
	private boolean finished;

	public void init(GBRenderer gbRenderer, Vibrator v) {
		super.init(gbRenderer, v);
		gbRenderer.getSoundManager().playMusic(R.raw.music_warp);

		if (drWho != null) {
			// drWho.stretch(1);
			centreSpaceShip.rotX = -90;
		}
	}

	public void start(GameState gameState) {
		super.start(gameState);
		if (centreSpaceShip == null) {
			targetScale = GBGLSurfaceView.MIN_SCALE;
			gbRenderer.setScale(targetScale);
			graphicParticles = new ArrayList<PGraphicParticle>(100);
			objects = new FArrayList<PObject>(MAX_OBJECTS);
			curObjects = new FArrayList<PObject>();
			edgesX = new FArrayList<PEdge>(MAX_OBJECTS);

			planets = new PPlanet[0];

			if (drWho == null) {
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

				drWho = new PDrWho(this, BOUNDRY_WIDTH + Boundry.HELIO_SIZE / 2, R.drawable.warp);
				drWho.timeStep(0, lastTime);
				zooming = 0;
			}
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
			float amt2 = Util.slowInOut(amt);
			drWho.stretch(amt2);
			fade = 1f - amt;
			if (amt < .5f) {
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
		return 9;
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
		return R.string.leaderboard_warp_milky_way;
	}
}
