package au.com.f1n.spaceinator.game.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PolyReader {
	private float[][] points;

	public PolyReader(InputStream inputStream) {
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		try {
			line = br.readLine();
			points = new float[Integer.parseInt(line)][];
			for (int t = 0; t < points.length; t++) {
				line = br.readLine();
				String[] split = line.split(",");
				float p[] = new float[Integer.parseInt(split[1]) * 2 + 2];
				boolean reverse = split[0].charAt(0) == 'r';

				int i = 0;
				int counter = 0;
				if (reverse)
					i = p.length - 2;

				while (counter < p.length - 2) {
					line = br.readLine();
					split = line.split(",");

					p[i] = Float.parseFloat(split[0]);
					p[i + 1] = Float.parseFloat(split[1]);

					if (reverse)
						i -= 2;
					else
						i += 2;
					counter += 2;
				}

				if (reverse) {
					p[0] = p[p.length - 2];
					p[1] = p[p.length - 1];
				} else {
					// this closes the loop
					p[p.length - 2] = p[0];
					p[p.length - 1] = p[1];
				}

				points[t] = p;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public float[][] getPoints() {
		return points;
	}
}
