package hgeom.hmesh.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import hgeom.hmesh.core.HMeshWalker.Monitor;
import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HElement;
import hgeom.hmesh.elements.HFace;
import hgeom.hmesh.elements.HMesh;
import hgeom.hmesh.elements.HVertex;
import hgeom.hmesh.sequence.Sequence;

/**
 * A walker able to walk inside a {@link HMesh half-edge data structure} along a
 * tree-like path
 *
 * @author Pierre B.
 */
public final class HMeshTreeWalker {

	/**
	 * Monitor for a {@link HMesh half-edge data structure} tree walker
	 *
	 * @param <E> type of elements being visited by the tree walker
	 */
	public interface TreeMonitor<E> extends Monitor<E> {

		/**
		 * @return current depth from the tree root. a depth of 0 indicates the
		 *         root itself ; a depth of 1 indicates that a root's neighbor
		 *         is being visited ; etc
		 */
		int depth();
	}

	/**
	 * Type of path used by a {@link HMesh half-edge data structure} tree walker
	 */
	public enum TreePathType {

		/**
		 * Walk around the tree root in depth order, starting from the closest
		 * depths then broadening
		 */
		BREADTH_FIRST,

		/**
		 * Walk using a pre-order depth-first tree path
		 */
		DEPTH_FIRST
	}

	/**
	 * Tree node children supplier used by a half-edge mesh tree walker
	 *
	 * @param <E>
	 */
	@FunctionalInterface
	public interface TreeChildrenSupplier<E> {

		/**
		 * Function used by a tree walker to obtain the children of a tree node
		 * currently visited.
		 *
		 * @param parent   the tree parent node whose children are seeked
		 * @param monitor  a monitor that watch the walker's progress. It
		 *                 indicates which nodes have already been visited. May
		 *                 help the method to select the children
		 * @param children a collection in which the children should be put
		 *
		 * @see HMeshTreeWalker
		 */
		void get(E parent, TreeMonitor<E> monitor, Collection<E> children);
	}

	/**
	 *
	 */
	private final HMesh mesh;

	/**
	 *
	 */
	private final TreePathType pathType;

	/**
	 *
	 */
	private final int treeMaxDepth;

	/**
	 * Constructs a walker intended to operate on the specified mesh. The walker
	 * will walk the mesh through a breadth-first tree path
	 *
	 * @param mesh
	 */
	public HMeshTreeWalker(HMesh mesh) {
		this(mesh, TreePathType.BREADTH_FIRST, -1);
	}

	/**
	 * Constructs a walker intended to operate on the specified mesh and to walk
	 * along the specified type of tree-like path
	 *
	 * @param mesh
	 * @param pathType if {@link TreePathType#DEPTH_FIRST}, construct a walker
	 *                 that will walk the mesh along a depth-first pre-order
	 *                 tree path; if {@link TreePathType#BREADTH_FIRST},
	 *                 construct a walker that will walk the mesh along a
	 *                 breadth-first tree path
	 */
	public HMeshTreeWalker(HMesh mesh, TreePathType pathType) {
		this(mesh, pathType, -1);
	}

	/**
	 * Constructs a walker intended to operate on the specified mesh and to walk
	 * along the specified type of tree-like path. Specifies also the maximum
	 * tree depth the walker is allowed
	 *
	 * @param mesh
	 * @param pathType     if {@link TreePathType#DEPTH_FIRST}, construct a
	 *                     walker that will walk the along through a depth-first
	 *                     pre-order tree path; if
	 *                     {@link TreePathType#BREADTH_FIRST}, construct a
	 *                     walker that will walk the mesh along a breadth-first
	 *                     tree path
	 * @param treeMaxDepth exclusive max bound for the tree depth or {@code -1}
	 *                     if no bound should be given to the walker
	 */
	public HMeshTreeWalker(HMesh mesh, TreePathType pathType,
			int treeMaxDepth) {

		this.mesh = Objects.requireNonNull(mesh);
		this.pathType = Objects.requireNonNull(pathType);
		this.treeMaxDepth = treeMaxDepth;
	}

	/**
	 * Starts from the specified {@link HVertex vertex} and walks within the
	 * underlying {@link HMesh} along a tree-like path made up of vertices.
	 * Children of a {@link HVertex vertex} in the tree are its
	 * {@link HVertex#neighbors() neighbors} in the {@link HMesh}
	 *
	 * @param treeRoot the root of the tree
	 * @return a stream on all visited vertices
	 * @see HVertex#neighbors()
	 */
	public Stream<HVertex> walk(HVertex treeRoot) {
		return walk(treeRoot, HVertex::neighbors);
	}

	/**
	 * Starts from the specified {@link HEdge edge} and walks within the
	 * underlying {@link HMesh} along a tree-like path made up of edges.
	 * Children of a {@link HEdge edge} in the tree are the
	 * {@link HEdge#outgoingEdges() edges going out} of it in the {@link HMesh}
	 *
	 * @param treeRoot the root of the tree
	 * @return a stream on all visited edges
	 * @see HEdge#outgoingEdges()
	 */
	public Stream<HEdge> walk(HEdge treeRoot) {
		return walk(treeRoot, HEdge::outgoingEdges);
	}

	/**
	 * Starts from the specified {@link HFace face} and walks within the
	 * underlying {@link HMesh} along a tree-like path made up of faces.
	 * Children of a {@link HFace face} in the tree are its
	 * {@link HFace#neighbors() neighbors} in the {@link HMesh}
	 *
	 * @param treeRoot the root of the tree
	 * @return a stream on all visited faces
	 * @see HFace#neighbors()
	 */
	public Stream<HFace> walk(HFace treeRoot) {
		return walk(treeRoot, HFace::neighbors);
	}

	/**
	 * Starts from the specified element and walks through a tree whose nodes
	 * are elements of the underlying mesh
	 *
	 * @param treeRoot         the root of the tree
	 * @param childrenSupplier a operator for supplying the children of each
	 *                         node of the tree
	 * @return a stream on all visited element
	 * @throws NullPointerException if the root or the children operator is
	 *                              {@code null}
	 */
	public <E extends HElement> Stream<E> walk(E treeRoot,
			TreeChildrenSupplier<E> childrenSupplier) {

		return unknownSizeStream(HMeshTreeIterator.create(mesh, pathType,
				treeMaxDepth, treeRoot, childrenSupplier));
	}

	/**
	 * Starts from the specified element and walks through a tree whose nodes
	 * are elements of the underlying mesh
	 *
	 * @param treeRoot         the root of the tree
	 * @param childrenSupplier a operator for supplying the children of each
	 *                         node of the tree
	 * @return a stream on all visited element
	 * @throws NullPointerException if the root or the children operator is
	 *                              {@code null}
	 */
	public <E extends HElement> Stream<E> walk(E treeRoot,
			Function<E, Sequence<E>> childrenSupplier) {

		return unknownSizeStream(HMeshTreeIterator.create(mesh, pathType,
				treeMaxDepth, treeRoot, childrenSupplier));
	}

	/**
	 * Starts from the specified element and walks through a tree whose nodes
	 * are elements of the underlying mesh
	 *
	 * @param treeRoot         the root of the tree
	 * @param childrenSupplier a operator for supplying the children of each
	 *                         node of the tree
	 * @return a stream on all visited element
	 * @throws NullPointerException if the root or the children operator is
	 *                              {@code null}
	 */
	public <E extends HElement> Stream<E> walk(E treeRoot,
			BiFunction<E, TreeMonitor<E>, Sequence<E>> childrenSupplier) {

		return unknownSizeStream(HMeshTreeIterator.create(mesh, pathType,
				treeMaxDepth, treeRoot, childrenSupplier));
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
