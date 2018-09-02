package hgeom.hmesh.core;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Predicate;

import hgeom.hmesh.elements.HElement;
import hgeom.hmesh.elements.HMesh;

/**
 *
 * @author Pierre B.
 * @param <E>
 */
final class HMeshBreadthFirstTreeIterator<E extends HElement>
		extends HMeshTreeIterator<E> {

	/**
	 *
	 */
	private Queue<E> depthPendingElements = new ArrayDeque<>();

	/**
	 *
	 */
	private Queue<E> nextDepthPendingElements = new ArrayDeque<>();

	/**
	 * @param mesh
	 * @param root
	 */
	public HMeshBreadthFirstTreeIterator(HMesh mesh, E root) {
		super(mesh, root);
	}

	@Override
	protected E iterate(E prev) {
		E e = depthPendingElements.remove();

		if (isVisited(e)) {
			throw new IllegalStateException("visited twice: " + e);
		}

		return e;
	}

	@Override
	protected void postIteration(E e) {

		// Ajout des enfants de l'element courant dans la profondeur un cran
		// en-dessous
		collectChildren(e, nextDepthPendingElements);

		// Dans la pile des elements a traiter, depilement des elements deja
		// visitees jusqu'a arriver a un element pas encore visite. La queue
		// peut en effet contenir plusieurs occurences d'un meme element
		// (ajoutees a partir de plusieurs parents)
		pollWhile(this::isVisited);
	}

	@Override
	protected boolean isIterationOver(E e) {
		return depthPendingElements.isEmpty();
	}

	/**
	 * @param p
	 */
	private void pollWhile(Predicate<E> p) {
		pollWhile(depthPendingElements, p);

		// Passage a la profondeur suivante si profondeur courante epuisee
		if (depthPendingElements.isEmpty()) {
			incrementDepth();

			// Inversion des 2 queues : la queue contenant les elements de la
			// profondeur suivante devient la queue de la profondeur courante
			Queue<E> q = depthPendingElements;
			depthPendingElements = nextDepthPendingElements;
			nextDepthPendingElements = q;

			// De nouveau, elimination des elements de la profondeur
			pollWhile(depthPendingElements, p);
		}
	}

	/**
	 * @param q
	 * @param p
	 */
	private static <E> void pollWhile(Queue<E> q, Predicate<E> p) {
		if (!q.isEmpty()) {
			boolean nextToRemove;

			do {
				nextToRemove = p.test(q.peek());

				if (nextToRemove) {
					q.remove();
				}
			} while (nextToRemove && !q.isEmpty());
		}
	}
}
