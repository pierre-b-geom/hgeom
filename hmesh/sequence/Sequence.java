package hgeom.hmesh.sequence;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import hgeom.hmesh.elements.HMesh;

/**
 * Sequence of elements used when browsing a {@link HMesh}. Light version of the
 * java {@link Stream}. Has roughly the same operations than {@link Stream} but
 * does not allow flat mapping and parallelism.
 * <p>
 * A sequence has several advantages over a stream: 1/ faster build and lighter
 * memory usage (sequences may be created intensively when browsing a
 * {@link HMesh}) 2/ extension of {@link Iterable} 3/ reusability after a
 * terminal operation
 *
 * @author Pierre B.
 * @param <T>
 */
public interface Sequence<T> extends Iterable<T> {

	/**
	 * @return an empty sequence
	 */
	static <T> Sequence<T> empty() {
		@SuppressWarnings("unchecked")
		Sequence<T> emptySequence = EmptySequence.EMPTY_SEQUENCE;
		return emptySequence;
	}

	/**
	 * @return number of elements contained in the sequence
	 * @see Stream#count()
	 */
	int count();

	/**
	 * builds a new sequence containing the elements of this sequence that match
	 * the specified predicate
	 *
	 * @param predicate
	 * @return the new sequence
	 * @see Stream#filter(Predicate)
	 */
	Sequence<T> filter(Predicate<? super T> predicate);

	/**
	 * builds a new sequence containing the results of the mapping of the
	 * element of this sequence by the specified mapper
	 *
	 * @param mapper
	 * @return the new sequence
	 * @see Stream#map(Function)
	 */
	<U> Sequence<U> map(Function<? super T, ? extends U> mapper);

	/**
	 * Iterates on the elements of this sequence until a element is found
	 * matching the given predicate
	 *
	 * @param predicate the predicate to be matched
	 * @return {@code true} if a element of the sequence was found matching the
	 *         predicate ; {@code false} otherwise
	 * @see Stream#anyMatch(Predicate)
	 */
	boolean anyMatch(Predicate<? super T> predicate);

	/**
	 * Checks if all elements of this sequence match the specified predicate
	 *
	 * @param predicate
	 * @return {@code true} if all elements match the specified predicate;
	 *         {@code false} otherwise
	 * @see Stream#allMatch(Predicate)
	 */
	boolean allMatch(Predicate<? super T> predicate);

	/**
	 * Gets the first element of this sequence
	 *
	 * @return an {@link Optional} on the first element if it exists;
	 *         {@link Optional#empty()} otherwise
	 * @see Stream#findFirst()
	 */
	Optional<T> findFirst();

	/**
	 * Reduce the elements of this sequence with the given operator
	 *
	 * @param accumulator reduction operator
	 * @return an {@link Optional} on the reduction result if it exists;
	 *         {@link Optional#empty()} otherwise
	 * @see Stream#reduce(BinaryOperator)
	 */
	Optional<T> reduce(BinaryOperator<T> accumulator);

	/**
	 * Reduces the elements of this sequence with the given operator
	 *
	 * @param identity    the initial value of the reduction
	 * @param accumulator reduction operator
	 * @return the reduction result
	 * @see Stream#reduce(Object, BinaryOperator)
	 */
	<U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator);

	/**
	 * Collects the elements of this sequence into a array
	 *
	 * @param generator a function for generating an array with a given size
	 * @return the array containing the elements of this sequence
	 * @see Stream#toArray(IntFunction)
	 */
	<A> A[] toArray(IntFunction<A[]> generator);

	/**
	 * Collects the elements of this sequence into a list
	 *
	 * @return the list containing the elements of this sequence
	 */
	default List<T> toList() {
		return toList(null);
	}

	/**
	 * Collects the elements of this sequence into a list
	 *
	 * @param list if not {@code null}, the list that will contain the elements
	 * @return the list containing the elements of this sequence
	 */
	List<T> toList(List<T> list);

	/**
	 * Collect the elements of this sequence into a container
	 *
	 * @param container   the container
	 * @param accumulator operator for adding element of this sequence into the
	 *                    container
	 * @return the container
	 * @see Stream#collect(Supplier, BiConsumer, BiConsumer)
	 */
	<U> U collect(U container, BiConsumer<U, ? super T> accumulator);
}
