package hgeom.hmesh.core;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HFace;
import hgeom.hmesh.elements.HVertex;
import hgeom.hmesh.sequence.Sequence;
import hgeom.hmesh.util.Loops;

/**
 *
 * @author Pierre B.
 */
class HEdgeImpl extends HElementImpl implements HEdge {

	/**
	 *
	 */
	private static final class HEdgeLinkedToPreviousImpl extends HEdgeImpl {

		/**
		 *
		 */
		private HEdge previous;

		/**
		 * @param id
		 * @param head
		 */
		public HEdgeLinkedToPreviousImpl(int id, HVertex head) {
			super(id, head);
		}

		/**
		 * @param previous
		 */
		public void setPrevious(HEdge previous) {
			this.previous = requireValid(previous);
		}

		@Override
		public HEdge previous() {
			requireNotDiscarded();
			return previous;
		}
	}

	/**
	 *
	 */
	private HFace face;

	/**
	 *
	 */
	private HVertex head;

	/**
	 *
	 */
	private HEdge next;

	/**
	 *
	 */
	private HEdge opposite;

	/**
	 * @param id
	 * @param vertex
	 */
	public HEdgeImpl(int id, HVertex vertex) {
		super(id);
		this.head = HVertexImpl.requireValid(vertex);
	}

	/**
	 * @param id
	 * @param head
	 * @param linkedToPrevious
	 * @return
	 */
	public static HEdge create(int id, HVertex head, boolean linkedToPrevious) {
		return linkedToPrevious ? new HEdgeLinkedToPreviousImpl(id, head)
				: new HEdgeImpl(id, head);
	}

	/**
	 * @param edge
	 * @return
	 */
	public static HEdgeImpl requireValid(HEdge edge) {
		return requireValid(edge, HEdgeImpl.class);
	}

	/**
	 * @param edge
	 * @param vertex
	 */
	public static void setVertex(HEdge edge, HVertex vertex) {
		requireValid(edge).head = HVertexImpl.requireValid(vertex);
	}

	/**
	 * @param edge
	 * @param next
	 */
	public static void link(HEdge edge, HEdge next) {

		// Exception si le lien provoque la presence d'un aller-retour
		if (edge.opposite() == next) {
			throw new IllegalStateException("edge: " + edge);
		}

		requireValid(edge).next = requireValid(next);

		if (next instanceof HEdgeLinkedToPreviousImpl) {
			((HEdgeLinkedToPreviousImpl) next).setPrevious(edge);
		}
	}

	/**
	 * @param hEdge1
	 * @param hEdge2
	 */
	public static void linkAsOpposites(HEdge hEdge1, HEdge hEdge2) {
		HEdgeImpl hEdge1Impl = requireValid(hEdge1);
		HEdgeImpl hEdge2Impl = requireValid(hEdge2);
		hEdge1Impl.opposite = hEdge2Impl;
		hEdge2Impl.opposite = hEdge1Impl;
	}

	/**
	 * @param edge
	 * @param face
	 */
	public static void setFace(HEdge edge, HFace face) {
		requireValid(edge).face = HFaceImpl.requireValid(face);
	}

	@Override
	public final HFace face() {
		requireNotDiscarded();
		return face;
	}

	@Override
	public final HVertex head() {
		requireNotDiscarded();
		return head;
	}

	@Override
	public final HEdge opposite() {
		requireNotDiscarded();
		return opposite;
	}

	@Override
	public final HEdge next() {
		requireNotDiscarded();
		return next;
	}

	@Override
	public final Optional<HEdge> next(Predicate<? super HEdge> predicate) {
		requireNotDiscarded();
		return Loops.findFirst(this, HEdge::next, predicate);
	}

	@Override
	public HEdge previous() {
		requireNotDiscarded();

		return Loops
				.findFirst(tail().edge(), HVertexImpl.NEXT_INCOMING_EDGE,
						e -> e.next() == this)
				.orElseThrow(() -> new IllegalStateException(
						"Unknown previous for: " + this));
	}

	@Override
	public final Sequence<HEdge> cycle() {
		requireNotDiscarded();
		return Loop.createLoop(this, UnaryOperator.identity(), HEdge::next);
	}

	@Override
	public final String toString() {
		return ToStringUtils.toString(this);
	}
}
