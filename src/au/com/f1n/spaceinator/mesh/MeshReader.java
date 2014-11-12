package au.com.f1n.spaceinator.mesh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import au.com.f1n.spaceinator.Util;

public class MeshReader {
	private FloatBuffer vertices;
	// private FloatBuffer normals;
	private ShortBuffer drawOrder;
	private short[] vertexColours;
	private float[] colour;
	private float[] textCoords;
	private int ti;

	public MeshReader(InputStream inputStream, boolean textured) {
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		int mode = 0;
		int vci = 0;

		try {
			while ((line = br.readLine()) != null) {
				String[] split = line.split(",");
				if (line.startsWith("v")) {
					vertices = Util.makeFloatBuffer(Integer.parseInt(split[1]) * 3);
					mode = 1;
				} else if (line.startsWith("n")) {
					// normals = Util.makeFloatBuffer(Integer.parseInt(split[1]) *
					// 3);
					mode = 2;
				} else if (line.startsWith("d")) {
					drawOrder = Util.makeShortBuffer(Integer.parseInt(split[1]) * 3);
					colour = new float[4];
					colour[0] = Float.parseFloat(split[2]);
					colour[1] = Float.parseFloat(split[3]);
					colour[2] = Float.parseFloat(split[4]);
					colour[3] = 1;
					mode = 3;
				} else if (line.startsWith("u") && textured) {
					textCoords = new float[Integer.parseInt(split[1]) * 2];
					ti = 0;
					mode = 4;
				} else if (line.startsWith("c")) {
					vertexColours = new short[Integer.parseInt(split[1]) * 4];
					vci = 0;
					mode = 5;
				} else {
					switch (mode) {
					case 1:
						vertices.put(Float.parseFloat(split[0]));
						vertices.put(Float.parseFloat(split[1]));
						vertices.put(Float.parseFloat(split[2]));
						break;
					case 2:
						// normals.put(Float.parseFloat(split[0]));
						// normals.put(Float.parseFloat(split[1]));
						// normals.put(Float.parseFloat(split[2]));
						break;
					case 3:
						drawOrder.put(Short.parseShort(split[0]));
						drawOrder.put(Short.parseShort(split[1]));
						drawOrder.put(Short.parseShort(split[2]));
						break;
					case 4:
						textCoords[ti++] = Float.parseFloat(split[0]);
						textCoords[ti++] = Float.parseFloat(split[1]);
						break;
					case 5:
						vertexColours[vci++] = Short.parseShort(split[0]);
						vertexColours[vci++] = Short.parseShort(split[1]);
						vertexColours[vci++] = Short.parseShort(split[2]);
						vertexColours[vci++] = 255;
						break;
					default:
						break;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// public FloatBuffer getNormals() {
	// ShortBuffer drawOrd = drawOrder[0];
	//
	// FloatBuffer norm = Util.makeFloatBuffer(drawOrd.capacity() * 3);
	// drawOrd.position(0);
	// while (drawOrd.hasRemaining()) {
	// int pos = drawOrd.get();
	// norm.put(normals.get(pos * 3 + 0));
	// norm.put(normals.get(pos * 3 + 1));
	// norm.put(normals.get(pos * 3 + 2));
	// }
	// drawOrd.position(0);
	// norm.position(0);
	// return norm;
	// }

	public FloatBuffer getVertices() {
		vertices.position(0);
		return vertices;
	}

	public FloatBuffer getTriangles() {
		FloatBuffer tri = Util.makeFloatBuffer(drawOrder.capacity() * 3);
		drawOrder.position(0);
		while (drawOrder.hasRemaining()) {
			int pos = drawOrder.get();
			tri.put(vertices.get(pos * 3 + 0));
			tri.put(vertices.get(pos * 3 + 1));
			tri.put(vertices.get(pos * 3 + 2));
		}
		drawOrder.position(0);
		tri.position(0);
		return tri;
	}

	public ByteBuffer getVertexColours() {
		ByteBuffer bb = ByteBuffer.allocateDirect(vertices.capacity() * 4 / 3);
		drawOrder.position(0);
		int i = 0;
		while (drawOrder.hasRemaining()) {
			int pos = drawOrder.get();
			bb.put(pos * 4 + 0, (byte) vertexColours[i++]);
			bb.put(pos * 4 + 1, (byte) vertexColours[i++]);
			bb.put(pos * 4 + 2, (byte) vertexColours[i++]);
			bb.put(pos * 4 + 3, (byte) vertexColours[i++]);
		}
		drawOrder.position(0);

		bb.position(0);
		return bb;
	}

	public ShortBuffer getDrawOrders() {
		drawOrder.position(0);
		return drawOrder;
	}

	public float[] getColour() {
		return colour;
	}

	/**
	 * Only call this once since it is reasonably expensive
	 * 
	 * @return
	 */
	public FloatBuffer getTextCoords() {
		return Util.makeFloatBuffer(textCoords);
	}
}
