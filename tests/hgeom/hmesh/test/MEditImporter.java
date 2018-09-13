package hgeom.hmesh.test;

import java.util.Arrays;
import java.util.StringTokenizer;

/**
 *
 * @author Pierre B.
 */
final class MEditImporter extends Importer {

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
	READ_NUM_DIMENSIONS,

	/**
	 *
	 */
	READ_NUM_COORDS,

	/**
	 *
	 */
	READ_COORDS,

	/**
	 *
	 */
	READ_NUM_TRIANGLES,

	/**
	 *
	 */
	READ_TRIANGLES,

	/**
	 *
	 */
	READ_NUM_TETRAHEDRA,

	/**
	 *
	 */
	READ_TETRAHEDRA,

	/**
	 *
	 */
	READ_NUM_QUADRILATERALS,

	/**
	 *
	 */
	READ_QUADRILATERALS,

	/**
	 *
	 */
	READ_NUM_HEXAHEDRA,

	/**
	 *
	 */
	READ_HEXAHEDRA
	}

	/**
	 *
	 */
	private State state = State.VOID;

	/**
	 *
	 */
	private int numDimensions;

	/**
	 *
	 */
	private int counter;

	/**
	 * @param line
	 */
	@Override
	protected void readLine(String line) {
		StringTokenizer tokenizer = new StringTokenizer(line);
		String[] tokens = new String[tokenizer.countTokens()];
		Arrays.setAll(tokens, i -> tokenizer.nextToken());

		if (state == State.VOID) {
			readLine(tokens);
		}

		else if (state == State.READ_NUM_DIMENSIONS) {
			readNumDimensions(tokens);
		}

		else if (state == State.READ_NUM_COORDS) {
			readNumCoords(tokens);
		}

		else if (state == State.READ_COORDS) {
			readCoords(tokens);
		}

		else if (state == State.READ_NUM_TRIANGLES) {
			readNumTriangles(tokens);
		}

		else if (state == State.READ_TRIANGLES) {
			readTriangle(tokens);
		}

		else if (state == State.READ_NUM_QUADRILATERALS) {
			readNumQuadrilaterals(tokens);
		}

		else if (state == State.READ_QUADRILATERALS) {
			readQuadrilateral(tokens);
		}

		else if (state == State.READ_NUM_TETRAHEDRA) {
			readNumTetrahedra(tokens);
		}

		else if (state == State.READ_TETRAHEDRA) {
			readTetrahedra(tokens);
		}

		else if (state == State.READ_NUM_HEXAHEDRA) {
			readNumHexahedra(tokens);
		}

		else if (state == State.READ_HEXAHEDRA) {
			readHexahedra(tokens);
		}
	}

	/**
	 * @param tokens
	 */
	private void readLine(String[] tokens) {
		if (tokens.length == 1) {
			if ("Dimension".equals(tokens[0])) {
				state = State.READ_NUM_DIMENSIONS;
			}

			else if ("Vertices".equals(tokens[0])) {
				state = State.READ_NUM_COORDS;
			}

			else if ("Triangles".equals(tokens[0])) {
				state = State.READ_NUM_TRIANGLES;
			}

			else if ("Tetrahedra".equals(tokens[0])) {
				state = State.READ_NUM_TETRAHEDRA;
			}

			else if ("Quadrilaterals".equals(tokens[0])) {
				state = State.READ_NUM_QUADRILATERALS;
			}

			else if ("Hexahedra".equals(tokens[0])) {
				state = State.READ_NUM_HEXAHEDRA;
			}
		}
	}

	/**
	 * @param tokens
	 */
	private void readNumDimensions(String[] tokens) {
		if (tokens.length == 1) {
			numDimensions = Integer.parseInt(tokens[0]);
			state = State.VOID;
		}
	}

	/**
	 * @param tokens
	 */
	private void readNumCoords(String[] tokens) {
		if (tokens.length == 1) {
			counter = Integer.parseInt(tokens[0]);
			state = State.READ_COORDS;
		}
	}

	/**
	 * @param tokens
	 */
	private void readCoords(String[] tokens) {
		if (tokens.length >= 2) {
			double x = Double.parseDouble(tokens[0]);
			double y = Double.parseDouble(tokens[1]);

			if (numDimensions == 2) {
				addCoord(new double[] { x, y });
			}

			else if (numDimensions == 3) {
				double z = Double.parseDouble(tokens[2]);
				addCoord(new double[] { x, y, z });
			}

			if (--counter == 0) {
				state = State.VOID;
			}
		}
	}

	/**
	 * @param tokens
	 */
	private void readNumTriangles(String[] tokens) {
		if (tokens.length == 1) {
			counter = Integer.parseInt(tokens[0]);
			state = State.READ_TRIANGLES;
		}
	}

	/**
	 * @param tokens
	 */
	private void readTriangle(String[] tokens) {
		if (addFace(tokens, 3) && --counter == 0) {
			state = State.VOID;
		}
	}

	/**
	 * @param tokens
	 */
	private void readNumQuadrilaterals(String[] tokens) {
		if (tokens.length == 1) {
			counter = Integer.parseInt(tokens[0]);
			state = State.READ_QUADRILATERALS;
		}
	}

	/**
	 * @param tokens
	 */
	private void readQuadrilateral(String[] tokens) {
		if (addFace(tokens, 4) && --counter == 0) {
			state = State.VOID;
		}
	}

	/**
	 * @param tokens
	 */
	private void readNumTetrahedra(String[] tokens) {
		if (tokens.length == 1) {
			counter = Integer.parseInt(tokens[0]);
			state = State.READ_TETRAHEDRA;
		}
	}

	/**
	 * @param tokens
	 */
	private void readTetrahedra(String[] tokens) {
		if (tokens.length >= 4) {
			int[] indices = new int[4];
			Arrays.setAll(indices, i -> Integer.parseInt(tokens[i]) - 1);
			addFace(indices[1], indices[2]);
			addFace(indices[0], indices[2]);
			addFace(indices[0], indices[1]);
			addFace(indices[0], indices[1]);

			if (--counter == 0) {
				state = State.VOID;
			}
		}
	}

	/**
	 * @param tokens
	 */
	private void readNumHexahedra(String[] tokens) {
		if (tokens.length == 1) {
			counter = Integer.parseInt(tokens[0]);
			state = State.READ_HEXAHEDRA;
		}
	}

	/**
	 * Faire comme pour le tetraedre
	 *
	 * @param tokens
	 */
	private void readHexahedra(String[] tokens) {
		if (addFace(tokens, 8) && --counter == 0) {
			state = State.VOID;
		}
	}

	/**
	 * @param tokens
	 * @param numVertices
	 * @return
	 */
	private boolean addFace(String[] tokens, int numVertices) {
		if (tokens.length < numVertices) {
			return false;
		}

		int[] face = new int[numVertices];
		Arrays.setAll(face, i -> Integer.parseInt(tokens[i]) - 1);
		addFace(face);
		return true;
	}
}
