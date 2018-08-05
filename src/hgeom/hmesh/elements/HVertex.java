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
	 */
	HEdge edge();

	/**
	 * Returns the degree of this vertex. Correspond to the number of half-edges
	 * pointing at this vertex.
	 *
	 * @return the degree of this vertex. Always >= 2
	 */
	int degree();

	/**
	 * Indicates if this vertex is the neighbor of another vertex. Two vertices
	 * are neighbors if there exists a pair of twin half-edges connecting them
	 *
	 * @param other
	 * @return {@code true} if yhis vertex and the specified one are neighbors
	 * @throws NullPointerException if argument is {@code null}
	 * @see HVertex#neighbors()
	 */
	boolean isNeighborOf(HVertex other);

	/**
	 * Returns a sequence over the neighbors of this vertex. A neighbor is a
	 * vertex connected to this vertex by a pair of twin half-edges
	 *
	 * @return
	 * @see HVertex#isNeighborOf(HVertex)
	 */
	Sequence<HVertex> neighbors();

	/**
	 * Returns a sequence over the half-edges pointing at this vertex
	 *
	 * @return
	 */
	Sequence<HEdge> incomingEdges();

	/**
	 * Returns a sequence over the half-edges whose tail is this vertex
	 *
	 * @return
	 */
	Sequence<HEdge> outgoingEdges();
}
