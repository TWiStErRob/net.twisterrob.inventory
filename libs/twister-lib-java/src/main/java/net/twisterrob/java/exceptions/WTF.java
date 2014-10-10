package net.twisterrob.java.exceptions;

public class WTF extends RuntimeException {
	private static final long serialVersionUID = 8144983081650945396L;

	public WTF() {
		super();
	}

	public WTF(String message, Throwable cause) {
		super(message, cause);
	}

	public WTF(String message) {
		super(message);
	}

	public WTF(Throwable cause) {
		super(cause);
	}
}
