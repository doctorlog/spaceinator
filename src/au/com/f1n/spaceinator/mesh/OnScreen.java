package au.com.f1n.spaceinator.mesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.opengl.GLES10;
import au.com.f1n.spaceinator.GBGLSurfaceView;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.PSpaceShip;
import au.com.f1n.spaceinator.game.World;
import au.com.f1n.spaceinator.game.logic.GameState;
import au.com.f1n.spaceinator.physics.menu.MenuWorld;

/**
 * The orthogonal on screen components
 */
public class OnScreen extends OnScreenAbstract {
	private ControlDrawer controlDrawer;

	private float[] shieldVertices;
	private byte[] shieldColour;
	private FloatBuffer mShieldVertexData;
	private ByteBuffer mShieldColorData;

	private FloatBuffer mPlasmaVertexData;
	private FloatBuffer mShieldCellVertexData;

	private float[] zoomVertices;
	private int[] zoomColour;
	private FloatBuffer zoomVerticesBuffer;
	private ByteBuffer zoomColourBuffer;

	private PSpaceShip pSpaceShip;
	private FloatBuffer mIconVertexBuffer;
	private float[] mIconColours;

	private FloatBuffer fullScreen;
	private float[] colour = { 1, 1, 1, 1 };
	private static final int N_POWER = 33;

	private static final String PAUSED = "PAUSED";
	private FloatBuffer powerVertices;
	private ByteBuffer powerColourBuffer;
	private byte[] powerColours;

	private int curPowerColour = 0;
	private byte[] upColour = { (byte) 255, (byte) 102, (byte) 0 };
	private byte[] downColour = { (byte) 145, (byte) 124, (byte) 111 };
	private byte[] topColour = { (byte) 204, (byte) 255, (byte) 0 };

	private EndSequence endSequence;
	// private int[] textureEnd;

	private float zoomAlpha = 1;

	private float topCount;

	private int lastScore = Integer.MIN_VALUE;

	private String scoreStr;

	public OnScreen(Context context, int screenWidth, int screenHeight, World world, GBGLSurfaceView view) {
		super(context, screenWidth, screenHeight, world, view);

		pSpaceShip = world.getCentreSpaceShip();

		topCount = screenHeight - curPad - view.getAdHeight();

		// Little logo
		float[] vertices = { 0, 0, -10, // top left
				0.07865f, -0.72816f, -10,// 1
				0.49717f, -1.02468f, -10, // 2
				0.91568f, -0.72816f, -10,// 3
				0.99433f, 0, -10 // 4
		};

		for (int i = 0; i < vertices.length / 3; i++) {
			vertices[i * 3 + 0] = vertices[i * 3 + 0] * curIndicatorHeight + curPad;
			vertices[i * 3 + 1] = screenHeight - curPad + vertices[i * 3 + 1] * curIndicatorHeight;
		}

		mIconColours = new float[] { .25098f, .25882f, 0.62745f, 0.70588f };
		mIconVertexBuffer = Util.makeFloatBuffer(vertices);
		// Shield amount
		shieldVertices = new float[5 * 3];
		shieldColour = new byte[5 * 4];

		ByteBuffer bb = ByteBuffer.allocateDirect(shieldVertices.length * 4);
		bb.order(ByteOrder.nativeOrder());
		mShieldVertexData = bb.asFloatBuffer();
		mShieldVertexData.position(0);

		shieldVertices[0] = vertices[9] + curPad;
		shieldVertices[1] = vertices[10];
		shieldVertices[2] = -10;

		shieldVertices[3] = vertices[6] + curPad;
		shieldVertices[4] = vertices[7];
		shieldVertices[5] = -10;

		shieldVertices[7] = vertices[7];
		shieldVertices[8] = -10;

		shieldVertices[10] = vertices[13];
		shieldVertices[11] = -10;

		shieldVertices[12] = vertices[12] + curPad;
		shieldVertices[13] = vertices[13];
		shieldVertices[14] = -10;

		mShieldColorData = ByteBuffer.allocateDirect(5 * 4);

		fullScreen = Util.makeFloatBuffer(4 * 3);
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

		zoomVertices = new float[8 * 3];
		zoomColour = new int[8 * 4];

		zoomColourBuffer = ByteBuffer.allocateDirect(zoomColour.length);
		zoomColourBuffer.position(0);
		zoomVerticesBuffer = Util.makeFloatBuffer(zoomVertices.length);

		for (int i = 0; i < 8; i++) {
			zoomColour[i * 4 + 0] = 55;
			zoomColour[i * 4 + 1] = 171;
			zoomColour[i * 4 + 2] = 200;
			zoomColour[i * 4 + 3] = 5;
		}

		Arrays.fill(zoomVertices, -10);

		// middle
		zoomVertices[0] = screenWidth / 2;
		zoomVertices[1] = curIndicatorHeight * 3 - curIndicatorHeight * 1.5f - curPad;
		zoomColour[0] = 170;
		zoomColour[1] = 238;
		zoomColour[2] = 255;
		zoomColour[3] = 255;
		// top1
		zoomVertices[3] = screenWidth / 2;
		zoomVertices[4] = curIndicatorHeight * 3 - curIndicatorHeight - curPad;
		zoomColour[7] = 255;

		zoomVertices[6] = screenWidth / 4;
		zoomVertices[7] = curIndicatorHeight * 3 - curIndicatorHeight - curPad * 2;

		zoomVertices[9] = screenWidth / 4;
		zoomVertices[10] = curIndicatorHeight * 3 - curIndicatorHeight * 2;

		// bottom
		zoomVertices[12] = screenWidth / 2;
		zoomVertices[13] = curIndicatorHeight * 3 - curIndicatorHeight * 2 - curPad;
		zoomColour[19] = 255;

		zoomVertices[15] = screenWidth * 3 / 4;
		zoomVertices[16] = curIndicatorHeight * 3 - curIndicatorHeight * 2;

		zoomVertices[18] = screenWidth * 3 / 4;
		zoomVertices[19] = curIndicatorHeight * 3 - curIndicatorHeight - curPad * 2;

		// top2
		zoomVertices[21] = screenWidth / 2;
		zoomVertices[22] = curIndicatorHeight * 3 - curIndicatorHeight - curPad;
		zoomColour[31] = 255;

		mPlasmaVertexData = Util.makeFloatBuffer(7 * 3);
		mPlasmaVertexData.put(curIndicatorHeight / 3);
		mPlasmaVertexData.put(curIndicatorHeight / 2);
		mPlasmaVertexData.put(-10);

		mPlasmaVertexData.put(curIndicatorHeight / 3);
		mPlasmaVertexData.put(curIndicatorHeight);
		mPlasmaVertexData.put(-10);

		mPlasmaVertexData.put(0);
		mPlasmaVertexData.put(curIndicatorHeight * .7f);
		mPlasmaVertexData.put(-10);

		mPlasmaVertexData.put(0);
		mPlasmaVertexData.put(0);
		mPlasmaVertexData.put(-10);

		mPlasmaVertexData.put(curIndicatorHeight * 2 / 3);
		mPlasmaVertexData.put(0);
		mPlasmaVertexData.put(-10);

		mPlasmaVertexData.put(curIndicatorHeight * 2 / 3);
		mPlasmaVertexData.put(curIndicatorHeight * .7f);
		mPlasmaVertexData.put(-10);

		mPlasmaVertexData.put(curIndicatorHeight / 3);
		mPlasmaVertexData.put(curIndicatorHeight);
		mPlasmaVertexData.put(-10);
		mPlasmaVertexData.position(0);

		mShieldCellVertexData = Util.makeFloatBuffer(3 * 3);
		mShieldCellVertexData.put(0);
		mShieldCellVertexData.put(0);
		mShieldCellVertexData.put(-10);

		mShieldCellVertexData.put(curIndicatorHeight * .8f);
		mShieldCellVertexData.put(0);
		mShieldCellVertexData.put(-10);

		mShieldCellVertexData.put(curIndicatorHeight * .4f);
		mShieldCellVertexData.put(curIndicatorHeight);
		mShieldCellVertexData.put(-10);

		mShieldCellVertexData.position(0);

		// setup the power colour transparencies
		powerColours = new byte[N_POWER * 4];

		powerColours[3] = (byte) 128;
		for (int i = 2; i < N_POWER; i++)
			powerColours[i * 4 + 3] = (byte) (255 - i * 7);

		powerColourBuffer = ByteBuffer.allocateDirect(N_POWER * 4);
		powerColourBuffer.position(0);

		setPowerColour(0);

		// Make a 180 degree fan
		powerVertices = Util.makeFloatBuffer(N_POWER * 3);
		powerVertices.put(0);
		powerVertices.put(0);
		powerVertices.put(-10);
		powerVertices.put(curIndicatorHeight * 3);
		powerVertices.put(0);
		powerVertices.put(-10);
		for (int i = 2; i < N_POWER; i++) {
			double angle = Math.PI * (i - 1.5) / (N_POWER - 2);
			powerVertices.put((float) Math.cos(angle) * curIndicatorHeight * 3);
			powerVertices.put((float) Math.sin(angle) * curIndicatorHeight * 3);
			powerVertices.put(-10);
		}
		powerVertices.position(0);

		if (pSpaceShip != null)
			controlDrawer = new ControlDrawer(screenWidth, screenHeight, context, world, this);
	}

	private void setPowerColour(int c) {
		curPowerColour = c;
		byte[] col;

		switch (c) {
		case 0:
			col = downColour;
			break;
		case 1:
			col = upColour;
			break;
		default:
			col = topColour;
			break;
		}

		for (int i = 0; i < N_POWER; i++) {
			powerColours[i * 4] = col[0];
			powerColours[i * 4 + 1] = col[1];
			powerColours[i * 4 + 2] = col[2];
		}
		powerColourBuffer.put(powerColours);
		powerColourBuffer.position(0);
	}

	public void draw(float scaleFactor, boolean zooming) {
		// Draw on screen things
		GLES10.glMatrixMode(GL10.GL_PROJECTION);
		GLES10.glLoadIdentity();
		GLES10.glOrthof(0, screenWidth, 0, screenHeight, 0.001f, 100);

		GLES10.glMatrixMode(GL10.GL_MODELVIEW);
		GLES10.glLoadIdentity();

		GLES10.glDisable(GL10.GL_LIGHTING);
		GLES10.glDisable(GL10.GL_TEXTURE_2D);
		GLES10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		GLES10.glEnableClientState(GL10.GL_COLOR_ARRAY);
		GLES10.glEnable(GL10.GL_BLEND);
		GLES10.glDisable(GL10.GL_DEPTH_TEST);

		textDraw.setColour(colour);
		if (world.zooming == 0) {
			// Draw controls
			if (controlDrawer != null)
				controlDrawer.draw(view);

			// Draw plasma icons
			int nPlasma = world.getGameState().getShipUpgrades()[GameState.PLASMA_WAVE];
			for (int i = 0; i < nPlasma; i++) {
				GLES10.glPushMatrix();
				GLES10.glTranslatef(screenWidth - curIndicatorHeight * (i + 1),
						screenHeight - curIndicatorHeight * (world.getBest() == -1 ? 2 : 2.5f) - curPad * 3, 0);
				GLES10.glDisableClientState(GL10.GL_COLOR_ARRAY);
				GLES10.glVertexPointer(3, GL11.GL_FLOAT, 0, mPlasmaVertexData);
				GLES10.glColor4f(.047059f, .964706f, .258824f, .5f);
				GLES10.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, mPlasmaVertexData.capacity() / 3);
				GLES10.glPopMatrix();
			}

			// Draw shield cell icons
			int nShield = world.getGameState().getShipUpgrades()[GameState.SHIELD_CELL];
			for (int i = 0; i < nShield; i++) {
				GLES10.glPushMatrix();
				GLES10.glTranslatef(curPad + curIndicatorHeight * .4f + curIndicatorHeight * i * .7f, screenHeight - curIndicatorHeight * 1.5f - curPad * 2, 0);
				if (i % 2 == 1)
					GLES10.glRotatef(180, 0, 0, 1);
				GLES10.glTranslatef(-curIndicatorHeight * .4f, -curIndicatorHeight / 2, 0);
				GLES10.glDisableClientState(GL10.GL_COLOR_ARRAY);
				GLES10.glVertexPointer(3, GL11.GL_FLOAT, 0, mShieldCellVertexData);
				GLES10.glColor4f(0.25098f, 0.25882f, 0.62745f, 0.70588f);
				GLES10.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, mShieldCellVertexData.capacity() / 3);
				GLES10.glPopMatrix();
			}

			// Draw icons
			GLES10.glVertexPointer(3, GL11.GL_FLOAT, 0, mIconVertexBuffer);
			GLES10.glColor4f(mIconColours[0], mIconColours[1], mIconColours[2], mIconColours[3]);
			GLES10.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, mIconVertexBuffer.capacity() / 3);

			// Draw shield amount
			GLES10.glEnableClientState(GL10.GL_COLOR_ARRAY);
			shieldVertices[6] = shieldVertices[9] = curIndicatorHeight + curPad * 2 + Math.max(0, world.getCentreSpaceShip().getCurShield()) * screenWidth / 255
					/ 6;
			mShieldVertexData.put(shieldVertices);
			mShieldVertexData.position(0);

			for (int i = 0; i < 5; i++) {
				shieldColour[i * 4] = pSpaceShip.getCurShield() < 25 ? (byte) 255 : (byte) 64;
				shieldColour[i * 4 + 1] = (byte) 66;
				shieldColour[i * 4 + 2] = (byte) 160;
				shieldColour[i * 4 + 3] = (byte) 180;
			}
			mShieldColorData.put(shieldColour);
			mShieldColorData.position(0);

			GLES10.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, mShieldColorData);
			GLES10.glVertexPointer(3, GL10.GL_FLOAT, 0, mShieldVertexData);
			GLES10.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 5);

			// Draw the score
			colour[3] = 1;

			if (world.score != lastScore)
				scoreStr = "SCORE:" + world.score;

			textDraw.draw(screenWidth - curPad, screenHeight - curPad, scoreStr, true);
			if (world.getBest() != -1) {
				GLES10.glPushMatrix();
				GLES10.glScalef(.5f, .5f, 1);
				textDraw.draw((screenWidth - curPad) * 2, (screenHeight - curPad * 2 - curIndicatorHeight) * 2, "BEST:" + world.getBest(), true);
				GLES10.glPopMatrix();
			}

			// Draw the power level
			GLES10.glPushMatrix();
			GLES10.glTranslatef(screenWidth / 2, 0, 0);

			int newCol = 0;

			if (pSpaceShip.power == -1) {
				// Special case
				GLES10.glRotatef(180 * (world.lastTime - pSpaceShip.lastPower) / PSpaceShip.POWER_TIME, 0, 0, 1);
				// top col
				newCol = 2;
			} else {
				GLES10.glRotatef(180 - pSpaceShip.power, 0, 0, 1);
				newCol = pSpaceShip.powerDown ? 0 : 1;
			}

			if (newCol != curPowerColour)
				setPowerColour(newCol);

			GLES10.glDisable(GL10.GL_TEXTURE_2D);
			GLES10.glEnableClientState(GL10.GL_COLOR_ARRAY);
			GLES10.glVertexPointer(3, GL10.GL_FLOAT, 0, powerVertices);
			GLES10.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, powerColourBuffer);
			GLES10.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, N_POWER);
			GLES10.glPopMatrix();

			// Draw the zoom bar
			if (zoomAlpha > 0) {
				zoomVertices[0] = zoomVertices[3] = zoomVertices[12] = zoomVertices[21] = screenWidth / 4 + screenWidth * (2f - scaleFactor) / 3;

				zoomVerticesBuffer.put(zoomVertices);
				zoomVerticesBuffer.position(0);

				for (int i = 0; i < 8; i++) {
					zoomColourBuffer.put((byte) zoomColour[i * 4 + 0]);
					zoomColourBuffer.put((byte) zoomColour[i * 4 + 1]);
					zoomColourBuffer.put((byte) zoomColour[i * 4 + 2]);
					zoomColourBuffer.put((byte) (zoomColour[i * 4 + 3] * zoomAlpha));
				}
				zoomColourBuffer.position(0);

				GLES10.glEnableClientState(GL10.GL_COLOR_ARRAY);
				GLES10.glDisable(GL10.GL_TEXTURE_2D);
				GLES10.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, zoomColourBuffer);
				GLES10.glVertexPointer(3, GL10.GL_FLOAT, 0, zoomVerticesBuffer);
				GLES10.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 8);

				if (!zooming)
					zoomAlpha -= .02f;
			}
		}
		// Draw the current text message
		if (world.getCurMessage() != null) {
			colour[3] = world.getCurMessageAlpha();
			textDraw.draw(screenWidth / 2 - textDraw.getCharSize() * world.getCurMessage().length() / 2, screenHeight * 2 / 3 + textDraw.getCharSize() / 2,
					world.getCurMessage(), false);
		}

		if (world.zooming == 0 && world.getCountdown() != null) {
			colour[3] = 1;
			String msg = world.getCountdown();

			textDraw.draw(screenWidth / 2 - textDraw.getCharSize() * msg.length() / 2, topCount, msg, false);
		}

		// Draw the screen blackout
		if (world.getFade() > 0) {
			GLES10.glDisableClientState(GL10.GL_COLOR_ARRAY);
			GLES10.glVertexPointer(3, GL10.GL_FLOAT, 0, fullScreen);
			GLES10.glDisable(GL10.GL_TEXTURE_2D);
			GLES10.glColor4f(0, 0, 0, world.getFade());
			GLES10.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 4);
		}
		
		if (world.isEnded()) {
			// Draw the ending sequence
			if (endSequence == null)
				endSequence = new EndSequence(context, screenWidth, screenHeight, textDraw, world);

			endSequence.draw();
		}

		if (world.isPaused()) {
			GLES10.glDisableClientState(GL10.GL_COLOR_ARRAY);
			GLES10.glDisable(GLES10.GL_TEXTURE_2D);
			GLES10.glVertexPointer(3, GL10.GL_FLOAT, 0, fullScreen);
			GLES10.glColor4f(0, 0, 0, .2f);
			GLES10.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 4);

			colour[3] = 1;
			GLES10.glPushMatrix();

			GLES10.glTranslatef(screenWidth / 2, screenHeight / 2, 0);
			GLES10.glRotatef(30f * (float) Math.sin((double) System.currentTimeMillis() / 500.0), 0, 0, 1);
			textDraw.draw(-textDraw.getCharSize() * PAUSED.length() / 2, textDraw.getCharSize() / 2, PAUSED, false);
			GLES10.glPopMatrix();
		}
	}

	public void maxZoomAlpha() {
		zoomAlpha = 1;
	}

	public void setTextDraw(TextDrawer textDraw) {
		super.setTextDraw(textDraw);
		textDraw.setColour(colour);
	}

	public FloatBuffer getFullScreen() {
		return fullScreen;
	}

	@Override
	public void release() {
		// NA
	}

	@Override
	public boolean isEndSequence() {
		return endSequence != null;
	}

	@Override
	public boolean click(float x, float y) {
		if (isEndSequence()) {
			if (!endSequence.click(x, y)) {
				// User has finished level, load menu now with a tripple menu beep
				world.gbRenderer.getSoundManager().beepMenu(.3f);
				world.gbRenderer.getSoundManager().beepMenu(.6f);
				world.gbRenderer.getSoundManager().beepMenu(.9f);
				world.gbRenderer.loadWorld(MenuWorld.class);
			}

			return true;
		}

		return false;
	}
}