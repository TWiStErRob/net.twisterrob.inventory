package net.twisterrob.java.exceptions;

import net.twisterrob.java.annotations.DebugHelper;

@DebugHelper
public class StackTrace extends RuntimeException {
	private static final long serialVersionUID = -2617948264981185300L;

	public StackTrace() {
		super();
	}

	public StackTrace(String message) {
		super(message);
	}

	public StackTrace(Throwable cause) {
		super(cause);
	}

	public StackTrace(String message, Throwable cause) {
		super(message, cause);
	}
}
