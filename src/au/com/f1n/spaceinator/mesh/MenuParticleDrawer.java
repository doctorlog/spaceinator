package au.com.f1n.spaceinator.mesh;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES10;
import au.com.f1n.spaceinator.game.PGraphicParticle;
import au.com.f1n.spaceinator.game.World;
import au.com.f1n.spaceinator.physics.menu.GalaxyParticles;

public class MenuParticleDrawer extends ParticleDrawer {

	public MenuParticleDrawer(World loading) {
		super(loading);
	}

	public void draw(float scaleFactor) {
		GLES10.glDisable(GL10.GL_TEXTURE_2D);
		GLES10.glEnable(GL10.GL_BLEND);
		GLES10.glDisable(GL10.GL_LIGHTING);
		GLES10.glDisable(GL10.GL_DEPTH_TEST);

		GLES10.glEnableClientState(GL10.GL_VERTEX_ARRAY);

		// Draw graphic particles first
		ArrayList<PGraphicParticle> particles = world.getGrpahicParticles();

		for (PGraphicParticle particle : particles) {
			GalaxyParticles gp = (GalaxyParticles) particle;

			GLES10.glPushMatrix();
			GLES10.glTranslatef(particle.getX(), particle.getY(), 0);
			GLES10.glRotatef(gp.getYRot(), 0, 1, 0);
			GLES10.glScalef(particle.getScale(), particle.getScale(), particle.getScale());

			GLES10.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, particle.colourByte());

			GLES10.glPointSize(particle.getWidth() / scaleFactor);
			GLES10.glVertexPointer(3, GL10.GL_FLOAT, 0, particle.getFloatBuffer());

			GLES10.glEnableClientState(GL10.GL_COLOR_ARRAY);
			GLES10.glDrawArrays(GL10.GL_POINTS, 0, particle.getLastNPoints());

			// Special case - only for the galaxy, redraw as white dots
			GLES10.glPointSize(1);
			GLES10.glDisableClientState(GL10.GL_COLOR_ARRAY);
			GLES10.glColor4f(1, 1, 1, ((GalaxyParticles) particle).getProportion());
			GLES10.glDrawArrays(GL10.GL_POINTS, 0, particle.getLastNPoints());

			GLES10.glPopMatrix();
		}
	}
}
