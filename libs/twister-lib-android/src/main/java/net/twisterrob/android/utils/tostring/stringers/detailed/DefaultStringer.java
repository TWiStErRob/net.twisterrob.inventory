package net.twisterrob.android.utils.tostring.stringers.detailed;

import android.support.annotation.NonNull;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tostring.Stringer;
import net.twisterrob.java.annotations.DebugHelper;
import net.twisterrob.java.utils.ArrayTools;

@DebugHelper
public class DefaultStringer implements Stringer<Object> {
	@Override public @NonNull String toString(Object value) {
		if (value == null) {
			return AndroidTools.NULL;
		}
		String display;
		if (value.getClass().isArray()) {
			display = ArrayTools.toString(value);
		} else {
			display = shortenPackageNames(value.toString());
		}
//		if (type != null && type.length() <= display.length() && display.startsWith(type)) {
//			display = display.substring(type.length()); // from @ sign or { in case of View
//		}
		return display;
	}

	public static String shortenPackageNames(String string) {
		string = string.replaceAll("^android\\.(?:[a-z0-9]+\\.)+(v4|v7|v13)\\.(?:[a-z0-9]+\\.)+", "$1.");
		string = string.replaceAll("^android\\.(?:[a-z0-9]+\\.)+", "");
		string = string.replaceAll("^javax?\\.(?:[a-z0-9]+\\.)+", "");
		string = string.replaceAll("^net\\.twisterrob\\.([a-z0-9.]+\\.)+", "tws.");
		return string;
	}
}
