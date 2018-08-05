package hgeom.hmesh.elements;

import hgeom.hmesh.data.HDData;

/**
 * A half-edge data structure in the 2D space. Each vertex of the structure has
 * 2D coordinates
 *
 * @author Pierre B.
 */
public interface HMesh2D extends HMesh {

	/**
	 * Gets the {@link HDData} containing the x coordinates of the vertices of
	 * this mesh
	 *
	 * @return
	 */
	HDData<HVertex> vertexXs();

	/**
	 * Gets the {@link HDData} containing the y coordinates of the vertices of
	 * this mesh
	 *
	 * @return
	 */
	HDData<HVertex> vertexYs();

	/**
	 * Gets the x coordinate of the given vertex
	 *
	 * @param v
	 * @return the x coordinate
	 */
	double x(HVertex v);

	/**
	 * Sets the x coordinate of the given vertex
	 *
	 * @param v
	 * @param x the x coordinate to be set
	 */
	void setX(HVertex v, double x);

	/**
	 * Gets the y coordinate of the given vertex
	 *
	 * @param v
	 * @return the y coordinate
	 */
	double y(HVertex v);

	/**
	 * Sets the y coordinate of the given vertex
	 *
	 * @param v
	 * @param y the y coordinate to be set
	 */
	void setY(HVertex v, double y);

	/**
	 * Gets the 2D coordinates of the given vertex
	 *
	 * @param v
	 * @return an array [x y] containing the coordinates
	 */
	default double[] xy(HVertex v) {
		return xy(v, null);
	}

	/**
	 * Gets the coordinates of the given vertex
	 *
	 * @param v
	 * @param xy if not {@code null}, will contain the 2D coordinates
	 * @return an array [x y] containing the coordinates
	 */
	double[] xy(HVertex v, double[] xy);

	/**
	 * Sets the coordinate of the given vertex
	 *
	 * @param v
	 * @param xy the coordinates to be set
	 */
	void setXY(HVertex v, double[] xy);

	/**
	 * Sets the coordinate of the given vertex
	 *
	 * @param v
	 * @param x the x coordinate to be set
	 * @param y the y coordinate to be set
	 */
	void setXY(HVertex v, double x, double y);

	/**
	 * Calls {@link HMesh#splitEdge(HEdge)} to split the specified half-edge;
	 * then sets the specified coordinates to the vertex created by the split
	 *
	 * @param edge the edge to split
	 * @param x    a x coordinate for the new vertex
	 * @param y    a y coordinate for the new vertex
	 * @return the new vertex created by the split. Never {@code null}.
	 */
	default HVertex splitEdge(HEdge edge, double x, double y) {
		HVertex v = splitEdge(edge);
		setX(v, x);
		setY(v, y);
		return v;
	}
}
