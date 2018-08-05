package hgeom.hmesh.core;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import hgeom.hmesh.core.HMeshWalker.Monitor;
import hgeom.hmesh.elements.HElement;

/**
 *
 * @author Pierre B.
 * @param <E>
 */
abstract class HMeshIterator<E extends HElement>
		implements Iterator<E>, Monitor<E> {

	/**
	 *
	 */
	private enum State {

			/**
			 *
			 */
			NOT_STARTED,

			/**
			 *
			 */
			ITERATING,

			/**
			 *
			 */
			FINISHED
	}

	/**
	 *
	 */
	private final HMeshImpl mesh;

	/**
	 *
	 */
	private final E seed;

	/**
	 *
	 */
	private final int expectedModCount;

	/**
	 *
	 */
	private final HMeshElementFlags elementVisited;

	/**
	 *
	 */
	private E e;

	/**
	 *
	 */
	private int numVisits;

	/**
	 *
	 */
	private State state = State.NOT_STARTED;

	/**
	 * @param mesh
	 * @param seed
	 */
	protected HMeshIterator(HMeshImpl mesh, E seed) {
		this.mesh = Objects.requireNonNull(mesh);
		this.seed = Objects.requireNonNull(seed);
		expectedModCount = mesh.modCount();
		elementVisited = new HMeshElementFlags(mesh);
	}

	@Override
	public final boolean isVisited(E e) {
		return elementVisited.get(e);
	}

	@Override
	public final int numVisits() {
		return numVisits;
	}

	@Override
	public final boolean hasNext() {
		return state != State.FINISHED;
	}

	@Override
	public final E next() {
		if (state == State.FINISHED) {
			throw new NoSuchElementException();
		}

		if (mesh.modCount() != expectedModCount) {
			throw new ConcurrentModificationException();
		}

		if (state == State.NOT_STARTED) {
			state = State.ITERATING;
			e = seed;
		}

		else {
			e = iterate(e);
		}

		if (e != null) {
			elementVisited.set(e);
			numVisits++;
		}

		postIteration(e);

		if (isIterationOver(e)) {
			state = State.FINISHED;
		}

		return e;
	}

	/**
	 * @param prev
	 * @return
	 */
	protected abstract E iterate(E prev);

	/**
	 * @param e
	 */
	protected void postIteration(E e) {
		// Redefinie par les classes derivees si besoin
	}

	/**
	 * @param e
	 * @return
	 */
	protected abstract boolean isIterationOver(E e);
}
