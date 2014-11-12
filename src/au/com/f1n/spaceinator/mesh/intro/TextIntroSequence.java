package au.com.f1n.spaceinator.mesh.intro;

import android.opengl.GLES10;
import au.com.f1n.spaceinator.GBRenderer;
import au.com.f1n.spaceinator.mesh.TextDrawer;
import au.com.f1n.spaceinator.physics.menu.MenuWorld;

public class TextIntroSequence extends IntroSequence {
	private String[][] texts;
	private int screen;
	private int line;
	private int charNum;
	private long nextCharTime;
	private boolean tapped;
	private float musicAlpha = 1;
	private boolean loaded;

	public TextIntroSequence(String[][] texts) {
		this.texts = texts;
		instantLoad = false;
	}

	@Override
	public synchronized boolean draw(GBRenderer gbRenderer, TextDrawer textDraw) {
		// Draw stars
		super.draw(gbRenderer, textDraw);

		int width = gbRenderer.width;
		int height = gbRenderer.height;

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

		String[] curText;

		textDraw.setColour(MenuWorld.COLOUR_AVAILABLE_F);

		if (musicAlpha > 0) {
			MenuWorld.COLOUR_AVAILABLE_F[3] = musicAlpha;

			if (gbRenderer.getSoundManager().getTrackName() != null)
				textDraw.draw(width, height - textDraw.getCharSize() - gbRenderer.getAdHeight(), gbRenderer.getSoundManager().getTrackName(), true);

			musicAlpha -= 0.005f;
		}

		MenuWorld.COLOUR_AVAILABLE_F[3] = 1;

		if (screen < texts.length) {
			curText = texts[screen];

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
							if (screen >= texts.length - 1) {
								// We will hit the end of the screens next (pause
								// created by loading later)
							} else {
								// about to switch to the next screen - long pause
								nextCharTime = System.currentTimeMillis() + 1800;
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
			curText = texts[texts.length - 1];

			String tap = "TAP TO CONTINUE...";
			textDraw.draw(width / 2 - tap.length() * textDraw.getCharSize() / 2, textDraw.getCharSize() * 2, tap, false);

			if (!loaded) {
				gbRenderer.loadNewWorld();
				loaded = true;
			}
		}
		if (screen == texts.length)
			line = curText.length;

		float top = height / 2 + curText.length * textDraw.getCharSize();

		for (int i = 0; i < curText.length; i++) {
			if (i < line) {
				// Draw the full line of text
				textDraw.draw(width / 2 - curText[i].length() * textDraw.getCharSize() / 2, top - textDraw.getCharSize() * i * 2, curText[i], false);
			} else if (i == line) {
				String tmp = curText[i].substring(0, charNum);
				// Draw a partial line of text
				textDraw.draw(width / 2 - curText[i].length() * textDraw.getCharSize() / 2, top - textDraw.getCharSize() * i * 2, tmp, false);
			}
		}

		return tapped && screen >= texts.length && loaded;
	}

	@Override
	public synchronized void tap(float x, float y) {
		tapped = true;
		if (screen < texts.length)
			// Tapping does something current
			if (line >= texts[screen].length) {
				// force next screen
				screen++;
			} else {
				// force end of typing
				line = texts[screen].length;
				nextCharTime = System.currentTimeMillis() + 1200;
			}
	}
}
