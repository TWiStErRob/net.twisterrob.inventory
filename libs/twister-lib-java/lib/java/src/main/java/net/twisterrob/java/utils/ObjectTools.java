package net.twisterrob.java.utils;

import java.io.*;

import javax.annotation.*;

public class ObjectTools {

	private ObjectTools() {
		// prevent instantiation
	}

	public static String getFullStackTrace(Throwable t) {
		if (t == null) {
			return null;
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.close();
		return sw.toString();
	}

	public static @Nonnull Throwable getRootCause(@Nonnull Throwable t) {
		while(t.getCause() != null) {
			t = t.getCause();
		}
		return t;
	}

	/**
	 * Null-safe equals.
	 * @see java.util.Objects#equals(Object, Object)
	 */
	public static boolean equals(Object o1, Object o2) {
		//noinspection ConstantConditions o1 is always null at the end, but still call equals with it
		return o1 == o2 || (o1 != null? o1.equals(o2) : o2.equals(o1));
	}

	/**
	 * Assert a non-null object state
	 * @see com.google.common.base.Preconditions#checkNotNull(java.lang.Object)
	 */
	public static @Nonnull <T> T checkNotNull(@Nullable T reference) {
		if (reference == null) {
			throw new NullPointerException();
		}
		return reference;
	}
}
