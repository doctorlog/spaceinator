package au.com.f1n.spaceinator.mesh;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import android.content.Context;
import android.opengl.GLES10;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.Boundry;
import au.com.f1n.spaceinator.game.World;

public class BoundryDrawer {
	// This can change when we have isChanging() == true
	private FloatBuffer mBoundryVertexData[];
	// The texture is the only thing that will change
	private FloatBuffer mBoundryTextureData;
	private float[][] textureData;
	private ByteBuffer mBoundryColorData[];
	private int[] textures = new int[1];
	private Boundry boundry;

	public BoundryDrawer(Context context, World world) {
		reloadTexture(context);
		boundry = world.getBoundry();
		mBoundryVertexData = boundry.makeVertexData();
		mBoundryColorData = boundry.getColourBuffer();
		textureData = boundry.getTextureData();
		int max = 0;
		for (int i = 0; i < textureData.length; i++)
			if (textureData[i].length > max)
				max = textureData[i].length;
		mBoundryTextureData = Util.makeFloatBuffer(max);
	}

	public void draw() {
		GLES10.glEnable(GLES10.GL_BLEND);

		for (int j = 0; j < mBoundryVertexData.length; j++) {
			float[] tex = textureData[j];
			int nTex = tex.length;

			for (int i = 1; i < nTex; i += 2)
				tex[i] += .02f;

			mBoundryTextureData.put(tex);
			mBoundryTextureData.position(0);

			GLES10.glEnable(GLES10.GL_TEXTURE_2D);
			GLES10.glEnableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
			GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, textures[0]);
			GLES10.glTexCoordPointer(2, GLES10.GL_FLOAT, 0, mBoundryTextureData);

			GLES10.glDisableClientState(GLES10.GL_NORMAL_ARRAY);
			GLES10.glEnableClientState(GLES10.GL_COLOR_ARRAY);

			GLES10.glColorPointer(4, GLES10.GL_UNSIGNED_BYTE, 0, mBoundryColorData[j]);
			GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, mBoundryVertexData[j]);
			GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 0, mBoundryVertexData[j].capacity() / 3);
		}
	}

	public void release() {
		GLES10.glDeleteTextures(1, textures, 0);
	}

	public void reloadTexture(Context context) {
		Util.createTexture(context, R.drawable.helio, textures);
	}
}