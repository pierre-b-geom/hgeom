package hgeom.hmesh.test;

import java.util.ArrayList;
import java.util.List;

import hgeom.hmesh.elements.HMesh2D;

/**
 *
 * @author Pierre B.
 */
final class GridDrawer {

	/**
	 *
	 */
	private final List<int[][]> edges = new ArrayList<>();

	/**
	 *
	 */
	private int firstX;

	/**
	 *
	 */
	private int firstY;

	/**
	 *
	 */
	private int lastX = Integer.MIN_VALUE;

	/**
	 *
	 */
	private int lastY;

	/**
	 * @param xy
	 * @return
	 */
	public GridDrawer penFrom(int[] xy) {
		return penFrom(xy[0], xy[1]);
	}

	/**
	 * @param x
	 * @param y
	 * @return
	 */
	public GridDrawer penFrom(int x, int y) {
		firstX = x;
		firstY = y;
		lastX = x;
		lastY = y;
		return this;
	}

	/**
	 * @param xy
	 * @return
	 */
	public GridDrawer to(int[] xy) {
		return to(xy[0], xy[1]);
	}

	/**
	 * @param x
	 * @param y
	 * @return
	 */
	public GridDrawer to(int x, int y) {
		if (lastX == Integer.MIN_VALUE) {
			throw new IllegalStateException();
		}

		edges.add(new int[][] { { lastX, lastY }, { x, y } });
		lastX = x;
		lastY = y;
		return this;
	}

	/**
	 * @return
	 */
	public GridDrawer close() {
		edges.add(new int[][] { { lastX, lastY }, { firstX, firstY } });
		lastX = Integer.MIN_VALUE;
		return this;
	}

	/**
	 * @return
	 */
	public HMesh2D mesh() {
		return Grid.meshFromEdges(edges);
	}
}
