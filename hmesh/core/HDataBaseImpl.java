package hgeom.hmesh.core;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

/**
 *
 * @author Pierre B.
 * @param <E>
 */
class HDataBaseImpl<E> {

	/**
	 *
	 */
	private final ToIntFunction<E> toIndex;

	/**
	 *
	 */
	private final Supplier<Stream<E>> elementStreamSupplier;

	/**
	 * @param toIndex
	 * @param elementStreamSupplier
	 */
	protected HDataBaseImpl(ToIntFunction<E> toIndex,
			Supplier<Stream<E>> elementStreamSupplier) {

		this.toIndex = Objects.requireNonNull(toIndex);
		this.elementStreamSupplier = elementStreamSupplier;
	}

	/**
	 * @param e
	 * @return
	 */
	protected final int index(E e) {
		return toIndex.applyAsInt(e);
	}

	/**
	 * @return
	 */
	protected final Stream<E> elementStream() {
		return elementStreamSupplier.get();
	}
}
