package au.com.f1n.spaceinator.game;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import au.com.f1n.spaceinator.Util;

/**
 * Implements particles that move in relation to weapon effects
 * 
 * @author luke
 */
public class PMissilesParticle implements PGraphicParticle {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int N = 500;
	private int next;
	private float[] vertices;
	private float[] dVertices;
	private transient FloatBuffer verticesBuffer;
	private transient ByteBuffer colourBuffer;
	private byte[] colours;

	public PMissilesParticle() {
		colours = new byte[N * 4];
		vertices = new float[N * 3];
		dVertices = new float[N * 2];
	}

	@Override
	public boolean timeStep(int curTimeRate, long timeS) {
		// fade
		for (int i = 0; i < N; i++) {
			// transparency fade
			int tmp = i * 4 + 3;
			if (colours[tmp] != 0) {
				colours[tmp] -= 5;

				// particle move
				tmp = i * 2 + 0;
				vertices[i * 3 + 0] += dVertices[tmp] * curTimeRate;
				dVertices[tmp] *= .95f;

				tmp = i * 2 + 1;
				vertices[i * 3 + 1] += dVertices[tmp] * curTimeRate;
				dVertices[tmp] *= .95f;
			}
		}

		return false;
	}

	@Override
	public int getLastNPoints() {
		return N;
	}

	@Override
	public float[] colourArray() {
		return null;
	}

	@Override
	public ByteBuffer colourByte() {
		if (colourBuffer == null) {
			colourBuffer = ByteBuffer.allocateDirect(N * 4);
			colourBuffer.position(0);
		}
		
		colourBuffer.put(colours);
		colourBuffer.position(0);

		return colourBuffer;
	}

	@Override
	public float getWidth() {
		return 2;
	}

	@Override
	public FloatBuffer getFloatBuffer() {
		if (verticesBuffer == null) {
			verticesBuffer = Util.makeFloatBuffer(N * 3);
			verticesBuffer.position(0);
		}

		verticesBuffer.put(vertices);
		verticesBuffer.position(0);

		return verticesBuffer;
	}

	@Override
	public float getX() {
		return 0;
	}

	@Override
	public float getY() {
		return 0;
	}

	@Override
	public float getScale() {
		return 1;
	}

	@Override
	public float getZRot() {
		return 0;
	}

	public void addParticleFor(PObject object, byte red, byte green, byte blue, float dx, float dy) {
		colours[next * 4 + 0] = red;
		colours[next * 4 + 1] = green;
		colours[next * 4 + 2] = blue;
		colours[next * 4 + 3] = (byte) 255;

		vertices[next * 3 + 0] = object.x;
		vertices[next * 3 + 1] = object.y;
		vertices[next * 3 + 2] = object.z;

		dVertices[next * 2 + 0] = dx;
		dVertices[next * 2 + 1] = dy;

		next = (next + 1) % N;
	}

	@Override
	public void explode() {
	}
}
