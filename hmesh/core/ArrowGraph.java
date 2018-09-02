package hgeom.hmesh.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HMesh;
import hgeom.hmesh.elements.HVertex;

/**
 * Association sommet => toutes les aretes pointant vers le sommet. Classe
 * interne aux constructions de {@link HMesh}
 *
 * @author Pierre B.
 */
final class ArrowGraph {

	/**
	 * Association sommet => toutes les aretes pointant vers le sommet
	 */
	private final Map<HVertex, Arrow> arrows = new HashMap<>();

	/**
	 * @param hEdge
	 */
	public void addArrow(HEdge hEdge) {
		addArrow(hEdge, -1);
	}

	/**
	 * @param hEdge
	 * @param cycleIndex
	 */
	public void addArrow(HEdge hEdge, int cycleIndex) {
		Arrow firstArrow = arrows.computeIfAbsent(hEdge.head(),
				v -> new Arrow(hEdge, cycleIndex));

		firstArrow.linkLastIfAbsent(hEdge, cycleIndex);
	}

	/**
	 * Obtient l'une des fleches pointant vers l'un sommet du graphe
	 *
	 * @param head le sommet du graphe
	 * @return la fleche ou {@code null}
	 */
	public Arrow arrow(HVertex head) {
		return arrows.get(head);
	}

	/**
	 * Obtient une fleche situe entre 2 sommets
	 *
	 * @param tail le sommet correspondant a la queue de la fleche
	 * @param head le sommet correspondant a la tete de la fleche
	 * @return la fleche ou {@code null}
	 */
	public Arrow arrow(HVertex tail, HVertex head) {
		Arrow arrow = arrows.get(head);
		return arrow == null ? null : arrow.get(a -> a.tail() == tail);
	}

	/**
	 * @param hEdge
	 * @return
	 */
	public Arrow arrow(HEdge hEdge) {
		return arrows.get(hEdge.head()).get(a -> a.hEdge() == hEdge);
	}

	/**
	 * Obtient un flux sur le sous-ensemble de fleches compose d'une seul fleche
	 * par sommet du graphe
	 *
	 * @return le flux
	 */
	public Collection<Arrow> arrows() {
		return arrows.values();
	}
}
