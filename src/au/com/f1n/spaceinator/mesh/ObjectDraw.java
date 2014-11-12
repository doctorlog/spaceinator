package au.com.f1n.spaceinator.mesh;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES10;
import android.opengl.GLES11;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.PBlackHole;
import au.com.f1n.spaceinator.game.PObject;
import au.com.f1n.spaceinator.game.PPlasmaParticle;
import au.com.f1n.spaceinator.game.PSpaceShip;
import au.com.f1n.spaceinator.game.PWeaponParticle;
import au.com.f1n.spaceinator.game.World;
import au.com.f1n.spaceinator.game.WorldBlackHole;
import au.com.f1n.spaceinator.game.enemy.PEnemy;
import au.com.f1n.spaceinator.game.enemy.PWormEnemy;

/**
 * All enemies
 */
public class ObjectDraw {
	public static final int LIFE = 10;
	private static final int N_LASER_BLOB = 14;

	private FloatBuffer mFVertexBuffer[];
	private ShortBuffer mDrawBytes[];
	private ByteBuffer colours[];
	private World world;

	private FloatBuffer laserMeshBuffer;
	private ByteBuffer[] laserColourBuffer;

	private int nPlasma = 33;
	private FloatBuffer plasmaMeshBuffer;
	private FloatBuffer plasmaTextureBuffer;
	private ByteBuffer plasmaColourBuffer;
	private byte[] plasmaColour;
	private int[] plasmaTexture = new int[1];
	// private byte[] tmpCol = new byte[4];

	private FloatBuffer blackHoleBuffer;
	private FloatBuffer blackHoleTexture;
	private float[] blackHoleTextureArray;
	private int[] blackHoleTex;
	private float lastX;
	private float lastY;

	public ObjectDraw(Context context, World world, Nebula nebula) {
		this.world = world;
		int[] enemies = new int[] { R.raw.enemy0, R.raw.enemy1, R.raw.enemy_tank, R.raw.enemy_fighter, R.raw.enemy_spike, R.raw.enemy_bomb, R.raw.enemy_missile,
				R.raw.enemy_wormhead, R.raw.enemy_wormbody, R.raw.enemy_wormtail, R.raw.enemy_dart, R.raw.enemy_dart1, R.raw.enemy_dart2, R.raw.astr0, R.raw.astr1,
				R.raw.astr2, R.raw.astr3 };

		mFVertexBuffer = new FloatBuffer[enemies.length];
		mDrawBytes = new ShortBuffer[enemies.length];
		colours = new ByteBuffer[enemies.length];

		for (int i = 0; i < enemies.length; i++) {
			MeshReader meshReader = new MeshReader(context.getResources().openRawResource(enemies[i]), false);

			mFVertexBuffer[i] = meshReader.getVertices();
			mDrawBytes[i] = meshReader.getDrawOrders();
			colours[i] = meshReader.getVertexColours();
		}

		// Create the plasma
		plasmaTextureBuffer = Util.makeFloatBuffer(nPlasma * 2);
		plasmaTextureBuffer.put(0.5f);
		plasmaTextureBuffer.put(0.5f);
		plasmaMeshBuffer = Util.makeFloatBuffer(nPlasma * 3);
		plasmaMeshBuffer.put(0);
		plasmaMeshBuffer.put(0);
		plasmaMeshBuffer.put(0);
		plasmaColour = new byte[nPlasma * 4];
		plasmaColourBuffer = ByteBuffer.allocateDirect(nPlasma * 4);
		plasmaColourBuffer.position(0);

		for (int i = 1; i < nPlasma; i++) {
			double theta = Math.PI * 2 * (i - 1) / (nPlasma - 2);
			plasmaMeshBuffer.put((float) Math.cos(theta));
			plasmaMeshBuffer.put((float) Math.sin(theta));
			plasmaTextureBuffer.put((float) Math.cos(theta) * .5f + .5f);
			plasmaTextureBuffer.put((float) Math.sin(theta) * .5f + .5f);
			plasmaMeshBuffer.put(0);
		}
		plasmaMeshBuffer.position(0);
		plasmaTextureBuffer.position(0);

		reloadTexture(context);

		if (world instanceof WorldBlackHole) {
			MeshReader meshReader = new MeshReader(context.getResources().openRawResource(R.raw.black_hole), true);
			blackHoleBuffer = meshReader.getTriangles();
			blackHoleTexture = meshReader.getTextCoords();
			blackHoleTex = nebula.textures;

			blackHoleTextureArray = new float[blackHoleTexture.capacity()];
			blackHoleTexture.get(blackHoleTextureArray);
			blackHoleTexture.position(0);
		}

		// Create the laser particle mesh
		laserColourBuffer = new ByteBuffer[PWeaponParticle.ALL_COL.length];
		for (int i = 0; i < laserColourBuffer.length; i++) {
			laserColourBuffer[i] = ByteBuffer.allocateDirect(4 * N_LASER_BLOB);
			laserColourBuffer[i].position(0);

			byte[] col = PWeaponParticle.ALL_COL[i];
			col[3] = (byte) 255;
			laserColourBuffer[i].put(col);
			col[3] = (byte) 0;
			for (int j = 1; j < N_LASER_BLOB; j++)
				laserColourBuffer[i].put(col);
			laserColourBuffer[i].position(0);
		}
		laserMeshBuffer = Util.makeFloatBuffer(3 * N_LASER_BLOB);

		float s = 70;
		// float z = -100;
		// First coordinate
		laserMeshBuffer.put(0);
		laserMeshBuffer.put(0.35f * s);
		laserMeshBuffer.put(0);

		// Wrap around, CCW
		laserMeshBuffer.put(0);
		laserMeshBuffer.put(1f * s);
		laserMeshBuffer.put(0);

		laserMeshBuffer.put(-.3f * s);
		laserMeshBuffer.put(0.91f * s);
		laserMeshBuffer.put(0);

		laserMeshBuffer.put(-0.55f * s);
		laserMeshBuffer.put(0.62f * s);
		laserMeshBuffer.put(0);

		laserMeshBuffer.put(-.64f * s);
		laserMeshBuffer.put(0.27f * s);
		laserMeshBuffer.put(0);

		laserMeshBuffer.put(-.5f * s);
		laserMeshBuffer.put(-0.58f * s);
		laserMeshBuffer.put(0);

		laserMeshBuffer.put(-.27f * s);
		laserMeshBuffer.put(-1.66f * s);
		laserMeshBuffer.put(0);

		laserMeshBuffer.put(0);
		laserMeshBuffer.put(-2.8f * s);
		laserMeshBuffer.put(0);

		laserMeshBuffer.put(.27f * s);
		laserMeshBuffer.put(-1.66f * s);
		laserMeshBuffer.put(0);

		laserMeshBuffer.put(.5f * s);
		laserMeshBuffer.put(-0.58f * s);
		laserMeshBuffer.put(0);

		laserMeshBuffer.put(.64f * s);
		laserMeshBuffer.put(0.27f * s);
		laserMeshBuffer.put(0);

		laserMeshBuffer.put(0.55f * s);
		laserMeshBuffer.put(0.62f * s);
		laserMeshBuffer.put(0);

		laserMeshBuffer.put(.3f * s);
		laserMeshBuffer.put(0.91f * s);
		laserMeshBuffer.put(0);

		laserMeshBuffer.put(0);
		laserMeshBuffer.put(1f * s);
		laserMeshBuffer.put(0);

		laserMeshBuffer.position(0);
	}

	public void draw(float scaleFactor) {
		// Draw objects
		GLES10.glDisable(GL10.GL_TEXTURE_2D);

		Object[] objects = world.getObjects();
		int nObject = world.getObjectSize();

		if (objects != null && nObject > 0) {
			int i = 0;
			PObject pObject = (PObject) objects[i];
			// Draw black hole first (will always be first on the list and there
			// can only be one)
			if (pObject.drawOrder == PBlackHole.DRAW_ORDER) {
				PBlackHole blackHole = (PBlackHole) pObject;

				if (blackHole.radius > 1) {
					// Draw a really large halo
					GLES10.glDisableClientState(GLES10.GL_COLOR_ARRAY);
					GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, plasmaMeshBuffer);
					GLES10.glColor4f(0, 0, 0, 1);
					GLES10.glPushMatrix();
					float scale = Math.max(200f, blackHole.radius);
					GLES10.glScalef(scale, scale, scale);
					GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, nPlasma);
					GLES10.glEnable(GLES10.GL_TEXTURE_2D);
					GLES10.glEnableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
					GLES10.glVertexPointer(3, GLES11.GL_FLOAT, 0, blackHoleBuffer);
					GLES10.glColor4f(1, 1, 1, 1);
					GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, blackHoleTex[0]);

					float dx = (world.getCentreSpaceShip().x - lastX) * .0002f;
					float dy = (world.getCentreSpaceShip().y - lastY) * .0002f;

					for (int t = 0; t < blackHoleTextureArray.length; t += 2) {
						blackHoleTextureArray[t] += dx;
						blackHoleTextureArray[t + 1] += dy;
					}

					lastX = world.getCentreSpaceShip().x;
					lastY = world.getCentreSpaceShip().y;

					blackHoleTexture.put(blackHoleTextureArray);
					blackHoleTexture.position(0);

					GLES10.glTexCoordPointer(2, GLES10.GL_FLOAT, 0, blackHoleTexture);
					GLES10.glDrawArrays(GLES10.GL_TRIANGLES, 0, blackHoleBuffer.capacity() / 3);
					GLES10.glPopMatrix();

					GLES10.glDisable(GLES10.GL_TEXTURE_2D);
				}
				// Go to the next object...
				i++;
			}

			// Increment over the space ship and planets
			while (i < nObject && ((PObject) objects[i]).drawOrder == PSpaceShip.DRAW_ORDER)
				i++;

			// Ready to draw enemies
			GLES10.glEnableClientState(GLES10.GL_COLOR_ARRAY);

			int curDraw = -1;
			ShortBuffer curDrawBytes = null;
			int curDrawByteN = 0;
			for (; i < nObject; i++) {
				pObject = (PObject) objects[i];
				if (!(pObject instanceof PEnemy))
					// Finished enemies!
					break;

				if (curDraw != pObject.drawOrder) {
					curDraw = pObject.drawOrder;
					// New type of object to draw
					GLES10.glVertexPointer(3, GLES11.GL_FLOAT, 0, mFVertexBuffer[curDraw]);
					GLES10.glColorPointer(4, GLES10.GL_UNSIGNED_BYTE, 0, colours[curDraw]);
					curDrawBytes = mDrawBytes[curDraw];
					curDrawByteN = curDrawBytes.capacity();
				}

				PEnemy pEnemy = (PEnemy) pObject;
				// GLES10.glColor4f(1, 1, 1, pEnemy.warpZ < 1 ? .34f : 1);

				// Draw the current loaded enemy
				GLES10.glPushMatrix();
				GLES10.glTranslatef(pEnemy.x, pEnemy.y, pEnemy.z + (1f - pEnemy.warpZ) * -PEnemy.BASE_WARP);
				GLES10.glRotatef(pEnemy.facingAngle * Util.RAD_TO_DEG - 90, 0, 0, 1);
				GLES10.glRotatef(pEnemy.yRot, 0, 1, 0);
				float scale = pEnemy.scale;
				GLES10.glScalef(scale, scale, scale);

				GLES10.glDrawElements(GLES10.GL_TRIANGLES, curDrawByteN, GLES10.GL_UNSIGNED_SHORT, curDrawBytes);

				if (pEnemy instanceof PWormEnemy && ((PWormEnemy) pEnemy).isShield()) {
					// Draw a red worm shield
					plasmaCol((byte) 246, (byte) 0, (byte) 0, (byte) 0, (byte) 246, (byte) 0, (byte) 0, (byte) 255);

					GLES10.glColorPointer(4, GLES10.GL_UNSIGNED_BYTE, 0, plasmaColourBuffer);
					GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, plasmaMeshBuffer);

					GLES10.glScalef(pEnemy.radius, pEnemy.radius, pEnemy.radius);
					GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, nPlasma);

					// Reinstate the enemy class
					GLES10.glVertexPointer(3, GLES11.GL_FLOAT, 0, mFVertexBuffer[curDraw]);
					GLES10.glColorPointer(4, GLES10.GL_UNSIGNED_BYTE, 0, colours[curDraw]);
				}

				GLES10.glPopMatrix();
			}

			if (i < nObject && objects[i] instanceof PWeaponParticle) {
				// There are lasers to draw
				GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, laserMeshBuffer);

				for (; i < nObject; i++) {
					pObject = (PObject) objects[i];
					if (!(pObject instanceof PWeaponParticle))
						// Finished lasers!
						break;

					PWeaponParticle particle = (PWeaponParticle) pObject;

					GLES10.glColorPointer(4, GLES10.GL_UNSIGNED_BYTE, 0, laserColourBuffer[particle.getColour()]);
					GLES10.glPushMatrix();
					GLES10.glTranslatef(particle.x, particle.y, particle.z);
					GLES10.glRotatef(particle.getAngleDegrees() - 90, 0, 0, 1);
					float scale = particle.getScale();
					GLES10.glScalef(scale, scale, scale);
					GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, N_LASER_BLOB);
					GLES10.glPopMatrix();
				}
			}

			// Draw plasmas last
			if (i < nObject && objects[i] instanceof PPlasmaParticle) {
				// There are lasers to draw
				GLES10.glEnable(GLES10.GL_TEXTURE_2D);
				GLES10.glEnableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
				GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, plasmaTexture[0]);
				GLES10.glTexCoordPointer(2, GLES10.GL_FLOAT, 0, plasmaTextureBuffer);
				GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, plasmaMeshBuffer);

				for (; i < nObject; i++) {
					pObject = (PObject) objects[i];
					// No need to check if last things are plasmas
					// if (!(pObject instanceof PPlasmaParticle))
					// // Finished plasmas!
					// break;

					PPlasmaParticle plasma = (PPlasmaParticle) pObject;

					if (plasma.isFriendly())
						plasmaCol((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 12, (byte) 246, (byte) 66, (byte) (255 * plasma.getAmt()));
					else
						plasmaCol((byte) 255, (byte) 255, (byte) 255, (byte) (255 * plasma.getAmt()), (byte) 246, (byte) 66, (byte) 12,
								(byte) (255 * plasma.getAmt()));

					GLES10.glColorPointer(4, GLES10.GL_UNSIGNED_BYTE, 0, plasmaColourBuffer);
					GLES10.glPushMatrix();
					GLES10.glTranslatef(plasma.x, plasma.y, 0);
					GLES10.glScalef(plasma.radius, plasma.radius, plasma.radius);

					GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, nPlasma);
					GLES10.glPopMatrix();
				}
				GLES10.glDisable(GLES10.GL_TEXTURE_2D);
			}
		}
	}

	private void plasmaCol(byte r, byte g, byte b, byte alpha, byte r2, byte g2, byte b2, byte alpha2) {
		plasmaColour[0] = r;
		plasmaColour[1] = g;
		plasmaColour[2] = b;
		plasmaColour[3] = alpha;
		for (int j = 1; j < nPlasma; j++) {
			plasmaColour[j * 4 + 0] = r2;
			plasmaColour[j * 4 + 1] = g2;
			plasmaColour[j * 4 + 2] = b2;
			plasmaColour[j * 4 + 3] = alpha2;
		}
		plasmaColourBuffer.put(plasmaColour);
		plasmaColourBuffer.position(0);
	}

	public void release() {
		GLES10.glDeleteTextures(1, plasmaTexture, 0);
	}

	public void reloadTexture(Context context) {
		// Plasma texture
		Util.createTexture(context, R.drawable.plasma, plasmaTexture);
	}
}