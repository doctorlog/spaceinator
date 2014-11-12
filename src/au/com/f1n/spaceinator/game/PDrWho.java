package au.com.f1n.spaceinator.game;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.enemy.PAsteroidEnemy;

public class PDrWho implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int N_U = 84;
	private static final int N_V = 32;
	private static final int DRAW_N = (N_U - 1) * (N_V - 1) * 2 * 3;
	private static final float[] COS = new float[N_V];
	private static final float[] SIN = new float[N_V];

	private transient FloatBuffer vertexBuffer;
	private float[] vertices;
	float[] textures;
	private transient FloatBuffer textureBuffer;
	private transient ShortBuffer drawOrder;
	private transient ByteBuffer colours;
	public float[] xOff;
	public float[] yOff;
	private float zOff;
	private World world;
	public float speed;
	private float floatScore;
	public float straight = 1;
	public boolean finished;
	private float radius;
	private int textureID;
	public float dZOff;
	public float prop;

	static {
		for (int v = 0; v < N_V; v++) {
			double angle = Math.PI * 2 * v / (N_V - 1);
			COS[v] = (float) Math.cos(angle);
			SIN[v] = (float) Math.sin(angle);
		}
	}

	public PDrWho(World world, float radius, int textureID) {
		this.radius = radius;
		this.world = world;
		this.textureID = textureID;
		vertices = new float[N_U * N_V * 3];
		drawOrder = Util.makeShortBuffer(DRAW_N);

		xOff = new float[N_U];
		yOff = new float[N_U];

		textures = new float[N_U * N_V * 2];

		int i = 0;
		for (int u = 0; u < N_U; u++) {
			for (int v = 0; v < N_V; v++) {
				if (u < N_U - 1 && v < N_V - 1) {
					drawOrder.put((short) (u * N_V + v));
					drawOrder.put((short) ((u + 1) * N_V + v));
					drawOrder.put((short) (u * N_V + v + 1));

					drawOrder.put((short) (u * N_V + v + 1));
					drawOrder.put((short) ((u + 1) * N_V + v));
					drawOrder.put((short) ((u + 1) * N_V + v + 1));
				}

				textures[i++] = (float) (u - 1) / (N_U * 10f);
				textures[i++] = (float) v / (N_V - 1);

			}
		}
		drawOrder.position(0);
	}

	public boolean timeStep(int dTime, long timeS) {
		speed += 0.00001 * dTime;

		floatScore += speed * speed * dTime / 3;
		if (straight >= 1)
			world.score = (int) floatScore;

		zOff -= dTime * .00005f * speed;

		for (int u = 0; u < N_U; u++) {
			xOff[u] = (float) Math.sin(u * .053 + zOff * 41) * 2003 + (float) Math.sin(u * .037 + zOff * 83 * speed) * 1013;
			yOff[u] = (float) Math.sin(u * .013 + zOff * 47) * 2011 + (float) Math.sin(u * .071 + zOff * 97 * speed) * 1009;

			float l;
			l = straight * straight * ((float) Math.exp(((float) (N_U - u) / 20)) / 20f - 0.1f);
			if (l < 0)
				l = 0;

			xOff[u] *= l;
			yOff[u] *= l;
		}

		int i = 0;
		for (int u = 0; u < N_U; u++) {
			float z = 1000 * (u - N_U) + PCamera.BASE_Z;
			for (int v = 0; v < N_V; v++) {
				float r = radius + (z > 0 ? z * z / 50000 : 0);
				vertices[i++] = COS[v] * r + xOff[u];
				vertices[i++] = SIN[v] * r + yOff[u];
				vertices[i++] = z;
			}
		}

		float xShift = (xOff[N_U - 14] - xOff[N_U - 16]) * dTime / 20000 * speed;
		if (straight >= 1) {
			world.getCentreSpaceShip().dx -= xShift;
			world.getCentreSpaceShip().dy += (yOff[N_U - 14] - yOff[N_U - 16]) * dTime / 20000 * speed;
		}

		float d = dTime * .00005f * speed;
		if (!finished)
			for (i = 0; i < textures.length; i += 2)
				// textures[i] -= .0005f * dTime * speed;
				textures[i] -= d;
		// d represents the proportion (of 1) that the texture moved
		prop += d;

		d = .1f * xShift * dTime * speed;
		for (i = 1; i < textures.length; i += 2)
			textures[i] -= d;

		if (textures[0] < -.998f) {
			// They have finished
			finished = true;
		}

		return false;
	}

	public void shiftAsteroid(PAsteroidEnemy asteroid, float x, float y, float z) {
		int idx = N_U - (int) (z / 1000f) + 15;
		asteroid.z = z;
		asteroid.x = x + xOff[idx];
		asteroid.y = y + yOff[idx];
	}

	public FloatBuffer getVertices() {

		if (vertexBuffer == null) {
			vertexBuffer = Util.makeFloatBuffer(vertices);
		} else {
			vertexBuffer.put(vertices);
			vertexBuffer.position(0);
		}

		return vertexBuffer;
	}

	public int getN() {
		return DRAW_N;
	}

	public ShortBuffer getDrawOrder() {
		if (drawOrder == null) {
			drawOrder = Util.makeShortBuffer(DRAW_N);
			for (int u = 0; u < N_U; u++) {
				for (int v = 0; v < N_V; v++) {
					if (u < N_U - 1 && v < N_V - 1) {
						drawOrder.put((short) (u * N_V + v));
						drawOrder.put((short) ((u + 1) * N_V + v));
						drawOrder.put((short) (u * N_V + v + 1));

						drawOrder.put((short) (u * N_V + v + 1));
						drawOrder.put((short) ((u + 1) * N_V + v));
						drawOrder.put((short) ((u + 1) * N_V + v + 1));
					}
				}
			}
			drawOrder.position(0);
		}

		return drawOrder;
	}

	public ByteBuffer getColours() {
		if (colours == null) {
			colours = ByteBuffer.allocateDirect(N_U * N_V * 4);

			byte black = 0;

			for (int u = 0; u < N_U; u++) {
				for (int v = 0; v < N_V; v++) {
					colours.put(black);
					colours.put(black);
					colours.put(black);
					colours.put((byte) 255);
				}

				black += 3;
			}
			colours.position(0);
		}

		return colours;
	}

	public FloatBuffer getTextureCoords() {
		if (textureBuffer == null) {
			textureBuffer = Util.makeFloatBuffer(textures);
		} else {
			textureBuffer.put(textures);
			textureBuffer.position(0);
		}

		return textureBuffer;
	}

	public void stretch(float amt) {
		int i = 0;
		for (int u = 0; u < N_U; u++) {
			for (int v = 0; v < N_V; v++) {
				textures[i++] = (float) (u - 100f) / (N_U * 100f * amt);
				i++;
			}
		}
	}

	public int getTextureID() {
		return textureID;
	}
}
