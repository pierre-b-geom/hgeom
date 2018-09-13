package hgeom.hmesh.elements;

import hgeom.hmesh.sequence.Sequence;

/**
 * A face within a {@link HMesh}
 *
 * @author Pierre B.
 */
public interface HFace extends HElement {

	/**
	 * Status of the face in relation to its {@link HMesh}
	 */
	public enum Status {

	/**
	 * The face is located inside the {@link HMesh mesh} it is part of
	 */
	INTERIOR,

	/**
	 * The face is a boundary of the {@link HMesh mesh} it is part of. A
	 * boundary is a face that surrounds the {@link HMesh mesh} (an outer
	 * border) or a face that surrounds a hole within the {@link HMesh mesh} (an
	 * inner border)
	 */
	BOUNDARY,

	/**
	 * Unknown status. The face could be either inside or on the border of the
	 * {@link HMesh mesh} it is part of. When a {@link HMesh mesh} is built from
	 * a set of edges, all its faces have an unknown status
	 */
	UNKNOWN
	}

	/**
	 * @return the {@link Status status} of this face
	 * @throws IllegalStateException if this face is discarded
	 */
	Status status();

	/**
	 * @return one of the half-edges located on the border of this face
	 * @throws IllegalStateException if this face is discarded
	 */
	HEdge edge();

	/**
	 * @return a ordered circular sequence over the half-edges located along the
	 *         border of this face. Half-edges are ordered in the sequence so
	 *         that a half-edge is the {@link HEdge#next} of its preceeding
	 *         half-edge
	 * @throws IllegalStateException if this face is discarded
	 */
	Sequence<HEdge> edges();

	/**
	 * @return a ordered circular sequence over the vertices located along the
	 *         border of this face. Vertices are ordered so that 2 consecutive
	 *         vertices in the sequence are neighbors
	 * @throws IllegalStateException if this face is discarded
	 */
	Sequence<HVertex> vertices();

	/**
	 * Indicates if this face is adjacent to the specified face. Two faces are
	 * adjacent if they are in contact via at least one pair of twin half-edges
	 *
	 * @param other the other face
	 * @return {@code true} if there is neighborhood
	 * @throws IllegalStateException if this face is discarded
	 * @see HFace#neighbors()
	 */
	boolean isNeighborOf(HFace other);

	/**
	 * Returns a orderd circular sequence over the neighbors of this face. The
	 * neighbors are the faces in contact with this face via a shared border
	 * consisting of at least one pair of twin half-edges
	 * <p>
	 * In the particular case of a face being in contact with this face via two
	 * (or more) distinct borders, the neighbor face will appear twice (or more)
	 * in the returned sequence
	 *
	 * @return
	 * @throws IllegalStateException if this face is discarded
	 * @see HVertex#neighbors()
	 */
	Sequence<HFace> neighbors();
}
