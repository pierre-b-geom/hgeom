package hgeom.hmesh.core;

import java.util.Objects;
import java.util.function.BiFunction;

import hgeom.hmesh.core.HMeshWalker.Monitor;
import hgeom.hmesh.elements.HElement;
import hgeom.hmesh.elements.HMesh;

/**
 *
 * @author Pierre B.
 * @param <E>
 */
final class HMeshPathIterator<E extends HElement> extends HMeshIterator<E> {

	/**
	 *
	 */
	private final BiFunction<E, Monitor<E>, E> next;

	/**
	 * @param mesh
	 * @param start
	 * @param next
	 */
	public HMeshPathIterator(HMesh mesh, E start,
			BiFunction<E, Monitor<E>, E> next) {

		super((HMeshImpl) mesh, start);
		this.next = Objects.requireNonNull(next);
	}

	@Override
	protected E iterate(E prev) {
		return next.apply(prev, this);
	}

	@Override
	protected boolean isIterationOver(E e) {
		return e == null;
	}
}