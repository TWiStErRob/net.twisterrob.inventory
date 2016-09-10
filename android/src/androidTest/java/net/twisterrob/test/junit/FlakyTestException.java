package net.twisterrob.test.junit;

import org.hamcrest.Matcher;
import org.junit.AssumptionViolatedException;

/** To be thrown when the test can detect itself to be flaky. */
public class FlakyTestException extends AssumptionViolatedException {
	public <T> FlakyTestException(T actual, Matcher<T> matcher) {
		super(actual, matcher);
	}
	public <T> FlakyTestException(String message, T expected, Matcher<T> matcher) {
		super(message, expected, matcher);
	}
	public FlakyTestException(String message) {
		super(message);
	}
	public FlakyTestException(String assumption, Throwable t) {
		super(assumption, t);
	}
}
