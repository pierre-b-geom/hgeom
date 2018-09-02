package hgeom.hmesh.util;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Classe utilitaire pour iterer sur des boucles d'elements
 *
 * @author Pierre B.
 */
public final class Loops {

	/**
	 *
	 */
	public enum Direction {

	/**
	 *
	 */
	FORWARD {

		@Override
		public Direction reverse() {
			return BACKWARD;
		}
	},

	/**
	 *
	 */
	BACKWARD {

		@Override
		public Direction reverse() {
			return FORWARD;
		}
	};

		/**
		 * @return
		 */
		public abstract Direction reverse();
	}

	/**
	 *
	 */
	@FunctionalInterface
	public interface IntBiConsumer {

		/**
		 * @param first
		 * @param second
		 */
		void accept(int first, int second);
	}

	/**
	 *
	 */
	@FunctionalInterface
	public interface IntBiPredicate {

		/**
		 * @param first
		 * @param second
		 * @return
		 */
		boolean test(int first, int second);
	}

	/**
	 *
	 */
	private Loops() {
	}

	/**
	 * @param loop
	 * @param index
	 * @return
	 */
	public static int get(int[] loop, int index) {
		return loop[clamp(index, loop.length)];
	}

	/**
	 * @param loop
	 * @param index
	 * @return
	 */
	public static <T> T get(T[] loop, int index) {
		return get(i -> loop[i], loop.length, index);
	}

	/**
	 * @param loop
	 * @param index
	 * @return
	 */
	public static <T> T get(List<T> loop, int index) {
		return get(loop::get, loop.size(), index);
	}

	/**
	 * @param loop
	 * @param loopLength
	 * @param index
	 * @return
	 */
	public static <T> T get(IntFunction<T> loop, int loopLength, int index) {
		return loop.apply(clamp(index, loopLength));
	}

	/**
	 * @param first
	 * @param next
	 * @return
	 */
	public static <T> int size(T first, UnaryOperator<T> next) {
		T t = first;
		int size = 0;

		do {
			size++;
			t = next.apply(t);
		} while (t != first);

		return size;
	}

	/**
	 * @param first
	 * @param next
	 * @param consumer
	 */
	public static <T> void forEach(T first, UnaryOperator<T> next,
			Consumer<? super T> consumer) {

		T t = first;

		do {
			consumer.accept(t);
			t = next.apply(t);
		} while (t != first);
	}

	/**
	 * @param first
	 * @param next
	 * @param predicate
	 * @return
	 */
	public static <T> boolean anyMatch(T first, UnaryOperator<T> next,
			Predicate<? super T> predicate) {

		T t = first;

		do {
			if (predicate.test(t)) {
				return true;
			}

			t = next.apply(t);
		} while (t != first);

		return false;
	}

	/**
	 * @param first
	 * @param next
	 * @param predicate
	 * @return
	 */
	public static <T> boolean allMatch(T first, UnaryOperator<T> next,
			Predicate<? super T> predicate) {

		T t = first;

		do {
			if (!predicate.test(t)) {
				return false;
			}

			t = next.apply(t);
		} while (t != first);

		return true;
	}

	/**
	 * @param first
	 * @param next
	 * @param filter
	 * @return
	 */
	public static <T> Optional<T> findFirst(T first, UnaryOperator<T> next,
			Predicate<? super T> filter) {

		T t = first;

		do {
			if (filter == null || filter.test(t)) {
				return Optional.of(t);
			}

			t = next.apply(t);
		} while (t != first);

		return Optional.empty();
	}

	/**
	 * @param first
	 * @param next
	 * @param supplier
	 * @param accumulator
	 * @return
	 */
	public static <T, U> U collect(T first, UnaryOperator<T> next,
			Supplier<U> supplier, BiConsumer<U, ? super T> accumulator) {

		U result = supplier.get();
		T t = first;

		do {
			accumulator.accept(result, t);
			t = next.apply(t);
		} while (t != first);

		return result;
	}

	/**
	 * @param loop
	 * @param c
	 */
	public static void forEachPair(int[] loop, IntBiConsumer c) {
		forEachPair(loop, c, Direction.FORWARD, 0, loop.length);
	}

	/**
	 * @param loop
	 * @param c
	 * @param direction
	 */
	public static void forEachPair(int[] loop, IntBiConsumer c,
			Direction direction) {

		forEachPair(loop, c, direction, 0, loop.length);
	}

	/**
	 * @param loop
	 * @param c
	 * @param direction
	 * @param fromIndex
	 * @param toIndex
	 */
	public static void forEachPair(int[] loop, IntBiConsumer c,
			Direction direction, int fromIndex, int toIndex) {

		int loopLength = toIndex - fromIndex;

		if (loopLength >= 2) {
			if (direction == Direction.FORWARD) {
				int first = loop[fromIndex];

				for (int i = fromIndex; i < toIndex; i++) {
					int second = loop[next(i, fromIndex, toIndex)];
					c.accept(first, second);
					first = second;
				}
			}

			else {
				int first = loop[toIndex - 1];

				for (int i = toIndex - 1; i >= fromIndex; i--) {
					int second = loop[previous(i, fromIndex, toIndex)];
					c.accept(first, second);
					first = second;
				}
			}
		}
	}

	/**
	 * @param loop
	 * @param c
	 */
	public static <T> void forEachPair(T[] loop, BiConsumer<T, T> c) {
		forEachPair(loop, c, Direction.FORWARD);
	}

	/**
	 * @param loop
	 * @param c
	 * @param direction
	 */
	public static <T> void forEachPair(T[] loop, BiConsumer<T, T> c,
			Direction direction) {

		forEachPair(loop, c, direction, 0, loop.length);
	}

	/**
	 * @param loop
	 * @param c
	 * @param direction
	 * @param fromIndex
	 * @param toIndex
	 */
	public static <T> void forEachPair(T[] loop, BiConsumer<T, T> c,
			Direction direction, int fromIndex, int toIndex) {

		forEachPair(i -> loop[i], c, direction, fromIndex, toIndex);
	}

	/**
	 * @param loop
	 * @param c
	 */
	public static <T> void forEachPair(List<T> loop, BiConsumer<T, T> c) {
		forEachPair(loop, c, Direction.FORWARD);
	}

	/**
	 * @param loop
	 * @param c
	 * @param direction
	 */
	public static <T> void forEachPair(List<T> loop, BiConsumer<T, T> c,
			Direction direction) {

		forEachPair(loop, c, direction, 0, loop.size());
	}

	/**
	 * @param loop
	 * @param c
	 * @param direction
	 * @param fromIndex
	 * @param toIndex
	 */
	public static <T> void forEachPair(List<T> loop, BiConsumer<T, T> c,
			Direction direction, int fromIndex, int toIndex) {

		forEachPair(loop::get, c, direction, fromIndex, toIndex);
	}

	/**
	 * @param loop
	 * @param c
	 * @param direction
	 * @param fromIndex
	 * @param toIndex
	 */
	public static <T> void forEachPair(IntFunction<T> loop, BiConsumer<T, T> c,
			Direction direction, int fromIndex, int toIndex) {

		int loopLength = toIndex - fromIndex;

		if (loopLength >= 2) {
			if (direction == Direction.FORWARD) {
				T first = loop.apply(fromIndex);

				for (int i = fromIndex; i < toIndex; i++) {
					T second = loop.apply(next(i, fromIndex, toIndex));
					c.accept(first, second);
					first = second;
				}
			}

			else {
				T first = loop.apply(toIndex - 1);

				for (int i = toIndex - 1; i >= fromIndex; i--) {
					T second = loop.apply(previous(i, fromIndex, toIndex));
					c.accept(first, second);
					first = second;
				}
			}
		}
	}

	/**
	 * @param loop
	 * @param p
	 * @return
	 */
	public static boolean takePairWhile(int[] loop, IntBiPredicate p) {
		return takePairWhile(loop, p, Direction.FORWARD, 0, loop.length);
	}

	/**
	 * @param loop
	 * @param p
	 * @param direction
	 * @return
	 */
	public static boolean takePairWhile(int[] loop, IntBiPredicate p,
			Direction direction) {

		return takePairWhile(loop, p, direction, 0, loop.length);
	}

	/**
	 * @param loop
	 * @param p
	 * @param direction
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	public static boolean takePairWhile(int[] loop, IntBiPredicate p,
			Direction direction, int fromIndex, int toIndex) {

		int loopLength = toIndex - fromIndex;

		if (loopLength >= 2) {
			if (direction == Direction.FORWARD) {
				return takePairWhileForward(loop, p, fromIndex, toIndex);
			}

			takePairWhileBackward(loop, p, fromIndex, toIndex);
		}

		return true;
	}

	/**
	 * @param loop
	 * @param p
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	private static boolean takePairWhileForward(int[] loop, IntBiPredicate p,
			int fromIndex, int toIndex) {

		int first = loop[fromIndex];

		for (int i = fromIndex; i < toIndex; i++) {
			int second = loop[next(i, fromIndex, toIndex)];

			if (!p.test(first, second)) {
				return false;
			}

			first = second;
		}

		return true;
	}

	/**
	 * @param loop
	 * @param p
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	private static boolean takePairWhileBackward(int[] loop, IntBiPredicate p,
			int fromIndex, int toIndex) {

		int first = loop[toIndex - 1];

		for (int i = toIndex - 1; i >= fromIndex; i--) {
			int second = loop[previous(i, fromIndex, toIndex)];

			if (!p.test(first, second)) {
				return false;
			}

			first = second;
		}

		return true;
	}

	/**
	 * @param loop
	 * @param p
	 * @return
	 */
	public static <T> boolean takePairWhile(T[] loop, BiPredicate<T, T> p) {
		return takePairWhile(loop, p, Direction.FORWARD, 0, loop.length);
	}

	/**
	 * @param loop
	 * @param p
	 * @param direction
	 * @return
	 */
	public static <T> boolean takePairWhile(T[] loop, BiPredicate<T, T> p,
			Direction direction) {

		return takePairWhile(i -> loop[i], p, direction, 0, loop.length);
	}

	/**
	 * @param loop
	 * @param p
	 * @param direction
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	public static <T> boolean takePairWhile(T[] loop, BiPredicate<T, T> p,
			Direction direction, int fromIndex, int toIndex) {

		return takePairWhile(i -> loop[i], p, direction, fromIndex, toIndex);
	}

	/**
	 * @param loop
	 * @param p
	 * @return
	 */
	public static <T> boolean takePairWhile(List<T> loop, BiPredicate<T, T> p) {
		return takePairWhile(loop::get, p, Direction.FORWARD, 0, loop.size());
	}

	/**
	 * @param loop
	 * @param p
	 * @param direction
	 * @return
	 */
	public static <T> boolean takePairWhile(List<T> loop, BiPredicate<T, T> p,
			Direction direction) {

		return takePairWhile(loop::get, p, direction, 0, loop.size());
	}

	/**
	 * @param loop
	 * @param p
	 * @param direction
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	public static <T> boolean takePairWhile(List<T> loop, BiPredicate<T, T> p,
			Direction direction, int fromIndex, int toIndex) {

		return takePairWhile(loop::get, p, direction, fromIndex, toIndex);
	}

	/**
	 * @param loop
	 * @param p
	 * @param direction
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	public static <T> boolean takePairWhile(IntFunction<T> loop,
			BiPredicate<T, T> p, Direction direction, int fromIndex,
			int toIndex) {

		int loopLength = toIndex - fromIndex;

		if (loopLength >= 2) {
			if (direction == Direction.FORWARD) {
				return takePairWhileForward(loop, p, fromIndex, toIndex);
			}

			return takePairWhileBackward(loop, p, fromIndex, toIndex);
		}

		return true;
	}

	/**
	 * @param loop
	 * @param p
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	private static <T> boolean takePairWhileForward(IntFunction<T> loop,
			BiPredicate<T, T> p, int fromIndex, int toIndex) {

		T first = loop.apply(fromIndex);

		for (int i = fromIndex; i < toIndex; i++) {
			T second = loop.apply(next(i, fromIndex, toIndex));

			if (!p.test(first, second)) {
				return false;
			}

			first = second;
		}

		return true;
	}

	/**
	 * @param loop
	 * @param p
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	private static <T> boolean takePairWhileBackward(IntFunction<T> loop,
			BiPredicate<T, T> p, int fromIndex, int toIndex) {

		T first = loop.apply(toIndex - 1);

		for (int i = toIndex - 1; i >= fromIndex; i--) {
			T second = loop.apply(previous(i, fromIndex, toIndex));

			if (!p.test(first, second)) {
				return false;
			}

			first = second;
		}

		return true;
	}

	/**
	 * @param index
	 * @param size
	 * @return
	 */
	private static int clamp(int index, int size) {
		int clampedIndex = index % size;
		return clampedIndex >= 0 ? clampedIndex : clampedIndex + size;
	}

	/**
	 * @param index
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	private static int next(int index, int fromIndex, int toIndex) {
		return index == toIndex - 1 ? fromIndex : index + 1;
	}

	/**
	 * @param index
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	private static int previous(int index, int fromIndex, int toIndex) {
		return index == fromIndex ? toIndex - 1 : index - 1;
	}
}
