package hgeom.hmesh.core;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

import hgeom.hmesh.data.HData;

/**
 *
 * @author Pierre B.
 * @param <E>
 * @param <D>
 */
final class HDataImpl<E, D> extends HDataBaseImpl<E> implements HData<E, D> {

	/**
	 *
	 */
	private Object[] values = new Object[100];

	/**
	 * @param toIndex
	 * @param elementStreamSupplier
	 */
	public HDataImpl(ToIntFunction<E> toIndex,
			Supplier<Stream<E>> elementStreamSupplier) {

		super(toIndex, elementStreamSupplier);
	}

	@Override
	@SuppressWarnings("unchecked")
	public D get(E e) {
		int index = index(e);
		return index < values.length ? (D) values[index] : null;
	}

	@Override
	public void set(E e, D d) {
		int index = index(e);
		expandValues(index + 1);
		values[index] = d;
	}

	@Override
	public void clear() {
		Arrays.fill(values, null);
	}

	@Override
	public void setAll(Function<E, D> generator) {
		elementStream().forEach(e -> set(e, generator.apply(e)));
	}

	@Override
	public Stream<D> stream() {
		return elementStream().map(this::get);
	}

	/**
	 * Copie simplifiee du code ArrayList
	 *
	 * @param minCapacity
	 */
	private void expandValues(int minCapacity) {
		if (values.length < minCapacity) {
			int newCapacity = values.length + (values.length >> 1);

			if (newCapacity - minCapacity < 0) {
				newCapacity = minCapacity;
			}

			values = Arrays.copyOf(values, newCapacity);
		}
	}
}
