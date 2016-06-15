package net.twisterrob.android.utils.tostring.stringers.name;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import net.twisterrob.android.utils.tostring.Stringer;
import net.twisterrob.java.utils.StringTools;

public class DefaultNameStringer implements Stringer<Object> {
	public static final Stringer<Object> INSTANCE = new DefaultNameStringer();

	@Override public @NonNull String toString(Object object) {
		if (object == null) {
			return "<null>";
		}
		Class<?> clazz = object.getClass();
		String className = clazz.getSimpleName();
		if (TextUtils.isEmpty(className)) {
			className = clazz.getName();
			if (className != null) {
				className = className.substring(className.lastIndexOf('.'));
				if (className.endsWith(";")) {
					// unknown dimensioned array
					className = className.substring(0, className.length() - 1) + "[?]";
				}
			}
		}
		return className + "@" + StringTools.hashString(object);
	}
}
