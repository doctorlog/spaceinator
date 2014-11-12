package au.com.f1n.spaceinator.mesh;

import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.opengl.GLES10;
import au.com.f1n.spaceinator.GBGLSurfaceView;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.World;
import au.com.f1n.spaceinator.game.WorldLevel0;
import au.com.f1n.spaceinator.game.WorldLevel1;
import au.com.f1n.spaceinator.game.WorldLevel2;
import au.com.f1n.spaceinator.game.WorldLevel3;

public class ControlDrawer {
	private static final int FADE_MAX = 12;
	private FloatBuffer mShape;
	private FloatBuffer mShape2;
	private float[] circleBase;
	private float[] circle;
	private int controlN;
	private int screenHeight;
	private float screenWidth;

	private int fadeFire = 0;
	private int fadeAccel = 0;

	private FloatBuffer finger;
	private World world;
	private FloatBuffer fullScreen;
	private float controlScale;
	private FloatBuffer circleFan;

	public ControlDrawer(int screenWidth, int screenHeight, Context context, World world, OnScreen onScreen) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.world = world;
		controlN = 50;
		float controlR = GBGLSurfaceView.PRESS_BUFFER * 8;
		controlScale = screenHeight / controlR / 4;
		mShape = Util.makeFloatBuffer(controlN * 3);
		mShape2 = Util.makeFloatBuffer(controlN * 3);

		circle = new float[controlN * 3];
		circleBase = new float[controlN * 2];

		Arrays.fill(circle, -10);

		int p = 0;
		for (int i = 0; i < controlN; i++) {
			double angle = Math.PI * 2 * i / (controlN - 1);
			circleBase[p] = (float) Math.cos(angle);
			mShape.put(circleBase[p++] * controlR);
			circleBase[p] = (float) Math.sin(angle);
			mShape.put(circleBase[p++] * controlR);
			mShape.put(-10);
		}
		mShape.position(0);

		if (world instanceof WorldLevel0 || world instanceof WorldLevel1 || world instanceof WorldLevel2 || world instanceof WorldLevel3) {
			MeshReader meshReader = new MeshReader(context.getResources().openRawResource(R.raw.finger), false);
			finger = meshReader.getVertices();
			this.fullScreen = onScreen.getFullScreen();

			if (world instanceof WorldLevel3) {
				circleFan = Util.makeFloatBuffer((controlN + 1) * 3);
				circleFan.put(0);
				circleFan.put(0);
				circleFan.put(-10);
				for (int i = 0; i < controlN; i++) {
					double angle = Math.PI * 2 * i / (controlN - 1);
					circleFan.put((float) Math.cos(angle) * screenHeight / 4);
					circleFan.put((float) Math.sin(angle) * screenHeight / 4);
					circleFan.put(-10);
				}
				circleFan.position(0);
			}
		}
	}

	public void draw(GBGLSurfaceView view) {
		GLES10.glDisableClientState(GLES10.GL_COLOR_ARRAY);

		if (world instanceof WorldLevel0 && ((WorldLevel0) world).isFinger()) {
			GLES10.glPushMatrix();
			GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, fullScreen);
			GLES10.glColor4f(0, 0, 1, (float) Math.sin(System.currentTimeMillis() / 200.0) * .2f + .2f);
			GLES10.glTranslatef(-screenWidth / 2, 0, 0);
			GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, 4);
			GLES10.glPopMatrix();

			drawFinger(.25f, screenHeight * 0.5f, .1f, 0);
		} else if (world instanceof WorldLevel1 && ((WorldLevel1) world).isFinger()) {
			GLES10.glPushMatrix();
			GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, fullScreen);
			GLES10.glColor4f(1, .8f, 0, (float) Math.sin(System.currentTimeMillis() / 200.0) * .2f + .2f);
			GLES10.glTranslatef(screenWidth / 2, 0, 0);
			GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, 4);
			GLES10.glPopMatrix();

			drawFinger(.75f, screenHeight * 0.5f, .1f, 0);
		} else if (world instanceof WorldLevel2 && ((WorldLevel2) world).isFinger()) {
			GLES10.glPushMatrix();
			GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, fullScreen);
			GLES10.glColor4f(.5f, .9f, 1, (float) Math.sin(System.currentTimeMillis() / 200.0) * .2f + .2f);
			GLES10.glTranslatef(screenWidth * .25f, -screenHeight * .9f, 0);
			GLES10.glScalef(.5f, 1, 1);
			GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, 4);
			GLES10.glPopMatrix();

			drawFinger(.5f, screenHeight * 0.05f, .1f, 145);
		} else if (world instanceof WorldLevel3 && ((WorldLevel3) world).getFinger() == 1) {
			GLES10.glPushMatrix();
			GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, circleFan);
			GLES10.glColor4f(.047f, 0.9647f, .2588f, (float) Math.sin(System.currentTimeMillis() / 200.0) * .2f + .2f);
			GLES10.glTranslatef(screenWidth, screenHeight, 0);
			GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, controlN + 1);
			GLES10.glPopMatrix();

			drawFinger(.9f, screenHeight * 0.95f, .03f, 0);
		} else if (world instanceof WorldLevel3 && ((WorldLevel3) world).getFinger() == 2) {
			GLES10.glPushMatrix();
			GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, circleFan);
			GLES10.glColor4f(0, 0, 1, (float) Math.sin(System.currentTimeMillis() / 200.0) * .2f + .2f);
			GLES10.glTranslatef(0, screenHeight, 0);
			GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, controlN + 1);
			GLES10.glPopMatrix();

			drawFinger(.1f, screenHeight * 0.95f, .03f, 0);
		}

		if (view.isFireDown()) {
			GLES10.glPushMatrix();
			GLES10.glColor4f(1, 0.49f, 0, (float) fadeFire / FADE_MAX);
			if (world.getCentreSpaceShip().isShooting()) {
				// Draw the context shape
				makeShape2(view.getFireDist());
				GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, mShape2);
			} else {
				// Just draw a circle
				GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, mShape);
			}
			GLES10.glTranslatef(view.getFireXInit(), screenHeight - view.getFireYInit(), 0);
			GLES10.glRotatef(world.getCentreSpaceShip().getShootingAngle() * Util.RAD_TO_DEG, 0, 0, 1);

			if (fadeFire < FADE_MAX) {
				fadeFire++;
				float scale = 1f / Util.slowOut((float) fadeFire / FADE_MAX);
				GLES10.glScalef(scale, scale, scale);
			}

			GLES10.glDrawArrays(GLES10.GL_LINE_STRIP, 0, controlN);
			GLES10.glPopMatrix();
		} else {
			fadeFire = 0;
		}

		if (view.isAccelDown()) {
			GLES10.glPushMatrix();
			GLES10.glColor4f(0, 0.4f, 0.7f, (float) fadeAccel / FADE_MAX);
			if (world.getCentreSpaceShip().isAccelOn()) {
				// Draw the context shape
				makeShape2(view.getAccelDist());
				GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, mShape2);
			} else {
				// Just draw a circle
				GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, mShape);
			}
			GLES10.glTranslatef(view.getAccelXInit(), screenHeight - view.getAccelYInit(), 0);
			GLES10.glRotatef(world.getCentreSpaceShip().getAccelAngle() * Util.RAD_TO_DEG, 0, 0, 1);

			if (fadeAccel < FADE_MAX) {
				fadeAccel++;
				float scale = 1f / Util.slowOut((float) fadeAccel / FADE_MAX);
				GLES10.glScalef(scale, scale, scale);
			}

			GLES10.glDrawArrays(GLES10.GL_LINE_STRIP, 0, controlN);
			GLES10.glPopMatrix();
		} else {
			fadeAccel = 0;
		}

		float canPlasma = world.canPlasma();
		if (canPlasma > 0) {
			GLES10.glPushMatrix();
			GLES10.glColor4f(.047f, 0.9647f, .2588f, canPlasma);

			// Only every draw a circle
			GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, mShape);
			GLES10.glTranslatef(screenWidth, screenHeight, 0);
			GLES10.glScalef(controlScale, controlScale, 1);

			GLES10.glDrawArrays(GLES10.GL_LINE_STRIP, 0, controlN);
			GLES10.glPopMatrix();
		}

		float canShield = world.canShield();
		if (canShield > 0) {
			GLES10.glPushMatrix();
			GLES10.glColor4f(0.25098f, 0.25882f, 0.62745f, canShield);

			// Only every draw a circle
			GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, mShape);
			GLES10.glTranslatef(0, screenHeight, 0);
			GLES10.glScalef(controlScale, controlScale, 1);

			GLES10.glDrawArrays(GLES10.GL_LINE_STRIP, 0, controlN);
			GLES10.glPopMatrix();
		}
	}

	private void drawFinger(float x, float y, float slideScale, float rotZ) {
		GLES10.glPushMatrix();
		float offset = (float) Math.sin((double) System.currentTimeMillis() / 500);

		GLES10.glTranslatef(screenWidth * (x + slideScale * offset), y, 0);
		GLES10.glScalef(screenHeight / 4, screenHeight / 4, 1);
		GLES10.glRotatef(rotZ, 0, 0, 1);
		GLES10.glVertexPointer(3, GL11.GL_FLOAT, 0, finger);
		GLES10.glColor4f(.8f, .7f, .71f, 1);
		GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, 29);
		GLES10.glPopMatrix();
	}

	private void makeShape2(float dist) {
		float controlR = GBGLSurfaceView.PRESS_BUFFER * (2 + 6 / ((dist - GBGLSurfaceView.PRESS_BUFFER) * .1f + 1));

		circle[0] = Math.max(dist, controlR);
		circle[1] = 0;
		int p = 3;
		int b = 2;
		for (int i = 1; i < controlN - 1; i++) {
			circle[p++] = circleBase[b++] * controlR;
			circle[p++] = circleBase[b++] * controlR;
			p++;
		}
		circle[p++] = circle[0];
		circle[p] = 0;
		mShape2.put(circle);
		mShape2.position(0);
	}
}
