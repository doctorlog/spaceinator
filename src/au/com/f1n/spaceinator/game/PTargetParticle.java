package au.com.f1n.spaceinator.game;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.enemy.PEnemy;

public class PTargetParticle implements PGraphicParticle {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int N_PARTICLES = 100;
	private float x;
	private float y;
	private float r;
	private float proportion;
	private boolean inside;

	private float[] colours;
	private transient FloatBuffer particleBuffer;
	private float zRot;
	private boolean exploding;
	private float exploded = 1;
	private PEnemy tracking;

	public PTargetParticle(float x, float y, float r) {
		this.x = x;
		this.y = y;
		this.r = r;

		colours = new float[4];
		// inner
		colours[0] = 0;
		colours[1] = 0;
		colours[2] = 1;
		colours[3] = .8f;

		makeParticleBuf();
	}

	private void makeParticleBuf() {
		particleBuffer = Util.makeFloatBuffer(N_PARTICLES * 3);

		for (int i = 0; i < N_PARTICLES; i++) {
			if (i >= N_PARTICLES / 2) {
				float t = (float) (Math.PI * 4 * i / N_PARTICLES);
				particleBuffer.put((float) (Math.cos(t) * r));
				particleBuffer.put((float) (Math.sin(t) * r));
				particleBuffer.put(0f);
			} else {
				float t = (float) (Math.PI * 4 * (i + 0.5) / N_PARTICLES);
				particleBuffer.put((float) (Math.cos(t) * (r * .9)));
				particleBuffer.put((float) (Math.sin(t) * (r * .9)));
				particleBuffer.put(0f);
			}
		}
		particleBuffer.position(0);
	}

	@Override
	public boolean timeStep(int curTimeRate, long timeS) {
		if (tracking != null) {
			x = tracking.x;
			y = tracking.y;
			if (tracking.isDead() && !exploding)
				explode();
		}

		if (inside && proportion < 1)
			proportion += (float) curTimeRate / 4000f;
		else if (proportion > 0)
			proportion -= (float) curTimeRate / 4000f;
		if (proportion < 0)
			proportion = 0;
		if (proportion > 1)
			proportion = 1;

		if (exploding) {
			exploded += (float) curTimeRate / 100f;
			colours[3] -= 0.03f;
		} else {
			zRot -= (proportion + 1) * (float) curTimeRate / 25f;
			colours[3] = .8f + .2f * proportion;
		}
		return colours[3] <= 0;
	}

	@Override
	public int getLastNPoints() {
		return exploding ? N_PARTICLES / 2 : (int) (N_PARTICLES / 2 + proportion * N_PARTICLES / 2);
	}

	@Override
	public float[] colourArray() {
		return colours;
	}

	@Override
	public ByteBuffer colourByte() {
		return null;
	}

	@Override
	public float getWidth() {
		return 10;
	}

	@Override
	public FloatBuffer getFloatBuffer() {
		if (particleBuffer == null)
			makeParticleBuf();
		return particleBuffer;
	}

	@Override
	public float getX() {
		return x;
	}

	@Override
	public float getY() {
		return y;
	}

	@Override
	public float getZRot() {
		return zRot;
	}

	@Override
	public float getScale() {
		if (exploding)
			return exploded + .05f * (float) Math.sin(zRot * .1);

		return 1 + .05f * (float) Math.sin(zRot * .1);
	}

	public void setInside(boolean inside) {
		colours[0] = 0;
		colours[1] = inside ? 1 : 0;
		colours[2] = inside ? 0 : 1;

		this.inside = inside;
	}

	public float getProportion() {
		return proportion;
	}

	public void explode() {
		exploding = true;
		proportion = 0;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getR() {
		return r;
	}

	public void setTracking(PEnemy object) {
		this.tracking = object;

	}
}
