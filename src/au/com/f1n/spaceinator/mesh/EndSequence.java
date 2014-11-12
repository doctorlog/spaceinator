package au.com.f1n.spaceinator.mesh;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.google.android.gms.games.Games;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.opengl.GLES10;
import au.com.f1n.spaceinator.GBActivity;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.World;
import au.com.f1n.spaceinator.game.WorldLevel0;
import au.com.f1n.spaceinator.game.WorldLevel2;
import au.com.f1n.spaceinator.game.WorldWarp1;
import au.com.f1n.spaceinator.game.WorldWarp2;
import au.com.f1n.spaceinator.physics.menu.MenuWorld;

public class EndSequence {
	private static final float[] WHITE = { 1, 1, 1, 1 };

	private long startTime = -1;

	private FloatBuffer endLineBuffer;
	private ByteBuffer endLineColour;
	private FloatBuffer starBuffer;
	private ByteBuffer starColourNew;
	private ByteBuffer starColourOld;

	private int screenWidth;
	private int screenHeight;
	private TextDrawer textDraw;
	private World world;

	private long nextBeep = 0;

	private int lastScore;

	private int oldMaxBigStar;

	private boolean finished;
	private int[] starScores;

	private float angle;

	public EndSequence(Context context, int screenWidth, int screenHeight, TextDrawer textDraw, World world) {
		this.screenHeight = screenHeight;
		this.screenWidth = screenWidth;
		this.textDraw = textDraw;
		this.world = world;
		starScores = world.getWorldDef().getStarScores();

		// Play standard menu music
		world.gbRenderer.getSoundManager().playMusic(R.raw.music_menu);

		float thickness = 4;

		startTime = System.currentTimeMillis();

		endLineBuffer = Util.makeFloatBuffer(6 * 3);
		endLineColour = ByteBuffer.allocateDirect(6 * 4);

		endLineBuffer.put(0);
		endLineBuffer.put(screenHeight);
		endLineBuffer.put(-10);

		endLineBuffer.put(0);
		endLineBuffer.put(screenHeight - thickness);
		endLineBuffer.put(-10);

		endLineBuffer.put(screenWidth * 7 / 13);
		endLineBuffer.put(screenHeight);
		endLineBuffer.put(-10);

		endLineBuffer.put(screenWidth * 7 / 13);
		endLineBuffer.put(screenHeight - thickness);
		endLineBuffer.put(-10);

		float extra = screenWidth * 1 / 13;

		endLineBuffer.put(screenWidth * 7 / 13 + extra);
		endLineBuffer.put(screenHeight - extra * .7f);
		endLineBuffer.put(-10);

		endLineBuffer.put(screenWidth * 7 / 13 + extra);
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

		starBuffer = Util.makeFloatBuffer(12 * 3);
		starColourNew = ByteBuffer.allocateDirect(12 * 4);
		starColourOld = ByteBuffer.allocateDirect(12 * 4);
		starColourNew.position(0);
		starColourOld.position(0);

		starBuffer.put(0);
		starBuffer.put(0);
		starBuffer.put(-10);
		starColourOld.put(MenuWorld.COLOUR_BASE[0]);
		starColourOld.put(MenuWorld.COLOUR_BASE[1]);
		starColourOld.put(MenuWorld.COLOUR_BASE[2]);
		starColourOld.put((byte) 120);

		starColourNew.put(MenuWorld.COLOUR_WIN_2);

		for (int i = 1; i < 12; i++) {
			double t = Math.PI * 2 * (double) (i - 1) / 10.0 + Math.PI / 2;
			float r = i % 2 == 0 ? .4f : 1;
			starBuffer.put((float) Math.cos(t) * r);
			starBuffer.put((float) Math.sin(t) * r);
			starBuffer.put(-10);

			starColourOld.put(MenuWorld.COLOUR_BASE);
			starColourNew.put(MenuWorld.COLOUR_WIN_1);
		}
		starColourNew.position(0);
		starColourOld.position(0);
		starBuffer.position(0);
	}

	public void draw() {
		float amt1 = (float) (System.currentTimeMillis() - startTime) / 500f;
		float amt2 = 0;
		int curScore = 0;
		if (amt1 > 1)
			amt2 = (amt1 - 3) / 3;
		if (amt2 > 1)
			amt2 = 1;
		if (amt1 < 1)
			amt1 = Util.slowOut(amt1);
		else
			amt1 = 1;

		if (amt2 >= 1) {
			// Now we will allow the user to end the end
			finished = true;

			// Check for achievements now...
			if (!world.getGameState().achievement_ultimate_driver && world instanceof WorldLevel0 && world.getScore() >= starScores[4]) {
				world.getGameState().achievement_ultimate_driver = world.getGameState().achievement(R.string.achievement_ultimate_driver_20, 20);
			}
			if (!world.getGameState().achievement_store_is_open_10 && world instanceof WorldLevel2) {
				world.getGameState().achievement_store_is_open_10 = world.getGameState().achievement(R.string.achievement_store_is_open_10, 10);
			}
			if (!world.getGameState().achievement_milky_way_master && world instanceof WorldWarp1) {
				world.getGameState().achievement_milky_way_master = world.getGameState().achievement(R.string.achievement_milky_way_master_50, 50);
			}
			if (!world.getGameState().achievement_trigger_happy_40 && world.getSpentPlasma() >= 5) {
				world.getGameState().achievement_trigger_happy_40 = world.getGameState().achievement(R.string.achievement_trigger_happy_40, 40);
			}
			if (!world.getGameState().achievement_shield_celler_50 && world.getSpentCells() >= 5) {
				world.getGameState().achievement_shield_celler_50 = world.getGameState().achievement(R.string.achievement_shield_celler_50, 50);
			}
			if (!world.getGameState().achievement_advanced_andromeda_50 && world instanceof WorldWarp2) {
				world.getGameState().achievement_advanced_andromeda_50 = world.getGameState().achievement(R.string.achievement_advanced_andromeda_50, 50);
			}
		}

		GLES10.glPushMatrix();
		GLES10.glTranslatef(0, -amt1 * screenHeight * 1 / 13, 0);
		textDraw.setColour(MenuWorld.COLOUR_WIN);

		String msg = "LEVEL COMPLETE";

		textDraw
				.draw(screenWidth * 7 / 13,
						Math.min(screenHeight * 12.5f / 13 + textDraw.getCharSize() / 2, screenHeight - world.gbRenderer.getAdHeight() + textDraw.getCharSize()),
						msg, true);

		if (amt2 > 0) {
			curScore = (int) (amt2 * world.getScore());

			// counting through score
			if (System.currentTimeMillis() > nextBeep && curScore > lastScore) {
				world.gbRenderer.getSoundManager().beep();
				nextBeep = System.currentTimeMillis() + 80;
				lastScore = curScore;
			}

			msg = "SCORE: " + curScore;
			textDraw.draw(screenWidth * 8 / 13, screenHeight * 11 / 13 + textDraw.getCharSize() / 2, msg, true);

			msg = "$" + (int) (curScore / world.getWorldDef().getCreditScale());
			textDraw.draw(screenWidth * 8 / 13, screenHeight * 10 / 13 + textDraw.getCharSize() / 2, msg, true);
		}

		GLES10.glVertexPointer(3, GL10.GL_FLOAT, 0, endLineBuffer);
		GLES10.glDisable(GL10.GL_TEXTURE_2D);
		GLES10.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, endLineColour);
		GLES10.glEnableClientState(GL10.GL_COLOR_ARRAY);
		GLES10.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 6);

		// GLES10.glTranslatef(screenWidth, screenHeight + amt1 * screenHeight * 2
		// / 13, 0);
		// GLES10.glRotatef(180, 0, 0, 1);
		// GLES10.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 6);

		GLES10.glPopMatrix();

		if (curScore > 0 && curScore > world.getBest()) {
			msg = "NEW HIGH SCORE!";
			textDraw.draw(screenWidth * 3 / 13, screenHeight * 3 / 13 + textDraw.getCharSize() / 2, msg, false);
		}
		// Tweet new high score (if tweetable)
		if (world.getGameState().tweetReady()) {
			textDraw.setColour(WHITE);
			msg = "\"";
			textDraw.draw(screenWidth * 4 / 13 + textDraw.getCharSize() * 5, screenHeight * 2 / 13, msg, false);
			textDraw.setColour(MenuWorld.COLOUR_WIN);
			msg = "+$20";
			textDraw.draw(screenWidth * 4 / 13 + textDraw.getCharSize() * 8, screenHeight * 2 / 13, msg, false);
			GLES10.glPushMatrix();
			GLES10.glTranslatef(screenWidth * 4 / 13 + textDraw.getCharSize() * 4, screenHeight * 2 / 13 - textDraw.getCharSize(), 0);
			GLES10.glScalef(.5f, .5f, .5f);
			msg = "TWEET YOUR SCORE";
			textDraw.draw(0, 0, msg, false);
			GLES10.glPopMatrix();
		}

		if (world.getGameState().fbReady()) {
			textDraw.setColour(WHITE);
			msg = ")";
			textDraw.draw(screenWidth * 1 / 13, screenHeight * 2 / 13, msg, false);
			textDraw.setColour(MenuWorld.COLOUR_WIN);
			msg = "+$100";
			textDraw.draw(screenWidth * 1 / 13 + textDraw.getCharSize(), screenHeight * 2 / 13, msg, false);
			GLES10.glPushMatrix();
			GLES10.glTranslatef(textDraw.getCharSize() * 2, screenHeight * 2 / 13 - textDraw.getCharSize(), 0);
			GLES10.glScalef(.5f, .5f, .5f);
			msg = "LIKE SPACEINATOR";
			textDraw.draw(0, 0, msg, false);
			GLES10.glPopMatrix();
		}

		GLES10.glVertexPointer(3, GL10.GL_FLOAT, 0, starBuffer);
		GLES10.glEnableClientState(GL10.GL_COLOR_ARRAY);
		GLES10.glDisable(GL10.GL_TEXTURE_2D);
		for (int i = 1; i < 6; i++) {
			float scale = screenHeight / 13;

			if (curScore >= starScores[i - 1]) {
				GLES10.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, starColourNew);
				if (i > world.getOldStars()) {
					scale = 1.618f * screenHeight / 13;
					if (i > oldMaxBigStar) {
						oldMaxBigStar = i;
						world.gbRenderer.getSoundManager().beepMenu(1);
					}
				}
			} else {
				GLES10.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, starColourOld);
			}

			GLES10.glPushMatrix();
			GLES10.glTranslatef(screenWidth * (1.5f * i - .5f) / 13, screenHeight * 6 / 13, 0);
			GLES10.glScalef(scale, scale, 1);
			GLES10.glRotatef((i % 2 == 0 ? 1 : -1) * angle + i * 36, 0, 0, 1);
			angle += .01f;
			GLES10.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 12);
			GLES10.glPopMatrix();
		}

		// Draw leaderboard:
		GLES10.glPushMatrix();
		GLES10.glTranslatef(screenWidth * .65f, screenHeight * .84f, 0);
		GLES10.glScalef(.7f, .7f, .7f);
		textDraw.draw(0, 0, world.getLeaderBoardTitle(), false);
		GLES10.glPopMatrix();

		if (world.getScores() != null) {
			int i = 1;
			for (String s : world.getScores()) {
				GLES10.glPushMatrix();
				GLES10.glTranslatef(screenWidth * .65f, screenHeight * .82f - textDraw.getCharSize() * i++ * .7f, 0);
				GLES10.glScalef(.7f, .7f, .7f);
				textDraw.draw(0, 0, s, false);
				GLES10.glPopMatrix();
			}
		}
	}

	public boolean isFinished() {
		return finished;
	}

	public boolean click(float x, float y) {
		if (x < screenWidth * .4f && y > .7f * screenHeight && world.getGameState().fbReady()) {
			world.getGameState().faceBookLike();
			// Facebook
			Intent browserIntent = new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("http://www.facebook.com/plugins/like.php?href=https%3A%2F%2Ffacebook.com%2F1398743340377452&width&layout=standard&action=like&show_faces=true&share=true&height=80"));
			world.gbRenderer.getContext().startActivity(browserIntent);
			return true;
		} else if (x < screenWidth * .8f && x > screenWidth * .4f && y > .7f * screenHeight && world.getGameState().tweetReady()) {
			world.getGameState().tweet();
			// Twitter
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("https://twitter.com/intent/tweet?screen_name=SpaceinatorUAD&text=%23Spaceinator%20I%20just%20got%20a%20"
							+ (world.getScore() > world.getBest() ? "high%20" : "") + "score%20of%20" + world.getScore() + "%20on%20" + world.getWorldDef().getLabel()
							+ "!"));
			world.gbRenderer.getContext().startActivity(browserIntent);
			return true;
		} else if (x > screenWidth * .7f) {
			GBActivity context = world.gbRenderer.getContext();
			if (context.isSignedIn()) {
				context.startActivityForResult(Games.Leaderboards.getLeaderboardIntent(context.getApiClient(), context.getString(world.getLeaderBoardID())), 1);
				return true;
			}
		}

		return false;
	}
}
