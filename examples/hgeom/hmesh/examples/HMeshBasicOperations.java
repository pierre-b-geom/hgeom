package hgeom.hmesh.examples;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import hgeom.hmesh.elements.HFace;
import hgeom.hmesh.elements.HMesh;
import hgeom.hmesh.elements.HMesh2D;
import hgeom.hmesh.elements.HVertex;
import hgeom.hmesh.sequence.Sequence;

/**
 *
 * @author Pierre B.
 */
public final class HMeshBasicOperations {

	/**
	 *
	 */
	private HMeshBasicOperations() {
	}

	/**
	 * @param mesh
	 * @return number of triangles inside a mesh
	 */
	public static int numberOfTriangles(HMesh mesh) {
		return (int) mesh.faces().map(HFace::edges).mapToInt(Sequence::count)
				.filter(count -> count == 3).count();
	}

	/**
	 * @param mesh
	 * @return the average of the number of edges of all the faces of the given
	 *         mesh
	 */
	public static OptionalDouble averageNumberOfEdges(HMesh mesh) {
		return mesh.faces().map(HFace::edges).mapToInt(Sequence::count)
				.average();
	}

	/**
	 * @param mesh
	 * @return average length of all edges of a given 2D mesh
	 */
	public static OptionalDouble averageLengthOfEdges(HMesh2D mesh) {
		return mesh.edges().mapToDouble(e -> {
			double[] headCoords = mesh.vertexXY(e.head());
			double[] tailCoords = mesh.vertexXY(e.tail());
			double xDelta = headCoords[0] - tailCoords[0];
			double yDelta = headCoords[1] - tailCoords[1];
			return Math.sqrt(xDelta * xDelta + yDelta * yDelta);
		}).average();
	}

	/**
	 * @param mesh
	 * @return the leftmost x limit of the given mesh
	 */
	public static OptionalDouble leftBound(HMesh2D mesh) {
		return mesh.vertexXs().stream().min();
	}

	/**
	 * @param v
	 * @return all neighbors of the specified vertex
	 */
	public static Collection<HVertex> vertexNeighbors(HVertex v) {
		return v.neighbors().toList();
	}

	/**
	 * @param v
	 * @return the vertices contained in the first and second rings of vertices
	 *         surrounding the given vertex. The first ring is made up of the
	 *         vertices immediatly close to the given vertex. The second ring
	 *         consists of the vertices in contact with those of the first ring
	 */
	public static Collection<HVertex> vertexNeighborsLevel2(HVertex v) {
		Collection<HVertex> neighbors = new HashSet<>();

		for (HVertex neighbor : v.neighbors()) {
			neighbor.neighbors().filter(v2 -> !v.equals(v2)).collect(neighbors,
					Collection::add);
		}

		return neighbors;
	}

	/**
	 * @param mesh
	 * @param degree
	 * @return how many vertices whose degree is 3 a mesh contains
	 */
	public static int numVerticesWithDegree(HMesh mesh, int degree) {
		return (int) mesh.vertices().filter(v -> v.degree() == degree).count();
	}

	/**
	 * @param mesh
	 * @return a map associating vertex degree with the number of vertices of a
	 *         given mesh having this degree
	 */
	public static Map<Integer, Integer> numVerticesByDegree(HMesh mesh) {
		return mesh.vertices()
				.collect(Collectors.groupingBy(HVertex::degree,
						Collectors.collectingAndThen(Collectors.counting(),
								Number::intValue)));
	}

	/**
	 * @param mesh
	 * @return a map grouping the faces of a given mesh according to their
	 *         number of edges
	 */
	public static Map<Integer, List<HFace>> faceByNumberOfEdges(HMesh mesh) {
		return mesh.faces()
				.collect(Collectors.groupingBy(f -> f.edges().count()));
	}

	/**
	 * Basic triangulation of all quads found in the specified mesh
	 *
	 * @param mesh
	 */
	public static void triangulateQuads(HMesh mesh) {
		List<HFace> quads = mesh.faces().filter(f -> f.edges().count() == 4)
				.collect(Collectors.toList());

		quads.forEach(quad -> mesh.splitFace(quad, quad.edge().head(),
				quad.edge().next().next().head()));
	}

	/**
	 * Remove all vertices whose degree is 2 from the given mesh
	 *
	 * @param mesh
	 */
	public static void simplify(HMesh mesh) {
		List<HVertex> aloneVertices = mesh.vertices()
				.filter(v -> v.degree() == 2).collect(Collectors.toList());

		aloneVertices.forEach(mesh::removeVertex);
	}
}
