package hgeom.hmesh.core;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;

import hgeom.hmesh.elements.HVertex;
import hgeom.hmesh.winding.PolygonWindingProvider;
import hgeom.hmesh.winding.Winding;

/**
 * Classement entre aretes ayant une tete commune
 *
 * @author Pierre B.
 */
final class ArrowSorter {

	/**
	 * Comparateur de fleches en se basant sur l'angle forme par les fleches
	 * dans le cercle trigonometrique dont le centre est leur tete commune.
	 * Comparateur non transitif. Du fait de la cyclicite du cercle
	 * trigonometrique, il est possible d'avoir a1 < a2 < a3 < a1 !
	 */
	private final Comparator<Arrow> relativeComparator;

	/**
	 * @param windingProvider
	 * @param toVertexIndex
	 */
	public ArrowSorter(PolygonWindingProvider windingProvider,
			ToIntFunction<HVertex> toVertexIndex) {

		Objects.requireNonNull(windingProvider);
		Objects.requireNonNull(toVertexIndex);

		relativeComparator = (a1, a2) -> {
			HVertex head = a1.head();

			if (a2.head() != head) {
				throw new IllegalStateException(
						"invalid comparaison between 2 arrows");
			}

			// Obtention indices des sommets
			int a1TailIndex = toVertexIndex.applyAsInt(a1.tail());
			int a2TailIndex = toVertexIndex.applyAsInt(a2.tail());
			int headIndex = toVertexIndex.applyAsInt(head);
			int[] vertexIndices = { a2TailIndex, headIndex, a1TailIndex };

			// Si premiere fleche a1 consideree a O° dans cercle
			// trigonometrique, alors tableau de sommets avec ordre horaire si
			// 2eme fleche a2 dans demi-cercle superieur entre ]0° 180°[ (a1 <
			// a2) ; anti-horaire si 2eme fleche dans demi-cercle inferieur
			// entre ]-180° 0°[ (a1 > a2) ; indetermine si 2eme fleche a 0° ou
			// 180°
			Winding winding = windingProvider.get(vertexIndices);

			if (winding == Winding.COUNTERCLOCKWISE) {
				return -1;
			}

			if (winding == Winding.CLOCKWISE) {
				return 1;
			}

			return 0;
		};
	}

	/**
	 * Classement d'un tableau d'aretes ayant une tete commune selon le sens
	 * trigonometrique. Classement non effectue si les aretes ont la meme
	 * direction
	 *
	 * @param arrows
	 */
	public void sort(List<Arrow> arrows) {
		int numArrows = arrows.size();

		// Classement inutile si 2 aretes
		if (numArrows > 2) {
			Arrow axe1 = arrows.get(0);
			Arrow axe2 = null;

			// Recherche de 2 aretes qui ne soient pas alignees
			for (int iArrow = 1; axe2 == null && iArrow < numArrows; iArrow++) {
				Arrow arrow = arrows.get(iArrow);
				int diff = relativeComparator.compare(axe1, arrow);

				// choix du 2eme axe tel que axe1 > axe2
				if (diff > 0) {
					axe2 = arrow;
				}

				else if (diff < 0) {
					axe2 = axe1;
					axe1 = arrow;
				}
			}

			// Classement possible seulement quand les aretes ne sont pas
			// alignees
			if (axe2 != null) {
				arrows.sort(absoluteComparator(axe1, axe2));
			}
		}
	}

	/**
	 * @param axe1 une fleche situee par convention a 0°
	 * @param axe2 une fleche situee en dessous de la premier fleche (dans
	 *             l'intervalle ]-180° 0[)
	 * @return
	 */
	private Comparator<Arrow> absoluteComparator(Arrow axe1, Arrow axe2) {
		return (a1, a2) -> {

			// Positionnement des fleches sur le cercle trigonometrique tel
			// que 0° est donne par l'axe1 : -180° => -2, ]-180° 0°[ => -1, 0°
			// => 0, ]0° 180°[ => 1
			int axe1Pos = Integer.signum(relativeComparator.compare(a1, axe1));
			int axe2Pos = Integer.signum(relativeComparator.compare(a2, axe1));

			// pour le comparateur relatif, 0 peut correspondre a 0° ou a 180°.
			// La comparaison avec le 2eme axe qui est dans le demi-cercle
			// inferieur donne la valeur
			if (axe1Pos == 0 && relativeComparator.compare(a1, axe2) < 0) {
				axe1Pos = -2;
			}

			if (axe2Pos == 0 && relativeComparator.compare(a2, axe2) < 0) {
				axe2Pos = -2;
			}

			if ((axe1Pos == 1 && axe2Pos == 1)
					|| (axe1Pos == -1 && axe2Pos == -1)) {

				// La comparaison relative est valable puisque les 2 fleches
				// sont dans un interval de longueur < 180° (]0° 180°[ ou ]-180°
				// 0°[)
				return relativeComparator.compare(a1, a2);
			}

			// La comparaison des signums suffit ici
			return Integer.compare(axe1Pos, axe2Pos);
		};
	}
}
