package hgeom.hmesh.core;

import java.util.List;

import hgeom.hmesh.data.HDData;
import hgeom.hmesh.elements.HFace;
import hgeom.hmesh.elements.HMesh3D;
import hgeom.hmesh.elements.HVertex;

/**
 *
 * @author Pierre B.
 */
final class HMesh3DImpl extends HMeshImpl implements HMesh3D {

	/**
	 *
	 */
	private final HDData<HVertex> xs;

	/**
	 *
	 */
	private final HDData<HVertex> ys;

	/**
	 *
	 */
	private final HDData<HVertex> zs;

	/**
	 * @param faces
	 * @param elementFactory
	 */
	public HMesh3DImpl(List<HFace> faces, HElementFactory elementFactory) {
		super(faces, elementFactory);
		this.xs = elementFactory.createVertexDoubleData(this);
		this.ys = elementFactory.createVertexDoubleData(this);
		this.zs = elementFactory.createVertexDoubleData(this);
	}

	@Override
	public HDData<HVertex> vertexXs() {
		return xs;
	}

	@Override
	public HDData<HVertex> vertexYs() {
		return ys;
	}

	@Override
	public HDData<HVertex> vertexZs() {
		return ys;
	}

	@Override
	public double vertexX(HVertex v) {
		return xs.get(v);
	}

	@Override
	public void setVertexX(HVertex v, double x) {
		xs.set(v, x);
	}

	@Override
	public double vertexY(HVertex v) {
		return ys.get(v);
	}

	@Override
	public void setVertexY(HVertex v, double y) {
		ys.set(v, y);
	}

	@Override
	public double vertexZ(HVertex v) {
		return zs.get(v);
	}

	@Override
	public void setVertexZ(HVertex v, double y) {
		zs.set(v, y);
	}

	@Override
	public double[] vertexXYZ(HVertex v, double[] xyz) {
		double[] result = xyz == null ? new double[3] : xyz;
		result[0] = xs.get(v);
		result[1] = ys.get(v);
		result[2] = zs.get(v);
		return result;
	}

	@Override
	public void setVertexXYZ(HVertex v, double[] xyz) {
		xs.set(v, xyz[0]);
		ys.set(v, xyz[1]);
		zs.set(v, xyz[2]);
	}

	@Override
	public void setVertexXYZ(HVertex v, double x, double y, double z) {
		xs.set(v, x);
		ys.set(v, y);
		zs.set(v, z);
	}
}
