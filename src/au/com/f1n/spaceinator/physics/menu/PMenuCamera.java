package au.com.f1n.spaceinator.physics.menu;

import android.util.Log;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.PCamera;

public class PMenuCamera extends PCamera {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final long ROT_TIME = 1100;
	public static final long TRACK_TIME = 500;
	public static final float OUT_Z = BASE_Z * 1.7f;
	public static final float BASE_ROT_Z = 20;

	private GalaxyParticles tracking;
	private GalaxyParticles trackingLast;
	private float proportionTrack;
	private float rotXProportion;
	private long trackStart;
	private long timeStart;
	private float lastX;
	private float lastY;
	private float lastZ;
	private float nextX;
	private float nextY;
	private float nextZ;
	private float nextRotZ;
	private float lastRotZ;
	private float creditX;
	private float creditY;
	private float creditZ;
	private float creditXRot;
	private float creditYRot;
	private boolean initial;

	public PMenuCamera(MenuWorld world) {
		super(world);
		lastZ = nextZ = OUT_Z;
		lastRotZ = nextRotZ = BASE_ROT_Z;
	}

	@Override
	public float getX() {
		float tmp = Util.slowInOut(proportionTrack);
		return tmp * nextX + (1 - tmp) * lastX + creditX;
	}

	@Override
	public float getY() {
		float tmp = Util.slowInOut(proportionTrack);
		return tmp * nextY + (1 - tmp) * lastY + creditY;
	}

	@Override
	public float getZ() {
		return z + creditZ;
	}

	@Override
	public synchronized void timeStep(int dTime) {
		if (timeStart == 0) {
			timeStart = world.lastTime;
		}

		if (world.lastTime - timeStart < ROT_TIME)
			rotXProportion = Util.slowOut((float) (world.lastTime - timeStart) / ROT_TIME);
		else {
			// Tell the camera which galaxy to track immediately (unless the user
			// selected something else)
			rotXProportion = 1;

			if (!initial) {
				Log.d("Auto track to ", "" + world.getGameState().getMaxLevel() / 10);
				setTracking(((MenuWorld) world).getGalaxyParticles()[world.getGameState().getMaxLevel() / 10]);
				initial = true;
			}
		}

		if (world.lastTime - trackStart < TRACK_TIME)
			proportionTrack = (float) (world.lastTime - trackStart) / TRACK_TIME;
		else {
			proportionTrack = 1;
		}

		if (tracking != null)
			tracking.setTracking(proportionTrack);
		if (trackingLast != null)
			trackingLast.setTracking(1 - proportionTrack);

		z = proportionTrack * nextZ + (1 - proportionTrack) * lastZ;

		if (!(tracking instanceof GalaxyCreditParticles)) {
			creditX *= .9f;
			creditY *= .9f;
			creditZ *= .9f;
			creditXRot *= .9f;
			creditYRot *= .9f;
		}
	}

	@Override
	public float getRotX() {
		return -60 * Util.slowOut(rotXProportion) + creditXRot;
	}

	@Override
	public float getRotZ() {
		float tmp = Util.slowInOut(proportionTrack);
		return tmp * nextRotZ + (1 - tmp) * lastRotZ;
	}

	public GalaxyParticles getTracking() {
		return tracking;
	}

	public synchronized void setTracking(GalaxyParticles tracking) {
		if (rotXProportion < 1)
			return;

		if (trackingLast != null)
			trackingLast.setTracking(0);

		// Use the lasts that are current
		trackingLast = this.tracking;
		lastX = getX();
		lastY = getY();
		lastRotZ = getRotZ();
		lastZ = z;

		this.tracking = tracking;
		if (tracking == null) {
			nextX = 0;
			nextY = 0;
			nextRotZ = BASE_ROT_Z;
			nextZ = OUT_Z;
		} else {
			// Print out star locations
			// for (int i = 0; i < 3000; i++) {
			// Log.d("World", "," + i + ", " + tracking.getFloatBuffer().get(i * 3
			// + 0) + ", " + tracking.getFloatBuffer().get(i * 3 + 1));
			// }
			// tracking.getFloatBuffer().position(0);

			nextX = tracking.getX();
			nextY = tracking.getTargetY();
			nextRotZ = 0;
			nextZ = BASE_Z;
		}

		proportionTrack = 0;
		trackStart = world.lastTime;
	}

	public float getProportionTrack() {
		return proportionTrack;
	}

	public boolean isFinishedTrack() {
		return proportionTrack >= 1;
	}

	public void addCredits(float x, float y, float z, float xRotAdd, float yRotAdd) {
		creditX = x;
		creditY = y;
		creditZ = z;
		creditXRot = xRotAdd;
		creditYRot = yRotAdd;
	}

	public float getRotY() {
		return creditYRot;
	}

	@Override
	public void resumeNow(long dTime) {
	}

	public float getRotXProportion() {
		return rotXProportion;
	}
}
