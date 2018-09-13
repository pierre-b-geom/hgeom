/**
 *
 */
package hgeom.hmesh.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HFace;
import hgeom.hmesh.elements.HMesh;
import hgeom.hmesh.elements.HMesh2D;
import hgeom.hmesh.elements.HVertex;

/**
 * @author Pierre
 *
 */
public final class HMeshCollapseEdgeTests {

	/**
	 *
	 */
	@Test
	public void failures() {
		int[] v0 = v(0, 0);
		int[] v1 = v(100, 0);
		int[] v3 = v(0, 100);

		GridDrawer drawer = new GridDrawer();
		HMesh2D mesh = drawer.penFrom(v0).to(v1).to(v3).close().mesh();
		HVertex hv1 = Utils.vertex(mesh, v1);

		assertFalse(mesh.removeVertex(hv1));
		assertFalse(mesh.collapseEdge(hv1.edge()));
		assertFalse(mesh.collapseEdge(hv1.edge().opposite()));
	}

	/**
	 *
	 */
	@Test
	public void collapseQuadEdge() {
		int[][] quad = { v(0, 0), v(10, 0), v(10, 10), v(0, 10) };
		int[][][] faces = { quad };
		HMesh mesh = Grid.meshFromFaces(faces);
		HEdge edge = Assertions.present(mesh.edges().findFirst());

		assertTrue(mesh.collapseEdge(edge));
		HMeshTester.check(mesh).numEdges(6).numVertices(3);

		mesh = Grid.meshFromFaces(faces);
		edge = Assertions.present(mesh.edges().findFirst()).opposite();

		assertTrue(mesh.collapseEdge(edge));
		HMeshTester.check(mesh).numEdges(6).numVertices(3);
	}

	/**
	 *
	 */
	@Test
	public void collapseQuadEdgeInGrid() {
		HMesh2D mesh = Grid.mesh(3, 3);

		HFace outsideFace = Assertions.present(
				mesh.faces().filter(f -> f.vertices().count() == 12).findAny());

		HFace centralFace = Assertions.present(mesh.faces()
				.filter(f -> f != outsideFace
						&& !f.neighbors().anyMatch(n -> n == outsideFace))
				.findAny());

		HVertex hv11 = Utils.vertex(mesh, 1, 1);

		HEdge edge = Assertions
				.present(centralFace.edge().next(e -> e.head() == hv11));

		assertTrue(mesh.collapseEdge(edge));

		HMeshTester.check(mesh).numFaces(10).numEdges(46).numVertices(15)
				.export("collapseQuadEdgeInGrid");
	}

	/**
	 *
	 */
	@Test
	public void collapseOutsideEdgeInGrid() {
		HMesh2D mesh = Grid.mesh(3, 3);

		HFace outsideFace = Assertions.present(
				mesh.faces().filter(f -> f.vertices().count() == 12).findAny());

		HVertex hv00 = Utils.vertex(mesh, 0, 0);

		HEdge edge = Assertions
				.present(outsideFace.edge().next(e -> e.head() == hv00));

		assertTrue(mesh.collapseEdge(edge));

		HMeshTester.check(mesh).numFaces(10).numEdges(46).numVertices(15)
				.export("collapseOutsideEdgeInGrid");
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
