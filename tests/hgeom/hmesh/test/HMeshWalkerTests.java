package hgeom.hmesh.test;

import static org.junit.Assert.assertEquals;

import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import org.junit.Test;

import hgeom.hmesh.core.HMeshTreeWalker;
import hgeom.hmesh.core.HMeshTreeWalker.TreePathType;
import hgeom.hmesh.core.HMeshWalker;
import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HMesh;
import hgeom.hmesh.elements.HMesh2D;
import hgeom.hmesh.elements.HVertex;
import hgeom.hmesh.test.Importer.ImportResult;

/**
 *
 * @author Pierre B.
 */
public final class HMeshWalkerTests {

	/**
	 *
	 */
	@Test(expected = ConcurrentModificationException.class)
	public void pathFailure() {
		ImportResult importResult = Utils.importFromMEditFile("ell");
		HMesh mesh = Assertions.present(importResult.toMesh2D());

		HEdge firstEdge = Assertions.present(mesh.edges().findFirst());
		HMeshWalker walker = new HMeshWalker(mesh);

		BinaryOperator<HEdge> operator = (e1, e2) -> {
			mesh.removeVertex(e1.head());
			return e2;
		};

		walker.walk(firstEdge, operator).count();
	}

	/**
	 *
	 */
	@Test
	public void path() {
		ImportResult importResult = Utils.importFromMEditFile("ell");
		HMesh2D mesh = Assertions.present(importResult.toMesh2D());

		Comparator<HVertex> vertexComparator = Comparator
				.comparingDouble(mesh::vertexX)
				.thenComparingDouble(mesh::vertexY);

		BinaryOperator<HVertex> minOperator = BinaryOperator
				.minBy(vertexComparator);

		BinaryOperator<HVertex> maxOperator = BinaryOperator
				.maxBy(vertexComparator);

		HMeshWalker walker = new HMeshWalker(mesh);

		HVertex min = Assertions.present(mesh.vertices().reduce(minOperator));
		HVertex max = Assertions.present(mesh.vertices().reduce(maxOperator));

		HVertex optimizedMax = walker.find(min, maxOperator);

		Stream<HVertex> optimizedMaxPath = walker.walk(min, maxOperator);

		assertEquals(13, optimizedMaxPath.count());
		assertEquals(mesh.vertexY(max), mesh.vertexY(optimizedMax), 0.);
	}

	/**
	 *
	 */
	@Test
	public void treeFailure() {
		ImportResult importResult = Utils.importFromMEditFile("ell");
		HMesh mesh = Assertions.present(importResult.toMesh2D());

		HEdge firstEdge = Assertions.present(mesh.edges().findFirst());
		HMeshTreeWalker walker = new HMeshTreeWalker(mesh);

		walker.walk(firstEdge).count();
	}

	/**
	 *
	 */
	@Test
	public void tree() {
		GridDrawer drawer = new GridDrawer();
		int dim = 100;

		for (int j = 0; j < dim; j++) {
			drawer.penFrom(0, j);

			for (int i = 1; i < dim; i++) {
				drawer.to(i, j);
			}
		}

		for (int i = 0; i < dim; i++) {
			drawer.penFrom(i, 0);

			for (int j = 1; j < dim; j++) {
				drawer.to(i, j);
			}
		}

		HMesh2D mesh = drawer.mesh();
		HMeshTester.check(mesh).export("trees");

		HVertex start = Assertions.present(mesh.vertices().findFirst());
		double middleX = dim * .5;
		double middleY = dim * .5;

		BinaryOperator<HVertex> accumulator = (v1, v2) -> {
			double delta1 = Math.abs(mesh.vertexX(v1) - middleX);
			double delta2 = Math.abs(mesh.vertexX(v2) - middleX);

			if (delta1 == delta2) {
				delta1 = Math.abs(mesh.vertexY(v1) - middleY);
				delta2 = Math.abs(mesh.vertexY(v2) - middleY);
			}
			return delta1 < delta2 ? v1 : v2;
		};

		HMeshWalker walker = new HMeshWalker(mesh);
		HVertex middle = walker.find(start, accumulator);
		assertEquals(50, mesh.vertexX(middle), 0);
		assertEquals(50, mesh.vertexY(middle), 0);

		HMeshTreeWalker treeWalker = new HMeshTreeWalker(mesh,
				TreePathType.BREADTH_FIRST, 5);

		assertEquals(61, treeWalker.walk(middle).count());

		treeWalker = new HMeshTreeWalker(mesh, TreePathType.DEPTH_FIRST, 5);

		Comparator<HVertex> comp = Comparator.comparingDouble(mesh::vertexX)
				.thenComparingDouble(mesh::vertexY);

		HVertex v = Assertions
				.present(treeWalker.walk(middle).sorted(comp).findFirst());

		assertEquals(45, mesh.vertexX(v), 0);
		assertEquals(50, mesh.vertexY(v), 0);

		comp = Comparator.comparingDouble(mesh::vertexY)
				.thenComparingDouble(mesh::vertexX);

		v = Assertions
				.present(treeWalker.walk(middle).sorted(comp).findFirst());

		assertEquals(50, mesh.vertexX(v), 0);
		assertEquals(45, mesh.vertexY(v), 0);
	}
}
