package hgeom.hmesh.core;

import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntUnaryOperator;

import hgeom.hmesh.data.HBData;
import hgeom.hmesh.data.HDData;
import hgeom.hmesh.data.HData;
import hgeom.hmesh.data.HIData;
import hgeom.hmesh.elements.EdgeSource;
import hgeom.hmesh.elements.FaceSource;
import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HFace;
import hgeom.hmesh.elements.HMesh;
import hgeom.hmesh.elements.HVertex;

/**
 * Result of a conversion by a {@link ToHMeshConverter} of a collection of
 * {@link FaceSource faces} or {@link EdgeSource edges} into a half-edge data
 * structure
 * <p>
 * Provides also services for converting values associated with the initial data
 * into values associated with the elements of the generated half-edge data
 * structure. for instance, a collection of double values associated to the
 * initial collection of faces / edges could be mapped into a {@link HDData}
 * associated to the {@link HFace} / {@link HEdge} of the half-edge data
 * structure
 * <p>
 * The following example shows the conversion of both faces and vertex data:
 *
 * <pre>
 * <code>
 * {@code
 *
 * // Initial faces as arrays of indices to vertex
 * int[][] faces = ...
 *
 * // Some initial face data as an array of booleans
 * boolean[] faceStatus = ...
 *
 * // Some initial vertex data as array of doubles
 * double[] vertexWeights = ...
 *
 * // Converts initial faces
 * HConversion<HMesh> conversion = new ToHMeshConverter().
 *     convert(new FaceSource(faces)).orElseThrow(...)
 *
 * // Gets the generated half-edge data structure
 * HMesh mesh = conversion.mesh();
 *
 * // Converts initial face data into a HMesh dynamic face data
 * HBData<HFace> meshFaceStatus =
 *     conversion.meshFaceBooleanData(i -> faceStatus[i]);
 *
 * // Converts initial vertex data into a HMesh dynamic vertex data
 * HDData<HVertex> meshVertexWeights =
 *     conversion.meshVertexDoubleData(i -> vertexWeight[i]);
 * }
 * </code>
 * </pre>
 *
 * @author Pierre B.
 * @param <M> the type of generated half-edge data structure
 * @see ToHMeshConverter
 * @see HMesh
 * @see FaceSource
 * @see EdgeSource
 */
public final class HConversion<M extends HMesh> {

	/**
	 *
	 */
	private final M mesh;

	/**
	 *
	 */
	private final HIData<HVertex> vertexIndices;

	/**
	 *
	 */
	private final HIData<HEdge> edgeIndices;

	/**
	 *
	 */
	private final HIData<HFace> faceIndices;

	/**
	 * Interne a {@link ToHMeshConverter}
	 *
	 * @param mesh
	 * @param vertexIndices
	 * @param edgeIndices
	 * @param faceIndices
	 */
	HConversion(M mesh, HIData<HVertex> vertexIndices,
			HIData<HEdge> edgeIndices, HIData<HFace> faceIndices) {

		this.mesh = Objects.requireNonNull(mesh);
		this.vertexIndices = Objects.requireNonNull(vertexIndices);
		this.edgeIndices = edgeIndices;
		this.faceIndices = faceIndices;
	}

	/**
	 * @return the generated half-edge data structure
	 */
	public M mesh() {
		return mesh;
	}

	/**
	 * Converts vertex data into a {@link HData} associated with the vertices of
	 * the result half-edge data structure
	 *
	 * @param vertexIndexToObj a function that returns a data value according to
	 *                         a initial vertex index
	 * @return the data associated with the vertices of the result half-edge
	 *         data structure
	 */
	public <D> HData<HVertex, D> meshVertexData(
			IntFunction<D> vertexIndexToObj) {

		HData<HVertex, D> data = mesh.createVertexData();
		data.setAll(v -> vertexIndexToObj.apply(vertexIndices.get(v)));
		return data;
	}

	/**
	 * Converts vertex boolean data into a {@link HBData} associated with the
	 * vertices of the result half-edge data structure
	 *
	 * @param vertexIndexToBoolean a function that returns a boolean value
	 *                             according to a initial vertex index
	 * @return the data associated with the vertices of the result half-edge
	 *         data structure
	 */
	public HBData<HVertex> meshVertexBooleanData(
			IntPredicate vertexIndexToBoolean) {

		HBData<HVertex> data = mesh.createVertexBooleanData();
		data.setAll(v -> vertexIndexToBoolean.test(vertexIndices.get(v)));
		return data;
	}

	/**
	 * Converts vertex integer data into a {@link HIData} associated with the
	 * vertices of the result half-edge data structure
	 *
	 * @param vertexIndexToInt a function that returns a integer value according
	 *                         to a initial vertex index
	 * @return the data associated with the vertices of the result half-edge
	 *         data structure
	 */
	public HIData<HVertex> meshVertexIntData(
			IntUnaryOperator vertexIndexToInt) {

		HIData<HVertex> data = mesh.createVertexIntData();
		data.setAll(v -> vertexIndexToInt.applyAsInt(vertexIndices.get(v)));
		return data;
	}

	/**
	 * Converts vertex double data into a {@link HDData} associated with the
	 * vertices of the result half-edge data structure
	 *
	 * @param vertexIndexToDouble a function that returns a double value
	 *                            according to a initial vertex index
	 * @return the data associated with the vertices of the result half-edge
	 *         data structure
	 */
	public HDData<HVertex> meshVertexDoubleData(
			IntToDoubleFunction vertexIndexToDouble) {

		HDData<HVertex> data = mesh.createVertexDoubleData();

		data.setAll(
				v -> vertexIndexToDouble.applyAsDouble(vertexIndices.get(v)));

		return data;
	}

	/**
	 * Converts edge data into a {@link HData} associated with the edges of the
	 * result half-edge data structure
	 *
	 * @param edgeIndexToObj a function that returns a data value according to a
	 *                       initial edge index
	 * @return the data associated with the edges of the result half-edge data
	 *         structure
	 */
	public <D> HData<HEdge, D> meshEdgeData(IntFunction<D> edgeIndexToObj) {
		requireEdgeIndices();
		HData<HEdge, D> data = mesh.createEdgeData();
		data.setAll(e -> edgeIndexToObj.apply(edgeIndices.get(e)));
		return data;
	}

	/**
	 * Converts edge boolean data into a {@link HBData} associated with the
	 * edges of the result half-edge data structure
	 *
	 * @param edgeIndexToBoolean a function that returns a boolean value
	 *                           according to a initial edge index
	 * @return the data associated with the edges of the result half-edge data
	 *         structure
	 */
	public HBData<HEdge> meshEdgeBooleanData(IntPredicate edgeIndexToBoolean) {
		requireEdgeIndices();
		HBData<HEdge> data = mesh.createEdgeBooleanData();
		data.setAll(e -> edgeIndexToBoolean.test(edgeIndices.get(e)));
		return data;
	}

	/**
	 * Converts vertex integer data into a {@link HIData} associated with the
	 * edges of the result half-edge data structure
	 *
	 * @param edgeIndexToInt a function that returns a integer value according
	 *                       to a initial edge index
	 * @return the data associated with the edges of the result half-edge data
	 *         structure
	 */
	public HIData<HEdge> meshEdgeIntData(IntUnaryOperator edgeIndexToInt) {
		requireEdgeIndices();
		HIData<HEdge> data = mesh.createEdgeIntData();
		data.setAll(e -> edgeIndexToInt.applyAsInt(edgeIndices.get(e)));
		return data;
	}

	/**
	 * Converts vertex double data into a {@link HDData} associated with the
	 * edges of the result half-edge data structure
	 *
	 * @param edgeIndexToDouble a function that returns a double value according
	 *                          to a initial edge index
	 * @return the data associated with the edges of the result half-edge data
	 *         structure
	 */
	public HDData<HEdge> meshEdgeDoubleData(
			IntToDoubleFunction edgeIndexToDouble) {

		requireEdgeIndices();
		HDData<HEdge> data = mesh.createEdgeDoubleData();
		data.setAll(e -> edgeIndexToDouble.applyAsDouble(edgeIndices.get(e)));
		return data;
	}

	/**
	 *
	 */
	private void requireEdgeIndices() {
		if (edgeIndices == null) {
			throw new IllegalStateException(
					"edge data not available for this conversion");
		}
	}

	/**
	 * Converts face data into a {@link HData} associated with the faces of the
	 * result half-edge data structure
	 *
	 * @param faceIndexToObj a function that returns a data value according to a
	 *                       initial face index
	 * @return the data associated with the faces of the result half-edge data
	 *         structure
	 */
	public <D> HData<HFace, D> meshFaceData(IntFunction<D> faceIndexToObj) {
		requirefaceIndices();
		HData<HFace, D> data = mesh.createFaceData();

		// Attention aux indices egal a -1 pour les faces externes
		data.setAll(f -> {
			int faceIndex = faceIndices.get(f);
			return faceIndex == -1 ? null : faceIndexToObj.apply(faceIndex);
		});

		return data;
	}

	/**
	 * Converts face boolean data into a {@link HBData} associated with the
	 * faces of the result half-edge data structure
	 *
	 * @param faceIndexToBoolean a function that returns a boolean value
	 *                           according to a initial face index
	 * @return the data associated with the faces of the result half-edge data
	 *         structure
	 * @throws IllegalStateException if the result half-edge data structure has
	 *                               been generated from a list of edges (
	 */
	public HBData<HFace> meshFaceBooleanData(IntPredicate faceIndexToBoolean) {
		requirefaceIndices();
		HBData<HFace> data = mesh.createFaceBooleanData();

		data.setAll(f -> {
			int faceIndex = faceIndices.get(f);
			return faceIndex != -1 && faceIndexToBoolean.test(faceIndex);
		});

		return data;
	}

	/**
	 * Converts vertex integer data into a {@link HIData} associated with the
	 * faces of the result half-edge data structure
	 *
	 * @param faceIndexToInt a function that returns a integer value according
	 *                       to a initial face index
	 * @return the data associated with the faces of the result half-edge data
	 *         structure
	 */
	public HIData<HFace> meshFaceIntData(IntUnaryOperator faceIndexToInt) {
		requirefaceIndices();
		HIData<HFace> data = mesh.createFaceIntData();

		data.setAll(f -> {
			int faceIndex = faceIndices.get(f);
			return faceIndex == -1 ? 0 : faceIndexToInt.applyAsInt(faceIndex);
		});

		return data;
	}

	/**
	 * Converts vertex double data into a {@link HDData} associated with the
	 * faces of the result half-edge data structure
	 *
	 * @param faceIndexToDouble a function that returns a double value according
	 *                          to a initial face index
	 * @return the data associated with the faces of the result half-edge data
	 *         structure
	 */
	public HDData<HFace> meshFaceDoubleData(
			IntToDoubleFunction faceIndexToDouble) {

		requirefaceIndices();
		HDData<HFace> data = mesh.createFaceDoubleData();

		data.setAll(f -> {
			int faceIndex = faceIndices.get(f);

			return faceIndex == -1 ? 0
					: faceIndexToDouble.applyAsDouble(faceIndex);
		});

		return data;
	}

	/**
	 *
	 */
	private void requirefaceIndices() {
		if (faceIndices == null) {
			throw new IllegalStateException(
					"face data not available for this conversion");
		}
	}
}
