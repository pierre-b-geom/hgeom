package hgeom.hmesh.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import hgeom.hmesh.data.HDData;
import hgeom.hmesh.elements.HMesh2D;
import hgeom.hmesh.elements.HVertex;
import hgeom.hmesh.examples.WalkInHMesh;

/**
 *
 * @author Pierre B.
 */
public final class HMeshWalkerSnippetTests {

	/**
	 *
	 */
	@Test
	public void towardMax() {
		HMesh2D mesh = Grid.mesh(100, 100);
		HDData<HVertex> vertexValues = mesh.createVertexDoubleData();
		vertexValues.setAll(v -> mesh.vertexX(v) + mesh.vertexY(v));

		HVertex start = Assertions
				.present(mesh.vertices().skip(150).findFirst());

		HVertex result = WalkInHMesh.towardMax(mesh, start, vertexValues);

		double x = mesh.vertexX(result);
		double y = mesh.vertexY(result);
		assertEquals(100, x, 1E-6);
		assertEquals(100, y, 1E-6);
		assertEquals(200, vertexValues.get(result), 1E-6);
	}
}
