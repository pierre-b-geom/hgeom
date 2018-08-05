package hgeom.hmesh.core;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HElement;
import hgeom.hmesh.elements.HFace;
import hgeom.hmesh.elements.HMesh;
import hgeom.hmesh.elements.HVertex;
import hgeom.hmesh.sequence.Sequence;

/**
 * A walker able to walk inside a {@link HMesh half-edge data structure}
 *
 * @author Pierre B.
 */
public final class HMeshWalker {

	/**
	 * Monitor for a {@link HMesh half-edge data structure} walker
	 *
	 * @param <E> type of elements being visited by the walker
	 */
	public interface Monitor<E> {

		/**
		 * @return number of visited elements by the walker
		 */
		int numVisits();

		/**
		 * @param e a element of the {@link HMesh half-edge data structure}
		 *          being visited by the walker
		 * @return {@code true} if the given element has already been visited by
		 *         the walker; {@code false} otherwise
		 */
		boolean isVisited(E e);
	}

	/**
	 *
	 */
	private final HMesh mesh;

	/**
	 * Constructs a walker intended to operate on the specified mesh
	 *
	 * @param mesh
	 */
	public HMeshWalker(HMesh mesh) {
		this.mesh = Objects.requireNonNull(mesh);
	}

	/**
	 * Starts from the specified {@link HVertex vertex} and walks through the
	 * mesh, vertex by vertex, using the specified operator to select the next
	 * vertex. Returns the last element of the path taken by the walker
	 * <p>
	 * To move from a given vertex <b>v</b> to another, the walker applies the
	 * specified reduction operator to <b>v</b> and to all its
	 * {@link HVertex#neighbors() neighbors} that have not yet been visited. If
	 * one neighbor is the result of the reduction, then the walker advances to
	 * it ; otherwise, if the vertex <b>v</b> itself is the result, then the
	 * walker ends ts walk. Example:
	 *
	 * <pre>
	 * <code>
	 * {@code
	 * HMesh mesh = ...
	 * HVertex start = ...
	 *
	 * // Container of double values associated with the mesh's vertices
	 * HDData<HVertex> vertexValues = ...
	 *
	 * // Get a reduction operator selecting the vertex associated
	 * // with the minimal value
	 * BinaryOperator<HVertex> minOperator = vertexValues.minOperator();
	 *
	 * // Starts at the starting vertex and walks up to a vertex
	 * // associated with a local minimal value
	 * HVertex vertexWithMinValue =
	 *     new HMeshWalker(mesh).find(start, minOperator);
	 * }
	 * </code>
	 * </pre>
	 *
	 * @param start    starting vertex
	 * @param selector reduction operator used to select a candidate among a
	 *                 vertex and its neighbors
	 * @return the last element of the path taken by the walker
	 * @see HVertex#neighbors()
	 */
	public HVertex find(HVertex start, BinaryOperator<HVertex> selector) {
		return find(start, HVertex::neighbors, selector);
	}

	/**
	 * Starts from the specified {@link HEdge edge} and walks through the mesh,
	 * edge by edge, using the specified operator to select the next edge.
	 * Returns the last element of the path taken by the walker
	 * <p>
	 * To move from a given edge <b>e</b> to another, the walker applies the
	 * specified reduction operator to <b>e</b> and to all the
	 * {@link HEdge#outgoingEdges() edges going out} of <b>e</b> that have not
	 * yet been visited. If one outgoing edge is the result of the reduction,
	 * then the walker advances to it ; otherwise, if the edge <b>e</b> itself
	 * is the result, then the walker ends ts walk
	 *
	 * @param start    starting edge
	 * @param selector reduction operator used to select a candidate among edges
	 * @return the last element of the path taken by the walker
	 * @see HEdge#outgoingEdges()
	 */
	public HEdge find(HEdge start, BinaryOperator<HEdge> selector) {
		return find(start, HEdge::outgoingEdges, selector);
	}

	/**
	 * Starts from the specified {@link HFace face} and walks through the mesh,
	 * face by face, using the specified operator to select the next face.
	 * Returns the last element of the path taken by the walker
	 * <p>
	 * To move from a given face <b>f</b> to another, the walker applies the
	 * specified reduction operator to <b>f</b> and to all its
	 * {@link HFace#neighbors() neighbors} that have not yet been visited. If
	 * one neighbor is the result of the reduction, then the walker advances to
	 * it ; otherwise, if the face <b>f</b> itself is the result, then the
	 * walker ends ts walk
	 *
	 * @param start    starting face
	 * @param selector reduction operator used to select a candidate among a
	 *                 face and its neighbors
	 * @return the last element of the path taken by the walker
	 * @see HFace#neighbors()
	 */
	public HFace find(HFace start, BinaryOperator<HFace> selector) {
		return find(start, HFace::neighbors, selector);
	}

	/**
	 * Starts from the specified {@link HElement element} and walks through the
	 * mesh, element by element, using the specified binary operator to select
	 * the next element. Returns the last element of the path followed by the
	 * walker
	 * <p>
	 * To move from a given element <b>e</b> to another, the walker first
	 * applies the specified function on <b>e</b> to obtain a sequence of
	 * elements. The walker then applies the specified reduction operator to
	 * <b>e</b> and to the elements of the sequence that have not yet been
	 * visited. If one element of the sequence is the result of the reduction,
	 * then the walker advances to it ; otherwise, if the element <b>e</b>
	 * itself is the result, then the walker ends ts walk
	 *
	 * @param start    starting element
	 * @param nexts    a function returning a sequence of elements when applied
	 *                 on a given element
	 * @param selector reduction operator used to select a candidate among
	 *                 elements
	 * @return the last element of the path followed by the walker
	 */
	public <E extends HElement> E find(E start, Function<E, Sequence<E>> nexts,
			BinaryOperator<E> selector) {

		return walk(start, nexts, selector).reduce(start, (e1, e2) -> e2);
	}

	/**
	 * Starts from the specified {@link HVertex vertex} and walks through the
	 * mesh, vertex by vertex, using the specified operator to select the next
	 * vertex.
	 * <p>
	 * To move from a given vertex <b>v</b> to another, the walker applies the
	 * specified reduction operator to <b>v</b> and to all its
	 * {@link HVertex#neighbors() neighbors} that have not yet been visited. If
	 * one neighbor is the result of the reduction, then the walker advances to
	 * it ; otherwise, if the vertex <b>v</b> itself is the result, then the
	 * walker ends ts walk
	 *
	 * @param start    starting vertex
	 * @param selector reduction operator used to select a candidate among a
	 *                 vertex and its neighbors
	 * @return a stream on the elements of the path from the starting vertex to
	 *         the final vertex
	 * @see HVertex#neighbors()
	 */
	public Stream<HVertex> walk(HVertex start,
			BinaryOperator<HVertex> selector) {

		return walk(start, HVertex::neighbors, selector);
	}

	/**
	 * Starts from the specified {@link HEdge edge} and walks through the mesh,
	 * edge by edge, using the specified operator to select the next edge.
	 * <p>
	 * To move from a given edge <b>e</b> to another, the walker applies the
	 * specified reduction operator to <b>e</b> and to all the
	 * {@link HEdge#outgoingEdges() edges going out} of <b>e</b> that have not
	 * yet been visited. If one outgoing edge is the result of the reduction,
	 * then the walker advances to it ; otherwise, if the edge <b>e</b> itself
	 * is the result, then the walker ends ts walk
	 *
	 * @param start    starting edge
	 * @param selector reduction operator used to select a candidate among edges
	 * @return a stream on the elements of the path from the starting edge to
	 *         the final edge
	 * @see HEdge#outgoingEdges()
	 */
	public Stream<HEdge> walk(HEdge start, BinaryOperator<HEdge> selector) {
		return walk(start, HEdge::outgoingEdges, selector);
	}

	/**
	 * Starts from the specified {@link HFace face} and walks through the mesh,
	 * face by face, using the specified operator to select the next face.
	 * <p>
	 * To move from a given face <b>f</b> to another, the walker applies the
	 * specified reduction operator to <b>f</b> and to all its
	 * {@link HFace#neighbors() neighbors} that have not yet been visited. If
	 * one neighbor is the result of the reduction, then the walker advances to
	 * it ; otherwise, if the face <b>f</b> itself is the result, then the
	 * walker ends ts walk
	 *
	 * @param start    starting face
	 * @param selector reduction operator used to select a candidate among a
	 *                 face and its neighbors
	 * @return a stream on the elements of the path from the starting face to
	 *         the final face
	 * @see HFace#neighbors()
	 */
	public Stream<HFace> walk(HFace start, BinaryOperator<HFace> selector) {
		return walk(start, HFace::neighbors, selector);
	}

	/**
	 * Starts from the specified {@link HElement element} and walks through the
	 * mesh, element by element, using the specified operator to select the next
	 * element.
	 * <p>
	 * To move from a given element <b>e</b> to another, the walker first
	 * applies the specified function on <b>e</b> to obtain a sequence of
	 * elements. The walker then applies the specified reduction operator to
	 * <b>e</b> and to the elements of the sequence that have not yet been
	 * visited. If one element of the sequence is the result of the reduction,
	 * then the walker advances to it ; otherwise, if the element <b>ef</b>
	 * itself is the result, then the walker ends ts walk
	 *
	 * @param start    starting element
	 * @param nexts    a function returning a sequence of elements when applied
	 *                 on a given element
	 * @param selector reduction operator used to select a candidate among
	 *                 elements
	 * @return a stream on the elements of the path from the starting element to
	 *         the final element
	 */
	public <E extends HElement> Stream<E> walk(E start,
			Function<E, Sequence<E>> nexts, BinaryOperator<E> selector) {

		BiFunction<E, Monitor<E>, E> next = (e, monitor) -> {
			E reduced = nexts.apply(e).filter(e2 -> !monitor.isVisited(e2))
					.reduce(e, selector);

			// retourne null si le resultat de la reduction est egal a l'element
			// d'origine
			return reduced == e ? null : reduced;
		};

		return walk(start, next);
	}

	/**
	 * Starts from the specified {@link HElement element} and walks through the
	 * mesh, element by element, using the specified function to select the next
	 * element. The walk ends when the function returns {@code null}
	 *
	 * @param start starting element
	 * @param next  navigation operator providing the walker the way to move
	 *              from one element to another. The navigation operator takes a
	 *              input element and should return either the next element for
	 *              the walker or {@code null} to indicate that the input
	 *              element is the end of the path
	 *              <p>
	 *              The monitor provided to the navigation operator can help to
	 *              choose the element to return
	 * @return a stream on the elements of the path
	 * @throws NullPointerException if the specified navigation operator is
	 *                              {@code null}
	 */
	public <E extends HElement> Stream<E> walk(E start,
			BiFunction<E, Monitor<E>, E> next) {

		Iterator<E> iterator = new HMeshPathIterator<>(mesh, start, next);
		return unknownSizeStream(iterator).filter(Objects::nonNull);
	}

	/**
	 * @param iterator
	 * @return
	 */
	private static <E> Stream<E> unknownSizeStream(Iterator<E> iterator) {
		return StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(iterator,
						Spliterator.IMMUTABLE | Spliterator.NONNULL
								| Spliterator.DISTINCT | Spliterator.ORDERED),
				false);
	}
}
