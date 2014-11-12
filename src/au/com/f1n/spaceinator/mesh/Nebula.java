package au.com.f1n.spaceinator.mesh;

import java.nio.FloatBuffer;

import android.content.Context;
import android.opengl.GLES10;
import au.com.f1n.spaceinator.GBRenderer;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.PCamera;
import au.com.f1n.spaceinator.game.World;
import au.com.f1n.spaceinator.physics.menu.MenuWorld;

/**
 * A Nebula
 */
public class Nebula extends Mesh {
	int[] textures = new int[1];
	private FloatBuffer squareArray;
	private FloatBuffer squareTextureData;
	private float rotX = 0;
	private World world;

	public Nebula(Context context, World world) {
		this.world = world;
		reloadTexture(context);
		squareArray = Util.makeFloatBuffer(3 * 4);
		squareTextureData = Util.makeFloatBuffer(2 * 4);

		float offset;

		float z = -GBRenderer.Z_FAR + PCamera.BASE_Z * 8;
		float zLow = z;
		if (world instanceof MenuWorld) {
			rotX = 55;
			offset = 60000;
			zLow *= 0.4f;
		} else {
			offset = 70000;
		}

		int wideScale = 2;
		if (world.getNebulaID() == R.drawable.nebulawide)
			wideScale = 3;

		squareArray.put(-offset * wideScale);
		squareArray.put(-offset);
		squareArray.put(zLow);
		squareTextureData.put(0);
		squareTextureData.put(0);

		squareArray.put(offset * wideScale);
		squareArray.put(-offset);
		squareArray.put(zLow);
		squareTextureData.put(1);
		squareTextureData.put(0);

		squareArray.put(offset * wideScale);
		squareArray.put(offset);
		squareArray.put(z);
		squareTextureData.put(1);
		squareTextureData.put(1);

		squareArray.put(-offset * wideScale);
		squareArray.put(offset);
		squareArray.put(z);
		squareTextureData.put(0);
		squareTextureData.put(1);

		squareArray.position(0);
		squareTextureData.position(0);
	}

	public void draw(float scaleFactor) {
		GLES10.glDisableClientState(GLES10.GL_COLOR_ARRAY);
		GLES10.glDisable(GLES10.GL_BLEND);

		GLES10.glEnable(GLES10.GL_TEXTURE_2D);
		GLES10.glEnableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
		GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, textures[0]);
		GLES10.glTexCoordPointer(2, GLES10.GL_FLOAT, 0, squareTextureData);
		GLES10.glColor4f(1, 1, 1, 1);

		GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, squareArray);
		GLES10.glPushMatrix();
		GLES10.glRotatef(rotX, 1, 0, 0);
		GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, 4);
		GLES10.glPopMatrix();
	}

	public void release() {
		GLES10.glDeleteTextures(1, textures, 0);
	}

	@Override
	public void reloadTexture(Context context) {
		Util.createTexture(context, world.getNebulaID(), textures);
	}
}