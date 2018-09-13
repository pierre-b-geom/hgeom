package hgeom.hmesh.test;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Logger;

/**
 *
 * @author Pierre B.
 */
final class RunDuration {

	/**
	 *
	 */
	private static final Logger LOGGER = Logger
			.getLogger(RunDuration.class.getName());

	/**
	 *
	 */
	private final Duration duration;

	/**
	 * @param duration
	 */
	private RunDuration(Duration duration) {
		this.duration = duration;
	}

	/**
	 * @param runnable
	 * @return
	 */
	public static RunDuration of(Runnable runnable) {
		return of(runnable, 1);
	}

	/**
	 * @param runnable
	 * @param num
	 * @return
	 */
	public static RunDuration of(Runnable runnable, int num) {
		Instant start = Instant.now();

		for (int i = 0; i < num; i++) {
			runnable.run();
		}

		Instant end = Instant.now();
		return new RunDuration(Duration.between(start, end).dividedBy(num));
	}

	@Override
	public String toString() {
		long seconds = duration.getSeconds();
		long millis = duration.minusSeconds(seconds).toMillis();
		long nanos = duration.minusMillis(millis).toNanos();
		long micros = Math.floorDiv(nanos, 1000);
		nanos = Math.floorMod(nanos, 1000);

		if (seconds > 0) {
			return seconds + "." + prefixZeros(millis) + " s";
		}

		if (millis > 0) {
			return millis + "." + prefixZeros(micros) + " ms";
		}

		if (micros > 0) {
			return micros + "." + prefixZeros(nanos) + " microseconds";
		}

		return nanos + " nanoseconds";
	}

	/**
	 * @param l
	 * @return
	 */
	private static String prefixZeros(long l) {
		if (l == 0) {
			return "0";
		}

		if (l < 10) {
			return "00" + l;
		}

		return l < 100 ? "0" + l : Long.toString(l);
	}

	/**
	 *
	 */
	public void log() {
		String message = toString();
		LOGGER.info(message);
	}

	/**
	 * @param header
	 */
	public void log(String header) {
		String message = header + ": " + this;
		LOGGER.info(message);
	}

	/**
	 *
	 */
	public void printOut() {
		System.out.println(this); // NOSONAR
	}

	/**
	 * @param header
	 */
	public void printOut(String header) {
		System.out.println(header + ": " + this); // NOSONAR
	}
}
