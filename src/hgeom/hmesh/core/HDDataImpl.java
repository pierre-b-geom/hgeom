package hgeom.hmesh.core;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import hgeom.hmesh.data.HDData;

/**
 *
 * @author Pierre B.
 * @param <E>
 */
final class HDDataImpl<E> extends HDataBaseImpl<E> implements HDData<E> {

	/**
	 *
	 */
	private double[] values = new double[100];

	/**
	 * @param toIndex
	 * @param elementStreamSupplier
	 */
	public HDDataImpl(ToIntFunction<E> toIndex,
			Supplier<Stream<E>> elementStreamSupplier) {

		super(toIndex, elementStreamSupplier);
	}

	@Override
	public double get(E e) {
		int index = index(e);
		return index < values.length ? values[index] : 0;
	}

	@Override
	public void set(E e, double v) {
		int index = index(e);
		expandValues(index + 1);
		values[index] = v;
	}

	@Override
	public void clear() {
		Arrays.fill(values, 0.);
	}

	@Override
	public void setAll(ToDoubleFunction<E> generator) {
		elementStream().forEach(v -> set(v, generator.applyAsDouble(v)));
	}

	@Override
	public DoubleStream stream() {
		return elementStream().mapToDouble(this::get);
	}

	@Override
	public BinaryOperator<E> minOperator() {
		return BinaryOperator.minBy(Comparator.comparingDouble(this::get));
	}

	@Override
	public BinaryOperator<E> maxOperator() {
		return BinaryOperator.maxBy(Comparator.comparingDouble(this::get));
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
