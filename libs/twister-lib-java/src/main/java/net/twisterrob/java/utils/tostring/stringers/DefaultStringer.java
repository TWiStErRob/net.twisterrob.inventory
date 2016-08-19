package net.twisterrob.java.utils.tostring.stringers;

import javax.annotation.Nonnull;

import net.twisterrob.java.annotations.DebugHelper;
import net.twisterrob.java.utils.*;
import net.twisterrob.java.utils.tostring.*;

@DebugHelper
public class DefaultStringer extends Stringer<Object> {
	public static final Stringer<Object> INSTANCE = new DefaultStringer();

	@Override public void toString(@Nonnull ToStringAppender append, Object value) {
		if (value == null) {
			append.selfDescribingProperty(StringTools.NULL_STRING);
			return;
		}
		String display = getDisplay(value);
		display = fixType(value, display);
		append.selfDescribingProperty(display);
	}

	protected String getDisplay(Object value) {
		String display;
		if (value.getClass().isArray()) {
			display = ArrayTools.toString(value);
		} else {
			display = shortenPackageNames(value.toString());
		}
		return display;
	}

	protected String fixType(Object value, String display) {
		String type = getType(value);
		if (type != null && type.length() <= display.length() && display.startsWith(type)) {
			display = display.substring(type.length()); // from @ sign or { in case of View
		}
		return display;
	}

	public static String debugType(Object value) {
		if (value == null) {
			return StringTools.NULL_STRING;
		}
		String name = value.getClass().getCanonicalName();
		if (name == null) {
			name = value.getClass().toString();
		}
		return shortenPackageNames(name);
	}

	public static @Nonnull String shortenPackageNames(@Nonnull String string) {
		string = string.replaceAll("^android\\.(?:[a-z0-9]+\\.)+(v4|v7|v13)\\.(?:[a-z0-9]+\\.)+", "$1.");
		string = string.replaceAll("^android\\.(?:[a-z0-9]+\\.)+", "");
		string = string.replaceAll("^javax?\\.(?:[a-z0-9]+\\.)+", "");
		string = string.replaceAll("^com.google.android.gms\\.(?:[a-z0-9]+\\.)+", "gms.");
		string = string.replaceAll("^net\\.twisterrob\\.([a-z0-9.]+\\.)+", "tws.");
		return string;
	}
}
