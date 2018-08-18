package hgeom.hmesh.core;

import java.util.Arrays;
import java.util.Objects;

import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HFace;
import hgeom.hmesh.elements.HMesh;
import hgeom.hmesh.elements.HMesh2D;
import hgeom.hmesh.elements.HMesh3D;
import hgeom.hmesh.elements.HVertex;

/**
 * methodes en vrac
 *
 * @author Pierre B.
 */
final class ToStringUtils {

	/**
	 *
	 */
	private static final String DISCARDED = "[discarded]";

	/**
	 *
	 */
	private ToStringUtils() {
	}

	/**
	 * @param v
	 * @param mesh
	 * @return
	 */
	public static String toString(HVertex v, HMesh mesh) {
		if (v.isDiscarded()) {
			return DISCARDED;
		}

		if (mesh instanceof HMesh2D) {
			return Arrays.toString(((HMesh2D) mesh).vertexXY(v));
		}

		if (mesh instanceof HMesh3D) {
			return Arrays.toString(((HMesh3D) mesh).vertexXYZ(v));
		}

		return null;
	}

	/**
	 * @param e
	 * @return
	 */
	public static String toString(HEdge e) {
		if (e.isDiscarded()) {
			return DISCARDED;
		}

		// Certains composants peuvent etre null lors de la construction
		HVertex tail = e.opposite() == null ? null : e.opposite().head();
		HVertex head = e.head();
		HVertex nextHead = e.next() == null ? null : e.next().head();

		StringBuilder sb = new StringBuilder();
		sb.append(Objects.toString(tail));
		sb.append("->").append(Objects.toString(head));
		sb.append(", next->").append(Objects.toString(nextHead));
		return sb.toString();
	}

	/**
	 * @param f
	 * @return
	 */
	public static String toString(HFace f) {
		if (f.isDiscarded()) {
			return DISCARDED;
		}

		StringBuilder sb = new StringBuilder();
		int numEdges = f.edge().cycle().count();
		sb.append(f.isPrimary() ? "Primary, " : "Complementary, ");
		sb.append(numEdges).append(" edges: ");
		sb.append(Objects.toString(f.edge().head()));
		HEdge e = f.edge();

		for (int iEdge = 0; iEdge < Math.min(numEdges, 50); iEdge++) {
			e = e.next();
			sb.append("->").append(Objects.toString(e.head()));

			if (iEdge % 4 == 1) {
				sb.append("\n");
			}
		}

		sb.append("\n");
		return sb.toString();
	}
}
