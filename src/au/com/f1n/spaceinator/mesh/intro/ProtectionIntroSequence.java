package au.com.f1n.spaceinator.mesh.intro;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES10;
import au.com.f1n.spaceinator.GBActivity;
import au.com.f1n.spaceinator.GBRenderer;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.mesh.OnScreenAbstract;
import au.com.f1n.spaceinator.mesh.TextDrawer;
import au.com.f1n.spaceinator.physics.menu.MenuWorld;

public class ProtectionIntroSequence extends IntroSequence {
	private static final String[][] TEXTS = { { "THE NACHT PLAGUE MOST STARS", "IN THE MILKY WAY..." },
			{ "YOU HAVE NOT UPGRADED YOUR SHIP!", "", "BUY UPGRADES FROM THE MAIN MENU.", "" }, { "<DRIVE" }, { "SHOOT>" }, { "<ZOOM>" }, { "PLASMA WAVE>" },
			{ "<SHIELD CELL" }, { "" } };
	private static final float[][] OFFSET = { { 0, 0 }, { 0, 0 }, // nothings
			{ -0.2f, 0 },// drive
			{ 0.2f, 0 },// shoot
			{ 0, -0.38f },// zoom
			{ 0.24f, .27f },// plasma
			{ -0.23f, .27f },// shield
			{ 0, 0 },// extra
			{ 0, 0 },// extra
	};
	private int screen;
	private int line;
	private int charNum;
	private long nextCharTime;
	private boolean tapped;
	private boolean skipScreen1 = true;

	int[] textures = new int[1];
	private FloatBuffer squareArray;
	private FloatBuffer squareTextureData;
	private float fade = 0;
	private float musicAlpha = 1;
	private boolean loaded;

	public ProtectionIntroSequence(GBRenderer gbRenderer) {
		instantLoad = false;

		int[] shipU = gbRenderer.getGameState().getShipUpgrades();

		// if (true){
		if (shipU[GameState.UPGRADE_LASER_COUNT] + shipU[GameState.UPGRADE_SHIELD] + shipU[GameState.UPGRADE_THRUSTER] + shipU[GameState.UPGRADE_LASER_POWER] == 0) {
			skipScreen1 = false;
			TEXTS[1][3] = "YOU HAVE $" + gbRenderer.getGameState().getCredits() + " TO SPEND!";
		}

		reloadTexture(gbRenderer.getContext());
		squareArray = Util.makeFloatBuffer(3 * 4);
		squareTextureData = Util.makeFloatBuffer(2 * 4);

		float z = -10;

		float indent = OnScreenAbstract.INDICATOR_HEIGHT * gbRenderer.height * 2;

		squareArray.put(indent);
		squareArray.put(indent);
		squareArray.put(z);
		squareTextureData.put(0);
		squareTextureData.put(1);

		squareArray.put(gbRenderer.width - indent);
		squareArray.put(indent);
		squareArray.put(z);
		squareTextureData.put(1);
		squareTextureData.put(1);

		squareArray.put(gbRenderer.width - indent);
		squareArray.put(gbRenderer.height - indent);
		squareArray.put(z);
		squareTextureData.put(1);
		squareTextureData.put(0);

		squareArray.put(indent);
		squareArray.put(gbRenderer.height - indent);
		squareArray.put(z);
		squareTextureData.put(0);
		squareTextureData.put(0);

		squareArray.position(0);
		squareTextureData.position(0);
	}

	@Override
	public synchronized boolean draw(GBRenderer gbRenderer, TextDrawer textDraw) {
		// Draw stars
		super.draw(gbRenderer, textDraw);

		int width = gbRenderer.width;
		int height = gbRenderer.height;

		// Setup orthogonal projection
		GLES10.glMatrixMode(GL10.GL_PROJECTION);
		GLES10.glLoadIdentity();
		GLES10.glOrthof(0, width, 0, height, 0.001f, 100);
		GLES10.glMatrixMode(GL10.GL_MODELVIEW);

		GLES10.glLoadIdentity();
		GLES10.glTranslatef(0.0f, 0, -3.0f);

		GLES10.glDisable(GL10.GL_LIGHTING);
		GLES10.glEnableClientState(GL10.GL_VERTEX_ARRAY);

		GLES10.glEnable(GL10.GL_BLEND);
		GLES10.glDisableClientState(GL10.GL_COLOR_ARRAY);

		textDraw.setColour(MenuWorld.COLOUR_AVAILABLE_F);

		if (musicAlpha > 0) {
			MenuWorld.COLOUR_AVAILABLE_F[3] = musicAlpha;
			if (gbRenderer.getSoundManager().getTrackName() != null)
				textDraw.draw(width, height - textDraw.getCharSize() - gbRenderer.getAdHeight(), gbRenderer.getSoundManager().getTrackName(), true);

			musicAlpha -= 0.005f;
		}

		if (screen >= 2) {
			GLES10.glEnable(GL10.GL_TEXTURE_2D);
			GLES10.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			GLES10.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
			GLES10.glTexCoordPointer(2, GL10.GL_FLOAT, 0, squareTextureData);
			GLES10.glColor4f(1, 1, 1, fade);

			if (fade < 1)
				fade += .04f;
			else if (fade > 1)
				fade = 1;

			GLES10.glVertexPointer(3, GL10.GL_FLOAT, 0, squareArray);
			GLES10.glPushMatrix();
			GLES10.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 4);
			GLES10.glPopMatrix();
		}

		String[] curText;

		MenuWorld.COLOUR_AVAILABLE_F[3] = 1;
		textDraw.setColour(MenuWorld.COLOUR_AVAILABLE_F);

		if (screen < TEXTS.length) {
			curText = TEXTS[screen];

			if (System.currentTimeMillis() > nextCharTime) {
				if (line >= curText.length) {
					line = 0;
					charNum = 0;
					screen++;
					// This is a new screen - will pickup on the next frame
					if (screen == 1 && skipScreen1)
						screen++;
				} else {
					charNum++;
					if (charNum > curText[line].length()) {
						charNum = 0;
						line++;

						if (line >= curText.length) {
							if (screen >= TEXTS.length - 1) {
								// We will hit the end of the screens next (pause
								// created by loading)
							} else {
								// about to switch to the next screen - long pause
								nextCharTime = System.currentTimeMillis() + 3800;
								if (screen == 0)
									nextCharTime -= 2000;
							}
						} else
							// Line skip
							nextCharTime = System.currentTimeMillis() + 20;
					} else {
						// Character skip
						nextCharTime = System.currentTimeMillis() + 20;
					}
				}
			}
		} else {
			// waiting for a tap
			curText = TEXTS[TEXTS.length - 1];

			String tap = "OK, I GOT IT!";
			textDraw.draw(width / 2 - tap.length() * textDraw.getCharSize() / 2, height / 2, tap, false);

			if (!loaded) {
				gbRenderer.loadNewWorld();
				loaded = true;
			}
		}
		if (screen == TEXTS.length)
			line = curText.length;

		float top = height / 2 + curText.length * textDraw.getCharSize() + gbRenderer.height * OFFSET[screen][1];

		float bounce;
		if (screen > 1 && screen < 7)
			bounce = (float) Math.sin((double) System.currentTimeMillis() / 100.0) * textDraw.getCharSize();
		else
			bounce = 0;

		for (int i = 0; i < curText.length; i++) {
			if (i < line) {
				// Draw the full line of text
				textDraw.draw(width / 2 - curText[i].length() * textDraw.getCharSize() / 2 + gbRenderer.width * OFFSET[screen][0] + bounce,
						top - textDraw.getCharSize() * i * 2, curText[i], false);
			} else if (i == line) {
				String tmp = curText[i].substring(0, charNum);
				// Draw a partial line of text
				textDraw.draw(width / 2 - curText[i].length() * textDraw.getCharSize() / 2 + gbRenderer.width * OFFSET[screen][0] + bounce,
						top - textDraw.getCharSize() * i * 2, tmp, false);
			}
		}

		return tapped && screen >= TEXTS.length && loaded;
	}

	@Override
	public synchronized void tap(float x, float y) {
		tapped = true;
		if (screen < TEXTS.length)
			// Tapping does something current
			if (line >= TEXTS[screen].length) {
				// force next screen
				screen++;
				if (screen == 1 && skipScreen1)
					// special case
					screen++;
			} else {
				// force end of typing
				line = TEXTS[screen].length;
				nextCharTime = System.currentTimeMillis() + 1200;
			}
	}

	public void release() {
		GLES10.glDeleteTextures(1, textures, 0);
	}

	public void reloadTexture(GBActivity context) {
		Util.createTexture(context, R.drawable.instructions, textures);
	}
}
