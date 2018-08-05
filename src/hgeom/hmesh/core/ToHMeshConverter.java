package hgeom.hmesh.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import hgeom.hmesh.data.HIData;
import hgeom.hmesh.elements.Coord2DSource;
import hgeom.hmesh.elements.Coord3DSource;
import hgeom.hmesh.elements.EdgeSource;
import hgeom.hmesh.elements.FaceSource;
import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HFace;
import hgeom.hmesh.elements.HMesh;
import hgeom.hmesh.elements.HMesh2D;
import hgeom.hmesh.elements.HMesh3D;
import hgeom.hmesh.elements.HVertex;
import hgeom.hmesh.winding.PolygonWindingProvider;

/**
 * Converter of face collection and edge collection into {@link HMesh half-edge
 * data structure}
 *
 * @author Pierre B.
 * @see HMesh
 * @see HMesh2D
 * @see HMesh3D
 */
public final class ToHMeshConverter {

	/**
	 *
	 */
	private static final Logger LOGGER = Logger
			.getLogger(ToHMeshConverter.class.getName());

	/**
	 *
	 */
	private final boolean linkEdgesToPrevious;

	/**
	 *
	 */
	private final boolean debugInfo;

	/**
	 *
	 */
	private final List<HVertex> vertices = new ArrayList<>();

	/**
	 *
	 */
	private HElementFactory elementFactory;

	/**
	 *
	 */
	private HIData<HVertex> vertexIndices;

	/**
	 *
	 */
	private HIData<HEdge> edgeIndices;

	/**
	 *
	 */
	private HIData<HFace> faceIndices;

	/**
	 * Constructs a converter with default parameters
	 */
	public ToHMeshConverter() {
		this(false, false);
	}

	/**
	 * Constructs a converter with the specified option
	 *
	 * @param linkEdgesToPrevious if true, the generated half-edge data
	 *                            structures will contain half-edges implemented
	 *                            with links to their previous and next; if
	 *                            false, the half-edges implementations will
	 *                            only contain a link to their nexts
	 *                            <p>
	 *                            In both cases, calls to HEdge.previous() are
	 *                            valid but they are slower when a half-edge's
	 *                            implementation does not have a link to its
	 *                            previous because the link must then be
	 *                            computed
	 * @see HVertex
	 * @see HEdge
	 * @see HFace
	 */
	public ToHMeshConverter(boolean linkEdgesToPrevious) {
		this(linkEdgesToPrevious, false);
	}

	/**
	 * Constructs a converter with the specified options
	 *
	 * @param linkEdgesToPrevious if true, the generated half-edge data
	 *                            structures will contain half-edges implemented
	 *                            with links to their previous and next; if
	 *                            false, the half-edges implementations will
	 *                            only contain a link to their nexts
	 *                            <p>
	 *                            In both cases, calls to HEdge.previous() are
	 *                            valid but they are slower when a half-edge's
	 *                            implementation does not have a link to its
	 *                            previous because the link must then be
	 *                            computed
	 * @param debugInfo           if {@code true}, the generated {@link HMesh
	 *                            half-edge data structures} will contain
	 *                            debugging info accessible through
	 *                            {@link HVertex#toString()} ,
	 *                            {@link HEdge#toString()} and
	 *                            {@link HFace#toString()}
	 * @see HVertex
	 * @see HEdge
	 * @see HFace
	 */
	public ToHMeshConverter(boolean linkEdgesToPrevious, boolean debugInfo) {
		this.linkEdgesToPrevious = linkEdgesToPrevious;
		this.debugInfo = debugInfo;
	}

	/**
	 * Generates a {@link HMesh half-edge data structure} from a collection of
	 * faces
	 *
	 * @param faceSource the collection of faces
	 * @return an {@link Optional} on a {@link HConversion} containing the
	 *         generated {@link HMesh half-edge data structure} or
	 *         {@link Optional#empty()} in case the generation failed
	 * @see HMesh
	 */
	public Optional<HConversion<HMesh>> convert(FaceSource faceSource) {
		return convert(faceSource, (PolygonWindingProvider) null);
	}

	/**
	 * Generates a {@link HMesh half-edge data structure} from a collection of
	 * faces. Uses the specified winding provider to provide the winding of the
	 * faces of the generated {@link HMesh half-edge data structure}
	 *
	 * @param faceSource      the collection of faces
	 * @param windingProvider the winding provider used to provide the winding
	 *                        of the faces of the generated {@link HMesh
	 *                        half-edge data structure} or {@code null} if no
	 *                        winding provider is needed
	 * @return an {@link Optional} on a {@link HConversion} containing the
	 *         generated {@link HMesh half-edge data structure} or
	 *         {@link Optional#empty()} in case the generation failed
	 * @see HConversion
	 * @see HMesh
	 */
	public Optional<HConversion<HMesh>> convert(FaceSource faceSource,
			PolygonWindingProvider windingProvider) {

		init(true);

		return createFaces(faceSource, windingProvider).map(this::createMesh)
				.map(this::finishConversion);
	}

	/**
	 * Generates a {@link HMesh2D 2D half-edge data structure} from a collection
	 * of faces and a collection of 2D coordinates
	 *
	 * @param faceSource   the collection of faces
	 * @param vertexCoords the collection of 2D coordinates
	 * @return an {@link Optional} on a {@link HConversion} containing the
	 *         generated {@link HMesh half-edge data structure} or
	 *         {@link Optional#empty()} in case the generation failed
	 * @see HConversion
	 * @see HMesh
	 */
	public Optional<HConversion<HMesh2D>> convert(FaceSource faceSource,
			Coord2DSource vertexCoords) {

		return convert(faceSource, vertexCoords,
				vertexCoords.windingProvider());
	}

	/**
	 * Generates a {@link HMesh2D 2D half-edge data structure} from a collection
	 * of faces and a collection of 2D coordinates. Uses the specified winding
	 * provider to provide the winding of the faces of the generated
	 * {@link HMesh half-edge data structure}
	 *
	 * @param faceSource      the collection of faces
	 * @param vertexCoords    the collection of 2D coordinates
	 * @param windingProvider the winding provider used to provide the winding
	 *                        of the faces of the generated {@link HMesh
	 *                        half-edge data structure} or {@code null} if no
	 *                        winding provider is needed
	 * @return an {@link Optional} on a {@link HConversion} containing the
	 *         generated {@link HMesh half-edge data structure} or
	 *         {@link Optional#empty()} in case the generation failed
	 * @see HConversion
	 * @see HMesh
	 */
	public Optional<HConversion<HMesh2D>> convert(FaceSource faceSource,
			Coord2DSource vertexCoords,
			PolygonWindingProvider windingProvider) {

		init(true);

		return createFaces(faceSource, windingProvider)
				.map(faces -> createMesh(faces, vertexCoords))
				.map(this::finishConversion);
	}

	/**
	 * Generates a {@link HMesh3D 3D half-edge data structure} from a collection
	 * of faces and a collection of 3D coordinates
	 *
	 * @param faceSource   the collection of faces
	 * @param vertexCoords the collection of 3D coordinates
	 * @return an {@link Optional} on a {@link HConversion} containing the
	 *         generated {@link HMesh half-edge data structure} or
	 *         {@link Optional#empty()} in case the generation failed
	 * @see HConversion
	 * @see HMesh
	 */
	public Optional<HConversion<HMesh3D>> convert(FaceSource faceSource,
			Coord3DSource vertexCoords) {

		return convert(faceSource, vertexCoords,
				vertexCoords.xyWindingProvider());
	}

	/**
	 * Generates a {@link HMesh3D 3D half-edge data structure} from a collection
	 * of faces and a collection of 3D coordinates. Uses the specified winding
	 * provider to provide the winding of the faces of the generated
	 * {@link HMesh half-edge data structure}
	 *
	 * @param faceSource      the collection of faces
	 * @param vertexCoords    the collection of 3D coordinates
	 * @param windingProvider the winding provider used to provide the winding
	 *                        of the faces of the generated {@link HMesh
	 *                        half-edge data structure} or {@code null} if no
	 *                        winding provider is needed
	 * @return an {@link Optional} on a {@link HConversion} containing the
	 *         generated {@link HMesh half-edge data structure} or
	 *         {@link Optional#empty()} in case the generation failed
	 * @see HConversion
	 * @see HMesh
	 */
	public Optional<HConversion<HMesh3D>> convert(FaceSource faceSource,
			Coord3DSource vertexCoords,
			PolygonWindingProvider windingProvider) {

		init(true);

		return createFaces(faceSource, windingProvider)
				.map(faces -> createMesh(faces, vertexCoords))
				.map(this::finishConversion);
	}

	/**
	 * Generates a {@link HMesh half-edge data structure} from the a collection
	 * of edges. Uses the specified winding provider to make connection between
	 * the edges
	 *
	 * @param edgeSource      the collection of edges
	 * @param windingProvider the winding provider used to connect edges during
	 *                        the building of the {@link HMesh half-edge data
	 *                        structure}
	 * @return
	 */
	public Optional<HConversion<HMesh>> convert(EdgeSource edgeSource,
			PolygonWindingProvider windingProvider) {

		init(false);

		return createFaces(edgeSource, windingProvider).map(this::createMesh)
				.map(this::finishConversion);
	}

	/**
	 * Generates a {@link HMesh2D 2D half-edge data structure} from the a
	 * collection of edges
	 *
	 * @param edgeSource   the collection of edges
	 * @param vertexCoords the collection of coordinates
	 * @return
	 */
	public Optional<HConversion<HMesh2D>> convert(EdgeSource edgeSource,
			Coord2DSource vertexCoords) {

		return convert(edgeSource, vertexCoords,
				vertexCoords.windingProvider());
	}

	/**
	 * Generates a {@link HMesh2D 3D half-edge data structure} from a collection
	 * of edges and a collection of 2D coordinates. Uses the specified winding
	 * provider to make connection between the edges
	 *
	 * @param edgeSource      the collection of edges
	 * @param vertexCoords    the collection of coordinates
	 * @param windingProvider the winding provider used to connect edges during
	 *                        the building of the {@link HMesh half-edge data
	 *                        structure}
	 * @return
	 */
	public Optional<HConversion<HMesh2D>> convert(EdgeSource edgeSource,
			Coord2DSource vertexCoords,
			PolygonWindingProvider windingProvider) {

		init(false);

		return createFaces(edgeSource, windingProvider)
				.map(faces -> createMesh(faces, vertexCoords))
				.map(this::finishConversion);
	}

	/**
	 * Generates a {@link HMesh3D 3D half-edge data structure} from a collection
	 * of edges and a collection of 3D coordinates
	 *
	 * @param edgeSource   the collection of edges
	 * @param vertexCoords the collection of coordinates
	 * @return
	 */
	public Optional<HConversion<HMesh3D>> convert(EdgeSource edgeSource,
			Coord3DSource vertexCoords) {

		return convert(edgeSource, vertexCoords,
				vertexCoords.xyWindingProvider());
	}

	/**
	 * Generates a {@link HMesh3D 3D half-edge data structure} from a collection
	 * of edges and a collection of 3D coordinates. Uses the specified winding
	 * provider to make connection between the edges
	 *
	 * @param edgeSource      the collection of edges
	 * @param vertexCoords    the collection of coordinates
	 * @param windingProvider the winding provider used to connect edges during
	 *                        the building of the {@link HMesh half-edge data
	 *                        structure}
	 * @return
	 */
	public Optional<HConversion<HMesh3D>> convert(EdgeSource edgeSource,
			Coord3DSource vertexCoords,
			PolygonWindingProvider windingProvider) {

		init(false);

		return createFaces(edgeSource, windingProvider)
				.map(faces -> createMesh(faces, vertexCoords))
				.map(this::finishConversion);
	}

	/**
	 * @param forFaceSource
	 */
	private void init(boolean forFaceSource) {
		vertices.clear();
		elementFactory = new HElementFactory(linkEdgesToPrevious, debugInfo);
		vertexIndices = elementFactory.createVertexIntData(null);

		if (forFaceSource) {
			faceIndices = elementFactory.createFaceIntData(null);
			edgeIndices = null;
		}

		else {
			edgeIndices = elementFactory.createEdgeIntData(null);
			faceIndices = null;
		}
	}

	/**
	 * @param source
	 * @param windingProvider
	 * @return
	 */
	private Optional<List<HFace>> createFaces(FaceSource source,
			PolygonWindingProvider windingProvider) {

		ArrowSorter arrowSorter = windingProvider == null ? null
				: new ArrowSorter(windingProvider, vertexIndices::get);

		try (IntStream indices = source.faceIndices()) {
			Supplier<CycleGraph> supplier = () -> new CycleGraph(elementFactory,
					this::vertex, windingProvider);

			ObjIntConsumer<CycleGraph> accumulator = (cycleGraph,
					faceIndex) -> cycleGraph.addCycle(faceIndex,
							source.face(faceIndex));

			CycleGraph cycleGraph = indices.collect(supplier, accumulator,
					null);

			CycleGraphToHFaces cycleGraphToHFaces = new CycleGraphToHFaces(
					elementFactory, arrowSorter, faceIndices);

			return cycleGraphToHFaces.convert(cycleGraph);
		}
	}

	/**
	 * @param source
	 * @param windingProvider
	 * @return
	 */
	private Optional<List<HFace>> createFaces(EdgeSource source,
			PolygonWindingProvider windingProvider) {

		Objects.requireNonNull(windingProvider);

		ArrowSorter arrowSorter = new ArrowSorter(windingProvider,
				vertexIndices::get);

		try (IntStream indices = source.edgeIndices()) {
			ObjIntConsumer<ArrowGraph> accumulator = (arrowGraph,
					edgeIndex) -> addArrows(source, arrowGraph, edgeIndex);

			ArrowGraph arrowGraph = indices.collect(ArrowGraph::new,
					accumulator, null);

			ArrowGraphToHFaces arrowGraphToHFaces = new ArrowGraphToHFaces(
					elementFactory, arrowSorter);

			return arrowGraphToHFaces.convert(arrowGraph);
		}
	}

	/**
	 * @param edgeSource
	 * @param arrowGraph
	 * @param edgeIndex
	 */
	private void addArrows(EdgeSource edgeSource, ArrowGraph arrowGraph,
			int edgeIndex) {

		int v1Index = edgeSource.edgeV1Index(edgeIndex);
		int v2Index = edgeSource.edgeV2Index(edgeIndex);

		if (v1Index == v2Index) {
			LOGGER.warning(
					"cannot add an edge that connects a vertex to itself");
		}

		else {
			HVertex v1 = vertex(v1Index);
			HVertex v2 = vertex(v2Index);

			if (arrowGraph.arrow(v1, v2) == null) {
				HEdge edge1 = elementFactory.createEdge(v1);
				edgeIndices.set(edge1, edgeIndex);
				HEdge edge2 = elementFactory.createEdge(v2);
				edgeIndices.set(edge2, edgeIndex);

				HEdgeImpl.linkAsOpposites(edge1, edge2);
				HVertexImpl.setEdgeIfAbsent(v1, edge1);
				HVertexImpl.setEdgeIfAbsent(v2, edge2);
				arrowGraph.addArrow(edge1);
				arrowGraph.addArrow(edge2);
			}
		}
	}

	/**
	 * @param index
	 * @return
	 */
	private HVertex vertex(int index) {
		for (int i = vertices.size(); i <= index; i++) {
			vertices.add(null);
		}

		HVertex v = vertices.get(index);

		if (v == null) {
			v = elementFactory.createVertex();
			vertexIndices.set(v, index);
			vertices.set(index, v);
		}

		return v;
	}

	/**
	 * @param faces
	 * @return
	 */
	private HMesh createMesh(List<HFace> faces) {
		return new HMeshImpl(faces, elementFactory);
	}

	/**
	 * @param faces
	 * @param vertexCoords
	 * @return
	 */
	private HMesh2D createMesh(List<HFace> faces, Coord2DSource vertexCoords) {
		HMesh2DImpl mesh = new HMesh2DImpl(faces, elementFactory);
		mesh.vertexXs().setAll(v -> vertexCoords.x(vertexIndices.get(v)));
		mesh.vertexYs().setAll(v -> vertexCoords.y(vertexIndices.get(v)));
		return mesh;
	}

	/**
	 * @param faces
	 * @param vertexCoords
	 * @return
	 */
	private HMesh3D createMesh(List<HFace> faces, Coord3DSource vertexCoords) {
		HMesh3DImpl mesh = new HMesh3DImpl(faces, elementFactory);
		mesh.vertexXs().setAll(v -> vertexCoords.x(vertexIndices.get(v)));
		mesh.vertexYs().setAll(v -> vertexCoords.y(vertexIndices.get(v)));
		mesh.vertexZs().setAll(v -> vertexCoords.z(vertexIndices.get(v)));
		return mesh;
	}

	/**
	 * @param mesh
	 * @return
	 */
	private <M extends HMesh> HConversion<M> finishConversion(M mesh) {
		if (debugInfo) {
			mesh.vertices().forEach(v -> HVertexImpl.setMesh(v, mesh));
		}

		return new HConversion<>(mesh, vertexIndices, edgeIndices, faceIndices);
	}
}
