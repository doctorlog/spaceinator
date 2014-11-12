package au.com.f1n.spaceinator.mesh;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import android.content.Context;
import android.opengl.GLES10;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.PPlanet;

public class Sun extends Mesh {
	private FloatBuffer m_VertexData;
	private FloatBuffer m_TextureData;

	private FloatBuffer mHaloVertexData;
	private ByteBuffer mHaloColourData;
	private ByteBuffer mHaloMiniColourData;

	private int m_Stacks;
	private int m_Slices;
	private int[] textures = new int[1];
	float[] textData;
	private PPlanet pPlanet;
	private float x;
	private float[] haloVertices;
	private boolean[] dHaloVertices;
	private int iter;

	public Sun(PPlanet planet, int stacks, int slices, float squash, Context context) {
		this.m_Stacks = stacks;
		this.m_Slices = slices;
		this.pPlanet = planet;
		float[] vertexData;
		textData = null;

		int vIndex = 0; // vertex index
		int tIndex = 0; // texture index

		reloadTexture(context);

		m_Stacks = stacks;
		m_Slices = slices;

		// Vertices

		vertexData = new float[3 * ((m_Slices * 2 + 2) * m_Stacks)];
		textData = new float[2 * ((m_Slices * 2 + 2) * (m_Stacks))];

		int phiIdx, thetaIdx;

		// Latitude

		for (phiIdx = 0; phiIdx < m_Stacks; phiIdx++) {
			// Starts at -1.57 and goes up to +1.57 radians.

			// /The first circle.

			float phi0 = (float) Math.PI * ((float) (phiIdx + 0) * (1.0f / (float) (m_Stacks)) - 0.5f);

			// The next, or second one.
			float phi1 = (float) Math.PI * ((float) (phiIdx + 1) * (1.0f / (float) (m_Stacks)) - 0.5f);

			float cosPhi0 = (float) Math.cos(phi0);
			float sinPhi0 = (float) Math.sin(phi0);
			float cosPhi1 = (float) Math.cos(phi1);
			float sinPhi1 = (float) Math.sin(phi1);

			float cosTheta, sinTheta;

			// Longitude

			for (thetaIdx = 0; thetaIdx < m_Slices; thetaIdx++) {
				// Increment along the longitude circle each "slice."
				float theta = (float) (2.0f * (float) Math.PI * ((float) thetaIdx / (float) (m_Slices - 1)));
				cosTheta = (float) Math.cos(theta);
				sinTheta = (float) Math.sin(theta);

				// We're generating a vertical pair of points, such
				// as the first point of stack 0 and the first point of
				// stack 1 above it. This is how TRIANGLE_STRIPS work,
				// taking a set of 4 vertices and essentially drawing two
				// triangles at a time. The first is v0-v1-v2, and the next
				// is v2-v1-v3, etc.

				// Get x-y-z for the first vertex of stack.

				float radius = planet.radius;
				vertexData[vIndex] = radius * cosPhi0 * cosTheta;
				vertexData[vIndex + 1] = radius * (sinPhi0 * squash);
				vertexData[vIndex + 2] = radius * (cosPhi0 * sinTheta);

				vertexData[vIndex + 3] = radius * cosPhi1 * cosTheta;
				vertexData[vIndex + 4] = radius * (sinPhi1 * squash);
				vertexData[vIndex + 5] = radius * (cosPhi1 * sinTheta);

				if (textData != null) // 4
				{
					float texX = (float) thetaIdx * (1.0f / (float) (m_Slices - 1));
					textData[tIndex + 0] = texX;
					textData[tIndex + 1] = (float) (phiIdx + 0) * (1.0f / (float) (m_Stacks));
					textData[tIndex + 2] = texX;
					textData[tIndex + 3] = (float) (phiIdx + 1) * (1.0f / (float) (m_Stacks));
				}

				vIndex += 2 * 3;

				if (textData != null) // 5
					tIndex += 2 * 2;

				// Degenerate triangle to connect stacks and maintain
				// winding order.

				vertexData[vIndex + 0] = vertexData[vIndex + 3] = vertexData[vIndex - 3];
				vertexData[vIndex + 1] = vertexData[vIndex + 4] = vertexData[vIndex - 2];
				vertexData[vIndex + 2] = vertexData[vIndex + 5] = vertexData[vIndex - 1];

				if (textData != null) {
					textData[tIndex + 0] = textData[tIndex + 2] = textData[tIndex - 2];
					textData[tIndex + 1] = textData[tIndex + 3] = textData[tIndex - 1];
				}
			}
		}

		m_VertexData = Util.makeFloatBuffer(vertexData);

		if (textData != null)
			m_TextureData = Util.makeFloatBuffer(textData.length);

		// Make the halo
		if (planet.getHaloInner() != null) {
			haloVertices = new float[m_Stacks * 3];
			dHaloVertices = new boolean[m_Stacks];
			byte[] colours = new byte[m_Stacks * 4];
			colours[0] = planet.getHaloInner()[0];
			colours[1] = planet.getHaloInner()[1];
			colours[2] = planet.getHaloInner()[2];
			colours[3] = planet.getHaloInner()[3];

			for (int i = 1; i < m_Stacks; i++) {
				double angle = Math.PI * 2 * i / (m_Stacks - 2);
				float r = planet.radius * (0.995f + Util.randFloat() * .001f) * planet.getHaloProportion();
				haloVertices[i * 3 + 0] = (float) (Math.cos(angle) * r);
				haloVertices[i * 3 + 1] = (float) (Math.sin(angle) * r);
				haloVertices[i * 3 + 2] = -10;
				dHaloVertices[i] = Util.randFloat() > .5;
				colours[i * 4 + 0] = planet.getHaloOuter()[0];
				colours[i * 4 + 1] = planet.getHaloOuter()[1];
				colours[i * 4 + 2] = planet.getHaloOuter()[2];
				colours[i * 4 + 3] = planet.getHaloOuter()[3];
			}

			haloVertices[1 * 3 + 0] = haloVertices[m_Stacks * 3 - 3];
			haloVertices[1 * 3 + 1] = haloVertices[m_Stacks * 3 - 2];
			dHaloVertices[1] = dHaloVertices[m_Stacks - 1];

			mHaloVertexData = Util.makeFloatBuffer(haloVertices);
			mHaloColourData = Util.makeByteBuffer(colours);

			colours[0] = (byte) 255;
			colours[1] = (byte) 255;
			colours[2] = (byte) 255;
			colours[3] = (byte) 255;

			for (int i = 1; i < m_Stacks; i++) {
				colours[i * 4 + 0] = (byte) 255;
				colours[i * 4 + 1] = (byte) 255;
				colours[i * 4 + 2] = (byte) 255;
				colours[i * 4 + 3] = (byte) 0;
			}
			mHaloMiniColourData = Util.makeByteBuffer(colours);
		}
	}

	@Override
	public void draw(float scaleFactor) {
		GLES10.glPushMatrix();
		GLES10.glTranslatef(pPlanet.x, pPlanet.y, 0);

		GLES10.glDisable(GLES10.GL_TEXTURE_2D); // 1

		GLES10.glPushMatrix();
		GLES10.glRotatef(x * 30, 0, 0, 1);
		iter++;
		dHaloVertices[iter % m_Stacks] = !dHaloVertices[iter % m_Stacks];

		dHaloVertices[1] = dHaloVertices[m_Stacks - 1];
		for (int i = 1; i < m_Stacks; i++) {
			haloVertices[i * 3 + 0] *= dHaloVertices[i] ? 1.002 : 0.998;
			haloVertices[i * 3 + 1] *= dHaloVertices[i] ? 1.002 : 0.998;
		}
		mHaloVertexData.put(haloVertices);
		mHaloVertexData.position(0);

		float[] col = pPlanet.getSunCol();
		GLES10.glColor4f(col[0], col[1], col[2], col[3]);

		GLES10.glDisableClientState(GLES10.GL_NORMAL_ARRAY);
		GLES10.glEnableClientState(GLES10.GL_COLOR_ARRAY);
		GLES10.glDisable(GLES10.GL_LIGHTING);

		GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, mHaloVertexData);
		GLES10.glColorPointer(4, GLES10.GL_UNSIGNED_BYTE, 0, mHaloColourData);
		GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, m_Stacks);
		GLES10.glPopMatrix();
		GLES10.glDisableClientState(GLES10.GL_COLOR_ARRAY);

		GLES10.glPushMatrix();
		GLES10.glRotatef(270, 1, 0, 0);
		int phiIdx, thetaIdx;

		// Latitude
		int tIndex = 0;
		x += 0.001f;
		for (phiIdx = 0; phiIdx < m_Stacks; phiIdx++) {
			// Looking at stack 1 and 2
			float phi1Y = (float) (phiIdx + 1) * (1.0f / (float) (m_Stacks)) - x;
			float phi0Y = (float) (phiIdx + 0) * (1.0f / (float) (m_Stacks)) - x;

			for (thetaIdx = 0; thetaIdx < m_Slices; thetaIdx++) {
				float texX = (float) thetaIdx * (1.0f / (float) (m_Slices - 1));

				// The point at tIndex
				textData[tIndex + 0] = texX;
				textData[tIndex + 1] = phi0Y;
				textData[tIndex + 2] = texX;
				textData[tIndex + 3] = phi1Y;

				tIndex += 4;
			}
		}

		m_TextureData.put(textData);
		m_TextureData.position(0);

		GLES10.glEnable(GLES10.GL_TEXTURE_2D); // 1
		GLES10.glEnableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
		GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, textures[0]);
		GLES10.glTexCoordPointer(2, GLES10.GL_FLOAT, 0, m_TextureData);

		GLES10.glMatrixMode(GLES10.GL_MODELVIEW);
		GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, m_VertexData);
		GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 0, (m_Slices + 1) * 2 * (m_Stacks - 1) + 2);

		GLES10.glDisable(GLES10.GL_TEXTURE_2D);
		GLES10.glDisableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);

		GLES10.glPopMatrix();

		if (pPlanet.drawMiniHalos()) {
			// Draw the mini white halo
			GLES10.glScalef(.5f, .5f, .5f);
			GLES10.glTranslatef(0, 0, pPlanet.radius);
			GLES10.glEnableClientState(GLES10.GL_COLOR_ARRAY);
			GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, mHaloVertexData);
			GLES10.glColorPointer(4, GLES10.GL_UNSIGNED_BYTE, 0, mHaloMiniColourData);
			GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, m_Stacks);

			// Draw the mini white halo again but smaller
			GLES10.glScalef(.5f, .5f, .5f);
			GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, m_Stacks);
		}

		GLES10.glPopMatrix();
	}

	public void release() {
		GLES10.glDeleteTextures(1, textures, 0);
	}

	@Override
	public void reloadTexture(Context context) {
		Util.createTexture(context, pPlanet.getTextureID(), textures);
	}
}