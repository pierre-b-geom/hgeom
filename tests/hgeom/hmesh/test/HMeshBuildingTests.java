package hgeom.hmesh.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import hgeom.hmesh.core.HConversion;
import hgeom.hmesh.core.ToHMeshConverter;
import hgeom.hmesh.data.HData;
import hgeom.hmesh.elements.Coord2DSource;
import hgeom.hmesh.elements.EdgeSource;
import hgeom.hmesh.elements.FaceSource;
import hgeom.hmesh.elements.HFace;
import hgeom.hmesh.elements.HFace.Status;
import hgeom.hmesh.elements.HMesh;
import hgeom.hmesh.elements.HMesh2D;
import hgeom.hmesh.elements.HVertex;
import hgeom.hmesh.examples.EdgesToHMesh;
import hgeom.hmesh.examples.FacesToHMesh;
import hgeom.hmesh.test.Importer.ImportResult;

/**
 *
 * @author Pierre B.
 */
public final class HMeshBuildingTests {

	/**
	 * Ajout d'une face a 2 elements ou avec multiple reference a un sommet
	 * impossible
	 */
	@Test
	public void failures() {
		int[] v0 = v(0, 0);
		int[] v1 = v(1, 1);
		int[] v2 = v(2, 2);
		int[] v3 = v(3, 3);
		int[] v4 = v(4, 4);
		int[] v5 = v(5, 5);
		int[] v6 = v(6, 6);
		int[] v7 = v(7, 7);
		int[] v8 = v(8, 8);
		int[] v9 = v(9, 9);

		// Face a 2 sommets
		int[][][] faces1 = { { v0, v1 } };
		HMeshTester.checkFromGridFaces(faces1).empty();

		// Face avec 2 fios le meme sommet
		int[][][] faces2 = { { v0, v1, v0 }, { v4, v5 } };
		HMeshTester.checkFromGridFaces(faces2).empty();

		// Une arete presente dans 3 faces : la derniere face est rejetee
		int[][][] faces3 = { { v0, v6, v3, v2 }, { v3, v2, v4, v7 },
				{ v8, v9, v2, v3 } };

		HMeshTester.checkFromGridFaces(faces3).numFaces(3);
	}

	/**
	 *
	 */
	@Test
	public void facesToHMesh2D() {
		double[][] vertexCoords = { { 0, 0 }, { 0, 1 }, { 1, 0 }, { 1, 1 },
				{ 0, 2 }, { 1, 2 } };

		int[][] faces = { { 0, 1, 3, 2 }, { 1, 4, 5, 3 } };

		Optional<HMesh2D> mesh = FacesToHMesh.createHMesh2D(faces,
				vertexCoords);

		HMeshTester.check(mesh).numFaces(3).numEdges(14).numVertices(6)
				.numExteriorFaces(1);
	}

	/**
	 *
	 */
	@Test
	public void edgesToHMesh2D() {
		double[][] vertexCoords = { { 0, 0 }, { 0, 1 }, { 1, 0 }, { 1, 1 },
				{ 0, 2 }, { 1, 2 } };

		int[][] edges = { { 0, 1 }, { 1, 4 }, { 2, 0 }, { 1, 3 }, { 5, 3 },
				{ 4, 5 }, { 3, 1 }, { 3, 2 } };

		Optional<HMesh2D> mesh = EdgesToHMesh.createHMesh2D(edges,
				vertexCoords);

		HMeshTester.check(mesh).numFaces(3).numEdges(14).numVertices(6)
				.numExteriorFaces(0);
	}

	/**
	 * Construction d'un triangle
	 */
	@Test
	public void triangle() {
		int[][] triangle = { v(0, 0), v(1, 0), v(0, 1) };
		int[][][] faces = { triangle };

		HMeshTester.checkFromGridFaces(faces).numFaces(2).numEdges(6)
				.numVertices(3).export("triangleFromFaces");

		HMeshTester.checkFromGridEdges(Utils.toEdges(triangle)).numFaces(2)
				.numEdges(6).numVertices(3).export("triangleFromEdges");
	}

	/**
	 * Construction d'un quadrilatere
	 */
	@Test
	public void quad() {
		int[][][] quad = { { v(0, 0), v(1, 0), v(1, 1), v(0, 1) } };

		HMeshTester.checkFromGridFaces(quad).numFaces(2).numEdges(8)
				.numVertices(4).numExteriorFaces(1).export("quadFromFaces");

		HMeshTester.checkFromGridEdges(Utils.toEdges(quad)).numFaces(2)
				.numEdges(8).numVertices(4).export("quadFromEdges");
	}

	/**
	 * Construction a partir de 2 quadrilateres ayant une arete en commun
	 */
	@Test
	public void twoQuads() {
		int[][] quad1 = { v(0, 0), v(10, 0), v(10, 10), v(0, 10) };
		int[][] quad2 = { v(10, 0), v(10, 10), v(20, 10), v(20, 0) };
		int[][][] faces = { quad1, quad2 };

		HMeshTester.checkFromGridFaces(faces).numFaces(3).numEdges(14)
				.numVertices(6).minVertexEdges(2).maxVertexEdges(3)
				.numExteriorFaces(1).export("twoQuadsFromFaces");

		HMeshTester.checkFromGridEdges(Utils.toEdges(faces)).numFaces(3)
				.numEdges(14).numVertices(6).minVertexEdges(2).maxVertexEdges(3)
				.export("twoQuadsFromEdges");
	}

	/**
	 * Construction a partir de 2 faces n'ayant qu'un sommet en commun : 2 bords
	 * externes vont etre construit ayant ce sommet en commun
	 */
	@Test
	public void quadAndTriangle() {
		int[][] quad = { v(0, 0), v(10, 0), v(10, 10), v(0, 10) };
		int[][] triangle = { v(10, 0), v(20, 0), v(20, 10) };
		int[][][] faces = { triangle, quad };

		// Le triangle et le quadrilatere ont un seul sommet commun. 2 bords
		// externes sont crees et le sommet commun est partage par ces 2 bords
		HMeshTester.checkFromGridFaces(faces).numFaces(3).numEdges(14)
				.numVertices(6).numExteriorFaces(1)
				.export("quadAndTriangleFromFaces");

		HMeshTester.checkFromGridEdges(Utils.toEdges(faces)).numFaces(3)
				.numEdges(14).numVertices(6).export("quadAndTriangleFromEdges");
	}

	/**
	 * Construction d'un quadrilatere perce d'un triangle
	 */
	@Test
	public void holeInsideQuad() {
		int[][] quad = { v(0, 0), v(10, 0), v(10, 10), v(0, 10) };
		int[][] hole = { v(5, 8), v(8, 2), v(2, 2) };
		int[][][] faces = { quad, hole };

		HMeshTester.checkFromGridFaces(faces).numFaces(4).numEdges(14)
				.numVertices(7).numExteriorFaces(2).export("holeInsideQuad");
	}

	/**
	 *
	 */
	@Test
	public void multiQuads() {
		int[][] quad = { v(2, 2), v(9, 2), v(12, 2), v(12, 5), v(12, 12),
				v(5, 12), v(2, 12), v(2, 9) };

		int[][] innerQuad = { v(5, 5), v(9, 5), v(9, 9), v(5, 9) };
		int[][] innerLeftTopQuad = { v(5, 12), v(2, 12), v(2, 9), v(5, 9) };
		int[][] innerRightBottomQuad = { v(9, 2), v(12, 2), v(12, 5), v(9, 5) };
		int[][] outerLeftTopQuad = { v(2, 12), v(0, 12), v(0, 14), v(2, 14) };

		int[][] outerRightBottomQuad = { v(12, 2), v(14, 2), v(14, 0),
				v(12, 0) };

		int[][][] faces = { quad, innerRightBottomQuad, innerLeftTopQuad,
				outerLeftTopQuad, outerRightBottomQuad, innerQuad };

		HMeshTester.checkFromGridFaces(faces).numFaces(8).numEdges(48)
				.numVertices(18).numExteriorFaces(3)
				.export("multiQuadsFromFaces");

		HMeshTester.checkFromGridEdges(Utils.toEdges(faces)).numFaces(8)
				.numEdges(48).numVertices(18).export("multiQuadsFromEdges");
	}

	/**
	 *
	 */
	@Test
	public void trianglesInsideQuad() {
		int[][] quad = { v(2, 2), v(9, 2), v(12, 2), v(12, 5), v(12, 12),
				v(5, 12), v(2, 12), v(2, 9) };

		int[][] innerLeftTopTriangle = { v(2, 12), v(5, 9), v(5, 7) };
		int[][] innerRightBottomTriangle = { v(12, 2), v(9, 4), v(9, 6) };
		int[][] innerTriangle = { v(5, 9), v(5, 7), v(9, 6) };

		int[][][] faces = { quad, innerRightBottomTriangle,
				innerLeftTopTriangle, innerTriangle };

		HMeshTester.checkFromGridFaces(faces).numFaces(6).numEdges(32)
				.numVertices(12).numExteriorFaces(2)
				.export("trianglesInsideQuadFromFaces");

		HMeshTester.checkFromGridEdges(Utils.toEdges(faces)).numFaces(6)
				.numEdges(32).numVertices(12)
				.export("trianglesInsideQuadFromEdges");
	}

	/**
	 * Aretes sont ordonnees aleatoirement
	 */
	@Test
	public void holeInsideQuadFromEdges() {
		int[][][] edges = { { v(8, 2), v(2, 2) }, { v(10, 0), v(10, 10) },
				{ v(8, 2), v(5, 8) }, { v(5, 8), v(2, 2) },
				{ v(10, 0), v(0, 0) }, { v(10, 10), v(0, 10) },
				{ v(0, 10), v(0, 0) } };

		HMeshTester.checkFromGridEdges(Arrays.asList(edges)).numFaces(4)
				.numExteriorFaces(0).numEdges(14).numVertices(7)
				.export("holeInsideQuadFromEdges");
	}

	/**
	 * Construction a partir d'une face percee d'un trou touchant le bord
	 * externe. Ne marche pas pour l'instant mais marchera quand resolution du
	 * probleme des deconnections
	 */
	@Test
	public void triangleInsideQuad() {
		int[][] face1 = { v(0, 0), v(10, 0), v(10, 10), v(0, 10) };
		int[][] face2 = { v(5, 8), v(2, 2), v(10, 0) };
		int[][][] faces = { face1, face2 };

		HMeshTester.checkFromGridFaces(faces).numFaces(3).numVertices(6)
				.numExteriorFaces(1).export("triangleInsideQuadFromFaces");

		HMeshTester.checkFromGridEdges(Utils.toEdges(faces)).numFaces(3)
				.numVertices(6).export("triangleInsideQuadFromEdges");
	}

	/**
	 * Construction d'un quadrilatere perce de 2 triangles
	 */
	@Test
	public void twoHolesInsideQuad() {
		int[][] quad = { v(0, 0), v(10, 0), v(10, 10), v(0, 10) };
		int[][] hole1 = { v(5, 3), v(8, 2), v(2, 2) };
		int[][] hole2 = { v(2, 6), v(8, 6), v(5, 8) };
		int[][][] faces = { quad, hole1, hole2 };

		HMeshTester.checkFromGridFaces(faces).numFaces(6).numEdges(20)
				.numVertices(10).numExteriorFaces(3)
				.export("twoHolesInsideQuadFromFaces");

		HMeshTester.checkFromGridEdges(Utils.toEdges(faces)).numFaces(6)
				.numEdges(20).numVertices(10)
				.export("twoHolesInsideQuadFromEdges");
	}

	/**
	 * Construction d'un quadrilatere perce d'un trou contenant une face
	 */
	@Test
	public void faceInsideHole() {
		int[][] quad = { v(0, 0), v(10, 0), v(10, 10), v(0, 10) };

		int[][] hole = { v(1, 2), v(2, 1), v(5, 1), v(7, 1), v(9, 3), v(9, 5),
				v(9, 9), v(7, 7), v(5, 9), v(3, 8), v(1, 8), v(1, 5) };

		int[][] faceInsideHole = { v(1, 2), v(2, 1), v(5, 1), v(7, 1), v(9, 3),
				v(9, 5), v(9, 9), v(7, 7), v(5, 9) };

		int[][][] faces = { quad, hole, faceInsideHole };

		HMeshTester.checkFromGridFaces(faces).numFaces(5)
				.numVertices(quad.length + hole.length).numExteriorFaces(2)
				.export("faceInsideHoleFromFaces");

		HMeshTester.checkFromGridEdges(Utils.toEdges(faces)).numFaces(5)
				.numVertices(quad.length + hole.length)
				.export("faceInsideHoleFromEdges");
	}

	/**
	 * Construction d'un quadrilatere perce d'un trou contenant un triangle
	 * creant 2 trous ayant un sommet en commun. La construction va generer 2
	 * trous
	 */
	@Test
	public void faceInsideHole2() {
		int[][] quad = { v(0, 0), v(10, 0), v(10, 10), v(0, 10) };

		int[][] hole = { v(1, 2), v(2, 1), v(5, 1), v(7, 1), v(9, 3), v(9, 5),
				v(9, 9), v(7, 7), v(5, 9), v(3, 8), v(1, 8), v(1, 5) };

		int[][] faceInsideHole = { v(1, 2), v(9, 3), v(9, 5) };
		int[][][] faces = { quad, hole, faceInsideHole };

		// Les 2 faces, la face externe et les 2 sous-trous
		HMeshTester.checkFromGridFaces(faces).numFaces(6)
				.numVertices(quad.length + hole.length).numExteriorFaces(3)
				.export("faceInsideHole2FromFaces");

		HMeshTester.checkFromGridEdges(Utils.toEdges(faces)).numFaces(6)
				.numVertices(quad.length + hole.length)
				.export("faceInsideHole2FromEdges");
	}

	/**
	 * Construction a partir dz 2 faces dont l'une recouvre l'autre
	 * partiellement
	 */
	@Test
	public void intersectingFaces() {
		int[][] face1 = { v(0, 0), v(10, 0), v(20, 0), v(20, 10), v(10, 10),
				v(0, 10) };

		int[][] face2 = { v(0, 0), v(10, 0), v(10, 10), v(10, 20), v(0, 20),
				v(0, 10) };

		int[][][] faces = { face1, face2 };

		HMeshTester.checkFromGridFaces(faces).numFaces(4).numVertices(8)
				.numExteriorFaces(4).export("intersectingFacesFromFaces");

		HMeshTester.checkFromGridEdges(Utils.toEdges(faces)).numFaces(4)
				.numVertices(8).export("intersectingFacesFromEdges");
	}

	/**
	 * Construction a partir d'une face percee d'un trou contenant 2 autres
	 * faces qui s'enchevetrent - La construction de bords internes au trou doit
	 * echouer
	 */
	@Test(expected = IllegalArgumentException.class)
	public void intersectingFacesInsideHole() {
		int[][] quad = { v(0, 0), v(10, 0), v(10, 10), v(0, 10) };

		int[][] hole = { v(1, 2), v(2, 1), v(5, 1), v(7, 1), v(9, 3), v(9, 5),
				v(9, 9), v(7, 7), v(5, 9), v(3, 8), v(1, 8), v(1, 5) };

		int[][] faceInsideHole1 = { v(1, 2), v(9, 3), v(9, 5) };
		int[][] faceInsideHole2 = { v(1, 2), v(2, 1), v(5, 1), v(9, 5) };
		int[][][] faces = { quad, hole, faceInsideHole1, faceInsideHole2 };

		HMeshTester.checkFromGridFaces(faces);
	}

	/**
	 *
	 */
	@Test
	public void facesInsideHole2() {
		int[][] quad = { v(0, 0), v(10, 0), v(10, 10), v(0, 10) };

		int[][] hole = { v(1, 2), v(2, 1), v(5, 1), v(7, 1), v(9, 3), v(9, 5),
				v(9, 9), v(7, 7), v(5, 9), v(3, 8), v(1, 8), v(1, 5) };

		int[][] faceInsideHole = { v(1, 2), v(9, 3), v(9, 5) };
		int[][][] faces = { quad, hole, faceInsideHole };

		HMeshTester.checkFromGridFaces(faces).numFaces(6).numExteriorFaces(3)
				.numVertices(quad.length + hole.length)
				.export("facesInsideHole2FromFaces");

		HMeshTester.checkFromGridEdges(Utils.toEdges(faces)).numFaces(6)
				.numVertices(quad.length + hole.length)
				.export("facesInsideHole2FromEdges");
	}

	/**
	 * Construction a partir d'une face percee d'un trou contenant 2 autres
	 * faces qui separent le trou en 2 sous-trous
	 */
	@Test
	public void facesInsideHole() {
		int[][] quad = { v(0, 0), v(10, 0), v(10, 10), v(0, 10) };

		int[][] hole = { v(1, 2), v(2, 1), v(5, 1), v(7, 1), v(9, 3), v(9, 5),
				v(9, 9), v(7, 7), v(5, 9), v(3, 8), v(1, 8), v(1, 5) };

		int[][] faceInsideHole1 = { v(1, 2), v(9, 3), v(9, 5) };
		int[][] faceInsideHole2 = { v(1, 2), v(2, 1), v(5, 1), v(9, 3) };

		int[][][] faces = { quad, hole, faceInsideHole1, faceInsideHole2 };

		HMeshTester.checkFromGridFaces(faces).numFaces(7)
				.numVertices(quad.length + hole.length).numExteriorFaces(3)
				.export("facesInsideHoleFromFaces");

		HMeshTester.checkFromGridEdges(Utils.toEdges(faces)).numFaces(7)
				.numVertices(quad.length + hole.length)
				.export("facesInsideHoleFromEdges");
	}

	/**
	 *
	 */
	@Test
	public void quadInsideLosange() {
		int[][] losange = { v(2, 0), v(3, 1), v(4, 2), v(3, 3), v(2, 4),
				v(1, 3), v(0, 2), v(1, 1) };

		int[][] quad = { v(3, 1), v(3, 3), v(1, 3), v(1, 1) };
		int[][][] faces = { losange, quad };

		HMeshTester.checkFromGridFaces(faces).numFaces(6)
				.numVertices(losange.length).numExteriorFaces(4)
				.export("quadInsideLosangeFromFaces");

		HMeshTester.checkFromGridEdges(Utils.toEdges(faces)).numFaces(6)
				.numVertices(losange.length)
				.export("quadInsideLosangeFromEdges");
	}

	/**
	 *
	 */
	@Test
	public void grid() {
		int numI = 100;
		int numJ = 100;
		List<int[][]> faces = new ArrayList<>();

		for (int i = 0; i < numI; i += 5) {
			for (int j = 0; j < numJ; j += 5) {
				faces.add(new int[][] { v(i, j), v(i + 5, j), v(i + 5, j + 5),
						v(i, j + 5) });

				if (i % 2 == 0) {
					faces.add(new int[][] { v(i, j), v(i + 2, j + 4),
							v(i + 5, j + 5) });
				}

				else {
					faces.add(new int[][] { v(i, j), v(i + 4, j + 2),
							v(i + 5, j + 5) });
				}
			}
		}

		int[][][] faceArray = new int[faces.size()][][];
		faces.toArray(faceArray);
		HMeshTester.checkFromGridEdges(Utils.toEdges(faceArray))
				.export("gridFromEdges");
	}

	/**
	 * Performance d'une construction a partir d'un ensemble de millier de faces
	 */
	@Test
	public void facesPerformance() {
		int numI = 500;
		int numJ = 1000;
		List<int[][]> faces = new ArrayList<>();

		for (int i = 0; i < numI; i++) {
			for (int j = 0; j < numJ; j++) {
				faces.add(new int[][] { v(i, j), v(i + 1, j), v(i + 1, j + 1),
						v(i, j + 1) });
			}
		}

		Runnable runnable = () -> Grid.meshFromFaces(faces);
		RunDuration.of(runnable, 5).printOut("build from faces");
	}

	/**
	 *
	 */
	@Test
	public void edgeStreamPerformance() {
		int numI = 900;
		int numJ = 900;
		List<int[][]> faces = new ArrayList<>();

		for (int i = 0; i < numI; i++) {
			for (int j = 0; j < numJ; j++) {
				faces.add(new int[][] { v(i, j), v(i + 1, j), v(i + 1, j + 1),
						v(i, j + 1) });
			}
		}

		HMesh mesh = Grid.meshFromFaces(faces);

		RunDuration.of(() -> mesh.edges().count(), 20)
				.printOut("edges streaming");
	}

	/**
	 * Performance d'une construction a partir d'un ensemble de millier d'aretes
	 */
	@Test
	public void edgesPerformance() {
		int numI = 500;
		int numJ = 1000;
		List<int[][]> edges = new ArrayList<>();

		for (int i = 0; i < numI; i++) {
			for (int j = 0; j < numJ; j++) {
				edges.add(new int[][] { v(i, j), v(i + 1, j) });
				edges.add(new int[][] { v(i + 1, j), v(i + 1, j + 1) });
				edges.add(new int[][] { v(i + 1, j + 1), v(i, j + 1) });
				edges.add(new int[][] { v(i, j + 1), v(i, j) });
			}
		}

		Runnable runnable = () -> Grid.meshFromEdges(edges);
		RunDuration.of(runnable, 5).printOut("build from edges");
	}

	/**
	 *
	 */
	@Test
	public void hexahexa2x2x2() {
		ImportResult importResult = Utils.importFromMEditFile("hexahexa_2x2x2");

		HMeshTester.check(importResult.toMesh3D()).numVertices(26)
				.numInteriorFaces(24).export("hexahexa_2x2x2");
	}

	/**
	 *
	 */
	@Test
	public void m299() {
		ImportResult importResult = Utils.importFromOffFile("m299");

		HMeshTester.check(importResult.toMesh3D())
				.numVertices(importResult.numCoords())
				.numInteriorFaces(importResult.numFaces()).export("m299");
	}

	/**
	 *
	 */
	@Test
	public void m395() {
		ImportResult importResult = Utils.importFromOffFile("m395");

		HMeshTester.check(importResult.toMesh3D())
				.numVertices(importResult.numCoords())
				.numInteriorFaces(importResult.numFaces()).numExteriorFaces(134)
				.export("m299");
	}

	/**
	 *
	 */
	@Test
	public void snippets() {
		ImportResult importResult = Utils.importFromMEditFile("greenland");

		double[][] vertexCoords = importResult.coords()
				.toArray(double[][]::new);

		int[][] faces = importResult.faces().toArray(int[][]::new);
		int[][] edges = importResult.edges().toArray(new int[0][]);

		// Sans donnees associees
		FaceSource faceSource = new FaceSource(faces);
		Coord2DSource coord2DSource = new Coord2DSource(vertexCoords);
		ToHMeshConverter converter = new ToHMeshConverter();

		HMesh2D mesh = Assertions.present(converter
				.convert(faceSource, coord2DSource).map(HConversion::mesh));

		assertEquals(vertexCoords.length, mesh.vertices().count());

		assertEquals(faces.length, mesh.faces()
				.filter(f -> f.status() == Status.INTERIOR).count());

		// Avec donnees attachees aux sommets
		faceSource = new FaceSource(faces);
		converter = new ToHMeshConverter();

		HConversion<HMesh2D> conversion = Assertions
				.present(converter.convert(faceSource, coord2DSource));

		mesh = conversion.mesh();

		HData<HVertex, String> vertexIds = conversion
				.meshVertexData(Integer::toString);

		assertEquals(vertexCoords.length, mesh.vertices().count());

		assertEquals(faces.length, mesh.faces()
				.filter(f -> f.status() == Status.INTERIOR).count());

		int numComplementaryFaces = (int) mesh.faces()
				.filter(f -> f.status() == Status.BOUNDARY).count();

		int[] sortedIds = mesh.vertices().map(vertexIds::get)
				.map(String.class::cast).mapToInt(Integer::parseInt).sorted()
				.toArray();

		for (int i = 0; i < sortedIds.length; i++) {
			assertEquals(i, sortedIds[i]);
		}

		// Avec donnees attachees aux faces
		faceSource = new FaceSource(faces);
		converter = new ToHMeshConverter();

		conversion = Assertions
				.present(converter.convert(faceSource, coord2DSource));

		mesh = conversion.mesh();
		HData<HFace, String> faceIds = conversion
				.meshFaceData(Integer::toString);

		assertEquals(vertexCoords.length, mesh.vertices().count());

		assertEquals(faces.length, mesh.faces()
				.filter(f -> f.status() == Status.INTERIOR).count());

		sortedIds = mesh.faces().filter(f -> f.status() == Status.INTERIOR)
				.map(faceIds::get).map(String.class::cast)
				.mapToInt(Integer::parseInt).sorted().toArray();

		for (int i = 0; i < sortedIds.length; i++) {
			assertEquals(i, sortedIds[i]);
		}

		// Avec construction a partir des aretes
		EdgeSource edgeSource = new EdgeSource(edges);
		mesh = Assertions.present(edgeSource.toHMesh(coord2DSource));

		// Toutes les faces sont primaires dans les maillages construit avec des
		// aretes : il est necessaire d'ajouter le nombre de faces
		// complementaires
		assertEquals(faces.length + (long) numComplementaryFaces,
				mesh.faces().count());

		// Avec 2eme construction a partir des aretes
		int[] interleavedEdges = new int[edges.length * 2];

		Arrays.setAll(interleavedEdges,
				i -> i % 2 == 0 ? edges[i / 2][0] : edges[i / 2][1]);

		edgeSource = new EdgeSource(i -> interleavedEdges[2 * i],
				i -> interleavedEdges[2 * i + 1], 0, edges.length);

		mesh = Assertions.present(edgeSource.toHMesh(coord2DSource));

		HMeshTester.check(mesh).numFaces(faces.length + numComplementaryFaces)
				.export("greenland");
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
}// 755
