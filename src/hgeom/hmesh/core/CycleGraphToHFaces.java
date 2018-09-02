package hgeom.hmesh.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import hgeom.hmesh.data.HIData;
import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HFace;
import hgeom.hmesh.elements.HFace.Status;
import hgeom.hmesh.util.Loops;
import hgeom.hmesh.winding.Winding;

/**
 *
 * @author Pierre B.
 */
final class CycleGraphToHFaces {

	/**
	 *
	 */
	private final HElementFactory elementFactory;

	/**
	 *
	 */
	private final ArrowSorter arrowSorter;

	/**
	 *
	 */
	private final List<HEdge> hEdges = new ArrayList<>();

	/**
	 *
	 */
	private final List<CycleEdgePair> adjEdges = new ArrayList<>();

	/**
	 *
	 */
	private final List<Boolean> adjFromV1toV2s = new ArrayList<>();

	/**
	 *
	 */
	private final List<Integer> adjCycleIds = new ArrayList<>();

	/**
	 *
	 */
	private final Deque<BooleanSupplier> plannedCycleCreations = new ArrayDeque<>();

	/**
	 *
	 */
	private final HIData<HFace> faceIndices;

	/**
	 *
	 */
	private CycleGraph cycleGraph;

	/**
	 *
	 */
	private HEdge[] cycleHEdges;

	/**
	 * @param elementFactory
	 * @param arrowSorter
	 * @param faceIndices
	 */
	public CycleGraphToHFaces(HElementFactory elementFactory,
			ArrowSorter arrowSorter, HIData<HFace> faceIndices) {

		this.elementFactory = Objects.requireNonNull(elementFactory);
		this.arrowSorter = arrowSorter;
		this.faceIndices = faceIndices;
	}

	/**
	 * @param cycleGraph
	 * @return le graphe sous la forme de cycle de demi-aretes
	 */
	public Optional<List<HFace>> convert(CycleGraph cycleGraph) {
		this.cycleGraph = Objects.requireNonNull(cycleGraph);
		cycleHEdges = new HEdge[cycleGraph.numCycles()];
		plannedCycleCreations.clear();
		boolean ok = true;

		for (int iCycle = 0; ok && iCycle < cycleHEdges.length; iCycle++) {
			ok = createCycle(iCycle);

			// Execution des creations d'aretes pour les cycles adjacents
			while (ok && !plannedCycleCreations.isEmpty()) {
				ok = plannedCycleCreations.removeFirst().getAsBoolean();
			}
		}

		if (ok) {
			HEdgeCyclesCompletion hEdgesCompletion = new HEdgeCyclesCompletion(
					arrowSorter);

			return hEdgesCompletion.complete(Arrays.asList(cycleHEdges))
					.map(this::cyclesToFaces);
		}

		return Optional.empty();
	}

	/**
	 * @param cycles
	 * @return
	 */
	private List<HFace> cyclesToFaces(List<HEdge> cycles) {
		List<HFace> faces = new ArrayList<>(cycles.size());

		for (int iCycle = 0; iCycle < cycles.size(); iCycle++) {
			HEdge cycleEdge = cycles.get(iCycle);

			if (cycleEdge != null) {
				HFace face;

				if (iCycle < cycleGraph.numCycles()) {
					face = elementFactory.createFace(cycleEdge, Status.INTERIOR,
							false);

					int cycleIndex = cycleGraph.getCycleIndex(iCycle);
					faceIndices.set(face, cycleIndex);
				}

				else {
					face = elementFactory.createFace(cycleEdge, Status.EXTERIOR,
							false);

					faceIndices.set(face, -1);
				}

				faces.add(face);
			}
		}

		return faces;
	}

	/**
	 * @param cycleId
	 * @return
	 */
	private boolean createCycle(int cycleId) {
		if (cycleHEdges[cycleId] == null) {
			BooleanSupplier cycleDataCollector = () -> cycleGraph
					.forEachCycleEdgePair(cycleId, Winding.COUNTERCLOCKWISE,
							(edge1, edge2) -> collectCycleData(cycleId, edge1,
									edge2));

			return createCycle(cycleId, cycleDataCollector);
		}

		return true;
	}

	/**
	 * @param cycleId
	 * @param edge
	 * @param fromV1toV2
	 * @return
	 */
	private boolean createCycle(int cycleId, CycleEdgePair edge,
			boolean fromV1toV2) {

		if (cycleHEdges[cycleId] == null) {
			BooleanSupplier cycleDataCollector = () -> cycleGraph
					.forEachCycleEdgePair(cycleId, edge, fromV1toV2, (edge1,
							edge2) -> collectCycleData(cycleId, edge1, edge2));

			return createCycle(cycleId, cycleDataCollector);
		}

		return true;
	}

	/**
	 * @param cycleId
	 * @param cycleDataCollector
	 * @return
	 */
	private boolean createCycle(int cycleId,
			BooleanSupplier cycleDataCollector) {

		hEdges.clear();
		adjCycleIds.clear();
		adjEdges.clear();
		adjFromV1toV2s.clear();

		if (cycleDataCollector.getAsBoolean()) {

			// Liaison des demi-aretes du cycle
			Loops.forEachPair(hEdges, HEdgeImpl::link);
			cycleHEdges[cycleId] = hEdges.get(0);

			// Pour eviter un depassement de pile, l'obtention des demi-aretes
			// des cycles adjacents s'effectuent dans des runnables executes
			// ulterieurement plutot que dans la pile
			for (int iAdjCycle = 0; iAdjCycle < adjEdges.size(); iAdjCycle++) {
				int adjCycleId = adjCycleIds.get(iAdjCycle);

				if (adjCycleId != -1 && cycleHEdges[adjCycleId] == null) {
					CycleEdgePair adjEdge = adjEdges.get(iAdjCycle);
					boolean adjFromV1toV2 = adjFromV1toV2s.get(iAdjCycle);

					plannedCycleCreations.add(() -> createCycle(adjCycleId,
							adjEdge, adjFromV1toV2));
				}
			}

			return true;
		}

		return false;
	}

	/**
	 * @param cycleId
	 * @param edge
	 * @param nextEdge
	 * @return
	 */
	private boolean collectCycleData(int cycleId, CycleEdgePair edge,
			CycleEdgePair nextEdge) {

		HEdge hEdge = edge.edgeIncomingTo(nextEdge);

		// Si l'arete est deja liee a sa suivante, c'est qu'elle
		// fait deja partie d'un autre cycle, auquel cas il y a echec de
		// la creation
		if (hEdge.next() != null) {
			return false;
		}

		hEdges.add(hEdge);
		adjEdges.add(edge);
		adjFromV1toV2s.add(hEdge.opposite() == edge.edgeFromV1ToV2());
		adjCycleIds.add(edge.oppositeCycleId(cycleId));
		return true;
	}
}
