package au.com.f1n.spaceinator.game;

import java.nio.FloatBuffer;

import au.com.f1n.spaceinator.GBSoundManager;
import au.com.f1n.spaceinator.Util;

public class PolyBoundry extends Boundry {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected float[][] points;
	private int curPoint;
	private int curSet;

	public PolyBoundry(int capacity[]) {
		points = new float[capacity.length][];
		for (int i = 0; i < capacity.length; i++)
			points[i] = new float[capacity[i] * 2];
	}

	public PolyBoundry(float[][] points, float scale) {
		this.points = points;
		for (int i = 0; i < points.length; i++)
			for (int j = 0; j < points[i].length; j++)
				points[i][j] *= scale;
	}

	public void updateChangedVertexData(FloatBuffer[] fb, float[][] vertices) {
		for (int j = 0; j < points.length; j++) {
			float[] p = points[j];
			int boundryN = p.length / 2;
			float[] vertex = vertices[j];

			int v = 0;
			float curx, cury;
			float nextx = p[0];
			float nexty = p[1];

			float dxlast = nextx - p[p.length - 4];
			float dylast = nexty - p[p.length - 3];
			float dInvlast = Util.invSqrt(dxlast * dxlast + dylast * dylast);
			dxlast *= dInvlast;
			dylast *= dInvlast;

			for (int i = 0; i < boundryN; i++) {
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

				float dxnext = nextx - curx;
				float dynext = nexty - cury;
				float dInvnext = Util.invSqrt(dxnext * dxnext + dynext * dynext);

				dxnext *= dInvnext;
				dynext *= dInvnext;

				float dx = (dylast + dynext);
				float dy = -(dxlast + dxnext);
				// Note: invsqrt of dx*dx + dy*dy will always be 0.5

				dxlast = dxnext;
				dylast = dynext;

				vertex[v * 3 + 0] = curx;
				vertex[v++ * 3 + 1] = cury;
				vertex[v * 3 + 0] = curx + dx * HELIO_SIZE / 4;
				vertex[v++ * 3 + 1] = cury + dy * HELIO_SIZE / 4;
			}

			fb[j].put(vertex);
			fb[j].position(0);
		}
	}

	@Override
	public void collideTest(PSpaceShip pSpaceShip, GBSoundManager soundManager) {
		// Ray from x,y with direction [1,0]
		float x = pSpaceShip.x;
		float y = pSpaceShip.y;
		int count = 0;

		for (int j = 0; j < points.length; j++) {
			float[] p = points[j];
			float lastx = p[0];
			float lasty = p[1];
			float curx, cury;
			float maxy, miny, maxx, minx;

			for (int i = 1; i < p.length / 2; i++) {
				curx = p[i * 2];
				cury = p[i * 2 + 1];

				// Does the ray intersect the line segment last -> cur
				if (cury > lasty) {
					maxy = cury;
					miny = lasty;
				} else {
					miny = cury;
					maxy = lasty;
				}

				if (curx > lastx) {
					maxx = curx;
					minx = lastx;
				} else {
					minx = curx;
					maxx = lastx;
				}

				if (y >= miny && y <= maxy && x <= maxx) {
					// chance of hit
					if (x < minx) {
						count++;
					} else {
						// We want the line to slope from left to right

						float red = (lastx == curx ? Float.POSITIVE_INFINITY : (cury - lasty) / (curx - lastx));
						float blue;
						if (lasty < cury)
							blue = (x == lastx ? Float.POSITIVE_INFINITY : (y - lasty) / (x - lastx));
						else
							blue = (x == lastx ? Float.POSITIVE_INFINITY : (y - cury) / (x - curx));
						if (blue >= red) {
							count++;
						}
					}
				}

				lastx = curx;
				lasty = cury;
			}
		}

		if (count % 2 == 0) {
			float bestDist = Float.MAX_VALUE;
			float bestX = 0;
			float bestY = 0;

			for (int j = 0; j < points.length; j++) {
				// Ship is outside! Find the closest line segment
				float[] p = points[j];
				float lastx = p[0];
				float lasty = p[1];

				for (int i = 1; i < p.length / 2; i++) {
					float curx = p[i * 2];
					float cury = p[i * 2 + 1];

					// is this the closest?
					float d2 = Util.dist2(curx, cury, lastx, lasty, x, y);
					if (d2 < bestDist) {
						bestDist = d2;
						bestX = curx - lastx;
						bestY = cury - lasty;
					}
					lastx = curx;
					lasty = cury;
				}
			}

			float dInv = Util.invSqrt(bestX * bestX + bestY * bestY);

			pSpaceShip.dx *= boundryDecel;
			pSpaceShip.dy *= boundryDecel;
			pSpaceShip.dx -= bestY * dInv * BOUNDRY_ACCEL;
			pSpaceShip.dy += bestX * dInv * BOUNDRY_ACCEL;

			soundManager.bounce(1 - bestY * dInv, bestY * dInv);
		}
	}

	@Override
	protected float[][] make2DPoints() {
		return points;
	}

	public void move() {
		curSet++;
		curPoint = 0;
	}

	public void addPoint(float x, float y) {
		points[curSet][curPoint * 2] = x;
		points[curSet][curPoint++ * 2 + 1] = y;
	}
}
