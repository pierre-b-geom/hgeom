package hgeom.hmesh.test;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HFace;
import hgeom.hmesh.elements.HFace.Status;
import hgeom.hmesh.elements.HMesh2D;
import hgeom.hmesh.elements.HVertex;
import hgeom.hmesh.util.Loops;
import hgeom.hmesh.winding.Winding;

/**
 *
 * @author Pierre B.
 */
public final class HMeshWindingTests {

	/**
	 * Construction d'une face dont 3 sommets sont alignes verticalement.
	 * Verification de la bonne orientation de la face
	 */
	@Test
	public void faceWinding1() {
		int[][][] faces = { { v(0, 0), v(0, 1), v(0, 2), v(1, 0) } };
		HMesh2D mesh = mesh(faces, Winding.COUNTERCLOCKWISE);

		HFace face = Assertions.present(mesh.faces()
				.filter(f -> f.status() == Status.INTERIOR).findFirst());

		HEdge firstHEdge = Assertions.present(face.edges()
				.filter(e -> Utils.isLocatedOn(mesh, e.head(), 0, 0))
				.findFirst());

		HVertex nextVertex = firstHEdge.next().head();
		assertTrue(Utils.isLocatedOn(mesh, nextVertex, 1, 0));

		assertCCW(mesh, face);
	}

	/**
	 *
	 */
	@Test
	public void faceWinding2() {
		int[][][] faces = { { v(0, 0), v(0, 1), v(0, 2) } };
		HMesh2D mesh = mesh(faces, Winding.COUNTERCLOCKWISE);

		HFace face = Assertions.present(mesh.faces()
				.filter(f -> f.status() == Status.INTERIOR).findFirst());

		assertUndeterminedWinding(mesh, face);

		mesh = mesh(faces, Winding.CLOCKWISE);

		face = Assertions.present(mesh.faces()
				.filter(f -> f.status() == Status.INTERIOR).findFirst());

		assertUndeterminedWinding(mesh, face);
	}

	/**
	 *
	 */
	@Test
	public void facesWinding() {
		int[][] quad1 = { v(0, 0), v(10, 0), v(10, 10), v(0, 10) };
		int[][] quad2 = { v(10, 0), v(10, 10), v(20, 10), v(20, 0) };
		int[][] quad3 = { v(0, 10), v(10, 10), v(10, 20), v(0, 20) };
		int[][] quad4 = { v(10, 10), v(10, 20), v(20, 20), v(20, 10) };
		int[][][] faces = { quad1, quad2, quad3, quad4 };
		HMesh2D ccwMesh = mesh(faces, Winding.COUNTERCLOCKWISE);

		ccwMesh.faces().filter(f -> f.status() == Status.INTERIOR)
				.forEach(f -> assertCCW(ccwMesh, f));

		ccwMesh.faces().filter(f -> f.status() == Status.BOUNDARY)
				.forEach(f -> assertCW(ccwMesh, f));

		HMesh2D cwMesh = mesh(faces, Winding.CLOCKWISE);

		cwMesh.faces().filter(f -> f.status() == Status.INTERIOR)
				.forEach(f -> assertCW(cwMesh, f));

		cwMesh.faces().filter(f -> f.status() == Status.BOUNDARY)
				.forEach(f -> assertCCW(cwMesh, f));
	}

	/**
	 * aretes confondus pour le carre en haut a droite : la creation fonctionne
	 * mais avec une face externe qui boucle sur elle-meme et qui englobe le
	 * carre en haut a droite
	 */
	@Test
	public void facesWinding2() {
		int[][] quad1 = { v(0, 0), v(10, 0), v(10, 10), v(0, 10) };
		int[][] quad2 = { v(10, 0), v(10, 10), v(20, 10), v(20, 0) };
		int[][] quad3 = { v(0, 10), v(10, 10), v(10, 20), v(0, 20) };
		int[][] quad4 = { v(10, 10), v(10, 15), v(15, 15), v(15, 10) };
		int[][][] faces = { quad1, quad2, quad3, quad4 };
		HMesh2D ccwMesh = mesh(faces, Winding.COUNTERCLOCKWISE);

		ccwMesh.faces().filter(f -> f.status() == Status.INTERIOR)
				.forEach(f -> assertCCW(ccwMesh, f));

		ccwMesh.faces().filter(f -> f.status() == Status.BOUNDARY)
				.forEach(f -> assertCW(ccwMesh, f));

		HMesh2D cwMesh = mesh(faces, Winding.CLOCKWISE);

		cwMesh.faces().filter(f -> f.status() == Status.INTERIOR)
				.forEach(f -> assertCW(cwMesh, f));

		cwMesh.faces().filter(f -> f.status() == Status.BOUNDARY)
				.forEach(f -> assertCCW(cwMesh, f));
	}

	/**
	 *
	 */
	@Test
	public void rayWinding() {
		int numVertices = 100;
		int[][] vertices = new int[numVertices][];
		int[][][] faces = new int[numVertices][][];
		int[] center = { 2000, 2000 };

		Arrays.setAll(vertices, i -> {
			double angle = Math.PI * 2 * i / numVertices;

			return v(center[0] + (int) (2000 * Math.cos(angle)),
					center[1] + (int) (2000 * Math.sin(angle)));
		});

		Arrays.setAll(faces, i -> new int[][] { center, vertices[i],
				Loops.get(vertices, i + 1) });

		int j = faces.length / 2;
		int k = 0;

		// Melange dans le tableau
		for (int i = 0; i < 500; i++) {
			j = 31 * j + i;
			k = 37 * i + 31 * k;
			int index1 = Math.abs(j) % faces.length;
			int index2 = Math.abs(k) % faces.length;
			int[][] face = faces[index1];
			faces[index1] = faces[index2];
			faces[index2] = face;
		}

		int[][][] incompleteFaces = new int[faces.length - 10][][];
		Arrays.setAll(incompleteFaces, i -> faces[i]);

		HMesh2D ccwMesh = mesh(faces, Winding.COUNTERCLOCKWISE);

		ccwMesh.faces().filter(f -> f.status() == Status.INTERIOR)
				.forEach(f -> assertCCW(ccwMesh, f));

		ccwMesh.faces().filter(f -> f.status() == Status.BOUNDARY)
				.forEach(f -> assertCW(ccwMesh, f));

		HMeshTester.check(ccwMesh).export("rayWinding");

		HMesh2D cwMesh = mesh(faces, Winding.CLOCKWISE);

		cwMesh.faces().filter(f -> f.status() == Status.INTERIOR)
				.forEach(f -> assertCW(cwMesh, f));

		cwMesh.faces().filter(f -> f.status() == Status.BOUNDARY)
				.forEach(f -> assertCCW(cwMesh, f));
	}

	/**
	 * @param mesh
	 * @param face
	 */
	private static void assertCCW(HMesh2D mesh, HFace face) {
		assertTrue(winding(mesh, face) == Winding.COUNTERCLOCKWISE);
	}

	/**
	 * @param mesh
	 * @param face
	 */
	private static void assertCW(HMesh2D mesh, HFace face) {
		assertTrue(winding(mesh, face) == Winding.CLOCKWISE);
	}

	/**
	 * @param mesh
	 * @param face
	 */
	private static void assertUndeterminedWinding(HMesh2D mesh, HFace face) {
		assertTrue(winding(mesh, face) == Winding.UNDETERMINED);
	}

	/**
	 * @param mesh
	 * @param face
	 * @return
	 */
	private static Winding winding(HMesh2D mesh, HFace face) {
		List<double[]> coords = face.edges().map(HEdge::head)
				.map(mesh::vertexXY).toList();

		return Winding.ofPolygon2D(face.vertices().count(),
				i -> coords.get(i)[0], i -> coords.get(i)[1]);
	}

	/**
	 * @param faces
	 * @param faceWinding
	 * @return
	 */
	private static HMesh2D mesh(int[][][] faces, Winding faceWinding) {
		return Grid.meshFromFaces(faces, false, faceWinding);
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
