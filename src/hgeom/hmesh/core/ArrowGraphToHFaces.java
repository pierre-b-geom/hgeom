package hgeom.hmesh.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HFace;
import hgeom.hmesh.elements.HFace.Status;
import hgeom.hmesh.util.Loops;

/**
 *
 * @author Pierre B.
 */
final class ArrowGraphToHFaces {

	/**
	 *
	 */
	private final HElementFactory elementFactory;

	/**
	 *
	 */
	private final ArrowSorter arrowSorter;

	/**
	 * buffer de fleches
	 */
	private final List<Arrow> arrows = new ArrayList<>();

	/**
	 *
	 */
	private ArrowGraph arrowGraph;

	/**
	 * @param elementFactory
	 * @param arrowSorter
	 */
	public ArrowGraphToHFaces(HElementFactory elementFactory,
			ArrowSorter arrowSorter) {

		this.elementFactory = Objects.requireNonNull(elementFactory);
		this.arrowSorter = Objects.requireNonNull(arrowSorter);
	}

	/**
	 * Le graphe passe en entree est modifie ! toutes les branches ne formant
	 * pas de face sont supprimees
	 *
	 * @param arrowGraph
	 * @return
	 */
	public Optional<List<HFace>> convert(ArrowGraph arrowGraph) {
		this.arrowGraph = arrowGraph;
		arrowGraph.arrows().stream().forEach(this::joinVertexArrows);

		List<HFace> faces = new ArrayList<>();

		for (Arrow arrow : arrowGraph.arrows()) {
			if (!createFaces(faces, arrow)) {
				return Optional.empty();
			}
		}

		return Optional.of(faces);
	}

	/**
	 * Ordonnancement en etoile des aretes pointant vers la deconnexion
	 *
	 * @param firstArrow
	 */
	private void joinVertexArrows(Arrow firstArrow) {
		arrows.clear();
		firstArrow.forEach(arrows::add);
		arrowSorter.sort(arrows);
		Loops.forEachPair(arrows, ArrowGraphToHFaces::joinIncomingArrows);
	}

	/**
	 * @param arrow1
	 * @param arrow2
	 */
	private static void joinIncomingArrows(Arrow arrow1, Arrow arrow2) {
		HEdgeImpl.link(arrow2.hEdge(), arrow1.hEdge().opposite());
	}

	/**
	 * @param faces
	 * @param firstArrow
	 * @return
	 */
	private boolean createFaces(List<HFace> faces, Arrow firstArrow) {
		return firstArrow.forEachWhile(arrow -> createFace(faces, arrow));
	}

	/**
	 * Marque toutes les fleches formant une face
	 *
	 * @param faces
	 *
	 * @param faceArrow une des fleches de la face
	 * @return
	 */
	private boolean createFace(List<HFace> faces, Arrow faceArrow) {
		if (!faceArrow.isMarked()) {
			Arrow arrow = faceArrow;

			do {
				arrow.mark();
				HEdge hEdgeNext = arrow.hEdge().next();

				// La chaine de demi-aretes s'arete. Puisqu'il n'y a pas de
				// cloture, la face ne peut etre creee
				if (hEdgeNext == null) {
					return false;
				}

				// Obtention fleche recouvrant l'arete suivante
				arrow = arrowGraph.arrow(arrow.head(), hEdgeNext.head());
			} while (arrow != faceArrow);

			HFace face = elementFactory.createFace(arrow.hEdge(),
					Status.UNKNOWN, true);

			if (face == null) {
				return false;
			}

			faces.add(face);
		}

		return true;
	}
}
