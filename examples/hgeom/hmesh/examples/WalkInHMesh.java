package hgeom.hmesh.examples;

import hgeom.hmesh.core.HMeshWalker;
import hgeom.hmesh.data.HDData;
import hgeom.hmesh.elements.HMesh;
import hgeom.hmesh.elements.HVertex;

/**
 *
 * @author Pierre B.
 */
public final class WalkInHMesh {

	/**
	 *
	 */
	private WalkInHMesh() {
	}

	/**
	 * Walks withing a {@link HMesh} toward a maximal value starting at a given
	 * vertex
	 *
	 * @param mesh       the mesh
	 * @param start      the starting vertex
	 * @param vertexData data assigned to the mesh vertices
	 * @return the vertex with the local maximal value
	 */
	public static HVertex towardMax(HMesh mesh, HVertex start,
			HDData<HVertex> vertexData) {

		return new HMeshWalker(mesh).find(start, vertexData.maxOperator());
	}
}
