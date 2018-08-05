package hgeom.hmesh.core;

import java.util.function.UnaryOperator;

import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HMesh;
import hgeom.hmesh.elements.HVertex;
import hgeom.hmesh.sequence.Sequence;
import hgeom.hmesh.util.Loops;

/**
 *
 * @author Pierre B.
 */
class HVertexImpl extends HElementImpl implements HVertex {

	/**
	 *
	 */
	public static final UnaryOperator<HEdge> NEXT_INCOMING_EDGE = e -> e.next()
			.opposite();

	/**
	 *
	 */
	public static final UnaryOperator<HEdge> NEXT_OUTGOING_EDGE = e -> e
			.opposite().next();

	/**
	 *
	 */
	private static final class HVertexWithMeshImpl extends HVertexImpl {

		/**
		 *
		 */
		private HMesh mesh;

		/**
		 * @param id
		 */
		HVertexWithMeshImpl(int id) {
			super(id);
		}

		/**
		 * @param mesh
		 */
		public void setMesh(HMesh mesh) {
			this.mesh = mesh;
		}

		@Override
		public String toString() {
			String s = ToStringUtils.toString(this, mesh);
			return s == null ? super.toString() : s;
		}
	}

	/**
	 * demi-arete pointant vers le sommet. Jamais null sauf lors de la
	 * construction et pour indiquer que le sommet est declassifiee
	 *
	 */
	private HEdge edge;

	/**
	 * @param id
	 */
	protected HVertexImpl(int id) {
		super(id);
	}

	/**
	 * Cree un sommet
	 *
	 * @param id
	 * @param e         arete a associer au sommet ou {@code null}
	 * @param debugInfo
	 * @return le sommet cree
	 * @throws NullPointerException si tableau de coordonnees ou arete
	 *                              {@code null}
	 */
	public static HVertex create(int id, HEdge e, boolean debugInfo) {
		HVertexImpl v = debugInfo ? new HVertexWithMeshImpl(id)
				: new HVertexImpl(id);

		if (e != null) {
			v.edge = HEdgeImpl.requireValid(e);
		}

		return v;
	}

	/**
	 * @param v
	 * @param mesh
	 */
	public static void setMesh(HVertex v, HMesh mesh) {
		HVertexImpl vImpl = requireValid(v);

		if (vImpl instanceof HVertexWithMeshImpl) {
			((HVertexWithMeshImpl) vImpl).setMesh(mesh);
		}
	}

	/**
	 * @param v
	 * @param e
	 */
	public static void setEdge(HVertex v, HEdge e) {
		requireValid(v).edge = HEdgeImpl.requireValid(e);
	}

	/**
	 * @param v
	 * @param e
	 */
	public static void setEdgeIfAbsent(HVertex v, HEdge e) {
		HVertexImpl vImpl = requireValid(v);

		if (vImpl.edge == null) {
			vImpl.edge = HEdgeImpl.requireValid(e);
		}
	}

	/**
	 * @param vertex
	 * @return
	 */
	public static HVertexImpl requireValid(HVertex vertex) {
		return requireValid(vertex, HVertexImpl.class);
	}

	@Override
	public final HEdge edge() {
		requireNotDiscarded();
		return edge;
	}

	@Override
	public final int degree() {
		return Loops.size(edge(), NEXT_INCOMING_EDGE);
	}

	@Override
	public final Sequence<HVertex> neighbors() {
		return outgoingEdges().map(HEdge::head);
	}

	@Override
	public final boolean isNeighborOf(HVertex other) {
		return Loops.anyMatch(edge().opposite(), NEXT_OUTGOING_EDGE,
				e -> e.head() == other);
	}

	@Override
	public final Sequence<HEdge> incomingEdges() {
		return Loop.createLoop(this, HVertex::edge, NEXT_INCOMING_EDGE);
	}

	@Override
	public final Sequence<HEdge> outgoingEdges() {
		return Loop.createLoop(this, v -> v.edge().opposite(),
				NEXT_OUTGOING_EDGE);
	}
}