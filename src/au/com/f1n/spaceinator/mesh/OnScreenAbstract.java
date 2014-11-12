package au.com.f1n.spaceinator.mesh;

import android.content.Context;
import au.com.f1n.spaceinator.GBGLSurfaceView;
import au.com.f1n.spaceinator.game.World;

public abstract class OnScreenAbstract {
	public static final float INDICATOR_PAD = .01f;
	public static final float INDICATOR_HEIGHT = .04f;
	protected TextDrawer textDraw;
	protected int screenWidth;
	protected int screenHeight;
	protected World world;
	protected GBGLSurfaceView view;
	protected float curPad;
	protected float curIndicatorHeight;
	protected Context context;

	public OnScreenAbstract(Context context, int screenWidth, int screenHeight, World world, GBGLSurfaceView view) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.world = world;
		this.view = view;
		this.context = context;
		curPad = screenHeight * INDICATOR_PAD;
		curIndicatorHeight = screenHeight * INDICATOR_HEIGHT;
	}

	public abstract void draw(float scaleFactor, boolean zooming);

	public void setTextDraw(TextDrawer textDraw) {
		this.textDraw = textDraw;
	}

	public boolean click(float x, float y) {
		return false;
	}

	public boolean pressBack() {
		return false;
	}

	public void maxZoomAlpha() {
	}

	public abstract void release();

	public abstract boolean isEndSequence();

	public void reloadTexture(Context context) {
	}
}
