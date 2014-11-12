package au.com.f1n.spaceinator.physics.menu;

import java.nio.FloatBuffer;

import au.com.f1n.spaceinator.game.PGraphicParticle;

public interface GalaxyParticles extends PGraphicParticle {

	public float getStarCoord(int i, int j);

	public void setX(float x);

	public void setY(float y);

	/**
	 * Has the galaxy been exploded enough (more than 50%)
	 * 
	 * This name is somewhat misleading...
	 * 
	 * @return true when the galaxy has exploded some
	 */
	public boolean hasExploded();

	public float getProportion();

	public int getIndex();

	public float getTargetY();

	public void setTracking(float proportionTrack);

	public float getYRot();

	public float getTracking();

	public float getTextScale();

	public float getGlowSize();

	public float getGlowX();

	public float getGlowY();

	public float getGlowZ();

	public FloatBuffer getTextCoords();

	public float getTextX();

	public float getTextY();

	public float getLabelScale();
}
