package hgeom.hmesh.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import org.junit.Test;

import hgeom.hmesh.data.HData;
import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HFace;
import hgeom.hmesh.elements.HFace.Status;
import hgeom.hmesh.elements.HMesh;
import hgeom.hmesh.elements.HMesh2D;
import hgeom.hmesh.elements.HVertex;
import hgeom.hmesh.examples.HMeshBasicOperations;
import hgeom.hmesh.sequence.Sequence;

/**
 *
 * @author Pierre B.
 */
public final class HMeshOperationTests {

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
		HVertex hv0 = Utils.vertex(mesh, v0);
		HVertex hv1 = Utils.vertex(mesh, v1);

		List<HFace> faces = mesh.faces().collect(Collectors.toList());
		HFace face1 = faces.get(0);
		HFace face2 = faces.get(0);

		assertFalse(mesh.mergeFaces(face1, face2));
		assertFalse(mesh.removeVertex(hv1));
		Assertions.notPresent(mesh.splitFace(face1, hv1, hv0));
	}

	/**
	 *
	 */
	@Test
	public void edgeCycleOperations() {
		int[] v1 = v(10, 0);
		int[] v2 = v(0, 15);
		int[][][] faces = {
				{ v(0, 0), v1, v(10, 5), v(10, 10), v(5, 10), v2 } };

		HMesh2D mesh = Grid.meshFromFaces(faces);
		HVertex hv1 = Utils.vertex(mesh, v1);
		HVertex hv2 = Utils.vertex(mesh, v2);

		HFace face = Assertions.present(mesh.faces().skip(1).findFirst());
		HEdge hEdge = Assertions.present(face.edges().findFirst());
		assertEquals(6, face.edges().count());

		List<HEdge> hEdges = new ArrayList<>();
		face.edges().forEach(hEdges::add);
		assertEquals(6, hEdges.size());
		assertTrue(face.vertices().anyMatch(v -> v == hv1));

		assertTrue(face.vertices().map(mesh::vertexXY)
				.allMatch(coords -> coords[0] <= 10));

		Assertions.present(
				face.edges().filter(hEdge.previous()::equals).findFirst());

		BinaryOperator<HEdge> accumulator = (he1,
				he2) -> mesh.vertexY(he1.head()) > mesh.vertexY(he2.head())
						? he1
						: he2;

		HEdge reduction = Assertions.present(face.edges().reduce(accumulator));

		assertArrayEquals(mesh.vertexXY(reduction.head()), mesh.vertexXY(hv2),
				0.);

		BiFunction<Double, HEdge, Double> maxX = (d, he) -> Math.max(d,
				mesh.vertexX(he.head()));

		Double max = face.edges().reduce(Double.MIN_VALUE, maxX);
		assertEquals(10, max.doubleValue(), 0);

		List<HVertex> vertices = face.vertices().toList();
		assertTrue(vertices.stream().anyMatch(hv1::equals));
	}

	/**
	 *
	 */
	@Test
	public void vertexCycleOperations() {
		int[] center = v(100, 100);
		int[] zero = v(100, 130);
		int[] one = v(110, 120);
		int[] two = v(120, 110);
		int[] three = v(130, 100);
		int[] four = v(120, 90);
		int[] five = v(110, 80);
		int[] six = v(100, 70);
		int[] seven = v(90, 80);
		int[] eight = v(80, 90);
		int[] nine = v(70, 100);
		int[] ten = v(80, 110);
		int[] eleven = v(90, 120);

		int[][][] faces = { { center, zero, one }, { center, one, two },
				{ center, two, three }, { center, three, four },
				{ center, four, five }, { center, five, six },
				{ center, six, seven }, { center, seven, eight },
				{ center, eight, nine }, { center, nine, ten },
				{ center, ten, eleven }, { center, eleven, zero }, };

		HMesh2D mesh = Grid.meshFromFaces(faces);
		HVertex hCenter = Utils.vertex(mesh, center);
		HVertex hZero = Utils.vertex(mesh, zero);
		HVertex hSeven = Utils.vertex(mesh, seven);

		assertEquals(hCenter.degree(), faces.length);

		List<HEdge> hEdges = new ArrayList<>();
		hCenter.incomingEdges().forEach(hEdges::add);
		assertEquals(hEdges.size(), faces.length);

		hEdges.clear();
		hCenter.outgoingEdges().forEach(hEdges::add);
		assertEquals(hEdges.size(), faces.length);
		assertEquals(hEdges.stream().distinct().count(), faces.length);

		assertTrue(hCenter.incomingEdges().map(HEdge::head)
				.anyMatch(hCenter::equals));

		assertTrue(hCenter.outgoingEdges().map(HEdge::head)
				.anyMatch(hSeven::equals));

		assertTrue(hCenter.incomingEdges().map(HEdge::head)
				.allMatch(hCenter::equals));

		assertTrue(hCenter.outgoingEdges().map(HEdge::head)
				.allMatch(v -> mesh.vertexX(v) <= 130));

		Assertions.present(hCenter.incomingEdges().map(HEdge::head)
				.filter(hCenter::equals).findFirst());

		Assertions.present(hCenter.outgoingEdges().map(HEdge::head)
				.filter(hSeven::equals).findFirst());

		BinaryOperator<HVertex> accumulator = (v1,
				v2) -> mesh.vertexY(v1) > mesh.vertexY(v2) ? v1 : v2;

		HVertex reduction = Assertions.present(
				hCenter.outgoingEdges().map(HEdge::head).reduce(accumulator));

		assertEquals(reduction, hZero);

		accumulator = (v1, v2) -> mesh.vertexY(v1) > mesh.vertexY(v2) ? v1 : v2;

		reduction = Assertions.present(
				hCenter.incomingEdges().map(HEdge::tail).reduce(accumulator));

		assertEquals(reduction, hZero);

		BiFunction<Double, HVertex, Double> maxX = (d, v) -> Math.max(d,
				mesh.vertexX(v));

		Double max = hCenter.incomingEdges().map(HEdge::tail)
				.reduce(Double.MIN_VALUE, maxX);

		assertEquals(130, max.doubleValue(), 0);

		BiFunction<Double, HVertex, Double> minX = (d, v) -> Math.min(d,
				mesh.vertexX(v));

		Double min = hCenter.outgoingEdges().map(HEdge::head)
				.reduce(Double.MAX_VALUE, minX);

		assertEquals(70, min.doubleValue(), 0);

		List<HVertex> vertices = hCenter.outgoingEdges().map(HEdge::head)
				.toList();

		assertTrue(vertices.stream().anyMatch(hSeven::equals));

		vertices = hCenter.incomingEdges().map(HEdge::head).toList();
		assertEquals(1, vertices.stream().distinct().count());
	}

	/**
	 *
	 */
	@Test
	public void vertexNeighborsOperations() {
		int[] v1 = v(100, 100);
		int[] v2 = v(150, 100);
		int[] v3 = v(0, 50);
		int[] v4 = v(150, 50);
		int[] v5 = v(150, 150);
		int[] v6 = v(75, 150);
		int[] v7 = v(0, 150);

		int[][][] faces = { { v3, v1, v2, v4 }, { v5, v2, v4 }, { v5, v2, v6 },
				{ v1, v2, v6 }, { v7, v1, v6 }, { v3, v7, v1 } };

		HMesh2D mesh = Grid.meshFromFaces(faces);
		HVertex hv1 = Utils.vertex(mesh, v1);
		HVertex hv2 = Utils.vertex(mesh, v2);
		HVertex hv3 = Utils.vertex(mesh, v3);
		HVertex hv4 = Utils.vertex(mesh, v4);
		HVertex hv5 = Utils.vertex(mesh, v5);
		HVertex hv6 = Utils.vertex(mesh, v6);
		HVertex hv7 = Utils.vertex(mesh, v7);

		assertEquals(4, hv1.degree());
		assertTrue(hv1.isNeighborOf(hv2));
		Sequence<HVertex> neighbors = hv1.neighbors();
		assertEquals(4, neighbors.count());
		assertTrue(neighbors.anyMatch(hv2::equals));
		assertTrue(neighbors.anyMatch(hv6::equals));
		assertTrue(neighbors.anyMatch(hv7::equals));
		assertTrue(neighbors.anyMatch(hv3::equals));

		List<HEdge> edges = hv1.incomingEdges().toList();
		assertEquals(4, edges.size());
		assertEquals(edges.get(0).head(), hv1);

		edges = hv2.outgoingEdges().toList();
		assertEquals(4, edges.size());
		assertTrue(edges.stream().map(HEdge::head).anyMatch(hv1::equals));
		assertTrue(edges.stream().map(HEdge::head).anyMatch(hv6::equals));
		assertTrue(edges.stream().map(HEdge::head).anyMatch(hv5::equals));
		assertTrue(edges.stream().map(HEdge::head).anyMatch(hv4::equals));

		HFace face = Assertions.present(mesh.faces().findAny());
		assertEquals(4, face.neighbors().count());
	}

	/**
	 *
	 */
	@Test
	public void splitQuad() {
		int[][] quad = { v(0, 0), v(10, 0), v(10, 10), v(0, 10) };
		int[][][] faces = { quad };
		HMesh mesh = Grid.meshFromFaces(faces);
		HFace face = Assertions.present(mesh.faces().findFirst());
		HEdge hEdge = Assertions.present(face.edges().findFirst());
		HVertex vertex1 = hEdge.head();
		HVertex vertex2 = hEdge.next().next().head();
		HFace faceToSplit = Assertions.present(mesh.faces().findFirst());

		Assertions.present(mesh.splitFace(faceToSplit, vertex1, vertex2));

		assertEquals(3, mesh.faces().count());

		assertEquals(2,
				mesh.vertices().mapToInt(HVertex::degree).min().getAsInt());

		assertEquals(3,
				mesh.vertices().mapToInt(HVertex::degree).max().getAsInt());
	}

	/**
	 *
	 */
	@Test
	public void splitEdge() {
		int[][] quad = { v(0, 0), v(10, 0), v(10, 10), v(0, 10) };
		int[][][] faces = { quad };
		HMesh mesh = Grid.meshFromFaces(faces);

		HEdge edge = Assertions.present(mesh.faces().skip(1).findFirst()
				.map(HFace::edges).flatMap(Sequence::findFirst));

		mesh.splitEdge(edge);
		HMeshTester.check(mesh).numEdges(10).numVertices(5);
	}

	/**
	 *
	 */
	@Test
	public void meshOperations0() {
		int[] v1 = v(100, 100);
		int[] v2 = v(150, 100);
		int[] v3 = v(0, 50);
		int[] v4 = v(200, 50);
		int[] v5 = v(200, 150);
		int[] v6 = v(75, 150);
		int[] v7 = v(0, 150);

		int[][][] faces = { { v3, v1, v2, v4 }, { v5, v2, v4 }, { v5, v2, v6 },
				{ v1, v2, v6 }, { v7, v1, v6 }, { v3, v7, v1 } };

		HMesh2D mesh = Grid.meshFromFaces(faces);
		HVertex hv1 = Utils.vertex(mesh, v1);
		HVertex hv2 = Utils.vertex(mesh, v2);
		HVertex hv3 = Utils.vertex(mesh, v3);
		HVertex hv4 = Utils.vertex(mesh, v4);
		HVertex hv5 = Utils.vertex(mesh, v5);
		HVertex hv6 = Utils.vertex(mesh, v6);
		HVertex hv7 = Utils.vertex(mesh, v7);

		HData<HVertex, int[]> vertexCoords = mesh.createVertexData();
		vertexCoords.set(hv1, v1);
		vertexCoords.set(hv2, v2);
		vertexCoords.set(hv3, v3);
		vertexCoords.set(hv4, v4);
		vertexCoords.set(hv5, v5);
		vertexCoords.set(hv6, v6);
		vertexCoords.set(hv7, v7);

		HEdge edge12 = Utils.edge(hv1, hv2);
		assertTrue(mesh.mergeFaces(edge12.face(), edge12.opposite().face()));

		HEdge edge61 = Utils.edge(hv6, hv1);
		HFace centralFace = edge61.face();
		assertEquals(5, centralFace.edges().count());
		assertTrue(centralFace.vertices().anyMatch(hv1::equals));
		assertTrue(centralFace.vertices().anyMatch(hv6::equals));
		assertTrue(centralFace.vertices().anyMatch(hv2::equals));
		assertTrue(centralFace.vertices().anyMatch(hv4::equals));
		assertTrue(centralFace.vertices().anyMatch(hv3::equals));

		HFace newFace = Assertions
				.present(mesh.splitFace(centralFace, hv1, hv2));

		assertEquals(4, newFace.edges().count());
		assertEquals(3, centralFace.edges().count());

		assertTrue(centralFace.vertices().anyMatch(hv1::equals));
		assertTrue(centralFace.vertices().anyMatch(hv2::equals));
		assertTrue(centralFace.vertices().anyMatch(hv6::equals));
		assertTrue(newFace.vertices().anyMatch(hv1::equals));
		assertTrue(newFace.vertices().anyMatch(hv2::equals));
		assertTrue(newFace.vertices().anyMatch(hv3::equals));
		assertTrue(newFace.vertices().anyMatch(hv4::equals));

		HMeshTester.check(mesh).numFaces(7).numEdges(24);

		HEdge edge34 = Utils.edge(hv3, hv4);
		HVertex hv8 = mesh.splitEdge(edge34);
		vertexCoords.set(hv8, new int[] { 100, 0 });

		HFace face = Assertions.present(HMesh.edge(hv8, hv4).map(HEdge::face));

		assertEquals(5, face.edges().count());
		assertTrue(face.vertices().anyMatch(hv1::equals));
		assertTrue(face.vertices().anyMatch(hv8::equals));
		assertTrue(face.vertices().anyMatch(hv2::equals));
		assertTrue(face.vertices().anyMatch(hv4::equals));
		assertTrue(face.vertices().anyMatch(hv3::equals));

		HEdge edge54 = Utils.edge(hv4, hv5);
		assertTrue(mesh.mergeFaces(edge54.face(), edge54.opposite().face()));
		assertEquals(4, hv2.degree());
		assertTrue(hv2.isNeighborOf(hv4));
		assertTrue(hv2.isNeighborOf(hv5));

		HEdge edge25 = Utils.edge(hv2, hv5);
		HEdge edge42 = Utils.edge(hv4, hv2);
		HEdge edge26 = Utils.edge(hv2, hv6);
		HFace face256 = edge25.face();
		HFace face42138 = edge42.face();
		HFace face261 = edge26.face();

		assertFalse(mesh.mergeFaces(face42138, face256));
		assertFalse(mesh.mergeFaces(face42138, face42138));
		assertTrue(mesh.mergeFaces(face261, face256));

		edge12 = Utils.edge(hv1, hv2);
		HVertex hv9 = mesh.splitEdge(edge12);
		vertexCoords.set(hv8, new int[] { 120, 90 });
		HEdge edge29 = Utils.edge(hv2, hv9);
		HVertex hv10 = mesh.splitEdge(edge29);
		vertexCoords.set(hv10, new int[] { 140, 90 });
		HEdge edge910 = Utils.edge(hv9, hv10);
		assertTrue(mesh.mergeFaces(edge910.opposite().face(), edge910.face()));

		HMeshTester.check(mesh);
	}

	/**
	 *
	 */
	@Test
	public void meshOperations1() {
		int[] v0 = v(0, 0);
		int[] v1 = v(100, 0);
		int[] v2 = v(0, 100);

		GridDrawer drawer = new GridDrawer();
		HMesh2D mesh = drawer.penFrom(v0).to(v1).to(v2).close().mesh();
		HVertex hv0 = Utils.vertex(mesh, v0);
		HVertex hv1 = Utils.vertex(mesh, v1);
		HVertex hv2 = Utils.vertex(mesh, v2);

		HData<HVertex, int[]> vertexCoords = mesh.createVertexData();
		vertexCoords.set(hv0, v0);
		vertexCoords.set(hv1, v1);
		vertexCoords.set(hv2, v2);

		HEdge edge01 = Utils.edge(hv0, hv1);
		HVertex hv3 = mesh.splitEdge(edge01);
		vertexCoords.set(hv3, new int[] { 50, 10 });

		HEdge edge12 = Utils.edge(hv1, hv2);
		HVertex hv4 = mesh.splitEdge(edge12);
		vertexCoords.set(hv4, new int[] { 55, 55 });

		HEdge edge20 = Utils.edge(hv2, hv0);
		HVertex hv5 = mesh.splitEdge(edge20);
		vertexCoords.set(hv5, new int[] { 10, 50 });

		HFace face = edge01.face();
		HFace newFace = Assertions.present(mesh.splitFace(face, hv3, hv4));

		if (newFace.edges().count() == 3) {
			assertEquals(5, face.edges().count());
		}

		else if (newFace.edges().count() == 5) {
			assertEquals(3, face.edges().count());
		}

		else {
			assertFalse(false);
		}

		HMeshTester.check(mesh).numFaces(3).numVertices(6).numEdges(14);

		assertTrue(mesh.removeVertex(hv5));

		HMeshTester.check(mesh).numFaces(3).numVertices(5).numEdges(12);

		edge20 = Utils.edge(hv2, hv0);
		hv5 = mesh.splitEdge(edge20);
		vertexCoords.set(hv5, new int[] { 10, 50 });

		HFace outsideFace = Assertions
				.present(HMesh.edge(hv0, hv5).map(HEdge::face));

		assertTrue(mesh.splitFace(outsideFace, hv5, hv1).isPresent());

		HMeshTester.check(mesh).export("meshOperations1");
	}

	/**
	 *
	 */
	@Test
	public void meshOperations2() {
		int[] v0 = v(0, 0);
		int[] v1 = v(100, 0);
		int[] v3 = v(0, 100);

		GridDrawer drawer = new GridDrawer();
		HMesh2D mesh = drawer.penFrom(v0).to(v1).to(v3).close().mesh();
		HVertex hv0 = Utils.vertex(mesh, v0);
		HVertex hv1 = Utils.vertex(mesh, v1);
		HVertex hv3 = Utils.vertex(mesh, v3);

		HData<HVertex, int[]> vertexCoords = mesh.createVertexData();
		vertexCoords.set(hv0, v0);
		vertexCoords.set(hv1, v1);
		vertexCoords.set(hv3, v3);

		HFace face1 = hv0.edge().face();
		HFace face2 = hv0.edge().opposite().face();

		assertTrue(face1.status() == Status.UNKNOWN
				&& face2.status() == Status.UNKNOWN);

		assertFalse(mesh.mergeFaces(face1, face2));

		HEdge edge13 = Utils.edge(hv1, hv3);
		HVertex hv2 = mesh.splitEdge(edge13);
		vertexCoords.set(hv2, new int[] { 100, 100 });
		HEdge edge12 = Utils.edge(hv1, hv2);
		assertEquals(edge13, edge12);
		HEdge edge23 = Utils.edge(hv2, hv3);
		assertEquals(edge12.next(), edge23);
		HEdge edge30 = Utils.edge(hv3, hv0);
		HVertex hv30 = mesh.splitEdge(edge30);
		vertexCoords.set(hv30, new int[] { 0, 50 });
		HVertex hv12 = mesh.splitEdge(edge12);
		vertexCoords.set(hv12, new int[] { 100, 50 });

		// Decoupage
		HFace innerFace2 = Assertions
				.present(mesh.splitFace(face1, hv30, hv12));

		HEdge edge3012 = Utils.edge(hv30, hv12);
		HEdge edge1230 = edge3012.opposite();
		assertNotNull(innerFace2);

		// Fusion
		assertTrue(mesh.mergeFaces(innerFace2, face1));
		assertTrue(edge3012.isDiscarded());
		assertTrue(edge1230.isDiscarded());
		assertTrue(face1.isDiscarded());

		// Decoupage avec bord externe
		innerFace2 = Assertions.present(mesh.splitFace(innerFace2, hv30, hv12));
		assertTrue(mesh.mergeFaces(face2, innerFace2));
		HMeshTester.check(mesh).export("meshOperations2");
	}

	/**
	 *
	 */
	@Test
	public void meshOperations3() {
		GridDrawer drawer = new GridDrawer();

		drawer.penFrom(0, 0).to(50, 0).to(100, 0).to(100, 100).to(50, 100)
				.to(0, 100).close();

		drawer.penFrom(100, 100).to(200, 100).to(200, 200).to(100, 200).close();

		drawer.penFrom(100, 100).to(150, 150).to(200, 200);
		drawer.penFrom(50, 0).to(50, 25);
		drawer.penFrom(50, 75).to(50, 100);

		drawer.penFrom(25, 25).to(50, 25).to(75, 25).to(75, 75).to(50, 75)
				.to(25, 75).close();

		drawer.penFrom(150, 150).to(100, 200);

		HMesh2D mesh = drawer.mesh();
		HMeshTester.check(mesh).numFaces(7).numExteriorFaces(0);

		HEdge edge = Utils.edge(mesh, 100, 200, 200, 200);

		HFace skin = edge.cycle().count() > edge.opposite().cycle().count()
				? edge.face()
				: edge.opposite().face();

		assertEquals(10, skin.edges().count());

		Sequence<HFace> skinNeighbors = skin.neighbors();
		assertEquals(6, skinNeighbors.count());

		assertEquals(5,
				skinNeighbors.toList().stream().distinct().toArray().length);

		HFace insideFace = Assertions.present(mesh.faces()
				.filter(f -> f != skin && !f.isNeighborOf(skin)).findFirst());

		assertEquals(6, insideFace.edges().count());

		edge = Utils.edge(mesh, 25, 25, 25, 75);
		HVertex v1 = mesh.splitEdge(edge, 50, 50);

		edge = Utils.edge(mesh, 75, 25, 75, 75);
		HVertex v2 = mesh.splitEdge(edge, 75, 50);

		HFace newFace = Assertions.present(mesh.splitFace(insideFace, v1, v2));

		HFace otherFace = Assertions.present(v1.incomingEdges().map(HEdge::face)
				.filter(face -> face != newFace)
				.filter(face -> face != insideFace).findFirst());

		assertTrue(mesh.mergeFaces(skin, otherFace));

		otherFace = Assertions.present(v2.incomingEdges().map(HEdge::face)
				.filter(face -> face != newFace)
				.filter(face -> face != insideFace).findFirst());

		// pas de fusion car 2 bords entre les 2 faces
		assertFalse(mesh.mergeFaces(otherFace, skin));

		HMeshTester.check(mesh).export("meshOperations3");

		int maxNumEdges = Assertions
				.present(mesh.faces().mapToInt(f -> f.edges().count()).max());

		assertEquals(13, maxNumEdges);

		HMeshBasicOperations.simplify(mesh);

		HMeshTester.check(mesh).maxNumEdges(7)
				.export("meshOperations3_simplified");
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
