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
	double vertexX(HVertex v);

	/**
	 * Sets the x coordinate of the given vertex
	 *
	 * @param v
	 * @param x the x coordinate to be set
	 */
	void setVertexX(HVertex v, double x);

	/**
	 * Gets the y coordinate of the given vertex
	 *
	 * @param v
	 * @return the y coordinate
	 */
	double vertexY(HVertex v);

	/**
	 * Sets the y coordinate of the given vertex
	 *
	 * @param v
	 * @param y the y coordinate to be set
	 */
	void setVertexY(HVertex v, double y);

	/**
	 * Gets the 2D coordinates of the given vertex
	 *
	 * @param v
	 * @return an array [x y] containing the coordinates
	 */
	default double[] vertexXY(HVertex v) {
		return vertexXY(v, null);
	}

	/**
	 * Gets the coordinates of the given vertex
	 *
	 * @param v
	 * @param xy if not {@code null}, will contain the 2D coordinates
	 * @return an array [x y] containing the coordinates
	 */
	double[] vertexXY(HVertex v, double[] xy);

	/**
	 * Sets the coordinate of the given vertex
	 *
	 * @param v
	 * @param xy the coordinates to be set
	 */
	void setVertexXY(HVertex v, double[] xy);

	/**
	 * Sets the coordinate of the given vertex
	 *
	 * @param v
	 * @param x the x coordinate to be set
	 * @param y the y coordinate to be set
	 */
	void setVertexXY(HVertex v, double x, double y);

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
		setVertexXY(v, x, y);
		return v;
	}
}
