package au.com.f1n.spaceinator.game;

import java.util.ArrayList;
import java.util.Random;

import android.os.Vibrator;
import au.com.f1n.spaceinator.GBGLSurfaceView;
import au.com.f1n.spaceinator.GBRenderer;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.game.enemy.PAsteroidEnemyOrbiting;
import au.com.f1n.spaceinator.game.enemy.PEnemy;
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.mesh.intro.IntroSequence;
import au.com.f1n.spaceinator.mesh.intro.TextIntroSequence;

public class WorldUltraRift extends World {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final float BOUNDRY_RADIUS = 15000;
	private static final String[][] TEXTS = { { "THE CREW COULDN'T BELIEVE THEIR EYES" }, { "SO MANY ASTEROIDS!" } };
	private int phase;
	private long phaseTimeStart;

	public void init(GBRenderer gbRenderer, Vibrator v) {
		super.init(gbRenderer, v);
		gbRenderer.getSoundManager().playMusic(R.raw.music_mantilla);
	}

	public void start(GameState gameState) {
		super.start(gameState);
		if (centreSpaceShip == null) {
			targetScale = GBGLSurfaceView.MAX_SCALE;
			graphicParticles = new ArrayList<PGraphicParticle>(100);
			objects = new FArrayList<PObject>(MAX_OBJECTS);
			curObjects = new FArrayList<PObject>();
			edgesX = new FArrayList<PEdge>(MAX_OBJECTS);

			targetAsteroids = true;

			planets = new PPlanet[0];

			// Add the spaceship
			centreSpaceShip = new PSpaceShip(this, -7500 - SHIP_START_OFFSET, 0, 0, 0);
			addObject(centreSpaceShip);

			// Add the planets
			for (PPlanet p : planets)
				addObject(p);

			camera = new PStandardCamera(this);

			boundry = new CircleBoundry(BOUNDRY_RADIUS);
			// Add initial asteroids
			Random rand = new Random(69);
			for (int i = 0; i < 350; i++) {
				double angle = rand.nextDouble() * Math.PI * 2;
				float r = (float) rand.nextGaussian() * BOUNDRY_RADIUS * .75f;

				float newx = (float) Math.cos(angle) * r;
				float newy = (float) Math.sin(angle) * r;

				boolean collide = false;

				if (r > BOUNDRY_RADIUS * 1.1f || r < 3200)
					collide = true;
				else
					// Check collision
					for (int o = 0; o < objects.size; o++) {
						PObject ob = (PObject) objects.array[o];
						float dx = ob.x - newx;
						float dy = ob.y - newy;
						if (dx * dx + dy * dy < 500 * 500) {
							collide = true;
							break;
						}
					}

				if (!collide)
					addObject(new PAsteroidEnemyOrbiting(this, newx, newy, 0, 0, Math.max(1, (int) (r / 4500)), r > 6000 && r < 12000));
				else
					i--;
			}

			for (double angle = 0; angle < Math.PI * 2; angle += Math.PI * 2 / 3)
				addObject(new PAsteroidEnemyOrbiting(this, (float) Math.cos(angle) * 2000, (float) Math.sin(angle) * 2000, 0, 0, 6, true));

			msg = "ULTRA RIFT";
			msgAlpha = 1;
			updateNewObjects();
		}
	}

	public int timeStep() {
		int dTime = super.timeStep();
		if (dTime == -1)
			return -1;
		if (zooming == -1) {
			// Force timestep of enemies (this is abnormal and it also means that
			// the PSpaceship is double timestepped (double meh!))
			timeStepObjects(dTime, lastTime);
			return dTime;
		}

		if (dead && lastTime > deadTime + 2000) {
			gbRenderer.loadWorld(this.getClass());
		}

		switch (phase) {
		case 0:
			// magical stop - just for this level
			centreSpaceShip.dx = 0;
			phase++;
			msg = "TRY TO DESTORY THE BIG ONE";
			msgAlpha = 1;
			break;
		case 1:
			if (msgAlpha < .01) {
				phase++;
				phaseTimeStart = lastTime;
			}
			break;
		case 2:
			int sec = 30 - (int) ((lastTime - phaseTimeStart) / 1000);
			countdown = "" + sec;

			if (sec <= 0) {
				phase++;
				msg = "LET'S GET OUT OF HERE!";
				msgAlpha = 1;
				zooming = 1;
				startZoom = lastTime;
			}
			break;
		}

		return dTime;
	}

	@Override
	public int getNebulaID() {
		return R.drawable.nebulabrown;
	}

	@Override
	public int getStarIndex() {
		return 22;
	}

	@Override
	public void laserKilled(PEnemy otherEnemy) {
		int c = otherEnemy.enemyClass;
		if (c == PEnemy.CLASS_ASTEROID_BIG)
			score += 250;
		if (c == PEnemy.CLASS_ASTEROID || c == PEnemy.CLASS_ASTEROID_ALT || c == PEnemy.CLASS_ASTEROID_SMALL)
			score++;
	}

	@Override
	public IntroSequence getIntroSequence() {
		return new TextIntroSequence(TEXTS);
	}
	
	public long resumeNow() {
		long dTime = super.resumeNow();
		phaseTimeStart += dTime;
		return dTime;
	}

	@Override
	public int getLeaderBoardID() {
		return R.string.leaderboard_ultra_rift;
	}
}
