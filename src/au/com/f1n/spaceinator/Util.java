package au.com.f1n.spaceinator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.text.DecimalFormat;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES10;
import android.opengl.GLUtils;

public class Util {
	public static final float PI = (float) Math.PI;
	public static final float RAD_TO_DEG = 180f / PI;

	public static final DecimalFormat FMT = new DecimalFormat("#.00");
	private static final float[] RAND;
	private static final int RAND_N = 200;
	private static int nextRand;

	static {
		RAND = new float[RAND_N];
		for (int i = 0; i < RAND_N; i++)
			RAND[i] = (float) Math.random();
	}

	public static FloatBuffer makeFloatBuffer(float[] arr) {
		ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		fb.put(arr);
		fb.position(0);
		return fb;
	}

	public static float invSqrt(float x) {
		float xhalf = 0.5f * x;
		int i = Float.floatToIntBits(x);
		i = 0x5f3759df - (i >> 1);
		x = Float.intBitsToFloat(i);
		x = x * (1.5f - xhalf * x * x);
		return x;
	}

	public static ByteBuffer makeByteBuffer(byte[] arr) {
		ByteBuffer bb = ByteBuffer.allocateDirect(arr.length);
		bb.put(arr);
		bb.position(0);
		return bb;
	}

	public static ShortBuffer makeShortBuffer(short[] arr) {
		ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 2);
		bb.order(ByteOrder.nativeOrder());
		ShortBuffer fb = bb.asShortBuffer();
		fb.put(arr);
		fb.position(0);
		return fb;
	}

	public static ByteBuffer makeCastByteBuffer(short[] arr) {
		ByteBuffer bb = ByteBuffer.allocateDirect(arr.length);
		for (short s : arr)
			bb.put((byte) s);
		bb.position(0);
		return bb;
	}

	public static FloatBuffer makeFloatBuffer(int i) {
		ByteBuffer bb = ByteBuffer.allocateDirect(i * 4);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		fb.position(0);
		return fb;
	}

	public static ShortBuffer makeShortBuffer(int i) {
		ByteBuffer bb = ByteBuffer.allocateDirect(i * 2);
		bb.order(ByteOrder.nativeOrder());
		ShortBuffer sb = bb.asShortBuffer();
		sb.position(0);
		return sb;
	}

	public static int createTexture(Context contextRegf, int resource, int[] textures) {
		Bitmap image = BitmapFactory.decodeResource(contextRegf.getResources(), resource);
		if (image == null)
			return 0;
		GLES10.glGenTextures(1, textures, 0);
		GLES10.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, image, 0);

		GLES10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		GLES10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

		image.recycle();
		return resource;
	}

	public static float slowInOut(float v) {
		float v2 = v * v;
		float vm1 = 1 - v;
		return v2 / (v2 + vm1 * vm1);
	}

	public static float slowOut(float v) {
		float vm1 = v - 1;
		return 1 - vm1 * vm1;
	}

	/**
	 * Distance from line segment x1,y2 to x2,y2 with point xp,yp
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param xp
	 * @param yp
	 * @return
	 */
	public static float dist2(float x1, float y1, float x2, float y2, float xp, float yp) {
		float px = x2 - x1;
		float py = y2 - y1;

		float d = px * px + py * py;

		float u = ((xp - x1) * px + (yp - y1) * py) / d;

		if (u > 1)
			u = 1;
		else if (u < 0)
			u = 0;

		float x = x1 + u * px;
		float y = y1 + u * py;

		float dx = x - xp;
		float dy = y - yp;

		return dx * dx + dy * dy;
	}

	public static boolean distLT(float x1, float y1, float x2, float y2, float x3, float y3, float dist) {
		return dist2(x1, y1, x2, y2, x3, y3) <= dist * dist;
	}

	public static String timeFormat(int sec) {
		int sec2 = sec % 60;

		return (sec / 60) + ":" + (sec2 < 10 ? "0" : "") + sec2;
	}

	public static float randFloat() {
		nextRand = (nextRand + 1) % RAND_N;
		return RAND[nextRand];
	}

	/**
	 * This will ensure that the provided string will not cause TextDrawer to
	 * crash
	 * 
	 * @param str
	 * @return
	 */
	public static String happyString(String str) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < str.length(); i++) {
			char cur = str.charAt(i);
			if (cur == ' ' || cur == ':' || cur == '.' || cur == '+' || cur == '$' || cur >= '0' && cur <= '9' || cur >= 'A' && cur <= 'Z')
				sb.append(cur);
			else if (cur >= 'a' && cur <= 'z')
				sb.append((char) (cur - 32));
		}

		return sb.toString();
	}
}
