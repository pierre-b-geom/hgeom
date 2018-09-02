package hgeom.hmesh.data;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A container storing a collection of pairs of {@code (key, value)} in which
 * the value is a boolean
 *
 * @author Pierre B.
 * @param <E> type of the key in the container
 */
public interface HBData<E> {

	/**
	 * Returns the value associated with the specified key in a
	 * {@code (key, value)} pair stored in this container
	 *
	 * @param e the key whose associated value is seeked
	 * @return the boolean value associated with the specified key
	 */
	boolean get(E e);

	/**
	 * Stores the specified key and value into a {@code (key, value)} pair in
	 * this container
	 *
	 * @param e the key
	 * @param v the value
	 */
	void set(E e, boolean v);

	/**
	 * Sets to {@code false} the value in all the {@code (key, value)} pairs
	 * stored in this container
	 */
	void clear();

	/**
	 * Uses the specified function to set the value according to the key in each
	 * {@code (key, value)} pairs stored in this container
	 *
	 * @param generator a function returning a boolean according to a key
	 */
	void setAll(Predicate<E> generator);

	/**
	 * @return a stream on the values of all the {@code (key, value)} pairs
	 *         stored in this container
	 */
	Stream<Boolean> stream();
}
