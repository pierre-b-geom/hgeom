package hgeom.hmesh.elements;

import java.util.Optional;
import java.util.stream.Stream;

import hgeom.hmesh.data.HBData;
import hgeom.hmesh.data.HDData;
import hgeom.hmesh.data.HData;
import hgeom.hmesh.data.HIData;

/**
 * A half-edge data structure
 *
 * @author Pierre B.
 */
public interface HMesh {

	/**
	 * @return a sequential stream of this mesh's faces
	 */
	Stream<HFace> faces();

	/**
	 * @return a sequential stream of this mesh's edges
	 */
	Stream<HEdge> edges();

	/**
	 * @return a sequential stream of this mesh's vertices
	 */
	Stream<HVertex> vertices();

	/**
	 * Returns the half-edge connecting 2 vertices if it exists
	 *
	 * @param tail tail vertex of the requested half-edge
	 * @param head head vertex of the requested half-edge
	 * @return the half-edge or {@link Optional#empty()} if not found
	 */
	static Optional<HEdge> edge(HVertex tail, HVertex head) {
		return tail.outgoingEdges().filter(e -> e.head() == head).findFirst();
	}

	/**
	 * Splits a face of this mesh into 2 parts.
	 * <p>
	 * The split:
	 * <p>
	 * 1/ creates a pair of twin half-edges joining 2 vertices located o the
	 * border of the face and dividing the specified face into 2 zones
	 * <p>
	 * 2/ makes the face become one of the 2 zones
	 * <p>
	 * 3/ creates a new face covering the other zone
	 * <p>
	 * The split will fail if the 2 specified vertices are identical or
	 * neighbors
	 * <p>
	 *
	 * @param face    the face to split
	 * @param vertex1 one of the face's vertex where the split should start
	 * @param vertex2 one of the face's vertex where the split should end
	 * @return an {@link Optional} on the newly created face;
	 *         {@link Optional#empty()} if the split failed
	 * @throws IllegalArgumentException if one of the specified vertex is not
	 *                                  located on the face
	 * @throws IllegalStateException    if one of the argument is discarded
	 * @throws NullPointerException     if one argument is {@code null}
	 */
	Optional<HFace> splitFace(HFace face, HVertex vertex1, HVertex vertex2);

	/**
	 * Merges a face into another one. The 2 faces must be neighbors for the
	 * merge to succeed
	 *
	 * @param face1 the face that will absorb the other face
	 * @param face2 the face that will be absorbed by the other face. If the
	 *              merge succeeds, this face will be removed from this mesh and
	 *              discarded
	 * @return {@code true} if successful merge ; else {@code false}
	 * @throws IllegalStateException if one of the given faces is discarded
	 * @throws NullPointerException  if one of the given faces is {@code null}
	 */
	boolean mergeFaces(HFace face1, HFace face2);

	/**
	 * Splits a half-edge and its {@link HEdge#opposite() opposite} twin into 2
	 * pairs of twin half-edges and adds a new vertex in this mesh between the 2
	 * pairs.
	 * <p>
	 * <code>
	 * Before:<p>
	 * 2 half-edges: he[v1 -> v2], heopp[v2 -> v1]<p>
	 * <p>
	 *    v1 <-------------------------------------> v2<p>
	 * <p>
	 * After:<p>
	 * 1 new vertex v, 4 half-edges: he[v1 -> v], heopp[v -> v1]<p>
	 * he2[v2 -> v], h2opp[v -> v2]<p>
	 * <p>
	 *    v1 <----------------> v <----------------> v2<p>
	 * <p>
	 * </code>
	 * <p>
	 *
	 * @param edge the half-edge to split
	 * @return the new vertex created by the split. Never {@code null}.
	 * @throws IllegalStateException if the given half-edge is discarded
	 * @throws NullPointerException  if the given half-edge is {@code null}
	 */
	HVertex splitEdge(HEdge edge);

	/**
	 * Collapse a half-edge (and its opposite). Equivalent to "merge" the head
	 * and the tail of the half-edge. After the collapse, the edge and its
	 * opposite will be removed from this mesh. The head will be removed as well
	 * and its incoming and outgoing half-edges will be reconnected to the tail
	 *
	 * @param edge the edge to collapse
	 * @return {@code true} if successfull collapse ; {@code false} otherwise.
	 *         The collapse will not take place if the specified edge or its
	 *         opposite is part of a cycle / face containing only 3 elements.
	 *         Collapse would otherwise result in degenerate faces containing
	 *         only 2 half-edges
	 * @throws IllegalStateException if the given half-edge is discarded
	 * @throws NullPointerException  if the given half-edge is {@code null}
	 */
	boolean collapseEdge(HEdge edge);

	/**
	 * Removes a vertex from the mesh.
	 * <p>
	 * The vertex will be removed only if a/ its degree is 2 and b/ it does not
	 * belong to a triangle
	 * <p>
	 * After the vertex is removed, this mesh will contain 2 less half-edges and
	 * 2 other half-edges will have been modified
	 * <p>
	 * <code>
	 * Before:<p>
	 * 3 vertices, 4 half-edges: h1[v1 -> v], h1opp[v -> v1], h2[v2 -> v], h2opp[v -> v1]<p>
	 * <p>
	 *    v1 <----------------> v <----------------> v2<p>
	 * <p>
	 * After:<p>
	 * 2 vertices, 2 half-edges with theirs heads changed: h1[v1 -> v2], h2[v2 -> v1]<p>
	 * <p>
	 *    v1 <-------------------------------------> v2<p>
	 * <p>
	 * </code>
	 *
	 * @param vertex the vertex to be removed
	 * @return {@code true} if successfull removal ; {@code false} otherwise
	 * @throws IllegalStateException if the given vertex is discarded
	 * @throws NullPointerException  if the given vertex is {@code null}
	 * @see HVertex#degree()
	 */
	boolean removeVertex(HVertex vertex);

	/**
	 * Creates and returns a {@link HDData} for associating data to this mesh's
	 * vertices
	 *
	 * @return
	 */
	<D> HData<HVertex, D> createVertexData();

	/**
	 * Creates and returns a {@link HBData} for associating boolean data to this
	 * mesh's vertices
	 *
	 * @return
	 */
	HBData<HVertex> createVertexBooleanData();

	/**
	 * Creates and returns a {@link HIData} for associating integer data to this
	 * mesh's vertices
	 *
	 * @return
	 */
	HIData<HVertex> createVertexIntData();

	/**
	 * Creates and returns a {@link HDData} for associating double data to this
	 * mesh's vertices
	 *
	 * @return
	 */
	HDData<HVertex> createVertexDoubleData();

	/**
	 * Creates and returns a {@link HDData} for associating data to this mesh's
	 * edges.
	 *
	 * @return
	 */
	<D> HData<HEdge, D> createEdgeData();

	/**
	 * Creates and returns a {@link HBData} for associating boolean data to this
	 * mesh's edges
	 *
	 * @return
	 */
	HBData<HEdge> createEdgeBooleanData();

	/**
	 * Creates and returns a {@link HIData} for associating integer data to this
	 * mesh's edges
	 *
	 * @return
	 */
	HIData<HEdge> createEdgeIntData();

	/**
	 * Creates and returns a {@link HDData} for associating double data to this
	 * mesh's edges
	 *
	 * @return
	 */
	HDData<HEdge> createEdgeDoubleData();

	/**
	 * Creates and returns a {@link HDData} for associating data to this mesh's
	 * faces
	 *
	 * @return
	 */
	<D> HData<HFace, D> createFaceData();

	/**
	 * Creates and returns a {@link HBData} for associating boolean data to this
	 * mesh's faces
	 *
	 * @return
	 */
	HBData<HFace> createFaceBooleanData();

	/**
	 * Creates and returns a {@link HIData} for associating integer data to this
	 * mesh's faces
	 *
	 * @return
	 */
	HIData<HFace> createFaceIntData();

	/**
	 * Creates and returns a {@link HDData} for associating double data to this
	 * mesh's faces
	 *
	 * @return
	 */
	HDData<HFace> createFaceDoubleData();

	/**
	 * Minimize the memory usage of this mesh to its smallest size by removing
	 * garbage data that have been internally accumulated. Garbage data are
	 * produced during topological operations
	 */
	void trim();
}
