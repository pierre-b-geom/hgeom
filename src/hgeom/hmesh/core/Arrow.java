package hgeom.hmesh.core;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HVertex;

/**
 * Interne a la construction des structures demi-aretes
 */
final class Arrow {

	/**
	 *
	 */
	private Arrow next;

	/**
	 * La demi-arete equivalente a la fleche
	 */
	private final HEdge hEdge;

	/**
	 * Index du cycle auquel appartient la demi-arete s'il existe ou -1
	 */
	private final int cycleIndex;

	/**
	 *
	 */
	private boolean marked;

	/**
	 * @param hEdge
	 * @param cycleIndex
	 */
	public Arrow(HEdge hEdge, int cycleIndex) {
		this.hEdge = Objects.requireNonNull(hEdge);
		this.cycleIndex = cycleIndex;
	}

	/**
	 * @param hEdge
	 * @param cycleIndex
	 */
	public void linkLastIfAbsent(HEdge hEdge, int cycleIndex) {
		if (this.hEdge != hEdge) {
			if (next == null) {
				next = new Arrow(hEdge, cycleIndex);
			}

			else {
				next.linkLastIfAbsent(hEdge, cycleIndex);
			}
		}
	}

	/**
	 * @param p
	 * @return
	 */
	public Arrow get(Predicate<Arrow> p) {
		if (p.test(this)) {
			return this;
		}

		return next == null ? null : next.get(p);
	}

	/**
	 * @param c
	 */
	public void forEach(Consumer<Arrow> c) {
		c.accept(this);

		if (next != null) {
			next.forEach(c);
		}
	}

	/**
	 * @param p
	 * @return
	 */
	public boolean forEachWhile(Predicate<Arrow> p) {
		boolean result = p.test(this);

		if (result && next != null) {
			result = next.forEachWhile(p);
		}

		return result;
	}

	/**
	 * @return
	 */
	public HEdge hEdge() {
		return hEdge;
	}

	/**
	 * @return
	 */
	public int cycleIndex() {
		return cycleIndex;
	}

	/**
	 * @return
	 */
	public boolean isMarked() {
		return marked;
	}

	/**
	 *
	 */
	public void mark() {
		marked = true;
	}

	/**
	 * @return
	 */
	public HVertex head() {
		return hEdge.head();
	}

	/**
	 * @return
	 */
	public HVertex tail() {
		return hEdge.tail();
	}

	@Override
	public String toString() {
		return hEdge.toString();
	}
}