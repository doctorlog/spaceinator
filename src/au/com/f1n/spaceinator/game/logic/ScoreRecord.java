package au.com.f1n.spaceinator.game.logic;

import java.io.Serializable;
import java.util.Arrays;

public class ScoreRecord implements Serializable {
	private static final long serialVersionUID = 1L;
	private long time;
	private int score;
	private int[] kills;
	private int[] upgrades;
	private int deathCount;

	public ScoreRecord(long time, int score, int[] kills, int[] upgrades, int deathCount) {
		super();
		this.time = time;
		this.score = score;
		this.kills = kills;
		this.upgrades = Arrays.copyOf(upgrades, upgrades.length);
		this.deathCount = deathCount;
	}

	public long getTime() {
		return time;
	}

	public int getScore() {
		return score;
	}

	public int[] getKills() {
		return kills;
	}

	public int[] getUpgrades() {
		return upgrades;
	}

	public int getDeathCount() {
		return deathCount;
	}

	// public String printScores() {
	// return time + "," + score + "," + deathCount + "," + arrayString(upgrades)
	// + "," + arrayString(kills) + "\n";
	// }

	// private String arrayString(int[] arr) {
	// String s = Arrays.toString(arr);
	// return s.substring(1, s.length() - 1);
	// }
}
