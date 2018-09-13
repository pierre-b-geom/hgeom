package hgeom.hmesh.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HMesh3D;
import hgeom.hmesh.elements.HVertex;

/**
 *
 * @author Pierre B.
 */
final class MEditExporter3D {

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
	private HMesh3D mesh;

	/**
	 *
	 */
	private int color;

	/**
	 * @param mesh
	 * @param fileName
	 * @throws IOException
	 */
	public void export(HMesh3D mesh, String fileName) throws IOException {
		this.mesh = Objects.requireNonNull(mesh);

		double[] center = mesh.vertices().map(mesh::vertexXYZ).reduce(new double[3],
				(v1, v2) -> {
					v1[0] += v2[0];
					v1[1] += v2[1];
					v1[2] += v2[2];
					return v1;
				});

		long numVertices = mesh.vertices().count();
		center[0] /= numVertices;
		center[1] /= numVertices;
		center[2] /= numVertices;

		// Collecte des aretes et des sommets
		mesh.faces().forEach(f -> {
			int faceColor = color++;
			f.edges().forEach(e -> addHEdge(e, faceColor));
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
			write(os, "Dimension 3\n");
			write(os, "Vertices " + coords.size() + "\n");

			for (double[] xy : coords) {
				write(os, xy[0] + " " + xy[1] + " " + xy[2] + " 0\n");
			}

			write(os, "\nEdges " + edges.size() + "\n");

			for (int[] edge : edges) {
				write(os, edge[0] + " " + edge[1] + " " + edge[2] + "\n");
			}
		}
	}

	/**
	 * @param hEdge
	 * @return
	 */
	/**
	 * @param hEdge
	 * @param color
	 */
	private void addHEdge(HEdge hEdge, int color) {
		int originIndex = addXYZ(hEdge.tail());
		int endIndex = addXYZ(hEdge.head());
		edges.add(new int[] { originIndex, endIndex, color });
	}

	/**
	 * @param v
	 * @return
	 */
	private int addXYZ(HVertex v) {
		coords.add(mesh.vertexXYZ(v));
		return coords.size();
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
