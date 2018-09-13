package hgeom.hmesh.test;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import hgeom.hmesh.data.HDData;
import hgeom.hmesh.elements.HFace;
import hgeom.hmesh.elements.HFace.Status;
import hgeom.hmesh.elements.HMesh2D;
import hgeom.hmesh.elements.HMesh3D;
import hgeom.hmesh.elements.HVertex;
import hgeom.hmesh.examples.ComputeHMeshFaceData;
import hgeom.hmesh.examples.ComputeHMeshVertexData;
import hgeom.hmesh.examples.HMeshBasicOperations;
import hgeom.hmesh.sequence.Sequence;
import hgeom.hmesh.test.Importer.ImportResult;

/**
 *
 * @author Pierre B.
 */
public final class HMeshBasicOperationTests {

	/**
	 *
	 */
	@Test
	public void createFaceValues() {
		HMesh2D mesh = Grid.mesh(100, 100);
		HDData<HVertex> vertexValues = mesh.createVertexDoubleData();
		vertexValues.setAll(v -> mesh.vertexX(v) + mesh.vertexY(v));

		HDData<HFace> faceValues = ComputeHMeshFaceData.fromVertexData(mesh,
				vertexValues);

		double vertexAverage = vertexValues.stream().average().getAsDouble();
		double faceAverage = faceValues.stream().average().getAsDouble();

		assertEquals(100, vertexAverage, 1E-6);
		assertEquals(100, faceAverage, 1E-6);

		faceValues = ComputeHMeshFaceData.fromVertexData2(mesh, vertexValues);
		vertexAverage = vertexValues.stream().average().getAsDouble();
		faceAverage = faceValues.stream().average().getAsDouble();

		assertEquals(100, vertexAverage, 1E-6);
		assertEquals(100, faceAverage, 1E-6);
	}

	/**
	 *
	 */
	@Test
	public void createVertexValues() {
		HMesh2D mesh = Grid.mesh(100, 100);
		HDData<HFace> faceValues = mesh.createFaceDoubleData();
		faceValues.setAll(f -> 1);

		HDData<HVertex> vertexValues = ComputeHMeshVertexData.fromFaceData(mesh,
				faceValues);

		double faceAverage = faceValues.stream().average().getAsDouble();
		double vertexAverage = vertexValues.stream().average().getAsDouble();

		assertEquals(1, faceAverage, 1E-6);
		assertEquals(1, vertexAverage, 1E-6);
	}

	/**
	 *
	 */
	@Test
	public void numberOfTriangles() {
		ImportResult importResult = importGreenland();
		HMesh2D mesh = Assertions.present(importResult.toMesh2D());
		int numTriangles = HMeshBasicOperations.numberOfTriangles(mesh);
		assertEquals(importResult.numFaces() + 1L, numTriangles);
	}

	/**
	 *
	 */
	@Test
	public void averageNumberOfEdges() {
		ImportResult importResult = importGreenland();
		HMesh2D mesh = Assertions.present(importResult.toMesh2D());

		double n = HMeshBasicOperations.averageNumberOfEdges(mesh)
				.getAsDouble();

		assertEquals(3.012599757609019, n, 1E-6);

		n = mesh.faces().filter(f -> f.status() == Status.INTERIOR)
				.map(HFace::edges).mapToInt(Sequence::count).average()
				.getAsDouble();

		assertEquals(3, n, 1E-6);
	}

	/**
	 *
	 */
	@Test
	public void averageLengthOfEdges() {
		ImportResult importResult = importGreenland();
		HMesh2D mesh = Assertions.present(importResult.toMesh2D());

		double length = HMeshBasicOperations.averageLengthOfEdges(mesh)
				.getAsDouble();

		assertEquals(5.6911524373375855, length, 1E-6);
	}

	/**
	 *
	 */
	@Test
	public void leftBound() {
		ImportResult importResult = importGreenland();
		HMesh2D mesh = Assertions.present(importResult.toMesh2D());
		double leftBound = HMeshBasicOperations.leftBound(mesh).getAsDouble();
		assertEquals(60, leftBound, 1E-6);
	}

	/**
	 *
	 */
	@Test
	public void vertexNeighbors() {
		ImportResult importResult = importGreenland();
		HMesh2D mesh = Assertions.present(importResult.toMesh2D());
		HVertex v = Assertions.present(mesh.vertices().findFirst());
		assertEquals(5, HMeshBasicOperations.vertexNeighbors(v).size());
		assertEquals(12, HMeshBasicOperations.vertexNeighborsLevel2(v).size());

	}

	/**
	 *
	 */
	@Test
	public void numVerticesWithDegree() {
		ImportResult importResult = importGreenland();
		HMesh2D mesh = Assertions.present(importResult.toMesh2D());

		int numVertices1 = HMeshBasicOperations.numVerticesWithDegree(mesh, 3);

		int numVertices2 = HMeshBasicOperations.numVerticesByDegree(mesh)
				.get(3);

		assertEquals(numVertices1, numVertices2);
	}

	/**
	 *
	 */
	@Test
	public void faceByNumberOfEdges() {
		ImportResult importResult = importGreenland();
		HMesh2D mesh = Assertions.present(importResult.toMesh2D());

		Map<Integer, List<HFace>> faces = HMeshBasicOperations
				.faceByNumberOfEdges(mesh);

		assertEquals(importResult.numFaces() + 1L, faces.get(3).size());
		assertEquals(null, faces.get(4));

	}

	/**
	 * @return
	 */
	private static ImportResult importGreenland() {
		return Utils.importFromMEditFile("greenland");
	}

	/**
	 *
	 */
	@Test
	public void triangulateQuads() {
		ImportResult importResult = Utils.importFromMEditFile("hexahexa_2x2x2");

		HMesh3D mesh = Assertions.present(importResult.toMesh3D());
		HMeshTester.check(mesh).numFaces(24);

		HMeshBasicOperations.triangulateQuads(mesh);
		HMeshTester.check(mesh).numFaces(48).export("hexahexa_2x2x2_triangle");
	}

	/**
	 *
	 */
	@Test
	public void simplify() {
		GridDrawer drawer = new GridDrawer();

		drawer.penFrom(0, 0).to(50, 0).to(100, 0).to(100, 100).to(50, 100)
				.to(0, 100).close();

		drawer.penFrom(100, 100).to(200, 100).to(200, 200).to(100, 200).close();

		drawer.penFrom(100, 100).to(125, 150).to(200, 200);
		drawer.penFrom(50, 0).to(50, 25);
		drawer.penFrom(50, 75).to(50, 100);

		drawer.penFrom(25, 25).to(50, 25).to(75, 25).to(75, 75).to(50, 75)
				.to(25, 75).close();

		drawer.penFrom(125, 150).to(100, 200);
		drawer.penFrom(0, 100).to(0, 125).to(25, 125).to(100, 100);
		drawer.penFrom(100, 0).to(75, 25);
		drawer.penFrom(25, 125).to(25, 150).to(50, 150).to(75, 150).to(100,
				200);

		HMesh2D mesh = drawer.mesh();
		HMeshTester.check(mesh).maxNumEdges(13).export("simplify");

		HMeshBasicOperations.simplify(mesh);

		HMeshTester.check(mesh).maxNumEdges(7).numTriangles(5)
				.export("simplify2");
	}
}
