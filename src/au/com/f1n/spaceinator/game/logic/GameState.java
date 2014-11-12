package au.com.f1n.spaceinator.game.logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.content.pm.PackageManager.NameNotFoundException;
import au.com.f1n.spaceinator.GBActivity;
import au.com.f1n.spaceinator.R;
import au.com.f1n.spaceinator.game.World;
import au.com.f1n.spaceinator.game.enemy.PEnemy;
import au.com.f1n.spaceinator.physics.menu.MenuLevelDef;

import com.google.android.gms.games.Games;

public class GameState implements Serializable {
	private static final int VERSION = 0;

	public static final int UPGRADE_SHIELD = 0;
	public static final int UPGRADE_THRUSTER = 1;
	public static final int UPGRADE_LASER_COUNT = 2;
	public static final int UPGRADE_LASER_POWER = 3;
	public static final int PLASMA_WAVE = 4;
	public static final int SHIELD_CELL = 5;
	public static final int UPGRADE_TURRET = 6;
	public static final int UPGRADE_TURRET_POWER = 7;
	public static final int UPGRADE_TURRET_RATE = 8;
	public static final int UPGRADE_TURRET_SPREAD = 9;

	private static final long serialVersionUID = 1L;
	private static final int NUM_LEVELS = World.getNumLevels();
	private static final String FILENAME = "GameState.dat";

	private int version;
	private ScoreList[] scores;
	private transient GBActivity context;
	private transient boolean firstPlay;
	private transient boolean fullVersion;
	/**
	 * Stars are like money...yay, money
	 */
	private int stars;
	private int credits;
	private int[] shipUpgrades;
	private int shipPaintIndex;
	private int maxLevel;
	private boolean facebook;
	private long lastTweet;

	private boolean samsungFull;

	private int scouts;

	private boolean achieveShip;

	private boolean achieveMax;

	public boolean achievement_milky_way_master;

	public boolean achievement_ultimate_driver;

	public boolean achievement_trigger_happy_40;

	public boolean achievement_store_is_open_10;

	public boolean achievement_maximum_milky_way_100;

	public boolean achievement_shield_celler_50;

	private boolean achievement_big_asteroid_25;

	public boolean achievement_who_did_this_10;

	private boolean achievement_first_one_is_free_10;

	private boolean achievement_turrets_all_round_20;

	public boolean achievement_epic_fail_40;

	public boolean achievement_survivor_50;

	public boolean achievement_planet_basher_25;

	public boolean achievement_additional_andromeda_100;

	public boolean achievement_advanced_andromeda_50;

	private boolean achievement_zero_hero_10;
	private int fighters;

	public static final int UPGRADE_COSTS[][] = { { 20, 70, 120, 200, -1 },// Shield
			{ 45, 95, 190, 270, -1 },// Thruster
			{ 90, 240, 380, -1 },// Laser Count
			{ 40, 100, 180, 200, -1 },// Laser Power
			{ 10, 10, 10, 10, 10, -1 },// Plasma Waves
			{ 12, 12, 12, 12, 12, -1 },// Shield cells
			{ 7, 7, 8, 8, -1 }, // Turrets cost Stars
			{ 5, 10, 13, 20, -1 }, // Turret Power cost Stars
			{ 4, 8, 11, 20, -1 }, // Turret fire rate Stars
			{ 4, 6, 8, 10, -1 } // Turret target spread
	};

	public static final boolean COST_STARS[] = { false, false, false, false, false, false, true, true, true, true };
	public static final String UPGRADE_NAMES[] = { "SHIELD", "ENGINE", "LASERS", "LASER POWER", "PLASMA WAVES", "SHIELD CELLS", "TURRETS", "POWER", "FIRE RATE",
			"SPREAD" };

	public GameState() {
		scores = new ScoreList[NUM_LEVELS];
		shipUpgrades = new int[UPGRADE_NAMES.length];
		version = VERSION;
	}

	public int finishWorld(World world, int deathCount) {
		// Check for achievements...
		if (scouts > 10000) {
			if (achievement(R.string.achievement_scout_crusher_20, 20))
				scouts = -1;
		}
		if (fighters > 1000) {
			if (achievement(R.string.achievement_fighter_fighter_10, 10))
				fighters = -1;
		}

		if (!achievement_big_asteroid_25 && world.getKillArray()[PEnemy.CLASS_ASTEROID_BIG] > 0) {
			achievement_big_asteroid_25 = world.getGameState().achievement(R.string.achievement_big_asteroid_25, 25);
		}

		if (!achievement_zero_hero_10 && world.getScore() == 0) {
			achievement_zero_hero_10 = world.getGameState().achievement(R.string.achievement_zero_hero_10, 10);
		}

		// Do the other stuff...
		ScoreList curList = scores[world.getWorldDef().getId()];
		int oldStars = 0;

		int[] starScores = world.getWorldDef().getStarScores();

		if (curList == null)
			// First time we have completed this level
			scores[world.getWorldDef().getId()] = curList = new ScoreList();
		else
			// We need the old number of stars to display new stars as yellow
			oldStars = starCount(curList.getBest(), starScores);

		stars += curList.addScore(world.getScore(), world.getKillArray(), shipUpgrades, starScores, world.getPlayTime(), deathCount);
		credits += world.getScore() / world.getWorldDef().getCreditScale();

		if (world.getStarIndex() + 1 > maxLevel)
			maxLevel = world.getStarIndex() + 1;

		save();
		return oldStars;
	}

	public boolean achievement(int id, int credits) {
		if (context.isSignedIn()) {
			Games.Achievements.unlock(context.getApiClient(), context.getString(id));
			this.credits += credits;
			context.reloadAchievements();
			return true;
		}
		return false;
	}

	public ScoreList getScoreList(World world) {
		return getScoreList(world.getWorldDef().getId());
	}

	public ScoreList getScoreList(MenuLevelDef def) {
		return getScoreList(def.getId());
	}

	private ScoreList getScoreList(int worldIndex) {
		if (scores.length < NUM_LEVELS) {
			// User updated to a new version that has more levels
			ScoreList[] newScores = new ScoreList[NUM_LEVELS];
			System.arraycopy(scores, 0, newScores, 0, scores.length);
			scores = newScores;
		}

		return scores[worldIndex];
	}

	public void save() {
		try {
			File file = new File(context.getFilesDir(), FILENAME);
			FileOutputStream fout = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(this);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static GameState load(GBActivity context) {
		File file = new File(context.getFilesDir(), FILENAME);
		GameState gameState = null;

		try {
			FileInputStream fin = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fin);
			gameState = (GameState) ois.readObject();
			ois.close();
		} catch (Exception e) {
			// First time run...
		}
		if (gameState == null) {
			// First time we have run...
			gameState = new GameState();
			gameState.firstPlay = true;
		} else {
			gameState.firstPlay = false;
		}
		gameState.context = context;
		// gameState.credits = 4000;
		// gameState.stars = 180;
		// for (int i = 0; i < gameState.shipUpgrades.length; i++)
		// gameState.shipUpgrades[i] = 0;
		// gameState.maxLevel = 22;

		return gameState;
	}

	public void updateFullVersion() {
		fullVersion = samsungFull;
		if (fullVersion)
			return;
		// Does the user have the unlock installed?
		android.content.pm.PackageManager mPm = context.getPackageManager();
		try {
			fullVersion = mPm.getPackageInfo("au.com.f1n.spaceinatorunlock", 0) != null;
		} catch (NameNotFoundException e) {
			// No full version. That's ok, were in demo mode
		}
	}

	/**
	 * Simple method to count how many stars a certain score would be worth
	 * 
	 * @param score
	 * @param starScores
	 * @return
	 */
	public static int starCount(int score, int[] starScores) {
		int max = 0;
		for (int i = 0; i < starScores.length; i++)
			if (score >= starScores[i])
				max = i + 1;

		return max;
	}

	public int getStars() {
		return stars;
	}

	public int getCredits() {
		return credits;
	}

	public int[] getShipUpgrades() {
		if (shipUpgrades == null || shipUpgrades.length != UPGRADE_NAMES.length)
			shipUpgrades = new int[UPGRADE_NAMES.length];

		return shipUpgrades;
	}

	public int upgradeCount() {
		return shipUpgrades[0] + shipUpgrades[1] + shipUpgrades[2] + shipUpgrades[3];
	}

	public boolean buyUpgrade(int upgradeIndex) {
		getShipUpgrades();

		// java.util.Arrays.fill(shipUpgrades, 0);
		int cost = UPGRADE_COSTS[upgradeIndex][shipUpgrades[upgradeIndex]];

		if (COST_STARS[upgradeIndex]) {
			if (shipUpgrades[UPGRADE_TURRET] == 0 && upgradeIndex != UPGRADE_TURRET)
				return false;

			// buy in *
			if (cost != -1 && stars >= cost) {
				shipUpgrades[upgradeIndex]++;
				stars -= cost;

				if (!achievement_turrets_all_round_20 && shipUpgrades[UPGRADE_TURRET] == 4)
					achievement_turrets_all_round_20 = achievement(R.string.achievement_turrets_all_round_20, 20);

				save();
				return true;
			}
			// else if (cost == -1) {
			// shipUpgrades[upgradeIndex] = 0;
			// }
		} else if (cost != -1 && credits >= cost) {
			// buy in $
			shipUpgrades[upgradeIndex]++;
			credits -= cost;

			if (!achieveShip && shipUpgrades[UPGRADE_LASER_COUNT] > 0 && shipUpgrades[UPGRADE_LASER_POWER] > 0 && shipUpgrades[UPGRADE_SHIELD] > 0
					&& shipUpgrades[UPGRADE_THRUSTER] > 0) {
				achieveShip = true;
				achievement(R.string.achievement_better_ship_10, 10);
			}

			if (!achieveMax && shipUpgrades[UPGRADE_LASER_COUNT] == UPGRADE_COSTS[UPGRADE_LASER_COUNT].length - 1
					&& shipUpgrades[UPGRADE_LASER_POWER] == UPGRADE_COSTS[UPGRADE_LASER_POWER].length - 1
					&& shipUpgrades[UPGRADE_SHIELD] == UPGRADE_COSTS[UPGRADE_SHIELD].length - 1
					&& shipUpgrades[UPGRADE_THRUSTER] == UPGRADE_COSTS[UPGRADE_THRUSTER].length - 1) {
				achieveMax = true;
				achievement(R.string.achievement_ultimate_ship_1, 1);
			}

			save();
			return true;
		}

		// stars++;
		// credits += 1000;
		// maxLevel = 22;
		// samsungFull = false;
		// facebook = false;
		// lastTweet = 0;
		return false;
	}

	/**
	 * Spend a plasma - note we don't save for performance reasons
	 * 
	 * @return
	 */
	public boolean spendPlasma() {
		if (shipUpgrades[PLASMA_WAVE] > 0) {
			shipUpgrades[PLASMA_WAVE]--;
			return true;
		}
		return false;
	}

	public int getShipPaintIndex() {
		return shipPaintIndex;
	}

	/**
	 * This costs money if there is a chnage in the paintwork
	 * 
	 * @param shipPaintIndex
	 */
	public boolean setShipPaintIndex(int shipPaintIndex) {
		if (this.shipPaintIndex == shipPaintIndex)
			return false;

		if (credits >= 10) {
			if (!achievement_first_one_is_free_10) {
				achievement_first_one_is_free_10 = achievement(R.string.achievement_first_one_is_free_10, 10);
			}

			credits -= 10;
			this.shipPaintIndex = shipPaintIndex;
			save();
			return true;
		}

		return false;
	}

	public boolean spendShieldCell() {
		if (shipUpgrades[SHIELD_CELL] > 0) {
			shipUpgrades[SHIELD_CELL]--;
			return true;
		}
		return false;
	}

	public void bonusUpgrades() {
		shipUpgrades[PLASMA_WAVE]++;
		shipUpgrades[SHIELD_CELL]++;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	// public void sendScores(Context context) {
	// int i = 0;
	// String body = "";
	// for (ScoreList sl : scores) {
	// if (sl != null) {
	// body += "Records for " + i + ": " + findName(i) + "\n";
	// body += sl.printScores();
	// }
	// i++;
	// }
	//
	// Intent intent = new Intent(Intent.ACTION_SEND);
	// intent.setType("message/rfc822");
	// intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "doctorlog@gmail.com"
	// });
	// intent.putExtra(Intent.EXTRA_SUBJECT, "Spaceinator Scores");
	// intent.putExtra(Intent.EXTRA_TEXT, body);
	// try {
	// context.startActivity(Intent.createChooser(intent, "Send mail..."));
	// } catch (android.content.ActivityNotFoundException ex) {
	// Toast.makeText(context, "There are no email clients installed.",
	// Toast.LENGTH_SHORT).show();
	// }
	// }

	/**
	 * This is a bit lame but wont be in the final release
	 * 
	 * @param i
	 * @return
	 */
	// private String findName(int i) {
	// for (MenuLevelDef d : World.STARS) {
	// if (d.getId() == i)
	// return d.getLabel();
	// }
	// return "?";
	// }

	public boolean isFirstPlay() {
		if (firstPlay) {
			firstPlay = false;
			return true;
		}
		return false;
	}

	public boolean faceBookLike() {
		if (facebook == false) {
			facebook = true;
			credits += 100;
			save();
			return true;
		}
		return false;
	}

	public boolean tweet() {
		if (tweetReady()) {
			credits += 20;
			lastTweet = System.currentTimeMillis();
			save();
			return true;
		}
		return false;
	}

	public boolean tweetReady() {
		return maxLevel > 3 && (lastTweet == 0 || System.currentTimeMillis() - lastTweet > 1000 * 60 * 60 * 24);
	}

	public boolean fbReady() {
		return maxLevel > 3 && !facebook;
	}

	public boolean isFullVersion() {
		return fullVersion;
	}

	public int getVersion() {
		return version;
	}

	public void setSamsungPurchase() {
		samsungFull = fullVersion = true;
	}

	public void killScout() {
		if (scouts >= 0)
			scouts++;
	}

	public void killFighter() {
		if (fighters >= 0)
			fighters++;
	}
}
