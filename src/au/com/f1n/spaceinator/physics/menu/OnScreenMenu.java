package au.com.f1n.spaceinator.physics.menu;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import android.content.Context;
import android.opengl.GLES10;
import au.com.f1n.spaceinator.GBGLSurfaceView;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.PShipParticles;
import au.com.f1n.spaceinator.game.PSpaceShip;
import au.com.f1n.spaceinator.game.World;
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.mesh.OnScreenAbstract;
import au.com.f1n.spaceinator.mesh.SpaceShip;

/**
 * Orthogonal onscreen components for the main menu
 * 
 * This is also where the store is displayed
 */
public class OnScreenMenu extends OnScreenAbstract {
	private static final String[] STORE_LABEL = { "SHIP UPGRADES", "SHIP WEAPONS AND PAINT", "AUTO TURRETS" };
	private boolean storeOpen;
	private float storeNumber = 1;
	private int targetStoreNumber = 1;
	private FloatBuffer fullScreen;
	private float fade;
	private float inFade;
	private SpaceShip spaceShip;
	private PShipParticles shipParticles;
	private long lastTime;
	private int[] paintTex = new int[1];
	private FloatBuffer texBuffer;

	private FloatBuffer endLineBuffer;
	private ByteBuffer endLineColour;

	private FloatBuffer boxBuffer;
	private FloatBuffer paintBoxBuffer;
	private ByteBuffer onColor;
	private ByteBuffer offColor;
	private float fovTan;
	private float rotFace;
	private int newPaint = -1;

	private FloatBuffer arrowBuffer;
	private ByteBuffer arrowColour;
	private float musicAlpha = 1;
	private float arrowOffset;
	private String creditString;
	private String starString;
	private int lastCredits = -1;
	private int lastStars = -1;

	public OnScreenMenu(Context context, int screenWidth, int screenHeight, World world, GBGLSurfaceView view) {
		super(context, screenWidth, screenHeight, world, view);

		reloadTexture(context);
		texBuffer = Util.makeFloatBuffer(4 * 2);
		texBuffer.put(0);
		texBuffer.put(1);
		texBuffer.put(1);
		texBuffer.put(1);
		texBuffer.put(1);
		texBuffer.put(0);
		texBuffer.put(0);
		texBuffer.put(0);
		texBuffer.position(0);

		spaceShip = new SpaceShip(context, null, world.getGameState().getShipPaintIndex());
		shipParticles = new PShipParticles();
		fovTan = (float) Math.tan((30.0) * Math.PI / 180.0 / 2.0);

		fullScreen = Util.makeFloatBuffer(4 * 3);
		fullScreen.position(0);
		fullScreen.put(0);
		fullScreen.put(0);
		fullScreen.put(-10);

		fullScreen.put(screenWidth);
		fullScreen.put(0);
		fullScreen.put(-10);

		fullScreen.put(screenWidth);
		fullScreen.put(screenHeight);
		fullScreen.put(-10);

		fullScreen.put(0);
		fullScreen.put(screenHeight);
		fullScreen.put(-10);
		fullScreen.position(0);

		float thickness = 4;
		endLineBuffer = Util.makeFloatBuffer(6 * 3);
		endLineColour = ByteBuffer.allocateDirect(6 * 4);

		endLineBuffer.put(0);
		endLineBuffer.put(screenHeight);
		endLineBuffer.put(-10);

		endLineBuffer.put(0);
		endLineBuffer.put(screenHeight - thickness);
		endLineBuffer.put(-10);

		endLineBuffer.put(screenWidth * 8 / 13);
		endLineBuffer.put(screenHeight);
		endLineBuffer.put(-10);

		endLineBuffer.put(screenWidth * 8 / 13);
		endLineBuffer.put(screenHeight - thickness);
		endLineBuffer.put(-10);

		float extra = screenWidth * 1 / 13;

		endLineBuffer.put(screenWidth * 8 / 13 + extra);
		endLineBuffer.put(screenHeight - extra * .7f);
		endLineBuffer.put(-10);

		endLineBuffer.put(screenWidth * 8 / 13 + extra);
		endLineBuffer.put(screenHeight - extra * .7f - thickness);
		endLineBuffer.put(-10);
		endLineBuffer.position(0);

		for (int i = 0; i < 6; i++) {
			if (i == 0 || i == 2 || i == 4)
				endLineColour.put(MenuWorld.COLOUR_BASE);
			else
				endLineColour.put(MenuWorld.COLOUR_BLACK_TRANS);
		}
		endLineColour.position(0);

		boxBuffer = Util.makeFloatBuffer(5 * 3);
		boxBuffer.put(.1f);
		boxBuffer.put(.1f);
		boxBuffer.put(-10);

		boxBuffer.put(.9f);
		boxBuffer.put(.1f);
		boxBuffer.put(-10);

		boxBuffer.put(.9f);
		boxBuffer.put(.9f);
		boxBuffer.put(-10);

		boxBuffer.put(.1f);
		boxBuffer.put(.9f);
		boxBuffer.put(-10);

		boxBuffer.put(.1f);
		boxBuffer.put(.1f);
		boxBuffer.put(-10);

		boxBuffer.position(0);

		paintBoxBuffer = Util.makeFloatBuffer(5 * 3);
		paintBoxBuffer.put(0);
		paintBoxBuffer.put(0);
		paintBoxBuffer.put(-10);

		paintBoxBuffer.put(1);
		paintBoxBuffer.put(0);
		paintBoxBuffer.put(-10);

		paintBoxBuffer.put(1);
		paintBoxBuffer.put(1);
		paintBoxBuffer.put(-10);

		paintBoxBuffer.put(0);
		paintBoxBuffer.put(1);
		paintBoxBuffer.put(-10);

		paintBoxBuffer.put(0);
		paintBoxBuffer.put(0);
		paintBoxBuffer.put(-10);

		paintBoxBuffer.position(0);

		arrowBuffer = Util.makeFloatBuffer(3 * 7);
		arrowColour = ByteBuffer.allocateDirect(4 * 8);
		arrowColour.position(0);
		arrowBuffer.put(0);
		arrowBuffer.put(0);
		arrowBuffer.put(0);

		arrowBuffer.put(1);
		arrowBuffer.put(.35f);
		arrowBuffer.put(0);

		arrowBuffer.put(0.562f);
		arrowBuffer.put(.304f);
		arrowBuffer.put(0);

		arrowBuffer.put(.942f);
		arrowBuffer.put(.68f);
		arrowBuffer.put(0);

		arrowBuffer.put(.742f);
		arrowBuffer.put(.702f);
		arrowBuffer.put(0);

		arrowBuffer.put(.264f);
		arrowBuffer.put(.362f);
		arrowBuffer.put(0);

		arrowBuffer.put(.136f);
		arrowBuffer.put(.499f);
		arrowBuffer.put(0);

		arrowBuffer.position(0);
		arrowColour.position(0);

		onColor = ByteBuffer.allocateDirect(5 * 4);
		onColor.position(0);
		onColor.put(MenuWorld.COLOUR_WIN_2);
		onColor.put(MenuWorld.COLOUR_WIN_2);
		onColor.put(MenuWorld.COLOUR_WIN_1);
		onColor.put(MenuWorld.COLOUR_WIN_1);
		onColor.put(MenuWorld.COLOUR_WIN_2);
		onColor.position(0);

		offColor = ByteBuffer.allocateDirect(5 * 4);
		offColor.position(0);
		offColor.put(MenuWorld.COLOUR_BASE);
		offColor.put(MenuWorld.COLOUR_BASE);
		offColor.put(MenuWorld.COLOUR_BASE);
		offColor.put(MenuWorld.COLOUR_BASE);
		offColor.put(MenuWorld.COLOUR_BASE);
		offColor.position(0);
	}

	/**
	 * Handle the users click.
	 * 
	 * @return true if we "used" the click
	 */
	public boolean click(float x, float y) {
		if (world.getGameState().getMaxLevel() <= 2)
			return false;

		if (storeOpen)
			if (storeNumber < .01) {
				// left store
				for (int i = 0; i < 4; i++) {
					if (x < screenWidth / 2 && y > screenHeight * (3 + 2 * i) / 13 && y < screenHeight * (4.5f + 2 * i) / 13) {
						if (world.getGameState().buyUpgrade(i))
							world.gbRenderer.getSoundManager().coin();
						else
							world.gbRenderer.getSoundManager().alarm();

					}
				}
			} else if (storeNumber > 0.99 && storeNumber < 1.01 && x > screenWidth / 13 && x < screenWidth * 12 / 13) {
				// middle store
				if (x < screenWidth / 2 && y < screenHeight / 2) {
					if (world.getGameState().buyUpgrade(GameState.SHIELD_CELL))
						world.gbRenderer.getSoundManager().shield();
					else
						world.gbRenderer.getSoundManager().alarm();
				} else if (x < screenWidth / 2 && y > screenHeight / 2) {
					if (world.getGameState().buyUpgrade(GameState.PLASMA_WAVE))
						world.gbRenderer.getSoundManager().plasma();
					else
						world.gbRenderer.getSoundManager().alarm();
				} else if (x > screenWidth / 2 && y > screenHeight * 3.6f / 13 && y < screenHeight * 10.6f / 13) {
					int paintSel = (int) ((y - screenHeight * 3.6f / 13) / (screenHeight * 7 / 13) * SpaceShip.SHIP_PAINTS.length);
					if (world.getGameState().setShipPaintIndex(paintSel)) {
						world.gbRenderer.getSoundManager().coin();
						newPaint = paintSel;
					} else {
						world.gbRenderer.getSoundManager().alarm();
					}
				}
			} else if (storeNumber > 1.99) {
				for (int i = 6; i < GameState.UPGRADE_NAMES.length; i++) {
					if (x > screenWidth / 2 && y > screenHeight * (3 + 2 * (i - 6)) / 13 && y < screenHeight * (4.5f + 2 * (i - 6)) / 13) {
						if (world.getGameState().buyUpgrade(i))
							world.gbRenderer.getSoundManager().coin();
						else
							world.gbRenderer.getSoundManager().alarm();
					}
				}
			}

		// test for closing store
		if (y < textDraw.getCharSize() * 2) {
			storeOpen = !storeOpen;
			world.gbRenderer.getSoundManager().beepMenu(.5f);
			if (storeOpen) {
				storeNumber = .9f;
				targetStoreNumber = 1;
			}
			return true;
		}

		if (storeOpen)
			// Finally...test for going to a different store
			if (x < screenWidth / 10 && targetStoreNumber > 0) {
				targetStoreNumber--;
				world.gbRenderer.getSoundManager().beepMenu(.8f);
			} else if (x > screenWidth * 9 / 10 && targetStoreNumber < 2) {
				targetStoreNumber++;
				world.gbRenderer.getSoundManager().beepMenu(1);
			}

		return storeOpen;
	}

	public void draw(float scaleFactor, boolean zooming) {
		if (((PMenuCamera) world.getCamera()).getRotXProportion() < 1) {
			return;
		} else if (inFade < 1) {
			inFade += .05f;
		}

		// Swap paint if need be
		if (newPaint != -1) {
			spaceShip.setPaint(newPaint);
			newPaint = -1;
		}

		// Draw on screen things
		GLES10.glMatrixMode(GLES10.GL_PROJECTION);
		GLES10.glLoadIdentity();
		GLES10.glOrthof(0, screenWidth, 0, screenHeight, 0.001f, 100);

		GLES10.glMatrixMode(GLES10.GL_MODELVIEW);
		GLES10.glLoadIdentity();

		// Draw music?
		textDraw.setColour(MenuWorld.COLOUR_AVAILABLE_F);

		if (musicAlpha > 0) {
			MenuWorld.COLOUR_AVAILABLE_F[3] = musicAlpha;
			String msg = world.gbRenderer.getSoundManager().getTrackName();
			// Draw it in the bottom left
			textDraw.draw(curPad, curPad + curIndicatorHeight, msg, false);

			musicAlpha -= 0.005f;
		}

		// Draw back arrow if need be
		if (((PMenuCamera) world.getCamera()).getTracking() != null) {
			GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, arrowBuffer);
			GLES10.glPushMatrix();
			GLES10.glEnableClientState(GLES10.GL_COLOR_ARRAY);
			GLES10.glDisable(GLES10.GL_TEXTURE_2D);

			MenuWorld.COLOUR_WIN_1[3] = (byte) (((PMenuCamera) world.getCamera()).getProportionTrack() * 200);
			arrowColour.put(MenuWorld.COLOUR_WIN_1);
			MenuWorld.COLOUR_WIN_1[3] = (byte) (((PMenuCamera) world.getCamera()).getProportionTrack() * 64);
			for (int i = 0; i < 6; i++)
				arrowColour.put(MenuWorld.COLOUR_WIN_1);
			arrowColour.position(0);
			MenuWorld.COLOUR_WIN_1[3] = (byte) 255;
			GLES10.glColorPointer(4, GLES10.GL_UNSIGNED_BYTE, 0, arrowColour);

			GLES10.glTranslatef(curPad, curPad, -10);
			GLES10.glScalef(curIndicatorHeight * 3, curIndicatorHeight * 3, 1);
			GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, 7);
			GLES10.glPopMatrix();
		}

		if (storeOpen)
			fade += .025f;
		else
			fade -= .025f;
		if (fade > .8f)
			fade = .8f;
		else if (fade < 0)
			fade = 0;

		if (fade > 0) {
			storeNumber = storeNumber * .92f + targetStoreNumber * .08f;

			GLES10.glDisableClientState(GLES10.GL_COLOR_ARRAY);
			GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, fullScreen);
			GLES10.glDisable(GLES10.GL_TEXTURE_2D);
			GLES10.glColor4f(0, 0, 0, fade);
			GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, 4);

			float amt1 = Util.slowOut(fade * 1.25f);
			// draw the upgrade menu!
			GLES10.glPushMatrix();
			GLES10.glTranslatef(0, screenHeight * 18 / 13 - amt1 * screenHeight * 19 / 13, 0);

			textDraw.setColour(MenuWorld.COLOUR_BASE_F);
			textDraw.draw(screenWidth * 8 / 13, Math.min(screenHeight * 12.5f / 13, screenHeight - view.getAdHeight() + textDraw.getCharSize()),
					STORE_LABEL[targetStoreNumber], true);

			GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, endLineBuffer);
			GLES10.glDisable(GLES10.GL_TEXTURE_2D);
			GLES10.glColorPointer(4, GLES10.GL_UNSIGNED_BYTE, 0, endLineColour);
			GLES10.glEnableClientState(GLES10.GL_COLOR_ARRAY);
			GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 0, 6);

			GLES10.glTranslatef(-storeNumber * screenWidth, 0, 0);
			GameState gameState = world.getGameState();

			// First 4 upgrades are for $
			if (storeNumber < .5) {
				for (int i = 0; i < 4; i++)
					textDraw.draw(screenWidth * 4 / 13, screenHeight * (10.5f - 2 * i) / 13 + textDraw.getCharSize(), GameState.UPGRADE_NAMES[i], true);

			} else if (storeNumber > 1.5) {
				for (int i = 6; i < GameState.UPGRADE_NAMES.length; i++) {
					if (i == 7 && gameState.getShipUpgrades()[GameState.UPGRADE_TURRET] == 0)
						textDraw.setColour(MenuWorld.COLOUR_GREY_F);
					textDraw.draw(screenWidth * 32 / 13, screenHeight * (10.5f - 2 * (i - 6)) / 13 + textDraw.getCharSize(), GameState.UPGRADE_NAMES[i], false);
				}
			} else {
				// next 2 are for $ as well
				textDraw.draw(screenWidth * 14 / 13, screenHeight * 10 / 13 + textDraw.getCharSize(), GameState.UPGRADE_NAMES[GameState.SHIELD_CELL], false);
				textDraw.draw(screenWidth * 14 / 13, screenHeight * 4 / 13 + textDraw.getCharSize(), GameState.UPGRADE_NAMES[GameState.PLASMA_WAVE], false);
			}
			GLES10.glDisable(GLES10.GL_TEXTURE_2D);
			GLES10.glEnableClientState(GLES10.GL_COLOR_ARRAY);
			GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, boxBuffer);

			float x = 0;
			float y = 0;
			for (int i = 0; i < GameState.UPGRADE_COSTS.length; i++) {
				GLES10.glColorPointer(4, GLES10.GL_UNSIGNED_BYTE, 0, onColor);
				for (int j = 0; j < GameState.UPGRADE_COSTS[i].length - 1; j++) {
					if (j == gameState.getShipUpgrades()[i])
						GLES10.glColorPointer(4, GLES10.GL_UNSIGNED_BYTE, 0, offColor);

					if (i < 4) {
						x = screenWidth * 4.2f / 13;
						y = screenHeight * (10.5f - 2 * i) / 13;
					} else if (i < 6) {
						x = screenWidth * 14 / 13;
						if (i == GameState.SHIELD_CELL)
							// left side
							y = screenHeight * 9 / 13;
						else
							// i == GameState.PLASMA_WAVE
							// right side
							y = screenHeight * 5 / 13;
					} else {
						x = screenWidth * 35.5f / 13;
						y = screenHeight * (10.5f - 2 * (i - 6)) / 13;
					}
					x += textDraw.getCharSize() * j;

					GLES10.glPushMatrix();
					GLES10.glTranslatef(x, y, 0);
					GLES10.glScalef(textDraw.getCharSize(), textDraw.getCharSize(), 1);
					GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, 4);
					GLES10.glPopMatrix();
				}
			}

			textDraw.setColour(MenuWorld.COLOUR_WIN);
			for (int i = 0; i < GameState.UPGRADE_COSTS.length; i++) {

				int upgradeLevel = gameState.getShipUpgrades()[i];
				int cost;
				if (upgradeLevel >= GameState.UPGRADE_COSTS[i].length)
					cost = 0;
				else
					cost = GameState.UPGRADE_COSTS[i][upgradeLevel];

				boolean draw;
				if (cost != -1) {
					if (i < 4) {
						x = screenWidth * 6 / 13;
						y = screenHeight * (10.5f - 2 * i) / 13 + textDraw.getCharSize();
						draw = storeNumber < .5;
					} else if (i < 6) {
						x = screenWidth * 16 / 13;
						if (i == GameState.SHIELD_CELL)
							// left side
							y = screenHeight * 9 / 13 + textDraw.getCharSize();
						else
							// i == GameState.PLASMA_WAVE
							y = screenHeight * 5 / 13 + textDraw.getCharSize();
						draw = storeNumber > .3 && storeNumber < 1.5;
					} else {
						x = screenWidth * 37 / 13;
						y = screenHeight * (10.5f - 2 * (i - 6)) / 13 + textDraw.getCharSize();
						draw = storeNumber > 1.5;
					}

					if (draw) {
						if (i == 7 && gameState.getShipUpgrades()[GameState.UPGRADE_TURRET] == 0)
							textDraw.setColour(MenuWorld.COLOUR_GREY_F);

						textDraw.draw(x, y, (GameState.COST_STARS[i] ? "" : "$") + cost + (GameState.COST_STARS[i] ? "*" : ""), false);
					}
				}
			}

			GLES10.glPushMatrix();
			// draw the paint texture
			GLES10.glColor4f(1, 1, 1, fade * 1.25f);
			GLES10.glDisableClientState(GLES10.GL_COLOR_ARRAY);
			GLES10.glTexCoordPointer(2, GLES10.GL_FLOAT, 0, texBuffer);
			GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, paintBoxBuffer);
			GLES10.glEnable(GLES10.GL_TEXTURE_2D);
			GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, paintTex[0]);

			GLES10.glTranslatef(screenWidth * 21f / 13, screenHeight * 3.6f / 13, 0);
			GLES10.glScalef(screenWidth * 3 / 13, screenHeight * 7f / 13, 1);
			GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, 4);
			GLES10.glPopMatrix();

			GLES10.glPushMatrix();
			GLES10.glTranslatef(screenWidth * 24.2f / 13, screenHeight * 3.7f / 13, 0);
			GLES10.glRotatef(90, 0, 0, 1);
			textDraw.setColour(MenuWorld.COLOUR_WIN);
			textDraw.draw(0, 0, "ALL PAINT:$10", false);
			GLES10.glPopMatrix();

			// Draw the left and right arrows
			y = screenHeight / 2 + textDraw.getCharSize() * 2;
			if (storeNumber < 0.01) {
				x = screenWidth - textDraw.getCharSize() * 2;
				textDraw.draw(x - arrowOffset, y, ">", false);
			} else if (storeNumber > 0.99 && storeNumber < 1.01) {
				x = screenWidth + textDraw.getCharSize();
				textDraw.draw(x + arrowOffset, y, "<", false);
				x = screenWidth * 2 - textDraw.getCharSize() * 2;
				textDraw.draw(x - arrowOffset, y, ">", false);
			} else if (storeNumber > 1.99) {
				x = screenWidth * 2 + textDraw.getCharSize();
				textDraw.draw(x + arrowOffset, y, "<", false);
			}

			arrowOffset = (float) Math.sin((double) System.currentTimeMillis() / 100.0) * curPad;

			GLES10.glPopMatrix();

			GLES10.glPushMatrix();
			GLES10.glEnableClientState(GLES10.GL_COLOR_ARRAY);
			GLES10.glColorPointer(4, GLES10.GL_UNSIGNED_BYTE, 0, endLineColour);
			GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, endLineBuffer);
			GLES10.glTranslatef(screenWidth, screenHeight * 12 / 13 + amt1 * screenHeight * 2 / 13, 0);
			GLES10.glDisable(GLES10.GL_TEXTURE_2D);
			GLES10.glRotatef(180, 0, 0, 1);
			GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 0, 6);

			GLES10.glPopMatrix();
		}

		// Draw the star score
		GameState gameState = world.getGameState();
		MenuWorld.COLOUR_WIN[3] = inFade;
		textDraw.setColour(MenuWorld.COLOUR_WIN);

		if (gameState.getMaxLevel() > 2 && (gameState.getCredits() > 0 || gameState.getStars() > 0)) {
			if (lastCredits != gameState.getCredits() || creditString == null) {
				creditString = "$" + gameState.getCredits();
				lastCredits = gameState.getCredits();
			}
			textDraw.draw(curPad, screenHeight - curPad, creditString, false);
			if (lastStars != gameState.getStars() || starString == null) {
				starString = "" + gameState.getStars() + "*";
				lastStars = gameState.getStars();
			}
			textDraw.draw(screenWidth - curPad, screenHeight - curPad, starString, true);

			if (fade == 0) {
				MenuWorld.COLOUR_WIN[3] = 0.5f * inFade;
				String msg2 = gameState.isFullVersion() ? " <UPGRADE YOUR SHIP!" : " <SHIP";
				GLES10.glPushMatrix();
				GLES10.glTranslatef(textDraw.getCharSize() / 2 * (float) Math.sin((double) System.currentTimeMillis() / 750.0), textDraw.getCharSize(), 0);
				textDraw.draw(curPad + creditString.length() * textDraw.getCharSize(), screenHeight - curPad - textDraw.getCharSize(), msg2, false);
				GLES10.glPopMatrix();
			}
		}

		if (((PMenuCamera) world.getCamera()).getTracking() == null && fade == 0) {
			MenuWorld.COLOUR_WIN[3] = ((MenuWorld) world).getGalaxyParticles()[3].getProportion() * inFade;
			String msg1 = "CREDITS";
			textDraw.draw(screenWidth - curPad, curPad + textDraw.getCharSize(), msg1, true);
		}

		MenuWorld.COLOUR_WIN[3] = 1;

		// draw ship last (requres different projection matrix)
		if (fade > 0) {
			long curTime = System.currentTimeMillis();
			int dTime = (int) (curTime - lastTime);

			float aspectRatio = (float) screenWidth / screenHeight;
			GLES10.glMatrixMode(GLES10.GL_PROJECTION);
			float size = 200 * fovTan;
			GLES10.glLoadIdentity();
			GLES10.glFrustumf(-size, size, -size / aspectRatio, size / aspectRatio, 200, 4000);
			GLES10.glMatrixMode(GLES10.GL_MODELVIEW);
			GLES10.glLoadIdentity();

			GLES10.glEnable(GLES10.GL_DEPTH_TEST);
			GLES10.glPushMatrix();
			GLES10.glTranslatef(350 + (1 - Util.slowOut(fade * 1.25f)) * 1000 - storeNumber * 350, (storeNumber - 1) * (storeNumber - 1) * 60 - 25, -2500);
			GLES10.glRotatef(-66, 1, 0, 0);
			int[] upgrades = gameState.getShipUpgrades();
			spaceShip.draw(1, 0, 0, 0, rotFace * Util.RAD_TO_DEG, 0, 0, upgrades[GameState.UPGRADE_LASER_COUNT] + 1, -rotFace * Util.RAD_TO_DEG,
					upgrades[GameState.UPGRADE_TURRET], upgrades[GameState.UPGRADE_TURRET_SPREAD]);

			float sinA1 = (float) Math.sin(rotFace);
			float cosA1 = (float) Math.cos(rotFace);
			shipParticles.setAccel(upgrades[GameState.UPGRADE_THRUSTER]);
			shipParticles.particleStep(curTime, dTime, cosA1, sinA1, 0, 0, 0, 0, 0, true);

			GLES10.glEnable(GLES10.GL_BLEND);
			// We want the depth test here but not during the game
			GLES10.glEnable(GLES10.GL_DEPTH_TEST);
			GLES10.glPointSize(5);
			GLES10.glEnableClientState(GLES10.GL_COLOR_ARRAY);
			GLES10.glColorPointer(4, GLES10.GL_UNSIGNED_BYTE, 0, shipParticles.colourByte());
			GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, shipParticles.getFloatBuffer());
			GLES10.glDrawArrays(GLES10.GL_POINTS, 0, shipParticles.getLastNPoints());
			GLES10.glDisable(GLES10.GL_DEPTH_TEST);

			GLES10.glRotatef(66, 1, 0, 0);
			GLES10.glTranslatef(0, 0, -60);
			spaceShip.setShield(PSpaceShip.BASE_SHIELD + upgrades[GameState.UPGRADE_SHIELD] * PSpaceShip.BASE_SHIELD / 2, 1);
			GLES10.glColorPointer(4, GLES10.GL_UNSIGNED_BYTE, 0, spaceShip.getShieldColourBuffer());
			GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, spaceShip.getShieldVertexData());
			GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, SpaceShip.SHIELD_N);

			rotFace += .01f;
			lastTime = curTime;
			GLES10.glPopMatrix();
		}
	}

	@Override
	public boolean pressBack() {
		if (!storeOpen && ((PMenuCamera) world.getCamera()).getTracking() == null)
			return true;

		if (storeOpen)
			storeOpen = false;
		else
			((PMenuCamera) world.getCamera()).setTracking(null);

		return false;
	}

	public float getFade() {
		return fade;
	}

	public void release() {
		if (spaceShip != null)
			spaceShip.release();
		GLES10.glDeleteTextures(1, paintTex, 0);
	}

	public void reloadTexture(Context context) {
		Util.createTexture(context, R.drawable.paints, paintTex);
		if (spaceShip != null)
			spaceShip.reloadTexture(context);
	}

	@Override
	public boolean isEndSequence() {
		// Never...
		return false;
	}
}