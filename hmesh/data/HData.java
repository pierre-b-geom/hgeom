package hgeom.hmesh.data;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A container storing a collection of pairs of {@code (key, value)}
 *
 * @author Pierre B.
 * @param <E> type of the key in the container
 * @param <V> type of the value in the container
 */
public interface HData<E, V> {

	/**
	 * Returns the value associated with the specified key in a
	 * {@code (key, value)} pair stored in this container
	 *
	 * @param e the key whose associated value is seeked
	 * @return the value associated with the specified key
	 */
	V get(E e);

	/**
	 * Stores the specified key and value into a {@code (key, value)} pair in
	 * this container
	 *
	 * @param e the key
	 * @param v the value
	 */
	void set(E e, V v);

	/**
	 * Sets to {@code 0} the value in all the {@code (key, value)} pairs stored
	 * in this container
	 */
	void clear();

	/**
	 * Uses the specified function to set the value according to the key in each
	 * {@code (key, value)} pairs stored in this container
	 *
	 * @param generator
	 */
	void setAll(Function<E, V> generator);

	/**
	 * @return a stream on the values of all the {@code (key, value)} pairs
	 *         stored in this container
	 */
	Stream<V> stream();
}
