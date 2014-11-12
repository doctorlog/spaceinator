package au.com.f1n.spaceinator.mesh;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES10;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.PDrWho;
import au.com.f1n.spaceinator.game.World;

/**
 * A Nebula
 */
public class DrWho extends Mesh {
	private int[] textures = new int[1];
	private PDrWho pDrWho;

	public DrWho(Context context, World world) {
		pDrWho = world.getDrWho();
		reloadTexture(context);
	}

	public void draw(float scaleFactor) {
		GLES10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		GLES10.glDisableClientState(GL10.GL_COLOR_ARRAY);
		GLES10.glDisable(GL10.GL_BLEND);
		GLES10.glEnable(GLES10.GL_DEPTH_TEST);

		GLES10.glColor4f(1, 1, 1, 1);
		GLES10.glEnable(GL10.GL_TEXTURE_2D);
		GLES10.glEnableClientState(GL10.GL_COLOR_ARRAY);
		GLES10.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		GLES10.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		GLES10.glTexCoordPointer(2, GL10.GL_FLOAT, 0, pDrWho.getTextureCoords());
		GLES10.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, pDrWho.getColours());
		GLES10.glVertexPointer(3, GL10.GL_FLOAT, 0, pDrWho.getVertices());

		GLES10.glDrawElements(GL10.GL_TRIANGLES, pDrWho.getN(), GL10.GL_UNSIGNED_SHORT, pDrWho.getDrawOrder());
	}

	public void release() {
		GLES10.glDeleteTextures(1, textures, 0);
	}

	@Override
	public void reloadTexture(Context context) {
		Util.createTexture(context, pDrWho.getTextureID(), textures);
	}
}