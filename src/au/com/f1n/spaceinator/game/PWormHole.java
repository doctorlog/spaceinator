package au.com.f1n.spaceinator.game;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import au.com.f1n.spaceinator.Util;

public class PWormHole implements PGraphicParticle {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final float WORM_TIME = 2500;
	private static final int N_U = 20;
	private static final int N_V = 32;
	private static final int N_POINT = N_U * N_V;
	private static final int TIME_BUILD = 1000;
	private World world;
	private long start;
	private boolean worming;
	private float destX;
	private float destY;
	private float x;
	private float y;
	private PSpaceShip ss;
	private float radius;
	private transient FloatBuffer vertices;
	private transient ByteBuffer colour;
	private int colLayer = 0;
	private byte[] tmpCol = new byte[4];
	private float zRot;
	private int build;
	private float bigR;
	private float dx;
	private float dy;
	private int nDraw;
	private long nextBeep = 0;

	public PWormHole(World world, float x, float y, float radius, float destX, float destY) {
		this.x = x;
		this.y = y;
		this.world = world;
		this.destX = destX;
		this.destY = destY;
		this.radius = radius;
		ss = world.getCentreSpaceShip();

		updateVertices();
		updateColour();
	}

	private void updateVertices() {
		vertices = Util.makeFloatBuffer(N_POINT * 3);

		dx = destX - x;
		dy = destY - y;
		bigR = (float) Math.sqrt(dx * dx + dy * dy);
		zRot = (float) Math.atan2(dy, dx);

		for (int v = 0; v < N_V; v++) {
			float vC = Util.PI * (N_V - v) / N_V;
			for (int u = 0; u < N_U; u++) {
				float uC = Util.PI * 2 * u / N_U;
				vertices.put(bigR / 2 + (float) ((bigR / 2 + radius * Math.cos(uC)) * Math.cos(vC)));
				vertices.put((float) (radius * Math.sin(uC)));
				vertices.put((float) (-(bigR / 2 + radius * Math.cos(uC)) * Math.sin(vC)));
			}
		}
		vertices.position(0);
	}

	private void updateColour() {
		if (colour == null)
			colour = ByteBuffer.allocateDirect(N_POINT * 4);
		tmpCol[0] = (byte) 255;
		tmpCol[1] = (byte) 255;
		tmpCol[2] = (byte) 255;
		tmpCol[3] = (byte) 255;

		for (int v = 0; v < N_V; v++) {
			if (v > colLayer) {
				tmpCol[1] = (byte) 255;
				tmpCol[2] = (byte) 255;
			} else {
				tmpCol[1] = (byte) 0;
				tmpCol[2] = (byte) 0;
			}
			tmpCol[3] = (byte) (255 - 180 * v / N_V);
			for (int u = 0; u < N_U; u++) {
				colour.put(tmpCol);
			}
		}
		colour.position(0);
	}

	@Override
	public boolean timeStep(int dTime, long timeS) {
		if (worming) {
			float amt = (float) (timeS - start) / WORM_TIME;

			if (amt >= 1) {
				world.getCentreSpaceShip().setWorm(false);
				worming = false;
				ss.x = destX;
				ss.y = destY;
				ss.z = ss.dx = ss.dy = 0;
				world.getCamera().z = PCamera.BASE_Z;
				((PStandardCamera) world.getCamera()).dx = 0;
				((PStandardCamera) world.getCamera()).dy = 0;
			} else {
				double angle = amt * Math.PI;
				float halfX = (x + destX) / 2;
				float halfY = (y + destY) / 2;

				ss.setAccel(0, 0, zRot);
				ss.facingAngle = ss.facingAngle * .9f + zRot * .1f;
				float cos2 = (float) Math.cos(angle) / 2;
				ss.x = halfX - dx * cos2;
				ss.y = halfY - dy * cos2;
				ss.z = -(float) Math.sin(angle) * bigR;
				// Doesnt really work
				// ss.rotX = (Util.PI - (float) angle) * 180 / Util.PI;

				float amtSq = amt * (1 - amt);

				world.getCamera().z = PCamera.BASE_Z * (1 + 1.5f * amtSq);
				((PStandardCamera) world.getCamera()).dx = .0005f * amtSq * dx;
				((PStandardCamera) world.getCamera()).dy = .0005f * amtSq * dy;
			}
		} else {
			float dx = ss.x - x;
			float dy = ss.y - y;
			if (dx * dx + dy * dy < radius * radius) {
				build += dTime;
				if (build > TIME_BUILD) {
					worming = true;
					world.getCentreSpaceShip().setWorm(true);
					start = world.lastTime;
					world.gbRenderer.getSoundManager().teleportWorm();
				} else if (timeS > nextBeep) {
					world.gbRenderer.getSoundManager().beepMenu(0.5f + build / 1000f);
					nextBeep = timeS + 200;
				}
			} else {
				build -= dTime;
				if (build < 0)
					build = 0;
			}
			int newColLayer = (N_V * build) / TIME_BUILD;
			if (newColLayer != colLayer) {
				colLayer = newColLayer;
				updateColour();
			}
		}

		if (nDraw < N_POINT)
			nDraw++;

		return false;
	}

	@Override
	public int getLastNPoints() {
		return nDraw;
	}

	@Override
	public float[] colourArray() {
		return null;
	}

	@Override
	public ByteBuffer colourByte() {
		if (colour == null)
			updateColour();
		return colour;
	}

	@Override
	public float getWidth() {
		return 2;
	}

	@Override
	public FloatBuffer getFloatBuffer() {
		if (vertices == null)
			updateVertices();
		return vertices;
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
	public float getScale() {
		return 1;
	}

	@Override
	public float getZRot() {
		return zRot * 180 / Util.PI;
	}

	@Override
	public void explode() {
	}
}
