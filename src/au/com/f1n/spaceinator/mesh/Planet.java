package au.com.f1n.spaceinator.mesh;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import android.content.Context;
import android.opengl.GLES10;
import au.com.f1n.spaceinator.Util;
import au.com.f1n.spaceinator.game.PPlanet;

public class Planet extends Mesh {
	private FloatBuffer m_VertexData;
	private FloatBuffer m_NormalData;
	private FloatBuffer m_TextureData;

	private FloatBuffer mHaloVertexData;
	private ByteBuffer mHaloColourData;

	private int m_Stacks;
	private int m_Slices;
	private int[] textures = new int[1];
	private PPlanet pPlanet;
	private float x;

	public Planet(PPlanet planet, int stacks, int slices, float squash, Context context) {
		this.m_Stacks = stacks;
		this.m_Slices = slices;
		this.pPlanet = planet;
		float[] vertexData;
		float[] normalData;
		float[] textData = null;

		int vIndex = 0; // vertex index
		int nIndex = 0; // normal index
		int tIndex = 0; // texture index

		reloadTexture(context);

		m_Stacks = stacks;
		m_Slices = slices;

		// Vertices
		vertexData = new float[3 * ((m_Slices * 2 + 2) * m_Stacks)];

		// Normal pointers for lighting
		normalData = new float[3 * ((m_Slices * 2 + 2) * m_Stacks)];
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
				float theta = (float) (2.0f * (float) Math.PI * ((float) thetaIdx) * (1.0 / (float) (m_Slices - 1)));
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

				// Normal pointers for lighting

				normalData[nIndex + 0] = (float) (cosPhi0 * cosTheta);
				normalData[nIndex + 2] = cosPhi0 * sinTheta;
				normalData[nIndex + 1] = sinPhi0;

				// Get x-y-z for the first vertex of stack N.
				normalData[nIndex + 3] = cosPhi1 * cosTheta;
				normalData[nIndex + 5] = cosPhi1 * sinTheta;
				normalData[nIndex + 4] = sinPhi1;

				if (textData != null) // 4
				{
					float texX = (float) thetaIdx * (1.0f / (float) (m_Slices - 1));
					textData[tIndex + 0] = texX;
					textData[tIndex + 1] = (float) (phiIdx + 0) * (1.0f / (float) (m_Stacks));
					textData[tIndex + 2] = texX;
					textData[tIndex + 3] = (float) (phiIdx + 1) * (1.0f / (float) (m_Stacks));
				}

				vIndex += 2 * 3;
				nIndex += 2 * 3;

				if (textData != null) // 5
					tIndex += 2 * 2;

				// Degenerate triangle to connect stacks and maintain
				// winding order.

				vertexData[vIndex + 0] = vertexData[vIndex + 3] = vertexData[vIndex - 3];
				vertexData[vIndex + 1] = vertexData[vIndex + 4] = vertexData[vIndex - 2];
				vertexData[vIndex + 2] = vertexData[vIndex + 5] = vertexData[vIndex - 1];

				normalData[nIndex + 0] = normalData[nIndex + 3] = normalData[nIndex - 3];
				normalData[nIndex + 1] = normalData[nIndex + 4] = normalData[nIndex - 2];
				normalData[nIndex + 2] = normalData[nIndex + 5] = normalData[nIndex - 1];

				if (textData != null) {
					textData[tIndex + 0] = textData[tIndex + 2] = textData[tIndex - 2];
					textData[tIndex + 1] = textData[tIndex + 3] = textData[tIndex - 1];
				}
			}
		}

		m_VertexData = Util.makeFloatBuffer(vertexData);
		m_NormalData = Util.makeFloatBuffer(normalData);

		if (textData != null)
			m_TextureData = Util.makeFloatBuffer(textData);

		// Make the halo
		if (planet.getHaloInner() != null) {

			float[] vertices = new float[slices * 3];
			byte[] colours = new byte[slices * 4];
			colours[0] = planet.getHaloInner()[0];
			colours[1] = planet.getHaloInner()[1];
			colours[2] = planet.getHaloInner()[2];
			colours[3] = planet.getHaloInner()[3];

			for (int i = 1; i < slices; i++) {
				double angle = Math.PI * 2 * i / (slices - 2);
				vertices[i * 3 + 0] = (float) (Math.cos(angle) * planet.radius * planet.getHaloProportion());
				vertices[i * 3 + 1] = (float) (Math.sin(angle) * planet.radius * planet.getHaloProportion());
				colours[i * 4 + 0] = planet.getHaloOuter()[0];
				colours[i * 4 + 1] = planet.getHaloOuter()[1];
				colours[i * 4 + 2] = planet.getHaloOuter()[2];
				colours[i * 4 + 3] = planet.getHaloOuter()[3];
			}

			mHaloVertexData = Util.makeFloatBuffer(vertices);
			mHaloColourData = Util.makeByteBuffer(colours);
		}
	}

	@Override
	public void draw(float scaleFactor) {
		GLES10.glPushMatrix();
		GLES10.glTranslatef(pPlanet.x, pPlanet.y, 0);
		GLES10.glRotatef(x, 0, 0, 1);
		x += .04;

		// Draw the halo
		if (mHaloColourData != null) {
			GLES10.glDisableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
			GLES10.glDisableClientState(GLES10.GL_NORMAL_ARRAY);
			GLES10.glEnableClientState(GLES10.GL_COLOR_ARRAY);
			GLES10.glDisable(GLES10.GL_LIGHTING);
			GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, mHaloVertexData);
			GLES10.glColorPointer(4, GLES10.GL_UNSIGNED_BYTE, 0, mHaloColourData);
			GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, m_Slices);
		}

		GLES10.glEnable(GLES10.GL_LIGHTING);
		GLES10.glEnableClientState(GLES10.GL_NORMAL_ARRAY);
		GLES10.glDisableClientState(GLES10.GL_COLOR_ARRAY);

		if (m_TextureData != null) {
			GLES10.glEnable(GLES10.GL_TEXTURE_2D); // 1
			GLES10.glEnableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
			GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, textures[0]);
			GLES10.glTexCoordPointer(2, GLES10.GL_FLOAT, 0, m_TextureData);
		}

		GLES10.glMatrixMode(GLES10.GL_MODELVIEW);
		GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, m_VertexData);
		GLES10.glNormalPointer(GLES10.GL_FLOAT, 0, m_NormalData);
		GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 0, (m_Slices + 1) * 2 * (m_Stacks - 1) + 2);

		GLES10.glDisable(GLES10.GL_TEXTURE_2D);
		GLES10.glDisableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);

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