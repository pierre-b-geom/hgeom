package hgeom.hmesh.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Optional;
import java.util.OptionalInt;

import org.junit.Assert;

/**
 *
 * @author Pierre B.
 */
final class Assertions {

	/**
	 *
	 */
	private Assertions() {
	}

	/**
	 * @param opt
	 * @return
	 */
	public static <T> T present(Optional<T> opt) {
		T t = opt.orElse(null);
		assertNotNull(t);
		return t;
	}

	/**
	 * @param opt
	 * @return
	 */
	public static int present(OptionalInt opt) {
		if (opt.isPresent()) {
			return opt.getAsInt();
		}

		fail();
		return 0;
	}

	/**
	 * @param opt
	 */
	public static <T> void notPresent(Optional<T> opt) {
		Assert.assertFalse(opt.isPresent());
	}
}
