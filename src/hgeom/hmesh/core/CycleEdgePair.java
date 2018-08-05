package hgeom.hmesh.core;

import java.util.function.IntFunction;
import java.util.function.Supplier;

import hgeom.hmesh.elements.HEdge;

/**
 * L'arete situee entre 2 sommets et 2 limites de faces
 */
final class CycleEdgePair {

	/**
	 * La prochaine arete ou {@code null}
	 */
	private CycleEdgePair next;

	/**
	 * L'index du premier sommet
	 */
	private final int v1Index;

	/**
	 * L'index du second sommet
	 */
	private final int v2Index;

	/**
	 * La premiere demi-arete
	 */
	private final HEdge e1;

	/**
	 * L'identifiant du premier cycle adjacent a l'arete
	 */
	private int cycle1Id = -1;

	/**
	 * L'identifiant du second cycle adjacent a l'arete
	 */
	private int cycle2Id = -1;

	/**
	 * @param v1Index
	 * @param v2Index
	 * @param hEdgeSupplier
	 */
	public CycleEdgePair(int v1Index, int v2Index,
			IntFunction<HEdge> hEdgeSupplier) {

		this.v1Index = v1Index;
		this.v2Index = v2Index;
		e1 = hEdgeSupplier.apply(v1Index);
		HEdge hedge2 = hEdgeSupplier.apply(v2Index);
		HEdgeImpl.linkAsOpposites(e1, hedge2);
	}

	/**
	 * @return
	 */
	public int cycle1Id() {
		return cycle1Id;
	}

	/**
	 * @return
	 */
	public int cycle2Id() {
		return cycle2Id;
	}

	/**
	 * @return
	 */
	public int v1Index() {
		return v1Index;
	}

	/**
	 * @return
	 */
	public int v2Index() {
		return v2Index;
	}

	/**
	 * @return
	 */
	public HEdge edgeFromV2ToV1() {
		return e1;
	}

	/**
	 * @return
	 */
	public HEdge edgeFromV1ToV2() {
		return e1.opposite();
	}

	/**
	 * @param cycleId
	 * @return
	 */
	public int oppositeCycleId(int cycleId) {
		if (cycleId == cycle1Id) {
			return cycle2Id;
		}

		if (cycleId == cycle2Id) {
			return cycle1Id;
		}

		throw new IllegalStateException("Cycle id: " + cycleId);
	}

	/**
	 * @param otherPair
	 * @return la demi-arete dont le sommet est inclu dans l'arete passee en
	 *         argument ou {@code null}
	 */
	public HEdge edgeIncomingTo(CycleEdgePair otherPair) {
		if (v1Index == otherPair.v1Index || v1Index == otherPair.v2Index) {
			return e1;
		}

		if (v2Index == otherPair.v1Index || v2Index == otherPair.v2Index) {
			return e1.opposite();
		}

		return null;
	}

	/**
	 * @param otherEdge
	 * @return l'index du sommet partage avec l'arete passee en argument
	 */
	public int sharedVertexIndex(CycleEdgePair otherEdge) {
		if (v1Index == otherEdge.v1Index || v1Index == otherEdge.v2Index) {
			return v1Index;
		}

		if (v2Index == otherEdge.v1Index || v2Index == otherEdge.v2Index) {
			return v2Index;
		}

		return -1;
	}

	/**
	 * @param cycleId
	 */
	public void addAdjacentCycle(int cycleId) {
		if (cycle1Id == -1) {
			cycle1Id = cycleId;
		}

		else if (cycle2Id == -1) {
			cycle2Id = cycleId;
		}

		else {
			throw new IllegalStateException("Edge already adjacent to 2 faces");
		}
	}

	/**
	 * @param vIndex
	 * @param supplier
	 * @return
	 */
	public CycleEdgePair computeIfAbsent(int vIndex,
			Supplier<CycleEdgePair> supplier) {

		if (this.v2Index == vIndex) {
			return this;
		}

		if (next == null) {
			next = supplier.get();
			return next;
		}

		return next.computeIfAbsent(vIndex, supplier);
	}
}