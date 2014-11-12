package au.com.f1n.spaceinator.game;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public interface PGraphicParticle extends Serializable {
	public boolean timeStep(int curTimeRate, long timeS);

	public int getLastNPoints();

	public float[] colourArray();

	public ByteBuffer colourByte();

	public float getWidth();

	public FloatBuffer getFloatBuffer();

	public float getX();

	public float getY();

	public float getScale();

	public float getZRot();

	public void explode();
}
