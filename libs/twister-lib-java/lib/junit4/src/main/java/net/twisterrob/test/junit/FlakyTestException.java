package net.twisterrob.test.junit;

import org.junit.AssumptionViolatedException;

/** To be thrown when the test can detect itself to be flaky. */
public class FlakyTestException extends AssumptionViolatedException {
	private static final long serialVersionUID = 0L;
	public FlakyTestException(String message) {
		super(message);
	}
	public FlakyTestException(String assumption, Throwable t) {
		super(assumption, t);
	}
}
