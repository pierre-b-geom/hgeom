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
	 * Gets the z coordinate of the given vertex
	 *
	 * @param v
	 * @return the y coordinate
	 */
	double z(HVertex v);

	/**
	 * Sets the z coordinate of the given vertex
	 *
	 * @param v
	 * @param y the y coordinate to be set
	 */
	void setZ(HVertex v, double y);

	/**
	 * Gets the coordinates of the given vertex
	 *
	 * @param v
	 * @return an array [x y z] containing the coordinates
	 */
	default double[] xyz(HVertex v) {
		return xyz(v, null);
	}

	/**
	 * Gets the coordinates of the given vertex
	 *
	 * @param v
	 * @param xyz if not {@code null}, will contain the coordinates
	 * @return an array [x y z] containing the coordinates
	 */
	double[] xyz(HVertex v, double[] xyz);

	/**
	 * Sets the coordinate of the given vertex
	 *
	 * @param v
	 * @param xyz the coordinates to be set
	 */
	void setXYZ(HVertex v, double[] xyz);

	/**
	 * Sets the coordinate of the given vertex
	 *
	 * @param v
	 * @param x the x coordinate to be set
	 * @param y the y coordinate to be set
	 * @param z the y coordinate to be set
	 */
	void setXYZ(HVertex v, double x, double y, double z);

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
		setX(v, x);
		setY(v, y);
		setZ(v, z);
		return v;
	}
}
