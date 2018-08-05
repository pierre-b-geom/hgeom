package hgeom.hmesh.core;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import hgeom.hmesh.data.HIData;

/**
 *
 * @author Pierre B.
 * @param <E>
 */
final class HIDataImpl<E> extends HDataBaseImpl<E> implements HIData<E> {

	/**
	 *
	 */
	private int[] values = new int[100];

	/**
	 * @param toIndex
	 * @param elementStreamSupplier
	 */
	public HIDataImpl(ToIntFunction<E> toIndex,
			Supplier<Stream<E>> elementStreamSupplier) {

		super(toIndex, elementStreamSupplier);
	}

	@Override
	public int get(E e) {
		int index = index(e);
		return index < values.length ? values[index] : 0;
	}

	@Override
	public void set(E e, int i) {
		int index = index(e);
		expandValues(index + 1);
		values[index] = i;
	}

	@Override
	public void clear() {
		Arrays.fill(values, 0);
	}

	@Override
	public void setAll(ToIntFunction<E> generator) {
		elementStream().forEach(v -> set(v, generator.applyAsInt(v)));
	}

	@Override
	public IntStream stream() {
		return elementStream().mapToInt(this::get);
	}

	@Override
	public BinaryOperator<E> minOperator() {
		return BinaryOperator.minBy(Comparator.comparingInt(this::get));
	}

	@Override
	public BinaryOperator<E> maxOperator() {
		return BinaryOperator.maxBy(Comparator.comparingInt(this::get));
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
