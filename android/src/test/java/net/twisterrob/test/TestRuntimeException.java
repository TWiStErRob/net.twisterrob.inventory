package net.twisterrob.test;

public class TestRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 0L;
	public TestRuntimeException() {
		super("test");
	}
	public TestRuntimeException(String message) {
		super(message);
	}
	public TestRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
	public TestRuntimeException(Throwable cause) {
		super(cause);
	}
	public TestRuntimeException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
