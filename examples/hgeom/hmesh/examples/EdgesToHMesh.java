package hgeom.hmesh.examples;

import java.util.Optional;

import hgeom.hmesh.elements.Coord2DSource;
import hgeom.hmesh.elements.EdgeSource;
import hgeom.hmesh.elements.HMesh2D;

/**
 *
 * @author Pierre Beylot
 */
public final class EdgesToHMesh {

	/**
	 *
	 */
	private EdgesToHMesh() {
	}

	/**
	 * Build a half-edge data structure from an array of edges and an array of
	 * 2D vertex coordinates
	 *
	 * @param edges
	 * @param vertexCoords
	 *
	 * @return
	 */
	public static Optional<HMesh2D> createHMesh2D(int[][] edges,
			double[][] vertexCoords) {

		// Create edge source
		EdgeSource edgeSource = new EdgeSource(edges);

		// Create vertex coordinates source
		Coord2DSource coord2DSource = new Coord2DSource(vertexCoords);

		// Build the half-edge data structure from the faces and the coordinates
		return edgeSource.toHMesh(coord2DSource);
	}
}
