package hgeom.hmesh.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HVertex;
import hgeom.hmesh.sequence.Sequence;
import hgeom.hmesh.util.Loops;

/**
 * Interne a la construction des graphes demi-arete
 *
 * @author Pierre B.
 */
final class HEdgeCyclesCompletion {

	/**
	 *
	 */
	private final ArrowSorter arrowSorter;

	/**
	 *
	 */
	private final ArrowGraph arrowGraph = new ArrowGraph();

	/**
	 *
	 */
	private final List<Arrow> incomingArrows = new ArrayList<>();

	/**
	 *
	 */
	private final List<HEdge> outgoingHEdges = new ArrayList<>();

	/**
	 *
	 */
	private final Map<Integer, HVertex[]> hEdgeMementos = new HashMap<>();

	/**
	 *
	 */
	private final Deque<BooleanSupplier> plannedVertexConnectionCheckings = new ArrayDeque<>();

	/**
	 *
	 */
	private List<HEdge> hEdgeCycles;

	/**
	 *
	 */
	private List<HEdge> resultHEdges;

	/**
	 * @param arrowSorter
	 */
	public HEdgeCyclesCompletion(ArrowSorter arrowSorter) {
		this.arrowSorter = arrowSorter;
	}

	/**
	 * @param cycles
	 * @return
	 */
	public Optional<List<HEdge>> complete(List<HEdge> cycles) {
		this.hEdgeCycles = Objects.requireNonNull(cycles);

		for (int iCycle = 0; iCycle < cycles.size(); iCycle++) {
			for (HEdge e : cycles.get(iCycle).cycle()) {
				addArrow(e, iCycle);
			}
		}

		hEdgeMementos.clear();
		plannedVertexConnectionCheckings.clear();
		resultHEdges = new ArrayList<>(cycles);

		if (!arrowGraph.arrows().stream()
				.allMatch(this::checkVertexConnections)) {

			return Optional.empty();
		}

		// Marquage des cycles existants
		resultHEdges.stream().filter(Objects::nonNull).forEach(this::markCycle);

		// Recuperation des cycles qui sont apparus. Marquage de ceux-ci
		List<HEdge> extraCycles = arrowGraph.arrows().stream()
				.collect(ArrayList::new, this::createExtraCycles, List::addAll);

		// Pour chaque cycle initial qui a disparu en raison d'une reorientation
		// d'un de ses sommets, recherche d'un equivalent dans les cycles
		// ajoutes
		for (int iCycle = 0; iCycle < resultHEdges.size(); iCycle++) {
			if (resultHEdges.get(iCycle) == null) {
				HVertex[] vertices = hEdgeMementos.get(iCycle);

				for (int jCycle = 0; jCycle < extraCycles.size(); jCycle++) {
					HEdge extraCycle = extraCycles.get(jCycle);

					// Si le cycle ajoute est l'equivalent d'un cycle qui
					// avait disparu, il prend sa place
					if (match(extraCycle, vertices)) {
						extraCycles.remove(jCycle);
						resultHEdges.set(iCycle, extraCycle);
						break;
					}
				}
			}
		}

		resultHEdges.addAll(extraCycles);
		return Optional.of(resultHEdges);
	}

	/**
	 * @param hEdge
	 * @param cycleIndex
	 */
	private void addArrow(HEdge hEdge, int cycleIndex) {
		arrowGraph.addArrow(hEdge, cycleIndex);
		HEdge hEdgeNextOpposite = hEdge.next().opposite();

		// Arrete pointant vers le sommet mais encore deconnecte de sa suivante
		// : arete orpheline qui n'a pas encore ete integree a un cycle. Il
		// faut neanmoins l'ajouter a la liste des aretes qui pointent vers le
		// sommet
		if (hEdgeNextOpposite.next() == null) {
			arrowGraph.addArrow(hEdgeNextOpposite);
		}
	}

	/**
	 * @param arrow
	 * @return
	 */
	private boolean checkVertexConnections(Arrow arrow) {
		boolean ok = checkVertexConnections(arrow, false);

		while (ok && !plannedVertexConnectionCheckings.isEmpty()) {
			ok = plannedVertexConnectionCheckings.removeFirst().getAsBoolean();
		}

		return ok;
	}

	/**
	 * @param firstArrow
	 * @param forceOrientation
	 * @return
	 */
	private boolean checkVertexConnections(Arrow firstArrow,
			boolean forceOrientation) {

		incomingArrows.clear();
		outgoingHEdges.clear();

		// Obtention des liens entre aretes incidentes et aretes partantes
		firstArrow.forEach(arrow -> {
			incomingArrows.add(arrow);
			outgoingHEdges.add(arrow.hEdge().next());
		});

		boolean missingOutgoingHEdges = outgoingHEdges.contains(null);

		// Recensement des aretes opposees d'aretes incidentes seulement
		// lorsqu'elles n'ont pas encore ete ajoutees c'est-a-dire lorsqu'elle
		// n'ont pas de lien avec leurs predecesseurs
		if (missingOutgoingHEdges) {
			firstArrow.forEach(arrow -> {
				HEdge incomingHEdgeOpposite = arrow.hEdge().opposite();

				if (!outgoingHEdges.contains(incomingHEdgeOpposite)) {
					outgoingHEdges.add(incomingHEdgeOpposite);
				}
			});
		}

		return fixVertexConnections(incomingArrows, outgoingHEdges,
				forceOrientation);
	}

	/**
	 * @param incomingArrows
	 * @param outgoingHEdges
	 * @param forceOrientation
	 * @return
	 */
	private boolean fixVertexConnections(List<Arrow> incomingArrows,
			List<HEdge> outgoingHEdges, boolean forceOrientation) {

		// Le tableau des demi-aretes sortantes contient maintenant toutes les
		// demi-aretes sortantes plus un nombre de valeurs null correspondant au
		// nombre de connexions manquantes
		int numIncomingArrows = incomingArrows.size();
		int numMissingConnections = outgoingHEdges.size() - numIncomingArrows;
		boolean orientVertexConnections;

		if (numMissingConnections == 0) {

			// pour le cas de 2 aretes incidentes, la phase d'orientation est
			// toujours inutile
			orientVertexConnections = incomingArrows.size() == 2 ? false
					: forceOrientation;
		}

		else if (numMissingConnections == 1) {
			orientVertexConnections = forceOrientation;

			if (!orientVertexConnections) {
				HEdge lastOutgoingHEdge = outgoingHEdges
						.get(outgoingHEdges.size() - 1);

				boolean missingLink = true;

				for (int iArrow = 0; missingLink
						&& iArrow < numIncomingArrows; iArrow++) {

					Arrow arrow = incomingArrows.get(iArrow);

					// Etablissement du seul lien manquant
					if (arrow.hEdge().next() == null) {
						HEdgeImpl.link(arrow.hEdge(), lastOutgoingHEdge);
						missingLink = false;
					}
				}

				if (missingLink) {
					throw new IllegalStateException();
				}
			}
		}

		else {

			// Au moins 2 aretes incidentes privees de lien avec leurs
			// suivantes. Il faut utiliser l'orientation
			orientVertexConnections = true;
		}

		if (orientVertexConnections) {
			if (arrowSorter == null) {
				return false;
			}

			orientVertexConnections(incomingArrows, outgoingHEdges);
		}

		return true;
	}

	/**
	 * @param incomingArrows
	 * @param outgoingHEdges
	 */
	private void orientVertexConnections(List<Arrow> incomingArrows,
			List<HEdge> outgoingHEdges) {

		int numIncomingArrows = incomingArrows.size();
		List<Arrow> sortedIncomingArrows = new ArrayList<>(incomingArrows);

		// sauvegarde prealable des cycles initiaux traversant le sommet. La
		// sauvegarde sera utilisee pour restaurer les cycles disparus
		for (int iArrow = 0; iArrow < numIncomingArrows; iArrow++) {
			int cycleIndex = sortedIncomingArrows.get(iArrow).cycleIndex();

			// Le memento de sauvegarde est constitue des sommets du cycle
			if (cycleIndex != -1 && hEdgeMementos.get(cycleIndex) == null) {
				hEdgeMementos.put(cycleIndex,
						vertices(hEdgeCycles.get(cycleIndex)));
			}
		}

		// Ordonnancement en etoile des aretes pointant vers la deconnexion
		arrowSorter.sort(sortedIncomingArrows);

		// Connexion de toutes les aretes incidentes avec leurs suivantes apres
		// l'ordonnancement
		Loops.forEachPair(sortedIncomingArrows,
				HEdgeCyclesCompletion::joinIncomingHEdges);

		// Toutes les aretes incidentes ont maintenant un lien vers leurs
		// suivantes
		boolean neighbhorsNeedUpdate = false;

		for (int iArrow = 0; iArrow < numIncomingArrows; iArrow++) {
			Arrow arrow = sortedIncomingArrows.get(iArrow);
			HEdge newHEdgeNext = arrow.hEdge().next();

			HEdge oldHEdgeNext = outgoingHEdges
					.get(incomingArrows.indexOf(arrow));

			if (oldHEdgeNext != null && newHEdgeNext != oldHEdgeNext) {

				// L'arete incidente a change de suivante ce qui signifie qu'un
				// cycle a ete modifie. Il est necessaire de renouveler les
				// connexions environnantes
				neighbhorsNeedUpdate = true;

				// Le cycle modifie est retire de la liste des cycles resultat
				if (arrow.cycleIndex() != -1) {
					resultHEdges.set(arrow.cycleIndex(), null);
				}
			}
		}

		if (neighbhorsNeedUpdate) {
			for (int iArrow = 0; iArrow < numIncomingArrows; iArrow++) {
				HVertex neighbor = sortedIncomingArrows.get(iArrow).tail();
				Arrow arrow = arrowGraph.arrow(neighbor);

				// Pour eviter un depassement de pile, la verification des
				// connexions voisines s'effectue dans un runnable execute
				// ulterieurement plutot que tout de suite dans la pile
				plannedVertexConnectionCheckings
						.add(() -> checkVertexConnections(arrow, true));
			}
		}
	}

	/**
	 * @param arrow1
	 * @param arrow2
	 */
	private static void joinIncomingHEdges(Arrow arrow1, Arrow arrow2) {
		HEdgeImpl.link(arrow2.hEdge(), arrow1.hEdge().opposite());
	}

	/**
	 * @param extraCycles
	 * @param firstArrow
	 */
	private void createExtraCycles(List<HEdge> extraCycles, Arrow firstArrow) {
		firstArrow.forEach(arrow -> {
			if (!arrow.isMarked()) {
				extraCycles.add(arrow.hEdge());
				markCycle(arrow.hEdge());
			}
		});
	}

	/**
	 * @param hEdge la premiere demi-arete d'un cycle
	 */
	private void markCycle(HEdge hEdge) {
		hEdge.cycle().map(arrowGraph::arrow).forEach(Arrow::mark);
	}

	/**
	 * @param cycleFirst la premiere demi-arete d'un cycle
	 * @return les sommets du cycle
	 */
	private HVertex[] vertices(HEdge cycleFirst) {
		HVertex[] vertices = new HVertex[cycleFirst.cycle().count()];
		HEdge e = cycleFirst;

		for (int iHEdge = 0; iHEdge < vertices.length; iHEdge++) {
			vertices[iHEdge] = e.head();
			e = e.next();
		}

		return vertices;
	}

	/**
	 * @param hEdge    la premiere demi-arete d'un cycle
	 * @param vertices
	 * @return true si les sommets du cycle
	 */
	private boolean match(HEdge hEdge, HVertex[] vertices) {
		Sequence<HEdge> cycle = hEdge.cycle();
		int numHEdges = cycle.count();

		if (vertices.length != numHEdges) {
			return false;
		}

		Optional<HEdge> startingHEdge = cycle
				.filter(e -> e.head() == vertices[0]).findFirst();

		boolean ok = startingHEdge.isPresent();

		if (ok) {
			HEdge he = startingHEdge.get().next();

			// Comparaison dans un sens (sauf pour le premier sommet)
			for (int i = 1; ok && i < numHEdges; i++) {
				ok = he.head() == vertices[i];
				he = he.next();
			}

			if (!ok) {
				ok = true;
				he = startingHEdge.get().next();

				// Comparaison dans l'autre sens
				for (int i = numHEdges - 1; ok && i >= 1; i--) {
					ok = he.head() == vertices[i];
					he = he.next();
				}
			}
		}

		return ok;
	}
}
