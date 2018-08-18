package hgeom.hmesh.elements;

import hgeom.hmesh.data.HDData;

/**
 * A half-edge data structure in the 3D space. Each vertex of the structure has
 * 3D coordinates
 *
 * @author Pierre B.
 */
public interface HMesh3D extends HMesh {

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
	 * Gets the {@link HDData} containing the z coordinates of the vertices of
	 * this mesh
	 *
	 * @return
	 */
	HDData<HVertex> vertexZs();

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
	 * Gets the z coordinate of the given vertex
	 *
	 * @param v
	 * @return the y coordinate
	 */
	double vertexZ(HVertex v);

	/**
	 * Sets the z coordinate of the given vertex
	 *
	 * @param v
	 * @param y the y coordinate to be set
	 */
	void setVertexZ(HVertex v, double y);

	/**
	 * Gets the coordinates of the given vertex
	 *
	 * @param v
	 * @return an array [x y z] containing the coordinates
	 */
	default double[] vertexXYZ(HVertex v) {
		return vertexXYZ(v, null);
	}

	/**
	 * Gets the coordinates of the given vertex
	 *
	 * @param v
	 * @param xyz if not {@code null}, will contain the coordinates
	 * @return an array [x y z] containing the coordinates
	 */
	double[] vertexXYZ(HVertex v, double[] xyz);

	/**
	 * Sets the coordinate of the given vertex
	 *
	 * @param v
	 * @param xyz the coordinates to be set
	 */
	void setVertexXYZ(HVertex v, double[] xyz);

	/**
	 * Sets the coordinate of the given vertex
	 *
	 * @param v
	 * @param x the x coordinate to be set
	 * @param y the y coordinate to be set
	 * @param z the y coordinate to be set
	 */
	void setVertexXYZ(HVertex v, double x, double y, double z);

	/**
	 * Calls {@link HMesh#splitEdge(HEdge)} to split the specified half-edge;
	 * then sets the specified coordinates to the vertex created by the split
	 *
	 * @param edge
	 * @param x    a x coordinate for the new vertex
	 * @param y    a y coordinate for the new vertex
	 * @param z    a z coordinate for the new vertex
	 * @return the new vertex created by the split. Never {@code null}.
	 */
	default HVertex splitEdge(HEdge edge, double x, double y, double z) {
		HVertex v = splitEdge(edge);
		setVertexXYZ(v, x, y, z);
		return v;
	}
}
