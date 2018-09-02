package hgeom.hmesh.winding;

/**
 * A polygon winding provider
 */
@FunctionalInterface
public interface PolygonWindingProvider {

	/**
	 * Calculates and returns the winding of a polygon specified as a set of
	 * indices to vertices
	 *
	 * @param vertexIndices
	 *            the set of indices to vertices defining the polygon
	 * @return the winding of the specified polygon
	 */
	Winding get(int[] vertexIndices);

	/**
	 * @return
	 */
	default PolygonWindingProvider reverse() {
		return vertexIndices -> get(vertexIndices).reverse();
	}
}