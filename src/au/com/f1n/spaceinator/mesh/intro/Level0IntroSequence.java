package au.com.f1n.spaceinator.mesh.intro;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES10;
import au.com.f1n.spaceinator.GBActivity;
import au.com.f1n.spaceinator.GBRenderer;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.mesh.TextDrawer;
import au.com.f1n.spaceinator.physics.menu.MenuWorld;

public class Level0IntroSequence extends IntroSequence {
	private static final String[][] TEXTS = { { "2581 AD", "WE ARE NOT ALONE." }, { "THE ALIENS INVADED MARS COLONY 2", " THEY CALL THEMSELVES 'THE NACHT'" },
			{ "WITH A COMMON ENEMY,", "EARTH POOLED ALL OF ITS", "RESOURCES TO CREATE ONE SHIP." }, { "THE SPACEINATOR." }, { "<DRIVE" }, { "SHOOT>" } };
	private int screen;
	private int line;
	private int charNum;
	private long nextCharTime;
	private boolean tapped;
	int[] tex1 = new int[1];
	int[] tex2 = new int[1];
	private FloatBuffer squareTextureData;
	private FloatBuffer squareArray;
	private FloatBuffer squareTextureDataMars;
	private int marsHeight = 0;
	private int shipHeight = 0;
	private float yTrans;
	private float fade = 1;
	private float fade2 = 0;
	private float musicAlpha = 1;
	private boolean loaded;
	private boolean jumpedToTute;

	public Level0IntroSequence(GBRenderer gbRenderer) {
		instantLoad = false;

		reloadTexture(gbRenderer.getContext());
		squareArray = Util.makeFloatBuffer(3 * 4);
		squareTextureDataMars = Util.makeFloatBuffer(2 * 4);
		marsHeight = (int) (gbRenderer.height * 100f / 512f);
		shipHeight = (int) (gbRenderer.height * 392f / 512f);
		squareTextureData = Util.makeFloatBuffer(2 * 4);

		float z = -10;

		squareArray.put(0);
		squareArray.put(0);
		squareArray.put(z);
		squareTextureDataMars.put(0);
		squareTextureDataMars.put(0);
		squareTextureData.put(0);
		squareTextureData.put(1);

		squareArray.put(gbRenderer.width);
		squareArray.put(0);
		squareArray.put(z);
		squareTextureDataMars.put(1);
		squareTextureDataMars.put(0);
		squareTextureData.put(1);
		squareTextureData.put(1);

		squareArray.put(gbRenderer.width);
		squareArray.put(gbRenderer.height);
		squareArray.put(z);
		squareTextureDataMars.put(1);
		squareTextureDataMars.put(1);
		squareTextureData.put(1);
		squareTextureData.put(0);

		squareArray.put(0);
		squareArray.put(gbRenderer.height);
		squareArray.put(z);
		squareTextureDataMars.put(0);
		squareTextureDataMars.put(1);
		squareTextureData.put(0);
		squareTextureData.put(0);

		squareArray.position(0);
		squareTextureDataMars.position(0);
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

		if (screen >= 1 && fade > 0 && !jumpedToTute) {
			GLES10.glEnable(GL10.GL_TEXTURE_2D);
			GLES10.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			GLES10.glBindTexture(GL10.GL_TEXTURE_2D, tex1[0]);
			GLES10.glTexCoordPointer(2, GL10.GL_FLOAT, 0, squareTextureDataMars);
			GLES10.glColor4f(1, 1, 1, fade);

			GLES10.glVertexPointer(3, GL10.GL_FLOAT, 0, squareArray);
			GLES10.glPushMatrix();
			GLES10.glTranslatef(0, yTrans - height, 0);
			if (yTrans < marsHeight)
				yTrans++;
			else
				fade -= .01f;
			GLES10.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 4);
			GLES10.glPopMatrix();
		}

		if (screen >= 2 && !jumpedToTute) {
			GLES10.glEnable(GL10.GL_TEXTURE_2D);
			GLES10.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			GLES10.glBindTexture(GL10.GL_TEXTURE_2D, tex1[0]);
			GLES10.glTexCoordPointer(2, GL10.GL_FLOAT, 0, squareTextureDataMars);
			GLES10.glColor4f(1, 1, 1, fade2);

			if (fade2 < 1)
				fade2 += .02f;

			GLES10.glVertexPointer(3, GL10.GL_FLOAT, 0, squareArray);
			GLES10.glPushMatrix();
			GLES10.glTranslatef(0, height - shipHeight, 0);
			GLES10.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 4);
			GLES10.glPopMatrix();
		} else if (screen >= 2) {
			GLES10.glEnable(GL10.GL_TEXTURE_2D);
			GLES10.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			GLES10.glBindTexture(GL10.GL_TEXTURE_2D, tex2[0]);
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
								nextCharTime = System.currentTimeMillis() + 1800;
								if (screen == 1)
									nextCharTime += 3000;
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

			String tap = "OK, I GOT IT...";
			textDraw.draw(width / 2 - tap.length() * textDraw.getCharSize() / 2, textDraw.getCharSize() * 2, tap, false);

			if (!loaded) {
				gbRenderer.loadNewWorld();
				loaded = true;
			}
		}
		if (screen == TEXTS.length)
			line = curText.length;

		if (screen >= 4 && !jumpedToTute) {
			jumpedToTute = true;
			fade = 0;
		}

		float top = height / 2 + curText.length * textDraw.getCharSize();

		if (screen == 2) {
			top = (curText.length + 2) * textDraw.getCharSize();
		} else if (screen >= 3) {
			top = (curText.length + 3) * textDraw.getCharSize();
		}

		float x = 0;

		if (jumpedToTute) {
			x = screen == 4 ? -.2f : +.2f;
			x += 0.02 * Math.sin((double) System.currentTimeMillis() * 0.002f);
			x *= width;
			top = height / 2;
		}

		for (int i = 0; i < curText.length; i++) {
			if (i < line) {
				// Draw the full line of text
				textDraw.draw(x + width / 2 - curText[i].length() * textDraw.getCharSize() / 2, top - textDraw.getCharSize() * i * 2, curText[i], false);
			} else if (i == line) {
				String tmp = curText[i].substring(0, Math.min(charNum, curText[i].length() - 1));
				// Draw a partial line of text
				textDraw.draw(x + width / 2 - curText[i].length() * textDraw.getCharSize() / 2, top - textDraw.getCharSize() * i * 2, tmp, false);
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
			} else {
				// force end of typing
				line = TEXTS[screen].length;
				nextCharTime = System.currentTimeMillis() + 1200;
			}
	}

	public void release() {
		GLES10.glDeleteTextures(1, tex1, 0);
		GLES10.glDeleteTextures(1, tex2, 0);
	}

	public void reloadTexture(GBActivity context) {
		Util.createTexture(context, R.drawable.intro, tex1);
		Util.createTexture(context, R.drawable.instructions, tex2);
	}
}
