package hgeom.hmesh.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

import hgeom.hmesh.core.HConversion;
import hgeom.hmesh.core.ToHMeshConverter;
import hgeom.hmesh.elements.Coord2DSource;
import hgeom.hmesh.elements.Coord3DSource;
import hgeom.hmesh.elements.FaceSource;
import hgeom.hmesh.elements.HMesh2D;
import hgeom.hmesh.elements.HMesh3D;
import hgeom.hmesh.util.Loops;

/**
 *
 * @author Pierre B.
 */
abstract class Importer {

	/**
	 *
	 */
	static final class ImportResult {

		/**
		 *
		 */
		private final List<double[]> coords = new ArrayList<>();

		/**
		 *
		 */
		private final List<int[]> faces = new ArrayList<>();

		/**
		 *
		 */
		private boolean invalid;

		/**
		 * @param coord
		 */
		void addCoord(double... coord) {
			coords.add(coord);
		}

		/**
		 * @param face
		 */
		void addFace(int... face) {
			faces.add(face);
		}

		/**
		 *
		 */
		void setInvalid() {
			invalid = true;
		}

		/**
		 * @return
		 */
		public int numCoords() {
			return coords.size();
		}

		/**
		 * @return
		 */
		public Stream<double[]> coords() {
			return coords.stream();
		}

		/**
		 * @return
		 */
		public int numFaces() {
			return faces.size();
		}

		/**
		 * @return
		 */
		public Stream<int[]> faces() {
			return faces.stream();
		}

		/**
		 * @return
		 */
		public List<int[]> edges() {
			List<int[]> edges = new ArrayList<>();

			faces.forEach(face -> Loops.forEachPair(face,
					(i1, i2) -> edges.add(new int[] { i1, i2 })));

			return edges;
		}

		/**
		 * @return
		 */
		public Optional<HMesh3D> toMesh3D() {
			if (invalid) {
				return Optional.empty();
			}

			return new ToHMeshConverter(false, true)
					.convert(new FaceSource(faces), new Coord3DSource(coords))
					.map(HConversion::mesh);
		}

		/**
		 * @return
		 */
		public Optional<HMesh2D> toMesh2D() {
			if (invalid) {
				return Optional.empty();
			}

			return new ToHMeshConverter(false, true)
					.convert(new FaceSource(faces), new Coord2DSource(coords))
					.map(HConversion::mesh);
		}
	}

	/**
	 *
	 */
	private static final Logger LOGGER = Logger
			.getLogger(Importer.class.getName());

	/**
	 *
	 */
	private ImportResult result;

	/**
	 * @param fileName
	 * @return
	 */
	public final ImportResult importMesh(String fileName) {
		result = new ImportResult();

		try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
			lines.forEach(this::readLine);
		}

		catch (IOException | NumberFormatException e) {
			LOGGER.throwing(this.getClass().getName(), "importMesh", e);
			result.setInvalid();
		}

		return result;
	}

	/**
	 * @param l
	 */
	protected abstract void readLine(String l);

	/**
	 * @param coord
	 */
	protected final void addCoord(double[] coord) {
		result.addCoord(coord);
	}

	/**
	 * @param face
	 */
	protected final void addFace(int... face) {
		result.addFace(face);
	}
}
