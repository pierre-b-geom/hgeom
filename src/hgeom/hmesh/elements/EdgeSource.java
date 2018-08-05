package hgeom.hmesh.elements;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import hgeom.hmesh.core.HConversion;
import hgeom.hmesh.core.ToHMeshConverter;
import hgeom.hmesh.winding.PolygonWindingProvider;

/**
 * Supplier of an indexed collection of edges. Each edge is defined as a array
 * of indices to 2 vertices
 *
 * @author Pierre B.
 */
public final class EdgeSource {

	/**
	 *
	 */
	private final IntUnaryOperator v1Indices;

	/**
	 *
	 */
	private final IntUnaryOperator v2Indices;

	/**
	 *
	 */
	private final Supplier<IntStream> indexSupplier;

	/**
	 * Constructs a edge source from the specified list of edges
	 *
	 * @param edges a list of edges in which each edge is defined as a array of
	 *              indices to 2 vertices
	 */
	public EdgeSource(List<int[]> edges) {
		this(i -> edges.get(i)[0], i -> edges.get(i)[1], 0, edges.size());
	}

	/**
	 * Constructs a edge source from the specified array of edges
	 *
	 * @param edges an array of edges in which each edge is defined as a array
	 *              of indices to 2 vertices
	 */
	public EdgeSource(int[][] edges) {
		this(i -> edges[i][0], i -> edges[i][1], 0, edges.length);
	}

	/**
	 * Constructs a edge source from a range of indices and a function returning
	 * a edge according to an index. The collection of edges supplied by this
	 * source will be the result of the function applied on all the indices of
	 * the range
	 *
	 * @param edges               a function returning a edge according to an
	 *                            index. The edge should be an array of indices
	 *                            to 2 vertices
	 * @param lowerInclusiveIndex the lower inclusive bound of the range of
	 *                            indices
	 * @param upperExclusiveIndex the upper exclusive bound of the range of
	 *                            indices
	 */
	public EdgeSource(IntFunction<int[]> edges, int lowerInclusiveIndex,
			int upperExclusiveIndex) {

		this(i -> edges.apply(i)[0], i -> edges.apply(i)[1],
				lowerInclusiveIndex, upperExclusiveIndex);
	}

	/**
	 * Constructs a edge source from 2 arrays of vertex indices. The first
	 * specified array should contain the indices of the first vertices of the
	 * edges. The second specified array should contain the indices of the
	 * second vertices of the edges
	 *
	 * @param edgeFirstIndices  an array containing the indices of the first
	 *                          vertices of the edges
	 * @param edgeSecondIndices an array containing the indices of the second
	 *                          vertices of the edges
	 */
	public EdgeSource(int[] edgeFirstIndices, int[] edgeSecondIndices) {
		this(i -> edgeFirstIndices[i], i -> edgeSecondIndices[i], 0,
				edgeFirstIndices.length);
	}

	/**
	 * Constructs a edge source from a range of indices and 2 functions
	 * returning respectively the first and second vertex indices of an edge
	 * according to a index. The collection of edges supplied by this source
	 * will be the result of the 2 functions applied on all the indices of the
	 * range
	 *
	 * @param edgeFirstIndices    a function returning the first vertex index of
	 *                            an edge according to an index
	 * @param edgeSecondIndices   a function returning the second vertex index
	 *                            of an edge according to an index
	 * @param lowerInclusiveIndex the lower inclusive bound of the range of
	 *                            indices
	 * @param upperExclusiveIndex the upper exclusive bound of the range of
	 *                            indices
	 */
	public EdgeSource(IntUnaryOperator edgeFirstIndices,
			IntUnaryOperator edgeSecondIndices, int lowerInclusiveIndex,
			int upperExclusiveIndex) {

		this(edgeFirstIndices, edgeSecondIndices, () -> IntStream
				.range(lowerInclusiveIndex, upperExclusiveIndex));
	}

	/**
	 * Constructs a edge source from a index stream supplier and 2 functions
	 * returning respectively the first and second vertex indices of an edge
	 * according to a index. The collection of edges supplied by this source
	 * will be the result of the 2 functions applied on all indices of a stream
	 * generated by the stream supplier
	 *
	 * @param edgeV1Indices     a function returning the first vertex index of
	 *                          an edge according to an index
	 * @param edgeV2Indices     a function returning the second vertex index of
	 *                          an edge according to an index
	 * @param edgeIndexSupplier the index stream supplier
	 */
	public EdgeSource(IntUnaryOperator edgeV1Indices,
			IntUnaryOperator edgeV2Indices,
			Supplier<IntStream> edgeIndexSupplier) {

		this.v1Indices = Objects.requireNonNull(edgeV1Indices);
		this.v2Indices = Objects.requireNonNull(edgeV2Indices);
		this.indexSupplier = Objects.requireNonNull(edgeIndexSupplier);
	}

	/**
	 * Builds a half-edge data structure based on this edge source. Uses the
	 * specified winding provider to connect the edges between them
	 *
	 * @param windingProvider the winding provider used to to connect edges
	 *                        during the building of the {@link HMesh half-edge
	 *                        data structure}
	 * @return an optional on a {@link HMesh} representing the half-edge data
	 *         structure; {@link Optional#empty()} if the building failed
	 */
	public Optional<HMesh> toHMesh(PolygonWindingProvider windingProvider) {
		return new ToHMeshConverter().convert(this, windingProvider)
				.map(HConversion::mesh);
	}

	/**
	 * Builds a 2D half-edge data structure based on this edge source and on the
	 * specified {@link Coord2DSource 2D coords source}
	 *
	 * @param coordsSource a 2D coords supplier for the coords of the vertices
	 *                     of the 2D half-edge data structure
	 * @return an optional on a {@link HMesh2D} representing the 2D half-edge
	 *         data structure; {@link Optional#empty()} if the building failed
	 */
	public Optional<HMesh2D> toHMesh(Coord2DSource coordsSource) {
		return new ToHMeshConverter().convert(this, coordsSource)
				.map(HConversion::mesh);
	}

	/**
	 * Builds a 3D half-edge data structure based on this edge source and on the
	 * specified {@link Coord2DSource 3D coords source}
	 *
	 * @param coordsSource a 3D coords source for the coords of the vertices of
	 *                     the 3D half-edge data structure
	 * @return an optional on a {@link HMesh3D} representing the 3D half-edge
	 *         data structure; {@link Optional#empty()} if the building failed
	 */
	public Optional<HMesh3D> toHMesh(Coord3DSource coordsSource) {
		return new ToHMeshConverter().convert(this, coordsSource)
				.map(HConversion::mesh);
	}

	/**
	 * Returns the firtst vertex index of the edge with the specified index in
	 * this face source
	 *
	 * @param edgeIndex index of the edge whose first vertex index is seeked
	 * @return
	 */
	public int edgeV1Index(int edgeIndex) {
		return v1Indices.applyAsInt(edgeIndex);
	}

	/**
	 * Returns the second vertex index of the edge with the specified index in
	 * this face source
	 *
	 * @param edgeIndex index of the edge whose second vertex index is seeked
	 * @return
	 */
	public int edgeV2Index(int edgeIndex) {
		return v2Indices.applyAsInt(edgeIndex);
	}

	/**
	 * @return a stream on this source edge indices
	 */
	public IntStream edgeIndices() {
		return indexSupplier.get();
	}
}
