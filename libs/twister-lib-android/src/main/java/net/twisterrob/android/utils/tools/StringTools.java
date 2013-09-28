package net.twisterrob.android.utils.tools;

import java.io.IOException;
import java.net.*;
import java.util.Iterator;

public final class StringTools {
	private StringTools() {
		// prevent instantiation
	}

	public static String format(final String messageFormat, final Object... formatArgs) {
		if (formatArgs == null || formatArgs.length == 0) {
			return messageFormat;
		} else {
			return String.format(messageFormat, formatArgs);
		}
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

	public static String toNullString(final Object o, final String nullString) {
		if (nullString == null) {
			return String.valueOf(o);
		} else {
			return o != null? o.toString() : nullString;
		}
	}
}
