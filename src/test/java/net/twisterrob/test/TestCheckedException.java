package net.twisterrob.test;

public class TestCheckedException extends Exception {
	public TestCheckedException() {
	}
	public TestCheckedException(String message) {
		super(message);
	}
	public TestCheckedException(String message, Throwable cause) {
		super(message, cause);
	}
	public TestCheckedException(Throwable cause) {
		super(cause);
	}
	public TestCheckedException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
