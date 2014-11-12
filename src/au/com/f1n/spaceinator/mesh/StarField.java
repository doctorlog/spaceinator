package au.com.f1n.spaceinator.mesh;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Random;

import android.opengl.GLES10;
import au.com.f1n.spaceinator.Util;

public class StarField {
	private static final int STAR_COUNT = 1000;
	private static final int STAR_SET_COUNT = 4;

	private float[] starSize;
	private FloatBuffer[] m_VertexData;
	private ByteBuffer[] m_ColorData;

	public StarField() {
		starSize = new float[STAR_SET_COUNT];
		m_VertexData = new FloatBuffer[STAR_SET_COUNT];
		m_ColorData = new ByteBuffer[STAR_SET_COUNT];
		for (int s = 0; s < STAR_SET_COUNT; s++) {
			starSize[s] = s + 0.5f;
			m_VertexData[s] = Util.makeFloatBuffer(3 * STAR_COUNT);
			m_ColorData[s] = ByteBuffer.allocateDirect(4 * STAR_COUNT);

			Random r = new Random();

			for (int i = 0; i < STAR_COUNT; i++) {
				double u = -r.nextDouble();
				double theta = r.nextDouble() * 2 * Math.PI;

				double rad = 150000 + 10000 * s;
				double tmp = Math.sqrt(1 - u * u);
				// XVal
				m_VertexData[s].put((float) (rad * tmp * Math.cos(theta)));
				// YVal
				m_VertexData[s].put((float) (rad * tmp * Math.sin(theta)));
				// ZVal
				m_VertexData[s].put((float) (rad * u));

				byte grey = (byte) (255 - 100 * r.nextDouble());

				m_ColorData[s].put(grey);
				m_ColorData[s].put(grey);
				m_ColorData[s].put(grey);
				m_ColorData[s].put((byte) 255);
			}

			m_ColorData[s].position(0);
			m_VertexData[s].position(0);
		}
	}

	public void draw(float scaleFactor, float rotX) {
		GLES10.glDisable(GLES10.GL_TEXTURE_2D);

		GLES10.glEnableClientState(GLES10.GL_COLOR_ARRAY);

		GLES10.glEnable(GLES10.GL_BLEND);

		if (rotX > 0) {
			GLES10.glPushMatrix();
			GLES10.glRotatef(rotX, 1, 0, 0);
		}

		for (int s = 0; s < STAR_SET_COUNT; s++) {
			GLES10.glColorPointer(4, GLES10.GL_UNSIGNED_BYTE, 0, m_ColorData[s]);
			GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, m_VertexData[s]);

			GLES10.glEnable(GLES10.GL_POINT_SMOOTH);
			GLES10.glPointSize(starSize[s] / scaleFactor);

			GLES10.glDrawArrays(GLES10.GL_POINTS, 0, STAR_COUNT);
		}

		if (rotX > 0)
			GLES10.glPopMatrix();
	}
}
