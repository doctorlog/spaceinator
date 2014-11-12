package au.com.f1n.spaceinator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import au.com.f1n.spaceinator.game.World;
import au.com.f1n.spaceinator.mesh.OnScreen;
import au.com.f1n.spaceinator.physics.menu.MenuWorld;

@SuppressLint("NewApi")
public class GBGLSurfaceView extends GLSurfaceView {
	public static final float PRESS_BUFFER = 20f;
	public static final float MAX_SCALE = 2;
	public static final float MIN_SCALE = 0.5f;
	private GBRenderer mGBRenderer;
	private float accelXInit;
	private float accelYInit;
	private float fireXInit;
	private float fireYInit;
	private float accelDist;
	private float fireDist;
	private int accelIndex = -1;
	private int fireIndex = -1;
	private float zoomStart;
	private int zoomIndex = -1;
	private float oldScale;
	private float radBonus;
	private int adHeight;

	public GBGLSurfaceView(Context context, AttributeSet attr) {
		super(context, attr);

		if (isInEditMode()) {
			mGBRenderer = null;
			return;
		}

		// if older, crappier devices cant support 32-bit colour, stuff them!
		setEGLConfigChooser(8, 8, 8, 8, 16, 0);

		this.mGBRenderer = new GBRenderer(context, this);
		setRenderer(mGBRenderer);
		setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

		// This code causes fixes the immersive mode bug
		((GBActivity) context).getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
			@SuppressLint("InlinedApi")
			@Override
			public void onSystemUiVisibilityChange(int visibility) {
				if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
					setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		World world = mGBRenderer.getWorld();
		int actionMasked = e.getActionMasked();

		if (world == null || mGBRenderer.isWaitTap()) {
			// We're doing a loading sequence
			if (actionMasked == MotionEvent.ACTION_DOWN || actionMasked == MotionEvent.ACTION_POINTER_DOWN)
				mGBRenderer.worldClick(e.getX(), e.getY());
			return false;
		}

		float dx = 0;
		float dy = 0;
		float angle = 0;
		int w = getWidth();
		int h = getHeight();
		int actionIndex = e.getActionIndex();

		if (world instanceof MenuWorld || mGBRenderer.isWaitTap()) {
			// Menu code - let the render deal with working out the click
			// coordinates
			if (actionMasked == MotionEvent.ACTION_DOWN || actionMasked == MotionEvent.ACTION_POINTER_DOWN)
				mGBRenderer.worldClick(e.getX(), e.getY());
		} else {
			// Independent of any controls, did the user press something in the
			// plasma/shield area?
			if (shieldLoc(e.getX(), e.getY()))
				// shield regen
				world.fireShieldRegen();
			else if (plasmaLoc(e.getX(), e.getY()))
				// plasma
				world.firePlasma();

			if (actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_POINTER_UP || actionMasked == MotionEvent.ACTION_CANCEL) {
				if (actionIndex == e.findPointerIndex(accelIndex)) {
					// Accelerator off
					world.getCentreSpaceShip().setAccelOff();
					accelIndex = -1;
				} else if (actionIndex == e.findPointerIndex(fireIndex)) {
					// Stop firing
					fireIndex = -1;
					world.getCentreSpaceShip().setShootingOff();
				} else if (actionIndex == e.findPointerIndex(zoomIndex)) {
					zoomIndex = -1;
				}
			} else if (actionMasked == MotionEvent.ACTION_DOWN || actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
				int curIndex = actionIndex;

				if (e.getX() > w / 4 && e.getX() < w * 3 / 4 && e.getY(curIndex) > h * (1 - 2 * (OnScreen.INDICATOR_HEIGHT + OnScreen.INDICATOR_PAD))) {
					zoomIndex = curIndex;
					zoomStart = e.getX();
					oldScale = mGBRenderer.getScale();
				} else if (e.getX(curIndex) < w / 2) {
					accelIndex = curIndex;
					accelXInit = e.getX(curIndex);
					accelYInit = e.getY(curIndex);
				} else if (!world.getCentreSpaceShip().isWeaponless()) {
					fireIndex = curIndex;
					fireXInit = e.getX(curIndex);
					fireYInit = e.getY(curIndex);
				}
			} else {
				for (int i = 0; i < e.getPointerCount(); i++) {
					// Independent of any controls, did the user press something in
					// the
					// plasma/shield area?
					if (shieldLoc(e.getX(i), e.getY(i)))
						// shield regen
						world.fireShieldRegen();
					else if (plasmaLoc(e.getX(i), e.getY(i)))
						// plasma
						world.firePlasma();

					if (i == e.findPointerIndex(accelIndex)) {
						// Change acceleration vector
						dx = e.getX(i) - accelXInit;
						dy = accelYInit - e.getY(i);

						accelDist = (float) Math.sqrt(dx * dx + dy * dy);
						angle = (float) Math.atan2(dy, dx);

						if (accelDist > PRESS_BUFFER) {
							world.getCentreSpaceShip().setAccel(dx / accelDist, dy / accelDist, angle);
						} else {
							world.getCentreSpaceShip().setAccelOff();
						}
					} else if (i == e.findPointerIndex(fireIndex)) {
						// Change shooting vector
						dx = fireXInit - e.getX(i);
						dy = e.getY(i) - fireYInit;

						fireDist = (float) Math.sqrt(dx * dx + dy * dy);
						angle = (float) Math.atan2(-dy, -dx);

						if (fireDist > PRESS_BUFFER) {
							world.getCentreSpaceShip().shootAt(angle);
						} else {
							world.getCentreSpaceShip().setShootingOff();
						}
					} else if (i == e.findPointerIndex(zoomIndex)) {
						// This means you can do one unit of zoom per 1/4 of screen
						float scale = oldScale + 4 * (zoomStart - e.getX(i)) / w;
						if (scale > MAX_SCALE)
							scale = MAX_SCALE;
						if (scale < MIN_SCALE)
							scale = MIN_SCALE;

						mGBRenderer.setScale(scale);
					}
				}
			}
		}

		return true;
	}

	private boolean shieldLoc(float x, float y) {
		return x * x + y * y < radBonus;
	}

	private boolean plasmaLoc(float x, float y) {
		float dx = getWidth() - x;
		return dx * dx + y * y < radBonus;
	}

	public float getAccelXInit() {
		return accelXInit;
	}

	public float getAccelYInit() {
		return accelYInit;
	}

	public float getFireXInit() {
		return fireXInit;
	}

	public float getFireYInit() {
		return fireYInit;
	}

	public boolean onBackPressed() {
		if (mGBRenderer.getWorld() instanceof MenuWorld) {
			// Do nothing!
			return true;

			// if (mGBRenderer.getOnScreen().pressBack()) {
			// Intent intent = new Intent(Intent.ACTION_MAIN);
			// intent.addCategory(Intent.CATEGORY_HOME);
			// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// context.startActivity(intent);
			// }
		} else {
			mGBRenderer.loadWorld(MenuWorld.class);
			return false;
		}
	}

	public boolean isAccelDown() {
		return accelIndex != -1;
	}

	public boolean isFireDown() {
		return fireIndex != -1;
	}

	@SuppressLint("InlinedApi")
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
				setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

			else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
				setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
		}
	}

	public float getAccelDist() {
		return accelDist;
	}

	public float getFireDist() {
		return fireDist;
	}

	public boolean isZooming() {
		return zoomIndex != -1;
	}

	public GBRenderer getGBRenderer() {
		return mGBRenderer;
	}

	public void setSize(int width, int height) {
		radBonus = height / 4;
		radBonus *= radBonus;
	}

	public int getAdHeight() {
		return adHeight;
	}

	public void setAdHeight(int adHeight) {
		this.adHeight = adHeight;
	}

}