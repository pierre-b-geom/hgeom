package hgeom.hmesh.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hgeom.hmesh.core.HConversion;
import hgeom.hmesh.core.ToHMeshConverter;
import hgeom.hmesh.elements.Coord2DSource;
import hgeom.hmesh.elements.EdgeSource;
import hgeom.hmesh.elements.FaceSource;
import hgeom.hmesh.elements.HMesh2D;
import hgeom.hmesh.winding.Winding;

/**
 * maillage 2D particulier reposant sur les sommets de coordonnees entieres.
 * Uniquement pour les tests - rend lisible le codage des faces
 *
 * @author Pierre B.
 */
final class Grid {

	/**
	 *
	 */
	private static final int COORDS_GRID_DIM = 10000;

	/**
	 *
	 */
	private static final Coord2DSource COORD2D_SOURCE = new Coord2DSource(
			Grid::vertexX, Grid::vertexY);

	/**
	 *
	 */
	private Grid() {
	}

	/**
	 * @param numI
	 * @param numJ
	 * @return
	 */
	public static HMesh2D mesh(int numI, int numJ) {
		List<int[][]> edges = new ArrayList<>();

		for (int i = 0; i < numI; i++) {
			for (int j = 0; j < numJ; j++) {
				edges.add(new int[][] { v(i, j), v(i + 1, j) });
				edges.add(new int[][] { v(i + 1, j), v(i + 1, j + 1) });
				edges.add(new int[][] { v(i + 1, j + 1), v(i, j + 1) });
				edges.add(new int[][] { v(i, j + 1), v(i, j) });
			}
		}

		return meshFromEdges(edges);
	}

	/**
	 * @param edges
	 * @return
	 */
	public static HMesh2D meshFromEdges(List<int[][]> edges) {
		return meshFromEdges(edges, false);
	}

	/**
	 * @param edges
	 * @param linkEdgesToPrevious
	 * @return
	 */
	public static HMesh2D meshFromEdges(List<int[][]> edges,
			boolean linkEdgesToPrevious) {

		EdgeSource edgeSource = new EdgeSource(i -> vertexIndices(edges.get(i)),
				0, edges.size());

		ToHMeshConverter converter = new ToHMeshConverter(linkEdgesToPrevious,
				true);

		return converter.convert(edgeSource, COORD2D_SOURCE)
				.map(HConversion::mesh)
				.orElseThrow(IllegalArgumentException::new);
	}

	/**
	 * @param faces
	 * @return
	 */
	public static HMesh2D meshFromFaces(List<int[][]> faces) {
		return meshFromFaces(faces.toArray(new int[faces.size()][][]), false);
	}

	/**
	 * @param faces
	 * @return
	 */
	public static HMesh2D meshFromFaces(int[][][] faces) {
		return meshFromFaces(faces, false);
	}

	/**
	 * @param faces
	 * @param linkEdgesToPrevious
	 * @return
	 */
	public static HMesh2D meshFromFaces(int[][][] faces,
			boolean linkEdgesToPrevious) {

		return meshFromFaces(faces, linkEdgesToPrevious,
				Winding.COUNTERCLOCKWISE);
	}

	/**
	 * @param faces
	 * @param linkEdgesToPrevious
	 * @param faceWinding
	 * @return
	 */
	public static HMesh2D meshFromFaces(int[][][] faces,
			boolean linkEdgesToPrevious, Winding faceWinding) {

		FaceSource faceSource = new FaceSource(i -> vertexIndices(faces[i]), 0,
				faces.length);

		ToHMeshConverter converter = new ToHMeshConverter(linkEdgesToPrevious,
				true);

		if (faceWinding == Winding.COUNTERCLOCKWISE) {
			return converter.convert(faceSource, COORD2D_SOURCE)
					.map(HConversion::mesh)
					.orElseThrow(IllegalArgumentException::new);
		}

		return converter
				.convert(faceSource, COORD2D_SOURCE,
						COORD2D_SOURCE.windingProvider().reverse())
				.map(HConversion::mesh)
				.orElseThrow(IllegalArgumentException::new);
	}

	/**
	 * @param vertexCoords
	 * @return
	 */
	private static int[] vertexIndices(int[][] vertexCoords) {
		int[] indices = new int[vertexCoords.length];
		Arrays.setAll(indices, iVertex -> vertexIndex(vertexCoords[iVertex]));
		return indices;
	}

	/**
	 * @param vertexCoords
	 * @return
	 */
	private static int vertexIndex(int[] vertexCoords) {
		return vertexCoords[1] * COORDS_GRID_DIM + vertexCoords[0];
	}

	/**
	 * @param vertexIndex
	 * @return
	 */
	private static double vertexX(int vertexIndex) {
		return vertexIndex % COORDS_GRID_DIM;
	}

	/**
	 * @param vertexIndex
	 * @return
	 */
	private static double vertexY(int vertexIndex) {
		return Math.floor((double) vertexIndex / COORDS_GRID_DIM);
	}

	/**
	 * Pour faciliter lecture du code
	 *
	 * @param x
	 * @param y
	 * @return
	 */
	private static int[] v(int x, int y) {
		return new int[] { x, y };
	}
}
