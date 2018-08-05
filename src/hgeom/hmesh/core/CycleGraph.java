package hgeom.hmesh.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.IntFunction;
import java.util.logging.Logger;

import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HMesh;
import hgeom.hmesh.elements.HVertex;
import hgeom.hmesh.util.Loops;
import hgeom.hmesh.util.Loops.Direction;
import hgeom.hmesh.winding.PolygonWindingProvider;
import hgeom.hmesh.winding.Winding;

/**
 * Graphe compose de cycles d'aretes orientes. Les aretes contiennent des
 * demi-aretes. Classe interne a la construction de {@link HMesh}. Une arete ne
 * peut appartenir qu'a 2 cycles
 *
 * @author Pierre B.
 */
final class CycleGraph {

	/**
	 *
	 */
	private static final Logger LOGGER = Logger
			.getLogger(CycleGraph.class.getName());

	/**
	 *
	 */
	private final HElementFactory elementFactory;

	/**
	 *
	 */
	private final IntFunction<HVertex> vertexSupplier;

	/**
	 *
	 */
	private final PolygonWindingProvider windingProvider;

	/**
	 * Aretes classees dans un multimap avec index de sommet en cle
	 */
	private final Map<Integer, CycleEdgePair> verticesToEdges = new HashMap<>();

	/**
	 * Aretes classees par cycle d'appartenance
	 */
	private final List<CycleEdgePair[]> cycleEdges = new ArrayList<>();

	/**
	 *
	 */
	private final List<Integer> cycleIndices = new ArrayList<>();

	/**
	 * @param elementFactory
	 * @param vertexSupplier
	 * @param windingProvider si non {@code null}, utilise pour obtenir
	 *                        l'orientation du cycle a partir de sa geometrie
	 */
	public CycleGraph(HElementFactory elementFactory,
			IntFunction<HVertex> vertexSupplier,
			PolygonWindingProvider windingProvider) {

		this.elementFactory = Objects.requireNonNull(elementFactory);
		this.vertexSupplier = Objects.requireNonNull(vertexSupplier);
		this.windingProvider = windingProvider;
	}

	/**
	 * @param cycleIndex
	 * @param cycle
	 * @return
	 */
	public boolean addCycle(int cycleIndex, int[] cycle) {
		if (!validateCycle(cycle)) {
			return false;
		}

		CycleEdgePair[] edges = new CycleEdgePair[cycle.length];

		if (!createEdges(cycle, edges)) {
			return false;
		}

		// Tous les aretes sont disponibles. Il devient possible d'ajouter un
		// cycle au graphe
		int cycleId = cycleEdges.size();

		for (CycleEdgePair edge : edges) {
			edge.addAdjacentCycle(cycleId);
		}

		cycleIndices.add(cycleIndex);
		cycleEdges.add(edges);
		return true;
	}

	/**
	 * @param cycle
	 * @return
	 */
	private static boolean validateCycle(int[] cycle) {
		int numVertices = cycle.length;

		if (numVertices < 3) {
			LOGGER.warning("cannot add cycle with less than 3 vertices");
			return false;
		}

		// Rejet des cycles contenant 2 fois le meme sommet
		for (int iVertex = 0; iVertex < numVertices; iVertex++) {
			for (int jVertex = iVertex + 1; jVertex < numVertices; jVertex++) {
				if (cycle[iVertex] == cycle[jVertex]) {
					LOGGER.warning("cannot add self-crossing cycle");
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * @param cycle
	 * @param edges
	 * @return
	 */
	private boolean createEdges(int[] cycle, CycleEdgePair[] edges) {

		// Verification qu'aucune des aretes n'est deja reliee a 2 faces
		for (int iEdge = 0; iEdge < cycle.length; iEdge++) {
			int v1Index = cycle[iEdge];
			int v2Index = Loops.get(cycle, iEdge + 1);

			if (v1Index < 0 || v2Index < 0) {
				throw new IllegalStateException("Illegal vertex index found");
			}

			CycleEdgePair edge = cycleEdge(v1Index, v2Index);

			if (edge.cycle1Id() != -1 && edge.cycle2Id() != -1) {
				LOGGER.warning(
						"cannot add cycle. One of its edges already present "
								+ "in two other cycles");

				return false;
			}

			edges[iEdge] = edge;
		}

		return true;
	}

	/**
	 * @param v1Index
	 * @param v2Index
	 * @return
	 */
	private CycleEdgePair cycleEdge(int v1Index, int v2Index) {
		CycleEdgePair cycleEdge;
		int vMinIndex = Math.min(v1Index, v2Index);
		int vMaxIndex = Math.max(v1Index, v2Index);

		// La premiere arete a recuperer dans la table de hachage avec
		// l'indice de sommet minimal
		cycleEdge = verticesToEdges.computeIfAbsent(vMinIndex,
				k -> new CycleEdgePair(vMinIndex, vMaxIndex, this::edge));

		// 2eme parcours dans les aretes chainees a partir de l'indice de
		// sommet maximal
		return cycleEdge.computeIfAbsent(vMaxIndex,
				() -> new CycleEdgePair(vMinIndex, vMaxIndex, this::edge));
	}

	/**
	 * @param vertexIndex
	 * @return
	 */
	private HEdge edge(int vertexIndex) {
		HVertex vertex = vertexSupplier.apply(vertexIndex);
		HEdge edge = elementFactory.createEdge(vertex);
		HVertexImpl.setEdgeIfAbsent(vertex, edge);
		return edge;
	}

	/**
	 * @return
	 */
	public int numCycles() {
		return cycleEdges.size();
	}

	/**
	 * @param cycleId
	 * @return
	 */
	public int getCycleIndex(int cycleId) {
		return cycleIndices.get(cycleId);
	}

	/**
	 * @param cycleId
	 * @param edge
	 * @param fromV1toV2
	 * @param action
	 * @return
	 */
	public boolean forEachCycleEdgePair(int cycleId, CycleEdgePair edge,
			boolean fromV1toV2,
			BiPredicate<CycleEdgePair, CycleEdgePair> action) {

		CycleEdgePair[] edges = cycleEdges.get(cycleId);
		Direction direction = iterationDirection(edges, edge, fromV1toV2);
		return Loops.takePairWhile(edges, action, direction);
	}

	/**
	 * @param edges
	 * @param edge
	 * @param fromV1toV2
	 * @return sens de lecture du cycle
	 */
	private Direction iterationDirection(CycleEdgePair[] edges,
			CycleEdgePair edge, boolean fromV1toV2) {

		for (int iEdge = 0; iEdge < edges.length; iEdge++) {
			if (edge == edges[iEdge]) {
				int toIndex = fromV1toV2 ? edge.v2Index() : edge.v1Index();
				CycleEdgePair nextEdge = Loops.get(edges, iEdge + 1);

				if (edge.sharedVertexIndex(nextEdge) == toIndex) {
					return Direction.FORWARD;
				}

				CycleEdgePair previousEdge = Loops.get(edges, iEdge - 1);

				if (edge.sharedVertexIndex(previousEdge) == toIndex) {
					return Direction.BACKWARD;
				}
			}
		}

		throw new IllegalStateException("edge: " + edge);
	}

	/**
	 * @param cycleId
	 * @param expectedWinding
	 * @param action
	 * @return
	 */
	public boolean forEachCycleEdgePair(int cycleId, Winding expectedWinding,
			BiPredicate<CycleEdgePair, CycleEdgePair> action) {

		CycleEdgePair[] edges = cycleEdges.get(cycleId);
		Direction direction = iterationDirection(edges, expectedWinding);
		return Loops.takePairWhile(edges, action, direction);
	}

	/**
	 * @param edges
	 * @param expectedWinding
	 * @return sens de lecture du cycle correspondant a son orientation
	 */
	private Direction iterationDirection(CycleEdgePair[] edges,
			Winding expectedWinding) {

		if (expectedWinding == Winding.UNDETERMINED) {
			throw new IllegalStateException();
		}

		// Choix arbitraire si pas de possibilite de determiner la direction a
		// l'aide de l'orienteur
		if (windingProvider == null) {
			return Direction.FORWARD;
		}

		Winding cycleWinding = windingProvider.get(vertices(edges));

		// Choix arbitraire si echec de la determination
		if (cycleWinding == Winding.UNDETERMINED) {
			return Direction.FORWARD;
		}

		if (cycleWinding == expectedWinding) {
			return Direction.FORWARD;
		}

		// Si l'orientation souhaitee du cycle est l'inverse de son orientation
		// geometrique, la lecture doit etre inversee
		return Direction.BACKWARD;
	}

	/**
	 * @param edges
	 * @return
	 */
	private int[] vertices(CycleEdgePair[] edges) {
		int[] vertices = new int[edges.length];

		Arrays.setAll(vertices, i -> {
			CycleEdgePair edge = edges[i];
			CycleEdgePair nextEdge = Loops.get(edges, i + 1);
			return edge.sharedVertexIndex(nextEdge);
		});

		return vertices;
	}
}
