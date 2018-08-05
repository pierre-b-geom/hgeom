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
public final class ComputeHMeshFaceData {

	/**
	 *
	 */
	private ComputeHMeshFaceData() {
	}

	/**
	 * Assign a value to each face of a specified mesh.
	 *
	 * @param mesh
	 * @param vertexData vertex data
	 * @return face data
	 */
	public static HDData<HFace> fromVertexData(HMesh mesh,
			HDData<HVertex> vertexData) {

		HDData<HFace> faceData = mesh.createFaceDoubleData();

		faceData.setAll(face -> {
			double mean = 0;
			int counter = 0;
			HEdge fe = face.edge();
			HEdge e = fe;

			// Iterate on each edge on the face border and compute the average
			// of the values assigned to all edges' head
			do {
				mean += vertexData.get(e.head());
				counter++;
				e = e.next();
			} while (e != fe);

			// Assign the computed value to the face
			return mean / counter;
		});

		return faceData;
	}

	/**
	 * Assign a value to each face of a specified mesh.
	 *
	 * @param mesh
	 * @param vertexData vertex data
	 * @return face data
	 */
	public static HDData<HFace> fromVertexData2(HMesh mesh,
			HDData<HVertex> vertexData) {

		HDData<HFace> faceData = mesh.createFaceDoubleData();

		faceData.setAll(face -> {
			double mean = 0;
			int counter = 0;

			// Iterate on each vertices on the face border and compute the
			// average of the values assigned to all vertices
			for (HVertex vertex : face.vertices()) {
				mean += vertexData.get(vertex);
				counter++;
			}

			// Assign the computed value to the face
			return mean / counter;
		});

		return faceData;
	}
}
