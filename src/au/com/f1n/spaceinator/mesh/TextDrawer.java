package au.com.f1n.spaceinator.mesh;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.opengl.GLES10;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;

/**
 * Draw some text somewhere! Uses R.drawable.text
 * 
 * @author luke
 */

public class TextDrawer {
	private static final float CHAR_SIZE_TEX_X = 44 / 1024f;
	private static final float CHAR_SIZE_TEX_Y = 44 / 128f;
	private static final int MAX_CHAR = 80;
	private FloatBuffer vertexDataBuffer;
	private FloatBuffer textureDataBuffer;
	private int[] textures = new int[1];
	private float charSize;
	private float[] colour;
	private float[] textureData;
	private static final int[] X_OFFSETS = { 16, 5, 19, 21, 18, 3, 17, 1, 6, 22, 4, 2, 20, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 19, 18, 0, 20, 21,
			0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 0, 1, 2, 7 };
	private static final int[] Y_OFFSETS = { 1, 2, 1, 1, 1, 2, 1, 2, 2, 1, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 2 };

	public TextDrawer(Context context) {
		// TextDrawer is special - textures always reloaded on resume
		// reloadTextures(context);
		vertexDataBuffer = Util.makeFloatBuffer(MAX_CHAR * 3 * 6);
		textureDataBuffer = Util.makeFloatBuffer(MAX_CHAR * 6 * 2);
		textureData = new float[MAX_CHAR * 6 * 2];
	}

	public void setHeight(float height) {
		this.charSize = height * OnScreenAbstract.INDICATOR_HEIGHT;

		// Draw order is fixed - only the texture coordinates change
		float x = 0;
		for (int i = 0; i < MAX_CHAR; i++) {
			// 0
			vertexDataBuffer.put(x);
			vertexDataBuffer.put(0);
			vertexDataBuffer.put(0);

			// 1
			vertexDataBuffer.put(x);
			vertexDataBuffer.put(-charSize);
			vertexDataBuffer.put(0);

			// 2
			vertexDataBuffer.put(x + charSize);
			vertexDataBuffer.put(-charSize);
			vertexDataBuffer.put(0);

			// 0
			vertexDataBuffer.put(x);
			vertexDataBuffer.put(0);
			vertexDataBuffer.put(0);

			// 2
			vertexDataBuffer.put(x + charSize);
			vertexDataBuffer.put(-charSize);
			vertexDataBuffer.put(0);

			// 3
			vertexDataBuffer.put(x + charSize);
			vertexDataBuffer.put(0);
			vertexDataBuffer.put(0);

			x += charSize;
		}
		vertexDataBuffer.position(0);
	}

	/**
	 * Draw some text at a location on the screen
	 * 
	 * @param x
	 * @param y
	 * @param text
	 * @param rightAlign
	 */

	public void draw(float x, float y, String text, boolean rightAlign) {
		GLES10.glDisableClientState(GLES10.GL_COLOR_ARRAY);
		GLES10.glEnable(GLES10.GL_TEXTURE_2D);
		GLES10.glEnableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
		GLES10.glDisableClientState(GLES10.GL_NORMAL_ARRAY);
		GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, textures[0]);
		GLES10.glColor4f(colour[0], colour[1], colour[2], colour[3]);

		int n = Math.min(text.length(), MAX_CHAR);

		GLES10.glPushMatrix();
		if (rightAlign) {
			GLES10.glTranslatef(x - charSize * n, y, -10);
		} else {
			GLES10.glTranslatef(x, y, -10);
		}

		for (int i = 0; i < n; i++) {
			char curChar = text.charAt(i);
			int offsetI = i * 6 * 2;
			if (curChar < 33 || curChar > 92) {
				// Blank
				textureData[offsetI++] = 0;
				textureData[offsetI++] = 0;

				textureData[offsetI++] = 0;
				textureData[offsetI++] = 0;

				textureData[offsetI++] = 0;
				textureData[offsetI++] = 0;

				textureData[offsetI++] = 0;
				textureData[offsetI++] = 0;

				textureData[offsetI++] = 0;
				textureData[offsetI++] = 0;

				textureData[offsetI++] = 0;
				textureData[offsetI++] = 0;

			} else {
				float tmpx = X_OFFSETS[curChar - 33] * CHAR_SIZE_TEX_X;
				float tmpy = Y_OFFSETS[curChar - 33] * CHAR_SIZE_TEX_Y;

				// 0
				textureData[offsetI++] = tmpx;
				textureData[offsetI++] = tmpy;
				// 1
				textureData[offsetI++] = tmpx;
				textureData[offsetI++] = tmpy + CHAR_SIZE_TEX_Y;
				// 2
				textureData[offsetI++] = tmpx + CHAR_SIZE_TEX_X;
				textureData[offsetI++] = tmpy + CHAR_SIZE_TEX_Y;
				// 0
				textureData[offsetI++] = tmpx;
				textureData[offsetI++] = tmpy;
				// 2
				textureData[offsetI++] = tmpx + CHAR_SIZE_TEX_X;
				textureData[offsetI++] = tmpy + CHAR_SIZE_TEX_Y;
				// 3
				textureData[offsetI++] = tmpx + CHAR_SIZE_TEX_X;
				textureData[offsetI++] = tmpy;
			}
		}
		textureDataBuffer.put(textureData, 0, n * 2 * 6);
		textureDataBuffer.position(0);

		GLES10.glVertexPointer(3, GL11.GL_FLOAT, 0, vertexDataBuffer);
		GLES10.glTexCoordPointer(2, GLES10.GL_FLOAT, 0, textureDataBuffer);
		GLES10.glDrawArrays(GLES10.GL_TRIANGLES, 0, n * 2 * 3);

		GLES10.glPopMatrix();
	}

	/**
	 * Draw some text at a location on the screen
	 * 
	 * @param x
	 * @param y
	 * @param text
	 * @param rightAlign
	 */

	public void draw3D(float x, float y, float z, String text, boolean rightAlign, float scale, float rotX) {
		GLES10.glDisableClientState(GLES10.GL_COLOR_ARRAY);
		GLES10.glEnable(GLES10.GL_TEXTURE_2D);
		GLES10.glEnableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
		GLES10.glDisableClientState(GLES10.GL_NORMAL_ARRAY);
		GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, textures[0]);
		GLES10.glColor4f(colour[0], colour[1], colour[2], colour[3]);

		int n = Math.min(text.length(), MAX_CHAR);

		GLES10.glPushMatrix();
		if (rightAlign) {
			GLES10.glTranslatef(x - n * scale, y, z);
		} else {
			GLES10.glTranslatef(x, y, z);
		}
		GLES10.glScalef(scale / charSize, scale / charSize, 1);
		GLES10.glRotatef(rotX, 1, 0, 0);

		for (int i = 0; i < n; i++) {
			char curChar = text.charAt(i);
			int offsetI = i * 6 * 2;
			if (curChar < 33 || curChar > 92) {
				// Blank
				textureData[offsetI++] = 0;
				textureData[offsetI++] = 0;

				textureData[offsetI++] = 0;
				textureData[offsetI++] = 0;

				textureData[offsetI++] = 0;
				textureData[offsetI++] = 0;

				textureData[offsetI++] = 0;
				textureData[offsetI++] = 0;

				textureData[offsetI++] = 0;
				textureData[offsetI++] = 0;

				textureData[offsetI++] = 0;
				textureData[offsetI++] = 0;

			} else {
				float tmpx = X_OFFSETS[curChar - 33] * CHAR_SIZE_TEX_X;
				float tmpy = Y_OFFSETS[curChar - 33] * CHAR_SIZE_TEX_Y;

				// 0
				textureData[offsetI++] = tmpx;
				textureData[offsetI++] = tmpy;
				// 1
				textureData[offsetI++] = tmpx;
				textureData[offsetI++] = tmpy + CHAR_SIZE_TEX_Y;
				// 2
				textureData[offsetI++] = tmpx + CHAR_SIZE_TEX_X;
				textureData[offsetI++] = tmpy + CHAR_SIZE_TEX_Y;
				// 0
				textureData[offsetI++] = tmpx;
				textureData[offsetI++] = tmpy;
				// 2
				textureData[offsetI++] = tmpx + CHAR_SIZE_TEX_X;
				textureData[offsetI++] = tmpy + CHAR_SIZE_TEX_Y;
				// 3
				textureData[offsetI++] = tmpx + CHAR_SIZE_TEX_X;
				textureData[offsetI++] = tmpy;
			}
		}
		textureDataBuffer.put(textureData, 0, n * 2 * 6);
		textureDataBuffer.position(0);

		GLES10.glVertexPointer(3, GL11.GL_FLOAT, 0, vertexDataBuffer);
		GLES10.glTexCoordPointer(2, GLES10.GL_FLOAT, 0, textureDataBuffer);
		GLES10.glDrawArrays(GLES10.GL_TRIANGLES, 0, n * 2 * 3);

		GLES10.glPopMatrix();
	}

	public void setColour(float[] colour) {
		this.colour = colour;
	}

	public float getCharSize() {
		return charSize;
	}

	public void reloadTextures(Context context) {
		Util.createTexture(context, R.drawable.text, textures);
	}
}