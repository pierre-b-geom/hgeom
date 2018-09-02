package hgeom.hmesh.core;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import hgeom.hmesh.data.HBData;
import hgeom.hmesh.data.HDData;
import hgeom.hmesh.data.HData;
import hgeom.hmesh.data.HIData;
import hgeom.hmesh.elements.HEdge;
import hgeom.hmesh.elements.HFace;
import hgeom.hmesh.elements.HMesh;
import hgeom.hmesh.elements.HVertex;
import hgeom.hmesh.sequence.Sequence;
import hgeom.hmesh.util.Loops;

/**
 * @author Pierre B.
 *
 */
// TODO : Installer tests de performance avec librairie externe
// TODO : Utiliser Oxygen
// TODO : Logger dans HMeshImpl pour les operations d'edition
class HMeshImpl implements HMesh {

	/**
	 * Non prive afin de simplifier l'acces par EdgeIterator
	 */
	final List<HFace> faces;

	/**
	 *
	 */
	private final HElementFactory elementFactory;

	/**
	 *
	 */
	private int modCount;

	/**
	 * @param faces
	 * @param elementFactory
	 */
	public HMeshImpl(List<HFace> faces, HElementFactory elementFactory) {
		this.faces = Objects.requireNonNull(faces);
		this.elementFactory = Objects.requireNonNull(elementFactory);
	}

	@Override
	public Stream<HFace> faces() {
		return faces.stream().filter(f -> !f.isDiscarded());
	}

	@Override
	public Stream<HEdge> edges() {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
				new EdgeIterator(), Spliterator.IMMUTABLE | Spliterator.NONNULL
						| Spliterator.DISTINCT),
				false);
	}

	/**
	 *
	 */
	private final class EdgeIterator implements Iterator<HEdge> {

		/**
		 *
		 */
		private final int expectedModCount;

		/**
		 *
		 */
		private int faceIndex = -1;

		/**
		 *
		 */
		private HEdge firstEdge;

		/**
		 *
		 */
		private HEdge edge;

		/**
		 *
		 */
		public EdgeIterator() {
			expectedModCount = modCount();
			faceIndex = nextFaceIndex(faceIndex);
		}

		@Override
		public boolean hasNext() {
			return faceIndex != -1 || iteratingFace();
		}

		@Override
		public HEdge next() {
			if (modCount() != expectedModCount) {
				throw new ConcurrentModificationException();
			}

			if (iteratingFace()) {
				edge = edge.next();
			}

			else if (faceIndex != -1) {
				edge = firstEdge = faces.get(faceIndex).edge();
				faceIndex = nextFaceIndex(faceIndex);
			}

			else {
				throw new NoSuchElementException();
			}

			return edge;
		}

		/**
		 * @return
		 */
		private boolean iteratingFace() {
			return edge != null && edge.next() != firstEdge;
		}

		/**
		 * @param fromIndex
		 * @return
		 */
		private int nextFaceIndex(int fromIndex) {
			for (int iFace = fromIndex + 1; iFace < faces.size(); iFace++) {
				if (!faces.get(iFace).isDiscarded()) {
					return iFace;
				}
			}

			return -1;
		}
	}

	@Override
	public Stream<HVertex> vertices() {
		return edges().filter(e -> e.head().edge() == e).map(HEdge::head);
	}

	@Override
	public void trim() {
		modCount++;
		faces.removeIf(HFace::isDiscarded);
	}

	/**
	 * @return
	 */
	public int modCount() {
		return modCount;
	}

	@Override
	public <D> HData<HVertex, D> createVertexData() {
		return elementFactory.createVertexData(this);
	}

	@Override
	public HBData<HVertex> createVertexBooleanData() {
		return elementFactory.createVertexBooleanData(this);
	}

	@Override
	public HIData<HVertex> createVertexIntData() {
		return elementFactory.createVertexIntData(this);
	}

	@Override
	public HDData<HVertex> createVertexDoubleData() {
		return elementFactory.createVertexDoubleData(this);
	}

	@Override
	public <D> HData<HEdge, D> createEdgeData() {
		return elementFactory.createEdgeData(this);
	}

	@Override
	public HBData<HEdge> createEdgeBooleanData() {
		return elementFactory.createEdgeBooleanData(this);
	}

	@Override
	public HIData<HEdge> createEdgeIntData() {
		return elementFactory.createEdgeIntData(this);
	}

	@Override
	public HDData<HEdge> createEdgeDoubleData() {
		return elementFactory.createEdgeDoubleData(this);
	}

	@Override
	public <D> HData<HFace, D> createFaceData() {
		return elementFactory.createFaceData(this);
	}

	@Override
	public HBData<HFace> createFaceBooleanData() {
		return elementFactory.createFaceBooleanData(this);
	}

	@Override
	public HIData<HFace> createFaceIntData() {
		return elementFactory.createFaceIntData(this);
	}

	@Override
	public HDData<HFace> createFaceDoubleData() {
		return elementFactory.createFaceDoubleData(this);
	}

	@Override
	public Optional<HFace> splitFace(HFace face, HVertex vertex1,
			HVertex vertex2) {

		modCount++;
		HFaceImpl.requireValid(face);
		HVertexImpl.requireValid(vertex1);
		HVertexImpl.requireValid(vertex2);

		Sequence<HEdge> edges = face.edges();

		HEdge edge1 = edges.filter(edge -> edge.head() == vertex1).findFirst()
				.orElseThrow(() -> new IllegalArgumentException(
						"vertex [" + vertex1 + "] does not belong to face"));

		HEdge edge2 = edges.filter(edge -> edge.head() == vertex2).findFirst()
				.orElseThrow(() -> new IllegalArgumentException(
						"vertex [" + vertex2 + "] does not belong to face"));

		// Les 2 aretes pointant vers les 2 sommets ont ete trouvees ainsi que
		// leur positions respectives dans le parcours sur le bord. Reste a
		// verifier que les aretes permettent le decoupage
		if (edge1 == edge2 || edge1.next() == edge2 || edge2.next() == edge1) {
			return Optional.empty();
		}

		// Obtention des aretes suivantes avant de realiser les connexions
		HEdge edge2Next = edge2.next();
		HEdge edge1Next = edge1.next();

		// Creation des 2 demi-aretes de separation
		HEdge edgeToV2 = elementFactory.createEdge(edge2.head());
		HEdge edgeToV1 = elementFactory.createEdge(edge1.head());
		HEdgeImpl.linkAsOpposites(edgeToV1, edgeToV2);

		// Connexions le long du decoupage
		HEdgeImpl.link(edge1, edgeToV2);
		HEdgeImpl.link(edgeToV2, edge2Next);
		HEdgeImpl.link(edge2, edgeToV1);
		HEdgeImpl.link(edgeToV1, edge1Next);

		// A quelles faces appartiennent les 2 demi-aretes de la coupure ?
		HFace newFace;

		// Si la premiere demi-arete appartient a la face, creation de la
		// nouvelle face a partir de l'autre demi-arete
		if (face.edges().anyMatch(edgeToV1::equals)) {
			HEdgeImpl.setFace(edgeToV1, face);
			newFace = elementFactory.createFace(edgeToV2, face.status(), false);
		}

		else {
			HEdgeImpl.setFace(edgeToV2, face);
			newFace = elementFactory.createFace(edgeToV1, face.status(), false);
		}

		faces.add(newFace);
		return Optional.of(newFace);
	}

	@Override
	public boolean mergeFaces(HFace face1, HFace face2) {
		modCount++;
		HFaceImpl.requireValid(face1);
		HFaceImpl.requireValid(face2);

		// Predicats d'iteration sur les 2 faces
		Predicate<HEdge> onBorder1 = e -> e.opposite().face() == face2;

		Predicate<HEdge> lastNotInBorder1 = e -> e.next().opposite()
				.face() == face2;

		Predicate<HEdge> lastOnBorder1 = e -> e.next().opposite()
				.face() != face2;

		Predicate<HEdge> lastOnBorder2 = e -> e.next().opposite()
				.face() != face1;

		// Obtention derniere demi-arete du cycle de la premiere face sur le
		// bord
		HEdge border1LastEdge = face1.edges().filter(onBorder1).findFirst()
				.flatMap(e -> e.next(lastOnBorder1)).orElse(null);

		// La derniere arete peut ne pas exister si le bord commun recouvre
		// totalement le cycle (cas d'une face entouree par une autre face). La
		// fusion ne peut se faire
		if (border1LastEdge == null) {
			return false;
		}

		// Premiere demi-arete a l'exterieur du bord
		HEdge border1NextEdge = border1LastEdge.next();

		// Parcours jusqu'a arriver a la demi-arete avant le bord
		HEdge border1PreviousEdge = border1NextEdge.next(lastNotInBorder1)
				.orElseThrow(() -> new IllegalStateException(
						"Invalid border between face [" + face1 + "] and face ["
								+ face2 + "]"));

		// Premiere demi-arete du bord
		HEdge border1FirstEdge = border1PreviousEdge.next();

		// 1ere subtilite : les 2 faces peuvent avoir plusieurs bords communs.
		// Il faut verifier qu'avancer a partir de la premiere demi-arete du
		// bord fait bien rejoindre la derniere demi-arete
		if (border1FirstEdge.next(lastOnBorder1)
				.orElse(null) != border1LastEdge) {

			return false;
		}

		// 2eme subtilite : le bord est continu sur la premiere face mais
		// peut-etre pas sur la 2eme ! cas tres particulier d'un sommet
		// carrefour ou la premiere face passe une fois et la 2eme face 2 fois.
		// Il y a alors 2 bords du cote de la 2eme face
		HEdge border2FirstEdge = border1LastEdge.opposite();

		HEdge border2LastEdge = border2FirstEdge.next(lastOnBorder2)
				.orElseThrow(() -> new IllegalStateException(
						"Invalid border between face [" + face1 + "] and face ["
								+ face2 + "]"));

		if (border2LastEdge.opposite() != border1FirstEdge) {
			return false;
		}

		// Obtention sommets aux extremites du bord commun
		HVertex borderExtremity1 = border1PreviousEdge.head();
		HVertex borderExtremity2 = border1LastEdge.head();

		HEdge border2NextEdge = border2LastEdge.next();
		HEdge border2PreviousEdge = border2FirstEdge.previous();

		// Declassement des elements strictement a l'interieur du bord commun
		HEdge border1Edge = border1FirstEdge;

		do {
			HEdge removedBorder1Edge = border1Edge;
			HEdge removedBorder2Edge = border1Edge.opposite();
			HVertex removedVertex = border1Edge.head();

			// Passage au suivant avant declassement
			border1Edge = border1Edge.next();

			HElementImpl.discard(removedBorder1Edge);
			HElementImpl.discard(removedBorder2Edge);

			if (removedVertex != borderExtremity2) {
				HElementImpl.discard(removedVertex);
			}

		} while (border1Edge != border1NextEdge);

		// Suture de la 2eme face dans la premiere face
		HEdgeImpl.link(border1PreviousEdge, border2NextEdge);
		HEdgeImpl.link(border2PreviousEdge, border1NextEdge);

		// Mise a jour des aretes de reference des 2 sommets situes aux
		// extremites du bord commun. Les aretes de references peuvent en effet
		// faire partie du bord commun et donc des aretes supprimees
		HVertexImpl.setEdge(borderExtremity1, border1PreviousEdge);
		HVertexImpl.setEdge(borderExtremity2, border2PreviousEdge);

		// Idem pour la face : son arete de reference ne doit pas faire partie
		// des aretes supprimees
		HFaceImpl.setEdge(face1, border1PreviousEdge);

		// Les aretes de la 2eme face doivent changer d'appartenance. Le plus
		// simple est de boucler sur toutes les aretes
		Loops.forEach(border1PreviousEdge, HEdge::next,
				e -> HEdgeImpl.setFace(e, face1));

		// La 2eme face est declassee
		HElementImpl.discard(face2);
		return true;
	}

	@Override
	public HVertex splitEdge(HEdge edge) {
		modCount++;
		HEdgeImpl.requireValid(edge);

		HVertex newVertex = elementFactory.createVertex(edge);

		HEdge edgeNext = edge.next();
		HEdge edgeOpposite = edge.opposite();
		HEdge edgeOppositeNext = edge.opposite().next();
		HVertex head = edge.head();
		HVertex tail = edgeOpposite.head();

		HEdge newEdge = elementFactory.createEdge(head);
		HEdgeImpl.setFace(newEdge, edge.face());

		HEdge newEdgeOpposite = elementFactory.createEdge(tail);
		HEdgeImpl.setFace(newEdgeOpposite, edgeOpposite.face());

		HVertexImpl.setEdge(head, newEdge);
		HVertexImpl.setEdge(tail, newEdgeOpposite);
		HEdgeImpl.setVertex(edge, newVertex);
		HEdgeImpl.setVertex(edgeOpposite, newVertex);
		HEdgeImpl.setVertex(newEdge, head);
		HEdgeImpl.setVertex(newEdgeOpposite, tail);
		HEdgeImpl.link(edge, newEdge);
		HEdgeImpl.link(newEdge, edgeNext);
		HEdgeImpl.link(edgeOpposite, newEdgeOpposite);
		HEdgeImpl.link(newEdgeOpposite, edgeOppositeNext);
		HEdgeImpl.linkAsOpposites(newEdge, edgeOpposite);
		HEdgeImpl.linkAsOpposites(edge, newEdgeOpposite);
		return newVertex;
	}

	@Override
	public boolean collapseEdge(HEdge edge) {
		modCount++;
		HEdgeImpl.requireValid(edge);

		HEdge edgeNext = edge.next();

		// Rien a faire si arete dans un triangle
		if (edge == edgeNext.next().next()) {
			return false;
		}

		HEdge edgeOpposite = edge.opposite();
		HEdge edgeOppositeNext = edgeOpposite.next();

		// Rien a faire si arete opposee dans un triangle
		if (edgeOpposite == edgeOppositeNext.next().next()) {
			return false;
		}

		// Mise a jour des 2 faces si elles referencent les 2 aretes a
		// supprimer
		HFace face = edge.face();
		HFace faceOpposite = edgeOpposite.face();

		if (face.edge() == edge) {
			HFaceImpl.setEdge(face, edgeNext);
		}

		if (faceOpposite.edge() == edgeOpposite) {
			HFaceImpl.setEdge(faceOpposite, edgeOppositeNext);
		}

		HVertex head = edge.head();
		HVertex tail = edgeOpposite.head();
		HEdge edgeOppositePrevious = edgeOpposite.previous();
		HEdge edgePrevious = edge.previous();

		// Iteration sur les aretes pointant vers le sommet a supprimer
		// Les aretes doivent pointer sur le sommet restant
		HEdge e = edge;

		do {
			HEdgeImpl.setVertex(e, tail);
			e = e.next().opposite();
		} while (e != edge);

		// Decoupage de l'arete a supprimer et de son opposee
		HEdgeImpl.link(edgePrevious, edgeNext);
		HEdgeImpl.link(edgeOppositePrevious, edgeOppositeNext);

		// Suppression reference a la demi-arete qui va etre supprimee
		if (tail.edge() == edgeOpposite) {
			HVertexImpl.setEdge(tail, edgeOppositePrevious);
		}

		// Declassification des 2 aretes et du sommet supprimes
		HElementImpl.discard(edge);
		HElementImpl.discard(edgeOpposite);
		HElementImpl.discard(head);
		return true;
	}

	@Override
	public boolean removeVertex(HVertex vertex) {
		modCount++;
		HVertexImpl.requireValid(vertex);

		HEdge edge1 = vertex.edge();
		HEdge edge1Next = edge1.next();
		HEdge edge2 = edge1Next.opposite();
		HEdge edge2Next = edge2.next();

		// Rien a faire si degre different de 2
		if (edge1.opposite() != edge2Next) {
			return false;
		}

		HEdge edge1NextNext = edge1Next.next();
		HEdge edge2NextNext = edge2Next.next();

		// Rien a faire si l'une des demi-aretes appartient a un triangle
		if (edge1NextNext.next() == edge1 || edge2NextNext.next() == edge2) {
			return false;
		}

		HVertex v1 = edge1Next.head();
		HVertex v2 = edge2Next.head();

		// Operation de suture :
		HEdgeImpl.link(edge1, edge1NextNext);
		HEdgeImpl.setVertex(edge1, v1);

		// Suppression reference a la demi-arete qui va etre supprimee
		if (v1.edge() == edge1Next) {
			HVertexImpl.setEdge(v1, edge1);
		}

		if (edge1Next.face().edge() == edge1Next) {
			HFaceImpl.setEdge(edge1Next.face(), edge1);
		}

		HEdgeImpl.link(edge2, edge2NextNext);
		HEdgeImpl.setVertex(edge2, v2);

		if (v2.edge() == edge2Next) {
			HVertexImpl.setEdge(v2, edge2);
		}

		if (edge2Next.face().edge() == edge2Next) {
			HFaceImpl.setEdge(edge2Next.face(), edge2);
		}

		HEdgeImpl.linkAsOpposites(edge1, edge2);

		// Declassification des 2 aretes et du sommet supprimes
		HElementImpl.discard(edge1Next);
		HElementImpl.discard(edge2Next);
		HElementImpl.discard(vertex);
		return true;
	}
}
