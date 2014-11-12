package au.com.f1n.spaceinator.physics.menu;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES10;
import android.opengl.GLES11;
import au.com.f1n.spaceinator.GBActivity;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.World;
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.game.logic.ScoreList;
import au.com.f1n.spaceinator.mesh.TextDrawer;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.achievement.Achievement;
import com.google.android.gms.games.achievement.Achievements.LoadAchievementsResult;

public class GalaxyBG implements ResultCallback<LoadAchievementsResult> {
	private static final int RADIUS = 720;
	private static final int N = 25;
	private static final float SMALL_RELATION = .618f;
	private static final String[] STAR_CHAR = { "", "*", "**", "***", "****", "*****" };
	private FloatBuffer mBGVertexData;
	private ByteBuffer mBGColorData;
	private MenuWorld menuWorld;
	private FloatBuffer mLineBuffer;
	private FloatBuffer mLineTexture;
	private int[] lineTex = new int[1];
	private int[] wordTex = new int[1];
	private GalaxyParticles[] galaxyParticles;
	private TextDrawer textDraw;
	private float[] tmpModelMatrix = new float[16];
	private float[] tmpProjMatrix = new float[16];
	private float[] vecWork = new float[4];
	private float[] tmpVec = new float[4];
	private float[] winColour;
	private FloatBuffer textVertices;
	private FloatBuffer upgradeTexCoord;

	private boolean clicked;
	private float clickX;
	private float clickY;
	private PMenuCamera camera;
	private float pulse;
	private float bounce;
	private Context context;
	private String[] starCountString;
	private String achieveString;
	private float inFade;

	public GalaxyBG(MenuWorld menuWorld, TextDrawer textDraw, Context context) {
		this.menuWorld = menuWorld;
		this.textDraw = textDraw;
		this.galaxyParticles = menuWorld.getGalaxyParticles();
		this.context = context;

		camera = (PMenuCamera) menuWorld.getCamera();

		float[] bgVertex = new float[N * 3];
		byte[] bgColour = new byte[N * 4];
		bgVertex[0] = 0;
		bgVertex[1] = 0;
		bgVertex[2] = 0;

		bgColour[0] = (byte) 254;
		bgColour[1] = (byte) 241;
		bgColour[2] = (byte) 248;
		bgColour[3] = (byte) 255;

		for (int i = 1; i < N; i++) {
			double angle = Math.PI * 2 * i / (N - 2);

			bgVertex[i * 3] = (float) (Math.cos(angle) * RADIUS);
			bgVertex[i * 3 + 1] = (float) (Math.sin(angle) * RADIUS);
			// bgVertex[i * 3 + 2] = 0;

			bgColour[i * 4] = (byte) 54;
			bgColour[i * 4 + 1] = (byte) 52;
			bgColour[i * 4 + 2] = (byte) 73;
			bgColour[i * 4 + 3] = 0;
		}

		mBGVertexData = Util.makeFloatBuffer(bgVertex);
		mBGColorData = Util.makeByteBuffer(bgColour);

		reloadTexture(context);
		// lineTex
		mLineBuffer = Util.makeFloatBuffer(6 * 3);
		mLineTexture = Util.makeFloatBuffer(6 * 2);
		mLineTexture.put(0);
		mLineTexture.put(0);

		mLineTexture.put(0);
		mLineTexture.put(1);

		mLineTexture.put(0);
		mLineTexture.put(0);

		mLineTexture.put(0);
		mLineTexture.put(1);

		mLineTexture.put(0);
		mLineTexture.put(0);

		mLineTexture.put(0);
		mLineTexture.put(1);
		mLineTexture.position(0);

		vecWork[3] = 1;

		winColour = new float[4];
		System.arraycopy(MenuWorld.COLOUR_WIN, 0, winColour, 0, 4);

		// Make buffers for the upgrade text
		textVertices = Util.makeFloatBuffer(4 * 3);
		upgradeTexCoord = Util.makeFloatBuffer(4 * 2);

		float width = 1500;
		float height = 300;

		textVertices.put(-width);
		textVertices.put(-height);
		textVertices.put(0);
		upgradeTexCoord.put(0);
		upgradeTexCoord.put(1);

		textVertices.put(+width);
		textVertices.put(-height);
		textVertices.put(0);
		upgradeTexCoord.put(1);
		upgradeTexCoord.put(1);

		textVertices.put(+width);
		textVertices.put(+height);
		textVertices.put(0);
		upgradeTexCoord.put(1);
		upgradeTexCoord.put(.8f);

		textVertices.put(-width);
		textVertices.put(+height);
		textVertices.put(0);
		upgradeTexCoord.put(0);
		upgradeTexCoord.put(.8f);

		textVertices.position(0);
		upgradeTexCoord.position(0);

		starCountString = new String[galaxyParticles.length];
		for (int i = 0; i < starCountString.length; i++) {
			int count = 0;
			int total = 0;
			for (int j = 0; j < World.STARS.length; j++) {
				if (World.STARS[j].getGalaxy() == i) {
					menuWorld.getGameState();
					if (menuWorld.getGameState().getScoreList(World.STARS[j]) != null)
						count += GameState.starCount(menuWorld.getGameState().getScoreList(World.STARS[j]).getBest(), World.STARS[j].getStarScores());
					total += 5;
				}
			}

			// Check for achivements here...
			if (!menuWorld.getGameState().achievement_maximum_milky_way_100 && i == 0 && count == total) {
				menuWorld.getGameState().achievement_maximum_milky_way_100 = menuWorld.getGameState().achievement(R.string.achievement_maximum_milky_way_100, 100);
			}

			if (!menuWorld.getGameState().achievement_additional_andromeda_100 && i == 1 && count == total) {
				menuWorld.getGameState().achievement_additional_andromeda_100 = menuWorld.getGameState().achievement(R.string.achievement_additional_andromeda_100,
						100);
			}

			starCountString[i] = "" + count + "/" + total + "*";
		}

		if (((GBActivity) context).isSignedIn())
			Games.Achievements.load(((GBActivity) context).getApiClient(), false).setResultCallback(this);
	}

	/**
	 * Call this first - draws the faded background effect
	 */
	public void draw() {
		GLES10.glDisable(GL10.GL_LIGHTING);
		GLES10.glDisable(GL10.GL_TEXTURE_2D);
		GLES10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		GLES10.glEnableClientState(GL10.GL_COLOR_ARRAY);
		GLES10.glEnable(GL10.GL_BLEND);

		GLES10.glVertexPointer(3, GL10.GL_FLOAT, 0, mBGVertexData);

		int nextLvl = menuWorld.getGameState().getMaxLevel();
		for (int i = 0; i < galaxyParticles.length; i++) {
			GalaxyParticles gp = galaxyParticles[i];
			mBGColorData.put((byte) 254);
			mBGColorData.put((byte) 241);
			mBGColorData.put((byte) 248);

			if (i == nextLvl / 10 && !(gp instanceof GalaxyCreditParticles) && gp.getTracking() <= 0) {
				mBGColorData.put((byte) ((Math.abs(pulse) + 0.5f) * 255));
			} else {
				mBGColorData.put((byte) 255);
			}
			mBGColorData.position(0);
			GLES10.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, mBGColorData);

			GLES10.glPushMatrix();
			GLES10.glTranslatef(gp.getGlowX(), gp.getGlowY(), gp.getGlowZ());
			GLES10.glRotatef(30, 1, 0, 0);
			float s = gp.getGlowSize();
			GLES10.glScalef(s, s, s);
			GLES10.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, N);
			GLES10.glPopMatrix();
		}
	}

	/**
	 * Call this second - draws the level indicators (or the credits)
	 * 
	 * @param gl
	 */
	public void drawLevels() {
		if (((PMenuCamera) menuWorld.getCamera()).getRotXProportion() < 1)
			return;
		else if (inFade < 1)
			inFade += .02f;

		pulse += .01;
		if (pulse > 0.5f)
			pulse = -0.5f;

		int nextLvl = menuWorld.getGameState().getMaxLevel();
		// Draw padlocks first - assume 10 levels per galaxy

		float maxTrack = 0;

		for (int i = 0; i < galaxyParticles.length; i++) {
			GalaxyParticles gp = galaxyParticles[i];
			if (gp.getTracking() > maxTrack)
				maxTrack = gp.getTracking();

			if (i > nextLvl / 10 && !(gp instanceof GalaxyCreditParticles)) {
				textDraw.setColour(MenuWorld.COLOUR_RED);
				MenuWorld.COLOUR_RED[3] = inFade;
				GLES10.glPushMatrix();
				GLES10.glTranslatef(gp.getX() - RADIUS / 2, gp.getY() + RADIUS / 2, 0);
				GLES10.glRotatef(camera.getRotX(), -1, 0, 0);
				GLES10.glRotatef(camera.getRotZ(), 0, 0, -1);
				GLES10.glScalef(2, 2, 2);
				textDraw.draw3D(0, 0, 0, "(", false, 400, 0);
				GLES10.glPopMatrix();
			} else {
				FloatBuffer texName = gp.getTextCoords();
				if (texName != null) {
					GLES10.glDisableClientState(GLES10.GL_COLOR_ARRAY);

					GLES10.glEnable(GLES10.GL_TEXTURE_2D);
					GLES10.glEnableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
					GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, wordTex[0]);
					GLES10.glTexCoordPointer(2, GLES10.GL_FLOAT, 0, texName);
					GLES10.glColor4f(1, 1, 1, inFade);

					GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, textVertices);

					GLES10.glPushMatrix();
					GLES10.glTranslatef(gp.getTextX(), gp.getTextY(), 0);
					GLES10.glRotatef(camera.getRotX(), -1, 0, 0);
					float scale = gp.getLabelScale();
					GLES10.glScalef(scale, scale, scale);
					if (i > 0) {
						GLES10.glRotatef(camera.getRotZ(), 0, 0, -1);
						GLES10.glScalef(1, .5f, 1);
					}
					GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, 4);
					GLES10.glPopMatrix();

					if (gp.getTracking() < 1) {
						textDraw.setColour(MenuWorld.COLOUR_WIN);
						MenuWorld.COLOUR_WIN[3] = (1f - gp.getTracking()) * inFade;
						GLES10.glPushMatrix();
						GLES10.glTranslatef(gp.getX() - RADIUS / 2 - starCountString[i].length() * 100, gp.getY() - RADIUS - (i == 2 ? RADIUS * 1.5f : 0), 0);
						GLES10.glRotatef(camera.getRotX(), -1, 0, 0);
						GLES10.glRotatef(camera.getRotZ(), 0, 0, -1);
						GLES10.glScalef(2, 2, 2);
						textDraw.draw3D(0, 0, 0, starCountString[i], false, 130, 0);
						GLES10.glPopMatrix();
					}
				}
			}
		}

		// Draw play stuff
		textDraw.setColour(((GBActivity) context).isSignedIn() ? MenuWorld.COLOUR_GREEN : MenuWorld.COLOUR_GREY_F);
		MenuWorld.COLOUR_GREEN[3] = MenuWorld.COLOUR_GREY_F[3] = (1f - maxTrack) * inFade;

		GLES10.glPushMatrix();
		GLES10.glTranslatef(galaxyParticles[1].getX() + 430, galaxyParticles[0].getY(), 0);

		GLES10.glRotatef(camera.getRotX(), -1, 0, 0);
		GLES10.glRotatef(PMenuCamera.BASE_ROT_Z, 0, 0, -1);
		GLES10.glScalef(14, 14, 14);

		textDraw.draw3D(0, 0, 0, "[", false, 100, 0);
		GLES10.glPopMatrix();

		GLES10.glPushMatrix();
		GLES10.glTranslatef(galaxyParticles[1].getX(), galaxyParticles[0].getY() + 1000, 0);

		GLES10.glRotatef(camera.getRotX(), -1, 0, 0);
		GLES10.glRotatef(PMenuCamera.BASE_ROT_Z, 0, 0, -1);

		textDraw.draw3D(0, 0, 0, ((GBActivity) context).isSignedIn() ? "SIGNED IN" : "NOT SIGNED IN", false, 300, 0);
		GLES10.glPopMatrix();

		// Draw Achievments
		if (((GBActivity) context).isSignedIn()) {
			GLES10.glPushMatrix();
			GLES10.glTranslatef(galaxyParticles[0].getX() - 2900, galaxyParticles[1].getY() + 5000, 0);

			GLES10.glRotatef(camera.getRotX(), -1, 0, 0);
			GLES10.glRotatef(PMenuCamera.BASE_ROT_Z, 0, 0, -1);

			textDraw.draw3D(0, 0, 0, "ACHIEVEMENTS", false, 220, 0);
			GLES10.glPopMatrix();

			if (achieveString != null) {
				GLES10.glPushMatrix();
				GLES10.glTranslatef(galaxyParticles[0].getX() - 2900, galaxyParticles[1].getY() + 4300, 0);

				GLES10.glRotatef(camera.getRotX(), -1, 0, 0);
				GLES10.glRotatef(PMenuCamera.BASE_ROT_Z, 0, 0, -1);

				textDraw.draw3D(0, 0, 0, achieveString, false, 220, 0);
				GLES10.glPopMatrix();
			}
		}

		// Draw remove ads
		if (!menuWorld.getGameState().isFullVersion()) {
			GLES10.glDisableClientState(GLES10.GL_COLOR_ARRAY);

			GLES10.glEnable(GLES10.GL_TEXTURE_2D);
			GLES10.glEnableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
			GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, wordTex[0]);
			GLES10.glTexCoordPointer(2, GLES10.GL_FLOAT, 0, upgradeTexCoord);
			GLES10.glColor4f(1, 1, 1, inFade);

			GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, textVertices);

			GLES10.glPushMatrix();
			GLES10.glTranslatef(galaxyParticles[0].getX() - 1000, galaxyParticles[1].getY() + 500, 0);
			bounce += .02f;
			GLES10.glRotatef((float) Math.sin(bounce) * 10, 0, 0, 1);

			GLES10.glRotatef(camera.getRotX(), -1, 0, 0);
			GLES10.glRotatef(PMenuCamera.BASE_ROT_Z, 0, 0, -1);

			GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, 4);
			GLES10.glPopMatrix();
		}

		GalaxyParticles curGalaxy = camera.getTracking();
		if (curGalaxy == null)
			// No levels to draw!
			return;

		if (curGalaxy instanceof GalaxyCreditParticles) {
			// Special credit case
			clicked = false;
			creditDraw((GalaxyCreditParticles) curGalaxy);
		} else {
			float s = Util.slowOut(curGalaxy.getTracking());
			float transF = s > .5 ? (s - .5f) * 2 : 0;

			GLES10.glDisable(GL10.GL_LIGHTING);
			GLES10.glDisable(GL10.GL_TEXTURE_2D);
			GLES10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			GLES10.glEnable(GL10.GL_BLEND);

			if (clicked) {
				GLES11.glGetFloatv(GLES11.GL_PROJECTION_MATRIX, tmpProjMatrix, 0);
			}

			float lineWidth = 10;
			int sCount = Math.min(World.getNumLevels(), nextLvl + 1);

			for (int i = 0; i < sCount; i++) {
				if (World.STARS[i].getGalaxy() == curGalaxy.getIndex()) {
					float bigScale = curGalaxy.getTextScale();
					float charSizeBig = bigScale;
					float charSizeSmall = bigScale * SMALL_RELATION;

					String label = World.STARS[i].getLabel();

					float[] colour;

					if (i == nextLvl) {
						colour = MenuWorld.COLOUR_AVAILABLE_F;
						colour[3] = (transF * (Math.abs(pulse) + 0.5f)) * inFade;
					} else {
						colour = MenuWorld.COLOUR_UNAVAILABLE_F;
						colour[3] = transF * inFade;
					}

					// This modifies the unavailable colour but I dont really care
					// that
					// much
					winColour[3] = transF;
					float height = World.STARS[i].getHeight();
					float width = label.length() * bigScale;

					float x = curGalaxy.getStarCoord(i, 0) + curGalaxy.getX();
					float y = curGalaxy.getStarCoord(i, 1) + curGalaxy.getY();
					float z = curGalaxy.getStarCoord(i, 2);

					GLES10.glPushMatrix();
					GLES10.glTranslatef(x, y, z);
					GLES10.glRotatef(90, 1, 0, 0);

					if (clicked) {
						GLES11.glGetFloatv(GLES11.GL_MODELVIEW_MATRIX, tmpModelMatrix, 0);
					}

					if (height < 0) {
						mLineBuffer.put(lineWidth);
						mLineBuffer.put(0);
						mLineBuffer.put(0);

						mLineBuffer.put(-lineWidth);
						mLineBuffer.put(0);
						mLineBuffer.put(0);

						mLineBuffer.put(lineWidth);
						mLineBuffer.put(height + lineWidth);
						mLineBuffer.put(0);

						mLineBuffer.put(-lineWidth);
						mLineBuffer.put(height - lineWidth);
						mLineBuffer.put(0);

						mLineBuffer.put(width);
						mLineBuffer.put(height + lineWidth);
						mLineBuffer.put(0);

						mLineBuffer.put(width);
						mLineBuffer.put(height - lineWidth);
						mLineBuffer.put(0);
						height -= bigScale;
					} else {
						mLineBuffer.put(-lineWidth);
						mLineBuffer.put(0);
						mLineBuffer.put(0);

						mLineBuffer.put(lineWidth);
						mLineBuffer.put(0);
						mLineBuffer.put(0);

						mLineBuffer.put(-lineWidth);
						mLineBuffer.put(height + lineWidth);
						mLineBuffer.put(0);

						mLineBuffer.put(lineWidth);
						mLineBuffer.put(height - lineWidth);
						mLineBuffer.put(0);

						mLineBuffer.put(width);
						mLineBuffer.put(height + lineWidth);
						mLineBuffer.put(0);

						mLineBuffer.put(width);
						mLineBuffer.put(height - lineWidth);
						mLineBuffer.put(0);
					}
					mLineBuffer.position(0);

					GLES10.glDisableClientState(GL10.GL_COLOR_ARRAY);
					GLES10.glColor4f(MenuWorld.COLOUR_UNAVAILABLE_F[0], MenuWorld.COLOUR_UNAVAILABLE_F[1], MenuWorld.COLOUR_UNAVAILABLE_F[2], transF * inFade);
					GLES10.glVertexPointer(3, GL10.GL_FLOAT, 0, mLineBuffer);
					GLES10.glEnable(GL10.GL_TEXTURE_2D);
					GLES10.glBindTexture(GL10.GL_TEXTURE_2D, lineTex[0]);
					GLES10.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mLineTexture);

					GLES10.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 6);

					// Draw the label
					textDraw.setColour(colour);
					textDraw.draw3D(0, height + bigScale + (height < 0 ? -lineWidth : lineWidth), 0, label, false, charSizeBig, 0);
					ScoreList sl = menuWorld.getGameState().getScoreList(World.STARS[i]);
					if (sl != null) {
						int starCount = GameState.starCount(sl.getBest(), World.STARS[i].getStarScores());
						String stars = STAR_CHAR[starCount];
						textDraw.setColour(winColour);
						textDraw.draw3D(lineWidth, height + (height < 0 ? lineWidth + bigScale * (1 + SMALL_RELATION) : -lineWidth), 0, stars, false, charSizeSmall,
								0);
					}
					GLES10.glPopMatrix();

					if (clicked) {
						// Take away 2 small scales
						calcScreen(0, height - bigScale * SMALL_RELATION * 2);
						if (clickX > vecWork[0] && clickY > vecWork[1]) {
							// Add 2 small scales (makes it easier to click)
							calcScreen(bigScale * label.length(), height + bigScale + bigScale * SMALL_RELATION * 2);

							if (clickX < vecWork[0] && clickY < vecWork[1]) {
								menuWorld.gbRenderer.getSoundManager().beepMenu(1);
								menuWorld.clickedStar(i);
							}
						}
					}
				}
			}
			clicked = false;
		}
	}

	/**
	 * Draw the current credits
	 * 
	 * @param curGalaxy
	 */
	private void creditDraw(GalaxyCreditParticles creditGalaxy) {
		int nCred = GalaxyCreditParticles.CREDITS.length;
		// This is a bit of a fudge...
		float curCredF = (creditGalaxy.getStepI() + 50) / 550 * nCred - 1;
		int curCred = (int) curCredF;

		if (curCredF < 1.5f) {
			curCred = 1;
			curCredF = 1 + creditGalaxy.getTracking() * .5f;
		}

		if (curCred > 0 && curCred < nCred) {
			float credI = (float) curCred / (float) nCred * 600;

			float amt = curCredF - curCred;
			float x = creditGalaxy.funcX(credI);
			float y = creditGalaxy.funcY(credI);
			float z = creditGalaxy.funcZ(credI);

			// Poor mans differentiation (it works in this case)
			float x2 = creditGalaxy.funcX(credI + .1f);
			float y2 = creditGalaxy.funcY(credI + .1f);

			float angle = (float) Math.atan2(y - y2, x - x2);

			for (int i = 0; i < GalaxyCreditParticles.CREDITS[curCred].length; i++) {
				if (i == 0) {
					MenuWorld.COLOUR_WIN[3] = amt * (1 - amt) * 4;
					textDraw.setColour(MenuWorld.COLOUR_WIN);
				} else {
					MenuWorld.COLOUR_BASE_F[3] = amt * (1 - amt) * 4;
					textDraw.setColour(MenuWorld.COLOUR_BASE_F);
				}

				GLES10.glPushMatrix();
				GLES10.glTranslatef(x, y, z);
				if (curCred == 1) {
					GLES10.glRotatef(-6 + angle * Util.RAD_TO_DEG, 0, 0, 1);
					GLES10.glTranslatef(0, -creditGalaxy.getTextScale(), 0);
				} else if (curCred < 6) {
					GLES10.glRotatef(90 + angle * Util.RAD_TO_DEG, 0, 0, 1);
					GLES10.glRotatef(-30 + amt * 60, 0, 1, 0);
					GLES10.glTranslatef(-creditGalaxy.getTextScale(), 0, 0);
				} else {
					GLES10.glRotatef(174 + angle * Util.RAD_TO_DEG, 0, 0, 1);
					GLES10.glRotatef(-30 + amt * 60, 1, 0, 0);
					GLES10.glTranslatef(0, -creditGalaxy.getTextScale(), 0);
				}

				textDraw.draw3D(0, -i * creditGalaxy.getTextScale(), 0, GalaxyCreditParticles.CREDITS[curCred][i], curCred > 1 && curCred < 6,
						creditGalaxy.getTextScale(), 0);
				GLES10.glPopMatrix();
			}
		}

		// Reset this after use...
		MenuWorld.COLOUR_BASE_F[3] = 1;
	}

	private void calcScreen(float x, float y) {
		vecWork[0] = x;
		vecWork[1] = y;
		vecWork[2] = 0;
		vecWork[3] = 1;

		// Multiply matrices
		multVec(tmpModelMatrix);
		multVec(tmpProjMatrix);
		// Perspective scale
		vecWork[0] /= vecWork[3];
		vecWork[1] /= vecWork[3];
		// Convert to screen coords
		vecWork[0] = vecWork[0] / 2 + .5f;
		vecWork[1] = vecWork[1] / 2 + .5f;
	}

	private void multVec(float[] mat) {
		Arrays.fill(tmpVec, 0);
		for (int c = 0; c < 4; c++) {
			for (int r = 0; r < 4; r++) {
				tmpVec[r] += mat[c * 4 + r] * vecWork[c];
			}
		}
		// Copy the result into the in
		System.arraycopy(tmpVec, 0, vecWork, 0, 4);
	}

	public void click(float x, float y) {
		if (camera.getTracking() == null && ((PMenuCamera) menuWorld.getCamera()).isFinishedTrack()) {
			// User clicked screen to select a galaxy - this is quite the hack...
			if (x < .333) {
				if (y > .8 && !menuWorld.getGameState().isFullVersion()) {
					buyFullVersion();
				} else if (y > .5) {
					if (((GBActivity) context).isSignedIn())
						((GBActivity) context).startActivityForResult(Games.Achievements.getAchievementsIntent(((GBActivity) context).getApiClient()), 0);
				} else {
					camera.setTracking(menuWorld.getGalaxyParticles()[0]);
					menuWorld.gbRenderer.getSoundManager().beepMenu(.4f);
				}
			} else if (x < .666) {
				if (y < .5) {
					((GBActivity) context).signClick();
				} else {
					camera.setTracking(menuWorld.getGalaxyParticles()[1]);
					menuWorld.gbRenderer.getSoundManager().beepMenu(.4f);
				}
			} else if (y > .9) {
				camera.setTracking(menuWorld.getGalaxyParticles()[3]);
				menuWorld.gbRenderer.getSoundManager().beepMenu(.4f);
			} else {
				camera.setTracking(menuWorld.getGalaxyParticles()[2]);
				menuWorld.gbRenderer.getSoundManager().beepMenu(.4f);
			}
		} else {
			if (x < .25 && y > .75 && ((PMenuCamera) menuWorld.getCamera()).isFinishedTrack()) {
				// User clicked back
				camera.setTracking(null);
				menuWorld.gbRenderer.getSoundManager().beepMenu(.3f);
			} else {
				clicked = true;
				clickX = x;
				// Invert y so it's in opengl terms
				clickY = 1 - y;
			}
		}
	}

	private void buyFullVersion() {
		((GBActivity) context).purchase();
	}

	public void release() {
		GLES10.glDeleteTextures(1, lineTex, 0);
		GLES10.glDeleteTextures(1, wordTex, 0);
	}

	public void reloadTexture(Context context2) {
		Util.createTexture(context, R.drawable.bar, lineTex);
		Util.createTexture(context, R.drawable.galaxies, wordTex);
	}

	@Override
	public void onResult(LoadAchievementsResult a) {
		if (a.getStatus().getStatusCode() == GamesStatusCodes.STATUS_OK) {
			int complete = 0;
			int total = 0;

			for (Achievement ac : a.getAchievements()) {
				if (ac.getState() == Achievement.STATE_UNLOCKED) {
					complete++;
				}

				total++;
			}

			if (total == complete)
				achieveString = "ALL COMPLETE!";
			else
				achieveString = complete + "/" + total;

			try {
				a.getAchievements().close();
			} catch (Exception e) {
				// who cares
			}
		} else {
			achieveString = "UANBLE TO LOAD ACHIEVEMENTS";
		}

	}
}
