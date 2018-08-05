package hgeom.hmesh.sequence;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;

/**
 *
 * @author Pierre B.
 * @param <T>
 */
final class EmptySequence<T> implements Sequence<T> {

	/**
	 *
	 */
	@SuppressWarnings("rawtypes")
	public static final EmptySequence EMPTY_SEQUENCE = new EmptySequence<>();

	/**
	 *
	 */
	private EmptySequence() {
		// Uniquement pour le singleton
	}

	@Override
	public Iterator<T> iterator() {
		return Collections.emptyIterator();
	}

	@Override
	public int count() {
		return 0;
	}

	@Override
	public void forEach(Consumer<? super T> consumer) {
		// Vide donc rien a faire
	}

	@Override
	public Sequence<T> filter(Predicate<? super T> predicate) {
		return this;
	}

	@Override
	public <U> Sequence<U> map(Function<? super T, ? extends U> mapper) {
		@SuppressWarnings("unchecked")
		Sequence<U> that = (Sequence<U>) this;
		return that;
	}

	@Override
	public boolean anyMatch(Predicate<? super T> predicate) {
		return false;
	}

	@Override
	public boolean allMatch(Predicate<? super T> predicate) {
		return true;
	}

	@Override
	public Optional<T> findFirst() {
		return Optional.empty();
	}

	@Override
	public Optional<T> reduce(BinaryOperator<T> accumulator) {
		return Optional.empty();
	}

	@Override
	public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator) {
		return identity;
	}

	@Override
	public <A> A[] toArray(IntFunction<A[]> generator) {
		return generator.apply(0);
	}

	@Override
	public List<T> toList(List<T> list) {
		return Collections.emptyList();
	}

	@Override
	public <U> U collect(U container, BiConsumer<U, ? super T> accumulator) {
		return container;
	}
}
