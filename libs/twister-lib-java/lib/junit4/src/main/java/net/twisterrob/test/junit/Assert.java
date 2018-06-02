package net.twisterrob.test.junit;

import java.util.concurrent.*;

import org.junit.AssumptionViolatedException;
import org.slf4j.*;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssume.*;

public class Assert {

	private static final Logger LOG = LoggerFactory.getLogger(Assert.class);
	
	public static void assertTimeout(long minimumTime, long maximumTime, TimeUnit timeUnit, Runnable runnable) {
		assumeThat("minimum time", minimumTime, greaterThanOrEqualTo(0L));
		assumeThat("maximum time", maximumTime, greaterThanOrEqualTo(0L));
		assumeThat("timeout needs a proper range", minimumTime, lessThan(maximumTime));
		if (maximumTime < minimumTime) {
			throw new AssumptionViolatedException("Cannot ");
		}
		long start = System.currentTimeMillis();
		assertTimeout(maximumTime, timeUnit, runnable);
		long end = System.currentTimeMillis();
		long elapsed = end - start;
		if (elapsed < minimumTime) {
			throw AssertionError("Execution finished too fast: " + elapsed
					+ ", expected minimum time to execute: " + timeUnit.toMillis(minimumTime), null);
		}
	}

	/**
	 * @see <a href="https://stackoverflow.com/a/19183452/253468">Java: set timeout on a certain block of code?</a>
	 */
	public static void assertTimeout(long timeout, TimeUnit timeUnit, final Runnable runnable) {
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		final Future<?> future = executor.submit(runnable);
		long start = System.currentTimeMillis();
		executor.shutdown(); // This does not cancel the already-scheduled task.
		try {
			future.get(timeout, timeUnit);
			LOG.trace("assertTimeout({}, {}, {}) success in {} ms",
					timeout, timeUnit, runnable, System.currentTimeMillis() - start);
		} catch (TimeoutException ex) {
			long end = System.currentTimeMillis();
			future.cancel(true);
			long elapsed = end - start;
			throw AssertionError("Execution finished too slow: " + elapsed
					+ ", expected maximum time to execute: " + timeUnit.toMillis(timeout), ex);
		} catch (ExecutionException ex) {
			throw AssertionError("Execution failed with exception", ex.getCause());
		} catch (InterruptedException ex) {
			throw AssertionError("Execution was interrupted", ex);
		}
	}

	private static AssertionError AssertionError(String message, Throwable cause) {
		AssertionError wrapper = new AssertionError(message);
		//noinspection UnnecessaryInitCause Android vs. Java 1.7
		wrapper.initCause(cause);
		return wrapper;
	}
}
