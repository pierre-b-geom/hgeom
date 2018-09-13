package hgeom.hmesh.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HMesh2D;
import hgeom.hmesh.elements.HVertex;

/**
 *
 * @author Pierre B.
 */
final class MEditExporter2D {

	/**
	 *
	 */
	private final List<double[]> coords = new ArrayList<>();

	/**
	 *
	 */
	private final List<int[]> edges = new ArrayList<>();

	/**
	 *
	 */
	private HMesh2D mesh;

	/**
	 *
	 */
	private int color;

	/**
	 * @param mesh
	 * @param fileName
	 * @throws IOException
	 */
	public void export(HMesh2D mesh, String fileName) throws IOException {
		this.mesh = Objects.requireNonNull(mesh);
		coords.clear();
		edges.clear();

		double average = mesh.edges().mapToDouble(this::edgeLength).average()
				.orElseThrow(
						() -> new IllegalArgumentException("invalid mesh"));

		double shift = average * .015;

		// Collecte des aretes et des sommets
		mesh.faces().forEach(f -> {
			int faceColor = color++;
			f.edges().forEach(e -> addHEdge(e, faceColor, shift));
		});

		// Ecriture du fichier de parametrage de l'affichage
		try (OutputStream os = new FileOutputStream(fileName + ".medit")) {
			write(os, "BackgroundColor .85 .85 .85\n");
			write(os, "WindowSize 800 800\n");
			write(os, "RenderMode colorshading\n");
		}

		// Ecriture du fichier de definition du maillage
		try (OutputStream os = new FileOutputStream(fileName + ".mesh")) {
			write(os, "MeshVersionFormatted 1\n");
			write(os, "Dimension 2\n");
			write(os, "Vertices " + coords.size() + "\n");

			for (double[] xy : coords) {
				write(os, xy[0] + " " + xy[1] + " 0\n");
			}

			write(os, "\nEdges " + edges.size() + "\n");

			for (int[] edge : edges) {
				write(os, edge[0] + " " + edge[1] + " " + edge[2] + "\n");
			}
		}
	}

	/**
	 * @param hEdge
	 * @param color
	 * @param shift
	 */
	private void addHEdge(HEdge hEdge, int color, double shift) {
		HVertex v0 = hEdge.head();
		HVertex v1 = hEdge.next().head();
		HVertex v2 = hEdge.next().next().head();
		HVertex v3 = hEdge.next().next().next().head();
		double[] edgeOrigin = shift(v0, v1, v2, shift);
		double[] edgeEnd = shift(v1, v2, v3, shift);
		double[] edgeDir = normal(vector(edgeOrigin, edgeEnd));
		double[] orthoDir = orthogonalVector(edgeDir);
		double[] arrowBase = plus(edgeEnd, mult(edgeDir, -5 * shift));
		double[] arrowLeft = plus(arrowBase, mult(orthoDir, 1.7 * shift));

		int originIndex = addXY(edgeOrigin);
		int endIndex = addXY(edgeEnd);
		int arrowLeftIndex = addXY(arrowLeft);
		edges.add(new int[] { originIndex, endIndex, color });
		edges.add(new int[] { endIndex, arrowLeftIndex, color });
	}

	/**
	 * @param xy
	 * @return
	 */
	private int addXY(double[] xy) {
		coords.add(xy);
		return coords.size();
	}

	/**
	 * @param prev
	 * @param p     le point a decaler
	 * @param next
	 * @param shift
	 * @return
	 */
	private double[] shift(HVertex prev, HVertex p, HVertex next,
			double shift) {

		if (shift == 0.) {
			return xy(p);
		}

		double[] prevToP = vector(xy(prev), xy(p));
		double[] prevOrtho = orthogonalVector(normal(prevToP));
		double[] shiftedP1 = plus(xy(p), mult(prevOrtho, shift));
		double[] prevLine = line(shiftedP1, prevToP);

		double[] pToNext = vector(xy(p), xy(next));
		double[] nextOrtho = orthogonalVector(normal(pToNext));
		double[] shiftedP2 = plus(xy(p), mult(nextOrtho, shift));
		double[] nextLine = line(shiftedP2, pToNext);

		// Si pas d'intersection, choix du milieu entre les 2 points decales
		return linesIntersection(prevLine, nextLine)
				.orElse(mult(plus(shiftedP1, shiftedP2), .5));
	}

	/**
	 * @param hEdge
	 * @return
	 */
	private double edgeLength(HEdge hEdge) {
		return norm(vector(xy(hEdge.tail()), xy(hEdge.head())));
	}

	/**
	 * @param v
	 * @return
	 */
	private double[] xy(HVertex v) {
		return mesh.vertexXY(v);
	}

	/**
	 * @param p
	 * @param v
	 * @return la ligne sous la forme [a, b, c] de ax + by + c
	 */
	private static double[] line(double[] p, double[] v) {
		double px = p[0];
		double py = p[1];
		double vx = v[0];
		double vy = v[1];
		return new double[] { -vy, vx, vy * px - vx * py };
	}

	/**
	 * @param l0
	 * @param l1
	 * @return
	 */
	private static Optional<double[]> linesIntersection(double[] l0,
			double[] l1) {

		double a0 = l0[0];
		double b0 = l0[1];
		double c0 = l0[2];
		double a1 = l1[0];
		double b1 = l1[1];
		double c1 = l1[2];
		double det = b0 * a1 - b1 * a0;

		if (det < 1E-6) {
			return Optional.empty();
		}

		double y = (c1 * a0 - c0 * a1) / det;
		double x = a0 == 0. ? -(b1 * y + c1) / a1 : -(b0 * y + c0) / a0;
		return Optional.of(new double[] { x, y });
	}

	/**
	 * @param p0
	 * @param p1
	 * @return
	 */
	private static double[] vector(double[] p0, double[] p1) {
		return new double[] { p1[0] - p0[0], p1[1] - p0[1] };
	}

	/**
	 * @param v
	 * @return
	 */
	private static double norm(double[] v) {
		return Math.sqrt(v[0] * v[0] + v[1] * v[1]);
	}

	/**
	 * @param v
	 * @return
	 */
	private static double[] orthogonalVector(double[] v) {
		return new double[] { -v[1], v[0] };
	}

	/**
	 * Pas de verification sur la nullie de la norme !
	 *
	 * @param v
	 * @return
	 */
	private static double[] normal(double[] v) {
		return mult(v, 1 / norm(v));
	}

	/**
	 * @param v
	 * @param coeff
	 * @return
	 */
	private static double[] mult(double[] v, double coeff) {
		return new double[] { v[0] * coeff, v[1] * coeff };
	}

	/**
	 * @param p0
	 * @param p1
	 * @return
	 */
	private static double[] plus(double[] p0, double[] p1) {
		return new double[] { p0[0] + p1[0], p0[1] + p1[1] };
	}

	/**
	 * @param os
	 * @param s
	 * @throws IOException
	 */
	private static void write(OutputStream os, String s) throws IOException {
		os.write(s.getBytes());
	}
}
