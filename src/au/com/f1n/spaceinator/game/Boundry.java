package au.com.f1n.spaceinator.game;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import au.com.f1n.spaceinator.GBSoundManager;
import au.com.f1n.spaceinator.Util;

public abstract class Boundry implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final float BOUNDRY_ACCEL = 0.1f;
	protected float boundryDecel = .8f;
	public static final float HELIO_SIZE = PSpaceShip.SHIELD_SIZE * 1.6118f;
	private ByteBuffer colourBuffer[];
	private float[][] textureData;
	private boolean red;

	public abstract void collideTest(PSpaceShip pSpaceShip, GBSoundManager soundManager);

	public ByteBuffer[] getColourBuffer() {
		// Free the memory...may be GC later
		ByteBuffer bb[] = colourBuffer;
		colourBuffer = null;
		return bb;
	}

	public FloatBuffer[] makeVertexData() {
		float[][] points = make2DPoints();

		FloatBuffer[] fb = new FloatBuffer[points.length];
		textureData = new float[points.length][];
		colourBuffer = new ByteBuffer[points.length];

		for (int j = 0; j < points.length; j++) {
			float[] p = points[j];
			int boundryN = p.length / 2;
			float[] vertex = new float[boundryN * 3 * 2];

			colourBuffer[j] = ByteBuffer.allocateDirect(boundryN * 2 * 4);
			byte[] in;
			byte[] out;
			if (red) {
				in = new byte[] { (byte) 200, (byte) 0, (byte) 0, (byte) 0 };
				out = new byte[] { (byte) 255, (byte) 100, (byte) 100, (byte) 200 };
			} else {
				in = new byte[] { (byte) 200, (byte) 200, (byte) 200, (byte) 0 };
				out = new byte[] { (byte) 200, (byte) 200, (byte) 200, (byte) 200 };
			}

			textureData[j] = new float[boundryN * 2 * 2];

			int v = 0;
			float lastx = p[p.length - 4];
			float lasty = p[p.length - 3];
			float curx, cury;
			float nextx = p[0];
			float nexty = p[1];

			float length = 0;
			int t = 0;

			for (int i = 0; i < boundryN; i++) {
				colourBuffer[j].put(in);
				colourBuffer[j].put(out);

				textureData[j][t++] = length;
				textureData[j][t++] = 0;
				textureData[j][t++] = length;
				textureData[j][t++] = 1;

				curx = nextx;
				cury = nexty;
				if (i * 2 + 2 >= p.length) {
					// This assumes that the last point and the first point are the
					// same!
					nextx = p[2];
					nexty = p[3];
				} else {
					nextx = p[i * 2 + 2];
					nexty = p[i * 2 + 3];
				}

				float dxlast = curx - lastx;
				float dylast = cury - lasty;
				float dInvlast = Util.invSqrt(dxlast * dxlast + dylast * dylast);

				float dxnext = nextx - curx;
				float dynext = nexty - cury;
				float dInvnext = Util.invSqrt(dxnext * dxnext + dynext * dynext);
				length += 1 / dInvnext / HELIO_SIZE / 2;

				float dx = (dylast * dInvlast + dynext * dInvnext);
				float dy = -(dxlast * dInvlast + dxnext * dInvnext);
				float inv = Util.invSqrt(dx * dx + dy * dy);

				vertex[v * 3 + 0] = lastx = curx;
				vertex[v++ * 3 + 1] = lasty = cury;
				vertex[v * 3 + 0] = curx + dx * inv * HELIO_SIZE / 2;
				vertex[v++ * 3 + 1] = cury + dy * inv * HELIO_SIZE / 2;
			}

			colourBuffer[j].position(0);
			fb[j] = Util.makeFloatBuffer(vertex);
		}

		return fb;
	}

	protected abstract float[][] make2DPoints();

	public float[][] getTextureData() {
		return textureData;
	}

	public void setRed(boolean red) {
		this.red = red;
	}
}
