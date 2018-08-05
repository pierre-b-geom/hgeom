package hgeom.hmesh.core;

import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HElement;
import hgeom.hmesh.elements.HFace;
import hgeom.hmesh.elements.HMesh;
import hgeom.hmesh.elements.HVertex;

/**
 *
 * @author Pierre B.
 */
final class HElementFactory {

	/**
	 *
	 */
	private final boolean linkEdgesToPrevious;

	/**
	 *
	 */
	private final boolean debugInfo;

	/**
	 *
	 */
	private int vertexCount;

	/**
	 *
	 */
	private int edgeCount;

	/**
	 *
	 */
	private int faceCount;

	/**
	 * @param linkEdgesToPrevious
	 * @param debugInfo
	 */
	public HElementFactory(boolean linkEdgesToPrevious, boolean debugInfo) {
		this.linkEdgesToPrevious = linkEdgesToPrevious;
		this.debugInfo = debugInfo;
	}

	/**
	 * Cree un sommet
	 *
	 * @return le sommet cree
	 * @throws NullPointerException si tableau de coordonnees {@code null}
	 */
	public HVertex createVertex() {
		return createVertex(null);
	}

	/**
	 * Cree un sommet
	 *
	 * @param edge
	 * @return le sommet cree
	 * @throws NullPointerException si tableau de coordonnees ou arete
	 *                              {@code null}
	 */
	public HVertex createVertex(HEdge edge) {
		return HVertexImpl.create(vertexCount++, edge, debugInfo);
	}

	/**
	 * @param mesh
	 * @return
	 */
	public <D> HDataImpl<HVertex, D> createVertexData(HMesh mesh) {
		return new HDataImpl<>(v -> checkedId(v, vertexCount),
				mesh == null ? null : mesh::vertices);
	}

	/**
	 * @param mesh
	 * @return
	 */
	public HBDataImpl<HVertex> createVertexBooleanData(HMesh mesh) {
		return new HBDataImpl<>(v -> checkedId(v, vertexCount),
				mesh == null ? null : mesh::vertices);
	}

	/**
	 * @param mesh
	 * @return
	 */
	public HIDataImpl<HVertex> createVertexIntData(HMesh mesh) {
		return new HIDataImpl<>(v -> checkedId(v, vertexCount),
				mesh == null ? null : mesh::vertices);
	}

	/**
	 * @param mesh
	 * @return
	 */
	public HDDataImpl<HVertex> createVertexDoubleData(HMesh mesh) {
		return new HDDataImpl<>(v -> checkedId(v, vertexCount),
				mesh == null ? null : mesh::vertices);
	}

	/**
	 * @param vertex
	 * @return
	 */
	public HEdge createEdge(HVertex vertex) {
		return HEdgeImpl.create(edgeCount++, vertex, linkEdgesToPrevious);
	}

	/**
	 * @param mesh
	 * @return
	 */
	public <D> HDataImpl<HEdge, D> createEdgeData(HMesh mesh) {
		return new HDataImpl<>(e -> checkedId(e, edgeCount),
				mesh == null ? null : mesh::edges);
	}

	/**
	 * @param mesh
	 * @return
	 */
	public HBDataImpl<HEdge> createEdgeBooleanData(HMesh mesh) {
		return new HBDataImpl<>(e -> checkedId(e, edgeCount),
				mesh == null ? null : mesh::edges);
	}

	/**
	 * @param mesh
	 * @return
	 */
	public HIDataImpl<HEdge> createEdgeIntData(HMesh mesh) {
		return new HIDataImpl<>(e -> checkedId(e, edgeCount),
				mesh == null ? null : mesh::edges);
	}

	/**
	 * @param mesh
	 * @return
	 */
	public HDDataImpl<HEdge> createEdgeDoubleData(HMesh mesh) {
		return new HDDataImpl<>(e -> checkedId(e, edgeCount),
				mesh == null ? null : mesh::edges);
	}

	/**
	 * @param edge
	 * @param primary
	 * @param checkValidity
	 * @return
	 */
	public HFace createFace(HEdge edge, boolean primary,
			boolean checkValidity) {

		return HFaceImpl.create(faceCount++, edge, primary, checkValidity);
	}

	/**
	 * @param mesh
	 * @return
	 */
	public <D> HDataImpl<HFace, D> createFaceData(HMesh mesh) {
		return new HDataImpl<>(f -> checkedId(f, faceCount),
				mesh == null ? null : mesh::faces);
	}

	/**
	 * @param mesh
	 * @return
	 */
	public HBDataImpl<HFace> createFaceBooleanData(HMesh mesh) {
		return new HBDataImpl<>(f -> checkedId(f, faceCount),
				mesh == null ? null : mesh::faces);
	}

	/**
	 * @param mesh
	 * @return
	 */
	public HIDataImpl<HFace> createFaceIntData(HMesh mesh) {
		return new HIDataImpl<>(f -> checkedId(f, faceCount),
				mesh == null ? null : mesh::faces);
	}

	/**
	 * @param mesh
	 * @return
	 */
	public HDDataImpl<HFace> createFaceDoubleData(HMesh mesh) {
		return new HDDataImpl<>(f -> checkedId(f, faceCount), mesh::faces);
	}

	/**
	 * @param e
	 * @param idUpperBound
	 * @return
	 */
	private static int checkedId(HElement e, int idUpperBound) {
		int id = HElementImpl.requireValid(e, HElementImpl.class).id();

		if (id >= idUpperBound) {
			throw new IllegalArgumentException("illegal element: " + e);
		}

		return id;
	}
}
