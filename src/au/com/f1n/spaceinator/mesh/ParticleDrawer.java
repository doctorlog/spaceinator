package au.com.f1n.spaceinator.mesh;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES10;
import au.com.f1n.spaceinator.game.PGraphicParticle;
import au.com.f1n.spaceinator.game.World;

public class ParticleDrawer {
	protected World world;

	public ParticleDrawer(World loading) {
		this.world = loading;
	}

	public void draw(float scaleFactor) {
		GLES10.glDisable(GL10.GL_TEXTURE_2D);
		GLES10.glEnable(GL10.GL_BLEND);
		GLES10.glDisable(GL10.GL_LIGHTING);
		if (world.zooming > 0)
			GLES10.glEnable(GL10.GL_DEPTH_TEST);
		else
			GLES10.glDisable(GL10.GL_DEPTH_TEST);

		GLES10.glEnableClientState(GL10.GL_VERTEX_ARRAY);

		GLES10.glMatrixMode(GL10.GL_MODELVIEW);

		// Draw graphic particles first
		ArrayList<PGraphicParticle> particles = world.getGrpahicParticles();

		if (particles != null)
			for (PGraphicParticle particle : particles) {
				GLES10.glPushMatrix();
				GLES10.glTranslatef(particle.getX(), particle.getY(), 0);
				GLES10.glRotatef(particle.getZRot(), 0, 0, 1);
				GLES10.glScalef(particle.getScale(), particle.getScale(), particle.getScale());

				float[] colour = particle.colourArray();
				if (colour != null) {
					GLES10.glDisableClientState(GL10.GL_COLOR_ARRAY);
					GLES10.glColor4f(colour[0], colour[1], colour[2], colour[3]);
				} else {
					GLES10.glEnableClientState(GL10.GL_COLOR_ARRAY);
					GLES10.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, particle.colourByte());
				}

				GLES10.glPointSize(particle.getWidth() / scaleFactor);
				GLES10.glVertexPointer(3, GL10.GL_FLOAT, 0, particle.getFloatBuffer());

				GLES10.glDrawArrays(GL10.GL_POINTS, 0, particle.getLastNPoints());

				GLES10.glPopMatrix();
			}
	}
}
