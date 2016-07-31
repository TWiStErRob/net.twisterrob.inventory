package net.twisterrob.java.utils;

import java.io.*;

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

	/**
	 * Null-safe equals.
	 * @see java.util.Objects#equals(Object, Object)
	 */
	public static boolean equals(Object o1, Object o2) {
		//noinspection ConstantConditions o1 is always null at the end, but still call equals with it
		return o1 == o2 || (o1 != null? o1.equals(o2) : o2.equals(o1));
	}
}
