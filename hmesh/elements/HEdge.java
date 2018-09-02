package hgeom.hmesh.elements;

import java.util.Optional;
import java.util.function.Predicate;

import hgeom.hmesh.sequence.Sequence;

/**
 * A half-edge within a {@link HMesh}
 *
 * @author Pierre B.
 */
public interface HEdge extends HElement {

	/**
	 * @return the face to which this half-edge belongs
	 */
	HFace face();

	/**
	 * @return the vertex at the head of this edge (the vertex to which this
	 *         half-edge points)
	 */
	HVertex head();

	/**
	 * @return the vertex at the tail of this edge
	 */
	default HVertex tail() {
		return opposite().head();
	}

	/**
	 * @return the half-edge following this half-edge in the half-edge cycle
	 *         this half-edge is part of
	 */
	HEdge next();

	/**
	 * Starts from this half-edge and iterate over the cycle this edge belongs
	 * to by successive calls to {@link HEdge#next()} until a half-edge is found
	 * matching the specified predicate. If no half-edge is found, stops when
	 * the iteration leads back to this edge
	 * <p>
	 * Equivalent to, but faster than
	 * <code>this.cycle().filter(predicate).findFirst()</code>
	 *
	 * @param predicate a predicate used to test every encountered half-edge
	 *                  along the cycle
	 * @return an {@link Optional} on a half-edge found matching the predicate
	 *         or {@link Optional#empty()} if the cycle was completly parsed and
	 *         no half-edge was found
	 */
	Optional<HEdge> next(Predicate<? super HEdge> predicate);

	/**
	 * @return the previous half-edge on the cycle this half-edge belongs to
	 */
	HEdge previous();

	/**
	 * @return the opposite (or sibling) half-edge
	 */
	HEdge opposite();

	/**
	 * @return a sequence over all the elements of the cycle this half-edge
	 *         belongs to (along the border of a face). The first element of the
	 *         sequence is this half-Edge, the 2nd element is the
	 *         {@link HEdge#next() next} half-edge and so on
	 */
	Sequence<HEdge> cycle();

	/**
	 * @return a sequence over all the half-edges whose tail are this edge's
	 *         head. The {@link HEdge#opposite() opposite} of this half-edge is
	 *         included in the sequence
	 */
	default Sequence<HEdge> outgoingEdges() {
		return head().outgoingEdges();
	}

	/**
	 * @return a sequence over all the half-edges whose head are this edge's
	 *         tail. The {@link HEdge#opposite() opposite} of this half-edge is
	 *         included in the sequence
	 */
	default Sequence<HEdge> incomingEdges() {
		return tail().incomingEdges();
	}
}
