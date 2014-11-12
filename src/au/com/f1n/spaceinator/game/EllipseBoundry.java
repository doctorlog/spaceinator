package au.com.f1n.spaceinator.game;

public class EllipseBoundry extends PolyBoundry {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int N = 65;

	public EllipseBoundry(float width, float height) {
		super(new int[] { N });

		for (int i = 0; i < N; i++) {
			double angle = Math.PI * 2 * i / (N - 1);
			addPoint((float) Math.cos(angle) * width, (float) Math.sin(angle) * height);
		}
	}
}
