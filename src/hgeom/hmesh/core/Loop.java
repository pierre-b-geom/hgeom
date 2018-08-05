package hgeom.hmesh.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import hgeom.hmesh.sequence.Sequence;

/**
 *
 * @author Pierre B.
 * @param <I> type of loop's element
 * @param <O> type of result
 */
final class Loop<I, O> implements Sequence<O> {

	/**
	 *
	 */
	final class LoopIterator implements Iterator<O> {

		/**
		 *
		 */
		private I first;

		/**
		 *
		 */
		private I in;

		/**
		 *
		 */
		private O out;

		/**
		 *
		 */
		private boolean nextNeeded = true;

		/**
		 *
		 */
		private boolean finished;

		@Override
		public boolean hasNext() {
			if (nextNeeded) {
				advanceToNext();
				nextNeeded = false;
			}

			return !finished;
		}

		@Override
		public O next() {
			if (nextNeeded) {
				advanceToNext();
			}

			else {
				nextNeeded = true;
			}

			if (finished) {
				throw new NoSuchElementException();
			}

			return out;
		}

		/**
		 *
		 */
		private void advanceToNext() {
			out = null;

			while (!finished && out == null) {
				if (in == null) {
					in = first = firstIn.get();
					out = inToOut.apply(in);
				}

				else {
					in = nextIn.apply(in);

					if (in == first) {
						finished = true;
					}

					else {
						out = inToOut.apply(in);
					}
				}
			}
		}
	}

	/**
	 *
	 */
	final Supplier<I> firstIn;

	/**
	 *
	 */
	final UnaryOperator<I> nextIn;

	/**
	 * Retourne null si element filtre. Inverse de l'implementation de Stream
	 * (out->in) car pas de flatMap !
	 */
	final Function<? super I, ? extends O> inToOut;

	/**
	 * @param origin
	 * @param originToFirst
	 * @param next
	 * @return
	 */
	public static <F, I> Loop<I, I> createLoop(F origin,
			Function<F, I> originToFirst, UnaryOperator<I> next) {

		Supplier<I> first = () -> originToFirst.apply(origin);
		return new Loop<>(first, next, Function.identity());
	}

	/**
	 * @param firstIn
	 * @param nextIn
	 * @param inToOut
	 */
	private Loop(Supplier<I> firstIn, UnaryOperator<I> nextIn,
			Function<? super I, ? extends O> inToOut) {

		this.firstIn = Objects.requireNonNull(firstIn);
		this.nextIn = Objects.requireNonNull(nextIn);
		this.inToOut = Objects.requireNonNull(inToOut);
	}

	@Override
	public Iterator<O> iterator() {
		return new LoopIterator();
	}

	@Override
	public Loop<I, O> filter(Predicate<? super O> predicate) {
		Objects.requireNonNull(predicate);

		Function<? super I, ? extends O> newInToOut = inToOut.andThen(
				out -> out == null || !predicate.test(out) ? null : out);

		return new Loop<>(firstIn, nextIn, newInToOut);
	}

	@Override
	public <U> Loop<I, U> map(Function<? super O, ? extends U> mapper) {
		Objects.requireNonNull(mapper);

		Function<? super I, ? extends U> newInToOut = inToOut
				.andThen(out -> out == null ? null : mapper.apply(out));

		return new Loop<>(firstIn, nextIn, newInToOut);
	}

	@Override
	public int count() {
		I first = firstIn.get();
		I in = first;
		int count = 0;

		do {
			if (inToOut.apply(in) != null) {
				count++;
			}

			in = nextIn.apply(in);
		} while (in != first);

		return count;
	}

	@Override
	public void forEach(Consumer<? super O> consumer) {
		I first = firstIn.get();
		I in = first;

		do {
			O out = inToOut.apply(in);

			if (out != null) {
				consumer.accept(out);
			}

			in = nextIn.apply(in);
		} while (in != first);
	}

	@Override
	public boolean anyMatch(Predicate<? super O> predicate) {
		I first = firstIn.get();
		I in = first;

		do {
			O out = inToOut.apply(in);

			if (out != null && predicate.test(out)) {
				return true;
			}

			in = nextIn.apply(in);
		} while (in != first);

		return false;
	}

	@Override
	public boolean allMatch(Predicate<? super O> predicate) {
		I first = firstIn.get();
		I in = first;

		do {
			O out = inToOut.apply(in);

			if (out != null && !predicate.test(out)) {
				return false;
			}

			in = nextIn.apply(in);
		} while (in != first);

		return true;
	}

	@Override
	public Optional<O> findFirst() {
		I first = firstIn.get();
		I in = first;

		do {
			O out = inToOut.apply(in);

			if (out != null) {
				return Optional.of(out);
			}

			in = nextIn.apply(in);
		} while (in != first);

		return Optional.empty();
	}

	@Override
	public Optional<O> reduce(BinaryOperator<O> accumulator) {
		I first = firstIn.get();
		I in = first;
		O result = null;
		boolean empty = true;

		do {
			O out = inToOut.apply(in);

			if (out != null) {
				if (empty) {
					result = out;
					empty = false;
				}

				else {
					result = accumulator.apply(result, out);
				}
			}

			in = nextIn.apply(in);
		} while (in != first);

		return empty ? Optional.empty() : Optional.of(result);
	}

	@Override
	public <V> V reduce(V identity, BiFunction<V, ? super O, V> accumulator) {
		I first = firstIn.get();
		I in = first;
		V result = identity;

		do {
			O out = inToOut.apply(in);

			if (out != null) {
				result = accumulator.apply(result, out);
			}

			in = nextIn.apply(in);
		} while (in != first);

		return result;
	}

	@Override
	public <A> A[] toArray(IntFunction<A[]> generator) {
		List<O> list = toList();
		return list.toArray(generator.apply(list.size()));
	}

	@Override
	public List<O> toList(List<O> list) {
		return collect(list == null ? new ArrayList<>() : list, List::add);
	}

	@Override
	public <U> U collect(U container, BiConsumer<U, ? super O> accumulator) {
		I first = firstIn.get();
		I in = first;

		do {
			O out = inToOut.apply(in);

			if (out != null) {
				accumulator.accept(container, out);
			}

			in = nextIn.apply(in);
		} while (in != first);

		return container;
	}
}
