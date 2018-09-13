package hgeom.hmesh.test;

import java.util.Arrays;
import java.util.StringTokenizer;

/**
 *
 * @author Pierre B.
 */
final class OffImporter extends Importer {

	/**
	 *
	 */
	private enum State {

	/**
	 *
	 */
	VOID,

	/**
	 *
	 */
	READ_COORDS,

	/**
	 *
	 */
	READ_FACES,
	}

	/**
	 *
	 */
	private State state = State.VOID;

	/**
	 *
	 */
	private int numCoords;

	/**
	 *
	 */
	private int numFaces;

	/**
	 * @param line
	 */
	@Override
	protected void readLine(String line) {
		StringTokenizer tokenizer = new StringTokenizer(line);
		int numTokens = tokenizer.countTokens();
		String[] tokens = new String[numTokens];
		Arrays.setAll(tokens, i -> tokenizer.nextToken());

		if (state == State.VOID) {
			readLine(tokens);
		}

		else if (state == State.READ_COORDS) {
			readCoords(tokens);
		}

		else if (state == State.READ_FACES) {
			readFace(tokens);
		}
	}

	/**
	 * @param tokens
	 */
	private void readLine(String[] tokens) {
		if (tokens.length == 3) {
			numCoords = Integer.parseInt(tokens[0]);
			numFaces = Integer.parseInt(tokens[1]);
			state = State.READ_COORDS;
		}
	}

	/**
	 * @param tokens
	 */
	private void readCoords(String[] tokens) {
		if (tokens.length == 3) {
			double x = Double.parseDouble(tokens[0]);
			double y = Double.parseDouble(tokens[1]);
			double z = Double.parseDouble(tokens[2]);
			addCoord(new double[] { x, y, z });

			if (--numCoords == 0) {
				state = State.READ_FACES;
			}
		}
	}

	/**
	 * @param tokens
	 */
	private void readFace(String[] tokens) {
		int size = tokens.length - 1;
		int[] face = new int[size];
		Arrays.setAll(face, i -> Integer.parseInt(tokens[i + 1]));
		addFace(face);

		if (--numFaces == 0) {
			state = State.VOID;
		}
	}
}
