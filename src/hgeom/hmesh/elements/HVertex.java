package hgeom.hmesh.elements;

import hgeom.hmesh.sequence.Sequence;

/**
 * A vertex within a {@link HMesh}
 *
 * @author Pierre B.
 */
public interface HVertex extends HElement {

	/**
	 * @return one of the edges pointing at this vertex
	 * @throws IllegalStateException if this vertex is discarded
	 */
	HEdge edge();

	/**
	 * Returns the degree of this vertex. Correspond to the number of half-edges
	 * pointing at this vertex.
	 *
	 * @return the degree of this vertex. Always >= 2
	 * @throws IllegalStateException if this vertex is discarded
	 */
	int degree();

	/**
	 * Indicates if this vertex is the neighbor of another vertex. Two vertices
	 * are neighbors if there exists a pair of twin half-edges connecting them
	 *
	 * @param other
	 * @return {@code true} if yhis vertex and the specified one are neighbors
	 * @throws IllegalStateException if this half-edge or the specified one is
	 *                               discarded
	 * @throws NullPointerException  if argument is {@code null}
	 * @see HVertex#neighbors()
	 */
	boolean isNeighborOf(HVertex other);

	/**
	 * Returns a sequence over the neighbors of this vertex. A neighbor is a
	 * vertex connected to this vertex by a pair of twin half-edges
	 *
	 * @return
	 * @throws IllegalStateException if this vertex is discarded
	 * @see HVertex#isNeighborOf(HVertex)
	 */
	Sequence<HVertex> neighbors();

	/**
	 * @return an ordered sequence over the half-edges pointing at this vertex
	 * @throws IllegalStateException if this vertex is discarded
	 */
	Sequence<HEdge> incomingEdges();

	/**
	 * @return an ordered sequence over the half-edges whose tail is this vertex
	 * @throws IllegalStateException if this vertex is discarded
	 */
	Sequence<HEdge> outgoingEdges();
}
