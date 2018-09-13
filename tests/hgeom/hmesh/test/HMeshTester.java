package hgeom.hmesh.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HFace;
import hgeom.hmesh.elements.HFace.Status;
import hgeom.hmesh.elements.HMesh;
import hgeom.hmesh.elements.HVertex;
import hgeom.hmesh.sequence.Sequence;

/**
 *
 * @author Pierre B.
 */
final class HMeshTester {

	/**
	 *
	 */
	private final HMesh[] meshes;

	/**
	 * @param meshes
	 */
	private HMeshTester(HMesh... meshes) {
		this.meshes = meshes;
	}

	/**
	 * @param mesh
	 * @return
	 */
	public static HMeshTester check(HMesh mesh) {
		assertConsistent(mesh);
		return new HMeshTester(mesh);
	}

	/**
	 * @param mesh
	 * @return
	 */
	public static HMeshTester check(Optional<? extends HMesh> mesh) {
		assertNotNull(mesh.orElse(null));
		assertConsistent(mesh.orElse(null));
		return new HMeshTester(mesh.orElse(null));
	}

	/**
	 * @param faces
	 * @return
	 */
	public static HMeshTester checkFromGridFaces(int[][][] faces) {
		HMesh mesh1 = Grid.meshFromFaces(faces, false);
		assertConsistent(mesh1);
		HMesh mesh2 = Grid.meshFromFaces(faces, true);
		assertConsistent(mesh2);
		return new HMeshTester(mesh1, mesh2);
	}

	/**
	 * @param edges
	 * @return
	 */
	public static HMeshTester checkFromGridEdges(List<int[][]> edges) {
		HMesh mesh1 = Grid.meshFromEdges(edges, false);
		assertConsistent(mesh1);
		HMesh mesh2 = Grid.meshFromEdges(edges, true);
		assertConsistent(mesh2);
		return new HMeshTester(mesh1, mesh2);
	}

	/**
	 * @return
	 */
	public HMeshTester empty() {
		for (HMesh mesh : meshes) {
			assertEquals(mesh.faces().count(), 0);
		}

		return this;
	}

	/**
	 * @param expected
	 * @return
	 */
	public HMeshTester numFaces(int expected) {
		for (HMesh mesh : meshes) {
			assertEquals(expected, (int) mesh.faces().count());
		}

		return this;
	}

	/**
	 * @param expected
	 * @return
	 */
	public HMeshTester numInteriorFaces(int expected) {
		for (HMesh mesh : meshes) {
			assertEquals(expected, (int) mesh.faces()
					.filter(f -> f.status() == Status.INTERIOR).count());
		}

		return this;
	}

	/**
	 * @param expected
	 * @return
	 */
	public HMeshTester numExteriorFaces(int expected) {
		for (HMesh mesh : meshes) {
			assertEquals(expected, (int) mesh.faces()
					.filter(f -> f.status() == Status.BOUNDARY).count());
		}

		return this;
	}

	/**
	 * @param expected
	 * @return
	 */
	public HMeshTester numTriangles(int expected) {
		for (HMesh mesh : meshes) {
			int numTriangles = (int) mesh.faces().map(HFace::edges)
					.mapToInt(Sequence::count).filter(count -> count == 3)
					.count();

			assertEquals(expected, numTriangles);
		}

		return this;
	}

	/**
	 * @param expected
	 * @return
	 */
	public HMeshTester numEdges(int expected) {
		for (HMesh mesh : meshes) {
			assertEquals(expected, (int) mesh.edges().count());
		}

		return this;
	}

	/**
	 * @param expected
	 * @return
	 */
	public HMeshTester maxNumEdges(int expected) {
		for (HMesh mesh : meshes) {
			int maxNumEdges = Assertions.present(
					mesh.faces().mapToInt(f -> f.edges().count()).max());

			assertEquals(expected, maxNumEdges);
		}

		return this;
	}

	/**
	 * @param expected
	 * @return
	 */
	public HMeshTester numVertices(int expected) {
		for (HMesh mesh : meshes) {
			assertEquals(expected,
					(int) mesh.edges().map(HEdge::head).distinct().count());
		}

		return this;
	}

	/**
	 * @param expected
	 * @return
	 */
	public HMeshTester minVertexEdges(int expected) {
		for (HMesh mesh : meshes) {
			assertEquals(expected,
					mesh.vertices().mapToInt(HVertex::degree).min().getAsInt());
		}

		return this;
	}

	/**
	 * @param expected
	 * @return
	 */
	public HMeshTester maxVertexEdges(int expected) {
		for (HMesh mesh : meshes) {
			assertEquals(expected,
					mesh.vertices().mapToInt(HVertex::degree).max().getAsInt());
		}

		return this;
	}

	/**
	 * @param fileName
	 * @return
	 */
	public HMeshTester export(String fileName) {
		if (meshes.length == 0) {
			throw new IllegalStateException();
		}

		Utils.exportToMEditFile(meshes[0], fileName);
		return this;
	}

	/**
	 * @param mesh
	 */
	private static void assertConsistent(HMesh mesh) {
		assertTrue(mesh.edges().allMatch(e -> !e.isDiscarded()));
		assertTrue(mesh.vertices().allMatch(v -> !v.isDiscarded()));
		assertTrue(mesh.faces().allMatch(f -> !f.isDiscarded()));

		Predicate<HEdge> pe = e -> e.head() != null;
		assertTrue(mesh.edges().allMatch(pe));

		pe = e -> e.head().edge() != null;
		assertTrue(mesh.edges().allMatch(pe));

		pe = e -> e.next() != null;
		assertTrue(mesh.edges().allMatch(pe));

		pe = e -> e.previous() != null;
		assertTrue(mesh.edges().allMatch(pe));

		pe = e -> e.opposite() != null;
		assertTrue(mesh.edges().allMatch(pe));

		pe = e -> e == e.next().previous();
		assertTrue(mesh.edges().allMatch(pe));

		pe = e -> e == e.previous().next();
		assertTrue(mesh.edges().allMatch(pe));

		pe = e -> e == e.opposite().opposite();
		assertTrue(mesh.edges().allMatch(pe));

		pe = e -> e.head() == e.next().tail();
		assertTrue(mesh.edges().allMatch(pe));

		pe = e -> e.cycle().count() > 2;
		assertTrue(mesh.edges().allMatch(pe));

		// Premiere maniere d'obtenir l'arete precedente : parcours du cycle
		Function<HEdge, Optional<HEdge>> f1 = e -> e.next(e2 -> e2.next() == e);

		// 2eme maniere d'obtenir l'arete precedente : parcours autour du sommet
		// source
		Function<HEdge, Optional<HEdge>> f2 = e -> e.incomingEdges()
				.filter(e2 -> e2.next() == e).findFirst();

		pe = e -> f1.apply(e).get() == f2.apply(e).get();
		assertTrue(mesh.edges().allMatch(pe));

		// Sommets valides
		Predicate<HVertex> pv = v -> v == v.edge().head();
		assertTrue(mesh.vertices().allMatch(pv));

		// Les aretes convergeant vers un sommet referencent ce sommet
		pv = v -> v.incomingEdges().map(HEdge::head).allMatch(v::equals);
		assertTrue(mesh.vertices().allMatch(pv));

		// Les aretes convergeant vers un sommet contiennent l'arete de ce
		// sommet
		pv = v -> v.incomingEdges().anyMatch(v.edge()::equals);
		assertTrue(mesh.vertices().allMatch(pv));

		// Les aretes ayant un sommet comme depart ne referencent pas ce sommet
		pv = v -> v.outgoingEdges().map(HEdge::head)
				.allMatch(head -> head != v);

		assertTrue(mesh.vertices().allMatch(pv));

		// Les aretes referencees une seul fois dans la structure
		long numEdges = mesh.edges().count();
		long numDistinctEdges = mesh.edges().distinct().count();
		assertEquals(numEdges, numDistinctEdges);

		// Une arete du bord interne d'une face ne peut pas egalement figurer
		// sur le bord externe
		Predicate<HFace> pf = f -> {
			Sequence<HEdge> edges = f.edges();
			Sequence<HEdge> oppositeEdges = edges.map(HEdge::opposite);
			return edges.allMatch(e -> oppositeEdges.allMatch(e2 -> e2 != e));
		};

		assertTrue(mesh.faces().allMatch(pf));
	}
}
