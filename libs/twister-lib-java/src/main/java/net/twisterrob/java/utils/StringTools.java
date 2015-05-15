package net.twisterrob.java.utils;

import java.io.IOException;
import java.net.*;
import java.util.*;

import javax.annotation.Nullable;

public final class StringTools {
	private StringTools() {
		// prevent instantiation
	}

	public static String format(final String messageFormat, final Object... formatArgs) {
		if (formatArgs == null || formatArgs.length == 0) {
			return messageFormat;
		}
		return String.format(messageFormat, formatArgs);
	}

	public static String join(final Iterable<?> list, final String separator) {
		StringBuilder sb = new StringBuilder();
		Iterator<?> iterator = list.iterator();
		while (iterator.hasNext()) {
			sb.append(iterator.next());
			if (iterator.hasNext()) {
				sb.append(separator);
			}
		}
		return sb.toString();
	}

	public static String[] toStringArray(Object... objectArr) {
		if (objectArr == null) {
			return null;
		}
		String[] stringArr = new String[objectArr.length];
		for (int i = 0; i < stringArr.length; ++i) {
			stringArr[i] = /*objectArr[i] == null? null :*/ String.valueOf(objectArr[i]);
		}
		return stringArr;
	}

	public static URL createUrl(final String type, final String... urls) throws IOException {
		String url = CollectionTools.coalesce(urls);
		if (url != null) {
			try {
				return new URL(url);
			} catch (MalformedURLException ex) {
				throw new IOException(StringTools.format("Cannot associate %s Url: %s", type, url), ex);
			}
		}
		return null;
	}

	/**
	 * Behaves like {@link String#valueOf}, but with a customizable <code>null</code> String.
	 *
	 * @see String#valueOf(Object)
	 * @param nullObj will be used when {@param obj} is <code>null</code>
	 * @return toString of {@param obj} or {@param nullObj} or  <code>null</code> if both  <code>null</code>
	 */
	public static @Nullable String toString(Object obj, Object nullObj) {
		return obj == null? (nullObj == null? null : nullObj.toString()) : obj.toString();
	}

	public static boolean isNullOrEmpty(String string) {
		return string == null || string.isEmpty() || string.trim().isEmpty();
	}

	/** Inverse of {@link Locale#toString()}. */
	public static Locale toLocale(String localeToString) {
		if (localeToString == null) {
			return Locale.ROOT;
		}
		String[] parts = localeToString.split("_");
		switch (parts.length) {
			case 0:
				return Locale.ROOT;
			case 1:
				return new Locale(parts[0]);
			case 2:
				return new Locale(parts[0], parts[1]);
			case 3:
				return new Locale(parts[0], parts[1], parts[2]);
			default:
				throw new IllegalArgumentException("Invalid locale: " + localeToString);
		}
	}

	public static String hashString(Object object) {
		return Integer.toHexString(System.identityHashCode(object));
	}
	public static String partOf(String str, int start, int end) {
		if (str == null) {
			return null;
		}
		if (end < start) {
			int temp = start;
			start = end;
			end = temp;
		}
		if (start < 0) {
			start = 0;
		}
		if (str.length() < end) {
			end = str.length();
		}
		return str.substring(start, end);
	}
}
