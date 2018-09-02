package hgeom.hmesh.elements;

/**
 * Object that can be discarded
 *
 * @author Pierre B.
 */
public interface Discardable {

	/**
	 * @return whether this object is discarded or not. A discarded object is an
	 *         element (vertex, half-edge, face, ...) that have been removed
	 *         from a {@link HMesh mesh} due to a topological operation. A
	 *         discarded object is no longer usable
	 */
	boolean isDiscarded();

	/**
	 * throws an {@code IllegalStateException} if this object is discarded
	 *
	 * @see Discardable#isDiscarded()
	 */
	default void requireNotDiscarded() {
		if (isDiscarded()) {
			throw new IllegalStateException(
					"discarded object no longer usable");
		}
	}
}
