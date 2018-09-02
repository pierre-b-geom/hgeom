package hgeom.hmesh.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import hgeom.hmesh.elements.HElement;
import hgeom.hmesh.elements.HMesh;

/**
 *
 * @author Pierre B.
 * @param <E>
 */
final class HMeshDeepFirstTreeIterator<E extends HElement>
		extends HMeshTreeIterator<E> {

	/**
	 * les elements de l'arbre range par profondeur. La racine ne fait pas
	 * partie de l'arbre. Les elements de la profondeur d sont contenus dans la
	 * liste situee a l'index d-1. Equivalent a une pile dans laquelle les
	 * listes correspondantes aux elements depiles sont conservees (pour
	 * optimiser les allers et retours du parcours)
	 */
	private final List<List<E>> pendingElements = new ArrayList<>();

	/**
	 * @param mesh
	 * @param root
	 */
	public HMeshDeepFirstTreeIterator(HMesh mesh, E root) {
		super(mesh, root);
	}

	@Override
	protected E iterate(E prev) {
		List<E> deepestElements = depthElements(depth());
		E e = deepestElements.remove(deepestElements.size() - 1);

		if (isVisited(e)) {
			throw new IllegalStateException("visited twice: " + e);
		}

		return e;
	}

	@Override
	protected void postIteration(E e) {

		// Ajout des enfants de l'element courant dans la profondeur un cran
		// en-dessous
		int childrenDepthIndex = depth() + 1;
		List<E> childrenDepthElements = depthElements(childrenDepthIndex);

		if (!childrenDepthElements.isEmpty()) {
			throw new IllegalStateException(
					"should have been empty: " + childrenDepthIndex);
		}

		collectChildren(e, childrenDepthElements);

		// Il faut retirer des nouveaux elements, les elements deja presents en
		// amont dans l'arbre. Ceux-ci doivent etre traites apres selon le
		// principe du "deep-first"
		removeAncestors(childrenDepthElements);

		// Recherche "depth-first" donc positionnement du curseur a la
		// profondeur suivante si des elements s'y trouvent
		if (!childrenDepthElements.isEmpty()) {
			incrementDepth();
		}

		// Dans la pile des elements a traiter, depilement des elements deja
		// visitees jusqu'a arriver a un element pas encore visite. La queue
		// peut en effet contenir plusieurs occurences d'un meme element
		// (ajoutees a partir de plusieurs parents)
		pollWhile(this::isVisited);
	}

	@Override
	protected boolean isIterationOver(E e) {
		return depth() == 0;
	}

	/**
	 * @param childrenDepthElements
	 */
	private void removeAncestors(List<E> childrenDepthElements) {
		for (int i = childrenDepthElements.size() - 1; i >= 0; i--) {
			E e = childrenDepthElements.get(i);

			// La profondeur 0 correspondante a la racine n'est pas presente
			// dans la pile
			for (int iDepth = depth(); iDepth >= 1; iDepth--) {
				if (depthElements(iDepth).contains(e)) {
					childrenDepthElements.remove(i);
					break;
				}
			}
		}
	}

	/**
	 * @param p
	 */
	private void pollWhile(Predicate<E> p) {
		if (depth() > 0) {
			boolean nextToPoll;

			do {
				List<E> cursorElements = depthElements(depth());
				nextToPoll = true;

				while (nextToPoll && !cursorElements.isEmpty()) {
					E e = cursorElements.get(cursorElements.size() - 1);
					nextToPoll = p.test(e);

					if (nextToPoll) {
						cursorElements.remove(cursorElements.size() - 1);
					}
				}

				// Si pas d'element a visiter, remonte a la profondeur au-dessus
				if (cursorElements.isEmpty()) {
					decrementDepth();
				}
			} while (nextToPoll && depth() > 0);
		}
	}

	/**
	 * @param depth
	 * @return
	 */
	private List<E> depthElements(int depth) {
		int depthElementsIndex = depth - 1;

		if (depthElementsIndex < pendingElements.size()) {
			return pendingElements.get(depthElementsIndex);
		}

		List<E> elements = new ArrayList<>();
		pendingElements.add(elements);
		return elements;
	}
}