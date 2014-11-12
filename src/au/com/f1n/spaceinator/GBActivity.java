package au.com.f1n.spaceinator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import au.com.f1n.spaceinator.game.logic.GameState;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;

public class GBActivity extends BaseGameActivity {
	private GBGLSurfaceView mGLSurfaceView;
	private AdView adView;
	private GBRenderer gbRenderer;
	private String playerID;

	// private int img; // Used for videos

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gbactivity);

		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		mGLSurfaceView = (GBGLSurfaceView) findViewById(R.id.gl);
		gbRenderer = mGLSurfaceView.getGBRenderer();
		gbRenderer.setVibrator(v);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mGLSurfaceView.getGBRenderer().paused();
		mGLSurfaceView.onPause();
		if (adView != null)
			adView.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		gbRenderer.onResume();

		if (gbRenderer.getGameState() != null)
			gbRenderer.getGameState().updateFullVersion();

		mGLSurfaceView.onResume();
		if (adView != null) {
			adView.resume();
			adView.bringToFront();
		}
	}

	public void onBackPressed() {
		// This is a bit strange looking but the BaseGameActivity will exit when
		// back is pressed.
		if (mGLSurfaceView.onBackPressed())
			super.onBackPressed();
	}

	@Override
	public void onDestroy() {
		if (adView != null)
			adView.destroy();
		super.onDestroy();
	}

	public void purchase() {
		// Direct to google play
		final String appPackageName = "au.com.f1n.spaceinatorunlock";
		try {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
		} catch (android.content.ActivityNotFoundException anfe) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
		}
	}

	public void loadedGameState(final GameState gameState) {
		if (!gbRenderer.getGameState().isFullVersion() && adView == null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					adView = new AdView(GBActivity.this);
					adView.setAdUnitId("ca-app-pub-4882068263834445/3895146216");
					adView.setAdSize(AdSize.BANNER);

					AdRequest adRequest = new AdRequest.Builder().addTestDevice("5DED1E7137843B74CCEE77401E8CB313").build();
					// AdRequest adRequest = new AdRequest.Builder().build();

					// Load the adView with the ad request.
					adView.loadAd(adRequest);

					FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
					if (frame == null)
						return;

					int xWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 320, getResources().getDisplayMetrics());
					int yWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());

					mGLSurfaceView.setAdHeight(yWidth);

					Button b = new Button(GBActivity.this);
					b.setBackgroundColor(Color.BLACK);
					b.setTextColor(Color.WHITE);
					b.setText("Spaceinator Ads");
					frame.addView(b, new FrameLayout.LayoutParams(xWidth, yWidth, Gravity.TOP | Gravity.CENTER_HORIZONTAL));

					frame.addView(adView,
							new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL));
					adView.bringToFront();
					adView.setVisibility(View.VISIBLE);
				}
			});
		}
	}

	@Override
	public void onSignInFailed() {
	}

	@Override
	public void onSignInSucceeded() {
		// Load achievements if just logged in.
		reloadAchievements();

		playerID = Games.Players.getCurrentPlayerId(getApiClient());
	}

	public boolean isSignedIn() {
		return super.isSignedIn();
	}

	public void signClick() {
		if (super.isSignedIn()) {
			signOut();
		} else {
			beginUserInitiatedSignIn();
		}
	}

	public GoogleApiClient getApiClient() {
		return super.getApiClient();
	}

	public String getPlayerID() {
		return playerID;
	}

	public void reloadAchievements() {
		if (gbRenderer.getGalaxy() != null && isSignedIn())
			Games.Achievements.load(getApiClient(), false).setResultCallback(gbRenderer.getGalaxy());
	}

	// public void screenShot(Bitmap bitmap) {
	// long start = System.currentTimeMillis();
	// // image naming and path to include sd card appending name you choose for
	// // file
	// String zeros = "";
	// if (img < 10)
	// zeros = "000";
	// else if (img < 100)
	// zeros = "00";
	// else if (img < 1000)
	// zeros = "0";
	//
	// String mPath = "/storage/sdcard0/DCIM/100ANDRO/s" + zeros + (img++) +
	// ".jpg";
	//
	// OutputStream fout = null;
	// File imageFile = new File(mPath);
	//
	// try {
	// fout = new FileOutputStream(imageFile);
	// bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fout);
	// fout.flush();
	// fout.close();
	//
	// Log.d("World", mPath + " " + (System.currentTimeMillis() - start) + "ms");
	//
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
}
