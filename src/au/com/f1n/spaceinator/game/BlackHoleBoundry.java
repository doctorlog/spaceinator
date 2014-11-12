package au.com.f1n.spaceinator.game;

import java.nio.FloatBuffer;

import au.com.f1n.spaceinator.Util;

public class BlackHoleBoundry extends PolyBoundry {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int N = 257;
	private float[][] vertices;
	private transient FloatBuffer[] fb;
	private float radius;
	private double adder;
	private int nextAddStart;
	private int nextAddEnd;
	private int addLevel;
	private double nextAddAngle;
	private int nextAddLevel;

	public BlackHoleBoundry(float radius) {
		super(new int[] { N });
		// Aggressive boundry
		boundryDecel = 1;
		this.radius = radius;

		for (int i = 0; i < N; i++) {
			double angle = Math.PI * 2 * i / (N - 1);
			addPoint((float) Math.cos(angle) * radius, (float) Math.sin(angle) * radius);
		}

		// Make the vertices float array
		vertices = new float[1][N * 2 * 3];
	}

	/**
	 * This causes the boundrydrawer and this class to have the same fb reference
	 */
	@Override
	public FloatBuffer[] makeVertexData() {
		if (fb == null) {
			fb = super.makeVertexData();
		}

		return fb;
	}

	public void timeStep(int dTime, long timeS) {
		adder += dTime * .001;

		// Change the points
		int i = 0;
		for (int j = 0; j < N; j++) {
			double angle = Math.PI * 2 * j / (N - 1);
			double r = radius * (1 + .1 * Math.sin(angle * 3 - adder));

			if (adder < nextAddEnd) {
				double add = (1 - Math.cos(angle + nextAddAngle + adder * addLevel / 20)) * .5;
				// Raise ^4
				add *= add;
				add *= add;
				r += radius * (adder - nextAddStart) * (nextAddEnd - adder) * add / 100;
			} else {
				addLevel = nextAddLevel * (Util.randFloat() < .5 ? -1 : 1);
				nextAddStart = nextAddEnd;
				nextAddEnd = nextAddEnd + 20;
				nextAddAngle = Util.randFloat() * Math.PI * 2;
			}

			if (r < radius / 2)
				r = radius / 2;
			// Add small fast movements
			r += radius * (0.01 * Math.sin(angle * 13 - adder * 23) + 0.025 * Math.sin(angle * 17 + adder * 13));

			points[0][i++] = (float) (Math.cos(angle) * r);
			points[0][i++] = (float) (Math.sin(angle) * r);
		}

		// Update the vertex buffer to be drawn
		updateChangedVertexData(fb, vertices);
	}

	public void addLevel() {
		nextAddLevel++;
	}

	public void addRadius(int r) {
		radius += r;
	}
}
