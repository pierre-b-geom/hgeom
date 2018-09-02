package hgeom.hmesh.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import hgeom.hmesh.core.HMeshTreeWalker.TreeChildrenSupplier;
import hgeom.hmesh.core.HMeshTreeWalker.TreeMonitor;
import hgeom.hmesh.core.HMeshTreeWalker.TreePathType;
import hgeom.hmesh.elements.HElement;
import hgeom.hmesh.elements.HMesh;
import hgeom.hmesh.sequence.Sequence;

/**
 *
 * @author Pierre B.
 * @param <E>
 */
abstract class HMeshTreeIterator<E extends HElement> extends HMeshIterator<E>
		implements TreeMonitor<E> {

	/**
	 *
	 */
	private BiConsumer<E, Collection<E>> childrenSupplier;

	/**
	 *
	 */
	private int depth;

	/**
	 * @param mesh
	 * @param type
	 * @param maxDepth
	 * @param root
	 * @param childrenSupplier
	 * @return
	 */
	public static <E extends HElement> HMeshTreeIterator<E> create(HMesh mesh,
			TreePathType type, int maxDepth, E root,
			Function<E, Sequence<E>> childrenSupplier) {

		HMeshTreeIterator<E> iterator = create(mesh, root, type);

		iterator.childrenSupplier = (e, c) -> {
			if (maxDepth == -1 || iterator.depth < maxDepth) {
				childrenSupplier.apply(e)
						.forEach(child -> iterator.collectChild(child, c));
			}
		};

		return iterator;
	}

	/**
	 * @param mesh
	 * @param type
	 * @param maxDepth
	 * @param root
	 * @param childrenSupplier
	 * @return
	 */
	public static <E extends HElement> HMeshTreeIterator<E> create(HMesh mesh,
			TreePathType type, int maxDepth, E root,
			BiFunction<E, TreeMonitor<E>, Sequence<E>> childrenSupplier) {

		HMeshTreeIterator<E> iterator = create(mesh, root, type);

		iterator.childrenSupplier = (e, c) -> {
			if (maxDepth == -1 || iterator.depth < maxDepth) {
				childrenSupplier.apply(e, iterator)
						.forEach(child -> iterator.collectChild(child, c));
			}
		};

		return iterator;
	}

	/**
	 * @param mesh
	 * @param type
	 * @param maxDepth
	 * @param root
	 * @param childrenSupplier
	 * @return
	 */
	public static <E extends HElement> HMeshTreeIterator<E> create(HMesh mesh,
			TreePathType type, int maxDepth, E root,
			TreeChildrenSupplier<E> childrenSupplier) {

		HMeshTreeIterator<E> iterator = create(mesh, root, type);
		List<E> children = new ArrayList<>();

		iterator.childrenSupplier = (e, c) -> {
			if (maxDepth == -1 || iterator.depth < maxDepth) {
				children.clear();
				childrenSupplier.get(e, iterator, children);
				children.forEach(child -> iterator.collectChild(child, c));
			}
		};

		return iterator;
	}

	/**
	 * Suppression des enfants deja visites
	 *
	 * @param child
	 * @param children
	 */
	private void collectChild(E child, Collection<E> children) {
		if (!isVisited(child)) {
			children.add(child);
		}
	}

	/**
	 * @param mesh
	 * @param root
	 * @param type
	 * @return
	 */
	private static <E extends HElement> HMeshTreeIterator<E> create(HMesh mesh,
			E root, TreePathType type) {

		if (type == TreePathType.BREADTH_FIRST) {
			return new HMeshBreadthFirstTreeIterator<>(mesh, root);
		}

		if (type == TreePathType.DEPTH_FIRST) {
			return new HMeshDeepFirstTreeIterator<>(mesh, root);
		}

		throw new IllegalArgumentException("Illegal type for tree browsing");
	}

	/**
	 * @param mesh
	 * @param root
	 */
	protected HMeshTreeIterator(HMesh mesh, E root) {
		super((HMeshImpl) mesh, root);
	}

	/**
	 * @return
	 */
	@Override
	public final int depth() {
		return depth;
	}

	/**
	 *
	 */
	protected final void incrementDepth() {
		depth++;
	}

	/**
	 *
	 */
	protected final void decrementDepth() {
		depth--;
	}

	/**
	 * @param e
	 * @param children
	 */
	protected final void collectChildren(E e, Collection<E> children) {
		childrenSupplier.accept(e, children);
	}
}