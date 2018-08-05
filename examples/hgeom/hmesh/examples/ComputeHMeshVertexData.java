package hgeom.hmesh.examples;

import hgeom.hmesh.data.HDData;
import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HFace;
import hgeom.hmesh.elements.HMesh;
import hgeom.hmesh.elements.HVertex;

/**
 *
 * @author Pierre Beylot
 */
public final class ComputeHMeshVertexData {

	/**
	 *
	 */
	private ComputeHMeshVertexData() {
	}

	/**
	 * @param mesh
	 * @param faceData
	 * @return
	 */
	public static HDData<HVertex> fromFaceData(HMesh mesh,
			HDData<HFace> faceData) {

		HDData<HVertex> vertexData = mesh.createVertexDoubleData();

		vertexData.setAll(vertex -> {
			double mean = 0;
			int count = 0;

			// Iterate on all the faces connected to the vertex
			for (HFace face : vertex.outgoingEdges().map(HEdge::face)) {
				mean += faceData.get(face);
				count += 1;
			}

			return mean / count;
		});

		return vertexData;
	}

	/**
	 * Compute face values based on the average of neighbor face values
	 *
	 * @param mesh
	 * @param vertexData face data
	 * @return face data
	 */
	public static HDData<HVertex> averagedData(HMesh mesh,
			HDData<HVertex> vertexData) {

		HDData<HVertex> averagedVertexData = mesh.createVertexDoubleData();

		averagedVertexData.setAll(vertex -> {
			double mean = 0;
			int counter = 0;

			// Iterate on face's neighbor
			for (HVertex neighbor : vertex.neighbors()) {
				mean += vertexData.get(neighbor);
				counter++;
			}

			// Assign the computed value to the face
			return mean / counter;
		});

		return averagedVertexData;
	}
}
