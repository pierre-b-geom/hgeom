package hgeom.hmesh.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HMesh;
import hgeom.hmesh.elements.HMesh2D;
import hgeom.hmesh.elements.HMesh3D;
import hgeom.hmesh.elements.HVertex;
import hgeom.hmesh.test.Importer.ImportResult;
import hgeom.hmesh.util.Loops;

/**
 *
 * @author Pierre B.
 */
final class Utils {

	/**
	 *
	 */
	private static final String MEDIT_OUTPUT_DIR = "C:\\Users\\Pierre\\HalfEdge\\exports\\";

	/**
	 *
	 */
	private static final String MEDIT_INPUT_DIR = "C:\\Users\\Pierre\\HalfEdge\\examples\\";

	/**
	 *
	 */
	private Utils() {
	}

	/**
	 * @param fileName
	 * @return
	 */
	public static ImportResult importFromMEditFile(String fileName) {
		return new MEditImporter()
				.importMesh(MEDIT_INPUT_DIR + fileName + ".mesh");
	}

	/**
	 * @param fileName
	 * @return
	 */
	public static ImportResult importFromOffFile(String fileName) {
		return new OffImporter()
				.importMesh(MEDIT_INPUT_DIR + fileName + ".off");
	}

	/**
	 * @param mesh
	 * @param fileName
	 */
	public static void exportToMEditFile(HMesh mesh, String fileName) {
		try {
			if (mesh instanceof HMesh2D) {
				new MEditExporter2D().export((HMesh2D) mesh,
						MEDIT_OUTPUT_DIR + fileName);
			}

			else if (mesh instanceof HMesh3D) {
				new MEditExporter3D().export((HMesh3D) mesh,
						MEDIT_OUTPUT_DIR + fileName);
			}

			else {
				throw new IllegalArgumentException("unknown mesh: " + mesh);
			}
		}

		catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static HEdge edge(HVertex v1, HVertex v2) {
		return Assertions.present(HMesh.edge(v1, v2));
	}

	/**
	 * @param mesh
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static HEdge edge(HMesh2D mesh, int x1, int y1, int x2, int y2) {
		return edge(vertex(mesh, x1, y1), vertex(mesh, x2, y2));
	}

	/**
	 * @param mesh
	 * @param coords
	 * @return
	 */
	public static HVertex vertex(HMesh2D mesh, int[] coords) {
		return vertex(mesh, coords[0], coords[1]);
	}

	/**
	 * @param mesh
	 * @param x
	 * @param y
	 * @return
	 */
	public static HVertex vertex(HMesh2D mesh, int x, int y) {
		return Assertions.present(mesh.vertices()
				.filter(v -> isLocatedOn(mesh, v, x, y)).findFirst());
	}

	/**
	 * @param mesh
	 * @param v
	 * @param x
	 * @param y
	 * @return
	 */
	public static boolean isLocatedOn(HMesh2D mesh, HVertex v, int x, int y) {
		return (int) mesh.vertexX(v) == x && (int) mesh.vertexY(v) == y;
	}

	/**
	 * @param faces
	 * @return
	 */
	public static List<int[][]> toEdges(int[][]... faces) {
		List<int[][]> edges = new ArrayList<>();

		for (int[][] face : faces) {
			Loops.forEachPair(face,
					(v1, v2) -> edges.add(new int[][] { v1, v2 }));
		}

		return edges;
	}
}
