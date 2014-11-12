package au.com.f1n.spaceinator.game;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
import au.com.f1n.spaceinator.GBActivity;
import au.com.f1n.spaceinator.GBGLSurfaceView;
import au.com.f1n.spaceinator.GBRenderer;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.enemy.PAsteroidEnemy;
import au.com.f1n.spaceinator.game.enemy.PEnemy;
import au.com.f1n.spaceinator.game.enemy.PEnemyAIGroup;
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.mesh.intro.IntroSequence;
import au.com.f1n.spaceinator.physics.menu.MenuLevelDef;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards.LoadScoresResult;

/**
 * Define the world non-graphically.
 * 
 * The classes in this package define the physics of the world, their current
 * location and a main game thread.
 * 
 * Note that everything is defined in terms of x and y coordinates (no affine
 * transforms, etc).
 * 
 * lifecycle of world is:
 * 
 * Constructor (subclasses to ignore!)
 * 
 * init(GBRenderer) - start the music and initialise the introsequence
 * 
 * start(...) - renderer is ready to start this world
 * 
 * timeStep(...) - call per frame
 * 
 * @author luke
 * 
 */
public abstract class World implements Serializable, ResultCallback<LoadScoresResult> {
	private static final long serialVersionUID = 1L;

	private static long saving = 0;
	public static final MenuLevelDef[] STARS = { // Define worlds (the star IDs
																// are
			// hard coded from the random
			// number
			// generator):
			new MenuLevelDef(0, 0, 379, WorldLevel0.class, "DRIVING", 450, 194, new int[] { 1, 200, 800, 1000, 1200 }),// Training
			new MenuLevelDef(1, 0, 338, WorldLevel1.class, "LASERS", -550, 48, new int[] { 1, 200, 400, 680, 800 }), // Laser
			new MenuLevelDef(2, 0, 2948, WorldLevel2.class, "ALIENS", 400, 37, new int[] { 1, 150, 300, 420, 500 }), // Enemies
			new MenuLevelDef(3, 0, 1732, WorldLevel3.class, "ASTEROID CHALLENGE", 450, 154, new int[] { 1, 500, 1000, 1500, 2500 }), // AsteroidChallenge
			new MenuLevelDef(4, 0, 1319, WorldSolar1.class, "PROTECTION", 440, 29, new int[] { 1, 340, 450, 550, 610 }), // Solar1
			new MenuLevelDef(5, 0, 2358, WorldSolar2.class, "INFINITY", -2000, 49, new int[] { 1, 400, 2200, 3000, 4000 }), // Solar2
			new MenuLevelDef(6, 0, 505, WorldAsteroidRift.class, "ASTEROID RIFT", 500, 50, new int[] { 1, 200, 500, 1000, 1300 }), // AsteroidRift
			new MenuLevelDef(17, 0, 1670, WorldRace0.class, "TIME TRIAL", 291, 1056, new int[] { 1, 15000, 18000, 20000, 21000 }), // ZebraRace
			new MenuLevelDef(7, 0, 1298, WorldSolar3.class, "SPIKE", -430, 65, new int[] { 1, 1500, 2200, 2800, 3000 }), // Spike
			new MenuLevelDef(8, 0, 792, WorldWarp1.class, "WARP", 230, 76, new int[] { 1, 600, 1300, 1700, 2100 }), // Warp

			new MenuLevelDef(9, 1, 1607, WorldGauntlet.class, "GAUNTLET", -420, 87, new int[] { 1, 600, 1000, 1100, 1200 }), // Gauntlet
			new MenuLevelDef(10, 1, 917, WorldSolar4.class, "SOLAR FIGHTER", 120, 37, new int[] { 1, 200, 2000, 3000, 3500 }), // Solar
			new MenuLevelDef(18, 1, 1547, WorldSuperRift.class, "SUPER RIFT", 120, 50, new int[] { 1, 500, 1000, 1600, 1800 }), // AsteroidRift
			new MenuLevelDef(11, 1, 1683, WorldSolar5Bomb.class, "BOMB", -150, 41, new int[] { 1, 200, 500, 800, 1100 }), // Bomb
			new MenuLevelDef(12, 1, 21, WorldRace1.class, "PURPLE RACE", -130, 1181, new int[] { 1, 22000, 27000, 30000, 31000 }), // RACE
			new MenuLevelDef(13, 1, 736, WorldClock.class, "CLOCK", -140, 39, new int[] { 1, 200, 500, 800, 1100 }), // Clock
			new MenuLevelDef(14, 1, 1091, WorldRace2.class, "AMBER RACE", 280, 921, new int[] { 1, 22000, 27000, 30000, 31000 }), // RACE
			new MenuLevelDef(15, 1, 532, WorldWorm1.class, "ALIEN WORM", 140, 34, new int[] { 1, 200, 1000, 2000, 3000 }), // Worm1
			new MenuLevelDef(16, 1, 348, WorldBlackHole.class, "BLACK HOLE", 400, 79, new int[] { 1, 2000, 3000, 4000, 5000 }), // BlackHole
			new MenuLevelDef(19, 1, 959, WorldWarp2.class, "WARP", -230, 76, new int[] { 1, 300, 800, 1400, 1800 }), // Warp

			new MenuLevelDef(20, 2, 2637, WorldSuperGauntlet.class, "SUPER GAUNTLET", 200, 80, new int[] { 1, 1200, 2600, 3000, 4100 }), // Gauntelt2
			new MenuLevelDef(21, 2, 1349, WorldWhiteSun.class, "WHITE SUN", 500, 80, new int[] { 1, 1200, 2700, 3000, 3200 }), // WhiteSun
			new MenuLevelDef(22, 2, 1320, WorldUltraRift.class, "ULTRA RIFT", 190, 52, new int[] { 1, 1500, 2000, 2800, 3000 }), // AsteroidRift
	};

	/**
	 * Astronomical unit - mean distance from the earth to the sun.
	 */
	public static final float AU = 4000;

	/**
	 * Gravitational Constant
	 */
	public static final float G = 6.67384E-8f;

	public static final int MAX_OBJECTS = 1100;

	public static final float SPEED_CUTOFF = 0.001f;

	public static final float SHIP_START_OFFSET = 40000;

	protected static final int PLASMA_RECHARGE = 2000;

	private static final long SHIELD_RECHARGE = 500;
	private static final String FILENAME = "Save.dat";

	public PPlanet[] planets;
	/**
	 * These are just visual things and have no effect on gameplay
	 */
	protected ArrayList<PGraphicParticle> graphicParticles;
	/**
	 * Use as a stack
	 */
	protected FArrayList<PParticleExplosion> leftoverExplosions;
	protected FArrayList<PObject> objects;
	protected FArrayList<PObject> newObjects;
	protected FArrayList<PEdge> edgesX;
	protected FArrayList<PObject> curObjects;
	public FArrayList<PEnemyAIGroup> enemyAIGroups;

	protected PCamera camera;
	protected PSpaceShip centreSpaceShip;

	protected long startTime;
	/**
	 * This is basically the same as timeS
	 */
	public long lastTime = -1;
	// private long lastTimeFR = 0;
	protected boolean dead = false;
	// private int iter;
	protected transient Vibrator v;
	protected Boundry boundry;

	protected long startZoom;
	// Start with the fly in
	public int zooming = -1;

	public int score;
	private int finalScore;
	protected float fade;
	public float msgAlpha;
	public String msg;
	protected float targetScale = 1;

	/**
	 * The renderer - this is required so we can reload the world when the user
	 * dies and to grab the sound manager
	 */
	public transient GBRenderer gbRenderer;

	protected int[] killCount;
	protected GameState gameState;
	private int best = -1;

	protected long deadTime;

	protected int oldStars;

	protected String countdown = null;

	public boolean targetAsteroids = false;

	// private float boundryAlpha;

	protected long lastPlasma;
	private long lastShield;

	private float startX;

	private PMissilesParticle missilesParticle;

	private PObject[] inflyObj;

	private PLightFlash lightFlash;

	protected boolean skipLightBoom = false;

	private long pauseTime;
	public float scale = 1;

	private boolean hasCompleted;

	private String leaderBoardTitle = "LOADING LEADERBOARD...";

	private ArrayList<String> scores;

	protected int spentPlasma;

	protected int spentCells;

	private float startScale;

	/**
	 * init must be called as well
	 */
	public World() {
		killCount = new int[PEnemy.CLASS_COUNT];
		newObjects = new FArrayList<PObject>(100);
		leftoverExplosions = new FArrayList<PParticleExplosion>();
	}

	public void init(GBRenderer gbRenderer, Vibrator v) {
		this.gbRenderer = gbRenderer;
		this.v = v;

		if (centreSpaceShip != null) {
			centreSpaceShip.reloaded(gbRenderer.getSoundManager());
		}
	}

	public void start(GameState gameState) {
		this.gameState = gameState;

		if (gameState.getScoreList(this) != null)
			best = gameState.getScoreList(this).getBest();
	}

	/**
	 * Add an object to the end of the array
	 * 
	 * @param object
	 */
	public void addObject(PObject object) {
		if (objects.size + newObjects.size < MAX_OBJECTS) {
			newObjects.add(object);
		} else {
			Log.d("World", "hitting end of object allowance!");
		}
	}

	/**
	 * This is O(n) removal of objects from the arrays they are in.
	 * 
	 * @param object
	 */
	protected void removeObject(PObject object, int objectIndex) {
		if (object instanceof PEnemy)
			killCount[((PEnemy) object).enemyClass]++;

		// This is strange way to remove edges but will work
		object.leftEdge.object = null;
		object.rightEdge.object = null;

		objects.remove(objectIndex);
	}

	public PCamera getCamera() {
		return camera;
	}

	public PSpaceShip getCentreSpaceShip() {
		return centreSpaceShip;
	}

	/**
	 * Step forward one frame.
	 */
	public int timeStep() {
		if (pauseTime > 0)
			return -1;

		long timeS = System.currentTimeMillis();
		int dTime = (int) (timeS - lastTime);
		if (startTime == 0) {
			dTime = 1;
			startTime = lastTime = timeS;
		}
		lastTime = timeS;

		if (zooming == -1) {
			if (startX == 0) {
				startX = centreSpaceShip.x;
				// Add fly-in objects (can't collide with them - they are only
				// drawn)
				Random r = new Random();
				int nFly = 40;
				inflyObj = new PObject[nFly];
				for (int i = 0; i < nFly; i++) {
					PObject obj = new PAsteroidEnemy(this, startX + SHIP_START_OFFSET * i / 57 + r.nextFloat() * 650, r.nextFloat() * 5000, 0, 0, r.nextInt(4) + 1,
							true);
					obj.z = -r.nextFloat() * 10000;
					objects.add(obj);
					inflyObj[i] = obj;
				}
				sortObjects();
			}

			float amt = camera.flyin();
			if (amt < 1) {
				if (amt < .7f) {
					centreSpaceShip.setAccel(1, 0, 0);
					centreSpaceShip.z = -200;
				} else {
					centreSpaceShip.setAccelOff();
					float amt2 = (amt - .7f) / .3f;
					gbRenderer.setScale(1 * (1 - amt2) + targetScale * amt2);
					centreSpaceShip.z = -200 * (1 - amt2);
				}
				centreSpaceShip.timeStep(dTime, timeS);
				centreSpaceShip.getShipParticles().timeStep(dTime, timeS);
				centreSpaceShip.x = startX + Util.slowOut(amt) * SHIP_START_OFFSET;
				if (amt > .95f)
					centreSpaceShip.setShieldAlpha((amt - .95f) * 20);
				else
					centreSpaceShip.setShieldAlpha(0);
				if (amt < .2f)
					fade = 1 - amt / .2f;
				else
					fade = 0;

			} else {
				// Now start the level
				zooming = 0;
				centreSpaceShip.setShieldAlpha(1);
				fade = 0;
				centreSpaceShip.setAccelOff();
				centreSpaceShip.z = 0;
				// Remove the fly-in objects
				if (inflyObj != null)
					for (PObject p : inflyObj)
						objects.remove(p);
				inflyObj = null;
			}
			return dTime;
		}

		camera.timeStep(dTime);

		if (msgAlpha > 0)
			msgAlpha -= (float) dTime / 3000;
		if (msgAlpha < 0)
			msgAlpha = 0;

		if (centreSpaceShip != null && !dead && centreSpaceShip.timeStep(dTime, timeS)) {
			// DEATH!
			death(timeS);
		}

		if (zooming != 4) {
			timeStepObjects(dTime, timeS);

			// Update enemy AI
			if (enemyAIGroups != null) {
				Object[] groupArray = enemyAIGroups.array;
				int n = enemyAIGroups.size;
				for (int i = 0; i < n; i++) {
					if (((PEnemyAIGroup) groupArray[i]).timeStep(dTime, timeS)) {
						enemyAIGroups.remove(i--);
						n--;
					}
				}
			}

			// Add new objects that have been created this frame
			updateNewObjects();
		}

		// Do graphic effect timesteps last
		ListIterator<PGraphicParticle> graphicalIter = graphicParticles.listIterator();
		while (graphicalIter.hasNext()) {
			PGraphicParticle pgp = graphicalIter.next();
			if (pgp.timeStep(dTime, timeS)) {
				if (pgp instanceof PParticleExplosion && pgp.getLastNPoints() == PParticleExplosion.BASE_PARTICLE_N)
					leftoverExplosions.add((PParticleExplosion) pgp);
				graphicalIter.remove();
			}
		}

		// Zooming end
		if (zooming == 1) {
			// Spaceship rotates
			completed();

			centreSpaceShip.dx = centreSpaceShip.dy = 0;
			// This is a lie but meh, whatever
			startX = centreSpaceShip.y;
			centreSpaceShip.facingAngle = 0.9f * centreSpaceShip.facingAngle + 0.1f * Util.PI / 2;
			centreSpaceShip.rotX -= .002f * (lastTime - startZoom);

			if (centreSpaceShip.rotX <= -90) {
				centreSpaceShip.rotX = -90;
				zooming = 2;
				startZoom = lastTime;
				if (!dead) {
					graphicEffect(new ZoomParticles(centreSpaceShip, 2000));
					gbRenderer.getSoundManager().flyOut();
				}
			}
		} else if (zooming == 2) {
			// Spaceship blasts away
			float amt = (float) (lastTime - startZoom) / 3000f;
			float slowAmt = Util.slowInOut(amt);
			gbRenderer.setScale(amt * GBGLSurfaceView.MAX_SCALE + (1 - amt) * startScale);
			if (centreSpaceShip != null)
				centreSpaceShip.z = (-1 + Util.slowOut(1 - amt)) * 175000;
			camera.z = PCamera.BASE_Z - slowAmt * PCamera.BASE_Z * 2;
			if (camera instanceof PStandardCamera && !skipLightBoom) {
				((PStandardCamera) camera).x = slowAmt * 2000;
				((PStandardCamera) camera).y = slowAmt * 3236;
			}

			if (amt >= 1) {
				if (skipLightBoom || dead) {
					zooming = 5;
				} else {
					// spike up, light travel
					zooming = 3;
					startZoom = lastTime;

					lightFlash = new PLightFlash(centreSpaceShip.x, centreSpaceShip.y, 20);
					lightFlash.z = centreSpaceShip.z;
					lightFlash.shootAngle = Util.PI / 2;
					addObject(lightFlash);
					centreSpaceShip.rotX = 0;
				}
			}
		} else if (zooming == 3) {
			// spaceship goes up
			float amt = (float) (lastTime - startZoom) / 1200f;

			if (centreSpaceShip != null) {
				centreSpaceShip.y = startX + amt * 35000;
				if (camera instanceof PStandardCamera)
					camera.setY(3236 - centreSpaceShip.y + startX);
				lightFlash.y = centreSpaceShip.y;
				lightFlash.setAmt(Util.slowOut(amt));
			}

			if (amt >= 1)
				zooming = 4;
		} else if (zooming == 4) {
			centreSpaceShip.y += dTime * 20;
			if (camera instanceof PStandardCamera)
				((PStandardCamera) camera).y -= dTime * 20;
		} else if (zooming == 5) {
			// this is a funny case
			float amt = (float) (lastTime - startZoom) / 200f;
			if (amt >= 1) {
				zooming = 4;
			}
		}

		return dTime;
	}

	protected void updateNewObjects() {
		int newN = newObjects.size;
		Object[] newArray = newObjects.array;
		for (int i = 0; i < newN; i++) {
			PObject o = (PObject) newArray[i];
			edgesX.add(o.leftEdge = new PEdge(true, o));
			edgesX.add(o.rightEdge = new PEdge(false, o));
			objects.add(o);
		}
		newObjects.clear();
		sortObjects();
	}

	protected void timeStepObjects(int dTime, long timeS) {
		if (objects != null) {
			// Timestep movement of all objects
			Object[] o = objects.array;
			int osize = objects.size;
			for (int i = 0; i < osize; i++) {
				PObject pObject = (PObject) o[i];
				if (pObject.timeStep(dTime, timeS)) {
					// Note the decrement of i (this will also remove the edges)
					removeObject(pObject, i--);
					// Note the decrement of the number of total objects as well
					osize--;
				}
			}

			// Update all edges
			int numEdges = edgesX.size;
			Object[] edgeArray = edgesX.array;
			for (int i = 0; i < numEdges; i++) {
				PEdge curEdge = (PEdge) edgeArray[i];

				if (curEdge.object == null) {
					// Note decrement of i and numEdges
					edgesX.remove(i--);
					numEdges--;
				} else
					curEdge.reCalcVal();
			}

			// Sort x coordinates of all edges
			sortEdges();

			// Check collisions of all objects
			checkCollisions();
		}
	}

	public boolean isEnded() {
		return zooming == 4;
	}

	private void checkCollisions() {
		int numEdges = edgesX.size;
		Object[] edgeArray = edgesX.array;

		for (int i = 0; i < numEdges; i++) {
			PEdge curEdge = (PEdge) edgeArray[i];
			PObject curObject = curEdge.object;
			if (curEdge.isStart) {
				Object[] a = curObjects.array;
				int s = curObjects.size;

				// Start of an object - is it colliding with other objects?
				for (int j = 0; j < s; j++) {
					PObject other = (PObject) a[j];

					if (other.top > curObject.bottom && curObject.top > other.bottom)
						other.checkCollision(curObject);
				}
				// Add it to the future checked objects
				curObjects.add(curEdge.object);
			} else {
				// todo:use something else!
				curObjects.remove(curEdge.object);
			}
		}
	}

	private void sortEdges() {
		int j;
		PEdge key;
		int i;
		int n = edgesX.size;
		Object[] edgeArray = edgesX.array;

		for (j = 1; j < n; j++) {
			key = (PEdge) edgeArray[j];
			for (i = j - 1; (i >= 0) && (((PEdge) edgeArray[i]).x > key.x); i--)
				edgeArray[i + 1] = edgeArray[i];
			edgeArray[i + 1] = key;
		}
	}

	public void sortObjects() {
		int j;
		PObject key;
		int i;
		int n = objects.size;
		Object[] objectArray = objects.array;

		for (j = 1; j < n; j++) {
			key = (PObject) objectArray[j];
			for (i = j - 1; (i >= 0) && (((PObject) objectArray[i]).drawOrder > key.drawOrder); i--)
				objectArray[i + 1] = objectArray[i];
			objectArray[i + 1] = key;
		}
	}

	private void death(long timeS) {
		dead = true;
		deadTime = timeS;
		centreSpaceShip.curShield = 0;
		// Not a standard explosion...
		graphicEffect(new PParticleExplosion(centreSpaceShip.x, centreSpaceShip.y, 0, 0, 10, timeS, 5000, 1500, PSpaceShip.SHIELD_SIZE, .99f,
				PParticleExplosion.COLOUR_HOT));
		gbRenderer.addDeathCounter();
		// Cant load the level
		clearSave(gbRenderer.getContext());
	}

	public PPlanet[] getPlanets() {
		return planets;
	}

	public long getLastTime() {
		return lastTime;
	}

	public void hitShip(int damage, float dx, float dy) {
		if (damage > 0 && damage < 500)
			v.vibrate(damage);
		// camera.hit(dx, dy);
	}

	/**
	 * Use graphic effects for when a particle makes no difference to gameplay
	 * 
	 * @param particle
	 * @param accel
	 */
	public void graphicEffect(PGraphicParticle particle) {
		if (graphicParticles.size() < MAX_OBJECTS)
			graphicParticles.add(particle);
	}

	/**
	 * Return all of the objects currently in the world
	 * 
	 * The majority of these will be enemies or weapons.
	 * 
	 * @return
	 */
	public Object[] getObjects() {
		if (objects == null)
			return null;
		return objects.array;
	}

	public int getObjectSize() {
		return objects.size;
	}

	public boolean isDead() {
		return dead;
	}

	public ArrayList<PGraphicParticle> getGrpahicParticles() {
		return graphicParticles;
	}

	public boolean isNew() {
		return lastTime == -1;
	}

	public int getScore() {
		return finalScore;
	}

	public String getCurMessage() {
		return msg;
	}

	public float getCurMessageAlpha() {
		return msgAlpha;
	}

	public float getFade() {
		return fade;
	}

	public abstract int getNebulaID();

	/**
	 * This is the index of MenuWorld.STARS that this world relates to
	 * 
	 * @return
	 */
	public abstract int getStarIndex();

	// public abstract int getID();

	protected int sumKills() {
		int sum = 0;
		for (int i = 0; i < killCount.length; i++)
			sum += killCount[i];
		return sum;
	}

	public int[] getKillArray() {
		return killCount;
	}

	public int getBest() {
		return best;
	}

	public GameState getGameState() {
		return gameState;
	}

	public int getOldStars() {
		return oldStars;
	}

	public void goodKill(PEnemy enemy) {
		if (centreSpaceShip != null && (!PEnemy.ASTEROID_CLASS[enemy.enemyClass] || targetAsteroids))
			centreSpaceShip.increasePower();

		if (enemy.enemyClass == PEnemy.CLASS_SCOUT)
			gameState.killScout();
		if (enemy.enemyClass == PEnemy.CLASS_FIGHTER)
			gameState.killFighter();

		laserKilled(enemy);
	}

	public abstract void laserKilled(PEnemy otherEnemy);

	public String getCountdown() {
		return countdown;
	}

	public boolean isFinishedEnd() {
		return zooming == 4;
	}

	public void firePlasma() {
		if (!dead && zooming == 0 && lastTime - lastPlasma > PLASMA_RECHARGE && !centreSpaceShip.isWeaponless() && gameState.spendPlasma()) {
			addObject(new PPlasmaParticle(this, centreSpaceShip.x, centreSpaceShip.y, true));
			lastPlasma = lastTime;
			gbRenderer.getSoundManager().plasma();
			spentPlasma++;
		}
	}

	public long getPlayTime() {
		return lastTime - startTime;
	}

	public void fireShieldRegen() {
		if (!dead && zooming == 0 && lastTime - lastShield > SHIELD_RECHARGE && gameState.spendShieldCell()) {
			centreSpaceShip.curShield += 100;
			lastShield = lastTime;
			gbRenderer.getSoundManager().shield();
			spentCells++;
		}
	}

	public PMissilesParticle getMissilesParticle() {
		if (missilesParticle == null) {
			missilesParticle = new PMissilesParticle();
			graphicEffect(missilesParticle);
		}

		return missilesParticle;
	}

	public Boundry getBoundry() {
		return boundry;
	}

	public boolean drawStars() {
		return true;
	}

	public PDrWho getDrWho() {
		return null;
	}

	public abstract IntroSequence getIntroSequence();

	public static int getNumLevels() {
		return STARS.length;
	}

	public MenuLevelDef getWorldDef() {
		return STARS[getStarIndex()];
	}

	public float canPlasma() {
		if (!dead && zooming == 0 && !centreSpaceShip.isWeaponless() && gameState.getShipUpgrades()[GameState.PLASMA_WAVE] > 0) {
			if (lastTime - lastPlasma > PLASMA_RECHARGE)
				return 1;
			return (float) (lastTime - lastPlasma) / PLASMA_RECHARGE;
		}
		return 0;
	}

	public float canShield() {
		if (!dead && zooming == 0 && lastTime - lastShield > SHIELD_RECHARGE && gameState.getShipUpgrades()[GameState.SHIELD_CELL] > 0) {
			if (lastTime - lastShield > SHIELD_RECHARGE)
				return 1;
			return (float) (lastTime - lastShield) / SHIELD_RECHARGE;
		}
		return 0;
	}

	public void graphicEffect(float x, float y, float dx, float dy, float maxVelocity, long timeS, int exploisionTime, float initRadius, float dampner,
			int colourType) {
		if (leftoverExplosions.isEmpty()) {
			graphicEffect(new PParticleExplosion(x, y, dx, dy, maxVelocity, timeS, exploisionTime, PParticleExplosion.BASE_PARTICLE_N, initRadius, dampner,
					colourType));
		} else {
			PParticleExplosion ppe = leftoverExplosions.removeLast();
			ppe.setTo(x, y, dx, dy, maxVelocity, timeS, exploisionTime, initRadius, dampner, colourType);
			graphicEffect(ppe);
		}
	}

	public void save(Context context) {
		Log.d("World", "Starting save of " + this.getClass().getSimpleName());
		saving = System.currentTimeMillis();
		try {
			File file = new File(context.getFilesDir(), FILENAME);
			FileOutputStream fout = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(this);
			oos.close();

			Log.d("World", "Finished save of " + this.getClass().getSimpleName() + " in " + (System.currentTimeMillis() - saving) + "ms");
			saving = 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static World load(Context context) {
		long time = System.currentTimeMillis();
		int i = 0;
		while (time - saving < 2000 && i < 20) {
			i++;
			Log.d("World", "loading sleep " + i);
			// sleep while we are saving
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// If we are here, we dont want to do anything
				return null;
			}
		}

		File file = new File(context.getFilesDir(), FILENAME);
		World world = null;

		try {
			FileInputStream fin = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fin);
			world = (World) ois.readObject();
			ois.close();
		} catch (Exception e) {
			// No world to load (and that's ok)
		}

		return world;
	}

	public void pauseNow() {
		pauseTime = lastTime;
	}

	public long resumeNow() {
		lastTime = System.currentTimeMillis();
		long dTime = lastTime - pauseTime;
		startTime += dTime;
		camera.resumeNow(dTime);
		if (centreSpaceShip != null)
			centreSpaceShip.resumeNow(dTime);
		pauseTime = 0;
		return dTime;
	}

	public boolean isPaused() {
		return pauseTime > 0;
	}

	public static void clearSave(Context context) {
		File file = new File(context.getFilesDir(), FILENAME);
		file.delete();
	}

	protected void completed() {
		if (!hasCompleted) {
			startScale = gbRenderer.getScale();
			finalScore = score;
			hasCompleted = true;
			// Save the score
			oldStars = gameState.finishWorld(this, gbRenderer.getDeathCounter());
			World.clearSave(gbRenderer.getContext());

			GBActivity context = (GBActivity) gbRenderer.getContext();
			if (context.isSignedIn()) {

				String leaderboard = context.getResources().getString(getLeaderBoardID());

				Games.Leaderboards.submitScore(context.getApiClient(), leaderboard, finalScore);
				// PendingResult<LoadScoresResult> result =
				// Games.Leaderboards.loadTopScores(context.getGameHelper().getApiClient(),
				// leaderboard, LeaderboardVariant.TIME_SPAN_ALL_TIME,
				// LeaderboardVariant.COLLECTION_PUBLIC, 25);

				// result.setResultCallback(this);

				PendingResult<LoadScoresResult> i = Games.Leaderboards.loadTopScores(context.getApiClient(), leaderboard, LeaderboardVariant.TIME_SPAN_ALL_TIME,
						LeaderboardVariant.COLLECTION_PUBLIC, 25);
				i.setResultCallback(this);
			} else {
				leaderBoardTitle = "SIGN IN FOR";
				ArrayList<String> newScores = new ArrayList<String>();
				newScores.add("LEADERBOARDS");
				newScores.add("");
				newScores.add("YOUR NAME");
				newScores.add("COULD BE");
				newScores.add("HERE");
				scores = newScores;
			}
		}
	}

	public abstract int getLeaderBoardID();

	@Override
	public void onResult(LoadScoresResult result) {
		if (result.getStatus().getStatusCode() == GamesStatusCodes.STATUS_OK
				|| result.getStatus().getStatusCode() == GamesStatusCodes.STATUS_NETWORK_ERROR_STALE_DATA) {
			LeaderboardScoreBuffer lsb = result.getScores();

			ArrayList<String> newScores = new ArrayList<String>();

			for (LeaderboardScore s : lsb) {
				newScores.add((gbRenderer.getContext().getPlayerID().equals(s.getScoreHolder().getPlayerId()) ? ">" : "") + s.getRawScore() + " "
						+ Util.happyString(s.getScoreHolderDisplayName()));
			}

			lsb.close();

			leaderBoardTitle = "PUBLIC LEADERBORD";
			scores = newScores;
		} else {
			leaderBoardTitle = "LEADERBOARD ERROR " + result.getStatus().getStatusCode();
		}
		result.release();
	}

	public String getLeaderBoardTitle() {
		return leaderBoardTitle;
	}

	public ArrayList<String> getScores() {
		return scores;
	}

	public int getSpentPlasma() {
		return spentPlasma;
	}

	public int getSpentCells() {
		return spentCells;
	}
}
