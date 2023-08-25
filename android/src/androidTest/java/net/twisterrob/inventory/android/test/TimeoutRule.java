package net.twisterrob.inventory.android.test;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import androidx.annotation.NonNull;

/**
 * A combination of {@link Timeout} and {@link androidx.test.internal.runner.RunnerArgs#ARGUMENT_TIMEOUT}.
 * <p>
 * It supports both a global value (via {@link #TimeoutRule constructor})
 * and an override via {@link org.junit.Test#timeout @Test(timeout=)}.
 *
 * @see androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner#withPotentialTimeout
 * @noinspection JavadocReference
 */
public class TimeoutRule implements TestRule {
	private final long timeout;
	private final @NonNull TimeUnit timeoutUnit;

	public TimeoutRule(long timeout, @NonNull TimeUnit timeoutUnit) {
		this.timeout = timeout;
		this.timeoutUnit = timeoutUnit;
	}

	@Override public Statement apply(Statement base, Description description) {
		long timeout = this.timeout;
		TimeUnit timeoutUnit = this.timeoutUnit;
		Test testAnnotation = description.getAnnotation(Test.class);
		if (testAnnotation != null) {
			timeout = testAnnotation.timeout();
			timeoutUnit = TimeUnit.MILLISECONDS;
		}
		return Timeout
				.builder()
				.withTimeout(timeout, timeoutUnit)
				.build()
				.apply(base, description);
	}
}
