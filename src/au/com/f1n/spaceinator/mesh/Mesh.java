package au.com.f1n.spaceinator.mesh;

import android.content.Context;

public abstract class Mesh {
	public abstract void draw(float scaleFactor);

	public abstract void release();

	public abstract void reloadTexture(Context context);
}
