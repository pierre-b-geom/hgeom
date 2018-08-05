package hgeom.hmesh.examples;

import java.util.Comparator;
import java.util.PriorityQueue;

import hgeom.hmesh.data.HBData;
import hgeom.hmesh.data.HDData;
import hgeom.hmesh.data.HData;
import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HMesh;
import hgeom.hmesh.elements.HVertex;

/**
 *
 * @author Pierre Beylot
 */
public final class HMeshOperations {

	/**
	 *
	 */
	private HMeshOperations() {
	}

	/**
	 * Perform the prim algorithme on a {@link HMesh}
	 * <p>
	 * NOT TESTED! may not work
	 *
	 * @param mesh
	 * @param source      source of the prim algorithm
	 * @param edgeWeights weghts of each edge of the mesh
	 * @return predecessor of each vertex in the prim tree
	 */
	public static HData<HVertex, HVertex> prim(HMesh mesh, HVertex source,
			HDData<HEdge> edgeWeights) {

		HBData<HVertex> visited = mesh.createVertexBooleanData();
		HData<HVertex, HVertex> predecessors = mesh.createVertexData();
		HDData<HVertex> cost = mesh.createVertexDoubleData();
		cost.setAll(i -> Double.POSITIVE_INFINITY);
		cost.set(source, 0);

		PriorityQueue<HVertex> q = new PriorityQueue<>(
				Comparator.comparingDouble(cost::get));

		q.add(source);

		while (!q.isEmpty()) {
			HVertex v = q.poll();

			if (!visited.get(v)) {
				visited.set(v, true);

				v.outgoingEdges().forEach(e -> {
					if (cost.get(e.head()) > edgeWeights.get(e)) {
						predecessors.set(e.head(), v);
						cost.set(e.head(), edgeWeights.get(e));
						q.add(e.head());
					}
				});
			}
		}

		return predecessors;
	}

	/**
	 * Compiute the dijkstra distances on a {@link HMesh}
	 * <p>
	 * NOT TESTED! may not work
	 *
	 * @param mesh
	 * @param source
	 * @param edgeWeights
	 * @return
	 */
	public static HDData<HVertex> dijkstraDistances(HMesh mesh, HVertex source,
			HDData<HEdge> edgeWeights) {

		HBData<HVertex> visited = mesh.createVertexBooleanData();
		HDData<HVertex> distances = mesh.createVertexDoubleData();
		distances.setAll(i -> Double.POSITIVE_INFINITY);
		distances.set(source, 0);

		PriorityQueue<HVertex> q = new PriorityQueue<>(
				Comparator.comparingDouble(distances::get));

		q.add(source);

		while (!q.isEmpty()) {
			HVertex v = q.poll();

			if (!visited.get(v)) {
				visited.set(v, true);
				double vDistance = distances.get(v);

				for (HEdge e : v.outgoingEdges()) {
					double relaxed = vDistance + edgeWeights.get(e);

					if (relaxed < distances.get(e.head())) {
						distances.set(e.head(), relaxed);
						q.add(e.head());
					}
				}
			}
		}

		return distances;
	}
}
