package au.com.f1n.spaceinator.mesh.intro;

import android.opengl.GLES10;
import au.com.f1n.spaceinator.GBActivity;
import au.com.f1n.spaceinator.GBRenderer;
import au.com.f1n.spaceinator.mesh.TextDrawer;
import au.com.f1n.spaceinator.physics.menu.PMenuCamera;

public class IntroSequence {
	protected boolean instantLoad = true;

	public synchronized boolean draw(GBRenderer gbRenderer, TextDrawer textDraw) {
		// Just draw stars and load immediately
		float size;

		GLES10.glClear(GLES10.GL_COLOR_BUFFER_BIT | GLES10.GL_DEPTH_BUFFER_BIT);
		GLES10.glClearColor(0, 0, 0, 1);

		GLES10.glEnable(GLES10.GL_NORMALIZE);
		float aspectRatio = (float) gbRenderer.width / gbRenderer.height;
		GLES10.glMatrixMode(GLES10.GL_PROJECTION);
		size = GBRenderer.Z_NEAR * (float) Math.tan((30.0) * Math.PI / 180.0 / 2.0);
		GLES10.glLoadIdentity();
		GLES10.glFrustumf(-size, size, -size / aspectRatio, size / aspectRatio, GBRenderer.Z_NEAR, GBRenderer.Z_FAR);
		GLES10.glMatrixMode(GLES10.GL_MODELVIEW);
		GLES10.glLoadIdentity();

		GLES10.glRotatef(PMenuCamera.BASE_ROT_Z, 0, 0, 1);
		GLES10.glTranslatef(0, 0, PMenuCamera.OUT_Z);
		GLES10.glRotatef(-40, 1, 0, 0);

		gbRenderer.starField.draw(1, -50);

		if (instantLoad)
			gbRenderer.loadNewWorld();

		return true;
	}

	/**
	 * Tells the sequence that there has been a new screen tap
	 * 
	 * @param x
	 * @param y
	 */
	public synchronized void tap(float x, float y) {
	}

	public void release() {
	}

	public void reloadTexture(GBActivity context) {
		
	}
}
