package net.twisterrob.test;

public class TestCheckedException extends Exception {
	private static final long serialVersionUID = 0L;
	public TestCheckedException() {
		super("test");
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
