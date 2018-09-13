package hgeom.hmesh.core;

import java.util.function.Function;
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
class HFaceImpl extends HElementImpl implements HFace {

	/**
	 *
	 */
	private static final class InteriorHFaceImpl extends HFaceImpl {

		/**
		 * @param id
		 * @param edge
		 */
		public InteriorHFaceImpl(int id, HEdge edge) {
			super(id, edge);
		}

		@Override
		public Status status() {
			requireNotDiscarded();
			return Status.INTERIOR;
		}
	}

	/**
	 *
	 */
	private static final class ExteriorHFaceImpl extends HFaceImpl {

		/**
		 * @param id
		 * @param edge
		 */
		public ExteriorHFaceImpl(int id, HEdge edge) {
			super(id, edge);
		}

		@Override
		public Status status() {
			requireNotDiscarded();
			return Status.BOUNDARY;
		}
	}

	/**
	 *
	 */
	private HEdge edge;

	/**
	 * @param id
	 * @param edge l'une des aretes de la face
	 */
	public HFaceImpl(int id, HEdge edge) {
		super(id);
		this.edge = HEdgeImpl.requireValid(edge);
		Loops.forEach(edge, HEdge::next, e -> HEdgeImpl.setFace(e, this));
	}

	/**
	 * @param id
	 * @param edge
	 * @param status
	 * @param checkValidity
	 * @return la face ou null si pas valide
	 */
	public static HFace create(int id, HEdge edge, Status status,
			boolean checkValidity) {

		if (checkValidity && !validateCycle(edge)) {
			return null;
		}

		if (status == Status.INTERIOR) {
			return new InteriorHFaceImpl(id, edge);
		}

		if (status == Status.BOUNDARY) {
			return new ExteriorHFaceImpl(id, edge);
		}

		return new HFaceImpl(id, edge);
	}

	/**
	 * @param face
	 * @return
	 */
	private static boolean validateCycle(HEdge edge) {
		Sequence<HEdge> edges = edge.cycle();
		Sequence<HEdge> oppositeEdges = edges.map(HEdge::opposite);
		return edges.allMatch(e -> oppositeEdges.allMatch(oe -> oe != e));
	}

	/**
	 * @param face
	 * @return
	 */
	public static HFaceImpl requireValid(HFace face) {
		return requireValid(face, HFaceImpl.class);
	}

	/**
	 * @param face
	 * @param edge
	 */
	public static void setEdge(HFace face, HEdge edge) {
		requireValid(face).edge = HEdgeImpl.requireValid(edge);
	}

	@Override
	public Status status() {
		requireNotDiscarded();
		return Status.UNKNOWN;
	}

	@Override
	public final HEdge edge() {
		requireNotDiscarded();
		return edge;
	}

	@Override
	public final Sequence<HEdge> edges() {
		requireNotDiscarded();
		return Loop.createLoop(this, HFace::edge, HEdge::next);
	}

	@Override
	public final Sequence<HVertex> vertices() {
		return edges().map(HEdge::head);
	}

	@Override
	public final boolean isNeighborOf(HFace other) {
		return Loops.anyMatch(edge(), HEdge::next,
				e -> e.opposite().face() == other);
	}

	@Override
	public final Sequence<HFace> neighbors() {
		requireNotDiscarded();

		// En sortie, la premiere arete situee sur une face qui n'est pas la
		// face de l'arete passee en entree. Si pas trouve, retourne l'arete
		// passee en entree
		UnaryOperator<HEdge> firstInNextFace = e -> {
			Predicate<HEdge> onNextFace = nextE -> nextE.opposite().face() != e
					.opposite().face();

			return Loops.findFirst(e, HEdge::next, onNextFace).orElse(e);
		};

		// A partir de l'arete initiale, obtention d'une premiere arete situee
		// au debut d'une face lors du parcours sur le bord
		Function<HFace, HEdge> originToFirst = f -> firstInNextFace
				.apply(f.edge());

		return Loop.createLoop(this, originToFirst, firstInNextFace)
				.map(e -> e.opposite().face());
	}

	@Override
	public final String toString() {
		return ToStringUtils.toString(this);
	}
}
