package au.com.f1n.spaceinator;

import java.io.Serializable;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES10;
import android.opengl.GLSurfaceView;
import android.os.Vibrator;
import android.util.Log;
import au.com.f1n.spaceinator.game.PCamera;
import au.com.f1n.spaceinator.game.PPlanet;
import au.com.f1n.spaceinator.game.PStandardCamera;
import au.com.f1n.spaceinator.game.World;
import au.com.f1n.spaceinator.game.WorldLevel0;
import au.com.f1n.spaceinator.game.WorldLevel3;
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.mesh.BoundryDrawer;
import au.com.f1n.spaceinator.mesh.DrWho;
import au.com.f1n.spaceinator.mesh.MenuParticleDrawer;
import au.com.f1n.spaceinator.mesh.Mesh;
import au.com.f1n.spaceinator.mesh.Nebula;
import au.com.f1n.spaceinator.mesh.ObjectDraw;
import au.com.f1n.spaceinator.mesh.OnScreen;
import au.com.f1n.spaceinator.mesh.OnScreenAbstract;
import au.com.f1n.spaceinator.mesh.ParticleDrawer;
import au.com.f1n.spaceinator.mesh.Planet;
import au.com.f1n.spaceinator.mesh.SpaceShip;
import au.com.f1n.spaceinator.mesh.StarField;
import au.com.f1n.spaceinator.mesh.Sun;
import au.com.f1n.spaceinator.mesh.TextDrawer;
import au.com.f1n.spaceinator.mesh.intro.IntroSequence;
import au.com.f1n.spaceinator.physics.menu.GalaxyBG;
import au.com.f1n.spaceinator.physics.menu.MenuWorld;
import au.com.f1n.spaceinator.physics.menu.OnScreenMenu;

public class GBRenderer implements GLSurfaceView.Renderer, Serializable {
	private static final long serialVersionUID = 1L;
	public final static int SS_SUNLIGHT = GLES10.GL_LIGHT0;
	public static final float Z_FAR = 300000;
	public static final float Z_NEAR = 3000;
	private static final String LOADING = "LOADING...";
	// public static long TIME = 9999999; // used for video capture
	public float fovTan;

	private Mesh[] planets;
	private SpaceShip mSpaceShip;
	public StarField starField;
	private ParticleDrawer particleDrawer;
	private BoundryDrawer heliosphere;
	private ObjectDraw objectDraw;
	private OnScreenAbstract onScreen;
	private Nebula nebula;
	private GalaxyBG galaxy;

	private FloatBuffer sunPos = Util.makeFloatBuffer(new float[] { 100.0f, 0.0f, -200.0f, 1.0f });
	private FloatBuffer whiteCol = Util.makeFloatBuffer(new float[] { 1f, 1f, 1f, 1f });

	private GBActivity context;

	private World world;
	private World loading;
	private float mScaleFactor = 1;
	public int width;
	public int height;

	private TextDrawer textDraw;
	private Vibrator v;

	private GBGLSurfaceView gbglSurfaceView;
	private GameState gameState;
	private GBSoundManager soundManager;
	private int deathCounter;
	private DrWho drWho;
	private float rotZ;
	private float rotZAdd;
	private IntroSequence introSequence;
	// This only ever happens on the first load of a GBRenderer
	private int needLoadingScreen = 15;
	private boolean needLoad;
	private boolean needResume;
	private boolean needFirstLoad = true;

	public GBRenderer(Context context, GBGLSurfaceView gbglSurfaceView) {
		this.context = (GBActivity) context;
		this.gbglSurfaceView = gbglSurfaceView;

		textDraw = new TextDrawer(context);
		needResume = true;
	}

	public void onDrawFrame(GL10 gl) {
		synchronized (this) {
			// Constant "25 fps", move 20ms per frame (for video capture)
			// TIME += 40;
			// Only needed once
			GLES10.glBlendFunc(GLES10.GL_SRC_ALPHA, GLES10.GL_ONE_MINUS_SRC_ALPHA);
			GLES10.glEnableClientState(GLES10.GL_VERTEX_ARRAY);

			if (needLoadingScreen > 0) {
				// Draw "LOADING"
				GLES10.glClear(GLES10.GL_COLOR_BUFFER_BIT | GLES10.GL_DEPTH_BUFFER_BIT);
				GLES10.glClearColor(0, 0, 0, 1);

				// Setup orthogonal projection
				GLES10.glMatrixMode(GLES10.GL_PROJECTION);
				GLES10.glLoadIdentity();
				GLES10.glOrthof(0, width, 0, height, 0.001f, 100);
				GLES10.glMatrixMode(GLES10.GL_MODELVIEW);

				GLES10.glLoadIdentity();
				GLES10.glTranslatef(0.0f, 0, -3.0f);

				GLES10.glDisable(GLES10.GL_LIGHTING);
				GLES10.glEnable(GLES10.GL_BLEND);
				GLES10.glEnableClientState(GLES10.GL_COLOR_ARRAY);

				textDraw.setColour(MenuWorld.COLOUR_AVAILABLE_F);
				textDraw.draw(width / 2 - textDraw.getCharSize() * LOADING.length() / 2, height / 2 + textDraw.getCharSize() / 2,
						LOADING.substring(0, Math.max(0, 10 - needLoadingScreen)), false);

				needLoadingScreen--;

				if (needFirstLoad && needLoadingScreen == 5) {
					// We need the gamestate to be loaded
					loadGameState();
				}

				if (needFirstLoad && needLoadingScreen == 4) {
					// is this a full version?
					getGameState().updateFullVersion();
				}

				if (needFirstLoad && needLoadingScreen == 3) {
					// is this a full version?
					context.loadedGameState(gameState);
				}

				if (starField == null && needLoadingScreen == 2)
					starField = new StarField();

				if (needFirstLoad && needLoadingScreen == 1) {
					// Load a world from disk or go to the menu
					World w = World.load(context);
					if (w != null)
						loadWorld(w);
					else if (gameState.isFirstPlay())
						// First time play - don't load the menu...
						loadWorld(WorldLevel0.class);
					else
						loadWorld(MenuWorld.class);
					needFirstLoad = false;
				}

				if (needLoad && needLoadingScreen == 0)
					// This replaces the introsequence call
					loadNewWorld();

				return;
			}

			if (introSequence != null) {
				// Load textdraw here because there is always an introsequence and
				// the
				if (introSequence.draw(this, textDraw)) {
					// If we are here, it is expected that the introSequence has
					// called
					// loadNewWorld()!
					introSequence.release();
					introSequence = null;
				}
				return;
			} else if (world == null) {
				// Nope, were stuffed :)
				GLES10.glClear(GLES10.GL_COLOR_BUFFER_BIT | GLES10.GL_DEPTH_BUFFER_BIT);
				GLES10.glClearColor(0, 0, 0, 1);
			} else {
				// This call can cause world to become null.
				world.timeStep();
			}

			// At this point, world could have been nulled by the timestep
			if (world != null) {
				float size;

				GLES10.glClear(GLES10.GL_COLOR_BUFFER_BIT | GLES10.GL_DEPTH_BUFFER_BIT);
				GLES10.glClearColor(0, 0, 0, 1);

				GLES10.glEnable(GLES10.GL_NORMALIZE);
				float aspectRatio = (float) width / height;
				GLES10.glMatrixMode(GLES10.GL_PROJECTION);
				size = Z_NEAR * fovTan;
				GLES10.glLoadIdentity();
				GLES10.glFrustumf(-size, size, -size / aspectRatio, size / aspectRatio, Z_NEAR, Z_FAR);
				GLES10.glMatrixMode(GLES10.GL_MODELVIEW);
				GLES10.glLoadIdentity();

				PCamera camera = world.getCamera();
				GLES10.glRotatef(camera.getRotZ(), 0, 0, 1);
				if (world.isEnded()) {
					rotZ += rotZAdd;
					if (rotZAdd < .1f)
						rotZAdd += .0001f;
					GLES10.glRotatef(rotZ, 0, 0, 1);
					GLES10.glRotatef((float) Math.sin(rotZ * .0101) * 15, 1, 0, 0);
					GLES10.glRotatef((float) Math.sin(rotZ * .013) * 15, 0, 1, 0);
				}
				if (camera instanceof PStandardCamera) {
					PStandardCamera psc = (PStandardCamera) camera;
					GLES10.glRotatef(-psc.dy, 1, 0, 0);
					GLES10.glRotatef(psc.dx, 0, 1, 0);
				}
				GLES10.glRotatef(camera.getRotY(), 0, 1, 0);
				GLES10.glTranslatef(-camera.getX(), -camera.getY(), -camera.getZ());
				GLES10.glRotatef(camera.getRotX(), 1, 0, 0);

				// Draw nebula before anything else
				if (nebula != null)
					nebula.draw(mScaleFactor);

				if (world.drawStars())
					starField.draw(mScaleFactor, world instanceof MenuWorld ? -50 : 0);
				else
					drWho.draw(mScaleFactor);

				if (!world.isEnded() && objectDraw != null)
					objectDraw.draw(mScaleFactor);

				GLES10.glDisable(GLES10.GL_DEPTH_TEST);

				if (!world.isEnded()) {
					if (galaxy != null) {
						galaxy.draw();
					}

					if (heliosphere != null)
						heliosphere.draw();

					if (planets.length > 0) {
						GLES10.glLightfv(SS_SUNLIGHT, GLES10.GL_POSITION, sunPos);
						GLES10.glMaterialfv(GLES10.GL_FRONT_AND_BACK, GLES10.GL_DIFFUSE, whiteCol);
						GLES10.glMaterialfv(GLES10.GL_FRONT_AND_BACK, GLES10.GL_SPECULAR, whiteCol);
						GLES10.glMaterialf(GLES10.GL_FRONT_AND_BACK, GLES10.GL_SHININESS, 10);
						for (Mesh planet : planets) {
							planet.draw(mScaleFactor);
						}
						GLES10.glDisable(GLES10.GL_LIGHTING);
					}
				}
				particleDrawer.draw(mScaleFactor);

				if (mSpaceShip != null && !world.isDead()) {
					if (drWho != null)
						GLES10.glEnable(GLES10.GL_DEPTH_TEST);
					mSpaceShip.draw(mScaleFactor);
				}

				if (galaxy != null)
					galaxy.drawLevels();

				if (onScreen != null)
					onScreen.draw(mScaleFactor, gbglSurfaceView.isZooming());
			}
		}

		// if (world != null && loading == null && !(world instanceof MenuWorld)
		// && world.zooming == 0 && meh++ > 200) {
		// context.screenShot(createBitmapFromGLSurface(0, 0, width, height, gl));
		// try {
		// Thread.sleep(100);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
	}

	// private Bitmap createBitmapFromGLSurface(int x, int y, int w, int h, GL10
	// gl) throws OutOfMemoryError {
	// int bitmapBuffer[] = new int[w * h];
	// int bitmapSource[] = new int[w * h];
	// IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
	// intBuffer.position(0);
	//
	// try {
	// gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE,
	// intBuffer);
	// int offset1, offset2;
	// for (int i = 0; i < h; i++) {
	// offset1 = i * w;
	// offset2 = (h - i - 1) * w;
	// for (int j = 0; j < w; j++) {
	// int texturePixel = bitmapBuffer[offset1 + j];
	// int blue = (texturePixel >> 16) & 0xff;
	// int red = (texturePixel << 16) & 0x00ff0000;
	// int pixel = (texturePixel & 0xff00ff00) | red | blue;
	// bitmapSource[offset2 + j] = pixel;
	// }
	// }
	// } catch (GLException e) {
	// return null;
	// }
	//
	// return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
	// }

	public void loadGameState() {
		gameState = GameState.load(context);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;
		GLES10.glViewport(0, 0, width, height);

		gbglSurfaceView.setSize(width, height);
		textDraw.setHeight(height);

		Log.d("World", "onSurfaceCreated() " + ", w = " + width + ", h = " + height);

		if (needResume) {
			needResume = false;
			// Special case - ALWAYS reload the textdrawer texture
			textDraw.reloadTextures(context);

			if (world != null) {
				// Reload all textures in the current world
				if (planets.length > 0) {
					for (Mesh planet : planets) {
						planet.reloadTexture(context);
					}
				}

				if (nebula != null)
					nebula.reloadTexture(context);

				if (mSpaceShip != null)
					mSpaceShip.reloadTexture(context);

				if (drWho != null)
					drWho.reloadTexture(context);

				if (heliosphere != null)
					heliosphere.reloadTexture(context);

				if (galaxy != null)
					galaxy.reloadTexture(context);

				if (onScreen != null)
					onScreen.reloadTexture(context);

				if (objectDraw != null)
					objectDraw.reloadTexture(context);

				soundManager.resumeMusic();

				initLighting();
			} else if (introSequence != null)
				introSequence.reloadTexture(context);
		}
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES10.glDisable(GLES10.GL_DITHER);
		GLES10.glHint(GLES10.GL_PERSPECTIVE_CORRECTION_HINT, GLES10.GL_FASTEST);

		GLES10.glEnable(GLES10.GL_CULL_FACE);
		GLES10.glCullFace(GLES10.GL_BACK);
		GLES10.glDisable(GLES10.GL_DEPTH_TEST);
	}

	private void initLighting() {
		float[] grey = { 0.5f, 0.5f, 0.5f, 1.0f };
		float[] yellow = { 1.0f, 1.0f, 0.8f, 1.0f };

		// Lights go here.
		GLES10.glLightfv(SS_SUNLIGHT, GLES10.GL_DIFFUSE, whiteCol);
		GLES10.glLightfv(SS_SUNLIGHT, GLES10.GL_SPECULAR, Util.makeFloatBuffer(yellow));
		GLES10.glLightfv(SS_SUNLIGHT, GLES10.GL_AMBIENT, Util.makeFloatBuffer(grey));

		// Materials go here.
		GLES10.glMaterialfv(GLES10.GL_FRONT_AND_BACK, GLES10.GL_DIFFUSE, whiteCol);
		GLES10.glMaterialfv(GLES10.GL_FRONT_AND_BACK, GLES10.GL_SPECULAR, whiteCol);

		GLES10.glShadeModel(GLES10.GL_SMOOTH);
		GLES10.glLightModelf(GLES10.GL_LIGHT_MODEL_TWO_SIDE, 0.0f);

		GLES10.glEnable(SS_SUNLIGHT);
	}

	public void setScale(float mScaleFactor) {
		this.mScaleFactor = mScaleFactor;
		if (world != null)
			world.scale = mScaleFactor;
		fovTan = (float) Math.tan((30.0 * mScaleFactor) * Math.PI / 180.0 / 2.0);
		if (onScreen != null)
			onScreen.maxZoomAlpha();
	}

	public float getScale() {
		return mScaleFactor;
	}

	public synchronized void loadWorld(World savedWorld) {
		Log.d("World", "Schedule loading of world " + savedWorld.getClass().getSimpleName());

		world = null;
		introSequence = null;

		if (nebula != null)
			nebula.release();
		nebula = null;
		if (planets != null)
			for (Mesh planet : planets)
				planet.release();
		planets = null;
		if (heliosphere != null)
			heliosphere.release();
		heliosphere = null;
		if (mSpaceShip != null)
			mSpaceShip.release();
		mSpaceShip = null;
		if (onScreen != null)
			onScreen.release();
		onScreen = null;
		if (drWho != null)
			drWho.release();
		drWho = null;
		needLoad = true;

		loading = savedWorld;
		loading.init(this, v);
	}

	public synchronized void loadWorld(Class<? extends World> worldClass) {
		if (world != null && world instanceof WorldLevel3) {
			// special case when we unload this world
			((WorldLevel3) world).unloading();
		}

		Log.d("World", "Loading new INSTANCE OF world " + worldClass.getSimpleName());
		if (worldClass != null) {
			try {
				loadWorld(worldClass.newInstance());
			} catch (InstantiationException e) {
			} catch (IllegalAccessException e) {
			}
		}

		// Now that we have instantiated the world, create the intro sequence
		// (which is really the loading screen - it will call loadNewWorld on our
		// behalf)
		if (introSequence != null)
			introSequence.release();
		introSequence = loading.getIntroSequence();
		needLoad = false;
	}

	public World getWorld() {
		return world;
	}

	/**
	 * Call this when you want to load loading
	 */
	public void loadNewWorld() {
		needLoad = false;
		long start = System.currentTimeMillis();
		rotZ = rotZAdd = 0;
		setScale(1);

		if (soundManager == null) {
			soundManager = new GBSoundManager(context);
		}

		loading.start(gameState);

		if (loading.getPlanets() != null) {
			planets = new Mesh[loading.getPlanets().length];
			int i = 0;
			for (PPlanet planet : loading.getPlanets()) {
				if (planet.getSunCol() != null)
					planets[i++] = new Sun(planet, 25, 50, 1.0f, context);
				else
					planets[i++] = new Planet(planet, 40, 40, 1.0f, context);
			}
		} else {
			planets = new Mesh[0];
		}

		initLighting();

		// Make a nebula first (black hole may require it)
		if (loading.getNebulaID() != -1)
			nebula = new Nebula(context, loading);

		if (objectDraw != null)
			objectDraw.release();
		if (galaxy != null)
			galaxy.release();

		if (loading instanceof MenuWorld) {
			onScreen = new OnScreenMenu(context, width, height, loading, gbglSurfaceView);
			onScreen.setTextDraw(textDraw);
			galaxy = new GalaxyBG((MenuWorld) loading, textDraw, context);
			objectDraw = null;
			particleDrawer = new MenuParticleDrawer(loading);
		} else {
			mSpaceShip = new SpaceShip(context, loading.getCentreSpaceShip(), gameState.getShipPaintIndex());
			objectDraw = new ObjectDraw(context, loading, nebula);
			galaxy = null;
			heliosphere = new BoundryDrawer(context, loading);

			// make on screen thingo
			onScreen = new OnScreen(context, width, height, loading, gbglSurfaceView);
			onScreen.setTextDraw(textDraw);
			particleDrawer = new ParticleDrawer(loading);
		}

		if (loading.getDrWho() != null)
			drWho = new DrWho(context, loading);

		world = loading;
		loading = null;
		// reload the appropriate scale
		setScale(world.scale);
		Log.d("World", "Loaded " + world.getClass().getSimpleName() + " in " + (System.currentTimeMillis() - start) + "ms");
	}

	/**
	 * This can only be called when we have a MenuWorld or a loading sequecnce
	 * 
	 * @param x
	 * @param y
	 */
	public void worldClick(float x, float y) {
		if (introSequence != null) {
			introSequence.tap(x, y);
		} else if (onScreen != null && !onScreen.click(x, y) && galaxy != null) {
			galaxy.click(x / width, y / height);
		} else if (world != null && world.isPaused()) {
			world.resumeNow();
		}
	}

	public GameState getGameState() {
		return gameState;
	}

	public OnScreenAbstract getOnScreen() {
		return onScreen;
	}

	public GBSoundManager getSoundManager() {
		if (soundManager == null)
			soundManager = new GBSoundManager(context);

		return soundManager;
	}

	public GBActivity getContext() {
		return context;
	}

	public void addDeathCounter() {
		deathCounter++;

		if (!gameState.achievement_epic_fail_40 && deathCounter >= 2)
			gameState.achievement_epic_fail_40 = gameState.achievement(R.string.achievement_epic_fail_40, 40);
	}

	/**
	 * This is only called when a world is finished (and saved)
	 * 
	 * @return
	 */
	public int getDeathCounter() {
		int tmp = deathCounter;
		deathCounter = 0;
		return tmp;
	}

	public boolean isWaitTap() {
		return introSequence != null || world == null || (onScreen != null && onScreen.isEndSequence() || world.isPaused());
	}

	/**
	 * Call this when the view was paused (no longer active, we can assume the
	 * textures will unload)
	 */
	public synchronized void paused() {
		if (world != null) {
			world.pauseNow();
		}

		// Save the game state (just in case android destroys us) - this can be
		// null if the renderer wasnt fully loaded already!
		if (gameState != null)
			gameState.save();

		// release resources
		if (soundManager != null) {
			soundManager.pauseMusic();
			soundManager.stopShip();
		}

		Log.d("World", "Paused with world=" + world + ", loading=" + loading);
		World w = getAnyWorld();
		if (w != null && !(w instanceof MenuWorld))
			if (w.zooming >= 0)
				w.save(context);
			else
				World.clearSave(context);
		// cant clear save here in else block :(

		needResume = true;
	}

	public void setVibrator(Vibrator v) {
		this.v = v;
	}

	public int getAdHeight() {
		return gbglSurfaceView.getAdHeight();
	}

	public World getAnyWorld() {
		if (world != null)
			return world;
		if (loading != null)
			return loading;
		return null;
	}

	public void onResume() {
		if (gameState != null)
			gameState.updateFullVersion();
	}

	public GalaxyBG getGalaxy() {
		return galaxy;
	}
}