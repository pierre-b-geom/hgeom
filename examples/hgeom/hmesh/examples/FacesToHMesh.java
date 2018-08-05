package hgeom.hmesh.examples;

import java.util.List;
import java.util.Optional;

import hgeom.hmesh.core.HConversion;
import hgeom.hmesh.core.ToHMeshConverter;
import hgeom.hmesh.data.HDData;
import hgeom.hmesh.elements.Coord2DSource;
import hgeom.hmesh.elements.FaceSource;
import hgeom.hmesh.elements.HFace;
import hgeom.hmesh.elements.HMesh;
import hgeom.hmesh.elements.HMesh2D;
import hgeom.hmesh.elements.HVertex;

/**
 *
 * @author Pierre B.
 */
public final class FacesToHMesh {

	/**
	 *
	 */
	private FacesToHMesh() {
	}

	/**
	 * Build a half-edge data structure from a list of faces
	 *
	 * @param faces
	 * @return
	 */
	public static Optional<HMesh> createHMesh(List<int[]> faces) {
		return new FaceSource(faces).toHMesh();
	}

	/**
	 * Build a half-edge data structure from an array of faces and an array of
	 * 2D vertex coordinates
	 *
	 * @param faces
	 * @param vertexCoords
	 *
	 * @return
	 */
	public static Optional<HMesh2D> createHMesh2D(int[][] faces,
			double[][] vertexCoords) {

		// Create face source
		FaceSource faceSource = new FaceSource(faces);

		// Create vertex coordinates source
		Coord2DSource coord2DSource = new Coord2DSource(vertexCoords);

		// Build the half-edge data structure from the faces and the coordinates
		return faceSource.toHMesh(coord2DSource);
	}

	/**
	 * Build a half-edge data structure from an array of faces and an array of
	 * 2D vertex coordinates ; converts also an array of weights associated with
	 * the vertices
	 *
	 * @param faces
	 * @param vertexCoords
	 * @param vertexWeights
	 */
	public static void createHMesh2D(int[][] faces, double[][] vertexCoords,
			double[] vertexWeights) {

		// Create a faces source
		FaceSource faceSource = new FaceSource(faces);

		// Create a vertex coordinates source
		Coord2DSource coord2DSource = new Coord2DSource(vertexCoords);

		// Create a converter to HMesh
		ToHMeshConverter converter = new ToHMeshConverter();

		// Build the half-edge data structure from the faces and the coordinates
		// sources
		HConversion<HMesh2D> conversion = converter
				.convert(faceSource, coord2DSource)
				.orElseThrow(RuntimeException::new);

		// Get the created half-edge data structure
		HMesh2D mesh = conversion.mesh();

		// Map the original vertex weights to the half-mesh mesh vertices
		HDData<HVertex> meshVertexData = conversion
				.meshVertexDoubleData(i -> vertexWeights[i]);

		// Create a mesh faces data based upon the mesh vertices data
		HDData<HFace> meshFaceData = mesh.createFaceDoubleData();

		meshFaceData.setAll(f -> {
			double sum = 0;

			// Compute the sum of the value assigned to the vertices of the
			// face border
			for (HVertex v : f.vertices()) {
				sum += meshVertexData.get(v);
			}

			// Assign the computed value to the face
			return sum;
		});
	}
}
