package hgeom.hmesh.data;

import java.util.function.BinaryOperator;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;

/**
 * A container storing a collection of pairs of {@code (key, value)} in which
 * the value is a integer
 *
 * @author Pierre B.
 * @param <E> type of the key in the container
 */
public interface HDData<E> {

	/**
	 * Returns the value associated with the specified key in a
	 * {@code (key, value)} pair stored in this container
	 *
	 * @param e the key whose associated value is seeked
	 * @return the double value associated with the specified key
	 */
	double get(E e);

	/**
	 * Stores the specified key and value into a {@code (key, value)} pair in
	 * this container
	 *
	 * @param e the key
	 * @param v the value
	 */
	void set(E e, double v);

	/**
	 * Sets to {@code null} the value in all the {@code (key, value)} pairs
	 * stored in this container
	 */
	void clear();

	/**
	 * Uses the specified function to set the value according to the key in each
	 * {@code (key, value)} pairs stored in this container
	 *
	 * @param generator
	 */
	void setAll(ToDoubleFunction<E> generator);

	/**
	 * @return a stream on the values of all the {@code (key, value)} pairs
	 *         stored in this container
	 */
	DoubleStream stream();

	/**
	 * Returns a {@link BinaryOperator} which returns the lesser of two keys
	 * according to the comparaison of their associated values in the
	 * {@code (key, value)} pairs stored in this container
	 *
	 * @return
	 */
	BinaryOperator<E> minOperator();

	/**
	 * Returns a {@link BinaryOperator} which returns the greater of two keys
	 * according to the comparaison of their associated values in the
	 * {@code (key, value)} pairs stored in this container
	 *
	 * @return
	 */
	BinaryOperator<E> maxOperator();
}
