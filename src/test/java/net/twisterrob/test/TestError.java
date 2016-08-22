package net.twisterrob.test;

public class TestError extends Error {
	public TestError() {
	}
	public TestError(String message) {
		super(message);
	}
	public TestError(String message, Throwable cause) {
		super(message, cause);
	}
	public TestError(Throwable cause) {
		super(cause);
	}
	public TestError(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
