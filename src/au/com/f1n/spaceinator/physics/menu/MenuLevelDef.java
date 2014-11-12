package au.com.f1n.spaceinator.physics.menu;

import au.com.f1n.spaceinator.game.World;

public class MenuLevelDef {

	private int starIndex;
	private Class<? extends World> worldClass;
	private String label;
	private float height;
	private int[] starScores;
	private int creditScale;
	private int galaxy;
	private int id;

	public MenuLevelDef(int id, int galaxy, int starIndex, Class<? extends World> worldClass, String label, float height, int creditScale, int[] starScores) {
		this.id = id;
		this.galaxy = galaxy;
		this.starIndex = starIndex;
		this.worldClass = worldClass;
		this.label = label;
		this.height = height;
		this.starScores = starScores;
		this.creditScale = creditScale;
	}

	public int getStarIndex() {
		return starIndex;
	}

	public Class<? extends World> getWorldClass() {
		return worldClass;
	}

	public String getLabel() {
		return label;
	}

	public float getHeight() {
		return height;
	}

	public int[] getStarScores() {
		return starScores;
	}

	public int getCreditScale() {
		return creditScale;
	}

	public int getGalaxy() {
		return galaxy;
	}

	public int getId() {
		return id;
	}
}
