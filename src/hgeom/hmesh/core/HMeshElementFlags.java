package hgeom.hmesh.core;

import java.util.Objects;

import hgeom.hmesh.data.HBData;
import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HElement;
import hgeom.hmesh.elements.HFace;
import hgeom.hmesh.elements.HMesh;
import hgeom.hmesh.elements.HVertex;

/**
 *
 * @author Pierre B.
 */
final class HMeshElementFlags {

	/**
	 *
	 */
	private final HMesh mesh;

	/**
	 *
	 */
	private HBData<HVertex> vertexFlags;

	/**
	 *
	 */
	private HBData<HFace> faceFlags;

	/**
	 *
	 */
	private HBData<HEdge> edgeFlags;

	/**
	 * @param mesh
	 */
	public HMeshElementFlags(HMesh mesh) {
		this.mesh = Objects.requireNonNull(mesh);
	}

	/**
	 * @param e
	 * @return
	 */
	public boolean get(HElement e) {
		if (e instanceof HVertex) {
			return vertexFlags == null ? false : vertexFlags.get((HVertex) e);
		}

		if (e instanceof HEdge) {
			return edgeFlags == null ? false : edgeFlags.get((HEdge) e);
		}

		if (e instanceof HFace) {
			return faceFlags == null ? false : faceFlags.get((HFace) e);
		}

		throw new IllegalStateException();
	}

	/**
	 * @param e
	 */
	public void set(HElement e) {
		if (e instanceof HVertex) {
			if (vertexFlags == null) {
				vertexFlags = mesh.createVertexBooleanData();
			}

			vertexFlags.set((HVertex) e, true);
		}

		else if (e instanceof HEdge) {
			if (edgeFlags == null) {
				edgeFlags = mesh.createEdgeBooleanData();
			}

			edgeFlags.set((HEdge) e, true);
		}

		else if (e instanceof HFace) {
			if (faceFlags == null) {
				faceFlags = mesh.createFaceBooleanData();
			}

			faceFlags.set((HFace) e, true);
		}

		else {
			throw new IllegalStateException();
		}
	}
}
