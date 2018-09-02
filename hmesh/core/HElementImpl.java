package hgeom.hmesh.core;

import java.util.Objects;

import hgeom.hmesh.elements.HElement;

/**
 *
 * @author Pierre B.
 */
abstract class HElementImpl implements HElement {

	/**
	 *
	 */
	private int id;

	/**
	 * @param id
	 */
	protected HElementImpl(int id) {
		this.id = id;
	}

	/**
	 * @return
	 */
	public final int id() {
		return id;
	}

	@Override
	public final boolean isDiscarded() {
		return id == -1;
	}

	/**
	 * @param e
	 */
	public static void discard(HElement e) {
		requireValid(e, HElementImpl.class).id = -1;
	}

	/**
	 * @param e
	 * @param c
	 * @return
	 */
	public static <E extends HElement, I extends E> I requireValid(E e,
			Class<I> c) {

		Objects.requireNonNull(e).requireNotDiscarded();
		return c.cast(e);
	}
}
