package hgeom.hmesh.elements;

import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;

import hgeom.hmesh.winding.PolygonWindingProvider;
import hgeom.hmesh.winding.Winding;

/**
 * Supplier of an indexed collection of 2D coords. Each 2D coord is defined by a
 * array of 2 double values [x y]
 *
 * @author Pierre B.
 */
public final class Coord2DSource {

	/**
	 *
	 */
	private final IntToDoubleFunction xs;

	/**
	 *
	 */
	private final IntToDoubleFunction ys;

	/**
	 * Constructs a 2D coords source from the sepcified list of 2D coordinates
	 *
	 * @param coords a list of 2D coords in which each coord is defined by an
	 *               array of 2 values [x y]
	 */
	public Coord2DSource(List<double[]> coords) {
		this(coords::get);
	}

	/**
	 * Constructs a 2D coords source from specified array of 2D coordinates
	 *
	 * @param coords an array of 2D coords in which each coord is defined by an
	 *               array of 2 values [x y]
	 */
	public Coord2DSource(double[][] coords) {
		this(i -> coords[i]);
	}

	/**
	 * Constructs a 2D coords source from 2 arrays of doubles. The first
	 * specified array should contain the x coords and the second specified
	 * array should contain the y c coords
	 *
	 * @param xs an array containing the x coordinates
	 * @param ys an array containing the y coordinates
	 */
	public Coord2DSource(double[] xs, double[] ys) {
		this(i -> xs[i], i -> ys[i]);
	}

	/**
	 * Constructs a 2D coords source from a function returning a [x y] array
	 * according to an index.
	 *
	 * @param coords a function returning a [x y] array according to an index
	 */
	public Coord2DSource(IntFunction<double[]> coords) {
		this(i -> coords.apply(i)[0], i -> coords.apply(i)[1]);
	}

	/**
	 * Constructs a 2D coords source from a function returning a x coord
	 * according to an index and a function returning a y coord according to an
	 * index
	 *
	 * @param xs a function returning a x coordinate according to an index
	 * @param ys a function returning a y coordinate according to an index
	 */
	public Coord2DSource(IntToDoubleFunction xs, IntToDoubleFunction ys) {
		this.xs = Objects.requireNonNull(xs);
		this.ys = Objects.requireNonNull(ys);
	}

	/**
	 * Returns the x coordinate with the specified index in this 2D coords
	 * source
	 *
	 * @param index index of the x coordinate to return
	 * @return the x coordinate with the specified index
	 */
	public double x(int index) {
		return xs.applyAsDouble(index);
	}

	/**
	 * Returns the y coordinate with the specified index in this 2D coords
	 * source
	 *
	 * @param index index of the y coordinate to return
	 * @return the y coordinate with the specified index
	 */
	public double y(int index) {
		return ys.applyAsDouble(index);
	}

	/**
	 * Generates and returns a {@link PolygonWindingProvider} based upon the 2D
	 * coords of this source
	 *
	 * @return a polygon winding provider based on the 2D coords of this source
	 */
	public PolygonWindingProvider windingProvider() {
		return vertexIndices -> Winding.ofPolygon2D(vertexIndices.length,
				i -> x(vertexIndices[i]), i -> y(vertexIndices[i]));
	}
}
