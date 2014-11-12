package au.com.f1n.spaceinator.mesh;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.opengl.GLES10;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.PSpaceShip;
import au.com.f1n.spaceinator.game.WeaponShipTurret;

/**
 * A Spaceship!
 */
public class SpaceShip {
	private FloatBuffer mTriangleBuffer;
	private FloatBuffer mTextureCoords;
	private int[] texture = new int[1];

	private FloatBuffer tFVertexBuffer;
	private FloatBuffer tTextureCoords;

	private FloatBuffer atFVertexBuffer;
	private FloatBuffer atTextureCoords;

	private FloatBuffer mShieldVertexData;
	private PSpaceShip pSpaceShip;
	public static final int SHIELD_N = 65;
	private byte[] shieldColorArray;
	private ByteBuffer shieldColourBuffer;
	private int lastShield;
	private Context context;
	private int paint;
	public static final int[] SHIP_PAINTS = { R.drawable.shiptex0, R.drawable.shiptex1, R.drawable.shiptex2, R.drawable.shiptex3, R.drawable.shiptex4,
			R.drawable.shiptex5 };

	public SpaceShip(Context context, PSpaceShip pSpaceShip, int paint) {
		this.pSpaceShip = pSpaceShip;
		this.context = context;
		setPaint(paint);

		MeshReader meshReader = new MeshReader(context.getResources().openRawResource(R.raw.main_spaceship), true);
		mTriangleBuffer = meshReader.getTriangles();
		mTextureCoords = meshReader.getTextCoords();

		meshReader = new MeshReader(context.getResources().openRawResource(R.raw.main_turret), true);
		tFVertexBuffer = meshReader.getTriangles();
		tTextureCoords = meshReader.getTextCoords();

		meshReader = new MeshReader(context.getResources().openRawResource(R.raw.auto_turret), true);
		atFVertexBuffer = meshReader.getTriangles();
		atTextureCoords = meshReader.getTextCoords();
		meshReader = null;

		// Make the shield
		float[] shieldVertex = new float[SHIELD_N * 3];
		shieldColorArray = new byte[SHIELD_N * 4];

		shieldVertex[2] = 600;

		for (int i = 1; i < SHIELD_N; i++) {
			double angle = Math.PI * 2 * i / (SHIELD_N - 2);

			shieldVertex[i * 3] = (float) Math.cos(angle) * PSpaceShip.SHIELD_SIZE;
			shieldVertex[i * 3 + 1] = (float) Math.sin(angle) * PSpaceShip.SHIELD_SIZE;
			shieldVertex[i * 3 + 2] = 60;
		}

		mShieldVertexData = Util.makeFloatBuffer(shieldVertex);
		shieldColourBuffer = ByteBuffer.allocateDirect(SHIELD_N * 4);
	}

	public void setPaint(int paint) {
		this.paint = paint;
		// free some ram first
		release();
		reloadTexture(context);
	}

	public void draw(float scaleFactor, float ssx, float ssy, float ssz, float ssFaceDeg, float ssrotZ2, float ssrotX, int ssLaserCount, float ssShootDeg,
			int ssTurretCount, int turretSpread) {
		// Draw spaceship
		GLES10.glPushMatrix();
		GLES10.glDisableClientState(GLES10.GL_COLOR_ARRAY);
		GLES10.glDisableClientState(GLES10.GL_NORMAL_ARRAY);
		GLES10.glDisable(GLES10.GL_BLEND);
		GLES10.glTranslatef(ssx, ssy, ssz);
		GLES10.glPushMatrix();
		GLES10.glRotatef(ssrotZ2, 0, 0, 1);
		GLES10.glRotatef(ssrotX, 1, 0, 0);
		GLES10.glRotatef(ssFaceDeg, 0, 0, 1);

		GLES10.glEnable(GLES10.GL_TEXTURE_2D);
		GLES10.glEnableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
		GLES10.glVertexPointer(3, GL11.GL_FLOAT, 0, mTriangleBuffer);
		GLES10.glColor4f(1, 1, 1, 1);
		GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, texture[0]);
		GLES10.glTexCoordPointer(2, GLES10.GL_FLOAT, 0, mTextureCoords);
		GLES10.glDrawArrays(GLES10.GL_TRIANGLES, 0, mTriangleBuffer.capacity() / 3);

		// Draw Turrets
		for (int l = 0; l < ssLaserCount; l++) {
			GLES10.glPushMatrix();
			GLES10.glTranslatef(PSpaceShip.LASER_OFFSETS[l][0], PSpaceShip.LASER_OFFSETS[l][1], PSpaceShip.LASER_OFFSETS[l][2]);
			GLES10.glRotatef(ssShootDeg - ssFaceDeg, 0, 0, 1);

			GLES10.glVertexPointer(3, GL11.GL_FLOAT, 0, tFVertexBuffer);
			GLES10.glTexCoordPointer(2, GLES10.GL_FLOAT, 0, tTextureCoords);
			GLES10.glDrawArrays(GLES10.GL_TRIANGLES, 0, tFVertexBuffer.capacity() / 3);
			GLES10.glPopMatrix();
		}

		float sinRot = 0;
		if (pSpaceShip == null) {
			sinRot = (float) Math.sin(System.currentTimeMillis() / 200.0) * .5f + .5f;
		}

		// Draw Auto Turrets
		for (int l = 0; l < ssTurretCount; l++) {
			GLES10.glPushMatrix();
			GLES10.glTranslatef(PSpaceShip.TURRET_OFFSETS[l][0], PSpaceShip.TURRET_OFFSETS[l][1], 0);
			if (pSpaceShip == null) {
				float angleMin = (WeaponShipTurret.ANGLES[l][0] - turretSpread * .1f);
				float angleMax = (WeaponShipTurret.ANGLES[l][1] + turretSpread * .1f);

				GLES10.glRotatef((angleMin + sinRot * (angleMax - angleMin)) * Util.RAD_TO_DEG, 0, 0, 1);
			} else {
				GLES10.glRotatef((float) ((pSpaceShip.getShootingTurretAngle(l)) * Util.RAD_TO_DEG) - ssFaceDeg, 0, 0, 1);
			}

			GLES10.glVertexPointer(3, GL11.GL_FLOAT, 0, atFVertexBuffer);
			GLES10.glTexCoordPointer(2, GLES10.GL_FLOAT, 0, atTextureCoords);
			GLES10.glDrawArrays(GLES10.GL_TRIANGLES, 0, atFVertexBuffer.capacity() / 3);
			GLES10.glPopMatrix();
		}

		GLES10.glPopMatrix();

		// Drawn all texture things - disable now
		GLES10.glDisable(GLES10.GL_TEXTURE_2D);
		GLES10.glEnableClientState(GLES10.GL_COLOR_ARRAY);
		if (pSpaceShip != null) {
			GLES10.glPopMatrix();
			// Draw top particles
			GLES10.glEnable(GLES10.GL_BLEND);
			GLES10.glPointSize(3);
			GLES10.glColorPointer(4, GLES10.GL_UNSIGNED_BYTE, 0, pSpaceShip.getTopParticles().colourByte());
			GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, pSpaceShip.getTopParticles().getFloatBuffer());
			GLES10.glDrawArrays(GLES10.GL_POINTS, 0, pSpaceShip.getTopParticles().getLastNPoints());

			GLES10.glPushMatrix();
			GLES10.glTranslatef(ssx, ssy, ssz);

			// Draw shield
			if (lastShield != pSpaceShip.getCurShield() || pSpaceShip.getShieldAlpha() < 1)
				setShield(pSpaceShip.getCurShield(), pSpaceShip.getShieldAlpha());

			GLES10.glColorPointer(4, GLES10.GL_UNSIGNED_BYTE, 0, shieldColourBuffer);
			GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, mShieldVertexData);
			GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, SHIELD_N);
		}
		GLES10.glPopMatrix();
		GLES10.glDisable(GLES10.GL_DEPTH_TEST);
	}

	public void setShield(int curShield, float shieldAlpha) {
		lastShield = curShield;
		shieldColorArray[0] = curShield < 25 ? (byte) 255 : (byte) 64;
		shieldColorArray[1] = (byte) 66;
		shieldColorArray[2] = (byte) 160;
		shieldColorArray[3] = 0;
		for (int i = 1; i < SHIELD_N; i++) {
			shieldColorArray[i * 4] = curShield < 25 ? (byte) 255 : (byte) 64;
			shieldColorArray[i * 4 + 1] = (byte) 66;
			shieldColorArray[i * 4 + 2] = (byte) 160;
			if (((curShield / 2 + 20) * shieldAlpha) > 255)
				shieldColorArray[i * 4 + 3] = (byte) 255;
			else
				shieldColorArray[i * 4 + 3] = (byte) ((curShield / 2 + 20) * shieldAlpha);
		}
		shieldColourBuffer.put(shieldColorArray);
		shieldColourBuffer.position(0);
	}

	public void draw(float scaleFactor) {
		if (pSpaceShip.facingIn) {
			draw(scaleFactor, pSpaceShip.x, pSpaceShip.y, pSpaceShip.z, 90, pSpaceShip.facingAngle * Util.RAD_TO_DEG - 90, pSpaceShip.rotX,
					pSpaceShip.getLaserCount(), pSpaceShip.getShootingAngle() * Util.RAD_TO_DEG, pSpaceShip.getTurretCount(), 0);
		} else {
			draw(scaleFactor, pSpaceShip.x, pSpaceShip.y, pSpaceShip.z, pSpaceShip.facingAngle * Util.RAD_TO_DEG, 0, pSpaceShip.rotX, pSpaceShip.getLaserCount(),
					pSpaceShip.getShootingAngle() * Util.RAD_TO_DEG, pSpaceShip.getTurretCount(), 0);
		}
	}

	public FloatBuffer getShieldVertexData() {
		return mShieldVertexData;
	}

	public ByteBuffer getShieldColourBuffer() {
		return shieldColourBuffer;
	}

	public void release() {
		GLES10.glDeleteTextures(1, texture, 0);
	}

	public void reloadTexture(Context context) {
		Util.createTexture(context, SHIP_PAINTS[paint], texture);
	}
}