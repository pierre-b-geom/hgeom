package hgeom.hmesh.winding;

import java.util.function.IntToDoubleFunction;

/**
 * Winding of a polygon
 *
 * @author Pierre B.
 */
public enum Winding {

	/**
	 *
	 */
	CLOCKWISE {

		@Override
		public Winding reverse() {
			return COUNTERCLOCKWISE;
		}
	},

	/**
	 *
	 */
	COUNTERCLOCKWISE {

		@Override
		public Winding reverse() {
			return CLOCKWISE;
		}
	},

	/**
	 *
	 */
	UNDETERMINED {

		@Override
		public Winding reverse() {
			return UNDETERMINED;
		}
	};

	/**
	 * @return
	 */
	public abstract Winding reverse();

	/**
	 * Calculate the winding of a 2D polygon
	 *
	 * @param numVertices    the number of vertices composing the polygon
	 * @param vertexIndexToX a function giving the x coordinate of a vertice
	 *                       according to its local index in the polygon
	 * @param vertexIndexToY a function giving the y coordinate of a vertice
	 *                       according to its local index in the polygon
	 * @return the winding
	 */
	public static Winding ofPolygon2D(int numVertices,
			IntToDoubleFunction vertexIndexToX,
			IntToDoubleFunction vertexIndexToY) {

		if (numVertices < 3) {
			throw new IllegalArgumentException(
					"insufficient n. of points: " + numVertices);
		}

		double signedArea = 0;

		for (int i = 0; i < numVertices; i++) {
			double x0 = vertexIndexToX.applyAsDouble(i);
			double y0 = vertexIndexToY.applyAsDouble(i);
			int nextI = (i + 1) % numVertices;
			double x1 = vertexIndexToX.applyAsDouble(nextI);
			double y1 = vertexIndexToY.applyAsDouble(nextI);
			signedArea += y0 * x1 - x0 * y1;
		}

		// Epsilon ?
		if (signedArea > 0) {
			return Winding.CLOCKWISE;
		}

		if (signedArea < 0) {
			return Winding.COUNTERCLOCKWISE;
		}

		return Winding.UNDETERMINED;
	}
}
