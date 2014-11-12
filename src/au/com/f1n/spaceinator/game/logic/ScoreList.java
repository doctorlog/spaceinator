package au.com.f1n.spaceinator.game.logic;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Store the list of scores for a particular level
 * 
 * @author luke
 * 
 */

public class ScoreList implements Serializable {
	private static final long serialVersionUID = 1L;
	private ArrayList<ScoreRecord> records;

	private transient int bestScore;

	public ScoreList() {
		records = new ArrayList<ScoreRecord>();
	}

	public int addScore(int score, int[] kills, int[] upgrades, int[] starScores, long time, int deathCount) {
		int oldStars = GameState.starCount(getBest(), starScores);
		int newStars = GameState.starCount(score, starScores);

		records.add(new ScoreRecord(time, score, kills, upgrades, deathCount));

		if (score > bestScore)
			bestScore = score;

		return Math.max(0, newStars - oldStars);
	}

	public int getBest() {
		if (bestScore == 0 && !records.isEmpty()) {
			for (ScoreRecord sr : records)
				if (sr.getScore() > bestScore)
					bestScore = sr.getScore();
		}
		return bestScore;
	}

	// public String printScores() {
	// String ret = "";
	// for (ScoreRecord sr : records)
	// ret += sr.printScores();
	// return ret;
	// }
}
