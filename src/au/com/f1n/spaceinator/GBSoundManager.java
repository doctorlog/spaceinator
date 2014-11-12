package au.com.f1n.spaceinator;

import java.io.IOException;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;

public class GBSoundManager {
	private static final int LASER0 = 0;
	private static final int LASER1 = 1;
	private static final int LASER2 = 2;
	private static final int SHIP = 3;
	private static final int HIT = 4;
	private static final int EXPLODE = 5;
	private static final int ALARM = 6;
	private static final int BOUNCE = 7;
	private static final int SCRATCH = 8;
	private static final int FLY_OUT = 9;
	private static final int BEEP = 10;
	private static final int COIN = 11;
	private static final int SHIELD = 12;
	private static final int PLASMA = 13;
	private static final int BEEP_MENU = 14;
	private static final int TELEPORT = 15;
	private static final int N = 16;
	private static final int SHIP_MS = 6000;
	private long lastShip;
	private SoundPool soundPool;
	private int[] soundPoolMap;
	private Context context;
	private int shipSoundID = -1;
	private float shipVol = 0;
	private long lastBounce;
	private long BOUNCE_MS = 1000;

	private MediaPlayer mp;
	private int playing;
	private String curname;

	public GBSoundManager(Context context) {
		this.context = context;
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		soundPoolMap = new int[N];
		soundPoolMap[LASER0] = soundPool.load(context, R.raw.laser0, 1);
		soundPoolMap[LASER1] = soundPool.load(context, R.raw.laser1, 1);
		soundPoolMap[LASER2] = soundPool.load(context, R.raw.laser2, 1);
		soundPoolMap[SHIP] = soundPool.load(context, R.raw.ship0, 1);
		soundPoolMap[HIT] = soundPool.load(context, R.raw.hit0, 1);
		soundPoolMap[EXPLODE] = soundPool.load(context, R.raw.explode, 1);
		soundPoolMap[ALARM] = soundPool.load(context, R.raw.alarm, 1);
		soundPoolMap[BOUNCE] = soundPool.load(context, R.raw.bounce, 1);
		soundPoolMap[SCRATCH] = soundPool.load(context, R.raw.scratch, 1);
		soundPoolMap[FLY_OUT] = soundPool.load(context, R.raw.flyout, 1);
		soundPoolMap[BEEP] = soundPool.load(context, R.raw.beep, 1);
		soundPoolMap[COIN] = soundPool.load(context, R.raw.coin, 1);
		soundPoolMap[SHIELD] = soundPool.load(context, R.raw.alien_scanner, 1);
		soundPoolMap[PLASMA] = soundPool.load(context, R.raw.plasma, 1);
		soundPoolMap[BEEP_MENU] = soundPool.load(context, R.raw.beepmenu, 1);
		soundPoolMap[TELEPORT] = soundPool.load(context, R.raw.teleport_sound, 1);
	}

	public int playSound(int sound, float rate, int priority) {
		AudioManager mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
		float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float volume = streamVolumeCurrent / streamVolumeMax;

		return soundPool.play(soundPoolMap[sound], volume, volume, priority, 0, rate);
	}

	public void laser() {
		playSound((int) (Util.randFloat() * (LASER2 + 1)), Util.randFloat() * 0.2f + 0.9f, 1);
	}

	public void hit() {
		playSound(HIT, Util.randFloat() * 0.2f + 0.9f, 1);
	}

	public void beep() {
		playSound(BEEP, Util.randFloat() * 0.5f + 0.75f, 1);
	}

	public void beepMenu(float rate) {
		playSound(BEEP_MENU, rate, 1);
	}

	public void shield() {
		playSound(SHIELD, Util.randFloat() * 0.4f + 0.8f, 1);
	}

	public void plasma() {
		playSound(PLASMA, Util.randFloat() * 0.4f + 0.8f, 5);
	}

	public void coin() {
		playSound(COIN, 1, 1);
	}

	public void explode() {
		playSound(EXPLODE, 1, 3);
	}

	public void setShip(boolean accelerating) {
		long curTime = System.currentTimeMillis();

		shipVol += accelerating ? .005f : -.005f;
		shipVol = Math.max(0, Math.min(shipVol, .1f));

		if (accelerating && curTime - lastShip > SHIP_MS) {
			// Restart playing
			shipSoundID = playSound(SHIP, 1, 2);
			lastShip = curTime;
		}

		if (shipSoundID != -1)
			soundPool.setVolume(shipSoundID, shipVol, shipVol);

	}

	public void stopShip() {
		soundPool.stop(shipSoundID);
	}

	public void alarm() {
		playSound(ALARM, 1, 3);
	}

	public void bounce(float l, float r) {
		long curTime = System.currentTimeMillis();

		if (curTime - lastBounce > BOUNCE_MS) {
			soundPool.setVolume(playSound(BOUNCE, 1, 2), l, r);
			lastBounce = curTime;
		}

	}

	public void scratch() {
		playSound(SCRATCH, 1, 3);

	}

	public void flyOut() {
		playSound(FLY_OUT, 1, 2);
	}

	public void teleportWorm() {
		playSound(TELEPORT, 1, 3);
	}

	/**
	 * This is a bit different from the rest of the class - play a long,
	 * specified, resource.
	 * 
	 * @param res
	 */
	public void playMusic(int res) {
		if (playing != res) {
			if (mp == null) {
				mp = MediaPlayer.create(context, res);
			} else {
				mp.stop();
				mp.reset();
				try {
					Uri uri = Uri.parse("android.resource://au.com.f1n.spaceinator/" + res);
					mp.setDataSource(context, uri);
					mp.prepare();
				} catch (IllegalArgumentException e) {
				} catch (SecurityException e) {
				} catch (IllegalStateException e) {
				} catch (IOException e) {
				}
			}
			mp.setLooping(true);
			mp.start();

			playing = res;

			if (res == R.raw.music_namaste)
				curname = "& JASON SHAW - NAMASTE ";
			else if (res == R.raw.music_race)
				curname = "& JASON SHAW - VANISHING HORIZON ";
			else if (res == R.raw.music_blackhole)
				curname = "& GOLDEN HITS - JANGLE RHINOCART ";
			else if (res == R.raw.music_boss)
				curname = "& GOLDEN HITS - MORNING MOTION ";
			else if (res == R.raw.music_training)
				curname = "& LANGUIS - ENTERPRISE 1 ";
			else if (res == R.raw.music_warp)
				curname = "& HENRY HOMESWEET - UNTIL I SLEEP ";
			else if (res == R.raw.music_gauntlet)
				curname = "& BROKE FOR FREE - DAY BIRD ";
			else if (res == R.raw.music_myluck)
				curname = "& BROKE FOR FREE - MY LUCK ";
			else if (res == R.raw.music_menu)
				curname = "& AMBIENTEER - ECCLESIA ";
			else if (res == R.raw.music_mantilla)
				curname = "& MOON VEIL - MANTILLA ";
			else if (res == R.raw.music_snakecharm)
				curname = "& MOON VEIL - SNAKE CHARM ";
			else
				curname = "& ";

		}
	}

	// private void stopMusic() {
	// if (mp != null) {
	// playing = -1;
	// mp.stop();
	// mp.reset();
	// mp.release();
	// mp = null;
	// }
	// }

	public void pauseMusic() {
		if (mp != null)
			mp.pause();
	}

	public void resumeMusic() {
		if (mp != null)
			mp.start();
	}

	public String getTrackName() {
		return curname;
	}

}
