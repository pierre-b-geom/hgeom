package hgeom.hmesh.elements;

import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;

import hgeom.hmesh.winding.PolygonWindingProvider;
import hgeom.hmesh.winding.Winding;

/**
 * Supplier of an indexed collection of 3D coords. Each 3D coord is defined by a
 * array of 3 double values [x y z]
 *
 * @author Pierre B.
 */
public final class Coord3DSource {

	/**
	 *
	 */
	private final IntToDoubleFunction xs;

	/**
	 *
	 */
	private final IntToDoubleFunction ys;

	/**
	 *
	 */
	private final IntToDoubleFunction zs;

	/**
	 * Creates a vertex supplier from a list of vertex coordinates .
	 *
	 * @param coords
	 */
	public Coord3DSource(List<double[]> coords) {
		this(coords::get);
	}

	/**
	 * Constructs a 3D coords source from the sepcified list of 3D coordinates
	 *
	 * @param coords a list of 3D coords in which each coord is defined by an
	 *               array of 3 values [x y z]
	 */
	public Coord3DSource(double[][] coords) {
		this(i -> coords[i]);
	}

	/**
	 * Constructs a 3D coords source from 3 arrays of doubles. The arrays should
	 * contain respectively the x, y and z coordinates
	 *
	 * @param xs an array containing the x coordinates
	 * @param ys an array containing the y coordinates
	 * @param zs an array containing the z coordinates
	 */
	public Coord3DSource(double[] xs, double[] ys, double[] zs) {
		this(i -> xs[i], i -> ys[i], i -> zs[i]);
	}

	/**
	 * Constructs a 3D coords source from a function returning a [x y z] array
	 * according to an index.
	 *
	 * @param coords a function returning a [x y z] array according to an index
	 */
	public Coord3DSource(IntFunction<double[]> coords) {
		this(i -> coords.apply(i)[0], i -> coords.apply(i)[1],
				i -> coords.apply(i)[2]);
	}

	/**
	 * Constructs a 3D coords source from 3 functions returning respectively a x
	 * coord according to an index, a y coord according to an index and a z
	 * coord according to an index
	 *
	 * @param xs a function return a x coordinate according to an index
	 * @param ys a function return a y coordinate according to an index
	 * @param zs a function return a z coordinate according to an index
	 */
	public Coord3DSource(IntToDoubleFunction xs, IntToDoubleFunction ys,
			IntToDoubleFunction zs) {

		this.xs = Objects.requireNonNull(xs);
		this.ys = Objects.requireNonNull(ys);
		this.zs = Objects.requireNonNull(zs);
	}

	/**
	 * Returns the x coordinate with the specified index in this 3D coords
	 * source
	 *
	 * @param index index of the x coordinate to return
	 * @return the x coordinate with the specified index
	 */
	public double x(int index) {
		return xs.applyAsDouble(index);
	}

	/**
	 * Returns the y coordinate with the specified index in this 3D coords
	 * source
	 *
	 * @param index index of the y coordinate to return
	 * @return the y coordinate with the specified index
	 */
	public double y(int index) {
		return ys.applyAsDouble(index);
	}

	/**
	 * Returns the z coordinate with the specified index in this 3D coords
	 * source
	 *
	 * @param index index of the z coordinate to return
	 * @return the z coordinate with the specified index
	 */
	public double z(int index) {
		return zs.applyAsDouble(index);
	}

	/**
	 * Generates and returns a {@link PolygonWindingProvider} based upon the x &
	 * y of the 3D coords of this source
	 *
	 * @return a polygon winding provider based on the x & y coords of this
	 *         source
	 */
	public PolygonWindingProvider xyWindingProvider() {
		return vertexIndices -> Winding.ofPolygon2D(vertexIndices.length,
				i -> x(vertexIndices[i]), i -> y(vertexIndices[i]));
	}

	/**
	 * Generates and returns a {@link PolygonWindingProvider} based upon the x &
	 * z of the 3D coords of this source
	 *
	 * @return a polygon winding provider based on the x & z coords of this
	 *         source
	 */
	public PolygonWindingProvider xzWindingProvider() {
		return vertexIndices -> Winding.ofPolygon2D(vertexIndices.length,
				i -> x(vertexIndices[i]), i -> z(vertexIndices[i]));
	}

	/**
	 * Generates and returns a {@link PolygonWindingProvider} based upon the y &
	 * z of the 3D coords of this source
	 *
	 * @return a polygon winding provider based on the y & z coords of this
	 *         source
	 */
	public PolygonWindingProvider yzWindingProvider() {
		return vertexIndices -> Winding.ofPolygon2D(vertexIndices.length,
				i -> y(vertexIndices[i]), i -> z(vertexIndices[i]));
	}
}
